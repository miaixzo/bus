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
package org.miaixz.bus.image.metric;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.logger.Logger;

import javax.xml.stream.XMLStreamReader;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public final class TagValue {

    public static String formatDateTime(TemporalAccessor date) {
        Locale locale = Locale.getDefault();
        if (date instanceof LocalDate) {
            return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).format(date);
        } else if (date instanceof LocalTime) {
            return DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withLocale(locale).format(date);
        } else if (date instanceof LocalDateTime) {
            return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale).format(date);
        } else if (date instanceof Instant) {
            return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale).format(((Instant) date).atZone(ZoneId.systemDefault()));
        }
        return Normal.EMPTY;
    }

    public static String getTagAttribute(XMLStreamReader xmler, String attribute, String defaultValue) {
        if (null != attribute) {
            String val = xmler.getAttributeValue(null, attribute);
            if (null != val) {
                return val;
            }
        }
        return defaultValue;
    }

    public static String[] getStringArrayTagAttribute(XMLStreamReader xmler, String attribute, String[] defaultValue) {
        return getStringArrayTagAttribute(xmler, attribute, defaultValue, Symbol.BACKSLASH);
    }

    public static String[] getStringArrayTagAttribute(XMLStreamReader xmler, String attribute, String[] defaultValue,
                                                      String separator) {
        if (null != attribute) {
            String val = xmler.getAttributeValue(null, attribute);
            if (null != val) {
                return val.split(Pattern.quote(separator));
            }
        }
        return defaultValue;
    }


    public static Integer getIntegerTagAttribute(XMLStreamReader xmler, String attribute, Integer defaultValue) {
        if (null != attribute) {
            String val = xmler.getAttributeValue(null, attribute);
            try {
                if (null != val) {
                    return Integer.valueOf(val);
                }
            } catch (NumberFormatException e) {
                Logger.error("Cannot parse integer {} of {}", val, attribute);
            }
        }
        return defaultValue;
    }

    public static int[] getIntArrayTagAttribute(XMLStreamReader xmler, String attribute, int[] defaultValue) {
        return getIntArrayTagAttribute(xmler, attribute, defaultValue, Symbol.BACKSLASH);
    }

    public static int[] getIntArrayTagAttribute(XMLStreamReader xmler, String attribute, int[] defaultValue,
                                                String separator) {
        if (null != attribute) {
            String val = xmler.getAttributeValue(null, attribute);
            if (null != val) {
                String[] texts = val.split(Pattern.quote(separator));
                int[] vals = new int[texts.length];
                for (int i = 0; i < texts.length; i++) {
                    vals[i] = Integer.parseInt(texts[i], 10);
                }
                return vals;
            }
        }
        return defaultValue;
    }

    public static Double getDoubleTagAttribute(XMLStreamReader xmler, String attribute, Double defaultValue) {
        if (null != attribute) {
            String val = xmler.getAttributeValue(null, attribute);
            try {
                if (null != val) {
                    return Double.valueOf(val);
                }
            } catch (NumberFormatException e) {
                Logger.error("Cannot parse double {} of {}", val, attribute);
            }
        }
        return defaultValue;
    }

    public static double[] getDoubleArrayTagAttribute(XMLStreamReader xmler, String attribute, double[] defaultValue) {
        return getDoubleArrayTagAttribute(xmler, attribute, defaultValue, Symbol.BACKSLASH);
    }

    public static double[] getDoubleArrayTagAttribute(XMLStreamReader xmler, String attribute, double[] defaultValue,
                                                      String separator) {
        if (null != attribute) {
            String val = xmler.getAttributeValue(null, attribute);
            if (null != val) {
                String[] texts = val.split(Pattern.quote(separator));
                double[] vals = new double[texts.length];
                for (int i = 0; i < texts.length; i++) {
                    vals[i] = Double.parseDouble(texts[0]);
                }
                return vals;
            }
        }
        return defaultValue;
    }

    public static Float getFloatTagAttribute(XMLStreamReader xmler, String attribute, Float defaultValue) {
        if (null != attribute) {
            String val = xmler.getAttributeValue(null, attribute);
            try {
                if (null != val) {
                    return Float.valueOf(val);
                }
            } catch (NumberFormatException e) {
                Logger.error("Cannot parse float {} of {}", val, attribute);
            }
        }
        return defaultValue;
    }

    public static float[] getFloatArrayTagAttribute(XMLStreamReader xmler, String attribute, float[] defaultValue) {
        return getFloatArrayTagAttribute(xmler, attribute, defaultValue, Symbol.BACKSLASH);
    }

    public static float[] getFloatArrayTagAttribute(XMLStreamReader xmler, String attribute, float[] defaultValue,
                                                    String separator) {
        if (null != attribute) {
            String val = xmler.getAttributeValue(null, attribute);
            if (null != val) {
                String[] texts = val.split(Pattern.quote(separator));
                float[] vals = new float[texts.length];
                for (int i = 0; i < texts.length; i++) {
                    vals[i] = Float.parseFloat(texts[0]);
                }
                return vals;
            }
        }
        return defaultValue;
    }

    public static TemporalAccessor getDateFromElement(XMLStreamReader xmler, String attribute, TagCamel.TagType type,
                                                      TemporalAccessor defaultValue) {
        if (null != attribute) {
            String val = xmler.getAttributeValue(null, attribute);
            if (null != val) {
                try {
                    if (TagCamel.TagType.TIME.equals(type)) {
                        return LocalTime.parse(val);
                    } else if (TagCamel.TagType.DATETIME.equals(type)) {
                        return LocalDateTime.parse(val);
                    } else {
                        return LocalDate.parse(val);
                    }
                } catch (Exception e) {
                    Logger.error("Parse date", e);
                }
            }
        }
        return defaultValue;
    }

    public static TemporalAccessor[] getDatesFromElement(XMLStreamReader xmler, String attribute, TagCamel.TagType type,
                                                         TemporalAccessor[] defaultValue) {
        return getDatesFromElement(xmler, attribute, type, defaultValue, Symbol.BACKSLASH);
    }

    public static TemporalAccessor[] getDatesFromElement(XMLStreamReader xmler, String attribute, TagCamel.TagType type,
                                                         TemporalAccessor[] defaultValue, String separator) {
        if (null != attribute) {
            String val = xmler.getAttributeValue(null, attribute);
            if (null != val) {
                String[] texts = val.split(Pattern.quote(separator));
                TemporalAccessor[] vals = new TemporalAccessor[texts.length];
                for (int i = 0; i < texts.length; i++) {
                    try {
                        if (TagCamel.TagType.TIME.equals(type)) {
                            vals[i] = LocalTime.parse(texts[i]);
                        } else if (TagCamel.TagType.DATETIME.equals(type)) {
                            vals[i] = LocalDateTime.parse(texts[i]);
                        } else {
                            vals[i] = LocalDate.parse(texts[i]);
                        }
                    } catch (Exception e) {
                        Logger.error("Parse date", e);
                    }
                }
                return vals;
            }
        }
        return defaultValue;
    }


    public static Object getTagValue(TagCamel tag, Readable... tagable) {
        for (Readable t : tagable) {
            if (null != t) {
                Object val = t.getTagValue(tag);
                if (null != val) {
                    return val;
                }
            }
        }
        return null;
    }

}
