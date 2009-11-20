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

import database.RatingStore;

import tortuga.PredictionIO;
import tortuga.Predictor;

import movieRatings.MovieID_Ratings;
import movieRatings.MovieRatings;
import movieRatings.UserRating;
import neustore.base.LRUBuffer;

public class NaiveKNN implements Predictor {
	
	protected int movieIDLimit;
	protected DistanceTable distanceTable = new DistanceTable();
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
		MovieID_Ratings testObject = new MovieID_Ratings(new LRUBuffer(5, 4096), indexFile.getAbsolutePath(), 0);
		
		int movieID = 1; // TODO: use the total later for iterating/sorting
		for(;; movieID++){
			ArrayList<UserRating> ratings = testObject.getRatingsById(movieID);
			if(ratings == null){
				break;
			}
			
			// user ratings from movie A
			HashMap<Integer, Integer> aRatings = new HashMap<Integer, Integer>();
			
			for(UserRating r: ratings) {
				aRatings.put(r.userId, r.rating);
			}
			
 			for(int otherMovieID = movieID + 1;; otherMovieID++) {
				ArrayList<UserRating> otherRatings = testObject.getRatingsById(otherMovieID);
				
				if(otherRatings == null) {
					break;
				}
				
				// System.out.println("otherRatings.size(): " + otherRatings.size());
				
				HashMap<Integer, Integer> bRatings = new HashMap<Integer, Integer>();
				
				// now comparing one movie's set of ratings with another's
				for(UserRating r : otherRatings) {
					bRatings.put(r.userId, r.rating);
				}
				
				int ratingTotal = 0;
				int ratingCounter = 0;
				
				for(Map.Entry<Integer, Integer> entry : aRatings.entrySet()){
					if(bRatings.containsKey(entry.getKey()) ) {
						int squareDiff = entry.getValue() - bRatings.get(entry.getKey());
						squareDiff = squareDiff * squareDiff;
						ratingTotal += squareDiff;
						++ratingCounter;
					}
				}
				
				double distance = ratingTotal;
				if(ratingCounter > 0) {
					distance = (double) ratingTotal / ratingCounter;
					distanceTable.put(movieID, otherMovieID, distance);
				}
				else { // there were no users who rated both movies
					distanceTable.put(movieID, otherMovieID, Double.MAX_VALUE);
				}
			}
		}
		
		movieIDLimit = movieID;
		
		System.out.println("Done with the index file, closing ...");
		testObject.close();

		
		
		//DEBUG
		System.out.println(distanceTable);
	
	}
	
	public List<Neighbor> nearestNeighbors(int k, int movieId) {
		return nearestNeighbors(k, movieId, distanceTable);
	}
	
	/**
	 * TODO: THIS IS BROKEN!!!
	 * @param k how many nearest neighbors
	 * @param movieId for this movie
	 * @param distances the table of precomputed distances
	 * @return
	 */
	public List<Neighbor> nearestNeighbors(int k, int movieId, DistanceTable distances) {
		ArrayList<Neighbor> neighbors = new ArrayList<Neighbor>(movieIDLimit - 1);
		
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
		int sum = 0;
		// TODO: would be nice to be able to iterate through ratings in index instead of having to load index
		// and retrieve them one by one
		for(Neighbor neighbor: nearestNeighbors) {
			MovieRatings neighborRatings = new MovieRatings(neighbor.id, _indexFile);
			sum += neighborRatings.averageRating();
		}
		
		k = nearestNeighbors.size();
		
		if(k <= 0) {
			System.err.println("No nearest neighbors found for movie with movieId: " + movieId);
			return 0.0f;
		}
		assert(k > 0);
		float averageRating = (float) sum / k;
		
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
		
		private Hashtable<String, Double> table = new Hashtable<String, Double>();
		private final Set<Integer> movieIds = new HashSet<Integer>();
		
		public void put(int a, int b, double distance) {
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
		
		public double get(int a, int b) {
			int A = Math.min(a, b);
			int B = Math.max(a, b);
			
			Double distance = table.get(new Pair(A, B).toString());
			if(distance == null) {
				throw new IllegalArgumentException("No distance found for " + a + " and " + b);
			} else {
				return distance;
			}
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Distance table:\n");
			for(Entry<String, Double> entry: table.entrySet()) {
				sb.append(entry.getKey());
				sb.append(" distance: ");
				sb.append(entry.getValue());
				sb.append("\n");
			}
			return sb.toString();
		}
	}
	
	public static void main(String[] args) throws Exception {
		File inputData = new File("/vm/tortuga/fake_data");
		File indexFile = new File("/vm/tortuga/fake_data.neu");
		File outputFile = new File("/vm/tortuga/knn_fake_results.txt");
		File qualifyingSet = new File("/vm/tortuga/fake_qualifying.txt");
		
		/*
		RatingStore database = new RatingStore(indexFile);
		database.createFromFile(inputData);
		*/
		Predictor predictor = new NaiveKNN(indexFile);
		PredictionIO predictionIO = new PredictionIO(qualifyingSet, outputFile, predictor);
		
		predictionIO.run();
	}

}
