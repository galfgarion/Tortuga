package neustore.heapfile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import neustore.base.DBPage;
import neustore.base.Data;
import neustore.base.Key;
import neustore.base.KeyData;

/**
 * A memory-version page of a heap file.
 * 
 * @see HeapFile
 * @author Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
 */
public class HeapFilePage extends DBPage {
	/**
	 * size for the reserved space.
	 * The first 20 bytes of the disk-version of the HeapFilePage are reserved for:
	 * nodeType=1, prev, next, availableBytes, numRecs.
	 */
	protected final int RESERVED = 20;
	
	/**
	 * sample key used in readPage
	 */
	private Key sampleKey;
	
	/**
	 * sample data used in readPage
	 */
	private Data sampleData;
	
	/**
	 * the list of records
	 */
	protected Vector<KeyData> records;
	
	/**
	 * the next page in the linked list.
	 * -1 if not exist.
	 * Whether this page is in the full-page list or the nonfull-page list
	 * depends on whether this page is full.
	 */
	protected int next;
	
	/**
	 * the previous page in the linked list.
	 * -1 if not exist.
	 * Whether this page is in the full-page list or the nonfull-page list
	 * depends on whether this page is full.
	 */
	protected int prev;
	
	/**
	 * available space in the page
	 */
	protected int availableBytes;
		
	/**
	 * Constructor fo HeapFilePage. Similar to the constructor of {@link HeapFile}, 
	 * here a sample key and a sample data are taken to enable the generic feature
	 * of the index.
	 * 
	 * @param _pageSize     page size
	 * @param _sampleKey    a sample key
	 * @param _sampleData   a sample data
	 */
	public HeapFilePage( int _pageSize, Key _sampleKey, Data _sampleData) {
		super(1, _pageSize);  // nodeType=1 for all heap file pages
		records = new Vector<KeyData>();
		prev = next = -1;
		availableBytes = pageSize - RESERVED;
		sampleKey = _sampleKey;
		sampleData = _sampleData;
	}
	
	/**
	 * Returns the number of records in the page.
	 * @return number of records
	 */
	public int numRecs() { 
		return records.size();
	}
	
	/**
	 * Returns the i'th element in the page.
	 * @param i	index from 0 to numRecs()-1 
	 * @return	the i'th element
	 */
	public KeyData get(int i) {
		if (i<0 || i>=records.size()) return null;
		return records.elementAt(i);
	}
	
	/**
	 * Whether the page is full.
	 * 
	 * @return  whether full
	 */
	public boolean isFull() {
		if ( records.size() == 0 ) return false;
		return availableBytes < sampleKey.maxSize()+sampleData.maxSize();
	}

	/**
	 * Inserts a new record into this page. The caller needs to make sure that
	 * the page has enough space.
	 * @param key   the key part of the new record
	 * @param data  the data part of the new record
	 */
	public void insert(Key key, Data data) {
		KeyData rec = new KeyData(key, data);
		records.add(rec);
		availableBytes -= key.size()+data.size();
		assert availableBytes >= 0;
	}
	
	/**
	 * Searches for a record in the page.
	 * @param   key   the key to search for
	 * @return  the data if found; null otherwise.
	 */
	public Data search( Key key ) {
		for ( int i=0; i<records.size(); i++ ) {
			KeyData rec = (KeyData)records.elementAt(i);
			if ( rec.key.equals(key) ) return rec.data;
		}
		return null;
	}
	
	/**
	 * Deletes a record from the page.
	 * It is the caller's responsibility to free an empty page.
	 * @param   key  the key to delete
	 * @return  whether deleted
	 */
	public boolean delete( Key key ) {
		for ( int i=0; i<records.size(); i++ ) {
			KeyData rec = (KeyData)records.elementAt(i);
			if ( rec.key.equals(key) ) {
				records.removeElementAt(i);
				availableBytes += rec.key.size() + rec.data.size();
				return true;
			}
		}
		return false;
	}
	
	protected void read(byte[] page) throws IOException {
		ByteArrayInputStream byte_in = new ByteArrayInputStream(page);
		DataInputStream in = new DataInputStream(byte_in);
		in.readInt();  // skip nodeType
		prev = in.readInt();
		next = in.readInt();
		availableBytes = in.readInt();
		int numRecs = in.readInt();
		
		records.removeAllElements();
		for ( int i=0; i<numRecs; i++ ) {
			KeyData rec = new KeyData(
					(Key)sampleKey.clone(), (Data)sampleData.clone());
			rec.key.read(in);
			rec.data.read(in);
			records.add( rec );
		}
		
		in.close();
		byte_in.close();
	}

	protected void write(byte[] page) throws IOException {
		ByteArrayOutputStream byte_out = new ByteArrayOutputStream(pageSize);
		DataOutputStream out = new DataOutputStream(byte_out);
		out.writeInt( 1 );
		out.writeInt( prev );
		out.writeInt( next );
		out.writeInt( availableBytes );
		out.writeInt( records.size() );
		
		for ( int i=0; i<records.size(); i++ ) {
			KeyData rec = (KeyData)records.elementAt(i);
			rec.key.write(out);
			rec.data.write(out);
		}
		
		byte[] result = byte_out.toByteArray();
		System.arraycopy(result, 0, page, 0, result.length );
		out.close();
		byte_out.close();
	}

	/**
	 * A record which consists of a key and a data.
	 * @author Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
	class KeyData {
		public KeyData( Key _key, Data _data) {
			key = _key;
			data = _data;
		}
		public Key key;
		public Data data;
	}
	 */

}