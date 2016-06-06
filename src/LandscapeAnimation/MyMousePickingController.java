package LandscapeAnimation;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;




import org.jzy3d.chart.Chart;
import org.jzy3d.chart.controllers.camera.AbstractCameraController;
import org.jzy3d.chart.controllers.thread.camera.CameraThreadController;
import org.jzy3d.maths.Coord2d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.IntegerCoord2d;
import org.jzy3d.picking.PickingSupport;
import org.jzy3d.plot3d.rendering.scene.Graph;
import org.jzy3d.plot3d.rendering.view.View;


public class MyMousePickingController<V, E> extends AbstractCameraController implements MouseListener, MouseMotionListener, MouseWheelListener {
	public MyMousePickingController() {
		super();
		picking = new PickingSupport();
	}


	public MyMousePickingController(Chart chart) {
		super(chart);
		picking = new PickingSupport();
	}


	public MyMousePickingController(Chart chart, int brushSize) {
		super(chart);
		picking = new PickingSupport(brushSize);
	}


	public MyMousePickingController(Chart chart, int brushSize, int bufferSize) {
		super(chart);
		picking = new PickingSupport(brushSize, bufferSize);
	}


	public void register(Chart chart) {
		super.register(chart);
		this.chart = chart;
		this.prevMouse = Coord2d.ORIGIN;
		chart.getCanvas().addMouseController(this);
	}


	public void dispose() {
		for (Chart c : targets) {
			c.getCanvas().removeMouseController(this);
		}


		if (threadController != null)
			threadController.stop();


		super.dispose(); // i.e. target=null
	}


	/****************/


	public PickingSupport getPickingSupport() {
		return picking;
	}


	public void setPickingSupport(PickingSupport picking) {
		this.picking = picking;
	}


	/****************/


	public void mouseClicked(MouseEvent e) {
		//    System.out.println("X:"+e.getX()+" Y:"+e.getY());
		//    int yflip = -e.getY() + targets.get(0).getCanvas().getRendererHeight();
		//    ChartView view =(ChartView)targets.get(0).getView();
		//    Coord3d thisMouse3d = view.projectMouse(e.getX(), yflip);
		//    System.out.println("X:"+thisMouse3d.x+" Y:"+thisMouse3d.y+" Z:"+thisMouse3d.z);
		//
		//
		//    Coord3d eye = view.getCamera().getEye();
		//    System.out.println("eye: X:"+eye.x+" Y:"+eye.y+" Z:"+eye.z);
		//    Coord3d viewPoint = view.getViewPoint();
		//    System.out.println("ViewPoint: X:"+viewPoint.x+" Y:"+viewPoint.y+" Z:"+viewPoint.z);
		//    Coord3d scaling = view.getLastViewScaling();
		//    System.out.println("Scaling: X:"+scaling.x+" Y:"+scaling.y+" Z:"+scaling.z);
	}


	public void mouseEntered(MouseEvent e) {
	}


	public void mouseExited(MouseEvent e) {
	}


	public void mouseReleased(MouseEvent e) {
	}



	/** Compute zoom */
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (threadController != null)
			threadController.stop();
		//System.out.println(e.getWheelRotation());
		float factor = 1 + (e.getWheelRotation() / 10.0f);
		//System.out.println(MyMousePickingController.class.getSimpleName() + "wheel:" + factor * 100);
		zoomX(factor);
		zoomY(factor);
		chart.getView().shoot();
	}


	public void mouseMoved(MouseEvent e) {
		//System.out.println("moved");
		//pick(e);
	}


	public void mousePressed(MouseEvent e) {
		if (handleSlaveThread(e))
			return;
		pick(e);
	}

	public boolean handleSlaveThread(MouseEvent e) {

		if (threadController != null) {
			threadController.start();
			return true;
		}

		if (threadController != null)
			threadController.stop();
		return false;
	}

	public void pick(MouseEvent e) {
		int yflip = -e.getY() + targets.get(0).getCanvas().getRendererHeight();
		prevMouse.x = e.getX();
		prevMouse.y = e.getY();// yflip;
		View view = chart.getView();
		prevMouse3d = view.projectMouse(e.getX(), yflip);
		GL gl = chart().getView().getCurrentGL();
		Graph graph = chart().getScene().getGraph();
		// will trigger vertex selection event to those subscribing to
		// PickingSupport.
		picking.pickObjects(gl, glu, view, graph, new IntegerCoord2d(e.getX(), yflip));

		chart().getView().getCurrentContext().release();
	}




	/**********************/


	protected float factor = 1;
	protected float lastInc;
	protected Coord3d mouse3d;
	protected Coord3d prevMouse3d;
	protected PickingSupport picking;
	protected GLU glu = new GLU();


	protected Chart chart;


	protected Coord2d prevMouse;
	protected CameraThreadController threadController;
	
	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}


}