package WidgetsConsol;


import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class LogPanel extends JPanel {
    private JScrollPane logScrollPane;
    private JTextArea logTextArea;

    public LogPanel() {
        logScrollPane = new javax.swing.JScrollPane();
        logTextArea = new javax.swing.JTextArea();
        logTextArea.setEditable(false);
        logScrollPane.setViewportView(logTextArea);

        this.setLayout(new BorderLayout());
		this.add(logScrollPane,BorderLayout.CENTER);
        this.setPreferredSize(new Dimension(760,80));
        logTextArea.setPreferredSize(this.getPreferredSize());
        logScrollPane.setPreferredSize(this.getPreferredSize());
        logTextArea.setBorder(BorderFactory.createEmptyBorder()); 
        logScrollPane.setBorder(BorderFactory.createEmptyBorder()); 
    }

    public void initLog() {
        try {
            Thread t2; 
            t2 = new TextAreaLogAppender(logTextArea, logScrollPane);
            t2.start();
        } catch (Exception e) {
            MsgManager.Messages.errorMessage(e, "Error", "");
        }
    }

//    public static void main(String[] s) {
//    	JFrame jf = new JFrame();
//    	LogPanel logDemoFrame = new LogPanel();
//        logDemoFrame.initLog();
//        
//        jf.getContentPane().add(logDemoFrame);
//        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		jf.pack();
//		jf.setSize(1000, 600);
//		jf.setVisible(true);
//		
//        for (int i = 0; i < 1000; i++) {
//            log.info("测试日志输出:" + i);
//        }
//    }
}
