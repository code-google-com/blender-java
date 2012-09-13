package blender.editors.space_api;

import javax.media.opengl.GL2;

import blender.blenkernel.bContext;
import blender.blenkernel.ScreenUtil.ARegionType;
import blender.blenlib.ListBaseUtil;
import blender.makesdna.sdna.ARegion;
import blender.makesdna.sdna.Link;

public class SpaceTypeDraw {
	
	public static final int REGION_DRAW_POST_VIEW=	0;
	public static final int REGION_DRAW_POST_PIXEL=	1;
	public static final int REGION_DRAW_PRE_VIEW=	2;
	
	/* ********************** custom drawcall api ***************** */

	/* type */
	public static final int REGION_DRAW_PRE=		1;
	public static final int REGION_DRAW_POST=	0;

	public static class RegionDrawCB extends Link<RegionDrawCB> {
	        public static interface Draw {
	            public void run(GL2 gl, bContext C, ARegion ar, Object arg);
	        };
		public Draw draw;
		public Object customdata;

		public int type;
	};

	public static Object ED_region_draw_cb_activate(ARegionType art,
									 RegionDrawCB.Draw draw,
									 Object customdata, int type)
	{
		RegionDrawCB rdc= new RegionDrawCB();

		ListBaseUtil.BLI_addtail(art.drawcalls, rdc);
		rdc.draw= draw;
		rdc.customdata= customdata;
		rdc.type= type;

		return rdc;
	}

	public static void ED_region_draw_cb_exit(ARegionType art, Object handle)
	{
		RegionDrawCB rdc;

		for(rdc= (RegionDrawCB)art.drawcalls.first; rdc!=null; rdc= rdc.next) {
			if(rdc==(RegionDrawCB)handle) {
				ListBaseUtil.BLI_remlink(art.drawcalls, rdc);
//				MEM_freeN(rdc);
				return;
			}
		}
	}

	public static void ED_region_draw_cb_draw(GL2 gl, bContext C, ARegion ar, int type)
	{
		RegionDrawCB rdc;

		for(rdc= (RegionDrawCB)((ARegionType)ar.type).drawcalls.first; rdc!=null; rdc= rdc.next) {
			if(rdc.type==type)
				rdc.draw.run(gl, C, ar, rdc.customdata);
		}
	}

}
