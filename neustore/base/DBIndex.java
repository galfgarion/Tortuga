package neustore.base;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import neustore.base.ByteArray;

/**
 * Abstract base class for a disk-based, paginated and buffered index.
 * The index reads and writes {@link DBPage} using a {@link DBBuffer}. 
 * <p>
 * <b>Usage:</b><br>
 * <ul>
 * <li>The constructor of DBIndex takes a DBBuffer object. That means the caller
 * who wishes to generate a DBIndex object should generate a DBBuffer object first.
 * <li>An application should define a derived class like <code>HeapFile</code>, by
 * implementing the three abstract functions that manage the <i>head information</i> (to
 * be explained).
 * <li>The file can be viewed as a consecutive list of disk pages, each of size
 * pageSize. See the figure below:<br>
 * <pre>
 * +------+-------+-------+-------+-------+-------+-------+
 * | head | page1 | page2 | page3 | page4 | page5 | page6 |
 * +------+-------+-------+-------+-------+-------+-------+
 * </pre>
 * <li>
 * The head page (having pageID=0) uses the first OVERHEAD=16 bytes to store 
 * four integers:<br>
 *   <ul>
 *   <li><b>-1</b>
 *   <li><b>pagesize</b>
 *   <li><b>numOfPages</b>: number of non-empty pages
 *   <li><b>free</b>: pageID of the first empty page
 *   </ul>
 * The rest pageSize - OVERHEAD bytes stores the user-specified head information.
 * For instance, a tree index may store the pageID for the root page here. To manage
 * the head information, a derived class needs to implement the three abstract functions
 *   <ul>
 *   <li><code>initIndexHead</code>: called when the index is built;
 *   <li><code>readIndexHead</code>: called when the index is opened;
 *   <li><code>writeIndexHead</code>: called right before the index is closed.
 *   </ul>
 * <li>
 * Besides the constructor and the <code>close()</code> function, typical usages 
 * of the <code>DBIndex</code> are: 
 *   <ul>
 *   <li><code>allocate</code>: to get a new pageID;
 *   <li><code>freePage</code>: to declare a pageID to be empty;
 *   <li>The public functions in the buffer, e.g. <code>buffer.readPage</code>, 
 *       <code>buffer.writePage</code>.
 *   </ul>
 * <li> One implementation detail is that
 * all the empty pages in the file are linked together in 
 * a linked list. An empty page contains two integers: 0 and pageID for the
 * next empty page. If it is the last empty page in the file,
 * the next pointer is set to -1. A class called DBEmptyPage is defined for this purpose.
 * But all these are hidden from the user.
 * </ul>
 * 
 * @see DBPage
 * @see DBBuffer
 * @author Tian Xia &lt;tianxia@ccs.neu.edu&gt;<br>Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
 */
public abstract class DBIndex {
	public static final boolean CREATE = true;
	public static final boolean OPEN = false;
	
	/**
	 * Creates or Opens an index file.
	 *
	 * @param buffer	the buffer manager
	 * @param filename  the filename
	 * @param isCreate    whether create (or open)
	 * @throws IOException
	 */
	public DBIndex(DBBuffer buffer, String filename, boolean isCreate) throws IOException {
		this.buffer = buffer;
		pageSize = buffer.pageSize;
		
		if ( isCreate ) {
			File f = new File(filename);
			if (f.exists()) {
				System.out.println("File exists! Can not create new file!"); System.exit(0);
			}
			file = new RandomAccessFile (f, "rw");

			numPages = 0;
			firstEmpty = -1;
			initIndexHead();
		}
		else {
			File f = new File(filename);
			if (!f.exists()) {
				System.out.println("File not exists!"); System.exit(0); 
			}
			file = new RandomAccessFile(f, "rw");
			if (file.readInt() != -1) {
				System.out.println("Illegal file opened!"); System.exit(0);
			}
			pageSize = file.readInt();
			file.seek(0);

			DBBufferReturnElement ret = buffer.readPage(file, 0);
			byte[] headPage = (byte[]) ret.object;
			ByteArray ba = new ByteArray(headPage, ByteArray.READ);				
			ba.readInt(); // skip -1.
			ba.readInt(); // skip pageSize
			numPages = ba.readInt();
			firstEmpty = ba.readInt();
			byte[] indexHead = new byte[pageSize-OVERHEAD];
			ba.read(indexHead);
			readIndexHead(indexHead);
		}
	}

	/**
	 * The size of a page.
	 */
	protected int pageSize;
	/**
	 * The number of non-empty pages (excluding the head page).
	 */
	protected int numPages;
	/**
	 * The first empty page ID.
	 * It is -1 if there is no empty page in the file.
	 */
	protected int firstEmpty;
	/**
	 * The buffer manager.
	 */
	public DBBuffer buffer;
	/**
	 * The length of the system information in bytes.
	 */
	protected static final int OVERHEAD = 16;
	/**
	 * The random access file.
	 */
	protected RandomAccessFile file;
	
	/**
	 * Assigns a new pageID.
	 * 
	 * @return the new page ID.
	 */
	public int allocate () {
		numPages++;
		if (firstEmpty == -1) return numPages;
		int newID = firstEmpty;
		
		// get new firstEmpty
		DBBufferReturnElement ret = null;
		try {
			ret = buffer.readPage(file, firstEmpty);
		} catch (IOException e1) {}
		assert ret.nodeType == 0 ;
		byte[] theEmptyPage = null;
		if ( ret.parsed ) {
			theEmptyPage = new byte[pageSize];
			try {
				((DBPage)ret.object).write(theEmptyPage);
			} catch (IOException e) {}
		}
		else {
			theEmptyPage = (byte[])ret.object;
		}
		try {
			ByteArray ba = new ByteArray( theEmptyPage, true );
			ba.readInt();
			firstEmpty = ba.readInt();
		} catch (IOException e) {}

		return newID;
	}

	/**
	 * Frees an empty page.
	 * 
	 * @param pageID   the to-be-freed page ID
	 * @throws IOException
	 */
	public void freePage (int pageID) throws IOException {
		DBEmptyPage empty = new DBEmptyPage( pageSize, firstEmpty );
		buffer.writePage(file, pageID, empty);
		firstEmpty = pageID;
		numPages--;
	}

	/**
	 * Saves the header page to disk and flushes the buffer.
	 * It is important to call <code>close()</code> before exiting. 
	 * Otherwise the file may be corrupted.
	 * 
	 * @throws IOException
	 */
	public void close () throws IOException {
		byte[] indexHead = new byte[pageSize-OVERHEAD];
		writeIndexHead(indexHead);
		
		ByteArrayOutputStream byte_out = new ByteArrayOutputStream(pageSize);
		DataOutputStream out = new DataOutputStream(byte_out);
		
		out.writeInt(-1);
		out.writeInt(pageSize);
		out.writeInt(numPages);
		out.writeInt(firstEmpty);
		out.write(indexHead);
		
		DBByteArrayPage header = new DBByteArrayPage( pageSize, byte_out.toByteArray());
		out.close();
		byte_out.close();
		buffer.writePage( file, 0, header );
		
		buffer.flush(file);
		
		file.close();
		file = null;
	}
	
	/* *********************** *
	 *    abstract methods     *
	 * *********************** */
		
	/**
	 * Reads index head information from a byte array.
	 * Called when opening a DBIndex.
	 * @param indexHead   the byte array of length pageSize-OVERHEAD
	 */
	protected abstract void readIndexHead (byte[] indexHead);
	
	/**
	 * Writes index head information to a byte array.
	 * Called before closing a DBIndex.
	 * @param indexHead   the byte array of length pageSize-OVERHEAD
	 */
	protected abstract void writeIndexHead (byte[] indexHead);
	
	/**
	 * Initializes index head information.
	 * Called when creating a DBIndex.
	 */
	protected abstract void initIndexHead();
}
