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

import movieRatings.MovieRatings;

public class PredictionIO {
	
	final File _qualifyingSet;
	private PrintStream _printStream;
	final Predictor _predictor;
	
	public PredictionIO(File qualifyingSet, Predictor predictor) {
		_qualifyingSet = qualifyingSet;
		_predictor = predictor;
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
		File outputFile = new File(Predictor.class.getName() + new Date().toString());
		OutputStream outputStream = new FileOutputStream(outputFile);
		_printStream = new PrintStream(outputStream);
	}

	private void doPredictions(Scanner scanner) {
		Pattern ratingPattern = Pattern.compile("(\\d+),(\\d{4}-\\d{2}-\\d{2})");
		Pattern idPattern = MovieRatings.idPattern;
		
		int userID, movieID = 0, linecounter = 0;
		
		while(scanner.hasNextLine()) {
			linecounter ++;
			
			String inputLine = scanner.nextLine();
			Matcher movieIdMatcher = idPattern.matcher(inputLine);
			Matcher ratingMatcher = ratingPattern.matcher(inputLine);
			
			if(movieIdMatcher.matches()) {
				movieID = Integer.parseInt(movieIdMatcher.group(1));
				printMovieID(movieID);
			}
			else if(ratingMatcher.matches()) {
				assert(movieID > 0);
				userID = Integer.parseInt(ratingMatcher.group(1));
				printRating(_predictor.predictRating(movieID, userID));
			}
			else {
				// reached the end of valid input
				// the input may be bad
				String errorMessage = "Invalid input file, expected a movieID or rating at line " +
						linecounter + ", but input was: " + inputLine;
				System.err.println(errorMessage);
			}
		}
	}
	
	private void printMovieID(int movieID) {
		_printStream.println(movieID + ":");
	}

	private void printRating(float rating) {
		// TODO Auto-generated method stub
		_printStream.println(rating);
	}

	private Scanner getScanner() throws FileNotFoundException{
		FileInputStream fstream = new FileInputStream(_qualifyingSet);
		Scanner scanner = new Scanner(fstream);
		return scanner;
	}

}
