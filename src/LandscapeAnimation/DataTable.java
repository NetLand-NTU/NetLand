package LandscapeAnimation;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.networks.Node;


public class DataTable {
	private JPanel tablePanel;
	private JTable table;
	private String[] columnName = null; 
	private double maxU = 0; 
	private double minU = 100000;
	
	private GeneNetwork grn_;
	private String[] focusGenesList;
	private boolean isProbabilisticLandscape;
	private JRadioButton radio_set;
	private List<MyPickablePoint> points;
	private ColorMapper myColorMapper;
	private String xLabel = "X="; 
	private String yLabel = "Y=";
	
	public DataTable(JPanel tablePanel, double[] landpointsX, double[] landpointsY, double[][] landpointsZ, GeneNetwork grn_, boolean isProbabilisticLandscape, JRadioButton radio_set, List<MyPickablePoint> points,  ColorMapper myColorMapper){
		this.tablePanel = tablePanel;
		this.grn_ = grn_;
		this.isProbabilisticLandscape = isProbabilisticLandscape;
		this.radio_set = radio_set;
		this.points = points;
		this.myColorMapper = myColorMapper;

		focusGenesList = grn_.getLand_focusGenesList();
		int[] focus_index = new int[focusGenesList.length];
		for(int j=0;j<focusGenesList.length;j++)
			for(int i=0;i<grn_.getNodes().size();i++)
				if( grn_.getNodes().get(i).getLabel().equals(focusGenesList[j]) )
					focus_index[j] = i;
		
		
		ArrayList<String[]> data = new ArrayList<String[]>();		
		//colume names
		String[] tmp = new String[landpointsY.length+1];
		columnName = new String[landpointsY.length+1];
		tmp[0] = "";columnName[0] = "";
		
		
		if( !isProbabilisticLandscape ){
			xLabel = "Comp1: ";
			yLabel = "Comp2: ";
		}
		
		
		for(int i=0;i<landpointsY.length;i++){
			tmp[i+1] = yLabel+Double.toString(landpointsY[i]);
			columnName[i+1] = "";
		}
		data.add(0, tmp);
		
		
		for(int i=0;i<landpointsX.length;i++){
			String[] tmp1 = new String[landpointsX.length+1];
			tmp1[0] = xLabel+Double.toString(landpointsX[i]);
			for(int j=0;j<landpointsY.length;j++)
				tmp1[j+1] = Double.toString(Math.floor(landpointsZ[i][j]*100)/100.0);
			data.add(i+1, tmp1);
		}
		

		String[][] rowData = new String[landpointsX.length+1][landpointsY.length+1];
		//convert data		
		for(int i=0;i<landpointsX.length+1;i++)
			for(int j=0;j<landpointsY.length+1;j++)
				rowData[i][j] = data.get(i)[j];
		
		
		//max data min data
		for(int i=0;i<landpointsX.length;i++)
			for(int j=0;j<landpointsY.length;j++){
				if( landpointsZ[i][j]>maxU )
					maxU = landpointsZ[i][j];
				if( landpointsZ[i][j]<minU )
					minU = landpointsZ[i][j];
			}
		
		createDataTable(rowData);			
	}
	
	
	
	private void createDataTable(String[][] rowData) { 
		final DefaultTableModel tableModel = new DefaultTableModel( rowData, columnName){     
			public boolean isCellEditable(int row,int column){  
				return false;
			}
		};
		
		
		table = new JTable(tableModel); 
		
		
		table.addMouseListener(new MouseAdapter(){  
			public void mouseClicked(MouseEvent e) { 
				if(e.getClickCount() == 2 ){ 
					int row =((JTable)e.getSource()).rowAtPoint(e.getPoint()); 
					int col=((JTable)e.getSource()).columnAtPoint(e.getPoint()); 
					

					if( row>0 && col>0 ){
						String cellVal=(String)(tableModel.getValueAt(row,col)); 

						String xValue = (String) ((JTable)e.getSource()).getValueAt(row, 0);
						String yValue = (String) ((JTable)e.getSource()).getValueAt(0, col);

						//high light in landscape
						for(MyPickablePoint p: points)
							if( p.rowNo==col && p.colNo==row ){
								System.out.println(p);
								p.setColor(org.jzy3d.colors.Color.RED);
								p.setWidth(7);
							}else{
								p.setColor(myColorMapper.getColor(p.xyz));
								p.setWidth(3);
							}
						
						//alert dialog
						final JDialog a = new JDialog();
						a.setTitle("Landscape data");
						a.setModal(true);

						JPanel displayPanel = new JPanel();
						JLabel text1 = new JLabel("U: "+cellVal);
						JLabel text2 = new JLabel("Coordinates in Landscape:");
						JLabel xAxisText = new JLabel(xValue);
						JLabel yAxisText = new JLabel(yValue);

						displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS));
						JScrollPane  pane  =  new  JScrollPane(displayPanel);
						pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
						displayPanel.add(text1);
						displayPanel.add(text2);
						displayPanel.add(xAxisText);
						displayPanel.add(yAxisText);
						
						

						if( isProbabilisticLandscape ){ //two markers
							a.setSize(new Dimension(300,150));
						}else{ //gpdm
							a.setSize(new Dimension(300,220));

							JLabel text3 = new JLabel("Original vector:");
							displayPanel.add(text3);

							//get yped
							DoubleMatrix2D ypred = grn_.getAllY();
							ArrayList<Node> nodes = grn_.getNodes();
							final DoubleMatrix1D initialState = new DenseDoubleMatrix1D(nodes.size());
							
							for(int i=0;i<focusGenesList.length;i++){
								double value = ypred.get((row-1)*(table.getRowCount()-1)+col-1, i);
								JLabel tempxAxisText = new JLabel(focusGenesList[i]+": "+value);
								initialState.set(i, value);
								displayPanel.add(tempxAxisText);
							}
							
//							//set this cell as initial state
//							JButton generateTrajButton = new JButton("Generate trajectory from this point");
//							generateTrajButton.addActionListener(new ActionListener(){
//								public void actionPerformed(ActionEvent e){
//									Float temp = Float.parseFloat(Double.toString(grn_.getSpecies_initialState().get(0)));
//									if( temp.isNaN() ){
//										grn_.setInitialState(initialState);
//									}
//									grn_.setInitialState(initialState);
//									grn_.setSpecies_initialState(initialState);
//									a.dispose();
//									radio_set.doClick();				
//								}
//							});
//							
//							displayPanel.add(generateTrajButton);
						}

						a.add(pane);

						a.setLocationRelativeTo(null);  
						a.setVisible(true);
						
					}
				} else 
					return; 	
			}

			

		});
		
		
		
		TableColumnModel tcm = table.getColumnModel();
		tcm.getColumns();
        for (int i = 0, n = tcm.getColumnCount(); i < n; i++) {
            TableColumn tc = tcm.getColumn(i);   
            tc.setCellRenderer(new MyCellRendener());
        }
        
       

		
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		//scrollPane.setPreferredSize(new Dimension(200,500));

		
		tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS)); 
		tablePanel.add(Box.createVerticalStrut(10)); 
		tablePanel.add(scrollPane); 
		tablePanel.add(Box.createVerticalStrut(10)); 
	}
	
	
	private class MyCellRendener extends DefaultTableCellRenderer {
		private JLabel l = new JLabel(){
            public boolean isOpaque() {return true;}
        };
 
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column){
        	l.setText(value.toString());
            l.setForeground(hasFocus ? Color.RED : Color.black);
            l.setBackground(hasFocus ? Color.YELLOW :Color.white);
            
        	ColorMapper mycolorMapper = new ColorMapper(new ColorMapRainbow(), minU, maxU);//((Shape) comps.get(0)).getColorMapper();
        	
        	if( row>=0 && column>0 ){
        		if( t.getValueAt(row, column).toString().startsWith(yLabel) )
        			setBackground(Color.white);
        		else{
        			//int color = 250 - (int) (step*(Double.parseDouble((String) t.getValueAt(row, column))-minU));
        			org.jzy3d.colors.Color mycolor = mycolorMapper.getColor(Double.parseDouble((String) t.getValueAt(row, column)));
        			setBackground(new Color(mycolor.r,mycolor.g,mycolor.b));
        			
        			l.setForeground(hasFocus ? Color.RED : new Color(1-mycolor.r, 1-mycolor.g, 1-mycolor.b));
                    l.setBackground(hasFocus ? Color.YELLOW : new Color(mycolor.r,mycolor.g,mycolor.b));//new Color(mycolor.r,mycolor.g,mycolor.b,0.4f)

        		}
        	}else{
        		setBackground(Color.white);
        	}
            //return super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
        	return l;
        }
    }
	
	public JTable getTable(){
		return table;
	}
	
	public void setPoints(List<MyPickablePoint> points){
		this.points = points;
	}
}
