package WindowGUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import NetworkDisplay.NetworkView;
import Widgets.ModelTree;
import Widgets.TextBubbleBorder;
import WidgetsButtons.TopButtonGroup;
import WidgetsTables.MultiTabs;
import WidgetsTables.ParameterTable;
import WidgetsTables.ReactionTable;
import WidgetsTables.SpeciesTable;
import ch.epfl.lis.gnwgui.DynamicalModelElement;
import ch.epfl.lis.gnwgui.NetworkElement;

public class WindowNetworkPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	
	protected JPanel networkPanel;
	protected NetworkElement element;
	private static JFrame c;
	
	private void init(){
		/** THE CONTENT OF TOP BUTTON PANEL **/ 	    
		TopButtonGroup buttonGroup = new TopButtonGroup(element);
		

		/** THE CONTENT OF MODELPANEL **/ 		
		MultiTabs tabbsLeft = defineTabsLeft();
		JPanel modelPanel = tabbsLeft.getTabPanel();
		

		//set border
		modelPanel.setBackground(Color.white);
		AbstractBorder brdrLeft = new TextBubbleBorder(Color.LIGHT_GRAY,2,4,0,false);
		modelPanel.setBorder(brdrLeft);
		
		
		/** THE CONTENT OF NETWORK **/
		NetworkView newView = new NetworkView(element);
		JPanel conterPanel = newView.getCenterPanel();
		
		//set border
		conterPanel.setBackground(Color.white);
		conterPanel.setBorder(brdrLeft);
		
		
		/** THE CONTENT OF KERNEL **/
		JPanel kernelPanel = new JPanel();
//		LogPanel logPanel = new LogPanel();
//		logPanel.initLog();
		JTextArea jTextArea1 = new JTextArea();
		JScrollPane scroll = new JScrollPane(jTextArea1); 
		jTextArea1.setEditable(false);
		jTextArea1.setBackground(Color.white);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
		
		//set border
		kernelPanel.setBackground(Color.white);
		kernelPanel.setBorder(brdrLeft);
		kernelPanel.setPreferredSize(new Dimension(760,200));
		
		kernelPanel.setLayout(new BorderLayout());
		kernelPanel.add(scroll,BorderLayout.CENTER);
		redirectSystemStreams(jTextArea1);

		/**	SET LAYOUT **/			
		JPanel combinePanel = new JPanel();	
		combinePanel.setLayout(new BoxLayout(combinePanel, BoxLayout.Y_AXIS));
		combinePanel.setBorder(new EmptyBorder(5, 0, 5, 5));
		combinePanel.add(conterPanel);
		combinePanel.add(Box.createVerticalStrut(10));
		combinePanel.add(kernelPanel);
		
	
		//set background color
//		networkPanel.setBackground(new Color(187,207,232));
		//combinePanel.setBackground(new Color(187,207,232));
		
		//split pane
		JSplitPane hSplitPane = new JSplitPane();
		hSplitPane.setDividerLocation(200);
		
		hSplitPane.setLeftComponent(tabbsLeft.getTabbedPane());
		hSplitPane.setRightComponent(combinePanel);
		hSplitPane.setContinuousLayout(true);
		hSplitPane.setResizeWeight(0.8);
		
	
		networkPanel.setLayout(new BorderLayout());
		networkPanel.add(buttonGroup.getPanel(), BorderLayout.NORTH);
		networkPanel.add(hSplitPane, BorderLayout.CENTER);
		
	}
	

	
	/** MULTI TABS **/
	private MultiTabs defineTabsLeft(){
		//Tabs names
		ArrayList<String> LeftMultipleTabs = new ArrayList<String>();
		LeftMultipleTabs.add("Overview");
		LeftMultipleTabs.add("Species");
		LeftMultipleTabs.add("Reactions");	
		LeftMultipleTabs.add("Parameters");
		//LeftMultipleTabs.add("Info");

		//Components
		ArrayList<Component> c1 = new ArrayList<Component>();
	
		/** 0 Tab: tree view **/
		c1.add(overviewTabb());
		
		/** first Tab: Species **/
		c1.add(speciesTabb());

		/** second Tab: Reactions **/		
		c1.add(rxnsTabb());

		/** fourth Tab: Parameters **/
		c1.add(parametersTabb());
			
		///** fifth Tab: Parameters **/
		//c1.add(infoTabb());
		
		MultiTabs tabbs = new MultiTabs(LeftMultipleTabs, c1); 	
		return tabbs;
	}

	/** define each tabb  **/
	private JPanel overviewTabb(){
		ModelTree newTree = new ModelTree(((DynamicalModelElement)element).getGeneNetwork());
		
		return newTree.getTreePanel();
	}
	
	
	private JPanel speciesTabb(){
		JPanel speciesPanel = new JPanel();
		String[] columnName = {"Name", "InitialValue"};
		new SpeciesTable(speciesPanel, columnName, ((DynamicalModelElement)element).getGeneNetwork(), true);
		
		return speciesPanel;
	}
	
	private JPanel rxnsTabb(){
		JPanel rxnPanel = new JPanel();
		String[] columnName = {"RxnId","TargetGene","Reactants","Modifiers","Equation"};
		new ReactionTable(rxnPanel, columnName, element, c);

		return rxnPanel;
	}
	
	private JPanel parametersTabb(){
		String[] columnName1 = {"Name", "Value"};
		JPanel tablePanel1 = new JPanel();
		new ParameterTable(tablePanel1, columnName1, element, true);
		return tablePanel1;
	}

	
	public WindowNetworkPanel(NetworkElement element, JFrame parentFrame){
		this.element = element;
		networkPanel = new JPanel();
		this.c = parentFrame;
		
		init();
	}
		
	
	public JPanel getPanel(){ return networkPanel; }
	
	
	
	
	//The following codes set where the text get redirected. In this case, jTextArea1    
	private void updateTextArea(final String text, final JTextArea jTextArea1) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jTextArea1.append(text);
			}
		});
	}

	//Followings are The Methods that do the Redirect, you can simply Ignore them. 
	private void redirectSystemStreams( final JTextArea jTextArea1) {
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				updateTextArea(String.valueOf((char) b), jTextArea1);
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				updateTextArea(new String(b, off, len), jTextArea1);
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};

		System.setOut(new PrintStream(out, true));
		//System.setErr(new PrintStream(out, true));
	}
}
