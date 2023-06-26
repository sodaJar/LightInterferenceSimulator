package environment;

import java.util.ArrayList;

import main.Vec;

public class Source extends Ray {
	public double amplitude = 1; //amplitude of the wave, related to the intensity of the light
	public double shift = 0; //shift to left e.g. a*sin(x+shift)
	public boolean focused = false;
	public ArrayList<Observer> focusedObservers = new ArrayList<Observer>();
	
	public Source(double x, double y) {
		super(x, y);
	}
	public Source(Vec position) {
		super(position);
	}
	
	public Observer toObserver() {
		Observer o = new Observer(position.x,position.y);
		o.observation = amplitude;
		o.shift = shift;
		o.angle = angle;
		return o;
	}
	
	@Override
	public void travel(double distance) {
		shift += distance;
		super.travel(distance);
	}
}