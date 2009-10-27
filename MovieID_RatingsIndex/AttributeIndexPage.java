package MovieID_RatingsIndex;

/*import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import neustore.base.Data;
import neustore.base.Key;
import neustore.base.StudentRecord;
*/

import java.util.Vector;
import neustore.base.DBPage;
import neustore.base.ByteArray;
import java.io.IOException;

/**
 * @see AttributeIndex
 * @author Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
 */
public class AttributeIndexPage extends DBPage
{

    protected Vector<AttributeRecord> records;

    /**
     * Creates a new page to store Attribute Records.
     */
    public AttributeIndexPage( int pageSize )
    {
        super(1, pageSize);
        records = new Vector<AttributeRecord>();
    }

    /**
     * Searches the page for a record matching the specified attribute name
     * for a given node. Returns the value of the attribute, else null.
     */
    public String getAttValue(int nID, String attName)
    {
    	int index;

    	if( (index = searchPage(nID, attName)) > -1 )    //record was found
        {
            return records.get(index).attValue;
        }

        return null;
    }

    /**
     * Deletes the specified record from the page. Returns non-negative number
     * on success (record was found and deleted) or -1 on failure (record not
     * found in the page).
     */
    public int deleteRecord(int nID, String attName)
    {
        int index;
        if( (index = searchPage(nID, attName)) >= 0 )
            if( (records.remove(index)) != null )    //remove record from list
                return 1;
        return -1;
    }
    
    /**
     * Adds a new entry into the page, returns non-negative value on success,
     * -1 on failure - page full, not enough space to write the entry.
     */
    public boolean insertRecord(int nID, String attName, String attValue)
    {
        AttributeRecord record;
        record = new AttributeRecord(nID, attName, attValue);
        records.add(record);    //add record to the page
        return true;
    }

    /**
     * Searches the page for a record matching the specified attribute name
     * for a given node. Returns the index from the list in which the record
     * was found, -2 if page empty, -1 otherwise.
     */
    public int searchPage(int nID, String attName)
    {
    	AttributeRecord rec;
    	
    	if(records.size() > 0)
    	{
            for(int idx = 0; idx < records.size(); idx++)
            {
                rec = records.get(idx);

                if( (rec.nodeID == nID) && attName.equals(rec.attName) )
                    return idx;
            }
    	}    	
    	return -1;
    }

    /**
     * Returns the number of records currently stored on the page.
     */
    public int numRecs()
    {
        return records.size();
    }
    
    /**
     * Returns a record from the page using the index of the record in the "list".
     */
    public AttributeRecord getRecord(int recordIndex)
    {
        return records.get(recordIndex);
    }

    /**
     * Returns the last record on the page.
     */
    public AttributeRecord getLastElement()
    {
        return ((AttributeRecord) records.lastElement());
    }

    /**
     * Adds the shifted record to another index of the list.
     */
    public void addMovedRecord(int index, AttributeRecord record)
    {
        records.add(index, record);
    }

    /**
     * Write page info to byte array.
     */
    protected void write(byte[] bytePage) throws IOException
    {
        ByteArray ba = new ByteArray( bytePage, ByteArray.WRITE );

        ba.writeInt( 1 );
        ba.writeInt( records.size() );

        for (AttributeRecord record : records)
        	ba.writeAttributeRecord(record);
    }

    /**
     * Read info from byte array.
     */
    protected void read(byte[] bytePage) throws IOException
    {
        ByteArray ba = new ByteArray( bytePage, ByteArray.READ );
        ba.readInt();    // skip nodeType
        int numRecs = ba.readInt();
        records.removeAllElements();

        for( int i = 0; i < numRecs; i++ )
        {
        	AttributeRecord in = ba.readAttributeRecord();
            records.add(in);
        }
    }
}
