package LandscapeDisplay;

import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JOptionPane;



import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import LandscapeDisplay.MySDE;
import ch.epfl.lis.sde.SdeSettings;
import ch.epfl.lis.sde.solvers.SdeSolver;
import ch.epfl.lis.sde.solvers.SdeSolverFactory;


public class SDETimeSeriesExperiment {
	/** Time series data. */
	private DoubleMatrix2D timeSeries_;
	/** Time scale.*/
	private DoubleMatrix1D timeScale_;
	/** The duration of the experiment. */
	private double maxt_;
	/** Time interval between two time points (maxt/(numTimePoints-1)). */
	private double dt_;
	/** Number of time points. */
	private int numTimePoints_;
	
	/** Method used to integrate SDEs. */
	private SdeSolver solver_;
	
	private MySDE system ;
	private DoubleMatrix1D X0;
	private boolean converged;

	// ============================================================================
	// PUBLIC METHODS
	
	// ----------------------------------------------------------------------------
	
	/** Default constructor. */
	public SDETimeSeriesExperiment() {
		maxt_ = 0.;
		dt_ = 0.;
		numTimePoints_ = 0;
		timeSeries_ = null;
		timeScale_ = null;
		solver_ = null;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Instantiates the different variables _before_ simulate the experiment. */
	public void init() {
		
		if (numTimePoints_ <= 1)
			throw new IllegalArgumentException("The number of time points must be greater than 1.");
		
		// allocation
		int n = solver_.getSystem().getDimension(); // dimension of the system
		timeSeries_ = new DenseDoubleMatrix2D(numTimePoints_, n);
		timeScale_ = new DenseDoubleMatrix1D(numTimePoints_);
		
		// initialize solver
		solver_.initialize();
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * Run the experiment.
	 * @param N Number of stochastic differential equations to solve. 
	 */
	public void run(int N) {

		// set the parameters first
//		SdeSettings settings = SdeSettings.getInstance();
//		settings.setSeed(-1); // random seed
//		settings.setMaxt(maxt_);
//		settings.setDt(0.5); //0.001
		dt_ = 0.5;
		
		// set solver
		solver_ = SdeSolverFactory.createSolver(SdeSolverFactory.EULER_MARUYAMA);
		if (solver_ == null)
			throw new RuntimeException("Unable to instantiate the solver");
		
		solver_.setX(X0); // set initial condition X0=[1,1,1,...]^T
		
		// associate the system to the solver
		solver_.setSystem(system);
		//Log.info("TimeSeriesExperiment", solver_.getDescription() + "\n");
		

		// set the time series experiment
		init(); // number of time points wished
		
		integrate();		
		
		converged = solver_.converged();
	}
		
	// ----------------------------------------------------------------------------
	
	/** Numerical integration of the system of N SDEs.*/
	public void integrate() {
		
//		SdeSettings settings = SdeSettings.getInstance();
		
		int n = solver_.getSystem().getDimension();

		double t = 0;
//		maxt_ = settings.getMaxt();
		
		if (maxt_ <= 0)
			throw new IllegalArgumentException("Duration (maxt) must be greater than 0.");
		
		dt_ = maxt_/(double)(numTimePoints_-1);
		
		if (dt_ <= 0 || dt_ > maxt_)
			throw new IllegalArgumentException("Interval between two measured time points must be greater than 0 and and smaller than maxt.");
		
		double frac = dt_/dt_;
		if (frac - (int)frac != 0) // check whether the decimal part of frac is 0 or not
			throw new IllegalArgumentException("Interval between two measured time points (maxt/(numTimePoints-1)) must be a multiple of the integration step-size.");
		
	
		solver_.setH(dt_);
		
		DoubleMatrix1D X = solver_.getX();
		
		
		if (X == null)
			throw new RuntimeException("TimeSeriesExperiment.integrate(): X0 null");
		
		// Set first line of the time series dataset (at t=0)
		for (int i=0; i<n; i++)
			timeSeries_.set(0, i, X.get(i));
		
		int pt = 1;
		
		do {
			double t1 = t;
			try {

				// this steps the time by TimeSeriesExperiment.dt_, the solver integrates with a smaller, fixed step size
				// defined in SdeSettings by dt_*multiplier_ (SdeSettings.dt_ != TimeSeriesExperiment.dt_)
				// WARNING: TimeSeriesExperiment.dt_ must be a multiple of SdeSettings.dt_
				t += solver_.step();

			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "at t = " + t + "\n" + e.getMessage()+"\nPlease reset the parameters.", "Error", JOptionPane.INFORMATION_MESSAGE);		
				throw new RuntimeException("TimeSeriesExperiment.integrate(): Exception at t = " + t + ": " + e.getMessage());
			}
			
			if (t != t1 + dt_){
				JOptionPane.showMessageDialog(null, "Solver failed to step time by dt, expected t = " + (t1+dt_) + ", obtained t = " + t, "Error", JOptionPane.INFORMATION_MESSAGE);		
				throw new RuntimeException("TimeSeriesExperiment.integrate(): Solver failed to step time by dt, expected t = " + (t1+dt_) + ", obtained t = " + t);
			}

			// save the result
			X = solver_.getX();
			for (int i = 0; i < n; i++){
				//if( X.get(i)<0 ) X.set(i, 0);
				timeSeries_.set(pt, i,X.get(i));
			}
			timeScale_.set(pt, t);
			
			pt++;
			
		} while (t < maxt_ - 0.0001);
		
		assert t == maxt_ : "t=" + t + " maxt=" + maxt_;
		assert pt == numTimePoints_;
	}
	
	// ----------------------------------------------------------------------------

	/** Wrapper function to print trajectories to files. */
	public void printAll(FileWriter fw) {
		
		try {
			printTrajectories(fw, timeSeries_, timeScale_);
		} catch (Exception e) {
			//Log.error("TimeSeriesExperiment", "Failed to write time series to file.", e);
			JOptionPane.showMessageDialog(null, "Failed to write time series to file", "Error", JOptionPane.INFORMATION_MESSAGE);
			MsgManager.Messages.errorMessage(e, "Error", "");
		}
	}
	
	// ----------------------------------------------------------------------------

	/** Writes time series data with the time scale to file. */
	public void printTrajectories(FileWriter fw, DoubleMatrix2D timeSeries, DoubleMatrix1D timeScale) {
			int R = timeSeries.rows();
			int C = timeSeries.columns();
			
			for (int i = 0; i < R; i++) {
				// first column of the file is time scale
				try {
					fw.write(Double.toString(timeScale.get(i)));
					
					for (int j = 0; j < C; j++)
						fw.write("\t" + timeSeries.get(i, j));			
					fw.write("\n");
					
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Error in writing the file!", "Error", JOptionPane.INFORMATION_MESSAGE);
					MsgManager.Messages.errorMessage(e, "Error", "");
				}			
				
			}
	}
	
	// ============================================================================
	// GETTERS AND SETTERS

	public void setNumTimePoints(int numTimePoints) { numTimePoints_ = numTimePoints; }
	public int getNumTimePoints() { return numTimePoints_; }
	
	public DoubleMatrix2D getTimeSeries() { return timeSeries_; }
	public DoubleMatrix1D getTimeScale() { return timeScale_; }
	
	public void setTimeSeries(DoubleMatrix2D timeSeries_) { this.timeSeries_ = timeSeries_; }
	public void setTimeScale(DoubleMatrix1D timeScale_) { this.timeScale_ = timeScale_; }
	
	public void setSolver(SdeSolver solver) { solver_ = solver; }
	public SdeSolver getSolver() { return solver_; }
	
	public void setMaxt(double maxt) { maxt_ = maxt; }
	public double getMaxt() { return maxt_; }
	
	public void setDt(double dt) { dt_ = dt; }
	public double getDt() { return dt_; }
	
	public void setSystem(MySDE system) { this.system = system; }
	public MySDE getSystem() { return system; }	
	
	public void setX0(DoubleMatrix1D X0) { this.X0 = X0; }
	public DoubleMatrix1D getX0() { return X0; }

	public boolean isConverged() {
		return converged;
	}

	public void setConverged(boolean converged) {
		this.converged = converged;
	}
}
