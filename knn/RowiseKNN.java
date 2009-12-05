package knn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import neustore.base.LRUBuffer;

import tortuga.PredictionIO;
import tortuga.Predictor;

import movieRatings.MovieID_Ratings;
import movieRatings.MovieRatings;
import movieRatings.UserRating;

/**
 * This class computes the full matrix, but stores to disk
 * 
 * @author ntblack
 *
 */
public class RowiseKNN {
	
	//private boolean _isDistanceTableComputed = false;
	private static final int MAX_MOVIE_ID = 100;
	
	private static final int THRESHOLD = 10;
	
	private static final float[][] distanceTable = new float[MAX_MOVIE_ID + 1][MAX_MOVIE_ID + 1];
	
	private final MovieID_Ratings ratingsIndex;
	
	private final List<List<UserRating>> movieRatings = new ArrayList<List<UserRating>>(MAX_MOVIE_ID + 1);
	
	public RowiseKNN(File indexFile) throws IOException {
		System.out.println("Opending index file: " + indexFile);
		ratingsIndex = new MovieID_Ratings(new LRUBuffer(5, 4096), indexFile.getAbsolutePath(), 0);
		System.out.println("done.");
		System.out.println("Loading movie ratings ...");
		loadMovieRatings();
		System.out.println("done.");
		System.out.println("Sorting movie ratings ...");
		sortMovieRatings();
		System.out.println("done.");
		System.out.println("Building distances table for " + MAX_MOVIE_ID + " movies ...");
		computeMovieDistances(ratingsIndex, MAX_MOVIE_ID);
		System.out.println("done.");
		
		/*
		// print out movie rating 1 to see if it is in the correct order
		System.out.println("Movie rating 1: ");
		for(UserRating rating : movieRatings.get(1)) {
			System.out.println(rating);
		}
		*/
	}
	
	
	/**
	 * Load all the movie ratings into memory
	 */
	private void loadMovieRatings() {
		
		movieRatings.add(0, null);
		
		for(int movieId = 1; movieId <= MAX_MOVIE_ID; movieId++) {
			movieRatings.add(movieId, ratingsIndex.getRatingsById(movieId));
		}
	}
	
	/**
	 * Sort individual movie ratings by user id
	 */
	private void sortMovieRatings() {
		for(int movieId = 1; movieId <= MAX_MOVIE_ID; movieId++) {
			Collections.sort(movieRatings.get(movieId), UserRating.CompareByUserId);
		}
	}
	
	private float computeMovieDistance(int firstId, int secondId, List<UserRating> first, List<UserRating> second) {
		
		System.out.println("Computing distance from " + firstId + "to " + secondId);
		
		int matchCount = 0;
		int diffTotal = 0;
		
		// return zero distance if we are comparing the same movie
		if(firstId == secondId) {
			return 0.0f;
		}
		
		// PRECONDITION the ratings are sorted ascending by userId
		
		for(UserRating rating : first) {
			for(UserRating potentialMatch : second) {
				if(rating.userId < potentialMatch.userId) {
					break;
				} else if(rating.userId == potentialMatch.userId) {
					// if the movies were rated by the same user, they are a match
					// then do the calculation
					matchCount ++;
					int diff = rating.rating - potentialMatch.rating;
					int squareDiff = diff * diff;
					diffTotal += squareDiff;
				}
			}
		}
		
		// if we didn't find enough matches, just say the movies are infinitely distant
		// TODO we could get a confidence value here but would need to box up the confidence and distance together
		if(matchCount < THRESHOLD) {
			return Float.MAX_VALUE;
		}
		
		assert(matchCount >= THRESHOLD);
		return (float)diffTotal / matchCount;
		
	}
	
	private void computeMovieDistances(MovieID_Ratings ratingsIndex, int maxMovieId) {
		for(int firstId = 1; firstId <= maxMovieId; firstId++) {
			for(int secondId = 1; secondId <= maxMovieId; secondId++) {
				List<UserRating> first = movieRatings.get(firstId);
				List<UserRating> second = movieRatings.get(secondId);
			
				float distance = computeMovieDistance(firstId, secondId, first, second);
				distanceTable[firstId][secondId] = distance;
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		//File inputData = new File("fake_data");
		File workingDir = new File("/vm/");
		File indexFile = new File(workingDir, "tortuga/training_set_indexed2.neu");
		File outputFile = new File(workingDir, "naiveknn_results.txt");
		File qualifyingSet = new File(workingDir, "tortuga/download/qualifying.txt");
		
		//RatingStore database = new RatingStore(indexFile);
		//database.createFromFile(inputData);
		
		RowiseKNN knn = new RowiseKNN(indexFile);
		//Predictor predictor = new RowiseKNN(indexFile);
		System.out.println("Done making RowiseKNN");
		//System.out.println("Writing out predictions to " + outputFile);
		//PredictionIO predictionIO = new PredictionIO(qualifyingSet, outputFile, predictor);
		
		//predictionIO.run();
	}

}
