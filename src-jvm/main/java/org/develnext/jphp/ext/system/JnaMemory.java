package org.develnext.jphp.ext.system;

import com.sun.jna.Memory;
import com.sun.jna.Native;

public class JnaMemory extends Memory {
    public static final int INT_SIZE = 4;
    public static final int LONG_SIZE = 8;

    public JnaMemory(final int value) {
        super(INT_SIZE);
        setInt(0, value);
    }

    public JnaMemory(final long value) {
        super(LONG_SIZE);
        setLong(0, value);
    }

    public JnaMemory(final byte[] value) {
        super(value.length);
        write(0, value, 0, value.length);
    }

    public static JnaMemory fromString(final String value) {
        final byte[] data = Native.toByteArray(value, Native.getDefaultStringEncoding());

        return new JnaMemory(data);
    }
}