package MovieID_Ratings;

import java.sql.Date;

/**
 *  Should be immutable
 */
public class UserRating {
	
	public UserRating(int userId, int rating, String date) {
		this.userId = userId;
		this.rating = rating;
		// TODO: this is probably not what we want
		this.date = 0; //(int) Date.valueOf(date).getTime();
	}
	
	public UserRating(int userId, int rating, int date) {
		this.userId = userId;
		this.rating = rating;
		this.date = date;
	}
	
	public String toString() {
		return userId + "," + rating + "," + date;
	}
	
	public final int userId;
	public final int rating;
	public final int date;

}
