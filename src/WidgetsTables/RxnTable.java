package WidgetsTables;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;



public class RxnTable extends JTable{ 
	private static final long serialVersionUID = 1L;


	public RxnTable() { 
		super(); 
		// TODO Auto-generated constructor stub 
	} 

	int myRow=-1,myCol=-1; 
	List<RxnButtonRender> editors = new ArrayList<RxnButtonRender>();


	@Override 
	public TableCellEditor getCellEditor(int row, int column)
	{
		int modelColumn = convertColumnIndexToModel( column );

		if (modelColumn == 4)
			return editors.get(row);
		else
			return super.getCellEditor(row, column);
	}


	@Override 
	public TableCellRenderer getCellRenderer(int row, int column)
	{
		int modelColumn = convertColumnIndexToModel( column );

		if (modelColumn == 4)
			return editors.get(row);
		else
			return super.getCellRenderer(row, column);
	}

	public void setEditors(List<RxnButtonRender> editors){
		this.editors.addAll(editors);
	}


} 