package MovieID_RatingsIndex;

import java.io.IOException;
import java.util.Vector;

import neustore.base.DBBuffer;
import neustore.base.DBBufferReturnElement;
import neustore.base.DBIndex;
import neustore.base.ByteArray;

/**
 * This class stores and indexes the attributes and their values for XML
 * documents.
 * 
 * @see AttributeIndexPage
 */
public class AttributeIndex extends DBIndex
{
    /**
     * maximum number of records in a page
     */
    private int pageCapacity;

    /**
     * pageID of the last
     */
    private int lastPageID;
    
    private AttributeIndexPage lastPage; 

    /**
     * Constructor.
     * @param buffer
     * @param filename    filename under which index is to be stored on disk
     * @param isCreate    true if file needs to be created, false if file needs to be opened
     * @throws IOException
     */
    public AttributeIndex(DBBuffer buffer, String filename, int isCreate)
        throws IOException
    {
        super(buffer, filename, isCreate);

        // header of 96 bytes, record of 100 bytes
        pageCapacity = (pageSize-96)/400-1;
        pageCapacity = 1; // TEST CODE

        //create list of pages, list initially contains 1 empty page
        if(isCreate == 0)
            lastPage = myReadPage(lastPageID);
        else
            lastPage = new AttributeIndexPage(pageSize);
    }

    /**
     * Insert new entry into table, returns exit status. Returns non-negative
     * value on success, -1 on failure (ie - record already exists).
     */
    public boolean insertEntry(int nodeID, String attName, String attValue) throws IOException
    {
    	if ( lastPage.numRecs() >= pageCapacity ) {
			lastPageID = allocate();
			lastPage = new AttributeIndexPage(pageSize);
		}
		lastPage.insertRecord(nodeID, attName, attValue);
		buffer.writePage(file, lastPageID, lastPage);
    	return true;
    }

    /**
     * Returns the value of the specified attribute of the given node.
     */
    public String getValue(int nodeID, String attName)
    {
    	String recValue = null;
    	try
    	{
	    	AttributeIndexPage aPage;
			for (int currentPageID=1; currentPageID<=lastPageID; currentPageID++ )
	        {
				aPage = myReadPage(currentPageID);
	    	    recValue = aPage.getAttValue(nodeID, attName);
	    	    if(recValue != null)
	                break;
	        }
    	}
    	catch(IOException e) { System.err.println("rrr"); }
    	return recValue;
    }

    /**
     * Deletes the specified entry.
     */
    public int deleteEntry(int nodeID, String attName)
    {
    	AttributeRecord rec;

    	try
    	{
	    	AttributeIndexPage aPage;
			for (int currentPageID=1; currentPageID<=lastPageID; currentPageID++ )
	        {
				aPage = myReadPage(currentPageID);
	    	    rec = aPage.getLastElement();
	
	    	    if(rec.nodeID >= nodeID)
	                //delete the record
	                if(aPage.deleteRecord(nodeID, attName) != -1) {
	                	buffer.writePage(file, currentPageID, aPage);
	                    return 1;
	                }
	        }
    	}
    	catch(IOException e) {}

    	return -1;
    }


    /**
     * Initializes the the index.
     */
    protected void initIndexHead()
    {
        lastPageID = allocate();
	}

    /**
     * Reads head information - page size - from disk when index is opened.
     */
    protected void readIndexHead(byte[] head)
    {
    	//create byte array for reading
        ByteArray ba = new ByteArray(head, ByteArray.READ);

        try
        {
            lastPageID = ba.readInt();
        }
        catch (IOException e) {}
	}

    /**
     * Writes head info to disk right before index is closed.
     */
    protected void writeIndexHead(byte[] head)
    {
        //create byte array for writing
        ByteArray ba = new ByteArray(head, ByteArray.WRITE);

        try
        {
            ba.writeInt(lastPageID);
        }
        catch (IOException e) {}
    }
    
    public int numPages()
    {
    	return lastPageID;
    }
    
    public Vector<String> getAttributeValuesById(int NodeId)
    {
    	Vector<String> attribVals = new Vector<String>();
    	try
    	{
	    	AttributeIndexPage aPage;
			for (int currentPageID=1; currentPageID<=lastPageID; currentPageID++ )
	        {
				aPage = myReadPage(currentPageID);
	    	    for(AttributeRecord rec : aPage.records)
	    	    	if(rec.nodeID == NodeId)
	    	    		attribVals.add(rec.attValue);
	        }
    	}
    	catch(IOException e) {}

    	return attribVals;
    }
    
    public Vector<AttributeRecord> getAttributeRecordsById(int NodeId)
    {
    	Vector<AttributeRecord> attribRecs = new Vector<AttributeRecord>();
    	AttributeIndexPage aPage;
    	try
    	{
	    	for (int currentPageID=1; currentPageID<=lastPageID; currentPageID++ )
	        {
				aPage = myReadPage(currentPageID);
	    	    for(AttributeRecord rec : aPage.records)
	    	    	if(rec.nodeID == NodeId)
	    	    		attribRecs.add(rec);
	        }
    	} catch(IOException e) {}
    	return attribRecs;
    }
    
    public boolean hasAttributes(int nodeID)
    {
    	AttributeIndexPage aPage;
    	try
    	{
	    	for (int currentPageID=1; currentPageID<=lastPageID; currentPageID++ )
	        {
				aPage = myReadPage(currentPageID);
    	    for(AttributeRecord rec : aPage.records)
    	    	if(rec.nodeID == nodeID)
    	    		return true;
	        }
    	}
    	catch(IOException e) {}
		return false;
    }

    /**
     * Reads a page from buffer and parse it if not already parsed.
     * 
     * @param   pageID
     * @return  the parsed page
     * @throws IOException
     */
    protected AttributeIndexPage myReadPage( int pageID ) throws IOException
    {
        AttributeIndexPage page;
        DBBufferReturnElement ret = buffer.readPage(file, pageID);
        if ( ret.parsed )
            page = (AttributeIndexPage)ret.object;
        else {
            page = new AttributeIndexPage(pageSize);
            page.read((byte[])ret.object);
        }

        return page;
    }
    
  
}
