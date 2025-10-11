/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.crypto;

import java.util.Arrays;

import org.miaixz.bus.core.lang.Algorithm;

/**
 * Represents a cryptographic cipher, providing a unified API for various implementations like JCE and Bouncy Castle.
 * <p>
 * This interface supports block-wise encryption and decryption through the combined use of {@code process} and
 * {@code doFinal} methods. For example, if you need to encrypt a 23-byte message with a block size of 8 bytes, you
 * would make three calls:
 * </p>
 * <ul>
 * <li>Two calls to {@code process} for the first two 8-byte blocks. The partial results can be retrieved from the
 * return value of these calls.</li>
 * <li>One call to {@code doFinal} for the final 7 bytes (which will typically be padded to the block size of 8). This
 * call completes the encryption operation.</li>
 * </ul>
 * <p>
 * The {@code processFinal} method is provided as a convenience for processing data in a single operation, suitable for
 * data smaller than a block or when processing the entire data at once.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Cipher {

    /**
     * Gets the name of the algorithm.
     *
     * @return The algorithm name.
     */
    String getAlgorithm();

    /**
     * Gets the block size of the cipher.
     *
     * @return The block size in bytes, or -1 if the cipher is not a block cipher, or 0 for stream ciphers.
     */
    int getBlockSize();

    /**
     * Initializes the cipher with a specific mode and parameters.
     *
     * @param mode       The operation mode, such as encryption or decryption.
     * @param parameters The parameters required by the cipher, including Key, IV, etc.
     */
    void init(Algorithm.Type mode, Parameters parameters);

    /**
     * Returns the required size of the output buffer to store the result of the next {@code process} or {@code doFinal}
     * operation.
     * <p>
     * The actual output length may be less than the value returned by this method. This is typically the output size
     * corresponding to the block size.
     * </p>
     *
     * @param len The input length in bytes.
     * @return The required output buffer size in bytes, or -1 if not a block cipher.
     */
    int getOutputSize(int len);

    /**
     * Processes a single block of data. This method is intended for multi-part encryption or decryption operations.
     * After processing all full blocks, a call to {@link #doFinal(byte[], int)} is required to handle any remaining
     * data and padding.
     *
     * @param in     The input buffer containing the data.
     * @param inOff  The offset in the input buffer where the data begins.
     * @param len    The length of the data to process.
     * @param out    The output buffer for the processed data.
     * @param outOff The offset in the output buffer where the result should be stored.
     * @return The number of bytes processed and stored in the output buffer.
     */
    int process(byte[] in, int inOff, int len, byte[] out, int outOff);

    /**
     * Finishes a multi-part encryption or decryption operation. This method processes the final block of data,
     * including any necessary padding. It should be called after all calls to
     * {@link #process(byte[], int, int, byte[], int)} are complete.
     * <p>
     * For example, if an encryption algorithm requires 128-bit (16-byte) blocks and the last data segment is only 15
     * bytes, this method will apply the appropriate padding (e.g., filling the last byte with a specific value) before
     * finalizing the operation.
     * </p>
     *
     * @param out    The output buffer containing the results of previous {@code process} calls.
     * @param outOff The offset in the output buffer where the final result should be stored.
     * @return The number of bytes stored in the output buffer as a result of this final operation.
     */
    int doFinal(byte[] out, int outOff);

    /**
     * Processes the input data in a single step and returns the final result. This is a convenience method for
     * single-part operations.
     *
     * @param in The input data to be processed.
     * @return The resulting encrypted or decrypted data.
     */
    default byte[] processFinal(final byte[] in) {
        return processFinal(in, 0, in.length);
    }

    /**
     * Processes the input data in a single step and returns the final result. This method handles the entire operation,
     * including processing and finalization.
     *
     * @param in       The input buffer containing the data.
     * @param inOffset The offset in the input buffer where the data begins.
     * @param inputLen The length of the data to process.
     * @return The resulting encrypted or decrypted data.
     * @see #process(byte[], int, int, byte[], int)
     * @see #doFinal(byte[], int)
     */
    default byte[] processFinal(final byte[] in, final int inOffset, final int inputLen) {
        final byte[] buf = new byte[getOutputSize(in.length)];
        int len = process(in, inOffset, inputLen, buf, 0);
        // Process remaining data, such as padding
        len += doFinal(buf, len);
        return (len == buf.length) ? buf : Arrays.copyOfRange(buf, 0, len);
    }

    /**
     * A marker interface for cipher parameters, such as keys, initialization vectors (IVs), and other cryptographic
     * settings.
     */
    interface Parameters {

    }

}
