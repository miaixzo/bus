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
package org.miaixz.bus.health.builtin.hardware;

import java.util.Arrays;
import java.util.Locale;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Edid;
import org.miaixz.bus.health.Formats;
import org.miaixz.bus.health.Parsing;

/**
 * Default {@link DisplayInfo} implementation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public class DisplayInfoImpl implements DisplayInfo {

    /**
     * Whether this display information synthesizes its EDID.
     */
    private final boolean synthetic;

    /**
     * The raw or synthesized EDID bytes.
     */
    private volatile byte[] edid;

    /**
     * The manufacturer ID for synthetic EDID instances.
     */
    private final String manufacturerID;

    /**
     * The product ID for synthetic EDID instances.
     */
    private final String productID;

    /**
     * The serial number for synthetic EDID instances.
     */
    private final String serialNo;

    /**
     * The manufacture week for synthetic EDID instances.
     */
    private final byte week;

    /**
     * The manufacture year for synthetic EDID instances.
     */
    private final int year;

    /**
     * The EDID version for synthetic EDID instances.
     */
    private final String version;

    /**
     * Whether the synthetic display is digital.
     */
    private final boolean digital;

    /**
     * The horizontal size in centimeters for synthetic EDID instances.
     */
    private final int hcm;

    /**
     * The vertical size in centimeters for synthetic EDID instances.
     */
    private final int vcm;

    /**
     * The preferred resolution for synthetic EDID instances.
     */
    private final String preferredResolution;

    /**
     * The model name for synthetic EDID instances.
     */
    private final String model;

    /**
     * The product serial number descriptor for synthetic EDID instances.
     */
    private final String productSerialNumber;

    /**
     * Creates a new display information object from raw EDID bytes.
     *
     * @param edid the raw EDID bytes reported by the display
     */
    public DisplayInfoImpl(byte[] edid) {
        this.edid = Arrays.copyOf(edid, edid.length);
        this.synthetic = false;
        this.manufacturerID = null;
        this.productID = null;
        this.serialNo = null;
        this.week = 0;
        this.year = 0;
        this.version = null;
        this.digital = false;
        this.hcm = 0;
        this.vcm = 0;
        this.preferredResolution = null;
        this.model = null;
        this.productSerialNumber = null;
    }

    /**
     * Creates a synthetic display information object from decoded field values.
     *
     * @param manufacturerID      the manufacturer ID
     * @param productID           the product ID
     * @param serialNo            the serial number
     * @param week                the week of manufacture
     * @param year                the year of manufacture
     * @param version             the EDID version
     * @param digital             whether the display is digital
     * @param hcm                 the horizontal size in centimeters
     * @param vcm                 the vertical size in centimeters
     * @param preferredResolution the preferred resolution
     * @param model               the model name
     * @param productSerialNumber the product serial number descriptor text
     */
    public DisplayInfoImpl(String manufacturerID, String productID, String serialNo, byte week, int year,
            String version, boolean digital, int hcm, int vcm, String preferredResolution, String model,
            String productSerialNumber) {
        this.edid = null;
        this.synthetic = true;
        this.manufacturerID = manufacturerID;
        this.productID = productID;
        this.serialNo = serialNo;
        this.week = week;
        this.year = year;
        this.version = version;
        this.digital = digital;
        this.hcm = hcm;
        this.vcm = vcm;
        this.preferredResolution = preferredResolution;
        this.model = model;
        this.productSerialNumber = productSerialNumber == null ? Normal.EMPTY : productSerialNumber;
    }

    /**
     * Returns the EDID bytes.
     *
     * @return a copy of the EDID bytes
     */
    @Override
    public byte[] getEdid() {
        byte[] result = cachedEdid();
        return Arrays.copyOf(result, result.length);
    }

    /**
     * Returns the cached EDID, synthesizing it on first use.
     *
     * @return the cached EDID
     */
    private synchronized byte[] cachedEdid() {
        if (this.edid == null) {
            this.edid = synthesizeEdid();
        }
        return this.edid;
    }

    /**
     * Returns whether the EDID is synthetic.
     *
     * @return {@code true} if synthetic, otherwise {@code false}
     */
    @Override
    public boolean isEdidSynthetic() {
        return this.synthetic;
    }

    /**
     * Returns the manufacturer ID.
     *
     * @return the manufacturer ID
     */
    @Override
    public String getManufacturerID() {
        return this.synthetic ? this.manufacturerID : Edid.getManufacturerID(this.edid);
    }

    /**
     * Returns the product ID.
     *
     * @return the product ID
     */
    @Override
    public String getProductID() {
        return this.synthetic ? this.productID : Edid.getProductID(this.edid);
    }

    /**
     * Returns the serial number.
     *
     * @return the serial number
     */
    @Override
    public String getSerialNo() {
        return this.synthetic ? this.serialNo : Edid.getSerialNo(this.edid);
    }

    /**
     * Returns the week of manufacture.
     *
     * @return the week of manufacture
     */
    @Override
    public byte getWeek() {
        return this.synthetic ? this.week : Edid.getWeek(this.edid);
    }

    /**
     * Returns the year of manufacture.
     *
     * @return the year of manufacture
     */
    @Override
    public int getYear() {
        return this.synthetic ? this.year : Edid.getYear(this.edid);
    }

    /**
     * Returns the EDID version.
     *
     * @return the EDID version
     */
    @Override
    public String getVersion() {
        return this.synthetic ? this.version : Edid.getVersion(this.edid);
    }

    /**
     * Returns whether the display is digital.
     *
     * @return {@code true} if digital, otherwise {@code false}
     */
    @Override
    public boolean isDigital() {
        return this.synthetic ? this.digital : Edid.isDigital(this.edid);
    }

    /**
     * Returns the horizontal size in centimeters.
     *
     * @return the horizontal size in centimeters
     */
    @Override
    public int getHcm() {
        return this.synthetic ? this.hcm : Edid.getHcm(this.edid);
    }

    /**
     * Returns the vertical size in centimeters.
     *
     * @return the vertical size in centimeters
     */
    @Override
    public int getVcm() {
        return this.synthetic ? this.vcm : Edid.getVcm(this.edid);
    }

    /**
     * Returns the preferred resolution.
     *
     * @return the preferred resolution
     */
    @Override
    public String getPreferredResolution() {
        return this.synthetic ? this.preferredResolution : Edid.getPreferredResolution(this.edid);
    }

    /**
     * Returns the model name.
     *
     * @return the model name
     */
    @Override
    public String getModel() {
        return this.synthetic ? this.model : Edid.getModel(this.edid);
    }

    /**
     * Returns the product serial number descriptor text.
     *
     * @return the product serial number descriptor text
     */
    @Override
    public String getProductSerialNumber() {
        return this.synthetic ? this.productSerialNumber : Edid.getProductSerialNumber(this.edid);
    }

    /**
     * Synthesizes an EDID from the decoded fields.
     *
     * @return the synthesized EDID
     */
    private byte[] synthesizeEdid() {
        byte[] e = Edid.newEdidTemplate();
        Edid.setManufacturerID(e, this.manufacturerID);
        Edid.setProductID(e, this.productID);
        setSerialNoSafe(e, this.serialNo);
        Edid.setWeek(e, this.week);
        Edid.setYear(e, this.year);
        Edid.setVersion(e, this.version);
        Edid.setDigital(e, this.digital);
        Edid.setHcm(e, this.hcm);
        Edid.setVcm(e, this.vcm);
        Edid.setPreferredResolution(e, this.preferredResolution);
        Edid.setModel(e, this.model);
        if (!this.productSerialNumber.isEmpty()) {
            Edid.setProductSerialNumber(e, this.productSerialNumber);
        }
        Edid.updateChecksum(e);
        return e;
    }

    /**
     * Sets the serial number while tolerating serial values that are display attributes rather than EDID
     * round-trippable values.
     *
     * @param edid     the EDID byte array to modify
     * @param serialNo the serial number to set
     */
    private static void setSerialNoSafe(byte[] edid, String serialNo) {
        if (serialNo == null || serialNo.isEmpty()) {
            return;
        }
        try {
            Edid.setSerialNo(edid, serialNo);
        } catch (IllegalArgumentException e) {
            if (serialNo.length() == 8) {
                long numeric = Parsing.hexStringToLong(serialNo, 0L);
                if (numeric != 0L) {
                    Edid.setSerialNo(edid, numeric);
                }
            }
        }
    }

    /**
     * Returns this display information as text.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        if (!this.synthetic) {
            return Formats.formatEdid(this.edid);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("  Manuf. ID=").append(this.manufacturerID);
        sb.append(", Product ID=").append(this.productID);
        sb.append(Symbol.COMMA).append(Symbol.SPACE).append(this.digital ? "Digital" : "Analog");
        sb.append(", Serial=").append(this.serialNo);
        sb.append(", ManufDate=").append(this.week * 12 / 52 + 1).append('/').append(this.year);
        sb.append(", EDID v").append(this.version);
        sb.append(
                String.format(
                        Locale.ROOT,
                        "%n  %d x %d cm (%.1f x %.1f in)",
                        this.hcm,
                        this.vcm,
                        this.hcm / 2.54,
                        this.vcm / 2.54));
        sb.append(String.format(Locale.ROOT, "%n  Preferred Resolution: %s", this.preferredResolution));
        sb.append(String.format(Locale.ROOT, "%n  Monitor Name: %s", this.model));
        if (!this.productSerialNumber.isEmpty()) {
            sb.append(String.format(Locale.ROOT, "%n  Serial Number: %s", this.productSerialNumber));
        }
        return sb.toString();
    }

}
