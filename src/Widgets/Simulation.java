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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import javax.xml.stream.XMLStreamException;

import org.apache.commons.math.ConvergenceException;
import org.math.plot.Plot2DPanel;
//import org.math.plot.Plot2DPanel;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.text.parser.ParseException;
import org.systemsbiology.chem.IModelBuilder;
import org.systemsbiology.chem.Model;
import org.systemsbiology.chem.ModelBuilderCommandLanguage;
import org.systemsbiology.chem.SimulationController;
import org.systemsbiology.chem.SimulationResults;
import org.systemsbiology.chem.SimulatorParameters;
import org.systemsbiology.chem.SimulatorStochasticGillespie;
import org.systemsbiology.chem.Species;
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
import ch.epfl.lis.gnw.Gene;
import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.gnw.GnwSettings;
import ch.epfl.lis.gnwgui.DynamicalModelElement;
import ch.epfl.lis.gnwgui.NetworkElement;
import ch.epfl.lis.networks.Node;
import edu.umbc.cs.maple.utils.ColtUtils;
import FileManager.FileChooser;
import LandscapeDisplay.ODESolver;
import LandscapeDisplay.SDESolver;
import LandscapeDisplay.SDETimeSeriesExperiment;
import LandscapeDisplay.nonlinearEq;
import Widgets.SimulationWindow;
import WidgetsTables.AttractorTable;
import WidgetsTables.ConvergenceTable;
import WidgetsTables.SpeciesTable;
import WindowGUI.NetLand;


public class Simulation extends SimulationWindow {

	/** Serialization */
	private static final long serialVersionUID = 1L;

	/** NetworkItem to simulate */
	private GeneNetwork grn;

	/** random initial boundaries **/
	private double upbound;
	private double lowbound;
	private SimulationThread simulation = null;
	private Plot2DPanel plot;

	//private Runtime s_runtime = Runtime.getRuntime(); 


	// ============================================================================
	// PUBLIC METHODS

	public Simulation(final JFrame aFrame, NetworkElement item) {
		super(aFrame);		
		grn = ((DynamicalModelElement) item).getGeneNetwork();
		plot = new Plot2DPanel();

		//closing listener
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent windowEvent){
				if( simulation != null && simulation.myThread_.isAlive() ){
					simulation.stop();	
					System.out.print("Simulation is canceled.\n");
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
							int[] isConverge = new int[grn.getTraj_itsValue()];
							String out = calculateSteadyStates(focusGenes, focus_index, isConverge);

							//show the convergence
							final JDialog ifconvergent = new JDialog();
							ifconvergent.setSize(new Dimension(500,450));
							ifconvergent.setModal(true);
							ifconvergent.setTitle("Convergence");
							ifconvergent.setLocationRelativeTo(null);

							ConvergenceTable tablePanel = new ConvergenceTable(isConverge); 
							JButton continueButton = new JButton("Click to check the attractors.");
							continueButton.addActionListener(new ActionListener() {
								public void actionPerformed(final ActionEvent arg0) {
									ifconvergent.dispose();
								}
							});

							JPanel ifconvergentPanel = new JPanel();
							ifconvergentPanel.setLayout(new BorderLayout());
							ifconvergentPanel.add(tablePanel, BorderLayout.NORTH);
							ifconvergentPanel.add(continueButton, BorderLayout.SOUTH);

							ifconvergent.add(ifconvergentPanel);
							ifconvergent.setVisible(true);	


							//show attractors
							if( out.equals("ok") ){		
								AttractorTable panel = new AttractorTable(grn, focusGenes);
								jsp.setViewportView(panel);
							}else if( grn.getSumPara().size() == 0 )
								content += "Cannot find a steady state!";
							else 
								content += "\nI dont know why!";
						

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
				//System.out.print("Memory start: "+s_runtime.totalMemory()+"\n"); 
				enterAction();
			}
		});

		cancelButton_.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				if( simulation != null )		
					simulation.stop();
					System.out.print("Simulation is canceled!\n");
			}
		});


		fixButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				JDialog a = new JDialog();
				a.setTitle("Fixed initial values");
				a.setSize(new Dimension(400,400));
				a.setLocationRelativeTo(null);

				JPanel speciesPanel = new JPanel();
				String[] columnName = {"Name", "InitialValue"};
				boolean editable = false; //false;
				new SpeciesTable(speciesPanel, columnName, grn, editable);

				/** LAYOUT **/
				JPanel wholePanel = new JPanel();
				wholePanel.setLayout(new BoxLayout(wholePanel, BoxLayout.Y_AXIS));
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
	private String calculateSteadyStates(String[] focusGenes, int[] focus_index, int[] isConverge){	
		ArrayList<DoubleMatrix2D> timeSeries = grn.getTimeSeries();
		int its = timeSeries.size();

		//discrete the final state
		int dimension = grn.getNodes().size();

		//double check distances between attractors			
		//solver equations
		List<String> solverResults_focusgenes = new ArrayList<String>();

		//at the same time, check if trajectories are stable
		for(int i=0;i<its;i++){		
			isConverge[i] = 0; //0: converge 1:not converge

			DoubleMatrix1D tempX0 =  timeSeries.get(i).viewRow(timeSeries.get(i).rows()-1);
			DoubleMatrix1D tempX1 =  timeSeries.get(i).viewRow(timeSeries.get(i).rows()-2);
			cern.jet.math.Functions F = cern.jet.math.Functions.functions;	
			double dis = tempX0.aggregate(tempX1, F.plus, F.chain(F.square,F.minus))/dimension;

			if( model_.getSelectedIndex() == 1 ){//SDE
				double noise = Math.pow((Double) sdeDiffusionCoeff_.getModel().getValue(), 2);
				if( dis>noise ){
					isConverge[i] = 1;
					continue;
					//					return "notStable";
				}
			}else if( model_.getSelectedIndex() == 0 ){ //ODE
				if( dis>0.000001 ){
					isConverge[i] = 1;
					continue;
					//return "notStable"; //precision 0.000001
				}
			}


			nonlinearEq a = new nonlinearEq(grn);
			DoubleMatrix1D tempY = a.runSolver(tempX0,grn);
			if( tempY == null ){
				isConverge[i] = 2;
				continue;
			}


			String temp1 = "";
//			double[] tempArray = new double[focus_index.length];
			for(int j=0;j<focus_index.length;j++){		
				double temp = Math.floor(100*tempY.get(focus_index[j]))/100;
				temp1 += temp+";" ;
//				tempArray[j] = temp;
			}
//			attractorTypes_focusgene.add(tempArray.clone());
			solverResults_focusgenes.add(temp1);	
		}
		
		//remove duplicates
		List<String> uniqueList_focusgene = new ArrayList<String>(new HashSet<String>(solverResults_focusgenes));
		
		//distance matrix
		double threshold = 0.1;
		DoubleMatrix2D attractorTypes_focusgene_input = new DenseDoubleMatrix2D(uniqueList_focusgene.size(),dimension);
		for(int i=0;i<uniqueList_focusgene.size();i++){
			for(int j=0;j<dimension;j++)
				attractorTypes_focusgene_input.set(i, j, Double.parseDouble(uniqueList_focusgene.get(i).split(";")[j]));
		}
//		attractorTypes_focusgene = null;
		solverResults_focusgenes = null;
		uniqueList_focusgene = null;

		/** high iterations may cause the OutOfMemoryError **/
		ArrayList<Integer> output = Landscape.calculateDisMatrix(attractorTypes_focusgene_input);

		Collections.sort(output);
		//remove i or j
		for(int i=output.size()-1;i>=0;i--){
//			solverResults_focusgenes.remove((int)output.get(i));
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




	protected JPanel trajectoryTabb(){
		JPanel trajectoryPanel = new JPanel();

		//judge if converged
		//final JLabel convergePanel = new JLabel();	
		plot.removeAllPlots();
			
		plot.setAxisLabel(0, "t");
		plot.setAxisLabel(1, "Expression Level");
		plot.setPreferredSize(new Dimension(440,390));


		//ArrayList<DoubleMatrix2D> timeSeries_ = grn.getTimeSeries();
		//ArrayList<DoubleMatrix1D> timeScale_ = grn.getTimeScale();

		//display multiple time series
		//combobox
		//generate time series List
		String[] timeSeriesList = new String[grn.getTimeSeries().size()];

		for(int i=0;i<grn.getTimeSeries().size();i++)
			timeSeriesList[i] = Integer.toString(i);
		
		JLabel selectPanelName = new JLabel("Select trajectory: ");
		JPanel selectPanel = new JPanel();
		//selectPanel.setPreferredSize(new Dimension(1000,10));
		final JComboBox<String> combo=new JComboBox<String>(timeSeriesList);	    			    		

		//view window
		final JPanel trajectoryViewPanel = new JPanel();
		//trajectoryViewPanel.setPreferredSize(new Dimension(990,500));

		plot.setPreferredSize(new Dimension(440,350));
		double[] t = grn.getTimeScale().get(0).toArray();
		for(int k=0;k<grn.getTimeSeries().get(0).columns();k++)
			plot.addLinePlot(grn.getNode(k).getLabel(), t, grn.getTimeSeries().get(0).viewColumn(k).toArray());
		plot.addLegend("SOUTH");
		plot.repaint();	

		//combobox action	    		
		combo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = combo.getSelectedIndex();				

				plot.removeAllPlots();

				double[] t = grn.getTimeScale().get(row).toArray();
				for(int k=0;k<grn.getTimeSeries().get(row).columns();k++){
					plot.addLinePlot(grn.getNode(k).getLabel(), t, grn.getTimeSeries().get(row).viewColumn(k).toArray());
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
	public void enterAction() {

		try {			
			simulation = new SimulationThread(grn);
			simulation.start();
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null,  "Error in simulation of trajectories!", "Error", JOptionPane.INFORMATION_MESSAGE);
			MsgManager.Messages.errorMessage(e, "Error", "");
		}
	}

	

	// ============================================================================
	// PRIVATE CLASSES
	public class SimulationThread implements Runnable {

		/** Implemented types of benchmarkÂ */
		private GeneNetwork grn_;
		/** Main Thread */
		private Thread myThread_;
		/** handles the experiments */
		private volatile boolean stopRequested;

		private SimulationController controller = new SimulationController();
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

			if( model_.getSelectedIndex() == 2 ) //SSA
				controller.setCancelled(true);
				
			if( myThread_ != null )
				myThread_.interrupt();
		}

		// ----------------------------------------------------------------------------

		public void run()
		{		
			//System.out.print("ODE start: "+System.currentTimeMillis()+"\n"); 
			snake_.start();
			myCardLayout_.show(runButtonAndSnakePanel_, snakePanel_.getName());

			/** settings **/
			Integer numSeries = (Integer) numTimeSeries_.getModel().getValue();
			Integer maxt =  (Integer) tmax_.getModel().getValue();
			boolean randomInitial = false;
			if( randomButton.isSelected() )
				randomInitial = true;

			double step = 1; //default fixed step
			
			//if choose ode simulator
			if(  model_.getSelectedIndex() == 0 ){
				//step = 0.001;
				if( numSeries>1 && !randomInitial ){
					JOptionPane.showMessageDialog(null, "Fixed initial values are used.\n " +
							"The ODE simulator will give the same result.\n" +
							"Thus only one trajectory will be generated.\n", "Info", JOptionPane.INFORMATION_MESSAGE);
					numSeries = 1;
					numTimeSeries_.getModel().setValue(1);
				}
			}else if(  model_.getSelectedIndex() == 1 ){
				if( maxt>10000 )
					step = 2;
			}

			
			/** check memory **/			
			double leastMemoryReq = 1.3*maxt*1.0/step*numSeries*8*grn_.getNodes().size()/1024.0/1024; //MB
			boolean isEnoughMem = checkMemory(leastMemoryReq); //first check
			
		
			if( !isEnoughMem ){ 
				leastMemoryReq = maxt*1.0/step*1*8*grn.getNodes().size()/1024.0/1024; //MB for 1 traj
				int maxPointsPerTraj = calculateMaxPointsPerTraj(numSeries*2*8*grn.getNodes().size()/1024.0/1024);
				isEnoughMem = checkMemory(leastMemoryReq);
				
				if( !isEnoughMem ) //for one traj
					memoryWarning(leastMemoryReq, 0);
				else{ //enough memory for 1 traj
					boolean isCutTime = false;
					boolean isCutIts = false;

					switch(model_.getSelectedIndex()){
					case 0:
					{
						/********************************** ode *******************************************************/		
						/** decide m, save every m traj **/
						int m = calculateMTraj(leastMemoryReq);
						if( m<numSeries )
							isCutIts = true;
						else
							m = numSeries;

						if( m==0 ) m=1;

						/** decide t, save every t steps **/
						int t = maxt;
						//							int t = calculateTStep(leastMemoryReq, maxt);
						//							if( t<maxt ){
						//								isCutTime = true;
						//								maxPointsPerTraj = maxPointsPerTraj/ ( (int) (maxt*1.0/t)+1);
						//
						//								if( maxPointsPerTraj<10 ){
						//									memoryWarning(leastMemoryReq, maxPointsPerTraj);
						//									System.out.print("Simulation is stopped.\n");
						//									stop();							
						//									break;
						//								}
						//							}else
						//								t = maxt;
//
//						if( t==0 ) t=1;

						/** run SDE **/
						runODE(numSeries, maxt, randomInitial, m, t, isCutTime, isCutIts, maxPointsPerTraj);

						break;
					}
					case 1:
					{
						/**************************** sde ***************************************/	
						/** decide m, save every m traj **/
						int m = calculateMTraj(leastMemoryReq);
						if( m<numSeries )
							isCutIts = true;
						else
							m = numSeries;

						if( m==0 ) m=1;

						/** decide t, save every t steps **/
						int t = maxt;
//						int t = calculateTStep(leastMemoryReq, maxt);
//						if( t<maxt ){
//							isCutTime = true;
//							maxPointsPerTraj = maxPointsPerTraj/ ( (int) (maxt*1.0/t)+1);
//
//							if( maxPointsPerTraj<10 ){
//								memoryWarning(leastMemoryReq, maxPointsPerTraj);
//								System.out.print("Simulation is stopped.\n");
//								stop();							
//								break;
//							}
//						}else
//							t = maxt;
//
//						if( t==0 ) t=1;

						/** run SDE **/
						runSDE(numSeries, maxt, randomInitial, step, m, t, isCutTime, isCutIts, maxPointsPerTraj);

						break;
					}				
					case 2:
					{
						/***************** stochastic simulation by dizzy  *************************************/
						/** decide m, save every m traj **/
						int m = calculateMTraj(leastMemoryReq);
						if( m<numSeries )
							isCutIts = true;
						else
							m = numSeries;

						if( m==0 ) m=1;

						/** decide t, save every t steps **/
						int t = maxt;
//						int t = calculateTStep(leastMemoryReq, maxt);
//						if( t<maxt ){
//							isCutTime = true;
//							maxPointsPerTraj = maxPointsPerTraj/ ( (int) (maxt*1.0/t)+1);
//
//							if( maxPointsPerTraj<10 ){
//								memoryWarning(leastMemoryReq, maxPointsPerTraj);
//								System.out.print("Simulation is stopped.\n");
//								stop();							
//								break;
//							}
//						}else
//							t = maxt;
//
//						if( t==0 ) t=1;

						/** run SSA **/
						runSSA(numSeries, maxt, randomInitial, m, t, isCutTime, isCutIts, maxPointsPerTraj);

						break;
					}
					}
				}
					
			}else{ //enough memory

				switch(model_.getSelectedIndex()){
				case 0:
				{
					/********************************** ode *******************************************************/	
					runODE(numSeries, maxt, randomInitial, numSeries, maxt, false, false, maxt);
					break;
				}
				case 1:
				{
					/**************************** sde ***************************************/				
					runSDE(numSeries, maxt, randomInitial, step, numSeries, maxt, false, false, maxt);
					break;
				}				
				case 2:
				{
					/***************** stochastic simulation by dizzy  *************************************/
					runSSA(numSeries, maxt, randomInitial, numSeries, maxt, false, false, maxt);
					break;
				}
				}
			}


		}


		private void runSSA(int numSeries, Integer maxt, boolean randomInitial, int saveEveryMTraj, int saveEveryTStep, boolean isCutTime, boolean isCutIts, int maxPointsPerTraj) {
			try {	
				System.out.print("Running Gillespie algorithm\n"); 

				/** backup initial values **/
				DoubleMatrix1D initialX0 = grn_.getInitialState().copy();

				/** create a temp dir **/
				File dir = new File("./"+ "NetLand_" 
						+ new SimpleDateFormat("yyyyMMddhhmmss").format(new Date())  
						+ "_tempSimulationResult");  
				dir.mkdirs(); 

				
				/** create tmp file **/
				File tmpfilename = createTmpFile(dir, numSeries, grn_.getSize(), maxt, (double) sdeDiffusionCoeff_.getModel().getValue(), "gillespie");			

				if( tmpfilename == null ){
					dir.delete();
					finalizeAfterFail();
					return;
				}

				/** generate model **/
				DoubleMatrix1D tempInitial = grn_.getSpecies_initialState().copy();
				Transform.mult(tempInitial, 1000);
				//System.out.print("Start generating the model...\n");
				String modelText = writeCMDL(tempInitial);
				//System.out.print("Start simulating\n");
				Model model = processModel(modelText);
				
				
				/** define SSA simulator **/
				//SimulatorStochasticGibsonBruck, SimulatorStochasticTauLeapComplex, SimulatorStochasticTauLeapSimple		        		    		
				SimulatorStochasticGillespie simulator = new SimulatorStochasticGillespie();
				simulator.initialize(model);
				SimulatorParameters simParams = new SimulatorParameters();
				simParams.setEnsembleSize(new Integer(1));
				simParams.setComputeFluctuations(false);
				simParams.setNumHistoryBins(400);
				simulator.setController(controller);
				
				String[] requestedSymbolNames = new String[grn_.getSize()];
				for(int i=0;i<grn_.getSize();i++)
					requestedSymbolNames[i] = grn_.getNodes().get(i).getLabel();

				
				/** each iteration **/		
				int its = 1; //output iterations

				
				/** set temp storage, for saving **/
				ArrayList<DoubleMatrix2D> temptimeSeries = new ArrayList<DoubleMatrix2D>();
				ArrayList<DoubleMatrix1D> temptimeScale = new ArrayList<DoubleMatrix1D>();

				/** data to plot **/
				ArrayList<DoubleMatrix2D> timeSeries = new ArrayList<DoubleMatrix2D>(numSeries);
				ArrayList<DoubleMatrix1D> timeScale = new ArrayList<DoubleMatrix1D>(numSeries);


				int steps = saveEveryTStep; //simulation time for each run
				int numPoints = 0; //record the number of sampled points
				double previoudTime = 0; //if isCutTime=ture, record the end time of the previous segment 
				cern.colt.matrix.DoubleFactory1D Factory1D = cern.colt.matrix.DoubleFactory1D.dense;
				cern.colt.matrix.DoubleFactory2D Factory2D = cern.colt.matrix.DoubleFactory2D.dense;
				
	
				while( !stopRequested && its<=numSeries ){					
					System.out.print("\nIts: "+its+"\n"); 
					
					/** reset parameters **/
					numPoints = 0;			
					previoudTime = 0;

					/** check if random initials **/
					if( randomInitial ){
						tempInitial = randomInitial(upbound, lowbound);
						tempInitial = Transform.mult(tempInitial, 1000);					
						
						/** create a new CMDL file **/
						//System.out.print("Start writing CMDL model...\n");

						for(int i=0;i<requestedSymbolNames.length;i++){
							Species species = model.getSpeciesByName(requestedSymbolNames[i]);
							species.setSpeciesPopulation(tempInitial.get(i));
						}
							
						//System.out.print("Start simulating\n");
						
						/** set model **/
						simulator.initialize(model);
					}

					
					/** set time for each traj **/
					for(int j=0;j<maxt;j+=saveEveryTStep){		
						steps = saveEveryTStep;				
						if( j+saveEveryTStep>maxt )
							steps = maxt-j;

						/** run **/
						SimulationResults simulationResults = simulator.simulate(0.0, steps, simParams, 200, requestedSymbolNames);
					
						/** save tmp result **/
						double[] symbolTime = simulationResults.getResultsTimeValues();
						Object []symbolValues = simulationResults.getResultsSymbolValues();
						

						/** sample points **/
//						sSystem.out.print("sample\n");
						DoubleMatrix2D timeCourse = new DenseDoubleMatrix2D(200, grn.getSize());
						DoubleMatrix1D tempTime = new DenseDoubleMatrix1D(200);
						numPoints = samplePoints(symbolValues, symbolTime, tempTime, timeCourse);

//						tempTime = tempTime.viewPart(0, numPoints-1);
//						timeCourse = timeCourse.viewPart(0, numPoints-1, 0, grn.getSize());

						temptimeSeries.add(timeCourse.copy());
						temptimeScale.add(tempTime.copy());
						
						/** update time scale **/
						Transform.plus(tempTime, previoudTime);


						/** new initial state from previous calculation **/
						grn_.setInitialState(timeCourse.viewRow(timeCourse.rows()-1).copy());


						if( numPoints>maxPointsPerTraj ){
							
							if( previoudTime==0 ){
								timeSeries.add(selectNPoints(timeCourse.copy(), maxPointsPerTraj));
								timeScale.add(selectNPoints(tempTime.copy(), maxPointsPerTraj));
							}else{
								timeSeries.set(timeSeries.size()-1, Factory2D.appendRows(timeSeries.get(timeSeries.size()-1), selectNPoints(timeCourse.copy(),maxPointsPerTraj)));							
								timeScale.set(timeScale.size()-1, Factory1D.append(timeScale.get(timeScale.size()-1), selectNPoints(tempTime.copy(),maxPointsPerTraj)));
							}

						}else{					
							if( previoudTime==0 ){
								timeSeries.add(timeCourse.copy());
								timeScale.add(tempTime.copy());
							}else{
								timeSeries.set(timeSeries.size()-1, Factory2D.appendRows(timeSeries.get(timeSeries.size()-1), timeCourse));							
								timeScale.set(timeScale.size()-1, Factory1D.append(timeScale.get(timeScale.size()-1), tempTime));
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
						previoudTime = tempTime.get(tempTime.size()-1);

					}//end of for

					its++;
					
					//System.out.print("SSA its: "+System.currentTimeMillis()+"\n"); 
				}//end of while

				/** reset initial values **/
				grn_.setInitialState(initialX0);

				if( stopRequested == true ){
					System.out.print("The simulation is cancelled. \n"); //The temporary result is saved at "+tmpfilename.toString()+". \n
					finalizeAfterFail();
					return;
				}

				/** save for plot **/	
				grn_.setTimeScale(timeScale);				
				grn_.setTimeSeries(timeSeries);
				grn_.setTraj_itsValue(numSeries);
				grn_.setTraj_maxTime(maxt);
				grn_.setTraj_noise((Double) sdeDiffusionCoeff_.getModel().getValue());
				grn_.setTraj_model("gillespie");


				finalizeAfterSuccess();
				System.out.print("Done!\n"); 


				/** save result **/
				saveResult(tmpfilename, dir);
		

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
			}		
			
		}

		
		private int samplePoints(Object[] symbolValues, double[] tempTime, DoubleMatrix1D timeScale, DoubleMatrix2D timeSeries) {
			
			for(int i=0;i<symbolValues.length;i++){
				double[] symbolValue = (double []) symbolValues[i];
		
				for(int j=0;j<symbolValue.length;j++)
					timeSeries.set(i, j, symbolValue[j]/1000);
				timeScale.set(i, tempTime[i]);
			}

			return timeScale.size();
		}
		
		
		private void runODE(Integer numSeries, Integer maxt, boolean randomInitial, int saveEveryMTraj, int saveEveryTStep, boolean isCutTime, boolean isCutIts, int maxPointsPerTraj) {
			System.out.print("Simulate ODE\n"); 			


			/** run solver **/
			ODESolver deSolver_ = new ODESolver(grn_, maxt);

			if( randomInitial ){
				deSolver_.setUpBoundary(upbound);
				deSolver_.setLowBoundary(lowbound);
			}


			/** set parameters **/
			deSolver_.setIslandscape(false);
			deSolver_.setRandomInitial(randomInitial);		

			/** backup initial values **/
			DoubleMatrix1D initialX0 = grn_.getInitialState().copy();

			/** create a temp dir **/
			File dir = new File("./"+ "NetLand_" 
					+ new SimpleDateFormat("yyyyMMddhhmmss").format(new Date())  
					+ "_tempSimulationResult");  
			dir.mkdirs(); 

			/** create tmp file **/
			File tmpfilename = createTmpFile(dir, numSeries, grn_.getSize(), maxt, (double) sdeDiffusionCoeff_.getModel().getValue(), "ode");			

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

					if( !deSolver_.solveEquations_ODE() ){
						finalizeAfterFail();
						return;
					}
						


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
					grn_.setInitialState(deSolver_.getTimeSeries().viewRow(deSolver_.getTimeSeries().rows()-1).copy());


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
				
				//System.out.print("ODE its: "+System.currentTimeMillis()+"\n"); 
			}//end of while

			/** reset initial values **/
			grn_.setInitialState(initialX0);

			if( stopRequested == true ){
				System.out.print("The simulation is cancelled. \n"); //The temporary result is saved at "+tmpfilename.toString()+". \n
				finalizeAfterFail();
				return;
			}

			/** save for plot **/	
			grn_.setTimeScale(timeScale);				
			grn_.setTimeSeries(timeSeries);
			grn_.setTraj_itsValue(numSeries);
			grn_.setTraj_maxTime(maxt);
			grn_.setTraj_noise((Double) sdeDiffusionCoeff_.getModel().getValue());
			grn_.setTraj_model("ode");


			finalizeAfterSuccess();
			System.out.print("Done!\n"); 


			/** save result **/
			saveResult(tmpfilename, dir);			
		}



		private int samplePoints(ODESolver dSolver) {
			ArrayList<Integer> index = new ArrayList<Integer>();
			index.add(0);		
			for(int i=1;i<dSolver.getTimeSeries().rows()-1;i++){
				for(int j=0;j<grn.getSize();j++){
					if(dSolver.getTimeSeries().get(i, j)-dSolver.getTimeSeries().get(i-1, j)>0.0001 && dSolver.getTimeSeries().get(i, j)-dSolver.getTimeSeries().get(i+1, j)>0.0001){
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


		private int calculateTStep(double memoryForOneTraj, double maxt) {
			/** get JVM free memory **/
			MonitorServiceImpl monitorSys = new MonitorServiceImpl();
			long freeMemory = monitorSys.getTotalFreeMemory(); //monitorSys.getFreeMemory()/1000; //MB

			int t = (int) (freeMemory*0.5/memoryForOneTraj*maxt);

			return t;
		}

		private int calculateMTraj(double memoryForOneTraj) {
			/** get JVM free memory **/
			MonitorServiceImpl monitorSys = new MonitorServiceImpl();
			long freeMemory = monitorSys.getTotalFreeMemory(); //monitorSys.getFreeMemory()/1000; //MB

			int m = (int) (freeMemory*0.5/memoryForOneTraj);

			return m;
		}

		private void memoryWarning(double leastMemoryReq, int maxPointsPerTraj) {
			
//			if( maxPointsPerTraj >= 0 ){
				JOptionPane.showMessageDialog(new Frame(), "Not enough memory!\n" +
						"At least "+leastMemoryReq+" MB is required.\n"+
						"If enough amounts of RAM are installed on this computer, try to run the program \n" +
						"with the command-line argument -XmxXXm to set the maximum memory of JVM\n" +
						"Or please reduce the number of simulations and simulation time. \n", "Error", JOptionPane.INFORMATION_MESSAGE);
				finalizeAfterFail();
				this.stop();
				return;		
//			}else{	
//				Object[] options = {"Continue","Cancel"};
//
//				int response = JOptionPane.showOptionDialog(null, "Not enough memory to plot the result! Maximumly "+maxPointsPerTraj+" points per trajectory.\n" +
//						"At least "+Double.toString(leastMemoryReq)+" MB is required.\n"+
//						"If enough amounts of RAM are installed on this computer, try to run the program \n" +
//						"with the command-line argument -XmxXXm to set maximum memory of JVM\n" +
//						"Or please reduce the number of simulations and simulation time. \n" +
//						"If choose to 'continue', "+maxPointsPerTraj+" points per trajectory will be plotted. ","Warning: Out of memory!",JOptionPane.YES_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);	
//
//				if(response==0) {    
//					return;
//				}else if(response==1) {  
//					//cancel
//					finalizeAfterFail();
//					return;					
//				}
//			}
		}

		private void runSDE(int numSeries, int maxt, boolean randomInitial, double step, int saveEveryMTraj, int saveEveryTStep, boolean isCutTime, boolean isCutIts, int maxPointsPerTraj) {
			System.out.print("Simulate SDE\n"); 

			/** run solver **/
			SDESolver deSolver_ = new SDESolver(grn_, false, (Double) sdeDiffusionCoeff_.getModel().getValue());			

			/** set parameters **/
			deSolver_.setSimulateODE(false);
			deSolver_.setSimulateSDE(true);
			deSolver_.setODE(false);

			deSolver_.setRandomInitial(randomInitial);
			deSolver_.setDt(step);
			deSolver_.setIslandscape(false);
			deSolver_.getTs().setStopRun(stopRequested);
			deSolver_.setNumSeries(numSeries);

			if( randomInitial ){
				deSolver_.setUpBoundary(upbound);
				deSolver_.setLowBoundary(lowbound);
			}		

//			/** backup initial values **/
//			DoubleMatrix1D initialX0 = grn_.getInitialState().copy();


			/** create a temp dir **/
			File dir = new File("./"+ "NetLand_" 
					+ new SimpleDateFormat("yyyyMMddhhmmss").format(new Date())  
					+ "_tempSimulationResult");  
			dir.mkdirs(); 


			/** create tmp file **/
			File tmpfilename = createTmpFile(dir, numSeries, grn_.getSize(), maxt, (double) sdeDiffusionCoeff_.getModel().getValue(), "sde");			

			if( tmpfilename == null ){
				dir.delete();
				finalizeAfterFail();
				return;
			}		

			/** each iteration **/		
			int its = 1; //output iterations


			/** set temp storage, for saving **/
			ArrayList<SDETimeSeriesExperiment> tss = new ArrayList<SDETimeSeriesExperiment>();

			/** data to plot **/
			ArrayList<DoubleMatrix2D> timeSeries = new ArrayList<DoubleMatrix2D>();
			ArrayList<DoubleMatrix1D> timeScale = new ArrayList<DoubleMatrix1D>();			


			int steps = saveEveryTStep; //simulation time for each run
			int numPoints = 0; //record the number of sampled points
			double previoudTime = 0; //if isCutTime=ture, record the end time of the previous segment 
			cern.colt.matrix.DoubleFactory1D Factory1D = cern.colt.matrix.DoubleFactory1D.dense;
			cern.colt.matrix.DoubleFactory2D Factory2D = cern.colt.matrix.DoubleFactory2D.dense;


			try
			{			
				while( !stopRequested && its<=numSeries ){					
					System.out.print("\nIts: "+its+"\n"); 

					/** reset parameters **/
					numPoints = 0;			
					previoudTime = 0;


					/** set time for each traj **/
					for(int j=0;j<maxt;j+=saveEveryTStep){		
						steps = saveEveryTStep;				
						if( j+saveEveryTStep>maxt )
							steps = maxt-j;

						deSolver_.setMaxt(steps);
						deSolver_.setNumTimePoints((int) (steps/step+1));		

						if( deSolver_.solveEquations_SDE() ){

							/** sample points **/
							if( deSolver_.getTs().getTimeSeries() == null || deSolver_.getTs().getTimeSeries().rows()==0 ){
								its--;
								break;
							}else if( deSolver_.getTs().getTimeSeries().rows()>200 || deSolver_.getTs().getTimeSeries().rows()==1 ){
								numPoints = samplePoints(deSolver_.getTs());
							}else
								numPoints = deSolver_.getTs().getTimeSeries().rows();
							

//							/** update time scale **/
//							deSolver_.getTs().setTimeScale(Transform.plus(deSolver_.getTs().getTimeScale(), previoudTime));

//							/** new initial state from previous calculation **/
//							grn_.setInitialState(deSolver_.getTs().getTimeSeries().viewRow(deSolver_.getTs().getTimeSeries().rows()-1).copy());


							if( numPoints>maxPointsPerTraj ){
								
								if( previoudTime==0 ){
									timeSeries.add(selectNPoints(deSolver_.getTs().getTimeSeries(), 200));
									timeScale.add(selectNPoints(deSolver_.getTs().getTimeScale(), 200));
								}else{
									timeSeries.set(timeSeries.size()-1, Factory2D.appendRows(timeSeries.get(timeSeries.size()-1), selectNPoints(deSolver_.getTs().getTimeSeries().copy(),maxPointsPerTraj)));							
									timeScale.set(timeScale.size()-1, Factory1D.append(timeScale.get(timeScale.size()-1), selectNPoints(deSolver_.getTs().getTimeScale().copy(),maxPointsPerTraj)));
								}

							}else{
								if( previoudTime==0 ){
									timeSeries.add(deSolver_.getTs().getTimeSeries().copy());
									timeScale.add(deSolver_.getTs().getTimeScale().copy());
								}else{
									timeSeries.set(timeSeries.size()-1, Factory2D.appendRows(timeSeries.get(timeSeries.size()-1), deSolver_.getTs().getTimeSeries()));							
									timeScale.set(timeScale.size()-1, Factory1D.append(timeScale.get(timeScale.size()-1), deSolver_.getTs().getTimeScale()));
								}						
							}


							tss.add(deSolver_.getTs());


							/** save records **/
							if( its%saveEveryMTraj==0 ){
								saveTmpFile(tss, grn.getId(), tmpfilename, its-saveEveryMTraj, isCutTime);
								tss.clear();
								tss.trimToSize();
							}else if( its==numSeries ){
								saveTmpFile(tss, grn.getId(), tmpfilename, ((int)Math.floor(numSeries/saveEveryMTraj))*saveEveryMTraj, isCutTime);
								tss.clear();
								tss.trimToSize();
								break;
							}

//							/** cumulate time **/
//							previoudTime = deSolver_.getTs().getTimeScale().get(deSolver_.getTs().getTimeScale().size()-1);

						}else{
							stopRequested = true;
							JOptionPane.showMessageDialog(new Frame(), "The simulation is cancelled.\n" 
									, "Error", JOptionPane.INFORMATION_MESSAGE); //"The temporary result is saved at "+tmpfilename.toString()+".\n" +
							//"Reload the file to continue.\n"
							finalizeAfterFail();
							break;
						}
					}//end of for

					its++;
					
					//System.out.print("SDE its: "+System.currentTimeMillis()+"\n"); 
				}//end of while

//				/** reset initial values **/
//				grn_.setInitialState(initialX0);


				if( stopRequested == true ){
					System.out.print("The simulation is cancelled. \n "); //The temporary result is saved at "+tmpfilename.toString()+" \n
					finalizeAfterFail();
					return;
				}

				/** save for plot **/	
				grn_.setTimeScale(timeScale);				
				grn_.setTimeSeries(timeSeries);
				grn_.setTraj_itsValue(numSeries);
				grn_.setTraj_maxTime(maxt);
				grn_.setTraj_noise((Double) sdeDiffusionCoeff_.getModel().getValue());
				grn_.setTraj_model("sde");


				finalizeAfterSuccess();
				System.out.print("Done!\n"); 


				/** save result **/
				saveResult(tmpfilename, dir);

			}
			catch (OutOfMemoryError e)
			{
				JOptionPane.showMessageDialog(new Frame(), "There is not enough memory available to run this program.\n" +
						"Quit one or more programs, and then try again.\n" +
						"If enough amounts of RAM are installed on this computer, try to run the program\n " +
						"with the command-line argument -Xmx[xxx]m to set the maximum memory.\n", "Out of memory",  JOptionPane.INFORMATION_MESSAGE);
				finalizeAfterFail();
			}
			catch (IllegalArgumentException e)
			{
				JOptionPane.showMessageDialog(new Frame(), "Illegal argument", "Error", JOptionPane.INFORMATION_MESSAGE);
				MsgManager.Messages.errorMessage(e, "Error", "");
				finalizeAfterFail();
			}
			catch (CancelException e)
			{
				JOptionPane.showMessageDialog(new Frame(), "Program is interrupted!", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
				MsgManager.Messages.errorMessage(e, "Error", "");
				finalizeAfterFail();
			}
			catch (ConvergenceException e)
			{
				JOptionPane.showMessageDialog(new Frame(), "Unable to converge", "Error", JOptionPane.INFORMATION_MESSAGE);
				MsgManager.Messages.errorMessage(e, "Error", "");
				finalizeAfterFail();
			}
			catch (RuntimeException e)
			{
				JOptionPane.showMessageDialog(new Frame(), "Runtime exception", "Error", JOptionPane.INFORMATION_MESSAGE);
				MsgManager.Messages.errorMessage(e, "Error", "");
				finalizeAfterFail();
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(new Frame(), "Error encountered", "Error", JOptionPane.INFORMATION_MESSAGE);
				MsgManager.Messages.errorMessage(e, "Error", "");
				finalizeAfterFail();
			}
		}


		private DoubleMatrix1D selectNPoints(DoubleMatrix1D timeScale, int maxPointsPerTraj) {
			int[] index = incrementByN(0, maxPointsPerTraj, timeScale.size()-1);

			return timeScale.viewSelection(index).copy();
		}


		private DoubleMatrix2D selectNPoints(DoubleMatrix2D timeSeries, int maxPointsPerTraj) {
			int[] index = incrementByN(0, maxPointsPerTraj, timeSeries.rows()-1);

			int[] y = new int[timeSeries.columns()];
			for(int i=0;i<timeSeries.columns();i++)
				y[i] = i;

			return timeSeries.viewSelection(index, y).copy();
		}

		public int[] incrementByN(int start, int n, int end) {
			double aStep = (end-start)*1.0/n;

			int length = (int) (Math.rint((end-start)/aStep)+1) + 1;
			int[] temp=new int[length];
			for (int i = 0; i < temp.length-2; i++){
				double t = (Math.rint(1000*(start+aStep*i)))/1000.0;
				temp[i] = (int) Math.rint(t);
			}
			temp[temp.length-2] = end-1;
			temp[temp.length-1]	= end;
					
			return temp;
		}

		private int calculateMaxPointsPerTraj(double memoryPerPointAllTraj) {
			/** get JVM free memory **/
			MonitorServiceImpl monitorSys = new MonitorServiceImpl();
			long freeMemory = monitorSys.getTotalFreeMemory(); //monitorSys.getFreeMemory()/1000; //MB

			int maxPointsPerTraj = (int) (freeMemory*0.2/memoryPerPointAllTraj);
			return maxPointsPerTraj;		
		}

		private int samplePoints(SDETimeSeriesExperiment ts) {
			ArrayList<Integer> index = new ArrayList<Integer>();
			index.add(0);		
			for(int i=1;i<ts.getTimeSeries().rows()-1;i++){
				for(int j=0;j<grn.getSize();j++){
					if(ts.getTimeSeries().get(i, j)-ts.getTimeSeries().get(i-1, j)>0.00001 && ts.getTimeSeries().get(i, j)-ts.getTimeSeries().get(i+1, j)>0.00001){
						index.add(i); break;
					}else if(ts.getTimeSeries().get(i, j)-ts.getTimeSeries().get(i-1, j)==0 && ts.getTimeSeries().get(i, j)-ts.getTimeSeries().get(i+1, j)==0){
						index.add(i); break;
					}else if(ts.getTimeSeries().get(i, j)-ts.getTimeSeries().get(i-1, j)!=0 && ts.getTimeSeries().get(i, j)-ts.getTimeSeries().get(i+1, j)==0){
						index.add(i); break;
					}
				}			
			}

			if( index.size() == 1 )
				index.add(ts.getTimeSeries().rows()-1);
			else{
				index.add(ts.getTimeSeries().rows()-2);
				index.add(ts.getTimeSeries().rows()-1);
			}
			

			int[] y = new int[grn.getSize()];
			for(int i=0;i<grn.getSize();i++)
				y[i] = i;

			int[] x = new int[index.size()];
			for(int i=0;i<index.size();i++)
				x[i] = index.get(i);

			
			ts.setTimeSeries(ts.getTimeSeries().viewSelection(x, y).copy());
			ts.setTimeScale(ts.getTimeScale().viewSelection(x).copy());


			return x.length;
		}

		private void saveResult(File tmpfilename, File saveTempDir) {
			JFrame frame = new JFrame();
			int n = JOptionPane.showConfirmDialog(frame, "Would you like to save the result to a file?", "Export simulation result", JOptionPane.YES_NO_OPTION);
			if (n == JOptionPane.YES_OPTION) {
				JFileChooser fc = new JFileChooser();
				int retVal = fc.showSaveDialog(frame);                 // let frame to become a showDialog
				if (retVal == JFileChooser.APPROVE_OPTION) {
					File resultFile = fc.getSelectedFile();   // where is the selected file	
					copyfile(tmpfilename, resultFile);
				} else {
					System.out.println("Cancelled by user!");
				}
			} else if (n == JOptionPane.NO_OPTION) {
				System.out.println("Don't save it!");
			}

			tmpfilename.delete();
			saveTempDir.delete();
		}

		private void copyfile(File source, File target) {  
			FileChannel in = null;  
			FileChannel out = null;  
			FileInputStream inStream = null;  
			FileOutputStream outStream = null;  
			try {  
				inStream = new FileInputStream(source);  
				outStream = new FileOutputStream(target);  
				in = inStream.getChannel();  
				out = outStream.getChannel();  
				in.transferTo(0, in.size(), out);  
			} catch (IOException e) {  
				e.printStackTrace();  
			} finally {  	    	  
				try {
					inStream.close();  
					in.close();
					outStream.close();
					out.close();
				} catch (IOException e) {
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
				grn_.setId("NetLand_"+grn_.getId());
				grn_.writeSBML(url);

				//part2: save time series to file
				FileWriter fw = new FileWriter(fTemp.getAbsolutePath(), true);

				//parameters
				fw.write("\n\nTraj\t"+numTimeSeries+"\t"+dimension+"\t"+maxt+"\t"+noise+"\t"+model+"\n");
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


		private boolean checkMemory(double leastMemoryReq) {
			/** get JVM free memory **/
			MonitorServiceImpl monitorSys = new MonitorServiceImpl();
			long freeMemory = monitorSys.getTotalFreeMemory(); //monitorSys.getFreeMemory()/1000; //MB

			/** check memory **/
			if( leastMemoryReq<0 || freeMemory*0.6<leastMemoryReq )
				return false;	

			return true;
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


		/**  
		 * Save temp file
		 * @param ts
		 * @param networkName
		 * @param outputDirectory_
		 * @param tmpfilename 
		 * @param isCutTime 
		 * @throws Exception
		 */
		private void saveTmpFile(ArrayList<SDETimeSeriesExperiment> ts, String networkName, File tmpfilename, int fileIndex, boolean isCutTime) throws Exception{
			// save time series to file
			FileWriter fw = new FileWriter(tmpfilename.getAbsolutePath(), true);

			if( isCutTime )
				ts.get(0).printAll(fw);	
			else
				for(int i=0;i<ts.size();i++){
					fw.write(Integer.toString(i+fileIndex)+"\t"+ts.get(i).getTimeSeries().rows()+"\n");
					ts.get(i).printAll(fw);			
				}

			fw.close();

			System.out.print("Save the simulated data to "+tmpfilename.toString()+".\n");
		}


		private DoubleMatrix1D randomInitial(double upBoundary, double lowBoundary) {
			int dimension = grn_.getNodes().size();
			Random random = new Random();
			DoubleMatrix1D s = new DenseDoubleMatrix1D(dimension);

			for(int i=0;i<dimension;i++)
				s.set(i, random.nextDouble() * (upBoundary - lowBoundary) + lowBoundary);

			//			for(int i=dimension;i<dimension*2;i++)
			//				s[i] = 0.1;

			return s;
		}

		private String writeCMDL(DoubleMatrix1D X0) throws IOException{
			// Create a new SBMLDocument object, using SBML Level 1 Version 2.		
			String modelText = "";

			//create compartment if necessary
			String compartment = "cC1 = 1000.0;\n";
			modelText += compartment;


			ArrayList<Node> nodes_ = grn_.getNodes();
			ArrayList<Gene> species = grn_.getSpecies();
			DoubleMatrix1D species_initialState = grn_.getSpecies_initialState();

			//write species and parameters	
			// species and fake species_gene
			int size = grn_.getSize();
			for (int s=0; s < size; s++) { // save gene as species
				String geneName = ((Gene)nodes_.get(s)).getLabel();

				//String content = geneName+"="+(initialState.get(s)*sizeCompartment)+";\n";
				String content = geneName+"="+X0.get(s)+";\n";
				modelText += content;
				content = geneName+"_NetLandGene=1;\n";
				modelText += content;

				content = geneName+" @ cC1;\n"+geneName+"_NetLandGene @ cC1;\n";
				modelText += content;
			}	


			//write species
			size = species.size();
			for (int s=0; s < size; s++) { // save gene as species
				if( !nodes_.contains(species.get(s)) ){
					String content = ((Gene)species.get(s)).getLabel()+"="+species_initialState.get(s)+";\n";
					modelText += content;
				}
			}


			//create degradation 
			modelText += "degradation=1;\n";

			//parameters
			ArrayList<String> names = grn_.getParameterNames_(); // parameters names
			ArrayList<Double> values = grn_.getParameterValues_(); // parameters values

			// save gene parameters (note, the first param is the degradation rate)
			for (int p=0; p<names.size(); p++) {
				boolean flag = true;

				if( flag ){
					String content = names.get(p) + "=" + values.get(p);
					modelText += content+";\n";
				}
			}		


			// SET SYNTHESIS AND DEGRADATION REACTIONS FOR EVERY GENE		
			for (int i=0; i<grn_.getSize(); i++) {
				// the ID of gene i
				String currentGeneID = nodes_.get(i).getLabel();
				Gene targetGene = (Gene) nodes_.get(i);


				String degradationRxn = ""; String syntheticRxn = "";


				String wholeEquation = targetGene.getCombination();


				for(int ii=0;ii<wholeEquation.length();ii++){
					wholeEquation.substring(ii, ii+1);
				}


				//remove brakets in (-1) || (x13) from wholeEquation
				wholeEquation = wholeEquation.replaceAll("\\((-*[a-zA-Z0-9]+)\\)", "$1");


				//extract degradation part from combination
				parseEquation parseTemple = new parseEquation(wholeEquation);
				degradationRxn = parseTemple.getNegativePart();
				syntheticRxn = parseTemple.getPositivePart();		
				syntheticRxn = syntheticRxn.replaceAll("\\+\\+", "+");
				degradationRxn = degradationRxn.replaceAll("\\+\\+", "+");


				//replace each species with species/cC1
				size = grn_.getSize();

				for (int s=0; s < size; s++) { // save gene as species
					String content = "([\\(\\)\\*\\.\\-\\+\\/\\^])"+((Gene)nodes_.get(s)).getLabel()+"([\\(\\)\\*\\.\\-\\+\\/\\^]|$)";		
					String newcontent = "("+((Gene)nodes_.get(s)).getLabel()+"/cC1)";

					Pattern  pattern = Pattern.compile(content);
					Matcher  matcher = pattern.matcher(degradationRxn);
					//deg
					if( degradationRxn.equals("") ){
						degradationRxn = "0";
					}else{
						if( degradationRxn.equals(((Gene)nodes_.get(s)).getLabel()) ){
							degradationRxn = newcontent;
						}else{
							StringBuffer sb = new StringBuffer();  
							while(matcher.find()){  
								String matchStr = matcher.group();  
								matchStr = matchStr.replaceAll(content, "$1"+newcontent+"$2");  
								matcher.appendReplacement(sb, matchStr);  
							}  
							matcher.appendTail(sb);  
							degradationRxn = sb.toString();
						}
					}

					//syn
					Matcher  matcher1 = pattern.matcher(syntheticRxn);
					StringBuffer sb1 = new StringBuffer();  
					while(matcher1.find()){  
						String matchStr = matcher1.group();  
						matchStr = matchStr.replaceAll(content, "$1"+newcontent+"$2");  
						matcher1.appendReplacement(sb1, matchStr);  
					}  
					matcher1.appendTail(sb1);  
					syntheticRxn = sb1.toString();
				}

				// SYNTHESIS REACTION		
				String reactionId = currentGeneID + "_synthesis, ";
				String equation = currentGeneID+"_NetLandGene->"+currentGeneID+", ";
				String rate = "[cC1*("+syntheticRxn+")];";	

				modelText += reactionId + equation + rate + "\n";

				// degradation reaction			
				reactionId = currentGeneID + "_degradation, ";
				equation = currentGeneID+"->degradation, ";
				rate = "[cC1*("+degradationRxn+")];";	
				modelText += reactionId + equation + rate + "\n";
			}

			return modelText;
		}
		
		private Model processModel(String modelText) throws InvalidInputException, IOException, DataNotFoundException
		{
			Model model = null;
			IModelBuilder modelBuilder = null;	
			ModelBuilderCommandLanguage a = new ModelBuilderCommandLanguage();      
			modelBuilder = (IModelBuilder) a; 
			
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			modelBuilder.writeModel(modelText, outputStream);
			byte []bytes = outputStream.toByteArray();
			ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

			IncludeHandler includeHandler = new IncludeHandler();

			model = modelBuilder.buildModel(inputStream, includeHandler);

			return model;
		}


		// ----------------------------------------------------------------------------

		private void finalizeAfterSuccess()  
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
			//System.out.print("ODE end: "+System.currentTimeMillis()+"\n"); 
		}


		// ----------------------------------------------------------------------------

		private void finalizeAfterFail(){
			snake_.stop();
			myCardLayout_.show(runButtonAndSnakePanel_, runPanel_.getName());
			//escapeAction(); // close the simulation window
		}
	}


	


	
}

