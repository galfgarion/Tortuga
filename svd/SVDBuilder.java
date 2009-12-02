package svd;

import movieRatings.MovieID_Ratings;
import movieRatings.UserRating;
import neustore.base.LRUBuffer;

import java.io.*;
import java.util.ArrayList;

import cern.colt.matrix.impl.*;
import cern.colt.matrix.linalg.*;

public class SVDBuilder {

	/*
	 * rows = movieID
	 * cols = userID
	 * 
	 * matrix=> rows X cols = m X n = movieID X userID
	 */
	private int rows = 480000;
	private int cols = 17770;
		
	//public SVDBuilder(String neustoreTrainingSetFile) {
	public SVDBuilder(File index, String outputFile) throws Exception {
		
		SparseDoubleMatrix2D svdbuild = new SparseDoubleMatrix2D(rows, cols);
		
		MovieID_Ratings theRatings = new MovieID_Ratings(new LRUBuffer(5, 4096), index.getAbsolutePath(), 0);
		ArrayList<UserRating> ratings;
		
		for(int row=0; row<rows; ++row) {
			ratings = theRatings.getRatingsById(row+1);
			// for(int col=0; col<cols; ++col){
			for(UserRating r : ratings) {
				svdbuild.setQuick(row,r.userId, r.rating);
				//temp = (double)ratings.get(col).rating;
				
				//if(temp != null)
					//matrix[row][col] = temp;
			}
		}
		
		svdbuild.trimToSize();
		
		SingularValueDecomposition svdTab = new SingularValueDecomposition(svdbuild);
		
		try {
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(outputFile));
			os.writeObject(svdTab);
			os.close();
		}
		catch (IOException e) {
			System.out.println("Unable to open filename " + outputFile + " :" + e.getMessage());
		}
	}
}