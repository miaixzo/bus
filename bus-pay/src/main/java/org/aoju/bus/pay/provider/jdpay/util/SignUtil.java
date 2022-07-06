package org.aoju.bus.pay.provider.jdpay.util;

import org.aoju.bus.core.codec.Base64;
import org.aoju.bus.core.lang.Normal;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SignUtil {

    private static final List<String> unSignKeyList = Arrays.asList("merchantSign", "token", "version");

    public static String sign4SelectedKeys(Object object, String rsaPriKey, List<String> signKeyList) {
        String result = "";

        try {
            String sourceSignString = signString4SelectedKeys(object, signKeyList);
            String sha256SourceSignString = SHAUtil.Encrypt(sourceSignString, null);

            byte[] newsks = RSACoder.encryptByPrivateKey(sha256SourceSignString.getBytes(StandardCharsets.UTF_8), rsaPriKey);
            result = Base64.encode(newsks);
        } catch (Exception e) {
            throw new RuntimeException("sign4SelectedKeys>error", e);
        }
        return result;
    }


    public static String signRemoveSelectedKeys(Object object, String rsaPriKey, List<String> signKeyList) {
        String result = "";

        try {
            String sourceSignString = signString(object, signKeyList);

            String sha256SourceSignString = SHAUtil.Encrypt(sourceSignString, null);

            byte[] newK = RSACoder.encryptByPrivateKey(sha256SourceSignString.getBytes(StandardCharsets.UTF_8), rsaPriKey);
            result = Base64.encode(newK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public static String signString(Object object, List<String> unSignKeyList) throws IllegalArgumentException, IllegalAccessException {
        TreeMap<String, Object> map = objectToMap(object);


        StringBuilder sb = new StringBuilder();

        for (String text : unSignKeyList) {
            map.remove(text);
        }

        Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            if (entry.getValue() == null) {
                continue;
            }
            String value = (String) entry.getValue();
            if (value.trim().length() > 0) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }

        String result = sb.toString();
        if (result.endsWith("&")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }


    private static String signString4SelectedKeys(Object object, List<String> signedKeyList) throws IllegalArgumentException, IllegalAccessException {
        TreeMap<String, Object> map = objectToMap(object);
        if (map == null || map.isEmpty() || signedKeyList == null || signedKeyList.isEmpty()) {
            return null;
        }

        TreeMap<String, Object> signMap = new TreeMap<>();

        StringBuilder sb = new StringBuilder();

        for (String text : signedKeyList) {
            Object o = map.get(text);
            if (o != null) {
                signMap.put(text, o);
                continue;
            }
            signMap.put(text, Normal.EMPTY);
        }


        Iterator iterator = signMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            sb.append(entry.getKey() + "=" + (
                    (entry.getValue() == null) ? "" : entry.getValue()) + "&");
        }

        String result = sb.toString();
        if (result.endsWith("&")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }


    public static TreeMap<String, Object> objectToMap(Object object) throws IllegalArgumentException, IllegalAccessException {
        TreeMap<String, Object> map = new TreeMap<>();
        if (object instanceof Map) {
            Map<String, Object> objectMap = (Map) object;
            for (String text : objectMap.keySet()) {
                map.put(text, objectMap.get(text));
            }
            return map;
        }

        for (Class<?> cls = object.getClass(); cls != Object.class; cls = cls.getSuperclass()) {

            Field[] fields = cls.getDeclaredFields();
            for (Field f : fields) {
                f.setAccessible(true);
                map.put(f.getName(), f.get(object));
            }
        }
        return map;
    }


    public static String sign4PCString(Object object, List<String> unSignKeyList) throws IllegalArgumentException, IllegalAccessException {
        TreeMap<String, Object> map = objectToMap(object);
        StringBuilder sb = new StringBuilder();
        for (String text : unSignKeyList) {
            map.remove(text);
        }

        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            sb.append(entry.getKey() + "=" + ((entry.getValue() == null) ? "" : entry.getValue()) + "&");
        }

        String result = sb.toString();
        if (result.endsWith("&")) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }
}
