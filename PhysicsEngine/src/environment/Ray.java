package environment;

import main.Vec;

public class Ray extends Point {

	public double angle = 0;
	
	public Ray() {}
	public Ray(double x, double y) {
		super(x, y);
	}
	public Ray(Vec position) {
		super(position);
	}
	public Ray(Vec position, double angle) {
		super(position);
		this.angle = angle;
	}
	public Ray(double x, double y, double angle) {
		super(x,y);
		this.angle = angle;
	}

	public Vec simTravel(double distance) { return new Vec(position.x+distance*Math.cos(angle),position.y+distance*Math.sin(angle)); }
	public void travel(double distance) { position.add(simTravel(distance)); }
}
