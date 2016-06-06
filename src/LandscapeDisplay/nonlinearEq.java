package LandscapeDisplay;

import java.util.ArrayList;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import ch.epfl.lis.gnw.Gene;
import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.networks.Node;
import optimization.Lmdif_fcn;



public class nonlinearEq extends Object implements Lmdif_fcn{
	public static int nfev = 0;
	public static int njev = 0;
	public GeneNetwork grn;
	
	public nonlinearEq(GeneNetwork grn){
		this.grn = grn;
	}
	
	public DoubleMatrix1D runSolver(DoubleMatrix1D x0,GeneNetwork grn){
		int info[] = new int[2];

		int m = x0.size();//system of two equations
		int n = x0.size();// with two unknowns

		int iflag[] = new int[2];
		double fvec[] = new double[m+1];//vector of residuals
		double x[] = new double[n+1];

		//initialisation des inconnues / initialization of unknowns
		for(int j=0;j<n;j++){
			x[j+1] = x0.get(j);
			fvec[j+1] = 0;
		}

		iflag[1] = 0;

		
		nonlinearEq a = new nonlinearEq(grn);
		a.fcn(m,n,x,fvec,iflag);
		double residu0 = optimization.Minpack_f77.enorm_f77(m,fvec);
		
		
		nfev = 0;
		njev = 0;

		double epsi=0.0005;//overall accuracy required on the convergence
		double epsfcn = 1.e-6;// precision calculus of finite differences
		
		optimization.Minpack_f77.lmdif1_f77(a, m, n, x, fvec, epsi, info);
		

		// final L2 norm
		double residu1 = optimization.Minpack_f77.enorm_f77(m,fvec);

		if( info[1] >= 5 )
			return null;
		
		
		//output 
		DoubleMatrix1D y = new DenseDoubleMatrix1D(n); 
		for(int i=1;i<=n;i++){//display solution
			y.set(i-1, x[i]);
		}
		return y;
	}

	
	public void fcn(int m, int n, double x[], double fvec[], int iflag[]) {
		if (iflag[1]==1) nfev++;
		if (iflag[1]==2) njev++;

//		fvec[1] =-x[1]+(1.0/16)/(Math.pow(x[2],4)+1.0/16)+Math.pow(x[1],4)/(Math.pow(x[1],4)+1.0/16); //x[1]*x[1]+x[2]*x[2]-1;
//		fvec[2] =-x[2]+(1.0/16)/(Math.pow(x[1],4)+1.0/16)+Math.pow(x[2],4)/(Math.pow(x[2],4)+1.0/16); //3*x[1]+2*x[2]-1;
		
		ArrayList<String> origGeneNames = new ArrayList<String>();
		for(int i=0;i<n;i++)
			origGeneNames.add(grn.getNode(i).getLabel());

		ArrayList<String> parameterNames = grn.getParameterNames_();
		ArrayList<Double> parameterValues = grn.getParameterValues_();		
		ArrayList<Gene> species = grn.getSpecies();
		ArrayList<Node> nodes = grn.getNodes();

		MathEval math=new MathEval();	
		for(int j=0;j<parameterNames.size();j++)
			math.setVariable(parameterNames.get(j),  parameterValues.get(j));

		for(int j=0;j<species.size();j++)
			math.setVariable(species.get(j).getLabel(),  grn.getSpecies_initialState().get(j));

		for(int j=0;j<nodes.size();j++)
			math.setVariable(nodes.get(j).getLabel(),  x[j+1]);

		//construct SDEs
		//SDEs
		for (int i = 0; i < n; i++) {
			Gene gene = (Gene) grn.getNode(i);

			String combination = gene.getCombination();
			combination = combination.replace(" ", "");		

			Double result = math.evaluate(combination);
				
			fvec[i+1] = result;
		}

	}

	
}
