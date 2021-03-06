package blender.makesdna;

public class OutlinerTypes {
//	/**
//	 * $Id: DNA_outliner_types.h 26841 2010-02-12 13:34:04Z campbellbarton $ 
//	 *
//	 * ***** BEGIN GPL LICENSE BLOCK *****
//	 *
//	 * This program is free software; you can redistribute it and/or
//	 * modify it under the terms of the GNU General Public License
//	 * as published by the Free Software Foundation; either version 2
//	 * of the License, or (at your option) any later version.
//	 *
//	 * This program is distributed in the hope that it will be useful,
//	 * but WITHOUT ANY WARRANTY; without even the implied warranty of
//	 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	 * GNU General Public License for more details.
//	 *
//	 * You should have received a copy of the GNU General Public License
//	 * along with this program; if not, write to the Free Software Foundation,
//	 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
//	 *
//	 * The Original Code is Copyright (C) 2001-2002 by NaN Holding BV.
//	 * All rights reserved.
//	 *
//	 * The Original Code is: all of this file.
//	 *
//	 * Contributor(s): none yet.
//	 *
//	 * ***** END GPL LICENSE BLOCK *****
//	 */
//	#ifndef DNA_OUTLINER_TYPES_H
//	#define DNA_OUTLINER_TYPES_H
//
//	#include "DNA_listBase.h"
//
//	struct ID;
//
//	typedef struct TreeStoreElem {
//		short type, nr, flag, used;
//		struct ID *id;
//	} TreeStoreElem;
//
//	typedef struct TreeStore {
//		int totelem, usedelem;
//		TreeStoreElem *data;
//	} TreeStore;

	/* TreeStoreElem->flag */
	public static final int TSE_CLOSED=		1;
	public static final int TSE_SELECTED=	2;
	public static final int TSE_TEXTBUT=		4;

//	/* TreeStoreElem types in BIF_outliner.h */
//
//	#endif

}
