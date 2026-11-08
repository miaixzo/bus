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
package org.miaixz.bus.opencv.core;

/**
 * A correspondence between query and training descriptors, including its matching distance.
 */
public class DMatch {

    /**
     * Index of the descriptor in the query set.
     */
    public int queryIdx;
    /**
     * Index of the descriptor in the training set.
     */
    public int trainIdx;
    /**
     * Index of the training image containing the matched descriptor.
     */
    public int imgIdx;

    /**
     * Distance between the matched descriptors.
     */
    public float distance;

    /**
     * Creates an empty DMatch value.
     */
    public DMatch() {
        this(-1, -1, Float.MAX_VALUE);
    }

    /**
     * Creates a configured DMatch value using the supplied values.
     *
     * @param _queryIdx index of the query descriptor
     * @param _trainIdx index of the training descriptor
     * @param _distance distance between the matched descriptors
     */
    public DMatch(int _queryIdx, int _trainIdx, float _distance) {
        queryIdx = _queryIdx;
        trainIdx = _trainIdx;
        imgIdx = -1;
        distance = _distance;
    }

    /**
     * Creates a configured DMatch value using the supplied values.
     *
     * @param _queryIdx index of the query descriptor
     * @param _trainIdx index of the training descriptor
     * @param _imgIdx   index of the training image
     * @param _distance distance between the matched descriptors
     */
    public DMatch(int _queryIdx, int _trainIdx, int _imgIdx, float _distance) {
        queryIdx = _queryIdx;
        trainIdx = _trainIdx;
        imgIdx = _imgIdx;
        distance = _distance;
    }

    /**
     * Compares descriptor matches by their distance.
     *
     * @param it descriptor match whose distance is compared with this match
     * @return true if this match has a smaller distance than the other match
     * @throws NullPointerException if it is null
     */
    public boolean lessThan(DMatch it) {
        return distance < it.distance;
    }

    /**
     * Returns a textual representation of this object.
     *
     * @return the DMatch field values formatted as text
     */
    @Override
    public String toString() {
        return "DMatch [queryIdx=" + queryIdx + ", trainIdx=" + trainIdx + ", imgIdx=" + imgIdx + ", distance="
                + distance + "]";
    }

}
