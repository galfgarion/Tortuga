package userRatings;

import java.util.ArrayList;

public class EfficientUserRatings {
	public int MovieID;
	public int[] UserID;
	public byte[] Rating;
	public int numRatingsStored;
	
	public EfficientUserRatings(int MovieID, ArrayList<MovieRating> Ratings) {
		this.MovieID = MovieID;
		
		UserID = new int[Ratings.size()];
		Rating = new byte[Ratings.size()];
		
		for(int x = 0; x < Ratings.size(); x++) {
			UserID[x] = Ratings.get(x).userId;
			Rating[x] = Ratings.get(x).rating;
		}
		
		numRatingsStored = Ratings.size();
	}
	
	public MovieRating get(int index) {
		return new MovieRating(UserID[index], Rating[index]);
	}
	
	public String toString() {
		String result = "";
		for(int x = 0; x < numRatingsStored; x++) {
			result += UserID[x] + "/" + Rating[x] + " ";
		}
		return result;
	}
}
