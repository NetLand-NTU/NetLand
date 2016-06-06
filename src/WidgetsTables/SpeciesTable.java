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

import cern.colt.matrix.DoubleMatrix1D;
import ch.epfl.lis.gnw.Gene;
import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.networks.Node;



public class SpeciesTable extends Component{
	private static final long serialVersionUID = 1L;

	private JPanel tablePanel = new JPanel();
	private String[] columnName = null; 
	private GeneNetwork grn_;


	public SpeciesTable(JPanel tablePanel, String[] columnName, GeneNetwork grn, boolean editable){
		this.tablePanel = tablePanel;
		this.columnName = columnName;
		this.grn_ = grn;

		DoubleMatrix1D initialValues = grn.getSpecies_initialState();

//		Float temp = Float.parseFloat(Double.toString(initialValues.get(0)));
//		if( temp.isNaN() ){
//			initialValues = grn.getInitialState();
//		}
		ArrayList<Gene> species = grn.getSpecies();

		//		System.out.print(species.size());

		ArrayList<String[]> data = new ArrayList<String[]>();		
		for(int k=0;k<species.size();k++){
			String[] tmp = {species.get(k).getLabel(), Double.toString(initialValues.get(k))};
			data.add(k, tmp);
		}


		String[][] rowData = new String[data.size()][2];
		//convert data		
		for(int i=0;i<data.size();i++){
			rowData[i][0] = data.get(i)[0];
			rowData[i][1] = data.get(i)[1];
		}
//		rowData[data.size()][0] = "Cell Volume";
//		rowData[data.size()][1] = "1000.0";
		createDataTable(rowData, editable);	

	}



	@SuppressWarnings("serial")
	private void createDataTable(String[][] rowData, final boolean editable) { 
		final DefaultTableModel tableModel = new DefaultTableModel( rowData, columnName){     
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

		final JTable table = new JTable(tableModel); 
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); 

		tableModel.addTableModelListener(new TableModelListener(){    
			public void tableChanged(TableModelEvent e) {  
				int row = e.getFirstRow();  
				int column = e.getColumn();  

				TableModel model = (TableModel)e.getSource();  
				String targetName = (String) model.getValueAt(row,0);  
				String newdata = (String) model.getValueAt(row, column);  

				int index = -1;
				ArrayList<Gene> species = grn_.getSpecies();
				for(int i=0;i<species.size();i++){
					if(species.get(i).getLabel().equals(targetName))
						index = i;
				}

				DoubleMatrix1D old_initial = grn_.getSpecies_initialState();
				old_initial.set(index, Double.parseDouble(newdata));
				grn_.setSpecies_initialState(old_initial);

				
				DoubleMatrix1D old_initial1 = grn_.getInitialState();
				ArrayList<Node> nodes = grn_.getNodes();
				for(int i=0;i<nodes.size();i++)
					for(int j=0;j<species.size();j++)
						if(species.get(j).getLabel().equals(nodes.get(i).getLabel()))
							old_initial1.set(i, old_initial.get(j));
				
				
				grn_.setInitialState(old_initial1);		


				System.out.print("Update the inital value of "+targetName+"\n");

			}
		});


		tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS)); 
		tablePanel.add(Box.createVerticalStrut(10)); 
		tablePanel.add(scrollPane); 
		tablePanel.add(Box.createVerticalStrut(10)); 
	}
}
