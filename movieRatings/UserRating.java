package movieRatings;

/**
 *  Should be immutable
 */
public class UserRating implements Comparable<UserRating> {
	
	public UserRating(int userId, byte rating/*, String date*/) {
		this.userId = userId;
		this.rating = rating;
		// TODO: this is probably not what we want
		/* this.date = 0; //(int) Date.valueOf(date).getTime(); */
	}
	
	public UserRating(int userId, byte rating, int date) {
		this.userId = userId;
		this.rating = rating;
		/* this.date = date; */
	}
	
	public String toString() {
		return userId + "," + rating + ","/* + date*/;
	}
	
	public int compareTo(UserRating o) {
		return this.userId - o.userId;
	}
	
	public final int userId;
	public byte rating;
	// public final int date;
	

}
