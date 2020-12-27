package org.develnext.jphp.ext.system.classes;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import org.develnext.jphp.ext.system.DFFIExtension;
import org.develnext.jphp.ext.system.JnaMemory;
import org.develnext.jphp.ext.system.jni.NtdllDLL;
import php.runtime.Memory;
import php.runtime.annotation.Reflection.Namespace;
import php.runtime.annotation.Reflection.Signature;
import php.runtime.env.Environment;
import php.runtime.lang.BaseObject;
import php.runtime.reflection.ClassEntity;

@Namespace(DFFIExtension.NS)
public class DFFIKernel32 extends BaseObject {
    private static boolean isNtEnabled = false;

    public DFFIKernel32(final Environment env, final ClassEntity clazz) {
        super(env, clazz);
    }

    @Signature
    public static void setIsNtEnabled(final boolean isNtEnabled) {
        DFFIKernel32.isNtEnabled = isNtEnabled;
    }

    @Signature
    public static Memory writeMemoryInt(final int hProcess, final int lpBaseAddress, final int value) {
        return writeMemoryValue(hProcess, lpBaseAddress, new JnaMemory(value));
    }

    @Signature
    public static Memory writeMemoryLong(final int hProcess, final int lpBaseAddress, final long value) {
        return writeMemoryValue(hProcess, lpBaseAddress, new JnaMemory(value));
    }

    @Signature
    public static Memory writeMemoryString(final int hProcess, final int lpBaseAddress, final String value) {
        return writeMemoryValue(hProcess, lpBaseAddress, JnaMemory.fromString(value));
    }

    @Signature
    @Deprecated
    public static Memory writeMemoryValue(final int hProcess, final int lpBaseAddress, final Object value) {
        return writeMemoryValue(hProcess, lpBaseAddress, toJnaMemory(value));
    }

    private static Memory writeMemoryValue(final int hProcess, final int lpBaseAddress, final JnaMemory lpBufferPointer) {
        final WinNT.HANDLE hProcessHandle = new WinNT.HANDLE(new Pointer(hProcess));
        final Pointer lpBaseAddressPointer = new Pointer(lpBaseAddress);

        if (isNtEnabled && NtdllDLL.INSTANCE.NtWriteVirtualMemory(hProcessHandle, new WinDef.PVOID(lpBaseAddressPointer), lpBufferPointer, new WinDef.ULONG(lpBufferPointer.size()), null).longValue() != 0)
            return Memory.TRUE;

        if (Kernel32.INSTANCE.WriteProcessMemory(hProcessHandle, lpBaseAddressPointer, lpBufferPointer, (int) lpBufferPointer.size(), null))
            return Memory.TRUE;

        return Memory.FALSE;
    }

    private static JnaMemory toJnaMemory(final Object value) {
        if (value instanceof Integer || value instanceof Long)
            return new JnaMemory(((Long) value).intValue());

        if (value instanceof String)
            return JnaMemory.fromString((String) value);

        System.out.printf("Cannot find appropriate cast for %s%n", value);

        return null;
    }
}