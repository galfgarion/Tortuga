package movieRatings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import neustore.base.LRUBuffer;

import database.RatingStore;

public class MovieRatings implements Iterable<UserRating>{
	
	private int _movieID;
	protected final ArrayList<UserRating> _userRatings = new ArrayList<UserRating>();
	protected final ArrayList<EfficientMovieRatings> _userRatingsReadback = new ArrayList<EfficientMovieRatings>();
	public static Pattern ratingPattern = Pattern.compile("(\\d+),(\\d),(\\d{4}-\\d{2}-\\d{2})");
	public static Pattern idPattern = Pattern.compile("^\\s*(\\d+):\\s*$");
	
	/**
	 * Should only be used for testing
	 */
	MovieRatings() {
	}
	
	/**
	 * Should only be used for testing
	 * @param movieID
	 */
	MovieRatings(int movieID) {
		_movieID = movieID;
	}
	
	/**
	 * TODO 
	 * Loads a set of ratings for movie with id movieId from a NeuStore index file ratingsIndex
	 * @param movieId
	 * @param ratingsIndex
	 */
	public MovieRatings(int movieID, File indexFile) {
		try {
			MovieID_Ratings ratingsIndex = new MovieID_Ratings(new LRUBuffer(5, 4096), indexFile.getAbsolutePath(), 0);
			_movieID = movieID;
			_userRatingsReadback.add(ratingsIndex.getRatingsById(movieID));
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}
	
	public MovieRatings(int movieID, MovieID_Ratings ratingsIndex) {
		_movieID = movieID;
		_userRatingsReadback.add(ratingsIndex.getRatingsById(movieID));
	}
	
	public MovieRatings(File ratingsFile) {
		try {
			loadFromFile(ratingsFile);
		} catch(Exception e) {
			System.err.println("Couldn't load file: " + e);
		}
	}
	
	public float averageRating() {
		if(_userRatings.isEmpty()) {
			return 0.0f;
		}
		int size = _userRatings.size();
		int sum = 0;
		for(UserRating rating: _userRatings) {
			sum += rating.rating;
		}
		
		return (float)sum / size;
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
					byte rating = (byte)Integer.parseInt(matcher.group(2));
					// String date = matcher.group(3);
					
					_userRatings.add(new UserRating(userId, rating /*, date*/));
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
		try {
			store.createFromFile(ratingsDir);
		} catch(IOException e) {
			System.out.println("file not found in unique identifier kfwekakerw!lel1shurf");
		}
		
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
	
	public ArrayList<UserRating> getUserRatings() {
		// TODO Auto-generated method stub
		return _userRatings;
	}
	
	// return the user rating for user
	// precondition: the rating exists in the list of user ratings
	public UserRating getRatingByUser(int userId) {
		for(UserRating rating : _userRatings) {
			if(rating.userId == userId) {
				return rating;
			}
		}
		
		throw new IllegalStateException("Precondition violated:  the user id " + " does not exist in the list of ratings");
	}
	
	@Override
	public Iterator<UserRating> iterator() {
		// TODO Auto-generated method stub
		return _userRatings.iterator();
	}
	
	public boolean remove(Object o) {
		return _userRatings.remove(o);
	}
	
	public boolean isEmpty() {
		return _userRatings.isEmpty();
	}
	
	public UserRating removeFirst() {
		UserRating item = _userRatings.get(0);
		_userRatings.remove(0);
		return item;
	}
}
