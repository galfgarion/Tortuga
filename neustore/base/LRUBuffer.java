package neustore.base;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Vector;

/**
 * The LRU buffer.
 * 
 * @author Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
 */
public class LRUBuffer extends DBBuffer {
	protected Vector<DBBufferStoredElement> buffer;
	protected HashMap<DBBufferHashKey, DBBufferStoredElement> hash;
	
	public LRUBuffer(int bufferSize, int pageSize) {
		super(bufferSize, pageSize);
		buffer = new Vector<DBBufferStoredElement>();
		hash = new HashMap<DBBufferHashKey, DBBufferStoredElement>();
	}

	/**
	 * Finds a page in buffer.
	 * If found, move to the beginning of buffer.
	 * Note: do not increase bufferReadIO.
	 * 
	 * @param pageID    the page ID
	 * @return an instance of DBBufferStoredElement if found; <i>null</i>otherwise.
	 */
	protected DBBufferStoredElement find(RandomAccessFile file, int pageID) {
		DBBufferStoredElement e = hash.get(new DBBufferHashKey(file, pageID));
		if (e==null) return null;  // not found
		buffer.remove(e);
		buffer.add(0,e);  // move to top of array
		return e;
	}
	
	/**
	 * Adds a page to the buffer.
	 * Note: do not increase bufferReadIO.
	 * The element should not exist in the buffer already.
	 * 
	 * @param stored   the stored element
	 */
	protected void add( DBBufferStoredElement stored ) throws IOException{
		if ( buffer.size() >= bufferSize ) {
			// find a page to switch out
			int i = buffer.size()-1;
			DBBufferStoredElement s = null;
			assert i>=0;
			while ( i >= 0 ) {
				s = (DBBufferStoredElement)buffer.get(i);
				if ( s.pinCount == 0 ) break;
				i--;
			}
			assert i>=0 : "Buffer full! No page to switch out!";
			buffer.remove(i);
			hash.remove(new DBBufferHashKey(s.file, s.pageID));
			
			// write to disk if dirty
			if ( s.dirty ) {
				byte[] page = new byte[pageSize];
				if ( s.parsed ) {
					((DBPage)s.object).write( page );
				}
				else {
					System.arraycopy((byte[])s.object, 0, page, 0, pageSize );
				}
				save(s.file, page, s.pageID);				
			}
		}
		buffer.add( 0, stored );
		hash.put(new DBBufferHashKey(stored.file, stored.pageID), stored);
	}
	
	/**
	 * Flushes the buffer pages of a given file. 
	 * Makes the buffer empty, while saving modified pages to disk.
	 * 
	 * @throws IOException
	 */
	protected void flush (RandomAccessFile file) throws IOException {
		byte[] page = new byte[pageSize];
		int i = 0;
		while ( i < buffer.size() ) {
			DBBufferStoredElement stored = (DBBufferStoredElement)buffer.elementAt(i);
			if ( stored.file == file ) {
				buffer.remove(i);
				hash.remove(new DBBufferHashKey(stored.file, stored.pageID));
				if ( stored.dirty ) {
					if ( stored.parsed ) {
						((DBPage)stored.object).write( page );
					}
					else {
						System.arraycopy((byte[])stored.object, 0, page, 0, pageSize );
					}
					save(stored.file, page, stored.pageID);
				}
			}
			else {
				i ++ ;
			}
		}
	}
	
}
