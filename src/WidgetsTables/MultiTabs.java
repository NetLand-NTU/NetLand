package WidgetsTables;

import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;



public class MultiTabs extends JTabbedPane {
	private static final long serialVersionUID = 1L;
	
	JPanel tabPanel = new JPanel();	
	JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT , JTabbedPane.SCROLL_TAB_LAYOUT);
	
	public MultiTabs(ArrayList<String> multipleTabs, final ArrayList<Component> c) {	    	
		if(multipleTabs.size() == 0){
			System.err.print("Input Keys and Values for Tabs");
			System.exit(1);
		}
		
		
		for (int i=0;i<multipleTabs.size();i++)
		{
			String tabName = multipleTabs.get(i);
			tabbedPane.addTab(tabName, null, c.get(i) , null);
		}
		
		
		tabbedPane.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent event)
			{		
				if (tabbedPane.getSelectedComponent() == null)
				{
					int n = tabbedPane.getSelectedIndex();
					tabbedPane.setComponentAt(n, c.get(n));
				}
			}
		});
		

		//tabbedPane.setPreferredSize(new Dimension(300 , 600));
		//tabbedPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		tabbedPane.setTabPlacement(JTabbedPane.TOP);
		tabbedPane.setAutoscrolls(true); 
		
		tabPanel.add(tabbedPane);
	}

	
	public void loadContent(int n, Component c){
		tabbedPane.setComponentAt(n, c);
	}
	
	public JTabbedPane getTabbedPane(){ return this.tabbedPane; }
	public JPanel getTabPanel(){ return this.tabPanel; }
}
