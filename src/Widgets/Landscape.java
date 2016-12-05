package Widgets;

import java.awt.Component;
import java.awt.Frame;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.text.parser.ParseException;

import LandscapeAnimation.LandscapePanel;
import LandscapeDisplay.JacobiMatrix;
import LandscapeDisplay.ODESolver;
import LandscapeDisplay.ODESolverTheta;
import LandscapeDisplay.nonlinearEq;
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
	private GeneNetwork grn;
	private LandscapeThread landscape = null;

	private boolean ide = false; 

	public Landscape(Frame aFrame, NetworkElement item) {
		super(aFrame);		
		grn  = ((DynamicalModelElement) item).getGeneNetwork();

		//closing listener
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent windowEvent){
				if( landscape != null && landscape.myThread_.isAlive() ){
					landscape.stop();	
					System.out.print("Simulation is canceled.\n");
					//JOptionPane.showMessageDialog(new Frame(), "Simulation is canceled.", "Warning!", JOptionPane.INFORMATION_MESSAGE);
				}
				escapeAction();
			}
		});


		//set core genenames		
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

				enterAction();
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


	public void enterAction() {
		try {
			//System.out.print("land start: "+System.currentTimeMillis()+"\n"); 
			
			if( grn.getNodes().size() <= 1 ){
				JOptionPane.showMessageDialog(null, "The system only contains "+grn.getNodes().size()+" reaction! To plot the landscape, at least two kinetic reactions are required.", "Error", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			GnwSettings settings = GnwSettings.getInstance(); 				

			landscape = new LandscapeThread();
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
//		private GeneNetwork grn_;
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
		private String landscapeMethod;
		private boolean displayMethod = true; //default two markers
		private int gpdmItsValue;

		
		// ============================================================================
		// PUBLIC METHODS
		public LandscapeThread()
		{
			super();
			myThread_ = null;
			stopRequested = false;

			maxExpValue = Double.parseDouble(maxExp.getText());
			itsValue = Integer.parseInt(its.getText());
			maxTime = Integer.parseInt(maxT.getText());
			gpdmItsValue = Integer.parseInt(gpdmIts.getText());

			numTimePoints = (int) (maxTime*2+1);
			focusGenesList = focusGenes.getText().split(";");
//			outputPath = userPath_.getText();
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
					/** generate time series **/
					//run numSeries_ times
					ArrayList<DoubleMatrix2D> timeSeries = new ArrayList<DoubleMatrix2D>();
//					ArrayList<DoubleMatrix1D> timeScale = new ArrayList<DoubleMatrix1D>();		


					boolean isStable = true;
					if( generateTimeCourse.isSelected() ){
						generateTimeseriesData();												
					}//else if( loadTimeCourse.isSelected() )
						//timeSeries = grn.getLandTimeSeries();

					timeSeries = grn.getLandTimeSeries();

					grn.setDisplayMethod(landscapeMethod);


					/** get attractors focusGenes=all **/
					if( timeSeries.size() == 0 ){
						isStable = false;
					}else if( !stopRequested && isStable ){	
						System.out.print("Calculate attractors...\n");
						if( !checkAttractor(focusGenesList, timeSeries) ){
							isStable = false;
						}
					}


					/** calculate theta **/	
					int dimension = grn.getNodes().size();	
					ArrayList<DoubleMatrix1D> theta = new ArrayList<DoubleMatrix1D>(dimension);						

					isStable = calculateTheta(isStable, theta);
					//end of varation


					/** update figure **/
					if( !stopRequested ){	
						if( isStable ){
							/** draw landscape **/
							if( !stopRequested ){		
								System.out.print("Plot the landscape...\n");

								if( displayMethod ){ //two markers// not gpdm
									grn.setLand_isTwoGenes(true);

									grn.setLand_maxTime(maxTime);
									grn.setLand_itsValue(itsValue);
									grn.setLand_maxExpValue(maxExpValue);
									grn.setLand_focusGenesList(focusGenesList);
									grn.setLand_gpdmitsValue(gpdmItsValue);
									grn.setLandTimeSeries(timeSeries);

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

							JOptionPane.showMessageDialog(new Frame(), "Cannot find a steady state. \n Please check if this system is a stable system. Or try to increase the simulation time. \n NetLand cannot construct the landscape of a system without a stable state. \n And the current version of NetLand cannot construct the landscape of a system with stable oscillations. \n ", "Warning", JOptionPane.INFORMATION_MESSAGE);
						}
					}


				}
				catch (OutOfMemoryError e)
				{
					JOptionPane.showMessageDialog(new Frame(), "There is not enough memory available to run this program.\n" +
							"Quit one or more programs, and then try again.\n" +
							"If enough amounts of RAM are installed on this computer, try to run the program \n" +
							"with the command-line argument -Xmx[XXXX]m to set the maximum memory of JVM. \n "
							, "Out of memory", JOptionPane.INFORMATION_MESSAGE);
					finalizeAfterFail();

				}
				catch (IllegalArgumentException e)
				{
					JOptionPane.showMessageDialog(new Frame(), "Illegal argument", "Error", JOptionPane.INFORMATION_MESSAGE);
					MsgManager.Messages.errorMessage(e, "Error", "");
					finalizeAfterFail();
				}
			}
				
		}
		
		
		
		private boolean calculateTheta(boolean isStable, ArrayList<DoubleMatrix1D> theta) {
			int dimension = grn.getNodes().size();	
			
			if( !stopRequested && isStable ){
				for(int i=0;i<grn.getSumPara().rows();i++){

					DoubleMatrix1D x0 = new DenseDoubleMatrix1D(dimension);
					for(int j=0;j<dimension;j++)
						x0.set(j, grn.getSumPara().get(i, j));

					//mean = F(mean)  x0
					//jacobian
					JacobiMatrix jacobi = new JacobiMatrix(grn, x0);
					double[][] a = jacobi.getJacMatrix();
					DoubleMatrix2D A = new DenseDoubleMatrix2D(grn.getNodes().size(),grn.getNodes().size());
					A.assign(a);


					int trainingTime = 100;
					ODESolverTheta deSolverTheta = new ODESolverTheta(grn, trainingTime, A);

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
					for(int j=dimension;j<2*dimension;j++){
						if( stable.get(0+(dimension+1)*(j-dimension))<0 ){
							isStable = false;
							break;
						}else if( stable.get(0+(dimension+1)*(j-dimension))==0 )
							grn.getSumPara().set(i,j,0.01);
						grn.getSumPara().set(i,j,stable.get(0+(dimension+1)*(j-dimension)));
					}
				}						

			} //end of if
			
			return isStable;
		}

		private void generateTimeseriesData() {
			/** settings **/
			double step = 1; //default fixed step

			
			/** check memory **/			
			double leastMemoryReq = maxTime*1.0/step*itsValue*8*grn.getNodes().size()/1024.0/1024; //MB
			boolean isEnoughMem = checkMemory(leastMemoryReq);

			boolean isCutTime = false;
			boolean isCutIts = false;

			
			if( !isEnoughMem ){
				leastMemoryReq = maxTime*1.0/step*1*8*grn.getNodes().size()/1024.0/1024; //MB for 1 traj
				int maxPointsPerTraj = calculateMaxPointsPerTraj(itsValue*2*8*grn.getNodes().size()/1024.0/1024);
				isEnoughMem = checkMemory(leastMemoryReq);
				
				if( !isEnoughMem ) //for one traj
					memoryWarning(leastMemoryReq, 0);
				else{
					/** decide m, save every m traj **/
					int m = calculateMTraj(leastMemoryReq);
					if( m<itsValue )
						isCutIts = true;
					else
						m = itsValue;

					if( m==0 ) m=1;


					/** decide t, save every t steps **/
					int t = maxTime;
					
					/** run SDE **/
					runODE(itsValue, maxTime, true, m, t, isCutTime, isCutIts, maxPointsPerTraj);

				}
			}else
				runODE(itsValue, maxTime, true, itsValue, maxTime, isCutTime, isCutIts, maxTime);
			
		}

		private void runODE(Integer numSeries, Integer maxt, boolean randomInitial, int saveEveryMTraj, int saveEveryTStep, boolean isCutTime, boolean isCutIts, int maxPointsPerTraj) {
			System.out.print("Simulating\n"); 			

			/** run solver **/
			ODESolver deSolver_ = new ODESolver(grn, maxt);

			if( randomInitial ){
				deSolver_.setUpBoundary(maxExpValue);
				deSolver_.setLowBoundary(0);
			}


			/** set parameters **/
			deSolver_.setIslandscape(true);
			deSolver_.setRandomInitial(randomInitial);		

			/** backup initial values **/
			DoubleMatrix1D initialX0 = grn.getInitialState().copy();

			/** create a temp dir **/
			File dir = new File("./"+ "NetLand_" 
					+ new SimpleDateFormat("yyyyMMddhhmmss").format(new Date())  
					+ "_tempSimulationResult");  
			dir.mkdir(); 

			/** create tmp file **/
			File tmpfilename = createTmpFile(dir, numSeries, grn.getSize(), maxt, 0, "land");			

			if( tmpfilename == null ){
				dir.delete();
				finalizeAfterFail();
				return;
			}

			/** each iteration **/		
			int its = 1; //output iterations


			/** set temp storage, for saving **/
			ArrayList<DoubleMatrix2D> temptimeSeries = new ArrayList<DoubleMatrix2D>();
			ArrayList<DoubleMatrix1D> temptimeScale = new ArrayList<DoubleMatrix1D>();

			/** data to plot **/
			ArrayList<DoubleMatrix2D> timeSeries = new ArrayList<DoubleMatrix2D>();
			ArrayList<DoubleMatrix1D> timeScale = new ArrayList<DoubleMatrix1D>();		


			int steps = saveEveryTStep; //simulation time for each run
			int numPoints = 0; //record the number of sampled points
			double previoudTime = 0; //if isCutTime=ture, record the end time of the previous segment 
			cern.colt.matrix.DoubleFactory1D Factory1D = cern.colt.matrix.DoubleFactory1D.dense;
			cern.colt.matrix.DoubleFactory2D Factory2D = cern.colt.matrix.DoubleFactory2D.dense;

			while( !stopRequested && its<=numSeries ){					

				/** reset parameters **/
				numPoints = 0;			
				previoudTime = 0;

				/** set time for each traj **/
				for(int j=0;j<maxt;j+=saveEveryTStep){		
					steps = saveEveryTStep;			
					if( j+saveEveryTStep>maxt )
						steps = maxt-j;

					deSolver_.setT(steps);	

					deSolver_.solveEquations_ODE();						

					
					
					/** sample points **/
					if( deSolver_.getTimeSeries() == null || deSolver_.getTimeSeries().rows()==0 ){
						its--;
						break;
					}else if( deSolver_.getTimeSeries().rows()>200 || deSolver_.getTimeSeries().rows()==1 ){
						numPoints = samplePoints(deSolver_);
					}else
						numPoints = deSolver_.getTimeSeries().rows();
					
					System.out.print("\nIts: "+its+"\n"); 

					/** update time scale **/
					deSolver_.setTimeScale(Transform.plus(deSolver_.getTimeScale(), previoudTime));

					temptimeSeries.add(deSolver_.getTimeSeries().copy());
					temptimeScale.add(deSolver_.getTimeScale().copy());

					/** new initial state from previous calculation **/
					grn.setInitialState(deSolver_.getTimeSeries().viewRow(deSolver_.getTimeSeries().rows()-1).copy());


					if( numPoints>maxPointsPerTraj ){

						if( previoudTime==0 ){
							timeSeries.add(selectNPoints(deSolver_.getTimeSeries().copy(), maxPointsPerTraj));
							timeScale.add(selectNPoints(deSolver_.getTimeScale().copy(), maxPointsPerTraj));
						}else{
							timeSeries.set(timeSeries.size()-1, Factory2D.appendRows(timeSeries.get(timeSeries.size()-1), selectNPoints(deSolver_.getTimeSeries().copy(),maxPointsPerTraj)));							
							timeScale.set(timeScale.size()-1, Factory1D.append(timeScale.get(timeScale.size()-1), selectNPoints(deSolver_.getTimeScale().copy(),maxPointsPerTraj)));
						}

					}else{					
						if( previoudTime==0 ){
							timeSeries.add(deSolver_.getTimeSeries().copy());
							timeScale.add(deSolver_.getTimeScale().copy());
						}else{
							timeSeries.set(timeSeries.size()-1, Factory2D.appendRows(timeSeries.get(timeSeries.size()-1), deSolver_.getTimeSeries()));							
							timeScale.set(timeScale.size()-1, Factory1D.append(timeScale.get(timeScale.size()-1), deSolver_.getTimeScale()));
						}
					}


					/** save records **/
					if( its%saveEveryMTraj==0 ){
						saveTmpFile(temptimeSeries, temptimeScale, grn.getId(), tmpfilename, its-saveEveryMTraj, isCutTime, j);
						temptimeSeries.clear();
						temptimeScale.clear();
					}else if( its==numSeries ){
						saveTmpFile(temptimeSeries, temptimeScale, grn.getId(), tmpfilename, ((int)Math.floor(numSeries/saveEveryMTraj))*saveEveryMTraj, isCutTime, j);
						temptimeSeries.clear();
						temptimeScale.clear();
						break;
					}

					/** cumulate time **/
					previoudTime = deSolver_.getTimeScale().get(deSolver_.getTimeScale().size()-1);


				}//end of for

				its++;
			}//end of while

			/** reset initial values **/
			grn.setInitialState(initialX0);

			if( stopRequested == true ){
				System.out.print("The simulation is cancelled. \nThe temporary result is saved at "+tmpfilename.toString()+". \n");
				finalizeAfterFail();
				return;
			}

			/** save for plot **/			
			grn.setLandTimeSeries(timeSeries);
			grn.setLand_itsValue(numSeries);
			grn.setLand_maxTime(maxt);
			grn.setLand_gpdmitsValue(gpdmItsValue);
			grn.setLand_maxExpValue(maxExpValue);
			grn.setLand_isTwoGenes(displayMethod);


//			finalizeAfterSuccess();
			System.out.print("Done!\n"); 
			tmpfilename.delete();			
			dir.delete();
			
		}
		
		private void saveTmpFile(ArrayList<DoubleMatrix2D> temptimeSeries, ArrayList<DoubleMatrix1D> temptimeScale, String id, File tmpfilename, int fileIndex,	boolean isCutTime, int currentSegment) {
			// save time series to file
			try {
				FileWriter fw = new FileWriter(tmpfilename.getAbsolutePath(), true);

				if( isCutTime ){				
					if( currentSegment == 0 ) fw.write(Integer.toString(fileIndex)+"\t"+""+"\n");
					printAll(fw, temptimeSeries.get(0), temptimeScale.get(0));	
				}else
					for(int i=0;i<temptimeSeries.size();i++){
						fw.write(Integer.toString(i+fileIndex)+"\t"+temptimeSeries.get(i).rows()+"\n");
						printAll(fw, temptimeSeries.get(i), temptimeScale.get(i));			
					}

				fw.close();

				//System.out.print("Save the simulated data to "+tmpfilename.toString()+".\n");

			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Cannot open the temporary file!", "Error", JOptionPane.INFORMATION_MESSAGE);
				MsgManager.Messages.errorMessage(e, "Error", "");
				
				finalizeAfterFail();
				this.stop();
			}
			
		}

		private void printAll(FileWriter fw, DoubleMatrix2D timeSeries_, DoubleMatrix1D timeScale_) {

			try {
				printTrajectories(fw, timeSeries_, timeScale_);
			} catch (Exception e) {
				//Log.error("TimeSeriesExperiment", "Failed to write time series to file.", e);
				JOptionPane.showMessageDialog(null, "Failed to write time series to file", "Error", JOptionPane.INFORMATION_MESSAGE);
				MsgManager.Messages.errorMessage(e, "Error", "");
			}
		}

		/** Writes time series data with the time scale to file. */
		private void printTrajectories(FileWriter fw, DoubleMatrix2D timeSeries, DoubleMatrix1D timeScale) {
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

		private File createTmpFile(File saveTempDir, int numTimeSeries, int dimension, int maxt, double noise, String model) {
			String filename = "temp_"+System.currentTimeMillis();
			File fTemp = null;
			try {
				fTemp = File.createTempFile(filename, ".NetLand", saveTempDir);

				//part1: save the complete network in smbl2
				URL url = fTemp.toURI().toURL();
				grn.setId("NetLand_"+grn.getId());
				grn.writeSBML(url);

				//part2: save time series to file
				FileWriter fw = new FileWriter(fTemp.getAbsolutePath(), true);

				//parameters
				fw.write("\n\nLand\t"+numTimeSeries+"\t"+dimension+"\t"+maxt+"\t"+noise+"\t"+model+"\n");
				fw.close();

				return fTemp;
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Cannot create the temporary file!", "Error", JOptionPane.INFORMATION_MESSAGE);
				MsgManager.Messages.errorMessage(e, "Error", "");
			} catch (SBMLException e) {
				JOptionPane.showMessageDialog(null, "Cannot save the SBML model!", "Error", JOptionPane.INFORMATION_MESSAGE);
				MsgManager.Messages.errorMessage(e, "Error", "");
			} catch (XMLStreamException e) {
				JOptionPane.showMessageDialog(null, "Error in saving the SBML model!", "Error", JOptionPane.INFORMATION_MESSAGE);
				MsgManager.Messages.errorMessage(e, "Error", "");
			} catch (ParseException e) {
				JOptionPane.showMessageDialog(null, "Error in saving the SBML model!", "Error", JOptionPane.INFORMATION_MESSAGE);
				MsgManager.Messages.errorMessage(e, "Error", "");
			}

			return fTemp;			
		}

		private DoubleMatrix2D selectNPoints(DoubleMatrix2D timeSeries, int maxPointsPerTraj) {
			int[] index = incrementByN(0, maxPointsPerTraj, timeSeries.rows()-1);

			int[] y = new int[timeSeries.columns()];
			for(int i=0;i<timeSeries.columns();i++)
				y[i] = i;

			return timeSeries.viewSelection(index, y);
		}
		
		private DoubleMatrix1D selectNPoints(DoubleMatrix1D timeScale, int maxPointsPerTraj) {
			int[] index = incrementByN(0, maxPointsPerTraj, timeScale.size()-1);

			return timeScale.viewSelection(index);
		}
		
		private int samplePoints(ODESolver dSolver) {
			ArrayList<Integer> index = new ArrayList<Integer>();
			index.add(0);		
			for(int i=1;i<dSolver.getTimeSeries().rows()-1;i++){
				for(int j=0;j<grn.getSize();j++){
					if(dSolver.getTimeSeries().get(i, j)-dSolver.getTimeSeries().get(i-1, j)>0 && dSolver.getTimeSeries().get(i, j)-dSolver.getTimeSeries().get(i+1, j)>0){
						index.add(i); break;
					}else if(dSolver.getTimeSeries().get(i, j)-dSolver.getTimeSeries().get(i-1, j)==0 && dSolver.getTimeSeries().get(i, j)-dSolver.getTimeSeries().get(i+1, j)!=0){
						index.add(i); break;
					}else if(dSolver.getTimeSeries().get(i, j)-dSolver.getTimeSeries().get(i-1, j)!=0 && dSolver.getTimeSeries().get(i, j)-dSolver.getTimeSeries().get(i+1, j)==0){
						index.add(i); break;
					}
				}			
			}

			if( index.size() == 1 )
				index.add(dSolver.getTimeSeries().rows()-1);
			else{
				index.add(dSolver.getTimeSeries().rows()-2);
				index.add(dSolver.getTimeSeries().rows()-1);
			}
			

			int[] y = new int[grn.getSize()];
			for(int i=0;i<grn.getSize();i++)
				y[i] = i;

			int[] x = new int[index.size()];
			for(int i=0;i<index.size();i++)
				x[i] = index.get(i);

		
			dSolver.setTimeSeries(dSolver.getTimeSeries().viewSelection(x, y).copy());
			dSolver.setTimeScale(dSolver.getTimeScale().viewSelection(x).copy());

			return x.length;
		}
		
//		private int calculateTStep(double memoryForOneTraj, double maxt) {
//			/** get JVM free memory **/
//			MonitorServiceImpl monitorSys = new MonitorServiceImpl();
//			long freeMemory = monitorSys.getTotalFreeMemory(); //monitorSys.getFreeMemory()/1000; //MB
//
//			int t = (int) (freeMemory*0.5/memoryForOneTraj*maxt);
//
//			return t;
//		}

		private int calculateMTraj(double memoryForOneTraj) {
			/** get JVM free memory **/
			MonitorServiceImpl monitorSys = new MonitorServiceImpl();
			long freeMemory = monitorSys.getTotalFreeMemory(); //monitorSys.getFreeMemory()/1000; //MB

			int m = (int) (freeMemory*0.5/memoryForOneTraj);

			return m;
		}
		
		private void memoryWarning(double leastMemoryReq, int maxPointsPerTraj) {
			Object[] options = {"Continue","Cancel"};

			if( maxPointsPerTraj >= 0 ){
				JOptionPane.showMessageDialog(new Frame(), "Not enough memory! \n" +
						"At least "+Double.toString(leastMemoryReq)+" MB is required.\n"+
						"If enough amounts of RAM are installed on this computer, try to run the program \n" +
						"with the command-line argument -XmxXXm to set maximum memory of JVM\n" +
						"Or please reduce the number of simulations and simulation time. \n", "Error", JOptionPane.INFORMATION_MESSAGE);
				finalizeAfterFail();
				stopRequested = true;
				return;		
			}else{	
				int response = JOptionPane.showOptionDialog(null, "Not enough memory to plot the result! Maximumly "+maxPointsPerTraj+" points per trajectory.\n" +
						"At least "+Double.toString(leastMemoryReq)+" MB is required.\n"+
						"If enough amounts of RAM are installed on this computer, try to run the program \n" +
						"with the command-line argument -XmxXXm to set maximum memory of JVM\n" +
						"Or please reduce the number of simulations and simulation time. \n" +
						"If choose to 'continue', "+maxPointsPerTraj+" points per trajectory will be plotted. ","Warning: Out of memory!",JOptionPane.YES_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);	

				if(response==0) {    
					return;
				}else if(response==1) {  
					//cancel
					finalizeAfterFail();
					return;					
				}
			}
		}

		private int calculateMaxPointsPerTraj(double memoryPerPointAllTraj) {
			/** get JVM free memory **/
			MonitorServiceImpl monitorSys = new MonitorServiceImpl();
			long freeMemory = monitorSys.getTotalFreeMemory(); //monitorSys.getFreeMemory()/1000; //MB

			int maxPointsPerTraj = (int) (freeMemory*1.0/memoryPerPointAllTraj);
			return maxPointsPerTraj;		
		}

		private boolean checkMemory(double leastMemoryReq) {
			/** get JVM free memory **/
			MonitorServiceImpl monitorSys = new MonitorServiceImpl();
			long freeMemory = monitorSys.getTotalFreeMemory(); //monitorSys.getFreeMemory()/1000; //MB

			/** check memory **/
			if( leastMemoryReq<0 || freeMemory*0.6<leastMemoryReq )
				return false;	

			return true;
		}
		

		//---------------------------------------------------------------------------------------------------------------------------------
		private void computeGPDMLand(ArrayList<DoubleMatrix2D> timeSeries, ArrayList<DoubleMatrix1D> theta, String[] focusGenesList2) {
			//discrete the final state
			int dimension = grn.getNodes().size();

			//get gene index
			int[] focus_index = new int[focusGenesList.length];
			for(int j=0;j<focusGenesList.length;j++)
				for(int i=0;i<dimension;i++)
					if( grn.getNode(i).getLabel().equals(focusGenesList[j]) )
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


//					grn.setCounts(counts);
//					grn.setSumPara(sumPara);
					grn.setMinX(minX);
					grn.setMaxX(maxX);
					grn.setMinY(minY);
					grn.setMaxY(maxY);
					grn.setN(n);
					grn.setAllX(allX);
					grn.setAllY(allY);
					grn.setLand_isTwoGenes(false);

					fTemp.delete();
					fTempout.delete();
					fTempnew.delete();

					grn.setLand_maxTime(maxTime);
					grn.setLand_itsValue(itsValue);
					grn.setLand_maxExpValue(maxExpValue);
					grn.setLand_focusGenesList(focusGenesList);
					grn.setLand_gpdmitsValue(gpdmItsValue);

					grn.setLandTimeSeries(timeSeries);

					return;
				}//end of exitVal == 0 
			} catch (IOException e) {
				JOptionPane.showMessageDialog(new Frame(), "Cannot create a file!", "Error", JOptionPane.INFORMATION_MESSAGE);
				MsgManager.Messages.errorMessage(e, "Error", "");
				this.stop();
			}	

			if( exitVal == -2 && loadTimeCourse.isSelected() ){
				JOptionPane.showMessageDialog(new Frame(), "Error of Execution! Please run simulations again!", "Runtime exception", JOptionPane.INFORMATION_MESSAGE);
				return;
			}


			//in case of "Matrix non positive definite error"
			while( exitVal == -2 ){ 
				//regenerate traj							
				/** generate time series **/
				//run numSeries_ times				
				timeSeries = new ArrayList<DoubleMatrix2D>();			
				
				boolean isStable = true;
				generateTimeseriesData();												
				
				timeSeries = grn.getLandTimeSeries();

				/** get attractors focusGenes=all **/
				if( timeSeries.size() == 0 ){
					isStable = false;
				}else if( !stopRequested && isStable ){	
					//System.out.print("Calculate attractors...\n");
					if( !checkAttractor(focusGenesList, timeSeries) ){
						isStable = false;
					}
				}
	
				if( !isStable ){
					exitVal = -2;
					continue;
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
					if( !stopRequested )
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
						DoubleMatrix2D allY = new DenseDoubleMatrix2D(nPerDim*nPerDim,grn.getNodes().size());
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


//						grn.setCounts(counts);
//						grn.setSumPara(sumPara);
						grn.setMinX(minX);
						grn.setMaxX(maxX);
						grn.setMinY(minY);
						grn.setMaxY(maxY);
						grn.setN(n);
						grn.setAllX(allX);
						grn.setAllY(allY);
						grn.setLand_isTwoGenes(false);

						fTemp.delete();
						fTempout.delete();
						fTempnew.delete();

						grn.setLand_maxTime(maxTime);
						grn.setLand_itsValue(itsValue);
						grn.setLand_maxExpValue(maxExpValue);
						grn.setLand_focusGenesList(focusGenesList);
						grn.setLand_gpdmitsValue(gpdmItsValue);

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
					//System.out.println(msg); 
					System.out.print('.');
					if( msg.equals("Press enter for more.") ){
						p.destroy();
						return -2;
					}
					checkForInterruption_process(p);
				}

				exitVal = p.waitFor();
				System.out.print('\n');
			} catch (Exception e) {		
				if( e.getMessage().equals("\nSimulation is canceled!") ){
					snake_.stop();
					myCardLayout_.show(runButtonAndSnakePanel_, runPanel_.getName());
					return -1;
				}else{
					snake_.stop();
					myCardLayout_.show(runButtonAndSnakePanel_, runPanel_.getName());
					stopRequested = true;
					JOptionPane.showMessageDialog(null, "Error in the execution of GPDM!", "Error", JOptionPane.INFORMATION_MESSAGE);
					MsgManager.Messages.errorMessage(e, "Error", "");
					System.out.print("Generation stopped!\n");
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

			// Create a new file	
			FileWriter fw = new FileWriter(filename.getPath());   

			//cannot take all data
			//int n1 = its/30; total 30 trajectories are selected
			int n1 = 10; //default 10 traj per attractor

			//define the num of traj for each attractor; thus we should sample enough items from each attractor!!!
			int[] nTrajPerAttr = new int[grn.getCounts().length];
			int minCounts = 1000000;
			for(int i=0;i<grn.getCounts().length;i++)
				if( minCounts>grn.getCounts()[i] )
					minCounts = grn.getCounts()[i];

			if( minCounts>n1 )
				for(int i=0;i<grn.getCounts().length;i++)
					nTrajPerAttr[i] = n1;
			else
				for(int i=0;i<grn.getCounts().length;i++)
					if( grn.getCounts()[i]<n1 )
						nTrajPerAttr[i] = grn.getCounts()[i];
					else
						nTrajPerAttr[i] = n1;


			int lines = 0; 
			//write trajectories
			for(int i=0;i<grn.getCounts().length;i++){	
				for(int jj=0;jj<nTrajPerAttr[i];jj++){
					DoubleMatrix2D tempX0 =  timeSeries.get(labeledSeries.get(i)[jj]);	
					int len = tempX0.rows();
//					int interval = len<100?1:len/100;
//					int numItem = (len-1)/interval+1;
					int[] indexes;
					if( len>100 )
						indexes = incrementByN(0, 20, len-1);
					else
						indexes = incrementByN(0, len, len-1);

					//					//take the last 10
					//					for(int j=tempX0.rows()-10;j<tempX0.rows();j+=1){ //rows: time length
					//						String temp = "0";
					//						for(int k=1;k<=tempX0.columns();k++)
					//							temp += " "+k+":"+tempX0.get(j, k-1);
					//						fw.write(temp+"\n");
					//						lines++;
					//					}

					for(int j=0;j<indexes.length;j++){ //rows: time length
						String temp = "0";
						//for(int k=1;k<=tempX0.columns();k++)
						for(int k=1;k<=focus_index.length;k++){
							if( k==5 ){ //try gj
								temp += " "+k+":80";
							}else{
								temp += " "+k+":"+tempX0.get(indexes[j], focus_index[k-1]);			
							}
						}
						fw.write(temp+"\n");
						lines++;
					}
					
//					for(int j=0;j<tempX0.rows();j+=interval){ //rows: time length
//						String temp = "0";
//						//for(int k=1;k<=tempX0.columns();k++)
//						for(int k=1;k<=focus_index.length;k++)
//							temp += " "+k+":"+tempX0.get(j, focus_index[k-1]);
//						fw.write(temp+"\n");
//						lines++;
//					}
					//System.out.print(lines+"\n"); 
				}
			}

			//write origAllY
			DoubleMatrix2D origAllY = new DenseDoubleMatrix2D(lines, grn.getNodes().size());
			lines = 0;
			for(int i=0;i<grn.getCounts().length;i++){	
				for(int jj=0;jj<nTrajPerAttr[i];jj++){
					DoubleMatrix2D tempX0 =  timeSeries.get(labeledSeries.get(i)[jj]);	
					int len = tempX0.rows();
//					int interval = len<100?1:len/10;
//					int numItem = (len-1)/interval+1;
					int[] indexes;
					if( len>100 )
						indexes = incrementByN(0, 20, len-1);
					else
						indexes = incrementByN(0, len, len-1);

					//					//take the last 10
					//					for(int j=tempX0.rows()-10;j<tempX0.rows();j+=1){ //rows: time length
					//						for(int k=1;k<=tempX0.columns();k++)
					//							origAllY.set(lines, k-1, tempX0.get(j, k-1));
					//						lines++;
					//					}


					for(int j=0;j<indexes.length;j++){ //rows: time length
						for(int k=1;k<=focus_index.length;k++){
							origAllY.set(lines, k-1, tempX0.get(indexes[j], focus_index[k-1]));
						}
						lines++;
					}
					
//					for(int j=0;j<tempX0.rows();j+=interval){
//						for(int k=1;k<=tempX0.columns();k++)
//							origAllY.set(lines, k-1, tempX0.get(j, k-1));
//						lines++;
//					}
				}
			}
			grn.setOrigAllY(origAllY);

			fw.close();   

			return lines;
		}

		public int[] incrementByN(int start, int n, int end) {
			double aStep = (end-start)*1.0/n;

			int length = (int) (Math.rint((end-start)/aStep)+1);
			int[] temp=new int[length];
			for (int i = 0; i < temp.length; i++){
				double t = (Math.rint(1000*(start+aStep*i)))/1000.0;
				temp[i] = (int) Math.rint(t);
			}
			
			return temp;
		}
		
		//-------------------------------------------------------------------------------------------------------------------------
		private boolean checkAttractor(String[] focusGenes, ArrayList<DoubleMatrix2D> timeSeries){
			//discrete the final state
			int dimension = grn.getNodes().size();

			//get gene index
			int[] focus_index = new int[focusGenes.length];
			for(int j=0;j<focusGenes.length;j++)
				for(int i=0;i<dimension;i++)
					if( grn.getNode(i).getLabel().equals(focusGenes[j]) )
						focus_index[j] = i;

			//////old
			//double check distances between attractors	
			String out =  calculateDistances(timeSeries, focus_index, dimension);
			///old
			
			if( out.equals("ok") ){						
				return true;
			}

			return false;
			
		}

		
		//0 no stable state   1 one possible steady state  2 steady state exists
		private String calculateSteadyStates(int[] focus_index, int[] isConverge){	
			ArrayList<DoubleMatrix2D> timeSeries = grn.getLandTimeSeries();
			int its = timeSeries.size();

			//discrete the final state
			int dimension = grn.getNodes().size();

			//double check distances between attractors			
			//solver equations
			List<String> solverResults_focusgenes = new ArrayList<String>();
//			ArrayList<double[]> attractorTypes_focusgene = new ArrayList<double[]>();

		
			int allNumConverge = 0;
			//at the same time, check if trajectories are stable
			for(int i=0;i<its;i++){		
				isConverge[i] = 0; //0: converge 1:not converge

				DoubleMatrix1D tempX0 =  timeSeries.get(i).viewRow(timeSeries.get(i).rows()-1);
				DoubleMatrix1D tempX1 =  timeSeries.get(i).viewRow(timeSeries.get(i).rows()-2);
				cern.jet.math.Functions F = cern.jet.math.Functions.functions;	
				double dis = tempX0.aggregate(tempX1, F.plus, F.chain(F.square,F.minus))/dimension;

				
				if( dis>0.000001 ){
					isConverge[i] = 1;
					continue;
					//return "notStable"; //precision 0.000001
				}
				

				nonlinearEq a = new nonlinearEq(grn);
				DoubleMatrix1D tempY = a.runSolver(tempX0,grn);
				if( tempY == null ){
					isConverge[i] = 2;
					continue;
				}

				allNumConverge++;
				
				String temp1 = "";
				for(int j=0;j<focus_index.length;j++){		
					double temp = Math.floor(100*tempY.get(focus_index[j]))/100;
					temp1 += temp+";" ;
				}
				solverResults_focusgenes.add(temp1);	
			}
			
			
			if( allNumConverge == 0 )
				return "Not stable";
			
			//remove duplicates
			List<String> uniqueList_focusgene = new ArrayList<String>(new HashSet<String>(solverResults_focusgenes));
			
			//distance matrix
			DoubleMatrix2D attractorTypes_focusgene_input = new DenseDoubleMatrix2D(uniqueList_focusgene.size(),dimension);
			for(int i=0;i<uniqueList_focusgene.size();i++){
				for(int j=0;j<dimension;j++)
					attractorTypes_focusgene_input.set(i, j, Double.parseDouble(uniqueList_focusgene.get(i).split(";")[j]));
			}
			solverResults_focusgenes = null;
			uniqueList_focusgene = null;

			/** high iterations may cause the OutOfMemoryError **/
			ArrayList<Integer> output = Landscape.calculateDisMatrix(attractorTypes_focusgene_input);

			Collections.sort(output);
			//remove i or j
			for(int i=output.size()-1;i>=0;i--){
				attractorTypes_focusgene_input = ColtUtils.deleteRow(attractorTypes_focusgene_input, output.get(i));
			}
			output = null;

			//calculate para
			DoubleMatrix2D sumPara = new DenseDoubleMatrix2D( attractorTypes_focusgene_input.rows(), dimension*2);
			int[] counts = new int[attractorTypes_focusgene_input.size()];
			ArrayList<int[]> labeledSeries = new ArrayList<int[]>(attractorTypes_focusgene_input.size());
			sumPara.assign(0);

			int temp[][] = new int[counts.length][its];
			cern.jet.math.Functions F = cern.jet.math.Functions.functions;	
			for(int i=0;i<its;i++){
				if( isConverge[i] == 0 ){
					DoubleMatrix1D tempX0 =  timeSeries.get(i).viewRow(timeSeries.get(i).rows()-1);

					//generate current vector
					DoubleMatrix1D temp1 = new DenseDoubleMatrix1D(focus_index.length);
					for(int j=0;j<focus_index.length;j++)
						temp1.set(j, Math.floor(100*tempX0.get(focus_index[j]))/100);

					//close to which attractor
					double[] tempSum = new double[sumPara.rows()]; 
					for(int k=0;k<attractorTypes_focusgene_input.rows();k++)
						tempSum[k] =  temp1.aggregate(attractorTypes_focusgene_input.viewRow(k), F.plus, F.chain(F.square,F.minus));


					//find the minimal distance
					int flag = -1; double minimal = 100000;
					for(int j=0;j<tempSum.length;j++)
						if(tempSum[j]<minimal){flag=j;minimal=tempSum[j];}

					counts[flag] += 1;
					temp[flag][counts[flag]-1] = i;

					for(int j=0;j<dimension;j++)
						sumPara.set(flag, j, sumPara.get(flag, j) + tempX0.get(j));

					for(int j=dimension;j<2*dimension;j++)
						sumPara.set(flag, j, sumPara.get(flag, j) + 0.03);

					//not converged yet, but will converge to a steady state
				}else if( isConverge[i] == 1 ){


					//cannot converge to a steady state
				}else if( isConverge[i] == 2 ){

				}
			}
			//-------------------

			for(int i=0;i<counts.length;i++)
				labeledSeries.add(temp[i]);

			for(int j=0;j<attractorTypes_focusgene_input.rows();j++)
				Transform.div(sumPara.viewRow(j), counts[j]);

			grn.setCounts(counts);
			grn.setSumPara(sumPara);
			
			
			return "ok";
		}

		private String calculateDistances(ArrayList<DoubleMatrix2D> timeSeries, int[] focus_index, int dimension) {	
			
			int its = timeSeries.size();


			//solver equations
			List<String> solverResults_focusgenes = new ArrayList<String>();
			
			//java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");  
			int i = 0;
			while( !stopRequested && i<itsValue ){	
				DoubleMatrix1D tempX0 =  timeSeries.get(i).viewRow(timeSeries.get(i).rows()-1);

				cern.jet.math.Functions F = cern.jet.math.Functions.functions;	
				if( this.displayMethod ){					
					DoubleMatrix1D tempX1 =  timeSeries.get(i).viewRow(timeSeries.get(i).rows()-2);					
					double dis = tempX0.aggregate(tempX1, F.plus, F.chain(F.square,F.minus));
					if( dis>tempX1.aggregate(F.plus,F.identity)/dimension ) // 
						return "notStable";
				}
				
				nonlinearEq a = new nonlinearEq(grn);
				DoubleMatrix1D tempY = a.runSolver(tempX0,grn);
				if( tempY == null ) 
					return "notStable";

				
				//judge if the stable state is far from the end position	
				double dis =  Math.sqrt(tempX0.aggregate(tempY, F.plus, F.chain(F.square,F.minus)))/dimension;
				if( dis>tempX0.aggregate(F.plus,F.identity)/dimension ) 
					return "notStable"; ////ad hoc	

				String temp1 = "";
				for(int j=0;j<focus_index.length;j++){				
					double temp = Math.floor(100*tempY.get(focus_index[j]))/100;
					temp1 += temp+";" ;
				}
				solverResults_focusgenes.add(temp1);

				i++;
			}

			//remove duplicates
			List<String> uniqueList_focusgene = new ArrayList<String>(new HashSet<String>(solverResults_focusgenes));

			//distance matrix
			DoubleMatrix2D attractorTypes_focusgene_input = new DenseDoubleMatrix2D(uniqueList_focusgene.size(),dimension);
			for(i=0;i<uniqueList_focusgene.size();i++){
				for(int j=0;j<dimension;j++)
					attractorTypes_focusgene_input.set(i, j, Double.parseDouble(uniqueList_focusgene.get(i).split(";")[j]));
			}

		
			solverResults_focusgenes = null;
			uniqueList_focusgene = null;
			

			/** high iterations may cause the OutOfMemoryError **/
			ArrayList<Integer> output = calculateDisMatrix(attractorTypes_focusgene_input);
			Collections.sort(output);
	
			//remove i or j
			for(i=output.size()-1;i>=0;i--){
//				solverResults_focusgenes.remove((int)output.get(i));
				attractorTypes_focusgene_input = ColtUtils.deleteRow(attractorTypes_focusgene_input, output.get(i));
			}
			output = null;
			

			//calculate para
			DoubleMatrix2D sumPara = new DenseDoubleMatrix2D( attractorTypes_focusgene_input.rows(), dimension*2);
			int[] counts = new int[attractorTypes_focusgene_input.rows()];
			labeledSeries = new ArrayList<int[]>(attractorTypes_focusgene_input.rows());
			sumPara.assign(0);

			int temp[][] = new int[counts.length][its];
			cern.jet.math.Functions F = cern.jet.math.Functions.functions;	
			for(i=0;i<itsValue;i++){
				DoubleMatrix1D tempX0 =  timeSeries.get(i).viewRow(timeSeries.get(i).rows()-1);

				//generate current vector
				DoubleMatrix1D temp1 = new DenseDoubleMatrix1D(focus_index.length);
				for(int j=0;j<focus_index.length;j++)
					temp1.set(j, Math.floor(100*tempX0.get(focus_index[j]))/100);

				//close to which attractor
				double[] tempSum = new double[attractorTypes_focusgene_input.rows()]; 
				for(int k=0;k<attractorTypes_focusgene_input.rows();k++)
					tempSum[k] =  temp1.aggregate(attractorTypes_focusgene_input.viewRow(k), F.plus, F.chain(F.square,F.minus));


				//find the minimal distance
				int flag = -1; double minimal = 100000000;
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
			
			//remove counts==0
			int length = counts.length;
			for(int j=0;j<counts.length;j++)
				if( counts[j] == 0 )
					length--;
				else
					Transform.div(sumPara.viewRow(j), counts[j]);
			
			
			this.sumPara = new DenseDoubleMatrix2D( length, dimension*2);
			this.counts = new int[length];

			int index=0;
			for(int j=0;j<counts.length;j++)
				if( counts[j] != 0 ){
					this.counts[index] = counts[j];
					
					for(int jj=0;jj<dimension;jj++)
						this.sumPara.set(index, jj, sumPara.get(j, jj) );

					for(int jj=dimension;jj<2*dimension;jj++)
						this.sumPara.set(index, jj, 0.03);
					
					index++;
				}

			
			grn.setCounts(this.counts);
			grn.setSumPara(this.sumPara);


			return "ok";
		}


		// ----------------------------------------------------------------------------

		public void finalizeAfterSuccess() 
		{
			snake_.stop();
			myCardLayout_.show(runButtonAndSnakePanel_, runPanel_.getName());
			dispose();

			LandscapePanel onea = new LandscapePanel(grn, displayMethod);	

			querySaveLand();
			
			//System.out.print("land end: "+System.currentTimeMillis()+"\n"); 
		}

		private void querySaveLand() {
			JFrame frame = new JFrame();
			int n = JOptionPane.showConfirmDialog(frame, "Would you like to save the result to a file?", "Export landscape result", JOptionPane.YES_NO_OPTION);
			if (n == JOptionPane.YES_OPTION) {
				JFileChooser fc = new JFileChooser();
				int retVal = fc.showSaveDialog(frame);                 // let frame to become a showDialog
				if (retVal == JFileChooser.APPROVE_OPTION) {
					File resultFile = fc.getSelectedFile();   // where is the selected file	
					saveLand(resultFile.toString());
				} else {
					System.out.println("Cancelled by user!");
				}
			} else if (n == JOptionPane.NO_OPTION) {
				System.out.println("Don't save it!");
			}		

		}

		private void saveLand(String outputPath){
			//first part: SBML
			grn.setId("NetLand_"+grn.getId());
			String outputNewPointName = "temp_"+System.currentTimeMillis();
			String temppath = System.getProperty("java.io.tmpdir");
			File fnew = new File(temppath);

			try {
				File fTempnew = File.createTempFile(outputNewPointName, ".sbml", fnew);			
				URL url = fTempnew.toURI().toURL();
				grn.writeSBML(url);	

				//second part: land info	
				String outputNewPointName1 = "temp_"+System.currentTimeMillis();
				String temppath1 = System.getProperty("java.io.tmpdir");
				File fnew1 = new File(temppath1);
				File fTempnew1 = File.createTempFile(outputNewPointName1, ".landscape", fnew1);
				FileWriter fw = new FileWriter(fTempnew1.getPath(), false);

				fw.write("\n\nLand\t"+grn.getMaxX()+"\t"+grn.getMinX()+"\t"+grn.getMaxY()+"\t"+grn.getMinY()+"\n");


				//write parameters
				fw.write(grn.getSize()+"\t"+maxExpValue+"\t"+itsValue+"\t"+maxTime+"\t"+displayMethod+"\t"+gpdmIts.getText()+"\n");

				for(int i=0;i<focusGenesList.length;i++)
					fw.write(focusGenesList[i]+"\t");
				fw.write("\n");

				//write x
				for(int i=0;i<grn.getX().size();i++)
					fw.write(grn.getX().get(i)+"\t");
				fw.write("\n");

				//write y
				for(int i=0;i<grn.getY().size();i++)
					fw.write(grn.getY().get(i)+"\t");
				fw.write("\n");

				//write counts
				for(int i=0;i<grn.getCounts().length;i++)
					fw.write(grn.getCounts()[i]+"\t");
				fw.write("\n");


				//write sumpara
				for(int i=0;i<grn.getSumPara().rows();i++){
					for(int j=0;j<grn.getSumPara().columns();j++)
						fw.write(grn.getSumPara().get(i,j)+"\t");
					fw.write("\n");
				}

				//write allY for gpdm				
				if( !displayMethod ){//only for gpdm
					fw.write(grn.getAllY().rows()+"\t"+grn.getAllY().columns()+"\n");

					for(int i=0;i<grn.getAllY().rows();i++){
						for(int j=0;j<grn.getAllY().columns();j++)
							fw.write(grn.getAllY().get(i, j)+"\t");
						fw.write("\n");
					}
				}


				//write size of griddata
				fw.write(grn.getGridData().rows()+"\t"+grn.getGridData().columns()+"\n");

				//write griddata
				for(int i=0;i<grn.getGridData().rows();i++){
					for(int j=0;j<grn.getGridData().columns();j++)
						fw.write(grn.getGridData().get(i, j)+"\t");
					fw.write("\n");
				}

				//write timeseries data
				for(int k=0;k<grn.getLandTimeSeries().size();k++){
					fw.write(grn.getLandTimeSeries().get(k).rows()+"\n");
					for(int i=0;i<grn.getLandTimeSeries().get(k).rows();i++){
						for(int j=0;j<grn.getLandTimeSeries().get(k).columns();j++)
							fw.write(grn.getLandTimeSeries().get(k).get(i, j)+"\t");
						fw.write("\n");
					}
				}


				if( !displayMethod ){
					//write origAllY
					fw.write(grn.getOrigAllY().rows()+"\t"+grn.getOrigAllY().columns()+"\n");

					for(int i=0;i<grn.getOrigAllY().rows();i++){
						for(int j=0;j<grn.getOrigAllY().columns();j++)
							fw.write(grn.getOrigAllY().get(i, j)+"\t");
						fw.write("\n");
					}
				}



				fw.close();

				//merge			
				mergeFiles(outputPath, new String[]{fTempnew.getAbsolutePath(), fTempnew1.getAbsolutePath()});

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


	public static ArrayList<Integer> calculateDisMatrix(DoubleMatrix2D matrix) {
		ArrayList<Integer> output = new ArrayList<Integer>();
		matrix = ColtUtils.transpose(matrix);
		DoubleMatrix2D a = Statistic.distance(matrix, Statistic.EUCLID);

		double threshold = ColtUtils.sum(a)/a.size();;
				
		for(int i=0;i<a.rows()-1;i++)
			for(int j=i+1;j<a.columns();j++)
				if( a.get(i, j)<threshold ){
					if( !output.contains(j) )
						output.add(j);
				}

		return output;
	}



}


