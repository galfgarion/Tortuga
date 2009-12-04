package knn;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;


import tortuga.PredictionIO;
import tortuga.Predictor;

import movieRatings.MovieID_Ratings;
import movieRatings.UserRating;
import neustore.base.LRUBuffer;

public class NaiveKNN implements Predictor {
	
	// 100: 0m42s
	public final static int LIMIT = 100;
	protected int movieIDLimit;
	protected DistanceTable distanceTable = new DistanceTable();
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
		for(; movieID < LIMIT; movieID++){ // TODO, take off limit
			System.out.println("Populating distance table for movie " +  movieID);
			ArrayList<UserRating> ratings = testObject.getRatingsById(movieID);
			if(ratings == null){
				break;
			}
			
			// populate average rating table
			float sum = 0.0f;
			for(UserRating r: ratings) {
				sum += r.rating;
			}
			assert(ratings.size() > 0);
			float avg = sum / ratings.size();
			averageRatingTable.add(avg);
			
			
			// user ratings from movie A
			HashMap<Integer, Byte> aRatings = new HashMap<Integer, Byte>();
			
			for(UserRating r: ratings) {
				aRatings.put(r.userId, r.rating);
			}
			
 			for(int otherMovieID = movieID + 1; otherMovieID < LIMIT; otherMovieID++) {
				ArrayList<UserRating> otherRatings = testObject.getRatingsById(otherMovieID);
				
				System.out.println("Comparing movie " + movieID + " to movie " + otherMovieID);
				
				if(otherRatings == null) {
					break;
				}
				
				// System.out.println("otherRatings.size(): " + otherRatings.size());
				
				HashMap<Integer, Byte> bRatings = new HashMap<Integer, Byte>();
				
				// now comparing one movie's set of ratings w2.7394366, 3.ith another's
				for(UserRating r : otherRatings) {
					bRatings.put(r.userId, r.rating);
				}
				
				int ratingTotal = 0;
				int ratingCounter = 0;
				
				for(Map.Entry<Integer, Byte> entry : aRatings.entrySet()){
					if(bRatings.containsKey(entry.getKey()) ) {
						int squareDiff = entry.getValue() - bRatings.get(entry.getKey());
						squareDiff = squareDiff * squareDiff;
						ratingTotal += squareDiff;
						++ratingCounter;
						//System.exit(1);
					}
				}
				float distance = ratingTotal;
				if(ratingCounter > 0) {
					distance = (float) ratingTotal / ratingCounter;
					distanceTable.put(movieID, otherMovieID, distance);
				}
				else { // there were no users who rated both movies
					distanceTable.put(movieID, otherMovieID, Float.MAX_VALUE);
				}
			}
		}
		
		movieIDLimit = movieID;
		
		//System.out.println("Done with the index file, closing ...");
		//testObject.close();

		//DEBUG
		
		System.out.println(distanceTable);
		System.out.println("done printing distance table.");
		System.out.println(averageRatingTable);
		System.out.println("done printing average rating table.");
	
	}
	
	public List<Neighbor> nearestNeighbors(int k, int movieId) {
		return nearestNeighbors(k, movieId, distanceTable);
	}
	
	/**
	 * @param k how many nearest neighbors
	 * @param movieId for this movie
	 * @param distances the table of precomputed distances
	 * @return
	 */
	public List<Neighbor> nearestNeighbors(int k, int movieId, DistanceTable distances) {
		List<Neighbor> neighbors = new ArrayList<Neighbor>(movieIDLimit - 1);
		
		for(int neighborId : distances.getMovieIds()) {
			if(neighborId != movieId) {
				neighbors.add(new Neighbor(neighborId, distances.get(movieId, neighborId)));
			}
		}
		
		Collections.sort(neighbors);
		
		if(neighbors.size() <= k) {
			return neighbors;
		}
		
		assert(neighbors.size() > k);
		
		return neighbors.subList(0, k);
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
		List<Neighbor> nearestNeighbors = nearestNeighbors(k, movieId);
		float sum = 0;
		
		//System.out.println("Nearest neighbors for " + movieId + ":" );
		
			
		for(Neighbor neighbor: nearestNeighbors) {
			float neighborAvg = averageRatingTable.get(neighbor.id);
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
	
	private static class Pair {
		public final int a, b;
		public Pair(int A, int B) {
			this.a = A;
			this.b = B;
		}
		
		public boolean equals(Object o) {
			if(!(o instanceof Pair)) {
				return false;
			}
			
			return a == ((Pair)o).a && b == ((Pair)o).b;
			
		}
		
		public String toString() {
			return "(" + a + ", " + b + ")";
		}
	}
	
	protected static class DistanceTable {
		
		private Hashtable<String, Float> table = new Hashtable<String, Float>();
		private final Set<Integer> movieIds = new HashSet<Integer>();
		
		public void put(int a, int b, float distance) {
			if(a > b) {
				table.put(new Pair(b, a).toString(), distance);
			} else {
				table.put(new Pair(a, b).toString(), distance);
			}
			
			movieIds.add(a);
			movieIds.add(b);
		}
		
		public Set<Integer> getMovieIds() {
			return movieIds;
		}
		
		public float get(int a, int b) {
			int A = Math.min(a, b);
			int B = Math.max(a, b);
			
			Float distance = table.get(new Pair(A, B).toString());
			if(distance == null) {
				return Float.MAX_VALUE;
			} else {
				return distance;
			}
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Distance table:\n");
			
			for(Entry<String, Float> entry: table.entrySet()) {
				sb.append(entry.getKey());
				sb.append(" distance: ");
				sb.append(entry.getValue());
				sb.append("\n");
			}
			/*
			for(int i=1; i <= LIMIT; i++) {
				for(int j=i+1; j <= LIMIT; j++) {
					sb.append("(" + i + "," + j + ")");
					sb.append(" distance: " + this.get(i, j) + "\n");
				}
			}
			*/
			return sb.toString();
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
		
		Predictor predictor = new NaiveKNN(indexFile);
		System.out.println("Done making NaiveKNN");
		System.out.println("Writing out predictions to " + outputFile);
		PredictionIO predictionIO = new PredictionIO(qualifyingSet, outputFile, predictor);
		
		predictionIO.run();
	}

}
