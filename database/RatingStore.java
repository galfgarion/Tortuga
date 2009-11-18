package database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import movieRatings.MovieID_Ratings;
import movieRatings.MovieRatings;
import neustore.base.LRUBuffer;

public class RatingStore {
	
	private final File indexFile;
	
	public RatingStore(File indexFile) {
		this.indexFile = indexFile;
	}
	
	public File getIndexFile() {
		return indexFile.getAbsoluteFile();
	}
	
	/**
	 * Create a new index by loading from a file, or, if a directory is given, from all suitable
	 * files within the directory
	 * 
	 * @param ratingsFile
	 */
	public void createFromFile(File ratingsFile) throws FileNotFoundException {
		loadFromFile(ratingsFile, true);
	}
	/**
	 * Append to an existing by loading from a file, or, if a directory is given, from all suitable
	 * files within the directory
	 * 
	 * @param ratingsFile
	 */
	public void appendFromFile(File ratingsDirectory) throws FileNotFoundException {
		loadFromFile(ratingsDirectory, false);
	}
	
	/* strict precondition: ratingsDirectory is a directory */
	private void loadFromFile(File ratingsDirectory, boolean create) throws FileNotFoundException {
		MovieID_Ratings index = null;
		try {
			index = new MovieID_Ratings(new LRUBuffer (5, 4096), indexFile.getAbsolutePath(), create? 1 : 0);
		} catch(IOException e) {
			System.err.println("IOException while populating MovieID_Ratings index");
			System.exit(0);
		}
		
		if(!ratingsDirectory.canRead()) {
			System.err.println("Can't open file: " + ratingsDirectory);
			throw new FileNotFoundException();
		}
		
		MovieRatings currentMovieRatings;
		try {
			File[] movieFiles = ratingsDirectory.listFiles();
			
			int currentMovie = 1;
			for(File file: movieFiles) {
				if(currentMovie % 100 == 0)
					System.out.println("Reading Movie #" + currentMovie);
				currentMovie++;
				currentMovieRatings = new MovieRatings(file);
				index.insertEntry(currentMovieRatings);
			}
	
			index.close();
		} catch (IOException e) {
			System.err.println("input \"directory\" not a directory at all _or_ another input error on dataset read blurfy hurp");
			System.exit(0);
		}
		
		/* for(MovieRatings movieRatings: movieRatingsList) {
			System.out.println("Movie Id: " + movieRatings.getMovieID());
			for(UserRating userRating: movieRatings) {
				System.out.println("\tUser: " + userRating.userId + " Rating: " + userRating.rating + " Date: " + userRating.date);
			}
		} */
	}

}
