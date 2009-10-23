package MovieID_Ratings;

import java.io.IOException;
import java.util.Vector;

import neustore.base.DBPage;
import neustore.base.ByteArray;

/**
 * The StructureIndexPage
 * 
 * @see MovieID_Ratings
 * @author Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
 */
public class MovieID_RatingsPage extends DBPage {
	private MovieRatings MovieAndRatings;
	
	public MovieID_RatingsPage( int _pageSize ) {
		super(1, _pageSize);
		// records = new Vector<StructureIndexRecord>();
	}
	
	/**
	 * Returns the number of records in this page.
	 * @return   the number of records 
	 */
	public int numRecs() { return MovieAndRatings.UserRatings.size(); }
	
	public int insert(MovieRatings toInsert)
	{ 
		MovieAndRatings = toInsert;
		return 1;
	}
	
	/* returns StructureIndexRecord with NodeId == TargetNodeId if found
	 * null otherwise
	 */
	public Vector<Ratings> getRatingsById (int TargetMovieID)
	{
		if(MovieAndRatings.MovieID == TargetMovieID)
		{
			return MovieAndRatings.UserRatings;
		}
		return null;
	}
	
	protected void read( byte[] b ) throws IOException {
		ByteArray ba = new ByteArray( b, ByteArray.READ );
		int numRecs = ba.readInt();
		ba.readString(46); // 92 bytes of padding
		MovieAndRatings = ba.readMovieRatings();
	}
	
	protected void write( byte[] b ) throws IOException {
		ByteArray ba = new ByteArray( b, ByteArray.WRITE );
		int numRecs = 1; // records.size();
		ba.writeInt( numRecs );
		String padding = " ";
		for(int padChars = 0; padChars < 46; padChars++)
			ba.writeString( padding );
		ba.writeMovieRatings(MovieAndRatings);
	}
}