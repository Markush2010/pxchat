package pxchat.net.protocol.frames;

import java.io.Serializable;

/**
 * <p>This class is the base class for all data transferred over the network. It
 * implements the {@link Serializable} interface so that it can be written to
 * the output stream of the socket.</p>
 * 
 * Each subclass of <code>Frame</code> must have an <code>id</code> that is
 * unique among the set of all frames.
 * 
 * @author Robert Waury
 */
public abstract class Frame implements Serializable {

	private static final long serialVersionUID = 2128944833500982220L;

	public static final int ID_NOP = 0;
	public static final int ID_VERSION = 1;
	public static final int ID_SID = 2;
	public static final int ID_MSG = 3;
	public static final int ID_NOTIFICATION = 4;
	public static final int ID_LOCK = 5;
	public static final int ID_AUTH = 6;
	public static final int ID_USERLIST = 7;

	public static final int ID_IMG_START = 10;
	public static final int ID_IMG_ID = 11;
	public static final int ID_IMG_CHUNK = 12;
	public static final int ID_IMG_STOP = 13;
	public static final int ID_IMG_SYNC = 14;

	public static final int ID_POINT = 100;
	public static final int ID_LINE = 101;
	public static final int ID_RECT = 102;
	public static final int ID_CIRCLE = 103;
	public static final int ID_TEXT = 104;
	public static final int ID_ELLIPSE = 105;
	public static final int ID_BACKGROUND = 106;
	public static final int ID_FREEHAND = 107;
	public static final int ID_CLEAR = 108;
	public static final int ID_IMG = 109;

	/**
	 * The id of this frame
	 */
	protected int id = ID_NOP;

	/**
	 * Constructs a new <code>Frame</code>. The id is set to <code>ID_NOP</code>
	 * .
	 */
	protected Frame() {
	}

	/**
	 * @return The id of this frame
	 */
	public int getId() {
		return this.id;
	}
}