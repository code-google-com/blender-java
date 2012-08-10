package blender.makesdna;

import java.nio.ByteBuffer;

/**
 *
 * @author jladere
 */
public class SDNA {

    public ByteBuffer data;
	public int datalen, nr_names;
    public String[] names;
	public int nr_types, pointerlen;
    public String[] types;
    public short[] typelens;
	public int nr_structs;
    public short[][] structs;
	public int lastfind;

}
