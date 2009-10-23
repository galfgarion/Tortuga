package MovieID_RatingsIndex;

public class AttributeRecord
{
    public String attName, attValue;
    public int nodeID;
    
    public AttributeRecord(int nID, String attName, String attValue)
    {
    	nodeID = nID;
        this.attName = attName;
        this.attValue = attValue;
    }
    
    public String toString()
    {
    	return (attName + " = " + attValue);
    }
}