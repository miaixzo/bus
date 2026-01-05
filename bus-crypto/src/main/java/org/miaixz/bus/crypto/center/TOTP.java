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
package org.miaixz.bus.crypto.center;

import java.time.Duration;
import java.time.Instant;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Time-based one-time passwords (TOTP) generator based on a timestamp algorithm.
 *
 * <p>
 * Specification: <a href="https://tools.ietf.org/html/rfc6238">RFC&nbsp;6238</a>
 * </p>
 *
 *
 * <p>
 * Time synchronization is required. It's based on a comparison between the client's dynamic password and the dynamic
 * password verification server's time. A new password is typically generated every 30 seconds. It requires that the
 * client and server clocks be precisely synchronized for the time-based dynamic passwords to match.
 * </p>
 * <p>
 * Reference: https://github.com/jchambers/java-otp
 * </p>
 *
 * <p>
 * TOTP is based on HOTP with a timestamp-based counter. By defining the start of an epoch (T0) and counting in time
 * intervals (TI), the current timestamp is converted into an integer time counter (TC). For example: TC =
 * floor((unixtime(now) - unixtime(T0)) / TI), TOTP = HOTP(SecretKey, TC), TOTP-Value = TOTP mod 10^d, where d is the
 * desired number of digits for the one-time password. Services like Google Authenticator's two-factor authentication
 * use this method.
 * </p>
 *
 * <p>
 * Authentication process: A QR code is generated with a Google otpauth link. A shared secret key is generated. This key
 * is returned to the app, along with the user account and service name. The key is Base32 encoded. The app stores this
 * key to generate 6-digit verification codes in the future. The server also stores this key and the username. You can
 * store it as a key-value pair where the username is the key and the secret is the value. The app generates a new
 * 6-digit code every 30 seconds, which the user uses to log in to the website. The server uses the stored secret key
 * and the TOTP algorithm to generate a 6-digit code and compares it with the code sent from the client. If the two
 * codes are equal, authorization is successful; otherwise, it fails. (Note: The server can retrieve the secret key
 * based on the currently logged-in username and then use the TOTP algorithm to generate the verification code).
 * </p>
 *
 * <p>
 * Login Process Summary: The user first logs in with a username and password. The session stores information like the
 * username. A QR code and secret key are generated. The key is stored in the server's key-value store, using the
 * username from the session as the key and the generated secret as the value. The user scans the QR code with their
 * app, which starts generating new 6-digit codes. After logging in with username and password, the user is directed to
 * a verification page to enter the current 6-digit code. The code is submitted to the server. The server retrieves the
 * corresponding secret key based on the username and then uses the TOTP algorithm to generate its own 6-digit code. If
 * the server's code and the client's code match, the login is successful!
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TOTP extends HOTP {

    /**
     * The time step for TOTP generation.
     */
    private final Duration timeStep;

    /**
     * Constructor, uses the default HMAC algorithm (HmacSHA1).
     *
     * @param key The shared secret, RFC 4226 recommends at least 128 bits.
     */
    public TOTP(final byte[] key) {
        this(Duration.ofSeconds(30), key);
    }

    /**
     * Constructor, uses the default HMAC algorithm (HmacSHA1).
     *
     * @param timeStep The time step used to generate the moving factor, default is 30 seconds.
     * @param key      The shared secret, RFC 4226 recommends at least 128 bits.
     */
    public TOTP(final Duration timeStep, final byte[] key) {
        this(timeStep, DEFAULT_PASSWORD_LENGTH, key);
    }

    /**
     * Constructor, uses the default HMAC algorithm (HmacSHA1).
     *
     * @param timeStep       The time step used to generate the moving factor.
     * @param passwordLength The password length, can be 6, 7, or 8.
     * @param key            The shared secret, RFC 4226 recommends at least 128 bits.
     */
    public TOTP(final Duration timeStep, final int passwordLength, final byte[] key) {
        this(timeStep, passwordLength, Algorithm.HMACSHA1, key);
    }

    /**
     * Constructor.
     *
     * @param timeStep       The time step used to generate the moving factor.
     * @param passwordLength The password length, can be 6, 7, or 8.
     * @param algorithm      The HMAC algorithm enum.
     * @param key            The shared secret, RFC 4226 recommends at least 128 bits.
     */
    public TOTP(final Duration timeStep, final int passwordLength, final Algorithm algorithm, final byte[] key) {
        super(passwordLength, algorithm, key);
        this.timeStep = timeStep;
    }

    /**
     * Generates a Google Authenticator compatible key URI (for QR codes). Time-based, not suitable for counters.
     *
     * @param account  The account name.
     * @param numBytes The number of seed bytes to generate.
     * @return The shared secret key as a URI string.
     */
    public static String generateGoogleSecretKey(final String account, final int numBytes) {
        return StringKit.format("otpauth://totp/{}?secret={}", account, generateSecretKey(numBytes));
    }

    /**
     * Generates a one-time password using the given timestamp.
     *
     * @param timestamp The timestamp used to generate the password.
     * @return The one-time password as an int.
     */
    public int generate(final Instant timestamp) {
        return this.generate(timestamp.toEpochMilli() / this.timeStep.toMillis());
    }

    /**
     * Used to validate if a code is correct.
     *
     * @param timestamp  The validation timestamp.
     * @param offsetSize The time step tolerance window (number of steps before and after).
     * @param code       The code to validate.
     * @return Whether the validation passes.
     */
    public boolean validate(final Instant timestamp, final int offsetSize, final int code) {
        if (offsetSize == 0) {
            return generate(timestamp) == code;
        }
        for (int i = -offsetSize; i <= offsetSize; i++) {
            if (generate(timestamp.plus(getTimeStep().multipliedBy(i))) == code) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the time step.
     *
     * @return The time step.
     */
    public Duration getTimeStep() {
        return this.timeStep;
    }

}
