package LandscapeAnimation;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import org.jzy3d.chart.ContourChart;
import org.jzy3d.chart.controllers.mouse.camera.AWTCameraMouseController;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.contour.DefaultContourColoringPolicy;
import org.jzy3d.contour.MapperContourPictureGenerator;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Range;
import org.jzy3d.maths.Utils;
import org.jzy3d.picking.IObjectPickedListener;
import org.jzy3d.picking.PickingSupport;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.primitives.axes.ContourAxeBox;
import org.jzy3d.plot3d.rendering.canvas.ICanvas;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.legends.colorbars.AWTColorbarLegend;
import org.jzy3d.plot3d.rendering.view.Renderer2d;

import WidgetsTables.AttractorTable;
import WidgetsTables.SpeciesTable;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import ch.epfl.lis.animation.Snake;
import ch.epfl.lis.gnw.GeneNetwork;










public class LandscapePanel extends JFrame{	
	/** DATA **/
	private GeneNetwork grn_;
	private boolean isProbabilisticLandscape;

	private double[] landpointsX;
	private double[] landpointsY;
	private double[][] landpointsZ;
	private double[] trajDataInLandX;
	private double[] trajDataInLandY;
	private double[] trajDataInLandZ;

	private double[] time;
	public int nGenes;
	private String[] geneNames;
	private ArrayList<Integer> selectedGenes = new ArrayList<Integer>();


	/** GUI components **/
	private JSplitPane splitPane;	
	private JPanel landscapePanel;
	private JPanel contourTablePanel;
	private JPanel controlPanel;

	private JPanel trajLinePanel;
	private JPanel trajTablePanel;
	private JPanel trajControlPanel;

	private JPanel comboPanel;

	private JPanel runButtonAndSnakePanel_;
	private JPanel runPanel_;
	private JPanel snakePanel_;	
	private Snake snake_;
	private CardLayout myCardLayout_ = new CardLayout();
	
	private JTable dataTable;

	private JComboBox<String> combo1;
	private JComboBox<String> combo2;

	
	private Shape surface;
	private ContourChart landscapeChart;

	private String fpsText;
//	protected myThread t;
	private List<Coord3d> coords;
	private List<MyPickablePoint> points;
	private JButton contourButton;
	private BoundingBox3d bounds;
	private Color[] colors;
	private JRadioButton radio_set;
	private ColorMapper myColorMapper;
	private DataTable atable;

	

	public LandscapePanel(GeneNetwork grn, boolean displayMethod){	
		//initial data
		grn_ = grn;
		isProbabilisticLandscape = displayMethod;

		nGenes = grn.getNodes().size();
		geneNames = new String[nGenes];
		for(int i=0;i<nGenes;i++)
			geneNames[i] = grn.getNodes().get(i).getLabel();


		if( isProbabilisticLandscape )
			updateDataProLand(0,0);
		else
			updateDataGPDMLand();	

		//plot
		createDrawingChart();
		if( isProbabilisticLandscape ){
			landscapeChart.getAxeLayout().setXAxeLabel(geneNames[0]);
			landscapeChart.getAxeLayout().setYAxeLabel(geneNames[0]);
		}else{
			landscapeChart.getAxeLayout().setXAxeLabel("Component 1");
			landscapeChart.getAxeLayout().setYAxeLabel("Component 2");
		}

		GUI();
	}

	public double[] increment(double d, double e, double f) {
		int length = (int) (Math.rint((f-d)/e)+1);
		double[] temp=new double[length];
		for (int i = 0; i < temp.length; i++){
			temp[i]=(Math.rint(1000*(d+e*i)))/1000.0;        		
		}
		return temp;
	}
	
	
	public double[] incrementByN(double d, int n, double f) {
		double aStep = (f-d)/n;
		double[] temp = increment(d, aStep, f);
		
		return temp;
	}

	public void GUI(){
		//set icon
		Image image;
		try {
			URL ab = ClassLoader.getSystemResource("WidgetsButtons/rsc/buttons/land.png");
			image = ImageIO.read(ab);
			setIconImage(image);
		} catch (IOException e1) {
			MsgManager.Messages.errorMessage(e1, "Error", "");
		}

		//drawing
		landscapePanel = new JPanel();
		contourTablePanel = new JPanel();
		controlPanel = new JPanel();
		trajControlPanel = new JPanel();
		trajLinePanel = new JPanel();
		trajTablePanel = new JPanel();
		comboPanel = new JPanel();

		JPanel rightPanel = new JPanel();
		JPanel leftPanel = new JPanel();

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
		splitPane.setDividerLocation(2.0 / 3.0);  
		splitPane.setResizeWeight(0.5);
		splitPane.setOneTouchExpandable(true);
		splitPane.setContinuousLayout(true);


		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("Landscape");
		landscapePanel.setLayout(new java.awt.BorderLayout());
		landscapePanel.setPreferredSize(new java.awt.Dimension(700, 550));
		landscapePanel.setBackground(java.awt.Color.WHITE);
		comboPanel.setPreferredSize(new java.awt.Dimension(700, 50));
		contourTablePanel.setPreferredSize(new java.awt.Dimension(200, 500));
		contourTablePanel.setLayout(new java.awt.BorderLayout());
		trajLinePanel.setPreferredSize(new java.awt.Dimension(200, 250));
		trajLinePanel.setLayout(new java.awt.BorderLayout());
		controlPanel.setLayout(new java.awt.BorderLayout());
		controlPanel.setPreferredSize(new java.awt.Dimension(200, 100));
		trajTablePanel.setLayout(new BoxLayout(trajTablePanel, BoxLayout.Y_AXIS));
		trajTablePanel.setPreferredSize(new java.awt.Dimension(200, 200));
		trajControlPanel.setLayout(new BoxLayout(trajControlPanel, BoxLayout.Y_AXIS));
		trajControlPanel.setPreferredSize(new java.awt.Dimension(200, 100)); 
		rightPanel.setPreferredSize(new java.awt.Dimension(200, 600));
		leftPanel.setPreferredSize(new java.awt.Dimension(700, 600));


		trajLinePanel.setVisible(false);
		trajControlPanel.setVisible(false);
		trajTablePanel.setVisible(false);
		BoxLayout layout=new BoxLayout(rightPanel, BoxLayout.Y_AXIS); 
		rightPanel.setLayout(layout);
		rightPanel.add(controlPanel);
		rightPanel.add(trajControlPanel);
		rightPanel.add(contourTablePanel);
		rightPanel.add(trajTablePanel);
		rightPanel.add(trajLinePanel);


		layout = new BoxLayout(leftPanel, BoxLayout.Y_AXIS); 
		leftPanel.setLayout(layout);		
		leftPanel.add(landscapePanel);
		leftPanel.add(comboPanel);
		

		//draw landscapePanel
		plotLandscapePanel();

		//draw comboPanel
		plotComboPanel();
		if( isProbabilisticLandscape )
			comboPanel.setVisible(true);
		else
			comboPanel.setVisible(false);



		//draw controlPanel
		plotControlPanel();

		//draw trajPanel
		plotTrajPanel();		

		//draw contourTablePanel
		atable = new DataTable(contourTablePanel, landpointsX, landpointsY, landpointsZ, grn_, isProbabilisticLandscape, radio_set, points, myColorMapper);
		dataTable = atable.getTable();

		setPreferredSize(new Dimension(1200, 600));
		getContentPane().add(splitPane, BorderLayout.CENTER);
		pack();
		setVisible(true);
		setResizable(false);		
		setLocationRelativeTo(null);  
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);	
	}

	private void plotComboPanel() {
		combo1=new JComboBox<String>(geneNames);
		combo1.setBorder(BorderFactory.createTitledBorder("X-axis"));
		combo1.setLightWeightPopupEnabled(false);
		combo2=new JComboBox<String>(geneNames);
		combo2.setSelectedIndex(0);
		combo2.setBorder(BorderFactory.createTitledBorder("Y-axis"));   
		combo2.setLightWeightPopupEnabled(false);

		//combo actions
		combo1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int indexX = combo1.getSelectedIndex();
				int indexY = combo2.getSelectedIndex();
				String axisX = (String) combo1.getSelectedItem();
				String axisY = (String) combo2.getSelectedItem();

				updateFrameWithComboSelection(indexX, indexY, axisX, axisY);
			}
		});

		combo2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int indexX = combo1.getSelectedIndex();
				int indexY = combo2.getSelectedIndex();
				String axisX = (String) combo1.getSelectedItem();
				String axisY = (String) combo2.getSelectedItem();

				updateFrameWithComboSelection(indexX, indexY, axisX, axisY);
			}
		});

		//set layout
		comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.X_AXIS));
		combo1.setBackground(java.awt.Color.WHITE);
		combo2.setBackground(java.awt.Color.WHITE);
		comboPanel.add(combo1);
		comboPanel.add(combo2);
	}


	protected void updateFrameWithComboSelection(int indexX, int indexY, String axisX, String axisY) {
		//draw probabilistic landscape
		if( isProbabilisticLandscape ){
			updateDataProLand(indexX, indexY);

			//refresh landscape figure
			updateDrawingChart();
			landscapeChart.getAxeLayout().setXAxeLabel(axisX);
			landscapeChart.getAxeLayout().setYAxeLabel(axisY);

			
			//refresh dataTable
			atable.setPoints(points);
			contourTablePanel.updateUI();
			
			//remove contour
			float newMaxZ = (float) getMax(landpointsZ);
			float newMinZ = (float) getMin(landpointsZ);
			bounds.setZmax(newMaxZ); bounds.setZmin(newMinZ);
			
			landscapeChart.getView().setBoundManual(bounds);
			contourButton.setText("Show the contour map");
			ContourAxeBox newcab = new ContourAxeBox(bounds); 
			landscapeChart.getView().setAxe(newcab);
			
//			//refresh traj on land if linePanel is on
//			if( trajLinePanel.isVisible() )
//				mapAlineToLand();
			

		}
//		//draw gpdm
//		else if( indexX == -1 ){			
//			updateDataGPDMLand();	
//			comboPanel.setVisible(false);
//
//			//refresh landscape figure
//			updateDrawingChart();
//			landscapeChart.getAxeLayout().setXAxeLabel("Component 1");
//			landscapeChart.getAxeLayout().setYAxeLabel("Component 2");
//
//			//refresh dataTable
//			contourTablePanel.updateUI();	
//
//			//remove contour
//			landscapeChart.getView().setBoundManual(bounds);
//			contourButton.setText("Show the contour map");
//			ContourAxeBox newcab = new ContourAxeBox(bounds); 
//			landscapeChart.getView().setAxe(newcab);
//		}//end of else gpdm	

	}
	

	private void updateDataGPDMLand() {
		landpointsX = Utils.vector(grn_.getMinX(), grn_.getMaxX(), grn_.getN()+1);
		landpointsY = Utils.vector(grn_.getMinY(), grn_.getMaxY(), grn_.getN()+1);
//		landpointsX = increment(grn_.getMinX(), (grn_.getMaxX()-grn_.getMinX())/grn_.getN(), grn_.getMaxX());
//		landpointsY = increment(grn_.getMinY(), (grn_.getMaxY()-grn_.getMinY())/grn_.getN(), grn_.getMaxY());
		landpointsZ = new double[landpointsX.length][landpointsY.length];	
		DoubleMatrix2D Yout = grn_.getAllY();


		double maxU = 0; double minU = 100000;
		for(int i=0;i<landpointsX.length;i++){
			for(int j=0;j<landpointsY.length;j++){
				landpointsZ[i][j] = computeOnePointGPDM(Yout, i, j);

				if(landpointsZ[i][j] >maxU)
					maxU = landpointsZ[i][j] ;
				if(landpointsZ[i][j] <minU)
					minU = landpointsZ[i][j] ;

			}
		}

		//set data output
		grn_.setGridData(new DenseDoubleMatrix2D(landpointsZ));
		grn_.setMinX(getMin(landpointsX));
		grn_.setMaxX(getMax(landpointsX));
		grn_.setMinY(getMin(landpointsY));
		grn_.setMaxY(getMax(landpointsY));
		
		grn_.setX(new DenseDoubleMatrix1D(landpointsX));
		grn_.setY(new DenseDoubleMatrix1D(landpointsY));
	}


	private double computeOnePointGPDM(DoubleMatrix2D Yout, int xIndex, int yIndex) {
		DoubleMatrix1D currentY = Yout.viewRow(xIndex*landpointsX.length+yIndex);
		double u = 0;
		
		String[] focusgenes = grn_.getLand_focusGenesList();
		int[] focus_index = new int[focusgenes.length];
		for(int j=0;j<focusgenes.length;j++)
			for(int i=0;i<grn_.getNodes().size();i++)
				if( grn_.getNodes().get(i).getLabel().equals(focusgenes[j]) )
					focus_index[j] = i;
		
		//all types
		for(int k1=0;k1<grn_.getSumPara().rows();k1++){
			
			double u1 = 1;
			//consider all genes
			for(int index=0;index<currentY.size();index++){
				if( index==4 )
					continue;
				
				currentY.set(index, Math.abs(currentY.get(index)));
				u1 *= Math.exp(-1.0*Math.pow((currentY.get(index)-grn_.getSumPara().get(k1, index)), 2)/2.0/grn_.getSumPara().get(k1, index+nGenes))/Math.sqrt(2.0*Math.PI*grn_.getSumPara().get(k1, index+nGenes));
			}
//			//consider only focus genes
//			for(int index=0;index<focus_index.length;index++){
//				currentY.set(index, Math.abs(currentY.get(focus_index[index])));
//				u1 *= Math.exp(-1.0*Math.pow((currentY.get(focus_index[index])-grn_.getSumPara().get(k1, focus_index[index])), 2)/2.0/grn_.getSumPara().get(k1, focus_index[index]+nGenes))/grn_.getSumPara().get(k1, focus_index[index]+nGenes)/Math.sqrt(2.0*Math.PI);
//			}

			u += u1*grn_.getCounts()[k1]/((double)grn_.getLand_itsValue());
		}

		double up = -1.0*Math.log(u);
		if( up>500 )
			up=500;

		return up;
	}

	private void updateDrawingChart() {
		clearAllinChart();

		//generate points
		coords = getSurf();
		
		/**        surface           **/
		createSurface();

		//generate pickable points
		createPickablePoints();

		//add new objects
		landscapeChart.getScene().getGraph().add(surface);
		landscapeChart.getScene().getGraph().add(points);
	}


	private void updateDataProLand(int indexX, int indexY) {
//		landpointsX = increment(0.0, 0.05, grn_.getLand_maxExpValue());
//		landpointsY = increment(0.0, 0.05, grn_.getLand_maxExpValue());		
		landpointsX = incrementByN(0.0, 50, grn_.getLand_maxExpValue());
		landpointsY = incrementByN(0.0, 50, grn_.getLand_maxExpValue());			
		landpointsZ = new double[landpointsX.length][landpointsY.length];;

		calculateLandU(landpointsX, landpointsX, landpointsZ, indexX, indexY);

		//update contourTable
		if( contourTablePanel != null ){
			contourTablePanel.removeAll();
			DataTable atable = new DataTable(contourTablePanel, landpointsX, landpointsY, landpointsZ, grn_, isProbabilisticLandscape, radio_set, points, myColorMapper);
			dataTable = atable.getTable();
		}
		
		//set data output
		grn_.setGridData(new DenseDoubleMatrix2D(landpointsZ));
		grn_.setMinX(getMin(landpointsX));
		grn_.setMaxX(getMax(landpointsX));
		grn_.setMinY(getMin(landpointsY));
		grn_.setMaxY(getMax(landpointsY));
		
		grn_.setX(new DenseDoubleMatrix1D(landpointsX));
		grn_.setY(new DenseDoubleMatrix1D(landpointsY));
	}



	private void calculateLandU(double[] landpointsX, double[] landpointsY, double[][] landpointsZ, int indexX, int indexY) {
		double its = 0;
		for(int i=0;i<grn_.getCounts().length;i++)
			its += grn_.getCounts()[i];

		double maxU = 0; double minU = 100000;
		for(int i=0;i<landpointsX.length;i++){
			for(int j=0;j<landpointsY.length;j++){
				double u = 0;
				//all types
				for(int k1=0;k1<grn_.getSumPara().rows();k1++){
					double u1 = Math.exp(-1.0*Math.pow((landpointsX[i]-grn_.getSumPara().get(k1, indexX)), 2)/2.0/grn_.getSumPara().get(k1, indexX+nGenes))/Math.sqrt(2.0*Math.PI*grn_.getSumPara().get(k1, indexX+nGenes));
					double u2 = Math.exp(-1.0*Math.pow((landpointsY[j]-grn_.getSumPara().get(k1, indexY)), 2)/2.0/grn_.getSumPara().get(k1, indexY+nGenes))/Math.sqrt(2.0*Math.PI*grn_.getSumPara().get(k1, indexY+nGenes));
					u += u1*u2*grn_.getCounts()[k1]/its;
				}

				double up = -1.0*Math.log(u);
				if( up>100 )
					up=100;

				landpointsZ[i][j] = up;


				if(landpointsZ[i][j] >maxU)
					maxU = landpointsZ[i][j] ;
				if(landpointsZ[i][j] <minU)
					minU = landpointsZ[i][j] ;
			}
		}//end of for	

	}




	private void plotTrajPanel() {
		JLabel label = new JLabel("Set initial value: ");

		radio_set = new JRadioButton("Define the initial position");	
		final JRadioButton radio_select = new JRadioButton("Select the start point from the landscape");

		if( isProbabilisticLandscape )
			radio_select.setVisible(false);

		ButtonGroup loadOutput = new ButtonGroup();
		loadOutput.add(radio_select);
		loadOutput.add(radio_set);

		//		JButton submit = new JButton("Submit");
		JButton goBackButton = new JButton("Back");

		//		JPanel buttons = new JPanel();
		//		buttons.setPreferredSize(new java.awt.Dimension(100, 100));
		//		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		//		buttons.add(submit); buttons.add(cancel);

		trajControlPanel.add(label);trajControlPanel.add(radio_select);
		trajControlPanel.add(radio_set);trajControlPanel.add(goBackButton);
		trajControlPanel.setVisible(false);

		//select a point from landscape
		radio_select.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(radio_select.isSelected()){
					JOptionPane.showMessageDialog(null, "Double click cells in the table below to set the initial point.", "Info", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		radio_select.setSelected(true);

		//define initial value
		TBHandler TB=new TBHandler(8, null);       
		radio_set.addActionListener(TB);
		TB = new TBHandler(9, null);
		goBackButton.addActionListener(TB);
	}



	private void plotControlPanel() {
		contourButton = new JButton("Show the contour map");
		JButton interactiveWithGraphButton = new JButton("Select points from the landscpae");
		JButton viewGraphButton = new JButton("Show the landscape");
		JButton mapTrajButton = new JButton("Show attractors");


		TBHandler TB=new TBHandler(0, null);       
		contourButton.addActionListener(TB);
		TB=new TBHandler(1, null);       
		interactiveWithGraphButton.addActionListener(TB);  
		TB=new TBHandler(2, null);       
		viewGraphButton.addActionListener(TB);  
		TB=new TBHandler(3, null);       
		mapTrajButton.addActionListener(TB);

		GridLayout layout1 = new GridLayout(2,2);
		layout1.setHgap(10);
		layout1.setVgap(10);

		controlPanel.setLayout(layout1);     
		controlPanel.add(contourButton); controlPanel.add(interactiveWithGraphButton);
		controlPanel.add(viewGraphButton); controlPanel.add(mapTrajButton);		
	}

	private void plotLandscapePanel() {
		org.jzy3d.chart.Settings.getInstance().setHardwareAccelerated(true);
		final ICanvas canvas = landscapeChart.getCanvas();

		//draw 
		JLabel instructionLabel = instructions();
		instructionLabel.setOpaque(false);
		instructionLabel.setBounds(10, 10, 200, 100);

		Component a = (Component) canvas;
		a.setBackground(java.awt.Color.BLACK);
		a.setVisible(true);
		
		landscapePanel.add(instructionLabel, java.awt.BorderLayout.BEFORE_FIRST_LINE);
		landscapePanel.add( a, java.awt.BorderLayout.CENTER);

		
		
		
		this.addWindowListener(new WindowAdapter() { 
			public void windowClosing(WindowEvent e) {
				remove((java.awt.Component)canvas);
				landscapeChart.dispose();
			}
		});			
	}

	private class TBHandler implements ActionListener
	{
		private int funcNum = 1;
		public TBHandler(int funcNum, JDialog a){
			this.funcNum = funcNum;
		}

		public void actionPerformed(ActionEvent e)
		{      		
			if( funcNum == 0 ){ showContour(); } 
			else if( funcNum == 1 ){ interactiveWithGraph((JButton)e.getSource()); }
			else if( funcNum == 2 ){ viewGraph(); }
			else if( funcNum == 3 ){ mapTraj(); }
			else if( funcNum == 8 ){ selectInitialStateForMapTraj(); }
			else if( funcNum == 9 ){ backFromTrajToContrl(); }
			else{ } //other	
		}

		private void backFromTrajToContrl() {
			trajControlPanel.setVisible(false);
			controlPanel.setVisible(true);
		}

		private void selectInitialStateForMapTraj() {
			final JDialog a = new JDialog();
			a.setTitle("Fixed initial values");
			a.setSize(new Dimension(400,400));

			JLabel noteLabel = new JLabel();
			noteLabel.setText("Please set the initial values");

			JPanel speciesPanel = new JPanel();
			String[] columnName = {"Name", "InitialValue"};
			boolean editable = true; //false;
			new SpeciesTable(speciesPanel, columnName, grn_, editable);

			JPanel buttonPanel = new JPanel();
			JButton submit = new JButton("Submit");
			JButton cancel = new JButton("Cancel");

			runButtonAndSnakePanel_ = new JPanel();
			runButtonAndSnakePanel_.setLayout(myCardLayout_);
	

			runPanel_ = new JPanel();
			runPanel_.setBackground(java.awt.Color.WHITE);
			runPanel_.setLayout(new BoxLayout(runPanel_, BoxLayout.X_AXIS));
			runPanel_.setName("runPanel");
			runButtonAndSnakePanel_.add(runPanel_, runPanel_.getName());
			
			runPanel_.add(submit);
			submit.setMnemonic(KeyEvent.VK_R);
			submit.setBackground(UIManager.getColor("Button.background"));
			submit.setName("computeButton");

			snakePanel_ = new JPanel();
			snakePanel_.setLayout(new BorderLayout());
			snakePanel_.setBackground(null);
			snakePanel_.setName("snakePanel");
			runButtonAndSnakePanel_.add(snakePanel_, snakePanel_.getName());
				
			snake_ = new Snake();
			snakePanel_.add(snake_, BorderLayout.WEST);
			snake_.setName("snake_");
			//snake_.setBackground(java.awt.Color.WHITE);
			snake_.setPreferredSize(new Dimension(50,50));

			buttonPanel.add(runButtonAndSnakePanel_);
			buttonPanel.add(cancel);

			TBHandler TB=new TBHandler(4, a);       
			submit.addActionListener(TB);
			TB=new TBHandler(5, a);       
			cancel.addActionListener(TB);

			/** LAYOUT **/
			JPanel wholePanel = new JPanel();

			wholePanel.setLayout(new BoxLayout(wholePanel, BoxLayout.Y_AXIS));
			wholePanel.add(noteLabel); wholePanel.add(speciesPanel);
			wholePanel.add(buttonPanel);

			a.add(wholePanel);
			a.setModal(true);
			a.setLocationRelativeTo(null);
			a.setVisible(true);						
		}



		

	}

	private void calculateLandUPro(int indexX, int indexY) {
		double its = 0;
		for(int i=0;i<grn_.getCounts().length;i++)
			its += grn_.getCounts()[i];

		double maxU = 0; double minU = 100000;
		for(int i=0;i<trajDataInLandZ.length;i++){
			double u = 0;
			//all types
			for(int k1=0;k1<grn_.getSumPara().rows();k1++){
				double u1 = Math.exp(-1.0*Math.pow((trajDataInLandX[i]-grn_.getSumPara().get(k1, indexX)), 2)/2.0/grn_.getSumPara().get(k1, indexX+nGenes))/Math.sqrt(2.0*Math.PI*grn_.getSumPara().get(k1, indexX+nGenes));
				double u2 = Math.exp(-1.0*Math.pow((trajDataInLandY[i]-grn_.getSumPara().get(k1, indexY)), 2)/2.0/grn_.getSumPara().get(k1, indexY+nGenes))/Math.sqrt(2.0*Math.PI*grn_.getSumPara().get(k1, indexY+nGenes));
				u += u1*u2*grn_.getCounts()[k1]/its;
			}

			double up = -1.0*Math.log(u);
			if( up>100 )
				up=100;

			trajDataInLandZ[i] = up;


			if(trajDataInLandZ[i] >maxU)
				maxU = trajDataInLandZ[i] ;
			if(trajDataInLandZ[i] <minU)
				minU = trajDataInLandZ[i] ;
		}
	}//end of for	

	
	private void mapTraj() {
		//set visible
//		controlPanel.setVisible(false);
//		trajControlPanel.setVisible(true);
		//initialSelectFromTable();	
		final JDialog a = new JDialog();
		a.setSize(new Dimension(500,300));
		a.setModal(true);
		a.setTitle("Attractors");
		a.setLocationRelativeTo(null);

		JPanel wholePanel = new JPanel();
		
//		JTextArea details = new JTextArea();
//		details.setLineWrap(true);
//		JScrollPane scroll = new JScrollPane(details);
//		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
//		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//		
//		
//		
//		String content = "Attractor locations: \r\n   ";
//		for(int i=0;i<grn_.getSize();i++)
//			content += grn_.getGene(i).getLabel()+"  ";
//		content += "\r\n";
//
//		String content1 = "Percentage: ";
//		for(int i=0;i<grn_.getSumPara().rows();i++){
//			content += (i+1)+":   ";
//			for(int j=0;j<grn_.getSize();j++)
//				content += Math.abs(Math.floor(grn_.getSumPara().viewRow(i).get(j)*100)/100)+";  ";				
//			content1 += grn_.getCounts()[i]/((double)grn_.getLand_itsValue())+" ("+grn_.getCounts()[i]+")  ";
//			content += "\r\n";
//		}
//		content += "\r\n"+content1;
//		details.setText(content);
		
		AttractorTable panel = new AttractorTable(grn_, grn_.getLand_focusGenesList());
		JScrollPane scroll = new JScrollPane(panel);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		
		wholePanel.setLayout(new BorderLayout());
		wholePanel.add(scroll, BorderLayout.CENTER);
		
		a.add(wholePanel);
		a.setVisible(true);
	}

	private void viewGraph() {
		surface.setDisplayed(true);
		for(MyPickablePoint p: points)
			p.setWidth(3);
	}

	private void interactiveWithGraph(JButton source) {
		surface.setDisplayed(false);	
		for(MyPickablePoint p: points){
			p.setDisplayed(true);
			p.setWidth(3);
		}
	}




	private void showContour() {	
		landscapeChart.getView().setBoundManual(bounds);

		if(contourButton.getText().equals("Show the contour map")){
			contourButton.setText("Hide the contour map");
			createContourMap();
		}else{
			contourButton.setText("Show the contour map");
			ContourAxeBox newcab = new ContourAxeBox(bounds); 
			landscapeChart.getView().setAxe(newcab);
		}
	}

	public void setColors() {
		this.colors = new Color[nGenes];

		ArrayList<Random> nums = new ArrayList<Random>(nGenes);
		for(int i=0;i<nGenes;i++){
			Random r=new Random();
			while( nums.contains(r) ){
				r=new Random();
			}
			nums.add(r);
			Color color = new Color(r.nextInt(256),r.nextInt(256),r.nextInt(256));
			colors[i] = color;
		}

	}

	private void createContourMap() {
		//double[] to arraylist
		final ArrayList<Double> landpointsXList = new ArrayList<Double>(landpointsX.length);
		final ArrayList<Double> landpointsYList = new ArrayList<Double>(landpointsY.length);
		for(int i=0;i<landpointsX.length;i++)
			landpointsXList.add(landpointsX[i]);
		for(int i=0;i<landpointsY.length;i++)
			landpointsYList.add(landpointsY[i]);

		// Define a function to plot
		Mapper mapper = new Mapper() {
			public double f(double x, double y) {
				int indexx = -1; int indexy = -1;
				
				if( isProbabilisticLandscape ){
					indexx = landpointsXList.indexOf((Math.rint(x*1000))/1000.0);
					indexy = landpointsYList.indexOf((Math.rint(y*1000))/1000.0);
				}else{
					indexx = landpointsXList.indexOf(x);
					indexy = landpointsYList.indexOf(y);
				}

				return landpointsZ[indexx][indexy];
			}
		};


		/**        contour           **/
		// Define range and precision for the function to plot
		Range rangex = new Range(getMin(landpointsX), getMax(landpointsX));
		Range rangey = new Range(getMin(landpointsY), getMax(landpointsY));

		// Create a chart and add the surface	
		myColorMapper = new ColorMapper(new ColorMapRainbow(), getMin(landpointsZ), getMax(landpointsZ), new Color(1, 1, 1, .5f));

		ContourAxeBox cab = (ContourAxeBox) landscapeChart.getView().getAxe(); //new ContourAxeBox(box , chart.getView().getAxe().getLayout()); //
		
		MapperContourPictureGenerator contour = new MapperContourPictureGenerator(mapper, rangex, rangey);
		cab.setContourImg( contour.getContourImage(new DefaultContourColoringPolicy(myColorMapper), landpointsX.length, landpointsY.length, 30), rangex, rangey);
		//getContourImage //getFilledContourImage

	}


	private void clearAllinChart(){
		landscapeChart.getScene().getGraph().getAll().clear();
//		landscapeChart.removeDrawable(surface);
//		for(int i=0;i<points.size();i++)
//			landscapeChart.removeDrawable(points.get(i),false);
	}

	public void createDrawingChart(){	
		landscapeChart = new ContourChart(Quality.Advanced);	

		//generate points
		coords = getSurf();

		/**        surface           **/
		createSurface();

		//generate pickable points
		createPickablePoints();

		/**        control           **/
		landscapeChart.addKeyController();
		landscapeChart.addScreenshotKeyController();
		

		landscapeChart.addController(new AWTCameraMouseController(){
			@Override
			public void mousePressed(MouseEvent e) {
			}
		});


		//set x label  y label
		landscapeChart.getScene().getGraph().add(surface);
		landscapeChart.getScene().getGraph().add( points );	

		//define bounds
		bounds = landscapeChart.getView().getBounds();


		fpsText = "";
		landscapeChart.addRenderer(new Renderer2d(){
			public void paint(Graphics g) {
				Graphics2D g2d = (Graphics2D)g;
				g2d.setColor(java.awt.Color.BLACK);
				g2d.drawString(fpsText, 50, 50);
			}
		});

		landscapeChart.render();

	}

	private void createSurface() {
		/**        surface           **/
		surface = (Shape) Builder.buildDelaunay(coords);
		myColorMapper = new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1, .8f));
		surface.setColorMapper(myColorMapper);
		surface.setFaceDisplayed(true);
		surface.setWireframeDisplayed(false);
		surface.setWireframeColor(Color.BLACK);
		//legend
		surface.setLegend(new AWTColorbarLegend(surface,landscapeChart.getView().getAxe().getLayout()));
		surface.setLegendDisplayed(true);
	}

	private void createPickablePoints(){
		/**        pickable points           **/
		myColorMapper = new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1, .5f));

		//pickable points
		points = new ArrayList<>();

		for(int i = 0; i < coords.size(); i++) {
			Coord3d newcoord = new Coord3d(coords.get(i).x,coords.get(i).y,coords.get(i).z);
			points.add(new MyPickablePoint(i%landpointsX.length, (int)i/landpointsX.length, newcoord, myColorMapper.getColor(newcoord), 1));
			//points.get(i).setDisplayed(false);
		}


		/**        picking control       **/
		//mouse selection
		MyMousePickingController<?,?> mousePicker = new MyMousePickingController<>(landscapeChart, 5);
		PickingSupport picking = mousePicker.getPickingSupport();


		for(MyPickablePoint p : points) {
			picking.registerPickableObject(p, p);	
		}

		picking.addObjectPickedListener(new IObjectPickedListener() {
			@Override
			public void objectPicked(List<?> picked, PickingSupport ps) {
				if (picked.isEmpty()) {  
					//System.out.println("Nothing was picked.");	
					for(Object p: points){
						((MyPickablePoint) p).setColor(myColorMapper.getColor(((MyPickablePoint) p).xyz));
						((MyPickablePoint) p).setWidth(3);
					}
				} else {
					for(Object p: points){
						if( picked.get(0) == p ){
							System.out.println(p);
							((MyPickablePoint) p).setColor(Color.RED);
							((MyPickablePoint) p).setWidth(7);

							dataTable.setRowSelectionInterval(dataTable.getRowCount() - 1, dataTable.getRowCount() - 1);
							dataTable.grabFocus();
							dataTable.changeSelection(((MyPickablePoint) p).getColNo(), ((MyPickablePoint) p).getRowNo(), false, false);
						}else{
							((MyPickablePoint) p).setColor(myColorMapper.getColor(((MyPickablePoint) p).xyz));
							((MyPickablePoint) p).setWidth(3);
						}
					}
				}
			}
		});


	}

	public JLabel instructions(){
		JLabel instructionLabel = new JLabel();
		instructionLabel.setText("<html>"+
				"Rotate     : Left click and drag mouse<br>"+
				"Scale      : Roll mouse wheel<br>"+
				"Z Shift    : Right click and drag mouse<br>"+
				"Screenshot : Press 's'<br>"+
				"</html>"+
				"------------------------------------");
		return instructionLabel;
	}

	public List<Coord3d> getSurf(){
		int rows = landpointsX.length;

		List<Coord3d> coords = new ArrayList<Coord3d>(rows*rows);

		for(int i=0;i<rows;i++)
			for(int j=0;j<rows;j++)
				coords.add(new Coord3d((float) landpointsX[i],(float) landpointsY[j],(float) landpointsZ[i][j]));	

		return coords;
	}





	public double getMin(DoubleMatrix1D matrix){
		double min = 1000000;
		for(int i=0;i<matrix.size();i++)
			if( matrix.get(i)<min ) min=matrix.get(i);
		
		return min;
	}

	public double getMax(DoubleMatrix1D matrix){
		double max = -1000000;
		for(int i=0;i<matrix.size();i++)
			if( matrix.get(i)>max ) max=matrix.get(i);
		
		return max;
	}

	public double getMin(double[][] values){
		double minValue = 100000;
		for(int i=0;i<landpointsX.length;i++)
			for(int j=0;j<landpointsY.length;j++)
				if(minValue>values[i][j])
					minValue=values[i][j];

		return minValue;
	}

	public double getMax(double[][] values){
		double maxValue = -100000;
		for(int i=0;i<landpointsX.length;i++)
			for(int j=0;j<landpointsY.length;j++)
				if(maxValue<values[i][j])
					maxValue=values[i][j];

		return maxValue;
	}

	private double getMax(double[] values) {
		double maxValue = -100000;
		for(int i=0;i<values.length;i++)
			if(maxValue<values[i])
				maxValue=values[i];

		return maxValue;
	}

	private double getMin(double[] values) {
		double minValue = 100000;
		for(int i=0;i<values.length;i++)
			if(minValue>values[i])
				minValue=values[i];

		return minValue;
	}


	public void setLandpointsX(double[] landpointsX){
		this.landpointsX = landpointsX;		
	}

	public void setLandpointsY(double[] landpointsY){
		this.landpointsY = landpointsY;		
	}

	public void setLandpointsZ(double[][] landpointsZ){
		this.landpointsZ = landpointsZ;		
	}


	public double[] getTrajDataInLandX() {
		return trajDataInLandX;
	}

	public void setTrajDataInLandX(double[] trajDataInLandX) {
		this.trajDataInLandX = trajDataInLandX;
	}

	public double[] getTrajDataInLandY() {
		return trajDataInLandY;
	}

	public void setTrajDataInLandY(double[] trajDataInLandY) {
		this.trajDataInLandY = trajDataInLandY;
	}

	public double[] getTrajDataInLandZ() {
		return trajDataInLandZ;
	}

	public void setTrajDataInLandZ(double[] trajDataInLandZ) {
		this.trajDataInLandZ = trajDataInLandZ;
	}

	public double[] getTime() {
		return time;
	}

	public void setTime(double[] time) {
		this.time = time;
	}

	public boolean isProbabilisticLandscape() {
		return isProbabilisticLandscape;
	}

	public void setProbabilisticLandscape(boolean isProbabilisticLandscape) {
		this.isProbabilisticLandscape = isProbabilisticLandscape;
	}


}
