package org.aoju.bus.spring.core.selector;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySourcesPropertyResolver;

import java.util.Map;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class RelaxedPropertyResolver implements PropertyResolver {

    private final PropertyResolver resolver;

    private final String prefix;

    public RelaxedPropertyResolver(PropertyResolver resolver) {
        this(resolver, null);
    }

    public RelaxedPropertyResolver(PropertyResolver resolver, String prefix) {
        this.resolver = resolver;
        this.prefix = (prefix != null ? prefix : "");
    }

    /**
     * Return a property resolver for the environment, preferring first that ignores
     * unresolvable nested placeholders.
     *
     * @param environment the source environment
     * @param prefix      the prefix
     * @return a property resolver for the environment
     * @since 1.4.3
     */
    public static RelaxedPropertyResolver ignoringUnresolvableNestedPlaceholders(
            Environment environment, String prefix) {
        PropertyResolver resolver = environment;
        if (environment instanceof ConfigurableEnvironment) {
            resolver = new PropertySourcesPropertyResolver(
                    ((ConfigurableEnvironment) environment).getPropertySources());
            ((PropertySourcesPropertyResolver) resolver)
                    .setIgnoreUnresolvableNestedPlaceholders(true);
        }
        return new RelaxedPropertyResolver(resolver, prefix);
    }

    @Override
    public String getRequiredProperty(String key) throws IllegalStateException {
        return getRequiredProperty(key, String.class);
    }

    @Override
    public <T> T getRequiredProperty(String key, Class<T> targetType)
            throws IllegalStateException {
        T value = getProperty(key, targetType);
        return value;
    }

    @Override
    public String getProperty(String key) {
        return getProperty(key, String.class, null);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return getProperty(key, String.class, defaultValue);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        return getProperty(key, targetType, null);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        RelaxedNames prefixes = new RelaxedNames(this.prefix);
        RelaxedNames keys = new RelaxedNames(key);
        for (String prefix : prefixes) {
            for (String relaxedKey : keys) {
                if (this.resolver.containsProperty(prefix + relaxedKey)) {
                    return this.resolver.getProperty(prefix + relaxedKey, targetType);
                }
            }
        }
        return defaultValue;
    }

    @Override
    public boolean containsProperty(String key) {
        RelaxedNames prefixes = new RelaxedNames(this.prefix);
        RelaxedNames keys = new RelaxedNames(key);
        for (String prefix : prefixes) {
            for (String relaxedKey : keys) {
                if (this.resolver.containsProperty(prefix + relaxedKey)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String resolvePlaceholders(String text) {
        throw new UnsupportedOperationException(
                "Unable to resolve placeholders with relaxed properties");
    }

    @Override
    public String resolveRequiredPlaceholders(String text)
            throws IllegalArgumentException {
        throw new UnsupportedOperationException(
                "Unable to resolve placeholders with relaxed properties");
    }

    /**
     * Return a Map of all values from all underlying properties that start with the
     * specified key. NOTE: this method can only be used if the underlying resolver is a
     * {@link ConfigurableEnvironment}.
     *
     * @param keyPrefix the key prefix used to filter results
     * @return a map of all sub properties starting with the specified key prefix.
     * @see PropertySourceUtils#getSubProperties
     */
    public Map<String, Object> getSubProperties(String keyPrefix) {
        ConfigurableEnvironment env = (ConfigurableEnvironment) this.resolver;
        return PropertySourceUtils.getSubProperties(env.getPropertySources(), this.prefix,
                keyPrefix);
    }

}