package blender.makesdna;

import blender.readblenfile.Readfile.BHeadN;
import java.nio.ByteBuffer;

/**
 *
 * @author jladere
 */
public class BHead {

    public static String getCodeString(ByteBuffer buffer) {
        byte[] b = new byte[4];
        b[0] = buffer.get(0);
        b[1] = buffer.get(1);
        b[2] = buffer.get(2);
        b[3] = buffer.get(3);
        return new String(b);
    }

    public static String getCodeString(int code) {
        byte[] b = new byte[4];
        b[0] = (byte)(code>>24);
        b[1] = (byte)(code>>16);
        b[2] = (byte)(code>>8);
        b[3] = (byte)(code>>0);
        return new String(b);
    }

    public void init(ByteBuffer buffer, int pointersize) {
    	switch (pointersize) {
    	case 4:
	        code = ((buffer.get(0)&0xFF)<<24) | ((buffer.get(1)&0xFF)<<16) | ((buffer.get(2)&0xFF)<<8) | ((buffer.get(3)&0xFF)<<0);
	        len = buffer.getInt(4);
	        old = buffer.getInt(8)&0xFFFFFFFFFFFFFFFFL;
	        SDNAnr = buffer.getInt(12);
	        nr = buffer.getInt(16);
	        break;
    	case 8:
    		code = ((buffer.get(0)&0xFF)<<24) | ((buffer.get(1)&0xFF)<<16) | ((buffer.get(2)&0xFF)<<8) | ((buffer.get(3)&0xFF)<<0);
	        len = buffer.getInt(4);
	        old = buffer.getLong(8);
	        SDNAnr = buffer.getInt(16);
	        nr = buffer.getInt(20);
    		break;
    	}
    }

	public int code, len;
	public long old;
	public int SDNAnr, nr;

    public BHeadN bheadN;
    
}
