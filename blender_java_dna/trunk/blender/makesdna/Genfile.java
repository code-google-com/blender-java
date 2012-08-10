package blender.makesdna;

import blender.makesdna.AutoGenSDNA;
import blender.makesdna.SDNA;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Genfile {

    public static void init_structDNA(SDNA sdna, boolean do_endian_swap, String path) {
        /* in sdna->data the data, now we convert that to something understandable */
        System.out.println("Starting init structDNA ...");
        ByteBuffer data;
        String verg;
        int nr;
        ByteBuffer sp;
        ByteBuffer cp;
        byte[] dataStr = new byte[4];
        data = sdna.data;

        if (do_endian_swap) {
            data.order(ByteOrder.LITTLE_ENDIAN);
        }

        data.get(dataStr, 0, 4);
        verg = new String(dataStr);

        if (verg.equals("SDNA")) {
            data.get(dataStr, 0, 4);
            verg = new String(dataStr);
            if (verg.equals("NAME")) {
                sdna.nr_names = data.getInt();
                sdna.names = new String[sdna.nr_names];
            } else {
                System.out.printf("NAME error in SDNA file\n");
                return;
            }

            nr = 0;
            cp = data;

            StringBuilder sb = new StringBuilder();
            while (nr < sdna.nr_names) {
                sb.setLength(0);
                byte b = cp.get();
                while (b != 0) {
                    sb.append((char) b);
                    b = cp.get();
                }
                sdna.names[nr] = sb.toString();
                nr++;
            }
            while (cp.position() % 4 != 0) {
                cp.get();
            }

            data = cp;
            data.get(dataStr, 0, 4);
            verg = new String(dataStr);
            if (verg.equals("TYPE")) {
                sdna.nr_types = data.getInt();
                sdna.types = new String[sdna.nr_types];
            } else {
                System.out.printf("TYPE error in SDNA file\n");
                return;
            }

            nr = 0;
            cp = data;

            sb = new StringBuilder();
            while (nr < sdna.nr_types) {
                sb.setLength(0);

                byte b = cp.get();
                while (b != 0) {
                    sb.append((char) b);
                    b = cp.get();
                }
                sdna.types[nr] = sb.toString();
                nr++;
            }
            while (cp.position() % 4 != 0) {
                cp.get();
            }

            /* load typelen array */
            data = cp;
            data.get(dataStr, 0, 4);
            verg = new String(dataStr);
            if (verg.equals("TLEN")) {
                sp = data;
                sdna.typelens = new short[sdna.nr_types];
                int a = 0;
                while (a < sdna.nr_types) {
                    sdna.typelens[a] = sp.getShort();
                    a++;
                }
            } else {
                System.out.printf("TLEN error in SDNA file\n");
                return;
            }
            if ((sdna.nr_types & 1) != 0) {
                sp.getShort();
            }

            /* load struct array */
            data = sp;
            data.get(dataStr, 0, 4);
            verg = new String(dataStr);
            if (verg.equals("STRC")) {
                sdna.nr_structs = data.getInt();
                sdna.structs = new short[sdna.nr_structs][0];
            } else {
                System.out.printf("STRC error in SDNA file\n");
                return;
            }

            nr = 0;
            sp = data;
            while (nr < sdna.nr_structs) {
                short typeNumber = sp.getShort();
                short numFields = sp.getShort();
                short[] struct = new short[2 * numFields + 2];
                struct[0] = typeNumber;
                struct[1] = numFields;
                int a = 0;
                int sp_i = 2;
                while (a < numFields) {
                    struct[sp_i] = sp.getShort(); // field type index
                    struct[sp_i + 1] = sp.getShort(); // field name index
                    a++;
                    sp_i += 2;
                }
                sdna.structs[nr] = struct;
                nr++;
            }

            sdna.pointerlen = 4;

        }
        AutoGenSDNA.generate(sdna, path);
    }

    public static SDNA dna_sdna_from_data(ByteBuffer data, int datalen, boolean do_endian_swap, String path) {
        SDNA sdna = new SDNA();

        sdna.lastfind = 0;

        sdna.datalen = datalen;
        sdna.data = ByteBuffer.allocate(datalen);
        System.arraycopy(data.array(), 0, sdna.data.array(), 0, datalen);

        init_structDNA(sdna, do_endian_swap, path);

        return sdna;
    }
}
