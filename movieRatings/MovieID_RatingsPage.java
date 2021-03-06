package movieRatings;

import java.io.IOException;
import java.util.ArrayList;

import neustore.base.DBPage;
import neustore.base.ByteArray;

/**
 * The StructureIndexPage
 * 
 * @see MovieID_Ratings
 * @author Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
 */
public class MovieID_RatingsPage extends DBPage {
	public EfficientMovieRatings EfficientRatings;
	public MovieRatings MovieAndRatings;
	
	public MovieID_RatingsPage( int _pageSize ) {
		super(1, _pageSize);
	}
	
	/**
	 * Returns the number of records in this page.
	 * @return   the number of records 
	 */
	public int numRecs() { return MovieAndRatings.size(); }
	
	public int insert(MovieRatings toInsert, int NumRatingsToInsert) { 
		if(MovieAndRatings != null) {
			System.err.println("just making sure that you really meant to insert to a page that already has a MovieAndRatings object instantiated");
			System.exit(1);
		}
		MovieAndRatings = new MovieRatings(toInsert.getMovieID());
		ArrayList<UserRating> y = toInsert.getUserRatings();
		if(y.size() < NumRatingsToInsert)
			NumRatingsToInsert = y.size();
		for(int RatingsAdded = 0; RatingsAdded < NumRatingsToInsert; RatingsAdded++) {
			MovieAndRatings.add(y.remove(0));
		}
		return 1;
	}
	
	/* returns StructureIndexRecord with NodeId == TargetNodeId if found
	 * null otherwise
	 */
	public ArrayList<UserRating> getRatingsById (int TargetMovieID)
	{
		// System.out.println(MovieAndRatings.getMovieID());
		if(MovieAndRatings.getMovieID() == TargetMovieID) {
			return MovieAndRatings.getUserRatings();
		}
		return null;
	}
	
	protected void read( byte[] b ) throws IOException {
		ByteArray ba = new ByteArray( b, ByteArray.READ );
		int MovieID = ba.readInt();
		int numRecs = ba.readInt();
		
		MovieAndRatings = new MovieRatings(MovieID);
		MovieAndRatings._userRatings.ensureCapacity(numRecs);
		ba.readAllMovieRatings(MovieAndRatings._userRatings, numRecs);
	}
	
	protected void write( byte[] b ) throws IOException {
		ByteArray ba = new ByteArray( b, ByteArray.WRITE );
		/* write MovieID and NumRecs */
		ba.writeInt( MovieAndRatings.getMovieID() );
		// System.out.println(MovieAndRatings.UserRatings.size());
		ba.writeInt( MovieAndRatings.size() );
		
		for(UserRating R : MovieAndRatings) {
			ba.writeRating(R);
		}
	}
}