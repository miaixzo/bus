/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.windows.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.platform.win32.BaseTSD.ULONG_PTR;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

/**
 * NtDll
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface NtDll extends com.sun.jna.platform.win32.NtDll {

    /**
     * The INSTANCE value.
     */
    NtDll INSTANCE = Native.load("NtDll", NtDll.class, W32APIOptions.DEFAULT_OPTIONS);

    /**
     * The PROCESS_BASIC_INFORMATION value.
     */
    int PROCESS_BASIC_INFORMATION = 0;

    /**
     * Windows API docs say NtQueryInformationProcess may be altered or unavailable in future versions of Windows.
     * Applications should use the alternate functions listed in this topic. However, there is no other way to get this
     * information, it's been officially non-API for over a decade, and many many programs including windows sysinternal
     * tools rely on this behavior, so the odds of it going away are small.
     *
     * @param ProcessHandle            the process handle value
     * @param ProcessInformation       the process information value
     *
     * @param ProcessInformationClass  the process information class value
     *
     * @param ProcessInformationLength the process information length value
     *
     * @param ReturnLength             the return length value
     *
     * @return the result
     */
    int NtQueryInformationProcess(
            HANDLE ProcessHandle,
            int ProcessInformationClass,
            Pointer ProcessInformation,
            int ProcessInformationLength,
            IntByReference ReturnLength);

    /**
     * JNA wrapper for the PROCESS_BASIC_INFORMATION structure.
     * <p>
     * This class maps to the native Windows structure: {@code typedef struct _PROCESS_BASIC_INFORMATION { PVOID
     * Reserved1; PVOID PebBaseAddress; PVOID Reserved2[4]; } PROCESS_BASIC_INFORMATION; }
     * </p>
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @FieldOrder({ "Reserved1", "PebBaseAddress", "Reserved2" })
    class PROCESS_BASIC_INFORMATION extends Structure {

        /**
         * The Reserved1 value.
         */
        public Pointer Reserved1;

        /**
         * The PebBaseAddress value.
         */
        public Pointer PebBaseAddress;

        /**
         * The Reserved2 value.
         */
        public Pointer[] Reserved2 = new Pointer[4];

    }

    /**
     * JNA wrapper for the PEB (Process Environment Block) structure.
     * <p>
     * This class maps to the native Windows structure: {@code typedef struct _PEB { BYTE pad[4]; PVOID pad2[3]; PVOID
     * ProcessParameters; } PEB; }
     * </p>
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @FieldOrder({ "pad", "pad2", "ProcessParameters" })
    class PEB extends Structure {

        /**
         * The pad value.
         */
        public byte[] pad = new byte[4];

        /**
         * The pad2 value.
         */
        public Pointer[] pad2 = new Pointer[3];

        /**
         * The ProcessParameters value.
         */
        public Pointer ProcessParameters; // RTL_USER_PROCESS_PARAMETERS

    }

    /**
     * JNA wrapper for the RTL_USER_PROCESS_PARAMETERS structure.
     * <p>
     * This class maps to the native Windows RTL_USER_PROCESS_PARAMETERS structure which contains process parameters
     * including command line, environment, and other startup information.
     * </p>
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @FieldOrder({ "MaximumLength", "Length", "Flags", "DebugFlags", "ConsoleHandle", "ConsoleFlags", "StandardInput",
            "StandardOutput", "StandardError", "CurrentDirectory", "DllPath", "ImagePathName", "CommandLine",
            "Environment", "StartingX", "StartingY", "CountX", "CountY", "CountCharsX", "CountCharsY", "FillAttribute",
            "WindowFlags", "ShowWindowFlags", "WindowTitle", "DesktopInfo", "ShellInfo", "RuntimeData",
            "CurrentDirectories", "EnvironmentSize", "EnvironmentVersion", "PackageDependencyData", "ProcessGroupId",
            "LoaderThreads", "RedirectionDllName", "HeapPartitionName", "DefaultThreadpoolCpuSetMasks",
            "DefaultThreadpoolCpuSetMaskCount" })
    class RTL_USER_PROCESS_PARAMETERS extends Structure {

        /**
         * The MaximumLength value.
         */
        public int MaximumLength;

        /**
         * The Length value.
         */
        public int Length;

        /**
         * The Flags value.
         */
        public int Flags;

        /**
         * The DebugFlags value.
         */
        public int DebugFlags;

        /**
         * The ConsoleHandle value.
         */
        public HANDLE ConsoleHandle;

        /**
         * The ConsoleFlags value.
         */
        public int ConsoleFlags;

        /**
         * The StandardInput value.
         */
        public HANDLE StandardInput;

        /**
         * The StandardOutput value.
         */
        public HANDLE StandardOutput;

        /**
         * The StandardError value.
         */
        public HANDLE StandardError;

        /**
         * The CurrentDirectory value.
         */
        public CURDIR CurrentDirectory;

        /**
         * The DllPath value.
         */
        public UNICODE_STRING DllPath;

        /**
         * The ImagePathName value.
         */
        public UNICODE_STRING ImagePathName;

        /**
         * The CommandLine value.
         */
        public UNICODE_STRING CommandLine;

        /**
         * The Environment value.
         */
        public Pointer Environment;

        /**
         * The StartingX value.
         */
        public int StartingX;

        /**
         * The StartingY value.
         */
        public int StartingY;

        /**
         * The CountX value.
         */
        public int CountX;

        /**
         * The CountY value.
         */
        public int CountY;

        /**
         * The CountCharsX value.
         */
        public int CountCharsX;

        /**
         * The CountCharsY value.
         */
        public int CountCharsY;

        /**
         * The FillAttribute value.
         */
        public int FillAttribute;

        /**
         * The WindowFlags value.
         */
        public int WindowFlags;

        /**
         * The ShowWindowFlags value.
         */
        public int ShowWindowFlags;

        /**
         * The WindowTitle value.
         */
        public UNICODE_STRING WindowTitle;

        /**
         * The DesktopInfo value.
         */
        public UNICODE_STRING DesktopInfo;

        /**
         * The ShellInfo value.
         */
        public UNICODE_STRING ShellInfo;

        /**
         * The RuntimeData value.
         */
        public UNICODE_STRING RuntimeData;

        /**
         * The CurrentDirectories value.
         */
        public RTL_DRIVE_LETTER_CURDIR[] CurrentDirectories = new RTL_DRIVE_LETTER_CURDIR[32];

        /**
         * The EnvironmentSize value.
         */
        public ULONG_PTR EnvironmentSize;

        /**
         * The EnvironmentVersion value.
         */
        public ULONG_PTR EnvironmentVersion;

        /**
         * The PackageDependencyData value.
         */
        public Pointer PackageDependencyData;

        /**
         * The ProcessGroupId value.
         */
        public int ProcessGroupId;

        /**
         * The LoaderThreads value.
         */
        public int LoaderThreads;

        /**
         * The RedirectionDllName value.
         */
        public UNICODE_STRING RedirectionDllName;

        /**
         * The HeapPartitionName value.
         */
        public UNICODE_STRING HeapPartitionName;

        /**
         * The DefaultThreadpoolCpuSetMasks value.
         */
        public ULONG_PTR DefaultThreadpoolCpuSetMasks;

        /**
         * The DefaultThreadpoolCpuSetMaskCount value.
         */
        public int DefaultThreadpoolCpuSetMaskCount;

    }

    /**
     * JNA wrapper for the UNICODE_STRING structure.
     * <p>
     * This class maps to the native Windows structure: {@code typedef struct _UNICODE_STRING { USHORT Length; USHORT
     * MaximumLength; PWSTR Buffer; } UNICODE_STRING; }
     * </p>
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @FieldOrder({ "Length", "MaximumLength", "Buffer" })
    class UNICODE_STRING extends Structure {

        /**
         * The Length value.
         */
        public short Length;

        /**
         * The MaximumLength value.
         */
        public short MaximumLength;

        /**
         * The Buffer value.
         */
        public Pointer Buffer;

    }

    /**
     * JNA wrapper for the STRING (ANSI_STRING) structure.
     * <p>
     * This class maps to the native Windows structure: {@code typedef struct _STRING { USHORT Length; USHORT
     * MaximumLength; PCHAR Buffer; } STRING; }
     * </p>
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @FieldOrder({ "Length", "MaximumLength", "Buffer" })
    class STRING extends Structure {

        /**
         * The Length value.
         */
        public short Length;

        /**
         * The MaximumLength value.
         */
        public short MaximumLength;

        /**
         * The Buffer value.
         */
        public Pointer Buffer;

    }

    /**
     * JNA wrapper for the CURDIR structure.
     * <p>
     * This class maps to the native Windows structure for current directory information.
     * </p>
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @FieldOrder({ "DosPath", "Handle" })
    class CURDIR extends Structure {

        /**
         * The DosPath value.
         */
        public UNICODE_STRING DosPath;

        /**
         * The Handle value.
         */
        public Pointer Handle;

    }

    /**
     * JNA wrapper for the RTL_DRIVE_LETTER_CURDIR structure.
     * <p>
     * This class maps to the native Windows structure for drive letter current directory information.
     * </p>
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @FieldOrder({ "Flags", "Length", "TimeStamp", "DosPath" })
    class RTL_DRIVE_LETTER_CURDIR extends Structure {

        /**
         * The Flags value.
         */
        public short Flags;

        /**
         * The Length value.
         */
        public short Length;

        /**
         * The TimeStamp value.
         */
        public int TimeStamp;

        /**
         * The DosPath value.
         */
        public STRING DosPath;

    }

}
