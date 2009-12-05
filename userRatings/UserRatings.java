package userRatings;

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


public class UserRatings implements Iterable<MovieRating>, Comparable<UserRatings> {
	
	private int _userID;
	public final ArrayList<MovieRating> _movieRatings = new ArrayList<MovieRating>();
	public static Pattern ratingPattern = Pattern.compile("(\\d+),(\\d),(\\d{4}-\\d{2}-\\d{2})");
	public static Pattern idPattern = Pattern.compile("^\\s*(\\d+):\\s*$");
	
	/**
	 * Should only be used for testing
	 */
	UserRatings() {
	}
	
	/**
	 * Should only be used for testing
	 * @param movieID
	 */
	public UserRatings(int movieID) {
		_userID = movieID;
	}
	
	/**
	 * TODO 
	 * Loads a set of ratings for movie with id movieId from a NeuStore index file ratingsIndex
	 * @param movieId
	 * @param ratingsIndex
	 */
	public UserRatings(int userID, File indexFile) {
		try {
			UserID_Ratings ratingsIndex = new UserID_Ratings(new LRUBuffer(5, 4096), indexFile.getAbsolutePath(), 0);
			_userID = userID;
			_movieRatings.addAll(ratingsIndex.getRatingsById(userID));
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}
	
	public UserRatings(int movieID, UserID_Ratings ratingsIndex) {
		_userID = movieID;
		_movieRatings.addAll(ratingsIndex.getRatingsById(movieID));
	}
	
	public UserRatings(File ratingsFile) {
		try {
			loadFromFile(ratingsFile);
		} catch(Exception e) {
			System.err.println("Couldn't load file: " + e);
		}
	}
	
	public float averageRating() {
		if(_movieRatings.isEmpty()) {
			return 0.0f;
		}
		int size = _movieRatings.size();
		int sum = 0;
		for(MovieRating rating: _movieRatings) {
			sum += rating.rating;
		}
		
		return (float)sum / size;
	}
	
	public void loadFromFile(File file) throws Exception {
		if(file.canRead()) {
			FileInputStream fstream = new FileInputStream(file);
			Scanner scanner = new Scanner(fstream);
			
			
			scanner.useDelimiter(":");
			_userID = scanner.nextInt();
			
			
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				Matcher matcher = ratingPattern.matcher(line);
				if(matcher.find()) {
					int userId = Integer.parseInt(matcher.group(1));
					byte rating = (byte)Integer.parseInt(matcher.group(2));
					String date = matcher.group(3);
					
					_movieRatings.add(new MovieRating(userId, rating /*, date*/));
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
		out.println(_userID + ":");
		for(MovieRating rating: _movieRatings) {
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
		return _userID;
	}
	
	public int size() {
		return _movieRatings.size();
	}
	public void add(MovieRating rating) {
		// TODO Auto-generated method stub
		_movieRatings.add(rating);
	}
	
	public ArrayList<MovieRating> getUserRatings() {
		// TODO Auto-generated method stub
		return _movieRatings;
	}
	
	// return the user rating for user
	// precondition: the rating exists in the list of user ratings
	public MovieRating getRatingByUser(int userId) {
		for(MovieRating rating : _movieRatings) {
			if(rating.movieId == userId) {
				return rating;
			}
		}
		
		throw new IllegalStateException("Precondition violated:  the user id " + " does not exist in the list of ratings");
	}
	
	@Override
	public Iterator<MovieRating> iterator() {
		// TODO Auto-generated method stub
		return _movieRatings.iterator();
	}
	
	public boolean remove(Object o) {
		return _movieRatings.remove(o);
	}
	
	public boolean isEmpty() {
		return _movieRatings.isEmpty();
	}
	
	public MovieRating removeFirst() {
		MovieRating item = _movieRatings.get(0);
		_movieRatings.remove(0);
		return item;
	}

	public int compareTo(UserRatings o) {
		return this._userID - o._userID;
	}
	
	public boolean equals(Object o) {
		UserRatings c = (UserRatings)o;
		return c._userID == this._userID;
	}
}
