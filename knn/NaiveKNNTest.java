package knn;

import java.io.File;
import java.util.List;

import knn.NaiveKNN.Similarity;

import neustore.base.LRUBuffer;
import movieRatings.MovieID_Ratings;

import database.RatingStore;

public class NaiveKNNTest extends junit.framework.TestCase {
	
	public static void main(String argv[]) {
		try {
			// Thread.currentThread().sleep(45000);
			testCreateFullDataIndex();
		} catch (Exception e) {
			
		}
	}
	
	public static void testLoadData() throws Exception {
		File indexFile = new File("/tmp/test.index");
		RatingStore database = new RatingStore(indexFile);
		database.createFromFile(new File("data"));
		
		NaiveKNN knn = new NaiveKNN(indexFile);
		List<Similarity> neighbors = knn.nearestNeighbors(5, 1);
		
		for(int i=0; i < 5; i++)
			System.out.println(neighbors.get(i));
		
	}
	
	public void testFakeData() throws Exception {
		File indexFile = new File("/tmp/test.index");
		RatingStore database = new RatingStore(indexFile);
		database.createFromFile(new File("fake_data"));
		
		NaiveKNN knn = new NaiveKNN(indexFile);

		knn.nearestNeighbors(2, 1);
	}
	
	public static void testCreateFullDataIndex() throws Exception {
		File indexFile = new File("/tmp/training_set_indexed3.neu");
		RatingStore database = new RatingStore(indexFile);
		database.createFromFile(new File("training_set"));
	}
	
	public static void testLoadFullMovieDataIndex() throws Exception {
		MovieID_Ratings index;
		index = new MovieID_Ratings(new LRUBuffer (1, 4096), "/tmp/training_set_indexed3.neu", 0);
		long what = System.currentTimeMillis();
		for(int x = 1; x <= 17770; x++)
			index.getRatingsById(x);
		System.out.println(index.getRatingsById(5000));
		System.out.println("MS elapsed during search: " + (System.currentTimeMillis() - what));
		
		what = System.currentTimeMillis();
		for(int x = 1; x <= 17770; x++)
			index.getRatingsById(x);
		System.out.println(index.getRatingsById(5000));
		System.out.println("MS elapsed during search: " + (System.currentTimeMillis() - what));
	}
}