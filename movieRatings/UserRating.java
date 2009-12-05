package movieRatings;

import java.util.Comparator;

/**
 *  Should be immutable
 */
public class UserRating {
	
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
	
	public static final Comparator<UserRating> CompareByUserId = new Comparator<UserRating>() {
		@Override
		public int compare(UserRating o1, UserRating o2) {
			if(o1.userId < o2.userId) {
				return -1;
			} else if(o1.userId > o2.userId) {
				return 1;
			} else {
				return 0;
			}
		}
	};
	
	public final int userId;
	public byte rating;
	// public final int date;

}
