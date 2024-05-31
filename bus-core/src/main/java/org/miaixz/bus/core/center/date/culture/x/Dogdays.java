package org.miaixz.bus.core.center.date.culture.x;

/**
 * 三伏
 * 从夏至后第3个庚日算起，初伏为10天，中伏为10天或20天，末伏为10天。当夏至与立秋之间出现4个庚日时中伏为10天，出现5个庚日则为20天。</p>
 */
public class Dogdays {

    /**
     * 名称：初伏、中伏、末伏
     */
    private String name;

    /**
     * 当前入伏第几天，1-20
     */
    private int index;


    public Dogdays() {
    }

    public Dogdays(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return name;
    }

    public String toFullString() {
        return name + "第" + index + "天";
    }

}
