package environment;

import main.Vec;

public class Point {
	public Vec position;
	
	public Point() {}
	public Point(double x, double y) {
		position = new Vec(x,y);
	}
	public Point(Vec position) {
		this.position = position;
	}

}