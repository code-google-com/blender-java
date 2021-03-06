# ##### BEGIN GPL LICENSE BLOCK #####
#
#  This program is free software; you can redistribute it and/or
#  modify it under the terms of the GNU General Public License
#  as published by the Free Software Foundation; either version 2
#  of the License, or (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program; if not, write to the Free Software Foundation,
#  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
#
# ##### END GPL LICENSE BLOCK #####

# <pep8 compliant>
import bpy
from rna_prop_ui import PropertyPanel


def active_node_mat(mat):
    # TODO, 2.4x has a pipeline section, for 2.5 we need to communicate
    # which settings from node-materials are used
    if mat is not None:
        mat_node = mat.active_node_material
        if mat_node:
            return mat_node
        else:
            return mat

    return None


class MATERIAL_MT_sss_presets(bpy.types.Menu):
    bl_label = "SSS Presets"
    preset_subdir = "sss"
    preset_operator = "script.execute_preset"
    #draw = bpy.types.Menu.draw_preset


class MATERIAL_MT_specials(bpy.types.Menu):
    bl_label = "Material Specials"

    def draw(self, context):
        layout = self.layout

        layout.operator("object.material_slot_copy", icon='COPY_ID')
        layout.operator("material.copy", icon='COPYDOWN')
        layout.operator("material.paste", icon='PASTEDOWN')


class MaterialButtonsPanel():
    bl_space_type = 'PROPERTIES'
    bl_region_type = 'WINDOW'
    bl_context = "material"
    # COMPAT_ENGINES must be defined in each subclass, external engines can add themselves here

    @classmethod
    def poll(cls, context):
        return context.material and (context.scene.render.engine in cls.COMPAT_ENGINES)


class MATERIAL_PT_context_material(MaterialButtonsPanel, bpy.types.Panel):
    bl_label = ""
    bl_options = set(['HIDE_HEADER'])
    COMPAT_ENGINES = set(['BLENDER_RENDER', 'BLENDER_GAME'])

    @classmethod
    def poll(cls, context):
        # An exception, dont call the parent poll func because
        # this manages materials for all engine types

        engine = context.scene.render.engine
        return (context.material or context.object) and (engine in cls.COMPAT_ENGINES)

    def draw(self, context):
        layout = self.layout

        mat = context.material
        ob = context.object
        slot = context.material_slot
        space = context.space_data

        if ob:
            row = layout.row()

            row.template_list(ob, "material_slots", ob, "active_material_index", rows=2)

            col = row.column(align=True)
            col.operator("object.material_slot_add", icon='ZOOMIN', text="")
            col.operator("object.material_slot_remove", icon='ZOOMOUT', text="")

            col.menu("MATERIAL_MT_specials", icon='DOWNARROW_HLT', text="")

            if ob.mode == 'EDIT':
                row = layout.row(align=True)
                row.operator("object.material_slot_assign", text="Assign")
                row.operator("object.material_slot_select", text="Select")
                row.operator("object.material_slot_deselect", text="Deselect")

        split = layout.split(percentage=0.65)

        if ob:
            split.template_ID(ob, "active_material", new="material.new")
            row = split.row()
            if mat:
                row.prop(mat, "use_nodes", icon="NODETREE", text="")

            if slot:
                row.prop(slot, "link", text="")
            else:
                row.label()
        elif mat:
            split.template_ID(space, "pin_id")
            split.separator()

        if mat:
            layout.prop(mat, "type", expand=True)


class MATERIAL_PT_preview(MaterialButtonsPanel, bpy.types.Panel):
    bl_label = "Preview"
    COMPAT_ENGINES = set(['BLENDER_RENDER', 'BLENDER_GAME'])

    def draw(self, context):
        self.layout.template_preview(context.material)


class MATERIAL_PT_diffuse(MaterialButtonsPanel, bpy.types.Panel):
    bl_label = "Diffuse"
    COMPAT_ENGINES = set(['BLENDER_RENDER', 'BLENDER_GAME'])

    @classmethod
    def poll(cls, context):
        mat = active_node_mat(context.material)
        engine = context.scene.render.engine
        return mat and (mat.type in ('SURFACE', 'WIRE')) and (engine in cls.COMPAT_ENGINES)

    def draw(self, context):
        layout = self.layout

        mat = active_node_mat(context.material)

        split = layout.split()

        col = split.column()
        col.prop(mat, "diffuse_color", text="")
        sub = col.column()
        sub.active = (not mat.use_shadeless)
        sub.prop(mat, "diffuse_intensity", text="Intensity")

        col = split.column()
        col.active = (not mat.use_shadeless)
        col.prop(mat, "diffuse_shader", text="")
        col.prop(mat, "use_diffuse_ramp", text="Ramp")

        col = layout.column()
        col.active = (not mat.use_shadeless)
        if mat.diffuse_shader == 'OREN_NAYAR':
            col.prop(mat, "roughness")
        elif mat.diffuse_shader == 'MINNAERT':
            col.prop(mat, "darkness")
        elif mat.diffuse_shader == 'TOON':
            split = col.split()

            col = split.column()
            col.prop(mat, "diffuse_toon_size", text="Size")

            col = split.column()
            col.prop(mat, "diffuse_toon_smooth", text="Smooth")
        elif mat.diffuse_shader == 'FRESNEL':
            split = col.split()

            col = split.column()
            col.prop(mat, "diffuse_fresnel", text="Fresnel")

            col = split.column()
            col.prop(mat, "diffuse_fresnel_factor", text="Factor")

        if mat.use_diffuse_ramp:
            layout.separator()
            layout.template_color_ramp(mat, "diffuse_ramp", expand=True)
            layout.separator()

            split = layout.split()

            col = split.column()
            col.prop(mat, "diffuse_ramp_input", text="Input")

            col = split.column()
            col.prop(mat, "diffuse_ramp_blend", text="Blend")
            row = layout.row()
            row.prop(mat, "diffuse_ramp_factor", text="Factor")


class MATERIAL_PT_specular(MaterialButtonsPanel, bpy.types.Panel):
    bl_label = "Specular"
    COMPAT_ENGINES = set(['BLENDER_RENDER', 'BLENDER_GAME'])

    @classmethod
    def poll(cls, context):
        mat = active_node_mat(context.material)
        engine = context.scene.render.engine
        return mat and (mat.type in ('SURFACE', 'WIRE')) and (engine in cls.COMPAT_ENGINES)

    def draw(self, context):
        layout = self.layout

        mat = active_node_mat(context.material)

        layout.active = (not mat.use_shadeless)

        split = layout.split()

        col = split.column()
        col.prop(mat, "specular_color", text="")
        col.prop(mat, "specular_intensity", text="Intensity")

        col = split.column()
        col.prop(mat, "specular_shader", text="")
        col.prop(mat, "use_specular_ramp", text="Ramp")

        col = layout.column()
        if mat.specular_shader in ('COOKTORR', 'PHONG'):
            col.prop(mat, "specular_hardness", text="Hardness")
        elif mat.specular_shader == 'BLINN':
            split = layout.split()

            col = split.column()
            col.prop(mat, "specular_hardness", text="Hardness")

            col = split.column()
            col.prop(mat, "specular_ior", text="IOR")
        elif mat.specular_shader == 'WARDISO':
            col.prop(mat, "specular_slope", text="Slope")
        elif mat.specular_shader == 'TOON':
            split = layout.split()

            col = split.column()
            col.prop(mat, "specular_toon_size", text="Size")

            col = split.column()
            col.prop(mat, "specular_toon_smooth", text="Smooth")

        if mat.use_specular_ramp:
            layout.separator()
            layout.template_color_ramp(mat, "specular_ramp", expand=True)
            layout.separator()
            split = layout.split()

            col = split.column()
            col.prop(mat, "specular_ramp_input", text="Input")

            col = split.column()
            col.prop(mat, "specular_ramp_blend", text="Blend")

            row = layout.row()
            row.prop(mat, "specular_ramp_factor", text="Factor")


class MATERIAL_PT_shading(MaterialButtonsPanel, bpy.types.Panel):
    bl_label = "Shading"
    COMPAT_ENGINES = set(['BLENDER_RENDER', 'BLENDER_GAME'])

    @classmethod
    def poll(cls, context):
        mat = active_node_mat(context.material)
        engine = context.scene.render.engine
        return mat and (mat.type in ('SURFACE', 'WIRE')) and (engine in cls.COMPAT_ENGINES)

    def draw(self, context):
        layout = self.layout

        mat = active_node_mat(context.material)

        if mat.type in ('SURFACE', 'WIRE'):
            split = layout.split()

            col = split.column()
            sub = col.column()
            sub.active = not mat.use_shadeless
            sub.prop(mat, "emit")
            sub.prop(mat, "ambient")
            sub = col.column()
            sub.prop(mat, "translucency")

            col = split.column()
            col.prop(mat, "use_shadeless")
            sub = col.column()
            sub.active = not mat.use_shadeless
            sub.prop(mat, "use_tangent_shading")
            sub.prop(mat, "use_cubic")


class MATERIAL_PT_transp(MaterialButtonsPanel, bpy.types.Panel):
    bl_label = "Transparency"
    bl_options = set(['DEFAULT_CLOSED'])
    COMPAT_ENGINES = set(['BLENDER_RENDER'])

    @classmethod
    def poll(cls, context):
        mat = active_node_mat(context.material)
        engine = context.scene.render.engine
        return mat and (mat.type in ('SURFACE', 'WIRE')) and (engine in cls.COMPAT_ENGINES)

    def draw_header(self, context):
        mat = active_node_mat(context.material)

        self.layout.prop(mat, "use_transparency", text="")

    def draw(self, context):
        layout = self.layout

        mat = active_node_mat(context.material)
        rayt = mat.raytrace_transparency

        row = layout.row()
        row.active = mat.use_transparency and (not mat.use_shadeless)
        row.prop(mat, "transparency_method", expand=True)

        split = layout.split()

        col = split.column()
        col.prop(mat, "alpha")
        row = col.row()
        row.active = mat.use_transparency and (not mat.use_shadeless)
        row.prop(mat, "specular_alpha", text="Specular")

        col = split.column()
        col.active = (not mat.use_shadeless)
        col.prop(rayt, "fresnel")
        sub = col.column()
        sub.active = rayt.fresnel > 0
        sub.prop(rayt, "fresnel_factor", text="Blend")

        if mat.transparency_method == 'RAYTRACE':
            layout.separator()
            split = layout.split()
            split.active = mat.use_transparency

            col = split.column()
            col.prop(rayt, "ior")
            col.prop(rayt, "filter")
            col.prop(rayt, "falloff")
            col.prop(rayt, "depth_max")
            col.prop(rayt, "depth")

            col = split.column()
            col.label(text="Gloss:")
            col.prop(rayt, "gloss_factor", text="Amount")
            sub = col.column()
            sub.active = rayt.gloss_factor < 1.0
            sub.prop(rayt, "gloss_threshold", text="Threshold")
            sub.prop(rayt, "gloss_samples", text="Samples")


class MATERIAL_PT_mirror(MaterialButtonsPanel, bpy.types.Panel):
    bl_label = "Mirror"
    bl_options = set(['DEFAULT_CLOSED'])
    COMPAT_ENGINES = set(['BLENDER_RENDER'])

    @classmethod
    def poll(cls, context):
        mat = active_node_mat(context.material)
        engine = context.scene.render.engine
        return mat and (mat.type in ('SURFACE', 'WIRE')) and (engine in cls.COMPAT_ENGINES)

    def draw_header(self, context):
        raym = active_node_mat(context.material).raytrace_mirror

        self.layout.prop(raym, "use", text="")

    def draw(self, context):
        layout = self.layout

        mat = active_node_mat(context.material)
        raym = mat.raytrace_mirror

        layout.active = raym.use

        split = layout.split()

        col = split.column()
        col.prop(raym, "reflect_factor")
        col.prop(mat, "mirror_color", text="")

        col = split.column()
        col.prop(raym, "fresnel")
        sub = col.column()
        sub.active = raym.fresnel > 0
        sub.prop(raym, "fresnel_factor", text="Blend")

        split = layout.split()

        col = split.column()
        col.separator()
        col.prop(raym, "depth")
        col.prop(raym, "distance", text="Max Dist")
        col.separator()
        sub = col.split(percentage=0.4)
        sub.active = raym.distance > 0.0
        sub.label(text="Fade To:")
        sub.prop(raym, "fade_to", text="")

        col = split.column()
        col.label(text="Gloss:")
        col.prop(raym, "gloss_factor", text="Amount")
        sub = col.column()
        sub.active = raym.gloss_factor < 1.0
        sub.prop(raym, "gloss_threshold", text="Threshold")
        sub.prop(raym, "gloss_samples", text="Samples")
        sub.prop(raym, "gloss_anisotropic", text="Anisotropic")


class MATERIAL_PT_sss(MaterialButtonsPanel, bpy.types.Panel):
    bl_label = "Subsurface Scattering"
    bl_options = set(['DEFAULT_CLOSED'])
    COMPAT_ENGINES = set(['BLENDER_RENDER'])

    @classmethod
    def poll(cls, context):
        mat = active_node_mat(context.material)
        engine = context.scene.render.engine
        return mat and (mat.type in ('SURFACE', 'WIRE')) and (engine in cls.COMPAT_ENGINES)

    def draw_header(self, context):
        mat = active_node_mat(context.material)
        sss = mat.subsurface_scattering

        self.layout.active = (not mat.use_shadeless)
        self.layout.prop(sss, "use", text="")

    def draw(self, context):
        layout = self.layout

        mat = active_node_mat(context.material)
        sss = mat.subsurface_scattering

        layout.active = (sss.use) and (not mat.use_shadeless)

        row = layout.row().split()
        sub = row.row(align=True).split(percentage=0.75)
        sub.menu("MATERIAL_MT_sss_presets", text=bpy.types.MATERIAL_MT_sss_presets.bl_label)
        sub.operator("material.sss_preset_add", text="", icon="ZOOMIN")
        sub.operator("material.sss_preset_add", text="", icon="ZOOMOUT").remove_active = True

        split = layout.split()

        col = split.column()
        col.prop(sss, "ior")
        col.prop(sss, "scale")
        col.prop(sss, "color", text="")
        col.prop(sss, "radius", text="RGB Radius", expand=True)

        col = split.column()
        sub = col.column(align=True)
        sub.label(text="Blend:")
        sub.prop(sss, "color_factor", text="Color")
        sub.prop(sss, "texture_factor", text="Texture")
        sub.label(text="Scattering Weight:")
        sub.prop(sss, "front")
        sub.prop(sss, "back")
        col.separator()
        col.prop(sss, "error_threshold", text="Error")


class MATERIAL_PT_halo(MaterialButtonsPanel, bpy.types.Panel):
    bl_label = "Halo"
    COMPAT_ENGINES = set(['BLENDER_RENDER'])

    @classmethod
    def poll(cls, context):
        mat = context.material
        engine = context.scene.render.engine
        return mat and (mat.type == 'HALO') and (engine in cls.COMPAT_ENGINES)

    def draw(self, context):
        layout = self.layout

        mat = context.material  # dont use node material
        halo = mat.halo

        def number_but(layout, toggle, number, name, color):
            row = layout.row(align=True)
            row.prop(halo, toggle, text="")
            sub = row.column()
            sub.active = getattr(halo, toggle)
            sub.prop(halo, number, text=name)
            if not color == "":
                sub.prop(mat, color, text="")

        split = layout.split()

        col = split.column()
        col.prop(mat, "alpha")
        col.prop(mat, "diffuse_color", text="")

        col = split.column()
        col.prop(halo, "size")
        col.prop(halo, "hardness")
        col.prop(halo, "add")

        layout.label(text="Options:")

        split = layout.split()
        col = split.column()
        col.prop(halo, "use_texture")
        col.prop(halo, "use_vertex_normal")
        col.prop(halo, "use_extreme_alpha")
        col.prop(halo, "use_shaded")
        col.prop(halo, "use_soft")

        col = split.column()
        number_but(col, "use_ring", "ring_count", "Rings", "mirror_color")
        number_but(col, "use_lines", "line_count", "Lines", "specular_color")
        number_but(col, "use_star", "star_tip_count", "Star tips", "")


class MATERIAL_PT_flare(MaterialButtonsPanel, bpy.types.Panel):
    bl_label = "Flare"
    COMPAT_ENGINES = set(['BLENDER_RENDER'])

    @classmethod
    def poll(cls, context):
        mat = context.material
        engine = context.scene.render.engine
        return mat and (mat.type == 'HALO') and (engine in cls.COMPAT_ENGINES)

    def draw_header(self, context):
        halo = context.material.halo

        self.layout.prop(halo, "use_flare_mode", text="")

    def draw(self, context):
        layout = self.layout

        mat = context.material  # dont use node material
        halo = mat.halo

        layout.active = halo.use_flare_mode

        split = layout.split()

        col = split.column()
        col.prop(halo, "flare_size", text="Size")
        col.prop(halo, "flare_boost", text="Boost")
        col.prop(halo, "flare_seed", text="Seed")

        col = split.column()
        col.prop(halo, "flare_subflare_count", text="Subflares")
        col.prop(halo, "flare_subflare_size", text="Subsize")


class MATERIAL_PT_physics(MaterialButtonsPanel, bpy.types.Panel):
    bl_label = "Physics"
    COMPAT_ENGINES = set(['BLENDER_GAME'])

    @classmethod
    def poll(cls, context):
        return context.material and (context.scene.render.engine in cls.COMPAT_ENGINES)

    def draw(self, context):
        layout = self.layout

        phys = context.material.physics  # dont use node material

        split = layout.split()

        col = split.column()
        col.prop(phys, "distance")
        col.prop(phys, "friction")
        col.prop(phys, "use_normal_align")

        col = split.column()
        col.prop(phys, "force", slider=True)
        col.prop(phys, "elasticity", slider=True)
        col.prop(phys, "damping", slider=True)


class MATERIAL_PT_strand(MaterialButtonsPanel, bpy.types.Panel):
    bl_label = "Strand"
    bl_options = set(['DEFAULT_CLOSED'])
    COMPAT_ENGINES = set(['BLENDER_RENDER'])

    @classmethod
    def poll(cls, context):
        mat = context.material
        engine = context.scene.render.engine
        return mat and (mat.type in ('SURFACE', 'WIRE', 'HALO')) and (engine in cls.COMPAT_ENGINES)

    def draw(self, context):
        layout = self.layout

        mat = context.material  # dont use node material
        tan = mat.strand

        split = layout.split()

        col = split.column()
        sub = col.column(align=True)
        sub.label(text="Size:")
        sub.prop(tan, "root_size", text="Root")
        sub.prop(tan, "tip_size", text="Tip")
        sub.prop(tan, "size_min", text="Minimum")
        sub.prop(tan, "use_blender_units")
        sub = col.column()
        sub.active = (not mat.use_shadeless)
        sub.prop(tan, "use_tangent_shading")
        col.prop(tan, "shape")

        col = split.column()
        col.label(text="Shading:")
        col.prop(tan, "width_fade")
        ob = context.object
        if ob and ob.type == 'MESH':
            col.prop_search(tan, "uv_layer", ob.data, "uv_textures", text="")
        else:
            col.prop(tan, "uv_layer", text="")
        col.separator()
        sub = col.column()
        sub.active = (not mat.use_shadeless)
        sub.label("Surface diffuse:")
        sub = col.column()
        sub.prop(tan, "blend_distance", text="Distance")


class MATERIAL_PT_options(MaterialButtonsPanel, bpy.types.Panel):
    bl_label = "Options"
    COMPAT_ENGINES = set(['BLENDER_RENDER', 'BLENDER_GAME'])

    @classmethod
    def poll(cls, context):
        mat = active_node_mat(context.material)
        engine = context.scene.render.engine
        return mat and (mat.type in ('SURFACE', 'WIRE')) and (engine in cls.COMPAT_ENGINES)

    def draw(self, context):
        layout = self.layout

        mat = active_node_mat(context.material)

        split = layout.split()

        col = split.column()
        col.prop(mat, "use_raytrace")
        col.prop(mat, "use_full_oversampling")
        col.prop(mat, "use_sky")
        col.prop(mat, "use_mist")
        col.prop(mat, "invert_z")
        sub = col.row()
        sub.prop(mat, "offset_z")
        sub.active = mat.use_transparency and mat.transparency_method == 'Z_TRANSPARENCY'
        sub = col.column(align=True)
        sub.label(text="Light Group:")
        sub.prop(mat, "light_group", text="")
        row = sub.row()
        row.active = bool(mat.light_group)
        row.prop(mat, "use_light_group_exclusive", text="Exclusive")

        col = split.column()
        col.prop(mat, "use_face_texture")
        sub = col.column()
        sub.active = mat.use_face_texture
        sub.prop(mat, "use_face_texture_alpha")
        col.separator()
        col.prop(mat, "use_vertex_color_paint")
        col.prop(mat, "use_vertex_color_light")
        col.prop(mat, "use_object_color")


class MATERIAL_PT_shadow(MaterialButtonsPanel, bpy.types.Panel):
    bl_label = "Shadow"
    bl_options = set(['DEFAULT_CLOSED'])
    COMPAT_ENGINES = set(['BLENDER_RENDER', 'BLENDER_GAME'])

    @classmethod
    def poll(cls, context):
        mat = active_node_mat(context.material)
        engine = context.scene.render.engine
        return mat and (mat.type in ('SURFACE', 'WIRE')) and (engine in cls.COMPAT_ENGINES)

    def draw(self, context):
        layout = self.layout

        mat = active_node_mat(context.material)

        split = layout.split()

        col = split.column()
        col.prop(mat, "use_shadows", text="Receive")
        col.prop(mat, "use_transparent_shadows", text="Receive Transparent")
        col.prop(mat, "use_only_shadow", text="Shadows Only")
        col.prop(mat, "use_cast_shadows_only", text="Cast Only")
        col.prop(mat, "shadow_cast_alpha", text="Casting Alpha")

        col = split.column()
        col.prop(mat, "use_cast_buffer_shadows")
        sub = col.column()
        sub.active = mat.use_cast_buffer_shadows
        sub.prop(mat, "shadow_buffer_bias", text="Buffer Bias")
        col.prop(mat, "use_ray_shadow_bias", text="Auto Ray Bias")
        sub = col.column()
        sub.active = (not mat.use_ray_shadow_bias)
        sub.prop(mat, "shadow_ray_bias", text="Ray Bias")
        col.prop(mat, "use_cast_approximate")


class MATERIAL_PT_transp_game(MaterialButtonsPanel, bpy.types.Panel):
    bl_label = "Transparency"
    bl_options = set(['DEFAULT_CLOSED'])
    COMPAT_ENGINES = set(['BLENDER_GAME'])

    @classmethod
    def poll(cls, context):
        mat = active_node_mat(context.material)
        engine = context.scene.render.engine
        return mat  and (engine in cls.COMPAT_ENGINES)

    def draw_header(self, context):
        mat = active_node_mat(context.material)

        self.layout.prop(mat, "use_transparency", text="")

    def draw(self, context):
        layout = self.layout

        mat = active_node_mat(context.material)
        rayt = mat.raytrace_transparency

        row = layout.row()
        row.active = mat.use_transparency and (not mat.use_shadeless)
        row.prop(mat, "transparency_method", expand=True)

        split = layout.split()

        col = split.column()
        col.prop(mat, "alpha")


class VolumeButtonsPanel():
    bl_space_type = 'PROPERTIES'
    bl_region_type = 'WINDOW'
    bl_context = "material"
    COMPAT_ENGINES = set(['BLENDER_RENDER'])

    @classmethod
    def poll(cls, context):
        mat = context.material
        engine = context.scene.render.engine
        return mat and (mat.type == 'VOLUME') and (engine in cls.COMPAT_ENGINES)


class MATERIAL_PT_volume_density(VolumeButtonsPanel, bpy.types.Panel):
    bl_label = "Density"
    COMPAT_ENGINES = set(['BLENDER_RENDER'])

    def draw(self, context):
        layout = self.layout

        vol = context.material.volume  # dont use node material

        split = layout.split()
        col = split.column()
        col.prop(vol, "density")

        col = split.column()
        col.prop(vol, "density_scale")


class MATERIAL_PT_volume_shading(VolumeButtonsPanel, bpy.types.Panel):
    bl_label = "Shading"
    COMPAT_ENGINES = set(['BLENDER_RENDER'])

    def draw(self, context):
        layout = self.layout

        vol = context.material.volume  # dont use node material

        split = layout.split()

        col = split.column()
        col.prop(vol, "scattering")
        col.prop(vol, "asymmetry")
        col.prop(vol, "transmission_color")

        col = split.column()
        sub = col.column(align=True)
        sub.prop(vol, "emission")
        sub.prop(vol, "emission_color", text="")
        sub = col.column(align=True)
        sub.prop(vol, "reflection")
        sub.prop(vol, "reflection_color", text="")


class MATERIAL_PT_volume_lighting(VolumeButtonsPanel, bpy.types.Panel):
    bl_label = "Lighting"
    COMPAT_ENGINES = set(['BLENDER_RENDER'])

    def draw(self, context):
        layout = self.layout

        vol = context.material.volume  # dont use node material

        split = layout.split()

        col = split.column()
        col.prop(vol, "light_method", text="")

        col = split.column()

        if vol.light_method == 'SHADED':
            col.prop(vol, "use_external_shadows")
            col.prop(vol, "use_light_cache")
            sub = col.column()
            sub.active = vol.use_light_cache
            sub.prop(vol, "cache_resolution")
        elif vol.light_method in ('MULTIPLE_SCATTERING', 'SHADED_PLUS_MULTIPLE_SCATTERING'):
            sub = col.column()
            sub.enabled = True
            sub.active = False
            sub.prop(vol, "use_light_cache")
            col.prop(vol, "cache_resolution")

            sub = col.column(align=True)
            sub.prop(vol, "ms_diffusion")
            sub.prop(vol, "ms_spread")
            sub.prop(vol, "ms_intensity")


class MATERIAL_PT_volume_transp(VolumeButtonsPanel, bpy.types.Panel):
    bl_label = "Transparency"
    COMPAT_ENGINES = set(['BLENDER_RENDER'])

    def draw(self, context):
        layout = self.layout

        mat = context.material  # dont use node material

        layout.prop(mat, "transparency_method", expand=True)


class MATERIAL_PT_volume_integration(VolumeButtonsPanel, bpy.types.Panel):
    bl_label = "Integration"
    COMPAT_ENGINES = set(['BLENDER_RENDER'])

    def draw(self, context):
        layout = self.layout

        vol = context.material.volume  # dont use node material

        split = layout.split()

        col = split.column()
        col.label(text="Step Calculation:")
        col.prop(vol, "step_method", text="")
        col = col.column(align=True)
        col.prop(vol, "step_size")

        col = split.column()
        col.label()
        col.prop(vol, "depth_threshold")


class MATERIAL_PT_volume_options(VolumeButtonsPanel, bpy.types.Panel):
    bl_label = "Options"
    COMPAT_ENGINES = set(['BLENDER_RENDER', 'BLENDER_GAME'])
    bl_options = set(['DEFAULT_CLOSED'])

    def draw(self, context):
        layout = self.layout

        mat = active_node_mat(context.material)

        split = layout.split()

        col = split.column()
        col.prop(mat, "use_raytrace")
        col.prop(mat, "use_full_oversampling")
        col.prop(mat, "use_mist")

        col = split.column()
        col.label(text="Light Group:")
        col.prop(mat, "light_group", text="")
        row = col.row()
        row.active = bool(mat.light_group)
        row.prop(mat, "use_light_group_exclusive", text="Exclusive")


class MATERIAL_PT_custom_props(MaterialButtonsPanel, PropertyPanel, bpy.types.Panel):
    COMPAT_ENGINES = set(['BLENDER_RENDER', 'BLENDER_GAME'])
    _context_path = "material"
    _property_type = bpy.types.Material


def register():
    pass


def unregister():
    pass

if __name__ == "__main__":
    register()
