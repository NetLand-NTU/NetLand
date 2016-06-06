package LandscapeDisplay;


import java.util.ArrayList;

import cern.colt.matrix.DoubleMatrix1D;
import ch.epfl.lis.gnw.Gene;
import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.networks.Node;
import numal.*;



public class JacobiMatrix{
	private GeneNetwork grn_;
	private double jac[][];
	private int dimension;
	private MathEval bak_math;

	public class jacobi extends Object implements AE_jacobnnf_methods{

		public double di(int i, int n) {
			return ((i == 1) ? 0.001 : 0.01);
		}

		public void funct(int n, double x[], double f[]) {	
			ArrayList<String> parameterNames = grn_.getParameterNames_();
			ArrayList<Double> parameterValues = grn_.getParameterValues_();		
			ArrayList<Gene> species = grn_.getSpecies();
			ArrayList<Node> nodes = grn_.getNodes();
			
			MathEval math=new MathEval();	
			for(int j=0;j<parameterNames.size();j++)
				math.setVariable(parameterNames.get(j),  parameterValues.get(j));
			
			for(int j=0;j<species.size();j++)
				math.setVariable(species.get(j).getLabel(),  grn_.getSpecies_initialState().get(j));
			
			for(int j=0;j<nodes.size();j++)
				math.setVariable(nodes.get(j).getLabel(),  x[j+1]);
			
			for(int ii=1;ii<=n;ii++){
				Gene gene = (Gene) grn_.getNode(ii-1);
				String combination = gene.getCombination();
				combination = combination.replace(" ", "");		
				
				Double result = math.evaluate(combination);
				
				f[ii] = result;
			}
		}

	}
	
	
	public JacobiMatrix(GeneNetwork grn, DoubleMatrix1D Xt) {
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
			realJac[i] = jac[i+1][i+1];
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