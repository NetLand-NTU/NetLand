package WidgetsTables;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.imageio.ImageIO;
import javax.swing.AbstractCellEditor;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import WidgetsMenu.MainMenu;
import WindowGUI.NetLand;
import WindowGUI.WindowNetworkPanel;

import ch.epfl.lis.gnw.Gene;
import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.gnwgui.DynamicalModelElement;
import ch.epfl.lis.gnwgui.NetworkElement;
import ch.epfl.lis.networks.Edge;
import ch.epfl.lis.networks.Node;


public class RxnButtonRender extends AbstractCellEditor implements TableCellRenderer,ActionListener, TableCellEditor{	
	private static final long serialVersionUID = 1L;
	
	private JButton button = null;
	private String targetGeneName;
	private static NetworkElement element;
	private Gene targetGene;
	
	/** widgets **/
	private static JTextPane reactionField;
	private JCheckBox isEditable;
	private JButton btnRender;
	private JPanel drawingArea;
	
	private static JFrame c;
	
	
	public RxnButtonRender(RxnTable parameterTable_, String targetGeneName, String rxnID, NetworkElement item, Gene targetGene, JFrame parentFrame){
		this.targetGeneName = targetGeneName;
		this.element = item;
		this.targetGene = targetGene;
		this.c = parentFrame;
		
		button = new JButton(rxnID);
		button.setHorizontalAlignment(SwingConstants.CENTER);
		button.addActionListener(this);
	}

	public Object getCellEditorValue() {
		// TODO Auto-generated method stub
		return null;
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {	
		return button;
	}

	public void actionPerformed(ActionEvent e) {
	
		final JDialog a = new JDialog();
		a.setTitle("Modify Reaction");
		a.setSize(new Dimension(1100,400));
		//a.setResizable(false);
		a.setModal(true);
		a.setLocationRelativeTo(null);

		
		Image image;
		try {
			image = ImageIO.read(this.getClass().getResource("rsc/dialogLogo.png"));
			a.setIconImage(image);
		} catch (IOException e1) {
			MsgManager.Messages.errorMessage(e1, "Error", "");
		}
		
		JPanel content = new JPanel();

		//content
		JLabel targetGeneName = new JLabel("TargetGene: " + this.targetGeneName);
		JLabel reactionFieldLable = new JLabel("Reaction:");
		isEditable = new JCheckBox("Edit");
		//isEditable.setBackground(new Color(187,207,232));	
		isEditable.setSelected(false);
		reactionField = new JTextPane();
		reactionField.setEditable(false);
		reactionField.setAutoscrolls(true);
		JScrollPane scrollPane = new JScrollPane(reactionField);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 

		JPanel textArea = new JPanel();
		textArea.setPreferredSize(new Dimension(950,70));
		textArea.setLayout(new BorderLayout());
		textArea.add(scrollPane, BorderLayout.CENTER);
		
		//set reactionField content  
		GeneNetwork grn = ((DynamicalModelElement)element).getGeneNetwork();
		HashMap<String , Double> para = new HashMap<String , Double>();   	
		for(int i=0;i<grn.getParameterNames_().size();i++){
			para.put(grn.getParameterNames_().get(i) , grn.getParameterValues_().get(i));   
		}
				
		
		final ArrayList<HashMap.Entry<String, Double>> infoIds = new ArrayList<HashMap.Entry<String, Double>>(para.entrySet());

		Comparator<HashMap.Entry<String, Double>> comparator = new Comparator<HashMap.Entry<String, Double>>(){
			public int compare(Entry<String, Double> s1, Entry<String, Double> s2) {
				return -s1.getKey().length()+s2.getKey().length();
			}
		};
		Collections.sort(infoIds, comparator);
			
		ArrayList<String> temp1 = new ArrayList<String>();
		ArrayList<Double> temp2 = new ArrayList<Double>();
		for(int i=0;i<infoIds.size();i++){
			temp1.add(infoIds.get(i).getKey());
			temp2.add(infoIds.get(i).getValue());
		}
		
		grn.getParameterNames_().clear();
		grn.getParameterValues_().clear();
		grn.setParameterNames_(temp1);
		grn.setParameterValues_(temp2);
		
		final ArrayList<Node> nodes = grn.getNodes();
		Comparator<Node> comparator1 = new Comparator<Node>(){
			public int compare(Node s1, Node s2) {				
				return -s1.getLabel().length()+s2.getLabel().length();
			}
		};
		Collections.sort(nodes,comparator1);
		
		//color paras and genes
		String disEquation = targetGene.getCombination();
		for(int i=0;i<infoIds.size();i++)
			disEquation = disEquation.replace(infoIds.get(i).getKey(), "<"+infoIds.get(i).getKey()+">");
		for(int i=0;i<nodes.size();i++)
			disEquation = disEquation.replace(nodes.get(i).getLabel(), "["+nodes.get(i).getLabel()+"]");

		//remove "["Oct4"]" from [[Oct4]gene]
		disEquation = disEquation.replaceAll("(\\[[a-zA-Z0-9]*)\\[([a-zA-Z0-9]*)\\]([a-zA-Z0-9]*\\])", "$1$2$3");
    

		setColoredTextArea(disEquation);
	
		
		drawingArea = new JPanel();
		//drawingArea.setBackground(new Color(187,207,232));	
		renderFun(infoIds, nodes);
		
		btnRender = new JButton("Render");
		btnRender.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				renderFun(infoIds, nodes);
			}
		});
		
		
		JPanel buttonPanel = new JPanel();
		//buttonPanel.setBackground(new Color(187,207,232));	
		JButton submitButton = new JButton("Submit");
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(submitButton); 
		buttonPanel.add(cancelButton);
		
		//add listener
		submitButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				boolean result = submitModification();		
				
				if( result ){
					a.dispose();
					
					repaintWhole("");
				}
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
		//content.setBackground(new Color(187,207,232));	
		content.setBorder(new EmptyBorder(10,5,10,5));
		content.setLayout(new BorderLayout());  
		
		content.add(targetGeneName, BorderLayout.NORTH);
		content.add(reactionFieldLable, BorderLayout.WEST);
		content.add(isEditable);
		
		JPanel subcontent = new JPanel();
		//subcontent.setBackground(new Color(187,207,232));	
		subcontent.setLayout(new BoxLayout(subcontent, BoxLayout.Y_AXIS));  
		subcontent.add(textArea);
		subcontent.add(btnRender);
		subcontent.add(drawingArea);
		subcontent.add(buttonPanel, BorderLayout.WEST);
		
		content.add(subcontent, BorderLayout.SOUTH);
		
									
		a.add(content);
		a.setVisible(true);		
		
	}
	
	
	public static void repaintWhole(String content) {
		JPanel a = (JPanel)(c.getContentPane().getComponent(0));
		JSplitPane b = (JSplitPane) a.getComponent(1);
		JPanel d = (JPanel) b.getComponent(2);
		JPanel d2 = (JPanel)d.getComponent(2);
		JScrollPane g = (JScrollPane)d2.getComponent(0);
		JViewport h = (JViewport)g.getComponent(0);
		JTextArea f = (JTextArea) h.getComponent(0);

		c.getJMenuBar().removeAll();
		c.getContentPane().removeAll();

		//WindowTopPanel TopPanel = new WindowTopPanel(element);	
		MainMenu TopPanel = new MainMenu(element, c);	
		WindowNetworkPanel NetworkPanel = new WindowNetworkPanel(element, c);


		/** SET LAYOUT **/		
		c.setJMenuBar(TopPanel.getMenuBar());
		c.getContentPane().setLayout(new GridBagLayout());  
		NetLand.addComponent((c), NetworkPanel.getPanel(), 0, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1, 1);

		String temp = f.getText();		
		temp += content;

		((JTextArea)((JViewport)((JScrollPane) ((JPanel)((JPanel)((JSplitPane)((JPanel)c.getContentPane().getComponent(0)).getComponent(1)).getComponent(2)).getComponent(2)).getComponent(0)).getComponent(0)).getComponent(0)).setText(temp);

		c.invalidate();
		c.repaint();
		c.setVisible(true);					
	}
	
	protected void renderFun(ArrayList<Entry<String, Double>> infoIds, ArrayList<Node> nodes) {
		try {
			// get the text
			String latex = reactionField.getText();
			
			//remove /r/n
			latex = latex.replaceAll("\r|\n", "");
			
			for(int i=0;i<infoIds.size();i++){
				String items[] = infoIds.get(i).getKey().split("_");
				String tempStr = "{"+items[0]+"}";
				for(int j=1;j<items.length;j++)
					tempStr += "_"+"{"+items[j]+"}";

				latex = latex.replace(infoIds.get(i).getKey(), "{"+tempStr+"}");
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
			BufferedImage image = new BufferedImage(icon.getIconWidth(),
					icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
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
			MsgManager.Messages.errorMessage(ex, "Error", "");
		}
	}

	private   void   insert(String   str,   AttributeSet   attrSet)   {   
        Document   doc   =   reactionField.getDocument();      
        try   {   
            doc.insertString(doc.getLength(),   str,   attrSet);   
        }   
        catch   (BadLocationException   e)   {   
            System.out.println("BadLocationException:   "   +   e);   
        }   
    }   
	
	
	private void setDocs(String   str,Color   col,boolean   bold,int   fontSize)   {   
        SimpleAttributeSet   attrSet   =   new   SimpleAttributeSet();   
        StyleConstants.setForeground(attrSet,   col);   

        if(bold==true){   
            StyleConstants.setBold(attrSet,   true);   
        }
        StyleConstants.setFontSize(attrSet,   fontSize);   
          
        insert(str,   attrSet);   
    }   
  
	private void setColoredTextArea(String temp){
		String[] sArray1 = temp.split("");
    	int flag=0;
    	
    	for(int i=0;i<sArray1.length;){
    		String k = sArray1[i];
    		
    		if(k.equals("<")){
    			flag++;
    			i++;k = sArray1[i];
    			while( flag!=0 ){
    				if( k.equals(">") )
    					flag--;
    				else if( k.equals("<") )
    					flag++;
    				else if( k.equals("[") || k.equals("]"))
    					flag=flag;
    				else
    					setDocs(k,Color.green,false,20);  	
    				i++;
    				if( i<sArray1.length )
    					k = sArray1[i];
    			}
    			
    			continue;
    		}else if(k.equals("[")){
    			flag++;
    			i++;k = sArray1[i];
    			while( flag!=0 ){
    				if( k.equals("]") )
    					flag--;
    				else if( k.equals("[") )
    					flag++;
    				else if( k.equals("<") || k.equals(">"))
    					flag=flag;
    				else
    					setDocs(k,Color.BLUE,false,20);  	
    				i++;
    				if( i<sArray1.length )
    					k = sArray1[i];
    			}
    			
    			continue;
    		}
    		
    		
    		setDocs(k,Color.black,false,20);
    		i++;
    	}
	}
	
	
	private boolean submitModification() {
		boolean result = true;
		
		//format checking of reaction formula
		GeneNetwork grn = ((DynamicalModelElement)element).getGeneNetwork();
		Gene targetGene = (Gene) grn.getNode(targetGeneName);
		String old_reaction = ((Gene) targetGene).getCombination();
		String newReaction = reactionField.getText();

		//remove /r/n
		newReaction = newReaction.replaceAll("\r|\n", "");
		
		if( !old_reaction.equals(newReaction) ){
			newReaction = newReaction.replace(" ", "");
			ArrayList<Gene> newinputs = new ArrayList<Gene>();
			
			result = parseNewRxn(grn, newReaction, targetGene,newinputs);

			System.out.print("Update rxn of "+targetGene.getLabel()+"\n");
		}
		
		return result;
	}


	public static boolean parseNewRxn(GeneNetwork grn, String newReaction, Gene targetGene, ArrayList<Gene> newinputs){
		//check if parenthese match
		int leftBracket = countString(newReaction, "(");
		int rightBracket = countString(newReaction, ")");		
		
		if( leftBracket != rightBracket ){
			JOptionPane.showMessageDialog(null,"Mismatch parentheses!","NOTE",JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		
		//check if new parameters
		String[] items = newReaction.split("\\*|\\^|\\+|\\/|\\-|\\(|\\)");

		ArrayList<String> parameterNames_ = grn.getParameterNames_();
		ArrayList<Gene> species = grn.getSpecies();
		
		for(int i=0;i<items.length;i++){
			if( items[i].length() != 0 && !items[i].matches("\\d+") && !items[i].equals(" ") && !items[i].matches("\\d+\\.\\d+") ){
				if( !parameterNames_.contains(items[i]) ){ //it is not an existing parameter
					int flag = 0;
					for(int j=0;j<species.size();j++) 
						if(species.get(j).getLabel().equals(items[i]))
							flag = j+1;

					//new item
					if( flag == 0 )
						idenfityNewItem(grn, items[i]);
					else{
						if( !newinputs.contains(species.get(flag-1)) )
							newinputs.add(species.get(flag-1));
					}				
				}
			}	
		}//end of for
		
		
		ArrayList<Gene> inputnodes = new ArrayList<Gene>();
		for( Gene agene : targetGene.getInputGenes() )
			inputnodes.add(agene);
		
		//remove edges when change inputs
		ArrayList<Edge> newedges = new ArrayList<Edge>();
		inputnodes.removeAll(newinputs);
		for(int i=0;i<grn.getEdges().size();i++){
			boolean flag = true;
			for(Gene modifier: inputnodes ){
				if( grn.getEdge(i).getTarget() == targetGene && grn.getEdge(i).getSource() == modifier ){
					flag = false;
					break;
				}			
			}
			
			if( flag ) newedges.add(grn.getEdge(i));
		}
				
		
		inputnodes = new ArrayList<Gene>();
		for( Gene agene : targetGene.getInputGenes() )
			inputnodes.add(agene);
		ArrayList<Gene> newinputsBak = new ArrayList<Gene>();
		for( Gene agene : newinputs )
			newinputsBak.add(agene);
		
		//add new edges when inputs changed
		newinputsBak.removeAll(inputnodes);
		for(Gene modifier: newinputsBak ){
			Edge newedge = new Edge();
			newedge.setSource(modifier);
			newedge.setTarget(targetGene);
			newedge.setType(Edge.UNKNOWN);
			newedges.add(newedge);
		}
		
		//update element
		targetGene.setInputGenes(newinputs);
		targetGene.setCombination(newReaction);
		grn.setEdges(newedges);
		
		element.getNetworkViewer().AddNewNodesAndEdges(grn.getNodes(), grn.getEdges());
		
		return true;
	}

	private static int countString(String str, String search_str){
		int x=0;  

        for(int i=0;i<=str.length()-1;i++) {  
            String getstr=str.substring(i,i+1);  
            if(getstr.equals(search_str)){  
                x++;  
            }  
        }  
        return x;
	}

	public static void idenfityNewItem(GeneNetwork grn, String newitem) {
		//int isParameter = JOptionPane.showConfirmDialog(null, "Is \""+newitem+"\" a parameter?", "New parameters found", JOptionPane.YES_NO_OPTION);
		
		//new parameter
		//if(isParameter == JOptionPane.YES_OPTION){ 
			String inputValue = JOptionPane.showInputDialog("Please input the value for "+newitem);
			try{    
				double newValue = Double.parseDouble(inputValue);
				grn.getParameterNames_().add(newitem);
				grn.getParameterValues_().add(newValue);
			}catch(Exception e){
				JOptionPane.showMessageDialog(null,"Assign default values to new items!","Warning",JOptionPane.INFORMATION_MESSAGE);
				grn.getParameterNames_().add(newitem);
				grn.getParameterValues_().add(1.0);
			}

			//update parameter Tab
			updateParaTab(grn);
			
		//}else
		//	JOptionPane.showMessageDialog(null,"Please identify new items!","NOTE",JOptionPane.INFORMATION_MESSAGE);

	}




	private static void updateParaTab(GeneNetwork grn) {
		if ( c instanceof JFrame )
		{
			JPanel a = (JPanel)((JFrame) c).getContentPane().getComponent(0);
			JSplitPane b = (JSplitPane) a.getComponent(1);
			JTabbedPane d = (JTabbedPane) b.getComponent(1);
			JPanel d2 = (JPanel)d.getComponent(4);
			d2.removeAll(); 
			d2.add(parametersTabb());
			d2.updateUI();
		}		
		
	}

	private static JPanel parametersTabb(){
		String[] columnName1 = {"Name", "Value"};
		JPanel tablePanel1 = new JPanel();
		new ParameterTable(tablePanel1, columnName1, element, true);
		return tablePanel1;
	}

	
	public Component getTableCellEditorComponent(JTable table, Object value, 
			boolean isSelected, int row, int column) {
		// TODO Auto-generated method stub
		return button;
	}
	
	

}
