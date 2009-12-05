package movieRatings;

import java.util.ArrayList;

public class EfficientMovieRatings {
	public int MovieID;
	public int[] UserID;
	public byte[] Rating;
	public int numRatingsStored;
	
	public EfficientMovieRatings(int MovieID, ArrayList<UserRating> Ratings) {
		this.MovieID = MovieID;
		
		UserID = new int[Ratings.size()];
		Rating = new byte[Ratings.size()];
		
		for(int x = 0; x < Ratings.size(); x++) {
			UserID[x] = Ratings.get(x).userId;
			Rating[x] = Ratings.get(x).rating;
		}
		
		numRatingsStored = Ratings.size();
	}
	
	public UserRating get(int index) {
		return new UserRating(UserID[index], Rating[index]);
	}
	
	public String toString() {
		String result = "";
		for(int x = 0; x < numRatingsStored; x++) {
			result += UserID[x] + "/" + Rating[x] + " ";
		}
		return result;
	}
}
