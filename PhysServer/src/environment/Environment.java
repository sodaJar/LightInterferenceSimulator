package environment;

import java.util.ArrayList;

import javax.swing.JProgressBar;

import main.Lis;
import main.Main;

public class Environment {
	public ArrayList<Source> sources = new ArrayList<Source>();
	public ArrayList<Observer> observers = new ArrayList<Observer>();
	
	public void run() {
		JProgressBar pb = Main.showProgressBar(observers.size(), "PROCESSING - PORT: "+Lis.instancePort);

		
		int nObservers = observers.size();
		double sqrtNObservers = Math.sqrt(nObservers);

		for (int i = 0; i < nObservers; i++) {
			pb.setValue(i);
			Observer o = observers.get(i);
			double s0 = 0;
			double c0 = 0;
			for (int j = 0; j < sources.size(); j++) {
				Source s = sources.get(j);
				double length = Math.sqrt(Math.pow(o.position.x-s.position.x,2)+Math.pow(o.position.y-s.position.y,2));
				
				s0 += (s.amplitude/sqrtNObservers)*Math.sin((length+s.shift)*Lis.wavelengthNormalizer);
				c0 += (s.amplitude/sqrtNObservers)*Math.cos((length+s.shift)*Lis.wavelengthNormalizer);
			}
			o.shift = Math.atan2(s0,c0); //arc-tangent on drugs
			o.observation = Math.sqrt(s0*s0+c0*c0);
			o.shift *= Lis.wavelengthDenormalizer;
		}
		Main.disposeProgressBar(pb);
	}
	public void addEntries(ArrayList<ArrayList<Source>> entries) {
		for (ArrayList<Source> entry : entries) {
			sources.addAll(entry);
		}
	}
	public void addEntry(ArrayList<Source> entry) {
		sources.addAll(entry);
	}
	
	public int addSource(double x, double y) {
		sources.add(new Source(x, y));
		return sources.size()-1;
	}
	public int addObserver(double x, double y) {
		observers.add(new Observer(x, y));
		return sources.size()-1;
	}
	
}

