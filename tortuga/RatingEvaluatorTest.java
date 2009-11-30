package tortuga;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class RatingEvaluatorTest extends TestCase {
	
	public void testQuizRMSE() throws IOException {
		File grandPrizeFolder = new File("/vm/tortuga/grand_prize/");
		File qualifying = new File(grandPrizeFolder, "winning_submission");
		File judging = new File(grandPrizeFolder, "judging.txt");
		System.out.println(RatingEvaluator.rootMeanSquaredError(qualifying, judging, RatingEvaluator.QUIZ));
	}
	
	public void testTestRMSE() throws IOException {
		File grandPrizeFolder = new File("/vm/tortuga/grand_prize/");
		File qualifying = new File(grandPrizeFolder, "winning_submission");
		File judging = new File(grandPrizeFolder, "judging.txt");
		System.out.println(RatingEvaluator.rootMeanSquaredError(qualifying, judging, RatingEvaluator.TEST));
	}

}
