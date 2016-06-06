package LandscapeAnimation;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class TrajDataTable extends JPanel {
	private JPanel tabledataPanel;
	private double[] time;
	private double[][] trajDataY;
	private JTable table;
	private org.jzy3d.colors.Color[] colors;
	
	
	public TrajDataTable(double[] time, double[][] trajDataY, String[] geneNames, org.jzy3d.colors.Color[] colors){
		this.time = time;
		this.trajDataY = trajDataY;
		this.colors = colors;
		
		tabledataPanel = new JPanel();
		
		//assign data
		ArrayList<String[]> data = new ArrayList<String[]>();		
		
		String[] tmp = new String[trajDataY.length+1];
		tmp[0] = "";
		for(int i=0;i<geneNames.length;i++){
			tmp[i+1] = geneNames[i];
		}

		
		for(int i=0;i<time.length;i++){
			String[] tmp1 = new String[trajDataY.length+1];
			tmp1[0] = "t="+Double.toString(time[i]);
			for(int j=0;j<trajDataY.length;j++)
				tmp1[j+1] = Double.toString(Math.floor(trajDataY[j][i]*100)/100.0);
			data.add(i, tmp1);
		}
		
		String[][] rowData = new String[data.size()][trajDataY.length+1];
		//convert data		
		for(int i=0;i<time.length;i++)
			for(int j=0;j<trajDataY.length+1;j++)
				rowData[i][j] = data.get(i)[j];
		
		createDataTable(rowData, tmp);		
	}

	private void createDataTable(String[][] rowData, String[] geneNames) { 
		final DefaultTableModel tableModel = new DefaultTableModel( rowData, geneNames){     
			public boolean isCellEditable(int row,int column){  
				return false;
			}
		};

		table = new JTable(tableModel); 

		MyCellRendener render = new MyCellRendener();
		TableColumnModel tcm = table.getColumnModel();
		tcm.getColumns();
		for (int i = 0, n = tcm.getColumnCount(); i < n; i++) {
			TableColumn tc = tcm.getColumn(i);   
			tc.setCellRenderer(render);
		}


		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		//scrollPane.setPreferredSize(new Dimension(400,100));

		tabledataPanel.setLayout(new BoxLayout(tabledataPanel, BoxLayout.Y_AXIS)); 
		tabledataPanel.add(Box.createVerticalStrut(10)); 
		tabledataPanel.add(scrollPane); 
		tabledataPanel.add(Box.createVerticalStrut(10)); 
	}


	

	private class MyCellRendener extends DefaultTableCellRenderer {
		private JLabel l = new JLabel(){
			public boolean isOpaque() {return true;}
		};

		public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column){
			l.setText(value.toString());
			l.setBackground(Color.white);
			l.setForeground(Color.BLACK);
			
			if( column>0 && row>=0 ){		
				java.awt.Color color = new java.awt.Color(colors[column-1].r, colors[column-1].g, colors[column-1].b);
				l.setForeground(color);
			}
			
			if (isSelected || hasFocus)
				l.setBackground(Color.LIGHT_GRAY);
			
			return l;
		}
	}


	
	public JPanel getTabledataPanel() {
		return tabledataPanel;
	}

	public double[] getTime() {
		return time;
	}

	public void setTime(double[] time) {
		this.time = time;
	}

	public double[][] getTrajDataY() {
		return trajDataY;
	}

	public void setTrajDataY(double[][] trajDataY) {
		this.trajDataY = trajDataY;
	}


	public JTable getTable() {
		return table;
	}



	public void setTable(JTable table) {
		this.table = table;
	}
	
	
}
