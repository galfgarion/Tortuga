package svd;

import java.io.File;

import tortuga.Predictor;
import tortuga.PredictionIO;

public class SVDPredictor implements Predictor{
	/* instantiated by passing in an index file, has a float predictRating(int movieid, int userid) */
	public SVDPredictor(File index, String svdFile){
		
	}

	@Override
	public float predictRating(int movieID, int userID) {
		// TODO Auto-generated method stub
		return 0;
	}
}
