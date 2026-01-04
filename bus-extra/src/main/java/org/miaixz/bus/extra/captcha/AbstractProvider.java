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
package org.miaixz.bus.extra.captcha;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.UrlKit;
import org.miaixz.bus.extra.captcha.strategy.CodeStrategy;
import org.miaixz.bus.extra.captcha.strategy.RandomStrategy;
import org.miaixz.bus.extra.image.ImageKit;

/**
 * Abstract CAPTCHA. This abstract class implements CAPTCHA string generation, verification, and image writing.
 * Implementations generate the image object by implementing the {@link #createImage(String)} method.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractProvider implements CaptchaProvider {

    @Serial
    private static final long serialVersionUID = 2852291319897L;

    /**
     * The width of the image.
     */
    protected int width;
    /**
     * The height of the image.
     */
    protected int height;
    /**
     * The number of interfering elements in the CAPTCHA.
     */
    protected int interfereCount;
    /**
     * The font.
     */
    protected Font font;
    /**
     * The CAPTCHA code.
     */
    protected String code;
    /**
     * The CAPTCHA image bytes.
     */
    protected byte[] imageBytes;
    /**
     * The CAPTCHA code generator.
     */
    protected CodeStrategy generator;
    /**
     * The background color.
     */
    protected Color background = Color.WHITE;
    /**
     * The text transparency.
     */
    protected AlphaComposite textAlpha;

    /**
     * Constructor, uses a random CAPTCHA generator.
     *
     * @param width          Image width.
     * @param height         Image height.
     * @param codeCount      Number of characters.
     * @param interfereCount Number of interfering elements.
     */
    public AbstractProvider(final int width, final int height, final int codeCount, final int interfereCount) {
        this(width, height, new RandomStrategy(codeCount), interfereCount);
    }

    /**
     * Constructor.
     *
     * @param width          Image width.
     * @param height         Image height.
     * @param generator      CAPTCHA code generator.
     * @param interfereCount Number of interfering elements.
     */
    public AbstractProvider(final int width, final int height, final CodeStrategy generator, final int interfereCount) {
        this(width, height, generator, interfereCount, Normal.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructor.
     *
     * @param width          Image width.
     * @param height         Image height.
     * @param generator      CAPTCHA code generator.
     * @param interfereCount Number of interfering elements.
     * @param sizeBaseHeight Font size as a multiplier of the height.
     */
    public AbstractProvider(final int width, final int height, final CodeStrategy generator, final int interfereCount,
            final float sizeBaseHeight) {
        this.width = width;
        this.height = height;
        this.generator = generator;
        this.interfereCount = interfereCount;
        // Set font height to captcha height - 2 to leave a margin
        this.font = new Font(Font.SANS_SERIF, Font.PLAIN, (int) (this.height * sizeBaseHeight));
    }

    /**
     * Creates a new CAPTCHA image and code.
     * <p>
     * This method generates a random CAPTCHA code, creates the corresponding image, and stores the image data for later
     * use.
     * </p>
     */
    @Override
    public void create() {
        generateCode();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        Image image = null;
        try {
            image = createImage(this.code);
            ImageKit.writePng(image, out);
        } finally {
            ImageKit.flush(image);
        }

        this.imageBytes = out.toByteArray();
    }

    /**
     * Generates the CAPTCHA code string.
     */
    protected void generateCode() {
        this.code = generator.generate();
    }

    /**
     * Creates the CAPTCHA image based on the generated code.
     *
     * @param code The CAPTCHA code.
     * @return The CAPTCHA image.
     */
    protected abstract Image createImage(String code);

    /**
     * Gets the CAPTCHA code.
     * <p>
     * If the CAPTCHA has not been created yet, this method will automatically create it first.
     * </p>
     *
     * @return the CAPTCHA code string
     */
    @Override
    public String get() {
        if (null == this.code) {
            create();
        }
        return this.code;
    }

    /**
     * Verifies the user's input against the generated CAPTCHA code.
     *
     * @param userInputCode the code entered by the user
     * @return {@code true} if the user input matches the CAPTCHA code, {@code false} otherwise
     */
    @Override
    public boolean verify(final String userInputCode) {
        return this.generator.verify(get(), userInputCode);
    }

    /**
     * Writes the CAPTCHA to a file.
     *
     * @param path The file path.
     * @throws InternalException if an I/O error occurs.
     */
    public void write(final String path) throws InternalException {
        this.write(FileKit.touch(path));
    }

    /**
     * Writes the CAPTCHA to a file.
     *
     * @param file The file.
     * @throws InternalException if an I/O error occurs.
     */
    public void write(final File file) throws InternalException {
        try (final OutputStream out = FileKit.getOutputStream(file)) {
            this.write(out);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Writes the CAPTCHA image data to an output stream.
     *
     * @param out the output stream to write the image data to
     */
    @Override
    public void write(final OutputStream out) {
        IoKit.write(out, false, getImageBytes());
    }

    /**
     * Gets the graphic CAPTCHA image bytes.
     *
     * @return The graphic CAPTCHA image bytes.
     */
    public byte[] getImageBytes() {
        if (null == this.imageBytes) {
            create();
        }
        return this.imageBytes;
    }

    /**
     * Gets the CAPTCHA image. Note: After using the returned {@link BufferedImage}, you need to call
     * {@link BufferedImage#flush()} to release resources.
     *
     * @return The CAPTCHA image.
     */
    public BufferedImage getImage() {
        return ImageKit.read(IoKit.toStream(getImageBytes()));
    }

    /**
     * Gets the Base64 representation of the image.
     *
     * @return The Base64 of the image.
     */
    public String getImageBase64() {
        return Base64.encode(getImageBytes());
    }

    /**
     * Gets the Base64 of the image with the file format.
     *
     * @return The Base64 of the image with the file format.
     */
    public String getImageBase64Data() {
        return UrlKit.getDataUriBase64("image/png", getImageBase64());
    }

    /**
     * Sets a custom font.
     *
     * @param font The font.
     */
    public void setFont(final Font font) {
        this.font = font;
    }

    /**
     * Gets the CAPTCHA code generator.
     *
     * @return The CAPTCHA code generator.
     */
    public CodeStrategy getGenerator() {
        return generator;
    }

    /**
     * Sets the CAPTCHA code generator.
     *
     * @param generator The CAPTCHA code generator.
     */
    public void setGenerator(final CodeStrategy generator) {
        this.generator = generator;
    }

    /**
     * Sets the background color. {@code null} means a transparent background.
     *
     * @param background The background color.
     */
    public void setBackground(final Color background) {
        this.background = background;
    }

    /**
     * Sets the text transparency.
     *
     * @param textAlpha The text transparency, a value from 0 to 1, where 1 is opaque.
     */
    public void setTextAlpha(final float textAlpha) {
        this.textAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textAlpha);
    }

}
