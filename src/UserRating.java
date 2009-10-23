
public class UserRating {
	
	public UserRating(int userId, int rating, String date) {
		this.userId = userId;
		this.rating = rating;
		this.date = date;
		
	}
	
	public String toString() {
		return userId + "," + rating + "," + date;
	}
	
	public final int userId;
	public final int rating;
	public final String date;

}
