package WidgetsConsol;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.util.Scanner;
import javax.swing.JLabel;


public class LabelLogAppender extends LogAppender {

    private JLabel label;

    public LabelLogAppender(JLabel label) throws IOException {
        super("label");
        this.label = label;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(reader);

        while (scanner.hasNextLine()) {
        	try {
        		Thread.sleep(100);
        		String line = scanner.nextLine();
        		label.setText(line);
        		line = null;
        	} catch (Exception ex) {
        		MsgManager.Messages.errorMessage(ex, "Error", "");
        	}
        }
    }
}
