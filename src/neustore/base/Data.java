package neustore.base;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Interface for the data part of a record.
 * @see Key
 * @author Tian Xia &lt;tianxia@ccs.neu.edu&gt;<br>Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
 */
public interface Data{
	/**
	 * Creates a new copy of the object.
	 * @return  the new copy
	 */
	public abstract Object clone();
	/* 
	 * Returns the number of bytes the object occupy.
	 * @return  number of bytes
	 */
	public abstract int size();
	/**
	 * Returns the maximum number of bytes this type of object may occupy.
	 * It is used to support variable-length data.
	 * @return  maximum number of bytes
	 */
	public abstract int maxSize();
	/**
	 * Reads the object from an input stream.
	 * @param in   input stream
	 * @throws IOException
	 */
	public abstract void read(DataInputStream in) throws IOException ;
	/**
	 * Writes the object to an output stream.
	 * @param out   output stream
	 * @throws IOException
	 */
	public abstract void write(DataOutputStream out) throws IOException ;
}
