package Widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import WidgetsMenu.MainMenu;
import WidgetsTables.ScrollImage;
import WindowGUI.NetLand;
import WindowGUI.WindowNetworkPanel;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import ch.epfl.lis.gnw.Gene;
import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.gnw.HillGene;
import ch.epfl.lis.gnwgui.DynamicalModelElement;
import ch.epfl.lis.gnwgui.NetworkElement;
import ch.epfl.lis.gnwgui.StructureElement;
import ch.epfl.lis.networks.Edge;
import ch.epfl.lis.networks.Node;

public class AddNodesAndEdges extends AddNodesAndEdgesWindow {
	private static final long serialVersionUID = 1L;
	
	private ArrayList<Node> nodesContent = new ArrayList<Node>();
	private ArrayList<Edge> edgesContent = new ArrayList<Edge>();
	
	private static JFrame fatherFrame; 
	private static NetworkElement element;

	public AddNodesAndEdges(Frame aFrame, final NetworkElement item_, JFrame c){
		super(aFrame);		
		fatherFrame = c;
		element = item_;
		
		
		/** add new edge **/
		ArrayList<Edge> edges = new ArrayList<Edge>();
		ArrayList<Node> nodes = new ArrayList<Node>();
		if( item_ instanceof StructureElement ){
			edges = item_.getNetworkViewer().getStructure().getEdges();
			nodes = item_.getNetworkViewer().getStructure().getNodes();
		}else if( item_ instanceof DynamicalModelElement ){
			GeneNetwork grn = ((DynamicalModelElement) item_).getGeneNetwork();
			edges = grn.getEdges();
			nodes = grn.getNodes();
		}
		
		
		//ArrayList<Node> nodesContent = new ArrayList<Node>();
		for(int i=0;i<nodes.size();i++){
			nodesContent.add(nodes.get(i));
		}
		
		//ArrayList<Edge> edgesContent = new ArrayList<Edge>();
		for(int i=0;i<edges.size();i++){
			edgesContent.add(edges.get(i));
		}
		
		
		final ArrayList<String> nodeNames = new ArrayList<String>();
		for(int i=0;i<nodes.size();i++){
			nodeNames.add(nodes.get(i).getLabel());
			source.addItem(nodes.get(i).getLabel());
			target.addItem(nodes.get(i).getLabel());
		}

		
		
		//actions
		addNodeButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if("".equals(nameOfNode.getText())){
					JOptionPane.showMessageDialog(null, "Please input the node name!", "Error", JOptionPane.INFORMATION_MESSAGE);
				}else{
					//check format no space and no special charaters
					//---------------------------------------------------------
					//gj to code
					
					//check if duplicated
					boolean duplicated = false;
					for(int i=0;i<nodeNames.size();i++){
						if( nodeNames.get(i).equals(nameOfNode.getText()) ){
							duplicated = true;
						}
					}
					if( duplicated ){
						JOptionPane.showMessageDialog(null, "Already exist!", "Duplicated", JOptionPane.INFORMATION_MESSAGE);
						nameOfNode.setText("");
					}else{
						//change nodesContent
						Node newNode = new Node();
						newNode.setLabel(nameOfNode.getText());
						nodesContent.add(newNode);
						//change  nodeNames
						nodeNames.add(nameOfNode.getText());
						//change Jlist
						newNodesName.addElement(nameOfNode.getText());
						//change Jcombobox
						source.addItem(nameOfNode.getText());
						target.addItem(nameOfNode.getText());
						//change input text
						nameOfNode.setText("");
					}
					
				}
			}
		});
		
		final int len = nodes.size();
		delNodeButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				int selectedIndex = newNodesList.getSelectedIndex();
				if(selectedIndex != -1){
					nodeNames.remove(len+selectedIndex);
					nodesContent.remove(len+selectedIndex);		
					newNodesName.remove(selectedIndex);
					source.removeItemAt(len+selectedIndex);
					target.removeItemAt(len+selectedIndex);
				}
			}
		});
				
		addEdgeButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				boolean duplicated = false;
				for(int i=0;i<edgesContent.size();i++){
					if( edgesContent.get(i).getSource().getLabel().equals(source.getSelectedItem()) ){
						if( edgesContent.get(i).getTarget().getLabel().equals(target.getSelectedItem()) ){
							if( edgesContent.get(i).getTypeString().equals(type.getSelectedItem()) ){
								duplicated = true;
							}
						}
					}
				}
				if( duplicated ){
					JOptionPane.showMessageDialog(null, "Already exist!", "Duplicated", JOptionPane.INFORMATION_MESSAGE);
				}else{
					//change edgesContent
					Edge newEdge = new Edge();
					newEdge.setSource(nodesContent.get(source.getSelectedIndex()));
					newEdge.setTarget(nodesContent.get(target.getSelectedIndex()));
					newEdge.setType((byte) (type.getSelectedIndex()+1));
					edgesContent.add(newEdge);
					//change Jlist
					newEdgesName.addElement(source.getSelectedItem()+" "+type.getSelectedItem()+" "+target.getSelectedItem());
				}
			}
		});
		
		final int numEdge = edges.size();
		delEdgeButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				int selectedIndex = newEdgesList.getSelectedIndex();
				if(selectedIndex != -1){
					edgesContent.remove(numEdge+selectedIndex);	
					newEdgesName.remove(selectedIndex);
				}
			}
		});
		
		
		submitButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				ArrayList<Node> newnodes = nodesContent;
				ArrayList<Edge> newedges = edgesContent;
	
				//reset genenetwork
				AddrepaintFrame(item_, newnodes, newedges);	
			}
		});
		
		cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){				
				escapeAction();
			}
		});
		
	}
	
	
	private void AddrepaintFrame(final NetworkElement element, final ArrayList<Node> newnodes, final ArrayList<Edge> newedges) {
		//step 1: get current grn
		final GeneNetwork grn = ((DynamicalModelElement) element).getGeneNetwork();

		final ArrayList<Node> nodes = grn.getNodes();
		final ArrayList<Edge> edges = grn.getEdges();
		
		final ArrayList<Node> newAddNodes = new ArrayList<Node>();
		final ArrayList<Edge> newAddEdges = new ArrayList<Edge>();
		for(int i=0;i<newnodes.size();i++)
			if( !nodes.contains(newnodes.get(i)) )
				newAddNodes.add(newnodes.get(i));
		
		for(int i=0;i<newedges.size();i++)
			if( !edges.contains(newedges.get(i)) )
				newAddEdges.add(newedges.get(i));

		//step 2: parameters of new gene
		if( newAddNodes.size() != 0 ){
			AddNodeRepaintFrame(element, newAddNodes, newAddEdges);
		}//end of judge num of newnodes
		else{
			if( newAddEdges.size() != 0 ){			
				AddEdgeRepaintFrame(element, newAddEdges);					
			}// end of judge num  of newedges
		}
		
	}  
	
	private void AddNodeRepaintFrame(final NetworkElement element, final ArrayList<Node> newAddNodes, final ArrayList<Edge> newAddEdges){
		//step 1: get current grn
		final GeneNetwork grn = ((DynamicalModelElement) element).getGeneNetwork();
				
		final JDialog a = new JDialog();
		a.setSize(new Dimension(300,300));
		a.setLocationRelativeTo(null);

//		//closing listener
//		a.addWindowListener(new WindowAdapter() {
//			@Override
//			public void windowClosing(WindowEvent windowEvent){
//				JOptionPane.showConfirmDialog(null, "Have you verified rxns? Otherwise the dynamic model won't change!", "Reset reactions", JOptionPane.YES_NO_OPTION);
//			}
//		});

		JPanel content = new JPanel();
		content.setPreferredSize(new Dimension(300,300));

		//content
		JLabel targetGeneName = new JLabel("NewGene: ");

		//define combo
		String[] genelist = new String[newAddNodes.size()];
		final String[] basal = new String[newAddNodes.size()];
		final String[] max = new String[newAddNodes.size()];
		final String[] deg = new String[newAddNodes.size()];
		final String[] initial = new String[newAddNodes.size()];
		for(int i=0;i<newAddNodes.size();i++){
			genelist[i] = newAddNodes.get(i).getLabel();
			basal[i] = "0.0";
			max[i] = "1.0";
			deg[i] = "1.0";
			initial[i] = "1.0";
		}
		final JComboBox<String> combo=new JComboBox<String>(genelist);
		combo.setSelectedIndex(0);
	
		//parameters
		JLabel parameterLabel = new JLabel("Set parameters:");
		JLabel basalExpLabel = new JLabel("Basic Expression:");
		final JTextField basalExpValue = new JTextField("0.0");
		JLabel maxLabel = new JLabel("Max transcriptional rate:");
		final JTextField maxValue = new JTextField("1.0");
		JLabel degLabel = new JLabel("Degradation rate:");
		final JTextField degValue = new JTextField("1.0");
		JLabel initialLabel = new JLabel("Initial value:");
		final JTextField initialValue = new JTextField("1.0");


		//combo actions
		combo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int index = combo.getSelectedIndex();
				basalExpValue.setText(basal[index]);
				maxValue.setText(max[index]);
				degValue.setText(deg[index]);
				initialValue.setText(initial[index]);
			}
		});

		// textField action
		maxValue.getDocument().addDocumentListener(new DocumentListener(){
			public void changedUpdate(final DocumentEvent e) {
				int index = combo.getSelectedIndex();
				String newvalue = maxValue.getText();
				max[index] = newvalue;		
			}

			public void insertUpdate(DocumentEvent arg0) {
				int index = combo.getSelectedIndex();
				String newvalue = maxValue.getText();
				max[index] = newvalue;	
			}

			public void removeUpdate(DocumentEvent arg0) {
				int index = combo.getSelectedIndex();
				String newvalue = maxValue.getText();
				max[index] = newvalue;	
			}

		});

		degValue.getDocument().addDocumentListener(new DocumentListener(){
			public void changedUpdate(final DocumentEvent e) {
				int index = combo.getSelectedIndex();
				String newvalue = degValue.getText();
				deg[index] = newvalue;		
			}

			public void insertUpdate(DocumentEvent arg0) {
				int index = combo.getSelectedIndex();
				String newvalue = degValue.getText();
				deg[index] = newvalue;	
			}

			public void removeUpdate(DocumentEvent arg0) {
				int index = combo.getSelectedIndex();
				String newvalue = degValue.getText();
				deg[index] = newvalue;	
			}
		});

		basalExpValue.getDocument().addDocumentListener(new DocumentListener(){
			public void changedUpdate(final DocumentEvent e) {
				int index = combo.getSelectedIndex();
				String newvalue = basalExpValue.getText();
				basal[index] = newvalue;		
			}

			public void insertUpdate(DocumentEvent arg0) {
				int index = combo.getSelectedIndex();
				String newvalue = basalExpValue.getText();
				basal[index] = newvalue;	
			}

			public void removeUpdate(DocumentEvent arg0) {
				int index = combo.getSelectedIndex();
				String newvalue = basalExpValue.getText();
				basal[index] = newvalue;	
			}
		});

		initialValue.getDocument().addDocumentListener(new DocumentListener(){
			public void changedUpdate(final DocumentEvent e) {
				int index = combo.getSelectedIndex();
				String newvalue = initialValue.getText();
				initial[index] = newvalue;		
			}

			public void insertUpdate(DocumentEvent arg0) {
				int index = combo.getSelectedIndex();
				String newvalue = initialValue.getText();
				initial[index] = newvalue;	
			}

			public void removeUpdate(DocumentEvent arg0) {
				int index = combo.getSelectedIndex();
				String newvalue = initialValue.getText();
				initial[index] = newvalue;	
			}
		});


		JPanel buttonPanel = new JPanel();
		JButton submitButton = new JButton("Submit");
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(submitButton); 
		buttonPanel.add(cancelButton); 

		//add node listener
		submitButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {					
				DoubleMatrix1D initialValues = grn.getInitialState();
				DoubleMatrix1D speciesValues = grn.getSpecies_initialState();
				ArrayList<Gene> species = grn.getSpecies();
				ArrayList<String> parameterNames = grn.getParameterNames_();
				ArrayList<Double> parameterValues = grn.getParameterValues_();
				
				DoubleMatrix1D newinitialValues = new DenseDoubleMatrix1D(initialValues.size()+newAddNodes.size());
				DoubleMatrix1D newspeciesValues = new DenseDoubleMatrix1D(speciesValues.size()+newAddNodes.size());
				
				for(int i=0;i<initialValues.size();i++)
					newinitialValues.set(i, initialValues.get(i));
				for(int i=0;i<speciesValues.size();i++)
					newspeciesValues.set(i, speciesValues.get(i));
				
				//add new nodes/species/initial/speciesInitial
				int sizeNode = grn.getNodes().size();
				int sizeSpecies = grn.getSpecies().size();
				String temp = "";
				for(int i=0;i<newAddNodes.size();i++){
					HillGene newgene = new HillGene();
					temp += ","+newAddNodes.get(i).getLabel();
					newgene.setLabel(newAddNodes.get(i).getLabel());
					newgene.setBasalExpression_( Double.parseDouble(basal[i]) );
					newgene.setMax(Double.parseDouble(max[i]));
					newgene.setDelta(Double.parseDouble(deg[i]));
					newgene.setRnID("R_"+newAddNodes.get(i).getLabel());
					newgene.setCombination("max_"+newAddNodes.get(i).getLabel()+"*BasalExpression_"+newAddNodes.get(i).getLabel()+"-deg_"+newAddNodes.get(i).getLabel()+"*"+newAddNodes.get(i).getLabel());
					

					newgene.setInputGenes(new ArrayList<Gene>());
					
					parameterNames.add("max_"+newAddNodes.get(i).getLabel());
					parameterNames.add("BasalExpression_"+newAddNodes.get(i).getLabel());
					parameterNames.add("deg_"+newAddNodes.get(i).getLabel());
					
					parameterValues.add(Double.parseDouble(max[i]));
					parameterValues.add(Double.parseDouble(basal[i]));
					parameterValues.add(Double.parseDouble(deg[i]));
					
					newinitialValues.set(sizeNode+i, Double.parseDouble(initial[i]));
					newspeciesValues.set(sizeSpecies+i, Double.parseDouble(initial[i]));
					
					grn.getNodes().add(newgene);
					species.add(newgene);
				}

				//change view						
				element.getNetworkViewer().AddNewNodesAndEdges(grn.getNodes(), grn.getEdges());
				
				//add new edges
				grn.setNodes(grn.getNodes());
				grn.setSpecies(species);
				grn.setEdges(grn.getEdges());
				grn.setInitialState(newinitialValues);
				grn.setSpecies_initialState(newspeciesValues);
				grn.setParameterNames_(parameterNames);
				grn.setParameterValues_(parameterValues);

				a.dispose();	
				//UI
				repaintWhole(fatherFrame, "Add new node(s): "+temp.substring(1)+"\n");
				
				
				if( newAddEdges.size() != 0 ){			
					AddEdgeRepaintFrame(element, newAddEdges);					
				}// end of judge num  of newedges
				else escapeAction();
				
				
			}
		});   

		cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {				
				a.dispose();	
				
//				if( newAddEdges.size() == 0 )
//					escapeAction();
			}
		});   
		
		
		//set layout
		content.setLayout(new GridBagLayout());  
		// Row 1
		NetLand.addComponent(content, targetGeneName, 0, 0, 2, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1, 0);
		// Row 2
		NetLand.addComponent(content, combo, 0, 1, 2, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1, 1);
		// Row 3
		NetLand.addComponent(content, parameterLabel, 0, 2, 2, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1, 1);
		// Row 4
		NetLand.addComponent(content, basalExpLabel, 0, 3, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1, 1);
		NetLand.addComponent(content, basalExpValue, 1, 3, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1, 1);
		// Row 5
		NetLand.addComponent(content, maxLabel, 0, 4, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1, 1);
		NetLand.addComponent(content, maxValue, 1, 4, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1, 1);
		// Row 6
		NetLand.addComponent(content, degLabel, 0, 5, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1, 1);
		NetLand.addComponent(content, degValue, 1, 5, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1, 1);
		// Row 7
		NetLand.addComponent(content, initialLabel, 0, 6, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1, 1);
		NetLand.addComponent(content, initialValue, 1, 6, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1, 1);
		// Row 8
		NetLand.addComponent(content, buttonPanel, 0, 7, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1, 1);
					
		content.setBorder(new EmptyBorder(5,5,5,5));
		a.add(content);
		a.setVisible(true);	
	}
	
	private void AddEdgeRepaintFrame(final NetworkElement element, final ArrayList<Edge> newAddEdges) {
		//step 1: get current grn
		final GeneNetwork grn = ((DynamicalModelElement) element).getGeneNetwork();
				
		ArrayList<String> selectionTarget = new ArrayList<String>(newAddEdges.size());
		ArrayList<String> selectionSource = new ArrayList<String>(newAddEdges.size());
		ArrayList<String> selectionType = new ArrayList<String>(newAddEdges.size());
		
		for(int i=0;i<newAddEdges.size();i++){			
			selectionTarget.add(newAddEdges.get(i).getTarget().getLabel());
			selectionSource.add(newAddEdges.get(i).getSource().getLabel());
			selectionType.add(newAddEdges.get(i).getTypeString());
			
			newAddEdges.get(i).setTarget(indexOfNodeName(grn, newAddEdges.get(i).getTarget().getLabel()));
			newAddEdges.get(i).setSource(indexOfNodeName(grn, newAddEdges.get(i).getSource().getLabel()));		
		}			
		

		//step 2: input rxn foreach selected gene
		final JDialog a = new JDialog();
		a.setTitle("Modify reaction");
		a.setSize(new Dimension(1100,400));
		a.setModal(true);
		a.setLocationRelativeTo(null);

//		//closing listener
//		a.addWindowListener(new WindowAdapter() {
//			@Override
//			public void windowClosing(WindowEvent windowEvent){
//				JOptionPane.showConfirmDialog(null, "Have you verified rxns? Otherwise the dynamic model won't change!", "Reset reactions", JOptionPane.YES_NO_OPTION);
//			}
//		});
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
		final ArrayList<String> uniqueGenelist = array_unique(selectionTarget); //remove duplicates
		final String[] reactionlist = new String[uniqueGenelist.size()];
		
		//initialize reactionlist
		for(int i=0;i<selectionTarget.size();i++){
			int indexInRxnList = uniqueGenelist.indexOf(selectionTarget.get(i));
			
			reactionlist[indexInRxnList] =  ((Gene)grn.getNode(selectionTarget.get(i))).getCombination();
		}
		
		//update
		for(int i=0;i<selectionTarget.size();i++){
			ArrayList<Gene> inputs = ((Gene)grn.getNode(selectionTarget.get(i))).getInputGenes();			
			int m = inputs.size();
			
			int addon = Collections.frequency(selectionTarget.subList(0, i), selectionTarget.get(i));
			m += addon;

			int indexInRxnList = uniqueGenelist.indexOf(selectionTarget.get(i));
			
			if( selectionType.get(i).equals("+") )
				reactionlist[indexInRxnList] +=  "+(BasalExpression_" + selectionTarget.get(i) + "_" + m + "+I_" + selectionTarget.get(i) + "_" + m + "*(" + selectionSource.get(i) + "/K_" + selectionTarget.get(i) + "_" + m + ")^N_" + selectionTarget.get(i) + "_" + m + "/(1+(" + selectionSource.get(i) + "/K_" + selectionTarget.get(i) + "_" + m + ")^N_" + selectionTarget.get(i) + "_" + m + "))";
			else
				reactionlist[indexInRxnList] +=  "+(BasalExpression_" + selectionTarget.get(i) + "_" + m + "+I_" + selectionTarget.get(i) + "_" + m + "*(1/(1+(" + selectionSource.get(i) + "/K_" + selectionTarget.get(i) + "_" + m + ")^N_" + selectionTarget.get(i) + "_" + m + ")))";
		}
		final JComboBox<String> combo = new JComboBox<String>((String[]) uniqueGenelist.toArray(new String[uniqueGenelist.size()]));
		combo.setSelectedIndex(0);

		reactionField.setText(reactionlist[0]);
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		topPanel.add(targetGeneName); topPanel.add(combo);
//		topPanel.setBackground(new Color(187,207,232));	
		
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

		
		//combo actions
		combo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int index = combo.getSelectedIndex();
				reactionField.setText(reactionlist[index]);
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
		renderFun(para, nodes, reactionField, drawingArea);
		
		JButton btnRender = new JButton("Render");
		btnRender.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				renderFun(para, nodes, reactionField, drawingArea);
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
				String temp = "";
				for(int j=0;j<reactionlist.length;j++){
					String newrxn = reactionlist[j];
					//check brackets
					int left = 0; int right = 0;
					char[] alph = newrxn.toCharArray();
					for(char a: alph)
						if(a == '(') left++;
						else if(a==')') right++;
					if( left != right ){
						JOptionPane.showMessageDialog(null, "Mismatched parentheses in the reaction of \""+uniqueGenelist.get(j)+"\"!", "Warning", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					
					newrxn = newrxn.replace(" ", "");
					temp += ","+newrxn;
				}		
				renewAddNodeSpeicesInitialSpeiceInitial(grn,uniqueGenelist,reactionlist,a);
				
				//add new edges to grn
				grn.getEdges().addAll(newAddEdges);
			
				//change view						
				element.getNetworkViewer().AddNewNodesAndEdges(grn.getNodes(), grn.getEdges());
				
				//UI
				repaintWhole(fatherFrame, "Add new edge(s): "+temp.substring(1)+"\n");
				escapeAction();
			}
		});   
		
		cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(null, "The new edges are not created!", "Warning", JOptionPane.INFORMATION_MESSAGE);				
				a.dispose();
				escapeAction();
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
	

	
	
	
	
	public static void renderFun(ArrayList<String> para, ArrayList<Node> nodes, JTextPane reactionField, JPanel drawingArea) {
		try {
			// get the text
			String latex = reactionField.getText();
			
			for(int i=0;i<para.size();i++){
				String items[] = para.get(i).split("_");
				String tempStr = "{"+items[0]+"}";
				for(int j=1;j<items.length;j++)
					tempStr += "_"+"{"+items[j]+"}";

				latex = latex.replace(para.get(i), "{"+tempStr+"}");
			}
			for(int i=0;i<nodes.size();i++)
				latex = latex.replace(nodes.get(i).getLabel(), "{"+nodes.get(i).getLabel()+"}");

			
			// create a formula
			TeXFormula formula = new TeXFormula(latex);
			
			// render the formla to an icon of the same size as the formula.
			TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 22);
			
			// insert a border 
			icon.setInsets(new Insets(5, 5, 5, 5));

			// now create an actual image of the rendered equation
			BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = image.createGraphics();
			g2.setColor(Color.white);
			g2.fillRect(0, 0, icon.getIconWidth(), icon.getIconHeight());
			JLabel jl = new JLabel();
			jl.setForeground(new Color(0, 0, 0));
			icon.paintIcon(jl, g2, 0, 0);
			// at this point the image is created, you could also save it with ImageIO
								
			drawingArea.removeAll();
			JPanel contentPane = new ScrollImage(image);
			contentPane.setOpaque(true);// Content pane must be opaque.
			drawingArea.add(contentPane);
			drawingArea.updateUI();
			
			// now draw it to the screen
//			Graphics g = drawingArea.getGraphics();
//			g.drawImage(image,0,0,null);
			
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Error in parsing the fomula!", "Error", JOptionPane.INFORMATION_MESSAGE);
			MsgManager.Messages.errorMessage(ex, "Error", "");
		}
	}
	
	public static ArrayList<String> array_unique(ArrayList<String> selectionTarget) {  
	    // array_unique  
	    ArrayList<String> list = new ArrayList<String>();  
	    for(int i = 0; i < selectionTarget.size(); i++) {  
	        if(!list.contains(selectionTarget.get(i))) {  
	            list.add(selectionTarget.get(i));  
	        }  
	    }  
	    return list;  
	}  
	
	private static void renewAddNodeSpeicesInitialSpeiceInitial(GeneNetwork grn, ArrayList<String> uniqueGenelist,String[] reactionlist, JDialog a) {
		//update reactionlist and parameters
		for(int i=0;i<uniqueGenelist.size();i++){
			String newReaction = reactionlist[i];
			Gene targetGene = (Gene) grn.getNode(uniqueGenelist.get(i));

			ArrayList<Gene> newinputs = new ArrayList<Gene>();
			parseNewRxn(grn, newReaction, targetGene, newinputs);
			
			//update
			targetGene.setInputGenes(newinputs);
			targetGene.setCombination(newReaction);
		}

		a.dispose();
	}
	
	public static void parseNewRxn(final GeneNetwork grn, String newReaction, Gene targetGene, ArrayList<Gene> newinputs){
		String[] items = newReaction.split("\\*|\\^|\\+|\\/|\\-|\\(|\\)");

		ArrayList<String> parameterNames_ = grn.getParameterNames_();
		ArrayList<Gene> species = grn.getSpecies();

		ArrayList<String> newPara = new ArrayList<String>();
		for(int i=0;i<items.length;i++){
			if( items[i].length() != 0 && !items[i].matches("\\d+") && !items[i].equals(" ") && !items[i].matches("\\d+\\.\\d+") ){
				if( !parameterNames_.contains(items[i]) ){ //it is not an existing parameter
					int flag = 0;
					for(int j=0;j<species.size();j++) 
						if(species.get(j).getLabel().equals(items[i]))
							flag = j+1;

					//new item
					if( flag == 0 )
						newPara.add(items[i]);
					else{
						if( !newinputs.contains(species.get(flag-1)) )
							newinputs.add(species.get(flag-1));
					}				
				}
			}	
		}//end of for
		
		//remove duplicates
		final ArrayList<String> newPara1 = array_unique(newPara);
		
		if( newPara1.size() == 0 ) return;
		
		//show dialog
		final JDialog a = new JDialog();
		a.setTitle("Set new parameters");
		a.setSize(new Dimension(300,300));
		a.setModal(true);
		a.setLocationRelativeTo(null);

		//content
		JPanel content = new JPanel();		
		JScrollPane scrollPane = new JScrollPane(content);
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		
		final ArrayList<JTextField> tempTextFieldList = new ArrayList<JTextField>(newPara1.size());
		for(int i=0;i<newPara1.size();i++){
			JLabel temp = new JLabel(newPara1.get(i)+"    ");
			JTextField defaultValue = new JTextField("1.0");
			tempTextFieldList.add(defaultValue);
			
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			panel.add(temp); panel.add(defaultValue);
			
			content.add(panel);
		}
		
		JPanel buttonPanel = new JPanel();
		JButton submitButton = new JButton("Submit");
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(submitButton); 

		//add listener
		submitButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {	
				for(int i=0;i<newPara1.size();i++){
					double newValue = Double.parseDouble(tempTextFieldList.get(i).getText());
					grn.getParameterNames_().add(newPara1.get(i));
					grn.getParameterValues_().add(newValue);
				}
				a.dispose();
			}
		});   

		//closing listener
		a.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent){
				for(int i=0;i<newPara1.size();i++){
					grn.getParameterNames_().add(newPara1.get(i));
					grn.getParameterValues_().add(1.0);
				}
				JOptionPane.showMessageDialog(null,"Assign default values to new items!","Warning",JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
				
		content.add(buttonPanel);
		content.setBorder(new EmptyBorder(5,5,5,5));
		
		a.add(scrollPane);
		a.setVisible(true);			
	}
	
	private static Node indexOfNodeName(GeneNetwork grn, String nodeName){
		ArrayList<Node> nodes = grn.getNodes();
		
		for(int i=0;i<nodes.size();i++)
			if( nodes.get(i).getLabel().equals(nodeName) )
				return nodes.get(i);
		
		return nodes.get(0);
	}

	public static void repaintWhole(JFrame c, String content) {
		JPanel a = (JPanel)((JFrame) c).getContentPane().getComponent(0);
		JSplitPane b = (JSplitPane) a.getComponent(1);
		JPanel d = (JPanel) b.getComponent(2);
		JPanel d2 = (JPanel)d.getComponent(2);
		JScrollPane g = (JScrollPane)d2.getComponent(0);
		JViewport h = (JViewport)g.getComponent(0);
		JTextArea f = (JTextArea) h.getComponent(0);
					
		((JFrame) c).getJMenuBar().removeAll();
		((JFrame) c).getContentPane().removeAll();
						
		//WindowTopPanel TopPanel = new WindowTopPanel(element);	
		MainMenu TopPanel = new MainMenu(element, (JFrame) c);	
		WindowNetworkPanel NetworkPanel = new WindowNetworkPanel(element, (JFrame) c);

		
		/** SET LAYOUT **/		
		((JFrame) c).setJMenuBar(TopPanel.getMenuBar());
		((JFrame) c).getContentPane().setLayout(new GridBagLayout());  
		NetLand.addComponent(((JFrame) c), NetworkPanel.getPanel(), 0, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1, 1);

		String temp = f.getText();
		temp += content;
		((JTextArea)((JViewport)((JScrollPane) ((JPanel)((JPanel)((JSplitPane)((JPanel)((JFrame) c).getContentPane().getComponent(0)).getComponent(1)).getComponent(2)).getComponent(2)).getComponent(0)).getComponent(0)).getComponent(0)).setText(temp);
		
		c.invalidate();
		c.repaint();
		c.setVisible(true);			
	}
}
