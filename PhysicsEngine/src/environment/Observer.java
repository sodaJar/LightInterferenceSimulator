package environment;

import main.Vec;

public class Observer extends Ray {
	public double observation = 0;
	public double shift = 0;
	
	public Observer(double x, double y) {
		super(x, y);
	}
	public Observer(Vec position) {
		super(position);
	}
	
	public void reset() {
		observation = 0;
	}
	
	public Source toSource() {
		Source s = new Source(position.x,position.y);
		s.amplitude = observation;
		s.shift = shift;
		s.angle = angle;
		return s;
	}
	
	@Override
	public void travel(double distance) {
		shift += distance;
		super.travel(distance);
	}
	
}