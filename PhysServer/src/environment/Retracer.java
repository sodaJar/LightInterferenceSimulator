package environment;

import java.util.ArrayList;
import java.util.HashSet;

import components.C_SingleSlit;
import components.Component;
import components.HitboxSegment;
import components.HitboxSegmentPuppet;
import main.Lis;
import main.Main;
import main.Setup;
import main.Vec;

public class Retracer extends Ray {
	public boolean scattering; //whether the ray scatters in all directions (usually when diffraction occurs)
	public double sourcePower = Double.NaN; //power of the light source this retracer landed on, NaN if has not landed on source
	public double energyPercentage; //the percentage of the energy to receive from the source, used to ensure intensity uniformity
	public double distanceTravelled = 0; //total distance travelled by the retracer
	public Component ignoreComponent = null; //the component this retracer shouln't collide with
	public RetracerRoot root = null; //the initial retracer from which this retracer branched out
	
	public Retracer(Vec position, double angle, boolean scattering) {
		super(position, angle);
		this.scattering = scattering;
	}
	public Retracer(Vec position, double angle, boolean scattering, double sourcePower, double energyPercentage, double distanceTravelled, Component ignoreComponent, RetracerRoot root) {
		super(position, angle);
		this.scattering = scattering;
		this.sourcePower = sourcePower;
		this.energyPercentage = energyPercentage;
		this.distanceTravelled = distanceTravelled;
		this.ignoreComponent = ignoreComponent;
		this.root = root;
	}
	
	public ArrayList<Retracer> retrace(Setup setup) { //wraps the component retrace function
		ArrayList<Retracer> nextRetracers = new ArrayList<Retracer>();
		if (scattering) {
			/*
			 * The main idea is to get a list of hitboxes fully or partially exposed to the scattering retracer and then process them
			 * to keep parts that is directly exposed to the retracer and the intersection points simply become each point on those parts.
			 * This algorithm replaced the older extremely inefficient one where rays spreading in all directions were tested one by one
			 */
			//the set of master hitbox objects to check for duplicates (each puppet is unique even if they represent the same hitbox)
			HashSet<HitboxSegment> masters = new HashSet<HitboxSegment>();
			//the set of "dummy" hitboxes that represent their masters and are free to be modified unlike their master hitboxes who are the actual unique hitboxes
			HashSet<HitboxSegmentPuppet> puppets = new HashSet<HitboxSegmentPuppet>();
			//get all hitboxes whose surfaces are exposed to the scattering retracer
			for (Component component : setup.components) {
				if (component == ignoreComponent) { continue; }
				for (HitboxSegment hitbox : component.hitboxes) {
					if (Math.abs(Lis.fromAngleToAngle(position.to(hitbox.pos1).angle(),angle))>Lis.PI_BY_TWO*0.95 && Math.abs(Lis.fromAngleToAngle(position.to(hitbox.pos2).angle(),angle))>Lis.PI_BY_TWO*0.95) {
						continue;
					}
//					Main.println("CHECK HB "+hitbox.owner.toString()+" "+hitbox.owner.hitboxes.indexOf(hitbox));
					Vec s = hitbox.getDirVec().dividedBy(Lis.collisionTestSize);
					Vec posOnHitbox = hitbox.pos1.clone();
					
					for(int i = 0; i < Lis.collisionTestSize; i++) { //for every point on hitbox
						Vec testVec = position.to(posOnHitbox); //test if this path is not blocked by any hitbox
						boolean clear = true;
//						if (component instanceof C_SingleSlit && hitbox.owner.hitboxes.indexOf(hitbox) == 1) Main.println("test vec: "+testVec.toString());
//						if (component instanceof C_SingleSlit && hitbox.owner.hitboxes.indexOf(hitbox) == 1) {
//							Main.println("test for "+hitbox.toString()+" of "+hitbox.owner.toString());
//						}
						cLoop: for (Component c : setup.components) { //check if any other hitbox is between this one and the retracer
							if (c == ignoreComponent) { continue; }
							for (HitboxSegment hb : c.hitboxes) {
								if (hb == hitbox) { continue; }
//								Main.println("get intersection"+position.toString() + testVec.toString() + hb.pos1.toString() + hb.getDirVec().toString());
								Vec intersection = Lis.getIntersection(position, testVec, hb.pos1, hb.getDirVec(), Lis.LINE_TYPE.SEGMENT, Lis.LINE_TYPE.SEGMENT);
								if (intersection == null) { continue; }
								//if the hitbox can connect to the retracer
								clear = false;
								break cLoop;
							}
						}
						if (clear) {
							//make sure that pos1 is always at a smaller angle than pos2; adds to the array of hitboxes; component angle is assumed to be [-PI,PI]
							if (Lis.fromNormAngleToNormAngle(position.to(hitbox.pos1).angle(),position.to(hitbox.pos2).angle()) < 0) { hitbox.pos1.swapWith(hitbox.pos2); }
							if (masters.add(hitbox)) { puppets.add(hitbox.newPuppet()); }
							break;
						}
						
						posOnHitbox.add(s); //move to next point on hitbox
					}
				}
			}
			
			//deal with cases where some hitboxes are overlapping with other hitboxes
			//hitboxes are assumed to be non-intersecting
			//a set of extra puppets produced when one is partially covered by an "island" hitbox in front
			HashSet<HitboxSegmentPuppet> extraPuppets = new HashSet<HitboxSegmentPuppet>();
			for (HitboxSegmentPuppet hitbox : puppets) {
				for (HitboxSegmentPuppet withHitbox : puppets) {
					if (hitbox == withHitbox) { continue; }
					Vec intersection1 = Lis.getIntersection(position, position.to(hitbox.pos1), withHitbox.pos1, withHitbox.getDirVec(), Lis.LINE_TYPE.RAY, Lis.LINE_TYPE.SEGMENT);
					Vec intersection2 = Lis.getIntersection(position, position.to(hitbox.pos2), withHitbox.pos1, withHitbox.getDirVec(), Lis.LINE_TYPE.RAY, Lis.LINE_TYPE.SEGMENT);
					if (intersection1==null && intersection2==null) { continue; } //sth wrong with this, but its prbly ok
					boolean pos1Covering = (intersection1 == null)?false:(position.distanceSquaredTo(intersection1)>position.distanceSquaredTo(hitbox.pos1));
					boolean pos2Covering = (intersection2 == null)?false:(position.distanceSquaredTo(intersection2)>position.distanceSquaredTo(hitbox.pos2));
					if (intersection1 != null && intersection2 != null && pos1Covering && pos2Covering) { //if the hitbox is covering a middle part of another hitbox
						HitboxSegmentPuppet extraHitbox = withHitbox.clone();
						withHitbox.pos2.set(intersection1);
						extraHitbox.pos1.set(intersection2);
						extraPuppets.add(extraHitbox);
					}else if(intersection1 != null && pos1Covering) { //pos1 covers another hitbox
						withHitbox.pos2.set(intersection1);
					//pos2 covers another hitbox; if intersection1 is null, intersection2 must be valid
					//if pos1Covering is false, pos2Covering must be false as hitboxes don't intersect; so the below check is sufficient
					}else if(pos2Covering) { withHitbox.pos1.set(intersection2); }
				}
			}
			puppets.addAll(extraPuppets);
			
			//test rays towards each hitbox
			for(HitboxSegmentPuppet hb : puppets) {
				if (!hb.responsive) { continue; }
//				Main.println("hb "+hb.pos1.toString()+" "+hb.pos2.toString());
				double a1 = position.to(hb.pos1).angle();
				double a2 = position.to(hb.pos2).angle();
				boolean pos1Behind = Math.abs(Lis.fromAngleToAngle(angle, a1))>Lis.PI_BY_TWO; //pos1 is invalid as it's behind the retracer
				boolean pos2Behind = Math.abs(Lis.fromAngleToAngle(angle, a2))>Lis.PI_BY_TWO; //pos2 is invalid as it's behind the retracer
				if (pos1Behind) { a1 = Lis.normalizeAngleShallow(angle-Lis.PI_BY_TWO); }
				else if (pos2Behind) { a2 = Lis.normalizeAngleShallow(angle+Lis.PI_BY_TWO); }
				double delta = Lis.fromNormAngleToNormAngle(a1, a2);
				if (delta<-1e-10) Main.println("delta: "+Lis.r2d(delta)+" a1: "+Lis.r2d(a1)+" a2: "+Lis.r2d(a2));
				if (!Lis.nearEqual(delta)) {
//					double s = (Math.PI/Lis.scatterDensity); //angular spacing fixed angle
//					int sc = (int)Math.abs(delta/s);
//					double s = delta/(Lis.scatterCount-1); //angular spacing fixed count
					int scCount = (int)(Lis.scatterCount*hb.owner.quality*hb.owner.innateQuality);
					double s = delta/(scCount-1); //angular spacing fixed count for specific hitbox
					for (int scIdx = 0; scIdx < scCount/*or Lis.scatterCount or sc*/; scIdx++) { //test each ray
						Retracer nextR = clone();
//						double rayAngle = a1+(((double)scIdx)/(Lis.scatterCount-1))*delta;
						double rayAngle = Lis.normalizeAngleShallow(a1+((double)scIdx)*s);
//						Main.println("ray angle "+Lis.r2d(rayAngle));
						Vec intersection = Lis.getLineIntersection(position, Vec.newVecModArg(1, rayAngle), hb.pos1, hb.getDirVec());
						if (intersection == null) { continue; }
						//assume component at Vec(0) and angle 0, normalize incoming ray
						nextR.distanceTravelled += position.distanceTo(intersection);
						nextR.position = intersection.clone(); //DEBUGGED
						nextR.position.subtract(hb.owner.position);
						nextR.position.rotate(-hb.owner.angle);
						nextR.angle = Lis.normalizeAngle(rayAngle - hb.owner.angle); //each scattered ray has a different angle
						nextR.scattering = false; //scattering by default false
						nextR.energyPercentage /= scCount;//Lis.scatterCount;//Lis.scatterDensity;// 
//						if (hb.owner instanceof C_Laser) Main.println("input "+nextR.position.toString()); //DEBUG
						ArrayList<Retracer> results = new ArrayList<Retracer>();
						hb.owner.retrace(nextR,results);
						for (Retracer res : results) {
//							if (hb.owner instanceof C_Laser) Main.println("output "+res.position.toString());
							//denormalize retracing results from the component
							res.position.rotate(hb.owner.angle);
							res.position.add(hb.owner.position);
							res.angle += hb.owner.angle;
							res.root = root;
							res.ignoreComponent = hb.owner;
							if (Double.isNaN(res.sourcePower)) { nextRetracers.add(res); } //hasn't reached a light source
							else { root.vecWave.add(Vec.newVecModArg(res.sourcePower*res.energyPercentage, Lis.normalizeAngle(res.distanceTravelled*Lis.wavelengthNormalizer))); } //reached a light source
						}
					}
				}
//				Vec s = hb.getDirVec().dividedBy(Lis.scatterCount-1);
//				Vec intersectionDir = new Vec(0);
//				for (int scIdx = 0; scIdx < Lis.scatterCount; scIdx++) {
//					Vec intersection = hb.pos1.added(intersectionDir);
//					double rayAngle = position.to(intersection).angle();
//					intersectionDir.add(s);
//					Retracer nextR = clone();
//					//assume component at Vec(0) and angle 0, normalize incoming ray
//					nextR.distanceTravelled += position.distanceTo(intersection);
//					nextR.position = intersection.clone(); //DEBUGGED
//					nextR.position.subtract(hb.owner.position);
//					nextR.position.rotate(-hb.owner.angle);
//					nextR.angle = Lis.normalizeAngleShallow(rayAngle - hb.owner.angle); //each scattered ray has a different angle
//					nextR.scattering = false; //scattering by default false
//					nextR.energyPercentage /= Lis.scatterDensity;
//					ArrayList<Retracer> results = new ArrayList<Retracer>();
//					hb.owner.retrace(nextR,results);
//					for (Retracer res : results) {
//						//denormalize retracing results from the component
//						res.position.rotate(hb.owner.angle);
//						res.position.add(hb.owner.position);
//						res.angle += hb.owner.angle;
//						res.root = root;
//						res.ignoreComponent = hb.master;
//						if (Double.isNaN(res.sourcePower)) { nextRetracers.add(res); } //hasn't reached a light source
//						else { root.vecWave.add(Vec.newVecModArg(res.sourcePower*res.energyPercentage, Lis.normalizeAngle(res.distanceTravelled*Lis.wavelengthNormalizer))); } //reached a light source
//					}
//				}
			}
		}else { //else if not scattering
			final Vec dir = Vec.newVecModArg(1, angle);
			double minDistanceSquared = Double.POSITIVE_INFINITY;
			Vec closestIntersection = null;
			Component closestComponent = null;
			for (Component c : setup.components) { //get closest (valid) intersection of the retracing ray with any hitbox of any component
				if (c == ignoreComponent) { continue; }
				for (int i = 0; i < c.hitboxes.size(); i++) {
					HitboxSegment hb = c.hitboxes.get(i);
//					if (c instanceof C_SingleSlit) {
//						Main.println("Single slit not scatter");
//					}
//					if (this instanceof RetracerRoot) Main.println("is root");
					if (!hb.responsive) { continue; } //to prevent the retracer from detecting intersection with the same hitbox over and over
					Vec intersection = Lis.getIntersection(hb.pos1, hb.getDirVec(), position, dir, Lis.LINE_TYPE.SEGMENT, Lis.LINE_TYPE.RAY);
					if (intersection == null || intersection.nearEqual(position,1e-9)) { continue; }
					double distanceSquared = position.distanceSquaredTo(intersection); //DEBUGGED
					if (distanceSquared < minDistanceSquared) {
						minDistanceSquared = distanceSquared;
						closestIntersection = intersection;
						closestComponent = c;
					}
				}
			}
			if (closestIntersection==null) { return nextRetracers; }
			//create a clone at point of intersection and accumulate distance
			Retracer nextR = clone();
			//assume component at Vec(0) and angle 0, normalize incoming ray (angle already fully normalized)
			nextR.position = closestIntersection.clone(); //DEBUGGED
			nextR.position.subtract(closestComponent.position);
			nextR.position.rotate(-closestComponent.angle);
			nextR.angle = Lis.normalizeAngle(angle - closestComponent.angle); //each scattered ray has a different angle
			nextR.scattering = false; //scattering by default false
			nextR.distanceTravelled += Math.sqrt(minDistanceSquared);
			ArrayList<Retracer> results = new ArrayList<Retracer>();
			if (closestComponent instanceof C_SingleSlit) Main.println("input "+position.toString());
			closestComponent.retrace(nextR,results);
			for (Retracer res : results) {
				if (closestComponent instanceof C_SingleSlit) Main.println("output "+res.position.toString());
				//denormalize retracing results from the component
				res.position.rotate(closestComponent.angle);
				res.position.add(closestComponent.position);
				res.angle += closestComponent.angle;
				res.root = root;
				res.ignoreComponent = closestComponent;
				if (Double.isNaN(res.sourcePower)) { nextRetracers.add(res); } //hasn't reached a light source
				else { root.vecWave.add(Vec.newVecModArg(res.sourcePower*res.energyPercentage, Lis.normalizeAngle(res.distanceTravelled*Lis.wavelengthNormalizer))); } //reached a light source
			}
		}
		return nextRetracers;
	}
	
	public Retracer clone() { return new Retracer(position.clone(),angle,scattering,sourcePower,energyPercentage,distanceTravelled,ignoreComponent,root); } //DEBUGGED
}
