
/*  mesh.c
 *
 *  
 * 
 * $Id: MeshUtil.java,v 1.2 2009/09/18 05:20:13 jladere Exp $
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
 * The Original Code is Copyright (C) 2001-2002 by NaN Holding BV.
 * All rights reserved.
 *
 * Contributor(s): Blender Foundation
 *
 * ***** END GPL LICENSE BLOCK *****
 */
package blender.blenkernel;

import static blender.blenkernel.Blender.G;
import blender.blenlib.Arithb;
import blender.blenlib.StringUtil;
import blender.blenlib.EditVertUtil.EditMesh;
import blender.makesdna.CustomDataTypes;
import blender.makesdna.DNA_ID;
import blender.makesdna.MeshTypes;
import blender.makesdna.ObjectTypes;
import blender.makesdna.sdna.BoundBox;
import blender.makesdna.sdna.CustomData;
import blender.makesdna.sdna.MCol;
import blender.makesdna.sdna.MDeformVert;
import blender.makesdna.sdna.MEdge;
import blender.makesdna.sdna.MFace;
import blender.makesdna.sdna.MSticky;
import blender.makesdna.sdna.MTFace;
import blender.makesdna.sdna.MVert;
import blender.makesdna.sdna.Mesh;
import blender.makesdna.sdna.bObject;

public class MeshUtil {

public static EditMesh BKE_mesh_get_editmesh(Mesh me)
{
	return (EditMesh)me.edit_mesh;
}

//void BKE_mesh_end_editmesh(Mesh *me, EditMesh *em)
//{
//}


public static void mesh_update_customdata_pointers(Mesh me)
{
	me.mvert = (MVert)CustomDataUtil.CustomData_get_layer(me.vdata, CustomDataTypes.CD_MVERT);
	me.dvert = (MDeformVert)CustomDataUtil.CustomData_get_layer(me.vdata, CustomDataTypes.CD_MDEFORMVERT);
	me.msticky = (MSticky)CustomDataUtil.CustomData_get_layer(me.vdata, CustomDataTypes.CD_MSTICKY);

	me.medge = (MEdge)CustomDataUtil.CustomData_get_layer(me.edata, CustomDataTypes.CD_MEDGE);

	me.mface = (MFace)CustomDataUtil.CustomData_get_layer(me.fdata, CustomDataTypes.CD_MFACE);
	me.mcol = (MCol)CustomDataUtil.CustomData_get_layer(me.fdata, CustomDataTypes.CD_MCOL);
	me.mtface = (MTFace)CustomDataUtil.CustomData_get_layer(me.fdata, CustomDataTypes.CD_MTFACE);
}

/* Note: unlinking is called when me.id.us is 0, question remains how
 * much unlinking of Library data in Mesh should be done... probably
 * we need a more generic method, like the expand() functions in
 * readfile.c */

public static void unlink_mesh(Mesh me)
{
	int a;

	if(me==null) return;

//	for(a=0; a<me.totcol; a++) {
//		if(me.mat[a]) me.mat[a].id.us--;
//		me.mat[a]= 0;
//	}

	if(me.key!=null) {
	   	me.key.id.us--;
		if (me.key.id.us == 0 && me.key.ipo!=null )
			me.key.ipo.id.us--;
	}
	me.key= null;

	if(me.texcomesh!=null) me.texcomesh= null;
}


/* do not free mesh itself */
public static void free_mesh(Mesh me)
{
	unlink_mesh(me);

//	if(me.pv!=null) {
////		if(me.pv.vert_map) MEM_freeN(me.pv.vert_map);
////		if(me.pv.edge_map) MEM_freeN(me.pv.edge_map);
////		if(me.pv.old_faces) MEM_freeN(me.pv.old_faces);
////		if(me.pv.old_edges) MEM_freeN(me.pv.old_edges);
//		me.totvert= me.pv.totvert;
//		me.totedge= me.pv.totedge;
//		me.totface= me.pv.totface;
////		MEM_freeN(me.pv);
//	}

//	CustomData_free(&me.vdata, me.totvert);
//	CustomData_free(&me.edata, me.totedge);
//	CustomData_free(&me.fdata, me.totface);

//	if(me.mat) MEM_freeN(me.mat);
//
//	if(me.bb) MEM_freeN(me.bb);
//	if(me.mselect) MEM_freeN(me.mselect);
//	if(me.edit_mesh) MEM_freeN(me.edit_mesh);
}

//void copy_dverts(MDeformVert *dst, MDeformVert *src, int copycount)
//{
//	/* Assumes dst is already set up */
//	int i;
//
//	if (!src || !dst)
//		return;
//
//	memcpy (dst, src, copycount * sizeof(MDeformVert));
//
//	for (i=0; i<copycount; i++){
//		if (src[i].dw){
//			dst[i].dw = MEM_callocN (sizeof(MDeformWeight)*src[i].totweight, "copy_deformWeight");
//			memcpy (dst[i].dw, src[i].dw, sizeof (MDeformWeight)*src[i].totweight);
//		}
//	}
//
//}
//
//void free_dverts(MDeformVert *dvert, int totvert)
//{
//	/* Instead of freeing the verts directly,
//	call this function to delete any special
//	vert data */
//	int	i;
//
//	if (!dvert)
//		return;
//
//	/* Free any special data from the verts */
//	for (i=0; i<totvert; i++){
//		if (dvert[i].dw) MEM_freeN (dvert[i].dw);
//	}
//	MEM_freeN (dvert);
//}

public static Mesh add_mesh(String name)
{
	Mesh me;

	me= (Mesh)LibraryUtil.alloc_libblock(G.main.mesh, DNA_ID.ID_ME, StringUtil.toCString(name),0);

	me.size[0]= me.size[1]= me.size[2]= 1.0f;
	me.smoothresh= 30;
	me.texflag= MeshTypes.AUTOSPACE;
	me.flag= MeshTypes.ME_TWOSIDED;
	me.bb= ObjectUtil.unit_boundbox();
	me.drawflag= MeshTypes.ME_DRAWEDGES|MeshTypes.ME_DRAWFACES|MeshTypes.ME_DRAWCREASES;

	return me;
}

//Mesh *copy_mesh(Mesh *me)
//{
//	Mesh *men;
//	MTFace *tface;
//	int a, i;
//
//	men= copy_libblock(me);
//
//	men.mat= MEM_dupallocN(me.mat);
//	for(a=0; a<men.totcol; a++) {
//		id_us_plus((ID *)men.mat[a]);
//	}
//	id_us_plus((ID *)men.texcomesh);
//
//	CustomData_copy(&me.vdata, &men.vdata, CD_MASK_MESH, CD_DUPLICATE, men.totvert);
//	CustomData_copy(&me.edata, &men.edata, CD_MASK_MESH, CD_DUPLICATE, men.totedge);
//	CustomData_copy(&me.fdata, &men.fdata, CD_MASK_MESH, CD_DUPLICATE, men.totface);
//	mesh_update_customdata_pointers(men);
//
//	/* ensure indirect linked data becomes lib-extern */
//	for(i=0; i<me.fdata.totlayer; i++) {
//		if(me.fdata.layers[i].type == CD_MTFACE) {
//			tface= (MTFace*)me.fdata.layers[i].data;
//
//			for(a=0; a<me.totface; a++, tface++)
//				if(tface.tpage)
//					id_lib_extern((ID*)tface.tpage);
//		}
//	}
//
//	men.mselect= NULL;
//
//	men.bb= MEM_dupallocN(men.bb);
//
//	men.key= copy_key(me.key);
//	if(men.key) men.key.from= (ID *)men;
//
//	return men;
//}
//
//void make_local_tface(Mesh *me)
//{
//	MTFace *tface;
//	Image *ima;
//	int a, i;
//
//	for(i=0; i<me.fdata.totlayer; i++) {
//		if(me.fdata.layers[i].type == CD_MTFACE) {
//			tface= (MTFace*)me.fdata.layers[i].data;
//
//			for(a=0; a<me.totface; a++, tface++) {
//				/* special case: ima always local immediately */
//				if(tface.tpage) {
//					ima= tface.tpage;
//					if(ima.id.lib) {
//						ima.id.lib= 0;
//						ima.id.flag= LIB_LOCAL;
//						new_id(0, (ID *)ima, 0);
//					}
//				}
//			}
//		}
//	}
//}
//
//void make_local_mesh(Mesh *me)
//{
//	Object *ob;
//	Mesh *men;
//	int local=0, lib=0;
//
//	/* - only lib users: do nothing
//	    * - only local users: set flag
//	    * - mixed: make copy
//	    */
//
//	if(me.id.lib==0) return;
//	if(me.id.us==1) {
//		me.id.lib= 0;
//		me.id.flag= LIB_LOCAL;
//		new_id(0, (ID *)me, 0);
//
//		if(me.mtface) make_local_tface(me);
//
//		return;
//	}
//
//	ob= G.main.object.first;
//	while(ob) {
//		if( me==get_mesh(ob) ) {
//			if(ob.id.lib) lib= 1;
//			else local= 1;
//		}
//		ob= ob.id.next;
//	}
//
//	if(local && lib==0) {
//		me.id.lib= 0;
//		me.id.flag= LIB_LOCAL;
//		new_id(0, (ID *)me, 0);
//
//		if(me.mtface) make_local_tface(me);
//
//	}
//	else if(local && lib) {
//		men= copy_mesh(me);
//		men.id.us= 0;
//
//		ob= G.main.object.first;
//		while(ob) {
//			if( me==get_mesh(ob) ) {
//				if(ob.id.lib==0) {
//					set_mesh(ob, men);
//				}
//			}
//			ob= ob.id.next;
//		}
//	}
//}

public static void boundbox_mesh(Mesh me, float[] loc, float[] size)
{
	MVert[] mverts;
        int mvert_p;
	BoundBox bb;
	float[] min=new float[3], max=new float[3];
	float[] mloc=new float[3], msize=new float[3];
	int a;

	if(me.bb==null) me.bb= new BoundBox();
	bb= me.bb;

	UtilDefines.INIT_MINMAX(min, max);

	if (loc==null) loc= mloc;
	if (size==null) size= msize;

	mverts= me.mvert.myarray;
        mvert_p = 0;
	for(a=0; a<me.totvert; a++, mvert_p++) {
		UtilDefines.DO_MINMAX(mverts[mvert_p].co, min, max);
	}

	if(me.totvert==0) {
		min[0] = min[1] = min[2] = -1.0f;
		max[0] = max[1] = max[2] = 1.0f;
	}

	loc[0]= (min[0]+max[0])/2.0f;
	loc[1]= (min[1]+max[1])/2.0f;
	loc[2]= (min[2]+max[2])/2.0f;

	size[0]= (max[0]-min[0])/2.0f;
	size[1]= (max[1]-min[1])/2.0f;
	size[2]= (max[2]-min[2])/2.0f;

	ObjectUtil.boundbox_set_from_min_max(bb, min, max);
}

public static void tex_space_mesh(Mesh me)
{
//	KeyBlock kb;
//	float *fp;
        float[] loc=new float[3], size=new float[3], min=new float[3], max=new float[3];
	int a;

	boundbox_mesh(me, loc, size);

	if((me.texflag & MeshTypes.AUTOSPACE)!=0) {
//		if(me.key) {
//			kb= me.key.refkey;
//			if (kb) {
//
//				INIT_MINMAX(min, max);
//
//				fp= kb.data;
//				for(a=0; a<kb.totelem; a++, fp+=3) {
//					DO_MINMAX(fp, min, max);
//				}
//				if(kb.totelem) {
//					loc[0]= (min[0]+max[0])/2.0f; loc[1]= (min[1]+max[1])/2.0f; loc[2]= (min[2]+max[2])/2.0f;
//					size[0]= (max[0]-min[0])/2.0f; size[1]= (max[1]-min[1])/2.0f; size[2]= (max[2]-min[2])/2.0f;
//				}
//				else {
//					loc[0]= loc[1]= loc[2]= 0.0;
//					size[0]= size[1]= size[2]= 0.0;
//				}
//
//			}
//		}

		for (a=0; a<3; a++) {
			if(size[a]==0.0f) size[a]= 1.0f;
			else if(size[a]>0.0f && size[a]<0.00001f) size[a]= 0.00001f;
			else if(size[a]<0.0f && size[a]> -0.00001f) size[a]= -0.00001f;
		}

		UtilDefines.VECCOPY(me.loc, loc);
		UtilDefines.VECCOPY(me.size, size);
		me.rot[0]= me.rot[1]= me.rot[2]= 0.0f;
	}
}

public static BoundBox mesh_get_bb(bObject ob)
{
	Mesh me= (Mesh)ob.data;

	if(ob.bb!=null)
		return ob.bb;

	if (me.bb==null)
		tex_space_mesh(me);

	return me.bb;
}

//void mesh_get_texspace(Mesh *me, float *loc_r, float *rot_r, float *size_r)
//{
//	if (!me.bb) {
//		tex_space_mesh(me);
//	}
//
//	if (loc_r) VECCOPY(loc_r, me.loc);
//	if (rot_r) VECCOPY(rot_r, me.rot);
//	if (size_r) VECCOPY(size_r, me.size);
//}
//
//float *get_mesh_orco_verts(Object *ob)
//{
//	Mesh *me = ob.data;
//	int a, totvert;
//	float (*vcos)[3] = NULL;
//
//	/* Get appropriate vertex coordinates */
//	if(me.key && me.texcomesh==0 && me.key.refkey) {
//		vcos= mesh_getRefKeyCos(me, &totvert);
//	}
//	else {
//		MVert *mvert = NULL;
//		Mesh *tme = me.texcomesh?me.texcomesh:me;
//
//		vcos = MEM_callocN(sizeof(*vcos)*me.totvert, "orco mesh");
//		mvert = tme.mvert;
//		totvert = MIN2(tme.totvert, me.totvert);
//
//		for(a=0; a<totvert; a++, mvert++) {
//			vcos[a][0]= mvert.co[0];
//			vcos[a][1]= mvert.co[1];
//			vcos[a][2]= mvert.co[2];
//		}
//	}
//
//	return (float*)vcos;
//}
//
//void transform_mesh_orco_verts(Mesh *me, float (*orco)[3], int totvert, int invert)
//{
//	float loc[3], size[3];
//	int a;
//
//	mesh_get_texspace(me.texcomesh?me.texcomesh:me, loc, NULL, size);
//
//	if(invert) {
//		for(a=0; a<totvert; a++) {
//			float *co = orco[a];
//			co[0] = co[0]*size[0] + loc[0];
//			co[1] = co[1]*size[1] + loc[1];
//			co[2] = co[2]*size[2] + loc[2];
//		}
//	}
//	else {
//		for(a=0; a<totvert; a++) {
//			float *co = orco[a];
//			co[0] = (co[0]-loc[0])/size[0];
//			co[1] = (co[1]-loc[1])/size[1];
//			co[2] = (co[2]-loc[2])/size[2];
//		}
//	}
//}

static int[] corner_indices_1203 = {1, 2, 0, 3};
static int[] corner_indices_2301 = {2, 3, 0, 1};
/* rotates the vertices of a face in case v[2] or v[3] (vertex index) is = 0.
   this is necessary to make the if(mface.v4) check for quads work */
public static int test_index_face(MFace mface, CustomData fdata, int mfindex, int nr)
{
	/* first test if the face is legal */
	if(mface.v3!=0 && mface.v3==mface.v4) {
		mface.v4= 0;
		nr--;
	}
	if(mface.v2!=0 && mface.v2==mface.v3) {
		mface.v3= mface.v4;
		mface.v4= 0;
		nr--;
	}
	if(mface.v1==mface.v2) {
		mface.v2= mface.v3;
		mface.v3= mface.v4;
		mface.v4= 0;
		nr--;
	}

	/* prevent a zero at wrong index location */
	if(nr==3) {
		if(mface.v3==0) {
//			static int corner_indices[4] = {1, 2, 0, 3};

//			SWAP(int, mface.v1, mface.v2);
                        int sw_ap=mface.v1; mface.v1=mface.v2; mface.v2=sw_ap;
//			SWAP(int, mface.v2, mface.v3);
                        sw_ap=mface.v2; mface.v2=mface.v3; mface.v3=sw_ap;

			if(fdata!=null)
				CustomDataUtil.CustomData_swap(fdata, mfindex, corner_indices_1203);
		}
	}
	else if(nr==4) {
		if(mface.v3==0 || mface.v4==0) {
//			static int corner_indices[4] = {2, 3, 0, 1};

//			SWAP(int, mface.v1, mface.v3);
                        int sw_ap=mface.v1; mface.v1=mface.v3; mface.v3=sw_ap;
//			SWAP(int, mface.v2, mface.v4);
                        sw_ap=mface.v2; mface.v2=mface.v4; mface.v4=sw_ap;

			if(fdata!=null)
				CustomDataUtil.CustomData_swap(fdata, mfindex, corner_indices_2301);
		}
	}

	return nr;
}

public static Mesh get_mesh(bObject ob)
{

	if(ob==null) return null;
	if(ob.type==ObjectTypes.OB_MESH) return (Mesh)ob.data;
	else return null;
}

//void set_mesh(Object *ob, Mesh *me)
//{
//	Mesh *old=0;
//
//	if(ob==0) return;
//
//	if(ob.type==OB_MESH) {
//		old= ob.data;
//		old.id.us--;
//		ob.data= me;
//		id_us_plus((ID *)me);
//	}
//
//	test_object_materials((ID *)me);
//}
//
///* ************** make edges in a Mesh, for outside of editmode */
//
//struct edgesort {
//	int v1, v2;
//	short is_loose, is_draw;
//};
//
///* edges have to be added with lowest index first for sorting */
//static void to_edgesort(struct edgesort *ed, int v1, int v2, short is_loose, short is_draw)
//{
//	if(v1<v2) {
//		ed.v1= v1; ed.v2= v2;
//	}
//	else {
//		ed.v1= v2; ed.v2= v1;
//	}
//	ed.is_loose= is_loose;
//	ed.is_draw= is_draw;
//}
//
//static int vergedgesort(const void *v1, const void *v2)
//{
//	const struct edgesort *x1=v1, *x2=v2;
//
//	if( x1.v1 > x2.v1) return 1;
//	else if( x1.v1 < x2.v1) return -1;
//	else if( x1.v2 > x2.v2) return 1;
//	else if( x1.v2 < x2.v2) return -1;
//
//	return 0;
//}
//
//void make_edges(Mesh *me, int old)
//{
//	MFace *mface;
//	MEdge *medge;
//	struct edgesort *edsort, *ed;
//	int a, totedge=0, final=0;
//
//	/* we put all edges in array, sort them, and detect doubles that way */
//
//	for(a= me.totface, mface= me.mface; a>0; a--, mface++) {
//		if(mface.v4) totedge+=4;
//		else if(mface.v3) totedge+=3;
//		else totedge+=1;
//	}
//
//	if(totedge==0) {
//		/* flag that mesh has edges */
//		me.medge = MEM_callocN(0, "make mesh edges");
//		me.totedge = 0;
//		return;
//	}
//
//	ed= edsort= MEM_mallocN(totedge*sizeof(struct edgesort), "edgesort");
//
//	for(a= me.totface, mface= me.mface; a>0; a--, mface++) {
//		to_edgesort(ed++, mface.v1, mface.v2, !mface.v3, mface.edcode & ME_V1V2);
//		if(mface.v4) {
//			to_edgesort(ed++, mface.v2, mface.v3, 0, mface.edcode & ME_V2V3);
//			to_edgesort(ed++, mface.v3, mface.v4, 0, mface.edcode & ME_V3V4);
//			to_edgesort(ed++, mface.v4, mface.v1, 0, mface.edcode & ME_V4V1);
//		}
//		else if(mface.v3) {
//			to_edgesort(ed++, mface.v2, mface.v3, 0, mface.edcode & ME_V2V3);
//			to_edgesort(ed++, mface.v3, mface.v1, 0, mface.edcode & ME_V3V1);
//		}
//	}
//
//	qsort(edsort, totedge, sizeof(struct edgesort), vergedgesort);
//
//	/* count final amount */
//	for(a=totedge, ed=edsort; a>1; a--, ed++) {
//		/* edge is unique when it differs from next edge, or is last */
//		if(ed.v1 != (ed+1).v1 || ed.v2 != (ed+1).v2) final++;
//	}
//	final++;
//
//
//	medge= CustomData_add_layer(&me.edata, CD_MEDGE, CD_CALLOC, NULL, final);
//	me.medge= medge;
//	me.totedge= final;
//
//	for(a=totedge, ed=edsort; a>1; a--, ed++) {
//		/* edge is unique when it differs from next edge, or is last */
//		if(ed.v1 != (ed+1).v1 || ed.v2 != (ed+1).v2) {
//			medge.v1= ed.v1;
//			medge.v2= ed.v2;
//			if(old==0 || ed.is_draw) medge.flag= ME_EDGEDRAW|ME_EDGERENDER;
//			if(ed.is_loose) medge.flag|= ME_LOOSEEDGE;
//			medge++;
//		}
//		else {
//			/* equal edge, we merge the drawflag */
//			(ed+1).is_draw |= ed.is_draw;
//		}
//	}
//	/* last edge */
//	medge.v1= ed.v1;
//	medge.v2= ed.v2;
//	medge.flag= ME_EDGEDRAW;
//	if(ed.is_loose) medge.flag|= ME_LOOSEEDGE;
//	medge.flag |= ME_EDGERENDER;
//
//	MEM_freeN(edsort);
//
//	mesh_strip_loose_faces(me);
//}
//
//void mesh_strip_loose_faces(Mesh *me)
//{
//	int a,b;
//
//	for (a=b=0; a<me.totface; a++) {
//		if (me.mface[a].v3) {
//			if (a!=b) {
//				memcpy(&me.mface[b],&me.mface[a],sizeof(me.mface[b]));
//				CustomData_copy_data(&me.fdata, &me.fdata, a, b, 1);
//				CustomData_free_elem(&me.fdata, a, 1);
//			}
//			b++;
//		}
//	}
//	me.totface = b;
//}
//
//
//void mball_to_mesh(ListBase *lb, Mesh *me)
//{
//	DispList *dl;
//	MVert *mvert;
//	MFace *mface;
//	float *nors, *verts;
//	int a, *index;
//
//	dl= lb.first;
//	if(dl==0) return;
//
//	if(dl.type==DL_INDEX4) {
//		me.flag= ME_NOPUNOFLIP;
//		me.totvert= dl.nr;
//		me.totface= dl.parts;
//
//		mvert= CustomData_add_layer(&me.vdata, CD_MVERT, CD_CALLOC, NULL, dl.nr);
//		mface= CustomData_add_layer(&me.fdata, CD_MFACE, CD_CALLOC, NULL, dl.parts);
//		me.mvert= mvert;
//		me.mface= mface;
//
//		a= dl.nr;
//		nors= dl.nors;
//		verts= dl.verts;
//		while(a--) {
//			VECCOPY(mvert.co, verts);
//			mvert.no[0]= (short int)(nors[0]*32767.0);
//			mvert.no[1]= (short int)(nors[1]*32767.0);
//			mvert.no[2]= (short int)(nors[2]*32767.0);
//			mvert++;
//			nors+= 3;
//			verts+= 3;
//		}
//
//		a= dl.parts;
//		index= dl.index;
//		while(a--) {
//			mface.v1= index[0];
//			mface.v2= index[1];
//			mface.v3= index[2];
//			mface.v4= index[3];
//			mface.flag= ME_SMOOTH;
//
//			test_index_face(mface, NULL, 0, (mface.v3==mface.v4)? 3: 4);
//
//			mface++;
//			index+= 4;
//		}
//
//		make_edges(me, 0);	// all edges
//	}
//}
//
///* this may fail replacing ob.data, be sure to check ob.type */
//void nurbs_to_mesh(Object *ob)
//{
//	Object *ob1;
//	DispList *dl;
//	Mesh *me;
//	Curve *cu;
//	MVert *mvert;
//	MFace *mface;
//	float *data;
//	int a, b, ofs, vertcount, startvert, totvert=0, totvlak=0;
//	int p1, p2, p3, p4, *index;
//
//	cu= ob.data;
//
//	/* count */
//	dl= cu.disp.first;
//	while(dl) {
//		if(dl.type==DL_SEGM) {
//			totvert+= dl.parts*dl.nr;
//			totvlak+= dl.parts*(dl.nr-1);
//		}
//		else if(dl.type==DL_POLY) {
//			/* cyclic polys are filled. except when 3D */
//			if(cu.flag & CU_3D) {
//				totvert+= dl.parts*dl.nr;
//				totvlak+= dl.parts*dl.nr;
//			}
//		}
//		else if(dl.type==DL_SURF) {
//			totvert+= dl.parts*dl.nr;
//			totvlak+= (dl.parts-1+((dl.flag & DL_CYCL_V)==2))*(dl.nr-1+(dl.flag & DL_CYCL_U));
//		}
//		else if(dl.type==DL_INDEX3) {
//			totvert+= dl.nr;
//			totvlak+= dl.parts;
//		}
//		dl= dl.next;
//	}
//	if(totvert==0) {
//		/* error("can't convert"); */
//		/* Make Sure you check ob.data is a curve */
//		return;
//	}
//
//	/* make mesh */
//	me= add_mesh("Mesh");
//	me.totvert= totvert;
//	me.totface= totvlak;
//
//	me.totcol= cu.totcol;
//	me.mat= cu.mat;
//	cu.mat= 0;
//	cu.totcol= 0;
//
//	mvert= CustomData_add_layer(&me.vdata, CD_MVERT, CD_CALLOC, NULL, me.totvert);
//	mface= CustomData_add_layer(&me.fdata, CD_MFACE, CD_CALLOC, NULL, me.totface);
//	me.mvert= mvert;
//	me.mface= mface;
//
//	/* verts and faces */
//	vertcount= 0;
//
//	dl= cu.disp.first;
//	while(dl) {
//		int smooth= dl.rt & CU_SMOOTH ? 1 : 0;
//
//		if(dl.type==DL_SEGM) {
//			startvert= vertcount;
//			a= dl.parts*dl.nr;
//			data= dl.verts;
//			while(a--) {
//				VECCOPY(mvert.co, data);
//				data+=3;
//				vertcount++;
//				mvert++;
//			}
//
//			for(a=0; a<dl.parts; a++) {
//				ofs= a*dl.nr;
//				for(b=1; b<dl.nr; b++) {
//					mface.v1= startvert+ofs+b-1;
//					mface.v2= startvert+ofs+b;
//					if(smooth) mface.flag |= ME_SMOOTH;
//					mface++;
//				}
//			}
//
//		}
//		else if(dl.type==DL_POLY) {
//			/* 3d polys are not filled */
//			if(cu.flag & CU_3D) {
//				startvert= vertcount;
//				a= dl.parts*dl.nr;
//				data= dl.verts;
//				while(a--) {
//					VECCOPY(mvert.co, data);
//					data+=3;
//					vertcount++;
//					mvert++;
//				}
//
//				for(a=0; a<dl.parts; a++) {
//					ofs= a*dl.nr;
//					for(b=0; b<dl.nr; b++) {
//						mface.v1= startvert+ofs+b;
//						if(b==dl.nr-1) mface.v2= startvert+ofs;
//						else mface.v2= startvert+ofs+b+1;
//						if(smooth) mface.flag |= ME_SMOOTH;
//						mface++;
//					}
//				}
//			}
//		}
//		else if(dl.type==DL_INDEX3) {
//			startvert= vertcount;
//			a= dl.nr;
//			data= dl.verts;
//			while(a--) {
//				VECCOPY(mvert.co, data);
//				data+=3;
//				vertcount++;
//				mvert++;
//			}
//
//			a= dl.parts;
//			index= dl.index;
//			while(a--) {
//				mface.v1= startvert+index[0];
//				mface.v2= startvert+index[2];
//				mface.v3= startvert+index[1];
//				mface.v4= 0;
//				test_index_face(mface, NULL, 0, 3);
//
//				if(smooth) mface.flag |= ME_SMOOTH;
//				mface++;
//				index+= 3;
//			}
//
//
//		}
//		else if(dl.type==DL_SURF) {
//			startvert= vertcount;
//			a= dl.parts*dl.nr;
//			data= dl.verts;
//			while(a--) {
//				VECCOPY(mvert.co, data);
//				data+=3;
//				vertcount++;
//				mvert++;
//			}
//
//			for(a=0; a<dl.parts; a++) {
//
//				if( (dl.flag & DL_CYCL_V)==0 && a==dl.parts-1) break;
//
//				if(dl.flag & DL_CYCL_U) {			/* p2 . p1 . */
//					p1= startvert+ dl.nr*a;	/* p4 . p3 . */
//					p2= p1+ dl.nr-1;		/* ----. next row */
//					p3= p1+ dl.nr;
//					p4= p2+ dl.nr;
//					b= 0;
//				}
//				else {
//					p2= startvert+ dl.nr*a;
//					p1= p2+1;
//					p4= p2+ dl.nr;
//					p3= p1+ dl.nr;
//					b= 1;
//				}
//				if( (dl.flag & DL_CYCL_V) && a==dl.parts-1) {
//					p3-= dl.parts*dl.nr;
//					p4-= dl.parts*dl.nr;
//				}
//
//				for(; b<dl.nr; b++) {
//					mface.v1= p1;
//					mface.v2= p3;
//					mface.v3= p4;
//					mface.v4= p2;
//					mface.mat_nr= (unsigned char)dl.col;
//					test_index_face(mface, NULL, 0, 4);
//
//					if(smooth) mface.flag |= ME_SMOOTH;
//					mface++;
//
//					p4= p3;
//					p3++;
//					p2= p1;
//					p1++;
//				}
//			}
//
//		}
//
//		dl= dl.next;
//	}
//
//	make_edges(me, 0);	// all edges
//	mesh_calc_normals(me.mvert, me.totvert, me.mface, me.totface, NULL);
//
//	if(ob.data) {
//		free_libblock(&G.main.curve, ob.data);
//	}
//	ob.data= me;
//	ob.type= OB_MESH;
//
//	/* other users */
//	ob1= G.main.object.first;
//	while(ob1) {
//		if(ob1.data==cu) {
//			ob1.type= OB_MESH;
//
//			ob1.data= ob.data;
//			id_us_plus((ID *)ob.data);
//		}
//		ob1= ob1.id.next;
//	}
//
//}
//
//void mesh_delete_material_index(Mesh *me, int index)
//{
//	int i;
//
//	for (i=0; i<me.totface; i++) {
//		MFace *mf = &((MFace*) me.mface)[i];
//		if (mf.mat_nr && mf.mat_nr>=index)
//			mf.mat_nr--;
//	}
//}
//
//void mesh_set_smooth_flag(Object *meshOb, int enableSmooth)
//{
//	Mesh *me = meshOb.data;
//	int i;
//
//	for (i=0; i<me.totface; i++) {
//		MFace *mf = &((MFace*) me.mface)[i];
//
//		if (enableSmooth) {
//			mf.flag |= ME_SMOOTH;
//		} else {
//			mf.flag &= ~ME_SMOOTH;
//		}
//	}
//
//// XXX do this in caller	DAG_object_flush_update(scene, meshOb, OB_RECALC_DATA);
//}

public static void mesh_calc_normals(MVert[] mverts, int numVerts, MFace[] mfaces, int numFaces, float[][][] faceNors_r)
{
	float[][] tnorms= new float[numVerts][3];
	float[][] fnors= new float[numFaces][3];
	int i;

	for (i=0; i<numFaces; i++) {
		MFace mf= mfaces[i];
		float[] f_no= fnors[i];

		if (mf.v4!=0)
			Arithb.CalcNormFloat4(mverts[mf.v1].co, mverts[mf.v2].co, mverts[mf.v3].co, mverts[mf.v4].co, f_no);
		else
			Arithb.CalcNormFloat(mverts[mf.v1].co, mverts[mf.v2].co, mverts[mf.v3].co, f_no);

		Arithb.VecAddf(tnorms[mf.v1], tnorms[mf.v1], f_no);
		Arithb.VecAddf(tnorms[mf.v2], tnorms[mf.v2], f_no);
		Arithb.VecAddf(tnorms[mf.v3], tnorms[mf.v3], f_no);
		if (mf.v4!=0)
			Arithb.VecAddf(tnorms[mf.v4], tnorms[mf.v4], f_no);
	}
	for (i=0; i<numVerts; i++) {
		MVert mv= mverts[i];
		float[] no= tnorms[i];

		if (Arithb.Normalize(no)==0.0f) {
			UtilDefines.VECCOPY(no, mv.co);
			Arithb.Normalize(no);
		}

		mv.no[0]= (short)(no[0]*32767.0f);
		mv.no[1]= (short)(no[1]*32767.0f);
		mv.no[2]= (short)(no[2]*32767.0f);
	}

//	MEM_freeN(tnorms);

	if (faceNors_r!=null) {
		faceNors_r[0] = fnors;
	} else {
//		MEM_freeN(fnors);
	}
}

//float (*mesh_getVertexCos(Mesh *me, int *numVerts_r))[3]
//{
//	int i, numVerts = me.totvert;
//	float (*cos)[3] = MEM_mallocN(sizeof(*cos)*numVerts, "vertexcos1");
//
//	if (numVerts_r) *numVerts_r = numVerts;
//	for (i=0; i<numVerts; i++)
//		VECCOPY(cos[i], me.mvert[i].co);
//
//	return cos;
//}
//
//float (*mesh_getRefKeyCos(Mesh *me, int *numVerts_r))[3]
//{
//	KeyBlock *kb;
//	float (*cos)[3] = NULL;
//	int totvert;
//
//	if(me.key && me.key.refkey) {
//		if(numVerts_r) *numVerts_r= me.totvert;
//
//		kb= me.key.refkey;
//
//		/* prevent accessing invalid memory */
//		if (me.totvert > kb.totelem)		cos= MEM_callocN(sizeof(*cos)*me.totvert, "vertexcos1");
//		else								cos= MEM_mallocN(sizeof(*cos)*me.totvert, "vertexcos1");
//
//		totvert= MIN2(kb.totelem, me.totvert);
//
//		memcpy(cos, kb.data, sizeof(*cos)*totvert);
//	}
//
//	return cos;
//}
//
//UvVertMap *make_uv_vert_map(struct MFace *mface, struct MTFace *tface, unsigned int totface, unsigned int totvert, int selected, float *limit)
//{
//	UvVertMap *vmap;
//	UvMapVert *buf;
//	MFace *mf;
//	MTFace *tf;
//	unsigned int a;
//	int	i, totuv, nverts;
//
//	totuv = 0;
//
//	/* generate UvMapVert array */
//	mf= mface;
//	tf= tface;
//	for(a=0; a<totface; a++, mf++, tf++)
//		if(!selected || (!(mf.flag & ME_HIDE) && (mf.flag & ME_FACE_SEL)))
//			totuv += (mf.v4)? 4: 3;
//
//	if(totuv==0)
//		return NULL;
//
//	vmap= (UvVertMap*)MEM_callocN(sizeof(*vmap), "UvVertMap");
//	if (!vmap)
//		return NULL;
//
//	vmap.vert= (UvMapVert**)MEM_callocN(sizeof(*vmap.vert)*totvert, "UvMapVert*");
//	buf= vmap.buf= (UvMapVert*)MEM_callocN(sizeof(*vmap.buf)*totuv, "UvMapVert");
//
//	if (!vmap.vert || !vmap.buf) {
//		free_uv_vert_map(vmap);
//		return NULL;
//	}
//
//	mf= mface;
//	tf= tface;
//	for(a=0; a<totface; a++, mf++, tf++) {
//		if(!selected || (!(mf.flag & ME_HIDE) && (mf.flag & ME_FACE_SEL))) {
//			nverts= (mf.v4)? 4: 3;
//
//			for(i=0; i<nverts; i++) {
//				buf.tfindex= i;
//				buf.f= a;
//				buf.separate = 0;
//				buf.next= vmap.vert[*(&mf.v1 + i)];
//				vmap.vert[*(&mf.v1 + i)]= buf;
//				buf++;
//			}
//		}
//	}
//
//	/* sort individual uvs for each vert */
//	tf= tface;
//	for(a=0; a<totvert; a++) {
//		UvMapVert *newvlist= NULL, *vlist=vmap.vert[a];
//		UvMapVert *iterv, *v, *lastv, *next;
//		float *uv, *uv2, uvdiff[2];
//
//		while(vlist) {
//			v= vlist;
//			vlist= vlist.next;
//			v.next= newvlist;
//			newvlist= v;
//
//			uv= (tf+v.f).uv[v.tfindex];
//			lastv= NULL;
//			iterv= vlist;
//
//			while(iterv) {
//				next= iterv.next;
//
//				uv2= (tf+iterv.f).uv[iterv.tfindex];
//				Vec2Subf(uvdiff, uv2, uv);
//
//
//				if(fabs(uv[0]-uv2[0]) < limit[0] && fabs(uv[1]-uv2[1]) < limit[1]) {
//					if(lastv) lastv.next= next;
//					else vlist= next;
//					iterv.next= newvlist;
//					newvlist= iterv;
//				}
//				else
//					lastv=iterv;
//
//				iterv= next;
//			}
//
//			newvlist.separate = 1;
//		}
//
//		vmap.vert[a]= newvlist;
//	}
//
//	return vmap;
//}
//
//UvMapVert *get_uv_map_vert(UvVertMap *vmap, unsigned int v)
//{
//	return vmap.vert[v];
//}
//
//void free_uv_vert_map(UvVertMap *vmap)
//{
//	if (vmap) {
//		if (vmap.vert) MEM_freeN(vmap.vert);
//		if (vmap.buf) MEM_freeN(vmap.buf);
//		MEM_freeN(vmap);
//	}
//}
//
///* Generates a map where the key is the vertex and the value is a list
//   of faces that use that vertex as a corner. The lists are allocated
//   from one memory pool. */
//void create_vert_face_map(ListBase **map, IndexNode **mem, const MFace *mface, const int totvert, const int totface)
//{
//	int i,j;
//	IndexNode *node = NULL;
//
//	(*map) = MEM_callocN(sizeof(ListBase) * totvert, "vert face map");
//	(*mem) = MEM_callocN(sizeof(IndexNode) * totface*4, "vert face map mem");
//	node = *mem;
//
//	/* Find the users */
//	for(i = 0; i < totface; ++i){
//		for(j = 0; j < (mface[i].v4?4:3); ++j, ++node) {
//			node.index = i;
//			BLI_addtail(&(*map)[((unsigned int*)(&mface[i]))[j]], node);
//		}
//	}
//}
//
///* Generates a map where the key is the vertex and the value is a list
//   of edges that use that vertex as an endpoint. The lists are allocated
//   from one memory pool. */
//void create_vert_edge_map(ListBase **map, IndexNode **mem, const MEdge *medge, const int totvert, const int totedge)
//{
//	int i, j;
//	IndexNode *node = NULL;
//
//	(*map) = MEM_callocN(sizeof(ListBase) * totvert, "vert edge map");
//	(*mem) = MEM_callocN(sizeof(IndexNode) * totedge * 2, "vert edge map mem");
//	node = *mem;
//
//	/* Find the users */
//	for(i = 0; i < totedge; ++i){
//		for(j = 0; j < 2; ++j, ++node) {
//			node.index = i;
//			BLI_addtail(&(*map)[((unsigned int*)(&medge[i].v1))[j]], node);
//		}
//	}
//}
//
///* Partial Mesh Visibility */
//PartialVisibility *mesh_pmv_copy(PartialVisibility *pmv)
//{
//	PartialVisibility *n= MEM_dupallocN(pmv);
//	n.vert_map= MEM_dupallocN(pmv.vert_map);
//	n.edge_map= MEM_dupallocN(pmv.edge_map);
//	n.old_edges= MEM_dupallocN(pmv.old_edges);
//	n.old_faces= MEM_dupallocN(pmv.old_faces);
//	return n;
//}
//
//void mesh_pmv_free(PartialVisibility *pv)
//{
//	MEM_freeN(pv.vert_map);
//	MEM_freeN(pv.edge_map);
//	MEM_freeN(pv.old_faces);
//	MEM_freeN(pv.old_edges);
//	MEM_freeN(pv);
//}
//
//void mesh_pmv_revert(Object *ob, Mesh *me)
//{
//	if(me.pv) {
//		unsigned i;
//		MVert *nve, *old_verts;
//
//		/* Reorder vertices */
//		nve= me.mvert;
//		old_verts = MEM_mallocN(sizeof(MVert)*me.pv.totvert,"PMV revert verts");
//		for(i=0; i<me.pv.totvert; ++i)
//			old_verts[i]= nve[me.pv.vert_map[i]];
//
//		/* Restore verts, edges and faces */
//		CustomData_free_layer_active(&me.vdata, CD_MVERT, me.totvert);
//		CustomData_free_layer_active(&me.edata, CD_MEDGE, me.totedge);
//		CustomData_free_layer_active(&me.fdata, CD_MFACE, me.totface);
//
//		CustomData_add_layer(&me.vdata, CD_MVERT, CD_ASSIGN, old_verts, me.pv.totvert);
//		CustomData_add_layer(&me.edata, CD_MEDGE, CD_ASSIGN, me.pv.old_edges, me.pv.totedge);
//		CustomData_add_layer(&me.fdata, CD_MFACE, CD_ASSIGN, me.pv.old_faces, me.pv.totface);
//		mesh_update_customdata_pointers(me);
//
//		me.totvert= me.pv.totvert;
//		me.totedge= me.pv.totedge;
//		me.totface= me.pv.totface;
//
//		me.pv.old_edges= NULL;
//		me.pv.old_faces= NULL;
//
//		/* Free maps */
//		MEM_freeN(me.pv.edge_map);
//		me.pv.edge_map= NULL;
//		MEM_freeN(me.pv.vert_map);
//		me.pv.vert_map= NULL;
//
//// XXX do this in caller		DAG_object_flush_update(scene, ob, OB_RECALC_DATA);
//	}
//}
//
//void mesh_pmv_off(Object *ob, Mesh *me)
//{
//	if(ob && me.pv) {
//		mesh_pmv_revert(ob, me);
//		MEM_freeN(me.pv);
//		me.pv= NULL;
//	}
//}
}