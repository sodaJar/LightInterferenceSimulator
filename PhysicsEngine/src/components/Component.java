package components;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import environment.Ray;
import environment.Retracer;

public abstract class Component extends Ray{

	public double quality = 1;
	public double innateQuality = 1;
	public ArrayList<HitboxSegment> hitboxes = new ArrayList<HitboxSegment>();
	
	public abstract void  initialize();
	public abstract void retrace(Retracer r, ArrayList<Retracer> results);
	
	public void setProperty(String propertyName, Object value) {
		try { getClass().getField(propertyName).set(this,value); }
		catch (Exception e) { JOptionPane.showMessageDialog(null, e.toString()); }
	}
	
	public Object getProperty(String propertyName) {
		try { return getClass().getField(propertyName).get(this); }
		catch (Exception e) { JOptionPane.showMessageDialog(null, e.toString()); }
		return null;
	}
}
