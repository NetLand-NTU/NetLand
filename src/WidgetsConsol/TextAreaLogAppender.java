package WidgetsConsol;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.util.Scanner;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class TextAreaLogAppender extends LogAppender {

    private JTextArea textArea;
    private JScrollPane scroll;

    public TextAreaLogAppender(JTextArea textArea, JScrollPane scroll) throws IOException {
        super("textArea");
        this.textArea = textArea;
        this.scroll = scroll;
    }

    @Override
    public void run() {
        // 不间断地扫描输入流
        Scanner scanner = new Scanner(reader);
        // 将扫描到的字符流输出到指定的JTextArea组件
        while (scanner.hasNextLine()) {
        	try {
        		//睡眠
        		Thread.sleep(100);
        		String line = scanner.nextLine();
        		textArea.append(line);
        		textArea.append("\n");
        		line = null;
        		//使垂直滚动条自动向下滚动
        		scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
        	} catch (Exception ex) {
        		MsgManager.Messages.errorMessage(ex, "Error", "");
        	}
        }
        System.out.print(textArea.getText()+"aaa\n");
    }
}
