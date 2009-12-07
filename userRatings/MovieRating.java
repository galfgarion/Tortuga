package userRatings;

/**
 *  Should be immutable
 */
public class MovieRating implements Comparable<MovieRating> {
	
	public MovieRating(int userId, byte rating/*, String date*/) {
		this.movieId = userId;
		this.rating = rating;
		// TODO: this is probably not what we want
		/* this.date = 0; //(int) Date.valueOf(date).getTime(); */
	}
	
	public MovieRating(int userId, byte rating, int date) {
		this.movieId = userId;
		this.rating = rating;
		/* this.date = date; */
	}
	
	public String toString() {
		return movieId + "," + rating + ","/* + date*/;
	}
	
	public int compareTo(MovieRating o) {
		return this.movieId - o.movieId;
	}
	
	public final int movieId;
	public byte rating;
	// public final int date;
	

}
