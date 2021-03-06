package org.develnext.jphp.ext.system.classes;

import com.sun.jna.*;
import com.sun.jna.ptr.*;
import php.runtime.Memory;
import php.runtime.env.Environment;
import php.runtime.lang.ForeachIterator;
import php.runtime.memory.ArrayMemory;
import php.runtime.memory.DoubleMemory;
import php.runtime.memory.LongMemory;
import php.runtime.memory.StringMemory;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Helper {
    public static Memory callFunction(Environment env, String lib, String returnType, String functionName, Memory types, Memory[] args)
            throws ClassNotFoundException {
        Memory returnValue = Memory.NULL;
        if (args != null) {
            ArrayMemory _types = (ArrayMemory) types.toValue(ArrayMemory.class);

            Object[] functionArgs = ConvertArrayMemoryByType(env, args, _types);
            Class type = ConvertToJavaClassByType(returnType);
            Object response = callFunction(lib, functionName, type, functionArgs);
            returnValue = ConvertObjectToMemory(type, response);
        }
        return returnValue;
    }

    public static Object callFunction(String lib, String functionName, Class type, Object[] functionArgs) {
        Function function = NativeLibrary.getInstance(lib).getFunction(functionName);
        return function.invoke(type, functionArgs);
    }

    public static Object ConvertMemoryToObject(Environment env, String valueType, Memory value) {
        //logger.println(String.format("CONVERT MEMORY TO OBJECT: %s %s", valueType, value.toString()));

        Object returnValue = null;

        switch (valueType) {
            case DFFIType.BOOL:
                returnValue = Boolean.valueOf(value.toBoolean());
                break;

            case DFFIType.INT:
                returnValue = Integer.valueOf(value.toInteger());
                break;

            case DFFIType.LONG:
                returnValue = Long.valueOf(value.toLong());
                break;

            case DFFIType.CHAR:
                returnValue = value;
                break;

            case DFFIType.DOUBLE:
                returnValue = Double.valueOf(value.toDouble());
                break;

            case DFFIType.FLOAT:
                returnValue = Float.valueOf(value.toFloat());
                break;

            case DFFIType.STRING:
                returnValue = value.toString();
                break;

            case DFFIType.WIDESTRING:
                returnValue = new WString(value.toString());
                break;

            case DFFIType.STRUCT:
                Structure struct = value.toObject(DFFIStruct.class).struct;
                returnValue = struct;
                break;

            case DFFIType.REFERENCE: {
                Object reference = null;
                DFFIReferenceValue _obj = value.toObject(DFFIReferenceValue.class);

                if (_obj.isReference()) {
                    reference = _obj.refval;
                } else {
                    reference = _obj._refval;
                }

                returnValue = reference;
                break;
            }

            case DFFIType.CHAR_ARRAY:
                returnValue = new char[value.toInteger()];
                break;
        }

        return returnValue;
    }

    public static Object[] ConvertArrayMemoryByType(Environment env, Memory[] array, ArrayMemory types) {
        ArrayList<Object> temp = new ArrayList<Object>();

        ForeachIterator iterator_types = types.getNewIterator(env, false, false);
        for (Memory value : array) {
            iterator_types.next();

            String valuType = iterator_types.getValue().toString().toUpperCase();
            Object result = ConvertMemoryToObject(env, valuType, value);
            temp.add(result);
        }

        return temp.toArray();
    }

    public static Class<?> ConvertToJavaClassByType(String cls) throws ClassNotFoundException {
        Class c = Class.class;

        String cls_up = cls.toUpperCase();

        switch (cls_up) {
            case "INT":
                c = int.class;
                break;
            case "BYTE":
                c = byte.class;
                break;
            case "CHAR":
                c = char.class;
                break;
            case "BOOL":
                c = boolean.class;
                break;
            case "LONG":
                c = long.class;
                break;
            case "DOUBLE":
                c = double.class;
                break;
            case "FLOAT":
                c = float.class;
                break;
            case "STRING":
                c = String.class;
                break;
            case "POINTER":
                c = Pointer.class;
                break;
            case "CHAR[]":
                c = char[].class;
        }

        return c;
    }

    public static Memory ConvertObjectToMemory(Class<?> type, Object value) {
        Memory returnValue = Memory.NULL;

        if (type == boolean.class || type == Boolean.class) {
            if ((boolean) value == true) {
                returnValue = Memory.TRUE;
            } else {
                returnValue = Memory.FALSE;
            }
        } else if (type == Pointer.class) {
            returnValue = new LongMemory(((Pointer) value).getLong(0));
        } else if (type == int.class || type == Integer.class) {
            returnValue = new LongMemory((int) value);
        } else if (type == long.class || type == Long.class) {
            returnValue = new LongMemory((long) value);
        } else if (type == float.class || type == Float.class) {
            returnValue = new DoubleMemory((float) value);
        } else if (type == double.class || type == Double.class) {
            returnValue = new DoubleMemory((double) value);
        } else if (type == String.class) {
            returnValue = new StringMemory((String) value);
        } else if (type == char[].class || type == Character[].class) {
            returnValue = new StringMemory(Native.toString((char[]) value));
        }

        return returnValue;
    }

    public static ByReference ConvertToReference(String cls) {
        ByReference c = null;

        cls = cls.toUpperCase();
        switch (cls) {
            case "INT":
                c = new IntByReference();
                break;
            case "LONG":
                c = new LongByReference();
                break;
            case "DOUBLE":
                c = new DoubleByReference();
                break;
            case "FLOAT":
                c = new FloatByReference();
                break;
            case "STRING":
                c = new StringByReference();
                break;
            case "POINTER":
                c = new PointerByReference();
                break;
        }

        return c;
    }

    public static ByReference ConvertObjectToReference(String cls, Object value) {
        ByReference c = null;

        cls = cls.toUpperCase();
        switch (cls) {
            case "INT":
                c = new IntByReference((int) value);
                break;
            case "LONG":
                c = new LongByReference((long) value);
                break;
            case "DOUBLE":
                c = new DoubleByReference((double) value);
                break;
            case "FLOAT":
                c = new FloatByReference((float) value);
                break;
            case "STRING":
                c = new StringByReference((String) value);
                break;
            case "POINTER":
                c = new PointerByReference((Pointer) value);
                break;
        }

        return c;
    }

    public static ByReference SetValueToReference(ByReference refval, Object value) {
        ByReference returnValue = null;
        Class cls = refval.getClass();

        if (cls == IntByReference.class) {
            IntByReference _refval = (IntByReference) refval;
            _refval.setValue((int) value);
            returnValue = _refval;
        } else if (cls == LongByReference.class) {
            LongByReference _refval = (LongByReference) refval;
            _refval.setValue((long) value);
            returnValue = _refval;
        } else if (cls == DoubleByReference.class) {
            DoubleByReference _refval = (DoubleByReference) refval;
            _refval.setValue((double) value);
            returnValue = _refval;
        } else if (cls == FloatByReference.class) {
            FloatByReference _refval = (FloatByReference) refval;
            _refval.setValue((float) value);
            returnValue = _refval;
        } else if (cls == StringByReference.class) {
            StringByReference _refval = (StringByReference) refval;
            _refval.setValue((String) value);
            returnValue = _refval;
        } else if (cls == PointerByReference.class) {
            PointerByReference _refval = (PointerByReference) refval;
            _refval.setValue((Pointer) value);
            returnValue = _refval;
        }

        return returnValue;
    }

    public static Object ConvertReferenceToObject(ByReference refval) {
        Object returnValue = null;
        Class cls = refval.getClass();

        if (cls == IntByReference.class) {
            IntByReference _refval = (IntByReference) refval;
            returnValue = _refval.getValue();
        } else if (cls == LongByReference.class) {
            LongByReference _refval = (LongByReference) refval;
            returnValue = _refval.getValue();
        } else if (cls == DoubleByReference.class) {
            DoubleByReference _refval = (DoubleByReference) refval;
            returnValue = _refval.getValue();
        } else if (cls == FloatByReference.class) {
            FloatByReference _refval = (FloatByReference) refval;
            returnValue = _refval.getValue();
        } else if (cls == StringByReference.class) {
            StringByReference _refval = (StringByReference) refval;
            returnValue = _refval.getValue();
        } else if (cls == PointerByReference.class) {
            PointerByReference _refval = (PointerByReference) refval;
            returnValue = _refval.getValue();
        }

        return returnValue;
    }

    public static Class[] ConvertJavaClassByType(Environment env, ArrayMemory types) throws ClassNotFoundException {
        ArrayList<Class> temp = new ArrayList<Class>();

        ForeachIterator iterator_types = types.getNewIterator(env, false, false);
        while (iterator_types.next()) {
            String valuType = iterator_types.getValue().toString().toUpperCase();
            Class result = ConvertToJavaClassByType(valuType);
            temp.add(result);
        }

        Class[] result = temp.toArray(new Class[0]);
        return result;
    }


    public static Object ConvertToJavaArrayByType(String cls, int size) throws ClassNotFoundException {
        Class c = Class.class;
        Object value = null;

        String cls_up = cls.toUpperCase();
        switch (cls_up) {
            case "INT":
                c = int.class;
                break;
            case "BYTE":
                c = byte.class;
                break;
            case "CHAR":
                c = char.class;
                break;
            case "BOOL":
                c = boolean.class;
                break;
            case "LONG":
                c = long.class;
                break;
            case "DOUBLE":
                c = double.class;
                break;
            case "FLOAT":
                c = float.class;
                break;
            case "STRING":
                c = String.class;
                break;
            case "POINTER":
                c = Pointer.class;
                break;
        }

        value = Array.newInstance(c, size);
        return value;
    }


    /*** StringByReference ***/
    public static class StringByReference extends ByReference {
        public StringByReference() {
            this(0);
        }

        public StringByReference(int size) {
            super(size < 4 ? 4 : size);
            getPointer().clear(size < 4 ? 4 : size);
        }

        public StringByReference(String str) {
            super(str.length() < 4 ? 4 : str.length() + 1);
            setValue(str);
        }

        private void setValue(String str) {
            getPointer().setString(0, str);
        }

        public String getValue() {
            return getPointer().getString(0);
        }
    }
    /*** StringByReference ***/
}