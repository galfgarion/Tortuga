package neustore.base;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import MovieID_RatingsIndex.AttributeRecord;
import MovieID_Ratings.MovieRatings;


/**
 * A class that provides basic read/write operators on a byte array.
 * 
 * @author Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
 */
public class ByteArray {

	public static final boolean READ = true;
	public static final boolean WRITE = false;
	
	private ByteArrayInputStream byte_in = null;
	private DataInputStream in = null;
	private byte[] buf = null;
	private int offset = 0;
	
	/**
	 * Constructor.
	 * @param _buf     the associated byte array
	 * @param isRead   whether to read or write
	 */
	public ByteArray( byte[] _buf, boolean isRead ) {
		if ( isRead ) {
			byte_in = new ByteArrayInputStream(_buf);
			in = new DataInputStream(byte_in);
		}
		else {
			buf = _buf;
		}
	}
	
	/**
	 * Reads an integer.
	 * @return the integer
	 * @throws IOException
	 */
	public int readInt() throws IOException {
		return in.readInt();
	}
	
	/**
	 * Reads a string.
	 * @return the string
	 * @throws IOException
	 */
	public String readString( int numChars ) throws IOException {
		String ret = "";
		for(int x = 0; x < numChars; x++)
			ret += in.readChar();
		return ret;
	}

	
	/**
	 * Reads from the byte array to fill a passed buffer.
	 * Note: the number of bytes read is typically equal to <code>b.length</code>.
	 * @param b   buffer to read to.
	 * @throws IOException
	 */
	public void read( byte[] b ) throws IOException {
		in.read(b);
	}
	
	/**
	 * Writes an integer.
	 * @param value   the integer to write
	 * @throws IOException
	 */
	public void writeInt( int value ) throws IOException {
		ByteArrayOutputStream byte_out = new ByteArrayOutputStream(4);
		DataOutputStream out = new DataOutputStream(byte_out);
		
		out.writeInt( value );
		System.arraycopy(byte_out.toByteArray(), 0, buf, offset, 4 );
		offset += 4;
		
		out.close();
		byte_out.close();
	}
	
	/**
	 * Writes a string.
	 * @param value   the string to write
	 * @throws IOException
	 */
	public void writeString( String value ) throws IOException {
		ByteArrayOutputStream byte_out = new ByteArrayOutputStream(value.length());
		DataOutputStream out = new DataOutputStream(byte_out);
		
		out.writeChars( value );
		System.arraycopy(byte_out.toByteArray(), 0, buf, offset, value.length()*2 );
		offset += value.length()*2;
		
		out.close();
		byte_out.close();
	}
	
	/**
	 * Writes the passed buffer to the byte array.
	 * @param b  the passed buffer
	 * @throws IOException
	 */
	public void write( byte[] b ) throws IOException {
		System.arraycopy( b, 0, buf, offset, b.length);
	}
	
	public void writeMovieRatings ( MovieRatings toWrite ) throws IOException
	{
		/* writeInt (toWrite.nodeId);
		writeInt (toWrite.elementId);
		writeInt (toWrite.PreOrder);
		writeInt (toWrite.PostOrder);
		writeInt (toWrite.Ordinal);
		writeInt (toWrite.Layer);
		writeInt (toWrite.Parent);
		writeInt (toWrite.isLeaf); */
		writeInt (toWrite.MovieID);
	}
	
	public void writeAttributeRecord(AttributeRecord record) throws IOException {
		if(record.attName.length() >= 64)
			record.attName = record.attName.substring(0, 64);
		while(record.attName.length() < 64)
			record.attName += " ";
		writeString (record.attName);
		record.attName = record.attName.trim();
		
		if(record.attValue.length() > 128)
			record.attValue = record.attValue.substring(0, 128);
		while(record.attValue.length() < 128)
			record.attValue += " ";
		writeString (record.attValue);
		record.attValue = record.attValue.trim();
		
		writeInt (record.nodeID);
		
	}
	
	/**
	 * Reads a student record.
	 * @return the student record
	 * @throws IOException
	 */
	public MovieRatings readMovieRatings () throws IOException {
		int nodeId = readInt();
		int elementId = readInt();
		int PreOrder = readInt();
		int PostOrder = readInt();
		return new MovieRatings();
	}
	
	public AttributeRecord readAttributeRecord () throws IOException {
		String attName = readString(64).trim();
		String attValue = readString(128).trim();
		int nodeId = readInt();
		return new AttributeRecord(nodeId, attName, attValue);
	}
}