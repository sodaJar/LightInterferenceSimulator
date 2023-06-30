package components;

import main.Vec;

public class HitboxSegment {
	public Vec pos1;
	public Vec pos2;
	public boolean responsive;
	public Component owner;
	
	public HitboxSegment(Vec pos1, Vec pos2, boolean responsive, Component owner) {
		this.pos1 = pos1;
		this.pos2 = pos2;
		this.responsive = responsive;
		this.owner = owner;
	}
	public HitboxSegment(double pos1x, double pos1y, double pos2x, double pos2y, boolean responsive, Component owner) {
		this.pos1 = new Vec(pos1x,pos1y);
		this.pos2 = new Vec(pos2x,pos2y);
		this.responsive = responsive;
		this.owner = owner;
	}
	public Vec getDirVec() {
		return pos1.to(pos2);
	}
	public HitboxSegment clone() {
		return new HitboxSegment(pos1, pos2, responsive, owner);
	}
	public HitboxSegmentPuppet newPuppet() {
		return new HitboxSegmentPuppet(pos1, pos2, responsive, owner, this);
	}
}
