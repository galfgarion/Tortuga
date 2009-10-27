package neustore.base;

/**
 * A class that stores a pair of Key and Data.
 * 
 * @author Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
 */
public class KeyData {
	public Key key;
	public Data data;
	
	public KeyData(Key key, Data data) {
		this.key = key;
		this.data = data;
	}
}