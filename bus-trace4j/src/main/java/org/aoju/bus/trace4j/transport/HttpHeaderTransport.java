package org.aoju.bus.trace4j.transport;

import org.aoju.bus.core.consts.Charset;
import org.aoju.bus.logger.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

public class HttpHeaderTransport {

    Map<String, String> parse(String serialized) {
        final StringTokenizer pairTokenizer = new StringTokenizer(serialized.trim(), ",");
        final Map<String, String> context = new HashMap<>();
        while (pairTokenizer.hasMoreTokens()) {
            final String pairStr = pairTokenizer.nextToken();
            final String[] keyValuePair = pairStr.split("=");
            if (keyValuePair.length != 2) {
                continue;
            }
            try {
                final String key = URLDecoder.decode(keyValuePair[0], Charset.DEFAULT_UTF_8);
                final String value = URLDecoder.decode(keyValuePair[1], Charset.DEFAULT_UTF_8);
                context.put(key, value);
            } catch (UnsupportedEncodingException e) {
                Logger.error("Charset not found", e);
            }
        }

        return context;
    }

    public Map<String, String> parse(List<String> serializedElements) {
        final Map<String, String> contextMap = new HashMap<>();
        for (String serializedElement : serializedElements) {
            contextMap.putAll(parse(serializedElement));
        }

        return contextMap;
    }

    public String render(Map<String, String> context) {
        final StringBuilder sb = new StringBuilder(128);
        for (Iterator<Map.Entry<String, String>> iterator = context.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, String> entry = iterator.next();
            try {
                final String key = URLEncoder.encode(entry.getKey().trim(), Charset.DEFAULT_UTF_8);
                final String value = URLEncoder.encode(entry.getValue().trim(), Charset.DEFAULT_UTF_8);
                sb.append(key).append('=').append(value);
                if (iterator.hasNext()) {
                    sb.append(',');
                }
            } catch (UnsupportedEncodingException e) {
                Logger.error("Charset not found", e);
            }
        }
        return sb.toString();
    }

}
