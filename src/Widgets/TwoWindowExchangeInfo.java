package Widgets;

import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ch.epfl.lis.gnwgui.NetworkElement;
import ch.epfl.lis.networks.Node;


public class TwoWindowExchangeInfo extends JPanel{
	private static final long serialVersionUID = 1L;
		
	private NetworkElement element;
	private JList<String> listleft,listright;
	private JPanel panel;
	private JButton [] btn=new JButton[4];
	private JButton bt;
	private DefaultListModel<String> modelleft,modelright;
	private int indexleft=0,indexright=0;


	public TwoWindowExchangeInfo(NetworkElement element, int type){
		this.element = element;
		this.setLayout(new GridLayout(1,3,50,10));
		this.setPreferredSize(new Dimension(400,200));
		this.display(type);
		this.mouseAction();
		this.sort();
		this.buttonAction();
		this.setVisible(true);

		Dimension dim=Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((dim.width-this.getSize().width)/2,
				(dim.height-this.getSize().height)/2);
	}


	public void display(int type){
		listleft=new JList<String>();
		modelleft=new DefaultListModel<String>();

		if( type == 0 ){ //node
			ArrayList<Node> nodes = element.getNetworkViewer().getStructure().getNodes();
			for(int i=0;i<nodes.size();i++){
				modelleft.addElement(nodes.get(i).getLabel());
			}
		}
//		}else if( type == 1 ){ //edge
//			ArrayList<Edge> edges = element.getNetworkViewer().getStructure().getEdges();
//			for(int i=0;i<edges.size();i++){
//				modelleft.addElement(edges.get(i).getSource() + "     " + edges.get(i).getTypeString()+ "     " + edges.get(i).getTarget());
//			}
//		}

		listleft.setModel(modelleft);

		JScrollPane paneleft = new JScrollPane(listleft);
		this.add(paneleft);

		panel=new JPanel();
		panel.setLayout(new GridLayout(5,1,10,20));
		btn[0]=new JButton(">>");
		btn[1]=new JButton(">");
		btn[2]=new JButton("<<");
		btn[3]=new JButton("<");
		bt=new JButton("sort");
		for(int i=0;i<btn.length;i++){
			panel.add(btn[i]);
		}
		panel.add(bt);
//		panel.setBackground(new Color(187,207,232));

		this.add(panel);
		listright=new JList<String>();
		modelright=new DefaultListModel<String>();
		
		for(int i=0;i<element.getNetworkViewer().getBlockedNodes().size();i++){
//			modelright.addElement(element.getNetworkViewer().getBlockedNodes().get(i).getLabel());
		}
		listright.setModel(modelright);
		JScrollPane paneright = new JScrollPane(listright);
		this.add(paneright);
	}



	public void mouseAction(){
		listleft.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				if(e.getClickCount()==1)
					indexleft = listleft.locationToIndex(e.getPoint());

				if(e.getClickCount()==2){
					indexleft = listleft.locationToIndex(e.getPoint());
					modelright.addElement(modelleft.getElementAt(indexleft));
					modelleft.removeElementAt(indexleft);
				}
				setButton();
			}
		});

		listright.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				if(e.getClickCount()==1){
					indexright = listright.locationToIndex(e.getPoint());
				}

				if(e.getClickCount()==2){
					indexright = listright.locationToIndex(e.getPoint());
					modelleft.addElement(modelright.getElementAt(indexright));
					modelright.removeElementAt(indexright);
				}
				setButton();
			}
		});
	}


	public void buttonAction(){
		setButton();

		btn[0].addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){    
				for(int i=0;i<modelleft.getSize();i++){
					modelright.addElement(modelleft.getElementAt(i));
				}
				modelleft.clear();
				setButton();
			}
		});

		btn[1].addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				modelright.addElement(modelleft.getElementAt(indexleft));
				modelleft.removeElementAt(indexleft);
				indexleft=0;
				setButton();
			}   
		});

		btn[2].addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){    
				for(int i=0;i<modelright.getSize();i++){
					modelleft.addElement(modelright.getElementAt(i));
				}
				modelright.clear();
				setButton();
			}
		});

		btn[3].addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				modelleft.addElement(modelright.getElementAt(indexright));
				modelright.removeElementAt(indexright);
				indexright=0;
				setButton();
			}   
		});
	}


	
	public void sort(){
		setButton();
		bt.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

				List<String> llist=new ArrayList<String>();
				for(int i=0;i<modelleft.size();i++){
					llist.add(modelleft.get(i));
					//System.out.print(listleft.getSelectedIndices()[i]+"\t");
				}
				modelleft.clear();
				Collections.sort(llist);
				for(int i=0;i<llist.size();i++){
					modelleft.addElement(llist.get(i));
				}
				listleft.setModel(modelleft);

				List<String> rlist=new ArrayList<String>();
				for(int i=0;i<modelright.size();i++){
					rlist.add(modelright.get(i));
				}
				modelright.clear();
				Collections.sort(rlist);
				for(int i=0;i<rlist.size();i++){
					modelright.addElement(rlist.get(i));
				}
				listright.setModel(modelright);
				setButton();
			}   
		});
	}


	
	public void setButton(){
		if(modelleft.getSize()==0){
			btn[0].setEnabled(false);
			btn[1].setEnabled(false);
		}else{
			btn[0].setEnabled(true);
			btn[1].setEnabled(true);
		}
		if(modelright.getSize()==0){
			btn[2].setEnabled(false);
			btn[3].setEnabled(false);
		}else{
			btn[2].setEnabled(true);
			btn[3].setEnabled(true);
		}
	}

	public JList<String> getListright(){ return listright; }
	public JList<String> getListleft(){ return listleft; }
}