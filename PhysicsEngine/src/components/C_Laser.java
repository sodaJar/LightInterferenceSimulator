package components;

import java.util.ArrayList;

import environment.Retracer;
import main.Lis;

public class C_Laser extends Component{
	
	public double beamWidth; //width where power > max power/e^2
	public double power; //total power = area under the Gaussian distribution; value should be in range [0,1] (percentage)
	public double scatteringAngle; //the FOV of the laser, less angle = more collimation
	
	private double normalDistCoeff;
	
	@Override
	public void initialize() {
		hitboxes.add(new HitboxSegment(-beamWidth/2,0, beamWidth/2,0,true,this));
		normalDistCoeff = 400*power/(beamWidth*Math.sqrt(Lis.TWO_PI));
	}
	@Override
	public void retrace(Retracer r, ArrayList<Retracer> results) {
		if (r.angle < -Lis.PI_BY_TWO+scatteringAngle/2 && r.angle > -Lis.PI_BY_TWO-scatteringAngle/2) {
			//path difference correction for plane waves
			final double correctionAngle = r.angle + Lis.PI_BY_TWO;
			r.distanceTravelled -= r.position.x*Math.sin(correctionAngle);
			r.sourcePower = getPowerAt(r.position.x);
			results.add(r);
		}
	}
	
	//gets the power of the laser at x according to the normal distribution
	//the beam width is the width where the power is greater than 1/e^2 of the max power
	private double getPowerAt(double x) {
		return normalDistCoeff*Math.exp(-8*x*x/(beamWidth*beamWidth));
	}
}