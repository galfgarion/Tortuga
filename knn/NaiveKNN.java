package knn;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import neustore.base.LRUBuffer;
import MovieID_Ratings.MovieID_Ratings;
import MovieID_Ratings.UserRating;

public class NaiveKNN {
	
	int movieIDLimit;
	DistanceTable distanceTable = new DistanceTable();
	
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
	
	}
	
	public List<Neighbor> nearestNeighbors(int k, int movieId) {
		return nearestNeighbors(k, movieId, distanceTable);
	}
	
	/**
	 * 
	 * @param k how many nearest neighbors
	 * @param movieId for this movie
	 * @param distances the table of precomputed distances
	 * @return
	 */
	public List<Neighbor> nearestNeighbors(int k, int movieId, DistanceTable distances) {
		ArrayList<Neighbor> neighbors = new ArrayList<Neighbor>(movieIDLimit - 1);
		
		for(int neighborId=1; neighborId < movieIDLimit; neighborId++) {
			if(neighborId != movieId) {
				neighbors.add(new Neighbor(neighborId, distances.get(movieId, neighborId)));
			}
		}
		
		Collections.sort(neighbors);
		
		return neighbors.subList(0, k);
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
		
		public void put(int a, int b, double distance) {
			if(a > b) {
				table.put(new Pair(b, a).toString(), distance);
			} else {
				table.put(new Pair(a, b).toString(), distance);
			}
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
	}

}
