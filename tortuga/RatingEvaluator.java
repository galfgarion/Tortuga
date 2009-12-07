package tortuga;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import movieRatings.MovieID_Ratings;
import movieRatings.MovieRatings;
import movieRatings.UserRating;
import neustore.base.LRUBuffer;

public class RatingEvaluator {
	
	private static final PrintStream _printStream = System.out;
	public static final char QUIZ = '0';
	public static final char TEST = '1';
	private static final int QUIZ_OR_TEST_POSITION = 2;
	//private static final boolean USE_QUIZ_AND_TEST_DATA = true;

	public static double rootMeanSquaredError(File qualifying, File judging) throws IOException {
		return rootMeanSquaredError(qualifying, judging, QUIZ);
	}
	
	public static double rootMeanSquaredError(File qualifying, File judging, char quizOrTest) throws IOException {
		
		Scanner qScanner = new Scanner(new FileInputStream(qualifying));
		Scanner jScanner = new Scanner(new FileInputStream(judging));
		
		int count = 0;
		double totalSquareError = 0.0;
		
		while(qScanner.hasNextLine()) {
			String qLine = qScanner.nextLine();
			String jLine = jScanner.nextLine();
			
			if(!(qLine.charAt(qLine.length() - 1) == ':')) {
				if(jLine.charAt(QUIZ_OR_TEST_POSITION) == quizOrTest) {
				
					double prediction = Double.parseDouble(qLine);
					double rating = Double.parseDouble(jLine.substring(0, 1)); // take only the first character
					double delta = rating - prediction;
					double squareError = delta * delta;
					totalSquareError += squareError;
					++count;
					
					//System.out.println(rating + " - " + prediction);
				}
			} 
			
		}
		double meanSquareError = totalSquareError / count;
		return (float) Math.sqrt(meanSquareError);
	}
	
	static void extractJudgingSet(File probeSet, File movieRatingsIndex) throws IOException {
		Pattern userIdPattern = Pattern.compile("(\\d+)");
		Pattern movieIdPattern = MovieRatings.idPattern;
		
		MovieID_Ratings index = new MovieID_Ratings(new LRUBuffer(5, 4096), movieRatingsIndex.getAbsolutePath(), 0);
		
		Scanner sc = new Scanner(probeSet);
	
		while(sc.hasNextLine()) {
			String input = sc.nextLine();
			Matcher movieIdMatcher = movieIdPattern.matcher(input);
			movieIdMatcher.matches();
			int movieId = Integer.parseInt(movieIdMatcher.group(1));
			printMovieId(movieId);
			MovieRatings trainingRatings = new MovieRatings(movieId, index); // has the ratings we want to print
			
			ArrayList<FakeUserRating> fakeTrainingRatings = new ArrayList<FakeUserRating>();
			
			
			for(UserRating rating: trainingRatings) {
				fakeTrainingRatings.add(new FakeUserRating(rating.userId, rating.rating/*, rating.date*/));
			}
			
			Collections.sort(fakeTrainingRatings);
		
			List<FakeUserRating> probeRatings = getNextProbeRatings(sc); // want to print in this order
			
			
			for(FakeUserRating rating: probeRatings) {
				rating.rating = fakeTrainingRatings.get(Collections.binarySearch(fakeTrainingRatings, rating)).rating;
				//printUserRatings(rating);
			}
			
			printUserRatings(probeRatings);
			
			//fakeTrainingRatings.retainAll();
		}
		
	}
	

	private static class FakeUserRating extends UserRating {
		public FakeUserRating(int userId, byte rating/*, int date*/) {
			super(userId, rating /*, date*/);
		}

		@Override
		public boolean equals(Object o) {
			return userId == ((FakeUserRating)o).userId;
		}
	}
	
	
	/**
	 * 
	 * @param sc Scanner
	 * @return Fake user ratings from the probe set (containing only the user Id)
	 */
	public static List<FakeUserRating> getNextProbeRatings(Scanner sc) {
		Pattern movieIdPattern = MovieRatings.idPattern;
		List<FakeUserRating> userRatings = new ArrayList<FakeUserRating>();
		
		while(sc.hasNextLine() && sc.hasNextInt()) {
			// quit once we get to the next movie ID line
			if(sc.hasNext(movieIdPattern)) {
				return userRatings;
			}
			String line = sc.nextLine();
			userRatings.add(new FakeUserRating(Integer.parseInt(line), (byte)0/*, 0*/));
		}
		
		return userRatings;
	}
	
	// precondition:  users are in the correct order
	// want to go through sequentially and throw out users that won't be in the probe judging set
	// so we make only one pass
	private static UserRating getRatingByUser(MovieRatings ratings, int userId) {
		while(!ratings.isEmpty()) {
			UserRating rating = ratings.removeFirst();
			if(rating.userId == userId) {
				return rating;
			}
		}
		return null;
	}
	
	private static  void printMovieId(int movieID) {
		_printStream.println(movieID + ":");
	}

	private static void printUserRatings(List<FakeUserRating> ratings) {
		for(UserRating rating: ratings) {
			_printStream.println(rating.userId + "," + rating.rating);
		}
	}
	
	public static void main(String[] args) throws IOException {
		File probeSet = new File("/vm/tortuga/download/probe.txt");
		File movieRatingsIndex = new File("/vm/tortuga/training_set.neu");
		
		//extractJudgingSet(probeSet, movieRatingsIndex);
		RatingEvaluatorTest test = new RatingEvaluatorTest();
		test.testTestRMSE();
	}

}


