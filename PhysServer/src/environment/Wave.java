package environment;

import main.Lis;

/*
 * OBSOLETE CLASS
 * WAVE SUPERPOSITION REPLACED
 * WITH BULK ALGORITHM
 * LOOK TO Environment.java
 */

public class Wave { //one single light ray
	public double amplitude = 1;
	public double shift = 0;
	public double length = 0;
	
	Wave(double amplitude, double shift, double length){
		this.amplitude = amplitude;
		this.shift = shift;
		this.length = length;
	}
	
	void add(Wave addWave) { //addition of sine waves with different amplitude and/or shift but same wavelength
		//System.out.println("=====ADD WAVE=====");
		final double wavelengthNormalizer = (2*Math.PI/Lis.wavelength);
		double pd = wavelengthNormalizer * ((addWave.length + addWave.shift) - (length + shift)); //absolute phase difference between two waves (treat base wave as no shift)
		
		//System.out.println("a1 "+amplitude+" a2 "+addWave.amplitude+" pd "+pd+"   wavelength/2 "+(wavelength/2));
		double newAmplitude = Math.sqrt( amplitude*amplitude+addWave.amplitude*addWave.amplitude+2*amplitude*addWave.amplitude*Math.cos(pd) ); //amplitude of new wave
		//System.out.println("new amplitude "+newAmplitude);
		double newShift = (newAmplitude<Lis.nm2m(0.0001)) ? 0 : (Math.asin(addWave.amplitude*Math.sin(pd)/newAmplitude)); //shift of new wave
		if (amplitude+addWave.amplitude*Math.cos(pd)<0) { newShift = Math.signum(newShift)*(Math.PI - Math.abs(newShift)); } //arcsin ambiguity correction
		
		amplitude = newAmplitude;
		shift += newShift/wavelengthNormalizer;
//		length = Math.max(length,addWave.length);
	}
}