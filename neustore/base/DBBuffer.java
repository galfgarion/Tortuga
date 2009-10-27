package neustore.base;

import java.io.IOException;
import java.io.RandomAccessFile;

import neustore.base.ByteArray;

/**
 * Abstract base class for a buffer manager. 
 * It helps index structures (derived from {@link DBIndex}) manage disk pages.
 * <p>
 * <b>Usage:</b><br>
 * <ul>
 * <li> An application can choose to use a provided sub-class like LRUBuffer.
 * <li> If the user chooses to define their own buffer management, they should
 * derive a class from <code>DBBuffer</code> by providing methods <code>add</code>, 
 * <code>find</code> and <code>flush</code>.
 * <li> The user typically calls {@link #readPage} and {@link #writePage}
 * to read/write a disk page. Here <code>readPage</code> returns an object of class
 * <code>DBBufferReturnedElement</code>. Besides the page itself, the returned element also
 * specifies the nodeType and whether the node was parsed. If parsed, the returned
 * object is of class DBPage.  Otherwise, the returned object is a byte array and
 * it's up to the application to generate a DBPage object. Moreover, nodeType=-1 and
 * 0 are reserved (for DBByteArrayPage and DBEmptyPage). The application can explain
 * the other values. For example, a B+-tree may cast a nodeType=1 node to an index
 * node.
 * <li> The user can {@link #pin} or {@link #unpin} a page.
 * <li> The user can call {@link #getIOs} to get statistics, or call {@link #clearIOs}
 * to initialize the counters.
 * </ul>
 * 
 * @see DBIndex
 * @see DBPage
 * @author Tian Xia &lt;tianxia@ccs.neu.edu&gt;<br>Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
 */
public abstract class DBBuffer {
	
	/**
	 * Opens an existing Database file.
	 *  
	 * @param bufferSize	total number of buffer pages
	 * @param pageSize		page size
	 */
	public DBBuffer ( int bufferSize, int pageSize ) {
		this.bufferSize = bufferSize;
		this.pageSize = pageSize;
		clearIOs();
	}

	/**
	 * Reports the disk/buffer I/Os. There are four I/Os reported:
	 * <i>disk-read</i>, <i>disk-write</i>, <i>buffer-read</i> and
	 * <i>buffer-write</i>. They are stored in the integer array in
	 * the above order.
	 * 
	 * @return  the four I/Os
	 */
	public int[] getIOs() {
		int[] io = new int[4];
		io[0] += diskReadIO;
		io[1] += diskWriteIO;
		io[2] += bufferReadIO;
		io[3] += bufferWriteIO;
		return io;
	}

	/**
	 * Loads a page (the byte array) from disk.
	 * Increases <code>diskReadIO</code> by one.
	 * 
	 * @param file	the file
	 * @param page    the byte array of length pageSize
	 * @param pageID  the page ID
	 * @throws IOException
	 */
	protected void load (RandomAccessFile file, byte[] page, int pageID) throws IOException {
		file.seek(pageSize*pageID);
		int len = file.read(page);
		if ( len != pageSize ) {
			System.out.println("Load a page whose length is not page size!");
			System.exit(0);
		}
		diskReadIO++;
	}
	
	/**
	 * Saves a page (the byte array) to the disk.
	 * Increases <code>diskWriteIO</code> by one.
	 * 
	 * @param page   the byte array of length pageSize
	 * @param pageID the page ID
	 * @throws IOException
	 */
	protected void save (RandomAccessFile file, byte[] page, int pageID) throws IOException {
		if ( page.length != pageSize ) {
			System.out.println( "Plans to save a page whose size is not pageSize!" );
			System.exit(0);
		}
		diskWriteIO++;
		file.seek(pageSize*pageID);
		file.write(page);
	}
		
	/**
	 * Resets the four counters <code>bufferReadIO</code>, etc.
	 */
	public void clearIOs(){
		bufferReadIO = 0;
		bufferWriteIO = 0;
		diskReadIO = 0;
		diskWriteIO = 0;
	}
	
	/**
	 * Returns a buffered page. If it is in buffer, returns it. Otherwise,
	 * loads it from disk and then returns it. Increases <code>bufferReadIO</code>.
	 * 
	 * @param file		which file
	 * @param pageID   page ID
	 * @return the buffered page 
	 * @throws IOException
	 */
	public DBBufferReturnElement readPage(RandomAccessFile file, int pageID) throws IOException {
		bufferReadIO ++;
		
		DBBufferStoredElement stored = find(file, pageID);
		if ( stored == null ) {
			byte[] page = new byte[pageSize];
			load(file, page, pageID);
			stored = new DBBufferStoredElement();
			stored.object = page;
			stored.parsed = false;
			ByteArray ba = new ByteArray(page, true);
			stored.nodeType = ba.readInt();
			stored.pageID = pageID;
			stored.file = file;
			add( stored );
		}	
		DBBufferReturnElement ret = new DBBufferReturnElement(
				stored.nodeType, stored.object, stored.parsed);
		return ret;
	}
	
	/**
	 * Writes a DBPage to buffer.
	 * 
	 * @param file		the file
	 * @param pageID  the page ID
	 * @param dbpage  the DBPage object
	 * @throws IOException
	 */
	public void writePage( RandomAccessFile file, int pageID, DBPage dbpage ) throws IOException {
		bufferWriteIO ++;
		
		DBBufferStoredElement stored = find( file, pageID );
		if ( stored == null ) {
			stored = new DBBufferStoredElement();
			stored.object = dbpage;
			stored.parsed = true;
			stored.dirty = true;
			stored.nodeType = dbpage.nodeType;
			stored.pageID = pageID;
			stored.file = file;
			add( stored );
		}
		else {
			stored.object = dbpage;
			stored.parsed = true;
			stored.dirty = true;
			stored.nodeType = dbpage.nodeType;
		}
	}
	
	/**
	 * Pins a page. 
	 * Such pages can not be swapped out of the buffer. The pinCount 
	 * in DBBufferStoredElement is increased by 1.
	 * 
	 * @param file		the file
	 * @param pageID   the page ID of the page to be pinned
	 */
	public void pin (RandomAccessFile file, int pageID) {
		DBBufferStoredElement stored = find( file, pageID );
		if ( stored != null ) {
			stored.pinCount ++;
		}
	}

	/**
	 * Unpins a page. 
	 * The pinCount in DBBufferStoredElement is decreased by 1.
	 * 
	 * @param file		the file
	 * @param pageID   the page ID of the page to be unpinned
	 */
	public void unpin (RandomAccessFile file, int pageID) {
		DBBufferStoredElement stored = find( file, pageID );
		if ( stored != null && stored.pinCount > 0 ) {
			stored.pinCount --;
		}
	}
	
	/**
	 * number of read IOs on the buffer
	 */
	protected int bufferReadIO;
	/**
	 * number of write IOs on the buffer
	 */
	protected int bufferWriteIO;
	/**
	 * number of read IOs on the disk
	 */
	protected int diskReadIO;
	/**
	 * number of write IOs on the disk
	 */
	protected int diskWriteIO;	
	/**
	 * how many pages the buffer has
	 */
	protected int bufferSize;
	/**
	 * page size
	 */
	protected int pageSize;

	/* *********************** *
	 *    abstract methods     *
	 * *********************** */

	/**
	 * Finds a page in buffer.
	 * Note: do not increase bufferReadIO.
	 * 
	 * @param file		the file
	 * @param pageID    the page ID
	 * @return an instance of DBBufferStoredElement if found; <i>null</i>otherwise.
	 */
	protected abstract DBBufferStoredElement find(RandomAccessFile file, int pageID);
	
	/**
	 * Adds a page to the buffer.
	 * Note: do not increase bufferReadIO.
	 * 
	 * @param stored   the stored element
	 * @throws IOException
	 */
	protected abstract void add( DBBufferStoredElement stored ) throws IOException;
	
	/**
	 * Flushes the buffer pages corresponding to a give file. 
	 * Makes the buffer empty, while saving modified pages to disk.
	 * 
	 * @param file	the file
	 * @throws IOException
	 */
	protected abstract void flush (RandomAccessFile file) throws IOException;	
	
	/**
	 * An element stored in {@link DBBuffer}.
	 * The users do not need to know this class, unless they want to design a
	 * new buffer management class.
	 *  
	 * @see DBBuffer
	 * @author Tian Xia &lt;tianxia@ccs.neu.edu&gt;<br>Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
	 */
	class DBBufferStoredElement {
		/**
		 * pin count
		 */
		protected int pinCount;
		/**
		 * whether dirty
		 */
		protected boolean dirty;
		/**
		 * which file
		 */
		protected RandomAccessFile file;
		/**
		 * page ID
		 */
		protected int pageID;
		/**
		 * stored object.
		 * If parsed, it is a DBPage; otherwise it is a byte[].
		 */
		protected Object object;
		/**
		 * whether the page has been parsed by the application
		 */
		protected boolean parsed;
		/**
		 * type of node.
		 * -1 means DBByteArrayPage. 0 means DBEmptyPage. 
		 * Other values should be explained by the application.
		 */
		protected int nodeType;
		
		public DBBufferStoredElement() {
			pinCount = 0;
			dirty = false;
			file = null;
			pageID = 0;
			object = null;
			parsed = false;
			nodeType = 0;
		}
	}
	
	/**
	 * A class that represents on entry in the buffer.
	 * It is composed of a file and a pageID.
	 *  
	 * @see DBBuffer
	 * @see LRUBuffer
	 * @author Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
	 */
	class DBBufferHashKey {
		protected RandomAccessFile file;
		protected int pageID;
		
		public DBBufferHashKey( RandomAccessFile file, int pageID ) {
			this.file = file;
			this.pageID = pageID;
		}
		
		public int hashCode() {
			return file.hashCode()+(new Integer(pageID)).hashCode();
		}
		
		public boolean equals(Object obj) {
			DBBufferHashKey o = (DBBufferHashKey)obj;
			return file==o.file && pageID==o.pageID;
		}
	}
}
