/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.builtin.ldap;

import java.lang.reflect.Array;
import java.util.*;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.Format;
import org.miaixz.bus.image.galaxy.data.Code;
import org.miaixz.bus.image.galaxy.data.DatePrecision;
import org.miaixz.bus.image.galaxy.data.Issuer;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.api.ConfigurationChanges;

/**
 * Utility class for building and manipulating LDAP attributes and Distinguished Names (DNs) related to DICOM
 * configurations. It provides methods for storing attributes, comparing values, and handling LDAP-specific operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LdapBuilder {

    /**
     * An empty array of {@link Code} objects, used as a default or placeholder.
     */
    private static final Code[] EMPTY_CODES = {};
    /**
     * Character array containing digits and uppercase letters, used for ordinal prefixes. The array includes characters
     * '0' through '9' and 'A' through 'Z'.
     */
    private final static char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
            'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

    /**
     * Checks if the given {@link Attributes} contain a specific object class.
     *
     * @param attrs       The {@link Attributes} to check.
     * @param objectClass The object class string to look for.
     * @return {@code true} if the object class is found, {@code false} otherwise.
     * @throws NamingException if an LDAP naming error occurs during attribute retrieval.
     */
    public static boolean hasObjectClass(Attributes attrs, String objectClass) throws NamingException {
        NamingEnumeration<String> ne = (NamingEnumeration<String>) attrs.get("objectclass").getAll();
        try {
            while (ne.hasMore())
                if (objectClass.equals(ne.next()))
                    return true;
        } finally {
            LdapBuilder.safeClose(ne);
        }
        return false;
    }

    /**
     * Safely closes a {@link NamingEnumeration}, suppressing any {@link NamingException} that may occur. This method is
     * useful for ensuring resources are released without interrupting program flow.
     *
     * @param enumeration The {@link NamingEnumeration} to close.
     * @param <T>         The type of elements in the enumeration.
     */
    public static <T> void safeClose(NamingEnumeration<T> enumeration) {
        if (enumeration != null)
            try {
                enumeration.close();
            } catch (NamingException e) {
                // Ignore exception on close
            }
    }

    /**
     * Stores references to DICOM network connections in the LDAP attributes. If {@code ldapObj} is not null, it also
     * records the changes for configuration tracking. The connection references are stored as Distinguished Names
     * (DNs).
     *
     * @param ldapObj  The {@link ConfigurationChanges.ModifiedObject} to record changes, or {@code null} if changes do
     *                 not need to be tracked.
     * @param attrs    The {@link Attributes} to which connection references will be added.
     * @param conns    A collection of {@link Connection} objects to store.
     * @param deviceDN The Distinguished Name of the parent DICOM device.
     */
    public static void storeConnRefs(
            ConfigurationChanges.ModifiedObject ldapObj,
            Attributes attrs,
            Collection<Connection> conns,
            String deviceDN) {
        if (!conns.isEmpty()) {
            attrs.put(LdapBuilder.connRefs(conns, deviceDN));
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(
                        "dicomNetworkConnectionReference");
                for (Connection conn : conns)
                    attribute.addValue(LdapBuilder.dnOf(conn, deviceDN));
                ldapObj.add(attribute);
            }
        }
    }

    /**
     * Creates a {@link Attribute} containing Distinguished Names (DNs) of DICOM network connections. Each connection's
     * DN is generated based on its properties and the parent device's DN.
     *
     * @param conns    A collection of {@link Connection} objects.
     * @param deviceDN The Distinguished Name of the parent DICOM device.
     * @return A {@link Attribute} with connection DNs, identified by "dicomNetworkConnectionReference".
     */
    private static Attribute connRefs(Collection<Connection> conns, String deviceDN) {
        Attribute attr = new BasicAttribute("dicomNetworkConnectionReference");
        for (Connection conn : conns)
            attr.add(LdapBuilder.dnOf(conn, deviceDN));
        return attr;
    }

    /**
     * Stores an array of values in LDAP attributes if the array is not empty and not equal to default values. Records
     * changes in {@code ldapObj} if provided.
     *
     * @param ldapObj The {@link ConfigurationChanges.ModifiedObject} to record changes, or {@code null} if changes do
     *                not need to be tracked.
     * @param attrs   The {@link Attributes} to store the values.
     * @param attrID  The ID of the attribute.
     * @param vals    The array of values to store.
     * @param defVals Optional default values for comparison. If the {@code vals} array is equal to {@code defVals}, the
     *                attribute is not stored.
     * @param <T>     The type of the values.
     */
    public static <T> void storeNotEmpty(
            ConfigurationChanges.ModifiedObject ldapObj,
            Attributes attrs,
            String attrID,
            T[] vals,
            T... defVals) {
        if (vals.length > 0 && !LdapBuilder.equals(vals, defVals)) {
            attrs.put(LdapBuilder.attr(attrID, vals));
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrID);
                for (T val : vals)
                    attribute.addValue(val);
                ldapObj.add(attribute);
            }
        }
    }

    /**
     * Stores a map of string-keyed values in LDAP attributes if the map is not empty. Each entry in the map is
     * converted to a "key=value" string. Records changes in {@code ldapObj} if provided.
     *
     * @param ldapObj The {@link ConfigurationChanges.ModifiedObject} to record changes, or {@code null} if changes do
     *                not need to be tracked.
     * @param attrs   The {@link Attributes} to store the values.
     * @param attrID  The ID of the attribute.
     * @param map     The map of values to store.
     * @param <T>     The type of the map values.
     */
    public static <T> void storeNotEmpty(
            ConfigurationChanges.ModifiedObject ldapObj,
            Attributes attrs,
            String attrID,
            Map<String, T> map) {
        storeNotEmpty(ldapObj, attrs, attrID, toStrings(map));
    }

    /**
     * Converts a map of string-keyed values to an array of strings in "key=value" format.
     *
     * @param map The map to convert.
     * @param <T> The type of the map values.
     * @return An array of strings representing the map entries.
     */
    public static <T> String[] toStrings(Map<String, T> map) {
        String[] ss = new String[map.size()];
        int i = 0;
        for (Map.Entry<String, T> entry : map.entrySet())
            ss[i++] = entry.getKey() + '=' + entry.getValue();
        return ss;
    }

    /**
     * Stores an array of values in LDAP attributes if the array is not empty and not equal to default values.
     *
     * @param attrs   The {@link Attributes} to store the values.
     * @param attrID  The ID of the attribute.
     * @param vals    The array of values to store.
     * @param defVals Optional default values for comparison. If the {@code vals} array is equal to {@code defVals}, the
     *                attribute is not stored.
     * @param <T>     The type of the values.
     */
    public static <T> void storeNotEmpty(Attributes attrs, String attrID, T[] vals, T... defVals) {
        if (vals.length > 0 && !LdapBuilder.equals(vals, defVals))
            attrs.put(LdapBuilder.attr(attrID, vals));
    }

    /**
     * Creates a {@link Attribute} from a map of string-keyed values. Each entry in the map is converted to a
     * "key=value" string and added to the attribute.
     *
     * @param attrID The ID of the attribute.
     * @param map    The map of values.
     * @param <T>    The type of the map values.
     * @return A {@link Attribute} containing the map entries.
     */
    public static <T> Attribute attr(String attrID, Map<String, T> map) {
        return attr(attrID, toStrings(map));
    }

    /**
     * Creates a {@link Attribute} from an array of values. Each value is converted to its string representation and
     * added to the attribute.
     *
     * @param attrID The ID of the attribute.
     * @param vals   The array of values.
     * @param <T>    The type of the values.
     * @return A {@link Attribute} containing the values.
     */
    public static <T> Attribute attr(String attrID, T... vals) {
        Attribute attr = new BasicAttribute(attrID);
        for (T val : vals)
            attr.add(val.toString());
        return attr;
    }

    /**
     * Stores an array of integer values in LDAP attributes if the array is not null and not empty. Records changes in
     * {@code ldapObj} if provided.
     *
     * @param ldapObj The {@link ConfigurationChanges.ModifiedObject} to record changes, or {@code null} if changes do
     *                not need to be tracked.
     * @param attrs   The {@link Attributes} to store the values.
     * @param attrID  The ID of the attribute.
     * @param vals    The array of integer values to store.
     */
    public static void storeNotEmpty(
            ConfigurationChanges.ModifiedObject ldapObj,
            Attributes attrs,
            String attrID,
            int... vals) {
        if (vals != null && vals.length > 0) {
            attrs.put(LdapBuilder.attr(attrID, vals));
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrID);
                for (int val : vals)
                    attribute.addValue(val);
                ldapObj.add(attribute);
            }
        }
    }

    /**
     * Stores an array of integer values in LDAP attributes if the array is not null and not empty.
     *
     * @param attrs  The {@link Attributes} to store the values.
     * @param attrID The ID of the attribute.
     * @param vals   The array of integer values to store.
     */
    public static void storeNotEmpty(Attributes attrs, String attrID, int... vals) {
        if (vals != null && vals.length > 0)
            attrs.put(LdapBuilder.attr(attrID, vals));
    }

    /**
     * Creates a {@link Attribute} from an array of integer values. Each integer value is converted to its string
     * representation and added to the attribute.
     *
     * @param attrID The ID of the attribute.
     * @param vals   The array of integer values.
     * @return A {@link Attribute} containing the integer values as strings.
     */
    public static Attribute attr(String attrID, int... vals) {
        Attribute attr = new BasicAttribute(attrID);
        for (int val : vals)
            attr.add(Integer.toString(val));
        return attr;
    }

    /**
     * Stores a value in LDAP attributes if it is not null and not equal to a default value. Records changes in
     * {@code ldapObj} if provided.
     *
     * @param ldapObj The {@link ConfigurationChanges.ModifiedObject} to record changes, or {@code null} if changes do
     *                not need to be tracked.
     * @param attrs   The {@link Attributes} to store the value.
     * @param attrID  The ID of the attribute.
     * @param val     The value to store.
     * @param defVal  The default value for comparison. If {@code val} is equal to {@code defVal}, the attribute is not
     *                stored.
     * @param <T>     The type of the value.
     */
    public static <T> void storeNotNullOrDef(
            ConfigurationChanges.ModifiedObject ldapObj,
            Attributes attrs,
            String attrID,
            T val,
            T defVal) {
        if (val != null && !val.equals(defVal)) {
            attrs.put(attrID, LdapBuilder.toString(val));
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrID);
                attribute.addValue(val);
                ldapObj.add(attribute);
            }
        }
    }

    /**
     * Stores a value in LDAP attributes if it is not null and not equal to a default value.
     *
     * @param attrs  The {@link Attributes} to store the value.
     * @param attrID The ID of the attribute.
     * @param val    The value to store.
     * @param defVal The default value for comparison. If {@code val} is equal to {@code defVal}, the attribute is not
     *               stored.
     * @param <T>    The type of the value.
     */
    public static <T> void storeNotNullOrDef(Attributes attrs, String attrID, T val, T defVal) {
        if (val != null && !val.equals(defVal))
            attrs.put(attrID, LdapBuilder.toString(val));
    }

    /**
     * Stores an {@link Integer} value in LDAP attributes if it is not null. Records changes in {@code ldapObj} if
     * provided.
     *
     * @param ldapObj The {@link ConfigurationChanges.ModifiedObject} to record changes, or {@code null} if changes do
     *                not need to be tracked.
     * @param attrs   The {@link Attributes} to store the value.
     * @param attrID  The ID of the attribute.
     * @param val     The {@link Integer} value to store.
     */
    public static void storeNotNull(
            ConfigurationChanges.ModifiedObject ldapObj,
            Attributes attrs,
            String attrID,
            Integer val) {
        if (val != null) {
            LdapBuilder.storeInt(attrs, attrID, val);
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrID);
                attribute.addValue(val);
                ldapObj.add(attribute);
            }
        }
    }

    /**
     * Stores an integer value in LDAP attributes if it is not equal to a default value. Records changes in
     * {@code ldapObj} if provided.
     *
     * @param ldapObj The {@link ConfigurationChanges.ModifiedObject} to record changes, or {@code null} if changes do
     *                not need to be tracked.
     * @param attrs   The {@link Attributes} to store the value.
     * @param attrID  The ID of the attribute.
     * @param val     The integer value to store.
     * @param defVal  The default integer value for comparison. If {@code val} is equal to {@code defVal}, the attribute
     *                is not stored.
     */
    public static void storeNotDef(
            ConfigurationChanges.ModifiedObject ldapObj,
            Attributes attrs,
            String attrID,
            int val,
            int defVal) {
        if (val != defVal) {
            LdapBuilder.storeInt(attrs, attrID, val);
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrID);
                attribute.addValue(val);
                ldapObj.add(attribute);
            }
        }
    }

    /**
     * Stores an integer value in LDAP attributes if it is not equal to a default value.
     *
     * @param attrs  The {@link Attributes} to store the value.
     * @param attrID The ID of the attribute.
     * @param val    The integer value to store.
     * @param defVal The default integer value for comparison. If {@code val} is equal to {@code defVal}, the attribute
     *               is not stored.
     */
    public static void storeNotDef(Attributes attrs, String attrID, int val, int defVal) {
        if (val != defVal)
            LdapBuilder.storeInt(attrs, attrID, val);
    }

    /**
     * Stores a long value in LDAP attributes if it is not equal to a default value. Records changes in {@code ldapObj}
     * if provided.
     *
     * @param ldapObj The {@link ConfigurationChanges.ModifiedObject} to record changes, or {@code null} if changes do
     *                not need to be tracked.
     * @param attrs   The {@link Attributes} to store the value.
     * @param attrID  The ID of the attribute.
     * @param val     The long value to store.
     * @param defVal  The default long value for comparison. If {@code val} is equal to {@code defVal}, the attribute is
     *                not stored.
     */
    public static void storeNotDef(
            ConfigurationChanges.ModifiedObject ldapObj,
            Attributes attrs,
            String attrID,
            long val,
            long defVal) {
        if (val != defVal) {
            LdapBuilder.storeLong(attrs, attrID, val);
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrID);
                attribute.addValue(val);
                ldapObj.add(attribute);
            }
        }
    }

    /**
     * Stores a boolean value in LDAP attributes if it is not equal to a default value. Records changes in
     * {@code ldapObj} if provided.
     *
     * @param ldapObj The {@link ConfigurationChanges.ModifiedObject} to record changes, or {@code null} if changes do
     *                not need to be tracked.
     * @param attrs   The {@link Attributes} to store the value.
     * @param attrID  The ID of the attribute.
     * @param val     The boolean value to store.
     * @param defVal  The default boolean value for comparison. If {@code val} is equal to {@code defVal}, the attribute
     *                is not stored.
     */
    public static void storeNotDef(
            ConfigurationChanges.ModifiedObject ldapObj,
            Attributes attrs,
            String attrID,
            boolean val,
            boolean defVal) {
        if (val != defVal) {
            LdapBuilder.storeBoolean(attrs, attrID, val);
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrID);
                attribute.addValue(val);
                ldapObj.add(attribute);
            }
        }
    }

    /**
     * Stores a boolean value in LDAP attributes. The boolean value is converted to its string representation ("TRUE" or
     * "FALSE").
     *
     * @param attrs  The {@link Attributes} to store the value.
     * @param attrID The ID of the attribute.
     * @param val    The boolean value to store.
     * @return The {@link Attribute} that was added or replaced.
     */
    public static Attribute storeBoolean(Attributes attrs, String attrID, boolean val) {
        return attrs.put(attrID, LdapBuilder.toString(val));
    }

    /**
     * Stores a boolean value in LDAP attributes and records changes in {@code ldapObj} if provided. The boolean value
     * is converted to its string representation ("TRUE" or "FALSE").
     *
     * @param ldapObj The {@link ConfigurationChanges.ModifiedObject} to record changes, or {@code null} if changes do
     *                not need to be tracked.
     * @param attrs   The {@link Attributes} to store the value.
     * @param attrID  The ID of the attribute.
     * @param val     The boolean value to store.
     * @return The {@link Attribute} that was added or replaced.
     */
    public static Attribute storeBoolean(
            ConfigurationChanges.ModifiedObject ldapObj,
            Attributes attrs,
            String attrID,
            boolean val) {
        if (ldapObj != null) {
            ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrID);
            attribute.addValue(val);
            ldapObj.add(attribute);
        }
        return attrs.put(attrID, LdapBuilder.toString(val));
    }

    /**
     * Stores an integer value in LDAP attributes and records changes in {@code ldapObj} if provided. The integer value
     * is converted to its string representation.
     *
     * @param ldapObj The {@link ConfigurationChanges.ModifiedObject} to record changes, or {@code null} if changes do
     *                not need to be tracked.
     * @param attrs   The {@link Attributes} to store the value.
     * @param attrID  The ID of the attribute.
     * @param val     The integer value to store.
     * @return The {@link Attribute} that was added or replaced.
     */
    public static Attribute storeInt(
            ConfigurationChanges.ModifiedObject ldapObj,
            Attributes attrs,
            String attrID,
            int val) {
        if (ldapObj != null) {
            ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrID);
            attribute.addValue(val);
            ldapObj.add(attribute);
        }
        return attrs.put(attrID, Integer.toString(val));
    }

    /**
     * Stores an integer value in LDAP attributes. The integer value is converted to its string representation.
     *
     * @param attrs  The {@link Attributes} to store the value.
     * @param attrID The ID of the attribute.
     * @param val    The integer value to store.
     * @return The {@link Attribute} that was added or replaced.
     */
    public static Attribute storeInt(Attributes attrs, String attrID, int val) {
        return attrs.put(attrID, Integer.toString(val));
    }

    /**
     * Stores a long value in LDAP attributes. The long value is converted to its string representation.
     *
     * @param attrs  The {@link Attributes} to store the value.
     * @param attrID The ID of the attribute.
     * @param val    The long value to store.
     * @return The {@link Attribute} that was added or replaced.
     */
    public static Attribute storeLong(Attributes attrs, String attrID, long val) {
        return attrs.put(attrID, Long.toString(val));
    }

    /**
     * Constructs the Distinguished Name (DN) for a DICOM network connection. The DN format depends on whether the
     * connection has a common name (cn) or is a server connection.
     *
     * @param conn     The {@link Connection} object.
     * @param deviceDN The Distinguished Name of the parent DICOM device.
     * @return The DN string for the connection.
     */
    public static String dnOf(Connection conn, String deviceDN) {
        String cn = conn.getCommonName();
        return (cn != null) ? LdapBuilder.dnOf("cn", cn, deviceDN)
                : (conn.isServer() ? LdapBuilder.dnOf(
                        "dicomHostname",
                        conn.getHostname(),
                        "dicomPort",
                        Integer.toString(conn.getPort()),
                        deviceDN) : LdapBuilder.dnOf("dicomHostname", conn.getHostname(), deviceDN));
    }

    /**
     * Constructs a Distinguished Name (DN) from an attribute ID, its value, and a parent DN. The format will be
     * "attrID=attrValue,parentDN".
     *
     * @param attrID    The ID of the attribute (e.g., "cn", "dicomDeviceName").
     * @param attrValue The value of the attribute.
     * @param parentDN  The parent Distinguished Name.
     * @return The constructed DN string.
     */
    public static String dnOf(String attrID, String attrValue, String parentDN) {
        return attrID + '=' + attrValue + ',' + parentDN;
    }

    /**
     * Extracts the value of a specific attribute from a Distinguished Name (DN) string.
     *
     * @param dn     The Distinguished Name string from which to extract the attribute value.
     * @param attrID The ID of the attribute to extract (e.g., "cn", "dicomDeviceName").
     * @return The value of the attribute, or {@code null} if the attribute is not found in the DN.
     */
    public static String cutAttrValueFromDN(String dn, String attrID) {
        int beginIndex = dn.indexOf(attrID + '=');
        if (beginIndex < 0)
            return null;

        beginIndex += attrID.length() + 1;
        int endIndex = dn.indexOf(',', beginIndex);
        return endIndex >= 0 ? dn.substring(beginIndex, endIndex) : dn.substring(beginIndex);
    }

    /**
     * Constructs a Distinguished Name (DN) from two attribute ID-value pairs and a base DN. The format will be
     * "attrID1=attrValue1+attrID2=attrValue2,baseDN".
     *
     * @param attrID1    The ID of the first attribute.
     * @param attrValue1 The value of the first attribute.
     * @param attrID2    The ID of the second attribute.
     * @param attrValue2 The value of the second attribute.
     * @param baseDN     The base Distinguished Name.
     * @return The constructed DN string.
     */
    public static String dnOf(String attrID1, String attrValue1, String attrID2, String attrValue2, String baseDN) {
        return attrID1 + '=' + attrValue1 + '+' + attrID2 + '=' + attrValue2 + ',' + baseDN;
    }

    /**
     * Stores a difference in a boolean attribute as a {@link ModificationItem} for LDAP update. If the new value
     * {@code val} is different from the previous value {@code prev}, a modification item is created. If {@code val} is
     * equal to {@code defVal}, the attribute is removed; otherwise, it's replaced. Records changes in {@code ldapObj}
     * if provided.
     *
     * @param ldapObj The {@link ConfigurationChanges.ModifiedObject} to record changes, or {@code null} if changes do
     *                not need to be tracked.
     * @param mods    The list of {@link ModificationItem}s to add to.
     * @param attrId  The ID of the attribute.
     * @param prev    The previous boolean value.
     * @param val     The new boolean value.
     * @param defVal  The default boolean value for comparison.
     */
    public static void storeDiff(
            ConfigurationChanges.ModifiedObject ldapObj,
            List<ModificationItem> mods,
            String attrId,
            boolean prev,
            boolean val,
            boolean defVal) {
        if (val != prev) {
            mods.add(
                    (val == defVal) ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(attrId))
                            : new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                                    new BasicAttribute(attrId, LdapBuilder.toString(val))));
            if (ldapObj != null)
                ldapObj.add(new ConfigurationChanges.ModifiedAttribute(attrId, prev, val));
        }
    }

    /**
     * Stores a difference in an integer attribute as a {@link ModificationItem} for LDAP update. If the new value
     * {@code val} is different from the previous value {@code prev}, a modification item is created. If {@code val} is
     * equal to {@code defVal}, the attribute is removed; otherwise, it's replaced. Records changes in {@code ldapObj}
     * if provided.
     *
     * @param ldapObj The {@link ConfigurationChanges.ModifiedObject} to record changes, or {@code null} if changes do
     *                not need to be tracked.
     * @param mods    The list of {@link ModificationItem}s to add to.
     * @param attrId  The ID of the attribute.
     * @param prev    The previous integer value.
     * @param val     The new integer value.
     * @param defVal  The default integer value for comparison.
     */
    public static void storeDiff(
            ConfigurationChanges.ModifiedObject ldapObj,
            List<ModificationItem> mods,
            String attrId,
            int prev,
            int val,
            int defVal) {
        if (val != prev) {
            mods.add(
                    (val == defVal) ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(attrId))
                            : new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                                    new BasicAttribute(attrId, Integer.toString(val))));
            if (ldapObj != null)
                ldapObj.add(new ConfigurationChanges.ModifiedAttribute(attrId, prev, val));
        }
    }

    /**
     * Stores a difference in a long attribute as a {@link ModificationItem} for LDAP update. If the new value
     * {@code val} is different from the previous value {@code prev}, a modification item is created. If {@code val} is
     * equal to {@code defVal}, the attribute is removed; otherwise, it's replaced. Records changes in {@code ldapObj}
     * if provided.
     *
     * @param ldapObj The {@link ConfigurationChanges.ModifiedObject} to record changes, or {@code null} if changes do
     *                not need to be tracked.
     * @param mods    The list of {@link ModificationItem}s to add to.
     * @param attrId  The ID of the attribute.
     * @param prev    The previous long value.
     * @param val     The new long value.
     * @param defVal  The default long value for comparison.
     */
    public static void storeDiff(
            ConfigurationChanges.ModifiedObject ldapObj,
            List<ModificationItem> mods,
            String attrId,
            long prev,
            long val,
            long defVal) {
        if (val != prev) {
            mods.add(
                    (val == defVal) ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(attrId))
                            : new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                                    new BasicAttribute(attrId, Long.toString(val))));
            if (ldapObj != null)
                ldapObj.add(new ConfigurationChanges.ModifiedAttribute(attrId, prev, val));
        }
    }

    /**
     * Stores a difference in an object attribute as a {@link ModificationItem} for LDAP update. If the new value
     * {@code val} is different from the previous value {@code prev}, a modification item is created. If {@code val} is
     * null or equal to {@code defVal}, and {@code prev} was not null and not equal to {@code defVal}, the attribute is
     * removed. Otherwise, if {@code val} is not equal to {@code prev}, the attribute is replaced. Records changes in
     * {@code ldapObj} if provided.
     *
     * @param ldapObj The {@link ConfigurationChanges.ModifiedObject} to record changes, or {@code null} if changes do
     *                not need to be tracked.
     * @param mods    The list of {@link ModificationItem}s to add to.
     * @param attrId  The ID of the attribute.
     * @param prev    The previous object value.
     * @param val     The new object value.
     * @param defVal  The default object value for comparison.
     * @param <T>     The type of the object.
     */
    public static <T> void storeDiffObject(
            ConfigurationChanges.ModifiedObject ldapObj,
            List<ModificationItem> mods,
            String attrId,
            T prev,
            T val,
            T defVal) {
        if (val == null || val.equals(defVal)) {
            if (prev != null && !prev.equals(defVal)) {
                mods.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(attrId)));
                if (ldapObj != null)
                    ldapObj.add(
                            new ConfigurationChanges.ModifiedAttribute(attrId, LdapBuilder.toString(prev),
                                    LdapBuilder.toString(val)));
            }
        } else if (!val.equals(prev)) {
            mods.add(
                    new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                            new BasicAttribute(attrId, LdapBuilder.toString(val))));
            if (ldapObj != null)
                ldapObj.add(
                        new ConfigurationChanges.ModifiedAttribute(attrId, LdapBuilder.toString(prev),
                                LdapBuilder.toString(val)));
        }
    }

    /**
     * Stores differences in properties (represented as a map) as {@link ModificationItem}s for LDAP update. If the new
     * properties {@code props} are different from the previous properties {@code prevs}, a modification item is
     * created. If {@code props} is empty, the attribute is removed; otherwise, it's replaced with the new properties.
     * Records changes in {@code ldapObj} if provided.
     *
     * @param ldapObj The {@link ConfigurationChanges.ModifiedObject} to record changes, or {@code null} if changes do
     *                not need to be tracked.
     * @param mods    The list of {@link ModificationItem}s to add to.
     * @param attrID  The ID of the attribute.
     * @param prevs   The previous map of properties.
     * @param props   The new map of properties.
     * @param <T>     The type of the property values.
     */
    public static <T> void storeDiffProperties(
            ConfigurationChanges.ModifiedObject ldapObj,
            List<ModificationItem> mods,
            String attrID,
            Map<String, T> prevs,
            Map<String, T> props) {
        if (!equalsProperties(prevs, props)) {
            mods.add(
                    props.size() == 0 ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(attrID))
                            : new ModificationItem(DirContext.REPLACE_ATTRIBUTE, LdapBuilder.attr(attrID, props)));
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrID);
                for (String val : LdapBuilder.toStrings(props))
                    attribute.addValue(val);
                for (String prev : LdapBuilder.toStrings(prevs))
                    attribute.removeValue(prev);
                ldapObj.add(attribute);
            }
        }
    }

    /**
     * Compares two maps of properties for equality. The comparison considers both keys and values. Values are compared
     * using their {@code toString()} representation if they are not null.
     *
     * @param prevs The first map of properties.
     * @param props The second map of properties.
     * @param <T>   The type of the property values.
     * @return {@code true} if the maps are equal in size and content, {@code false} otherwise.
     */
    private static <T> boolean equalsProperties(Map<String, T> prevs, Map<String, T> props) {
        if (prevs == props)
            return true;

        if (prevs.size() != props.size())
            return false;

        for (Map.Entry<String, T> prop : props.entrySet()) {
            Object value = prop.getValue();
            Object prevValue = prevs.get(prop.getKey());
            if (!(value == null ? prevValue == null && prevs.containsKey(prop.getKey())
                    : prevValue != null && prevValue.toString().equals(value.toString())))
                return false;
        }
        return true;
    }

    /**
     * Stores differences in an array of integer values as {@link ModificationItem}s for LDAP update. This method boxes
     * the primitive int arrays into {@link Integer} arrays for comparison and modification tracking. Records changes in
     * {@code ldapObj} if provided.
     *
     * @param ldapObj The {@link ConfigurationChanges.ModifiedObject} to record changes, or {@code null} if changes do
     *                not need to be tracked.
     * @param mods    The list of {@link ModificationItem}s to add to.
     * @param attrId  The ID of the attribute.
     * @param prevs   The previous array of integer values.
     * @param vals    The new array of integer values.
     * @param defVals Optional default values for comparison.
     * @param <T>     The type of the values (typically Integer, but inferred from the boxed arrays).
     */
    public static <T> void storeDiff(
            ConfigurationChanges.ModifiedObject ldapObj,
            List<ModificationItem> mods,
            String attrId,
            int[] prevs,
            int[] vals,
            int... defVals) {
        storeDiff(
                ldapObj,
                mods,
                attrId,
                Arrays.stream(prevs).boxed().toArray(Integer[]::new),
                Arrays.stream(vals).boxed().toArray(Integer[]::new),
                Arrays.stream(defVals).boxed().toArray(Integer[]::new));
    }

    /**
     * Stores differences in an array of objects as {@link ModificationItem}s for LDAP update. If the new array
     * {@code vals} is different from the previous array {@code prevs}, a modification item is created. If {@code vals}
     * is empty or equal to {@code defVals}, the attribute is removed; otherwise, it's replaced. Records changes in
     * {@code ldapObj} if provided.
     *
     * @param ldapObj The {@link ConfigurationChanges.ModifiedObject} to record changes, or {@code null} if changes do
     *                not need to be tracked.
     * @param mods    The list of {@link ModificationItem}s to add to.
     * @param attrId  The ID of the attribute.
     * @param prevs   The previous array of objects.
     * @param vals    The new array of objects.
     * @param defVals Optional default values for comparison.
     * @param <T>     The type of the objects.
     */
    public static <T> void storeDiff(
            ConfigurationChanges.ModifiedObject ldapObj,
            List<ModificationItem> mods,
            String attrId,
            T[] prevs,
            T[] vals,
            T... defVals) {
        if (!LdapBuilder.equals(prevs, vals)) {
            mods.add(
                    (vals.length == 0 || LdapBuilder.equals(defVals, vals))
                            ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(attrId))
                            : new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr(attrId, vals)));
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrId);
                for (T val : vals)
                    attribute.addValue(val);
                for (T prev : prevs)
                    attribute.removeValue(prev);
                ldapObj.add(attribute);
            }
        }
    }

    /**
     * Stores differences in an array of string values with ordinal prefixes as {@link ModificationItem}s for LDAP
     * update. If the new array {@code vals} is different from the previous array {@code prevs}, a modification item is
     * created. If {@code vals} is empty, the attribute is removed; otherwise, it's replaced with the new values (with
     * ordinal prefixes). Records changes in {@code ldapObj} if provided.
     *
     * @param ldapObj The {@link ConfigurationChanges.ModifiedObject} to record changes, or {@code null} if changes do
     *                not need to be tracked.
     * @param mods    The list of {@link ModificationItem}s to add to.
     * @param attrId  The ID of the attribute.
     * @param prevs   The previous array of string values.
     * @param vals    The new array of string values.
     */
    public static void storeDiffWithOrdinalPrefix(
            ConfigurationChanges.ModifiedObject ldapObj,
            List<ModificationItem> mods,
            String attrId,
            String[] prevs,
            String[] vals) {
        if (!Arrays.equals(prevs, vals)) {
            String[] valsWithOrdinalPrefix = addOrdinalPrefix(vals);
            mods.add(
                    (vals.length == 0) ? new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(attrId))
                            : new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr(attrId, valsWithOrdinalPrefix)));
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrId);
                for (String val : valsWithOrdinalPrefix)
                    attribute.addValue(val);
                for (String prev : addOrdinalPrefix(prevs))
                    attribute.removeValue(prev);
                ldapObj.add(attribute);
            }
        }
    }

    /**
     * Stores differences in a list of {@link Connection} objects as {@link ModificationItem}s for LDAP update. If the
     * new list of connections {@code conns} is different from the previous list {@code prevs}, a modification item is
     * created. The attribute is replaced with the new connection references. Records changes in {@code ldapObj} if
     * provided.
     *
     * @param ldapObj  The {@link ConfigurationChanges.ModifiedObject} to record changes, or {@code null} if changes do
     *                 not need to be tracked.
     * @param mods     The list of {@link ModificationItem}s to add to.
     * @param attrId   The ID of the attribute.
     * @param prevs    The previous list of connections.
     * @param conns    The new list of connections.
     * @param deviceDN The Distinguished Name of the parent DICOM device.
     */
    public static void storeDiff(
            ConfigurationChanges.ModifiedObject ldapObj,
            List<ModificationItem> mods,
            String attrId,
            List<Connection> prevs,
            List<Connection> conns,
            String deviceDN) {
        if (!LdapBuilder.equalsConnRefs(prevs, conns, deviceDN)) {
            mods.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, connRefs(conns, deviceDN)));
            if (ldapObj != null) {
                ConfigurationChanges.ModifiedAttribute attribute = new ConfigurationChanges.ModifiedAttribute(attrId);
                for (Connection conn : conns)
                    attribute.addValue(LdapBuilder.dnOf(conn, deviceDN));
                for (Connection conn : prevs)
                    attribute.removeValue(LdapBuilder.dnOf(conn, deviceDN));
                ldapObj.add(attribute);
            }
        }
    }

    /**
     * Compares two lists of {@link Connection} objects based on their Distinguished Names (DNs) for equality. The order
     * of connections in the lists does not matter for equality.
     *
     * @param conns1   The first list of connections.
     * @param conns2   The second list of connections.
     * @param deviceDN The Distinguished Name of the parent DICOM device, used to construct connection DNs.
     * @return {@code true} if the lists contain the same connections (based on their generated DNs), {@code false}
     *         otherwise.
     */
    private static boolean equalsConnRefs(List<Connection> conns1, List<Connection> conns2, String deviceDN) {
        if (conns1.size() != conns2.size())
            return false;
        for (Connection conn1 : conns1)
            if (LdapBuilder.findByDN(deviceDN, conns2, dnOf(conn1, deviceDN)) == null)
                return false;
        return true;
    }

    /**
     * Compares two arrays of objects for equality, ignoring order. This method checks if both arrays contain the same
     * elements, regardless of their position.
     *
     * @param a   The first array.
     * @param a2  The second array.
     * @param <T> The type of the objects in the arrays.
     * @return {@code true} if the arrays contain the same elements, {@code false} otherwise.
     */
    public static <T> boolean equals(T[] a, T[] a2) {
        int length = a.length;
        if (a2.length != length)
            return false;

        outer: for (Object o1 : a) {
            for (Object o2 : a2)
                if (o1.equals(o2))
                    continue outer;
            return false;
        }
        return true;
    }

    /**
     * Finds a {@link Connection} in a list by its Distinguished Name (DN).
     *
     * @param deviceDN The Distinguished Name of the parent DICOM device, used to construct the DN for comparison.
     * @param conns    The list of {@link Connection} objects to search within.
     * @param dn       The Distinguished Name of the connection to find.
     * @return The {@link Connection} object if found, or {@code null} if no matching connection is found.
     */
    public static Connection findByDN(String deviceDN, List<Connection> conns, String dn) {
        for (Connection conn : conns)
            if (dn.equals(dnOf(conn, deviceDN)))
                return conn;
        return null;
    }

    /**
     * Retrieves a {@link Boolean} object from an {@link Attribute}.
     *
     * @param attr   The {@link Attribute} to retrieve the value from.
     * @param defVal The default {@link Boolean} value if the attribute is null.
     * @return The {@link Boolean} value of the attribute, or the default value if the attribute is null.
     * @throws NamingException if an LDAP naming error occurs during attribute retrieval.
     */
    public static Boolean booleanValue(Attribute attr, Boolean defVal) throws NamingException {
        return attr != null ? Boolean.valueOf((String) attr.get()) : defVal;
    }

    /**
     * Retrieves a primitive boolean value from an {@link Attribute}.
     *
     * @param attr   The {@link Attribute} to retrieve the value from.
     * @param defVal The default primitive boolean value if the attribute is null.
     * @return The primitive boolean value of the attribute, or the default value if the attribute is null.
     * @throws NamingException if an LDAP naming error occurs during attribute retrieval.
     */
    public static boolean booleanValue(Attribute attr, boolean defVal) throws NamingException {
        return attr != null ? Boolean.parseBoolean((String) attr.get()) : defVal;
    }

    /**
     * Retrieves a string value from an {@link Attribute}.
     *
     * @param attr   The {@link Attribute} to retrieve the value from.
     * @param defVal The default string value if the attribute is null.
     * @return The string value of the attribute, or the default value if the attribute is null.
     * @throws NamingException if an LDAP naming error occurs during attribute retrieval.
     */
    public static String stringValue(Attribute attr, String defVal) throws NamingException {
        return attr != null ? stringValue(attr.get()) : defVal;
    }

    /**
     * Converts an object retrieved from an LDAP attribute to a string. Handles byte arrays by converting them to UTF-8
     * strings. Other objects are converted using {@code toString()}.
     *
     * @param o The object to convert.
     * @return The string representation of the object.
     */
    private static String stringValue(Object o) {
        return o instanceof byte[] ? new String((byte[]) o, Charset.UTF_8) : (String) o;
    }

    /**
     * Retrieves a {@link TimeZone} object from an {@link Attribute}. The attribute value is expected to be a valid
     * TimeZone ID string.
     *
     * @param attr   The {@link Attribute} to retrieve the value from.
     * @param defVal The default {@link TimeZone} value if the attribute is null.
     * @return The {@link TimeZone} object, or the default value if the attribute is null.
     * @throws NamingException if an LDAP naming error occurs during attribute retrieval.
     */
    public static TimeZone timeZoneValue(Attribute attr, TimeZone defVal) throws NamingException {
        return attr != null ? TimeZone.getTimeZone((String) attr.get()) : defVal;
    }

    /**
     * Retrieves a {@link Date} object from an {@link Attribute} representing a DICOM DateTime string. The attribute
     * value is parsed using {@link Format#parseDT(TimeZone, String, DatePrecision)}.
     *
     * @param attr The {@link Attribute} to retrieve the value from.
     * @return The {@link Date} object, or {@code null} if the attribute is null.
     * @throws NamingException if an LDAP naming error occurs during attribute retrieval.
     */
    public static Date dateTimeValue(Attribute attr) throws NamingException {
        return attr != null ? Format.parseDT(null, (String) attr.get(), new DatePrecision()) : null;
    }

    /**
     * Retrieves an enum value from an {@link Attribute}. The attribute value is expected to be the name of an enum
     * constant.
     *
     * @param enumType The class of the enum.
     * @param attr     The {@link Attribute} to retrieve the value from.
     * @param defVal   The default enum value if the attribute is null.
     * @param <T>      The enum type.
     * @return The enum value, or the default value if the attribute is null.
     * @throws NamingException if an LDAP naming error occurs during attribute retrieval.
     */
    public static <T extends Enum<T>> T enumValue(Class<T> enumType, Attribute attr, T defVal) throws NamingException {
        return attr != null ? Enum.valueOf(enumType, (String) attr.get()) : defVal;
    }

    /**
     * Retrieves an array of enum values from an {@link Attribute}. Each value in the attribute is expected to be the
     * name of an enum constant.
     *
     * @param enumType The class of the enum.
     * @param attr     The {@link Attribute} to retrieve the values from.
     * @param <T>      The enum type.
     * @return An array of enum values. Returns an empty array if the attribute is null or contains no values.
     * @throws NamingException if an LDAP naming error occurs during attribute retrieval.
     */
    public static <T extends Enum<T>> T[] enumArray(Class<T> enumType, Attribute attr) throws NamingException {
        T[] a = (T[]) Array.newInstance(enumType, attr != null ? attr.size() : 0);
        for (int i = 0; i < a.length; i++)
            a[i] = Enum.valueOf(enumType, (String) attr.get(i));

        return a;
    }

    /**
     * Retrieves an array of string values from an {@link Attribute}.
     *
     * @param attr    The {@link Attribute} to retrieve the values from.
     * @param defVals Optional default string values if the attribute is null.
     * @return An array of string values, or the default values if the attribute is null.
     * @throws NamingException if an LDAP naming error occurs during attribute retrieval.
     */
    public static String[] stringArray(Attribute attr, String... defVals) throws NamingException {
        if (attr == null)
            return defVals;

        String[] ss = new String[attr.size()];
        for (int i = 0; i < ss.length; i++)
            ss[i] = (String) attr.get(i);

        return ss;
    }

    /**
     * Retrieves a long value from an {@link Attribute}. The attribute value is expected to be a string representation
     * of a long.
     *
     * @param attr   The {@link Attribute} to retrieve the value from.
     * @param defVal The default long value if the attribute is null.
     * @return The long value of the attribute, or the default value if the attribute is null.
     * @throws NamingException if an LDAP naming error occurs during attribute retrieval.
     */
    public static long longValue(Attribute attr, long defVal) throws NamingException {
        return attr != null ? Long.parseLong((String) attr.get()) : defVal;
    }

    /**
     * Retrieves an integer value from an {@link Attribute}. The attribute value is expected to be a string
     * representation of an integer.
     *
     * @param attr   The {@link Attribute} to retrieve the value from.
     * @param defVal The default integer value if the attribute is null.
     * @return The integer value of the attribute, or the default value if the attribute is null.
     * @throws NamingException if an LDAP naming error occurs during attribute retrieval.
     */
    public static int intValue(Attribute attr, int defVal) throws NamingException {
        return attr != null ? Integer.parseInt((String) attr.get()) : defVal;
    }

    /**
     * Retrieves a {@link Long} object from an {@link Attribute}. The attribute value is expected to be a string
     * representation of a long.
     *
     * @param attr   The {@link Attribute} to retrieve the value from.
     * @param defVal The default {@link Long} value if the attribute is null.
     * @return The {@link Long} object, or the default value if the attribute is null.
     * @throws NamingException if an LDAP naming error occurs during attribute retrieval.
     */
    public static Long longValue(Attribute attr, Long defVal) throws NamingException {
        return attr != null ? Long.valueOf((String) attr.get()) : defVal;
    }

    /**
     * Retrieves an {@link Integer} object from an {@link Attribute}. The attribute value is expected to be a string
     * representation of an integer.
     *
     * @param attr   The {@link Attribute} to retrieve the value from.
     * @param defVal The default {@link Integer} value if the attribute is null.
     * @return The {@link Integer} object, or the default value if the attribute is null.
     * @throws NamingException if an LDAP naming error occurs during attribute retrieval.
     */
    public static Integer intValue(Attribute attr, Integer defVal) throws NamingException {
        return attr != null ? Integer.valueOf((String) attr.get()) : defVal;
    }

    /**
     * Retrieves a {@link Code} object from an {@link Attribute}. The attribute value is expected to be a string that
     * can be used to construct a {@link Code} object.
     *
     * @param attr The {@link Attribute} to retrieve the value from.
     * @return The {@link Code} object, or {@code null} if the attribute is null.
     * @throws NamingException if an LDAP naming error occurs during attribute retrieval.
     */
    public static Code codeValue(Attribute attr) throws NamingException {
        return attr != null ? new Code((String) attr.get()) : null;
    }

    /**
     * Retrieves an {@link Issuer} object from an {@link Attribute}. The attribute value is expected to be a string that
     * can be used to construct an {@link Issuer} object.
     *
     * @param attr The {@link Attribute} to retrieve the value from.
     * @return The {@link Issuer} object, or {@code null} if the attribute is null.
     * @throws NamingException if an LDAP naming error occurs during attribute retrieval.
     */
    public static Issuer issuerValue(Attribute attr) throws NamingException {
        return attr != null ? new Issuer((String) attr.get()) : null;
    }

    /**
     * Retrieves an array of integer values from an {@link Attribute}. Each value in the attribute is expected to be a
     * string representation of an integer.
     *
     * @param attr The {@link Attribute} to retrieve the values from.
     * @return An array of integer values. Returns an empty array if the attribute is null or contains no values.
     * @throws NamingException if an LDAP naming error occurs during attribute retrieval.
     */
    public static int[] intArray(Attribute attr) throws NamingException {
        if (attr == null)
            return new int[] {};

        int[] a = new int[attr.size()];
        for (int i = 0; i < a.length; i++)
            a[i] = Integer.parseInt((String) attr.get(i));

        return a;
    }

    /**
     * Retrieves an array of {@link Code} objects from an {@link Attribute}. Each value in the attribute is expected to
     * be a string that can be used to construct a {@link Code} object.
     *
     * @param attr The {@link Attribute} to retrieve the values from.
     * @return An array of {@link Code} objects. Returns an empty array if the attribute is null or contains no values.
     * @throws NamingException if an LDAP naming error occurs during attribute retrieval.
     */
    public static Code[] codeArray(Attribute attr) throws NamingException {
        if (attr == null)
            return EMPTY_CODES;

        Code[] codes = new Code[attr.size()];
        for (int i = 0; i < codes.length; i++)
            codes[i] = new Code((String) attr.get(i));

        return codes;
    }

    /**
     * Finds a {@link Connection} object within a {@link Device} by its Distinguished Name (DN). The connection's DN is
     * constructed using {@link #dnOf(Connection, String)} for comparison.
     *
     * @param connDN   The Distinguished Name of the connection to find.
     * @param deviceDN The Distinguished Name of the parent DICOM device.
     * @param device   The {@link Device} object to search within its connections.
     * @return The found {@link Connection} object.
     * @throws NameNotFoundException if no connection with the specified DN is found within the device.
     */
    public static Connection findConnection(String connDN, String deviceDN, Device device)
            throws NameNotFoundException {
        for (Connection conn : device.listConnections())
            if (dnOf(conn, deviceDN).equalsIgnoreCase(connDN))
                return conn;

        throw new NameNotFoundException(connDN);
    }

    /**
     * Converts a boolean value to its string representation ("TRUE" or "FALSE").
     *
     * @param val The boolean value to convert.
     * @return The string representation of the boolean value.
     */
    public static String toString(boolean val) {
        return val ? "TRUE" : "FALSE";
    }

    /**
     * Converts an object to its string representation. Handles {@link Boolean}, {@link TimeZone}, and {@link Date}
     * types specifically. For {@link Boolean}, it uses {@link #toString(boolean)}. For {@link TimeZone}, it returns its
     * ID. For {@link Date}, it formats it as a DICOM DateTime string. For other objects, it uses their
     * {@code toString()} method.
     *
     * @param o The object to convert.
     * @return The string representation of the object, or {@code null} if the object is {@code null}.
     */
    public static String toString(Object o) {
        return (o instanceof Boolean) ? toString(((Boolean) o).booleanValue())
                : (o instanceof TimeZone) ? ((TimeZone) o).getID()
                        : (o instanceof Date) ? Format.formatDT(null, (Date) o) : o != null ? o.toString() : null;
    }

    /**
     * Creates a new {@link Attributes} object with a specified object class and an initial attribute. The
     * {@link Attributes} object is created with case-ignore enabled.
     *
     * @param objectclass The object class for the new attributes.
     * @param attrID      The ID of the initial attribute.
     * @param attrVal     The value of the initial attribute.
     * @return A new {@link Attributes} object containing the specified object class and initial attribute.
     */
    public static Attributes attrs(String objectclass, String attrID, String attrVal) {
        Attributes attrs = new BasicAttributes(true); // case-ignore
        attrs.put("objectclass", objectclass);
        storeNotNullOrDef(attrs, attrID, attrVal, null);
        return attrs;
    }

    /**
     * Adds an ordinal prefix to each string in an array. The prefix is in the format "{N}" where N is the 0-based index
     * of the string in the array. The index N is represented by a character from {@link #DIGITS}.
     *
     * @param vals The array of strings to modify.
     * @return A new array of strings with ordinal prefixes.
     */
    public static String[] addOrdinalPrefix(String[] vals) {
        String[] result = new String[vals.length];
        for (int i = 0; i < result.length; i++) {
            String val = vals[i];
            int vallen = val.length();
            char[] cs = new char[3 + vallen];
            cs[0] = '{';
            cs[1] = DIGITS[i];
            cs[2] = '}';
            val.getChars(0, vallen, cs, 3);
            result[i] = new String(cs);
        }
        return result;
    }

    /**
     * Removes the ordinal prefix from each string in an array. Assumes the strings were previously prefixed by
     * {@link #addOrdinalPrefix(String[])}. The resulting array of strings is sorted alphabetically after prefix
     * removal.
     *
     * @param vals The array of strings with ordinal prefixes (e.g., "{0}value").
     * @return A new array of strings with prefixes removed, and sorted alphabetically.
     */
    public static String[] removeOrdinalPrefix(String[] vals) {
        Arrays.sort(vals);
        String[] result = new String[vals.length];
        for (int i = 0; i < result.length; i++)
            result[i] = vals[i].substring(3);
        return result;
    }

    /**
     * Extracts the DICOM device name from a Distinguished Name (DN) string. It looks for the "dicomDeviceName="
     * component in the DN.
     *
     * @param name The DN string.
     * @return The DICOM device name, or {@code null} if the "dicomDeviceName" component is not found.
     */
    public static String cutDeviceName(String name) {
        int start = name.indexOf("dicomDeviceName=");
        if (start < 0)
            return null;

        start += 16; // Length of "dicomDeviceName="
        int end = name.indexOf(',', start);
        return end < 0 ? name.substring(start) : name.substring(start, end);
    }

}
