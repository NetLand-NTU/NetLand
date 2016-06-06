/*
Copyright (c) 2008-2010 Daniel Marbach & Thomas Schaffter

We release this software open source under an MIT license (see below). If this
software was useful for your scientific work, please cite our paper(s) listed
on http://gnw.sourceforge.net.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */

package Widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import org.apache.commons.math.ConvergenceException;
import org.math.plot.Plot2DPanel;
import org.sbml.jsbml.SBMLException;
import org.systemsbiology.chem.IModelBuilder;
import org.systemsbiology.chem.Model;
import org.systemsbiology.chem.ModelBuilderCommandLanguage;
import org.systemsbiology.chem.SimulationResults;
import org.systemsbiology.chem.SimulatorDeterministicRungeKuttaAdaptive;
import org.systemsbiology.chem.SimulatorParameters;
import org.systemsbiology.chem.SimulatorStochasticGillespie;
import org.systemsbiology.math.AccuracyException;
import org.systemsbiology.util.DataNotFoundException;
import org.systemsbiology.util.IncludeHandler;
import org.systemsbiology.util.InvalidInputException;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.doublealgo.Transform;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import ch.epfl.lis.gnw.CancelException;
import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.gnw.GnwSettings;
import ch.epfl.lis.gnwgui.DynamicalModelElement;
import ch.epfl.lis.gnwgui.NetworkElement;
import edu.umbc.cs.maple.utils.ColtUtils;
import FileManager.FileChooser;
import LandscapeDisplay.ODESolver;
import LandscapeDisplay.SDESolver;
import LandscapeDisplay.SDETimeSeriesExperiment;
import LandscapeDisplay.nonlinearEq;
import Widgets.SimulationWindow;
import WidgetsTables.AttractorTable;
import WidgetsTables.SpeciesTable;
import WindowGUI.NetLand;



public class Simulation extends SimulationWindow {

	/** Serialization */
	private static final long serialVersionUID = 1L;

	/** NetworkItem to simulate */
	private NetworkElement item_ = null;

	/** random initial boundaries **/
	private double upbound;
	private double lowbound;
	private SimulationThread simulation = null;

	// ============================================================================
	// PUBLIC METHODS

	/**
	 * Constructor
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Simulation(final JFrame aFrame, NetworkElement item) {
		super(aFrame);		
		item_ = item;

		//closing listener
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent windowEvent){
				if( simulation != null && simulation.myThread_.isAlive() ){
					simulation.stop();	
					System.out.print("Simulation is canceled.");
					JOptionPane.showMessageDialog(new Frame(), "Simulation is canceled.", "Warning!", JOptionPane.INFORMATION_MESSAGE);
				}
				escapeAction();
			}
		});

		// Model
		model_.setModel(new DefaultComboBoxModel(new String[] {"Deterministic Model (ODEs)", "Stochastic Model (SDEs)", "Stochastic Simulation (Gillespie Algorithm)"}));
		model_.setSelectedIndex(0);


		setModelAction();


		//set plot part
		//display result
		final GeneNetwork grn = ((DynamicalModelElement) item).getGeneNetwork();
		if( grn.getTimeScale().size()>=1 ){
			//update parameters
			numTimeSeries_.setValue(grn.getTraj_itsValue());
			tmax_.setValue(grn.getTraj_maxTime());
			sdeDiffusionCoeff_.setValue(grn.getTraj_noise());

			if( grn.getTraj_model().equals("ode") )
				model_.setSelectedIndex(0);
			else if( grn.getTraj_model().equals("sde") )
				model_.setSelectedIndex(1);
			else
				model_.setSelectedIndex(2);

			//update plot
			trajPlot.removeAll();
			trajPlot.add(trajectoryTabb());
			trajPlot.updateUI();
			trajPlot.setVisible(true);	
			trajPlot.repaint();

			analyzeResult.setVisible(true);
		}

		/**
		 * ACTIONS
		 */

		model_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setModelAction();
			}
		});

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

		analyzeResult.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				final JDialog a = new JDialog();
				a.setSize(new Dimension(500,450));
				a.setModal(true);
				a.setTitle("Gene List (seperated by ';')");
				a.setLocationRelativeTo(null);

				final JTextArea focusGenesArea = new JTextArea();
				focusGenesArea.setLineWrap(true);
				focusGenesArea.setEditable(false);
				focusGenesArea.setRows(3);

				String geneNames = "";
				for(int i=0;i<grn.getNodes().size();i++)
					geneNames += grn.getNodes().get(i).getLabel()+";";				
				focusGenesArea.setText(geneNames);

				JButton submitButton = new JButton("Submit");
				JButton cancelButton = new JButton("Cancel");
				JPanel buttonPanel = new JPanel();
				buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
				buttonPanel.add(submitButton); buttonPanel.add(cancelButton);

				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent arg0) {
						a.dispose();
					}
				});

				submitButton.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent arg0) {
						a.dispose();
						final JDialog a = new JDialog();
						a.setSize(new Dimension(500,450));
						a.setModal(true);
						a.setTitle("Statistics");
						a.setLocationRelativeTo(null);

						GeneNetwork grn = ((DynamicalModelElement)item_).getGeneNetwork();

						if( grn.getTimeSeries().isEmpty() ){
							JOptionPane.showMessageDialog(new Frame(), "Please run the simulation first.", "Warning!", JOptionPane.INFORMATION_MESSAGE);
						}else{
							JPanel infoPanel = new JPanel();
							infoPanel.setLayout(new BorderLayout());
							

							//output 
							String[] focusGenes = focusGenesArea.getText().split(";");;
							String content = "";


							//discrete the final state
							int dimension = grn.getNodes().size();

							//get gene index
							int[] focus_index = new int[focusGenes.length];
							for(int j=0;j<focusGenes.length;j++)
								for(int i=0;i<dimension;i++)
									if( grn.getNode(i).getLabel().equals(focusGenes[j]) )
										focus_index[j] = i;

							
							JScrollPane jsp = new JScrollPane();
							//calculate steady states		
							grn.setLand_itsValue((Integer) numTimeSeries_.getModel().getValue());
							String out = calculateSteadyStates(grn, focusGenes, focus_index);
							
							if( out.equals("ok") ){		
								AttractorTable panel = new AttractorTable(grn, focusGenes);
								jsp.setViewportView(panel);
							}else if( out.equals("notStable") )
								content += "Cannot find a steady state!";
							else //if( out.equals("negativeSolution") )
								content += "\nCannot find a steady state!";
//							else
//								content += "\nOne possible steady state is "+out;

							if( content != "" ){
								JLabel warningLabel = new JLabel();
								warningLabel.setText(content);
								jsp.setViewportView(warningLabel);
							}
							
							grn.setSumPara(null);
							grn.setCounts(null);

							
							
							//jsp.setPreferredSize(new Dimension(280,130));
							jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
							jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
							infoPanel.add(jsp, BorderLayout.CENTER);

							a.add(infoPanel);
							a.setVisible(true);	
						}//end of else
					}

				});


				JPanel options3 = new JPanel();
				options3.setLayout(new BorderLayout());
				options3.add(focusGenesArea, BorderLayout.NORTH);
				options3.add(buttonPanel);

				options3.setBorder(new EmptyBorder(5, 0, 5, 0));

				a.add(options3);
				a.setVisible(true);	
			}
		});



		runButton_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				enterAction(item_);
			}
		});

		cancelButton_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				if( simulation != null )
					simulation.stop();
			}
		});


		fixButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				JDialog a = new JDialog();
				a.setTitle("Fixed initial values");
				a.setSize(new Dimension(400,400));
				a.setLocationRelativeTo(null);

				//				JLabel noteLabel = new JLabel();
				//				noteLabel.setText("Please set the initial values of species in the main panel!");

				JPanel speciesPanel = new JPanel();
				String[] columnName = {"Name", "InitialValue"};
				boolean editable = true; //false;
				new SpeciesTable(speciesPanel, columnName, ((DynamicalModelElement)item_).getGeneNetwork(), editable);

				/** LAYOUT **/
				JPanel wholePanel = new JPanel();
				//				noteLabel.setBackground(new Color(187,207,232));
				//				speciesPanel.setBackground(new Color(187,207,232));
				//				wholePanel.setBackground(new Color(187,207,232));

				wholePanel.setLayout(new BoxLayout(wholePanel, BoxLayout.Y_AXIS));
				//				wholePanel.add(noteLabel);
				wholePanel.add(speciesPanel);

				a.add(wholePanel);
				a.setModal(true);
				a.setVisible(true);			
			}
		});


		randomButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				final JDialog a = new JDialog();
				a.setTitle("Set boundaries");
				a.setSize(new Dimension(300,200));
				a.setModal(true);
				a.setLocationRelativeTo(null);

				JPanel upPanel = new JPanel();
				JLabel upLabel = new JLabel("Input the upper boundary: ");
				final JTextField upValue = new JTextField("3");
				upPanel.setLayout(new BoxLayout(upPanel, BoxLayout.X_AXIS));
				upPanel.add(upLabel);upPanel.add(upValue);

				JPanel lowPanel = new JPanel();
				JLabel lowLabel = new JLabel("Input the lower boundary: ");
				final JTextField lowValue = new JTextField("0");
				lowPanel.setLayout(new BoxLayout(lowPanel, BoxLayout.X_AXIS));
				lowPanel.add(lowLabel);lowPanel.add(lowValue);

				JPanel buttonPanel = new JPanel();
				JButton submit = new JButton("Submit");  
				JButton cancel = new JButton("Cancel");
				buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
				buttonPanel.add(submit);buttonPanel.add(cancel);
				buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

				submit.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						if( upValue.getText().equals("") )
							JOptionPane.showMessageDialog(null, "Please input upper boundary", "Error", JOptionPane.ERROR_MESSAGE);
						else{
							try{
								upbound = Double.parseDouble(upValue.getText());

								if( lowValue.getText().equals("") )
									JOptionPane.showMessageDialog(null, "Please input lower boundary", "Error", JOptionPane.ERROR_MESSAGE);
								else{
									try{
										lowbound = Double.parseDouble(lowValue.getText());

										if( upbound < lowbound )
											JOptionPane.showMessageDialog(null, "Upper boundary should be not less than lower boundary", "Error", JOptionPane.ERROR_MESSAGE);
										else
											a.dispose();
									}catch(Exception er){
										JOptionPane.showMessageDialog(null, "Invalid value", "Error", JOptionPane.INFORMATION_MESSAGE);
										MsgManager.Messages.errorMessage(er, "Error", "");
									}
								}
							}catch(Exception er){
								JOptionPane.showMessageDialog(null, "Invalid value", "Error", JOptionPane.INFORMATION_MESSAGE);
								MsgManager.Messages.errorMessage(er, "Error", "");
							}
						}				

					}
				});

				cancel.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						JOptionPane.showMessageDialog(null, "The default values are used!", "", JOptionPane.INFORMATION_MESSAGE);
						a.dispose();
					}
				});



				JPanel wholePanel = new JPanel();
				wholePanel.setLayout(new BoxLayout(wholePanel, BoxLayout.Y_AXIS));
				wholePanel.add(upPanel);
				wholePanel.add(lowPanel); 
				wholePanel.add(buttonPanel);
				wholePanel.setBorder(new EmptyBorder(5, 5, 5, 5));

				a.add(wholePanel);	
				a.setVisible(true);

			}
		});
	}

	//0 no stable state   1 one possible steady state  2 steady state exists
	private String calculateSteadyStates(GeneNetwork grn_, String[] focusGenes, int[] focus_index){	
		ArrayList<DoubleMatrix2D> timeSeries = grn_.getTimeSeries();
		int its = timeSeries.size();
		
		//discrete the final state
		int dimension = grn_.getNodes().size();

		//double check distances between attractors			
		//solver equations
		List<String> solverResults_focusgenes = new ArrayList<String>(its);
		DoubleMatrix2D attractorTypes_focusgene = new DenseDoubleMatrix2D(its, focus_index.length);
		ArrayList<DoubleMatrix1D> attractorTypesAll = new ArrayList<DoubleMatrix1D>(its);

		//java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");  
		//at the same time, check if trajectories are stable
		for(int i=0;i<its;i++){		
			DoubleMatrix1D tempX0 =  timeSeries.get(i).viewRow(timeSeries.get(i).rows()-1);
			DoubleMatrix1D tempX1 =  timeSeries.get(i).viewRow(timeSeries.get(i).rows()-2);
			cern.jet.math.Functions F = cern.jet.math.Functions.functions;	
			double dis = tempX0.aggregate(tempX1, F.plus, F.chain(F.square,F.minus));
			
			if( model_.getSelectedIndex() == 1 ){
				double noise = Math.pow((Double) sdeDiffusionCoeff_.getModel().getValue(), 2);
				if( dis>noise ) return "notStable";
			}else if( model_.getSelectedIndex() == 0 ){
				if( dis>0.000001 ) return "notStable";
			}
				
			
			nonlinearEq a = new nonlinearEq(grn_);
			DoubleMatrix1D tempY = a.runSolver(tempX0,grn_);
			if( tempY == null ) return "notStable";

			//judge if the stable state is far from the end position
//			cern.jet.math.Functions F = cern.jet.math.Functions.functions;	
			dis = tempX0.aggregate(tempY, F.plus, F.chain(F.square,F.minus));
			if( dis>500 ) return tempY.toString();			 //ad hoc

			attractorTypesAll.add(tempY);

			String temp1 = "";
			for(int j=0;j<focus_index.length;j++){		
				if( tempY.get(focus_index[j])<-1E10 ) return "negativeSolution";
				double temp = Math.floor(100*tempY.get(focus_index[j]))/100;
				temp1 += temp+";" ;
				attractorTypes_focusgene.set(i, j, temp);
			}
			solverResults_focusgenes.add(temp1);	
		}

		//distance matrix
		double threshold = 0.1;
		ArrayList<Integer> output = Landscape.calculateDisMatrix(attractorTypes_focusgene, threshold);

		Collections.sort(output);
		//remove i or j
		for(int i=output.size()-1;i>=0;i--){
			solverResults_focusgenes.remove((int)output.get(i));
			attractorTypes_focusgene = ColtUtils.deleteRow(attractorTypes_focusgene, output.get(i));
		}

		//remove duplicates
		List<String> uniqueList_focusgene = new ArrayList<String>(new HashSet<String>(solverResults_focusgenes));

		//calculate para
		DoubleMatrix2D sumPara = new DenseDoubleMatrix2D( uniqueList_focusgene.size(), dimension*2);
		int[] counts = new int[uniqueList_focusgene.size()];
		ArrayList<int[]> labeledSeries = new ArrayList<int[]>(uniqueList_focusgene.size());
		sumPara.assign(0);

		int temp[][] = new int[counts.length][its];
		cern.jet.math.Functions F = cern.jet.math.Functions.functions;	
		for(int i=0;i<its;i++){
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
				sumPara.set(flag, j, sumPara.get(flag, j) + 0.03);
		}
		//-------------------

		for(int i=0;i<counts.length;i++)
			labeledSeries.add(temp[i]);

		for(int j=0;j<uniqueList_focusgene.size();j++)
			Transform.div(sumPara.viewRow(j), counts[j]);

		grn_.setCounts(counts);
		grn_.setSumPara(sumPara);

		return "ok";
	}




	protected JPanel trajectoryTabb(){
		JPanel trajectoryPanel = new JPanel();

		//judge if converged
		//final JLabel convergePanel = new JLabel();

		final Plot2DPanel plot = new Plot2DPanel();
		plot.addLegend("SOUTH");
		plot.setAxisLabel(0, "t");
		plot.setAxisLabel(1, "Expression Level");
		plot.setPreferredSize(new Dimension(440,390));


		GeneNetwork grn = ((DynamicalModelElement)item_).getGeneNetwork();
		final ArrayList<DoubleMatrix2D> timeSeries_ = grn.getTimeSeries();
		final ArrayList<DoubleMatrix1D> timeScale_ = grn.getTimeScale();

		//display multiple time series
		//combobox
		//generate time series List
		String[] timeSeriesList = new String[timeSeries_.size()];

		for(int i=0;i<timeSeries_.size();i++)
			timeSeriesList[i] = Integer.toString(i);

		JLabel selectPanelName = new JLabel("Select trajectory: ");
		JPanel selectPanel = new JPanel();
		//selectPanel.setPreferredSize(new Dimension(1000,10));
		final JComboBox<String> combo=new JComboBox<String>(timeSeriesList);	    			    		

		//view window
		final JPanel trajectoryViewPanel = new JPanel();
		//trajectoryViewPanel.setPreferredSize(new Dimension(990,500));

		plot.setPreferredSize(new Dimension(440,350));
		double[] t = timeScale_.get(0).toArray();
		for(int k=0;k<timeSeries_.get(0).columns();k++){
			double[] y = timeSeries_.get(0).viewColumn(k).toArray();
			plot.addLinePlot(((DynamicalModelElement) item_).getGeneNetwork().getNode(k).getLabel(), t, y);	
			//convergePanel.setText("isConverged: "+converged(timeSeries_.get(0)));
		}		

		//combobox action	    		
		combo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = combo.getSelectedIndex();				

				plot.removeAllPlots();

				double[] t = timeScale_.get(row).toArray();
				for(int k=0;k<timeSeries_.get(row).columns();k++){
					double[] y = timeSeries_.get(row).viewColumn(k).toArray();
					plot.addLinePlot(((DynamicalModelElement) item_).getGeneNetwork().getNode(k).getLabel(), t, y);
					//convergePanel.setText("isConverged: "+converged(timeSeries_.get(0)));
				}	

				trajectoryViewPanel.repaint();
				trajectoryViewPanel.setVisible(true);
			}			 
		});


		selectPanel.setLayout(new BoxLayout(selectPanel, BoxLayout.X_AXIS)); 
		selectPanel.add(selectPanelName);
		selectPanel.add(combo);
		trajectoryViewPanel.add(plot);

		//set layout
		trajectoryPanel.setLayout(new GridBagLayout());
		NetLand.addComponent(trajectoryPanel, trajectoryViewPanel, 0, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 0, 1);
		NetLand.addComponent(trajectoryPanel, selectPanel, 0, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1, 0);
		//NetLand.addComponent(trajectoryPanel, convergePanel, 0, 2, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1, 0);


		return trajectoryPanel;
	}


	//================================================================================
	// ----------------------------------------------------------------------------


	public void setModelAction() {
		boolean useSDE = model_.getSelectedIndex() == 1;// || model_.getSelectedIndex() == 2;
		sdeDiffusionCoeff_.setEnabled(useSDE);	
		if( useSDE ){
			((JSpinner.NumberEditor)sdeDiffusionCoeff_.getEditor()).getTextField().setBackground(new Color(240,240,240));
		}else{
			((JSpinner.NumberEditor)sdeDiffusionCoeff_.getEditor()).getTextField().setBackground(new Color(214,214,214));
		}
	}


	// ----------------------------------------------------------------------------


	// ----------------------------------------------------------------------------


	public void escapeAction() {
		super.escapeAction();
	}


	// ----------------------------------------------------------------------------

	/**
	 * Run the simulation process and benchmark generation.
	 * Save the simulation parameters defined by the user in the settings of GNW.
	 * The the simulation thread reads these values in the settings of GNW.
	 * @param item 
	 */
	public void enterAction(NetworkElement item) {

		try {
			GeneNetwork grn = ((DynamicalModelElement) item).getGeneNetwork();			

			GnwSettings settings = GnwSettings.getInstance();

			// Save the required settings
			// Model
			settings.setSimulateODE(model_.getSelectedIndex() == 0);
			settings.setSimulateSDE(model_.getSelectedIndex() == 1);
			settings.setSimulateSTS(model_.getSelectedIndex() == 2);


			settings.setNumTimeSeries((Integer) numTimeSeries_.getModel().getValue());


			// TODO check that correct			
			int maxt = (int)tmax_.getModel().getValue();
			int dt = 0;

			if (durationOfSeriesLabel_.isEnabled())
				settings.setMaxtTimeSeries(maxt);
			//			if (numPointsPerSeriesLabel_.isEnabled())
			settings.setDt(dt);
			//settings.setNumMeasuredPoints((Integer) numPointsPerTimeSerie_.getModel().getValue());


			if (settings.getSimulateSDE())
				settings.setNoiseCoefficientSDE((Double) sdeDiffusionCoeff_.getModel().getValue());



			simulation = new SimulationThread(grn);

			// Perhaps make a test on the path validity
			settings.stopBenchmarkGeneration(false); // reset

			// be sure to have set the output directory before running the simulation
			simulation.start();
		}
		catch (Exception e)
		{
			//log_.log(Level.WARNING, "Simulation::enterAction(): " + e.getMessage(), e);
			JOptionPane.showMessageDialog(null,  "Error in simulation of trajectories!", "Error", JOptionPane.INFORMATION_MESSAGE);
			MsgManager.Messages.errorMessage(e, "Error", "");
		}
	}



	// ============================================================================
	// PRIVATE CLASSES
	public class SimulationThread implements Runnable {

		/** Implemented types of benchmark */
		private GeneNetwork grn_;
		/** Main Thread */
		private Thread myThread_;
		/** handles the experiments */
		private volatile boolean stopRequested;

		// ============================================================================
		// PUBLIC METHODS

		public SimulationThread(GeneNetwork grn){
			super();
			this.grn_ = grn;
			myThread_ = null;	
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

			if( myThread_ != null )
				myThread_.interrupt();
		}

		// ----------------------------------------------------------------------------

		public void run()
		{
			snake_.start();
			myCardLayout_.show(runButtonAndSnakePanel_, snakePanel_.getName());

			//settings
			Integer numSeries = (Integer) numTimeSeries_.getModel().getValue();
			Integer maxt =  (Integer) tmax_.getModel().getValue();
			boolean randomInitial = false;
			if( randomButton.isSelected() )
				randomInitial = true;

			double dt_ = 0;
			int numPoints = maxt*2+1;


			//simulation
			/** stochastic simulation by dizzy  **/
			if(  model_.getSelectedIndex() == 2 ){ 
				File fTemp = null;
				try {	
					System.out.print("Running Gillespie algorithm\n");
					grn_.setId("temp_"+System.currentTimeMillis() );
					String temppath = System.getProperty("java.io.tmpdir");
					File f = new File(temppath);
					fTemp = File.createTempFile(grn_.getId(), ".cmdl", f);

					URL url = fTemp.toURI().toURL();
					grn_.writeCMDL(url,grn_.getSpecies_initialState());
					//System.out.print(deSolver_.getGrn().getSpecies_initialState());

					//get model from file
					String fileName = fTemp.getAbsolutePath();
					String modelText = readFileByLines(fileName);
					Model model = processModel(modelText,fileName);

					//		            System.out.println(model.toString());

					//simulate
					String []requestedSymbolNames = new String[grn_.getSize()];
					for(int i=0;i<grn_.getSize();i++)
						requestedSymbolNames[i] = grn_.getNodes().get(i).getLabel();

					if( model_.getSelectedIndex() == 2 ){ //stochastic
						//SimulatorStochasticGibsonBruck,       	SimulatorStochasticTauLeapComplex,      	SimulatorStochasticTauLeapSimple		        		    		

						//its
						ArrayList<DoubleMatrix2D> timeSeries_ = new ArrayList<DoubleMatrix2D>(numSeries);
						ArrayList<DoubleMatrix1D> timeScale_ = new ArrayList<DoubleMatrix1D>(numSeries);

						int its = 0;
						while( !stopRequested && its<numSeries ){
							System.out.print("Its: "+its+"\n");

							//check if random initials
							if( randomInitial ){
								DoubleMatrix1D x0 = randomInitial(upbound, lowbound);

								//new file
								fTemp = File.createTempFile("temp_"+System.currentTimeMillis(), ".cmdl", f);
								url = fTemp.toURI().toURL();
								grn_.writeCMDL(url, x0);

								//get model from file
								fileName = fTemp.getAbsolutePath();
								modelText = readFileByLines(fileName);
								model = processModel(modelText,fileName);
							}

							SimulatorStochasticGillespie simulator = new SimulatorStochasticGillespie();
							simulator.initialize(model);
							SimulatorParameters simParams = new SimulatorParameters();
							simParams.setEnsembleSize(new Integer(1));
							simParams.setComputeFluctuations(false);
							simParams.setNumHistoryBins(400);


							SimulationResults simulationResults = simulator.simulate(0.0, maxt, simParams, numPoints, requestedSymbolNames);			

							double []timeValues = simulationResults.getResultsTimeValues();
							Object []symbolValues = simulationResults.getResultsSymbolValues();

							DoubleMatrix1D tempTime = new DenseDoubleMatrix1D(timeValues);
							double[][] tempValues = new double[numPoints][requestedSymbolNames.length];

							for(int i=0;i<numPoints;i++){	            	
								double []symbolValue = (double []) symbolValues[i];
								tempValues[i] = symbolValue;
							}

							timeScale_.add(tempTime);
							cern.jet.math.Functions F = cern.jet.math.Functions.functions;

							DoubleMatrix2D timeCourse = new DenseDoubleMatrix2D(tempValues);
							timeCourse.assign(F.div(1000.0));

							timeSeries_.add( timeCourse );

							if( randomInitial )
								fTemp.delete();

							its++;
						}

						grn_.setTimeScale(timeScale_);
						grn_.setTimeSeries(timeSeries_);
						grn_.setTraj_itsValue(timeSeries_.size());
						grn_.setTraj_maxTime(maxt);
						grn_.setTraj_noise((Double) sdeDiffusionCoeff_.getModel().getValue());
						grn_.setTraj_model("gillespie");

						//save result
						if( !userPath_.getText().isEmpty() )
							try {
								saveFile(timeSeries_, timeScale_);
							} catch (Exception e) {
								JOptionPane.showMessageDialog(null, "Failed to write time series to file", "Error", JOptionPane.INFORMATION_MESSAGE);
								MsgManager.Messages.errorMessage(e, "Error", "");
							}

						finalizeAfterSuccess();
						fTemp.delete();
						//log_.log(Level.INFO, "Done!");
						System.out.print("Done!\n");
					}else{
						//SimulatorDeterministicRungeKuttaFixed   SimulatorDeterministicRungeKuttaAdaptive
						SimulatorDeterministicRungeKuttaAdaptive simulator = new SimulatorDeterministicRungeKuttaAdaptive();
						simulator.initialize(model);
						SimulatorParameters simParams = new SimulatorParameters();
						simParams.setEnsembleSize(new Integer(1));
						simParams.setComputeFluctuations(false);
						simParams.setNumHistoryBins(400);
						simParams.setMaxAllowedAbsoluteError(0.01);
						simParams.setMaxAllowedRelativeError(0.0001);
						simParams.setStepSizeFraction(0.001);

						//its
						ArrayList<DoubleMatrix2D> timeSeries_ = new ArrayList<DoubleMatrix2D>(numSeries);
						ArrayList<DoubleMatrix1D> timeScale_ = new ArrayList<DoubleMatrix1D>(numSeries);
						for(int its=0;its<numSeries;its++){
							SimulationResults simulationResults = simulator.simulate(0.0, maxt, simParams, numPoints, requestedSymbolNames);			

							double []timeValues = simulationResults.getResultsTimeValues();
							Object []symbolValues = simulationResults.getResultsSymbolValues();

							DoubleMatrix1D tempTime = new DenseDoubleMatrix1D(timeValues);
							double[][] tempValues = new double[numPoints][requestedSymbolNames.length];

							for(int i=0;i<numPoints;i++){	            	
								double []symbolValue = (double []) symbolValues[i];
								tempValues[i] = symbolValue;
							}

							timeScale_.add(tempTime);
							timeSeries_.add( new DenseDoubleMatrix2D(tempValues) );
						}

						grn_.setTimeScale(timeScale_);
						grn_.setTimeSeries(timeSeries_);

						finalizeAfterSuccess();
						fTemp.delete();
						//log_.log(Level.INFO, "Done!");
						System.out.print("Done!\n");
					}



				} catch (IOException e) {
					MsgManager.Messages.errorMessage(e, "Error", "");
				} catch (SBMLException e) {
					MsgManager.Messages.errorMessage(e, "Error", "");
				} catch (DataNotFoundException e) {
					MsgManager.Messages.errorMessage(e, "Error", "");
				} catch (InvalidInputException e) {
					MsgManager.Messages.errorMessage(e, "Error", "");
				} catch (IllegalStateException e) {
					MsgManager.Messages.errorMessage(e, "Error", "");
				} catch (IllegalArgumentException e) {
					MsgManager.Messages.errorMessage(e, "Error", "");
				} catch (AccuracyException e) {
					MsgManager.Messages.errorMessage(e, "Error", "");
				} finally {
					fTemp.delete();
				}			

			}else  
				/** sde **/	
				if( model_.getSelectedIndex() == 1 ){
					SDESolver deSolver_ = new SDESolver(grn_, false, (Double) sdeDiffusionCoeff_.getModel().getValue());
					try
					{				
						deSolver_.setNumSeries(numSeries);
						deSolver_.setMaxt(maxt);
						deSolver_.setNumTimePoints(maxt*2+1);
						deSolver_.setRandomInitial(randomInitial);
						deSolver_.setDt(dt_);
						deSolver_.setIslandscape(false);
						deSolver_.setOutputDirectory(userPath_.getText());
						//deSolver_.setNoiseStrength((Double) sdeDiffusionCoeff_.getModel().getValue());

						if( randomInitial ){
							deSolver_.setUpBoundary(upbound);
							deSolver_.setLowBoundary(lowbound);
						}

						System.out.print("Simulate SDE\n");				

					
						deSolver_.setSimulateODE(false);
						deSolver_.setSimulateSDE(true);
						deSolver_.setODE(false);


						//run numSeries_ times
						ArrayList<SDETimeSeriesExperiment> tss = new ArrayList<SDETimeSeriesExperiment>(numSeries);
						ArrayList<DoubleMatrix2D> timeSeries = new ArrayList<DoubleMatrix2D>();
						ArrayList<DoubleMatrix1D> timeScale = new ArrayList<DoubleMatrix1D>();		

						deSolver_.setTss(tss);
						deSolver_.setTimeScale(timeScale);
						deSolver_.setTimeSeries(timeSeries);

						int its = 0;
						while( !stopRequested && its<numSeries ){
							System.out.print("Its: "+its+"\n");
							deSolver_.solveEquations_SDE();
							its++;
						}

						grn_.setTimeScale(timeScale);
						grn_.setTimeSeries(timeSeries);
						grn_.setTraj_itsValue(timeSeries.size());
						grn_.setTraj_maxTime(maxt);
						grn_.setTraj_noise((Double) sdeDiffusionCoeff_.getModel().getValue());
						grn_.setTraj_model("sde");

						if( !userPath_.getText().isEmpty() )
							saveFile(tss, grn_.getId(), userPath_.getText());

						finalizeAfterSuccess();
						//log_.log(Level.INFO, "Done!");
						System.out.print("Done!\n");
					}
					catch (OutOfMemoryError e)
					{
						//					log_.log(Level.WARNING, "There is not enough memory available to run this program.\n" +
						//							"Quit one or more programs, and then try again.\n" +
						//							"If enough amounts of RAMÂ are installed on this computer, try to run the program " +
						//							"with the command-line argument -Xmx1024m to use maximum 1024Mb of memory, " +
						//							"-Xmx2048m to use max 2048Mb, etc.");
						JOptionPane.showMessageDialog(new Frame(), "There is not enough memory available to run this program.\n" +
								"Quit one or more programs, and then try again.\n" +
								"If enough amounts of RAMÂ are installed on this computer, try to run the program " +
								"with the command-line argument -Xmx1024m to use maximum 1024Mb of memory, " +
								"-Xmx2048m to use max 2048Mb, etc.", "Out of memory",  JOptionPane.INFORMATION_MESSAGE);
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
					catch (CancelException e)
					{
						// do not display an annoying dialog to say "cancelled!"
						//log_.log(Level.INFO, e.getMessage());
						JOptionPane.showMessageDialog(new Frame(), "Program is interrupted!", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
						MsgManager.Messages.errorMessage(e, "Error", "");
						finalizeAfterFail();
					}
					catch (ConvergenceException e)
					{
						//log_.log(Level.WARNING, "Simulation::run(): " + e.getMessage(), e);
						JOptionPane.showMessageDialog(new Frame(), "Unable to converge", "Error", JOptionPane.INFORMATION_MESSAGE);
						MsgManager.Messages.errorMessage(e, "Error", "");
						finalizeAfterFail();
					}
					catch (RuntimeException e)
					{
						//log_.log(Level.WARNING, "Simulation::run(): " + e.getMessage(), e);
						JOptionPane.showMessageDialog(new Frame(), "Runtime exception", "Error", JOptionPane.INFORMATION_MESSAGE);
						MsgManager.Messages.errorMessage(e, "Error", "");
						finalizeAfterFail();
					}
					catch (Exception e)
					{
						//log_.log(Level.WARNING, "Simulation::run(): " + e.getMessage(), e);
						JOptionPane.showMessageDialog(new Frame(), "Error encountered", "Error", JOptionPane.INFORMATION_MESSAGE);
						MsgManager.Messages.errorMessage(e, "Error", "");
						finalizeAfterFail();
					}
				}else  //model_.getSelectedIndex() == 0 || ode
					/** ode **/	
					if( model_.getSelectedIndex() == 0 ){ 
						ODESolver deSolver_ = new ODESolver(grn_, maxt, numSeries);

						if( randomInitial ){
							deSolver_.setUpBoundary(upbound);
							deSolver_.setLowBoundary(lowbound);
						}

						System.out.print("Simulate ODE\n");


						deSolver_.setIslandscape(false);
						deSolver_.setRandomInitial(randomInitial);
						deSolver_.setOutputDirectory_(userPath_.getText());			

						//run numSeries_ times
						ArrayList<SDETimeSeriesExperiment> tss = new ArrayList<SDETimeSeriesExperiment>(numSeries);
						ArrayList<DoubleMatrix2D> timeSeries = new ArrayList<DoubleMatrix2D>();
						ArrayList<DoubleMatrix1D> timeScale = new ArrayList<DoubleMatrix1D>();		

						deSolver_.setTss(tss);
						deSolver_.setTimeScale(timeScale);
						deSolver_.setTimeSeries(timeSeries);

						int its = 0;
						while( !stopRequested && its<numSeries ){
							System.out.print("Its: "+its+"\n");
							deSolver_.solveEquations_ODE(); 
							its++;
						}

						grn_.setTimeScale(timeScale);
						grn_.setTimeSeries(timeSeries);
						grn_.setTraj_itsValue(timeSeries.size());
						grn_.setTraj_maxTime(maxt);
						grn_.setTraj_noise((Double) sdeDiffusionCoeff_.getModel().getValue());
						grn_.setTraj_model("ode");

						if( !userPath_.getText().isEmpty() )
							try {
								saveFile(grn_.getTimeSeries(), grn_.getTimeScale(), "NetLand_time_courses", userPath_.getText());
							} catch (Exception e) {
								MsgManager.Messages.errorMessage(e, "Failed to save result", "");
							}


						finalizeAfterSuccess();
						//log_.log(Level.INFO, "Done!");
						System.out.print("Done!\n");

					}
		}

		private void saveFile(ArrayList<DoubleMatrix2D> timeSeries, ArrayList<DoubleMatrix1D> timeScales, String networkName, String outputDirectory_) throws Exception{
			//interpolate new points
			String filename = "temp_"+System.currentTimeMillis();
			String temppath = System.getProperty("java.io.tmpdir");
			File f = new File(temppath);
			File fTemp = File.createTempFile(filename, ".sbml", f);

			String filename1 = "temp_"+System.currentTimeMillis();
			File fTemp1 = File.createTempFile(filename1, ".timeseries", f);


			//part1: save the complete network in smbl2
			URL url = fTemp.toURI().toURL();
			grn_.setId("NetLand_"+grn_.getId());
			grn_.writeSBML(url);


			//part2: save time series to file
			FileWriter fw = new FileWriter(fTemp1.getAbsolutePath(), true);
			fw.write("\n\nTraj\t"+timeSeries.size()+"\t"+timeSeries.get(0).rows()+"\t"+timeSeries.get(0).columns()+"\n");
			//parameters
			fw.write(grn_.getTraj_itsValue()+"\t"+grn_.getTraj_maxTime()+"\t"+grn_.getTraj_noise()+"\t"+grn_.getTraj_model()+"\n");

			for(int i=0;i<timeSeries.size();i++){
				fw.write(Integer.toString(i)+"\n");
				printAll(fw, timeSeries.get(i), timeScales.get(i));			
			}
			fw.close();


			//merge
			String output = outputDirectory_;
			mergeFiles(output, new String[]{fTemp.getAbsolutePath(), fTemp1.getAbsolutePath()});

			fTemp.delete();
			fTemp1.delete();

			System.out.print("Save the time course to "+outputDirectory_+"\n");
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

		public void printAll(FileWriter fw, DoubleMatrix2D timeSeries_, DoubleMatrix1D timeScale_) {

			try {
				printTrajectories(fw, timeSeries_, timeScale_);
			} catch (Exception e) {
				//Log.error("TimeSeriesExperiment", "Failed to write time series to file.", e);
				JOptionPane.showMessageDialog(null, "Failed to write time series to file", "Error", JOptionPane.INFORMATION_MESSAGE);
				MsgManager.Messages.errorMessage(e, "Error", "");
			}
		}

		/** Writes time series data with the time scale to file. */
		public void printTrajectories(FileWriter fw, DoubleMatrix2D timeSeries, DoubleMatrix1D timeScale) {
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

		public void saveFile(ArrayList<SDETimeSeriesExperiment> ts, String networkName, String outputDirectory_) throws Exception{
			//interpolate new points
			String filename = "temp_"+System.currentTimeMillis();
			String temppath = System.getProperty("java.io.tmpdir");
			File f = new File(temppath);
			File fTemp = File.createTempFile(filename, ".sbml", f);

			String filename1 = "temp_"+System.currentTimeMillis();
			File fTemp1 = File.createTempFile(filename1, ".timeseries", f);


			//part1: save the complete network in smbl2
			URL url = fTemp.toURI().toURL();
			grn_.setId("NetLand_"+grn_.getId());
			grn_.writeSBML(url);


			//part2: save time series to file
			FileWriter fw = new FileWriter(fTemp1.getAbsolutePath(), true);
			fw.write("\n\nTraj\t"+ts.size()+"\t"+ts.get(0).getNumTimePoints()+"\t"+ts.get(0).getTimeSeries().columns()+"\n");
			//parameters
			fw.write(grn_.getTraj_itsValue()+"\t"+grn_.getTraj_maxTime()+"\t"+grn_.getTraj_noise()+"\t"+grn_.getTraj_model()+"\n");

			for(int i=0;i<ts.size();i++){
				fw.write(Integer.toString(i)+"\n");
				ts.get(i).printAll(fw);			
			}
			fw.close();


			//merge
			String output = outputDirectory_;
			mergeFiles(output, new String[]{fTemp.getAbsolutePath(), fTemp1.getAbsolutePath()});

			fTemp.delete();
			fTemp1.delete();

			System.out.print("Save the time course to "+outputDirectory_+"\n");
		}
		
		
		public DoubleMatrix1D randomInitial(double upBoundary, double lowBoundary) {
			int dimension = grn_.getNodes().size();
			Random random = new Random();
			DoubleMatrix1D s = new DenseDoubleMatrix1D(dimension);

			for(int i=0;i<dimension;i++)
				s.set(i, random.nextDouble() * (upBoundary - lowBoundary) + lowBoundary);

			//			for(int i=dimension;i<dimension*2;i++)
			//				s[i] = 0.1;

			return s;
		}
		
		private void saveFile(ArrayList<DoubleMatrix2D> timeSeries_, ArrayList<DoubleMatrix1D> timeScale_) throws Exception{
			//interpolate new points
			String filename = "temp_"+System.currentTimeMillis();
			String temppath = System.getProperty("java.io.tmpdir");
			File f = new File(temppath);
			File fTemp = File.createTempFile(filename, ".sbml", f);

			String filename1 = "temp_"+System.currentTimeMillis();
			File fTemp1 = File.createTempFile(filename1, ".timeseries", f);


			//part1: save the complete network in smbl2
			URL url = fTemp.toURI().toURL();
			grn_.setId("NetLand_"+grn_.getId());
			grn_.writeSBML(url);


			//part2: save time series to file
			FileWriter fw = new FileWriter(fTemp1.getAbsolutePath(), true);
			fw.write("\n\nTraj\t"+timeSeries_.size()+"\t"+timeSeries_.get(0).rows()+"\t"+timeSeries_.get(0).columns()+"\n");
			//parameters
			fw.write(grn_.getTraj_itsValue()+"\t"+grn_.getTraj_maxTime()+"\t"+grn_.getTraj_noise()+"\t"+grn_.getTraj_model()+"\n");

			for(int i=0;i<timeSeries_.size();i++){
				fw.write(Integer.toString(i)+"\n");
				printTrajectories(fw, timeSeries_.get(i), timeScale_.get(i));			
			}
			fw.close();


			//merge
			String output = userPath_.getText();
			mergeFiles(output, new String[]{fTemp.getAbsolutePath(), fTemp1.getAbsolutePath()});

			fTemp.delete();
			fTemp1.delete();

			System.out.print("Save the time course to "+output+"\n");
		}
	}


	public Model processModel(String modelText, String fileName) throws InvalidInputException, IOException, DataNotFoundException
	{
		Model model = null;
		IModelBuilder modelBuilder = null;

		modelBuilder = queryModelBuilder(fileName);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		modelBuilder.writeModel(modelText, outputStream);
		byte []bytes = outputStream.toByteArray();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

		IncludeHandler includeHandler = new IncludeHandler();

		model = modelBuilder.buildModel(inputStream, includeHandler);

		return model;
	}

	private IModelBuilder queryModelBuilder(String pFileName) throws DataNotFoundException
	{
		IModelBuilder modelBuilder = null;
		ModelBuilderCommandLanguage a = new ModelBuilderCommandLanguage();      
		modelBuilder = (IModelBuilder) a;  

		return modelBuilder;
	}

	public String readFileByLines(String fileName) {
		String output = "";
		File file = new File(fileName);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				// 显示行号
				output += tempString+"\n";
			}
			reader.close();
		} catch (IOException e) {
			MsgManager.Messages.errorMessage(e, "Error", "");
		} finally {
			if (reader != null) {
				try {
					reader.close();

				} catch (IOException e1) {
					MsgManager.Messages.errorMessage(e1, "Error", "");
				}
			}
		}

		return output;
	}


	// ----------------------------------------------------------------------------

	public void finalizeAfterSuccess()  
	{
		snake_.stop();
		myCardLayout_.show(runButtonAndSnakePanel_, runPanel_.getName());

		analyzeResult.setVisible(true);

		//display result
		trajPlot.removeAll();
		trajPlot.add(trajectoryTabb());
		trajPlot.updateUI();
		trajPlot.setVisible(true);	
		trajPlot.repaint();


		//escapeAction(); // close the simulation window
	}

//	private JPanel trajectoryTabb(){
//		JPanel trajectoryPanel = new JPanel();
//
//		//judge if converged
//		final JLabel convergePanel = new JLabel();
//
//		final Plot2DPanel plot = new Plot2DPanel();
//		plot.addLegend("SOUTH");
//		plot.setAxisLabel(0, "t");
//		plot.setAxisLabel(1, "ExpressionValue");
//		plot.setPreferredSize(new Dimension(440,390));
//
//
//		final ArrayList<DoubleMatrix2D> timeSeries_ = grn_.getTimeSeries();
//		final ArrayList<DoubleMatrix1D> timeScale_ = grn_.getTimeScale();
//
//		//display initial values
//		if(timeSeries_.size()==0){   			        
//			DoubleMatrix1D x0 = grn_.getInitialState();
//			for(int i=0;i<x0.size();i++){
//				double[] x = { 0 };
//				double[] y = { 0 };
//
//				plot.addLinePlot(grn_.getNode(i).getLabel(), x, y);	
//			}
//			trajectoryPanel.add(plot);
//		}else{
//			//display multiple time series
//			//combobox
//			//generate time series List
//			String[] timeSeriesList = new String[timeSeries_.size()];
//
//			for(int i=0;i<timeSeries_.size();i++)
//				timeSeriesList[i] = Integer.toString(i);
//
//			JLabel selectPanelName = new JLabel("Select trajectory: ");
//			JPanel selectPanel = new JPanel();
//			//selectPanel.setPreferredSize(new Dimension(1000,10));
//			final JComboBox<String> combo=new JComboBox<String>(timeSeriesList);	    			    		
//
//			//view window
//			final JPanel trajectoryViewPanel = new JPanel();
//			//trajectoryViewPanel.setPreferredSize(new Dimension(990,500));
//
//			plot.setPreferredSize(new Dimension(440,350));
//			double[] t = timeScale_.get(0).toArray();
//			for(int k=0;k<timeSeries_.get(0).columns();k++){
//				double[] y = timeSeries_.get(0).viewColumn(k).toArray();
//				plot.addLinePlot(grn_.getNode(k).getLabel(), t, y);	
//				//convergePanel.setText("isConverged: "+converged(timeSeries_.get(0)));
//			}		
//
//			//combobox action	    		
//			combo.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent e) {
//					int row = combo.getSelectedIndex();				
//
//					plot.removeAllPlots();
//
//					double[] t = timeScale_.get(row).toArray();
//					for(int k=0;k<timeSeries_.get(row).columns();k++){
//						double[] y = timeSeries_.get(row).viewColumn(k).toArray();
//						plot.addLinePlot(grn_.getNode(k).getLabel(), t, y);
//						//convergePanel.setText("isConverged: "+converged(timeSeries_.get(0)));
//					}	
//
//					trajectoryViewPanel.repaint();
//					trajectoryViewPanel.setVisible(true);
//				}			 
//			});
//
//
//			selectPanel.setLayout(new BoxLayout(selectPanel, BoxLayout.X_AXIS)); 
//			selectPanel.add(selectPanelName);
//			selectPanel.add(combo);
//			trajectoryViewPanel.add(plot);
//
//			//set layout
//			trajectoryPanel.setLayout(new GridBagLayout());
//			NetLand.addComponent(trajectoryPanel, trajectoryViewPanel, 0, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 0, 1);
//			NetLand.addComponent(trajectoryPanel, selectPanel, 0, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1, 0);
//		}
//
//		return trajectoryPanel;
//	}

	public String converged(DoubleMatrix2D x) {	 
		double absolutePrecision_ = 0.00001;
		double relativePrecision_ = 0.001;

		int totalTime = x.rows();
		int genes = x.columns();
		DoubleMatrix1D previousState_ = x.viewRow(totalTime-2);
		DoubleMatrix1D state_ = x.viewRow(totalTime-1);

		for (int i=0; i<genes; i++) {	
			double dxy = Math.abs(previousState_.get(i) - state_.get(i)); 

			if (dxy > absolutePrecision_ + relativePrecision_*Math.abs(state_.get(i))) {
				return "Not converged";
			}
		}
		return "Converged";
	}


	// ----------------------------------------------------------------------------

	public void finalizeAfterFail()
	{
		snake_.stop();
		myCardLayout_.show(runButtonAndSnakePanel_, runPanel_.getName());
		//escapeAction(); // close the simulation window
	}
}

