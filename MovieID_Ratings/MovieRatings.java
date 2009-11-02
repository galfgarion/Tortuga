package MovieID_Ratings;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import database.RatingStore;

import neustore.base.LRUBuffer;


public class MovieRatings  extends junit.framework.TestCase implements Iterable<UserRating>{
	
	private int _movieID;
	private Vector<UserRating> _userRatings = new Vector<UserRating>();
	static Pattern ratingPattern = Pattern.compile("(\\d+),(\\d),(\\d{4}-\\d{2}-\\d{2})");
	static Pattern idPattern = Pattern.compile("^\\s*(\\d+):\\s*$");
	
	public MovieRatings() {
		/* TODO: empty constructor only should be used for junit test */
	}
	
	public MovieRatings(int movieID) {
		_movieID = movieID;
	}
	public MovieRatings(File ratingsFile) {
		try {
			loadFromFile(ratingsFile);
		} catch(Exception e) {
			System.err.println("Couldn't load file: " + e);
		}
	}
	
	public void loadFromFile(File file) throws Exception {
		if(file.canRead()) {
			FileInputStream fstream = new FileInputStream(file);
			Scanner scanner = new Scanner(fstream);
			
			
			scanner.useDelimiter(":");
			_movieID = scanner.nextInt();
			
			
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				Matcher matcher = ratingPattern.matcher(line);
				if(matcher.find()) {
					int userId = Integer.parseInt(matcher.group(1));
					int rating = Integer.parseInt(matcher.group(2));
					String date = matcher.group(3);
					
					// TODO: remove this HACK
					if(_userRatings.size() >= 2 /*340*/){
						break;
					} // end TODO
					
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
		out.println(_movieID + ":");
		for(UserRating rating: _userRatings) {
			out.println(rating);
		}
		
		out.close();
	}
	
	/* unit tests */
	public void testPatternMatching() {
		Matcher matcher = ratingPattern.matcher("1234,3,2009-04-12\n");
		matcher.find();
		assertEquals("1234", matcher.group(1));
		assertEquals("2009-04-12",matcher.group(3));
	}
	
	public void testIdPattern() {
		Matcher matcher = idPattern.matcher("1234:\n");
		matcher.find();
		assertEquals("1234", matcher.group(1));
	}
	
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
		
		if(args.length < 1) {
			System.out.println("Usage: <command> <ratingsDir>");
			System.exit(0);
		}
		File ratingsDir = new File(args[0]);
		if(!ratingsDir.canRead()) {
			System.err.println("Can't open file: " + ratingsDir);
			System.exit(1);
		}
		
		RatingStore store = new RatingStore(ratingsDir);
		store.createFromFile(ratingsDir);
		
	}
	
	public int getMovieID() {
		return _movieID;
	}
	
	public int size() {
		return _userRatings.size();
	}
	public void add(UserRating rating) {
		// TODO Auto-generated method stub
		_userRatings.add(rating);
	}
	
	public Vector<UserRating> getUserRatings() {
		// TODO Auto-generated method stub
		return _userRatings;
	}
	
	@Override
	public Iterator<UserRating> iterator() {
		// TODO Auto-generated method stub
		return _userRatings.iterator();
	}
}
