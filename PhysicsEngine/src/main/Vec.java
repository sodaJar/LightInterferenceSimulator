package main;

public class Vec {
	public double x;
	public double y;
	
	public Vec() {
		
	}
	public Vec(double x, double y) {
		this.x = x;
		this.y = y;
	}
	public Vec(double n) {
		x = n;
		y = n;
	}
	
	public static Vec newVecModArg(double mod, double arg) {
		return new Vec(mod*Math.cos(arg), mod*Math.sin(arg));
	}
	public static Vec newVecInfinite() {
		return new Vec(Double.POSITIVE_INFINITY);
	}
	
	public boolean nearEqual(Vec v) { return Lis.nearEqual(x, v.x) && Lis.nearEqual(y, v.y);}
	public boolean nearEqual(Vec v, double tolerance) { return Lis.nearEqual(x, v.x, tolerance) && Lis.nearEqual(y, v.y, tolerance);}
	public boolean isInfinite() { return Double.isInfinite(x) || Double.isInfinite(y); }
	public boolean isInfinite(boolean checkX,boolean checkY) { //if a component is checked, that component must be infinite to return true
		return (!checkX || Double.isInfinite(x)) && (!checkY || Double.isInfinite(y));
	}
	public double dot(Vec v) {
		return x*v.x+y*v.y;
	}
	public double length() {
		return Math.sqrt(lengthSquared());
	}
	public double lengthSquared() {
		return x*x+y*y;
	}
	public double distanceTo(Vec v) {
		return Math.sqrt((v.x-x)*(v.x-x)+(v.y-y)*(v.y-y));
	}
	public double distanceSquaredTo(Vec v) {
		return (v.x-x)*(v.x-x)+(v.y-y)*(v.y-y);
	}
	public double angle() {
		return Math.atan2(y, x);
	}
	public Vec to(Vec v) {
		return new Vec(v.x-x,v.y-y);
	}
	public Vec clone() {
		return new Vec(x,y);
	}
	public String toString() {
		return "("+x+", "+y+")";
	}
	
	//self modifying methods
	public void swapWith(Vec v) {
		double newX = v.x;
		double newY = v.y;
		v.x = x;
		v.y = y;
		x = newX;
		y = newY;
	}
	public void set(Vec v) {
		x = v.x;
		y = v.y;
	}
	public void setModArg(double mod, double arg) {
		x = mod*Math.cos(arg);
		y = mod*Math.sin(arg);
	}
	public void add(Vec v) {
		x += v.x;
		y += v.y;
	}
	public void add(double n) {
		x += n;
		y += n;
	}
	public void subtract(Vec v) {
		x -= v.x;
		y -= v.y;
	}
	public void subtract(double n) {
		x -= n;
		y -= n;
	}
	public void multiply(Vec v) {
		x *= v.x;
		y *= v.y;
	}
	public void multiply(double n) {
		x *= n;
		y *= n;
	}
	public void divideBy(Vec v) {
		x /= v.x;
		y /= v.y;
	}
	public void divideBy(double n) {
		x /= n;
		y /= n;
	}
	public void negate() {
		x = -x;
		y = -y;
	}
	public void negateX() {
		x = -x;
	}
	public void negateY() {
		y = -y;
	}
	public void flipXY() {
		x = x + y;
		y = x - y;
		x = x - y;
	}
	public void rotate(double angle) { //rotates a 2D vector about the origin by angle in radians
		double s = Math.sin(angle);
		double c = Math.cos(angle);
		double newX = c*x-s*y;
		y = s*x+c*y;
		x = newX;
	}
	//non self modifying methods
	public Vec linearInterpolated(Vec target, double weight) {
		return new Vec(x+(target.x-x)*weight,y+(target.y-y)*weight);
	}
	public Vec added(Vec v) {
		return new Vec(x+v.x,y+v.y);
	}
	public Vec added(double n) {
		return new Vec(x+n,y+n);
	}
	public Vec subtracted(Vec v) {
		return new Vec(x-v.x,y-v.y);
	}
	public Vec subtracted(double n) {
		return new Vec(x-n,y-n);
	}
	public Vec multiplied(Vec v) {
		return new Vec(x*v.x,y*v.y);
	}
	public Vec multiplied(double n) {
		return new Vec(x*n,y*n);
	}
	public Vec dividedBy(Vec v) {
		return new Vec(x/v.x,y/v.y);
	}
	public Vec dividedBy(double n) {
		return new Vec(x/n,y/n);
	}
	public Vec negated() {
		return new Vec(-x,-y);
	}
	public Vec negatedX() {
		return new Vec(-x,y);
	}
	public Vec negatedY() {
		return new Vec(x,-y);
	}
	public Vec flippedXY() {
		return new Vec(y,x);
	}
	public Vec rotated(double angle) { //rotates about the origin by angle in radians
		double s = Math.sin(angle);
		double c = Math.cos(angle);
		return new Vec(c*x-s*y, s*x+c*y);
	}
}
