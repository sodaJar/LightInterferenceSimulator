package components;

import java.util.ArrayList;

import environment.Retracer;
import main.Lis;

public class C_SingleSlit extends Component {
	public double obstacleWidth;
	public double slitWidth;
	
	@Override
	public void initialize() {
		if (slitWidth>obstacleWidth) { return; }
		innateQuality = 0.01;
		hitboxes.add(new HitboxSegment(-obstacleWidth/2,0, -slitWidth/2,0,false,this));
		hitboxes.add(new HitboxSegment(-slitWidth/2,0, slitWidth/2,0,true,this));
		hitboxes.add(new HitboxSegment(slitWidth/2,0, obstacleWidth/2,0,false,this));
	}
	@Override
	public void retrace(Retracer r, ArrayList<Retracer> results){
//		Main.println("Intersects with single slit at x = "+r.position.x);
//		Main.println("Slit hitbox L "+hitboxes.get(1).getDirVec().length());
		if (Math.abs(r.position.x)>slitWidth/2) { return; }
//		if (Math.abs(r.position.x)>slitWidth*2) { return; }
//		if (Math.abs(r.position.x)<slitWidth) { return; }
		r.angle = r.angle>0?Lis.PI_BY_TWO:-Lis.PI_BY_TWO;
		r.scattering = true;
//		Main.println("Add to single slit component results for x = "+r.position.x);
		results.add(r);
	}
	
}
