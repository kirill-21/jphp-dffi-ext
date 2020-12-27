package org.develnext.jphp.ext.system.classes;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Tlhelp32;
import org.develnext.jphp.ext.system.DFFIExtension;
import php.runtime.Memory;
import php.runtime.annotation.Reflection;
import php.runtime.annotation.Reflection.Signature;
import php.runtime.env.Environment;
import php.runtime.env.TraceInfo;
import php.runtime.lang.BaseObject;
import php.runtime.memory.ArrayMemory;
import php.runtime.reflection.ClassEntity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Reflection.Name("DFFIStruct")
@Reflection.Namespace(DFFIExtension.NS)
public class DFFIStruct extends BaseObject {

    public Structure struct;

    public DFFIStruct(Environment env, ClassEntity clazz) {
        super(env, clazz);
    }

    public DFFIStruct(Environment env, Structure _struct) {
        super(env);
        this.struct = _struct;
    }

    @Signature
    public void __construct(Environment env, String name, ArrayMemory types) throws ClassNotFoundException {
        Class[] classes = Helper.ConvertJavaClassByType(env, types);

        if (name.equals("MODULEENTRY32W"))
            this.struct = new Tlhelp32.MODULEENTRY32W();
        else
            this.struct = createStructure(name, classes);
    }

    @Reflection.Signature
    public void setValue(Environment env, TraceInfo trace, int index, String valueType, Memory value) throws IllegalAccessException {
        this.struct.getClass().getDeclaredFields()[index].set(this.struct, Helper.ConvertMemoryToObject(env, valueType, value));
    }

    @Reflection.Signature
    public ArrayMemory getResponse() throws IllegalAccessException {
        final ArrayMemory fieldsArray = new ArrayMemory();

        if (this.struct instanceof Tlhelp32.MODULEENTRY32W) {
            System.out.println("Decoding MODULEENTRY32W structure...");

            final Tlhelp32.MODULEENTRY32W moduleEntry32W = ((Tlhelp32.MODULEENTRY32W) this.struct);

            fieldsArray.add(moduleEntry32W.dwSize.intValue());
            fieldsArray.add(moduleEntry32W.th32ModuleID.intValue());
            fieldsArray.add(moduleEntry32W.th32ProcessID.intValue());
            fieldsArray.add(moduleEntry32W.GlblcntUsage.intValue());
            fieldsArray.add(moduleEntry32W.ProccntUsage.intValue());

            System.out.println("Converting modBaseAddr to int");
            fieldsArray.add(Pointer.nativeValue(moduleEntry32W.modBaseAddr));

            fieldsArray.add(moduleEntry32W.modBaseSize.intValue());

            System.out.println("Converting hModule to int");
            fieldsArray.add(Pointer.nativeValue(moduleEntry32W.hModule.getPointer()));

            System.out.println("Converting szModule && szExePath to string");

            fieldsArray.add(Native.toString(moduleEntry32W.szModule));
            fieldsArray.add(Native.toString(moduleEntry32W.szExePath));

        } else {
            Field[] fields = this.struct.getClass().getDeclaredFields();

            for (Field itm : fields) {
                Object value = itm.get(this.struct);
                fieldsArray.add(Helper.ConvertObjectToMemory(value.getClass(), value));
            }
        }

        return fieldsArray;
    }

    public static Structure createStructure(String className, Class[] classes) {
        return allocate(className, classes);
    }

    public static class AutoStructure extends Structure {
        @Override
        protected java.util.List getFieldOrder() {
            Field[] declared = getClass().getDeclaredFields();
            ArrayList<String> ordered = new ArrayList<>(declared.length);
            for (Field f : declared)
                ordered.add(f.getName());
            return ordered;
        }
    }

    public static Structure allocate(final String name, Class... classes) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(0xCAFEBABE);
            dos.writeShort(0);
            dos.writeShort(49); // Java 5

            ByteArrayOutputStream constants = new ByteArrayOutputStream();
            DataOutputStream con = new DataOutputStream(constants);
            AtomicInteger idx = new AtomicInteger(0);

            int superClass = classref(con, idx, AutoStructure.class.getName());
            int thisClass = classref(con, idx, name);

            ByteArrayOutputStream methods = new ByteArrayOutputStream();
            DataOutputStream met = new DataOutputStream(methods);
            met.writeShort(1);
            met.writeShort(0x0001);
            met.writeShort(utf(con, idx, "<init>"));
            met.writeShort(utf(con, idx, "()V"));
            met.writeShort(1);

            met.writeShort(utf(con, idx, "Code"));
            met.writeInt(2 + 2 + 4 + 1 + 1 + 2 + 1 + 2 + 2); // LENGTH
            met.writeShort(1); // 1 stack and locals
            met.writeShort(1);

            met.writeInt(1 + 1 + 2 + 1);
            met.writeByte(42); // aload_0
            met.writeByte(183); // invokespecial super init
            met.writeShort(methodref(con, idx, AutoStructure.class.getName(), "<init>", "()V"));
            met.writeByte(177); // return

            met.writeShort(0); // No exceptions or attributes
            met.writeShort(0);

            ByteArrayOutputStream fields = new ByteArrayOutputStream();
            DataOutputStream fie = new DataOutputStream(fields);

            fie.writeShort(classes.length);
            for (Class d : classes) {
                fie.writeShort(0x0001); // ACC_PUBLIC
                // Random name
                fie.writeShort(utf(con, idx, UUID.randomUUID().toString()));
                HashMap<String, String> typeMap = new HashMap<>();

                typeMap.put("int", "I");
                typeMap.put("boolean", "Z");
                typeMap.put("byte", "B");
                typeMap.put("char", "C");
                typeMap.put("short", "S");
                typeMap.put("double", "D");
                typeMap.put("float", "F");
                typeMap.put("long", "J");

                typeMap.put("[C", "[C");
                typeMap.put("[B", "[B");

                String descriptor = typeMap.get(d.getName());

                if (descriptor == null)
                    descriptor = String.format("L%s;", d.getName().replace(".", "/"));

                fie.writeShort(utf(con, idx, descriptor));
                fie.writeShort(0); // No attributes
            }

            dos.writeShort(idx.get() + 1);
            dos.write(constants.toByteArray());

            // Class
            dos.writeShort(0x0001); // Public
            dos.writeShort(thisClass);
            dos.writeShort(superClass);
            dos.writeShort(0); // No implemented interfaces
            dos.write(fields.toByteArray());
            dos.write(methods.toByteArray());
            dos.writeShort(0); // No attributes


            final Object structure = new ClassLoader() {
                public Class defineClass(byte[] bytes) {
                    return super.defineClass(name, bytes, 0, bytes.length);
                }
            }.defineClass(baos.toByteArray()).newInstance();


            //logger.println(Dumper.dump(structure));

            return (Structure) structure;
        } catch (IOException | ReflectiveOperationException e) {
            return null;
        }
    }

    private static int classref(DataOutputStream out, AtomicInteger size, String id) throws IOException {
        int utf = utf(out, size, id.replace(".", "/"));
        out.writeByte(7); // Class pointing to ^
        out.writeShort(utf);
        return size.addAndGet(1);
    }

    private static int utf(DataOutputStream out, AtomicInteger size, String id) throws IOException {
        out.writeByte(1); // UTF-8
        out.writeUTF(id);
        return size.addAndGet(1);
    }

    private static int methodref(DataOutputStream out, AtomicInteger size, String clazz, String name, String descriptor) throws IOException {
        int classIdx = classref(out, size, clazz);
        int descIdx = nametype(out, size, name, descriptor);
        out.writeByte(10); // Methodref
        out.writeShort(classIdx);
        out.writeShort(descIdx);
        return size.addAndGet(1);
    }

    private static int nametype(DataOutputStream out, AtomicInteger size, String name, String descriptor) throws IOException {
        int nameIdx = utf(out, size, name);
        int descIdx = utf(out, size, descriptor);
        out.writeByte(12); // Nametype
        out.writeShort(nameIdx);
        out.writeShort(descIdx);
        return size.addAndGet(1);
    }

}