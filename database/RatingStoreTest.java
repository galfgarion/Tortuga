package database;

import java.io.File;
import java.io.IOException;

import movieRatings.MovieID_Ratings;
import neustore.base.LRUBuffer;

import junit.framework.TestCase;

public class RatingStoreTest extends TestCase {
	
	public void testCreateFromFiles() throws IOException {
		File testIndexName = new File("test.index");
		File testDir = new File("data");
		
		RatingStore index = new RatingStore(testIndexName);
		index.createFromFile(testDir);
		
		MovieID_Ratings testObject;
		try {
			testObject = new MovieID_Ratings(new LRUBuffer (5, 4096), "test.index", 0);
			assertEquals(1488844, testObject.getRatingsById(1).get(0).userId);
			// testObject.close();
			
			
			testObject = new MovieID_Ratings(new LRUBuffer (5, 4096), "test.index", 0);
			assertEquals(1488844, testObject.getRatingsById(1).get(0).userId);
			// testObject.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("WTFBBQ?");
			e.printStackTrace();
		}
		
	}

}
