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

import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A class for handling monetary calculations, currency, and rounding for a single currency. The Money class
 * encapsulates the monetary amount and currency. The amount is internally represented as a long, in the smallest unit
 * of the currency (e.g., cents for USD).
 *
 * <p>
 * Currently, the Money class provides the following main features:
 * <ul>
 * <li>Supports conversion between Money objects and double(float)/long(int)/String/BigDecimal.</li>
 * <li>Provides arithmetic operations similar to JDK's BigDecimal, supporting arbitrary precision to accommodate various
 * financial rules.</li>
 * <li>Offers a set of simple arithmetic operations that use default precision handling rules.</li>
 * <li>It is recommended to use Money instead of BigDecimal directly because, with BigDecimal, the same amount and
 * currency can have multiple representations (e.g., new BigDecimal("10.5") is not equal to new BigDecimal("10.50") due
 * to different scales). The Money class ensures that the same amount and currency have a single representation (e.g.,
 * new Money("10.5") and new Money("10.50") are equal).</li>
 * <li>Another reason not to use BigDecimal directly is that it is immutable. Any operation on a BigDecimal object
 * creates a new object, which can be inefficient for large-scale statistical calculations. The Money class is mutable,
 * providing better performance for bulk statistics.</li>
 * <li>Provides basic formatting functionality.</li>
 * <li>The Money class does not include business-related statistical and formatting functions. It is recommended to
 * implement business-related functions using utility classes.</li>
 * <li>The Money class implements the Serializable interface, allowing it to be used as a parameter and return value in
 * remote calls.</li>
 * <li>The Money class implements the equals and hashCode methods.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Money implements Serializable, Comparable<Money> {

    @Serial
    private static final long serialVersionUID = 2852229719218L;

    /**
     * A set of possible conversion factors between major and minor currency units.
     *
     * <p>
     * Here, "minor unit" refers to the smallest unit of the currency, and "major unit" is the most commonly used unit.
     * Different currencies have different conversion factors, such as 100 for USD (dollar to cent) and 1 for JPY (yen).
     */
    private static final int[] CENT_FACTORS = new int[] { 1, 10, 100, 1000 };

    /**
     * The currency.
     */
    private final Currency currency;

    /**
     * The amount in the smallest currency unit (cents).
     */
    private long cent;

    /**
     * Default constructor. Creates a Money object with a default amount (0) and default currency.
     */
    public Money() {
        this(0);
    }

    /**
     * Constructor. Creates a Money object with the specified major and minor units and the default currency.
     *
     * @param yuan The major unit of the amount. If 0, the major part is extracted from the minor unit.
     * @param cent The minor unit of the amount.
     */
    public Money(final long yuan, final int cent) {
        this(yuan, cent, Currency.getInstance(Normal.CNY));
    }

    /**
     * Constructor. Creates a Money object with the specified major and minor units and the specified currency.
     *
     * @param yuan     The major unit of the amount. If 0, the major part is extracted from the minor unit.
     * @param cent     The minor unit of the amount.
     * @param currency The currency unit.
     */
    public Money(final long yuan, final int cent, final Currency currency) {
        this.currency = currency;

        if (0 == yuan) {
            this.cent = cent;
        } else {
            this.cent = (yuan * getCentFactor()) + (cent % getCentFactor());
        }
    }

    /**
     * Constructor. Creates a Money object with the specified amount in major units and the default currency.
     *
     * @param amount The amount in major units.
     */
    public Money(final String amount) {
        this(amount, Currency.getInstance(Normal.CNY));
    }

    /**
     * Constructor. Creates a Money object with the specified amount in major units and the specified currency.
     *
     * @param amount   The amount in major units.
     * @param currency The currency.
     */
    public Money(final String amount, final Currency currency) {
        this(new BigDecimal(amount), currency);
    }

    /**
     * Constructor. Creates a Money object with the specified amount in major units and the specified currency. If the
     * amount cannot be converted to an integer minor unit, it is rounded using the specified rounding mode.
     *
     * @param amount       The amount in major units.
     * @param currency     The currency.
     * @param roundingMode The rounding mode.
     */
    public Money(final String amount, final Currency currency, final RoundingMode roundingMode) {
        this(new BigDecimal(amount), currency, roundingMode);
    }

    /**
     * Constructor. Creates a Money object with the specified amount in major units and the default currency. If the
     * amount cannot be converted to an integer minor unit, it is rounded half-up.
     *
     * <p>
     * Note: Due to potential errors in double-precision floating-point arithmetic, the result of rounding half-up is
     * not always deterministic. Therefore, it is recommended to avoid creating Money objects from double types.
     * Example: {@code
     * assertEquals(999, Math.round(9.995 * 100));
     * assertEquals(1000, Math.round(999.5));
     * money = new Money(9.995);
     * assertEquals(999, money.getCent());
     * money = new Money(10.005);
     * assertEquals(1001, money.getCent());
     * }
     *
     * @param amount The amount in major units.
     */
    public Money(final double amount) {
        this(amount, Currency.getInstance(Normal.CNY));
    }

    /**
     * Constructor. Creates a Money object with the specified amount and currency. If the amount cannot be converted to
     * an integer minor unit, it is rounded half-up. Note: Due to potential errors in double-precision floating-point
     * arithmetic, the result of rounding half-up is not always deterministic. Therefore, it is recommended to avoid
     * creating Money objects from double types. Example: {@code
     * assertEquals(999, Math.round(9.995 * 100));
     * assertEquals(1000, Math.round(999.5));
     * money = new Money(9.995);
     * assertEquals(999, money.getCent());
     * money = new Money(10.005);
     * assertEquals(1001, money.getCent());
     * }
     *
     * @param amount   The amount in major units.
     * @param currency The currency.
     */
    public Money(final double amount, final Currency currency) {
        this.currency = currency;
        this.cent = Math.round(amount * getCentFactor());
    }

    /**
     * Constructor. Creates a Money object with the specified amount and the default currency. If the amount cannot be
     * converted to an integer minor unit, it is rounded using the default rounding mode.
     *
     * @param amount The amount in major units.
     */
    public Money(final BigDecimal amount) {
        this(amount, Currency.getInstance(Normal.CNY));
    }

    /**
     * Constructor. Creates a Money object with the specified amount and the default currency. If the amount cannot be
     * converted to an integer minor unit, it is rounded using the specified rounding mode.
     *
     * @param amount       The amount in major units.
     * @param roundingMode The rounding mode.
     */
    public Money(final BigDecimal amount, final RoundingMode roundingMode) {
        this(amount, Currency.getInstance(Normal.CNY), roundingMode);
    }

    /**
     * Constructor. Creates a Money object with the specified amount and currency. If the amount cannot be converted to
     * an integer minor unit, it is rounded using the default rounding mode.
     *
     * @param amount   The amount in major units.
     * @param currency The currency.
     */
    public Money(final BigDecimal amount, final Currency currency) {
        this(amount, currency, RoundingMode.HALF_EVEN);
    }

    /**
     * Constructor. Creates a Money object with the specified amount and currency. If the amount cannot be converted to
     * an integer minor unit, it is rounded using the specified rounding mode.
     *
     * @param amount       The amount in major units.
     * @param currency     The currency.
     * @param roundingMode The rounding mode.
     */
    public Money(final BigDecimal amount, final Currency currency, final RoundingMode roundingMode) {
        this.currency = currency;
        this.cent = rounding(amount.movePointRight(currency.getDefaultFractionDigits()), roundingMode);
    }

    /**
     * Gets the amount represented by this Money object.
     *
     * @return The amount in major units.
     */
    public BigDecimal getAmount() {
        return BigDecimal.valueOf(cent, currency.getDefaultFractionDigits());
    }

    /**
     * Sets the amount represented by this Money object.
     *
     * @param amount The amount in major units.
     */
    public void setAmount(final BigDecimal amount) {
        if (amount != null) {
            cent = rounding(amount.movePointRight(currency.getDefaultFractionDigits()), RoundingMode.HALF_EVEN);
        }
    }

    /**
     * Gets the amount represented by this Money object in minor units.
     *
     * @return The amount in minor units (cents).
     */
    public long getCent() {
        return cent;
    }

    /**
     * Sets the minor unit value of the currency.
     *
     * @param cent The minor unit value.
     */
    public void setCent(final long cent) {
        this.cent = cent;
    }

    /**
     * Gets the currency of this Money object.
     *
     * @return The currency of this Money object.
     */
    public Currency getCurrency() {
        return currency;
    }

    /**
     * Gets the conversion factor between the major and minor units of this currency.
     *
     * @return The conversion factor.
     */
    public int getCentFactor() {
        return CENT_FACTORS[currency.getDefaultFractionDigits()];
    }

    /**
     * Checks if this Money object is equal to another object.
     * <p>
     * A Money object is equal to another object if and only if:
     * <ul>
     * <li>The other object is also a Money object.</li>
     * <li>The amounts are the same.</li>
     * <li>The currencies are the same.</li>
     * </ul>
     *
     * @param other The object to compare with.
     * @return {@code true} if equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof Money) && equals((Money) other);
    }

    /**
     * Checks if this Money object is equal to another Money object.
     * <p>
     * A Money object is equal to another Money object if and only if:
     * <ul>
     * <li>The amounts are the same.</li>
     * <li>The currencies are the same.</li>
     * </ul>
     *
     * @param other The other Money object to compare with.
     * @return {@code true} if equal, {@code false} otherwise.
     */
    public boolean equals(final Money other) {
        return currency.equals(other.currency) && (cent == other.cent);
    }

    /**
     * Computes the hash code for this Money object.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Long.hashCode(cent);
    }

    /**
     * Compares this Money object with another Money object. Throws an {@code IllegalArgumentException} if the
     * currencies are different. Returns -1 if this amount is less than the other's, 0 if they are equal, and 1 if it is
     * greater.
     *
     * @param other The other Money object.
     * @return -1, 0, or 1 as this amount is less than, equal to, or greater than the other's.
     * @throws IllegalArgumentException if the currencies are different.
     */
    @Override
    public int compareTo(final Money other) {
        assertSameCurrencyAs(other);
        return Long.compare(cent, other.cent);
    }

    /**
     * Checks if this Money object is greater than another Money object. Throws an {@code IllegalArgumentException} if
     * the currencies are different.
     *
     * @param other The other Money object.
     * @return {@code true} if this amount is greater, {@code false} otherwise.
     * @throws IllegalArgumentException if the currencies are different.
     */
    public boolean greaterThan(final Money other) {
        return compareTo(other) > 0;
    }

    /**
     * Adds another Money object to this one. If the currencies are the same, returns a new Money object with the sum of
     * the amounts. This object's value remains unchanged. Throws an {@code IllegalArgumentException} if the currencies
     * are different.
     *
     * @param other The Money object to add.
     * @return The result of the addition.
     * @throws IllegalArgumentException if the currencies are different.
     */
    public Money add(final Money other) {
        assertSameCurrencyAs(other);
        return newMoneyWithSameCurrency(cent + other.cent);
    }

    /**
     * Adds another Money object to this one, modifying this object. If the currencies are the same, this object's
     * amount becomes the sum of the two amounts. Throws an {@code IllegalArgumentException} if the currencies are
     * different.
     *
     * @param other The Money object to add.
     * @return This Money object after addition.
     * @throws IllegalArgumentException if the currencies are different.
     */
    public Money addTo(final Money other) {
        assertSameCurrencyAs(other);
        this.cent += other.cent;
        return this;
    }

    /**
     * Subtracts another Money object from this one. If the currencies are the same, returns a new Money object with the
     * difference of the amounts. This object's value remains unchanged. Throws an {@code IllegalArgumentException} if
     * the currencies are different.
     *
     * @param other The Money object to subtract.
     * @return The result of the subtraction.
     * @throws IllegalArgumentException if the currencies are different.
     */
    public Money subtract(final Money other) {
        assertSameCurrencyAs(other);
        return newMoneyWithSameCurrency(cent - other.cent);
    }

    /**
     * Subtracts another Money object from this one, modifying this object. If the currencies are the same, this
     * object's amount becomes the difference of the two amounts. Throws an {@code IllegalArgumentException} if the
     * currencies are different.
     *
     * @param other The Money object to subtract.
     * @return This Money object after subtraction.
     * @throws IllegalArgumentException if the currencies are different.
     */
    public Money subtractFrom(final Money other) {
        assertSameCurrencyAs(other);
        this.cent -= other.cent;
        return this;
    }

    /**
     * Multiplies this Money object by a factor. Returns a new Money object with the same currency and the multiplied
     * amount. This object's value remains unchanged.
     *
     * @param val The multiplier.
     * @return The result of the multiplication.
     */
    public Money multiply(final long val) {
        return newMoneyWithSameCurrency(cent * val);
    }

    /**
     * Multiplies this Money object by a factor, modifying this object.
     *
     * @param val The multiplier.
     * @return This Money object after multiplication.
     */
    public Money multiplyBy(final long val) {
        this.cent *= val;
        return this;
    }

    /**
     * Multiplies this Money object by a factor. Returns a new Money object with the same currency and the multiplied
     * amount. This object's value remains unchanged. If the result cannot be converted to an integer minor unit, it is
     * rounded half-up.
     *
     * @param val The multiplier.
     * @return The result of the multiplication.
     */
    public Money multiply(final double val) {
        return newMoneyWithSameCurrency(Math.round(cent * val));
    }

    /**
     * Multiplies this Money object by a factor, modifying this object. If the result cannot be converted to an integer
     * minor unit, it is rounded half-up.
     *
     * @param val The multiplier.
     * @return This Money object after multiplication.
     */
    public Money multiplyBy(final double val) {
        this.cent = Math.round(this.cent * val);
        return this;
    }

    /**
     * Multiplies this Money object by a factor. Returns a new Money object with the same currency and the multiplied
     * amount. This object's value remains unchanged. If the result cannot be converted to an integer minor unit, the
     * default rounding mode is used.
     *
     * @param val The multiplier.
     * @return The result of the multiplication.
     */
    public Money multiply(final BigDecimal val) {
        return multiply(val, RoundingMode.HALF_EVEN);
    }

    /**
     * Multiplies this Money object by a factor, modifying this object. If the result cannot be converted to an integer
     * minor unit, the default rounding mode is used.
     *
     * @param val The multiplier.
     * @return This Money object after multiplication.
     */
    public Money multiplyBy(final BigDecimal val) {
        return multiplyBy(val, RoundingMode.HALF_EVEN);
    }

    /**
     * Multiplies this Money object by a factor. Returns a new Money object with the same currency and the multiplied
     * amount. This object's value remains unchanged. If the result cannot be converted to an integer minor unit, the
     * specified rounding mode is used.
     *
     * @param val          The multiplier.
     * @param roundingMode The rounding mode.
     * @return The result of the multiplication.
     */
    public Money multiply(final BigDecimal val, final RoundingMode roundingMode) {
        final BigDecimal newCent = BigDecimal.valueOf(cent).multiply(val);
        return newMoneyWithSameCurrency(rounding(newCent, roundingMode));
    }

    /**
     * Multiplies this Money object by a factor, modifying this object. If the result cannot be converted to an integer
     * minor unit, the specified rounding mode is used.
     *
     * @param val          The multiplier.
     * @param roundingMode The rounding mode.
     * @return This Money object after multiplication.
     */
    public Money multiplyBy(final BigDecimal val, final RoundingMode roundingMode) {
        final BigDecimal newCent = BigDecimal.valueOf(cent).multiply(val);
        this.cent = rounding(newCent, roundingMode);
        return this;
    }

    /**
     * Divides this Money object by a divisor. Returns a new Money object with the same currency and the divided amount.
     * This object's value remains unchanged. If the result cannot be converted to an integer minor unit, it is rounded
     * half-up.
     *
     * @param val The divisor.
     * @return The result of the division.
     */
    public Money divide(final double val) {
        return newMoneyWithSameCurrency(Math.round(cent / val));
    }

    /**
     * Divides this Money object by a divisor, modifying this object. If the result cannot be converted to an integer
     * minor unit, it is rounded half-up.
     *
     * @param val The divisor.
     * @return This Money object after division.
     */
    public Money divideBy(final double val) {
        this.cent = Math.round(this.cent / val);
        return this;
    }

    /**
     * Divides this Money object by a divisor. Returns a new Money object with the same currency and the divided amount.
     * This object's value remains unchanged. If the result cannot be converted to an integer minor unit, the default
     * rounding mode is used.
     *
     * @param val The divisor.
     * @return The result of the division.
     */
    public Money divide(final BigDecimal val) {
        return divide(val, RoundingMode.HALF_EVEN);
    }

    /**
     * Divides this Money object by a divisor. Returns a new Money object with the same currency and the divided amount.
     * This object's value remains unchanged. If the result cannot be converted to an integer minor unit, the specified
     * rounding mode is used.
     *
     * @param val          The divisor.
     * @param roundingMode The rounding mode.
     * @return The result of the division.
     */
    public Money divide(final BigDecimal val, final RoundingMode roundingMode) {
        final BigDecimal newCent = BigDecimal.valueOf(cent).divide(val, roundingMode);
        return newMoneyWithSameCurrency(newCent.longValue());
    }

    /**
     * Divides this Money object by a divisor, modifying this object. If the result cannot be converted to an integer
     * minor unit, the default rounding mode is used.
     *
     * @param val The divisor.
     * @return This Money object after division.
     */
    public Money divideBy(final BigDecimal val) {
        return divideBy(val, RoundingMode.HALF_EVEN);
    }

    /**
     * Divides this Money object by a divisor, modifying this object. If the result cannot be converted to an integer
     * minor unit, the specified rounding mode is used.
     *
     * @param val          The divisor.
     * @param roundingMode The rounding mode for decimal places.
     * @return This Money object after division.
     */
    public Money divideBy(final BigDecimal val, final RoundingMode roundingMode) {
        final BigDecimal newCent = BigDecimal.valueOf(cent).divide(val, roundingMode);
        this.cent = newCent.longValue();
        return this;
    }

    /**
     * Allocates this Money object into a specified number of targets as evenly as possible. If the amount cannot be
     * divided evenly, the remainder is distributed among the first few targets. This operation ensures that no amount
     * is lost.
     *
     * @param targets The number of targets to allocate to.
     * @return An array of Money objects, with a length equal to the number of targets. The elements are sorted in
     *         descending order, and the difference between any two amounts is at most 1 minor unit.
     */
    public Money[] allocate(final int targets) {
        final Money[] results = new Money[targets];

        final Money lowResult = newMoneyWithSameCurrency(cent / targets);
        final Money highResult = newMoneyWithSameCurrency(lowResult.cent + 1);

        final int remainder = (int) (cent % targets);

        for (int i = 0; i < remainder; i++) {
            results[i] = highResult;
        }

        for (int i = remainder; i < targets; i++) {
            results[i] = lowResult;
        }

        return results;
    }

    /**
     * Allocates this Money object into several parts according to the given ratios. The remainder is distributed
     * sequentially from the first part. This operation ensures that no amount is lost.
     *
     * @param ratios An array of allocation ratios. Each ratio is a long representing a relative part of the total.
     * @return An array of Money objects, with a length equal to the length of the ratios array.
     */
    public Money[] allocate(final long[] ratios) {
        final Money[] results = new Money[ratios.length];

        long total = 0;

        for (final long element : ratios) {
            total += element;
        }

        long remainder = cent;

        for (int i = 0; i < results.length; i++) {
            results[i] = newMoneyWithSameCurrency((cent * ratios[i]) / total);
            remainder -= results[i].cent;
        }

        for (int i = 0; i < remainder; i++) {
            results[i].cent++;
        }

        return results;
    }

    /**
     * Generates the default string representation of this object.
     */
    @Override
    public String toString() {
        return getAmount().toString();
    }

    /**
     * Asserts that this Money object has the same currency as another Money object. If they have the same currency, the
     * method returns. Otherwise, it throws an {@code IllegalArgumentException}.
     *
     * @param other The other Money object.
     * @throws IllegalArgumentException if the currencies are different.
     */
    protected void assertSameCurrencyAs(final Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Money math currency mismatch.");
        }
    }

    /**
     * Rounds a BigDecimal value using the specified rounding mode.
     *
     * @param val          The BigDecimal value to round.
     * @param roundingMode The rounding mode.
     * @return The rounded long value.
     */
    protected long rounding(final BigDecimal val, final RoundingMode roundingMode) {
        return val.setScale(0, roundingMode).longValue();
    }

    /**
     * Creates a new Money object with the same currency and the specified amount.
     *
     * @param cent The amount in minor units.
     * @return A new Money object with the same currency and the specified amount.
     */
    protected Money newMoneyWithSameCurrency(final long cent) {
        final Money money = new Money(0, currency);
        money.cent = cent;
        return money;
    }

    /**
     * Generates a string representation of the internal variables of this object, for debugging purposes.
     *
     * @return A string representation of the internal variables.
     */
    public String dump() {
        return StringKit.builder().append("cent = ").append(this.cent).append(File.separatorChar).append("currency = ")
                .append(this.currency).toString();
    }

}
