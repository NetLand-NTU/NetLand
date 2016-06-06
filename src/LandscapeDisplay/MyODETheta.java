package LandscapeDisplay;

import static java.lang.Math.pow;

import java.util.ArrayList;

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.opensourcephysics.numerics.ODE;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.doublealgo.Transform;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import ch.epfl.lis.gnw.Gene;
import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.networks.Node;
import edu.umbc.cs.maple.utils.ColtUtils;



public class MyODETheta implements FirstOrderDifferentialEquations {
	
	private GeneNetwork grn_;
	private int dimension;

	private String name_;
	private DoubleMatrix2D A;
	// ============================================================================
	// PUBLIC METHODS

	/** Constructor, x0 is the current state of the network (initial conditions) * @param rateX */
	public MyODETheta(GeneNetwork grn, boolean islandscape, DoubleMatrix2D A) {	
		grn_ = grn;	
		int tempN = grn_.getNodes().size();
		dimension = (int) (Math.pow(tempN,2));	
		this.A = A;
	}


	// ----------------------------------------------------------------------------
	public void computeDerivatives(double t, double[] x0, double[] rate) {			
		//theta		
		DoubleMatrix2D B = new DenseDoubleMatrix2D(grn_.getNodes().size(), grn_.getNodes().size());
		for(int i=0;i<grn_.getNodes().size();i++)
			for(int j=0;j<grn_.getNodes().size();j++)
				B.set(i, j, x0[i*grn_.getNodes().size()+j]);
		
		DoubleMatrix2D C = ColtUtils.mult(B, A.viewDice());
		DoubleMatrix2D D = ColtUtils.mult(A, B);
		DoubleMatrix2D E = ColtUtils.plus(C, D);
//		Algebra alge = Algebra.ZERO;
//		theta = alge.mult(B, theta);	
		
//		JacobiMatrixSecondOrder drift = new JacobiMatrixSecondOrder(grn_, xTemp);
//		double[] dx = drift.getJac();
		//
		
		//theta = Transform.mult(theta, 0.05);
		//theta = Transform.plus(theta, new DenseDoubleMatrix1D(dx));
//		DoubleFactory2D factory = DoubleFactory2D.dense;
//		B = factory.diagonal(A);
		
		int count = 0;
		for(int i=0;i<grn_.getNodes().size();i++)
			for(int j=0;j<grn_.getNodes().size();j++){
				if( i==j )
					rate[count] = E.get(i, j)+0.05;
				else
					rate[count] = 0;
				count++;
			}
			
	}
	
	
	
	
		
	
	// ----------------------------------------------------------------------------
//	public boolean converged() {
//		 
//		for (int i=0; i<previousState_.length; i++) {
//			
//			double dxy = Math.abs(previousState_[i] - x0[i]); 
//			
//			if (dxy > absolutePrecision_ + relativePrecision_*Math.abs(x0[i])) {
//				// remember point
//				for (int k=0; k<previousState_.length; k++)
//					previousState_[k] = x0[k];
//				
//				return false;
//			}
//		}
//		return true;
//	}
	
	
	// ----------------------------------------------------------------------------
	
	public void setGrn(GeneNetwork grn) { grn_ = grn; }


	public String getName_() {
		return name_;
	}


	public void setName_(String name_) {
		this.name_ = name_;
	}


	public int getDimension() {
		return dimension;
	}


	public void setDimension(int dimension) {
		this.dimension = dimension;
	}


	
}

