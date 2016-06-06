package LandscapeDisplay;
import org.apache.commons.math3.ode.*;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import ch.epfl.lis.gnw.GeneNetwork;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JOptionPane;

import static java.lang.Math.*;

public class ODESolverTheta {	
	/** Output directory */
	private double t;
	private GeneNetwork grn;
	private boolean islandscape;
	private boolean isRandomInitial;

	
	private DoubleMatrix2D atimeSeries;
	private DoubleMatrix1D atimeArray;

	private ArrayList<DoubleMatrix2D> timeSeries;
	private ArrayList<DoubleMatrix1D> timeScale;
	
	private FirstOrderIntegrator dp853;
	private FirstOrderDifferentialEquations ode;
	
	private DoubleMatrix1D initialX0;

	
	public ODESolverTheta(GeneNetwork grn, int t, DoubleMatrix2D A){
		this.t = t;
		this.grn = grn;

		dp853 = new DormandPrince853Integrator(0.000001, 1, 0.001, 0.001);
		ode = new MyODETheta(grn, false, A);		
	}

	public void solveEquations_ODE(){
								
			atimeSeries = new DenseDoubleMatrix2D((int)t, ode.getDimension());
			atimeArray = new DenseDoubleMatrix1D((int)t);
			
			double[] x = new double[initialX0.size()];
			
			StepHandler stepHandler = new StepHandler() {
				int count = 0;
			
				public void init(double t0, double[] y0, double t) {

				}

				
				public void handleStep(StepInterpolator interpolator, boolean isLast) {
					double   t = interpolator.getCurrentTime();
					double[] y = interpolator.getInterpolatedState();

					if( t > count ){
						for(int i=0;i<y.length;i++)
							atimeSeries.set(count, i, y[i]);
						atimeArray.set(count, t);
						count++;
					}
				}
			};
			dp853.addStepHandler(stepHandler);
			dp853.integrate(ode, 0.0, initialX0.toArray(), t, x);
			
			//Log.info("Is it converged? " + ts.isConverged() + "\n");
			
			timeSeries.add(atimeSeries);
			timeScale.add(atimeArray);
			
			atimeSeries = null;
			atimeArray = null;

		

	}
	

//	
//	public DoubleMatrix1D randomInitial(double upBoundary, double lowBoundary) {
//		int dimension = grn.getNodes().size();
//		Random random = new Random();
//		DoubleMatrix1D s = new DenseDoubleMatrix1D(dimension);
//		
//		for(int i=0;i<dimension;i++)
//			s.set(i, random.nextDouble() * (upBoundary - lowBoundary) + lowBoundary);
//		
////		for(int i=dimension;i<dimension*2;i++)
////			s[i] = 0.1;
//		
//		return s;
//	}

	



	public boolean isIslandscape() {
		return islandscape;
	}

	public void setIslandscape(boolean islandscape) {
		this.islandscape = islandscape;
	}

	
	public boolean isRandomInitial() {
		return isRandomInitial;
	}

	public void setRandomInitial(boolean isRandomInitial) {
		this.isRandomInitial = isRandomInitial;
	}
	
	public ArrayList<DoubleMatrix2D> getTimeSeries() {
		return timeSeries;
	}

	public void setTimeSeries(ArrayList<DoubleMatrix2D> timeSeries) {
		this.timeSeries = timeSeries;
	}

	public ArrayList<DoubleMatrix1D> getTimeScale() {
		return timeScale;
	}

	public void setTimeScale(ArrayList<DoubleMatrix1D> timeScale) {
		this.timeScale = timeScale;
	}

	public DoubleMatrix1D getInitialX0() {
		return initialX0;
	}

	public void setInitialX0(DoubleMatrix1D initialX0) {
		this.initialX0 = initialX0;
	}

}