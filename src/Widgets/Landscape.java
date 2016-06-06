package Widgets;

import java.awt.Component;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.text.parser.ParseException;

import FileManager.FileChooser;
import LandscapeAnimation.LandscapePanel;
import LandscapeDisplay.JacobiMatrix;
import LandscapeDisplay.ODESolverTheta;
import LandscapeDisplay.SDESolver;
import LandscapeDisplay.SDETimeSeriesExperiment;
import LandscapeDisplay.nonlinearEq;
import WidgetsMenu.MainMenu;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.doublealgo.Statistic;
import cern.colt.matrix.doublealgo.Transform;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import ch.epfl.lis.gnw.CancelException;
import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.gnw.GnwSettings;
import ch.epfl.lis.gnwgui.DynamicalModelElement;
import ch.epfl.lis.gnwgui.NetworkElement;
import ch.epfl.lis.networks.Node;
import edu.umbc.cs.maple.utils.ColtUtils;

public class Landscape extends LandscapeWindow {
	private NetworkElement item_;
	private LandscapeThread landscape = null;
	
	private boolean ide = false;
	
	public Landscape(Frame aFrame, NetworkElement item) {
		super(aFrame);		
		item_ = item;
		
		//closing listener
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent windowEvent){
				if( landscape != null && landscape.myThread_.isAlive() ){
					landscape.stop();	
					System.out.print("Simulation is canceled.");
					//JOptionPane.showMessageDialog(new Frame(), "Simulation is canceled.", "Warning!", JOptionPane.INFORMATION_MESSAGE);
				}
				escapeAction();
			}
		});
		
		
		//set core genenames
		final GeneNetwork grn = ((DynamicalModelElement) item_).getGeneNetwork();
		ArrayList<Node> nodes = grn.getNodes();
	

		String geneNames = "";
		for(int i=0;i<nodes.size();i++)
			geneNames += nodes.get(i).getLabel()+";";
		
		focusGenes.setText(geneNames);
		
		gpdmIts.setEnabled(false);
		
		
		//set plot part
		//display saved result
		if( grn.getGridData()!=null ){		
			//update parameters
			its.setText(grn.getLand_itsValue()+"");
			maxExp.setText(grn.getLand_maxExpValue()+"");
			maxT.setText(grn.getLand_maxTime()+"");
			
			geneNames = "";
			for(int i=0;i<grn.getLand_focusGenesList().length;i++)
				geneNames += grn.getLand_focusGenesList()[i]+";";
			
			focusGenes.setText(geneNames);

			if( grn.isLand_isTwoGenes() ){
				randioButton11.setSelected(true);
				randioButton31.setSelected(false);
			}else{
				randioButton11.setSelected(false);
				randioButton31.setSelected(true);
			}
			
			this.gpdmIts.setText(grn.getLand_gpdmitsValue()+"");
			
		}
		
		
		//radios
		generateTimeCourse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(generateTimeCourse.isSelected()){
					maxExp.setEnabled(true);
					its.setEnabled(true);
					maxT.setEnabled(true);
				}
			}
		});
		
		loadTimeCourse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(loadTimeCourse.isSelected()){
					maxExp.setEnabled(false);
					its.setEnabled(false);
					maxT.setEnabled(false);
					
					if( grn.getLandTimeSeries().isEmpty() ){
						JOptionPane.showMessageDialog(null, "Please run simulation first", "Error", JOptionPane.INFORMATION_MESSAGE);
						generateTimeCourse.setSelected(true);
						loadTimeCourse.setSelected(false);
						
						maxExp.setEnabled(true);
						its.setEnabled(true);
						maxT.setEnabled(true);
					}else{
						JOptionPane.showMessageDialog(null, "Pre-computed time course data is loaded.", "Information", JOptionPane.INFORMATION_MESSAGE);
					}
					
				}
			}
		});
		
		//button functions
		browse_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				try {
		            JFileChooser c = new FileChooser();
		            int result = c.showSaveDialog(new JFrame());
		            if(result == JFileChooser.APPROVE_OPTION){
		                c.approveSelection();
		                userPath_.setText(c.getSelectedFile().getAbsolutePath());
		            }
		        } catch (HeadlessException e) {
		            MsgManager.Messages.errorMessage(e, "Error", "Cannot create a new file!");
		        }	
			}
		});
		
		clear_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				userPath_.setText("");
			}
		});
		
		
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {	
				String geneNames = "";
				for(int i=0;i<grn.getNodes().size();i++)
					geneNames += grn.getNodes().get(i).getLabel()+";";				
				focusGenes.setText(geneNames);
			}
		});
		
		runButton_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {	
				String text = focusGenes.getText();
				text = text.replaceAll(" ","");
				String[] lines = text.split(";");			
				String[] focusGenes = new String[lines.length];
				for(int i=0;i<lines.length;i++){
					focusGenes[i] = lines[i];
				}
								
				enterAction(item_);
			}
		});

		cancelButton_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				if( landscape != null ){
					snake_.stop();
					myCardLayout_.show(runButtonAndSnakePanel_, runPanel_.getName());
					landscape.stop();
					System.out.print("Generation canceled!\n");
				}
			}
		});
		
//		randioButton3.addActionListener(new ActionListener() {
//			public void actionPerformed(final ActionEvent arg0) {
//				randioButton11.setEnabled(false);
//				randioButton31.setSelected(true);
//				randioButton11.setSelected(false);
//			}
//		});
		
		randioButton1.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				randioButton11.setEnabled(true);
				randioButton31.setSelected(false);
				randioButton11.setSelected(true);
			}
		});
		
		randioButton11.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				gpdmIts.setEnabled(false);
			}
		});
		
		randioButton31.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				gpdmIts.setEnabled(true);
				//focusGenes.setEnabled(false);
			}
		});
	}
	
	
	public void enterAction(NetworkElement item) {
		try {
			
			//converted to dynamic model
			DynamicalModelElement grnItem = new DynamicalModelElement((DynamicalModelElement) item);	
			
			if( grnItem.getGeneNetwork().getNodes().size() <= 1 ){
				JOptionPane.showMessageDialog(null, "The system only contains "+grnItem.getGeneNetwork().getNodes().size()+" reaction! To plot the landscape, at least two kinetic reactions are required.", "Error", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			GnwSettings settings = GnwSettings.getInstance(); 				
			
			landscape = new LandscapeThread(grnItem);
			settings.stopBenchmarkGeneration(false); // reset
			
			// be sure to have set the output directory before running the simulation
			landscape.start();
		}
		catch (Exception e){
			JOptionPane.showMessageDialog(null, "Error in running Landscape!", "Error", JOptionPane.INFORMATION_MESSAGE);
			MsgManager.Messages.errorMessage(e, "Error", "");
		}

	}
	
	
	
	public class LandscapeThread implements Runnable {
		private GeneNetwork grn_;
		/** Main Thread */
		private Thread myThread_;
		private volatile boolean stopRequested;

		private DoubleMatrix2D sumPara;
		private int[] counts;
		//size is num attractors with indexes 
		private	ArrayList<int[]> labeledSeries;
		
		private double maxExpValue;
		private int itsValue;
		private int maxTime;
		private int numTimePoints;
		private String[] focusGenesList;
		private String outputPath;
		private String landscapeMethod;
		private boolean displayMethod = true; //default two markers
		private int gpdmItsValue;

//		Logger log_ = Logger.getLogger(TopButton.class.getName());
		
		// ============================================================================
		// PUBLIC METHODS
		public LandscapeThread(DynamicalModelElement grnItem)
		{
			super();
			myThread_ = null;
			stopRequested = false;
			
			grn_ = grnItem.getGeneNetwork();
			maxExpValue = Double.parseDouble(maxExp.getText());
			itsValue = Integer.parseInt(its.getText());
			maxTime = Integer.parseInt(maxT.getText());
			gpdmItsValue = Integer.parseInt(gpdmIts.getText());

			numTimePoints = (int) (maxTime*2+1);
			focusGenesList = focusGenes.getText().split(";");
			outputPath = userPath_.getText();
			if( randioButton1.isSelected() )
				landscapeMethod = "probabilistic";
			else
				landscapeMethod = "entropy";

			if( randioButton11.isSelected() )//select two markers
				displayMethod = true;
			else //gpdm
				displayMethod = false;		

			
		}

		// ----------------------------------------------------------------------------

		public void start() {
			// If myThread_ is null, we start it!
			if (myThread_ == null) {
				myThread_ = new Thread(this);
				stopRequested = false;
				myThread_.start();
			}
		}

		// ----------------------------------------------------------------------------

		public void stop(){
			stopRequested = true;

			if( myThread_ != null ){
				myThread_.interrupt();
				JOptionPane.showMessageDialog(new Frame(), "The program is interrupted!", "Cancel", JOptionPane.INFORMATION_MESSAGE);
			}
		}

		// ----------------------------------------------------------------------------

		public void run()
		{	
			snake_.start();
			myCardLayout_.show(runButtonAndSnakePanel_, snakePanel_.getName());
			
			Component c = snake_.getParent();
			while ( c.getParent() != null ){
				c = c.getParent();
			}

			if ( c instanceof JFrame )
			{
				try{		
					if( randioButton11.isSelected() )//select two markers
						System.out.print("Start to construct Landscape\n");
					else //gpdm
						System.out.print("Start to construct Landscape using GPDM\n");
					
					//construct landscape
					//DrawLandscape instance = new DrawLandscape((JFrame) c, grn_, maxExpValue, itsValue, maxTime, numTimePoints, focusGenesList, outputPath, landscapeMethod, displayMethod, gpdmItsValue);
					/** generate time series **/
					//run numSeries_ times
					ArrayList<SDETimeSeriesExperiment> tss = new ArrayList<SDETimeSeriesExperiment>();
					ArrayList<DoubleMatrix1D> timeScale = new ArrayList<DoubleMatrix1D>();	
					ArrayList<DoubleMatrix2D> timeSeries = new ArrayList<DoubleMatrix2D>();
					
					boolean isStable = true;
					if( generateTimeCourse.isSelected() ){
						SDESolver deSolver_ = new SDESolver(grn_, true, 0);

						//random start values
						deSolver_.setOutputDirectory(System.getProperty("user.dir"));
						deSolver_.setNumSeries(itsValue); //foreach X0 runs once
						deSolver_.setMaxt(maxTime);
						deSolver_.setNumTimePoints(numTimePoints);
						deSolver_.setNoiseStrength(0);
						deSolver_.setIslandscape(true);

						double dt_ = maxTime*1.0 / (numTimePoints - 1) ;
						deSolver_.setDt(dt_);


						deSolver_.setTss(tss);
						deSolver_.setTimeScale(timeScale);
						deSolver_.setTimeSeries(timeSeries);

						int its = 0;
						while( !stopRequested && its<itsValue ){
							System.out.print("Trajectory no: "+its+"\n");
							try {
								deSolver_.solveEquations_landscape(maxExpValue, displayMethod);
								
								if( deSolver_.getTimeScale() == null ){
									isStable = false;
									break;
								}
							} catch (Exception e) {			
								JOptionPane.showMessageDialog(new Frame(), "Error in execution!", "Runtime exception", JOptionPane.INFORMATION_MESSAGE);
								MsgManager.Messages.errorMessage(e, "Runtime exception", "");
								this.stop();
							}
							its++;
						}
					}else if( loadTimeCourse.isSelected() )
						timeSeries = grn_.getLandTimeSeries();
						
					
					
					grn_.setDisplayMethod(landscapeMethod);
					
					
					
					/** get attractors focusGenes=all **/
					if( !stopRequested && isStable ){	
						System.out.print("Calculate attractors...\n");
						if( !checkAttractor(focusGenesList, timeSeries) ){
							isStable = false;
						}
					}
								
					
					/** calculate theta **/		
					int dimension = grn_.getNodes().size();	
					ArrayList<DoubleMatrix1D> theta = new ArrayList<DoubleMatrix1D>(dimension);						
					
					if( !stopRequested && isStable ){
						for(int i=0;i<sumPara.rows();i++){
							
							DoubleMatrix1D x0 = new DenseDoubleMatrix1D(dimension);
							for(int j=0;j<dimension;j++)
								x0.set(j, sumPara.get(i, j));
							
							//mean = F(mean)  x0
							//jacobian
							JacobiMatrix jacobi = new JacobiMatrix(grn_, x0);
							double[][] a = jacobi.getJacMatrix();
							DoubleMatrix2D A = new DenseDoubleMatrix2D(grn_.getNodes().size(),grn_.getNodes().size());
							A.assign(a);
							
							
							int trainingTime = 100;
							ODESolverTheta deSolverTheta = new ODESolverTheta(grn_, trainingTime, A);
						
							DoubleMatrix1D initialX0 = new DenseDoubleMatrix1D((int) (Math.pow(dimension,2)));
							for(int j=0;j<initialX0.size();j+=dimension+1)
								initialX0.set(j, 0.1);
							deSolverTheta.setInitialX0(initialX0);

							deSolverTheta.setIslandscape(false);
							deSolverTheta.setRandomInitial(false);
		
							//run numSeries_ times
							ArrayList<DoubleMatrix2D> timeSeriesTheta = new ArrayList<DoubleMatrix2D>();
							ArrayList<DoubleMatrix1D> timeScaleTheta = new ArrayList<DoubleMatrix1D>();		

							deSolverTheta.setTimeScale(timeScaleTheta);
							deSolverTheta.setTimeSeries(timeSeriesTheta);
			
							
							if( !stopRequested )
								deSolverTheta.solveEquations_ODE(); 
							
							//stable state 
							DoubleMatrix1D stable = timeSeriesTheta.get(0).viewRow(trainingTime-1);
							for(int j=dimension;j<2*dimension;j++)
								sumPara.set(i,j,stable.get(0+(dimension+1)*(j-dimension)));
						}						
						
					}
					//end of varation
					
					
					/** update figure **/
					if( !stopRequested ){	
						if( isStable ){
							/** draw landscape **/
							if( !stopRequested ){		
								System.out.print("Plot the landscape...\n");
								
								if( displayMethod ){ //two markers// not gpdm
									grn_.setCounts(counts);
									grn_.setSumPara(sumPara);
									grn_.setLand_isTwoGenes(true);

									grn_.setLand_maxTime(maxTime);
									grn_.setLand_itsValue(itsValue);
									grn_.setLand_maxExpValue(maxExpValue);
									grn_.setLand_focusGenesList(focusGenesList);
									grn_.setLand_gpdmitsValue(gpdmItsValue);
									grn_.setLandTimeSeries(timeSeries);

								}else if( !displayMethod ){//gpdm
									computeGPDMLand(timeSeries, theta, focusGenesList);						
								}
							}

							if( !stopRequested ){	
								finalizeAfterSuccess();
								System.out.print("Done!\n");
							}
						}else{
							snake_.stop();
							myCardLayout_.show(runButtonAndSnakePanel_, runPanel_.getName());
							dispose();

							JOptionPane.showMessageDialog(new Frame(), "<html><body>Cannot find a steady state. <br> Please check if this system is a stable system. Or try to increase the simulation time. <br><br> NetLand cannot construct the landscape of a system without a stable state. The steady state is a situation in which all state variables are constant in spite of ongoing processes that strive to change them. <br>For example, a system with its variables exponentially increasing can never reach a steady state. And the current version of NetLand cannot construct the landscape of a system with stable oscillations. <br></body></html> ", "Warning", JOptionPane.INFORMATION_MESSAGE);
						}
					}
				
				
				}
				catch (OutOfMemoryError e)
				{
//					log_.log(Level.WARNING, "There is not enough memory available to run this program.\n" +
//							"Quit one or more programs, and then try again.\n" +
//							"If enough amounts of RAM are installed on this computer, try to run the program " +
//							"with the command-line argument -Xmx1024m to use maximum 1024Mb of memory, " +
//							"-Xmx2048m to use max 2048Mb, etc.");
					JOptionPane.showMessageDialog(new Frame(), "There is not enough memory available to run this program.\n" +
							"Quit one or more programs, and then try again.\n" +
							"If enough amounts of RAM are installed on this computer, try to run the program " +
							"with the command-line argument -Xmx1024m to use maximum 1024Mb of memory, " +
							"-Xmx2048m to use max 2048Mb, etc.", "Out of memory", JOptionPane.INFORMATION_MESSAGE);
					finalizeAfterFail();

				}
				catch (IllegalArgumentException e)
				{
					//log_.log(Level.WARNING, e.getMessage(), e);
					JOptionPane.showMessageDialog(new Frame(), "Illegal argument", "Error", JOptionPane.INFORMATION_MESSAGE);
					MsgManager.Messages.errorMessage(e, "Error", "");
					//log_.log(Level.INFO, "Potential orkaround: gene names must contain at least one char (e.g. \"5\" is not a valid gene name, but \"G5\" is)");
					finalizeAfterFail();
				}
			}
		}
		
		


		//---------------------------------------------------------------------------------------------------------------------------------
		private void computeGPDMLand(ArrayList<DoubleMatrix2D> timeSeries, ArrayList<DoubleMatrix1D> theta, String[] focusGenesList2) {
			//discrete the final state
			int dimension = grn_.getNodes().size();
			
			//get gene index
			int[] focus_index = new int[focusGenesList.length];
			for(int j=0;j<focusGenesList.length;j++)
				for(int i=0;i<dimension;i++)
					if( grn_.getNode(i).getLabel().equals(focusGenesList[j]) )
						focus_index[j] = i;
			
			
			//calculate Ua
			int exitVal = -2;
			
			//first round of GPDM
			try {
				//write svml
				//input file
				String filename = "temp_"+System.currentTimeMillis();
				String temppath = System.getProperty("java.io.tmpdir");
				File f = new File(temppath);
				File fTemp = File.createTempFile(filename, ".svml", f);

				URL url = fTemp.toURI().toURL();
				if( !stopRequested )
					writeSVML(url,timeSeries,labeledSeries, focus_index);		

				//output file
				String outputModelName = "temp_"+System.currentTimeMillis();
				File fout = new File(temppath);
				File fTempout = File.createTempFile(outputModelName, ".gpdm", fout);

				//run GPDM
				if( !stopRequested )
					exitVal = runGPDM(temppath,fTemp.getName(),fTempout.getName(),gpdmItsValue);

				if( exitVal == 0 ){
					//interpolate new points
					String outputNewPointName = "temp_"+System.currentTimeMillis();
					File fnew = new File(temppath);
					File fTempnew = File.createTempFile(outputNewPointName, ".ypred", fnew);

					if( !stopRequested )
						runMyGPDM(temppath, fTempout.getName(), fTempnew.getName());

					int nPerDim=31;
					//get new Y and new X
					DoubleMatrix2D allY = new DenseDoubleMatrix2D(nPerDim*nPerDim, focusGenesList.length);
					DoubleMatrix2D allX = new DenseDoubleMatrix2D(nPerDim*nPerDim,2); //numYorig+
					double minX=0, maxX=0, minY=0, maxY=0; int n=0;

					String tempString = getYandX(fTempnew, allY, allX);

					//System.out.print(tempString);
					String temp1[] = tempString.split("\t");
					maxX = Double.parseDouble(temp1[0]);
					minX = Double.parseDouble(temp1[1]);
					maxY = Double.parseDouble(temp1[2]);
					minY = Double.parseDouble(temp1[3]);
					n = Integer.parseInt(temp1[4]);
					

					grn_.setCounts(counts);
					grn_.setSumPara(sumPara);
					grn_.setMinX(minX);
					grn_.setMaxX(maxX);
					grn_.setMinY(minY);
					grn_.setMaxY(maxY);
					grn_.setN(n);
					grn_.setAllX(allX);
					grn_.setAllY(allY);
					grn_.setLand_isTwoGenes(false);

					fTemp.delete();
					fTempout.delete();
					fTempnew.delete();

					grn_.setLand_maxTime(maxTime);
					grn_.setLand_itsValue(itsValue);
					grn_.setLand_maxExpValue(maxExpValue);
					grn_.setLand_focusGenesList(focusGenesList);
					grn_.setLand_gpdmitsValue(gpdmItsValue);
					
					grn_.setLandTimeSeries(timeSeries);
					
					return;
				}//end of exitVal == 0 
			} catch (IOException e) {
				JOptionPane.showMessageDialog(new Frame(), "Cannot create a file!", "Error", JOptionPane.INFORMATION_MESSAGE);
				MsgManager.Messages.errorMessage(e, "Error", "");
				this.stop();
			}	
			
			if( exitVal == -2 && loadTimeCourse.isSelected() ){
				JOptionPane.showMessageDialog(new Frame(), "Error of Execution! Please reselect the gene list or run simulations!", "Runtime exception", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
				
			
			//in case of "Matrix non positive definite error"
			while( exitVal == -2 ){ 
				//regenerate traj							
				/** generate time series **/
				SDESolver deSolver_ = new SDESolver(grn_, true, 0);

				//random start values
				deSolver_.setOutputDirectory(System.getProperty("user.dir"));
				deSolver_.setNumSeries(itsValue); //foreach X0 runs once
				deSolver_.setMaxt(maxTime);
				deSolver_.setNumTimePoints(numTimePoints);
				deSolver_.setNoiseStrength(0);
				deSolver_.setIslandscape(true);

				double dt_ = maxTime*1.0 / (numTimePoints - 1) ;
				deSolver_.setDt(dt_);
				
				//run numSeries_ times
				ArrayList<SDETimeSeriesExperiment> tss = new ArrayList<SDETimeSeriesExperiment>();
				ArrayList<DoubleMatrix1D> timeScale = new ArrayList<DoubleMatrix1D>();	
				timeSeries = new ArrayList<DoubleMatrix2D>();
				theta = new ArrayList<DoubleMatrix1D>();
				
				deSolver_.setTss(tss);
				deSolver_.setTimeScale(timeScale);
				deSolver_.setTimeSeries(timeSeries);
				
				int its = 0;
				while( !stopRequested && its<itsValue ){
					System.out.print("Trajectory no: "+its+"\n");
					try {
						deSolver_.solveEquations_landscape(maxExpValue, displayMethod);
					} catch (Exception e) {			
						JOptionPane.showMessageDialog(new Frame(), "Error in execution!", "Runtime exception", JOptionPane.INFORMATION_MESSAGE);
						MsgManager.Messages.errorMessage(e, "Runtime exception", "");
						this.stop();
					}
					its++;
				}
				
				
				try {
					//write svml
					//input file
					String filename = "temp_"+System.currentTimeMillis();
					String temppath = System.getProperty("java.io.tmpdir");
					File f = new File(temppath);
					File fTemp = File.createTempFile(filename, ".svml", f);

					URL url = fTemp.toURI().toURL();
					int numYorig = writeSVML(url,timeSeries,labeledSeries,focus_index);		

					//output file
					String outputModelName = "temp_"+System.currentTimeMillis();
					File fout = new File(temppath);
					File fTempout = File.createTempFile(outputModelName, ".gpdm", fout);

					//run GPDM
					while( !stopRequested )
						exitVal = runGPDM(temppath,fTemp.getName(),fTempout.getName(),gpdmItsValue);

					if( exitVal == 0 ){
						//interpolate new points
						String outputNewPointName = "temp_"+System.currentTimeMillis();
						File fnew = new File(temppath);
						File fTempnew = File.createTempFile(outputNewPointName, ".ypred", fnew);

						while( !stopRequested )
							runMyGPDM(temppath, fTempout.getName(), fTempnew.getName());

						int nPerDim=31;
						//get new Y and new X
						DoubleMatrix2D allY = new DenseDoubleMatrix2D(nPerDim*nPerDim,grn_.getNodes().size());
						DoubleMatrix2D allX = new DenseDoubleMatrix2D(nPerDim*nPerDim,2); //numYorig+
						double minX=0, maxX=0, minY=0, maxY=0; int n=0;

						String tempString = getYandX(fTempnew, allY, allX);

						//System.out.print(tempString);
						String temp1[] = tempString.split("\t");
						maxX = Double.parseDouble(temp1[0]);
						minX = Double.parseDouble(temp1[1]);
						maxY = Double.parseDouble(temp1[2]);
						minY = Double.parseDouble(temp1[3]);
						n = Integer.parseInt(temp1[4]);


						grn_.setCounts(counts);
						grn_.setSumPara(sumPara);
						grn_.setMinX(minX);
						grn_.setMaxX(maxX);
						grn_.setMinY(minY);
						grn_.setMaxY(maxY);
						grn_.setN(n);
						grn_.setAllX(allX);
						grn_.setAllY(allY);
						grn_.setLand_isTwoGenes(false);

						fTemp.delete();
						fTempout.delete();
						fTempnew.delete();

						grn_.setLand_maxTime(maxTime);
						grn_.setLand_itsValue(itsValue);
						grn_.setLand_maxExpValue(maxExpValue);
						grn_.setLand_focusGenesList(focusGenesList);
						grn_.setLand_gpdmitsValue(gpdmItsValue);
						
						return;
					}//end of exitVal == 0
				} catch (IOException e) {
					JOptionPane.showMessageDialog(new Frame(), "Cannot create a file!", "Error", JOptionPane.INFORMATION_MESSAGE);
					MsgManager.Messages.errorMessage(e, "Error", "");
					this.stop();
				}		
			}//end of while
			
		}
		
		
		//run c++ GPDM
		public int runGPDM(String filePath, String filename, String outputModelName, int gpdmItsValue){	
			int exitVal = -1;

			Runtime rn = Runtime.getRuntime();
			Process p = null;
			try {
				if( ide ){
					//for ide
					String programPath = "E:\\netland\\workspace\\NetLand\\GPDM\\win";			
					p = rn.exec("cmd /C gplvm.exe -v 3 learn -L true -D rbf -# "+gpdmItsValue+" "+filePath+"\\"+filename+" "+filePath+"\\"+outputModelName,null, new File(programPath));		
				}else{
					//for standalone package
					String programPath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();	
	
	
					String os = System.getProperty("os.name");  		
					if(os.toLowerCase().startsWith("win")){   
						programPath = programPath.replaceAll("^/", "");
						programPath = programPath.replaceAll("\\/", "\\\\");	
						programPath = programPath.substring(0, programPath.lastIndexOf("\\")+1)+"GPDM\\win";
						programPath = programPath.replaceAll("%20"," ");
						p = rn.exec("cmd /C gplvm.exe -v 3 learn -L true -D rbf -# "+gpdmItsValue+" "+filePath+"\\"+filename+" "+filePath+"\\"+outputModelName,null, new File(programPath));
					}else if(os.toLowerCase().startsWith("linux")){ 
						p = rn.exec("./GPDM/linux/gplvm -v 3 learn -L true -D rbf -# "+gpdmItsValue+" "+filePath+"/"+filename+" "+filePath+"/"+outputModelName);
					}else if(os.toLowerCase().startsWith("mac")){ 
						//				programPath = programPath.substring(0, programPath.lastIndexOf("/")+1)+"GPDM/mac";
						//				programPath = programPath.replaceAll("%20","\" \"");
						p = rn.exec("./GPDM/mac/gplvm -v 3 learn -L true -D rbf -# "+gpdmItsValue+" "+filePath+"/"+filename+" "+filePath+"/"+outputModelName);
					}
				}
			
				
				System.out.print("Run GPDM \n"); //+programPath+"\n"+filePath+"\n"+outputModelName+"\n"


				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String msg = null;
				while ((msg = br.readLine()) != null) {
					System.out.println(msg);
					if( msg.equals("Press enter for more.") ){
						p.destroy();
						return -2;
//						JOptionPane.showMessageDialog(null, "Please try again.", "Error in execution of GPDM!", JOptionPane.INFORMATION_MESSAGE);
//						return exitVal;
					}
					checkForInterruption_process(p);
				}

				exitVal = p.waitFor();

			} catch (Exception e) {		
				if( e.getMessage().equals("Generation canceled!") ){
					snake_.stop();
					myCardLayout_.show(runButtonAndSnakePanel_, runPanel_.getName());
					return -1;
				}else{
					snake_.stop();
					myCardLayout_.show(runButtonAndSnakePanel_, runPanel_.getName());
					stopRequested = true;
					JOptionPane.showMessageDialog(null, "Error in the execution of GPDM!", "Error", JOptionPane.INFORMATION_MESSAGE);
					MsgManager.Messages.errorMessage(e, "Error", "");
					System.out.print("Generation stopped!");
				}
			}
			
			return exitVal;	
		}
		
		
		private void checkForInterruption_process(Process p) throws CancelException {	
			if ( stopRequested ){ 
				p.destroy();
				throw new CancelException("Generation canceled!");
			}
		}
		
		//read Ypred Y and Xold Xnew from files
		private String getYandX(File fTempnew, DoubleMatrix2D allY, DoubleMatrix2D allX) {
			String paras = "";
			
			//get ogrinal Y and X
	        BufferedReader reader = null;
	        try {
	            reader = new BufferedReader(new FileReader(fTempnew));
	            String tempString = null;
	            //get minX maxX minY maxY
	            tempString = reader.readLine();
	            paras = tempString;
	            
	            int line = 0;
	            while ((tempString = reader.readLine()) != null) {
	            	String temp[] = tempString.split("\t");
	            	for(int i=0;i<allY.columns();i++)
	            		allY.set(line, i, Double.parseDouble(temp[i]));
	            	for(int i=allY.columns();i<temp.length;i++)
	            		allX.set(line, i-allY.columns(), Double.parseDouble(temp[i]));
	            	line++;
	            }
	            reader.close();
	            
	        } catch (IOException e) {
	        	JOptionPane.showMessageDialog(null, "Error in reading the file!", "Error", JOptionPane.INFORMATION_MESSAGE);
	        	MsgManager.Messages.errorMessage(e, "Error", "");
	        } finally {
	            if (reader != null) {
	                try {
	                    reader.close();                 
	                } catch (IOException e1) {
	                	JOptionPane.showMessageDialog(null, "Error in reading the file!", "Error", JOptionPane.INFORMATION_MESSAGE);
	                	MsgManager.Messages.errorMessage(e1, "Error", "");
	                }
	            }
	        }
	    	
	        return paras;
		}


		//from latent to orginal
		private int runMyGPDM(String filePath, String modelName, String outputName) {
			int exitVal = -1;
			
			Runtime rn = Runtime.getRuntime();
			Process p = null;
			try {
				if( ide ){
					//for ide
					String programPath = "E:\\netland\\workspace\\NetLand\\GPDM\\win";
					p = rn.exec("cmd /c myGPLVM.exe -i "+filePath+"\\"+modelName+" -o "+filePath+"\\"+outputName,null, new File(programPath));
				}else{
					//for standalone package
					String programPath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();	

					String os = System.getProperty("os.name");  
					if(os.toLowerCase().startsWith("win")){  
						programPath = programPath.replaceAll("^/", "");
						programPath = programPath.replaceAll("\\/", "\\\\");
						programPath = programPath.substring(0, programPath.lastIndexOf("\\")+1)+"GPDM\\win";
						programPath = programPath.replaceAll("%20"," ");
						p = rn.exec("cmd /c myGPLVM.exe -i "+filePath+"\\"+modelName+" -o "+filePath+"\\"+outputName,null, new File(programPath));
					}else if(os.toLowerCase().startsWith("linux")){ 
						p = rn.exec("./GPDM/linux/myGPLVM -i "+filePath+"/"+modelName+" -o "+filePath+"/"+outputName);
					}else if(os.toLowerCase().startsWith("mac")){ 
						//					programPath = programPath.substring(0, programPath.lastIndexOf("/")+1)+"GPDM/mac";
						//					programPath = programPath.replaceAll("%20","\" \"");
						p = rn.exec("./GPDM/mac/myGPLVM -i "+filePath+"/"+modelName+" -o "+filePath+"/"+outputName);
					}  
				}
				
								
				
				//System.out.println("Run reverse mapping\n");
				checkForInterruption_process(p);
							
				exitVal = p.waitFor();
//				System.out.println("Process exitValue mygpdm: " + exitVal);
				
				
//				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
//				String msg = null;
//				while ((msg = br.readLine()) != null) {
//					System.out.println(msg);
//				}
			} catch (Exception e) {
				snake_.stop();
				myCardLayout_.show(runButtonAndSnakePanel_, runPanel_.getName());
				stopRequested = true;
				JOptionPane.showMessageDialog(null, "Error in the execution of GPDM!", "Error", JOptionPane.INFORMATION_MESSAGE);
				MsgManager.Messages.errorMessage(e, "Error", "");		
			}	
			return exitVal;
		}
		
		public int writeSVML(URL filename, ArrayList<DoubleMatrix2D> timeSeries, ArrayList<int[]> labeledSeries, int[] focus_index) throws IOException{
			
			//cannot take all data
			//int n1 = its/30; total 30 trajectories are selected
			int n1 = 10; //default 10 traj per attractor
			
			// Create a new file	
			FileWriter fw = new FileWriter(filename.getPath());   


			//define the num of traj for each attractor; thus we should sample enough items from each attractor!!!
			int[] nTrajPerAttr = new int[counts.length];
			int minCounts = 1000000;
			for(int i=0;i<counts.length;i++)
				if( minCounts>counts[i] )
					minCounts = counts[i];
			
			if( minCounts>n1 )
				for(int i=0;i<counts.length;i++)
					nTrajPerAttr[i] = n1;
			else
				for(int i=0;i<counts.length;i++)
					if( counts[i]<n1 )
						nTrajPerAttr[i] = counts[i];
					else
						nTrajPerAttr[i] = n1;

			
			int lines = 0; 
			//write trajectories
			for(int i=0;i<counts.length;i++){	
				for(int jj=0;jj<nTrajPerAttr[i];jj++){
					DoubleMatrix2D tempX0 =  timeSeries.get(labeledSeries.get(i)[jj]);	
					int len = tempX0.rows();
					int interval = len<100?1:len/10;
					int numItem = (len-1)/interval+1;
					
//					//take the last 10
//					for(int j=tempX0.rows()-10;j<tempX0.rows();j+=1){ //rows: time length
//						String temp = "0";
//						for(int k=1;k<=tempX0.columns();k++)
//							temp += " "+k+":"+tempX0.get(j, k-1);
//						fw.write(temp+"\n");
//						lines++;
//					}
					
					for(int j=0;j<tempX0.rows();j+=interval){ //rows: time length
						String temp = "0";
						//for(int k=1;k<=tempX0.columns();k++)
						for(int k=1;k<=focus_index.length;k++)
							temp += " "+k+":"+tempX0.get(j, focus_index[k-1]);
						fw.write(temp+"\n");
						lines++;
					}
				}
			}
			
			//write origAllY
			DoubleMatrix2D origAllY = new DenseDoubleMatrix2D(lines, grn_.getNodes().size());
			lines = 0;
			for(int i=0;i<counts.length;i++){	
				for(int jj=0;jj<nTrajPerAttr[i];jj++){
					DoubleMatrix2D tempX0 =  timeSeries.get(labeledSeries.get(i)[jj]);	
					int len = tempX0.rows();
					int interval = len<100?1:len/10;
					int numItem = (len-1)/interval+1;
					
//					//take the last 10
//					for(int j=tempX0.rows()-10;j<tempX0.rows();j+=1){ //rows: time length
//						for(int k=1;k<=tempX0.columns();k++)
//							origAllY.set(lines, k-1, tempX0.get(j, k-1));
//						lines++;
//					}
					
					
					for(int j=0;j<tempX0.rows();j+=interval){
						for(int k=1;k<=tempX0.columns();k++)
							origAllY.set(lines, k-1, tempX0.get(j, k-1));
						lines++;
					}
				}
			}
			grn_.setOrigAllY(origAllY);
			
			fw.close();   

			return lines;
		}

		//-------------------------------------------------------------------------------------------------------------------------
		private boolean checkAttractor(String[] focusGenes, ArrayList<DoubleMatrix2D> timeSeries){
			//discrete the final state
			int dimension = grn_.getNodes().size();
			
			//get gene index
			int[] focus_index = new int[focusGenes.length];
			for(int j=0;j<focusGenes.length;j++)
				for(int i=0;i<dimension;i++)
					if( grn_.getNode(i).getLabel().equals(focusGenes[j]) )
						focus_index[j] = i;
			
			//double check distances between attractors	
			String out =  calculateDistances(timeSeries, focus_index, dimension);
			
			if( out.equals("ok") ){						
				return true;
			}
			
			return false;
		}
		
		
		private String calculateDistances(ArrayList<DoubleMatrix2D> timeSeries, int[] focus_index, int dimension) {		
			//solver equations
			List<String> solverResults_focusgenes = new ArrayList<String>(itsValue);
			DoubleMatrix2D attractorTypes_focusgene = new DenseDoubleMatrix2D(itsValue, focus_index.length);
			ArrayList<DoubleMatrix1D> attractorTypesAll = new ArrayList<DoubleMatrix1D>(itsValue);
			
			//java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");  
			int i = 0;
			while( !stopRequested && i<itsValue ){	
				DoubleMatrix1D tempX0 =  timeSeries.get(i).viewRow(timeSeries.get(i).rows()-1);
			
				cern.jet.math.Functions F = cern.jet.math.Functions.functions;	
//				if( this.displayMethod ){
//					DoubleMatrix1D tempX1 =  timeSeries.get(i).viewRow(timeSeries.get(i).rows()-2);					
//					double dis = tempX0.aggregate(tempX1, F.plus, F.chain(F.square,F.minus));
//					if( dis>0.000001 ) return "notStable";
//				}
				
				nonlinearEq a = new nonlinearEq(grn_);
				DoubleMatrix1D tempY = a.runSolver(tempX0,grn_);
				if( tempY == null ) return "notStable";

//				DoubleMatrix1D tempX1 =  timeSeries.get(i).viewRow(timeSeries.get(i).rows()-2);
				//judge if the stable state is far from the end position
//				cern.jet.math.Functions F = cern.jet.math.Functions.functions;	
				double dis = tempX0.aggregate(tempY, F.plus, F.chain(F.square,F.minus));
				if( dis>500 ) return "notStable"; ////ad hoc	
//				DoubleMatrix1D tempY = tempX0;
				
				attractorTypesAll.add(tempY);

				String temp1 = "";
				for(int j=0;j<focus_index.length;j++){				
					double temp = Math.floor(100*tempY.get(focus_index[j]))/100;
					temp1 += temp+";" ;
					attractorTypes_focusgene.set(i, j, Math.abs(temp));
				}
				solverResults_focusgenes.add(temp1);
				
				i++;
			}
			
			//distance matrix
			double threshold = 0.1; //0.05 hahahaha
			ArrayList<Integer> output = calculateDisMatrix(attractorTypes_focusgene, threshold);
			
			Collections.sort(output);
			//remove i or j
			for(i=output.size()-1;i>=0;i--){
				solverResults_focusgenes.remove((int)output.get(i));
				attractorTypes_focusgene = ColtUtils.deleteRow(attractorTypes_focusgene, output.get(i));
			}
			
			//remove duplicates
			List<String> uniqueList_focusgene = new ArrayList<String>(new HashSet<String>(solverResults_focusgenes));
			
			
//			//double check uniqueList
//			nonlinearEq a = new nonlinearEq(grn_);
//			for(i=0;i<uniqueList_focusgene.size();i++){	
//				String str = uniqueList_focusgene.get(i);
//				String[] strlist = str.split(";");
//				DoubleMatrix1D tempX0 = new DenseDoubleMatrix1D(strlist.length);
//				for(int j=0;j<strlist.length;j++)
//					tempX0.set(j, Double.parseDouble(strlist[j]));
//				DoubleMatrix1D tempY = a.runSolver(tempX0,grn_);	
//				if( tempY == null ) return "notStable";
//				
//				str = "";
//				for(int j=0;j<tempY.size();j++)
//					str += tempY.get(j)+";" ;
//				uniqueList_focusgene.set(i,str);
//			}
//			List<String> newUniqueList_focusgene = new ArrayList<String>(new HashSet<String>(uniqueList_focusgene));
				
			
			//calculate para
			sumPara = new DenseDoubleMatrix2D( uniqueList_focusgene.size(), dimension*2);
			counts = new int[uniqueList_focusgene.size()];
			labeledSeries = new ArrayList<int[]>(uniqueList_focusgene.size());
			sumPara.assign(0);
				
			int temp[][] = new int[counts.length][itsValue];
			cern.jet.math.Functions F = cern.jet.math.Functions.functions;	
			for(i=0;i<itsValue;i++){
				DoubleMatrix1D tempX0 =  timeSeries.get(i).viewRow(timeSeries.get(i).rows()-1);
		
				//generate current vector
				DoubleMatrix1D temp1 = new DenseDoubleMatrix1D(focus_index.length);
				for(int j=0;j<focus_index.length;j++)
					temp1.set(j, Math.floor(100*tempX0.get(focus_index[j]))/100);
			
				//close to which attractor
				double[] tempSum = new double[sumPara.rows()]; 
				for(int k=0;k<uniqueList_focusgene.size();k++)
					tempSum[k] =  temp1.aggregate(attractorTypes_focusgene.viewRow(k), F.plus, F.chain(F.square,F.minus));
					
				
				//find the minimal distance
				int flag = -1; double minimal = 100000;
				for(int j=0;j<tempSum.length;j++)
					if(tempSum[j]<minimal){flag=j;minimal=tempSum[j];}
				
				counts[flag] += 1;
				temp[flag][counts[flag]-1] = i;
						
				for(int j=0;j<dimension;j++)
					sumPara.set(flag, j, sumPara.get(flag, j) + tempX0.get(j));
				
				for(int j=dimension;j<2*dimension;j++)
					sumPara.set(flag, j, 0.03);
			}
			//-------------------
			
			for(i=0;i<counts.length;i++)
				labeledSeries.add(temp[i]);
			
			for(int j=0;j<uniqueList_focusgene.size();j++)
				Transform.div(sumPara.viewRow(j), counts[j]);
			
			return "ok";
		}
		
		
		// ----------------------------------------------------------------------------

		public void finalizeAfterSuccess() 
		{
			snake_.stop();
			myCardLayout_.show(runButtonAndSnakePanel_, runPanel_.getName());
			dispose();
			
			LandscapePanel onea = new LandscapePanel(grn_, displayMethod);	
			
			if( !outputPath.isEmpty() )
				saveLand();
		
		}

		private void saveLand(){
			//first part: SBML
			grn_.setId("NetLand_"+grn_.getId());
			String outputNewPointName = "temp_"+System.currentTimeMillis();
			String temppath = System.getProperty("java.io.tmpdir");
			File fnew = new File(temppath);

			try {
				File fTempnew = File.createTempFile(outputNewPointName, ".sbml", fnew);			
				URL url = fTempnew.toURI().toURL();
				grn_.writeSBML(url);	
				
				//second part: land info	
				String outputNewPointName1 = "temp_"+System.currentTimeMillis();
				String temppath1 = System.getProperty("java.io.tmpdir");
				File fnew1 = new File(temppath1);
				File fTempnew1 = File.createTempFile(outputNewPointName1, ".landscape", fnew1);
				FileWriter fw = new FileWriter(fTempnew1.getPath(), false);

				fw.write("\n\nLand\t"+grn_.getMaxX()+"\t"+grn_.getMinX()+"\t"+grn_.getMaxY()+"\t"+grn_.getMinY()+"\n");
				

				//write parameters
				fw.write(grn_.getSize()+"\t"+maxExpValue+"\t"+itsValue+"\t"+maxTime+"\t"+displayMethod+"\t"+gpdmIts.getText()+"\n");
				
				for(int i=0;i<focusGenesList.length;i++)
					fw.write(focusGenesList[i]+"\t");
				fw.write("\n");
				
				//write x
				for(int i=0;i<grn_.getX().size();i++)
					fw.write(grn_.getX().get(i)+"\t");
				fw.write("\n");
				
				//write y
				for(int i=0;i<grn_.getY().size();i++)
					fw.write(grn_.getY().get(i)+"\t");
				fw.write("\n");
				
				//write counts
				for(int i=0;i<grn_.getCounts().length;i++)
					fw.write(grn_.getCounts()[i]+"\t");
				fw.write("\n");
				
				
				//write sumpara
				for(int i=0;i<grn_.getSumPara().rows();i++){
					for(int j=0;j<grn_.getSumPara().columns();j++)
						fw.write(grn_.getSumPara().get(i,j)+"\t");
					fw.write("\n");
				}
				
				//write allY for gpdm				
				if( !displayMethod ){//only for gpdm
					fw.write(grn_.getAllY().rows()+"\t"+grn_.getAllY().columns()+"\n");
					
					for(int i=0;i<grn_.getAllY().rows();i++){
						for(int j=0;j<grn_.getAllY().columns();j++)
							fw.write(grn_.getAllY().get(i, j)+"\t");
						fw.write("\n");
					}
				}
				
				
				//write size of griddata
				fw.write(grn_.getGridData().rows()+"\t"+grn_.getGridData().columns()+"\n");
				
				//write griddata
				for(int i=0;i<grn_.getGridData().rows();i++){
					for(int j=0;j<grn_.getGridData().columns();j++)
						fw.write(grn_.getGridData().get(i, j)+"\t");
					fw.write("\n");
				}
				
				//write timeseries data
				for(int k=0;k<grn_.getLandTimeSeries().size();k++){
					fw.write(grn_.getLandTimeSeries().get(k).rows()+"\n");
					for(int i=0;i<grn_.getLandTimeSeries().get(k).rows();i++){
						for(int j=0;j<grn_.getLandTimeSeries().get(k).columns();j++)
							fw.write(grn_.getLandTimeSeries().get(k).get(i, j)+"\t");
						fw.write("\n");
					}
				}
				
				
				if( !displayMethod ){
					//write origAllY
					fw.write(grn_.getOrigAllY().rows()+"\t"+grn_.getOrigAllY().columns()+"\n");

					for(int i=0;i<grn_.getOrigAllY().rows();i++){
						for(int j=0;j<grn_.getOrigAllY().columns();j++)
							fw.write(grn_.getOrigAllY().get(i, j)+"\t");
						fw.write("\n");
					}
				}
				
				
				
				fw.close();
				
				//merge
				String output = outputPath;
				mergeFiles(output, new String[]{fTempnew.getAbsolutePath(), fTempnew1.getAbsolutePath()});
				
				fTempnew.delete();
				fTempnew1.delete();
				
				System.out.print("Save the landsacpe to "+outputPath+"\n");
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null,  "Cannot create a new file!", "Error", JOptionPane.INFORMATION_MESSAGE);
				MsgManager.Messages.errorMessage(e1, "Error", "");
			} catch (SBMLException e) {
				JOptionPane.showMessageDialog(null, "Cannot write file!", "Error", JOptionPane.INFORMATION_MESSAGE);
				MsgManager.Messages.errorMessage(e, "Error", "");
			} catch (XMLStreamException e) {
				JOptionPane.showMessageDialog(null,  "Invalid format!", "Error", JOptionPane.INFORMATION_MESSAGE);
				MsgManager.Messages.errorMessage(e, "Error", "");
			} catch (ParseException e) {
				JOptionPane.showMessageDialog(null,  "Error in parsing the file!", "Error", JOptionPane.INFORMATION_MESSAGE);
				MsgManager.Messages.errorMessage(e, "Error", "");
			}
			
			
		}
		
		// ----------------------------------------------------------------------------

		public void finalizeAfterFail()
		{
			snake_.stop();
			myCardLayout_.show(runButtonAndSnakePanel_, runPanel_.getName());
			//escapeAction(); // close the simulation window
		}

		public String getOutputPath() {
			return outputPath;
		}

		public void setOutputPath(String outputPath) {
			this.outputPath = outputPath;
		}
		
		public void mergeFiles(String outFile, String[] files) {  
	        FileChannel outChannel = null;  
	        int BUFSIZE = 1024 * 8;
	        
	        try {  
	            outChannel = new FileOutputStream(outFile).getChannel();  
	            for(String f : files){  
	                FileChannel fc = new FileInputStream(f).getChannel();   
	                ByteBuffer bb = ByteBuffer.allocate(BUFSIZE);  
	                while(fc.read(bb) != -1){  
	                    bb.flip();  
	                    outChannel.write(bb);  
	                    bb.clear();  
	                }  
	                fc.close();  
	            }  
	        } catch (IOException ioe) {  
	        	JOptionPane.showMessageDialog(null, "Cannot merge two files!", "Error", JOptionPane.INFORMATION_MESSAGE);
	        	MsgManager.Messages.errorMessage(ioe, "Error", "");
	        } finally {  
	            try {if (outChannel != null) {outChannel.close();}}
	            catch (IOException ignore) {
	            	JOptionPane.showMessageDialog(null, "Cannot merge two files!", "Error", JOptionPane.INFORMATION_MESSAGE);
	            	MsgManager.Messages.errorMessage(ignore, "Error", "");
	            }  
	        }  
	    }  
		
	}
	
	
	public static ArrayList<Integer> calculateDisMatrix(DoubleMatrix2D matrix, double threshold) {
		ArrayList<Integer> output = new ArrayList<Integer>();
		matrix = ColtUtils.transpose(matrix);
		DoubleMatrix2D a = Statistic.distance(matrix, Statistic.EUCLID);
				
		for(int i=0;i<a.rows()-1;i++)
			for(int j=i+1;j<a.columns();j++)
				if( a.get(i, j)<threshold ){
					if( !output.contains(j) )
						output.add(j);
				}
		
		return output;
	}

	

}


