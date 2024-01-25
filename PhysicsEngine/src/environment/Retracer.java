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
			 */
			//the set of master hitbox objects to check for duplicates (each puppet is unique even if they represent the same hitbox)
			HashSet<HitboxSegment> masters = new HashSet<HitboxSegment>();
			//the set of "dummy" hitboxes that represent their masters and are free to be modified unlike their master hitboxes who are the actual unique hitboxes
			HashSet<HitboxSegmentPuppet> puppets = new HashSet<HitboxSegmentPuppet>();
			//get all hitboxes whose surfaces are exposed to the scattering retracer
			for (Component component : setup.components) {
				if (component == ignoreComponent) { continue; }
				for (HitboxSegment hitbox : component.hitboxes) {
					if (Math.abs(Lis.fromAngleToAngle(position.to(hitbox.pos1).angle(),angle))>Lis.PI_BY_TWO*0.9 && Math.abs(Lis.fromAngleToAngle(position.to(hitbox.pos2).angle(),angle))>Lis.PI_BY_TWO*0.9) {
						continue;
					}
					Vec s = hitbox.getDirVec().dividedBy(Lis.collisionTestSize);
					Vec posOnHitbox = hitbox.pos1.clone(); //this vector is incremented to cover each point on the hitbox
					for(int i = 0; i < Lis.collisionTestSize; i++) { //for every point on hitbox
						Vec testVec = position.to(posOnHitbox); //test if this path is not blocked by any hitbox
						boolean clear = true;
						cLoop: for (Component c : setup.components) { //check if any other hitbox is between this one and the retracer
							if (c == ignoreComponent) { continue; }
							for (HitboxSegment hb : c.hitboxes) {
								if (hb == hitbox) { continue; }
								Vec intersection = Lis.getIntersection(position, testVec, hb.pos1, hb.getDirVec(), Lis.LINE_TYPE.SEGMENT, Lis.LINE_TYPE.SEGMENT);
								if (intersection != null) { //if the ray is blocked by something, move on to the next ray
									clear = false;
									break cLoop;	
								}
							}
						}
						if (clear) { //if there is at least one ray that can reach the hitbox, then the hitbox is exposed to the retracer 
							//make sure that pos1 is always at a smaller angle than pos2; adds to the array of hitboxes; component angle is assumed to be [-PI,PI]
							if (Lis.fromNormAngleToNormAngle(position.to(hitbox.pos1).angle(),position.to(hitbox.pos2).angle()) < 0) { hitbox.pos1.swapWith(hitbox.pos2); }
							if (masters.add(hitbox)) { puppets.add(hitbox.newPuppet()); }
							break;
						}
						posOnHitbox.add(s); //move to next point on hitbox
					}
				}
			}
			
			/*
			 * This part resolves cases where some hitboxes are overlapping with other hitboxes
			 * by changing the pos1(one end of the segment) or pos2(the second end) or both to make sure
			 * that no two puppet hitboxes overlap
			 */
			//note: hitboxes are assumed to be non-intersecting
			//extra puppet hitboxes are produced when one is partially covered by an "island" hitbox in front
			HashSet<HitboxSegmentPuppet> extraPuppets = new HashSet<HitboxSegmentPuppet>();
			for (HitboxSegmentPuppet hitbox : puppets) {
				for (HitboxSegmentPuppet withHitbox : puppets) {
					if (hitbox == withHitbox) { continue; } //don't check with one self
					Vec intersection1 = Lis.getIntersection(position, position.to(hitbox.pos1), withHitbox.pos1, withHitbox.getDirVec(), Lis.LINE_TYPE.RAY, Lis.LINE_TYPE.SEGMENT);
					Vec intersection2 = Lis.getIntersection(position, position.to(hitbox.pos2), withHitbox.pos1, withHitbox.getDirVec(), Lis.LINE_TYPE.RAY, Lis.LINE_TYPE.SEGMENT);
					if (intersection1==null && intersection2==null) { continue; } //if the hitbox doesn't cover the other hitbox in any way, don't modify anything
					//then, either pos1 or pos2 is covering another hitbox
					boolean pos1Covering = (intersection1 == null) ? false : (position.distanceSquaredTo(intersection1)>position.distanceSquaredTo(hitbox.pos1));
					boolean pos2Covering = (intersection2 == null) ? false : (position.distanceSquaredTo(intersection2)>position.distanceSquaredTo(hitbox.pos2));
					if (pos1Covering && pos2Covering) { //if the hitbox is covering a middle part of another hitbox
						HitboxSegmentPuppet extraHitbox = withHitbox.clone(); //produce an extra puppet because the original has a gap in the middle
						withHitbox.pos2 = intersection1;
						extraHitbox.pos1 = intersection2;
						extraPuppets.add(extraHitbox);
					}else if(pos1Covering) { withHitbox.pos2 = intersection1; } //pos1 covers another hitbox
					else if(pos2Covering) { withHitbox.pos1 = intersection2; } //pos2 covers another hitbox
					//else would imply that the hitbox is being covered by another, in which case we don't need to do anything
				}
			}
			puppets.addAll(extraPuppets);
			
			//test rays towards each hitbox
			for(HitboxSegmentPuppet hb : puppets) {
				if (!hb.responsive) { continue; }
				double a1 = position.to(hb.pos1).angle();
				double a2 = position.to(hb.pos2).angle();
				boolean pos1Behind = Math.abs(Lis.fromAngleToAngle(angle, a1))>Lis.PI_BY_TWO; //pos1 is invalid as it's behind the retracer
				boolean pos2Behind = Math.abs(Lis.fromAngleToAngle(angle, a2))>Lis.PI_BY_TWO; //pos2 is invalid as it's behind the retracer
				if (pos1Behind) { a1 = Lis.normalizeAngleShallow(angle-Lis.PI_BY_TWO); }
				else if (pos2Behind) { a2 = Lis.normalizeAngleShallow(angle+Lis.PI_BY_TWO); }
				double delta = Lis.fromNormAngleToNormAngle(a1, a2); //delta must be positive
				if (delta <= -1e-10) { //this occurs under certain abnormal conditions (E.g. when the length of a hitbox is 0)
					Main.println("NEGATIVE DELTA WARNING\nFrom "+position+" a1: "+Lis.r2d(a1)+" a2: "+Lis.r2d(a2)+" pos1: "+hb.pos1.toString()+" pos2: "+hb.pos2.toString());
					continue; //continue since this may cause trouble
				}
				if (!Lis.nearEqual(delta)) {
					int scCount = (int)(Lis.scatterCount*hb.owner.quality*hb.owner.innateQuality);
					double s = delta/(scCount-1); //angular spacing fixed scatter count for specific hitbox
					for (int scIdx = 0; scIdx < scCount; scIdx++) { //retrace each ray
						Retracer nextR = clone();
						double rayAngle = Lis.normalizeAngleShallow(a1+((double)scIdx)*s);
						//since we know for sure all rays are concentrated on the hitbox, and they must intersect, a line intersection check is enough
						Vec intersection = Lis.getLineIntersection(position, Vec.newVecModArg(1, rayAngle), hb.pos1, hb.getDirVec());
						if (intersection == null) { continue; }
						//assume component at Vec(0) and angle 0, normalize incoming ray
						nextR.distanceTravelled += position.distanceTo(intersection);
						nextR.position = intersection.clone();
						nextR.position.subtract(hb.owner.position);
						nextR.position.rotate(-hb.owner.angle);
						nextR.angle = Lis.normalizeAngle(rayAngle - hb.owner.angle); //each scattered ray has a different angle
						nextR.scattering = false; //scattering by default false
						nextR.energyPercentage *= delta/(Math.PI*scCount);
						ArrayList<Retracer> results = new ArrayList<Retracer>();
						hb.owner.retrace(nextR,results);
						for (Retracer res : results) {
							//denormalize retracing results from the component
							res.position.rotate(hb.owner.angle);
							res.position.add(hb.owner.position);
							res.angle += hb.owner.angle;
							res.root = root;
							res.ignoreComponent = hb.owner;
							if (Double.isNaN(res.sourcePower)) { nextRetracers.add(res); } //hasn't reached a light source
							//else reached a light source
							else { root.vecWave.add(Vec.newVecModArg(res.sourcePower*res.energyPercentage, Lis.normalizeAngle(res.distanceTravelled*Lis.wavelengthNormalizer))); }
						}
					}
				}
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
					if (!hb.responsive) { continue; } //to prevent the retracer from detecting intersection with the same hitbox over and over
					Vec intersection = Lis.getIntersection(hb.pos1, hb.getDirVec(), position, dir, Lis.LINE_TYPE.SEGMENT, Lis.LINE_TYPE.RAY);
					if (intersection == null || intersection.nearEqual(position,1e-9)) { continue; }
					double distanceSquared = position.distanceSquaredTo(intersection);
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
			nextR.position = closestIntersection.clone();
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
	
	public Retracer clone() { return new Retracer(position.clone(),angle,scattering,sourcePower,energyPercentage,distanceTravelled,ignoreComponent,root); }
}
