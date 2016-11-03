package LandscapeDisplay;

import java.util.ArrayList;
//import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import ch.epfl.lis.gnw.Gene;
import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.networks.Node;



public class MyODE implements FirstOrderDifferentialEquations {
	
	private GeneNetwork grn_;
	private int dimension;

	private String name_;
	
	private MathEval math;
	private String[] combinations;
	// ============================================================================
	// PUBLIC METHODS

	/** Constructor, x0 is the current state of the network (initial conditions) */
	public MyODE(GeneNetwork grn, boolean islandscape) {	
		grn_ = grn;
		
		
		dimension = grn_.getNodes().size();
		
		
		ArrayList<String> parameterNames = grn_.getParameterNames_();
		ArrayList<Double> parameterValues = grn_.getParameterValues_();	
		
//		DecimalFormat decimalFormat = new DecimalFormat("#,##0.0000000");
//		ArrayList<String> parameterValuesString = new ArrayList<String>();	
//		for(int j=0;j<parameterValues.size();j++)
//			parameterValuesString.add(decimalFormat.format(parameterValues.get(j)));
		
		
		
		math=new MathEval();	
		
//		System.out.print("MyODE Prior Start: "+System.currentTimeMillis()); 
		combinations = new String[dimension];
		for (int i = 0; i < dimension; i++) {
			Gene gene = (Gene) grn_.getNode(i);

			String combination = gene.getCombination();
			combination = combination.replace(" ", "");		
			
//			ArrayList<String> variables = math.MygetVariablesWithin(combination);
//			for(int j=0;j<variables.size();j++){
//				int index = parameterNames.indexOf(variables.get(j));
//				if( index>=0 ){
//					combination = combination.replaceFirst(variables.get(j), parameterValuesString.get(index));
//				}
//			}
			
			
			combinations[i] = combination;
		}
		
//		System.out.print("MyODE Prior End: "+System.currentTimeMillis()); 
		
//		ArrayList<String> origGeneNames = new ArrayList<String>();
//		for(int i=0;i<dimension;i++)
//			origGeneNames.add(grn_.getNode(i).getLabel());

		
		
		ArrayList<Gene> species = grn_.getSpecies();
		for(int j=0;j<species.size();j++)
			math.setVariable(species.get(j).getLabel(),  grn_.getSpecies_initialState().get(j));
		
		for(int j=0;j<parameterNames.size();j++)
			math.setVariable(parameterNames.get(j),  parameterValues.get(j));

//		System.out.print("\nParaNames: "+parameterNames+"\n");
//		System.out.print("\nParaValues: "+parameterValues+"\n");
	}


	// ----------------------------------------------------------------------------
	public void computeDerivatives(double t, double[] x0, double[] rate) {
		//origGenes list
		//int dimension = grn_.getNodes().size();
		//				if(islandscape) 
		//					dimension = dimension_/2;
		ArrayList<Node> nodes = grn_.getNodes();
		for(int j=0;j<nodes.size();j++){
			//if( x0[j]<0 ) x0[j]=0;
			math.setVariable(nodes.get(j).getLabel(),  x0[j]);
		}
		
//		System.out.print("MyODE Start: "+System.currentTimeMillis()); 
		//construct ODEs
		for (int i = 0; i < dimension; i++) {
//			Gene gene = (Gene) grn_.getNode(i);

//			String combination = gene.getCombination();
//			combination = combination.replace(" ", "");
			String combination = combinations[i];

			Double result = math.evaluate(combination);

			rate[i] = result;
		}
//		System.out.print("Rate: "+rate.length+"\n"); //hahah
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

