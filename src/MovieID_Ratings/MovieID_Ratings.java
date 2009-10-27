package MovieID_Ratings;

import java.io.IOException;
import java.util.Vector;

import neustore.base.*;

/**
 * The StructureIndex is a heap file storing information about individual elements in an
 * XML document in the form of StudentIndexRecords. This file is indexed by ElementIndex
 * and references XMLContent and ElementIndex. 
 * @see MovieID_RatingsPage 
 * @author Donghui Zhang <donghui@ccs.neu.edu>
 * @modified Matthew Robertson
 */

public class MovieID_Ratings extends DBIndex {
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
	private MovieID_RatingsPage lastPage;
	
	public MovieID_Ratings(DBBuffer _buffer, String filename, int isCreate) throws IOException {
		super(_buffer, filename, isCreate);
		pageCapacity = (pageSize-96)/40; // header of 96 bytes, record of 40 bytes
		if(isCreate == 1)
			lastPage = new MovieID_RatingsPage(pageSize);
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
	protected MovieID_RatingsPage myReadPage( int pageID ) throws IOException {
		MovieID_RatingsPage page;
		DBBufferReturnElement ret = buffer.readPage(file, pageID);
		if ( ret.parsed ) {
			page = (MovieID_RatingsPage)ret.object;
		}
		else {
			page = new MovieID_RatingsPage(pageSize);
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
	 * Appends a new MovieRating to the end of the file.
	 * @param key  the SIR to be appended
	 * @return The page on which the ratings for the inserted movie begin.
	 * @throws IOException
	 */
	public int insertEntry( MovieRatings key ) throws IOException {
		lastPageID = allocate();
		lastPage = new MovieID_RatingsPage(pageSize);
		int entryNumber = lastPage.insert( key );
		buffer.writePage(file, lastPageID, lastPage);
		return lastPageID;
	}
	
	public Vector<Ratings> getRatingsById (int TargetNodeId)
	{
		try
		{
			for ( int currentPageID=1; currentPageID<=lastPageID; currentPageID++ ) {
				MovieID_RatingsPage currentPage = myReadPage(currentPageID);
				Vector<Ratings> returnRecord = currentPage.getRatingsById(TargetNodeId);
				buffer.writePage(file, currentPageID, currentPage);
				if(returnRecord != null)
					return returnRecord;
			}
		}
		catch(IOException e)
		{
			System.err.println("IOException in getRecordById: " + e.toString());
			System.exit(1);
		}
		return null;
	}
	
    public int numPages()
    {
    	return lastPageID;
    }
}