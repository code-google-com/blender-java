/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package blender.makesdna;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jladere
 */
public class AutoGenSDNA {

    public static void generate(SDNA sdna, String path) {
        PrintWriter printWriter;
        
        List<String> structTypes = new ArrayList<String>(sdna.nr_structs+20);
        for (int i = 0; i < sdna.nr_structs; i++) {
            short[] struct = sdna.structs[i];
            if (sdna.types[struct[0]].equals("Object")) {
                structTypes.add("bObject");
            }
            else {
                structTypes.add(sdna.types[struct[0]]);
            }
        }
        structTypes.add("Object");
        structTypes.add("byte");
        structTypes.add("short");
        structTypes.add("int");
        structTypes.add("long");
        structTypes.add("float");
        structTypes.add("double");

        for (int i=0; i<sdna.nr_structs; i++) {
            short[] struct = sdna.structs[i];
            try {
                String classType = sdna.types[struct[0]];
                if (classType.equals("Object")) {
                    classType = "bObject";
                }
                String filename = classType+".java";
                File file = new File(path+filename);
                printWriter = new PrintWriter(file);
                boolean spacelink = false;
                boolean spacelink25 = false;
                boolean link = false;
                boolean id = false;
                boolean modifier = false;
                if (!classType.equals("SpaceLink") && struct[1] > 2 && sdna.names[struct[3]].equals("*next") && sdna.names[struct[5]].equals("*prev") && sdna.names[struct[7]].equals("spacetype")) {
                    spacelink = true;
                }
                if (!classType.equals("SpaceLink") && struct[1] > 2 && sdna.names[struct[3]].equals("*next") && sdna.names[struct[5]].equals("*prev") && sdna.names[struct[7]].equals("regionbase")) {
                    spacelink25 = true;
                }
                else if (!classType.equals("Link") && struct[1] > 1 && sdna.names[struct[3]].equals("*next") && sdna.names[struct[5]].equals("*prev")) {
                    link = true;
                }
                else if (struct[1] > 0 && sdna.names[struct[3]].equals("id")) {
                    id = true;
                }
                else if (struct[1] > 0 && sdna.types[struct[2]].equals("ModifierData") && sdna.names[struct[3]].equals("modifier")) {
                    modifier = true;
                }
                StringBuilder sb = new StringBuilder("package blender.makesdna.sdna;\n\n");
                sb.append("import java.nio.ByteBuffer;\n");
                sb.append("import java.io.DataOutput;\n");
                sb.append("import java.io.IOException;\n");
                sb.append("import java.util.Arrays;\n\n");
                sb.append("public class ");
                sb.append(classType);

                if (classType.equals("Link") || classType.equals("ListBase")) {
                    sb.append("<T>");
                }

                if (spacelink) {
                    sb.append(" extends SpaceLink");
                }
                if (spacelink25) {
                    sb.append(" extends SpaceLink");
                }
                if (link) {
                    if (classType.equals("ID") ||
                            classType.equals("ModifierData") ||
                            classType.equals("SpaceLink")) {
                        sb.append(" extends Link");
                    }
                    else {
                        sb.append(" extends Link<");
                        sb.append(classType);
                        sb.append(">");
                    }
                }
                else if (id) {
                    sb.append(" extends ID");
                }
                else if (modifier) {
                    sb.append(" extends ModifierData");
                }
                sb.append(" implements DNA, Cloneable { // #");
                sb.append(i);
                sb.append("\n");

                if (!classType.equals("Link") &&
                        !classType.equals("SpaceLink") &&
                        !classType.equals("ID") &&
                        !classType.equals("ModifierData") &&
                        !classType.equals("ListBase")) {
                    sb.append("  public ");
                    sb.append(classType);
                    sb.append("[] myarray;\n");
                }

                if (id) {
                    sb.append("  public ID id = (ID)this;\n");
                }
                if (modifier) {
                    sb.append("  public ModifierData modifier = (ModifierData)this;\n");
                }
                
                List<String> modTypes = new ArrayList<String>(struct[1]);
                List<String> modNames = new ArrayList<String>(struct[1]);
                List<String> modNamesExts = new ArrayList<String>(struct[1]);
                List<String> descriptions = new ArrayList<String>(struct[1]);
                for (int a=0, sp_i=2; a<struct[1]; a++, sp_i += 2) {
                    String type = sdna.types[struct[sp_i]];
                    String name = sdna.names[struct[sp_i + 1]];
                    String ext = "";
                    String descr = String.valueOf(sdna.typelens[struct[sp_i]]);
                    // Fix Types
                    if (type.equals("char")) {
                        type = "byte";
                    }
                    else if (type.equals("uint64_t")) {
                        type = "long";
                    }
                    else if (type.equals("Object")) {
                        type = "bObject";
                    }
                    else if (type.equals("void") ||
                            // these are super classes
                            type.equals("Link") ||
                            type.equals("SpaceLink") ||
                            type.equals("ModifierData") ||
                            type.equals("ID")) {
                        type = "Object";
                    }
                    if (!structTypes.contains(type)) {
                        descr = "("+type+") "+descr;
                        type = "Object";
                    }
                    // Fix Names
                    if (name.startsWith("*")) {
                        if (type.equals("byte") ||
                                type.equals("short") ||
                                type.equals("int") ||
                                type.equals("long") ||
                                type.equals("float") ||
                                type.equals("double")) {
                            type = "Object";
                        }
                        name = name.substring(1);
                        descr = "ptr "+descr;
                    }
                    else {
                        if (!type.equals("byte") &&
                                !type.equals("short") &&
                                !type.equals("int") &&
                                !type.equals("long") &&
                                !type.equals("float") &&
                                !type.equals("double")) {
                            ext = " = new "+type+"()";
                                }
                    }
                    if (name.contains("[")) {
                        int arrayLevel = name.replaceAll("[^\\[]", "").length();
                        String array = name.substring(name.indexOf("["), name.lastIndexOf("]")+1);
                        name = name.substring(0, name.indexOf("["));
                        ext = " = new "+type+array;
                        for (int lev=0; lev<arrayLevel; lev++) {
                            type = type+"[]";
                        }
                    }
                    if (name.startsWith("*")) {
                        name = name.substring(1);
                        if (!type.equals("Object")) {
                            type = type+"[]";
                        }
                    }
                    if (name.startsWith("(")) {
                        name = name.substring(2, name.indexOf(")"));
                        ext = "";
                        descr = "func ptr "+descr;
                        if (type.equals("int")) {
                            type = "Object";
                        }
                    }
                    // catch duplicate or empty names
                    if (modNames.contains(name) || name.trim().isEmpty()) {
                        continue;
                    }
                    if (classType.equals("SpaceLink") && a == 5) {
                        continue;
                    }
                    if ((id || modifier) && a == 0) {
                        continue;
                    }
                    modTypes.add(type);
                    modNames.add(name);
                    modNamesExts.add(ext);
                    descriptions.add(descr);
                    if (spacelink && a >= 0 && a <= 4) {
                        continue;
                    }
                    if (spacelink25 && a >= 0 && a <= 3) {
                        continue;
                    }
                    if (link && (a == 0 || a == 1)) {
                        continue;
                    }
                    if (classType.equals("Link") || classType.equals("ListBase")) {
                        sb.append("  public T ");
                        sb.append(name);
                        sb.append(";\n");
                        continue;
                    }
                    sb.append("  public ");
                    sb.append(type);
                    sb.append(" ");
                    sb.append(name);
                    sb.append(ext);
                    sb.append("; // ");
                    sb.append(descr);
                    sb.append("\n");
                }

                sb.append("\n");

                sb.append("  public void read(ByteBuffer buffer) {\n");
                if (id || modifier) {
                    sb.append("    super.read(buffer);\n");
                }
                for (int j=0; j<modTypes.size(); j++) {
                    if (modTypes.get(j)== null) {
                        continue;
                    }

                    if (classType.equals("Link") || classType.equals("ListBase")) {
                        sb.append("    ");
                        sb.append(modNames.get(j));
                        sb.append(" = (T)(Object)DNATools.ptr(buffer);\n");
                        continue;
                    }

                    String reader = "    ";
                    if (modTypes.get(j).equals("byte")) {
                        reader = reader + modNames.get(j) + " = buffer.get();\n";
                    }
                    else if (modTypes.get(j).equals("short")) {
                        reader = reader + modNames.get(j) + " = buffer.getShort();\n";
                    }
                    else if (modTypes.get(j).equals("int")) {
                        reader = reader + modNames.get(j) + " = buffer.getInt();\n";
                    }
                    else if (modTypes.get(j).equals("float")) {
                        reader = reader + modNames.get(j) + " = buffer.getFloat();\n";
                    }
                    else if (modTypes.get(j).equals("double")) {
                        reader = reader + modNames.get(j) + " = buffer.getDouble();\n";
                    }
                    else if (!modTypes.get(j).endsWith("]") && descriptions.get(j).contains("ptr")) {
                        if (modTypes.get(j).equals("Object")) {
                            reader = reader + modNames.get(j) + " = DNATools.ptr(buffer); // get ptr\n";
                        }
                        else {
                            reader = reader + modNames.get(j) + " = DNATools.link(DNATools.ptr(buffer), "+modTypes.get(j)+".class); // get ptr\n";
                        }
                    }
                    else if (modTypes.get(j).endsWith("]") && modNamesExts.get(j).contains("=")) {
                        int arrayLevel = modTypes.get(j).replaceAll("[^\\[]", "").length();
                        String name = modNames.get(j);
                        if (arrayLevel == 1) {
                            if (modTypes.get(j).startsWith("byte")) {
                                reader = reader + "buffer.get(" + name + ");\n";
                            }
                            else if (modTypes.get(j).startsWith("short")) {
                                reader = reader + "for(int i=0;i<"+name+".length;i++) "+name+"[i]=buffer.getShort();\n";
                            }
                            else if (modTypes.get(j).startsWith("int")) {
                                reader = reader + "for(int i=0;i<"+name+".length;i++) "+name+"[i]=buffer.getInt();\n";
                            }
                            else if (modTypes.get(j).startsWith("float")) {
                                reader = reader + "for(int i=0;i<"+name+".length;i++) "+name+"[i]=buffer.getFloat();\n";
                            }
                            else if (modTypes.get(j).startsWith("double")) {
                                reader = reader + "for(int i=0;i<"+name+".length;i++) "+name+"[i]=buffer.getDouble();\n";
                            }
                            else if (descriptions.get(j).contains("ptr")) {
                                String type = modTypes.get(j).substring(0, modTypes.get(j).indexOf("["));
                                reader = reader + "for(int i=0;i<"+name+".length;i++) "+name+"[i]=DNATools.link(DNATools.ptr(buffer), "+type+".class);\n";
                            }
                            else {
                                String type = modTypes.get(j).substring(0, modTypes.get(j).indexOf("["));
                                reader = reader + "for(int i=0;i<"+name+".length;i++) { "+name+"[i]=new "+type+"(); "+name+"[i].read(buffer); }\n";
                            }
                        }
                        else if (arrayLevel == 2) {
                            String loop = "for(int i=0; i<"+name+".length; i++) ";
                            if (modTypes.get(j).startsWith("byte")) {
                                reader = reader + loop + "buffer.get(" + name + "[i]);\n";
                            }
                            else if (modTypes.get(j).startsWith("short")) {
                                reader = reader + "for(int i=0;i<"+name+".length;i++) for(int j=0;j<"+name+"[i].length;j++) "+name+"[i][j]=buffer.getShort();\n";
                            }
                            else if (modTypes.get(j).startsWith("int")) {
                                reader = reader + "for(int i=0;i<"+name+".length;i++) for(int j=0;j<"+name+"[i].length;j++) "+name+"[i][j]=buffer.getInt();\n";
                            }
                            else if (modTypes.get(j).startsWith("float")) {
                                reader = reader + "for(int i=0;i<"+name+".length;i++) for(int j=0;j<"+name+"[i].length;j++) "+name+"[i][j]=buffer.getFloat();\n";
                            }
                            else if (modTypes.get(j).startsWith("double")) {
                                reader = reader + "for(int i=0;i<"+name+".length;i++) for(int j=0;j<"+name+"[i].length;j++) "+name+"[i][j]=buffer.getDouble();\n";
                            }
                            else {
                                reader = reader + "// UNKNOWN\n";
                            }
                        }
                    }
                    else if (modNamesExts.get(j).contains("=")) {
                        String name = modNames.get(j);
                        reader = reader + name + ".read(buffer);\n";
                    }
                    else if (descriptions.get(j).contains("ptr")) {
                        if (modTypes.get(j).equals("Object")) {
                            reader = reader + modNames.get(j) + " = DNATools.ptr(buffer); // get ptr\n";
                        }
                        else {
                            reader = reader + modNames.get(j) + " = DNATools.link(DNATools.ptr(buffer), "+modTypes.get(j)+".class); // get ptr\n";
                        }
                    }
                    else {
                        reader = reader + "// UNKNOWN\n";
                    }
                    sb.append(reader);
                }
                sb.append("  }\n");

                sb.append("  public void write(DataOutput buffer) throws IOException {\n");

                if (id || modifier) {
                    sb.append("    super.write(buffer);\n");
                }
                for (int j=0; j<modTypes.size(); j++) {
                    if (modTypes.get(j)== null) {
                        continue;
                    }

                    if (classType.equals("Link") || classType.equals("ListBase")) {
                        sb.append("    buffer.writeInt(");
                        sb.append(modNames.get(j));
                        sb.append("!=null?");
                        sb.append(modNames.get(j));
                        sb.append(".hashCode():0);\n");
                        continue;
                    }

                    String writer = "    ";
                    if (modTypes.get(j).equals("byte")) {
                        writer = writer + "buffer.writeByte(" + modNames.get(j) + ");\n";
                    }
                    else if (modTypes.get(j).equals("short")) {
                        writer = writer + "buffer.writeShort(" + modNames.get(j) + ");\n";
                    }
                    else if (modTypes.get(j).equals("int")) {
                        writer = writer + "buffer.writeInt(" + modNames.get(j) + ");\n";
                    }
                    else if (modTypes.get(j).equals("float")) {
                        writer = writer + "buffer.writeFloat(" + modNames.get(j) + ");\n";
                    }
                    else if (modTypes.get(j).equals("double")) {
                        writer = writer + "buffer.writeDouble(" + modNames.get(j) + ");\n";
                    }
                    else if (!modTypes.get(j).endsWith("]") && descriptions.get(j).contains("ptr")) {
                        writer = writer + "buffer.writeInt(" + modNames.get(j) + "!=null?" + modNames.get(j) + ".hashCode():0);\n";
                    }
                    else if (modTypes.get(j).endsWith("]") && modNamesExts.get(j).contains("=")) {
                        int arrayLevel = modTypes.get(j).replaceAll("[^\\[]", "").length();
                        String name = modNames.get(j);
                        if (arrayLevel == 1) {
                            if (modTypes.get(j).startsWith("byte")) {
                                writer = writer + "buffer.write(" + name + ");\n";
                            }
                            else if (modTypes.get(j).startsWith("short")) {
                                writer = writer + "for(int i=0;i<"+name+".length;i++) buffer.writeShort("+name+"[i]);\n";
                            }
                            else if (modTypes.get(j).startsWith("int")) {
                                writer = writer + "for(int i=0;i<"+name+".length;i++) buffer.writeInt("+name+"[i]);\n";
                            }
                            else if (modTypes.get(j).startsWith("float")) {
                                writer = writer + "for(int i=0;i<"+name+".length;i++) buffer.writeFloat("+name+"[i]);\n";
                            }
                            else if (modTypes.get(j).startsWith("double")) {
                                writer = writer + "for(int i=0;i<"+name+".length;i++) buffer.writeDouble("+name+"[i]);\n";
                            }
                            else if (descriptions.get(j).contains("ptr")) {
                                writer = writer + "for(int i=0;i<"+name+".length;i++) buffer.writeInt("+name+"[i]!=null?"+name+"[i].hashCode():0);\n";
                            }
                            else {
                                writer = writer + "for(int i=0;i<"+name+".length;i++) "+name+"[i].write(buffer);\n";
                            }
                        }
                        else if (arrayLevel == 2) {
                            String loop = "for(int i=0; i<"+name+".length; i++) ";
                            if (modTypes.get(j).startsWith("byte")) {
                                writer = writer + loop + "buffer.write(" + name + "[i]);\n";
                            }
                            else if (modTypes.get(j).startsWith("short")) {
                                writer = writer + loop + " for(int j=0;j<"+name+"[i].length;j++) buffer.writeShort("+name+"[i][j]);\n";
                            }
                            else if (modTypes.get(j).startsWith("int")) {
                                writer = writer + loop + " for(int j=0;j<"+name+"[i].length;j++) buffer.writeInt("+name+"[i][j]);\n";
                            }
                            else if (modTypes.get(j).startsWith("float")) {
                                writer = writer + loop + " for(int j=0;j<"+name+"[i].length;j++) buffer.writeFloat("+name+"[i][j]);\n";
                            }
                            else if (modTypes.get(j).startsWith("double")) {
                                writer = writer + loop + " for(int j=0;j<"+name+"[i].length;j++) buffer.writeDouble("+name+"[i][j]);\n";
                            }
                            else {
                                writer = writer + "// UNKNOWN\n";
                            }
                        }
                    }
                    else if (modNamesExts.get(j).contains("=")) {
                        String name = modNames.get(j);
                        writer = writer + name + ".write(buffer);\n";
                    }
                    else if (descriptions.get(j).contains("ptr")) {
                        writer = writer + "buffer.writeInt(" + modNames.get(j) + "!=null?" + modNames.get(j) + ".hashCode():0);\n";
                    }
                    else {
                        writer = writer + "// UNKNOWN\n";
                    }
                    sb.append(writer);
                }

                sb.append("  }\n");
                
                sb.append("  public Object setmyarray(Object array) {\n");

                if (!classType.equals("Link") &&
                        !classType.equals("SpaceLink") &&
                        !classType.equals("ID") &&
                        !classType.equals("ModifierData") &&
                        !classType.equals("ListBase")) {
                    sb.append("    myarray = (");
                    sb.append(classType);
                    sb.append("[])array;\n");
                }
                sb.append("    return this;\n");

                sb.append("  }\n");

                sb.append("  public Object getmyarray() {\n");

                if (!classType.equals("Link") &&
                        !classType.equals("SpaceLink") &&
                        !classType.equals("ID") &&
                        !classType.equals("ModifierData") &&
                        !classType.equals("ListBase")) {
                    sb.append("    return myarray;\n");
                }
                else {
                    sb.append("    return null;\n");
                }

                sb.append("  }\n");

                sb.append("  public String toString() {\n");
                String printer = "    StringBuilder sb = new StringBuilder(\"" + classType + ":\\n\");\n";
                if (id || modifier) {
                    printer += "    sb.append(super.toString());\n";
                }
                for (int j=0; j<modTypes.size(); j++) {
                    if (modTypes.get(j) == null) {
                        continue;
                    }
                    String name = modNames.get(j);
                    if (spacelink && (name.equals("next") || name.equals("prev"))) {
                        continue;
                    }
                    if (spacelink25 && (name.equals("next") || name.equals("prev"))) {
                        continue;
                    }
                    if (link && (name.equals("next") || name.equals("prev"))) {
                        continue;
                    }
                    String printformat = name;
                    if (modTypes.get(j).equals("byte[]")) {
                        printformat = "new String("+name+")";
                    }
                    else if (modTypes.get(j).endsWith("]")) {
                        printformat = "Arrays.toString("+name+")";
                    }
                    printer += "    sb.append(\"  "
                            + name
                            + ": \").append("
                            + printformat
                            + ").append(\"\\n\");\n";
                }
                printer = printer + "    return sb.toString();\n";
                sb.append(printer);
                sb.append("  }\n");

                sb.append("  public ");
                sb.append(classType);
                sb.append(" copy() { try {return (");
                sb.append(classType);
                sb.append(")super.clone();} catch(CloneNotSupportedException ex) {return null;} }\n");

                sb.append("}");
                printWriter.println(sb.toString());
                printWriter.close();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        try {
            String filename = "DNATools.java";
            File file = new File(path+filename);
            printWriter = new PrintWriter(file);
            StringBuilder sb = new StringBuilder("package blender.makesdna.sdna;\n\n");
            sb.append("import java.nio.ByteBuffer;\n");
            sb.append("import java.util.Map;\n");
            sb.append("import java.util.HashMap;\n\n");
            sb.append("public class DNATools {\n");
            sb.append("  public static int pointerSize = 4;\n");
            sb.append("  private static Map<Integer, Object> pointerMap = new HashMap<Integer, Object>();\n");
            sb.append("  public static Class getClassType(int SDNAnr) {\n");
            sb.append("    switch (SDNAnr) {\n");
            for (int i=0; i<sdna.nr_structs; i++) {
                sb.append("      case ");
                sb.append(i);
                sb.append(": return ");
                sb.append(structTypes.get(i));
                sb.append(".class;\n");
            }
            sb.append("    }\n");
            sb.append("    return Object.class;\n");
            sb.append("  }\n");

            sb.append("  public static <T> T link(int pointer, Class<T> type) {\n");
            sb.append("    if (pointer==0 || type==Object.class || type.isArray()) return null;\n");
            sb.append("    if (pointerMap.containsKey(pointer)) {\n");
            sb.append("      Object existing = pointerMap.get(pointer);\n");
            sb.append("      if (type.isAssignableFrom(existing.getClass())) return type.cast(existing);\n");
            sb.append("      else return null;\n");
            sb.append("    } else {\n");
            sb.append("      try {\n");
            sb.append("        T temp = type.newInstance();\n");
            sb.append("        pointerMap.put(pointer, temp);\n");
            sb.append("        return temp;\n");
            sb.append("      } catch (Exception ex) { ex.printStackTrace(); return null; }\n");
            sb.append("    }\n");
            sb.append("  }\n");
            
            sb.append("  public static Integer ptr(ByteBuffer buffer) {\n");
            sb.append("    int pointer = 0;\n");
            sb.append("    if (pointerSize == 4) pointer = buffer.getInt();\n");
            sb.append("    else if (pointerSize == 8) pointer = (int)buffer.getLong();\n");
            sb.append("    return pointer;\n");
            sb.append("  }\n");

            sb.append("}\n");
            printWriter.println(sb.toString());
            printWriter.close();
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        try {
            String filename = "DNA.java";
            File file = new File(path+filename);
            printWriter = new PrintWriter(file);
            StringBuilder sb = new StringBuilder("package blender.makesdna.sdna;\n\n");
            sb.append("import java.nio.ByteBuffer;\n");
            sb.append("import java.io.DataOutput;\n");
            sb.append("import java.io.IOException;\n\n");
            sb.append("public interface DNA {\n");
            sb.append("  public void read(ByteBuffer buffer);\n");
            sb.append("  public void write(DataOutput buffer) throws IOException;\n");
            sb.append("  public Object setmyarray(Object array);\n");
            sb.append("  public Object getmyarray();\n");
            sb.append("}\n");
            printWriter.println(sb.toString());
            printWriter.close();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

}
