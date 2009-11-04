package knn;

public class Neighbor implements Comparable<Neighbor> {
	public final double distance;
	public final int id;
	
	public Neighbor(int id, double distance) {
		this.id = id;
		this.distance = distance;
	}
	
	public String toString() {
		return "id: " + id + ", distance: " + distance;
	}

	@Override
	public int compareTo(Neighbor o) {
		// TODO Auto-generated method stub
		double diff = distance - o.distance;
		if(diff < 0) {
			return -1;
		} else if(diff > 0) {
			return 1;
		} else {
			return 0;
		}
	}
	

}
