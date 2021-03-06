package blender.makesrna.srna;

import org.python.core.PyObject;
import blender.blenkernel.ScreenUtil.Menu;
import blender.python.BpyRna.BPy_StructRNA;

import blender.makesdna.sdna.*;

import static blender.makesrna.RnaRna.*;
import static blender.makesrna.RnaAccess.*;
import static blender.makesrna.RnaDefine.*;
import static blender.makesrna.RNATypes.*;
import blender.makesrna.rna_internal_types.*;
import blender.blenlib.*;

import static blender.makesrna.RnaUI.*;
import static blender.makesrna.RnaUIApi.*;

public class RnaMenu extends RnaStruct {
	
	public RnaMenu(PointerRNA ptr) {
		super(ptr);
	}
	
	public Object getRna_type() {
		return Menu_rna_type_get.getPtr(ptr);
	}
	
	public RnaUILayout getLayout() {
		return new RnaUILayout(Menu_layout_get.getPtr(ptr));
	}
	
	public String getBl_idname() {
		Menu_bl_idname_get.getStr(ptr, DEFAULT_BUF,0);
		return StringUtil.toJString(DEFAULT_BUF,0);
	}
	
	public void setBl_idname(String value) {
		Menu_bl_idname_set.setStr(ptr, StringUtil.toCString(value),0);
	}
	
	public String getBl_label() {
		Menu_bl_label_get.getStr(ptr, DEFAULT_BUF,0);
		return StringUtil.toJString(DEFAULT_BUF,0);
	}
	
	public void setBl_label(String value) {
		Menu_bl_label_set.setStr(ptr, StringUtil.toCString(value),0);
	}
	
	// If this method returns a non-null output, then the menu can be drawn.
	public boolean poll(PyObject[] values, String[] names) {
		// 
		 boolean visible = true;
		// 
		/*required*/ Object context = null;
		
		return false;
	}
	
	// Draw UI elements into the menu UI layout.
	public void draw(PyObject[] values, String[] names) {
		// 
		/*required*/ Object context = null;
	}
	
	/* Autogenerated Functions */
	
	private static PropCollectionGetFunc Menu_rna_properties_get = new PropCollectionGetFunc() {
	public PointerRNA get(CollectionPropertyIterator iter)
	{
		return rna_builtin_properties_get.get(iter);
	}};

	private static PropCollectionBeginFunc Menu_rna_properties_begin = new PropCollectionBeginFunc() {
	public void begin(CollectionPropertyIterator iter, PointerRNA ptr)
	{

		iter.clear();
		iter.parent= ptr;
		iter.prop= (PropertyRNA)rna_Menu_rna_properties;

		rna_builtin_properties_begin.begin(iter, ptr);

		if(iter.valid)
			iter.ptr= Menu_rna_properties_get.get(iter);
	}};

	private static PropCollectionNextFunc Menu_rna_properties_next = new PropCollectionNextFunc() {
	public void next(CollectionPropertyIterator iter)
	{
		rna_builtin_properties_next.next(iter);

		if(iter.valid)
			iter.ptr= Menu_rna_properties_get.get(iter);
	}};

	private static PropCollectionEndFunc Menu_rna_properties_end = new PropCollectionEndFunc() {
	public void end(CollectionPropertyIterator iter)
	{
		rna_iterator_listbase_end.end(iter);
	}};

	private static PropPointerGetFunc Menu_rna_type_get = new PropPointerGetFunc() {
	public PointerRNA getPtr(PointerRNA ptr)
	{
		return rna_builtin_type_get.getPtr(ptr);
	}};
	
	private static PropPointerGetFunc Menu_layout_get = new PropPointerGetFunc() {
	public PointerRNA getPtr(PointerRNA ptr)
	{
		Menu data= (Menu)(ptr.data);
		return rna_pointer_inherit_refine(ptr, RNA_UILayout, data.layout);
	}};

	private static PropStringGetFunc Menu_bl_idname_get = new PropStringGetFunc() {
	public void getStr(PointerRNA ptr, byte[] value, int offset)
	{
		Menu data= (Menu)(ptr.data);
		StringUtil.BLI_strncpy(value,offset, data.type.idname,0, data.type.idname.length);
	}};

	private static PropStringLengthFunc Menu_bl_idname_length = new PropStringLengthFunc() {
	public int lenStr(PointerRNA ptr)
	{
		Menu data= (Menu)(ptr.data);
		return StringUtil.strlen(data.type.idname,0);
	}};

	private static PropStringSetFunc Menu_bl_idname_set = new PropStringSetFunc() {
	public void setStr(PointerRNA ptr, byte[] value, int offset)
	{
		Menu data= (Menu)(ptr.data);
		StringUtil.BLI_strncpy(data.type.idname,0, value,offset, data.type.idname.length);
	}};

	private static PropStringGetFunc Menu_bl_label_get = new PropStringGetFunc() {
	public void getStr(PointerRNA ptr, byte[] value, int offset)
	{
		Menu data= (Menu)(ptr.data);
		StringUtil.BLI_strncpy(value,offset, data.type.label,0, data.type.label.length);
	}};

	private static PropStringLengthFunc Menu_bl_label_length = new PropStringLengthFunc() {
	public int lenStr(PointerRNA ptr)
	{
		Menu data= (Menu)(ptr.data);
		return StringUtil.strlen(data.type.label,0);
	}};

	private static PropStringSetFunc Menu_bl_label_set = new PropStringSetFunc() {
	public void setStr(PointerRNA ptr, byte[] value, int offset)
	{
		Menu data= (Menu)(ptr.data);
		StringUtil.BLI_strncpy(data.type.label,0, value,offset, data.type.label.length);
	}};
	
	/* Menu */
	private static CollectionPropertyRNA rna_Menu_rna_properties = new CollectionPropertyRNA(
		null, null,
		-1, "rna_properties", 128, "Properties",
		"RNA property collection",
		0,
		PROP_COLLECTION, PROP_NONE|PROP_UNIT_NONE, null, 0, new int[]{0, 0, 0}, 0,
		null, 0, null, null,
		0, -1, null,
		Menu_rna_properties_begin, Menu_rna_properties_next, Menu_rna_properties_end, Menu_rna_properties_get, null, null, rna_builtin_properties_lookup_string, RnaProperty.RNA_Property
	);

	private static PointerPropertyRNA rna_Menu_rna_type = new PointerPropertyRNA(
		null, null,
		-1, "rna_type", 524288, "RNA",
		"RNA type definition",
		0,
		PROP_POINTER, PROP_NONE|PROP_UNIT_NONE, null, 0, new int[]{0, 0, 0}, 0,
		null, 0, null, null,
		0, -1, null,
		Menu_rna_type_get, null, null, null,RnaStruct.RNA_Struct
	);

	private static PointerPropertyRNA rna_Menu_layout = new PointerPropertyRNA(
		null, null,
		-1, "layout", 0, "Layout",
		"Defines the structure of the menu in the UI.",
		0,
		PROP_POINTER, PROP_NONE|PROP_UNIT_NONE, null, 0, new int[]{0, 0, 0}, 0,
		null, 0, null, null,
		0, -1, null,
		Menu_layout_get, null, null, null,RnaUILayout.RNA_UILayout
	);

	private static StringPropertyRNA rna_Menu_bl_idname = new StringPropertyRNA(
		null, null,
		-1, "bl_idname", 17, "ID Name",
		"If this is set, the menu gets a custom ID, otherwise it takes the name of the class used to define the panel. For example, if the class name is \"OBJECT_MT_hello\", and bl_idname is not set by the script, then bl_idname = \"OBJECT_MT_hello\"",
		0,
		PROP_STRING, PROP_NONE|PROP_UNIT_NONE, null, 0, new int[]{0, 0, 0}, 0,
		null, 0, null, null,
		0, -1, null,
		Menu_bl_idname_get, Menu_bl_idname_length, Menu_bl_idname_set, 0, ""
	);

	private static StringPropertyRNA rna_Menu_bl_label = new StringPropertyRNA(
		null, null,
		-1, "bl_label", 17, "Label",
		"The menu label",
		0,
		PROP_STRING, PROP_NONE|PROP_UNIT_NONE, null, 0, new int[]{0, 0, 0}, 0,
		null, 0, null, null,
		0, -1, null,
		Menu_bl_label_get, Menu_bl_label_length, Menu_bl_label_set, 0, ""
	);
	
	static { ListBase tmp = new ListBase();
		ListBaseUtil.BLI_addtail(tmp, rna_Menu_rna_properties);
		ListBaseUtil.BLI_addtail(tmp, rna_Menu_rna_type);
		ListBaseUtil.BLI_addtail(tmp, rna_Menu_layout);
		ListBaseUtil.BLI_addtail(tmp, rna_Menu_bl_idname);
		ListBaseUtil.BLI_addtail(tmp, rna_Menu_bl_label);
	}
	
	private static BooleanPropertyRNA rna_Menu_poll_visible = new BooleanPropertyRNA(
		null, null,
		-1, "visible", 11, "",
		"",
		0,
		PROP_BOOLEAN, PROP_NONE|PROP_UNIT_NONE, null, 0, new int[]{0, 0, 0}, 0,
		null, 0, null, null,
		0, -1, null,
		null, null, null, null, true, null
	);

	private static PointerPropertyRNA rna_Menu_poll_context = new PointerPropertyRNA(
		null, null,
		-1, "context", 4, "",
		"",
		0,
		PROP_POINTER, PROP_NONE|PROP_UNIT_NONE, null, 0, new int[]{0, 0, 0}, 0,
		null, 0, null, null,
		0, -1, null,
		null, null, null, null,RnaContext.RNA_Context
	);
	
	static { ListBase tmp = new ListBase();
		ListBaseUtil.BLI_addtail(tmp, rna_Menu_poll_visible);
		ListBaseUtil.BLI_addtail(tmp, rna_Menu_poll_context);
	}

	private static FunctionRNA rna_Menu_poll_func = new FunctionRNA(
		null, null,
		null,
		rna_Menu_poll_visible, rna_Menu_poll_context,
		"poll", 25, "If this method returns a non-null output, then the menu can be drawn.",
		null,
		rna_Menu_poll_visible
	);

	private static PointerPropertyRNA rna_Menu_draw_context = new PointerPropertyRNA(
		null, null,
		-1, "context", 4, "",
		"",
		0,
		PROP_POINTER, PROP_NONE|PROP_UNIT_NONE, null, 0, new int[]{0, 0, 0}, 0,
		null, 0, null, null,
		0, -1, null,
		null, null, null, null,RnaContext.RNA_Context
	);
	
	static { ListBase tmp = new ListBase();
		ListBaseUtil.BLI_addtail(tmp, rna_Menu_draw_context);
	}

	private static FunctionRNA rna_Menu_draw_func = new FunctionRNA(
		null, null,
		null,
		rna_Menu_draw_context, rna_Menu_draw_context,
		"draw", 8, "Draw UI elements into the menu UI layout.",
		null,
		null
	);
	
	static { ListBase tmp = new ListBase();
		ListBaseUtil.BLI_addtail(tmp, rna_Menu_poll_func);
		ListBaseUtil.BLI_addtail(tmp, rna_Menu_draw_func);
	}
	
	public static StructRNA RNA_Menu = new StructRNA(
		RnaOperator.RNA_Operator, RnaHeader.RNA_Header,
		null,
		rna_Menu_rna_properties, rna_Menu_bl_label,
		null,null,
		"Menu", 0, "Menu", "Editor menu containing buttons",
		17,
		null, rna_Menu_rna_properties,
		null,
		null,
		rna_Menu_refine,
		null,
		rna_Menu_register,
		rna_Menu_unregister,
		null,
		rna_Menu_poll_func, rna_Menu_draw_func
	);
	
	public static BPy_StructRNA __rna__ = new BPy_StructRNA(new StructRNA(), RNA_Menu);

}
