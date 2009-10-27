package neustore.base;

/**
 * A buffered page as return result to {@link DBBuffer#readPage}.
 * Besides the page itself, the returned element also
 * specifies the nodeType and whether the node was parsed. If parsed, the returned
 * object is of class DBPage.  Otherwise, the returned object is a byte array and
 * it's up to the application to generate a DBPage object.
 * 
 * @see DBBuffer
 * @author Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
 */
public class DBBufferReturnElement {
	public int nodeType;
	public Object object;
	public boolean parsed;
	
	public DBBufferReturnElement( int _nodeType, Object _object, boolean _parsed ) {
		nodeType = _nodeType;
		object = _object;
		parsed = _parsed;
	}
}
