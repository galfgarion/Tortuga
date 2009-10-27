package MovieID_Ratings;

import java.io.IOException;

import neustore.base.LRUBuffer;

public class MovieID_Ratings_Test {
	
	public static void main(String[] argv) {
		/* 10 pages, 4096-byte page, wug.txt, isCreate = 1 */
		try {
			MovieID_Ratings testObject = new MovieID_Ratings(new LRUBuffer (5, 4096), "wug", 1);
			for(int x = 0; x < 9000; x++) {
				MovieRatings wug = new MovieRatings(x);
				wug.UserRatings.add(new Rating(1, 5, 10));
				wug.UserRatings.add(new Rating(3, 5, 10));
				testObject.insertEntry(wug);
			}
			
			testObject.close();
			
			testObject = new MovieID_Ratings(new LRUBuffer(5, 4096), "wug", 0);
			System.out.println(testObject.getRatingsById(1));
			System.out.println(testObject.getRatingsById(1));
			System.out.println(testObject.getRatingsById(2));
			System.out.println(testObject.getRatingsById(10));
		} catch (IOException e) {
			System.err.println("could not open file blah");
		}
	}
}
