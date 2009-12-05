package userRatings;

import java.io.IOException;
import java.util.ArrayList;

import neustore.base.DBPage;
import neustore.base.ByteArray;

/**
 * The StructureIndexPage
 * 
 * @see UserID_Ratings
 * @author Donghui Zhang &lt;donghui@ccs.neu.edu&gt;
 */
public class UserID_RatingsPage extends DBPage {
	public UserRatings MovieAndRatings;
	
	public UserID_RatingsPage( int _pageSize ) {
		super(1, _pageSize);
	}
	
	/**
	 * Returns the number of records in this page.
	 * @return   the number of records 
	 */
	public int numRecs() { return MovieAndRatings.size(); }
	
	public int insert(UserRatings toInsert, int NumRatingsToInsert) { 
		if(MovieAndRatings != null) {
			System.err.println("just making sure that you really meant to insert to a page that already has a MovieAndRatings object instantiated");
			System.exit(1);
		}
		MovieAndRatings = new UserRatings(toInsert.getMovieID());
		ArrayList<MovieRating> y = toInsert.getUserRatings();
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
	public ArrayList<MovieRating> getRatingsById (int TargetMovieID)
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
		
		MovieAndRatings = new UserRatings(MovieID);
		MovieAndRatings._movieRatings.ensureCapacity(numRecs);
		ba.readAllUserRatings(MovieAndRatings._movieRatings, numRecs);
	}
	
	protected void write( byte[] b ) throws IOException {
		ByteArray ba = new ByteArray( b, ByteArray.WRITE );
		/* write MovieID and NumRecs */
		ba.writeInt( MovieAndRatings.getMovieID() );
		// System.out.println(MovieAndRatings.UserRatings.size());
		ba.writeInt( MovieAndRatings.size() );
		
		for(MovieRating R : MovieAndRatings) {
			ba.writeRating(R);
		}
	}
}