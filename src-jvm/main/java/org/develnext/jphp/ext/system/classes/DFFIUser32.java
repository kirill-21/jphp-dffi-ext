package org.develnext.jphp.ext.system.classes;

import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import org.develnext.jphp.ext.system.DFFIExtension;
import php.runtime.Memory;
import php.runtime.annotation.Reflection.Namespace;
import php.runtime.annotation.Reflection.Signature;
import php.runtime.env.Environment;
import php.runtime.lang.BaseObject;
import php.runtime.reflection.ClassEntity;

@Namespace(DFFIExtension.NS)
public class DFFIUser32 extends BaseObject {
    public DFFIUser32(final Environment env, final ClassEntity clazz) {
        super(env, clazz);
    }

    @Signature
    public static Memory sendInput(char value, boolean isKeyUpEnabled) {
        /*
            INPUT_MOUSE 0
            INPUT_KEYBOARD 1
            INPUT_HARDWARE 2
         */

        final WinUser.INPUT input = new WinUser.INPUT();

        input.type = new WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD);
        input.input.setType("ki");
        input.input.ki.wScan = new WinDef.WORD(0);
        input.input.ki.time = new WinDef.DWORD(0);
        input.input.ki.dwExtraInfo = new BaseTSD.ULONG_PTR(0);

        // Press button
        input.input.ki.wVk = new WinDef.WORD(value);
        input.input.ki.dwFlags = new WinDef.DWORD(0);
        User32.INSTANCE.SendInput(new WinDef.DWORD(1), (WinUser.INPUT[]) input.toArray(1), input.size());

        // Release button
        if (isKeyUpEnabled) {
            input.input.ki.wVk = new WinDef.WORD(value);
            input.input.ki.dwFlags = new WinDef.DWORD(2);
            User32.INSTANCE.SendInput(new WinDef.DWORD(1), (WinUser.INPUT[]) input.toArray(1), input.size());
        }

        System.out.println("OK!");

        return Memory.FALSE;
    }
}