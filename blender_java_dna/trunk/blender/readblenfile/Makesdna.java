package blender.readblenfile;

public class Makesdna {
    
    public static void main(String[] args) {
    	if (args.length < 2) {
    		System.out.println("Usage: Makesdna <filepath> <outputpath>");
    		return;
    	}
        System.out.println("Starting ...");
        String path = args[0];
        String outpath = args[1];
        int[] error_r = new int[1];
        BLO_readblenfile.blo_read_runtime(path, error_r, outpath);
        System.out.println("Finished.");
        System.out.println("Exit code: "+error_r[0]);
    }

}
