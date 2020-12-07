package org.develnext.jphp.ext.system.classes;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import org.develnext.jphp.ext.system.DFFIExtension;
import org.develnext.jphp.ext.system.JnaMemory;
import php.runtime.Memory;
import php.runtime.annotation.Reflection.Namespace;
import php.runtime.annotation.Reflection.Signature;
import php.runtime.env.Environment;
import php.runtime.lang.BaseObject;
import php.runtime.reflection.ClassEntity;

@Namespace(DFFIExtension.NS)
public class DFFIKernel32 extends BaseObject {
    public DFFIKernel32(final Environment env, final ClassEntity clazz) {
        super(env, clazz);
    }

    @Signature
    public static Memory writeMemoryValue(final int hProcess, final int lpBaseAddress, final Object value) throws Exception {
        final WinNT.HANDLE hProcessHandle = new WinNT.HANDLE(new Pointer(hProcess));
        final Pointer lpBaseAddressPointer = new Pointer(lpBaseAddress);

        final JnaMemory lpBufferPointer = toJnaMemory(value);

        if (lpBufferPointer == null)
            throw new Exception(String.format("Cannot find appropriate cast for %s", value));

        if (Kernel32.INSTANCE.WriteProcessMemory(hProcessHandle, lpBaseAddressPointer, lpBufferPointer, (int) lpBufferPointer.size(), null))
            return Memory.TRUE;

        return Memory.FALSE;
    }

    private static JnaMemory toJnaMemory(final Object value) {

        if (value instanceof Integer || value instanceof Long) {
            return new JnaMemory((int) value);
        }

        if (value instanceof String)
            return JnaMemory.fromString((String) value);

        return null;
    }
}