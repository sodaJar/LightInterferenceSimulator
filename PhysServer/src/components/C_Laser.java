package components;

import java.util.ArrayList;

import environment.Retracer;
import main.Lis;
import main.Main;

public class C_Laser extends Component{
	
	public double beamWidth; //width where power > max power/e^2
	public double power; //total power = area under the Gaussian distribution; value should be in range [0,1] (percentage)
	
	private double normalDistCoeff;
	
	@Override
	public void initialize() {
		hitboxes.add(new HitboxSegment(-beamWidth,0, beamWidth,0,true,this)); //the hitbox width is twice the beam width
		normalDistCoeff = 4*power/(beamWidth*Math.sqrt(Lis.TWO_PI));
	}
	@Override
	public void retrace(Retracer r, ArrayList<Retracer> results){
		if (r.angle < 0) {
			r.sourcePower = getPowerAt(r.position.x);
			results.add(r);
		}
	}
	
	//gets the power of the laser at x according to the normal distribution
	//the beam width is the width where the power is greater than 1/e^2 of the max power
	private double getPowerAt(double x) {
		return normalDistCoeff*Math.exp(-8*x*x/(beamWidth*beamWidth));
//		return power/Math.exp(8*(x*x)/(beamWidth*beamWidth)); //wrong obsolete formula
	}
}