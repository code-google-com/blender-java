/**
 * $Id:
 *
 * ***** BEGIN GPL LICENSE BLOCK *****
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version. 
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * The Original Code is Copyright (C) 2007 Blender Foundation.
 * All rights reserved.
 *
 * 
 * Contributor(s): Blender Foundation
 *
 * ***** END GPL LICENSE BLOCK *****
 */
package blender.makesdna;

import blender.blenkernel.bContext;
import blender.makesdna.sdna.Link;
import blender.makesdna.sdna.wmKeyMap;
import blender.makesdna.sdna.wmOperator;
import blender.makesrna.RNATypes.ExtensionRNA;
import blender.makesrna.rna_internal_types.StructRNA;
import blender.windowmanager.WmTypes.wmEvent;

public class WindowManagerTypes {

public static final int OP_MAX_TYPENAME=	64;
public static final int KMAP_MAX_NAME=	64;


//typedef enum ReportType {
//	RPT_DEBUG					= 1<<0,
//	RPT_INFO					= 1<<1,
//	RPT_OPERATOR				= 1<<2,
//	RPT_WARNING					= 1<<3,
//	RPT_ERROR					= 1<<4,
//	RPT_ERROR_INVALID_INPUT		= 1<<5,
//	RPT_ERROR_INVALID_CONTEXT	= 1<<6,
//	RPT_ERROR_OUT_OF_MEMORY		= 1<<7
//} ReportType;
//
//#define RPT_DEBUG_ALL		(RPT_DEBUG)
//#define RPT_INFO_ALL		(RPT_INFO)
//#define RPT_OPERATOR_ALL	(RPT_OPERATOR)
//#define RPT_WARNING_ALL		(RPT_WARNING)
//#define RPT_ERROR_ALL		(RPT_ERROR|RPT_ERROR_INVALID_INPUT|RPT_ERROR_INVALID_CONTEXT|RPT_ERROR_OUT_OF_MEMORY)
//
//enum ReportListFlags {
//	RPT_PRINT = 1,
//	RPT_STORE = 2,
//};
//typedef struct Report {
//	struct Report *next, *prev;
//	short type; /* ReportType */
//	short flag;
//	int len; /* strlen(message), saves some time calculating the word wrap  */
//	char *typestr;
//	char *message;
//} Report;
//typedef struct ReportList {
//	ListBase list;
//	int printlevel; /* ReportType */
//	int storelevel; /* ReportType */
//	int flag, pad;
//} ReportList;
///* reports need to be before wmWindowManager */
//
//
///* windowmanager is saved, tag WMAN */
//typedef struct wmWindowManager {
//	ID id;
//
//	struct wmWindow *windrawable, *winactive;		/* separate active from drawable */
//	ListBase windows;
//
//	int initialized;		/* set on file read */
//	short file_saved;		/* indicator whether data was saved */
//	short pad;
//
//	ListBase operators;		/* operator registry */
//
//	ListBase queue;			/* refresh/redraw wmNotifier structs */
//
//	struct ReportList reports;	/* information and error reports */
//
//	ListBase jobs;			/* threaded jobs manager */
//
//	ListBase paintcursors;	/* extra overlay cursors to draw, like circles */
//
//	/* used keymaps, optionally/partially saved */
//	ListBase keymaps;
//
//} wmWindowManager;

/* wmWindowManager.initialized */
public static final int WM_INIT_WINDOW=		1<<0;
public static final int WM_INIT_KEYMAP=		1<<1;

///* the savable part, rest of data is local in ghostwinlay */
//typedef struct wmWindow {
//	struct wmWindow *next, *prev;
//
//	void *ghostwin;		/* dont want to include ghost.h stuff */
//
//	int winid, pad;		/* winid also in screens, is for retrieving this window after read */
//
//	struct bScreen *screen;		/* active screen */
//	struct bScreen *newscreen;	/* temporary when switching */
//	char screenname[32];	/* MAX_ID_NAME for matching window with active screen after file read */
//
//	short posx, posy, sizex, sizey;	/* window coords */
//	short windowstate;	/* borderless, full */
//	short monitor;		/* multiscreen... no idea how to store yet */
//	short active;		/* set to 1 if an active window, for quick rejects */
//	short cursor;		/* current mouse cursor type */
//	short lastcursor;	/* for temp waitcursor */
//	short addmousemove;	/* internal: tag this for extra mousemove event, makes cursors/buttons active on UI switching */
//	short downstate; /* used for drag & drop: remembers mouse button down state */
//	short downx, downy; /* mouse coords for button down event */
//	short pad3, pad4, pad5;
//
//	struct wmEvent *eventstate;	/* storage for event system */
//
//	struct wmSubWindow *curswin;	/* internal for wm_subwindow.c only */
//
//	struct wmGesture *tweak;	/* internal for wm_operators.c */
//
//	int drawmethod, drawfail;	/* internal for wm_draw.c only */
//	void *drawdata;				/* internal for wm_draw.c only */
//
//	ListBase timers;
//
//	ListBase queue;				/* all events (ghost level events were handled) */
//	ListBase handlers;			/* window+screen handlers, overriding all queues */
//
//	ListBase subwindows;	/* opengl stuff for sub windows, see notes in wm_subwindow.c */
//	ListBase gesture;		/* gesture stuff */
//} wmWindow;

/* should be somthing like DNA_EXCLUDE
 * but the preprocessor first removes all comments, spaces etc */

//#
//#
public static class wmOperatorType extends Link<wmOperatorType> {

        public static interface Operator {
            public int run(bContext C, wmOperator op, wmEvent event);
        }
	
	public String name;		/* text for ui, undo */
	public String idname;		/* unique identifier */
	public String description;	/* tooltips and python docs */

	/* this callback executes the operator without any interactive input,
	 * parameters may be provided through operator properties. cannot use
	 * any interface code or input device state.
	 * - see defines below for return values */
	public Operator exec;

	/* for modal temporary operators, initially invoke is called. then
	 * any further events are handled in modal. if the operation is
	 * cancelled due to some external reason, cancel is called
	 * - see defines below for return values */
	public Operator invoke;
	public Operator cancel;
	public Operator modal;

	/* verify if the operator can be executed in the current context, note
	 * that the operator might still fail to execute even if this return true */
	public Operator poll;

	/* panel for redo and repeat */
//	public void *(*uiBlock)(struct wmOperator *);

	/* rna for properties */
	public StructRNA srna;

	public short flag;

	/* pointer to modal keymap, do not free! */
	public wmKeyMap modalkeymap;

	/* only used for operators defined with python
	 * use to store pointers to python functions */
	public Object pyop_data;
	
	/* RNA integration */
	public ExtensionRNA ext = new ExtensionRNA();

};


///* partial copy of the event, for matching by eventhandler */
//typedef struct wmKeymapItem {
//	struct wmKeymapItem *next, *prev;
//
//	char idname[64];				/* used to retrieve operator type pointer */
//	struct PointerRNA *ptr;			/* rna pointer to access properties */
//
//	short type;						/* event code itself */
//	short val;						/* 0=any, 1=click, 2=release, or wheelvalue, or... */
//	short shift, ctrl, alt, oskey;	/* oskey is apple or windowskey, value denotes order of pressed */
//	short keymodifier;				/* rawkey modifier */
//
//	short propvalue;				/* if used, the item is from modal map */
//
//	short inactive;					/* if set, deactivated item */
//	short maptype;						/* keymap editor */
//	short pad2, pad3;
//} wmKeymapItem;
//
//
///* stored in WM, the actively used keymaps */
//typedef struct wmKeyMap {
//	struct wmKeyMap *next, *prev;
//
//	ListBase keymap;
//
//	char nameid[64];	/* global editor keymaps, or for more per space/region */
//	short spaceid;		/* same IDs as in DNA_space_types.h */
//	short regionid;		/* see above */
//
//	short is_modal;		/* modal map, not using operatornames */
//	short pad;
//
//	void *items;		/* struct EnumPropertyItem for now */
//} wmKeyMap;

/* wmKeyMap.flag */
public static final int KEYMAP_MODAL=				1;	/* modal map, not using operatornames */
public static final int KEYMAP_USER=					2;	/* user created keymap */
public static final int KEYMAP_EXPANDED=				4;
public static final int KEYMAP_CHILDREN_EXPANDED=	8;

//typedef struct wmKeyConfig {
//	struct wmKeyConfig *next, *prev;
//
//	char idname[64];		/* unique name */
//	char basename[64];		/* idname of configuration this is derives from, "" if none */
//	
//	ListBase keymaps;
//	int actkeymap, flag;
//} wmKeyConfig;

///* this one is the operator itself, stored in files for macros etc */
///* operator + operatortype should be able to redo entirely, but for different contextes */
//typedef struct wmOperator {
//	struct wmOperator *next, *prev;
//
//	/* saved */
//	char idname[64];			/* used to retrieve type pointer */
//	IDProperty *properties;		/* saved, user-settable properties */
//
//	/* runtime */
//	wmOperatorType *type;		/* operator type definition from idname */
//	void *customdata;			/* custom storage, only while operator runs */
//	struct PointerRNA *ptr;		/* rna pointer to access properties */
//	struct ReportList *reports;	/* errors and warnings storage */
//
//} wmOperator;

/* operator type exec(), invoke() modal(), return values */
public static final int OPERATOR_RUNNING_MODAL=	1;
public static final int OPERATOR_CANCELLED=		2;
public static final int OPERATOR_FINISHED=		4;
/* add this flag if the event should pass through */
public static final int OPERATOR_PASS_THROUGH=	8;


///* ************** wmEvent ************************ */
///* for read-only rna access, dont save this */
//
///* each event should have full modifier state */
///* event comes from eventmanager and from keymap */
//typedef struct wmEvent {
//	struct wmEvent *next, *prev;
//
//	short type;			/* event code itself (short, is also in keymap) */
//	short val;			/* press, release, scrollvalue */
//	short x, y;			/* mouse pointer position, screen coord */
//	short mval[2];		/* region mouse position, name convention pre 2.5 :) */
//	short prevx, prevy;	/* previous mouse pointer position */
//	short unicode;		/* future, ghost? */
//	char ascii;			/* from ghost */
//	char pad;
//
//	/* modifier states */
//	short shift, ctrl, alt, oskey;	/* oskey is apple or windowskey, value denotes order of pressed */
//	short keymodifier;				/* rawkey modifier */
//
//	short pad1;
//
//	/* keymap item, set by handler (weak?) */
//	const char *keymap_idname;
//
//	/* custom data */
//	short custom;		/* custom data type, stylus, 6dof, see wm_event_types.h */
//	short customdatafree;
//	int pad2;
//	void *customdata;	/* ascii, unicode, mouse coords, angles, vectors, dragdrop info */
//
//} wmEvent;
//
//typedef enum wmRadialControlMode {
//	WM_RADIALCONTROL_SIZE,
//	WM_RADIALCONTROL_STRENGTH,
//	WM_RADIALCONTROL_ANGLE
//} wmRadialControlMode;
//
//#endif /* DNA_WINDOWMANAGER_TYPES_H */
}
