package org.math.plot.components;

import java.awt.Toolkit;

import javax.swing.*;
import org.math.plot.canvas.*;
import org.math.plot.plots.*;

/**
 * BSD License
 * 
 * @author Yann RICHET
 */
public class DataFrame extends JDialog {

    private static final long serialVersionUID = 1L;
    private PlotCanvas plotCanvas;
    private JTabbedPane panels;

    public DataFrame(PlotCanvas p) {
//        super("Data");
        plotCanvas = p;
        JPanel panel = new JPanel();
        panels = new JTabbedPane();

        panel.add(panels);
        //setContentPane(panel);
        add(panel);
        setModal(true);
        //setVisible(true);
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            setPanel();
        }
        setLocationRelativeTo(null);
        super.setVisible(b);
    }

    private void setPanel() {
        panels.removeAll();
        for (Plot plot : plotCanvas.getPlots()) {
            panels.add(plot.getDataPanel(plotCanvas), plot.getName());
        }
        pack();
    }

    public void selectIndex(int i) {
        setVisible(true);
        if (panels.getTabCount() > i) {
            panels.setSelectedIndex(i);
        }
    }
}