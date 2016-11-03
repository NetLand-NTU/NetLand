package WidgetsTables;


import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;


public class ConvergenceTable  extends JPanel  {  
	private int[] isConvergent;

	
    public ConvergenceTable(int[] isConvergent){  
    	this.isConvergent = isConvergent;
    	
        intiComponent();  
    }  
  
  
    private void intiComponent() {  
        JTable table = new JTable(new MyTableModel());  
        
        TableColumnModel tcm = table.getColumnModel();
		tcm.getColumns();
//        for (int i = 0, n = tcm.getColumnCount(); i < n; i++) {
//            TableColumn tc = tcm.getColumn(i);   
////            tc.setCellRenderer(new MyCellRendener());
//        }
        
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(440,400));
		add(scrollPane, BorderLayout.CENTER);  
        this.setVisible(true);   
    }  
  
    private class MyTableModel extends AbstractTableModel {    
        String[] columnNames =  {"#","isConvergent"};
        Object[][] data;
         
        public MyTableModel() { 
        	data = new Object[isConvergent.length+1][columnNames.length];  

        	//data first line
        	for(int i=0;i<isConvergent.length;i++){
        		data[i][0] = i;
        		if( isConvergent[i]==0 )
        			data[i][1] = "Convergent";
        		else if( isConvergent[i]==1 )
        			data[i][1] = "Not Convergent, but will converge to a steady state.";
        		else if( isConvergent[i]==2 )
        			data[i][1] = "Cannot converge to a fixed state.";
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

  
}  
