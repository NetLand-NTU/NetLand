package LandscapeDisplay;

import org.apache.commons.math3.ode.*;
import org.apache.commons.math3.ode.nonstiff.DormandPrince54Integrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import ch.epfl.lis.gnw.GeneNetwork;
import org.apache.commons.math3.ode.FirstOrderIntegrator;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;



public class ODESolver {	
	private double t;
	private GeneNetwork grn;
	private boolean islandscape;
	private boolean isRandomInitial;
	private int dimension;
	private int timeCount;

	private double upBoundary;
	private double lowBoundary;

	private DoubleMatrix2D atimeSeries;
	private DoubleMatrix1D atimeArray;


	private ArrayList<double[]> tempAtimeSeries; //each step
	private ArrayList<Double> tempAtimeArray; //each step


	private FirstOrderIntegrator dp853;
	private FirstOrderDifferentialEquations ode;

	
	public ODESolver(GeneNetwork grn, int t){
		this.t = t;
		this.grn = grn;
		dp853 = new DormandPrince54Integrator(0.0001, 1000, 0.0001, 0.0001); //0.01
		this.dimension = grn.getNodes().size();


		ode = new MyODE(grn, false);	
		
	}

	public boolean solveEquations_ODE(){
		timeCount=0; 
		
		atimeSeries = null;
		atimeArray = null;
		
		tempAtimeSeries = new ArrayList<double[]>();
		tempAtimeArray = new ArrayList<Double>();	
		
		//set initials
		DoubleMatrix1D initialX0 = null;
		if( !isRandomInitial ){ //fix
			initialX0 = grn.getInitialState().copy();
		}else{ //random
			initialX0 = randomInitial(upBoundary, lowBoundary);
		}


		double[] x = new double[initialX0.size()];


		StepHandler stepHandler = new StepHandler() {
			double count = 0;
//			double[] ylast = new double[grn.getSize()];

			public void init(double t0, double[] y0, double t) {

			}


			public void handleStep(StepInterpolator interpolator, boolean isLast) {
				double   t = interpolator.getCurrentTime();
				double[] y = interpolator.getInterpolatedState();

				if( t-count>=1 ){
//					double dis = 0;
//					for(int i=0;i<ylast.length;i++)
//						dis += Math.abs(ylast[i] - y[i]);
//					
//					if( dis>0.00001 ){
						tempAtimeSeries.add(y.clone());
						tempAtimeArray.add(t);

						count = tempAtimeArray.get(tempAtimeArray.size()-1);
//						ylast = y.clone();
						
						if( timeCount%5000 == 0 )
							System.out.print(".");
						
						timeCount++;		
//					}
				}
			}
		};
		dp853.addStepHandler(stepHandler);


		try{
			dp853.integrate(ode, 0.0, initialX0.toArray(), t, x);
		}
		catch (OutOfMemoryError e)
		{
			JOptionPane.showMessageDialog(new Frame(), "There is not enough memory available to run this program.\n" +
					"Quit one or more programs, and then try again.\n" +
					"If enough amounts of RAM are installed on this computer, try to run the program \n" +
					"with the command-line argument -Xmx[XXXX]m to set the maximum memory of JVM. \n "
					, "Out of memory", JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		catch (IllegalArgumentException e)
		{
			JOptionPane.showMessageDialog(new Frame(), "Illegal argument", "Error", JOptionPane.INFORMATION_MESSAGE);
			MsgManager.Messages.errorMessage(e, "Error", "");
			return false;
		}


		//landscape
		if( this.islandscape  ){ //
			//check if converge
			int len = isConverged(tempAtimeSeries,tempAtimeArray);
			//converged
			if( len != -1 ){				
				//only save maximum 200, minimum 20 points per trajectory			
				if( len>200 ){
					int localStep = (int)(len-1)/200;
					int numPoints = (int)(len-1)/localStep+1;

					//System.out.print("a: "+localStep+"\t"+numPoints+"\t"+timeCount+"\n"); 
					atimeSeries = new DenseDoubleMatrix2D(numPoints, ode.getDimension());
					atimeArray = new DenseDoubleMatrix1D(numPoints);


					for(int i=0;i<len;i+=localStep){
						for(int j=0;j<dimension;j++){
							atimeSeries.set((int)(i/localStep), j, tempAtimeSeries.get(i)[j]);				
						}
						atimeArray.set((int)(i/localStep), tempAtimeArray.get(i));
					}
				}else{
					atimeSeries = new DenseDoubleMatrix2D(len, ode.getDimension());
					atimeArray = new DenseDoubleMatrix1D(len);

					for(int i=0;i<len;i++){
						for(int j=0;j<dimension;j++){
							atimeSeries.set(i, j, tempAtimeSeries.get(i)[j]);
						}
						atimeArray.set(i, tempAtimeArray.get(i));
					}
				}


			//not converged
			}else{
				return false;
			}			

		//simulation
		}else{
			atimeSeries = new DenseDoubleMatrix2D(timeCount, ode.getDimension());
			atimeArray = new DenseDoubleMatrix1D(timeCount);

			for(int i=0;i<timeCount;i++){
				for(int j=0;j<dimension;j++){
					atimeSeries.set(i, j, tempAtimeSeries.get(i)[j]);
				}
				atimeArray.set(i, tempAtimeArray.get(i));
			}

		}

		tempAtimeArray = null;
		tempAtimeSeries = null;

		return true;
	}


	private int isConverged(List<double[]> tempAtimeSeries, List<Double> tempAtimeArray) {
		//check if converge
		DoubleMatrix1D lasttime = new DenseDoubleMatrix1D(tempAtimeSeries.get(tempAtimeSeries.size()-1));
		DoubleMatrix1D secondlasttime = new DenseDoubleMatrix1D(tempAtimeSeries.get(tempAtimeSeries.size()-2));

		cern.jet.math.Functions F = cern.jet.math.Functions.functions;
		//		while( lasttime.aggregate(secondlasttime, F.plus, F.chain(F.square, F.minus))>0.001 ){			
		//			ts.setX0(lasttime);
		//			ts.run(lasttime.size());
		//
		//			time = combineTwo1DMatrix(time, ts.getTimeScale()).copy();
		//			timecourse = DoubleFactory2D.dense.appendRows(timecourse, ts.getTimeSeries());
		//
		//			lasttime = ts.getTimeSeries().viewRow(ts.getTimeSeries().rows()-1);
		//			secondlasttime = ts.getTimeSeries().viewRow(ts.getTimeSeries().rows()-2);
		//		}
		//if not converged to the threshold 0.000001,return -1 
		if( lasttime.aggregate(secondlasttime, F.plus, F.chain(F.square, F.minus))>0.01 ){			
			return -1; 
		}


		int len = 0;
		//get unique states			
		for(int j=0;j<tempAtimeSeries.size();j++){
			secondlasttime = secondlasttime.assign(tempAtimeSeries.get(j));
			if( lasttime.aggregate(secondlasttime, F.plus, F.chain(F.square, F.minus))<0.001 ){
				len = j;
				break;
			}
			len++;
		}

		len = len<20?len:20;

		//
		tempAtimeSeries = tempAtimeSeries.subList(0, len);
		tempAtimeArray = tempAtimeArray.subList(0, len);


		return len;

	}

	public DoubleMatrix1D randomInitial(double upBoundary, double lowBoundary) {
		Random random = new Random();
		DoubleMatrix1D s = new DenseDoubleMatrix1D(dimension);

		for(int i=0;i<dimension;i++)
			s.set(i, random.nextDouble() * (upBoundary - lowBoundary) + lowBoundary);

		//		for(int i=dimension;i<dimension*2;i++)
		//			s[i] = 0.1;

		return s;
	}


	public boolean isIslandscape() {
		return islandscape;
	}

	public void setIslandscape(boolean islandscape) {
		this.islandscape = islandscape;
	}

	public void setUpBoundary(double upbound) {
		this.upBoundary = upbound;		
	}

	public void setLowBoundary(double lowbound) {
		this.lowBoundary = lowbound;

	}

	public boolean isRandomInitial() {
		return isRandomInitial;
	}

	public void setRandomInitial(boolean isRandomInitial) {
		this.isRandomInitial = isRandomInitial;
	}


	public DoubleMatrix2D getTimeSeries() {
		return atimeSeries;
	}

	public void setTimeSeries(DoubleMatrix2D timeSeries) {
		this.atimeSeries = timeSeries;
	}

	public DoubleMatrix1D getTimeScale() {
		return atimeArray;
	}

	public void setTimeScale(DoubleMatrix1D timeScale) {
		this.atimeArray = timeScale;
	}

	public double getT() {
		return t;
	}

	public void setT(double t) {
		this.t = t;
	}
}