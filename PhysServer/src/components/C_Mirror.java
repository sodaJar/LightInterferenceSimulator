package components;

import java.util.ArrayList;

import environment.Retracer;
import main.Lis;

public class C_Mirror extends Component{
	
	public double slabWidth;
	
	@Override
	public void initialize() {
		hitboxes.add(new HitboxSegment(-slabWidth/2,0,slabWidth/2,0,true,this));
	}
	@Override
	public void retrace(Retracer r, ArrayList<Retracer> results){
		r.angle = -r.angle;
		r.distanceTravelled += Lis.wavelength/2;
		results.add(r);
	}
	

}
