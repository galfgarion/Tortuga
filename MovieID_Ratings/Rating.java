package MovieID_Ratings;

public class Rating {
	public int UserID, Rating, DateOfRating;
	
	public Rating (int UserID, int Rating, int DateOfRating) {
		this.UserID = UserID;
		this.Rating = Rating;
		this.DateOfRating = DateOfRating;
	}
	
	public String toString() {
		return UserID + " " + Rating + " " + DateOfRating;
	}
}
