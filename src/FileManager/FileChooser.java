package FileManager;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
 
 
public class FileChooser extends JFileChooser{

	private String sysdefine = "~`!@#$%^&()[]{}?<>:;',/\"";

	//check file name
	private boolean validity(String name){
		int len = sysdefine.toCharArray().length;
		for(int i=0;i<len;i++){
			if(name.indexOf(sysdefine.charAt(i))!=-1){
				return true;
			}
		}
		return false;
	}

	public void approveSelection() {           
		String inputFileName = getSelectedFile().getName();
		if (validity(inputFileName)) {
			JOptionPane.showMessageDialog(getParent(), "The file name should exclude the following characters: \\ / : * ; ' ? \" < > |");
			return;
		}
		super.approveSelection();
	}
}