package WindowGUI;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import org.apache.log4j.Logger;
import org.jzy3d.maths.Utils;


import javax.imageio.ImageIO;
import javax.swing.*;

import WidgetsMenu.MainMenu;
import ch.epfl.lis.gnwgui.IONetwork;
import ch.epfl.lis.gnwgui.NetworkElement;
import ch.epfl.lis.gnwgui.NetworkGraph;
import ch.epfl.lis.imod.ImodNetwork;




public class NetLand extends JFrame{
	private static final long serialVersionUID = 1L;
	
	/** NetworkElement **/
	static NetworkElement element = null;
	JFrame jf;
	
	static Logger log_ = Logger.getLogger(NetLand.class.getName());
	
	public void init()
	{	
		jf = new JFrame("NetLand - software for quantitative modeling and visualization of Waddington’s epigenetic landscape"); 
		
		Image image;
		try {
			image = ImageIO.read(this.getClass().getResource("rsc/logo.png"));
			jf.setIconImage(image);
		} catch (IOException e) {
			MsgManager.Messages.errorMessage(e, "Error", "");
		}
		

		
		/** part 1: menu **/
		MainMenu menuPanel = new MainMenu(element, jf);
		/** part 2: network **/
		WindowNetworkPanel NetworkPanel = new WindowNetworkPanel(element, jf);

		/** SET LAYOUT **/
		jf.setJMenuBar(menuPanel.getMenuBar());

		jf.getContentPane().setLayout(new GridBagLayout());  
		addComponent(jf, NetworkPanel.getPanel(), 0, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1, 1);
		

		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.pack();
		jf.setSize(1000, 600);
		//jf.setExtendedState(Frame.MAXIMIZED_BOTH);
		jf.setLocationRelativeTo(null);
		jf.setVisible(true);
	}


	private void parseFile(String filename, String absPath, int format){
		URL url = getClass().getResource(absPath);
		
		try {
			element = IONetwork.loadItem(filename, url, format);
			element.setNetworkViewer(new NetworkGraph(element));
			
			element = WidgetsButtons.TopButton.convert2dynamicModel(element);
			element.setOrigFile(url, filename, format);
		} catch (Exception e) {
			MsgManager.Messages.errorMessage(e, "Error", "");
			System.exit(1);
		}
	}

	private void setDefault(String absPath, String filename){
		int format = ImodNetwork.TSV;

		parseFile(filename, absPath, format);
	}
	
	public static void addComponent(Container container, Component component, int gridx, int gridy, int gridwidth, int gridheight, int anchor, int fill, float weightx, float weighty) {
		Insets insets = new Insets(0, 0, 0, 0);
		GridBagConstraints gbc = new GridBagConstraints(gridx, gridy, gridwidth, gridheight, weightx, weighty,anchor, fill, insets, 0, 0);
		container.add(component, gbc);
	}


	public static void main(String[] args)
	{  	
//		try {
//			//System.load("E:\\netland\\workspace\\NetLand\\libquaqua.jnilib");
//			System.load("E:\\netland\\workspace\\NetLand\\libquaqua64.jnilib");
//		} catch (UnsatisfiedLinkError e) {
//			System.err.println("Native code library failed to load.\n" + e);
//			System.exit(1);
//		}
//		
//		System.setProperty("Quaqua.tabLayoutPolicy","scroll");
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				try {
//					JFrame.setDefaultLookAndFeelDecorated(true);
//					JDialog.setDefaultLookAndFeelDecorated(true);
//					UIManager.setLookAndFeel(ch.randelshofer.quaqua.QuaquaManager.getLookAndFeel());
//					
//					NetLand a = new NetLand();
//					a.setDefault("rsc/2genes.tsv", "2genes.tsv");
//					a.init();
//
//					System.out.print("Welcome! Thanks for using NetLand.\n");
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				
//			}
//		});

		try{
	        org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper.launchBeautyEyeLNF();
	        UIManager.put("RootPane.setupButtonVisible", false);        
	    }catch(Exception e){
	    	MsgManager.Messages.errorMessage(e, "Error", "");
	    }
		
		NetLand a = new NetLand();
		a.setDefault("rsc/2genes.tsv", "2genes.tsv");
		a.init();
		element.getNetworkViewer().getControl().changeGraphLayout("KK layout");
		
		System.out.print(Utils.dat2str(new Date())+"\n");
		System.out.print("Welcome! Thanks for using NetLand.\n");
	}

	

}

