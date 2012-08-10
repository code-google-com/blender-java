package blender.readblenfile;

import blender.makesdna.BlendFileData;
import blender.readblenfile.Readfile;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public class BLO_readblenfile {

    public static BlendFileData blo_read_runtime(String path, int[] error_r, String outpath) {
        BlendFileData bfd = null;
        ByteBuffer fd;

        try {
            RandomAccessFile file = new RandomAccessFile(new File(path), "r");
            byte[] fileData = new byte[(int) file.length()];
            file.read(fileData);
            file.close();
            fd = ByteBuffer.wrap(fileData);
            bfd = Readfile.blo_read_blendafterruntime(fd, path, 0, error_r, outpath);
            file.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            error_r[0] = BlendFileData.BRE_UNABLE_TO_OPEN;
            return null;
        }

        return bfd;
    }
}

