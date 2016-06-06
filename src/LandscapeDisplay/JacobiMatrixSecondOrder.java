package LandscapeDisplay;


import java.util.ArrayList;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import ch.epfl.lis.gnw.Gene;
import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.networks.Node;
import numal.*;



public class JacobiMatrixSecondOrder{
	private GeneNetwork grn_;
	private double jac[][];
	private int dimension;
	private MathEval bak_math;

	public class jacobi extends Object implements AE_jacobnnf_methods{

		public double di(int i, int n) {
			return 1; //((i == 1) ? 1.0e-5 : 1.0);
		}

		public void funct(int n, double x[], double f[]) {				
			JacobiMatrix jacobi = new JacobiMatrix(grn_, new DenseDoubleMatrix1D(x));
			double[] a = jacobi.getJac();
			
			for(int ii=1;ii<=n;ii++){	
				f[ii] = a[ii-1];
			}
		}

	}
	
	
	public JacobiMatrixSecondOrder(GeneNetwork grn, DoubleMatrix1D Xt) {
		this.grn_ = grn;
		
		dimension = grn_.getNodes().size();

		double x[] = new double[dimension+1];
		double f[] = new double[dimension+1];
		jac = new double[dimension+1][dimension+1]; 

		jacobi testjacobnnf = new jacobi();

		x[0] = 0;
		for(int i=1;i<=dimension;i++){
			x[i] = Xt.get(i-1);
		}
		
		
		testjacobnnf.funct(dimension,x,f);	
		Analytic_eval.jacobnnf(dimension,x,f,jac,testjacobnnf);
	
	}
		
	
	
	public double[] getJac(){
		double[] realJac = new double[dimension];
		for(int i=0;i<dimension;i++)
			realJac[i] = 2*jac[i+1][i+1];
		return realJac;
	}
	
	public double[][] getJacMatrix(){
		double[][] realJac = new double[dimension][dimension];
		for(int i=0;i<dimension;i++)
			for(int j=0;j<dimension;j++)
				realJac[i][j] = jac[i+1][j+1];
		return realJac;
	}
	
	public int getDimension(){return dimension;}

}