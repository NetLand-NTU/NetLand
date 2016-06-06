package WidgetsTables;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.gnwgui.DynamicalModelElement;
import ch.epfl.lis.gnwgui.NetworkElement;
import ch.epfl.lis.gnwgui.StructureElement;



public class ParameterTable extends Component{
	private static final long serialVersionUID = 1L;
	
	private JPanel tablePanel = new JPanel();
	private String[] columnName = null; 
	private NetworkElement element;
	
	
	public ParameterTable(JPanel tablePanel, String[] columnName, NetworkElement element, boolean editable){
		this.tablePanel = tablePanel;
		this.columnName = columnName;
		this.element = element;
		
		if (element instanceof StructureElement) {
//			System.out.print("Not Dynamic model!");
			String[][] rowData = new String[1][3];
			createDataTable(rowData, editable);
		} else if (element instanceof DynamicalModelElement) {
//			System.out.print("Dynamic model!");
			GeneNetwork grn = ((DynamicalModelElement)element).getGeneNetwork();
			ArrayList<String> parameterNames_ = grn.getParameterNames_();
			ArrayList<Double> parameterValues_ = grn.getParameterValues_();
			
			ArrayList<String[]> data = new ArrayList<String[]>();		
			for(int k=0;k<parameterNames_.size();k++){
				String[] tmp = {parameterNames_.get(k), Double.toString(parameterValues_.get(k))};
				data.add(k, tmp);
			}
			
//			for(int k=0;k<initialValues.size();k++){
//				String[] tmp = {"initalValue_"+grn.getNode(k).getLabel(), Double.toString(initialValues.get(k))};
//				data.add(k+parameterNames_.size(), tmp);
//			}
			
			String[][] rowData = new String[data.size()][2];
			//convert data		
			for(int i=0;i<data.size();i++){
				rowData[i][0] = data.get(i)[0];
				rowData[i][1] = data.get(i)[1];
			}
			createDataTable(rowData, editable);			
		}
		
	}
	
	
	
	private void createDataTable(String[][] rowData, final boolean editable) { 
		DefaultTableModel tableModel = new DefaultTableModel( rowData, columnName){     
			public boolean isCellEditable(int row,int column){  
				if( editable ){ //main panel species
					if(column == 0)
						return false;
					return true;
				}else{ //traj panel species
					return false;
				}
			}
		};

		tableModel.addTableModelListener(new TableModelListener(){    
			public void tableChanged(TableModelEvent e) {  
				int row = e.getFirstRow();  
				int column = e.getColumn();  
				TableModel model = (TableModel)e.getSource();  
				String targetName = (String) model.getValueAt(row,0);  
				String newdata = (String) model.getValueAt(row, column);  
				
				GeneNetwork grn = ((DynamicalModelElement)element).getGeneNetwork();
				ArrayList<String> parameterNames_ = grn.getParameterNames_();
				int index = -1;
				for(int i=0;i<parameterNames_.size();i++)
					if(parameterNames_.get(i).equals(targetName))
						index = i;
							
				ArrayList<Double> parameterValues_ = grn.getParameterValues_();
				parameterValues_.set(index, Double.parseDouble(newdata));
				grn.setParameterValues_(parameterValues_);
				
				System.out.print("Update the value of parameter "+targetName+"\n");
				
			}
		});
		
		
		
		
		JTable table = new JTable(tableModel); 
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); 

		tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS)); 
		tablePanel.add(Box.createVerticalStrut(10)); 
		tablePanel.add(scrollPane); 
		tablePanel.add(Box.createVerticalStrut(10)); 
	}
}
