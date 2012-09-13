package blender.windowmanager;

import blender.blenkernel.IdProp;
import blender.blenkernel.bContext;
import blender.blenkernel.IdProp.IDPropertyTemplate;
import blender.blenlib.ListBaseUtil;
import blender.blenlib.StringUtil;
import blender.editors.uinterface.UILayout;
import blender.editors.uinterface.UIRegions;
import blender.editors.uinterface.UILayout.uiLayout;
import blender.editors.uinterface.UIRegions.uiPopupMenu;
import blender.makesdna.DNA_ID;
import blender.makesdna.WindowManagerTypes;
import blender.makesdna.WindowManagerTypes.wmOperatorType;
import blender.makesdna.sdna.IDProperty;
import blender.makesdna.sdna.ListBase;
import blender.makesdna.sdna.wmOperator;
import blender.makesdna.sdna.wmWindow;
import blender.makesrna.RnaAccess;
import blender.makesrna.RnaDefine;
import blender.makesrna.RNATypes.PointerRNA;
import blender.makesrna.rna_internal_types.BlenderRNA;
import blender.windowmanager.WmTypes.wmEvent;

public class WmOperatorsUtil {
	
	public static interface OpFunc {
	    public void run(wmOperatorType ot);
	}

	static ListBase<wmOperatorType> global_ops= new ListBase<wmOperatorType>();

	/* ************ operator API, exported ********** */


	public static wmOperatorType WM_operatortype_find(String idname, boolean quiet)
	{
		wmOperatorType ot;

		byte[] idname_bl=new byte[WindowManagerTypes.OP_MAX_TYPENAME]; // XXX, needed to support python style names without the _OT_ syntax
		WM_operator_bl_idname(idname_bl, StringUtil.toCString(idname));

		for(ot= global_ops.first; ot!=null; ot= ot.next) {
			if(StringUtil.strncmp(StringUtil.toCString(ot.idname),0, idname_bl,0, WindowManagerTypes.OP_MAX_TYPENAME)==0)
			   return ot;
		}

//		if(!quiet)
//			System.out.printf("search for unknown operator %s, %s\n", StringUtil.toJString(idname_bl,0), idname);

		return null;
	}

	//wmOperatorType *WM_operatortype_exists(const char *idname)
	//{
//		wmOperatorType *ot;
	//
//		char idname_bl[OP_MAX_TYPENAME]; // XXX, needed to support python style names without the _OT_ syntax
//		WM_operator_bl_idname(idname_bl, idname);
	//
//		for(ot= global_ops.first; ot; ot= ot.next) {
//			if(strncmp(ot.idname, idname_bl, OP_MAX_TYPENAME)==0)
//			   return ot;
//		}
//		return NULL;
	//}
	//
	//wmOperatorType *WM_operatortype_first(void)
	//{
//		return global_ops.first;
	//}

	/* all ops in 1 list (for time being... needs evaluation later) */
	public static void WM_operatortype_append(OpFunc opfunc)
	{
		wmOperatorType ot;
		
		ot= new wmOperatorType();
		ot.srna= RnaDefine.RNA_def_struct(new BlenderRNA(), "", "OperatorProperties");
//	    ot.srna= new StructRNA();
		opfunc.run(ot);

		if(ot.name==null) {
//			static char dummy_name[] = "Dummy Name";
			System.err.printf("ERROR: Operator %s has no name property!\n", ot.idname);
//			ot.name= dummy_name;
	                ot.name= "Dummy Name";
		}

//		RNA_def_struct_ui_text(ot.srna, ot.name, ot.description ? ot.description:"(undocumented operator)"); // XXX All ops should have a description but for now allow them not to.
//		RNA_def_struct_identifier(ot.srna, ot.idname);
		ListBaseUtil.BLI_addtail(global_ops, ot);
	}

	//void WM_operatortype_append_ptr(void (*opfunc)(wmOperatorType*, void*), void *userdata)
	//{
//		wmOperatorType *ot;
	//
//		ot= MEM_callocN(sizeof(wmOperatorType), "operatortype");
//		ot.srna= RNA_def_struct(&BLENDER_RNA, "", "OperatorProperties");
//		opfunc(ot, userdata);
//		RNA_def_struct_ui_text(ot.srna, ot.name, ot.description ? ot.description:"(undocumented operator)");
//		RNA_def_struct_identifier(ot.srna, ot.idname);
//		BLI_addtail(&global_ops, ot);
	//}
	//
	//int WM_operatortype_remove(const char *idname)
	//{
//		wmOperatorType *ot = WM_operatortype_find(idname, 0);
	//
//		if (ot==NULL)
//			return 0;
	//
//		BLI_remlink(&global_ops, ot);
//		RNA_struct_free(&BLENDER_RNA, ot.srna);
//		MEM_freeN(ot);
	//
//		return 1;
	//}
	//
	///* SOME_OT_op . some.op */
	//void WM_operator_py_idname(char *to, const char *from)
	//{
//		char *sep= strstr(from, "_OT_");
//		if(sep) {
//			int i, ofs= (sep-from);
	//
//			for(i=0; i<ofs; i++)
//				to[i]= tolower(from[i]);
	//
//			to[ofs] = '.';
//			BLI_strncpy(to+(ofs+1), sep+4, OP_MAX_TYPENAME);
//		}
//		else {
//			/* should not happen but support just incase */
//			BLI_strncpy(to, from, OP_MAX_TYPENAME);
//		}
	//}

	/* some.op . SOME_OT_op */
	public static void WM_operator_bl_idname(byte[] to, byte[] from)
	{
		int sep_p= StringUtil.strstr(from,0, StringUtil.toCString("."),0);

		if(sep_p!=-1) {
			int i, ofs= sep_p;

			for(i=0; i<ofs; i++)
//				to[i]= toupper(from[i]);
	                        to[i]= (byte)Character.toUpperCase(from[i]&0xFF);

			StringUtil.BLI_strncpy(to,ofs, StringUtil.toCString("_OT_"),0, WindowManagerTypes.OP_MAX_TYPENAME);
			StringUtil.BLI_strncpy(to,(ofs+4), from,sep_p+1, WindowManagerTypes.OP_MAX_TYPENAME);
		}
		else {
			/* should not happen but support just incase */
			StringUtil.BLI_strncpy(to,0, from,0, WindowManagerTypes.OP_MAX_TYPENAME);
		}
	}

	///* print a string representation of the operator, with the args that it runs
	// * so python can run it again */
	//char *WM_operator_pystring(wmOperator *op)
	//{
//		const char *arg_name= NULL;
//		char idname_py[OP_MAX_TYPENAME];
	//
//		PropertyRNA *prop, *iterprop;
	//
//		/* for building the string */
//		DynStr *dynstr= BLI_dynstr_new();
//		char *cstring, *buf;
//		int first_iter=1;
	//
//		WM_operator_py_idname(idname_py, op.idname);
//		BLI_dynstr_appendf(dynstr, "bpy.ops.%s(", idname_py);
	//
//		iterprop= RNA_struct_iterator_property(op.ptr.type);
	//
//		RNA_PROP_BEGIN(op.ptr, propptr, iterprop) {
//			prop= propptr.data;
//			arg_name= RNA_property_identifier(prop);
	//
//			if (strcmp(arg_name, "rna_type")==0) continue;
	//
//			buf= RNA_property_as_string(op.ptr, prop);
	//
//			BLI_dynstr_appendf(dynstr, first_iter?"%s=%s":", %s=%s", arg_name, buf);
	//
//			MEM_freeN(buf);
//			first_iter = 0;
//		}
//		RNA_PROP_END;
	//
//		BLI_dynstr_append(dynstr, ")");
	//
//		cstring = BLI_dynstr_get_cstring(dynstr);
//		BLI_dynstr_free(dynstr);
//		return cstring;
	//}

	public static void WM_operator_properties_create_ptr(PointerRNA ptr, wmOperatorType ot)
	{
		RnaAccess.RNA_pointer_create(null, ot.srna, null, ptr);
	}

	public static void WM_operator_properties_create(PointerRNA ptr, String opstring)
	{
		wmOperatorType ot= WM_operatortype_find(opstring, false);

		if(ot!=null) {
			WM_operator_properties_create_ptr(ptr, ot);
	    }
		else {
			RnaAccess.RNA_pointer_create(null, RnaAccess.getRnaOperatorProperties(), null, ptr);
	    }
	}

	///* similar to the function above except its uses ID properties
	// * used for keymaps and macros */
	////public static void WM_operator_properties_alloc(PointerRNA[] ptr, IDProperty[] properties, String opstring)
	//public static void WM_operator_properties_alloc(wmKeyMapItem kmi)
	//{
////		if(kmi.properties==null) {
////			IDPropertyTemplate val = {0};
////			kmi.properties= IDP_New(IDP_GROUP, val, "wmOpItemProp");
////		}
	//
//		if(kmi.ptr==null) {
//			kmi.ptr= new PointerRNA();
//			WM_operator_properties_create((PointerRNA)kmi.ptr, StringUtil.toJString(kmi.idname,0));
//		}
	//
////		kmi.ptr.data= kmi.properties;
	//
	//}
	/* similar to the function above except its uses ID properties
	 * used for keymaps and macros */
	public static void WM_operator_properties_alloc(PointerRNA[] ptr, IDProperty[] properties, byte[] opstring, int offset)
	{
		if(properties[0]==null) {
			IDPropertyTemplate val = new IDPropertyTemplate();
			properties[0]= IdProp.IDP_New(DNA_ID.IDP_GROUP, val, "wmOpItemProp");
		}

		if(ptr[0]==null) {
			ptr[0]= new PointerRNA();
			WM_operator_properties_create(ptr[0], StringUtil.toJString(opstring,offset));
		}

		ptr[0].setData(properties[0]);

	}

	public static void WM_operator_properties_sanitize(PointerRNA ptr, boolean no_context)
	{
//		RNA_STRUCT_BEGIN(ptr, prop) {
//			switch(RNA_property_type(prop)) {
//			case PROP_ENUM:
//				if (no_context)
//					RNA_def_property_flag(prop, PROP_ENUM_NO_CONTEXT);
//				else
//					RNA_def_property_clear_flag(prop, PROP_ENUM_NO_CONTEXT);
//				break;
//			case PROP_POINTER:
//				{
//					StructRNA *ptype= RNA_property_pointer_type(ptr, prop);
	//
//					/* recurse into operator properties */
//					if (RNA_struct_is_a(ptype, &RNA_OperatorProperties)) {
//						PointerRNA opptr = RNA_property_pointer_get(ptr, prop);
//						WM_operator_properties_sanitize(&opptr, no_context);
//					}
//					break;
//				}
//			default:
//				break;
//			}
//		}
//		RNA_STRUCT_END;
	}

	//void WM_operator_properties_free(PointerRNA *ptr)
	//{
//		IDProperty *properties= ptr.data;
	//
//		if(properties) {
//			IDP_FreeProperty(properties);
//			MEM_freeN(properties);
//		}
	//}

	/* ************ default op callbacks, exported *********** */

	/* invoke callback, uses enum property named "type" */
	/* only weak thing is the fixed property name... */
	public static wmOperatorType.Operator WM_menu_invoke = new wmOperatorType.Operator() {
	public int run(bContext C, wmOperator op, wmEvent event)
	//int WM_menu_invoke(bContext *C, wmOperator *op, wmEvent *event)
	{
//		PropertyRNA prop= RNA_struct_find_property(op.ptr, "type");
		uiPopupMenu pup;
		uiLayout layout;

//		if(prop==null) {
//			printf("WM_menu_invoke: %s has no \"type\" enum property\n", op.type.idname);
//		}
//		else if (RNA_property_type(prop) != PROP_ENUM) {
//			printf("WM_menu_invoke: %s \"type\" is not an enum property\n", op.type.idname);
//		}
//		else {
			pup= UIRegions.uiPupMenuBegin(C, ((wmOperatorType)op.type).name, 0);
			layout= UIRegions.uiPupMenuLayout(pup);
			UILayout.uiItemsEnumO(layout, C, ((wmOperatorType)op.type).idname, "type");
			UIRegions.uiPupMenuEnd(C, pup);
//		}

		return WindowManagerTypes.OPERATOR_CANCELLED;
	}};
	
	//
	///* ************ window gesture operator-callback definitions ************** */
	///*
	// * These are default callbacks for use in operators requiring gesture input
	// */
	//
	///* **************** Border gesture *************** */
	//
	///* Border gesture has two types:
	//   1) WM_GESTURE_CROSS_RECT: starts a cross, on mouse click it changes to border
	//   2) WM_GESTURE_RECT: starts immediate as a border, on mouse click or release it ends
	//
	//   It stores 4 values (xmin, xmax, ymin, ymax) and event it ended with (event_type)
	//*/
	//
	//static int border_apply(bContext *C, wmOperator *op, int event_type, int event_orig)
	//{
//		wmGesture *gesture= op.customdata;
//		rcti *rect= gesture.customdata;
	//
//		if(rect.xmin > rect.xmax)
//			SWAP(int, rect.xmin, rect.xmax);
//		if(rect.ymin > rect.ymax)
//			SWAP(int, rect.ymin, rect.ymax);
	//
//		if(rect.xmin==rect.xmax || rect.ymin==rect.ymax)
//			return 0;
	//
//		/* operator arguments and storage. */
//		RNA_int_set(op.ptr, "xmin", rect.xmin);
//		RNA_int_set(op.ptr, "ymin", rect.ymin);
//		RNA_int_set(op.ptr, "xmax", rect.xmax);
//		RNA_int_set(op.ptr, "ymax", rect.ymax);
	//
//		/* XXX weak; border should be configured for this without reading event types */
//		if( RNA_struct_find_property(op.ptr, "event_type") ) {
//			if(ELEM4(event_orig, EVT_TWEAK_L, EVT_TWEAK_R, EVT_TWEAK_A, EVT_TWEAK_S))
//				event_type= LEFTMOUSE;
	//
//			RNA_int_set(op.ptr, "event_type", event_type);
//		}
//		op.type.exec(C, op);
	//
//		return 1;
	//}
	//
	//static void wm_gesture_end(bContext *C, wmOperator *op)
	//{
//		wmGesture *gesture= op.customdata;
	//
//		WM_gesture_end(C, gesture);	/* frees gesture itself, and unregisters from window */
//		op.customdata= NULL;
	//
//		ED_area_tag_redraw(CTX_wm_area(C));
	//
//		if( RNA_struct_find_property(op.ptr, "cursor") )
//			WM_cursor_restore(CTX_wm_window(C));
	//}
	//
	//int WM_border_select_invoke(bContext *C, wmOperator *op, wmEvent *event)
	//{
//		if(WM_key_event_is_tweak(event.type))
//			op.customdata= WM_gesture_new(C, event, WM_GESTURE_RECT);
//		else
//			op.customdata= WM_gesture_new(C, event, WM_GESTURE_CROSS_RECT);
	//
//		/* add modal handler */
//		WM_event_add_modal_handler(C, &CTX_wm_window(C).handlers, op);
	//
//		wm_gesture_tag_redraw(C);
	//
//		return OPERATOR_RUNNING_MODAL;
	//}
	//
	//int WM_border_select_modal(bContext *C, wmOperator *op, wmEvent *event)
	//{
//		wmGesture *gesture= op.customdata;
//		rcti *rect= gesture.customdata;
//		int sx, sy;
	//
//		switch(event.type) {
//			case MOUSEMOVE:
	//
//				wm_subwindow_getorigin(CTX_wm_window(C), gesture.swinid, &sx, &sy);
	//
//				if(gesture.type==WM_GESTURE_CROSS_RECT && gesture.mode==0) {
//					rect.xmin= rect.xmax= event.x - sx;
//					rect.ymin= rect.ymax= event.y - sy;
//				}
//				else {
//					rect.xmax= event.x - sx;
//					rect.ymax= event.y - sy;
//				}
	//
//				wm_gesture_tag_redraw(C);
	//
//				break;
	//
//			case LEFTMOUSE:
//			case MIDDLEMOUSE:
//			case RIGHTMOUSE:
//				if(event.val==KM_PRESS) {
//					if(gesture.type==WM_GESTURE_CROSS_RECT && gesture.mode==0) {
//						gesture.mode= 1;
//						wm_gesture_tag_redraw(C);
//					}
//				}
//				else {
//					if(border_apply(C, op, event.type, gesture.event_type)) {
//						wm_gesture_end(C, op);
//						return OPERATOR_FINISHED;
//					}
//					wm_gesture_end(C, op);
//					return OPERATOR_CANCELLED;
//				}
//				break;
//			case ESCKEY:
//				wm_gesture_end(C, op);
//				return OPERATOR_CANCELLED;
//		}
//		return OPERATOR_RUNNING_MODAL;
	//}
	//
	///* **************** circle gesture *************** */
	///* works now only for selection or modal paint stuff, calls exec while hold mouse, exit on release */
	//
	//int WM_gesture_circle_invoke(bContext *C, wmOperator *op, wmEvent *event)
	//{
//		op.customdata= WM_gesture_new(C, event, WM_GESTURE_CIRCLE);
	//
//		/* add modal handler */
//		WM_event_add_modal_handler(C, &CTX_wm_window(C).handlers, op);
	//
//		wm_gesture_tag_redraw(C);
	//
//		return OPERATOR_RUNNING_MODAL;
	//}
	//
	//static void gesture_circle_apply(bContext *C, wmOperator *op)
	//{
//		wmGesture *gesture= op.customdata;
//		rcti *rect= gesture.customdata;
	//
//		/* operator arguments and storage. */
//		RNA_int_set(op.ptr, "x", rect.xmin);
//		RNA_int_set(op.ptr, "y", rect.ymin);
//		RNA_int_set(op.ptr, "radius", rect.xmax);
	//
//		if(op.type.exec)
//			op.type.exec(C, op);
	//}
	//
	//int WM_gesture_circle_modal(bContext *C, wmOperator *op, wmEvent *event)
	//{
//		wmGesture *gesture= op.customdata;
//		rcti *rect= gesture.customdata;
//		int sx, sy;
	//
//		switch(event.type) {
//			case MOUSEMOVE:
	//
//				wm_subwindow_getorigin(CTX_wm_window(C), gesture.swinid, &sx, &sy);
	//
//				rect.xmin= event.x - sx;
//				rect.ymin= event.y - sy;
	//
//				wm_gesture_tag_redraw(C);
	//
//				if(gesture.mode)
//					gesture_circle_apply(C, op);
	//
//				break;
//			case WHEELUPMOUSE:
//				rect.xmax += 2 + rect.xmax/10;
//				wm_gesture_tag_redraw(C);
//				break;
//			case WHEELDOWNMOUSE:
//				rect.xmax -= 2 + rect.xmax/10;
//				if(rect.xmax < 1) rect.xmax= 1;
//				wm_gesture_tag_redraw(C);
//				break;
//			case LEFTMOUSE:
//			case MIDDLEMOUSE:
//			case RIGHTMOUSE:
//				if(event.val==0) {	/* key release */
//					wm_gesture_end(C, op);
//					return OPERATOR_FINISHED;
//				}
//				else {
//					if( RNA_struct_find_property(op.ptr, "event_type") )
//						RNA_int_set(op.ptr, "event_type", event.type);
	//
//					/* apply first click */
//					gesture_circle_apply(C, op);
//					gesture.mode= 1;
//				}
//				break;
//			case ESCKEY:
//				wm_gesture_end(C, op);
//				return OPERATOR_CANCELLED;
//		}
//		return OPERATOR_RUNNING_MODAL;
	//}
	//
	//#if 0
	///* template to copy from */
	//void WM_OT_circle_gesture(wmOperatorType *ot)
	//{
//		ot.name= "Circle Gesture";
//		ot.idname= "WM_OT_circle_gesture";
	//
//		ot.invoke= WM_gesture_circle_invoke;
//		ot.modal= WM_gesture_circle_modal;
	//
//		ot.poll= WM_operator_winactive;
	//
//		RNA_def_property(ot.srna, "x", PROP_INT, PROP_NONE);
//		RNA_def_property(ot.srna, "y", PROP_INT, PROP_NONE);
//		RNA_def_property(ot.srna, "radius", PROP_INT, PROP_NONE);
	//
	//}
	//#endif
	//
	///* **************** Tweak gesture *************** */
	//
	//static void tweak_gesture_modal(bContext *C, wmEvent *event)
	//{
//		wmWindow *window= CTX_wm_window(C);
//		wmGesture *gesture= window.tweak;
//		rcti *rect= gesture.customdata;
//		int sx, sy, val;
	//
//		switch(event.type) {
//			case MOUSEMOVE:
	//
//				wm_subwindow_getorigin(window, gesture.swinid, &sx, &sy);
	//
//				rect.xmax= event.x - sx;
//				rect.ymax= event.y - sy;
	//
//				if((val= wm_gesture_evaluate(C, gesture))) {
//					wmEvent event;
	//
//					event= *(window.eventstate);
//					if(gesture.event_type==LEFTMOUSE)
//						event.type= EVT_TWEAK_L;
//					else if(gesture.event_type==RIGHTMOUSE)
//						event.type= EVT_TWEAK_R;
//					else
//						event.type= EVT_TWEAK_M;
//					event.val= val;
//					/* mouse coords! */
//					wm_event_add(window, &event);
	//
//					WM_gesture_end(C, gesture);	/* frees gesture itself, and unregisters from window */
//					window.tweak= NULL;
//				}
	//
//				break;
	//
//			case LEFTMOUSE:
//			case RIGHTMOUSE:
//			case MIDDLEMOUSE:
//				if(gesture.event_type==event.type) {
//					WM_gesture_end(C, gesture);
//					window.tweak= NULL;
	//
//					/* when tweak fails we should give the other keymap entries a chance */
//					event.val= KM_RELEASE;
//				}
//				break;
//			default:
//				WM_gesture_end(C, gesture);
//				window.tweak= NULL;
//		}
	//}

	/* standard tweak, called after window handlers passed on event */
	public static void wm_tweakevent_test(bContext C, wmEvent event, int action)
	{
		wmWindow win= bContext.CTX_wm_window(C);

		if(win.tweak==null) {
			if(bContext.CTX_wm_region(C)!=null) {
				if(event.val==WmTypes.KM_PRESS) { // pressed
//					if(event.type==WmEventTypes.LEFTMOUSE || event.type==WmEventTypes.MIDDLEMOUSE || event.type==WmEventTypes.RIGHTMOUSE)
//						win.tweak= WM_gesture_new(C, event, WmTypes.WM_GESTURE_TWEAK);
				}
			}
		}
		else {
			if(action==WmEventSystem.WM_HANDLER_BREAK) {
//				WM_gesture_end(C, win.tweak);
				win.tweak= null;
			}
//			else
//				tweak_gesture_modal(C, event);
		}
	}

	///* *********************** lasso gesture ****************** */
	//
	//int WM_gesture_lasso_invoke(bContext *C, wmOperator *op, wmEvent *event)
	//{
//		op.customdata= WM_gesture_new(C, event, WM_GESTURE_LASSO);
	//
//		/* add modal handler */
//		WM_event_add_modal_handler(C, &CTX_wm_window(C).handlers, op);
	//
//		wm_gesture_tag_redraw(C);
	//
//		if( RNA_struct_find_property(op.ptr, "cursor") )
//			WM_cursor_modal(CTX_wm_window(C), RNA_int_get(op.ptr, "cursor"));
	//
//		return OPERATOR_RUNNING_MODAL;
	//}
	//
	//int WM_gesture_lines_invoke(bContext *C, wmOperator *op, wmEvent *event)
	//{
//		op.customdata= WM_gesture_new(C, event, WM_GESTURE_LINES);
	//
//		/* add modal handler */
//		WM_event_add_modal_handler(C, &CTX_wm_window(C).handlers, op);
	//
//		wm_gesture_tag_redraw(C);
	//
//		if( RNA_struct_find_property(op.ptr, "cursor") )
//			WM_cursor_modal(CTX_wm_window(C), RNA_int_get(op.ptr, "cursor"));
	//
//		return OPERATOR_RUNNING_MODAL;
	//}
	//
	//
	//static void gesture_lasso_apply(bContext *C, wmOperator *op, int event_type)
	//{
//		wmGesture *gesture= op.customdata;
//		PointerRNA itemptr;
//		float loc[2];
//		int i;
//		short *lasso= gesture.customdata;
	//
//		/* operator storage as path. */
	//
//		for(i=0; i<gesture.points; i++, lasso+=2) {
//			loc[0]= lasso[0];
//			loc[1]= lasso[1];
//			RNA_collection_add(op.ptr, "path", &itemptr);
//			RNA_float_set_array(&itemptr, "loc", loc);
//		}
	//
//		wm_gesture_end(C, op);
	//
//		if(op.type.exec)
//			op.type.exec(C, op);
	//
	//}
	//
	//int WM_gesture_lasso_modal(bContext *C, wmOperator *op, wmEvent *event)
	//{
//		wmGesture *gesture= op.customdata;
//		int sx, sy;
	//
//		switch(event.type) {
//			case MOUSEMOVE:
	//
//				wm_gesture_tag_redraw(C);
	//
//				wm_subwindow_getorigin(CTX_wm_window(C), gesture.swinid, &sx, &sy);
//				if(gesture.points < WM_LASSO_MAX_POINTS) {
//					short *lasso= gesture.customdata;
//					lasso += 2 * gesture.points;
//					lasso[0] = event.x - sx;
//					lasso[1] = event.y - sy;
//					gesture.points++;
//				}
//				else {
//					gesture_lasso_apply(C, op, event.type);
//					return OPERATOR_FINISHED;
//				}
//				break;
	//
//			case LEFTMOUSE:
//			case MIDDLEMOUSE:
//			case RIGHTMOUSE:
//				if(event.val==0) {	/* key release */
//					gesture_lasso_apply(C, op, event.type);
//					return OPERATOR_FINISHED;
//				}
//				break;
//			case ESCKEY:
//				wm_gesture_end(C, op);
//				return OPERATOR_CANCELLED;
//		}
//		return OPERATOR_RUNNING_MODAL;
	//}
	//
	//int WM_gesture_lines_modal(bContext *C, wmOperator *op, wmEvent *event)
	//{
//		return WM_gesture_lasso_modal(C, op, event);
	//}
	//
	//#if 0
	///* template to copy from */
	//
	//static int gesture_lasso_exec(bContext *C, wmOperator *op)
	//{
//		RNA_BEGIN(op.ptr, itemptr, "path") {
//			float loc[2];
	//
//			RNA_float_get_array(&itemptr, "loc", loc);
//			printf("Location: %f %f\n", loc[0], loc[1]);
//		}
//		RNA_END;
	//
//		return OPERATOR_FINISHED;
	//}
	//
	//void WM_OT_lasso_gesture(wmOperatorType *ot)
	//{
//		PropertyRNA *prop;
	//
//		ot.name= "Lasso Gesture";
//		ot.idname= "WM_OT_lasso_gesture";
	//
//		ot.invoke= WM_gesture_lasso_invoke;
//		ot.modal= WM_gesture_lasso_modal;
//		ot.exec= gesture_lasso_exec;
	//
//		ot.poll= WM_operator_winactive;
	//
//		prop= RNA_def_property(ot.srna, "path", PROP_COLLECTION, PROP_NONE);
//		RNA_def_property_struct_runtime(prop, &RNA_OperatorMousePath);
	//}
	//#endif
	//
	///* *********************** radial control ****************** */
	//
	//const int WM_RADIAL_CONTROL_DISPLAY_SIZE = 200;
	//
	//typedef struct wmRadialControl {
//		int mode;
//		float initial_value, value, max_value;
//		int initial_mouse[2];
//		void *cursor;
//		GLuint tex;
	//} wmRadialControl;
	//
	//static void wm_radial_control_paint(bContext *C, int x, int y, void *customdata)
	//{
//		wmRadialControl *rc = (wmRadialControl*)customdata;
//		ARegion *ar = CTX_wm_region(C);
//		float r1=0.0f, r2=0.0f, r3=0.0f, angle=0.0f;
	//
//		/* Keep cursor in the original place */
//		x = rc.initial_mouse[0] - ar.winrct.xmin;
//		y = rc.initial_mouse[1] - ar.winrct.ymin;
	//
//		glPushMatrix();
	//
//		glTranslatef((float)x, (float)y, 0.0f);
	//
//		if(rc.mode == WM_RADIALCONTROL_SIZE) {
//			r1= rc.value;
//			r2= rc.initial_value;
//			r3= r1;
//		} else if(rc.mode == WM_RADIALCONTROL_STRENGTH) {
//			r1= (1 - rc.value) * WM_RADIAL_CONTROL_DISPLAY_SIZE;
//			r2= WM_RADIAL_CONTROL_DISPLAY_SIZE;
//			r3= WM_RADIAL_CONTROL_DISPLAY_SIZE;
//		} else if(rc.mode == WM_RADIALCONTROL_ANGLE) {
//			r1= r2= WM_RADIAL_CONTROL_DISPLAY_SIZE;
//			r3= WM_RADIAL_CONTROL_DISPLAY_SIZE;
//			angle = rc.value;
//		}
	//
//		glColor4ub(255, 255, 255, 128);
//		glEnable( GL_LINE_SMOOTH );
//		glEnable(GL_BLEND);
	//
//		if(rc.mode == WM_RADIALCONTROL_ANGLE)
//			fdrawline(0, 0, WM_RADIAL_CONTROL_DISPLAY_SIZE, 0);
	//
//		if(rc.tex) {
//			const float str = rc.mode == WM_RADIALCONTROL_STRENGTH ? (rc.value + 0.5) : 1;
	//
//			if(rc.mode == WM_RADIALCONTROL_ANGLE) {
//				glRotatef(angle, 0, 0, 1);
//				fdrawline(0, 0, WM_RADIAL_CONTROL_DISPLAY_SIZE, 0);
//			}
	//
//			glBindTexture(GL_TEXTURE_2D, rc.tex);
	//
//			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	//
//			glEnable(GL_TEXTURE_2D);
//			glBegin(GL_QUADS);
//			glColor4f(0,0,0, str);
//			glTexCoord2f(0,0);
//			glVertex2f(-r3, -r3);
//			glTexCoord2f(1,0);
//			glVertex2f(r3, -r3);
//			glTexCoord2f(1,1);
//			glVertex2f(r3, r3);
//			glTexCoord2f(0,1);
//			glVertex2f(-r3, r3);
//			glEnd();
//			glDisable(GL_TEXTURE_2D);
//		}
	//
//		glColor4ub(255, 255, 255, 128);
//		glutil_draw_lined_arc(0.0, M_PI*2.0, r1, 40);
//		glutil_draw_lined_arc(0.0, M_PI*2.0, r2, 40);
//		glDisable(GL_BLEND);
//		glDisable( GL_LINE_SMOOTH );
	//
//		glPopMatrix();
	//}
	//
	//int WM_radial_control_modal(bContext *C, wmOperator *op, wmEvent *event)
	//{
//		wmRadialControl *rc = (wmRadialControl*)op.customdata;
//		int mode, initial_mouse[2], delta[2];
//		float dist;
//		double new_value = RNA_float_get(op.ptr, "new_value");
//		int ret = OPERATOR_RUNNING_MODAL;
	//
//		mode = RNA_int_get(op.ptr, "mode");
//		RNA_int_get_array(op.ptr, "initial_mouse", initial_mouse);
	//
//		switch(event.type) {
//		case MOUSEMOVE:
//			delta[0]= initial_mouse[0] - event.x;
//			delta[1]= initial_mouse[1] - event.y;
//			dist= sqrt(delta[0]*delta[0]+delta[1]*delta[1]);
	//
//			if(mode == WM_RADIALCONTROL_SIZE)
//				new_value = dist;
//			else if(mode == WM_RADIALCONTROL_STRENGTH) {
//				new_value = 1 - dist / WM_RADIAL_CONTROL_DISPLAY_SIZE;
//			} else if(mode == WM_RADIALCONTROL_ANGLE)
//				new_value = ((int)(atan2(delta[1], delta[0]) * (180.0 / M_PI)) + 180);
	//
//			if(event.ctrl) {
//				if(mode == WM_RADIALCONTROL_STRENGTH)
//					new_value = ((int)(new_value * 100) / 10*10) / 100.0f;
//				else
//					new_value = ((int)new_value + 5) / 10*10;
//			}
	//
//			break;
//		case ESCKEY:
//		case RIGHTMOUSE:
//			ret = OPERATOR_CANCELLED;
//			break;
//		case LEFTMOUSE:
//		case PADENTER:
//			op.type.exec(C, op);
//			ret = OPERATOR_FINISHED;
//			break;
//		}
	//
//		/* Clamp */
//		if(new_value > rc.max_value)
//			new_value = rc.max_value;
//		else if(new_value < 0)
//			new_value = 0;
	//
//		/* Update paint data */
//		rc.value = new_value;
	//
//		RNA_float_set(op.ptr, "new_value", new_value);
	//
//		if(ret != OPERATOR_RUNNING_MODAL) {
//			WM_paint_cursor_end(CTX_wm_manager(C), rc.cursor);
//			MEM_freeN(rc);
//		}
	//
//		ED_region_tag_redraw(CTX_wm_region(C));
	//
//		return ret;
	//}
	//
	///* Expects the operator customdata to be an ImBuf (or NULL) */
	//int WM_radial_control_invoke(bContext *C, wmOperator *op, wmEvent *event)
	//{
//		wmRadialControl *rc = MEM_callocN(sizeof(wmRadialControl), "radial control");
//		int mode = RNA_int_get(op.ptr, "mode");
//		float initial_value = RNA_float_get(op.ptr, "initial_value");
//		int mouse[2] = {event.x, event.y};
	//
//		if(mode == WM_RADIALCONTROL_SIZE) {
//			rc.max_value = 200;
//			mouse[0]-= initial_value;
//		}
//		else if(mode == WM_RADIALCONTROL_STRENGTH) {
//			rc.max_value = 1;
//			mouse[0]-= WM_RADIAL_CONTROL_DISPLAY_SIZE * (1 - initial_value);
//		}
//		else if(mode == WM_RADIALCONTROL_ANGLE) {
//			rc.max_value = 360;
//			mouse[0]-= WM_RADIAL_CONTROL_DISPLAY_SIZE * cos(initial_value);
//			mouse[1]-= WM_RADIAL_CONTROL_DISPLAY_SIZE * sin(initial_value);
//			initial_value *= 180.0f/M_PI;
//		}
	//
//		if(op.customdata) {
//			ImBuf *im = (ImBuf*)op.customdata;
//			/* Build GL texture */
//			glGenTextures(1, &rc.tex);
//			glBindTexture(GL_TEXTURE_2D, rc.tex);
//			glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, im.x, im.y, 0, GL_ALPHA, GL_FLOAT, im.rect_float);
//			MEM_freeN(im.rect_float);
//			MEM_freeN(im);
//		}
	//
//		RNA_int_set_array(op.ptr, "initial_mouse", mouse);
//		RNA_float_set(op.ptr, "new_value", initial_value);
	//
//		op.customdata = rc;
//		rc.mode = mode;
//		rc.initial_value = initial_value;
//		rc.initial_mouse[0] = mouse[0];
//		rc.initial_mouse[1] = mouse[1];
//		rc.cursor = WM_paint_cursor_activate(CTX_wm_manager(C), op.type.poll,
//						      wm_radial_control_paint, op.customdata);
	//
//		/* add modal handler */
//		WM_event_add_modal_handler(C, &CTX_wm_window(C).handlers, op);
	//
//		WM_radial_control_modal(C, op, event);
	//
//		return OPERATOR_RUNNING_MODAL;
	//}
	//
	///* Gets a descriptive string of the operation */
	//void WM_radial_control_string(wmOperator *op, char str[], int maxlen)
	//{
//		int mode = RNA_int_get(op.ptr, "mode");
//		float v = RNA_float_get(op.ptr, "new_value");
	//
//		if(mode == WM_RADIALCONTROL_SIZE)
//			sprintf(str, "Size: %d", (int)v);
//		else if(mode == WM_RADIALCONTROL_STRENGTH)
//			sprintf(str, "Strength: %d", (int)v);
//		else if(mode == WM_RADIALCONTROL_ANGLE)
//			sprintf(str, "Angle: %d", (int)(v * 180.0f/M_PI));
	//}
	//
	///** Important: this doesn't define an actual operator, it
//	    just sets up the common parts of the radial control op. **/
	//void WM_OT_radial_control_partial(wmOperatorType *ot)
	//{
//		static EnumPropertyItem prop_mode_items[] = {
//			{WM_RADIALCONTROL_SIZE, "SIZE", 0, "Size", ""},
//			{WM_RADIALCONTROL_STRENGTH, "STRENGTH", 0, "Strength", ""},
//			{WM_RADIALCONTROL_ANGLE, "ANGLE", 0, "Angle", ""},
//			{0, NULL, 0, NULL, NULL}};
	//
//		/* Should be set in custom invoke() */
//		RNA_def_float(ot.srna, "initial_value", 0, 0, FLT_MAX, "Initial Value", "", 0, FLT_MAX);
	//
//		/* Set internally, should be used in custom exec() to get final value */
//		RNA_def_float(ot.srna, "new_value", 0, 0, FLT_MAX, "New Value", "", 0, FLT_MAX);
	//
//		/* Should be set before calling operator */
//		RNA_def_enum(ot.srna, "mode", prop_mode_items, 0, "Mode", "");
	//
//		/* Internal */
//		RNA_def_int_vector(ot.srna, "initial_mouse", 2, NULL, INT_MIN, INT_MAX, "initial_mouse", "", INT_MIN, INT_MAX);
	//}
	//
	///* ************************** timer for testing ***************** */
	//
	///* uses no type defines, fully local testing function anyway... ;) */
	//
	//static int ten_timer_exec(bContext *C, wmOperator *op)
	//{
//		ARegion *ar= CTX_wm_region(C);
//		double stime= PIL_check_seconds_timer();
//		int type = RNA_int_get(op.ptr, "type");
//		int a, time;
//		char tmpstr[128];
	//
//		WM_cursor_wait(1);
	//
//		for(a=0; a<10; a++) {
//			if (type==0) {
//				ED_region_do_draw(C, ar);
//			}
//			else if (type==1) {
//				wmWindow *win= CTX_wm_window(C);
	//
//				ED_region_tag_redraw(ar);
//				wm_draw_update(C);
	//
//				CTX_wm_window_set(C, win);	/* XXX context manipulation warning! */
//			}
//			else if (type==2) {
//				wmWindow *win= CTX_wm_window(C);
//				ScrArea *sa;
	//
//				for(sa= CTX_wm_screen(C).areabase.first; sa; sa= sa.next)
//					ED_area_tag_redraw(sa);
//				wm_draw_update(C);
	//
//				CTX_wm_window_set(C, win);	/* XXX context manipulation warning! */
//			}
//			else if (type==3) {
//				Scene *scene= CTX_data_scene(C);
	//
//				if(a & 1) scene.r.cfra--;
//				else scene.r.cfra++;
//				scene_update_for_newframe(scene, scene.lay);
//			}
//			else {
//				ED_undo_pop(C);
//				ED_undo_redo(C);
//			}
//		}
	//
//		time= (int) ((PIL_check_seconds_timer()-stime)*1000);
	//
//		if(type==0) sprintf(tmpstr, "10 x Draw Region: %d ms", time);
//		if(type==1) sprintf(tmpstr, "10 x Draw Region and Swap: %d ms", time);
//		if(type==2) sprintf(tmpstr, "10 x Draw Window and Swap: %d ms", time);
//		if(type==3) sprintf(tmpstr, "Anim Step: %d ms", time);
//		if(type==4) sprintf(tmpstr, "10 x Undo/Redo: %d ms", time);
	//
//		WM_cursor_wait(0);
	//
//		uiPupMenuNotice(C, tmpstr);
	//
//		return OPERATOR_FINISHED;
	//}
	//
	
	/* called on initialize WM_exit() */
	public static void wm_operatortype_free()
	{
		ListBaseUtil.BLI_freelistN(global_ops);
	}

}
