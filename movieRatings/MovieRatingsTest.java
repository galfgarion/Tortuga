package movieRatings;

import java.io.File;
import java.util.regex.Matcher;

import junit.framework.TestCase;

public class MovieRatingsTest extends TestCase {
	
	public void testAverageRating() {
		MovieRatings testRatings = new MovieRatings();
		
		assertEquals(0.0f, testRatings.averageRating());
		testRatings._userRatings.add(new UserRating(1, 3, 0));
		assertEquals(3.0f, testRatings.averageRating());
		testRatings._userRatings.add(new UserRating(1, 4, 0));
		assertEquals(3.5f, testRatings.averageRating());
	}
	
	/* unit tests */
	public void testPatternMatching() {
		Matcher matcher = MovieRatings.ratingPattern.matcher("1234,3,2009-04-12\n");
		matcher.find();
		assertEquals("1234", matcher.group(1));
		assertEquals("2009-04-12",matcher.group(3));
	}
	
	public void testIdPattern() {
		Matcher matcher = MovieRatings.idPattern.matcher("1234:\n");
		matcher.find();
		assertEquals("1234", matcher.group(1));
	}
	
	/**
	 * This test just reads and writes a file
	 * as sort of an end to end test.  To make sure the
	 * data was read/written correctly, do a
	 * diff from the command line after the test runs
	 */
	public void testWrite() {
		
		MovieRatings movie = new MovieRatings(new File("data/test.txt"));
		try {
		movie.writeToFile(new File("data/testwrite.txt"));
		} catch(Exception e) {}
	}
	

}
