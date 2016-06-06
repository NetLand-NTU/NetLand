package WidgetsButtons;

import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import WidgetsMenu.MainMenu;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import ch.epfl.lis.animation.Snake;
import ch.epfl.lis.gnw.Gene;
import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.gnwgui.DynamicalModelElement;
import ch.epfl.lis.gnwgui.NetworkElement;
import ch.epfl.lis.gnwgui.StructureElement;
import ch.epfl.lis.imod.ImodNetwork;
import ch.epfl.lis.networks.Node;



public class TopButton extends JButton{
	private static final long serialVersionUID = 1L;

	protected static NetworkElement element;
	protected JButton b;
	protected int funcNum;
	protected ImageIcon icon;

	protected JPanel runButtonAndSnakePanel_;
	protected Snake snake_;
	protected CardLayout myCardLayout_ = new CardLayout();
	protected JPanel snakePanel_;
	protected JPanel runPanel_;
	
	
	public TopButton(int funcNum, ImageIcon icon, ImageIcon icon1, NetworkElement element){
		TopButton.element = element;
		this.funcNum = funcNum;
		this.icon = icon;
		b = new JButton();
		
		b.setBorderPainted(false);
		b.setFocusPainted(false);
		b.setContentAreaFilled(false);
        b.setDoubleBuffered(true);  
        
        b.setIcon(icon);
        b.setRolloverIcon(icon1);  
        b.setPressedIcon(icon);  
        b.setOpaque(false);  
        b.setFocusable(false);  
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));  



		TBHandler TB=new TBHandler(funcNum);       
		b.addActionListener(TB);   
	}

	public JButton getButton(){ return b; } 
	public NetworkElement getElement(){ return element; } 


	/*//==== Button 2 ==================================================================
	public void simulation(){ 
		try {
			//UI
			Component c = b.getParent();
			while ( c.getParent() != null ){
				c = c.getParent();
			}

			if ( c instanceof JFrame ){
				((JFrame) c).getContentPane().removeAll();
				//WindowTopPanel TopPanel = new WindowTopPanel(element);	
				WindowSimulationPanel SimulationPanel = new WindowSimulationPanel(element);

				*//** SET LAYOUT **//*
				((JFrame) c).getContentPane().setLayout(new GridBagLayout());  
				// Row 1
				//NetLand.addComponent(((JFrame) c), TopPanel.getTopPanel(), 0, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1, 0);
				// Row 2
				NetLand.addComponent(((JFrame) c), SimulationPanel.getSimulationPanel(), 0, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1, 1);

				c.invalidate();
				c.repaint();
				c.setVisible(true);		
			}

		} catch (Exception e) {
			System.err.print("Error in simulationButton " + e.getMessage());
			System.exit(1);
		}
	}

	//=== Button 3 ============================================================================
	public void network(){ //load network		
		//UI
		Component c = b.getParent();
		while ( c.getParent() != null ){
			c = c.getParent();
		}

		if ( c instanceof JFrame ){
			((JFrame) c).getContentPane().removeAll();
			//WindowTopPanel TopPanel = new WindowTopPanel(element);	
			//WindowNetworkPanel NetworkPanel = new WindowNetworkPanel(element);

			*//** SET LAYOUT **//*
			((JFrame) c).getContentPane().setLayout(new GridBagLayout());  
			// Row 1
			//NetLand.addComponent(((JFrame) c), TopPanel.getTopPanel(), 0, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1, 0);
			// Row 2
			//NetLand.addComponent(((JFrame) c), NetworkPanel.getPanel(), 0, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1, 1);

			c.invalidate();
			c.repaint();
			c.setVisible(true);		
		}	
		
	}
	
	*/
	//======================================================================
	public void network2(){ //generate Landscape
//		try {
//			//create a dialog
//			a = new JDialog();
//			a.setSize(new Dimension(400,600));
//
//			JLabel setMaxExp = new JLabel("Set MaxExp: ");
//			final JTextField maxExp = new JTextField("3");
//
//			JLabel setIts = new JLabel("Set Its: ");
//			final JTextField its = new JTextField("10");
//
//			JLabel setMaxTime = new JLabel("Set MaxT: ");
//			final JTextField maxT = new JTextField("5");
//
//			JLabel setNumPoints = new JLabel("Set NumPoints: ");
//			final JTextField numPoints = new JTextField("21");
//
//			JLabel setAttractorTypes = new JLabel("Set AttractorTypes: ");
//			final JTextField attractorTypes = new JTextField("0 1;1 1;0 2;2 0");
//			
//			//set layout
//			JPanel panelTop = new JPanel();
//			panelTop.setLayout(new BoxLayout(panelTop, BoxLayout.X_AXIS));
//			panelTop.add(setMaxExp);panelTop.add(maxExp);
//
//			JPanel panelMid = new JPanel();
//			panelMid.setLayout(new BoxLayout(panelMid, BoxLayout.X_AXIS));
//			panelMid.add(setIts);panelMid.add(its);
//
//			JPanel panelMid2 = new JPanel();
//			panelMid2.setLayout(new BoxLayout(panelMid2, BoxLayout.X_AXIS));
//			panelMid2.add(setMaxTime);panelMid2.add(maxT);
//
//			JPanel panelMid3 = new JPanel();
//			panelMid3.setLayout(new BoxLayout(panelMid3, BoxLayout.X_AXIS));
//			panelMid3.add(setNumPoints);panelMid3.add(numPoints);
//
//			JPanel panelMid4 = new JPanel();
//			panelMid4.setLayout(new BoxLayout(panelMid4, BoxLayout.X_AXIS));
//			panelMid4.add(setAttractorTypes);panelMid4.add(attractorTypes);
//
//			//buttons
//			myCardLayout_ = new CardLayout();
//			runButtonAndSnakePanel_ = new JPanel();
//			runButtonAndSnakePanel_.setLayout(myCardLayout_);
//
//			runPanel_ = new JPanel();
//			runPanel_.setBackground(Color.WHITE);
//			runPanel_.setLayout(new BorderLayout());
//			runPanel_.setName("runPanel");
//			runButtonAndSnakePanel_.add(runPanel_, runPanel_.getName());
//
//			JButton runButton_ = new JButton();
//			runPanel_.add(runButton_);
//			runButton_.setMnemonic(KeyEvent.VK_R);
//			runButton_.setBackground(UIManager.getColor("Button.background"));
//			runButton_.setName("computeButton");
//			runButton_.setText("Run");
//
//			snakePanel_ = new JPanel();
//			snakePanel_.setLayout(new BorderLayout());
//			snakePanel_.setName("snakePanel");
//			runButtonAndSnakePanel_.add(snakePanel_, snakePanel_.getName());
//
//			snake_ = new Snake();
//			snakePanel_.add(snake_);
//			snake_.setName("snake_");
//			snake_.setBackground(Color.WHITE);
//
//			JPanel cancelPanel = new JPanel();
//			cancelPanel.setBackground(Color.WHITE);
//			cancelPanel.setLayout(new BorderLayout());
//
//			JButton cancelButton_ = new JButton();
//			cancelPanel.add(cancelButton_);
//			cancelButton_.setMnemonic(KeyEvent.VK_C);
//			cancelButton_.setBackground(UIManager.getColor("Button.background"));
//			cancelButton_.setText("Cancel");
//
//
//			JPanel wholePanel = new JPanel();
//			wholePanel.setLayout(new BoxLayout(wholePanel, BoxLayout.Y_AXIS));
//			wholePanel.add(panelTop);wholePanel.add(panelMid);wholePanel.add(panelMid2);wholePanel.add(panelMid3); 
//			wholePanel.add(panelMid4);wholePanel.add(runButtonAndSnakePanel_);wholePanel.add(cancelPanel);
//
//			//create landscape
//			//button listeners
//			runButton_.addActionListener(new ActionListener() {
//				public void actionPerformed(final ActionEvent arg0) {		
//					double maxExpValue = Double.parseDouble(maxExp.getText());
//					int itsValue = Integer.parseInt(its.getText());
//					double maxTime = Double.parseDouble(maxT.getText());
//					int numTimePoints = Integer.parseInt(numPoints.getText());
//					
//									
//					String text = attractorTypes.getText();
//					String[] lines = text.split(";");
//					String[] cont = lines[0].split(" ");
//					DoubleMatrix2D attractorTypes = new DenseDoubleMatrix2D(lines.length, cont.length);	
//					for(int i=0;i<lines.length;i++){
//						cont = lines[i].split(" ");
//						for(int j=0;j<cont.length;j++)
//							attractorTypes.set(i, j, Double.parseDouble(cont[j]));
//					}
//										
//					enterAction(maxExpValue, itsValue, maxTime, numTimePoints, attractorTypes);
//				}
//			});
//
//			cancelButton_.addActionListener(new ActionListener() {
//				public void actionPerformed(final ActionEvent arg0) {
//					GnwSettings.getInstance().stopBenchmarkGeneration(true);
//					escapeAction();
//				}
//			});
//		
//			
//			a.getContentPane().add(wholePanel);
//			a.setModal(true);
//			a.setVisible(true);
//		
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

//=====================================================================================================	
	public static DynamicalModelElement convert2dynamicModel(NetworkElement element) {
		//inital values
		double[] initialValues=new double[element.getNetworkViewer().getStructure().getNodes().size()]; 
		Map<String, Double> parameters = new HashMap<String, Double>();
		generateInitialParameters(element, parameters, initialValues);	
		
		DynamicalModelElement grnItem = new DynamicalModelElement((StructureElement) element);
		grnItem.getGeneNetwork().assignParameters(parameters);
		
		grnItem.setOrigFile(element.getAbsPath(), element.getFilename(), element.getFormat());
		
		ArrayList<Gene> species = new ArrayList<Gene>();
		for(int i=0;i<grnItem.getGeneNetwork().getNodes().size();i++)
			species.add((Gene) grnItem.getGeneNetwork().getNode(i));
		grnItem.getGeneNetwork().setSpecies(species);

		DoubleMatrix1D matrix=new DenseDoubleMatrix1D(initialValues);
		grnItem.getGeneNetwork().setInitialState(matrix);	
		grnItem.getGeneNetwork().setSpecies_initialState(matrix);
		
		grnItem.setNetworkViewer(element.getNetworkViewer());
		return grnItem;
	}

	public static void generateInitialParameters(NetworkElement element, Map<String, Double> parameters, double[] values){
		ArrayList<Node> nodes = new ArrayList<Node>();
		if (element instanceof StructureElement) {
			ImodNetwork network = ((StructureElement)element).getNetwork();
			nodes = network.getNodes();
		} 

		//set parameters and initals  		
		for(int i=0;i<nodes.size();i++){
			values[i] = 1.0;
			//max transcription/degradation
			parameters.put("deg_"+nodes.get(i).getLabel(), 1.0);
			parameters.put("max_"+nodes.get(i).getLabel(), 1.0);
			
			//basalExp
			parameters.put("BasalExpression_"+nodes.get(i).getLabel(), 0.0);
			//K
			int numInputGenes = 0;
			for(int k=0;k<nodes.size();k++)
				if (element instanceof StructureElement) {
					ImodNetwork network = ((StructureElement)element).getNetwork();
					if( network.getEdge(nodes.get(k), nodes.get(i)) != null )
						numInputGenes++;
				} else if (element instanceof DynamicalModelElement) {
					GeneNetwork geneNetwork = ((DynamicalModelElement)element).getGeneNetwork();
					if( geneNetwork.getEdge(nodes.get(k), nodes.get(i)) != null )
						numInputGenes++;
				}

			for(int j=0;j<numInputGenes;j++){
				parameters.put("K_" +nodes.get(i).getLabel()+"_"+j, 0.5);
				parameters.put("N_" +nodes.get(i).getLabel()+"_"+j, 4.0);
				parameters.put("I_" +nodes.get(i).getLabel()+"_"+j, 1.0);
				parameters.put("BasalExpression_" +nodes.get(i).getLabel()+"_"+j, 0.0);
			}
		}  	
	}

	private class TBHandler implements ActionListener
	{
		private int funcNum = 1;

		public TBHandler(int funcNum){
			this.funcNum = funcNum;
		}

		public void actionPerformed(ActionEvent e)
		{      	
			if( funcNum == 0 ){ MainMenu.loadFile(); } 
			else if( funcNum == 1 ){ MainMenu.saveSBML(); }
			else if( funcNum == 2 ){ MainMenu.blockNodes(); }
			else if( funcNum == 3 ){ MainMenu.addNodesEdges(); }
			else if( funcNum == 4 ){ MainMenu.reset(); }
			else if( funcNum == 5 ){ MainMenu.generateTrajectory(); }
			else if( funcNum == 6 ){ MainMenu.generateLandscape(); }
			else if( funcNum == 7 ){ legend(); }
			else if( funcNum == 8 ){ 
				element.getNetworkViewer().setGraphMouseMode(0); 
				System.out.print("Move the whole graph\n");
			}
			else if( funcNum == 9 ){ 
				element.getNetworkViewer().setGraphMouseMode(1); 
				System.out.print("Move one node in the graph\n");
			}
			else if( funcNum == 10 ){ MainMenu.printGraph(); }
			else{ } //other	
		}

		private void legend() {
			final JDialog a = new JDialog();
			a.setTitle("Legend");
			a.setSize(new Dimension(138,140));
			a.setModal(true);
			
			bgPanel dialogPanel = new bgPanel();
			
			a.add(dialogPanel);
			a.setLocationRelativeTo(null);
			a.setVisible(true);
		}

	}
	
	@SuppressWarnings("serial")
	private class bgPanel extends JPanel{
		private Image image;

		public bgPanel(){
			image=new ImageIcon(getClass().getResource("rsc/buttons/interaction-labels.png")).getImage();

			this.setPreferredSize(new Dimension(image.getWidth(this),image.getHeight(this)));
		}

		public void paint(Graphics g){
			g.drawImage(image,0,0,image.getWidth(this),image.getHeight(this),0,0,image.getWidth(this),image.getHeight(this),this);
		}
	} 


}
