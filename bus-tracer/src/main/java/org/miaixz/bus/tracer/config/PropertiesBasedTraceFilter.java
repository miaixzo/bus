/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.tracer.config;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.tracer.Builder;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A TraceFilterConfiguration that is based on a {@link PropertyChain}.
 * The default property chain may be obtained by the {@link #loadPropertyChain()} method.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class PropertiesBasedTraceFilter implements TraceFilterConfig {

    static final String Trace_CONFIG_PREFIX = "Builder.";
    static final String PROFILED_PREFIX = Trace_CONFIG_PREFIX + "profile.";
    static final String Trace_DEFAULT_PROFILE_PREFIX = Trace_CONFIG_PREFIX + Builder.DEFAULT + Symbol.DOT;
    static final String GENERATE_INVOCATION_ID = "invocationIdLength";
    static final String GENERATE_SESSION_ID = "sessionIdLength";

    private final PropertyChain propertyChain;
    private final String profileName;
    private final Map<String, List<Pattern>> patternCache = new ConcurrentHashMap<>();

    public PropertiesBasedTraceFilter(PropertyChain propertyChain) {
        this(propertyChain, null);
    }

    public PropertiesBasedTraceFilter(PropertyChain propertyChain,
                                      String profileName) {
        this.propertyChain = propertyChain;
        this.profileName = profileName;
    }

    /**
     * Loads a layered property chain based on:
     * <ol>
     * <li>System properties</li>
     * <li>merged entries from all {@code /META-INF/Builder.properties} files on the classpath (loaded in undefined order)</li>
     * <li>merged entries from all {@code /META-INF/Builder.default.properties} files on the classpath (loaded in undefined order)</li>
     * </ol>
     *
     * @return the propertyChain
     */
    public static PropertyChain loadPropertyChain() {
        try {
            final Properties TraceDefaultFileProperties = new TracePropertiesFileLoader().loadTraceProperties(Builder.TRACE_DEFAULT_PROPERTIES_FILE);
            final Properties TraceFileProperties = new TracePropertiesFileLoader().loadTraceProperties(Builder.TRACE_PROPERTIES_FILE);
            return PropertyChain.build(System.getProperties(), TraceFileProperties, TraceDefaultFileProperties);
        } catch (IOException ioe) {
            throw new IllegalStateException("Could not load TraceProperties: " + ioe.getMessage(), ioe);
        }
    }

    private String getProfiledOrDefaultProperty(final String propertyName) {
        if (null != profileName && !Builder.DEFAULT.equals(profileName)) {
            final String profiledProperty = propertyChain.getProperty(PROFILED_PREFIX + profileName + Symbol.C_DOT + propertyName);
            if (null != profiledProperty)
                return profiledProperty;
        }
        return propertyChain.getProperty(Trace_DEFAULT_PROFILE_PREFIX + propertyName);
    }

    @Override
    public boolean shouldProcessParam(String paramName, Channel channel) {
        final String messageTypePropertyValue = getProfiledOrDefaultProperty(channel.name());
        final List<Pattern> patterns = retrievePatternsForPropertyValue(messageTypePropertyValue);
        return anyPatternMatchesParamName(patterns, paramName);
    }

    @Override
    public boolean shouldProcessContext(final Channel channel) {
        final String messageTypePropertyValue = getProfiledOrDefaultProperty(channel.name());
        return !StringKit.isEmpty(messageTypePropertyValue);
    }

    @Override
    public boolean shouldGenerateInvocationId() {
        return generatedInvocationIdLength() > 0;
    }

    @Override
    public int generatedInvocationIdLength() {
        return parseIntOrZero(getProfiledOrDefaultProperty(GENERATE_INVOCATION_ID));
    }

    @Override
    public boolean shouldGenerateSessionId() {
        return generatedSessionIdLength() > 0;
    }

    @Override
    public int generatedSessionIdLength() {
        return parseIntOrZero(getProfiledOrDefaultProperty(GENERATE_SESSION_ID));
    }

    @Override
    public Map<String, String> filterDeniedParams(final Map<String, String> unfiltered, final Channel channel) {
        final Map<String, String> filtered = new HashMap<>(unfiltered.size());
        for (Map.Entry<String, String> entry : unfiltered.entrySet()) {
            if (shouldProcessParam(entry.getKey(), channel)) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered;
    }

    private int parseIntOrZero(String intString) {
        try {
            return Integer.parseInt(intString);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    private boolean anyPatternMatchesParamName(Iterable<Pattern> patterns, String paramName) {
        for (Pattern pattern : patterns) {
            if (patternMatchesParamName(pattern, paramName))
                return true;
        }
        return false;
    }

    private boolean patternMatchesParamName(Pattern pattern, String paramName) {
        return ".*".equals(pattern.pattern()) || pattern.matcher(paramName).matches();
    }

    private List<Pattern> retrievePatternsForPropertyValue(final String propertyValue) {
        if (null == propertyValue) {
            return Collections.emptyList();
        }
        final List<Pattern> patterns = patternCache.get(propertyValue);
        if (null != patterns) {
            return patterns;
        }

        final List<Pattern> unmodPatterns = Collections.unmodifiableList(extractPatterns(propertyValue));
        patternCache.put(propertyValue, unmodPatterns);
        return unmodPatterns;
    }

    List<Pattern> extractPatterns(final String propertyValue) {
        if (null == propertyValue)
            return Collections.emptyList();

        final List<Pattern> trimmedPatterns = new ArrayList<>();
        final StringTokenizer tokenizer = new StringTokenizer(propertyValue, Symbol.COMMA);
        while (tokenizer.hasMoreTokens()) {
            final String trimmedString = tokenizer.nextToken().trim();
            if (!trimmedString.isEmpty()) {
                try {
                    trimmedPatterns.add(Pattern.compile(trimmedString));
                } catch (PatternSyntaxException e) {
                    Logger.error("Can not compile pattern '" + trimmedString + "'. Message: " + e.getMessage() + " -- Ignore pattern");
                }
            }
        }
        return trimmedPatterns;
    }

}
