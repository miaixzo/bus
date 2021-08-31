package org.aoju.bus.core.image.element;

import java.awt.*;

public class RectangleElement extends AbstractElement<RectangleElement> {

    private Integer width;                  //绘制宽度
    private Integer height;                 //绘制高度
    private Integer roundCorner = 0;        //圆角大小
    private Color color = new Color(255, 255, 255);   //颜色，默认白色

    /**
     * @param x      x坐标
     * @param y      y坐标
     * @param width  宽度
     * @param height 高度
     */
    public RectangleElement(int x, int y, int width, int height) {
        this.width = width;
        this.height = height;
        super.setX(x);
        super.setY(y);
    }

    public Integer getWidth() {
        return width;
    }

    public RectangleElement setWidth(Integer width) {
        this.width = width;
        return this;
    }

    public Integer getHeight() {
        return height;
    }

    public RectangleElement setHeight(Integer height) {
        this.height = height;
        return this;
    }

    public Integer getRoundCorner() {
        return roundCorner;
    }

    public RectangleElement setRoundCorner(Integer roundCorner) {
        this.roundCorner = roundCorner;
        return this;
    }

    public Color getColor() {
        return color;
    }

    public RectangleElement setColor(Color color) {
        this.color = color;
        return this;
    }

}