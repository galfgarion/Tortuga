package MovieID_Ratings;

import java.io.IOException;
import junit.framework.TestCase;
import neustore.base.LRUBuffer;

public class MovieID_Ratings_Test extends TestCase {
	
	public void testInsertion() {
		/* 10 pages, 4096-byte page, wug.txt, isCreate = 1 */
		try {
			MovieID_Ratings testObject = new MovieID_Ratings(new LRUBuffer (5, 4096), "wug", 1);
			for(int x = 0; x < 9000; x++) {
				MovieRatings wug = new MovieRatings(x);
				wug.add(new UserRating(x, 5, 10));
				wug.add(new UserRating(3, 5, 10));
				testObject.insertEntry(wug);
			}
			
			testObject.close();
			
			testObject = new MovieID_Ratings(new LRUBuffer(5, 4096), "wug", 0);
			assertEquals("[1,5,10, 3,5,10]", testObject.getRatingsById(1).toString());
			assertEquals("[1,5,10, 3,5,10]", testObject.getRatingsById(1).toString());
			assertEquals("[2,5,10, 3,5,10]", testObject.getRatingsById(2).toString());
			assertEquals("[10,5,10, 3,5,10]", testObject.getRatingsById(10).toString());

		} catch (IOException e) {
			fail("could not open file blah");
		}
	}
}
