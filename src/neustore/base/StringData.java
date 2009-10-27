package neustore.base;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A class that stores a variable-length String.
 * Note that to write a StringData to disk, the occupied size is four bytes
 * larger than the String size, for we also store the size of the String. 
 * 
 * @author Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
 */
public class StringData implements Data {
	/**
	 * maximum length of a String.
	 */
	protected int maxStringLength;
	/**
	 * the stored String
	 */
	public String string;
	
	/**
	 * The constructor takes as input the max length of a string.
	 * The maxSize of this object is this maxStringLength + 4.
	 * @param _maxStringLength   max length of a string
	 */
	public StringData( int _maxStringLength, String _string ) {
		maxStringLength = _maxStringLength;
		string = new String(_string);
	}
	
	public Object clone() {
		StringData newData = new StringData(maxStringLength, string);
		return newData;
	}

	public int size() {
		if ( string.length() > maxStringLength )
			return maxStringLength + 4;
		else 
			return string.length() + 4; 
	}

	public int maxSize() {
		return maxStringLength + 4;
	}

	public void read(DataInputStream in) throws IOException {
		int length = in.readInt();
		assert( length >= 0 );
		if ( length == 0 ) {
			string = new String();
			return;
		}
		byte[] b = new byte[length];
		in.read(b);
		string = new String(b);
	}

	public void write(DataOutputStream out) throws IOException {
		byte[] b = string.getBytes();
		int length;
		if ( b.length > maxStringLength ) 
			length = maxStringLength;
		else
			length = b.length;
		out.writeInt(length);
		out.write(b, 0, length);
	}
}
