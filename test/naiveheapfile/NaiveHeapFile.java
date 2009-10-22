package test.naiveheapfile;

import java.io.IOException;

import neustore.base.DBBuffer;
import neustore.base.DBBufferReturnElement;
import neustore.base.DBIndex;
import neustore.base.ByteArray;

/**
 * A naive version of the heap file. It stores a list of integers. 
 * The only update is to append an integer at the end of the file.
 * The only search is to tell whether an integer exists in the file or not.
 * This file is not meant to be used. Rather, it shows an example of using
 * the NEU Storage Package.
 * 
 * @see NaiveHeapFilePage 
 * @author Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
 */
public class NaiveHeapFile extends DBIndex {
	/**
	 * maximum number of records in a page
	 */
	private int pageCapacity;
	/**
	 * pageID of the last page
	 */
	private int lastPageID;
	/**
	 * the last page
	 */
	private NaiveHeapFilePage lastPage;
	
	public NaiveHeapFile(DBBuffer _buffer, String filename, boolean isCreate) throws IOException {
		super(_buffer, filename, isCreate);
		pageCapacity = (pageSize-8)/4;
		if(isCreate)
			lastPage = new NaiveHeapFilePage(pageSize);
		else
			lastPage = myReadPage(lastPageID);
	}
	
	protected void initIndexHead() {
		lastPageID = allocate();
	}
	
	/**
     * Reads a page from buffer and parse it if not parsed already.
	 * 
	 * @param   pageID
	 * @return  the parsed page
	 * @throws IOException
	 */
	protected NaiveHeapFilePage myReadPage( int pageID ) throws IOException {
		NaiveHeapFilePage page;
		DBBufferReturnElement ret = buffer.readPage(file, pageID);
		if ( ret.parsed ) {
			page = (NaiveHeapFilePage)ret.object;
		}
		else {
			page = new NaiveHeapFilePage(pageSize);
			page.read((byte[])ret.object);
		}		
		return page;
	}
	
	protected void readIndexHead(byte[] head) {
		ByteArray ba = new ByteArray(head, ByteArray.READ);
		try {
			lastPageID = ba.readInt();
		} catch (IOException e) {}
	}
	
	protected void writeIndexHead(byte[] head) {
		ByteArray ba = new ByteArray(head, ByteArray.WRITE);
		try {
			ba.writeInt(lastPageID);
		} catch (IOException e) {}
	}
	
	/**
	 * Appends a new integer to the end of the file.
	 * @param key  the integer to be appended
	 * @throws IOException
	 */
	public void insert( int key ) throws IOException {
		if ( lastPage.numRecs() >= pageCapacity ) {
			lastPageID = allocate();
			lastPage = new NaiveHeapFilePage(pageSize);
		}
		lastPage.insert( key );
		buffer.writePage(file, lastPageID, lastPage);
	}
	
	/**
	 * Searches for an integer.
	 * @param key   the integer to search for
	 * @return      whether found
	 * @throws IOException
	 */
	public boolean search( int key ) throws IOException {
		for ( int currentPageID=1; currentPageID<=lastPageID; currentPageID++ ) {
			NaiveHeapFilePage currentPage = myReadPage(currentPageID);
			if ( currentPage.search(key) ) return true;
		}
		return false;
	}
}