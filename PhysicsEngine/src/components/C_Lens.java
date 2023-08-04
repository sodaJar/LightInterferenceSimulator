package components;

import java.util.ArrayList;

import environment.Retracer;
import main.Lis;
import main.Vec;

public class C_Lens extends Component {
	
	public double lensWidth;
	public double focalLength;
	public boolean compensating;

	@Override
	public void initialize() {
		hitboxes.add(new HitboxSegment(-lensWidth/2,0, lensWidth/2,0,true,this));
	}

	@Override
	public void retrace(Retracer r, ArrayList<Retracer> results) {
		if (Lis.nearEqual(r.angle) || Lis.nearEqual(r.angle, Math.PI)) { return; } //light rays parallel to the lens
		final double incidentAngle = r.angle > 0 ? (-r.angle+Lis.PI_BY_TWO) : (r.angle+Lis.PI_BY_TWO);
		Vec focalPoint = new Vec(focalLength*Math.tan(incidentAngle),Math.signum(r.angle)*focalLength);
		r.angle = r.position.to(focalPoint).angle();
		//compensate for path difference on the focal plane introduced by the lens
		if (compensating) {
			final Vec emissionPoint = new Vec(r.position.x-focalLength*Math.tan(incidentAngle),focalLength);
			r.distanceTravelled += emissionPoint.distanceTo(new Vec(emissionPoint.x==0?lensWidth/2:(-Math.signum(emissionPoint.x)*lensWidth/2),0))-r.position.distanceTo(emissionPoint);
		}
		results.add(r);
	}
}
