package LandscapeDisplay;

import java.util.ArrayList;
import java.util.Comparator;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import ch.epfl.lis.gnw.Gene;
import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.networks.Node;
import ch.epfl.lis.sde.Sde;

/** 
 * for landscape, mean and dev 
 **/

public class MySDE extends Sde {
	
	private double sigma_;
	private GeneNetwork grn_;
	private String name_;
	
	private boolean islandscape = false;
	// ============================================================================
	// PUBLIC METHODS
	
	public MySDE(GeneNetwork grn, boolean islandscape) {
		this.islandscape = islandscape;
		this.grn_ = grn;
		
//		if(islandscape)
//			dimension_ = grn_.getNodes().size()*2;
//		else
			dimension_ = grn_.getNodes().size();
	}

	// ----------------------------------------------------------------------------

	/**
	 * <p>Computes the drift coefficients F and diffusion coefficient G at a given time.
	 * Note that Ito and Stratonovich drift terms are equivalent because the diffusion
	 * term G is constant. See class description.</p>
	 */
	@Override
	public void getDriftAndDiffusion(final double t, final DoubleMatrix1D Xin, DoubleMatrix1D F, DoubleMatrix2D G) throws Exception {
		//origGenes list
		int dimension = dimension_;
//		if(islandscape) 
//			dimension = dimension_/2;
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
			if( Xin.get(j)<0 ) Xin.set(j, 0);
			math.setVariable(nodes.get(j).getLabel(),  Xin.get(j));
		}
		
		//construct SDEs
		//SDEs
		for (int i = 0; i < dimension; i++) {
			Gene gene = (Gene) grn_.getNode(i);
			
			String combination = gene.getCombination();
			combination = combination.replace(" ", "");		
			
	    	Double result = math.evaluate(combination);
	    
			F.set(i, result);
			G.set(i, i, sigma_);
		}
		//end of SDEs
		
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public void setSigma(double sigma) { sigma_ = sigma; }
	public double getSigma() { return sigma_; }
	
	

	public String getName() {
		return name_;
	}

	public void setName(String name) {
		this.name_ = name;
	}



	class SortByID implements Comparator<Object> {
		public int compare(Object o1, Object o2) {
			if (Integer.parseInt(((String) o1).substring(1)) > Integer.parseInt(((String) o2).substring(1)))
				return 1;
			return -1;
		}
	}
}
