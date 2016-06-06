package WidgetsButtons;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import ch.epfl.lis.gnwgui.NetworkElement;

public class TopButtonGroup {
	public JPanel buttonPanel; 
	
	public TopButtonGroup(NetworkElement element){
		buttonPanel = new JPanel();
		
		// set format
		//buttonPanel.setBackground(new Color(187,207,232));
		buttonPanel.setBorder(BorderFactory.createEtchedBorder());
		
		TopButton openButton = new TopButton(0,new ImageIcon(getClass().getResource("rsc/buttons/open.png")), new ImageIcon(getClass().getResource("rsc/buttons/openOver.png")), element);
		TopButton saveButton = new TopButton(1,new ImageIcon(getClass().getResource("rsc/buttons/save.png")), new ImageIcon(getClass().getResource("rsc/buttons/saveOver.png")), element);
		
		TopButton blockButton = new TopButton(2, new ImageIcon(getClass().getResource("rsc/buttons/remove.png")), new ImageIcon(getClass().getResource("rsc/buttons/removeOver.png")), element);
		TopButton addButton = new TopButton(3, new ImageIcon(getClass().getResource("rsc/buttons/add.png")), new ImageIcon(getClass().getResource("rsc/buttons/addOver.png")), element);
		TopButton resetButton = new TopButton(4, new ImageIcon(getClass().getResource("rsc/buttons/reset.png")), new ImageIcon(getClass().getResource("rsc/buttons/resetOver.png")), element);
		
		TopButton trajButton = new TopButton(5, new ImageIcon(getClass().getResource("rsc/buttons/traj.png")), new ImageIcon(getClass().getResource("rsc/buttons/trajOver.png")), element);
		TopButton landButton = new TopButton(6, new ImageIcon(getClass().getResource("rsc/buttons/land.png")), new ImageIcon(getClass().getResource("rsc/buttons/landOver.png")), element);
		
		TopButton legendButton = new TopButton(7, new ImageIcon(getClass().getResource("rsc/buttons/legend.png")), new ImageIcon(getClass().getResource("rsc/buttons/legendOver.png")), element);
		TopButton controlGrapheButton = new TopButton(8, new ImageIcon(getClass().getResource("rsc/buttons/contr1.png")), new ImageIcon(getClass().getResource("rsc/buttons/contr1Over.png")), element);
		TopButton controlNodeButton = new TopButton(9, new ImageIcon(getClass().getResource("rsc/buttons/contr2.png")), new ImageIcon(getClass().getResource("rsc/buttons/contr2Over.png")), element);
		TopButton screenButton = new TopButton(10, new ImageIcon(getClass().getResource("rsc/buttons/screen.png")), new ImageIcon(getClass().getResource("rsc/buttons/screenOver.png")), element);

		//set tooltip
		openButton.getButton().setToolTipText("open network file");
		saveButton.getButton().setToolTipText("save SBML");
		blockButton.getButton().setToolTipText("block nodes");
		addButton.getButton().setToolTipText("add node/edge");
		resetButton.getButton().setToolTipText("reset");
		trajButton.getButton().setToolTipText("generate trajectory");
		landButton.getButton().setToolTipText("generate landscape");
		legendButton.getButton().setToolTipText("show legend");
		controlGrapheButton.getButton().setToolTipText("move graph");
		controlNodeButton.getButton().setToolTipText("move node");
		screenButton.getButton().setToolTipText("screenshot");
		
		// search box
		JLabel vertexSearchLabel = new JLabel();
		vertexSearchLabel.setText("Node search:");

		
		/** LAYOUT **/
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS)); 
		buttonPanel.add(openButton.getButton());
		buttonPanel.add(saveButton.getButton());
		buttonPanel.add(new JSeparator(JSeparator.VERTICAL), BorderLayout.WEST);
		
		
		buttonPanel.add(blockButton.getButton());
		buttonPanel.add(addButton.getButton());
		buttonPanel.add(resetButton.getButton());
		
		buttonPanel.add(new JSeparator(JSeparator.VERTICAL));
		
		buttonPanel.add(trajButton.getButton());
		buttonPanel.add(landButton.getButton());
		
		buttonPanel.add(new JSeparator(JSeparator.VERTICAL));
		
		buttonPanel.add(legendButton.getButton());
		buttonPanel.add(controlGrapheButton.getButton());
		buttonPanel.add(controlNodeButton.getButton());
		buttonPanel.add(screenButton.getButton());
		
		buttonPanel.add(new JSeparator(JSeparator.VERTICAL));
		
		buttonPanel.add(vertexSearchLabel);
		buttonPanel.add(element.getNetworkViewer().getControl().getSearch());

	}
	
	//get and set
	public JPanel getPanel(){ return buttonPanel; }
	
}
