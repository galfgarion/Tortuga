package neustore.base;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import userRatings.MovieRating;
import movieRatings.UserRating;


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
	private static byte b[] = new byte[1];
	
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
	
	public byte readByte() throws IOException {
		in.read(b, 0, 1);
		return b[0];
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
	
	public void writeByte( byte b ) throws IOException {
		byte[] c = new byte[1];
		c[0] = b;
		System.arraycopy( c, 0, buf, offset, 1);
		offset++;
	}
	
	public void writeRating ( UserRating r ) throws IOException {
		writeInt (r.userId);
		
		// is there a more efficient way to do this? should we store the rating as a byte[1] instead?
		/* byte Rating[] = new byte[1];
		Rating[0] = r.rating; */
		writeByte (r.rating);
		
		// writeInt (r.date);
	}
	
	public void writeRating ( MovieRating r ) throws IOException {
		writeInt (r.movieId);
		writeByte (r.rating);
	}
	
	/**
	 * Reads a student record.
	 * @return the student record
	 * @throws IOException
	 */
	public UserRating readMovieRating () throws IOException {
		/* date temporarily excised to improve performance */
		return new UserRating(readInt(), readByte());
	}
	
	public void readAllMovieRatings(ArrayList<UserRating> destinationArray, int numRatings) throws IOException {
		for(int x = 0; x < numRatings; x++)
			destinationArray.add(new UserRating(in.readInt(), readByte()));
	}
	
	public void readAllUserRatings(ArrayList<MovieRating> destinationArray, int numRatings) throws IOException {
		for(int x = 0; x < numRatings; x++)
			destinationArray.add(new MovieRating(in.readInt(), readByte()));
	}
}
