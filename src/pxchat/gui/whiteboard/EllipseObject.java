package pxchat.gui.whiteboard;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import pxchat.net.protocol.frames.Frame;

public class EllipseObject extends PrimitiveObject {

	private Point topLeft;
	private int width;
	private int height;

	private transient Shape ellipse;

	public EllipseObject(Point point1, Point point2, Color color, float width) {
		super(color, width);
		this.id = Frame.ID_ELLIPSE;
		this.topLeft = new Point(Math.min(point1.x, point2.x), Math.min(
				point1.y, point2.y));
		this.width = Math.abs(point1.x - point2.x);
		this.height = Math.abs(point1.y - point2.y);
	}

	public EllipseObject(Point topLeft, int width, int height, Color color,
							float strokeWidth) {
		super(color, strokeWidth);
		this.id = Frame.ID_ELLIPSE;
		this.topLeft = topLeft;
		this.width = width;
		this.height = height;
	}

	@Override
	public void draw(Graphics2D g) {
		// this may be moved to readObject, we will see what's better
		if (ellipse == null)
			this.ellipse = new Ellipse2D.Double(topLeft.x, topLeft.y, width,
					height);
		beginDraw(g);
		g.draw(this.ellipse);
		endDraw(g);
	}

}