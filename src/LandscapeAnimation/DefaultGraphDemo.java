package LandscapeAnimation;


import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.graphs.GraphChart;
import org.jzy3d.maths.graphs.IGraph;
import org.jzy3d.maths.graphs.StringGraphGenerator;
import org.jzy3d.plot3d.primitives.graphs.impl.DefaultDrawableGraph2d;
import org.jzy3d.plot3d.primitives.graphs.layout.DefaultGraphFormatter;
import org.jzy3d.plot3d.primitives.graphs.layout.IGraphFormatter;
import org.jzy3d.plot3d.primitives.graphs.layout.IGraphLayout2d;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.view.modes.ViewPositionMode;


public class DefaultGraphDemo extends JFrame{
	public static int NODES = 100000;
	public static int EDGES = 2000;
	public static float GL_LAYOUT_SIZE = 10;
	
	public static void main(String[] args) throws Exception{
		DefaultGraphDemo demo = new DefaultGraphDemo();
		
		
	}
	
	public DefaultGraphDemo(){
		// Init graph
		IGraph<String, String> graph = StringGraphGenerator.getGraph(NODES, EDGES);
		IGraphLayout2d<String> layout = StringGraphGenerator.getRandomLayout(graph, GL_LAYOUT_SIZE);
		IGraphFormatter<String, String> formatter = new DefaultGraphFormatter<String, String>();
		formatter.setVertexLabelsDisplayed(false);
		
		// Create chart
		Quality quality = Quality.Advanced;
		quality.setDepthActivated(false);
		chart = new GraphChart(quality);
		chart.getView().setAxeBoxDisplayed(false);
		chart.getView().setViewPositionMode(ViewPositionMode.TOP);
		chart.getView().setSquared(false);
		
		// Build a drawable graph
		final DefaultDrawableGraph2d<String,String> dGraph = new DefaultDrawableGraph2d<String,String>();
		
		
		
		dGraph.setGraphLayout(layout);
		dGraph.setGraphFormatter(formatter);

		chart.getScene().getGraph().add( dGraph );
		
		JPanel landscapePanel = new JPanel();
		landscapePanel .add( (Component) chart.getCanvas(), java.awt.BorderLayout.CENTER);
		
		this.getContentPane().add(landscapePanel);
		this.setVisible(true);
	}
		
	public Chart getChart(){
		return chart;
	}	
	protected Chart chart;
}
