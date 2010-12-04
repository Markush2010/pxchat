package pxchat.net.protocol.frames;

import java.io.Serializable;

/**
 * This class is the base class for all data transferred over the network. It
 * implements the {@link Serializable} interface so that it can be written to
 * the output stream of the socket.
 * 
 * Each subclass of <code>Frame</code> must have an <code>id</code> that is
 * unique among the set of all frames.
 * 
 * @author Markus Döllinger
 */
public abstract class Frame implements Serializable {

	public static final int ID_NOP = 0;
	public static final int ID_VERSION = 1;
	public static final int ID_SID = 2;
	public static final int ID_MSG = 3;
	public static final int ID_AUTO = 4;
	public static final int ID_LOCK = 5;
	public static final int ID_AUTH = 6;

	public static final int ID_IMG = 10;

	public static final int ID_POINT = 100;
	public static final int ID_LINE = 101;
	public static final int ID_RECT = 102;
	public static final int ID_CIRCLE = 103;
	public static final int ID_IMGTEXT = 104;

	/**
	 * The id of this frame
	 */
	protected int id = ID_NOP;

	/**
	 * Constructs a new <code>Frame</code>. The id is set to <i>ID_NOP</i>.
	 */
	public Frame() {
	}

	/**
	 * @return The id of this frame
	 */
	public int getId() {
		return this.id;
	}
}