package components;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import environment.Ray;
import environment.Retracer;
import main.Lis.ToBeOverriden;

public class Component extends Ray{

//	public String name = "";
	public double quality = 1;
	public double innateQuality = 1;
	public ArrayList<HitboxSegment> hitboxes = new ArrayList<HitboxSegment>();
	
	@ToBeOverriden
	public void  initialize() {}
	@ToBeOverriden
	public void retrace(Retracer r, ArrayList<Retracer> results) {}
	
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
