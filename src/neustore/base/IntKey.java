package neustore.base;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * An integer key. 
 * @author Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
 */
public class IntKey implements Key {
	public int key;
	
	public IntKey( int _key ) {
		key = _key;
	}
	
	public Object clone() {
		IntKey newKey = new IntKey(key);
		return newKey;
	}
	
	public int size() { return 4; }
	
	public int maxSize() { return 4;}
	
	public void read(DataInputStream in) throws IOException {
		key = in.readInt();
	}
	public void write(DataOutputStream out) throws IOException {
		out.writeInt(key);
	}

	public int compareTo(Key key2) {
		Integer i1 = new Integer(key);
		Integer i2 = new Integer( ((IntKey)key2).key );
		return i1.compareTo(i2);
	}
	
	public boolean equals(Object key2) {
		int k = ((IntKey)key2).key;
		return key==k;
	}
}

