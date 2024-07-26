/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.nimble.opencv.tile;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

/**
 * Not an API. This class is under development and can be changed or removed at any moment.
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class TiledProcessor {

    static void copyTileFromSource(Mat sourceImage, Mat tileInput, Rect tile, int mBorderType, int borderX,
            int borderY) {
        int tx = 0;
        int ty = 0;

        int bx = 0;
        int by = 0;

        // Take care of border cases
        if (tile.x < 0) {
            tx = -tile.x;
            tile.x = 0;
        }

        if (tile.y < 0) {
            ty = -tile.y;
            tile.y = 0;
        }

        if (borderX >= sourceImage.cols()) {
            bx = borderX - sourceImage.cols() + 1;
            tile.width -= bx;
        }

        if (borderY >= sourceImage.rows()) {
            by = borderY - sourceImage.rows() + 1;
            tile.height -= by;
        }

        // If any of the tile sides exceed source image boundary we must use copyMakeBorder to make
        // proper paddings
        // for this side
        if (tx > 0 || ty > 0 || bx > 0 || by > 0) {
            Rect paddedTile = new Rect(tile.tl(), tile.br());
            assert (paddedTile.x >= 0);
            assert (paddedTile.y >= 0);
            assert (paddedTile.br().x < sourceImage.cols());
            assert (paddedTile.br().y < sourceImage.rows());

            Core.copyMakeBorder(sourceImage.submat(paddedTile), tileInput, ty, by, tx, bx, mBorderType);
        } else {
            // Entire tile (with paddings lies inside image and it's safe to just take a region:
            sourceImage.submat(tile).copyTo(tileInput);
        }
    }

    public Mat blur(Mat input, int numberOfTimes) {
        Mat sourceImage;
        Mat destImage = input.clone();
        for (int i = 0; i < numberOfTimes; i++) {
            sourceImage = destImage.clone();
            // Imgproc.blur(sourceImage, destImage, new Size(3.0, 3.0));
            process(sourceImage, destImage, 256);
        }
        return destImage;
    }

    public void process(Mat sourceImage, Mat resultImage, int tileSize) {

        if (sourceImage.rows() != resultImage.rows() || sourceImage.cols() != resultImage.cols()) {
            throw new IllegalStateException("");
        }

        final int rowTiles = (sourceImage.rows() / tileSize) + (sourceImage.rows() % tileSize != 0 ? 1 : 0);
        final int colTiles = (sourceImage.cols() / tileSize) + (sourceImage.cols() % tileSize != 0 ? 1 : 0);

        Mat tileInput = new Mat(tileSize, tileSize, sourceImage.type());
        Mat tileOutput = new Mat(tileSize, tileSize, sourceImage.type());

        int boderType = Core.BORDER_DEFAULT;
        int mPadding = 3;

        for (int rowTile = 0; rowTile < rowTiles; rowTile++) {
            for (int colTile = 0; colTile < colTiles; colTile++) {
                Rect srcTile = new Rect(colTile * tileSize - mPadding, rowTile * tileSize - mPadding,
                        tileSize + 2 * mPadding, tileSize + 2 * mPadding);
                Rect dstTile = new Rect(colTile * tileSize, rowTile * tileSize, tileSize, tileSize);
                copyTileFromSource(sourceImage, tileInput, srcTile, boderType, 0, 0);
                processTileImpl(tileInput, tileOutput);
                copyTileToResultImage(tileOutput, resultImage, new Rect(mPadding, mPadding, tileSize, tileSize),
                        dstTile);
            }
        }
    }

    private void copyTileToResultImage(Mat tileOutput, Mat resultImage, Rect srcTile, Rect dstTile) {
        Point br = dstTile.br();

        if (br.x >= resultImage.cols()) {
            dstTile.width -= br.x - resultImage.cols();
            srcTile.width -= br.x - resultImage.cols();
        }

        if (br.y >= resultImage.rows()) {
            dstTile.height -= br.y - resultImage.rows();
            srcTile.height -= br.y - resultImage.rows();
        }

        Mat tileView = tileOutput.submat(srcTile);
        Mat dstView = resultImage.submat(dstTile);

        assert (tileView.rows() == dstView.rows());
        assert (tileView.cols() == dstView.cols());

        tileView.copyTo(dstView);
    }

    private void processTileImpl(Mat tileInput, Mat tileOutput) {
        Imgproc.blur(tileInput, tileOutput, new Size(7.0, 7.0));
    }

}
