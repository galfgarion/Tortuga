package svd;

import java.io.File;

import tortuga.Predictor;
import tortuga.PredictionIO;

import java.io.*;

import cern.colt.matrix.impl.*;
import cern.colt.matrix.linalg.*;

public class SVDPredictor implements Predictor{
	/* instantiated by passing in an index file, has a float predictRating(int movieid, int userid) */
	
	private SingularValueDecomposition svd;
	
	public SVDPredictor(File index, String svdFile) throws IOException, ClassNotFoundException {
		try{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(svdFile));
			svd = (SingularValueDecomposition)ois.readObject();
			
			ois.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public float predictRating(int movieID, int userID){
		float result = 0;
		return result;
	}

	@Override
	public float predictRating(int movieID, int userID) {
		// TODO Auto-generated method stub
		return 0;
	}
}
