package neustore.base;

import java.io.IOException;

import neustore.base.ByteArray;

/**
 * Abstract base class for a memory-version disk page.
 * For example, a derived class can be <code>BTreeIndexPage</code>.
 * A derived class should implement the functions to <code>read</code> from
 * a byte array, and to <code>write</code> to a byte array. 
 * A convention is that in the byte array, the first integer should be nodeType.
 * 
 * @author Tian Xia &lt;tianxia@ccs.neu.edu&gt;<br>Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
 */
public abstract class DBPage {
	
	/**
	 * The node type. -1 means a byte array page.
	 * 0 means an empty page. 
	 * Users can define their own node types.
	 */
	public int nodeType;
	
	/**
	 * The page size.
	 */
	public int pageSize;
	
	/**
	 * Creates a DBPage.
	 * @param _nodeType node type
	 * @param _pageSize page size
	 */
	public DBPage( int _nodeType, int _pageSize ) {
		nodeType = _nodeType;
		pageSize = _pageSize;
	}
	
	/**
	 * Reads the object from a byte array.
	 * 
	 * @param page
	 * @throws IOException
	 */
	protected abstract void read(byte[] page) throws IOException;

	/**
	 * Writes the object to a byte array.
	 * 
	 * @param page
	 * @throws IOException
	 */
	protected abstract void write(byte[] page) throws IOException;
}

/**
 * An empty page.
 * All empty pages are linked together.
 * @author Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
 */
class DBEmptyPage extends DBPage {
	/**
	 * pageID of the next empty page; -1 if none.
	 */
	public int nextEmptyPage;
	
	/**
	 * Constructor of <code>DBEmptyPage</code>.
	 * @param _pageSize      page size
	 * @param _nextEmptyPage the next empty page
	 */
	public DBEmptyPage( int _pageSize, int _nextEmptyPage ) {
		super( 0, _pageSize );
		nextEmptyPage = _nextEmptyPage;
	}
	
	/**
	 * Reads the second integer from the beginning of the page.
	 * This is the pointer to the next empty page.
	 */
	protected void read( byte[] page ) throws IOException {
		ByteArray ba = new ByteArray( page, ByteArray.READ );
		ba.readInt( );
		nextEmptyPage = ba.readInt();
	}

	/**
	 * Write two integers to the beginning of the page.
	 * The first integer is 0 standing for empty page.
	 * The second integer is a pointer to the next empty page.
	 * Note that this is the last empty page, the pointer is -1. 
	 */
	protected void write( byte[] page ) throws IOException {
		ByteArray ba = new ByteArray( page, ByteArray.WRITE );
		ba.writeInt( 0 );
		ba.writeInt( nextEmptyPage );
	}
}

/**
 * A <code>DBPage</code> which contains a byte array.  
 * An example usage of <code>DBByteArrayPage</code> is to use it as the header page
 * of the file. It is crucial that before calling {@link DBBuffer#writePage}
 * to write a <code>DBByteArrayPage</code> to buffer,
 * the application writes its header information to its stored
 * byte array: <code>content</code>.
 * 
 * @author Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
 */
class DBByteArrayPage extends DBPage {
	/**
	 * the stored byte array
	 */
	protected byte[] content;
	/**
	 * Constructor.
	 * @param _pageSize page size
	 * @param page      the source byte[] to be copied to <code>content</code>
	 */
	public DBByteArrayPage(int _pageSize, byte[] page) {
		super( -1, _pageSize);
		content = new byte[_pageSize];
		System.arraycopy(page, 0, content, 0, pageSize);
	}

	protected void read( byte[] page ) {
		System.arraycopy(page, 0, content, 0, pageSize );
	}

	protected void write( byte[] page ) {
		System.arraycopy(content, 0, page, 0, pageSize );
	}
}