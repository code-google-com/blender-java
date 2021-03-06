/**
 * $Id: TransformInput.java,v 1.1 2009/09/18 05:15:10 jladere Exp $
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
 * Contributor(s): none yet.
 *
 * ***** END GPL LICENSE BLOCK *****
 */
package blender.editors.transform;

//#include <stdlib.h>

import blender.blenlib.Arithb;
import blender.editors.transform.Transform.MouseInput;
import blender.editors.transform.Transform.MouseInputMode;
import blender.editors.transform.Transform.TransInfo;
import blender.windowmanager.WmEventTypes;
import blender.windowmanager.WmTypes;
import blender.windowmanager.WmTypes.wmEvent;

//#include <math.h>
//
//#include "DNA_screen_types.h"
//#include "DNA_windowmanager_types.h"
//
//#include "BLI_arithb.h"
//
//#include "WM_types.h"
//
//#include "transform.h"
//

public class TransformInput {


/* ************************** INPUT FROM MOUSE *************************** */

public static MouseInput.Apply InputVector = new MouseInput.Apply() {
public void run(TransInfo t, MouseInput mi, short[] mval, float[] output)
//void InputVector(TransInfo *t, MouseInput *mi, short mval[2], float output[3])
{
	float[] vec = new float[3], dvec = new float[3];
	if(mi.precision!=0)
	{
		/* calculate the main translation and the precise one separate */
		Transform.convertViewVec(t, dvec, (short)(mval[0] - mi.precision_mval[0]), (short)(mval[1] - mi.precision_mval[1]));
		Arithb.VecMulf(dvec, 0.1f);
		Transform.convertViewVec(t, vec, (short)(mi.precision_mval[0] - t.imval[0]), (short)(mi.precision_mval[1] - t.imval[1]));
		Arithb.VecAddf(output, vec, dvec);
	}
	else
	{
		Transform.convertViewVec(t, output, (short)(mval[0] - t.imval[0]), (short)(mval[1] - t.imval[1]));
	}
}};

public static MouseInput.Apply InputSpring = new MouseInput.Apply() {
public void run(TransInfo t, MouseInput mi, short[] mval, float[] output)
//void InputSpring(TransInfo *t, MouseInput *mi, short mval[2], float output[3])
{
	float ratio, precise_ratio, dx, dy;
	if(mi.precision!=0)
	{
		/* calculate ratio for shiftkey pos, and for total, and blend these for precision */
		dx = (float)(mi.center[0] - mi.precision_mval[0]);
		dy = (float)(mi.center[1] - mi.precision_mval[1]);
		ratio = (float)StrictMath.sqrt( dx*dx + dy*dy);

		dx= (float)(mi.center[0] - mval[0]);
		dy= (float)(mi.center[1] - mval[1]);
		precise_ratio = (float)StrictMath.sqrt( dx*dx + dy*dy);

		ratio = (ratio + (precise_ratio - ratio) / 10.0f) / mi.factor;
	}
	else
	{
		dx = (float)(mi.center[0] - mval[0]);
		dy = (float)(mi.center[1] - mval[1]);
		ratio = (float)StrictMath.sqrt( dx*dx + dy*dy) / mi.factor;
	}

	output[0] = ratio;
}};

public static MouseInput.Apply InputSpringFlip = new MouseInput.Apply() {
public void run(TransInfo t, MouseInput mi, short[] mval, float[] output)
//void InputSpringFlip(TransInfo *t, MouseInput *mi, short mval[2], float output[3])
{
	InputSpring.run(t, mi, mval, output);

	/* flip scale */
	if	((mi.center[0] - mval[0]) * (mi.center[0] - mi.imval[0]) +
		 (mi.center[1] - mval[1]) * (mi.center[1] - mi.imval[1]) < 0)
	 {
		output[0] *= -1.0f;
	 }
}};

public static MouseInput.Apply InputTrackBall = new MouseInput.Apply() {
public void run(TransInfo t, MouseInput mi, short[] mval, float[] output)
//void InputTrackBall(TransInfo *t, MouseInput *mi, short mval[2], float output[3])
{
	if(mi.precision!=0)
	{
		output[0] = ( mi.imval[1] - mi.precision_mval[1] ) + ( mi.precision_mval[1] - mval[1] ) * 0.1f;
		output[1] = ( mi.precision_mval[0] - mi.imval[0] ) + ( mval[0] - mi.precision_mval[0] ) * 0.1f;
	}
	else
	{
		output[0] = (float)( mi.imval[1] - mval[1] );
		output[1] = (float)( mval[0] - mi.imval[0] );
	}

	output[0] *= mi.factor;
	output[1] *= mi.factor;
}};

public static MouseInput.Apply InputHorizontalRatio = new MouseInput.Apply() {
public void run(TransInfo t, MouseInput mi, short[] mval, float[] output)
//void InputHorizontalRatio(TransInfo *t, MouseInput *mi, short mval[2], float output[3])
{
	float x, pad;

	pad = t.ar.winx / 10;

	if (mi.precision!=0)
	{
		/* deal with Shift key by adding motion / 10 to motion before shift press */
		x = mi.precision_mval[0] + (float)(mval[0] - mi.precision_mval[0]) / 10.0f;
	}
	else {
		x = mval[0];
	}

	output[0] = (x - pad) / (t.ar.winx - 2 * pad);
}};

public static MouseInput.Apply InputHorizontalAbsolute = new MouseInput.Apply() {
public void run(TransInfo t, MouseInput mi, short[] mval, float[] output)
//void InputHorizontalAbsolute(TransInfo *t, MouseInput *mi, short mval[2], float output[3])
{
	float[] vec = new float[3];

	InputVector.run(t, mi, mval, vec);
	Arithb.Projf(vec, vec, t.viewinv[0]);

	output[0] = Arithb.Inpf(t.viewinv[0], vec) * 2.0f;
}};

public static MouseInput.Apply InputVerticalRatio = new MouseInput.Apply() {
public void run(TransInfo t, MouseInput mi, short[] mval, float[] output)
//void InputVerticalRatio(TransInfo *t, MouseInput *mi, short mval[2], float output[3])
{
	float y, pad;

	pad = t.ar.winy / 10;

	if (mi.precision!=0) {
		/* deal with Shift key by adding motion / 10 to motion before shift press */
		y = mi.precision_mval[1] + (float)(mval[1] - mi.precision_mval[1]) / 10.0f;
	}
	else {
		y = mval[0];
	}

	output[0] = (y - pad) / (t.ar.winy - 2 * pad);
}};

public static MouseInput.Apply InputVerticalAbsolute = new MouseInput.Apply() {
public void run(TransInfo t, MouseInput mi, short[] mval, float[] output)
//void InputVerticalAbsolute(TransInfo *t, MouseInput *mi, short mval[2], float output[3])
{
	float[] vec = new float[3];

	InputVector.run(t, mi, mval, vec);
	Arithb.Projf(vec, vec, t.viewinv[1]);

	output[0] = Arithb.Inpf(t.viewinv[1], vec) * 2.0f;
}};

public static MouseInput.Apply InputAngle = new MouseInput.Apply() {
public void run(TransInfo t, MouseInput mi, short[] mval, float[] output)
//void InputAngle(TransInfo *t, MouseInput *mi, short mval[2], float output[3])
{
	double dx2 = mval[0] - mi.center[0];
	double dy2 = mval[1] - mi.center[1];
	double B = StrictMath.sqrt(dx2*dx2+dy2*dy2);

	double dx1 = mi.imval[0] - mi.center[0];
	double dy1 = mi.imval[1] - mi.center[1];
	double A = StrictMath.sqrt(dx1*dx1+dy1*dy1);

	double dx3 = mval[0] - mi.imval[0];
	double dy3 = mval[1] - mi.imval[1];

	/* use doubles here, to make sure a "1.0" (no rotation) doesnt become 9.999999e-01, which gives 0.02 for acos */
	double deler = ((dx1*dx1+dy1*dy1)+(dx2*dx2+dy2*dy2)-(dx3*dx3+dy3*dy3))
		/ (2.0 * (A*B!=0.0?A*B:1.0));
	/* (A*B?A*B:1.0f) this takes care of potential divide by zero errors */

	float dphi;

	dphi = Arithb.saacos((float)deler);
	if( (dx1*dy2-dx2*dy1)>0.0 ) dphi= -dphi;

	/* If the angle is zero, because of lack of precision close to the 1.0 value in acos
	 * approximate the angle with the oposite side of the normalized triangle
	 * This is a good approximation here since the smallest acos value seems to be around
	 * 0.02 degree and lower values don't even have a 0.01% error compared to the approximation
	 * */
	if (dphi == 0)
	{
		double dx, dy;

		dx2 /= A;
		dy2 /= A;

		dx1 /= B;
		dy1 /= B;

		dx = dx1 - dx2;
		dy = dy1 - dy2;

		dphi = (float)StrictMath.sqrt(dx*dx + dy*dy);
		if( (dx1*dy2-dx2*dy1)>0.0 ) dphi= -dphi;
	}

	if(mi.precision!=0) dphi = dphi/30.0f;

	/* if no delta angle, don't update initial position */
	if (dphi != 0)
	{
		mi.imval[0] = mval[0];
		mi.imval[1] = mval[1];
	}

	output[0] += dphi;
}};

public static void initMouseInput(TransInfo t, MouseInput mi, int[] center, short[] mval)
{
	mi.factor = 0;
	mi.precision = 0;

	mi.center[0] = center[0];
	mi.center[1] = center[1];

	mi.imval[0] = mval[0];
	mi.imval[1] = mval[1];
}

static void calcSpringFactor(MouseInput mi)
{
	mi.factor = (float)StrictMath.sqrt(
		(
			((float)(mi.center[1] - mi.imval[1]))*((float)(mi.center[1] - mi.imval[1]))
		+
			((float)(mi.center[0] - mi.imval[0]))*((float)(mi.center[0] - mi.imval[0]))
		) );

	if (mi.factor==0.0f)
		mi.factor= 1.0f; /* prevent Inf */
}

public static void initMouseInputMode(TransInfo t, MouseInput mi, MouseInputMode mode)
{

	switch(mode)
	{
	case INPUT_VECTOR:
		mi.apply = InputVector;
//		t.helpline = HLP_NONE;
		break;
	case INPUT_SPRING:
		calcSpringFactor(mi);
		mi.apply = InputSpring;
//		t.helpline = HLP_SPRING;
		break;
	case INPUT_SPRING_FLIP:
		calcSpringFactor(mi);
		mi.apply = InputSpringFlip;
//		t.helpline = HLP_SPRING;
		break;
	case INPUT_ANGLE:
		mi.apply = InputAngle;
//		t.helpline = HLP_ANGLE;
		break;
	case INPUT_TRACKBALL:
		/* factor has to become setting or so */
		mi.factor = 0.01f;
		mi.apply = InputTrackBall;
//		t.helpline = HLP_TRACKBALL;
		break;
	case INPUT_HORIZONTAL_RATIO:
		mi.factor = (float)(mi.center[0] - mi.imval[0]);
		mi.apply = InputHorizontalRatio;
//		t.helpline = HLP_HARROW;
		break;
	case INPUT_HORIZONTAL_ABSOLUTE:
		mi.apply = InputHorizontalAbsolute;
//		t.helpline = HLP_HARROW;
		break;
	case INPUT_VERTICAL_RATIO:
		mi.apply = InputVerticalRatio;
//		t.helpline = HLP_VARROW;
		break;
	case INPUT_VERTICAL_ABSOLUTE:
		mi.apply = InputVerticalAbsolute;
//		t.helpline = HLP_VARROW;
		break;
	case INPUT_NONE:
	default:
		mi.apply = null;
		break;
	}

	/* bootstrap mouse input with initial values */
	applyMouseInput(t, mi, mi.imval, t.values);
}

public static void applyMouseInput(TransInfo t, MouseInput mi, short[] mval, float[] output)
{
	if (mi.apply != null)
	{
		mi.apply.run(t, mi, mval, output);
	}
//        System.out.println("applyMouseInput in "+Arrays.toString(mval) + " out " + Arrays.toString(output));
}

public static int handleMouseInput(TransInfo t, MouseInput mi, wmEvent event)
{
	int redraw = 0;

	switch (event.type)
	{
	case WmEventTypes.LEFTSHIFTKEY:
	case WmEventTypes.RIGHTSHIFTKEY:
		if (event.val==WmTypes.KM_PRESS)
		{
			t.modifiers |= Transform.MOD_PRECISION;
			/* shift is modifier for higher precision transform
			 * store the mouse position where the normal movement ended */
			mi.precision_mval[0] = (short)(event.x - t.ar.winrct.xmin);
			mi.precision_mval[1] = (short)(event.y - t.ar.winrct.ymin);
			mi.precision = 1;
		}
		else
		{
			t.modifiers &= ~Transform.MOD_PRECISION;
			mi.precision = 0;
		}
		redraw = 1;
		break;
	}

	return redraw;
}

}