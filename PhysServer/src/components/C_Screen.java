package components;

import java.util.ArrayList;

import environment.Retracer;
import environment.RetracerRoot;
import main.Lis;
import main.Main;
import main.Vec;

public class C_Screen extends Component{
	
	public double screenWidth;
	public int resolution;
	public ArrayList<RetracerRoot> retracerRoots = new ArrayList<RetracerRoot>();
	
	@Override
	public void initialize() {
		Main.println("init screen");
		hitboxes.add(new HitboxSegment(-screenWidth/2,0, screenWidth/2,0,false,this));
		double s = screenWidth/resolution;
//		if (true) {
		for (int i = 0; i < resolution; i++) {
			RetracerRoot r = new RetracerRoot(Vec.newVecModArg((i-resolution/2)*s,angle).added(position), Lis.normalizeAngleShallow(Lis.PI_BY_TWO+angle), true);
			r.energyPercentage = 1.0;
			r.root = r;
			r.displacement = (i-resolution/2)*s;
			r.ignoreComponent = this;
			retracerRoots.add(r);
		}
//		}else {
//			RetracerRoot r = new RetracerRoot(Vec.newVecModArg(0,angle).added(position), Lis.normalizeAngleShallow(Lis.PI_BY_TWO+angle), true);
//			r.energyPercentage = 1.0;
//			r.root = r;
//			r.displacement = 0;
////			r.scattering = false;
//			r.ignoreComponent = this;
//			retracerRoots.add(r);
//		}
	}
	@Override
	public void retrace(Retracer r, ArrayList<Retracer> results) {
		//blocks all rays
	}
	
}
