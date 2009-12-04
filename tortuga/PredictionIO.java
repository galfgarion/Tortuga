package tortuga;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import knn.NaiveKNN;

import movieRatings.MovieRatings;

public class PredictionIO {
	
	static int LIMIT = NaiveKNN.LIMIT;
	
	final File _qualifyingSet;
	final File _outputFile;
	private PrintStream _printStream;
	final Predictor _predictor;
	
	public PredictionIO(File qualifyingSet, File outputFile, Predictor predictor) {
		_qualifyingSet = qualifyingSet;
		_predictor = predictor;
		_outputFile = outputFile;
	}
	
	/**
	 * TODO: factor out input validation, do that as separate function if necessary.
	 * @throws Exception
	 */
	public void run() throws FileNotFoundException{
		Scanner scanner = getScanner();
		openOutput();
		doPredictions(scanner);
		closeOutput();
		scanner.close();
	}
	
	private void closeOutput() {
		// TODO Auto-generated method stub
		_printStream.close();
	}

	private void openOutput() throws FileNotFoundException{
		// TODO Auto-generated method stub
		OutputStream outputStream = new FileOutputStream(_outputFile);
		_printStream = new PrintStream(outputStream);
	}

	private void doPredictions(Scanner scanner) {
		Pattern ratingPattern = Pattern.compile("(\\d+),(\\d{4}-\\d{2}-\\d{2})");
		Pattern idPattern = MovieRatings.idPattern;
		
		int userID, movieID = 0, linecounter = 0;
		boolean skip = false;
		
		while(scanner.hasNextLine()) {
			linecounter ++;
			
			String inputLine = scanner.nextLine();
			Matcher movieIdMatcher = idPattern.matcher(inputLine);
			Matcher ratingMatcher = ratingPattern.matcher(inputLine);
			
			if(movieIdMatcher.matches()) {
				movieID = Integer.parseInt(movieIdMatcher.group(1));
				
				if(movieID > LIMIT) {
					skip = true;
					continue;
				}
				skip = false;
				printMovieID(movieID);
			}
			else if(ratingMatcher.matches()) {
				if(skip) continue;
				
				assert(movieID > 0);
				
				userID = Integer.parseInt(ratingMatcher.group(1));
				float prediction = _predictor.predictRating(movieID, userID);
				//System.out.println(prediction);
				printRating(prediction);
			}
			else {
				// reached the end of valid input
				// the input may be bad
				String errorMessage = "Invalid input file " + _qualifyingSet + ", expected a movieID or rating at line " +
						linecounter + ", but input was: " + inputLine;
				System.err.println(errorMessage);
			}
		
		}
	}
	
	private void printMovieID(int movieID) {
		_printStream.println(movieID + ":");
	}

	private void printRating(float rating) {
		_printStream.println(rating);
	}

	private Scanner getScanner() throws FileNotFoundException{
		FileInputStream fstream = new FileInputStream(_qualifyingSet);
		Scanner scanner = new Scanner(fstream);
		return scanner;
	}
	
	private static class EnthusiasticRecommender implements tortuga.Predictor {
		@Override
		public float predictRating(int movieID, int userID) {
			return userID;
		}
		
	}
	
	public static void main(String[] args) {
		File qualifyingSet = new File("/vm/tortuga/download/qualifying.txt");
		Predictor predictor = new EnthusiasticRecommender();
		File outputFile = new File("/vm/tortuga/", predictor.getClass().getName() + " " + new Date().toString());
		PredictionIO predictionIO = new PredictionIO(qualifyingSet, outputFile, new EnthusiasticRecommender());
		
		try {
			predictionIO.run();
		} catch (FileNotFoundException e) {
			e.printStackTrace(System.err);
		}
	}

}
