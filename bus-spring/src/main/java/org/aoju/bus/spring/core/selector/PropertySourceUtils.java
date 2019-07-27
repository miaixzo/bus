package org.aoju.bus.spring.core.selector;

import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>Title: Convenience class for manipulating PropertySources.</p>
 * <p>Description: </p>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class PropertySourceUtils {

    /**
     * Return a Map of all values from the specified {@link PropertySources} that start
     * with a particular key.
     *
     * @param propertySources the property sources to scan
     * @param keyPrefix       the key prefixes to test
     * @return a map of all sub properties starting with the specified key prefixes.
     * @see PropertySourceUtils#getSubProperties(PropertySources, String, String)
     */
    public static Map<String, Object> getSubProperties(PropertySources propertySources,
                                                       String keyPrefix) {
        return PropertySourceUtils.getSubProperties(propertySources, null, keyPrefix);
    }

    /**
     * Return a Map of all values from the specified {@link PropertySources} that start
     * with a particular key.
     *
     * @param propertySources the property sources to scan
     * @param rootPrefix      a root prefix to be prepended to the keyPrefix (can be
     *                        {@code null})
     * @param keyPrefix       the key prefixes to test
     * @return a map of all sub properties starting with the specified key prefixes.
     * @see #getSubProperties(PropertySources, String, String)
     */
    public static Map<String, Object> getSubProperties(PropertySources propertySources,
                                                       String rootPrefix, String keyPrefix) {
        RelaxedNames keyPrefixes = new RelaxedNames(keyPrefix);
        Map<String, Object> subProperties = new LinkedHashMap<String, Object>();
        for (PropertySource<?> source : propertySources) {
            if (source instanceof EnumerablePropertySource) {
                for (String name : ((EnumerablePropertySource<?>) source)
                        .getPropertyNames()) {
                    String key = PropertySourceUtils.getSubKey(name, rootPrefix,
                            keyPrefixes);
                    if (key != null && !subProperties.containsKey(key)) {
                        subProperties.put(key, source.getProperty(name));
                    }
                }
            }
        }
        return Collections.unmodifiableMap(subProperties);
    }

    private static String getSubKey(String name, String rootPrefixes,
                                    RelaxedNames keyPrefix) {
        rootPrefixes = (rootPrefixes != null ? rootPrefixes : "");
        for (String rootPrefix : new RelaxedNames(rootPrefixes)) {
            for (String candidateKeyPrefix : keyPrefix) {
                if (name.startsWith(rootPrefix + candidateKeyPrefix)) {
                    return name.substring((rootPrefix + candidateKeyPrefix).length());
                }
            }
        }
        return null;
    }

}