package Widgets;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

import ch.epfl.lis.gnw.Gene;
import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.gnwgui.DynamicalModelElement;
import ch.epfl.lis.gnwgui.NetworkElement;


public class ModelTree {
	private JTree tree;
	private JPanel treePanel;
	private GeneNetwork grn;
	
	
	public ModelTree(GeneNetwork grn){
		this.grn = grn;
		treePanel = new JPanel();
		
		setTree();
//		tree.setPreferredSize(new Dimension(200,3500));
		JScrollPane scrollPane = new JScrollPane(tree);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
		
		treePanel.setLayout(new BorderLayout());
		treePanel.add(scrollPane, BorderLayout.CENTER);
		//treePanel.setMinimumSize(new Dimension(200,500));
	}

	public void setTree(){		
		//load dynamic model
//		DynamicalModelElement grnItem = new DynamicalModelElement((DynamicalModelElement) element);
		
		//add leaves
		DefaultMutableTreeNode lx=new DefaultMutableTreeNode("Model");
		DefaultMutableTreeNode qr=new DefaultMutableTreeNode("Genes");
		DefaultMutableTreeNode tx=new DefaultMutableTreeNode("Edges");

		lx.add(qr);
		lx.add(tx);
		
		for(int i=0;i<grn.getSpecies().size();i++){
			DefaultMutableTreeNode temp = new DefaultMutableTreeNode(grn.getSpecies().get(i).getLabel());
			qr.add(temp);
		}
		
		for(int i=0;i<grn.getEdges().size();i++){
			String rnName = "";
			if( ((Gene)grn.getEdge(i).getTarget()).getRnID().equals("") ){
				rnName = "R_"+((Gene)grn.getEdge(i).getTarget()).getLabel();
				((Gene)grn.getEdge(i).getTarget()).setRnID(rnName);
			}else
				rnName = ((Gene)grn.getEdge(i).getTarget()).getRnID();
			
			DefaultMutableTreeNode temp = new DefaultMutableTreeNode(rnName);
			String rxn = "";
			if( grn.getEdge(i).getTypeString().equals("+-") || grn.getEdge(i).getTypeString().equals("?") )
				rxn = grn.getEdge(i).toString() + " ";
			else
				rxn = grn.getEdge(i).toString() + " " + grn.getEdge(i).getTypeString();
			
			DefaultMutableTreeNode temp_sub = new DefaultMutableTreeNode(rxn);
			tx.add(temp);
			temp.add(temp_sub);
		}
		

		tree=new JTree(lx);
		
//		tree.addTreeSelectionListener(new TreeSelectionListener() {
//			public void valueChanged(TreeSelectionEvent e) {
//				DefaultMutableTreeNode node=(DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
//				if(node==null) return;
//				if(node.isLeaf()){
//					leaf(node);
//				}else{
//					branch(node);
//				}
//
//			}
//		});
	} 
	

	public JPanel getTreePanel() {
		return treePanel;
	}

	public void setTreePanel(JPanel treePanel) {
		this.treePanel = treePanel;
	} 
}

