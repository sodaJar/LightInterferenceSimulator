package main;

public class Lis {
	
	//to be passed in from UI app
	public static int instancePort = 9053; //the port to occupy
	public static double wavelength = nm2m(650); //the global wavelength of light
	public static int scatterCount = 10000; //number of scattered rays per hitbox
	public static int collisionTestSize = 25; //number of test rays with retracer per hitbox
	public static int threadCount = 30; //number of threads to generate
	
	public @interface ToBeOverriden {} //custom annotation
	
	public static final double TWO_PI = 2*Math.PI;
	public static final double PI_BY_TWO = Math.PI/2;
	
	public static double wavelengthNormalizer; //changes wavelengths in meters to phase in radians, initialized on runtime when wavelength is passed in
	public static double wavelengthDenormalizer; //changes phase in radians to wavelengths in meters, initialized on runtime when wavelength is passed in
	
	public static boolean nearEqual(double a) { return nearEqual(a,0,1e-10); }
	public static boolean nearEqual(double a, double b) { return nearEqual(a,b,1e-10); }
	public static boolean nearEqual(double a, double b,double tolerance) { return Math.abs(a-b)<tolerance; } //for comparing doubles
	
	public enum LINE_TYPE{ //line types for intersection checking
		POINT, //the <direction> does not in this case matter
		SEGMENT, //the segment from <position> to <position> + <direction>
		RAY, //the ray from <position> heading towards <direction>
		LINE //the line on lying on  <position> extending along <direction> 
	}
	
	//the method gets the intersection of the two lines and then verifies if the intersection is valid given the line types
	public static Vec getIntersection(Vec pos1, Vec dir1, Vec pos2, Vec dir2, LINE_TYPE line1Type, LINE_TYPE line2Type) {
		Vec intersection = getLineIntersection(pos1, dir1, pos2, dir2); //the intersection of lines without considering line types(rays, segments, etc.)
		if (intersection == null) { return null; }
		if (intersection.isInfinite()) { //if the lines are collinear, calculate a new intersection
			if (pos1.nearEqual(pos2)) { return pos1.clone(); } //if they on directly on each other, they intersect at that point
			boolean steep = Math.abs(pos1.y-pos2.y)>Math.abs(pos1.x-pos2.x); //steepness is the difference in positions since they are collinear
			if (steep) { //use y instead of x if it's steep to judge range
				pos1 = pos1.flippedXY(); //clones must created so the edits don't effect the original
				dir1 = dir1.flippedXY();
				pos2 = pos2.flippedXY();
				dir2 = dir2.flippedXY();
			}
			//the idea is to reduce the position direction pair to a 1D range, since the lines are collinear
			Vec minMax1 = getLineMinMax1D(pos1.x,dir1.x,line1Type); //the borders of the first range
			Vec minMax2 = getLineMinMax1D(pos2.x,dir2.x,line2Type); //the borders of the second range
			if (minMax1.x > minMax2.y || minMax1.y < minMax2.x) { return null; } //if they don't overlap
			double minOverlap = Math.max(minMax1.x, minMax2.x); //min overlapping point
			double maxOverlap = Math.min(minMax1.y, minMax2.y); //max overlapping point
			double newIntersection = Double.isInfinite(minOverlap)?maxOverlap:minOverlap; //finds a suitable intersection x 
			if (Double.isInfinite(newIntersection)) { return intersection; } //two collinear lines of type LINE
			//calculate the intersection point from the x (or y if steep) coordinate
			if (steep) { return new Vec(getYFromXVectorEquation(pos1,dir1,newIntersection), newIntersection); }
			return new Vec(newIntersection, getYFromXVectorEquation(pos1,dir1,newIntersection));
		}
		//normal intersection, not collinear
		boolean steep1 = Math.abs(dir1.y)>Math.abs(dir1.x);
		boolean steep2 = Math.abs(dir2.y)>Math.abs(dir2.x);
		if (steep1) {
			pos1 = pos1.flippedXY(); //clones must created so the edits don't effect the original
			dir1 = dir1.flippedXY();
		}
		if (steep2) {
			pos2 = pos2.flippedXY(); //clones must created so the edits don't effect the original
			dir2 = dir2.flippedXY();
		}
		Vec minMax1 = getLineMinMax1D(pos1.x,dir1.x,line1Type);
		Vec minMax2 = getLineMinMax1D(pos2.x,dir2.x,line2Type);
		//check if the intersection point is valid for both ranges with their respective line types
		if (steep1?(intersection.y<minMax1.x || intersection.y>minMax1.y):(intersection.x<minMax1.x || intersection.x>minMax1.y)) { return null; } //line1 not in range of minMax
		if (steep2?(intersection.y<minMax2.x || intersection.y>minMax2.y):(intersection.x<minMax2.x || intersection.x>minMax2.y)) { return null; } //line2 not in range of minMax
		return intersection;
	}
	public static Vec getLineIntersection(Vec pos1, Vec dir1, Vec pos2, Vec dir2) { //line-line intersection
		double numerator = dir2.x*(pos2.y-pos1.y)+dir2.y*(pos1.x-pos2.x);
		double denominator = dir2.x*dir1.y-dir2.y*dir1.x;
		//return infinite if the lines are the same, return null if they are parallel and never intersect
		if (Lis.nearEqual(denominator)) { return Lis.nearEqual(numerator)?Vec.newVecInfinite():null; }
		//else, return the intersection point of the lines
		Vec intersection = pos1.added(dir1.multiplied(numerator/denominator));
		return intersection;
	}
	//gets the 1D range based on the line type
	private static Vec getLineMinMax1D(double pos, double dir, LINE_TYPE type) {
		double min = Double.NEGATIVE_INFINITY;
		double max = Double.POSITIVE_INFINITY;
		switch(type) {
		case POINT: //value to value with no dir
			min = max = pos;
			break;
		case SEGMENT: //value to value
			min = Math.min(pos, pos+dir);
			max = 2*pos+dir-min;
			break;
		case RAY: //value to infinity (or negative infinity to value)
			if (dir > 0) { min = pos; }
			else if (dir < 0) { max = pos; }
			break;
		case LINE: break; //negative infinity to infinity by default
		}
		return new Vec(min,max);
	}
	//calculates the y coordinate from the x coordinate on the vector equation of a line
	public static double getYFromXVectorEquation(Vec pos, Vec dir, double x) { return dir.x==0?(nearEqual(pos.x,x)?pos.y:Double.NaN):((dir.y/dir.x)*(x-pos.x)+pos.y); }
	//calculates the x coordinate from the y coordinate on the vector equation of a line
	public static double getXFromYVectorEquation(Vec pos, Vec dir, double y) { return dir.y==0?(nearEqual(pos.y,y)?pos.x:Double.NaN):((dir.x/dir.y)*(y-pos.y)+pos.x); }
	//converts an angle in radians to an angle between -PI and PI inclusive. Algorithm taken from apache commons:
	//<https://commons.apache.org/proper/commons-math/javadocs/api-3.6.1/org/apache/commons/math3/util/MathUtils.html>
	public static double normalizeAngle(double a) { return a-TWO_PI*Math.floor((a+Math.PI)/TWO_PI); }
	//converts an angle in radians to an angle in range [0, 2*PI)
	public static double normalizeAngle2PI(double a) { return a%TWO_PI+(a<0?TWO_PI:0); } //unused
	//similar to normalizeAngle() but takes input only from -3PI to 3PI
	public static double normalizeAngleShallow(double a) { return (a>Math.PI)?(a-TWO_PI):((a<-Math.PI)?(a+TWO_PI):a); }
	//gets the difference of two angles (positive means clockwise)
	public static double fromAngleToAngle(double from, double to) { return normalizeAngle(to-from); }
	//similar to fromAngleToAngle() but only takes inputs in range [-PI, PI]
	public static double fromNormAngleToNormAngle(double normalizedFrom, double normalizedTo) { return normalizeAngleShallow(normalizedTo - normalizedFrom); }
	
	//unit conversions
	public static double m2nm(double m) { return m*1e9; }
	public static double nm2m(double nm) {	 return nm/1e9; }
	public static double m2cm(double m) { return m*100; }
	public static double cm2m(double cm) { return cm/100; }
	public static double m2mm(double m) { return m*1000; }
	public static double mm2m(double mm) { return mm/1000; }
	
	public static double d2r(double deg) { return deg*Math.PI/180; }
	public static double r2d(double rad) { return rad*180/Math.PI; }
}
