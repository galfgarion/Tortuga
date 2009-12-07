package knn;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import tortuga.PredictionIO;
import tortuga.Predictor;

import movieRatings.EfficientMovieRatings;
import movieRatings.MovieID_Ratings;
import neustore.base.LRUBuffer;

public class NaiveKNN implements Predictor {
	
	// 100: 0m42s
	public final static int LIMIT = 1000, K = 500;
	protected int movieIDLimit;
	protected final ArrayList<Float> averageRatingTable = new ArrayList<Float>(LIMIT + 1);
	private File _indexFile;
	
	/**
	 * For testing only
	 * @param limit
	 */
	protected void setMovieIDLimit(int limit) {
		movieIDLimit = limit;
	}
	
	/**
	 * For testing only
	 */
	protected NaiveKNN() {
		
	}
	
	public NaiveKNN(File indexFile) throws Exception {
		_indexFile = indexFile;
		
		System.out.println("Opening the index file ...");
		MovieID_Ratings testObject = new MovieID_Ratings(new LRUBuffer(5, 4096), indexFile.getAbsolutePath(), 0);
		System.out.println(" done.");
		averageRatingTable.add(-1f); // pad the rating table with an invalid value in position zero so we can look up by movieID
		int movieID = 1; // TODO: use the total later for iterating/sorting
		EfficientMovieRatings ratings, otherRatings;
		ArrayList<Similarity> similarityScores = new ArrayList<Similarity>();
		
		for(; movieID < LIMIT; movieID++){ // TODO, take off limit
			// System.out.println("Populating distance table for movie " +  movieID);
			similarityScores.clear();
			ratings = testObject.getRatingsById(movieID);
			
			if(ratings == null){
				break;
			}
			
			// populate average rating table
			float sum = 0.0f;
			for(byte r : ratings.Rating) {
				sum += r;
			}
			assert(ratings.numRatingsStored > 0);
			float avg = sum / ratings.numRatingsStored;
			averageRatingTable.add(avg);
			
 			for(int otherMovieID = 1; otherMovieID < LIMIT; otherMovieID++) {
 				if(movieID == otherMovieID)
 					continue;
				otherRatings = testObject.getRatingsById(otherMovieID);
				
				// System.out.println("Comparing movie " + movieID + " to movie " + otherMovieID);
				
				if(otherRatings == null) {
					break;
				}
				
				int ratingTotal = 0;
				int ratingCounter = 0;
				
				int y = 0;
				int squareDiff;
				for(int x = 0; x < ratings.numRatingsStored; x++)
					for(; y < otherRatings.numRatingsStored; y++) {
						if(ratings.UserID[x] == otherRatings.UserID[y]) {
							squareDiff = ratings.Rating[x] - otherRatings.Rating[y];
							squareDiff = squareDiff * squareDiff;
							ratingTotal += squareDiff;
							++ratingCounter;
							++y;
							break;
						} else if (ratings.UserID[x] < otherRatings.UserID[y])
							break;
					}
				
				float distance = (float) ratingTotal / ratingCounter;
				if(ratingCounter > 0) {
					similarityScores.add(new Similarity(otherMovieID, (distance+.05f) * (ratingCounter + 100) / ratingCounter));
				}
				else // not enough users rated both movies
					continue;
				
			}
 			
 			Collections.sort(similarityScores);
 			similarityScores.subList(0, K);
 			System.out.println(similarityScores);
		}
		
		movieIDLimit = movieID;
		
		//System.out.println("Done with the index file, closing ...");
		//testObject.close();

		//DEBUG
		
		/* System.out.println(distanceTable);
		System.out.println("done printing distance table.");
		System.out.println(averageRatingTable);
		System.out.println("done printing average rating table."); */
	}
	
	/**
	 * @param k how many nearest neighbors
	 * @param movieId for this movie
	 * @param distances the table of precomputed distances
	 * @return
	 */
	public List<Similarity> nearestNeighbors(int k, int movieId) {
		return null;
	}
	
	/**
	 * 
	 * @param userId
	 * @param movieId
	 * @return the average of average ratings of the k nearest neighbors
	 */
	public float predictRating(int movieId, int userId) {
		assert(_indexFile != null);
		int k = 5;
		List<Similarity> nearestNeighbors = nearestNeighbors(k, movieId);
		float sum = 0;
		
		//System.out.println("Nearest neighbors for " + movieId + ":" );
		
			
		for(Similarity neighbor: nearestNeighbors) {
			float neighborAvg = averageRatingTable.get(neighbor.MovieID);
			//System.out.println("\tdistance for neighbor " + neighbor.id + ": " + neighbor.distance);
			sum += neighborAvg;
		}
		
		k = nearestNeighbors.size();
		
		if(k <= 0) {
			System.err.println("No nearest neighbors found for movie with movieId: " + movieId);
			return 0.0f;
		}
		assert(k > 0);
		float averageRating = sum / k;
		
		return averageRating;
	}
	
	protected class Similarity implements Comparable<Similarity >{
		public int MovieID;
		public float Distance;
		
		public Similarity(int MovieID, float Distance) {
			this.MovieID = MovieID;
			this.Distance = Distance;
		}
		
		public int compareTo(Similarity s) {
			if(Distance < s.Distance)
				return -1;
			return 1;
		}
		
		public String toString() {
			return MovieID + "/" + Distance;
		}
	}
	
	public static void main(String[] args) throws Exception {
		//File inputData = new File("fake_data");
		File workingDir = new File("/tmp/");
		File indexFile = new File(workingDir, "training_set_indexed3.neu");
		File outputFile = new File(workingDir, "naiveknn_results.txt");
		File qualifyingSet = new File(workingDir, "tortuga/download/qualifying.txt");
		
		//RatingStore database = new RatingStore(indexFile);
		//database.createFromFile(inputData);
		
		long time = System.currentTimeMillis();
		Predictor predictor = new NaiveKNN(indexFile);
		System.out.println("Done making NaiveKNN");
		System.out.println("Writing out predictions to " + outputFile);
		PredictionIO predictionIO = new PredictionIO(qualifyingSet, outputFile, predictor);
		System.out.println("Total execution time: " + (System.currentTimeMillis() - time) + " ms");
		
		predictionIO.run();
	}

}
