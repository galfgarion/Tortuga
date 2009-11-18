package movieRatings;

import java.io.IOException;
import junit.framework.TestCase;
import neustore.base.LRUBuffer;

public class MovieID_Ratings_Test extends TestCase {
	
	public static void main() {
		testInsertion();
	}
	
	public static void testInsertion() {
		/* 10 pages, 4096-byte page, wug.txt, isCreate = 1 */
		try {
			MovieID_Ratings testObject = new MovieID_Ratings(new LRUBuffer (5, 4096), "wug", 1);
			for(int x = 1; x <= 10; x++) {
				MovieRatings wug = new MovieRatings(x);
				for(int y = 0; y < 500; y++) {
					wug.add(new UserRating(x, (byte)5, 10));
				}
				// wug.add(new UserRating(3, 5, 10));
				testObject.insertEntry(wug);
			}
			
			testObject.close();
			
			testObject = new MovieID_Ratings(new LRUBuffer(5, 4096), "wug", 0);
			assertEquals(500, testObject.getRatingsById(1).size());
			/* assertEquals("[1,5,10, 3,5,10]", testObject.getRatingsById(1).toString());
			assertEquals("[1,5,10, 3,5,10]", testObject.getRatingsById(1).toString());
			assertEquals("[02,5,10, 3,5,10]", testObject.getRatingsById(2).toString());
			assertEquals("[10,5,10, 3,5,10]", testObject.getRatingsById(10).toString()); */

		} catch (IOException e) {
			fail("could not open file blah");
		}
	}
}
