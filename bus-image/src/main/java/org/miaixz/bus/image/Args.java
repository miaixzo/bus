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
package org.miaixz.bus.image;

import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.metric.ApplicationEntity;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.SSLManagerFactory;
import org.miaixz.bus.image.metric.internal.pdu.AAssociateRQ;
import org.miaixz.bus.image.metric.internal.pdu.IdentityRQ;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.EnumSet;

/**
 * 请求参数信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class Args {

    public static String[] IVR_LE_FIRST = {
            UID.ImplicitVRLittleEndian,
            UID.ExplicitVRLittleEndian,
            UID.ExplicitVRBigEndianRetired
    };
    public static String[] EVR_LE_FIRST = {
            UID.ExplicitVRLittleEndian,
            UID.ExplicitVRBigEndianRetired,
            UID.ImplicitVRLittleEndian
    };
    public static String[] EVR_BE_FIRST = {
            UID.ExplicitVRBigEndianRetired,
            UID.ExplicitVRLittleEndian,
            UID.ImplicitVRLittleEndian
    };
    public static String[] IVR_LE_ONLY = {
            UID.ImplicitVRLittleEndian
    };
    /**
     * 绑定调用AET
     */
    private boolean bindCallingAet;

    /**
     * 接受的呼叫AET
     */
    private String[] acceptedCallingAETitles;
    /**
     * 信息模型
     */
    private Object informationModel;
    private EnumSet<Option.Type> types = EnumSet.noneOf(Option.Type.class);
    private String[] tsuidOrder = IVR_LE_FIRST;
    private String proxy;
    private IdentityRQ identity;
    private int priority = 0;
    private Option option;

    private boolean extendNegociation;
    /**
     * 扩展Sop类URL
     */
    private URL extendSopClassesURL;

    private URL extendStorageSOPClass;
    /**
     * 传输功能文件
     */
    private URL transferCapabilityFile;

    private int tag;
    private String[] values;
    private int[] parentSeqTags;
    /**
     * 存储模式
     */
    private String storagePattern;

    private Editors editors;

    public Args() {

    }

    public Args(boolean bindCallingAet) {
        this(null, bindCallingAet, null, null);
    }

    public Args(int tag, String... values) {
        this(null, tag, values);
    }

    /**
     * @param editors             修改DICOM属性的编辑器
     * @param extendNegociation   扩展SOP类
     * @param extendSopClassesURL SOP类扩展的配置文件
     */
    public Args(Editors editors,
                boolean extendNegociation,
                URL extendSopClassesURL) {
        this.editors = editors;
        this.extendNegociation = extendNegociation;
        this.extendSopClassesURL = extendSopClassesURL;
    }

    public Args(int[] parentSeqTags, int tag, String... values) {
        this.tag = tag;
        this.values = values;
        this.parentSeqTags = parentSeqTags;
    }

    /**
     * @param option                  可选的高级参数(代理、身份验证、连接和TLS)
     * @param bindCallingAet          当为true时，它将设置侦听器DICOM节点的AET。只有匹配称为AETitle的请求将被接受。
     *                                如果为假，所有被调用的AETs将被接受
     * @param storagePattern          存储模式
     * @param transferCapabilityFile  获取包含传输功能(sopclass、role、transferSyntaxes)的文件的URL
     * @param acceptedCallingAETitles 可接受的呼叫aetitle的列表。空将接受所有aetitle
     */
    public Args(Option option,
                boolean bindCallingAet,
                String storagePattern,
                URL transferCapabilityFile,
                String... acceptedCallingAETitles) {
        this.bindCallingAet = bindCallingAet;
        this.storagePattern = storagePattern;
        this.transferCapabilityFile = transferCapabilityFile;
        this.acceptedCallingAETitles = null == acceptedCallingAETitles ? new String[0] : acceptedCallingAETitles;
        if (null == option && null != this.option) {
            this.option.setMaxOpsInvoked(15);
            this.option.setMaxOpsPerformed(15);
        }
    }

    public String getTagName() {
        return ElementDictionary.keywordOf(tag, null);
    }

    /**
     * 使用callingNode绑定连接
     *
     * @param connection  连接信息
     * @param callingNode 节点信息
     */
    public void configureBind(Connection connection,
                              Node callingNode) {
        if (null != callingNode.getHostname()) {
            connection.setHostname(callingNode.getHostname());
        }
        if (null != callingNode.getPort()) {
            connection.setPort(callingNode.getPort());
        }
    }

    public void configureBind(AAssociateRQ aAssociateRQ,
                              Connection remote,
                              Node calledNode) {
        aAssociateRQ.setCalledAET(calledNode.getAet());
        if (null != identity) {
            aAssociateRQ.setIdentityRQ(identity);
        }
        remote.setHostname(calledNode.getHostname());
        remote.setPort(calledNode.getPort());
    }

    /**
     * 将连接和应用程序实体与callingNode绑定
     *
     * @param applicationEntity 应用实体
     * @param connection        连接信息
     * @param callingNode       节点信息
     */
    public void configureBind(ApplicationEntity applicationEntity,
                              Connection connection,
                              Node callingNode) {
        applicationEntity.setAETitle(callingNode.getAet());
        if (null != callingNode.getHostname()) {
            connection.setHostname(callingNode.getHostname());
        }
        if (null != callingNode.getPort()) {
            connection.setPort(callingNode.getPort());
        }
    }

    /**
     * 配置链接相关参数
     *
     * @param conn 链接信息
     */
    public void configure(Connection conn) {
        if (null != option) {
            conn.setBacklog(option.getBacklog());
            conn.setConnectTimeout(option.getConnectTimeout());
            conn.setRequestTimeout(option.getRequestTimeout());
            conn.setAcceptTimeout(option.getAcceptTimeout());
            conn.setReleaseTimeout(option.getReleaseTimeout());
            conn.setResponseTimeout(option.getResponseTimeout());
            conn.setRetrieveTimeout(option.getRetrieveTimeout());
            conn.setIdleTimeout(option.getIdleTimeout());
            conn.setSocketCloseDelay(option.getSocloseDelay());
            conn.setReceiveBufferSize(option.getSorcvBuffer());
            conn.setSendBufferSize(option.getSosndBuffer());
            conn.setReceivePDULength(option.getMaxPdulenRcv());
            conn.setSendPDULength(option.getMaxPdulenSnd());
            conn.setMaxOpsInvoked(option.getMaxOpsInvoked());
            conn.setMaxOpsPerformed(option.getMaxOpsPerformed());
            conn.setPackPDV(option.isPackPDV());
            conn.setTcpNoDelay(option.isTcpNoDelay());
        }
    }

    /**
     * 配置TLS链接相关参数
     *
     * @param conn   链接信息
     * @param remote 远程信息
     * @throws IOException 异常
     */
    public void configureTLS(Connection conn, Connection remote) throws IOException {
        if (null != option) {
            conn.setTlsCipherSuites(option.getCipherSuites());
            conn.setTlsProtocols(option.getTlsProtocols());
            conn.setTlsNeedClientAuth(option.isTlsNeedClientAuth());

            Device device = conn.getDevice();
            try {
                device.setKeyManager(SSLManagerFactory.createKeyManager(option.getKeystoreType(),
                        option.getKeystoreURL(), option.getKeystorePass(), option.getKeyPass()));
                device.setTrustManager(SSLManagerFactory.createTrustManager(option.getTruststoreType(),
                        option.getTruststoreURL(), option.getTruststorePass()));
                if (null != remote) {
                    remote.setTlsProtocols(conn.getTlsProtocols());
                    remote.setTlsCipherSuites(conn.getTlsCipherSuites());
                }
            } catch (GeneralSecurityException e) {
                throw new IOException(e);
            }
        }
    }

}
