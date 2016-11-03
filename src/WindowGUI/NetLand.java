package WindowGUI;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

//import org.apache.log4j.Logger;
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
	
	//static Logger log_ = Logger.getLogger(NetLand.class.getName());
	
	public void init()
	{	
		jf = new JFrame("NetLand - software for quantitative modeling and visualization of Waddington's epigenetic landscape"); 
		
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
		
		
//		runningAssessment();
	}

	

//	private static void runningAssessment(){
////		try {
////			String path = "E:\\" + "SDE_log.txt";
////			try {
////				new File(path).createNewFile();
////			} catch (IOException e) {
////				e.printStackTrace();
////			};
////			PrintStream ps = new PrintStream(path);
////			System.setOut(ps);
////			System.setErr(ps);
////		} catch (FileNotFoundException e){
////			e.printStackTrace();
////		}
//		
//		int[] testsize = {2,5,10,20,30,40,50,60,70,80,90,100,150,200}; //
//		//load file
//		int format = ImodNetwork.TSV;
//		
//		for(int i=0;i<testsize.length;i++){
//			int currentSize=testsize[i];
//			System.out.print("Current: "+currentSize+"\n");
//			URL url = NetLand.class.getResource("networks/testNet"+currentSize+".tsv");
//			
//			for(int j=0;j<1;j++){ //run 10 times				
//				NetworkElement element = null;
//				try {
//					element = IONetwork.loadItem("testNet"+currentSize+".tsv", url, format);
//					element.setNetworkViewer(new NetworkGraph(element));
//					
//					element = WidgetsButtons.TopButton.convert2dynamicModel(element);
//					element.setOrigFile(url, "testNet"+currentSize+".tsv", format);
//				} catch (Exception e) {
//					MsgManager.Messages.errorMessage(e, "Error", "");
//					System.exit(1);
//				}
//				
//				element.setName(j+"");
//				//System.out.print("Time begin: "+j+"\t"+System.currentTimeMillis()+"\n"); 
//				//run simulation
//				Simulation rd = new Simulation(new JFrame(), element);
//				//set parameters
//				rd.enterAction(element);	
//				
//			}
//		}	
//	}
}

