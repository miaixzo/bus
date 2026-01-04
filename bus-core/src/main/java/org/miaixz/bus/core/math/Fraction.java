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
package org.miaixz.bus.core.math;

import java.io.Serial;
import java.math.BigInteger;
import java.util.Objects;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.MathKit;

/**
 * {@code Fraction} is a {@link Number} implementation that can accurately store fractions.
 *
 * <p>
 * This class is immutable and compatible with most methods that accept a {@link Number}.
 *
 * <p>
 * Note that this class is intended for common use cases and is based on <em>int</em>, making it susceptible to various
 * overflow issues. For an equivalent class based on BigInteger, see the BigFraction class in the Commons Math library.
 * <p>
 * This class is derived from Apache Commons Lang3.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class Fraction extends Number implements Comparable<Fraction> {

    @Serial
    private static final long serialVersionUID = 2852228119600L;

    /**
     * {@link Fraction} representing 0.
     */
    public static final Fraction ZERO = new Fraction(0, 1);
    /**
     * {@link Fraction} representing 1.
     */
    public static final Fraction ONE = new Fraction(1, 1);
    /**
     * The numerator part of the fraction (the 3 in 3/7).
     */
    private final int numerator;
    /**
     * The denominator part of the fraction (the 7 in 3/7).
     */
    private final int denominator;
    /**
     * Cached output hashCode (class is immutable).
     */
    private transient int hashCode;
    /**
     * Cached output toString (class is immutable).
     */
    private transient String toString;
    /**
     * Cached output toProperString (class is immutable).
     */
    private transient String toProperString;

    /**
     * Constructs a {@code Fraction} instance with the two parts of the fraction, e.g., Y/Z.
     *
     * @param numerator   The numerator, e.g., the 3 in "three sevenths".
     * @param denominator The denominator, e.g., the 7 in "three sevenths".
     */
    public Fraction(final int numerator, final int denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    /**
     * Creates a {@code Fraction} instance from a {@code double} value.
     *
     * <p>
     * This method uses the
     * <a href="https://web.archive.org/web/20210516065058/http%3A//archives.math.utk.edu/articles/atuyl/confrac/">
     * continued fraction algorithm</a>, which calculates up to 25 convergents and limits the denominator to 10,000.
     *
     * @param value The double value to convert.
     * @return A new fraction instance that is close to the value.
     * @throws ArithmeticException If {@code |value| > Integer.MAX_VALUE} or {@code value = NaN}.
     * @throws ArithmeticException If the calculated denominator is {@code zero}.
     * @throws ArithmeticException If the algorithm does not converge.
     */
    public static Fraction of(double value) {
        final int sign = value < 0 ? -1 : 1;
        value = Math.abs(value);
        if (value > Integer.MAX_VALUE || Double.isNaN(value)) {
            throw new ArithmeticException("The value must not be greater than Integer.MAX_VALUE or NaN");
        }
        final int wholeNumber = (int) value;
        value -= wholeNumber;

        int numer0 = 0; // the pre-previous
        int denom0 = 1; // the pre-previous
        int numer1 = 1; // the previous
        int denom1 = 0; // the previous
        int numer2; // the current, setup in calculation
        int denom2; // the current, setup in calculation
        int a1 = (int) value;
        int a2;
        double x1 = 1;
        double x2;
        double y1 = value - a1;
        double y2;
        double delta1, delta2 = Double.MAX_VALUE;
        double fraction;
        int i = 1;
        do {
            delta1 = delta2;
            a2 = (int) (x1 / y1);
            x2 = y1;
            y2 = x1 - a2 * y1;
            numer2 = a1 * numer1 + numer0;
            denom2 = a1 * denom1 + denom0;
            fraction = (double) numer2 / (double) denom2;
            delta2 = Math.abs(value - fraction);
            a1 = a2;
            x1 = x2;
            y1 = y2;
            numer0 = numer1;
            denom0 = denom1;
            numer1 = numer2;
            denom1 = denom2;
            i++;
        } while (delta1 > delta2 && denom2 <= 10000 && denom2 > 0 && i < 25);
        if (i == 25) {
            throw new ArithmeticException("Unable to convert double to fraction");
        }
        return ofReduced((numer0 + wholeNumber * denom0) * sign, denom0);
    }

    /**
     * Creates a {@code Fraction} instance with the two parts Y/Z. Any negative sign is resolved to the numerator.
     *
     * @param numerator   The numerator, e.g., the 3 in '3/7'.
     * @param denominator The denominator, e.g., the 7 in '3/7'.
     * @return A new fraction instance.
     * @throws ArithmeticException If the denominator is {@code zero} or the denominator is {@code negative} and the
     *                             numerator is {@code Integer#MIN_VALUE}.
     */
    public static Fraction of(int numerator, int denominator) {
        if (denominator == 0) {
            throw new ArithmeticException("The denominator must not be zero");
        }
        if (denominator < 0) {
            if (numerator == Integer.MIN_VALUE || denominator == Integer.MIN_VALUE) {
                throw new ArithmeticException("overflow: can't negate");
            }
            numerator = -numerator;
            denominator = -denominator;
        }
        return new Fraction(numerator, denominator);
    }

    /**
     * Creates a {@code Fraction} instance with the three parts X Y/Z. The negative sign must be passed to the integer
     * part.
     *
     * @param whole       The integer part, e.g., the 1 in 'one and three sevenths'.
     * @param numerator   The numerator, e.g., the 3 in 'one and three sevenths'.
     * @param denominator The denominator, e.g., the 7 in 'one and three sevenths'.
     * @return A new fraction instance.
     * @throws ArithmeticException If the denominator is {@code zero}.
     * @throws ArithmeticException If the denominator is negative.
     * @throws ArithmeticException If the numerator is negative.
     * @throws ArithmeticException If the resulting numerator exceeds {@code Integer.MAX_VALUE}.
     */
    public static Fraction of(final int whole, final int numerator, final int denominator) {
        if (denominator == 0) {
            throw new ArithmeticException("The denominator must not be zero");
        }
        if (denominator < 0) {
            throw new ArithmeticException("The denominator must not be negative");
        }
        if (numerator < 0) {
            throw new ArithmeticException("The numerator must not be negative");
        }
        final long numeratorValue;
        if (whole < 0) {
            numeratorValue = whole * (long) denominator - numerator;
        } else {
            numeratorValue = whole * (long) denominator + numerator;
        }
        if (numeratorValue < Integer.MIN_VALUE || numeratorValue > Integer.MAX_VALUE) {
            throw new ArithmeticException("Numerator too large to represent as an Integer.");
        }
        return new Fraction((int) numeratorValue, denominator);
    }

    /**
     * Creates a Fraction from a {@link String}.
     *
     * <p>
     * The accepted formats are:
     *
     * <ol>
     * <li>A {@code double} string containing a dot.</li>
     * <li>'X Y/Z'</li>
     * <li>'Y/Z'</li>
     * <li>'X' (a simple integer)</li>
     * </ol>
     * <p>
     * and a ..
     *
     * @param str The string to parse, must not be {@code null}.
     * @return The new {@code Fraction} instance.
     * @throws NullPointerException  If the string is {@code null}.
     * @throws NumberFormatException If the number format is invalid.
     */
    public static Fraction of(String str) {
        Objects.requireNonNull(str, "str");
        // parse double format
        int pos = str.indexOf('.');
        if (pos >= 0) {
            return of(Double.parseDouble(str));
        }

        // parse X Y/Z format
        pos = str.indexOf(' ');
        if (pos > 0) {
            final int whole = Integer.parseInt(str.substring(0, pos));
            str = str.substring(pos + 1);
            pos = str.indexOf('/');
            if (pos < 0) {
                throw new NumberFormatException("The fraction could not be parsed as the format X Y/Z");
            }
            final int numer = Integer.parseInt(str.substring(0, pos));
            final int denom = Integer.parseInt(str.substring(pos + 1));
            return of(whole, numer, denom);
        }

        // parse Y/Z format
        pos = str.indexOf('/');
        if (pos < 0) {
            // simple whole number
            return of(Integer.parseInt(str), 1);
        }
        final int numer = Integer.parseInt(str.substring(0, pos));
        final int denom = Integer.parseInt(str.substring(pos + 1));
        return of(numer, denom);
    }

    /**
     * Creates a reduced {@code Fraction} instance with the two parts Y/Z.
     *
     * <p>
     * For example, if the input parameters represent 2/4, the created fraction will be 1/2.
     *
     * <p>
     * Any negative sign is resolved to the numerator.
     *
     * @param numerator   The numerator, e.g., the 3 in '3/7'.
     * @param denominator The denominator, e.g., the 7 in '3/7'.
     * @return A new fraction instance with the numerator and denominator reduced.
     * @throws ArithmeticException If the denominator is {@code zero}.
     */
    public static Fraction ofReduced(int numerator, int denominator) {
        if (denominator == 0) {
            throw new ArithmeticException("The denominator must not be zero");
        }
        if (numerator == 0) {
            return ZERO; // normalize zero.
        }
        // allow 2^k/-2^31 as a valid fraction (where k>0)
        if (denominator == Integer.MIN_VALUE && (numerator & 1) == 0) {
            numerator /= 2;
            denominator /= 2;
        }
        if (denominator < 0) {
            if (numerator == Integer.MIN_VALUE || denominator == Integer.MIN_VALUE) {
                throw new ArithmeticException("overflow: can't negate");
            }
            numerator = -numerator;
            denominator = -denominator;
        }
        // simplify fraction.
        final int gcd = MathKit.gcd(numerator, denominator);
        numerator /= gcd;
        denominator /= gcd;
        return new Fraction(numerator, denominator);
    }

    /**
     * Adds two integers, checking for overflow.
     *
     * @param x The first addend.
     * @param y The second addend.
     * @return The sum {@code x+y}.
     * @throws ArithmeticException If the result cannot be represented as an int.
     */
    private static int addAndCheck(final int x, final int y) {
        final long s = (long) x + (long) y;
        if (s < Integer.MIN_VALUE || s > Integer.MAX_VALUE) {
            throw new ArithmeticException("overflow: add");
        }
        return (int) s;
    }

    /**
     * Multiplies two integers, checking for overflow.
     *
     * @param x One multiplicand.
     * @param y The other multiplicand.
     * @return The product {@code x*y}.
     * @throws ArithmeticException If the result cannot be represented as an int.
     */
    private static int mulAndCheck(final int x, final int y) {
        final long m = (long) x * (long) y;
        if (m < Integer.MIN_VALUE || m > Integer.MAX_VALUE) {
            throw new ArithmeticException("overflow: mul");
        }
        return (int) m;
    }

    /**
     * Multiplies two positive integers, checking for overflow.
     *
     * @param x One positive multiplicand.
     * @param y The other positive multiplicand.
     * @return The product {@code x*y}.
     * @throws ArithmeticException If the result cannot be represented as an int or if either parameter is not positive.
     */
    private static int mulPosAndCheck(final int x, final int y) {
        /* assert x>=0 && y>=0; */
        final long m = (long) x * (long) y;
        if (m > Integer.MAX_VALUE) {
            throw new ArithmeticException("overflow: mulPos");
        }
        return (int) m;
    }

    /**
     * Subtracts one integer from another, checking for overflow.
     *
     * @param x The minuend.
     * @param y The subtrahend.
     * @return The difference {@code x - y}.
     * @throws ArithmeticException If the result cannot be represented as an int.
     */
    private static int subAndCheck(final int x, final int y) {
        final long s = (long) x - (long) y;
        if (s < Integer.MIN_VALUE || s > Integer.MAX_VALUE) {
            throw new ArithmeticException("overflow: add");
        }
        return (int) s;
    }

    /**
     * Gets the positive fraction equivalent to this fraction.
     * <p>
     * More precisely: {@code (fraction >= 0 ? this : -fraction)}
     *
     * <p>
     * The returned fraction is not reduced.
     *
     * @return {@code this} if this fraction is positive; otherwise, a new positive fraction instance with the opposite
     *         sign of the numerator.
     */
    public Fraction abs() {
        if (numerator >= 0) {
            return this;
        }
        return negate();
    }

    /**
     * Adds this fraction to another fraction, returning the reduced result. The algorithm follows Knuth's section
     * 4.5.1.
     *
     * @param fraction The fraction to add, not null.
     * @return A {@code Fraction} instance with the resulting value.
     * @throws NullPointerException If the passed fraction is {@code null}.
     * @throws ArithmeticException  If the numerator or denominator of the result exceeds {@code Integer.MAX_VALUE}.
     */
    public Fraction add(final Fraction fraction) {
        return addSub(fraction, true /* add */);
    }

    /**
     * Implements addition and subtraction using the algorithm described in Knuth's section 4.5.1.
     *
     * @param fraction The fraction to add or subtract, not null.
     * @param isAdd    If true, performs addition; if false, performs subtraction.
     * @return A {@code Fraction} instance with the resulting value.
     * @throws IllegalArgumentException If the passed fraction is {@code null}.
     * @throws ArithmeticException      If the numerator or denominator of the result cannot be represented as an
     *                                  {@code int}.
     */
    private Fraction addSub(final Fraction fraction, final boolean isAdd) {
        Objects.requireNonNull(fraction, "fraction");
        // zero is identity for addition.
        if (numerator == 0) {
            return isAdd ? fraction : fraction.negate();
        }
        if (fraction.numerator == 0) {
            return this;
        }
        // if denominators are randomly distributed, d1 will be 1 about 61%
        // of the time.
        final int d1 = MathKit.gcd(denominator, fraction.denominator);
        if (d1 == 1) {
            // result is ( (u*v' +/- u'v) / u'v')
            final int uvp = mulAndCheck(numerator, fraction.denominator);
            final int upv = mulAndCheck(fraction.numerator, denominator);
            return new Fraction(isAdd ? addAndCheck(uvp, upv) : subAndCheck(uvp, upv),
                    mulPosAndCheck(denominator, fraction.denominator));
        }
        // the quantity 't' requires 65 bits of precision; see knuth 4.5.1
        // exercise 7. we're going to use a BigInteger.
        // t = u(v'/d1) +/- v(u'/d1)
        final BigInteger uvp = BigInteger.valueOf(numerator).multiply(BigInteger.valueOf(fraction.denominator / d1));
        final BigInteger upv = BigInteger.valueOf(fraction.numerator).multiply(BigInteger.valueOf(denominator / d1));
        final BigInteger t = isAdd ? uvp.add(upv) : uvp.subtract(upv);
        // but d2 doesn't need extra precision because
        // d2 = gcd(t,d1) = gcd(t mod d1, d1)
        final int tmodd1 = t.mod(BigInteger.valueOf(d1)).intValue();
        final int d2 = tmodd1 == 0 ? d1 : MathKit.gcd(tmodd1, d1);

        // result is (t/d2) / (u'/d1)(v'/d2)
        final BigInteger w = t.divide(BigInteger.valueOf(d2));
        if (w.bitLength() > 31) {
            throw new ArithmeticException("overflow: numerator too large after multiply");
        }
        return new Fraction(w.intValue(), mulPosAndCheck(denominator / d1, fraction.denominator / d2));
    }

    /**
     * Divides this fraction by another fraction.
     *
     * @param fraction The fraction to divide by, not null.
     * @return A {@code Fraction} instance with the resulting value.
     * @throws NullPointerException If the passed fraction is {@code null}.
     * @throws ArithmeticException  If the fraction to divide by is zero.
     * @throws ArithmeticException  If the numerator or denominator of the result exceeds {@code Integer.MAX_VALUE}.
     */
    public Fraction divideBy(final Fraction fraction) {
        Objects.requireNonNull(fraction, "fraction");
        if (fraction.numerator == 0) {
            throw new ArithmeticException("The fraction to divide by must not be zero");
        }
        return multiplyBy(fraction.invert());
    }

    /**
     * Gets the denominator part of the fraction.
     *
     * @return The denominator part of the fraction.
     */
    public int getDenominator() {
        return denominator;
    }

    /**
     * Gets the numerator part of the fraction.
     *
     * <p>
     * This method may return a value greater than the denominator, i.e., an improper fraction, such as the 7 in 7/4.
     *
     * @return The numerator part of the fraction.
     */
    public int getNumerator() {
        return numerator;
    }

    /**
     * Gets the numerator part of the proper fraction, which is always positive.
     *
     * <p>
     * An improper fraction 7/4 can be decomposed into a proper fraction 1 3/4. This method returns the 3 from the
     * proper fraction.
     *
     * <p>
     * If the fraction is negative, e.g., -7/4, it can be decomposed into -1 3/4, so this method returns the positive
     * proper fraction numerator, which is 3.
     *
     * @return The numerator part of the proper fraction, always positive.
     */
    public int getProperNumerator() {
        return Math.abs(numerator % denominator);
    }

    /**
     * Gets the integer part of the proper fraction, including the sign.
     *
     * <p>
     * An improper fraction 7/4 can be decomposed into a proper fraction 1 3/4. This method returns the 1 from the
     * proper fraction.
     *
     * <p>
     * If the fraction is negative, e.g., -7/4, it can be decomposed into -1 3/4, so this method returns the integer
     * part of the proper fraction, -1.
     *
     * @return The integer part of the proper fraction, including the sign.
     */
    public int getProperWhole() {
        return numerator / denominator;
    }

    /**
     * Gets the reciprocal of this fraction (1/fraction).
     *
     * <p>
     * The returned fraction is not reduced.
     *
     * @return A new fraction instance with its numerator and denominator swapped.
     * @throws ArithmeticException If the fraction represents zero.
     */
    public Fraction invert() {
        if (numerator == 0) {
            throw new ArithmeticException("Unable to invert zero.");
        }
        if (numerator == Integer.MIN_VALUE) {
            throw new ArithmeticException("overflow: can't negate numerator");
        }
        if (numerator < 0) {
            return new Fraction(-denominator, -numerator);
        }
        return new Fraction(denominator, numerator);
    }

    /**
     * Multiplies this fraction by another fraction, returning the reduced result.
     *
     * @param fraction The fraction to multiply by, not null.
     * @return A {@code Fraction} instance with the resulting value.
     * @throws NullPointerException If the passed fraction is {@code null}.
     * @throws ArithmeticException  If the numerator or denominator of the result exceeds {@code Integer.MAX_VALUE}.
     */
    public Fraction multiplyBy(final Fraction fraction) {
        Objects.requireNonNull(fraction, "fraction");
        if (numerator == 0 || fraction.numerator == 0) {
            return ZERO;
        }
        // knuth 4.5.1
        // make sure we don't overflow unless the result *must* overflow.
        final int d1 = MathKit.gcd(numerator, fraction.denominator);
        final int d2 = MathKit.gcd(fraction.numerator, denominator);
        return ofReduced(
                mulAndCheck(numerator / d1, fraction.numerator / d2),
                mulPosAndCheck(denominator / d2, fraction.denominator / d1));
    }

    /**
     * Gets the negative of this fraction (-fraction).
     *
     * <p>
     * The returned fraction is not reduced.
     *
     * @return A new fraction instance with the opposite sign of the numerator.
     */
    public Fraction negate() {
        // the positive range is one smaller than the negative range of an int.
        if (numerator == Integer.MIN_VALUE) {
            throw new ArithmeticException("overflow: too large to negate");
        }
        return new Fraction(-numerator, denominator);
    }

    /**
     * Gets this fraction to the specified power.
     *
     * <p>
     * The returned fraction is reduced.
     *
     * @param power The power to raise the fraction to.
     * @return {@code this} if the power is one; {@link #ONE} if the power is zero (even if the fraction is zero);
     *         otherwise, a new fraction instance raised to the corresponding power.
     * @throws ArithmeticException If the numerator or denominator of the result exceeds {@code Integer.MAX_VALUE}.
     */
    public Fraction pow(final int power) {
        if (power == 1) {
            return this;
        }
        if (power == 0) {
            return ONE;
        }
        if (power < 0) {
            if (power == Integer.MIN_VALUE) { // MIN_VALUE can't be negated.
                return this.invert().pow(2).pow(-(power / 2));
            }
            return this.invert().pow(-power);
        }
        final Fraction f = this.multiplyBy(this);
        if (power % 2 == 0) { // if even...
            return f.pow(power / 2);
        }
        return f.pow(power / 2).multiplyBy(this);
    }

    /**
     * Reduces the fraction to its smallest numerator and denominator, and returns the result.
     *
     * <p>
     * For example, if this fraction represents 2/4, the result will be 1/2.
     *
     * @return A new reduced fraction instance, or this instance if it cannot be further simplified.
     */
    public Fraction reduce() {
        if (numerator == 0) {
            return equals(ZERO) ? this : ZERO;
        }
        final int gcd = MathKit.gcd(Math.abs(numerator), denominator);
        if (gcd == 1) {
            return this;
        }
        return of(numerator / gcd, denominator / gcd);
    }

    /**
     * Subtracts the value of another fraction from this fraction, returning the reduced result.
     *
     * @param fraction The fraction to subtract, not null.
     * @return A {@code Fraction} instance with the resulting value.
     * @throws NullPointerException If the passed fraction is {@code null}.
     * @throws ArithmeticException  If the numerator or denominator of the result cannot be represented as an
     *                              {@code int}.
     */
    public Fraction subtract(final Fraction fraction) {
        return addSub(fraction, false /* subtract */);
    }

    /**
     * Gets the string representation of this fraction in proper form, as X Y/Z.
     *
     * <p>
     * The format used is '<em>integer part</em> <em>numerator</em>/<em>denominator</em>'. If the integer part is zero,
     * it is omitted. If the numerator is zero, only the integer part is returned.
     *
     * @return The string representation of the fraction.
     */
    public String toProperString() {
        if (toProperString == null) {
            if (numerator == 0) {
                toProperString = "0";
            } else if (numerator == denominator) {
                toProperString = "1";
            } else if (numerator == -1 * denominator) {
                toProperString = "-1";
            } else if ((numerator > 0 ? -numerator : numerator) < -denominator) {
                // note that we do the magnitude comparison test above with
                // NEGATIVE (not positive) numbers, since negative numbers
                // have a larger range. otherwise numerator == Integer.MIN_VALUE
                // is handled incorrectly.
                final int properNumerator = getProperNumerator();
                if (properNumerator == 0) {
                    toProperString = Integer.toString(getProperWhole());
                } else {
                    toProperString = getProperWhole() + Symbol.SPACE + properNumerator + "/" + getDenominator();
                }
            } else {
                toProperString = getNumerator() + "/" + getDenominator();
            }
        }
        return toProperString;
    }

    /**
     * Returns the value of this fraction as a {@code double}.
     *
     * @return The fraction value as a double.
     */
    @Override
    public double doubleValue() {
        return (double) numerator / (double) denominator;
    }

    /**
     * Returns the value of this fraction as a {@code float}.
     *
     * @return The fraction value as a float.
     */
    @Override
    public float floatValue() {
        return (float) numerator / (float) denominator;
    }

    /**
     * Returns the value of this fraction as an {@code int}, truncating if necessary.
     *
     * @return The fraction value as an integer.
     */
    @Override
    public int intValue() {
        return numerator / denominator;
    }

    /**
     * Returns the value of this fraction as a {@code long}, truncating if necessary.
     *
     * @return The fraction value as a long.
     */
    @Override
    public long longValue() {
        return (long) numerator / denominator;
    }

    // region ----- private methods

    /**
     * Compares this fraction with another.
     *
     * @param other The fraction to compare with.
     * @return A negative integer, zero, or a positive integer as this fraction is less than, equal to, or greater than
     *         the specified fraction.
     */
    @Override
    public int compareTo(final Fraction other) {
        if (equals(other)) {
            return 0;
        }

        // otherwise see which is less
        final long first = (long) numerator * (long) other.denominator;
        final long second = (long) other.numerator * (long) denominator;
        return Long.compare(first, second);
    }

    /**
     * Checks if this fraction is equal to another object.
     *
     * @param object The object to compare with.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Fraction other)) {
            return false;
        }
        return getNumerator() == other.getNumerator() && getDenominator() == other.getDenominator();
    }

    /**
     * Returns the hash code for this fraction.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        if (hashCode == 0) {
            // hash code update should be atomic.
            hashCode = 37 * (37 * 17 + getNumerator()) + getDenominator();
        }
        return hashCode;
    }

    /**
     * Returns the string representation of this fraction.
     *
     * @return A string in the format "numerator/denominator".
     */
    @Override
    public String toString() {
        if (toString == null) {
            toString = getNumerator() + "/" + getDenominator();
        }
        return toString;
    }

}
