package org.aoju.bus.core.consts;

public class Normal {

    /**
     * 字符串:空
     */
    public static final String EMPTY = "";
    /**
     * 字符串:null
     */
    public static final String NULL = "null";
    /**
     * 字符串: 数字
     */
    public static final String NUMBER = "0123456789";
    /**
     * 字符串: 小字母
     */
    public static final String LETTER = "abcdefghijklmnopqrstuvwxyz";
    /**
     * 字符串: 大小字母
     */
    public static final String LETTER_LOWER_UPPER = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz";
    /**
     * 简体中文形式
     **/
    public static final String[] SIMPLE_DIGITS = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
    /**
     * 繁体中文形式
     **/
    public static final String[] TRADITIONAL_DIGITS = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};

    /**
     * 简体中文单位
     **/
    public static final String[] SIMPLE_UNITS = {"", "十", "百", "千"};
    /**
     * 繁体中文单位
     **/
    public static final String[] TRADITIONAL_UNITS = {"", "拾", "佰", "仟"};

    public static final String[] EN_NUMBER = new String[]{"", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN",
            "EIGHT", "NINE"};
    public static final String[] NUMBER_TEEN = new String[]{"TEN", "ELEVEN", "TWELEVE", "THIRTEEN", "FOURTEEN",
            "FIFTEEN", "SIXTEEN", "SEVENTEEN", "EIGHTEEN", "NINETEEN"};
    public static final String[] NUMBER_TEN = new String[]{"TEN", "TWENTY", "THIRTY", "FORTY", "FIFTY", "SIXTY",
            "SEVENTY", "EIGHTY", "NINETY"};
    public static final String[] NUMBER_MORE = new String[]{"", "THOUSAND", "MILLION", "BILLION"};

    public static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    /**
     * 字符串: 字母和数字
     */
    public static final String LETTER_NUMBER = LETTER + NUMBER;

    public static final String SETTER_PREFIX = "set";

    public static final String GETTER_PREFIX = "get";

}
