package LandscapeDisplay;

import java.awt.Container;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JOptionPane;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.doublealgo.Transform;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import ch.epfl.lis.gnw.CancelException;
import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.gnw.GnwSettings;
import ch.epfl.lis.sde.SdeSettings;
import LandscapeDisplay.MySDE;
import LandscapeDisplay.SDETimeSeriesExperiment;

public class SDESolver {
	
	private int maxt_;
	private double dt_;
	private int numTimePoints_;
	private DoubleMatrix1D X0;
	
	private double noiseStrength;
	private int numSeries_;
	
	private boolean SimulateODE;
	private boolean SimulateSDE;
	
	private boolean islandscape;
	private boolean isODE;
	private boolean isRandomInitial;
	private double upBoundary;
	private double lowBoundary;
	
	/** The gene network */
	protected GeneNetwork grn_ = null;
	
	
	private MySDE system;
	private SDETimeSeriesExperiment ts;

	
	// ============================================================================
	// PUBLIC METHODS
	
	public SDESolver(GeneNetwork grn, boolean islandscape, double noiseStrength)
	{	
		this.grn_ = grn;
		X0 = grn_.getInitialState();
		
		system = new MySDE(grn_, islandscape);
		if( islandscape ) system.setName(grn_.getId()+"_NetLand");
		else system.setName(grn_.getId());
		system.setSigma(noiseStrength); 	
		
		ts = new SDETimeSeriesExperiment();
	}

	public boolean solveEquations_SDE() throws Exception{	
		//Log.info("Running integration of stochastic differential equations (SDE).\n");
		
		try {					
			ts.setSystem(system);
			ts.setMaxt(maxt_);
			ts.setDt(dt_);
			ts.setNumTimePoints(numTimePoints_);

			//set initials
			DoubleMatrix1D initialX0 = new DenseDoubleMatrix1D(X0.size());
			if( !isRandomInitial ){ //fix
				initialX0 = X0.copy();
			}else{ //random
				initialX0 = randomInitial(upBoundary, lowBoundary);
			}

			ts.setX0(initialX0);

			checkForInterruption();

			ts.run(system.getDimension());

			return true;
//			System.out.print("ODESolver End: "+System.currentTimeMillis()); 
			
		} catch (OutOfMemoryError e) {
			String message = "There is not enough memory available to run this program.\n";
			message += "Quit one or more programs, and then try again.\n";
			message += "If enough amounts of RAM are installed on this computer, \n" +
				"try to run the Java Virtual Machine (JVM) with the command-line argument -Xmx[XXXX]m to set the maximum memory, \n";
			//Log.error("TimeSeriesExperiment", message, e);
			JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.INFORMATION_MESSAGE);
			return false;
		} catch (Exception e) {
			//Log.error("TimeSeriesExperiment", "Error while running the time series example.", e);
			JOptionPane.showMessageDialog(null, "Error while running the time series", "Error",  JOptionPane.INFORMATION_MESSAGE);
			MsgManager.Messages.errorMessage(e, "Error", "");
			return false;
		}
		
		
	}
	
	public DoubleMatrix1D randomInitial(double upBoundary, double lowBoundary) {
		int dimension = grn_.getNodes().size();
		Random random = new Random();
		DoubleMatrix1D s = new DenseDoubleMatrix1D(dimension);
		
		for(int i=0;i<dimension;i++)
			s.set(i, random.nextDouble() * (upBoundary - lowBoundary) + lowBoundary);
		
		return s;
	}

//	public void solveEquations_landscape(double max, boolean isProb) throws Exception{
//		//Log.info("Running integration of landscape.\n");
//		
//		try {			
//			
//			SDETimeSeriesExperiment ts = new SDETimeSeriesExperiment();
//			ts.setSystem(system);
//			ts.setMaxt(maxt_);
//			ts.setDt(dt_);
//			ts.setNumTimePoints(numTimePoints_);
//			
//			
////			for(int i=0;i<numSeries_;i++){				
//				
//				
//				//random start values
//				DoubleMatrix1D X0 = new DenseDoubleMatrix1D(randomInput(max));
//				ts.setX0(X0);
//				
//				checkForInterruption();
//				ts.run(system.getDimension());
//				
////				if( isProb ){					
////					tss.add(ts);
////					timeSeries.add(ts.getTimeSeries());
////					timeScale.add(ts.getTimeScale());
////				}else{
//					int len = isConverged(ts);
//					if( len != -1 ){
//						tss.add(ts);
//						timeSeries.add(ts.getTimeSeries().viewPart(0, 0, len, X0.size()));
//						timeScale.add(ts.getTimeScale().viewPart(0, len));
//					}else{
//						tss = null;
//						timeSeries = null;
//						timeScale = null;
//					}
////				}
////			}
//				
//			
//			
////			//get attractors
////			checkAttractor(attractorTypes, sumPara, timeSeries);	
////			saveFile(tss, system.getName());
//			
//		} catch (OutOfMemoryError e) {
//			String message = "There is not enough memory available to run this program.";
//			message += "Quit one or more programs, and then try again.";
//			message += "If enough amounts of RAM are installed on this computer, " +
//				"try to run the Java Virtual Machine (JVM) with the command-line argument -Xmx1024m to use maximum 1024Mb of memory, " +
//				"-Xmx2048m to use max 2048Mb, etc.";
//			JOptionPane.showMessageDialog(null,  message, "Error", JOptionPane.INFORMATION_MESSAGE);
//		} catch (Exception e) {
//			//Log.error("TimeSeriesExperiment", "Error while running the time series example.", e);
//			JOptionPane.showMessageDialog(null, "Error while running the time series", "Error",  JOptionPane.INFORMATION_MESSAGE);
//			MsgManager.Messages.errorMessage(e, "Error", "");
//		}
//		
//		
//	}
	
//	private int isConverged(SDETimeSeriesExperiment ts) {
//		//check if converge
//		DoubleMatrix1D lasttime = ts.getTimeSeries().viewRow(ts.getTimeSeries().rows()-1);
//		DoubleMatrix1D secondlasttime = ts.getTimeSeries().viewRow(ts.getTimeSeries().rows()-2);
//
//		DoubleMatrix1D time = ts.getTimeScale().copy();
//		DoubleMatrix2D timecourse = ts.getTimeSeries().copy();
//
//		cern.jet.math.Functions F = cern.jet.math.Functions.functions;
////		while( lasttime.aggregate(secondlasttime, F.plus, F.chain(F.square, F.minus))>0.001 ){			
////			ts.setX0(lasttime);
////			ts.run(lasttime.size());
////
////			time = combineTwo1DMatrix(time, ts.getTimeScale()).copy();
////			timecourse = DoubleFactory2D.dense.appendRows(timecourse, ts.getTimeSeries());
////
////			lasttime = ts.getTimeSeries().viewRow(ts.getTimeSeries().rows()-1);
////			secondlasttime = ts.getTimeSeries().viewRow(ts.getTimeSeries().rows()-2);
////		}
//		//if not converged
//		if( lasttime.aggregate(secondlasttime, F.plus, F.chain(F.square, F.minus))>0.000001 ){			
//			return -1;
//		}
//		
//		ts.setTimeSeries(timecourse);
//		ts.setTimeScale(time);
//		
//
//		int len = 0;
//		//get unique states			
//		for(int j=0;j<timecourse.rows();j++){
//			if( lasttime.aggregate(timecourse.viewRow(j), F.plus, F.chain(F.square, F.minus))<0.001 ){
//				len = j;
//				break;
//			}
//			len++;
//		}
//
//		len = len<20?20:len;
//
//		//System.out.print("len:"+len);
//		return len;
//		
//	}
//	
//	private DoubleMatrix1D combineTwo1DMatrix(DoubleMatrix1D m1, DoubleMatrix1D m2){
//		DoubleMatrix1D output = new DenseDoubleMatrix1D(m1.size()+m2.size());
//		for(int i=0;i<m1.size();i++)
//			output.set(i, m1.get(i));
//		for(int i=0;i<m2.size();i++)
//			output.set(i+m1.size(), m2.get(i));
//
//		return output;			
//	}
//
//
//	private double[] randomInput(double max) {
//		int dimension = grn_.getNodes().size();
//		Random random = new Random();
//		double[] s = new double[dimension];
//		
//		for(int i=0;i<dimension;i++)
//			s[i] = random.nextDouble() * max;
//		
////		for(int i=dimension;i<dimension*2;i++)
////			s[i] = 0.1;
//		
//		return s;
//	}

	
	
	
	//=========================================================================================
	
	
	/** Test whether the user has interrupted the process (e.g. using "cancel" in the GUI) */
	private void checkForInterruption() throws CancelException {	
		if (GnwSettings.getInstance().stopBenchmarkGeneration())
			throw new CancelException("Benchmark generation canceled!");
	}
	

	// ============================================================================
	// GET AND SET METHODS
	
	public double getMaxt() {
		return maxt_;
	}

	public void setMaxt(int maxt_) {
		this.maxt_ = maxt_;
	}

	public double getDt() {
		return dt_;
	}

	public void setDt(double dt_) {
		this.dt_ = dt_;
	}

	public int getNumTimePoints() {
		return numTimePoints_;
	}

	public void setNumTimePoints(int numTimePoints_) {
		this.numTimePoints_ = numTimePoints_;
	}

	public DoubleMatrix1D getX0() {
		return X0;
	}

	public void setX0(DoubleMatrix1D x0) {
		X0 = x0;
	}

	public double getNoiseStrength() {
		return noiseStrength;
	}

	public void setNoiseStrength(double noiseStrength) {
		this.noiseStrength = noiseStrength;
	}

	public boolean isSimulateODE() {
		return SimulateODE;
	}

	public void setSimulateODE(boolean simulateODE) {
		SimulateODE = simulateODE;
	}

	public boolean isSimulateSDE() {
		return SimulateSDE;
	}

	public void setSimulateSDE(boolean simulateSDE) {
		SimulateSDE = simulateSDE;
	}

	public int getNumSeries() {
		return numSeries_;
	}

	public void setNumSeries(int numSeries) {
		this.numSeries_ = numSeries;
	}

	public boolean isIslandscape() {
		return islandscape;
	}

	public void setIslandscape(boolean islandscape) {
		this.islandscape = islandscape;
	}
	
	public GeneNetwork getGrn() {
		return grn_;
	}

	public void setGrn(GeneNetwork grn) {
		this.grn_ = grn;
	}

	public boolean isODE() {
		return isODE;
	}

	public void setODE(boolean isODE) {
		this.isODE = isODE;
	}

	public boolean isRandomInitial() {
		return isRandomInitial;
	}

	public void setRandomInitial(boolean isRandomInitial) {
		this.isRandomInitial = isRandomInitial;
	}

	public double getUpBoundary() {
		return upBoundary;
	}

	public void setUpBoundary(double upBoundary) {
		this.upBoundary = upBoundary;
	}

	public double getLowBoundary() {
		return lowBoundary;
	}

	public void setLowBoundary(double lowBoundary) {
		this.lowBoundary = lowBoundary;
	}

	public SDETimeSeriesExperiment getTs() {
		return ts;
	}

	public void setTs(SDETimeSeriesExperiment ts) {
		this.ts = ts;
	}

	
	
}
