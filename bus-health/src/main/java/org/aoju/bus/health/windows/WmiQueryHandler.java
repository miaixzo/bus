/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.health.windows;

import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.COM.Wbemcli;
import com.sun.jna.platform.win32.COM.WbemcliUtil;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.platform.win32.WinNT;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.health.Config;
import org.aoju.bus.logger.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * 处理WMI查询
 *
 * @author Kimi Liu
 * @version 6.0.5
 * @since JDK 1.8+
 */
@ThreadSafe
public class WmiQueryHandler {

    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private static int globalTimeout = Config.get("health.wmi.timeout", -1);
    // 创建这个类或子类
    private static Class<? extends WmiQueryHandler> customClass = null;

    static {
        if (globalTimeout == 0 || globalTimeout < -1) {
            throw new Config.PropertyException("health.wmi.timeout");
        }
    }

    // 缓存失败的wmi类
    private final Set<String> failedWmiClassNames = new HashSet<>();
    // WMI查询超时
    private int wmiTimeout = globalTimeout;
    // 首选的线程模型
    private int comThreading = Ole32.COINIT_MULTITHREADED;
    // 安全跟踪初始化
    private boolean securityInitialized = false;

    /**
     * 方法来创建此类的实例。要覆盖这个类，使用{@link #setInstanceClass(Class)}
     * 来定义一个扩展了{@link WmiQueryHandler}的子类
     *
     * @return 类的实例 {@link #setInstanceClass(Class)}
     */
    public static synchronized WmiQueryHandler createInstance() {
        if (customClass == null) {
            return new WmiQueryHandler();
        }
        try {
            return customClass.getConstructor(EMPTY_CLASS_ARRAY).newInstance(EMPTY_OBJECT_ARRAY);
        } catch (NoSuchMethodException | SecurityException e) {
            Logger.error("Failed to find or access a no-arg constructor for {}", customClass);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            Logger.error("Failed to create a new instance of {}", customClass);
        }
        return null;
    }

    /**
     * Define a subclass to be instantiated by {@link #createInstance()}. The class
     * must extend {@link WmiQueryHandler}.
     *
     * @param instanceClass The class to instantiate with {@link #createInstance()}.
     */
    public static synchronized void setInstanceClass(Class<? extends WmiQueryHandler> instanceClass) {
        customClass = instanceClass;
    }

    /**
     * Query WMI for values, with no timeout.
     *
     * @param <T>   WMI queries use an Enum to identify the fields to query, and use
     *              the enum values as keys to retrieve the results.
     * @param query A WmiQuery object encapsulating the namespace, class, and
     *              properties
     * @return a WmiResult object containing the query results, wrapping an EnumMap
     */
    public <T extends Enum<T>> WbemcliUtil.WmiResult<T> queryWMI(WbemcliUtil.WmiQuery<T> query) {

        WbemcliUtil.WmiResult<T> result = WbemcliUtil.INSTANCE.new WmiResult<>(query.getPropertyEnum());
        if (failedWmiClassNames.contains(query.getWmiClassName())) {
            return result;
        }
        boolean comInit = false;
        try {
            comInit = initCOM();
            result = query.execute(wmiTimeout);
        } catch (COMException e) {
            // Ignore any exceptions with OpenHardwareMonitor
            if (!WmiKit.OHM_NAMESPACE.equals(query.getNameSpace())) {
                final int hresult = e.getHresult() == null ? -1 : e.getHresult().intValue();
                switch (hresult) {
                    case Wbemcli.WBEM_E_INVALID_NAMESPACE:
                        Logger.warn("COM exception: Invalid Namespace {}", query.getNameSpace());
                        break;
                    case Wbemcli.WBEM_E_INVALID_CLASS:
                        Logger.warn("COM exception: Invalid Class {}", query.getWmiClassName());
                        break;
                    case Wbemcli.WBEM_E_INVALID_QUERY:
                        Logger.warn("COM exception: Invalid Query: {}", WmiKit.queryToString(query));
                        break;
                    default:
                        handleComException(query, e);
                        break;
                }
                failedWmiClassNames.add(query.getWmiClassName());
            }
        } catch (TimeoutException e) {
            Logger.error("WMI query timed out after {} ms: {}", wmiTimeout, WmiKit.queryToString(query));
        }
        if (comInit) {
            unInitCOM();
        }
        return result;
    }

    /**
     * <p>
     * handleComException.
     * </p>
     *
     * @param query a {@link com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery}
     *              object.
     * @param ex    a {@link com.sun.jna.platform.win32.COM.COMException} object.
     */
    protected void handleComException(WbemcliUtil.WmiQuery<?> query, COMException ex) {
        Logger.warn(
                "COM exception querying {}, which might not be on your system. Will not attempt to query it again. Error was {}: {}",
                query.getWmiClassName(), ex.getHresult().intValue(), ex.getMessage());
    }

    /**
     * Initializes COM library and sets security to impersonate the local user
     *
     * @return True if COM was initialized and needs to be uninitialized, false
     * otherwise
     */
    public boolean initCOM() {
        boolean comInit = false;
        // Step 1: --------------------------------------------------
        // Initialize COM. ------------------------------------------
        comInit = initCOM(getComThreading());
        if (!comInit) {
            comInit = initCOM(switchComThreading());
        }
        // Step 2: --------------------------------------------------
        // Set general COM security levels --------------------------
        if (comInit && !isSecurityInitialized()) {
            WinNT.HRESULT hres = Ole32.INSTANCE.CoInitializeSecurity(null, -1, null, null,
                    Ole32.RPC_C_AUTHN_LEVEL_DEFAULT, Ole32.RPC_C_IMP_LEVEL_IMPERSONATE, null, Ole32.EOAC_NONE, null);
            // If security already initialized we get RPC_E_TOO_LATE
            // This can be safely ignored
            if (COMUtils.FAILED(hres) && hres.intValue() != WinError.RPC_E_TOO_LATE) {
                Ole32.INSTANCE.CoUninitialize();
                throw new COMException("Failed to initialize security.", hres);
            }
            securityInitialized = true;
        }
        return comInit;
    }

    /**
     * <p>
     * initCOM.
     * </p>
     *
     * @param coInitThreading a int.
     * @return a boolean.
     */
    protected boolean initCOM(int coInitThreading) {
        WinNT.HRESULT hres = Ole32.INSTANCE.CoInitializeEx(null, coInitThreading);
        switch (hres.intValue()) {
            // Successful local initialization (S_OK) or was already initialized
            // (S_FALSE) but still needs uninit
            case COMUtils.S_OK:
            case COMUtils.S_FALSE:
                return true;
            // COM was already initialized with a different threading model
            case WinError.RPC_E_CHANGED_MODE:
                return false;
            // Any other results is impossible
            default:
                throw new COMException("Failed to initialize COM library.", hres);
        }
    }

    /**
     * UnInitializes COM library. This should be called once for every successful
     * call to initCOM.
     */
    public void unInitCOM() {
        Ole32.INSTANCE.CoUninitialize();
    }

    /**
     * Returns the current threading model for COM initialization, as bus-health is
     * required to match if an external program has COM initialized already.
     *
     * @return The current threading model
     */
    public int getComThreading() {
        return comThreading;
    }

    /**
     * Switches the current threading model for COM initialization, as  bus-health is
     * required to match if an external program has COM initialized already.
     *
     * @return The new threading model after switching
     */
    public int switchComThreading() {
        if (comThreading == Ole32.COINIT_APARTMENTTHREADED) {
            comThreading = Ole32.COINIT_MULTITHREADED;
        } else {
            comThreading = Ole32.COINIT_APARTMENTTHREADED;
        }
        return comThreading;
    }

    /**
     * Security only needs to be initialized once. This boolean identifies whether
     * that has happened.
     *
     * @return Returns the securityInitialized.
     */
    public boolean isSecurityInitialized() {
        return securityInitialized;
    }

    /**
     * Gets the current WMI timeout. WMI queries will fail if they take longer than
     * this number of milliseconds. A value of -1 is infinite (no timeout).
     *
     * @return Returns the current value of wmiTimeout.
     */
    public int getWmiTimeout() {
        return wmiTimeout;
    }

    /**
     * Sets the WMI timeout. WMI queries will fail if they take longer than this
     * number of milliseconds.
     *
     * @param wmiTimeout The wmiTimeout to set, in milliseconds. To disable timeouts, set
     *                   timeout as -1 (infinite).
     */
    public void setWmiTimeout(int wmiTimeout) {
        this.wmiTimeout = wmiTimeout;
    }

}
