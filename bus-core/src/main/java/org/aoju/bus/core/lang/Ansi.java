/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * *
 * 使用方法:
 * String msg = Ansi.Red.and(Ansi.BgYellow).format("Hello %s", name)
 * String msg = Ansi.Blink.colorize("BOOM!")
 * 或者，可以直接使用常数:
 * String msg = new Ansi(Ansi.ITALIC, Ansi.GREEN).format("Green money")
 * Or, even:
 * String msg = Ansi.BLUE + "scientific"
 * 注意:同时出现多种组合FG颜色或BG颜色，只有最后一个会显示
 *
 * @author Kimi Liu
 * @version 5.1.0
 * @since JDK 1.8+
 */
public class Ansi {

    public static final String SANE = "\u001B[0m";

    public static final String HIGH_INTENSITY = "\u001B[1m";
    public static final String LOW_INTENSITY = "\u001B[2m";

    public static final String ITALIC = "\u001B[3m";
    public static final String UNDERLINE = "\u001B[4m";
    public static final String BLINK = "\u001B[5m";
    public static final String RAPID_BLINK = "\u001B[6m";
    public static final String REVERSE_VIDEO = "\u001B[7m";
    public static final String INVISIBLE_TEXT = "\u001B[8m";

    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    public static final String BACKGROUND_BLACK = "\u001B[40m";
    public static final String BACKGROUND_RED = "\u001B[41m";
    public static final String BACKGROUND_GREEN = "\u001B[42m";
    public static final String BACKGROUND_YELLOW = "\u001B[43m";
    public static final String BACKGROUND_BLUE = "\u001B[44m";
    public static final String BACKGROUND_MAGENTA = "\u001B[45m";
    public static final String BACKGROUND_CYAN = "\u001B[46m";
    public static final String BACKGROUND_WHITE = "\u001B[47m";

    public static final Ansi HighIntensity = new Ansi(HIGH_INTENSITY);
    public static final Ansi Bold = HighIntensity;
    public static final Ansi LowIntensity = new Ansi(LOW_INTENSITY);
    public static final Ansi Normal = LowIntensity;

    public static final Ansi Italic = new Ansi(ITALIC);
    public static final Ansi Underline = new Ansi(UNDERLINE);
    public static final Ansi Blink = new Ansi(BLINK);
    public static final Ansi RapidBlink = new Ansi(RAPID_BLINK);

    public static final Ansi Black = new Ansi(BLACK);
    public static final Ansi Red = new Ansi(RED);
    public static final Ansi Green = new Ansi(GREEN);
    public static final Ansi Yellow = new Ansi(YELLOW);
    public static final Ansi Blue = new Ansi(BLUE);
    public static final Ansi Magenta = new Ansi(MAGENTA);
    public static final Ansi Cyan = new Ansi(CYAN);
    public static final Ansi White = new Ansi(WHITE);

    public static final Ansi BgBlack = new Ansi(BACKGROUND_BLACK);
    public static final Ansi BgRed = new Ansi(BACKGROUND_RED);
    public static final Ansi BgGreen = new Ansi(BACKGROUND_GREEN);
    public static final Ansi BgYellow = new Ansi(BACKGROUND_YELLOW);
    public static final Ansi BgBlue = new Ansi(BACKGROUND_BLUE);
    public static final Ansi BgMagenta = new Ansi(BACKGROUND_MAGENTA);
    public static final Ansi BgCyan = new Ansi(BACKGROUND_CYAN);
    public static final Ansi BgWhite = new Ansi(BACKGROUND_WHITE);
    public static boolean isWindows;

    static {
        isWindows = System.getProperties().getProperty("os.name").toLowerCase().contains("win");
    }

    final private String[] codes;
    final private String codes_str;

    public Ansi(String... codes) {
        this.codes = codes;
        String _codes_str = "";
        for (String code : codes) {
            _codes_str += code;
        }
        codes_str = _codes_str;
    }

    public Ansi and(Ansi other) {
        List<String> both = new ArrayList<>();
        Collections.addAll(both, codes);
        Collections.addAll(both, other.codes);
        return new Ansi(both.toArray(new String[]{}));
    }

    public String colorize(String original) {
        return codes_str + original + SANE;
    }

    public String format(String template, Object... args) {
        if (isWindows) {
            if (null == args || args.length == 0) {
                return template;
            }
            return String.format(template, args);
        }
        String text = (null == args || args.length == 0) ? template : String.format(template, args);
        return colorize(text);
    }

}
