package MsgManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;



public class Messages {
	public static void errorMessage(Exception e1, String title, String specificInfo){
		JDialog a = new JDialog();
		a.setTitle(title);
		a.setSize(new Dimension(600,350));
		a.setModal(true);
		
		
		JTextArea text = new JTextArea();
		//text.setPreferredSize(new Dimension(580,530));
		StringWriter sw = new StringWriter();  
        PrintWriter pw = new PrintWriter(sw);  
        e1.printStackTrace(pw);      
		text.setText(specificInfo+sw.toString());
		
		JScrollPane scroll = new JScrollPane(text); 
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
		
		a.add(scroll);
		a.setLocationRelativeTo(null);
		a.setVisible(true);
	}
	
	public static void progressMessage(String title){
		JDialog a = new JDialog();
		a.setTitle(title);
		a.setSize(new Dimension(600,350));
		a.setModal(true);
		
		JPanel kernelPanel = new JPanel();

		JTextArea jTextArea1 = new JTextArea();
		JScrollPane scroll = new JScrollPane(jTextArea1); 
		jTextArea1.setEditable(false);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
		
		//set border
		kernelPanel.setBackground(Color.white);
		kernelPanel.setPreferredSize(new Dimension(600,400));
		
		kernelPanel.setLayout(new BorderLayout());
		kernelPanel.add(scroll,BorderLayout.CENTER);
		redirectSystemStreams(jTextArea1);
		
		a.add(kernelPanel);
		a.setLocationRelativeTo(null);
		a.setVisible(true);
	}
	
	//Followings are The Methods that do the Redirect, you can simply Ignore them. 
	private static void redirectSystemStreams( final JTextArea jTextArea1) {
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				updateTextArea(String.valueOf((char) b), jTextArea1);
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				updateTextArea(new String(b, off, len), jTextArea1);
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};

		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));
	}
	
	//The following codes set where the text get redirected. In this case, jTextArea1    
	private static void updateTextArea(final String text, final JTextArea jTextArea1) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jTextArea1.append(text);
			}
		});
	}
}
