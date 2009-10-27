package neustore.heapfile;

import java.io.IOException;
import java.util.Enumeration;

import neustore.base.DBBuffer;
import neustore.base.DBBufferReturnElement;
import neustore.base.DBIndex;
import neustore.base.ByteArray;
import neustore.base.Data;
import neustore.base.Key;
import neustore.base.KeyData;

/**
 * A generic heap file that stores a list of (key, data) pairs.  
 * <ul>
 * <li> It is generic in the sense that
 * the user can implement her own classes for the key and the data. To enable
 * this generic feature, the constructor of the HeapFile takes as parameter
 * a sample key and a sample data.
 * <li> The user may {@link #insert}, {@link #delete}, and {@link #search} a record.
 * </ul>
 * 
 * @see HeapFilePage
 * @author Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
 */
public class HeapFile extends DBIndex {
    /**
     * The HeapFile constructor. It takes as additional input a sample key
     * and a sample data.
     * 
     * @param _buffer      the buffer
     * @param isCreate     whether create (or open)
     * @param _sampleKey   the sample key
     * @param _sampleData  the sample data
     * @throws IOException
     */
    public HeapFile( DBBuffer _buffer, String filename, boolean isCreate, Key _sampleKey, Data _sampleData ) 
    throws IOException {
    	super( _buffer, filename, isCreate );
    	sampleKey = _sampleKey;
    	sampleData = _sampleData;
    }
    
	/**
	 * sample key
	 */
	private Key sampleKey;
	
	/**
	 * sample data
	 */
	private Data sampleData;
    
    /**
     * number of records
     */
    protected int numRecs;
    /**
     * Returns the number of records.
     * @return  number of records
     */
    public int numRecs() { return numRecs; }
    /**
     * pageID of the first full page. -1 if none.
     */
    protected int firstFullPageID;
    /**
     * pageID of the first non-full page. -1 if none.
     */
    protected int firstNonFullPageID;
 
    /**
     * Reads a page from buffer and parse it if not parsed already.
     * @param  pageID
     * @return an object of class HeapFilePage
     * @throws IOException
     */
    protected HeapFilePage myReadPage( int pageID ) throws IOException {
		DBBufferReturnElement ret = buffer.readPage(file, pageID);
		HeapFilePage thePage = null;
		if ( ret.parsed ) {
			thePage = (HeapFilePage)ret.object;
		}
		else {
			thePage = new HeapFilePage(pageSize, sampleKey, sampleData);
			thePage.read((byte[])ret.object);
		}
		return thePage;
    }
    
    /**
     * Removes a page from its (full or nonfull) linked list.
     * Note that this function does NOT write the page to buffer.
     * @param pageID  the pageID
     * @param page    the HeapFilePage object
     * @throws IOException
     */
    protected void removePageFromList( int pageID, HeapFilePage page ) 
    throws IOException {
    	if ( firstFullPageID == pageID ) firstFullPageID = page.next;
    	if ( firstNonFullPageID == pageID ) firstNonFullPageID = page.next;
    	
    	if ( page.next != -1 ) {
    		HeapFilePage nextPage = myReadPage(page.next);
    		nextPage.prev = page.prev;
    		buffer.writePage(file, page.next, nextPage);
    	}
    	if ( page.prev != -1 ) {
    		HeapFilePage prevPage = myReadPage(page.prev);
    		prevPage.next = page.next;
    		buffer.writePage(file, page.prev, prevPage);
    	}
    }
    
    /**
     * Adds a page to the beginning of the full-page list.
     * Note that this function writes the page to buffer.
     * @param pageID  the page ID
     * @param page    the HeapFilePage object
     * @throws IOException
     */
    protected void addPageToFullList( int pageID, HeapFilePage page ) 
    throws IOException {
    	page.prev = -1;
    	page.next = firstFullPageID;

		if ( firstFullPageID != -1 ) {
			HeapFilePage firstFullPage = myReadPage(firstFullPageID);
			firstFullPage.prev = pageID;
			buffer.writePage(file, firstFullPageID, firstFullPage);
		}
		
		firstFullPageID = pageID;
		buffer.writePage(file, pageID, page);
    }
    
    /**
     * Adds a page to the beginning of the nonfull-page list.
     * Note that this function writes the page to buffer.
     * @param pageID  the page ID
     * @param page    the HeapFilePage object
     * @throws IOException
     */
    protected void addPageToNonFullList( int pageID, HeapFilePage page ) 
    throws IOException {
    	page.prev = -1;
    	page.next = firstNonFullPageID;

		if ( firstNonFullPageID != -1 ) {
			HeapFilePage firstNonFullPage = myReadPage(firstNonFullPageID);
			firstNonFullPage.prev = pageID;
			buffer.writePage(file, firstNonFullPageID, firstNonFullPage);
		}
		
		firstNonFullPageID = pageID;
		buffer.writePage(file, pageID, page);
    }

    /**
     * Inserts a new record into the heap file.
     * Algorithm:<br>
     * <ul>If there is no non-full page,
     * <li>&nbsp; allocate;
     * <li>&nbsp; insert to the new page;
     * <li>&nbsp; add to the beginning of one linked list, depending on whether full;
     * <li>Else
     * <li>&nbsp; read the page in;
     * <li>&nbsp; insert to this page;
     * <li>&nbsp; if full, remove from non-full list and insert to full list.
     * </ul>
     *  
     * @param key   the key to be inserted
     * @param data  the data to be inserted
     * @throws IOException
     */
    public void insert( Key key, Data data ) throws IOException {
    	numRecs ++ ;
    	
    	if ( firstNonFullPageID == -1 ) { 
    		int newPageID = allocate();
    		HeapFilePage newPage = new HeapFilePage(pageSize, sampleKey, sampleData);
    		newPage.insert( key, data );
    		if ( newPage.isFull() ) {
    			addPageToFullList( newPageID, newPage );
    		}
    		else {
    			addPageToNonFullList( newPageID, newPage );
    		}
    	}
    	else {
    		HeapFilePage firstNonFullPage = myReadPage(firstNonFullPageID);
    		firstNonFullPage.insert(key, data);
    		if ( firstNonFullPage.isFull() ) {
    			int newFullPageID = firstNonFullPageID;
    			HeapFilePage newFullPage = firstNonFullPage;
    			removePageFromList( newFullPageID, newFullPage );
    			addPageToFullList( newFullPageID, newFullPage );
    		}
    		else {
    			buffer.writePage(file, firstNonFullPageID, firstNonFullPage);
    		}
    	}
    }
    	
    /**
     * Deletes the record by the given key.
     * After deletion, if the page is empty it should be set free.
     * Otherwise, if the page change from full to nonfull, it
     * should be removed from the full list and added to the nonfull list.
     * @param  key  the key of the record to be deleted
     * @return whether deleted
     * @throws IOException
     */
    public boolean delete( Key key ) throws IOException {
    	// search the full list
    	int pageID = firstFullPageID;
    	HeapFilePage page = null;
    	while ( pageID != -1 ) {
    		page = myReadPage(pageID);
    		boolean isDeleted = page.delete( key );
    		if ( isDeleted ) {
    			if ( page.numRecs() == 0 ) {
    				removePageFromList(pageID, page);
    				freePage(pageID);
    			}
    			else if ( !page.isFull() ) {
    				removePageFromList(pageID, page);
    				addPageToNonFullList(pageID, page);
    			}
    			else {
        			buffer.writePage(file, pageID, page);    				
    			}
    			numRecs --;
    			return true;
    		}
    		pageID = page.next;
    	}
    	
    	// search the nonfull list
    	pageID = firstNonFullPageID;
    	while ( pageID != -1 ) {
    		page = myReadPage(pageID);
    		boolean isDeleted = page.delete( key );
    		if ( isDeleted ) {
    			if ( page.numRecs() == 0 ) {
    				removePageFromList(pageID, page);
    				freePage(pageID);
    			}
    			else {
    				buffer.writePage(file, pageID, page);
    			}
    			numRecs --;
    			return true;
    		}
    		pageID = page.next;
    	}
    	
    	return false;
    }
    
    /**
     * Searches the record by the given key.
     * @param   key   the key to search
     * @return  <code>data</code> if found; <code>null</code> otherwise
     * @throws IOException
     */
    public Data search( Key key ) throws IOException {
    	// search the full list
    	int pageID = firstFullPageID;
    	HeapFilePage page = null;
    	while ( pageID != -1 ) {
    		page = myReadPage(pageID);
    		Data data = page.search( key );
    		if (data != null ) return data;
    		pageID = page.next;
    	}
    	
    	// search the nonfull list
    	pageID = firstNonFullPageID;
    	while ( pageID != -1 ) {
    		page = myReadPage(pageID);
    		Data data = page.search( key );
    		if (data != null ) return data;
    		pageID = page.next;
    	}
    	
    	return null;
    }
    
    class MyEnumeration implements Enumeration<KeyData> {
    	int status = 0; // 0: checking full list; 1: checking nonfull list; 2: finished.
    	int whichRecToReturnNext = 0;
    	HeapFilePage page = null;
		
    	public MyEnumeration(){        
    		try {
    			if (firstFullPageID != -1) {
    				status = 0;
    				page = myReadPage(firstFullPageID);
    			}
    			else if (firstNonFullPageID != -1 ) {
    				status = 1;
    				page = myReadPage(firstNonFullPageID);
    			}
    			else {
    				status = 2;
    			}
    		} catch (IOException e){
    		}
    	}
    	
		public boolean hasMoreElements() {
			return status != 2;
		}
		
		public KeyData nextElement(){
			if (!hasMoreElements()) return null;
			
        	KeyData keyData = page.get(whichRecToReturnNext);
        	whichRecToReturnNext++;
        	if (whichRecToReturnNext>=page.numRecs()) {
        		whichRecToReturnNext = 0;
        		int pageID = page.next;
        		if (pageID!=-1) {
        			try {
        				page = myReadPage(pageID);
        			} catch ( IOException e) {
        			}
        			return keyData;
        		}
        		switch( status ) {
        		case 0:
        			if (firstNonFullPageID!=-1) {
        				status = 1;
            			try {
            				page = myReadPage(firstNonFullPageID);
            			} catch ( IOException e) {
            			}
         			}
        			else {
        				page = null;
        				status = 2;
        			}
        			break;
        		case 1:
        			page = null;
        			status = 2;
        			break;
        		case 2:
        			assert(false);
        		}
        	}
        	return keyData;
		}
    }
    
    /**
     * Starts an enumeration.
     * Once started, the caller can call the hasMoreElements() and nextElement() 
     * functions of the returned Enumeration to iterate through the HeapFile.
     */
    public Enumeration<KeyData> StartEnumeration(){
    	return new MyEnumeration();
    }
    
    /**
     * Prints some information about the index.
     */
    public void printInformation() {
    	System.out.println( "number of pages = " + numPages );
    	System.out.println( "firstFullPageID = " + firstFullPageID );
    	System.out.println( "firstNonFullPageID = " + firstNonFullPageID );
    	System.out.println( "number of records = " + numRecs );
    	
    	int[] io = buffer.getIOs();
    	System.out.println( "#disk read = " + io[0] +
    			",  #disk write = " + io[1] );
    }
    
    protected void readIndexHead (byte[] indexHead) {
    	ByteArray ba = new ByteArray( indexHead, true );
    	try {
			numRecs = ba.readInt();
			firstFullPageID = ba.readInt();
			firstNonFullPageID = ba.readInt();
		} catch (IOException e) {}
    }
    protected void writeIndexHead (byte[] indexHead) {
    	ByteArray ba = new ByteArray( indexHead, false );
    	try {
    		ba.writeInt(numRecs);
    		ba.writeInt(firstFullPageID);
    		ba.writeInt(firstNonFullPageID);
		} catch (IOException e) {}
    }
    protected void initIndexHead() {
    	numRecs = 0;
    	firstFullPageID = -1;
    	firstNonFullPageID = -1;
    }
}
