package org.develnext.jphp.ext.system.classes;

import org.develnext.jphp.ext.system.DFFIExtension;
import php.runtime.Memory;
import php.runtime.annotation.Reflection.Namespace;
import php.runtime.annotation.Reflection.Signature;
import php.runtime.memory.*;
import php.runtime.env.Environment;
import php.runtime.env.TraceInfo;
import php.runtime.lang.BaseObject;
import php.runtime.reflection.ClassEntity;

import com.sun.javafx.tk.TKStage;
import javafx.stage.Stage;
import java.lang.reflect.Method;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.jna.*;

@Namespace(DFFIExtension.NS)
public class DFFI extends BaseObject {
    public String libName;
    public static Map<String, ArrayMemory> functions = new LinkedHashMap<>();
    public Map<String, ArrayMemory> pfunctions = new LinkedHashMap<>();

    public DFFI(Environment env, ClassEntity clazz) {
        super(env, clazz);
    }

    @Signature
    public void __construct(String lib) {
        this.libName = lib;
    }

    @Signature
    public void bind(String functionName, String returnType, ArrayMemory _types) {
        ArrayMemory array = new ArrayMemory();

        array.refOfIndex("lib").assign(libName);
        array.refOfIndex("returnType").assign(returnType);
        array.refOfIndex("types").assign(_types.toImmutable());

        functions.put(functionName, array);
        pfunctions.put(functionName, array);
    }

    @Signature
    public static Memory __callStatic(Environment env, TraceInfo trace, String functionName, Memory... args) throws ClassNotFoundException, AWTException {
        Memory returnValue = Memory.NULL;
        ArrayMemory function = functions.get(functionName);

        if (function != null) {
            String lib = function.valueOfIndex("lib").toString();
            String returnType = function.valueOfIndex("returnType").toString();
            Memory types = function.valueOfIndex("types");

            returnValue = Helper.callFunction(env, lib, returnType, functionName, types, args);
        }

        return returnValue;
    }

    @Signature
    public Memory __call(Environment env, TraceInfo trace, String functionName, Memory... args) throws ClassNotFoundException, AWTException {
        Memory returnValue = Memory.NULL;
        ArrayMemory pfunction = pfunctions.get(functionName);

        if (pfunction != Memory.UNDEFINED) {
            String lib = pfunction.valueOfIndex("lib").toString();
            String returnType = pfunction.valueOfIndex("returnType").toString();
            Memory types = pfunction.valueOfIndex("types");

            returnValue = Helper.callFunction(env, lib, returnType, functionName, types, args);
        }

        return returnValue;
    }

    @Signature
	public static Long getJFXHandle(Object window)
	{
		try {
            Stage stage = (Stage) window;

            TKStage tkStage = stage.impl_getPeer();
            Method getPlatformWindow = tkStage.getClass().getDeclaredMethod("getPlatformWindow" );
            getPlatformWindow.setAccessible(true);
            Object platformWindow = getPlatformWindow.invoke(tkStage);
            Method getNativeHandle = platformWindow.getClass().getMethod( "getNativeHandle" );
            getNativeHandle.setAccessible(true);
            Object nativeHandle = getNativeHandle.invoke(platformWindow);
            return (Long) nativeHandle;
        } catch (Throwable e) {
            return null;
        }
	}
    
    @Signature
    public static void addSearchPath(String lib, String path) {
        NativeLibrary.addSearchPath(lib, path);
    }
}
