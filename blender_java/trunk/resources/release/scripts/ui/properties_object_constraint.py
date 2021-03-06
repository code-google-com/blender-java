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


class ConstraintButtonsPanel():
    bl_space_type = 'PROPERTIES'
    bl_region_type = 'WINDOW'
    bl_context = "constraint"

    def draw_constraint(self, context, con):
        layout = self.layout

        box = layout.template_constraint(con)

        if box:
            # match enum type to our functions, avoids a lookup table.
            getattr(self, con.type)(context, box, con)

            if con.type not in ('RIGID_BODY_JOINT', 'NULL'):
                box.prop(con, "influence")

    def space_template(self, layout, con, target=True, owner=True):
        if target or owner:

            split = layout.split(percentage=0.2)

            split.label(text="Space:")
            row = split.row()

            if target:
                row.prop(con, "target_space", text="")

            if target and owner:
                row.label(icon='ARROW_LEFTRIGHT')

            if owner:
                row.prop(con, "owner_space", text="")

    def target_template(self, layout, con, subtargets=True):
        layout.prop(con, "target")  # XXX limiting settings for only 'curves' or some type of object

        if con.target and subtargets:
            if con.target.type == 'ARMATURE':
                layout.prop_search(con, "subtarget", con.target.data, "bones", text="Bone")

                if con.type in ('COPY_LOCATION', 'STRETCH_TO', 'TRACK_TO', 'PIVOT'):
                    row = layout.row()
                    row.label(text="Head/Tail:")
                    row.prop(con, "head_tail", text="")
            elif con.target.type in ('MESH', 'LATTICE'):
                layout.prop_search(con, "subtarget", con.target, "vertex_groups", text="Vertex Group")

    def ik_template(self, layout, con):
        # only used for iTaSC
        layout.prop(con, "pole_target")

        if con.pole_target and con.pole_target.type == 'ARMATURE':
            layout.prop_search(con, "pole_subtarget", con.pole_target.data, "bones", text="Bone")

        if con.pole_target:
            row = layout.row()
            row.label()
            row.prop(con, "pole_angle")

        split = layout.split(percentage=0.33)
        col = split.column()
        col.prop(con, "use_tail")
        col.prop(con, "use_stretch")

        col = split.column()
        col.prop(con, "chain_count")
        col.prop(con, "use_target")

    def CHILD_OF(self, context, layout, con):
        self.target_template(layout, con)

        split = layout.split()

        col = split.column()
        col.label(text="Location:")
        col.prop(con, "use_location_x", text="X")
        col.prop(con, "use_location_y", text="Y")
        col.prop(con, "use_location_z", text="Z")

        col = split.column()
        col.label(text="Rotation:")
        col.prop(con, "use_rotation_x", text="X")
        col.prop(con, "use_rotation_y", text="Y")
        col.prop(con, "use_rotation_z", text="Z")

        col = split.column()
        col.label(text="Scale:")
        col.prop(con, "use_scale_x", text="X")
        col.prop(con, "use_scale_y", text="Y")
        col.prop(con, "use_scale_z", text="Z")

        split = layout.split()

        col = split.column()
        col.operator("constraint.childof_set_inverse")

        col = split.column()
        col.operator("constraint.childof_clear_inverse")

    def TRACK_TO(self, context, layout, con):
        self.target_template(layout, con)

        row = layout.row()
        row.label(text="To:")
        row.prop(con, "track_axis", expand=True)

        split = layout.split()

        col = split.column()
        col.prop(con, "up_axis", text="Up")

        col = split.column()
        col.prop(con, "use_target_z")

        self.space_template(layout, con)

    def IK(self, context, layout, con):
        if context.object.pose.ik_solver == "ITASC":
            layout.prop(con, "ik_type")
            getattr(self, 'IK_' + con.ik_type)(context, layout, con)
        else:
            # Legacy IK constraint
            self.target_template(layout, con)
            layout.prop(con, "pole_target")

            if con.pole_target and con.pole_target.type == 'ARMATURE':
                layout.prop_search(con, "pole_subtarget", con.pole_target.data, "bones", text="Bone")

            if con.pole_target:
                row = layout.row()
                row.prop(con, "pole_angle")
                row.label()

            split = layout.split()
            col = split.column()
            col.prop(con, "iterations")
            col.prop(con, "chain_count")

            col.label(text="Weight:")
            col.prop(con, "weight", text="Position", slider=True)
            sub = col.column()
            sub.active = con.use_rotation
            sub.prop(con, "orient_weight", text="Rotation", slider=True)

            col = split.column()
            col.prop(con, "use_tail")
            col.prop(con, "use_stretch")
            col.separator()
            col.prop(con, "use_target")
            col.prop(con, "use_rotation")

    def IK_COPY_POSE(self, context, layout, con):
        self.target_template(layout, con)
        self.ik_template(layout, con)

        row = layout.row()
        row.label(text="Axis Ref:")
        row.prop(con, "reference_axis", expand=True)
        split = layout.split(percentage=0.33)
        split.row().prop(con, "use_location")
        row = split.row()
        row.prop(con, "weight", text="Weight", slider=True)
        row.active = con.use_location
        split = layout.split(percentage=0.33)
        row = split.row()
        row.label(text="Lock:")
        row = split.row()
        row.prop(con, "lock_location_x", text="X")
        row.prop(con, "lock_location_y", text="Y")
        row.prop(con, "lock_location_z", text="Z")
        split.active = con.use_location

        split = layout.split(percentage=0.33)
        split.row().prop(con, "use_rotation")
        row = split.row()
        row.prop(con, "orient_weight", text="Weight", slider=True)
        row.active = con.use_rotation
        split = layout.split(percentage=0.33)
        row = split.row()
        row.label(text="Lock:")
        row = split.row()
        row.prop(con, "lock_rotation_x", text="X")
        row.prop(con, "lock_rotation_y", text="Y")
        row.prop(con, "lock_rotation_z", text="Z")
        split.active = con.use_rotation

    def IK_DISTANCE(self, context, layout, con):
        self.target_template(layout, con)
        self.ik_template(layout, con)

        layout.prop(con, "limit_mode")
        row = layout.row()
        row.prop(con, "weight", text="Weight", slider=True)
        row.prop(con, "distance", text="Distance", slider=True)

    def FOLLOW_PATH(self, context, layout, con):
        self.target_template(layout, con)

        split = layout.split()

        col = split.column()
        col.prop(con, "use_curve_follow")
        col.prop(con, "use_curve_radius")

        col = split.column()
        col.prop(con, "use_fixed_location")
        if con.use_fixed_location:
            col.prop(con, "offset_factor", text="Offset")
        else:
            col.prop(con, "offset")

        row = layout.row()
        row.label(text="Forward:")
        row.prop(con, "forward_axis", expand=True)

        row = layout.row()
        row.prop(con, "up_axis", text="Up")
        row.label()

    def LIMIT_ROTATION(self, context, layout, con):

        split = layout.split()

        col = split.column(align=True)
        col.prop(con, "use_limit_x")
        sub = col.column()
        sub.active = con.use_limit_x
        sub.prop(con, "min_x", text="Min")
        sub.prop(con, "max_x", text="Max")

        col = split.column(align=True)
        col.prop(con, "use_limit_y")
        sub = col.column()
        sub.active = con.use_limit_y
        sub.prop(con, "min_y", text="Min")
        sub.prop(con, "max_y", text="Max")

        col = split.column(align=True)
        col.prop(con, "use_limit_z")
        sub = col.column()
        sub.active = con.use_limit_z
        sub.prop(con, "min_z", text="Min")
        sub.prop(con, "max_z", text="Max")

        row = layout.row()
        row.prop(con, "use_transform_limit")
        row.label()

        row = layout.row()
        row.label(text="Convert:")
        row.prop(con, "owner_space", text="")

    def LIMIT_LOCATION(self, context, layout, con):
        split = layout.split()

        col = split.column()
        col.prop(con, "use_min_x")
        sub = col.column()
        sub.active = con.use_min_x
        sub.prop(con, "min_x", text="")
        col.prop(con, "use_max_x")
        sub = col.column()
        sub.active = con.use_max_x
        sub.prop(con, "max_x", text="")

        col = split.column()
        col.prop(con, "use_min_y")
        sub = col.column()
        sub.active = con.use_min_y
        sub.prop(con, "min_y", text="")
        col.prop(con, "use_max_y")
        sub = col.column()
        sub.active = con.use_max_y
        sub.prop(con, "max_y", text="")

        col = split.column()
        col.prop(con, "use_min_z")
        sub = col.column()
        sub.active = con.use_min_z
        sub.prop(con, "min_z", text="")
        col.prop(con, "use_max_z")
        sub = col.column()
        sub.active = con.use_max_z
        sub.prop(con, "max_z", text="")

        row = layout.row()
        row.prop(con, "use_transform_limit")
        row.label()

        row = layout.row()
        row.label(text="Convert:")
        row.prop(con, "owner_space", text="")

    def LIMIT_SCALE(self, context, layout, con):
        split = layout.split()

        col = split.column()
        col.prop(con, "use_min_x")
        sub = col.column()
        sub.active = con.use_min_x
        sub.prop(con, "min_x", text="")
        col.prop(con, "use_max_x")
        sub = col.column()
        sub.active = con.use_max_x
        sub.prop(con, "max_x", text="")

        col = split.column()
        col.prop(con, "use_min_y")
        sub = col.column()
        sub.active = con.use_min_y
        sub.prop(con, "min_y", text="")
        col.prop(con, "use_max_y")
        sub = col.column()
        sub.active = con.use_max_y
        sub.prop(con, "max_y", text="")

        col = split.column()
        col.prop(con, "use_min_z")
        sub = col.column()
        sub.active = con.use_min_z
        sub.prop(con, "min_z", text="")
        col.prop(con, "use_max_z")
        sub = col.column()
        sub.active = con.use_max_z
        sub.prop(con, "max_z", text="")

        row = layout.row()
        row.prop(con, "use_transform_limit")
        row.label()

        row = layout.row()
        row.label(text="Convert:")
        row.prop(con, "owner_space", text="")

    def COPY_ROTATION(self, context, layout, con):
        self.target_template(layout, con)

        split = layout.split()

        col = split.column()
        col.prop(con, "use_x", text="X")
        sub = col.column()
        sub.active = con.use_x
        sub.prop(con, "invert_x", text="Invert")

        col = split.column()
        col.prop(con, "use_y", text="Y")
        sub = col.column()
        sub.active = con.use_y
        sub.prop(con, "invert_y", text="Invert")

        col = split.column()
        col.prop(con, "use_z", text="Z")
        sub = col.column()
        sub.active = con.use_z
        sub.prop(con, "invert_z", text="Invert")

        layout.prop(con, "use_offset")

        self.space_template(layout, con)

    def COPY_LOCATION(self, context, layout, con):
        self.target_template(layout, con)

        split = layout.split()

        col = split.column()
        col.prop(con, "use_x", text="X")
        sub = col.column()
        sub.active = con.use_x
        sub.prop(con, "invert_x", text="Invert")

        col = split.column()
        col.prop(con, "use_y", text="Y")
        sub = col.column()
        sub.active = con.use_y
        sub.prop(con, "invert_y", text="Invert")

        col = split.column()
        col.prop(con, "use_z", text="Z")
        sub = col.column()
        sub.active = con.use_z
        sub.prop(con, "invert_z", text="Invert")

        layout.prop(con, "use_offset")

        self.space_template(layout, con)

    def COPY_SCALE(self, context, layout, con):
        self.target_template(layout, con)

        row = layout.row(align=True)
        row.prop(con, "use_x", text="X")
        row.prop(con, "use_y", text="Y")
        row.prop(con, "use_z", text="Z")

        layout.prop(con, "use_offset")

        self.space_template(layout, con)

    def MAINTAIN_VOLUME(self, context, layout, con):

        row = layout.row()
        row.label(text="Free:")
        row.prop(con, "free_axis", expand=True)

        layout.prop(con, "volume")

        self.space_template(layout, con)

    def COPY_TRANSFORMS(self, context, layout, con):
        self.target_template(layout, con)

        self.space_template(layout, con)

    #def SCRIPT(self, context, layout, con):

    def ACTION(self, context, layout, con):
        self.target_template(layout, con)

        layout.prop(con, "action")

        layout.prop(con, "transform_channel")

        split = layout.split()

        col = split.column(align=True)
        col.label(text="Action Length:")
        col.prop(con, "frame_start", text="Start")
        col.prop(con, "frame_end", text="End")

        col = split.column(align=True)
        col.label(text="Target Range:")
        col.prop(con, "min", text="Min")
        col.prop(con, "max", text="Max")

        row = layout.row()
        row.label(text="Convert:")
        row.prop(con, "target_space", text="")

    def LOCKED_TRACK(self, context, layout, con):
        self.target_template(layout, con)

        row = layout.row()
        row.label(text="To:")
        row.prop(con, "track_axis", expand=True)

        row = layout.row()
        row.label(text="Lock:")
        row.prop(con, "lock_axis", expand=True)

    def LIMIT_DISTANCE(self, context, layout, con):
        self.target_template(layout, con)

        col = layout.column(align=True)
        col.prop(con, "distance")
        col.operator("constraint.limitdistance_reset")

        row = layout.row()
        row.label(text="Clamp Region:")
        row.prop(con, "limit_mode", text="")

    def STRETCH_TO(self, context, layout, con):
        self.target_template(layout, con)

        split = layout.split()

        col = split.column()
        col.prop(con, "rest_length", text="Rest Length")

        col = split.column()
        col.operator("constraint.stretchto_reset", text="Reset")

        col = layout.column()
        col.prop(con, "bulge", text="Volume Variation")

        row = layout.row()
        row.label(text="Volume:")
        row.prop(con, "volume", expand=True)

        row.label(text="Plane:")
        row.prop(con, "keep_axis", expand=True)

    def FLOOR(self, context, layout, con):
        self.target_template(layout, con)

        split = layout.split()

        col = split.column()
        col.prop(con, "use_sticky")

        col = split.column()
        col.prop(con, "use_rotation")

        layout.prop(con, "offset")

        row = layout.row()
        row.label(text="Min/Max:")
        row.prop(con, "floor_location", expand=True)

        self.space_template(layout, con)

    def RIGID_BODY_JOINT(self, context, layout, con):
        self.target_template(layout, con, subtargets=False)

        layout.prop(con, "pivot_type")
        layout.prop(con, "child")

        split = layout.split()

        col = split.column()
        col.prop(con, "use_linked_collision", text="Linked Collision")

        col = split.column()
        col.prop(con, "show_pivot", text="Display Pivot")

        split = layout.split()

        col = split.column(align=True)
        col.label(text="Pivot:")
        col.prop(con, "pivot_x", text="X")
        col.prop(con, "pivot_y", text="Y")
        col.prop(con, "pivot_z", text="Z")

        col = split.column(align=True)
        col.label(text="Axis:")
        col.prop(con, "axis_x", text="X")
        col.prop(con, "axis_y", text="Y")
        col.prop(con, "axis_z", text="Z")
        

        if con.pivot_type == 'CONE_TWIST':
            layout.label(text="Limits:")
            split = layout.split()
        
            col = split.column(align=True)
            col.prop(con, "use_angular_limit_x", text="Angular X")
            col.prop(con, "use_angular_limit_y", text="Angular Y")
            col.prop(con, "use_angular_limit_z", text="Angular Z")
                
            col = split.column()
            col.prop(con, "limit_cone_min", text="")
            col = split.column()
            col.prop(con, "limit_cone_max", text="")

        elif con.pivot_type == 'GENERIC_6_DOF':
            layout.label(text="Limits:")
            split = layout.split()
            
            col = split.column(align=True)
            col.prop(con, "use_limit_x", text="X")
            col.prop(con, "use_limit_y", text="Y")
            col.prop(con, "use_limit_z", text="Z")
            col.prop(con, "use_angular_limit_x", text="Angular X")
            col.prop(con, "use_angular_limit_y", text="Angular Y")
            col.prop(con, "use_angular_limit_z", text="Angular Z")
                
            col = split.column()
            col.prop(con, "limit_generic_min", text="")
            col = split.column()
            col.prop(con, "limit_generic_max", text="")
      
    def CLAMP_TO(self, context, layout, con):
        self.target_template(layout, con)

        row = layout.row()
        row.label(text="Main Axis:")
        row.prop(con, "main_axis", expand=True)

        row = layout.row()
        row.prop(con, "use_cyclic")

    def TRANSFORM(self, context, layout, con):
        self.target_template(layout, con)

        layout.prop(con, "use_motion_extrapolate", text="Extrapolate")

        col = layout.column()
        col.row().label(text="Source:")
        col.row().prop(con, "map_from", expand=True)

        split = layout.split()

        sub = split.column(align=True)
        sub.label(text="X:")
        sub.prop(con, "from_min_x", text="Min")
        sub.prop(con, "from_max_x", text="Max")

        sub = split.column(align=True)
        sub.label(text="Y:")
        sub.prop(con, "from_min_y", text="Min")
        sub.prop(con, "from_max_y", text="Max")

        sub = split.column(align=True)
        sub.label(text="Z:")
        sub.prop(con, "from_min_z", text="Min")
        sub.prop(con, "from_max_z", text="Max")

        split = layout.split()

        col = split.column()
        col.label(text="Destination:")
        col.row().prop(con, "map_to", expand=True)

        split = layout.split()

        col = split.column()
        col.label(text="X:")
        col.row().prop(con, "map_to_x_from", expand=True)

        sub = col.column(align=True)
        sub.prop(con, "to_min_x", text="Min")
        sub.prop(con, "to_max_x", text="Max")

        col = split.column()
        col.label(text="Y:")
        col.row().prop(con, "map_to_y_from", expand=True)

        sub = col.column(align=True)
        sub.prop(con, "to_min_y", text="Min")
        sub.prop(con, "to_max_y", text="Max")

        col = split.column()
        col.label(text="Z:")
        col.row().prop(con, "map_to_z_from", expand=True)

        sub = col.column(align=True)
        sub.prop(con, "to_min_z", text="Min")
        sub.prop(con, "to_max_z", text="Max")

        self.space_template(layout, con)

    def SHRINKWRAP(self, context, layout, con):
        self.target_template(layout, con, False)

        layout.prop(con, "distance")
        layout.prop(con, "shrinkwrap_type")

        if con.shrinkwrap_type == 'PROJECT':
            row = layout.row(align=True)
            row.prop(con, "use_x")
            row.prop(con, "use_y")
            row.prop(con, "use_z")

    def DAMPED_TRACK(self, context, layout, con):
        self.target_template(layout, con)

        row = layout.row()
        row.label(text="To:")
        row.prop(con, "track_axis", expand=True)

    def SPLINE_IK(self, context, layout, con):
        self.target_template(layout, con)

        col = layout.column()
        col.label(text="Spline Fitting:")
        col.prop(con, "chain_count")
        col.prop(con, "use_even_divisions")
        col.prop(con, "use_chain_offset")

        col = layout.column()
        col.label(text="Chain Scaling:")
        col.prop(con, "use_y_stretch")
        col.prop(con, "xz_scale_mode")
        col.prop(con, "use_curve_radius")

    def PIVOT(self, context, layout, con):
        self.target_template(layout, con)

        if con.target:
            col = layout.column()
            col.prop(con, "offset", text="Pivot Offset")
        else:
            col = layout.column()
            col.prop(con, "use_relative_location")
            if con.use_relative_location:
                col.prop(con, "offset", text="Relative Pivot Point")
            else:
                col.prop(con, "offset", text="Absolute Pivot Point")

        col = layout.column()
        col.prop(con, "rotation_range", text="Pivot When")

    def SCRIPT(self, context, layout, con):
        layout.label("blender 2.5 has no py-constraints")


class OBJECT_PT_constraints(ConstraintButtonsPanel, bpy.types.Panel):
    bl_label = "Object Constraints"
    bl_context = "constraint"

    @classmethod
    def poll(cls, context):
        return (context.object)

    def draw(self, context):
        layout = self.layout

        ob = context.object

        layout.operator_menu_enum("object.constraint_add", "type")

        for con in ob.constraints:
            self.draw_constraint(context, con)


class BONE_PT_constraints(ConstraintButtonsPanel, bpy.types.Panel):
    bl_label = "Bone Constraints"
    bl_context = "bone_constraint"

    @classmethod
    def poll(cls, context):
        return (context.pose_bone)

    def draw(self, context):
        layout = self.layout

        layout.operator_menu_enum("pose.constraint_add", "type")

        for con in context.pose_bone.constraints:
            self.draw_constraint(context, con)


def register():
    pass


def unregister():
    pass

if __name__ == "__main__":
    register()
