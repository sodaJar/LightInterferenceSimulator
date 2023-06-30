package main;

import java.awt.Dimension;
import java.util.HashMap;
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
	
	private static Setup interpretArgs(String[] args) {
		Lis.wavelength = Lis.nm2m((double)parseValue(args[0]));
		Lis.wavelengthNormalizer = 2*Math.PI/Lis.wavelength;
		Lis.wavelengthDenormalizer = 1/Lis.wavelengthNormalizer;
		Lis.scatterCount = (int)parseValue(args[1])*1000;
		Lis.collisionTestSize = (int)parseValue(args[2]);
		Lis.threadCount = (int)parseValue(args[3]);
		
		Setup setup = new Setup();
		
		for (int idx = 4; idx < args.length; idx += 2) { //go through every component to be added
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
				if (property.equals("rotation")) { property = "angle"; } //this property is called "rotation" in the GUI, but "angle" in this physics engine
				properties.put(property, value);
				println(property+" "+value);
			}
			properties.put("position", cPosition);
			setup.addComponent(component, properties); //add the component, their properties, and their values to the setup
		}
		return setup;
	}
	
	public static void main(String[] args) {
		if (args.length < 6) { System.exit(0); } //if the user directly clicks on the physics engine, nothing should happen
		try { //listens to any runtime error in the main program
			Setup setup = interpretArgs(args);
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
	public static void print(double message){ System.out.print(message); }
	public static void print(boolean message){ System.out.print(message); }
	public static void println(String message){ System.out.println(message); }
	public static void println(int message){ System.out.println(message); }
	public static void println(float message){ System.out.println(message); }
	public static void println(double message){ System.out.println(message); }
	public static void println(boolean message){ System.out.println(message); }
	public static void println(){ System.out.println(); }

}
