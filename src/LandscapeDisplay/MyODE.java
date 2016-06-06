package LandscapeDisplay;

import static java.lang.Math.pow;

import java.util.ArrayList;

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.opensourcephysics.numerics.ODE;

import ch.epfl.lis.gnw.Gene;
import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.networks.Node;



public class MyODE implements FirstOrderDifferentialEquations {
	
	private GeneNetwork grn_;
	private int dimension;

	private String name_;
	// ============================================================================
	// PUBLIC METHODS

	/** Constructor, x0 is the current state of the network (initial conditions) */
	public MyODE(GeneNetwork grn, boolean islandscape) {	
		grn_ = grn;
		
		if(islandscape)
			dimension = grn_.getNodes().size()*2;
		else
			dimension = grn_.getNodes().size();
		
	}


	// ----------------------------------------------------------------------------
	public void computeDerivatives(double t, double[] x0, double[] rate) {
		//origGenes list
		int dimension = grn_.getNodes().size();
		//				if(islandscape) 
		//					dimension = dimension_/2;
		ArrayList<String> origGeneNames = new ArrayList<String>();
		for(int i=0;i<dimension;i++)
			origGeneNames.add(grn_.getNode(i).getLabel());

		ArrayList<String> parameterNames = grn_.getParameterNames_();
		ArrayList<Double> parameterValues = grn_.getParameterValues_();		
		ArrayList<Gene> species = grn_.getSpecies();
		ArrayList<Node> nodes = grn_.getNodes();

		MathEval math=new MathEval();	
		for(int j=0;j<parameterNames.size();j++)
			math.setVariable(parameterNames.get(j),  parameterValues.get(j));

		for(int j=0;j<species.size();j++)
			math.setVariable(species.get(j).getLabel(),  grn_.getSpecies_initialState().get(j));

		for(int j=0;j<nodes.size();j++){
			if( x0[j]<0 ) x0[j]=0;
			math.setVariable(nodes.get(j).getLabel(),  x0[j]);
		}

		//construct ODEs
		for (int i = 0; i < dimension; i++) {
			Gene gene = (Gene) grn_.getNode(i);

			String combination = gene.getCombination();
			combination = combination.replace(" ", "");		

			Double result = math.evaluate(combination);

			rate[i] = result;
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

