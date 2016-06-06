package NetworkDisplay;

import java.awt.Graphics;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class BackgroundPanel extends JPanel {
	public BackgroundPanel() {

	}

	public void paintComponent(Graphics g) {
		int x = 0, y = 0;
		ImageIcon icon = new ImageIcon(getClass().getResource("rsc/interaction-labels.png"));
		g.drawImage(icon.getImage(), x, y, this);
	}

}
