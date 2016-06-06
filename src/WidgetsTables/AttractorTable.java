package WidgetsTables;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;


import ch.epfl.lis.gnw.GeneNetwork;



public class AttractorTable extends JPanel  {  
	private GeneNetwork grn;
	private String[] focusGenesList;
	private double min = 100000;
	private double max = -100000;
	
    public AttractorTable(GeneNetwork grn, String[] focusGenesList){  
    	this.grn = grn;
    	this.focusGenesList = focusGenesList;
    	
        intiComponent();  
    }  
  
  
    private void intiComponent() {  
        JTable table = new JTable(new MyTableModel());  
        
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
		
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(440,200));
		add(scrollPane, BorderLayout.CENTER);  
        this.setVisible(true);   
    }  
  
    private class MyTableModel extends AbstractTableModel {    
        String[] columnNames =  new String[grn.getSumPara().rows()+1];
        Object[][] data;
         
        public MyTableModel() { 
        	columnNames[0] = "Gene Names";

        	int rows = grn.getSumPara().rows();
        	for(int i=1;i<=rows;i++)
        		columnNames[i] = "#"+i;
        	
        	data = new Object[focusGenesList.length+1][columnNames.length];  
        	
   	
        	//calcualte percentage
        	String[] percentageList = new String[rows];
        	for(int i=0;i<rows;i++)			
        		percentageList[i] = grn.getCounts()[i]/((double)grn.getLand_itsValue())+" ("+grn.getCounts()[i]+")  ";
    		
        	//data first line
        	data[0][0] = "percentage";
        	for(int i=1;i<columnNames.length;i++)
        		data[0][i] = percentageList[i-1];
        	
        	//get gene index
			int[] focus_index = new int[focusGenesList.length];
			for(int j=0;j<focusGenesList.length;j++)
				for(int i=0;i<grn.getNodes().size();i++)
					if( grn.getNode(i).getLabel().equals(focusGenesList[j]) )
						focus_index[j] = i;

			

        	for (int i = 1; i <= focusGenesList.length; i++) {       		
        		data[i][0] = focusGenesList[i-1];
        		for(int j=1;j<columnNames.length;j++){
        			data[i][j] = Math.abs(Math.floor(grn.getSumPara().get(j-1, focus_index[i-1])*100)/100);
        			if( grn.getSumPara().get(j-1, focus_index[i-1]) > max ) max = grn.getSumPara().get(j-1, focus_index[i-1]);
        			if( grn.getSumPara().get(j-1, focus_index[i-1]) < min ) min = grn.getSumPara().get(j-1, focus_index[i-1]);
        		}
        	}  
        }  
  
       
      
        @Override  
        public String getColumnName(int column) {  
            return columnNames[column];  
        }  
          
     
        @Override  
        public int getColumnCount() {  
            return columnNames.length;  
        }  
  
        
        @Override  
        public int getRowCount() {  
            return data.length;  
        }  
  
       
        @Override  
        public Object getValueAt(int rowIndex, int columnIndex)  
        {  
            return data[rowIndex][columnIndex];  
        }  
  
      
        @Override  
        public Class<?> getColumnClass(int columnIndex)  
        {  
            return data[0][columnIndex].getClass();  
        }  
  
        
        @Override  
        public boolean isCellEditable(int rowIndex, int columnIndex)  
        {  
//            if (columnIndex < 2)  
                return false;  
//            else  
//                return true;  
        }  
          
       
        @Override  
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)  
        {  
            data[rowIndex][columnIndex] = aValue;  
            fireTableCellUpdated(rowIndex, columnIndex);  
        }  
  
    }  
    
    private class MyCellRendener extends DefaultTableCellRenderer {
		private JLabel l = new JLabel(){
            public boolean isOpaque() {return true;}
        };
 
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column){
        	l.setText(value.toString());
            l.setForeground(hasFocus ? Color.RED : Color.black);
            l.setBackground(hasFocus ? Color.YELLOW :Color.white);
            
        	ColorMapper mycolorMapper = new ColorMapper(new ColorMapRainbow(), min, max);//((Shape) comps.get(0)).getColorMapper();
        	
        	if( row>0 && column>0 ){           		
        		//int color = 250 - (int) (step*(Double.parseDouble((String) t.getValueAt(row, column))-minU));
        		org.jzy3d.colors.Color mycolor = mycolorMapper.getColor((double)t.getValueAt(row, column));
        		setBackground(new Color(mycolor.r,mycolor.g,mycolor.b));

        		l.setForeground(hasFocus ? Color.RED : new Color(1-mycolor.r, 1-mycolor.g, 1-mycolor.b));
        		l.setBackground(hasFocus ? Color.YELLOW : new Color(mycolor.r,mycolor.g,mycolor.b));//new Color(mycolor.r,mycolor.g,mycolor.b,0.4f)
        	}else{
        		setBackground(Color.white);
        	}
            //return super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
        	return l;
        }
    }
  
}  
