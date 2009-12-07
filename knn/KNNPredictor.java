package knn;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;


import tortuga.Constants;
import tortuga.PredictionIO;
import tortuga.Predictor;
import tortuga.RatingEvaluator;

public class KNNPredictor implements Predictor {
	
	public static final int K = 100;
	private NaiveKNN knn;
	
	static List<RatingSupport> averageMovieRatings = new ArrayList<RatingSupport>();
	static HashMap<Integer, RatingSupport> averageUserRatings = new HashMap<Integer, RatingSupport>();
	
	public KNNPredictor(File indexFile) throws IOException {
		//knn = new NaiveKNN(indexFile);
		
		loadMovieAverages(new File("MovieAverages.csv"));
		loadUserAverages(new File("UserAverages.csv"));
	}
	
	@Override
	public float predictRating(int movieId, int userId) {
		// TODO Auto-generated method stub
		//List<Similarity> nearestNeighbors = knn.nearestNeighbors(K, movieId);
		
		float movieAvg = averageMovieRatings.get(movieId).rating;
		float userAvg = averageUserRatings.get(userId).rating;
		
		float prediction = movieAvg + userAvg - Constants.globalAverageRating;
		
		return prediction;
	}
	
	public void loadUserAverages(File userAverages) throws IOException {
		averageUserRatings.clear();
		Reader reader = new FileReader(userAverages);
		Scanner sc = new Scanner(reader);
		sc.useDelimiter("[,\n]");
		
		int totalSupport = 0;
		
		int userId;
		float rating;
		int support;
		
		for(; sc.hasNextInt() ;) {
			userId = sc.nextInt();
			rating = sc.nextFloat();
			support = sc.nextInt();
			
			//totalSupport += support;
			
			averageUserRatings.put(userId, new RatingSupport(rating, support));
		}
		
		//System.out.println("Total support: " + totalSupport);
		
	}
	
	public void loadMovieAverages(File movieAverages) throws IOException {
		averageMovieRatings.clear();
		Scanner sc = new Scanner(new FileReader(movieAverages));
		sc.useDelimiter("[,\n]");
		
		// pad the beginning so we can look up by movieId directly
		averageMovieRatings.add(new RatingSupport(-1, -1));
		
		float rating;
		int support;
		
		for(; sc.hasNextFloat() ;) {
			rating = sc.nextFloat();
			support = sc.nextInt();
			//System.out.println(rating + "," + support);
			averageMovieRatings.add(new RatingSupport(rating, support));
		}
	}
	
	private static class RatingSupport {
		final float rating;
		final int support;
		
		public RatingSupport (float rating, int support) {
			this.rating = rating;
			this.support = support;
		}
		
		public String toString() {
			return rating + "," + support;
		}
	}
	

	public static void main(String[] args) throws IOException {
		System.out.println("Building knn ...");
		KNNPredictor predictor = new KNNPredictor(new File("/vm/tortuga/training_set_indexed3.neu"));
		System.out.println("Done");
		File qualifyingSet = new File("/vm/tortuga/download/qualifying.txt");
		File outputFile = new File("/vm/tortuga/knn_predictor_output.txt");
		
		PredictionIO predictionIO = new PredictionIO(qualifyingSet, outputFile, predictor);
		predictionIO.run();
		double rmse = RatingEvaluator.rootMeanSquaredError(outputFile, new File("/vm/tortuga/grand_prize/judging.txt"));
		System.out.println("RMSE: " + rmse);

	}
	
}
