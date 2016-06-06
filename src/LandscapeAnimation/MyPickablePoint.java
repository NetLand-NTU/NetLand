package LandscapeAnimation;

import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.pickable.PickablePoint;

public class MyPickablePoint extends PickablePoint {

	int rowNo;
	int colNo;
	
	public MyPickablePoint(int rowNo, int colNo, Coord3d xyz, Color rgb, float width)
	{
		super(xyz, rgb,width);
		this.rowNo = rowNo+1;
		this.colNo = colNo+1;
	}


	public int getRowNo() {
		return rowNo;
	}

	public int getColNo() {
		return colNo;
	}

	public float getX() {
		return this.xyz.x;
	}

	public float getY() {
		return this.xyz.y;
	}

	public float getZ() {
		return this.xyz.z;
	}
	
	public String toString()
	{
		return "Row: " + this.getRowNo() + " Col: " + this.getColNo() + " x:" + this.getX() + " y:" + this.getY() + " z:" + this.getZ();
	}
}
