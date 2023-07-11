package components;

import java.util.ArrayList;

import environment.Retracer;
import main.Lis;
import main.Vec;

public class C_Lens extends Component {
	
	public double lensWidth;
	public double focalLength;

	@Override
	public void initialize() {
		hitboxes.add(new HitboxSegment(-lensWidth/2,0, lensWidth/2,0,true,this));
	}

	@Override
	public void retrace(Retracer r, ArrayList<Retracer> results) {
		if (Lis.nearEqual(r.angle) || Lis.nearEqual(r.angle, Math.PI)) { return; } //light rays parallel to the lens
		double incidentAngle = r.angle > 0 ? (-r.angle+Lis.PI_BY_TWO) : (r.angle+Lis.PI_BY_TWO);
		r.angle = r.position.to(new Vec(focalLength*Math.tan(incidentAngle),Math.signum(r.angle)*focalLength)).angle();
		results.add(r);
	}

}
