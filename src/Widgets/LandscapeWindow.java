package Widgets;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import ch.epfl.lis.animation.Snake;

public class LandscapeWindow extends JDialog {
	protected JPanel centerPanel_;
	protected JTextField its;
	protected JTextField maxExp;
//	protected JTextArea userPath_;
	protected JTextArea focusGenes;
//	protected JButton browse_;
//	protected JButton clear_;
	protected JRadioButton randioButton1;
//	protected JRadioButton randioButton3;
	protected CardLayout myCardLayout_;
	protected JPanel runButtonAndSnakePanel_;
	protected JPanel runPanel_;
	protected JButton runButton_;
	protected JPanel snakePanel_;
	protected Snake snake_;
	protected JPanel cancelPanel;
	protected JButton cancelButton_;
	protected JTextField maxT;
	protected JTextField gpdmIts;
//	protected JPanel landPlot;
//	protected LandscapeView landViewPanel;
	protected JRadioButton randioButton11;
	protected JRadioButton randioButton31;
	private JPanel cancelPanel_;
	protected JPanel wholePanel;
//	protected JButton showDataButton_;
	
	protected JRadioButton generateTimeCourse;
	protected JRadioButton loadTimeCourse;
	
	protected JButton btn ;

	
	public LandscapeWindow(Frame aFrame) {
		super(aFrame);
		setResizable(true);		
		setSize(400, 630);
		setTitle("Generate landscape");
		
		//set icon
		Image image;
		try {
			URL ab = ClassLoader.getSystemResource("WidgetsButtons/rsc/buttons/land.png");
			image = ImageIO.read(ab);
			setIconImage(image);
		} catch (IOException e1) {
			MsgManager.Messages.errorMessage(e1, "Error", "");
		}

		centerPanel_ = new JPanel();
		//centerPanel_.setBorder(new EmptyBorder(10, 0, 0, 0));
//		centerPanel_.setBackground(Color.WHITE);
		getContentPane().add(centerPanel_);
		
		//radio generate time course
		ButtonGroup bg = new ButtonGroup();
		generateTimeCourse = new JRadioButton("Generate time series data");
		generateTimeCourse.setSelected(true);
		loadTimeCourse = new JRadioButton("Load saved data");
		bg.add(generateTimeCourse);
		bg.add(loadTimeCourse);
		
		JPanel options11 = new JPanel();
		options11.setLayout(new BorderLayout());
		options11.add(generateTimeCourse, BorderLayout.WEST);
		
		JPanel options22 = new JPanel();
		options22.setLayout(new BorderLayout());
		options22.add(loadTimeCourse, BorderLayout.WEST);
		
		// boundary
		JLabel setMaxExp = new JLabel("Upper boundary: ");
		maxExp = new JTextField("3");
		
		JPanel options = new JPanel();
		options.setLayout(new BoxLayout(options, BoxLayout.X_AXIS));
		
		options.add(setMaxExp);
		options.add(maxExp);
		
		options.setBorder(new EmptyBorder(5, 0, 5, 0));
		

		//number
		JLabel setIts = new JLabel("Number of simulations: ");
		its = new JTextField("100");
		
		JPanel options1 = new JPanel();
		options1.setLayout(new BoxLayout(options1, BoxLayout.X_AXIS));
		
		options1.add(setIts);
		options1.add(its);
		
		options1.setBorder(new EmptyBorder(5, 0, 5, 0));

		//time
		JLabel setMaxTime = new JLabel("Time: ");
		maxT = new JTextField("128");


		JPanel options2 = new JPanel();
		options2.setLayout(new BoxLayout(options2, BoxLayout.X_AXIS));
		
		options2.add(setMaxTime);
		options2.add(maxT);
		
		options2.setBorder(new EmptyBorder(5, 0, 5, 0));

		
		//gpdm its
		JLabel setGPDMits = new JLabel("GPDM iterations: ");
		gpdmIts = new JTextField("50");

		JPanel options21 = new JPanel();
		options21.setLayout(new BoxLayout(options21, BoxLayout.X_AXIS));

		options21.add(setGPDMits);
		options21.add(gpdmIts);

		options21.setBorder(new EmptyBorder(5, 0, 5, 0));
		
		
		//core genes
		JLabel setFocusGenes = new JLabel("Gene List(seperated by ';'): ");		
		focusGenes = new JTextArea();
		focusGenes.setLineWrap(true);
		focusGenes.setEditable(false);
		focusGenes.setRows(3);
		
		
		btn = new JButton("All genes");
		btn.setPreferredSize(new Dimension(80, 27));
		btn.setContentAreaFilled(false);
		btn.setMargin(new Insets(0, 0, 0, 0));
		
		JPanel options322 = new JPanel();
		options322.setLayout(new BoxLayout(options322, BoxLayout.X_AXIS));
		options322.add(setFocusGenes);
		options322.add(btn);
		
		JPanel options3 = new JPanel();
		options3.setLayout(new BorderLayout());
		
		options3.add(options322);
		options3.add(focusGenes, BorderLayout.SOUTH);
		
		options3.setBorder(new EmptyBorder(5, 0, 5, 0));

		
//		//save
//		JLabel label17 = new JLabel("Save landscape: ");	
//		userPath_ = new JTextArea();
//		userPath_.setBackground(Color.WHITE);
//		userPath_.setEditable(false);
//		userPath_.setLineWrap(true);
//		userPath_.setRows(2);
//		
//		JPanel options4 = new JPanel();
//		options4.setLayout(new BorderLayout());
//		
//		options4.add(label17, BorderLayout.WEST);
//		options4.add(userPath_, BorderLayout.CENTER);
//		
//		options4.setBorder(new EmptyBorder(5, 0, 5, 0));
		
		
//		//buttons
//		browse_ = new JButton();
//		browse_.setText("Browse");	
//		
//		clear_ = new JButton();
//		clear_.setText("Clear");
//		
//		JPanel options5 = new JPanel();
//		options5.setLayout(new BoxLayout(options5, BoxLayout.X_AXIS));
//		
//		options5.add(browse_);
//		options5.add(clear_);
//		
//		options5.setBorder(new EmptyBorder(5, 0, 5, 0));
		
		
		//select display method
		JLabel typeLandLabel = new JLabel();
		typeLandLabel.setText("Select landscape type: ");
		

	    randioButton1=new JRadioButton("Probabilistic",true);
//	    randioButton3=new JRadioButton("Entropy");
	    ButtonGroup groupMethod = new ButtonGroup();
	    groupMethod.add(randioButton1);
//	    groupMethod.add(randioButton3);
	    
	    //randioButton1.setBackground(new Color(187,207,232));
//	    randioButton3.setBackground(new Color(187,207,232));
	    JPanel radioPanel = new JPanel();
		radioPanel.setBorder(new EmptyBorder(5,5,5,5));
		radioPanel.setLayout(new BorderLayout());
	    radioPanel.add(typeLandLabel, BorderLayout.NORTH);
	    radioPanel.add(randioButton1, BorderLayout.WEST);
//	    radioPanel.add(randioButton3);
	    
	    
	    //visual method
	    JLabel viewLandLabel = new JLabel();
	    viewLandLabel.setText("Select visualization method: ");
		

	    randioButton11=new JRadioButton("Two markers",true);
	    randioButton31=new JRadioButton("GPDM");
	    ButtonGroup groupMethod2 = new ButtonGroup();
	    groupMethod2.add(randioButton11);
	    groupMethod2.add(randioButton31);
	    
//	    randioButton11.setBackground(new Color(187,207,232));
//	    randioButton31.setBackground(new Color(187,207,232));
	    JPanel radioPanel1 = new JPanel();
		radioPanel1.setBorder(new EmptyBorder(5,5,5,5));
		radioPanel1.setLayout(new BorderLayout());
	    radioPanel1.add(viewLandLabel, BorderLayout.NORTH);
	    radioPanel1.add(randioButton11, BorderLayout.WEST);
	    radioPanel1.add(randioButton31);
	   
		
		//run buttons
		myCardLayout_ = new CardLayout();
		runButtonAndSnakePanel_ = new JPanel();
		runButtonAndSnakePanel_.setLayout(myCardLayout_);
		runButtonAndSnakePanel_.setBorder(new EmptyBorder(0,105,0,0));

		runPanel_ = new JPanel();
//		runPanel_.setBackground(Color.WHITE);
		runPanel_.setName("runPanel");
		runButtonAndSnakePanel_.add(runPanel_, runPanel_.getName());
		
		
		runButton_ = new JButton();
		runPanel_.add(runButton_);
		runButton_.setMnemonic(KeyEvent.VK_R);
		runButton_.setBackground(UIManager.getColor("Button.background"));
		runButton_.setName("computeButton");
		runButton_.setText("  Run  ");

		snakePanel_ = new JPanel();
		snakePanel_.setBackground(null);
		snakePanel_.setName("snakePanel");
		runButtonAndSnakePanel_.add(snakePanel_, snakePanel_.getName());

		snake_ = new Snake();
		snakePanel_.add(snake_, BorderLayout.WEST);
		snake_.setName("snake_");
//		snake_.setBackground(Color.WHITE);
		snake_.setPreferredSize(new Dimension(50,50));

		cancelPanel_ = new JPanel();
		cancelButton_ = new JButton();
		cancelPanel_.add(cancelButton_);
		cancelButton_.setMnemonic(KeyEvent.VK_C);
		cancelButton_.setBackground(UIManager.getColor("Button.background"));
		cancelButton_.setText("Cancel");

		cancelPanel_.setBorder(new EmptyBorder(0,-105,0,0));
		
//		//show data button
//		showDataButton_ = new JButton("Show data");
//		showDataButton_.setVisible(false);
//		cancelPanel_.add(showDataButton_);
		
		
		JPanel buttonsPanel = new JPanel();
	    buttonsPanel.setLayout(new BorderLayout());
	    
		buttonsPanel.add(runButtonAndSnakePanel_, BorderLayout.WEST);
		buttonsPanel.add(cancelPanel_, BorderLayout.CENTER);
		
		
		/** LAYOUT **/
//		options.setBackground(new Color(187,207,232));
//		options1.setBackground(new Color(187,207,232));
//		options2.setBackground(new Color(187,207,232));
//		options3.setBackground(new Color(187,207,232));
//		options21.setBackground(new Color(187,207,232));
//		options4.setBackground(new Color(187,207,232));
//		options5.setBackground(new Color(187,207,232));
//		runPanel_.setBackground(new Color(187,207,232));
//		radioPanel.setBackground(new Color(187,207,232));
//		radioPanel1.setBackground(new Color(187,207,232));
//		cancelPanel_.setBackground(new Color(187,207,232));
//		runButtonAndSnakePanel_.setBackground(new Color(187,207,232));
//		buttonsPanel.setBackground(new Color(187,207,232));
//		snake_.setBackground(new Color(187,207,232));
		
		wholePanel = new JPanel();
		wholePanel.setLayout(new BoxLayout(wholePanel, BoxLayout.Y_AXIS));
		wholePanel.setBorder(new EmptyBorder(10, 10, 0, 10));
		wholePanel.setPreferredSize(new Dimension(100,150));
//		wholePanel.setBackground(new Color(187,207,232)); 
		wholePanel.add(options11); 
		wholePanel.add(options);wholePanel.add(options1);wholePanel.add(options2);wholePanel.add(options21);
		wholePanel.add(options22);
		wholePanel.add(options3);
//		wholePanel.add(options4);
//		wholePanel.add(options5);
		wholePanel.add(radioPanel);
		wholePanel.add(radioPanel1);
		wholePanel.add(buttonsPanel);

//		landPlot = new JPanel();
//		landPlot.setBackground(new Color(187,207,232));
//		try {
//			landPlot.add(landscapeTabb());
//		} catch (IOException e) {
//			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.INFORMATION_MESSAGE);
//		}
		
		centerPanel_.setLayout(new BorderLayout());
		centerPanel_.add(wholePanel, BorderLayout.CENTER);
//		centerPanel_.add(landPlot);
		
		setLocationRelativeTo(aFrame);
	}
	
	
//	private JPanel landscapeTabb() throws IOException{
//		/** first Tab: Landscape **/
//		//two comboboxes		
//		String[] geneList = {"-"};
//		JPanel bottomPanel = new JPanel();
//		
//		//add two combo
//		final JComboBox<String> combo1=new JComboBox<String>(geneList);
//		combo1.setBorder(BorderFactory.createTitledBorder("X-axis"));
//		final JComboBox<String> combo2=new JComboBox<String>(geneList);
//		combo2.setSelectedIndex(0);
//		combo2.setBorder(BorderFactory.createTitledBorder("Y-axis"));   
//
//		landViewPanel = new LandscapeView();
//		//get Jframe
//		generateFigure(landViewPanel);
//	
//		//set layout
//		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
//		combo1.setBackground(Color.white);
//		combo2.setBackground(Color.white);
//		bottomPanel.add(combo1);
//		bottomPanel.add(combo2);
//			
//		JPanel tempPanel = new JPanel();
//		landViewPanel.getCanvas().setSize(550,500);
//		tempPanel.setBackground(new Color(187,207,232));
//		tempPanel.add(landViewPanel.getCanvas());
//		
//		JPanel LandPanel = new JPanel();
//		LandPanel.setPreferredSize(new Dimension(550,550));		
//		LandPanel.setLayout(new BorderLayout());
//		LandPanel.add(tempPanel);
//		LandPanel.add(bottomPanel,BorderLayout.SOUTH);
//		
//		return LandPanel;
//	}
//	
//	public void generateFigure(LandscapeView landViewPanel){
//		SurfaceCanvas canvas = landViewPanel.getCanvas();	
//
//		//get its
//		double maxU = 0; double minU = 10;
//
//
//		//draw selection two genes
//		double stepSize = 0.1; 
//		double[] x = LandscapeView.increment(0.0, stepSize, 1);
//		double[] y = LandscapeView.increment(0.0, stepSize,  1);
//
//		double[][] gridData = new double[x.length][y.length];	
//		for(int i=0;i<x.length;i++)
//			for(int j=0;j<y.length;j++)
//				gridData[i][j] = 0;
//
//		//refresh figure
//		canvas.destroyImage();
//
//		LandscapeSurfaceModel model = new LandscapeSurfaceModel();
//		model.setXAxisLabel("x");
//		model.setYAxisLabel("y");
//		model.setZAxisLabel("U");
//		model.setXMax((float) x[x.length-1]);
//		model.setXMin((float) x[0]);
//		model.setYMax((float) y[y.length-1]);
//		model.setYMin((float) y[0]);
//		model.setZMax((float) maxU);
//		model.setZMin((float) minU);
//		model.setStepSizeX((float) stepSize);
//		model.setStepSizeY((float) stepSize);
//		model.setGridData(gridData);
//
//		canvas.setModel(model);
//		canvas.repaint();
//	}
	
	public void escapeAction() {
		this.dispose();
	}
}
