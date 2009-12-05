package userRatings;

import java.io.File;
import java.util.regex.Matcher;

import junit.framework.TestCase;

public class UserRatingsTest extends TestCase {
	
	public void testAverageRating() {
		UserRatings testRatings = new UserRatings();
		
		assertEquals(0.0f, testRatings.averageRating());
		testRatings._movieRatings.add(new MovieRating(1, (byte)3, 0));
		assertEquals(3.0f, testRatings.averageRating());
		testRatings._movieRatings.add(new MovieRating(1, (byte)4, 0));
		assertEquals(3.5f, testRatings.averageRating());
	}
	
	/* unit tests */
	public void testPatternMatching() {
		Matcher matcher = UserRatings.ratingPattern.matcher("1234,3,2009-04-12\n");
		matcher.find();
		assertEquals("1234", matcher.group(1));
		assertEquals("2009-04-12",matcher.group(3));
	}
	
	public void testIdPattern() {
		Matcher matcher = UserRatings.idPattern.matcher("1234:\n");
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
		
		UserRatings movie = new UserRatings(new File("data/test.txt"));
		try {
		movie.writeToFile(new File("data/testwrite.txt"));
		} catch(Exception e) {}
	}
	

}
