package NetworkDisplay;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import WindowGUI.NetLand;
import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.gnwgui.DynamicalModelElement;
import ch.epfl.lis.gnwgui.NetworkElement;
import ch.epfl.lis.gnwgui.NetworkGraph;
import ch.epfl.lis.gnwgui.StructureElement;
import ch.epfl.lis.imod.ImodNetwork;


public class NetworkView {
	protected NetworkElement item_ = null;
	JPanel leftPanel;
	JPanel centerPanel;



	public NetworkView(NetworkElement element) {
		leftPanel = new JPanel();
		centerPanel = new JPanel();

		item_ = element;

		init(leftPanel, centerPanel);
		String title1, title2;
		title1 = title2 = "";

//		if (item_ instanceof StructureElement) {
//			ImodNetwork network = ((StructureElement)item_).getNetwork();
//			title1 = item_.getLabel();
//			title2 = network() + " nodes, " + network.getNumEdges() + " edges";
//		} else if (item_ instanceof DynamicalModelElement) {
			GeneNetwork geneNetwork = ((DynamicalModelElement)item_).getGeneNetwork();
			title1 = item_.getLabel();
			title2 = geneNetwork.getSize() + " genes, " + geneNetwork.getNumEdges() + " interactions";
//		}
		JLabel networkInfo = new JLabel(title1 + " (" + title2 + ")");

		//set networkInfo layout format
//		networkInfo.setEditable(false);
//		networkInfo.setLineWrap(true);
//		networkInfo.setWrapStyleWord(true);
		//gj
		networkInfo.setBorder(BorderFactory.createLineBorder(Color.gray));
		//gj
		//networkInfo.setPreferredSize(new Dimension(300,5));

		if (item_.getNetworkViewer() == null) {
			item_.setNetworkViewer(new NetworkGraph(item_));
		}
		
//		//legend
//		JLabel labels = new JLabel(new ImageIcon(getClass().getResource("rsc/interaction-labels.png")));

		
		//centerPanel.setPreferredSize(new Dimension(400,1000));
		//set layout
		centerPanel.setLayout(new GridBagLayout());  
		
		// Row 1
		NetLand.addComponent(centerPanel, networkInfo, 0, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1, 0);
		// Row 2
		JPanel figure = item_.getNetworkViewer().getScreen();

		//figure.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		NetLand.addComponent(centerPanel, figure, 0, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1, 1);
		
		
		//add listener
//		centerPanel.addComponentListener(new ComponentAdapter(){
//			public void componentResized(ComponentEvent e) {
//				System.out.print("compoenent\n");
//				//set the new size of figure
//				item_.getNetworkViewer().getVisualizationViewer().setPreferredSize(centerPanel.getSize());
//				item_.getNetworkViewer().getControl().changeGraphLayout((String) item_.getNetworkViewer().getControl().getLayoutCombo().getSelectedItem());     
//			}
//		});

		

		// Add and display the network viewer controls
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.NORTH;
		leftPanel.add(item_.getNetworkViewer().getControl(), gridBagConstraints);
	}


	private void init(JPanel leftPanel, JPanel centerPanel){
		centerPanel.setBackground(Color.WHITE);
		leftPanel.setBackground(Color.WHITE);		
	}

	
	public NetworkElement getElement(){ return item_; }
	public JPanel getLeftPanel(){ return leftPanel; }
	public JPanel getCenterPanel(){ return centerPanel; }
}
