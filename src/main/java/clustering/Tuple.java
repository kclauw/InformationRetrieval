package clustering;

public class Tuple<X, Y> { 
	public X x; 
	public Y y; 
	public Tuple(X x, Y y) { 
		this.x = x; 
		this.y = y; 
	}
	
	public void setX(X x) {
		this.x = x;
	}
	public void setY(Y y) {
		this.y = y;
		
	} 
} 