package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

import components.C_Screen;
import components.Component;
import components.HitboxSegment;
import environment.Retracer;
import environment.RetracerRoot;

/*
 * A Setup is an experiment environment, it contains all the Components required to run the simulation.
 * The setup iteratively "expand" existing retracers (the first retracers are root retracers that are points on the screen) to simulate the path
 * of light rays until there are none left, meaning that all retracers have either escaped into nothingness, or blocked by a component,
 * or landed on a light source. The moment a retracer lands on a light source, it adds, to an existing wave vector ((0, 0) at the start) held by
 * the root retracer, the vector form of the light ray that would travel through the same path travelled by the retracer
 */

public class Setup {
	public ArrayList<Component> components = new ArrayList<Component>(); //all components in the experimental space
	public C_Screen screen; //the only screen (also in the components)
	
	private int idx; //idx used for for-each loops
	private ArrayList<Thread> threads = new ArrayList<Thread>(); //container for threads
	
	class RunRetracerRootsRunnable implements Runnable { //a single thread
		private ArrayList<RetracerRoot> retracerRoots; //roots to process, i.e. the workload of the thread
		public ArrayList<Retracer> currentRetracers = new ArrayList<Retracer>();
		public ArrayList<Retracer> nextRetracers = new ArrayList<Retracer>();
		private Setup setup;
		private JProgressBar pb;
		//passing in values from the parent class
		public RunRetracerRootsRunnable(ArrayList<RetracerRoot> retracerRoots, Setup setup, JProgressBar pb) {
			this.retracerRoots = retracerRoots;
			this.setup = setup;
			this.pb = pb;
		}
		@SuppressWarnings("unchecked")
		@Override
		public void run() { //the run function of the thread
			for (RetracerRoot retracerRoot : retracerRoots) {
				nextRetracers.clear();
				currentRetracers.clear();
				currentRetracers.add(retracerRoot);
				int retraceLimit = 15;
				while (retraceLimit > 0) { //loop until there are no nextRetracers or max steps exceeded (likely due to reflective surfaces facing each other)
					retraceLimit --;
					nextRetracers.clear(); //prepare for calculation
					for (Retracer r : currentRetracers) { //for each current retracer, calculate next retracers
						nextRetracers.addAll(r.retrace(setup));
					};
					if (nextRetracers.size() == 0) { break; }
					currentRetracers = (ArrayList<Retracer>)nextRetracers.clone();
				}
				idx++; //global idx for tracking progress
				Main.updateProgressBarTitle(pb, "PROCESSING "+setup.idx+"/"+setup.screen.retracerRoots.size());
				pb.setValue(idx);
			}
		}
	}
	
	public void run() { //start the setup by retracing all points (root retracers) on the screen 
		JProgressBar pb = Main.showProgressBar(screen.retracerRoots.size()-1,"PREPARING THREADS");
		//when the user closes the progress bar, the program should exit
		Main.getComponentJFrame(pb).setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		//split workload into batches according the the thread count global setting
		int batchSize = (int)Math.round(Math.ceil(screen.retracerRoots.size()/(double)Lis.threadCount));
		ArrayList<RetracerRoot> batch = new ArrayList<RetracerRoot>();
		idx = 0;
		for (int i = 0; i < screen.retracerRoots.size(); i++) {
			batch.add(screen.retracerRoots.get(i));
			if((i+1)%batchSize==0 || i == screen.retracerRoots.size()-1) { //each thread gets a workload no larger than the batch size
				Thread thread = new Thread(new RunRetracerRootsRunnable(batch,this,pb));
				batch = new ArrayList<RetracerRoot>();
				thread.start(); //calls the run method of the runnable class
				threads.add(thread);
			}
		}
		Main.println("All threads added");
		for (Thread thread : threads) { //wait for all threads to finish execution
			try { thread.join(); }
			catch (InterruptedException e) { JOptionPane.showMessageDialog(null, "Calculation thread exception\n"+e.toString()); }
		}
		Main.println("All threads joined");
		Main.disposeProgressBar(pb);
		
		double[] displacements = new double[screen.retracerRoots.size()];
		double[] observations = new double[screen.retracerRoots.size()];
		double mean = 0;
		idx = 0;
		for (RetracerRoot retracerRoot : screen.retracerRoots) {
			observations[idx] = retracerRoot.vecWave.lengthSquared()*100; //the intensity at each point on screen, multiply by a constant to make values pretty
			displacements[idx] = Lis.m2mm(retracerRoot.displacement); //the displacement of the point relative to the screen center (left = negative)
			mean += observations[idx];
			idx ++;
		}
		mean /= observations.length;
		Main.println("All calculations completed");
		double[] sortedObs = observations.clone();
		Arrays.sort(sortedObs);
		Main.displayGraph(displacements, observations, 1.05*sortedObs[sortedObs.length-1], "default", -70);
		Main.displayGraph(displacements, observations, Math.min(1.05*sortedObs[sortedObs.length-1],mean*5), "crop via mean", 0);
		Main.displayGraph(displacements, observations, Math.min( 1.05*sortedObs[sortedObs.length-1],sortedObs[(int)(sortedObs.length*0.75)]*2.5 ), "crop via median", 70);
	}

	public void addComponent(String componentName, Map<String,Object> properties) {
		Component component = null;
		//instance the component from string name
		try { component = (Component)Class.forName("components.C_"+componentName).getConstructor().newInstance(); }
		catch (Exception e) { Main.quitWithMessage("Component <"+componentName+"> could not be instanced\n"+e.toString()); }
		//set properties
		for (Map.Entry<String,Object> entry : properties.entrySet()) {
			try { component.getClass().getField(entry.getKey()).set(component, entry.getValue()); }
			catch (Exception e) { Main.quitWithMessage("Property <"+entry.getKey()+"> could not be set for component <"+componentName+">\n"+e.toString()); }
		}
		component.angle = Lis.normalizeAngle(component.angle); //this is needed for other procedures
		if (component instanceof C_Screen) { screen = (C_Screen)component; }
		components.add(component);
		component.initialize();
		for (HitboxSegment hb : component.hitboxes) { //denormalize hitboxes
			hb.pos1.rotate(component.angle);
			hb.pos1.add(component.position);
			hb.pos2.rotate(component.angle);
			hb.pos2.add(component.position);
		}
	}
}


