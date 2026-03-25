/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.office.excel;

import org.apache.poi.ss.usermodel.ClientAnchor;

/**
 * A simple implementation of {@link ClientAnchor}. This object represents the position and size of a graphic or drawing
 * in Excel. The parameters indicate:
 * <ul>
 * <li>{@code dx1} and {@code dy1} represent the offset within the top-left cell. {@code col1} and {@code row1}
 * represent the top-left cell.</li>
 * <li>{@code dx2} and {@code dy2} represent the offset within the bottom-right cell. {@code col2} and {@code row2}
 * represent the bottom-right cell.</li>
 * </ul>
 * For an illustration, see: <a href="https://www.cnblogs.com/sunyl/p/7527703.html">ClientAnchor Illustration</a>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SimpleAnchor implements ClientAnchor {

    /**
     * The x coordinate in EMU within the top-left cell.
     */
    private int dx1;
    /**
     * The y coordinate in EMU within the top-left cell.
     */
    private int dy1;
    /**
     * The y coordinate in EMU within the bottom-right cell.
     */
    private int dy2;
    /**
     * The x coordinate in EMU within the bottom-right cell.
     */
    private int dx2;

    /**
     * The column (0 based) of the first cell.
     */
    private int col1;
    /**
     * The row (0 based) of the first cell.
     */
    private int row1;
    /**
     * The column (0 based) of the second cell.
     */
    private int col2;
    /**
     * The row (0 based) of the second cell.
     */
    private int row2;

    /**
     * The anchor type.
     */
    private AnchorType anchorType = AnchorType.MOVE_AND_RESIZE;

    /**
     * Constructs a {@code SimpleAnchor} object by defining the top-left and bottom-right cells. Default offsets are 0,
     * and the default anchor type is {@link AnchorType#MOVE_AND_RESIZE}.
     *
     * @param col1 The starting column index, 0-based.
     * @param row1 The starting row index, 0-based.
     * @param col2 The ending column index, 0-based.
     * @param row2 The ending row index, 0-based.
     */
    public SimpleAnchor(final int col1, final int row1, final int col2, final int row2) {
        this(0, 0, 0, 0, col1, row1, col2, row2);
    }

    /**
     * Constructs a {@code SimpleAnchor} object by defining the top-left and bottom-right cells, along with offsets
     * within the cells. The default anchor type is {@link AnchorType#MOVE_AND_RESIZE}.
     *
     * @param dx1  The x-offset in pixels within the starting cell.
     * @param dy1  The y-offset in pixels within the starting cell.
     * @param dx2  The x-offset in pixels within the ending cell.
     * @param dy2  The y-offset in pixels within the ending cell.
     * @param col1 The starting column index, 0-based.
     * @param row1 The starting row index, 0-based.
     * @param col2 The ending column index, 0-based.
     * @param row2 The ending row index, 0-based.
     */
    public SimpleAnchor(final int dx1, final int dy1, final int dx2, final int dy2, final int col1, final int row1,
            final int col2, final int row2) {
        this.dx1 = dx1;
        this.dy1 = dy1;
        this.dx2 = dx2;
        this.dy2 = dy2;
        this.col1 = col1;
        this.row1 = row1;
        this.col2 = col2;
        this.row2 = row2;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the x coordinate in EMU within the top-left cell
     */
    @Override
    public int getDx1() {
        return this.dx1;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param dx1 the x coordinate in EMU within the top-left cell
     */
    @Override
    public void setDx1(final int dx1) {
        this.dx1 = dx1;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the y coordinate in EMU within the top-left cell
     */
    @Override
    public int getDy1() {
        return this.dy1;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param dy1 the y coordinate in EMU within the top-left cell
     */
    @Override
    public void setDy1(final int dy1) {
        this.dy1 = dy1;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the x coordinate in EMU within the bottom-right cell
     */
    @Override
    public int getDx2() {
        return this.dx2;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param dx2 the x coordinate in EMU within the bottom-right cell
     */
    @Override
    public void setDx2(final int dx2) {
        this.dx2 = dx2;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the y coordinate in EMU within the bottom-right cell
     */
    @Override
    public int getDy2() {
        return this.dy2;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param dy2 the y coordinate in EMU within the bottom-right cell
     */
    @Override
    public void setDy2(final int dy2) {
        this.dy2 = dy2;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the column (0 based) of the first cell
     */
    @Override
    public short getCol1() {
        return (short) this.col1;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param col1 the column (0 based) of the first cell
     */
    @Override
    public void setCol1(final int col1) {
        this.col1 = col1;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the row (0 based) of the first cell
     */
    @Override
    public int getRow1() {
        return this.row1;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param row1 the row (0 based) of the first cell
     */
    @Override
    public void setRow1(final int row1) {
        this.row1 = row1;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the column (0 based) of the second cell
     */
    @Override
    public short getCol2() {
        return (short) this.col2;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param col2 the column (0 based) of the second cell
     */
    @Override
    public void setCol2(final int col2) {
        this.col2 = col2;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the row (0 based) of the second cell.
     */
    @Override
    public int getRow2() {
        return this.row2;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param row2 the row (0 based) of the second cell
     */
    @Override
    public void setRow2(final int row2) {
        this.row2 = row2;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @return the anchor type
     */
    @Override
    public AnchorType getAnchorType() {
        return this.anchorType;
    }

    /**
     * Implements the behavior defined by the supertype.
     *
     * @param anchorType the anchor type
     */
    @Override
    public void setAnchorType(final AnchorType anchorType) {
        this.anchorType = anchorType;
    }

    /**
     * Copies the values from this object to a target {@link ClientAnchor} object.
     *
     * @param clientAnchor The target {@link ClientAnchor} object to copy values to.
     * @return The modified target {@link ClientAnchor} object.
     */
    public ClientAnchor copyTo(final ClientAnchor clientAnchor) {
        clientAnchor.setDx1(this.dx1);
        clientAnchor.setDy1(this.dy1);
        clientAnchor.setDx2(this.dx2);
        clientAnchor.setDy2(this.dy2);
        clientAnchor.setCol1(this.col1);
        clientAnchor.setRow1(this.row1);
        clientAnchor.setCol2(this.col2);
        clientAnchor.setRow2(this.row2);
        clientAnchor.setAnchorType(this.anchorType);
        return clientAnchor;
    }

}
