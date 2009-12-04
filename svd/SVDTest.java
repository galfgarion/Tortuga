package svd;

import java.io.File;

public class SVDTest {
	
	public static void printString(float value){
		System.out.println("Prediction: " + value);
	}
	
	public static void main(String[] args){
		
		File index = new File(args[0]);
		String svdFile = args[1];
		int k = 17770;
		
		int userID = Integer.parseInt(args[2]);
		int movieID = Integer.parseInt(args[3]);
		
		SVDPredictor work = null;

		try{
			new SVDBuilder(index, svdFile);
			work = new SVDPredictor(index, svdFile, k);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		printString(work.predictRating(userID, movieID));
	}
}