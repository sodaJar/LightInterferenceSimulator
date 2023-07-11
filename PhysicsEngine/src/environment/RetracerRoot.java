package environment;

import components.Component;
import main.Vec;

public class RetracerRoot extends Retracer {
	
	public double displacement = 0;
	
	public Vec vecWave = new Vec(0);
	
	public RetracerRoot(Vec position, double angle, boolean scattering) {
		super(position, angle, scattering);
	}
	public RetracerRoot(Vec position, double angle, boolean scattering, double sourcePower, double energyPercentage,double distanceTravelled, Component ignoreComponent, RetracerRoot root) {
		super(position, angle, scattering, sourcePower, energyPercentage, distanceTravelled, ignoreComponent, root);
	}
}
