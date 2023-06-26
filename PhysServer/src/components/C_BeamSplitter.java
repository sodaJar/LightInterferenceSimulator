package components;

import java.util.ArrayList;

import environment.Retracer;
import main.Lis;

public class C_BeamSplitter extends Component{
	
	public double slabWidth;
	
	@Override
	public void initialize() {
		hitboxes.add(new HitboxSegment(-slabWidth/2,0,slabWidth/2,0,true,this));
	}
	@Override
	public void retrace(Retracer r, ArrayList<Retracer> results){
		r.energyPercentage /= 2; //both ray have half the energy
		Retracer reflected = r.clone();
		reflected.angle = -reflected.angle;
		reflected.distanceTravelled += Lis.wavelength/2;
		results.add(reflected);
		results.add(r);
	}
	

}
