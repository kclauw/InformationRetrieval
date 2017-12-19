package clustering;

import java.util.ArrayList;

public class Cluster {
	public Tuple<Integer, Integer> centroid;
	public ArrayList<Tuple<Integer,Integer>> cluster;
	
	public void add(Tuple<Integer,Integer> newpnt) {
		cluster.add(newpnt);
	}
	
	public void remove(Tuple<Integer,Integer> oldpnt) {
		cluster.remove(oldpnt);
	}
	public void remove(int oldpnt) {
		cluster.remove(oldpnt);
	}
	
	public Cluster() {
		
	}
}