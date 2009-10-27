package MovieID_Ratings;

import java.util.Vector;

public class MovieRatings {
	public int MovieID;
	public Vector<Rating> UserRatings;
	
	public MovieRatings(int MovieID) {
		this.MovieID = MovieID;
		UserRatings = new Vector<Rating>();
	}
}
