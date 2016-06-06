package WidgetsTables;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import ch.epfl.lis.gnw.Gene;
import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.gnwgui.DynamicalModelElement;
import ch.epfl.lis.gnwgui.NetworkElement;



public class ReactionTable extends Component{
	private static final long serialVersionUID = 1L;
	
	private JPanel tablePanel = new JPanel();
	private String[] columnName = null; 
	private RxnTable table; 
	private NetworkElement element;
	private static JFrame c;
	
	public ReactionTable(JPanel tablePanel, String[] columnName, NetworkElement element, JFrame parentFrame){
		this.tablePanel = tablePanel;
		this.columnName = columnName;
		this.element = element;
		this.c = parentFrame;
		table = new RxnTable();
		
		GeneNetwork grn = ((DynamicalModelElement)element).getGeneNetwork();
		createDataTable(grn);			
		
	}
	
	
	//support ------------------------------------------------------------------------------------
	public static TableColumnModel getColumn(JTable table, int[] width) {
		TableColumnModel columns = table.getColumnModel();  
		for (int i = 0; i < width.length; i++) {  
			TableColumn column = columns.getColumn(i);  
			column.setMinWidth(width[i]);
		}  
		return columns;  
	}  
	
	
	private void createDataTable(GeneNetwork grn) {
		int numRxns = grn.getSize();
		
		DefaultTableModel modelTable = new DefaultTableModel(numRxns, columnName.length){     
			public boolean isCellEditable(int row,int column){  
				if(column == 0)
					return false;
				if(column == 1)
					return false;
				if(column == 2)
					return false;
				if(column == 3)
					return false;
//				if(column == 4)
//					return true;
				return true;
			}
		};  
		modelTable.setColumnIdentifiers(columnName);
		table.setModel(modelTable);		

		//set content 
		List<RxnButtonRender> editors = new ArrayList<RxnButtonRender>(numRxns);
		
		//set content 
		for(int i=0;i<numRxns;i++){
			Gene targetGene = (Gene) grn.getNode(i);
			
			String inputs = "";
			ArrayList<Gene> inputGenes = ((Gene) targetGene).getInputGenes();
			if( inputGenes != null && inputGenes.size()>0 ){
				for(int j=0;j<inputGenes.size()-1;j++)
					inputs += inputGenes.get(j).getLabel() + ",";
				inputs += inputGenes.get(inputGenes.size()-1).getLabel();
			}
			
			String reactantString = "";
			ArrayList<Gene> reactants = ((Gene) targetGene).getReactants_();
			if( reactants != null && reactants.size()>0 ){
				for(int j=0;j<reactants.size()-1;j++)
					reactantString += reactants.get(j).getLabel() + ",";
				reactantString += reactants.get(reactants.size()-1).getLabel();
			}

//			//third column
//			String inputsActive = "";
//			ArrayList<Gene> inputActivatorGenes = ((Gene) targetGene).getActivators();
//			if( inputActivatorGenes != null && inputActivatorGenes.size()>0 ){
//				for(int j=0;j<inputActivatorGenes.size()-1;j++)
//					inputsActive += inputActivatorGenes.get(j).getLabel() + ",";
//				inputsActive += inputActivatorGenes.get(inputActivatorGenes.size()-1).getLabel();
//			}
//			
//			//fourth column
//			String inputsInhibit = "";
//			ArrayList<Gene> inputInhibitorGenes = ((Gene) targetGene).getInhibitors();
//			if( inputInhibitorGenes != null && inputInhibitorGenes.size()>0 ){
//				for(int j=0;j<inputInhibitorGenes.size()-1;j++)
//					inputsInhibit += inputInhibitorGenes.get(j).getLabel() + ",";
//				inputsInhibit += inputInhibitorGenes.get(inputInhibitorGenes.size()-1).getLabel();
//			}
			
			//fifth column
//			String combination = ((Gene) targetGene).getCombination();
//			String disEquation = ((Gene) targetGene).getDisEquation();

			editors.add(new RxnButtonRender(table, targetGene.getLabel(), targetGene.getRnID(), element, targetGene, c));

//			//content
//			String[] tmp = {"R"+(i+1), targetGene.getLabel(), inputs, fourthContent};
			
			//content
			modelTable.setValueAt(targetGene.getRnID(), i, 0); //1st
			modelTable.setValueAt(targetGene.getLabel(), i, 1); //2nd
			modelTable.setValueAt(reactantString, i, 2); //2nd
			modelTable.setValueAt(inputs, i, 3); //3rd
//			modelTable.setValueAt(inputsInhibit, i, 3); //4rd
//			modelTable.setValueAt(fourthContent, i, 3); //5th

		}//end of for
		
				
		table.setEditors(editors);
		table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); 
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS); 

		//set column width
		int[] width = {20, 20, 30, 30, 30};
		table.setColumnModel(getColumn(table, width));
		table.setRowHeight(20);	

		tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS)); 
		tablePanel.add(Box.createVerticalStrut(10)); 
		tablePanel.add(scrollPane);

	}
}
