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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import org.math.plot.Plot2DPanel;

import ch.epfl.lis.animation.Snake;



/** This dialog handles all the simulations parameters of steady-states and time-series experiments.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 *
 */
public class SimulationWindow extends JDialog {
	
	protected JPanel runPanel_;
	protected JPanel snakePanel_;
	
	private static final long serialVersionUID = 1L;
	
	protected JSpinner numTimeSeries_;
	@SuppressWarnings("rawtypes")
	protected JComboBox model_;
	protected JSpinner tmax_;
	protected JSpinner sdeDiffusionCoeff_;
	
	protected JButton browse_;
	protected JTextArea userPath_;
	protected JLabel durationOfSeriesLabel_;
	protected JLabel numTimeSeriesLabel_;
	
	
	protected JButton runButton_;
	protected JButton cancelButton_;
	//protected JPanel validationPanel_;

	protected JPanel runButtonAndSnakePanel_;
	protected Snake snake_;
	protected JPanel centerPanel_;
	protected final CardLayout myCardLayout_ = new CardLayout();
	
	protected JPanel trajPlot;
	protected JButton clear_;
	protected JRadioButton fixButton;
	protected JRadioButton randomButton;
	private JPanel cancelPanel_;

	protected JButton analyzeResult;
	protected JButton generateSteadyStateConcentrationGraph;
	protected JButton setGateSignal;
	private JPanel analyzePanel;
	
    /** Logger for this class */
    @SuppressWarnings("unused")
	private static Logger log_ = Logger.getLogger(SimulationWindow.class.getName());

    

	@SuppressWarnings("rawtypes")
	public SimulationWindow(Frame aFrame) {
		super(aFrame);
		setResizable(false);	
		setSize(820, 490);
		setTitle("Generate trajectories");

		
		//set icon
		Image image;
		try {
			URL ab = ClassLoader.getSystemResource("WidgetsButtons/rsc/buttons/traj.png");
			image = ImageIO.read(ab);
			setIconImage(image);
		} catch (IOException e1) {
			MsgManager.Messages.errorMessage(e1, "Error", "");
		}
		
		centerPanel_ = new JPanel();
		centerPanel_.setBorder(new EmptyBorder(0, 0, 10, 0));
		//centerPanel_.setBackground(Color.WHITE);
		getContentPane().add(centerPanel_);
		
	
		//select model
		JPanel options = new JPanel();
		options.setLayout(new BoxLayout(options, BoxLayout.X_AXIS));
		
		final JLabel label1 = new JLabel();
		label1.setText("Model: ");
		options.add(label1);

		model_ = new JComboBox();
		model_.setFocusable(false);
		//model_.setBackground(Color.WHITE);
		options.add(model_);
		
		options.setBorder(new EmptyBorder(5, 0, 5, 0));
		
		//num traj
		JPanel options1 = new JPanel();
		options1.setLayout(new BoxLayout(options1, BoxLayout.X_AXIS));
		
		numTimeSeriesLabel_ = new JLabel();
		numTimeSeriesLabel_.setText("Number of series: ");
		options1.add(numTimeSeriesLabel_);
		
		numTimeSeries_ = new JSpinner();
		numTimeSeries_.setFocusable(false);
		options1.add(numTimeSeries_);

		options1.setBorder(new EmptyBorder(5, 0, 5, 0));
		
		// Set model of "number of time series" spinner
		SpinnerNumberModel model = new SpinnerNumberModel();
		model.setMinimum(1);
		model.setMaximum(10000);
		model.setStepSize(1);
		model.setValue(10);
		numTimeSeries_.setModel(model);

		((JSpinner.NumberEditor)numTimeSeries_.getEditor()).getTextField().setBackground(new Color(240,240,240));

		
		//time
		JPanel options2 = new JPanel();
		options2.setLayout(new BoxLayout(options2, BoxLayout.X_AXIS));
		
		durationOfSeriesLabel_ = new JLabel();
		durationOfSeriesLabel_.setText("Time: ");
		options2.add(durationOfSeriesLabel_);
		
		tmax_ = new JSpinner();
		tmax_.setFocusable(false);
		options2.add(tmax_);
		options2.setBorder(new EmptyBorder(5, 0, 5, 0));
		
		// Set model of "duration" spinner
		model = new SpinnerNumberModel();
		model.setMinimum(1);
		model.setMaximum(100000);
		model.setStepSize(1);
		model.setValue(128);
		tmax_.setModel(model);
	
		((JSpinner.NumberEditor)tmax_.getEditor()).getTextField().setBackground(new Color(240,240,240));
		
		
		//noise
		JPanel options3 = new JPanel();
		options3.setLayout(new BoxLayout(options3, BoxLayout.X_AXIS));
		
		final JLabel label14 = new JLabel();
		//label14.setBackground(Color.WHITE);
		label14.setText("Noise (SDEs): ");
		options3.add(label14);

		sdeDiffusionCoeff_ = new JSpinner();
		sdeDiffusionCoeff_.setFocusable(false);		
		options3.add(sdeDiffusionCoeff_);
		//sdeDiffusionCoeff_.setBackground(new Color(0,0,0,0));

		//options3.setBorder(new EmptyBorder(5, 0, 5, 0));
		// Diffusion multiplier (SDE only)
		model = new SpinnerNumberModel();
		model.setMinimum(0.0);
		model.setMaximum(10.);
		model.setStepSize(0.01);
		model.setValue(0.05);
		sdeDiffusionCoeff_.setModel(model);
		
		((JSpinner.NumberEditor)sdeDiffusionCoeff_.getEditor()).getTextField().setBackground(new Color(214,214,214));
		
		
		//initials
		JLabel initialLabel = new JLabel();
		initialLabel.setText("Initials: ");
		
		ButtonGroup bg = new ButtonGroup();
		fixButton = new JRadioButton("Fixed initials");
		fixButton.setSelected(true);
		//fixButton.setBackground(new Color(187,207,232));
		bg.add(fixButton);

		randomButton = new JRadioButton("Random initials");
		//randomButton.setBackground(new Color(187,207,232));
		bg.add(randomButton);
		
		JPanel options5 = new JPanel();
		options5.setLayout(new BoxLayout(options5, BoxLayout.X_AXIS));
		options5.add(initialLabel);
		options5.add(fixButton);
		options5.add(randomButton);
		options5.setBorder(new EmptyBorder(5, 0, 5, 0));
		
		//output
		JPanel options4 = new JPanel();
		options4.setLayout(new BoxLayout(options4, BoxLayout.X_AXIS));
		
		final JLabel label17 = new JLabel();
		label17.setText("Save result:");
		options4.add(label17);
		
		userPath_ = new JTextArea();
		userPath_.setBackground(Color.WHITE);
		userPath_.setEditable(false);
		userPath_.setLineWrap(true);
		userPath_.setColumns(10);
		userPath_.setPreferredSize(new Dimension(50,50));		
		options4.add(userPath_);

		options4.setBorder(new EmptyBorder(5, 0, 5, 0));
		
		JPanel options6 = new JPanel();
		options6.setLayout(new BoxLayout(options6, BoxLayout.X_AXIS));
		
		browse_ = new JButton();
		browse_.setText("Browse");
		
		clear_ = new JButton();
		clear_.setText("Clear");
		
		options6.add(browse_);
		options6.add(clear_);
		
		
		//buttons
		runButtonAndSnakePanel_ = new JPanel();
		runButtonAndSnakePanel_.setLayout(myCardLayout_);
		runButtonAndSnakePanel_.setBackground(null);
		runButtonAndSnakePanel_.setBorder(new EmptyBorder(0,50,0,0));
//		runButtonAndSnakePanel_.setPreferredSize(new Dimension(50,50));

		runPanel_ = new JPanel();
		runPanel_.setBackground(null);
		runPanel_.setLayout(new BoxLayout(runPanel_, BoxLayout.X_AXIS));
		runPanel_.setName("runPanel");
		runButtonAndSnakePanel_.add(runPanel_, runPanel_.getName());
		
		runButton_ = new JButton();
		runPanel_.add(runButton_);
		runButton_.setBackground(UIManager.getColor("Button.background"));
		runButton_.setName("computeButton");
		runButton_.setText("Simulate");
//		runButton_.setBorder(new EmptyBorder(0,50,0,0));

		snakePanel_ = new JPanel();
		snakePanel_.setLayout(new BorderLayout());
		snakePanel_.setBackground(null);
		snakePanel_.setName("snakePanel");
		runButtonAndSnakePanel_.add(snakePanel_, snakePanel_.getName());
			
		snake_ = new Snake();
		snakePanel_.add(snake_, BorderLayout.EAST);
		snake_.setName("snake_");
		//snake_.setBackground(Color.WHITE);
		snake_.setPreferredSize(new Dimension(50,50));

		cancelPanel_ = new JPanel();
		cancelPanel_.setLayout(new BoxLayout(cancelPanel_, BoxLayout.X_AXIS));
		cancelButton_ = new JButton();
		cancelPanel_.add(cancelButton_);
		cancelButton_.setMnemonic(KeyEvent.VK_C);
		cancelButton_.setBackground(UIManager.getColor("Button.background"));
		cancelButton_.setText("Cancel");
			
		//cancelPanel_.setBorder(new EmptyBorder(20,5,45,0));
				
		analyzeResult = new JButton("Analyze Result");
		cancelPanel_.add(analyzeResult);
		
		//analyzePanel.setBorder(new EmptyBorder(-120,15,45,10));
		//runButtonAndSnakePanel_.setBorder(BorderFactory.createLineBorder(Color.red));
		//cancelPanel_.setBorder(BorderFactory.createLineBorder(Color.red));
		
		JPanel buttonsPanel = new JPanel();
//	    buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
		buttonsPanel.setLayout(new BorderLayout());
	    
		buttonsPanel.add(runButtonAndSnakePanel_, BorderLayout.CENTER);
		buttonsPanel.add(cancelPanel_, BorderLayout.LINE_END);
		
		
		/** LAYOUT **/
		JPanel setPanel = new JPanel();
		setPanel.setLayout(new BoxLayout(setPanel, BoxLayout.Y_AXIS));
		setPanel.setBorder(new EmptyBorder(30, 10, 0, 10));
//		setPanel.setBackground(new Color(187,207,232));
//		options.setBackground(new Color(187,207,232));
//		options1.setBackground(new Color(187,207,232));
//		options2.setBackground(new Color(187,207,232));
//		options3.setBackground(new Color(187,207,232));
//		options4.setBackground(new Color(187,207,232));
//		options5.setBackground(new Color(187,207,232));
//		options6.setBackground(new Color(187,207,232));
//		runPanel_.setBackground(new Color(187,207,232));
//		cancelPanel_.setBackground(new Color(187,207,232));
//		runButtonAndSnakePanel_.setBackground(new Color(187,207,232));
//		buttonsPanel.setBackground(new Color(187,207,232));
//		snake_.setBackground(new Color(187,207,232));
		
		setPanel.add(options);
		setPanel.add(options1);
		setPanel.add(options2);
		setPanel.add(options3);
		setPanel.add(options5);
		setPanel.add(options4);
		setPanel.add(options6);	
		setPanel.add(buttonsPanel);
		
		
		trajPlot = new JPanel();
		//trajPlot.setBackground(new Color(187,207,232));
		trajPlot.add(trajectoryTabb());
		
		centerPanel_.setLayout(new BoxLayout(centerPanel_,BoxLayout.X_AXIS));
		centerPanel_.add(setPanel);
		centerPanel_.add(trajPlot);
		
//		JScrollPane scrollPane11 = new JScrollPane(centerPanel_);  
//		getContentPane().add(scrollPane11);

		setLocationRelativeTo(aFrame);
		
	}
	
	
	private JPanel trajectoryTabb(){
		JPanel trajectoryPanel = new JPanel();
		
		final Plot2DPanel plot = new Plot2DPanel();
        plot.addLegend("SOUTH");
        plot.setAxisLabel(0, "t");
        plot.setAxisLabel(1, "Expression Level");
        plot.setPreferredSize(new Dimension(440,390));
		
        double[] x = { 0 };
        double[] y = { 0 };

        plot.addLinePlot("my plot", x, y);	
        trajectoryPanel.add(plot);
        
        return trajectoryPanel;
	}


	
	public void escapeAction() {
		this.dispose();
	}
	
}
