/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.sensitive.metric;

import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.sensitive.Context;
import org.miaixz.bus.sensitive.magic.annotation.Shield;

/**
 * 签约协议号脱敏方式
 * 19031317273364059018
 * 签约协议号脱敏格式为前6位后6位保留明文,中间脱敏
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CardProvider extends AbstractProvider {

    @Override
    public Object build(Object object, Context context) {
        if (ObjectKit.isEmpty(object)) {
            return null;
        }
        final Shield shield = context.getShield();
        String agreementNo = object.toString();
        return StringKit.left(agreementNo, 6).concat(
                StringKit.removePrefix(
                        StringKit.padPre(
                                StringKit.right(agreementNo, 6),
                                StringKit.length(agreementNo), shield.shadow()
                        ),
                        StringKit.fill(3, shield.shadow())
                )
        );
    }

}
