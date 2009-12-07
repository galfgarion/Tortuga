package userRatings;

import java.util.ArrayList;

public class EfficientUserRatings {
	public int UserID;
	public int[] MovieID;
	public byte[] Rating;
	public int numRatingsStored;
	
	public EfficientUserRatings(int UserID, ArrayList<MovieRating> Ratings) {
		this.UserID = UserID;
		
		MovieID = new int[Ratings.size()];
		Rating = new byte[Ratings.size()];
		
		for(int x = 0; x < Ratings.size(); x++) {
			MovieID[x] = Ratings.get(x).movieId;
			Rating[x] = Ratings.get(x).rating;
		}
		
		numRatingsStored = Ratings.size();
	}
	
	public MovieRating get(int index) {
		return new MovieRating(MovieID[index], Rating[index]);
	}
	
	public String toString() {
		String result = "";
		for(int x = 0; x < numRatingsStored; x++) {
			result += MovieID[x] + "/" + Rating[x] + " ";
		}
		return result;
	}
}
