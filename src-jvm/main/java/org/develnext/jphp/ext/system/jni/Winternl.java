package org.develnext.jphp.ext.system.jni;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinDef.PVOID;
import com.sun.jna.platform.win32.WinDef.ULONG;
import com.sun.jna.platform.win32.WinDef.USHORT;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import java.util.Arrays;
import java.util.List;

public interface Winternl {
    public static class UNICODE_STRING extends Structure {
        public USHORT Length;
        public USHORT MaximumLength;
        public WString Buffer;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("Length", "MaximumLength", "Buffer");
        }

        public UNICODE_STRING() {
        }

        public static class ByReference extends UNICODE_STRING implements Structure.ByReference {
            public ByReference() {
            }

            public ByReference(Pointer memory) {
                super(memory);
            }
        }

        public UNICODE_STRING(Pointer memory) {
            super(memory);
            read();
        }
    }

    public static class OBJECT_ATTRIBUTES extends Structure {
        public ULONG Length;
        public HANDLE RootDirectory;
        public UNICODE_STRING ObjectName;
        public ULONG Attributes;
        public PVOID SecurityDescriptor;
        public PVOID SecurityQualityOfService;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("Length", "RootDirectory", "ObjectName", "Attributes",
                    "SecurityDescriptor", "SecurityQualityOfService");
        }

        public OBJECT_ATTRIBUTES() {
        }

        public static class ByReference extends OBJECT_ATTRIBUTES implements Structure.ByReference {
            public ByReference() {
            }

            public ByReference(Pointer memory) {
                super(memory);
            }
        }

        public OBJECT_ATTRIBUTES(Pointer memory) {
            super(memory);
            read();
        }

    }
}