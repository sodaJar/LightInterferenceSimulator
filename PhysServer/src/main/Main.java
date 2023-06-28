package main;

import java.awt.Dimension;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import environment.Environment;

public class Main {
	static Scanner scanner = new Scanner(System.in);
	
	public static JProgressBar showProgressBar(int max) { return showProgressBar(max,"PROCESSING - PORT: "+Lis.instancePort); }
	public static JProgressBar showProgressBar(int max,String title) { return showProgressBar(max,title,400,80); }
	public static JProgressBar showProgressBar(int max,String title, int width, int height) {
		JFrame f = new JFrame();
		JPanel p = new JPanel();
		JProgressBar pb = new JProgressBar(0,max);
		p.add(pb);
		pb.setPreferredSize(new Dimension(width,height));
		f.add(p);
		f.setSize(width,height);
		f.setTitle(title);
		f.setResizable(false);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		return pb;
	}
	public static JFrame getComponentJFrame(JComponent jc) { return (JFrame)SwingUtilities.getRoot(jc); }
	public static void disposeProgressBar(JProgressBar pb) { ((JFrame)SwingUtilities.getRoot(pb)).dispose(); }
	public static void updateProgressBarTitle(JProgressBar pb,String title) { ((JFrame)SwingUtilities.getRoot(pb)).setTitle(title); }
	
	//occupy port to ensure single instance
	private static boolean isPortOccupied() {
		try {
			//occupy a port to ensure one instance of this program is running at the same time
			//the port is automatically freed by the OS when the program terminates
			@SuppressWarnings({ "resource", "unused" })
			ServerSocket socket = new ServerSocket(Lis.instancePort, 0, InetAddress.getByAddress(new byte[] {127,0,0,1}));
		}
		catch (Exception e) { return true; } //if a port is already occupied
		return false;
	}
	
	private static Object parseValue(String val) { //parses a string value
		switch (val.charAt(0)) {
		case 'f':
			return (Object)Double.parseDouble(val.substring(1));
		case 'i':
			return (Object)Integer.parseInt(val.substring(1));
		case 'b':
			return (Object)Boolean.parseBoolean(val.substring(1));
		}
		return null;
	}
	
	public static void main(String[] args) {
		//args format:
		//[wavelength, scatterCount, testScatterCount, threadCount, instancePort, <component 1 name>, <property1> <value1>;<property2> <value2>;..., <c 2 name>, <p1> <v1>;, ...]
		//the first character of a value string indicates its type: f -> double, i -> int, b -> boolean
//		args = new String[] {"f650", "i10", "i30", "i20", "i9053", "Laser","beamWidth f0.0001;power f1;positionX f0;positionY f0.4;quality f1;rotation f3.14159265358979;", "Screen", "screenWidth f0.2;resolution i100;positionX f0;positionY f0;quality f1;rotation f0;", "SingleSlit", "obstacleWidth f1;slitWidth f0.000005;positionX f0;positionY f0.2;quality f1;rotation f0;"};
		if (args.length<7) { //when the physics server starts directly
			Environment env = new Environment();
			String option = JOptionPane.showInputDialog("PRESET\n0 - LASER\n1 - MULTI SLIT\n2 - GLASS SLAB EDGE\n");
			if (option==null || option.isBlank()) { return; }
			switch(option){
				case "0":
					Setup setup = new Setup();
					setup.addComponent("Screen", Map.ofEntries(Map.entry("position",new Vec(0)),Map.entry("angle",0),Map.entry("screenWidth",0.1),Map.entry("resolution",150),Map.entry("quality",1)));
					setup.addComponent("Laser", Map.ofEntries(Map.entry("position",new Vec(0,1)),Map.entry("angle",Math.PI),Map.entry("beamWidth",Lis.mm2m(0.1)),Map.entry("power",1),Map.entry("quality",1)));
					long startTime = System.nanoTime();
					setup.run();
					long endTime = System.nanoTime();
					System.out.println((endTime-startTime)/1000000000.0+"s");
					return;
				case "1":
					final int nSlits = Integer.parseInt(JOptionPane.showInputDialog("NUMBER OF SLITS"));
					if (nSlits <= 0) return; 
					for (int i = -450-(int)Math.pow(2,-nSlits/10+5)*50; i < 450+(int)Math.pow(2,-nSlits/10+5)*50; i++) {
						env.addObserver(0.5,i*0.0001);
					}
					for (int i = -nSlits/2; i < nSlits-nSlits/2; i++) {
						for (int j = -300; j < 300; j++) {
							env.addSource(0,(nSlits%2!=0?i:(i+0.5))*Lis.wavelength*12+(j+0.5)*Lis.wavelength/100);
						}
					}
					env.run();
					break;
				case "2": //GLASS SLAB
					final double s = 0.001/1000;
					for (int i = -1000; i < 1000; i++) {
						env.addObserver(0.05,i*0.0000005);
					}
					for (int i = -5000; i < 0; i++) {
						env.addSource(0,i*s);
					}
					for (int i = 0; i < 5000; i++) {
						int idx = env.addSource(0,i*s);
						env.sources.get(idx).shift = Lis.wavelength/3;
					}
					env.run();
					break;
			}
			int n = env.observers.size();
			//DISPLAY READINGS
			double displacements[] = new double[n];
			double observations[] = new double[n];
			for (int i = 0; i < n; i++) {
				displacements[i] = Lis.m2mm(env.observers.get(i).position.y);//i-nObservers/2;
				observations[i] = Math.pow(env.observers.get(i).observation,2);
			}
			displayGraph(displacements,observations);
			return;
		}
		
		//args format:
		//[wavelength, scatterCount, testScatterCount, threadCount, instancePort, <component 1 name>, <property1> <value1>;<property2> <value2>;..., <c 2 name>, <p1> <v1>;, ...]
		
		Lis.instancePort = (int)parseValue(args[4]);
		if (isPortOccupied()) {
			quitWithMessage("Another simulation instance is likely already running,\nchange the instance port to start a new instance");
		}
		try { //listens to any runtime error in the main program
			Lis.wavelength = Lis.nm2m((double)parseValue(args[0]));
			Lis.scatterCount = (int)parseValue(args[1])*1000;
			Lis.collisionTestSize = (int)parseValue(args[2]);
			Lis.threadCount = (int)parseValue(args[3]);
			
			Setup setup = new Setup();
			
			for (int idx = 5; idx < args.length; idx += 2) { //go through every component to be added
				String component = args[idx];
				HashMap<String,Object> properties = new HashMap<String,Object>();
				String propVal = args[idx+1];
				Vec cPosition = new Vec(); //because the GUI doesn't take the vector data type, positionX and positionY need to be joined manually to form position
				while(true) { //get the properties and corresponding values of the component, being careful of special properties whose formats are unusual
					int mid = propVal.indexOf(' ');
					if (mid<0) { break; }
					int end = propVal.indexOf(';');
					String property = propVal.substring(0,mid);
					Object value = parseValue(propVal.substring(mid+1,end));
					propVal = propVal.substring(end+1);
					if (property.equals("positionX")) {
						cPosition.x = (Double)value;
						continue;
					}else if (property.equals("positionY")) {
						cPosition.y = (Double)value;
						continue;
					}
					if (property.equals("rotation")) { property = "angle"; } //this property is called "rotation" in the GUI, but "angle" in this physics server
					properties.put(property, value);
					println(property+" "+value);
				}
				properties.put("position", cPosition);
				setup.addComponent(component, properties); //add the component, their properties, and their values to the setup
			}
			long startTime = System.nanoTime();
			setup.run();
			long endTime = System.nanoTime();
			System.out.println((endTime-startTime)/1000000000.0+"s"); //for checking performance, debug only
		}catch(Exception e) { //show a dialog if any error occurs during execution
			String stack = "";
			for (StackTraceElement ste: e.getStackTrace()) { stack += "- "+ste.toString()+"\n"; }
			JOptionPane.showMessageDialog(null, "An unexpected error occured\n"+e.toString());
			quitWithMessage("Stack Trace:\n"+stack);
		}
		return;
	}
	//display the intensity vs displacement graph
	public static void displayGraph(double[] displacements,double[] observations) {
		//JFreeChart
		JFrame frame = new JFrame();
		XYSeries series = new XYSeries("Readings");
		for (int i = 0; i < displacements.length; i++) { series.add(displacements[i],observations[i]); }
		 XYDataset dataset = new XYSeriesCollection(series);
		 JFreeChart chart = ChartFactory.createScatterPlot("Intensity - Displacement Graph", "Displacement (mm)","Normalized Intensity", dataset);
		 
		 frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		 frame.setSize(500,500);
		 frame.add(new ChartPanel(chart));
		 frame.setVisible(true);
	}
	//display a message box and exit the program
	public static void quitWithMessage(String message) {
		JOptionPane.showMessageDialog(null, message);
		System.exit(1);
	}
	//util wrapper methods
	public static String nextLine() {return scanner.nextLine(); }
	public static int nextInt() { return Integer.parseInt(scanner.nextLine()); }
	public static float nextFloat() { return Float.parseFloat(scanner.nextLine()); }
	public static boolean nextBoolean() { return Boolean.parseBoolean(scanner.nextLine()); }
	public static void print(String message){ System.out.print(message); }
	public static void print(int message){ System.out.print(message); }
	public static void print(float message){ System.out.print(message); }
	public static void print(boolean message){ System.out.print(message); }
	public static void println(String message){ System.out.println(message); }
	public static void println(int message){ System.out.println(message); }
	public static void println(float message){ System.out.println(message); }
	public static void println(boolean message){ System.out.println(message); }
	public static void println(){ System.out.println(); }

}
