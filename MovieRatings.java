import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import neustore.base.LRUBuffer;


public class MovieRatings /* extends junit.framework.TestCase */ {
	
	int _movieId;
	ArrayList<UserRating> _userRatings;
	static Pattern ratingPattern = Pattern.compile("(\\d+),(\\d),(\\d{4}-\\d{2}-\\d{2})");
	static Pattern idPattern = Pattern.compile("^\\s*(\\d+):\\s*$");
	
	public MovieRatings() {}
	public MovieRatings(File ratingsFile) {
		try {
			loadFromFile(ratingsFile);
		} catch(Exception e) {
			System.err.println("Couldn't load file: " + e);
		}
	}
	
	public void loadFromFile(File file) throws Exception {
		_userRatings = new ArrayList<UserRating>(1000);
		if(file.canRead()) {
			FileInputStream fstream = new FileInputStream(file);
			Scanner scanner = new Scanner(fstream);
			
			
			scanner.useDelimiter(":");
			_movieId = scanner.nextInt();
			
			
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				Matcher matcher = ratingPattern.matcher(line);
				if(matcher.find()) {
					int userId = Integer.parseInt(matcher.group(1));
					int rating = Integer.parseInt(matcher.group(2));
					String date = matcher.group(3);
					_userRatings.add(new UserRating(userId, rating, date));
				} else {
					// TODO: log a warning or throw an exception
				}
			}
			
			fstream.close();
			
		}
		else {
			// TODO: log a warning or throw an exception
		}
	}
	
	public void writeToFile(File file) throws Exception{
		PrintStream out = new PrintStream(file);
		out.println(_movieId + ":");
		for(UserRating rating: _userRatings) {
			out.println(rating);
		}
		
		out.close();
	}
	
	/* unit tests */
	/* public void testPatternMatching() {
		Matcher matcher = ratingPattern.matcher("1234,3,2009-04-12\n");
		matcher.find();
		assertEquals("1234", matcher.group(1));
		assertEquals("2009-04-12",matcher.group(3));
	}
	
	public void testIdPattern() {
		Matcher matcher = idPattern.matcher("1234:\n");
		matcher.find();
		assertEquals("1234", matcher.group(1));
	} */
	
	/**
	 * This test just reads and writes a file
	 * as sort of an end to end test.  To make sure the
	 * data was read/written correctly, do a
	 * diff from the command line after the test runs
	 * 
	 */
	public void testWrite() {
		
		MovieRatings movie = new MovieRatings(new File("data/test.txt"));
		try {
		movie.writeToFile(new File("data/testwrite.txt"));
		} catch(Exception e) {}
	}
	
	public static void main(String[] args) {
		ArrayList<MovieRatings> movieRatingsList = new ArrayList<MovieRatings>();
		
		if(args.length < 1) {
			System.out.println("Usage: <command> <ratingsDir>");
			System.exit(0);
		}
		File ratingsDir = new File(args[0]);
		if(!ratingsDir.canRead()) {
			System.err.println("Can't open file: " + ratingsDir);
			System.exit(1);
		}
		if(ratingsDir.isDirectory()) {
			File[] movieFiles = ratingsDir.listFiles();
			for(File file: movieFiles) {
				movieRatingsList.add(new MovieRatings(file));
			}
		}
		
		for(MovieRatings movieRatings: movieRatingsList) {
			System.out.println("Movie Id: " + movieRatings._movieId);
			for(UserRating userRating: movieRatings._userRatings) {
				System.out.println("\tUser: " + userRating.userId + " Rating: " + userRating.rating + " Date: " + userRating.date);
			}
		}
		
		// Write the movie ratings into an index
		MovieID_Ratings.MovieID_Ratings index = null;
		try{
			index = new MovieID_Ratings.MovieID_Ratings(new LRUBuffer (5, 4096), "ratings.index", 1);
			for(MovieRatings movieRatings: movieRatingsList) {
				MovieID_Ratings.MovieRatings ratingsEntry = new MovieID_Ratings.MovieRatings(movieRatings._movieId);
				for(UserRating rating: movieRatings._userRatings) {
					ratingsEntry.UserRatings.add(new MovieID_Ratings.Rating(rating.userId, rating.rating, 0));
				}
				
				// TODO: don't truncate the list once we have page spanning
				ratingsEntry.UserRatings =  new Vector<MovieID_Ratings.Rating>(ratingsEntry.UserRatings.subList(0, 93));
				index.insertEntry(ratingsEntry);
			}
			index.close();
		} catch(Exception e) {
			// TODO: handle exceptions in a less retarded way.
			System.err.println("Insert F##Xed up");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
