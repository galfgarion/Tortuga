package test.naiveheapfile;

import java.io.IOException;
import java.util.Vector;

import neustore.base.DBPage;
import neustore.base.ByteArray;

/**
 * A DBPage as a component of a naive version of the heap file. 
 * This naive heap file is not meant to be used. Rather, it only 
 * shows an example of using the NEU Storage Package.
 * 
 * @see NaiveHeapFile
 * @author Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
 */
public class NaiveHeapFilePage extends DBPage {
	private Vector<Integer> records;
	
	public NaiveHeapFilePage( int _pageSize ) {
		super(1, _pageSize);
		records = new Vector<Integer>();
	}
	
	/**
	 * Returns the number of records in this page.
	 * @return   the number of records 
	 */
	public int numRecs() { return records.size(); }
	
	/**
	 * Inserts a new integer into this page.
	 * @param key  the new integer
	 */
	public void insert( int key ) {
		records.add( new Integer(key) );
	}
	
	/**
	 * Searches for an integer in this page.
	 * @param key   the integer to search for
	 * @return whether found in this page
	 */
	public boolean search( int key ) {
		for ( int i=0; i<records.size(); i++ ) {
			Integer e = (Integer)records.elementAt(i);
			if (e.intValue() == key) return true;
		}
		return false;
	}
	
	protected void read( byte[] b ) throws IOException {
		ByteArray ba = new ByteArray( b, ByteArray.READ );
		ba.readInt();  // nodeType, ignore
		int numRecs = ba.readInt();
		records.removeAllElements();
		for ( int i=0; i<numRecs; i++ ) {
			int key = ba.readInt();
			records.add(new Integer(key));
		}
	}
	
	protected void write( byte[] b ) throws IOException {
		ByteArray ba = new ByteArray( b, ByteArray.WRITE );
		int numRecs = records.size();
		ba.writeInt( nodeType );
		ba.writeInt( numRecs );
		for ( int i=0; i<numRecs; i++ ) {
			Integer key = (Integer)records.elementAt(i);
			ba.writeInt( key.intValue() );
		}
	}
}