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
package org.miaixz.bus.extra.image.gif;

import java.io.IOException;
import java.io.OutputStream;

import org.miaixz.bus.core.lang.Normal;

/**
 * This class handles LZW encoding for GIF images. It is adapted from Jef Poskanzer's Java port by way of J. M. G.
 * Elliott.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
class LZWEncoder {

    /**
     * The maximum number of bits per code.
     */
    static final int BITS = 12;
    /**
     * The hash table size, chosen for 80% occupancy.
     */
    static final int HSIZE = 5003;
    /**
     * The image width.
     */
    private final int imgW;
    /**
     * The image height.
     */
    private final int imgH;
    /**
     * The array of pixels to be encoded.
     */
    private final byte[] pixAry;
    /**
     * The initial code size.
     */
    private final int initCodeSize;
    /**
     * The number of remaining pixels to be processed.
     */
    private int remaining;
    /**
     * The current pixel being processed.
     */
    private int curPixel;
    /**
     * The current number of bits per code.
     */
    int n_bits; // number of bits/code
    /**
     * The user-settable maximum number of bits per code.
     */
    int maxbits = BITS; // user settable max # bits/code
    /**
     * The maximum code for the current number of bits.
     */
    int maxcode; // maximum code, given n_bits
    /**
     * The maximum possible code, which should never be generated.
     */
    int maxmaxcode = 1 << BITS; // should NEVER generate this code
    /**
     * The hash table for LZW encoding.
     */
    int[] htab = new int[HSIZE];
    /**
     * The code table for LZW encoding.
     */
    int[] codetab = new int[HSIZE];
    /**
     * The hash table size, for dynamic sizing.
     */
    int hsize = HSIZE; // for dynamic table sizing
    /**
     * The first unused entry in the code table.
     */
    int free_ent = 0; // first unused entry
    /**
     * Flag to indicate if the table should be cleared.
     */
    boolean clear_flg = false;
    /**
     * The initial number of bits for encoding.
     */
    int g_init_bits;
    /**
     * The clear code used in LZW compression.
     */
    int ClearCode;
    /**
     * The end-of-information code.
     */
    int EOFCode;
    /**
     * The current accumulator for bits.
     */
    int cur_accum = 0;
    /**
     * The current number of bits in the accumulator.
     */
    int cur_bits = 0;
    /**
     * Masks for bit manipulation.
     */
    int masks[] = { 0x0000, 0x0001, 0x0003, 0x0007, 0x000F, 0x001F, 0x003F, 0x007F, 0x00FF, 0x01FF, 0x03FF, 0x07FF,
            0x0FFF, 0x1FFF, 0x3FFF, 0x7FFF, 0xFFFF };
    /**
     * The number of characters in the current packet.
     */
    int a_count;
    /**
     * The accumulator for the packet.
     */
    byte[] accum = new byte[256];

    /**
     * Constructor for LZWEncoder.
     *
     * @param width       The width of the image.
     * @param height      The height of the image.
     * @param pixels      The array of pixels to encode.
     * @param color_depth The color depth of the image.
     */
    LZWEncoder(int width, int height, byte[] pixels, int color_depth) {
        imgW = width;
        imgH = height;
        pixAry = pixels;
        initCodeSize = Math.max(2, color_depth);
    }

    /**
     * Calculates the maximum code for a given number of bits.
     *
     * @param n_bits The number of bits.
     * @return The maximum code.
     */
    final int MAXCODE(int n_bits) {
        return (1 << n_bits) - 1;
    }

    /**
     * Returns the next pixel from the image.
     *
     * @return The next pixel value, or -1 if no pixels are remaining.
     */
    private int nextPixel() {
        if (remaining == 0)
            return Normal.__1;

        --remaining;

        byte pix = pixAry[curPixel++];

        return pix & 0xff;
    }

    /**
     * Adds a character to the end of the current packet, and if it is 254 characters, flushes the packet to disk.
     *
     * @param c    The character to add.
     * @param outs The output stream.
     * @throws IOException if an I/O error occurs.
     */
    void char_out(byte c, OutputStream outs) throws IOException {
        accum[a_count++] = c;
        if (a_count >= 254)
            flush_char(outs);
    }

    /**
     * Clears out the hash table.
     *
     * @param outs The output stream.
     * @throws IOException if an I/O error occurs.
     */
    void cl_block(OutputStream outs) throws IOException {
        cl_hash(hsize);
        free_ent = ClearCode + 2;
        clear_flg = true;

        output(ClearCode, outs);
    }

    /**
     * Resets the code table.
     *
     * @param hsize The size of the hash table.
     */
    void cl_hash(int hsize) {
        for (int i = 0; i < hsize; ++i)
            htab[i] = -1;
    }

    /**
     * Compresses the image data.
     *
     * @param init_bits The initial number of bits.
     * @param outs      The output stream.
     * @throws IOException if an I/O error occurs.
     */
    void compress(int init_bits, OutputStream outs) throws IOException {
        int fcode;
        int i /* = 0 */;
        int c;
        int ent;
        int disp;
        int hsize_reg;
        int hshift;

        // Set up the globals: g_init_bits - initial number of bits
        g_init_bits = init_bits;

        // Set up the necessary values
        clear_flg = false;
        n_bits = g_init_bits;
        maxcode = MAXCODE(n_bits);

        ClearCode = 1 << (init_bits - 1);
        EOFCode = ClearCode + 1;
        free_ent = ClearCode + 2;

        a_count = 0; // clear packet

        ent = nextPixel();

        hshift = 0;
        for (fcode = hsize; fcode < 65536; fcode *= 2)
            ++hshift;
        hshift = 8 - hshift; // set hash code range bound

        hsize_reg = hsize;
        cl_hash(hsize_reg); // clear hash table

        output(ClearCode, outs);

        outer_loop: while ((c = nextPixel()) != Normal.__1) {
            fcode = (c << maxbits) + ent;
            i = (c << hshift) ^ ent; // xor hashing

            if (htab[i] == fcode) {
                ent = codetab[i];
                continue;
            } else if (htab[i] >= 0) // non-empty slot
            {
                disp = hsize_reg - i; // secondary hash (after G. Knott)
                if (i == 0)
                    disp = 1;
                do {
                    if ((i -= disp) < 0)
                        i += hsize_reg;

                    if (htab[i] == fcode) {
                        ent = codetab[i];
                        continue outer_loop;
                    }
                } while (htab[i] >= 0);
            }
            output(ent, outs);
            ent = c;
            if (free_ent < maxmaxcode) {
                codetab[i] = free_ent++; // code -> hashtable
                htab[i] = fcode;
            } else
                cl_block(outs);
        }
        // Put out the final code.
        output(ent, outs);
        output(EOFCode, outs);
    }

    /**
     * Encodes the pixel data and writes it to the output stream.
     *
     * @param os The output stream.
     * @throws IOException if an I/O error occurs.
     */
    void encode(OutputStream os) throws IOException {
        os.write(initCodeSize); // write "initial code size" byte

        remaining = imgW * imgH; // reset navigation variables
        curPixel = 0;

        compress(initCodeSize + 1, os); // compress and write the pixel data

        os.write(0); // write block terminator
    }

    /**
     * Flushes the packet to disk and resets the accumulator.
     *
     * @param outs The output stream.
     * @throws IOException if an I/O error occurs.
     */
    void flush_char(OutputStream outs) throws IOException {
        if (a_count > 0) {
            outs.write(a_count);
            outs.write(accum, 0, a_count);
            a_count = 0;
        }
    }

    /**
     * Outputs the given code to the file.
     *
     * @param code The code to output.
     * @param outs The output stream.
     * @throws IOException if an I/O error occurs.
     */
    void output(int code, OutputStream outs) throws IOException {
        cur_accum &= masks[cur_bits];

        if (cur_bits > 0)
            cur_accum |= (code << cur_bits);
        else
            cur_accum = code;

        cur_bits += n_bits;

        while (cur_bits >= 8) {
            char_out((byte) (cur_accum & 0xff), outs);
            cur_accum >>= 8;
            cur_bits -= 8;
        }

        // If the next entry is going to be too big for the code size,
        // then increase it, if possible.
        if (free_ent > maxcode || clear_flg) {
            if (clear_flg) {
                maxcode = MAXCODE(n_bits = g_init_bits);
                clear_flg = false;
            } else {
                ++n_bits;
                if (n_bits == maxbits)
                    maxcode = maxmaxcode;
                else
                    maxcode = MAXCODE(n_bits);
            }
        }

        if (code == EOFCode) {
            // At EOF, write the rest of the buffer.
            while (cur_bits > 0) {
                char_out((byte) (cur_accum & 0xff), outs);
                cur_accum >>= 8;
                cur_bits -= 8;
            }

            flush_char(outs);
        }
    }

}
