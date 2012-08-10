package blender.readblenfile;

import blender.makesdna.BlendFileData;
import blender.makesdna.Genfile;
import blender.makesdna.ListBase;
import blender.makesdna.BHead;
import blender.makesdna.SDNA;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Readfile {

    public static class FileData {

        public ListBase listbase = new ListBase();
        public int flags;
        public int eof;
        public int buffersize;
        public int seek;
        public FDRead reader;
        public ByteBuffer filedes;
        public SDNA filesdna;
        public int fileversion;
    };

    public static class BHeadN {

        BHeadN next, prev;
        public BHead bhead;
        public ByteBuffer data;

        public BHeadN(int size, BHead bh) {
            bhead = bh;
            bhead.bheadN = this;
            data = ByteBuffer.allocate(size);
        }
    };

    public static final int DATA = ('D' << 24) | ('A' << 16) | ('T' << 8) | ('A');
    public static final int GLOB = ('G' << 24) | ('L' << 16) | ('O' << 8) | ('B');
    //#define IMAG MAKE_ID('I','M','A','G')
    public static final int DNA1 = ('D' << 24) | ('N' << 16) | ('A' << 8) | ('1');
    public static final int TEST = ('T' << 24) | ('E' << 16) | ('S' << 8) | ('T');
    public static final int REND = ('R' << 24) | ('E' << 16) | ('N' << 8) | ('D');
    public static final int USER = ('U' << 24) | ('S' << 16) | ('E' << 8) | ('R');
    public static final int ENDB = ('E' << 24) | ('N' << 16) | ('D' << 8) | ('B');

    public static final int EOF = -1;
    public static final int FD_FLAGS_SWITCH_ENDIAN = (1 << 0);
    public static final int FD_FLAGS_FILE_POINTSIZE_IS_4 = (1 << 1);
    public static final int FD_FLAGS_POINTSIZE_DIFFERS = (1 << 2);
    public static final int FD_FLAGS_FILE_OK = (1 << 3);
    public static final int FD_FLAGS_NOT_MY_BUFFER = (1 << 4);
    public static final int FD_FLAGS_NOT_MY_LIBMAP = (1 << 5);
    public static final int SIZEOFBLENDERHEADER = 12;

    public static BHeadN get_bhead(FileData fd) {
        ByteBuffer bhead4 = ByteBuffer.allocate(20);
        ByteBuffer bhead8 = ByteBuffer.allocate(24);
        BHead bhead = new BHead();
        BHeadN new_bhead = null;
        int readsize = -1;

        if (fd != null) {
            if (fd.eof == 0) {

                // First read the bhead structure.
                // Depending on the platform the file was written on this can
                // be a big or little endian BHead4 or BHead8 structure.

                // As usual 'ENDB' (the last *partial* bhead of the file)
                // needs some special handling. We don't want to EOF just yet.

                if ((fd.flags & FD_FLAGS_FILE_POINTSIZE_IS_4) != 0) {
                    bhead4.put(0, (byte) 'D');
                    bhead4.put(1, (byte) 'A');
                    bhead4.put(2, (byte) 'T');
                    bhead4.put(3, (byte) 'A');
                    readsize = fd.reader.read(fd, bhead4.array(), bhead4.capacity());

                    if (readsize == bhead4.capacity() || bhead4.getInt(0) == ENDB) {
                        if ((fd.flags & FD_FLAGS_SWITCH_ENDIAN) != 0) {
                            bhead4.order(ByteOrder.LITTLE_ENDIAN);
                        }

                        if ((fd.flags & FD_FLAGS_POINTSIZE_DIFFERS) != 0) {
                            System.out.println("FD_FLAGS_POINTSIZE_DIFFERS");
                        } else {
                            bhead.init(bhead4, 4);
                        }
                    } else {
                        fd.eof = 1;
                        bhead.len = 0;
                    }
                } else {
                    bhead8.put(0, (byte) 'D');
                    bhead8.put(1, (byte) 'A');
                    bhead8.put(2, (byte) 'T');
                    bhead8.put(3, (byte) 'A');
    				readsize = fd.reader.read(fd, bhead8.array(), bhead8.capacity());

    				if (readsize == bhead8.capacity() || bhead8.getInt(0) == ENDB) {
    					if ((fd.flags & FD_FLAGS_SWITCH_ENDIAN)!=0) {
    						bhead8.order(ByteOrder.LITTLE_ENDIAN);
    					}

    					if ((fd.flags & FD_FLAGS_POINTSIZE_DIFFERS)!=0) {
//    						bh4_from_bh8(&bhead, &bhead8, (fd->flags & FD_FLAGS_SWITCH_ENDIAN));
    						System.out.println("FD_FLAGS_POINTSIZE_DIFFERS");
    					} else {
    						bhead.init(bhead8, 8);
    					}
    				} else {
    					fd.eof = 1;
    					bhead.len= 0;
    				}
                }

                /* make sure people are not trying to pass bad blend files */
                if (bhead.len < 0) {
                    fd.eof = 1;
                }

                // bhead now contains the (converted) bhead structure. Now read
                // the associated data and put everything in a BHeadN (creative naming !)

                if (fd.eof == 0) {
                    new_bhead = new BHeadN(bhead.len, bhead);
                    new_bhead.next = new_bhead.prev = null;

                    readsize = fd.reader.read(fd, new_bhead.data.array(), bhead.len);

                    if (readsize != bhead.len) {
                        fd.eof = 1;
                        new_bhead = null;
                    }
                }
            }
        }

        // We've read a new block. Now add it to the list
        // of blocks.

        if (new_bhead != null) {
            BLI_addtail(fd.listbase, new_bhead);
        }

        return new_bhead;
    }

    public static void BLI_addtail(ListBase listbase, BHeadN vlink) {
        BHeadN link = vlink;

        if (link == null) {
            return;
        }
        if (listbase == null) {
            return;
        }

        link.next = null;
        link.prev = (BHeadN) listbase.last;

        if (listbase.last != null) {
            ((BHeadN) listbase.last).next = link;
        }
        if (listbase.first == null) {
            listbase.first = link;
        }
        listbase.last = link;
    }

    public static BHead blo_firstbhead(FileData fd) {
        BHeadN new_bhead;
        BHead bhead = null;

        // Rewind the file
        // Read in a new block if necessary

        new_bhead = (BHeadN) fd.listbase.first;
        if (new_bhead == null) {
            new_bhead = get_bhead(fd);
        }

        if (new_bhead != null) {
            bhead = new_bhead.bhead;
        }

        return bhead;
    }

    public static BHead blo_nextbhead(FileData fd, BHead thisblock) {
        BHeadN new_bhead = null;
        BHead bhead = null;

        if (thisblock != null) {
            // bhead is actually a sub part of BHeadN
            // We calculate the BHeadN pointer from the BHead pointer below
            new_bhead = thisblock.bheadN;

            // get the next BHeadN. If it doesn't exist we read in the next one
            new_bhead = (BHeadN) new_bhead.next;
            if (new_bhead == null) {
                new_bhead = get_bhead(fd);
            }
        }

        if (new_bhead != null) {
            // here we do the reverse:
            // go from the BHeadN pointer to the BHead pointer
            bhead = new_bhead.bhead;
        }

        return bhead;
    }

    public static void decode_blender_header(FileData fd) {
        byte[] header = new byte[SIZEOFBLENDERHEADER];
        byte[] num = new byte[3];
        int readsize;

        System.out.println("Starting decode blender header ...");
        // read in the header data
        readsize = fd.reader.read(fd, header, header.length);

        if (readsize == header.length) {
            String headerStr = new String(header);
            System.out.println(headerStr);
            if (headerStr.startsWith("BLENDER")) {

                fd.flags |= FD_FLAGS_FILE_OK;
                System.out.println("FD_FLAGS_FILE_OK");

                // what size are pointers in the file ?
                if (headerStr.charAt(7) == '_') {
                    fd.flags |= FD_FLAGS_FILE_POINTSIZE_IS_4;
                    System.out.println("FD_FLAGS_FILE_POINTSIZE_IS_4");
                } else {
                }

                // is the file saved in a different endian
                // than we need ?
                if (headerStr.charAt(8) == 'v') {
                    System.out.println("FD_FLAGS_SWITCH_ENDIAN");
                    fd.flags |= FD_FLAGS_SWITCH_ENDIAN;
                } else {
                }

                // get the version number

                System.arraycopy(header, 9, num, 0, 3);
                String numStr = new String(num);
                fd.fileversion = Integer.parseInt(numStr);
                System.out.println("fileversion: " + fd.fileversion);
            }
        } else {
            System.out.println("ERROR READING HEADER");
        }
        System.out.println("Finished decode blender header");
    }

    public static int read_file_dna(FileData fd, String path) {
        BHead bhead;

        System.out.println("Starting read file dna ...");
        for (bhead = blo_firstbhead(fd); bhead != null; bhead = blo_nextbhead(fd, bhead)) {
            if (bhead.code == DNA1) {
                boolean do_endian_swap = ((fd.flags & FD_FLAGS_SWITCH_ENDIAN) != 0);

                fd.filesdna = Genfile.dna_sdna_from_data(bhead.bheadN.data, bhead.len, do_endian_swap, path);
                if (fd.filesdna != null) {

                }

                System.out.println("Finished read file dna.");
                return 1;
            } else if (bhead.code == ENDB) {
                break;
            }
        }
        System.out.println("ERROR READING FILE DNA.");

        return 0;
    }

    public static interface FDRead {
        public int read(FileData filedata, byte[] buffer, int size);
    }
    
    public static FDRead fd_read_from_file = new FDRead() {
        public int read(FileData filedata, byte[] buffer, int size) {
            int readsize = -1;
            try {
                filedata.filedes.get(buffer, 0, size);
                readsize = size;
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (readsize < 0) {
                readsize = EOF;
            } else {
                filedata.seek += readsize;
            }

            return readsize;
        }
    };

    public static FileData blo_decode_and_check(FileData fd, int[] error_r, String path) {
        decode_blender_header(fd);

        if ((fd.flags & FD_FLAGS_FILE_OK) != 0) {
            if (read_file_dna(fd, path) == 0) {
                error_r[0] = BlendFileData.BRE_INCOMPLETE;
                fd = null;
            }
        } else {
            error_r[0] = BlendFileData.BRE_NOT_A_BLEND;
            fd = null;
        }

        return fd;
    }

    public static BlendFileData blo_read_blendafterruntime(ByteBuffer file, String name, int actualsize, int[] error_r, String outpath) {
        BlendFileData bfd = null;
        FileData fd = new FileData();
        fd.filedes = file;
        fd.buffersize = actualsize;
        fd.reader = fd_read_from_file;

        fd = blo_decode_and_check(fd, error_r, outpath);
        if (fd == null) {
            return null;
        }

        return bfd;
    }
}
