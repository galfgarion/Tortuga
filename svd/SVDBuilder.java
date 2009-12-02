package svd;

import movieRatings.MovieID_Ratings;
import movieRatings.UserRating;
import neustore.base.LRUBuffer;

import java.io.*;
import java.lang.Object;
import java.util.ArrayList;

import cern.colt.PersistentObject;

public class SVDBuilder {

	/*
	 * rows = movieID
	 * cols = userID
	 * 
	 * matrix=> rows X cols = m X n = movieID X userID
	 */
	private int rows, cols;
		
	//public SVDBuilder(String neustoreTrainingSetFile) {
	public SVDBuilder(File index, String outputFile) {
		
		//DoubleFactory2D svdfactory;
		//svdfactory = DoubleFactory2D.sparse;
		//DoubleMatrix2D svdbuild = factory.make(rows, cols);
		
		double[][] matrix = new double[rows][cols];
		double temp;
		
		MovieID_Ratings theRatings = new MovieID_Ratings(new LRUBuffer(5, 4096), index.getAbsolutePath(), 0);
		ArrayList<UserRating> ratings;
		
		for(int row=0; row<rows; ++row) {
			ratings = theRatings.getRatingsById(row+1);
			for(int col=0; col<cols; ++col){
				//svdbuild.setQuick(row,col, (double)ratings.get(col).rating);
				temp = (double)ratings.get(col).rating;
				
				if(temp != null)
					matrix[row][col] = temp;
			}
		}
		
		SparseDoubleMatrix2D svdbuild = new SparseDoubleMatrix2D(matrix);
		svdbuild.trimToSize();
		
		SingularValueDecomposition svdTab = new SingularValueDecomposition(svdbuild);
		
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(outputFile));
		os.writeObject(svdTab);
		os.close();
	}
}