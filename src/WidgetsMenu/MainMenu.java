package WidgetsMenu;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;
import org.sbml.jsbml.SBMLException;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import FileManager.FileChooser;
import LandscapeAnimation.LandscapePanel;
import MsgManager.Messages;
import Widgets.AddNodesAndEdges;
import Widgets.Landscape;
import Widgets.Simulation;
import Widgets.TwoWindowExchangeInfo;
import WidgetsTables.ParameterTable;
import WidgetsTables.ReactionTable;
import WidgetsTables.SpeciesTable;
import WindowGUI.NetLand;
import WindowGUI.WindowNetworkPanel;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import ch.epfl.lis.gnw.Gene;
import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.gnwgui.DynamicalModelElement;
import ch.epfl.lis.gnwgui.IODialog;
import ch.epfl.lis.gnwgui.IONetwork;
import ch.epfl.lis.gnwgui.NetworkElement;
import ch.epfl.lis.gnwgui.NetworkGraph;
import ch.epfl.lis.gnwgui.StructureElement;
import ch.epfl.lis.imod.ImodNetwork;
import ch.epfl.lis.networks.Edge;
import ch.epfl.lis.networks.Node;
import ch.epfl.lis.networks.ios.ParseException;
import ch.epfl.lis.utilities.filefilters.FilenameUtilities;
import ch.epfl.lis.utilities.filefilters.FilterImageEPS;
import ch.epfl.lis.utilities.filefilters.FilterImageJPEG;
import ch.epfl.lis.utilities.filefilters.FilterImagePNG;
import edu.uci.ics.screencap.EPSDump;
import edu.uci.ics.screencap.PNGDump;


public class MainMenu {
	protected static NetworkElement element;
	private static JMenuBar menuBar = new JMenuBar();
	private static JFrame parentFrame;
	
	static Logger log = Logger.getLogger(MainMenu.class.getName());
	
	public MainMenu(NetworkElement element, JFrame c){
		MainMenu.element = element;
		this.parentFrame = c;
		
		//define listeners
		MenuActionListener menuListener = new MenuActionListener();
		
		/* ****************** */
		// File button
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu); 
		
		//create a network
		JMenuItem openMenuItem2 = new JMenuItem("New Network", KeyEvent.VK_L);
        openMenuItem2.addActionListener(menuListener);
        fileMenu.add(openMenuItem2); 
		
		// File: load
        JMenuItem openMenuItem = new JMenuItem("Load Network", KeyEvent.VK_L);
        openMenuItem.addActionListener(menuListener);
        fileMenu.add(openMenuItem); 
        
        JMenuItem openMenuItem1 = new JMenuItem("Load Saved Results", KeyEvent.VK_L);
        openMenuItem1.addActionListener(menuListener);
        fileMenu.add(openMenuItem1); 
        

        fileMenu.addSeparator();
        // File: save
        JMenuItem saveMenuItem = new JMenuItem("Save SBML", KeyEvent.VK_S);
        saveMenuItem.addActionListener(menuListener);
        fileMenu.add(saveMenuItem); 
        
        JMenuItem saveMenuItem1 = new JMenuItem("Save Network", KeyEvent.VK_N);
        saveMenuItem1.addActionListener(menuListener);
        fileMenu.add(saveMenuItem1); 
        
        fileMenu.addSeparator();
        // File: close        
        JMenuItem exitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitMenuItem.addActionListener(menuListener);
        fileMenu.add(exitMenuItem);
        
        
        /* ****************** */
		// Simulation button
        JMenu simMenu = new JMenu("Simulation");
        simMenu.setMnemonic(KeyEvent.VK_S);
        menuBar.add(simMenu); 
        
        // Simulation: Model
        JMenu modelOptionsMenu = new JMenu("Model");
        modelOptionsMenu.setMnemonic(KeyEvent.VK_M); 
        
        // Model: species, Parameters, Rxn
        JMenuItem modelMenuItem1 = new JMenuItem("Species", KeyEvent.VK_S);
        modelMenuItem1.addActionListener(menuListener);
        modelOptionsMenu.add(modelMenuItem1); 
        
        JMenuItem modelMenuItem2 = new JMenuItem("Parameters", KeyEvent.VK_P);
        modelMenuItem2.addActionListener(menuListener);
        modelOptionsMenu.add(modelMenuItem2); 
        
        JMenuItem modelMenuItem3 = new JMenuItem("Reactions", KeyEvent.VK_R);
        modelMenuItem3.addActionListener(menuListener);
        modelOptionsMenu.add(modelMenuItem3); 
        
        simMenu.add(modelOptionsMenu); 
        
        // Simulation: traj
        JMenuItem trajMenuItem = new JMenuItem("Trajectory", KeyEvent.VK_T);
        trajMenuItem.addActionListener(menuListener);
        simMenu.add(trajMenuItem); 
        
        // Simulation: land
        JMenuItem landMenuItem = new JMenuItem("Landscape", KeyEvent.VK_L);
        landMenuItem.addActionListener(menuListener);
        simMenu.add(landMenuItem); 
        
        
        /* ****************** */
        // Network button
        JMenu networkMenu = new JMenu("Network");
        networkMenu.setMnemonic(KeyEvent.VK_N);
        menuBar.add(networkMenu); 
        
        // Network: Modify
        JMenu modifyOptionsMenu = new JMenu("Modify");
        modifyOptionsMenu.setMnemonic(KeyEvent.VK_M); 
        
        // Model: block, add, Rxn
        JMenuItem modifyMenuItem1 = new JMenuItem("Block nodes", KeyEvent.VK_B);
        modifyMenuItem1.addActionListener(menuListener);
        modifyOptionsMenu.add(modifyMenuItem1); 
        
        JMenuItem modifyMenuItem2 = new JMenuItem("Add nodes/edges", KeyEvent.VK_A);
        modifyMenuItem2.addActionListener(menuListener);
        modifyOptionsMenu.add(modifyMenuItem2); 
        
        JMenuItem modifyMenuItem3 = new JMenuItem("Reset", KeyEvent.VK_R);
        modifyMenuItem3.addActionListener(menuListener);
        modifyOptionsMenu.add(modifyMenuItem3); 
        
        networkMenu.add(modifyOptionsMenu); 
       
        // Network: control
        JMenu controlOptionsMenu = new JMenu("Control");
        controlOptionsMenu.setMnemonic(KeyEvent.VK_L); 
        
        // Network: kk, fr, circle
        JMenuItem controlMenuItem = new JMenuItem("Move graphe", KeyEvent.VK_G);
        controlMenuItem.addActionListener(menuListener);
        controlOptionsMenu.add(controlMenuItem);
        
        JMenuItem controlMenuItem1 = new JMenuItem("Move node", KeyEvent.VK_N);
        controlMenuItem1.addActionListener(menuListener);
        controlOptionsMenu.add(controlMenuItem1);
        
        networkMenu.add(controlOptionsMenu);
        
        // Network: layout
        JMenu layoutOptionsMenu = new JMenu("Layout");
        layoutOptionsMenu.setMnemonic(KeyEvent.VK_L); 
        
        // Network: kk, fr, circle
        JMenuItem layoutMenuItem = new JMenuItem("KK Layout", KeyEvent.VK_S);
        layoutMenuItem.addActionListener(menuListener);
        layoutOptionsMenu.add(layoutMenuItem);
        
        JMenuItem layoutMenuItem1 = new JMenuItem("FR Layout", KeyEvent.VK_S);
        layoutMenuItem1.addActionListener(menuListener);
        layoutOptionsMenu.add(layoutMenuItem1);
        
        JMenuItem layoutMenuItem2 = new JMenuItem("Circle Layout", KeyEvent.VK_S);
        layoutMenuItem2.addActionListener(menuListener);
        layoutOptionsMenu.add(layoutMenuItem2);
        
        networkMenu.add(layoutOptionsMenu); 
    
        
        // Network: Screenshot    
        JMenuItem screenMenuItem = new JMenuItem("Screenshot", KeyEvent.VK_S);
        screenMenuItem.addActionListener(menuListener);
        networkMenu.add(screenMenuItem);
        
        
        /* ****************** */
		// Help button
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(helpMenu); 
		// Help: load
        JMenuItem helpMenuItem = new JMenuItem("Manual");
        helpMenuItem.addActionListener(menuListener);
        helpMenu.add(helpMenuItem); 
        
        JMenuItem helpMenuItem1 = new JMenuItem("About NetLand");
        helpMenuItem1.addActionListener(menuListener);
        helpMenu.add(helpMenuItem1); 
	}
	
	
	static class MenuActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event ) {
            
            if( event.getActionCommand().equals("Load Network") )
            	loadFile();
            else if( event.getActionCommand().equals("New Network") )
            	createNetwork();
            else if( event.getActionCommand().equals("Screenshot") )
            	printGraph();
            else if( event.getActionCommand().equals("Reset") )
            	reset();
            else if( event.getActionCommand().equals("Add nodes/edges") )
            	addNodesEdges();
            else if( event.getActionCommand().equals("Block nodes") )
            	blockNodes();
            else if( event.getActionCommand().equals("Exit") )
            	exit();
            else if( event.getActionCommand().equals("Save SBML") )
            	saveSBML();
            else if( event.getActionCommand().equals("Save Network") )
            	saveNetwork();
            else if( event.getActionCommand().equals("Move graphe") )
            	element.getNetworkViewer().setGraphMouseMode(0);
            else if( event.getActionCommand().equals("Move node") )
            	element.getNetworkViewer().setGraphMouseMode(1);
            else if( event.getActionCommand().equals("KK Layout") )
            	element.getNetworkViewer().getControl().changeGraphLayout("KK layout");
            else if( event.getActionCommand().equals("FR Layout") )
            	element.getNetworkViewer().getControl().changeGraphLayout("FR layout");
            else if( event.getActionCommand().equals("Circle Layout") )
            	element.getNetworkViewer().getControl().changeGraphLayout("Circle layout");
            else if( event.getActionCommand().equals("Trajectory") ){
            	generateTrajectory();
            }else if( event.getActionCommand().equals("Landscape") ){
            	generateLandscape();
            }else if( event.getActionCommand().equals("Load Saved Results") ){
            	loadSavedResult();
            }else if( event.getActionCommand().equals("Species") ){
            	showSpecies();
            }else if( event.getActionCommand().equals("Parameters") ){
            	showParameters();
            }else if( event.getActionCommand().equals("Reactions") ){
            	showReactions();
            }else if( event.getActionCommand().equals("Manual") ){
            	showManual();
            }else if( event.getActionCommand().equals("About NetLand") ){
            	about();
            }
                      
        }

    }
	
	
	
	// functions
	//======  reset ================================================================
	public static void reset(){
		//reload file
		URL absPath = element.getAbsPath();
		String filename = element.getFilename();
		int format = element.getFormat();

		
		try {
			element = IONetwork.loadItem(filename, absPath, format);
		} catch (FileNotFoundException e1) {
			JOptionPane.showMessageDialog(null, "File is not found!", "Error", JOptionPane.INFORMATION_MESSAGE);
			MsgManager.Messages.errorMessage(e1, "Error", "");
		} catch (ParseException e1) {
			MsgManager.Messages.errorMessage(e1, "Error", "");
		} catch (Exception e1) {
			MsgManager.Messages.errorMessage(e1, "Error", "");
		}
		element.setNetworkViewer(new NetworkGraph(element));
		
		
		if( element instanceof StructureElement )
			element = WidgetsButtons.TopButton.convert2dynamicModel(element);
		
		element.setOrigFile(absPath, filename, format);
		
		
		//UI
		repaintWhole(0,"Reset network\n");
		
		element.getNetworkViewer().getControl().changeGraphLayout("KK layout");
	}


	//========= about ==============================================================
	public static void about() {
		JOptionPane.showMessageDialog(null,"NetLand: A tool for kinetic modeling and visualization of Waddington's epigenetic landscape \n\n" +
				"Waddington’s epigenetic landscape is a powerful metaphor for cellular dynamics driven by gene regulatory networks. \n" +
				"Its quantitative modeling and visualization, however, remains a challenge, especially when there are more than two genes. \n" +
				"Here, we present NetLand, an open-source software tool for modeling and simulating the kinetic dynamics of transcriptional \n" +
				"regulatory networks with far more than two genes, and visualizing the corresponding Waddington’s epigenetic landscape. \n" +
				"With an interactive and graphical user interface, NetLand can facilitate the knowledge discovery and experimental design \n" +
				"in the study of cell fate regulation (e.g. stem cell differentiation and reprogramming). \n" +
				"The software NetLand is written in Java, with a graphical user interface (GUI). For detailed instruction, please refer to \n" +
				"the user manual.\n Current Version: 1.0","About NetLand",JOptionPane. PLAIN_MESSAGE);		
	}


	//========== show manual =========================================================
	public static void showManual() {
		Object[] options = {"Continue","Cancel"};
		int response = JOptionPane.showOptionDialog(null, "Start download the manual...", "Manual",JOptionPane.YES_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		
		if(response==0){ 
			Desktop dp = Desktop.getDesktop();

	        try {
				dp.browse(new java.net.URI("https://github.com/NetLand-NTU/NetLand/blob/master/NetLand_manual.docx"));
			} catch (IOException | URISyntaxException e) {
				MsgManager.Messages.errorMessage(e, "Error", "");
			}
		}
		else if(response==1){ 
			
		}
	}


	//============ model ===========================================================
	public static void showReactions() {
		JDialog a = new JDialog();
		a.setTitle("Reactions");
		a.setSize(new Dimension(400,380));
		a.setLocationRelativeTo(null);
		
		JPanel rxnPanel = new JPanel();
		String[] columnName = {"RxnId","TargetGene","Modifiers","Equation"};
		new ReactionTable(rxnPanel, columnName, element, parentFrame);
		
		/** LAYOUT **/
		//rxnPanel.setBackground(new Color(187,207,232));
		
		a.add(rxnPanel);
		a.setModal(true);
		a.setVisible(true);		
		
	}


	public static void showParameters() {
		JDialog a = new JDialog();
		a.setTitle("Parameters");
		a.setSize(new Dimension(400,380));
		a.setLocationRelativeTo(null);
		
		JPanel paraPanel = new JPanel();
		String[] columnName = {"Name", "Value"};
		boolean editable = false; //false;
		new ParameterTable(paraPanel, columnName, element, editable);
		
		/** LAYOUT **/
		//paraPanel.setBackground(new Color(187,207,232));
		
		a.add(paraPanel);
		a.setModal(true);
		a.setVisible(true);					
	}


	public static void showSpecies() {
		JDialog a = new JDialog();
		a.setTitle("Species");
		a.setSize(new Dimension(400,380));
		a.setLocationRelativeTo(null);
		
		JPanel speciesPanel = new JPanel();
		String[] columnName = {"Name", "InitialValue"};
		boolean editable = false; //false;
		new SpeciesTable(speciesPanel, columnName, ((DynamicalModelElement)element).getGeneNetwork(), editable);
		
		/** LAYOUT **/
		//speciesPanel.setBackground(new Color(187,207,232));
		
		a.add(speciesPanel);
		a.setModal(true);
		a.setVisible(true);			
	}


	//============== load saved traj/land ============================================
	public static void loadSavedResult() {
		final JDialog a = new JDialog();
		a.setSize(new Dimension(400,150));
		a.setLocationRelativeTo(null);
		a.setTitle("Load saved result");
		
		//set icon
		Image image;
		try {
			URL ab = ClassLoader.getSystemResource("WidgetsButtons/rsc/buttons/open.png");
			image = ImageIO.read(ab);
			a.setIconImage(image);
		} catch (IOException e1) {
			MsgManager.Messages.errorMessage(e1, "Error", "");
		}

		//set content
		final JRadioButton radio_land = new JRadioButton("Load saved LandScape file");
		final JRadioButton radio_traj = new JRadioButton("Load saved Trajectory file");
		
		//load land
		radio_land.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				
				if(radio_land.isSelected()){
					a.dispose();
					
					JFrame frame = new JFrame();
					IODialog dialog = new IODialog(frame, "load file",
							System.getProperty("user.dir"), IODialog.LOAD);
					dialog.display();

					String absPath = dialog.getSelection();
					if (absPath != null){
						String dir = FilenameUtilities.getDirectory(absPath);
						
						String outputFilename = "";
						String os = System.getProperty("os.name");  		
						if(os.toLowerCase().startsWith("win")){   
							outputFilename = dir + "\\" + FilenameUtilities.getFilenameWithoutPath(absPath);
						}else if(os.toLowerCase().startsWith("linux") || os.toLowerCase().startsWith("mac")){ 
							outputFilename = dir + "/" + FilenameUtilities.getFilenameWithoutPath(absPath);
						}
						
						
						String filename = "temp_"+System.currentTimeMillis();
						String temppath = System.getProperty("java.io.tmpdir");
						File f = new File(temppath);

				
						try {
							File fTemp = File.createTempFile(filename, ".sbml", f);							
							String SBMLfilename = fTemp.getAbsolutePath();	
							
							readFileByLines0(outputFilename, SBMLfilename);
										
							fTemp.delete();
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(null, "Cannot create file!", "Error", JOptionPane.INFORMATION_MESSAGE);
							MsgManager.Messages.errorMessage(e1, "Error", "");
						}			

					}	
				}
			}
		});
		
		//load traj
		radio_traj.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(radio_traj.isSelected()){ 
					a.dispose();
					
					JFrame frame = new JFrame();
					IODialog dialog = new IODialog(frame, "load file", System.getProperty("user.dir"), IODialog.LOAD);
					dialog.display();

					String absPath = dialog.getSelection();
					if (absPath != null){
						String dir = FilenameUtilities.getDirectory(absPath);

						String outputFilename = "";
						String os = System.getProperty("os.name");  		
						if(os.toLowerCase().startsWith("win")){   
							outputFilename = dir + "\\" + FilenameUtilities.getFilenameWithoutPath(absPath);
						}else if(os.toLowerCase().startsWith("linux") || os.toLowerCase().startsWith("mac")){ 
							outputFilename = dir + "/" + FilenameUtilities.getFilenameWithoutPath(absPath);
						}
						
						
						String filename = "temp_"+System.currentTimeMillis();
						String temppath = System.getProperty("java.io.tmpdir");
						File f = new File(temppath);

						try {
							File fTemp = File.createTempFile(filename, ".sbml", f);							
							String SBMLfilename = fTemp.getAbsolutePath();	

							try {
								readFileByLines1(outputFilename, SBMLfilename);
							} catch (Exception e1) {
								JOptionPane.showMessageDialog(null, "Please check the content of the file!", "Cannot load result!", JOptionPane.INFORMATION_MESSAGE);			
								Messages.errorMessage(e1, "Cannot load result!", "");
							}	
							
							fTemp.delete();
						} catch (IOException e2) {
							JOptionPane.showMessageDialog(null, "Failed to create a temp file!", "Cannot create tempfile!", JOptionPane.INFORMATION_MESSAGE);
							Messages.errorMessage(e2, "Cannot create tempfile!", "");
						}
										
					}		

				}
			}
		});
		
		ButtonGroup loadOutput = new ButtonGroup();
		loadOutput.add(radio_land);
		loadOutput.add(radio_traj);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(radio_land);panel.add(radio_traj);
	
		
		a.getContentPane().add(panel);
		a.setModal(true);
		a.setVisible(true);
		
	}

	public static void readFileByLines0(String landFile, String sbmlFileName) throws ch.epfl.lis.networks.ios.ParseException  {
		try {
			FileWriter  fw = new FileWriter(sbmlFileName, false);
			
			File file = new File(landFile);
	        BufferedReader reader = null;
	        try {
	            reader = new BufferedReader(new FileReader(file));
	            String tempString = null;
	  
	            //save sbml
	            while ((tempString = reader.readLine()) != null) {
	            	if( !tempString.startsWith("Land") )
	            		fw.write(tempString+"\n");
	            	else
	            		break;
	            }
	            fw.close();
	            
	            //first line maxx minx maxy miny
	            String[] firstline = tempString.split("\t");
	            double maxX = Double.parseDouble(firstline[1]);
	            double minX = Double.parseDouble(firstline[2]);
	            double maxY = Double.parseDouble(firstline[3]);
	            double minY = Double.parseDouble(firstline[4]);    
	            
	            //parameters 
	            tempString = reader.readLine();
	            String[] secondline1 = tempString.split("\t");
	            int dimension = Integer.parseInt(secondline1[0]);
	            double maxExpValue = Double.parseDouble(secondline1[1]);
	            int itsValue = Integer.parseInt(secondline1[2]);
	            int maxTime = Integer.parseInt(secondline1[3]);
	            boolean displayMethod = Boolean.parseBoolean(secondline1[4]);
	            int gpdmItsValue = Integer.parseInt(secondline1[5]);
	            
	            //focus gene
	            tempString = reader.readLine();
	            String[] focusgenelist = tempString.split("\t");
	            
	            //x
	            tempString = reader.readLine();
	            String[] secondline = tempString.split("\t");
	            DoubleMatrix1D x = new DenseDoubleMatrix1D(secondline.length);
	            for(int j=0;j<secondline.length;j++)
	            	x.set(j, Double.parseDouble(secondline[j]));
	            
	            //y
	            tempString = reader.readLine();
	            String[] thirdline = tempString.split("\t");
	            DoubleMatrix1D y = new DenseDoubleMatrix1D(thirdline.length);
	            for(int j=0;j<thirdline.length;j++)
	            	y.set(j, Double.parseDouble(thirdline[j]));
	           
	            //counts
	            tempString = reader.readLine();
	            String[] fourthline = tempString.split("\t");
	            int[] counts = new int[fourthline.length];
	            for(int j=0;j<fourthline.length;j++)
	            	counts[j] = Integer.parseInt(fourthline[j]);

	            //sumpara
	            DoubleMatrix2D sumPara = new DenseDoubleMatrix2D(counts.length, dimension*2);
	            for(int j=0;j<counts.length;j++){
	            	tempString = reader.readLine();
	                String[] fifthline = tempString.split("\t");
	                for(int i=0;i<fifthline.length;i++)
	                	sumPara.set(j, i, Double.parseDouble(fifthline[i]));
	            }
	            
	            //allY for gpdm
	            DoubleMatrix2D allY = null;
	            if( !displayMethod ){//only for gpdm
	            	tempString = reader.readLine();
	                String[] fifthline = tempString.split("\t");
	                int rows = Integer.parseInt(fifthline[0]);
	                int cols = Integer.parseInt(fifthline[1]);
	                
	                allY = new DenseDoubleMatrix2D(rows, cols);
	                for(int j=0;j<rows;j++){
	                	tempString = reader.readLine();
	                    fifthline = tempString.split("\t");
	                    for(int i=0;i<cols;i++)
	                    	allY.set(j, i, Double.parseDouble(fifthline[i]));
	                }
	            }
	            
	            //size of griddata
	            tempString = reader.readLine();
	            String[] fifthline = tempString.split("\t");
	            int rows = Integer.parseInt(fifthline[0]);
	            int cols = Integer.parseInt(fifthline[0]);
	            
	            //griddata
	            DoubleMatrix2D gridData = new DenseDoubleMatrix2D(rows, cols);
	            int i = 0;
	            while (i<rows) {
	            	tempString = reader.readLine();
	            	String[] nums = tempString.split("\t");
	            	for(int j=0;j<nums.length;j++)
	            		gridData.set(i, j, Double.parseDouble(nums[j]));
	            	i++;
	            }
	            
	            //timeseries
	            ArrayList<DoubleMatrix2D> landtimeSeries = new ArrayList<DoubleMatrix2D>(itsValue);
	            //write timeseries data
	            for(i = 0; i<itsValue; i++){  
	            	tempString = reader.readLine();
		            rows = Integer.parseInt(tempString);
		            
	            	DoubleMatrix2D temp = new DenseDoubleMatrix2D(rows, dimension);
	            	for(int j=0;j<temp.rows();j++){
	            		tempString = reader.readLine();
	            		String[] nums = tempString.split("\t");
	            		for(int k=0;k<temp.columns();k++)
	            			temp.set(j, k, Double.parseDouble(nums[k]));
	            	}
	            	landtimeSeries.add(temp);
	            }
	                        
	            
	            //origAllY
	            DoubleMatrix2D origAllY = null;
	            if( !displayMethod ){//only for gpdm
	            	tempString = reader.readLine();
	            	String[] sixthline = tempString.split("\t");
	            	rows = Integer.parseInt(sixthline[0]);
	            	cols = Integer.parseInt(sixthline[1]);

	            	origAllY = new DenseDoubleMatrix2D(rows, cols);
	            	i = 0;
	            	while ((tempString = reader.readLine()) != null) {
	            		String[] nums = tempString.split("\t");
	            		for(int j=0;j<nums.length;j++)
	            			origAllY.set(i, j, Double.parseDouble(nums[j]));
	            		i++;
	            	}
	            }
	            
	            
	            
	            reader.close();
	            
	            //load sbml and land
	            try {
					loadSBMLLAND(sbmlFileName, focusgenelist, maxExpValue, itsValue, maxTime, displayMethod, gpdmItsValue, counts, x, y, maxX, minX, minY, maxY, sumPara, allY, gridData, landtimeSeries, origAllY);
				} catch (Exception e) {
					MsgManager.Messages.errorMessage(e, "Error", "");
				}
	            
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
		} catch (IOException e2) {
			MsgManager.Messages.errorMessage(e2, "Error", "");
		}
		
		
        
    
    }

	//load traj
	public static void readFileByLines1(String landFile, String sbmlFileName) throws ch.epfl.lis.networks.ios.ParseException, Exception {
		FileWriter fw = new FileWriter(sbmlFileName, false);
		
		File file = new File(landFile);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
  
            //save sbml
            while ((tempString = reader.readLine()) != null) {
            	if( !tempString.startsWith("Traj") )
            		fw.write(tempString+"\n");
            	else
            		break;
            }
            fw.close();
            
            //first line
            String[] info = tempString.split("\t");
            int noSerries = Integer.parseInt(info[1]);
            int timePoints = Integer.parseInt(info[2]);
            int dimension = Integer.parseInt(info[3]);
           
         
            ArrayList<DoubleMatrix2D> timeSeries = new ArrayList<DoubleMatrix2D>(noSerries);
            ArrayList<DoubleMatrix1D> timeScales = new ArrayList<DoubleMatrix1D>(noSerries);
            for(int i=0;i<noSerries;i++){
            	DoubleMatrix1D tempTime = new DenseDoubleMatrix1D(timePoints);
            	timeScales.add(tempTime);
            	
            	DoubleMatrix2D tempSeries = new DenseDoubleMatrix2D(timePoints, dimension);
            	timeSeries.add(tempSeries); 
            }
            
            //second line
            tempString = reader.readLine();
            String[] para = tempString.split("\t");
            int itsValue = Integer.parseInt(para[0]);
            int maxTime = Integer.parseInt(para[1]);
            double noise = Double.parseDouble(para[2]);
            String modelType = para[3];
            
            
            //each time series
            tempString = reader.readLine();
            for(int its=0;its<noSerries;its++){
            	int i= Integer.parseInt(tempString);
            	DoubleMatrix1D tempTime = timeScales.get(i);
            	DoubleMatrix2D tempSeries = timeSeries.get(i);
            
            	int index = 0;
            	//save info
            	while ((tempString = reader.readLine()) != null && index<timePoints) {
            		String[] nums = tempString.split("\t");
            		tempTime.set(index, Double.parseDouble(nums[0]));
            		for(int j=1;j<dimension+1;j++)
            			tempSeries.set(index, j-1, Double.parseDouble(nums[j]));
            		index++;
            	}
            }
            
            reader.close();
            
            //load sbml and land
            loadSBMLTRAJ(sbmlFileName, itsValue, maxTime, noise, modelType, timeSeries, timeScales);

            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                	MsgManager.Messages.errorMessage(e1, "Error", "");
                }
            }
        }
        
    
    }
	
	private static void loadSBMLTRAJ(String sbmlFileName, int itsValue, int maxTime, double noise, String modelType, ArrayList<DoubleMatrix2D> timeSeries, ArrayList<DoubleMatrix1D> timeScale) throws FileNotFoundException, ch.epfl.lis.networks.ios.ParseException, Exception {
		//loadSBML		
		int format = GeneNetwork.SBML;	
		//URL url =  Thread.currentThread().getContextClassLoader().getResource(sbmlFileName);
		URL url =  new File(sbmlFileName).toURI().toURL();
		NetworkElement element = IONetwork.loadItem(sbmlFileName, url, format);
		MainMenu.element = element;
		MainMenu.element.setNetworkViewer(new NetworkGraph(element));
		GeneNetwork grn = ((DynamicalModelElement) MainMenu.element).getGeneNetwork();

		grn.setTimeScale(timeScale);;
		grn.setTimeSeries(timeSeries);
		grn.setTraj_itsValue(itsValue);
		grn.setTraj_maxTime(maxTime);
		grn.setTraj_model(modelType);
		grn.setTraj_noise(noise);
		
		
		//repaint components
		repaintWhole(3,"Load saved simulation results\n");
        new Simulation(new JFrame(), element);
        
		element.getNetworkViewer().getControl().changeGraphLayout("KK layout");
	}

	private static void loadSBMLLAND(String sbmlFileName, String[] focusgenelist, double maxExpValue, int itsValue, int maxTime, boolean displayMethod, int gpdmItsValue, int[] counts, DoubleMatrix1D x, DoubleMatrix1D y, double maxX, double minX, double minY, double maxY, DoubleMatrix2D sumPara, DoubleMatrix2D allY, DoubleMatrix2D gridData, ArrayList<DoubleMatrix2D> landtimeSeries, DoubleMatrix2D origAllY) throws FileNotFoundException, ch.epfl.lis.networks.ios.ParseException, Exception {
		//loadSBML		
		int format = GeneNetwork.SBML;	
		//URL url =  Thread.currentThread().getContextClassLoader().getResource(sbmlFileName);
		URL url =  new File(sbmlFileName).toURI().toURL();
		NetworkElement element = IONetwork.loadItem(sbmlFileName, url, format);
		MainMenu.element = element;
		MainMenu.element.setNetworkViewer(new NetworkGraph(element));
		GeneNetwork grn = ((DynamicalModelElement) MainMenu.element).getGeneNetwork();

		grn.setX(x);
		grn.setY(y);
		grn.setMaxX(maxX);
		grn.setMinX(minX);
		grn.setMaxY(maxY);
		grn.setMinY(minY);
		grn.setGridData(gridData);
		grn.setCounts(counts);
		grn.setSumPara(sumPara);
		grn.setLand_focusGenesList(focusgenelist);
		grn.setLand_isTwoGenes(displayMethod);
		grn.setLand_itsValue(itsValue);
		grn.setLand_maxExpValue(maxExpValue);
		grn.setLand_maxTime(maxTime);
		grn.setLand_gpdmitsValue(gpdmItsValue);
		grn.setN(x.size()-1);
		grn.setLandTimeSeries(landtimeSeries);
		
		
		if( !displayMethod ){
			grn.setAllY(allY);
			grn.setOrigAllY(origAllY);
		}

		
		//repaint components
		repaintWhole(4, "Load saved landscape\n");	
		new LandscapePanel(grn, displayMethod);
		
		element.getNetworkViewer().getControl().changeGraphLayout("KK layout");
	}

	
	
	//======== save network ===========================================================
	public static void saveNetwork() {
		try {
            JFileChooser c = new FileChooser();
            int result = c.showSaveDialog(new JFrame());
            if(result == JFileChooser.APPROVE_OPTION){
                c.approveSelection();
                String filename = c.getSelectedFile().getAbsolutePath() + ".tsv";
				
				try {
					URL url = new File(filename).toURI().toURL();
					writeNetwork(url);
					JOptionPane.showMessageDialog(null, "Saved at "+filename, "Save TSV", JOptionPane.INFORMATION_MESSAGE);
				} catch (SBMLException | IOException e) {
					JOptionPane.showMessageDialog(null, "Cannot save network", "Error", JOptionPane.INFORMATION_MESSAGE);
					MsgManager.Messages.errorMessage(e, "Error", "");
				}

            }
        } catch (HeadlessException e) {
            MsgManager.Messages.errorMessage(e, "Error", "Cannot create a new file!");
        }	
		
	}


	protected static void writeNetwork(URL url) throws IOException {
		FileWriter fw = new FileWriter(url.getPath());   
		
		GeneNetwork grn = ((DynamicalModelElement) element).getGeneNetwork();	
		ArrayList<Edge> edges = grn.getEdges();
		
		for(int i=0;i<edges.size();i++){
			fw.write(edges.get(i).getSource().getLabel()+"\t"+edges.get(i).getTarget().getLabel()+"\t"+edges.get(i).getTypeString()+"\n");
		}
		
		fw.close();
	}


	//======== save sbml ===========================================================
	public static void saveSBML() {
		try {
            JFileChooser c = new FileChooser();
            int result = c.showSaveDialog(new JFrame());
            if(result == JFileChooser.APPROVE_OPTION){
                c.approveSelection();
                String filename = c.getSelectedFile().getAbsolutePath() + ".xml";
                
                GeneNetwork grn = ((DynamicalModelElement) element).getGeneNetwork();	
				grn.setId("NetLand_"+grn.getId());
				
				
				try {
					URL url = new File(filename).toURI().toURL();
					grn.writeSBML(url);
					JOptionPane.showMessageDialog(null, "Saved at "+filename, "Save SBML", JOptionPane.INFORMATION_MESSAGE);
				} catch (SBMLException | IOException | XMLStreamException | org.sbml.jsbml.text.parser.ParseException e) {
					JOptionPane.showMessageDialog(null, "Cannot save SBML", "Error", JOptionPane.INFORMATION_MESSAGE);
					MsgManager.Messages.errorMessage(e, "Error", "");
				}

            }
        } catch (HeadlessException e) {
            MsgManager.Messages.errorMessage(e, "Error", "Cannot create a new file!");
        }	
		
	}


	public static void repaintWhole(int type, String content) {
		JPanel a = (JPanel)(parentFrame.getContentPane().getComponent(0));
		JSplitPane b = (JSplitPane) a.getComponent(1);
		JPanel d = (JPanel) b.getComponent(2);
		JPanel d2 = (JPanel)d.getComponent(2);
		JScrollPane g = (JScrollPane)d2.getComponent(0);
		JViewport h = (JViewport)g.getComponent(0);
		JTextArea f = (JTextArea) h.getComponent(0);

		parentFrame.getJMenuBar().removeAll();
		parentFrame.getContentPane().removeAll();

		//WindowTopPanel TopPanel = new WindowTopPanel(element);	
		MainMenu TopPanel = new MainMenu(element, parentFrame);	
		WindowNetworkPanel NetworkPanel = new WindowNetworkPanel(element, parentFrame);


		/** SET LAYOUT **/		
		parentFrame.setJMenuBar(TopPanel.getMenuBar());
		parentFrame.getContentPane().setLayout(new GridBagLayout());  
		NetLand.addComponent((parentFrame), NetworkPanel.getPanel(), 0, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1, 1);

		String temp = f.getText();		
		temp += content;

		((JTextArea)((JViewport)((JScrollPane) ((JPanel)((JPanel)((JSplitPane)((JPanel)parentFrame.getContentPane().getComponent(0)).getComponent(1)).getComponent(2)).getComponent(2)).getComponent(0)).getComponent(0)).getComponent(0)).setText(temp);

		parentFrame.invalidate();
		parentFrame.repaint();
		parentFrame.setVisible(true);					
	}

	//======  block ================================================================
	public static void blockNodes(){
		final JDialog a = new JDialog();
		a.setTitle("Block Nodes");
		a.setSize(new Dimension(400,300));
		
		parentFrame.addWindowFocusListener(new WindowFocusListener() {  	  
            @Override  
            public void windowGainedFocus(WindowEvent e) {  
            	a.requestFocus();
            }  
  
            @Override  
            public void windowLostFocus(WindowEvent e) {  
            }  
              
        }); 
		a.setModal(false);
		a.setLocationRelativeTo(null);
		
		//set icon
		Image image;
		try {
			URL ab = ClassLoader.getSystemResource("WidgetsButtons/rsc/buttons/remove.png");
			image = ImageIO.read(ab);
			a.setIconImage(image);
		} catch (IOException e1) {
			MsgManager.Messages.errorMessage(e1, "Error", "");
		}
		
		JPanel dialogPanel = new JPanel();
		final TwoWindowExchangeInfo panel = new TwoWindowExchangeInfo(element, 0);
		JButton submit = new JButton("Submit");
		JButton cancel = new JButton("Cancel");
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(submit);
		buttonPanel.add(cancel);
//		buttonPanel.setBackground(new Color(187,207,232));
//		panel.setBackground(new Color(187,207,232));
		
		submit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				ArrayList<String> selection = new ArrayList<String>();  //nodes to keep
				ArrayList<String> notselection = new ArrayList<String>();  //nodes not to keep
				for( int i=0;i<panel.getListleft().getModel().getSize();i++){
					selection.add((String) panel.getListleft().getModel().getElementAt(i));
				}
				for( int i=0;i<panel.getListright().getModel().getSize();i++){
					notselection.add((String) panel.getListright().getModel().getElementAt(i));
				}

				//reset genenetwork
				BlockrepaintFrame(element, notselection, selection);
								
				a.dispose();
			}
		});
			
		cancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){				
				a.dispose();
			}
		});
		
		dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
		dialogPanel.add(panel);
		dialogPanel.add(buttonPanel);
//		dialogPanel.setBackground(new Color(187,207,232));
		dialogPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		a.add(dialogPanel);
		a.setVisible(true);
	}
	
	
	private static void BlockrepaintFrame(NetworkElement element, final ArrayList<String> notselection, final ArrayList<String> selection) {
		//step 1: get current grn
		final GeneNetwork grn = ((DynamicalModelElement) element).getGeneNetwork();
	
		//step 2: input rxn foreach selected gene
		final JDialog a = new JDialog();
		a.setTitle("Modify reaction");
		a.setSize(new Dimension(1100,400));
		a.setModal(true);
		a.setLocationRelativeTo(null);
		
		JPanel content = new JPanel();

		//content
		JLabel targetGeneName = new JLabel("TargetGene: ");
		JLabel reactionFieldLable = new JLabel("Reaction:");
		JCheckBox isEditable = new JCheckBox("Edit");
//		isEditable.setBackground(new Color(187,207,232));	
		isEditable.setSelected(false);
		final JTextPane reactionField = new JTextPane();
		reactionField.setEditable(false);
		reactionField.setAutoscrolls(true);
		reactionField.setPreferredSize(new Dimension(950,70));
		JScrollPane scrollPane = new JScrollPane(reactionField);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 

		//define combo
		final String[] genelistOrig = new String[selection.size()];
		final String[] reactionlistOrig = new String[selection.size()];
		for(int i=0;i<selection.size();i++){
			genelistOrig[i] = selection.get(i);
			reactionlistOrig[i] =  ((Gene)grn.getNode(selection.get(i))).getCombination();
		}
		
		//changed rxns and genes
		ArrayList<String> genelistArray = new ArrayList<String>();
		final ArrayList<String> reactionlistArray = new ArrayList<String>();
		for(int j=0;j<notselection.size();j++){
			for(int i=0;i<selection.size();i++){
				String targetgeneName = selection.get(i);
				String combine = ((Gene)grn.getNode(selection.get(i))).getCombination();

				//find gene that have nonselection as modifier or reactant
				String[] items = combine.split("\\*|\\^|\\+|\\/|\\-|\\(|\\)");
				for(int k=0;k<items.length;k++)
					if( items[k].equals(notselection.get(j)) ){
						if( !genelistArray.contains(targetgeneName) ){
							genelistArray.add(targetgeneName);
							reactionlistArray.add(combine);
							break;
						}
					}		
			}
		}
		
		//the left genelist
		final String[] genelist = new String[genelistArray.size()];
		final String[] reactionlist = new String[reactionlistArray.size()];
		
		
		if( genelistArray.size() == 0 ){ 
			element.getNetworkViewer().removeAllVertices();
			renewBlockNodeEdgeSpeicesInitialSpeiceInitial(grn,selection,notselection,reactionlist,reactionlistOrig,genelist,genelistOrig,a);
			element.getNetworkViewer().RemoveNodesAndEdges(selection);
			return;
		}
			
		
		for(int i=0;i<genelist.length;i++){
			genelist[i] = genelistArray.get(i);
			reactionlist[i] = reactionlistArray.get(i);
		}
		
		//set combo and textfield
		final JComboBox<String> combo=new JComboBox<String>(genelist);
		combo.setSelectedIndex(0);
		
		reactionField.setText(reactionlist[0]);
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		topPanel.add(targetGeneName); topPanel.add(combo);
//		topPanel.setBackground(new Color(187,207,232));

		//combo actions
		combo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int index = combo.getSelectedIndex();
				reactionField.setText(reactionlist[index]);
			}
		});

		// reactionField action
		reactionField.getDocument().addDocumentListener(new DocumentListener(){
			public void changedUpdate(final DocumentEvent e) {
				int index = combo.getSelectedIndex();
				String newrxn = reactionField.getText();
				reactionlist[index] = newrxn;		
			}

			public void insertUpdate(DocumentEvent arg0) {
				int index = combo.getSelectedIndex();
				String newrxn = reactionField.getText();
				reactionlist[index] = newrxn;
			}

			public void removeUpdate(DocumentEvent arg0) {
				int index = combo.getSelectedIndex();
				String newrxn = reactionField.getText();
				reactionlist[index] = newrxn;
			}
		});


		//set reactionField content  
		final ArrayList<String> para = new ArrayList<String>(grn.getParameterNames_().size());
		for(String str: grn.getParameterNames_()) para.add(str);
		
		final ArrayList<Node> nodes = new ArrayList<Node>(grn.getNodes().size());
		for(Node node: grn.getNodes()) nodes.add(node);

		Comparator<String> comparator = new Comparator<String>(){
			public int compare(String s1, String s2) {				
				return -s1.length()+s2.length();
			}
		};
		Collections.sort(para,comparator);

		Comparator<Node> comparator1 = new Comparator<Node>(){
			public int compare(Node s1, Node s2) {				
				return -s1.getLabel().length()+s2.getLabel().length();
			}
		};
		Collections.sort(nodes,comparator1);

		final JPanel drawingArea = new JPanel();
//		drawingArea.setBackground(new Color(187,207,232));	
		AddNodesAndEdges.renderFun(para, nodes, reactionField, drawingArea);

		JButton btnRender = new JButton("Render");
		btnRender.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				AddNodesAndEdges.renderFun(para, nodes, reactionField, drawingArea);
			}
		});


		JPanel buttonPanel = new JPanel();
//		buttonPanel.setBackground(new Color(187,207,232));	
		JButton submitButton = new JButton("Submit");
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(submitButton); 
		buttonPanel.add(cancelButton);
		
		
		//add listener
		submitButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				//check if notselected gene is involved
				int flag = 0;
				for(int j=0;j<reactionlist.length;j++){
					String newrxn = reactionlist[j];
					newrxn = newrxn.replace(" ", "");
					for(int i=0;i<notselection.size();i++){
						String[] items = newrxn.split("\\*|\\^|\\+|\\/|\\-|\\(|\\)");
						for(int k=0;k<items.length;k++)
							if( items[k].equals(notselection.get(i)) && !notselection.contains(genelist[j]) ){
								JOptionPane.showMessageDialog(null,"\""+notselection.get(i)+"\" has been removed, but contains in the reaction of \""+genelist[j]+"\"!","ERROR",JOptionPane.INFORMATION_MESSAGE);
								combo.setSelectedIndex(j);
								reactionField.setText(reactionlist[j]);
								flag = 1;
								break;
							}
					}
				}

				if( flag == 0 ) //every reaction is ok
 					renewBlockNodeEdgeSpeicesInitialSpeiceInitial(grn,selection,notselection,reactionlist,reactionlistOrig,genelist,genelistOrig,a);	
				
			}
		});   
		
		cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {			
				a.dispose();
			}
		});  

		isEditable.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent ex) {
				boolean selected = (ex.getStateChange() == ItemEvent.SELECTED);
				if(selected == true)
					reactionField.setEditable(true);			
				else
					reactionField.setEditable(false);
			}
		}); 
		
		//set layout
//		content.setBackground(new Color(187,207,232));	
		content.setBorder(new EmptyBorder(10,5,10,5));
		content.setLayout(new BorderLayout());  
		
		content.add(topPanel, BorderLayout.NORTH);
		content.add(reactionFieldLable, BorderLayout.WEST);
		content.add(isEditable);
		
		JPanel subcontent = new JPanel();
//		subcontent.setBackground(new Color(187,207,232));	
		subcontent.setLayout(new BoxLayout(subcontent, BoxLayout.Y_AXIS));  
		subcontent.add(scrollPane);
		subcontent.add(btnRender);
		subcontent.add(drawingArea);
		subcontent.add(buttonPanel, BorderLayout.WEST);
		
		content.add(subcontent, BorderLayout.SOUTH);

		a.add(content);
		a.setVisible(true);	
		
	} 
	
	private static void renewBlockNodeEdgeSpeicesInitialSpeiceInitial(GeneNetwork grn, ArrayList<String> selection, ArrayList<String> notselection, String[] reactionlist, String[] reactionlistOrig, String[] genelist, String[] genelistOrig, JDialog a) {
		for(int i=0;i<genelist.length;i++)
			for(int j=0;j<genelistOrig.length;j++)
				if( genelist[i].equals(genelistOrig[j]) ){
					reactionlistOrig[j] = reactionlist[i];
					continue;
				}
		
		reactionlist = reactionlistOrig;
		genelist = genelistOrig;
		
		
		//remove notselected nodes from nodes and species
		ArrayList<Edge> edges = grn.getEdges();
		ArrayList<Edge> newedges = new ArrayList<Edge>();
		ArrayList<Node> nodes = grn.getNodes();
		ArrayList<Node> newnodes = new ArrayList<Node>();
		ArrayList<Gene> species = grn.getSpecies();
		ArrayList<Gene> newspecies = new ArrayList<Gene>();

		
		
		for(int i=0;i<edges.size();i++){
			boolean f = true;
			
			for(int j=0;j<notselection.size();j++){
				if( edges.get(i).getSource().getLabel().equals(notselection.get(j)) )
					f = false;
				if( edges.get(i).getTarget().getLabel().equals(notselection.get(j)) )
					f = false;
			}		
			if( f )
				newedges.add(edges.get(i));
		}
		
		
		for(int i=0;i<selection.size();i++)
			for(int j=0;j<nodes.size();j++)
				if( nodes.get(j).getLabel().equals(selection.get(i)) )
					newnodes.add(nodes.get(j));

		for(int i=0;i<species.size();i++){
			int flag1 = 0;
			for(int j=0;j<notselection.size();j++)
				if( species.get(i).getLabel().equals(notselection.get(j)) )
					flag1 = 1;

			if( flag1 == 0 )
				newspecies.add(species.get(i));
		}

		grn.setSpecies(newspecies);
		grn.setNodes(newnodes);
		grn.setEdges(newedges);
		//remove initial/species
		DoubleMatrix1D initial = grn.getInitialState();
		DoubleMatrix1D speciesinitial = grn.getSpecies_initialState();
		DoubleMatrix1D newinitial = new DenseDoubleMatrix1D(newnodes.size()); 
		DoubleMatrix1D newspeciesinitial = new DenseDoubleMatrix1D(newspecies.size()); 

		for(int i=0;i<newnodes.size();i++)
			for(int j=0;j<nodes.size();j++)
				if( nodes.get(j).equals(newnodes.get(i)) )
					newinitial.set(i,initial.get(j));

		for(int i=0;i<newspecies.size();i++){
			for(int j=0;j<species.size();j++)
				if( species.get(j).equals(newspecies.get(i)) )
					newspeciesinitial.set(i, speciesinitial.get(i));
		}

		grn.setInitialState(newinitial);
		grn.setSpecies_initialState(newspeciesinitial);
		
		//clear timeseries and timescale
		grn.setTimeSeries(new ArrayList<DoubleMatrix2D>());
		grn.setTimeScale(new ArrayList<DoubleMatrix1D>());
		
		//update reactionlist and parameters
//		ArrayList<String> newparameterNames_ = new ArrayList<String>();
//		ArrayList<Double> newparameterValues_ = new ArrayList<Double>();
		for(int i=0;i<newnodes.size();i++){
			String newReaction = reactionlist[i];
			Gene targetGene = (Gene) newnodes.get(i);
		
			ArrayList<Gene> newinputs = new ArrayList<Gene>();
			WidgetsTables.RxnButtonRender.parseNewRxn(grn, newReaction, targetGene, newinputs);
			
			targetGene.setCombination(newReaction);
			targetGene.setInputGenes(newinputs);
		}
//		grn.setParameterNames_(newparameterNames_);
//		grn.setParameterValues_(newparameterValues_)
		
		
		
		
		String temp = "";
		for(int i=0;i<notselection.size();i++)
			temp += ","+notselection.get(i);
		//UI
		repaintWhole(1, "Block node(s): "+temp.substring(1)+"\n");
		
		//element.getNetworkViewer().RemoveNodesAndEdges(selection);
		
		
		a.dispose();
		
	}	
	
	//====== add ===================================================================
	public static void addNodesEdges(){	
		Component c = menuBar.getParent();
		while ( c.getParent() != null ){
			c = c.getParent();
		}

		if ( c instanceof JFrame ){
			final AddNodesAndEdges a = new AddNodesAndEdges(new JFrame(), element, (JFrame) c);
			
			parentFrame.addWindowFocusListener(new WindowFocusListener() {  	  
	            @Override  
	            public void windowGainedFocus(WindowEvent e) {  
	            	a.requestFocus();
	            }  
	  
	            @Override  
	            public void windowLostFocus(WindowEvent e) {  
	            }  
	              
	        }); 
			a.setModal(false);
			a.setVisible(true);
		}
	}
	
	//==== create a network ===========================================================
	public static void createNetwork(){
		URL url = MainMenu.class.getResource("rsc/emptyNetwork.tsv");

		try {
			element = IONetwork.loadItem("emptyNetwork.tsv", url, ImodNetwork.TSV);
			element.setNetworkViewer(new NetworkGraph(element));

			element = WidgetsButtons.TopButton.convert2dynamicModel(element);
			element.setOrigFile(url, "emptyNetwork.tsv", ImodNetwork.TSV);
		} catch (Exception e) {
			MsgManager.Messages.errorMessage(e, "Error", "");
			System.exit(1);
		}
		
		//repaint components
		repaintWhole(2, "Create a network!\n");
		
		element.getNetworkViewer().getControl().changeGraphLayout("FR layout");
	}

	
	//======  load file ================================================================
	@SuppressWarnings("static-access")
	public static void loadFile(){  
		
		IONetwork a = new IONetwork();
		if( a.open(new JFrame()) ){
			element = a.getElement();
						
			if( element != null){		
				element.setNetworkViewer(new NetworkGraph(element));
				
				//convert to dynamic model
				if( element instanceof StructureElement)
					element = WidgetsButtons.TopButton.convert2dynamicModel(element);
				
				//repaint components
				repaintWhole(2, "Successful in loading file '"+element.getFilename()+"'\n");
				
				element.getNetworkViewer().getControl().changeGraphLayout("FR layout");
			}else
				JOptionPane.showMessageDialog(null,  "Error in loading file.\nPlease check the content and format of the file!","Error", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	//======  screen shot ================================================================
	/**  Print the graph into 3 different format modifiable through the controller: EPS, PNG and JPEG  **/
	public static void printGraph() {
		String title = "Save As";

		final JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);
		fc.addChoosableFileFilter(new FilterImagePNG());
		fc.addChoosableFileFilter(new FilterImageJPEG());
		fc.addChoosableFileFilter(new FilterImageEPS());
		fc.setSelectedFile(new File(element.getLabel())); // filename proposition

		// Center the file chooser dialog in the center of the JPanel that displays the JUNG visualization.
		int returnVal = fc.showDialog(element.getNetworkViewer().getScreen(), title);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			FileFilter filter = fc.getFileFilter();

			if (filter instanceof  FilterImagePNG) {
				String[] ext = {"png"};
				file = FilenameUtilities.addExtension(file, ext);
				if (FilenameUtilities.writeOrAbort(file.getAbsolutePath(), new JFrame())) {
					saveComponentAsPNG(file, element.getNetworkViewer().getVisualizationViewer());
				}
			}
			else if (filter instanceof  FilterImageJPEG) {
				String[] ext = {"jpg", "jpeg"};
				file = FilenameUtilities.addExtension(file, ext);
				if (FilenameUtilities.writeOrAbort(file.getAbsolutePath(), new JFrame())) {
					saveComponentAsJPEG(file, element.getNetworkViewer().getVisualizationViewer());
				}
			}
			else if (filter instanceof  FilterImageEPS) {
				String[] ext = {"eps"};
				file = FilenameUtilities.addExtension(file, ext);
				if (FilenameUtilities.writeOrAbort(file.getAbsolutePath(), new JFrame())) {
					saveComponentAsEPS(file, element.getNetworkViewer().getVisualizationViewer());
				}
			}
			else
				return;
		}
	}
	
	// ----------------------------------------------------------------------------
	/** Save a given Component as EPS image (uses EPSDump). **/
	public static void saveComponentAsEPS(File file, Component c) {
		EPSDump dumper = new EPSDump(false);

		// Little subtlety for EPS images, As EPS doesn't support transparency
		try { dumper.dumpComponent(file, element.getNetworkViewer().getVisualizationViewer()); }
		catch (IOException e) {
			MsgManager.Messages.errorMessage(e, "Error", "");
			System.exit(1); 
		}
	}


	// ----------------------------------------------------------------------------
	/**  Save a given Component as PNG image (uses PNGDump). **/
	public static void saveComponentAsPNG(File file, Component c) {
		PNGDump dumper = new PNGDump();
		try { dumper.dumpComponent(file, element.getNetworkViewer().getVisualizationViewer()); }
		catch (IOException e) {
			MsgManager.Messages.errorMessage(e, "Error", "");
			System.exit(1); 		
		}
	}


	// ----------------------------------------------------------------------------
	/**  Save a given Component as JPEG image (uses). **/
	public static void saveComponentAsJPEG(File file, Component c) {
		BufferedImage myImage = new BufferedImage(c.getWidth(), c.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = myImage.createGraphics();
		c.paint(g2);
		try {
			OutputStream out = new FileOutputStream(file.getAbsolutePath());
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			encoder.encode(myImage);
			out.close();
		} catch (Exception e) {
			MsgManager.Messages.errorMessage(e, "Error", "");
			System.exit(1);
		}
	}

	
	//=========== exit =======================================================
	private static void exit() {
		Component c = menuBar.getParent();
		while ( c.getParent() != null ){
			c = c.getParent();
		}

		if ( c instanceof JFrame )
		{
			((JFrame) c).dispose();
		}
	}
	
	
	//========= trajectory =====================================================
	public static void generateTrajectory(){
		final Simulation rd = new Simulation(new JFrame(), element);
		
		parentFrame.addWindowFocusListener(new WindowFocusListener() {  	  
            @Override  
            public void windowGainedFocus(WindowEvent e) {  
                // TODO Auto-generated method stub  
            	rd.requestFocus();
            }  
  
            @Override  
            public void windowLostFocus(WindowEvent e) {  
                // TODO Auto-generated method stub  
            }  
              
        }); 
		rd.setModal(false);
		rd.setVisible(true);	
	}
	
	
	//========= landscape ======================================================
	public static void generateLandscape(){ //generate Landscape
		try {
			final Landscape a = new Landscape(new JFrame(), element);

			parentFrame.addWindowFocusListener(new WindowFocusListener() {  	  
	            @Override  
	            public void windowGainedFocus(WindowEvent e) {  
	                // TODO Auto-generated method stub  
	            	a.requestFocus();
	            }  
	  
	            @Override  
	            public void windowLostFocus(WindowEvent e) {  
	                // TODO Auto-generated method stub  
	            }  
	              
	        }); 
			
			a.setModal(false);
			a.setVisible(true);
		
		} catch (Exception e) {
			MsgManager.Messages.errorMessage(e, "Error", "");
		}
	}
	
	
	
	

	//get and set
	public JMenuBar getMenuBar() {
		return menuBar;
	}

	public void setMenuBar(JMenuBar menuBar) {
		MainMenu.menuBar = menuBar;
	}

}
