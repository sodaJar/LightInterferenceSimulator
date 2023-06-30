package components;

import main.Vec;

public class HitboxSegmentPuppet extends HitboxSegment {
	public HitboxSegment master;
	
	public HitboxSegmentPuppet(Vec pos1, Vec pos2, boolean responsive, Component owner, HitboxSegment master) {
		super(pos1, pos2, responsive, owner);
		this.master = master;
	}
	
	@Override
	public HitboxSegmentPuppet clone() {
		return new HitboxSegmentPuppet(pos1,pos2,responsive,owner,master);
	}
}
