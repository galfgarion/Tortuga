package knn;

import java.io.File;
import java.util.List;

import neustore.base.LRUBuffer;
import movieRatings.MovieID_Ratings;

import database.RatingStore;

import knn.NaiveKNN.DistanceTable;

public class NaiveKNNTest extends junit.framework.TestCase {
	
	public static void main(String argv[]) {
		try {
			// Thread.currentThread().sleep(45000);
			testCreateFullDataIndex();
		} catch (Exception e) {
			
		}
	}
	
	public void testDistanceTable() {
		DistanceTable distances = new DistanceTable();
		
		distances.put(1, 1, 0);
		distances.put(1, 2, 2);
		distances.put(2, 3, 5);
		
		assertEquals(true, distances.getMovieIds().contains(1));
		assertEquals(true, distances.getMovieIds().contains(2));
		assertEquals(true, distances.getMovieIds().contains(3));
		
		assertEquals(false, distances.getMovieIds().contains(4));
		
	}
	
	public void testNearestNeighbors() {
		assert(false);
		NaiveKNN knn = new NaiveKNN();
		DistanceTable distances = new DistanceTable();
		
		int idLimit = 6;
		knn.setMovieIDLimit(idLimit);
		
		for(int i = 1; i < idLimit; i++) {
			for(int j = 1; j < idLimit; j++) {
				if(i == j){
					distances.put(i, i, 0);
				}
				else {
					distances.put(i, j, Float.MAX_VALUE);
				}
			}
		}
		
		distances.put(1, 5, 1.0f);
		distances.put(1, 2, 2.0f);
		
		List<Neighbor> nearest = knn.nearestNeighbors(2, 1, distances);
		
		Neighbor first = nearest.get(0);
		Neighbor second = nearest.get(1);
		
		assertEquals(5, first.id);
		assertEquals(1.0, first.distance);
		assertEquals(2, second.id);
		assertEquals(2.0, second.distance);
	}
	
	public static void testLoadData() throws Exception {
		File indexFile = new File("/tmp/test.index");
		RatingStore database = new RatingStore(indexFile);
		database.createFromFile(new File("data"));
		
		NaiveKNN knn = new NaiveKNN(indexFile);
		List<Neighbor> neighbors = knn.nearestNeighbors(5, 1);
		
		for(int i=0; i < 5; i++)
			System.out.println(neighbors.get(i));
		
	}
	
	public void testFakeData() throws Exception {
		File indexFile = new File("/tmp/test.index");
		RatingStore database = new RatingStore(indexFile);
		database.createFromFile(new File("fake_data"));
		
		NaiveKNN knn = new NaiveKNN(indexFile);

		knn.nearestNeighbors(2, 1);
		
		assertEquals(1.0f, knn.distanceTable.get(1, 2));
		assertEquals(1.0f, knn.distanceTable.get(2, 1));
		assertEquals(4.0f, knn.distanceTable.get(1, 3));
		assertEquals(1.0f, knn.distanceTable.get(2, 3));
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