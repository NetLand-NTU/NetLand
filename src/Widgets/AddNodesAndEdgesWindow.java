package Widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

public class AddNodesAndEdgesWindow extends JDialog {
	protected JPanel centerPanel_;
	protected JTextField nameOfNode;
	protected DefaultListModel<String> newNodesName;
	protected JList<String> newNodesList;
	protected JList<String> newEdgesList;
	protected DefaultListModel<String> newEdgesName;
	protected JComboBox<String> target;
	protected JComboBox<String> source;
	protected JComboBox<Object> type;
	protected JButton addNodeButton;
	protected JButton delNodeButton;
	protected JButton delEdgeButton;
	protected JButton addEdgeButton;
	protected JButton submitButton;
	protected JButton cancelButton;
	

	public AddNodesAndEdgesWindow(Frame aFrame) {
		super(aFrame);
		//setResizable(false);		
		setSize(400,430);
		setTitle("Add Nodes/Edges");
		
		
		//set icon
		Image image;
		try {
			URL ab = ClassLoader.getSystemResource("WidgetsButtons/rsc/buttons/add.png");
			image = ImageIO.read(ab);
			setIconImage(image);
		} catch (IOException e1) {
			MsgManager.Messages.errorMessage(e1, "Error", "");
		}


		centerPanel_ = new JPanel();
		centerPanel_.setBackground(Color.WHITE);
		getContentPane().add(centerPanel_);

		//content
		/** panels **/
		JPanel addNode = new JPanel();
		addNode.setBorder(BorderFactory.createTitledBorder("Add Node"));
		JPanel addEdge = new JPanel();
		addEdge.setBorder(BorderFactory.createTitledBorder("Add Edge"));

		/** add nodes **/
		JLabel setname = new JLabel("Input Gene name:");
		nameOfNode = new JTextField(20);

		//nameOfNode.setPreferredSize(new Dimension(50,100));
		/** list **/
		JLabel genelist = new JLabel("Gene list:");
		newNodesName = new DefaultListModel<String>();

		newNodesList = new JList<String>(newNodesName);
		newNodesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); //ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
		newNodesList.setSelectedIndex(0);
		newNodesList.setVisibleRowCount(3);     
		//newNodesList.setPreferredSize(new Dimension(200,30));
		

		JScrollPane nodeListScrollPane = new JScrollPane(newNodesList);  
		nodeListScrollPane.setPreferredSize(new Dimension(300,50));
		
		JLabel edgeList = new JLabel("Edge list:");
		newEdgesName = new DefaultListModel<String>();

		newEdgesList = new JList<String>(newEdgesName);
		newEdgesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); //ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
		newEdgesList.setSelectedIndex(0);
		newEdgesList.setVisibleRowCount(3);        

		JScrollPane edgeListScrollPane = new JScrollPane(newEdgesList);    
		edgeListScrollPane.setPreferredSize(new Dimension(300,50));
		
		
		
		//Jcombobox
		source = new JComboBox<String>();
		target = new JComboBox<String>();
		

		String[] types = {"+", "-"}; 	
		type = new JComboBox<Object>(types);

		
		/** buttons **/
		addNodeButton = new JButton("Add new nodes to list");
		delNodeButton = new JButton("Remove nodes from list");
		delEdgeButton = new JButton("Remove edges from list");
		addEdgeButton = new JButton("Add new edges to list");


		submitButton = new JButton("Submit");
		cancelButton = new JButton("Cancel");
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(submitButton);
		buttonPanel.add(cancelButton);
		

		//add component
		addNode.setLayout(new BoxLayout(addNode, BoxLayout.Y_AXIS));
		JPanel inputGenename = new JPanel();
		inputGenename.add(setname); inputGenename.add(nameOfNode);
		addNode.add(inputGenename);
		
		
		JPanel inputGeneButton = new JPanel();
		inputGeneButton.add(addNodeButton); inputGeneButton.add(delNodeButton);
		addNode.add(inputGeneButton);

		JPanel genelistPanel = new JPanel();
		genelistPanel.add(genelist); genelistPanel.add(nodeListScrollPane);
		addNode.add(genelistPanel);
		

		addEdge.setLayout(new BoxLayout(addEdge, BoxLayout.Y_AXIS));
		addEdge.add(source);
		addEdge.add(type);
		addEdge.add(target);
		
		JPanel inputEdgeButton = new JPanel();
		inputEdgeButton.add(addEdgeButton); inputEdgeButton.add(delEdgeButton);
		addEdge.add(inputEdgeButton);
		
		JPanel edgelistPanel = new JPanel();
		edgelistPanel.add(edgeList); edgelistPanel.add(edgeListScrollPane);
		addEdge.add(edgelistPanel);


		centerPanel_.setLayout(new BoxLayout(centerPanel_, BoxLayout.Y_AXIS));
		centerPanel_.add(addNode);
		centerPanel_.add(addEdge);
		centerPanel_.add(buttonPanel);
		
		
		setLocationRelativeTo(aFrame);
	}
	
	
	public void escapeAction() {
		this.dispose();
	}
}
