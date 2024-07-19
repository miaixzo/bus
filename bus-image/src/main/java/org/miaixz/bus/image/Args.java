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

import lombok.Builder;
import lombok.*;
import org.miaixz.bus.core.net.tls.TrustAnyTrustManager;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.QueryOption;
import org.miaixz.bus.image.metric.net.ApplicationEntity;
import org.miaixz.bus.image.metric.net.Priority;
import org.miaixz.bus.image.metric.pdu.AAssociateRQ;
import org.miaixz.bus.image.metric.pdu.IdentityAC;
import org.miaixz.bus.image.metric.pdu.IdentityRQ;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * 请求参数信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Args {

    public static final String[] IVR_LE_FIRST = {
            UID.ImplicitVRLittleEndian.uid,
            UID.ExplicitVRLittleEndian.uid,
            UID.ExplicitVRBigEndian.uid
    };
    public static final String[] EVR_LE_FIRST = {
            UID.ExplicitVRLittleEndian.uid,
            UID.ExplicitVRBigEndian.uid,
            UID.ImplicitVRLittleEndian.uid
    };
    public static final String[] EVR_BE_FIRST = {
            UID.ExplicitVRBigEndian.uid,
            UID.ExplicitVRLittleEndian.uid,
            UID.ImplicitVRLittleEndian.uid
    };
    public static final String[] IVR_LE_ONLY = {
            UID.ImplicitVRLittleEndian.uid
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

    @Builder.Default
    private EnumSet<QueryOption> queryOptions = EnumSet.noneOf(QueryOption.class);
    @Builder.Default
    private String[] tsuidOrder = Arrays.copyOf(IVR_LE_FIRST, IVR_LE_FIRST.length);

    /**
     * 配置proxy <[user:password@]host:port>，指定HTTP代理隧道连接DICOM的主机和端口。
     */
    private String proxy;

    private IdentityRQ identityRQ;
    private IdentityAC identityAC;

    /**
     * 优先级
     */
    @Builder.Default
    private int priority = Priority.NORMAL;

    private Option option;

    private List<Editors> editors;

    /**
     * 是否启用通过UID或名称指定传输
     */
    private boolean negociation;
    /**
     * SOP类和传输语法可以通过其UID或名称指定
     * sop-classes.properties
     */
    private URL sopClasses;
    /**
     * 根据DICOM Part 4, B.3.1.4定义相关的通用SOP类
     * sop-classes-uid.properties
     */
    private URL sopClassesUID;
    /**
     * 扩展Sop类和传输语法的存储传输能力
     * sop-classes-tcs.properties
     */
    private URL sopClassesTCS;


    public Args(boolean bindCallingAet) {
        this(null, bindCallingAet, null, null);
    }

    /**
     * @param editors             修改DICOM属性的编辑器
     * @param negociation   扩展SOP类
     * @param sopClasses SOP类扩展的配置文件
     */
    public Args(List<Editors> editors,
                boolean negociation,
                URL sopClasses) {
        this.editors = editors;
        this.negociation = negociation;
        this.sopClasses = sopClasses;
    }

    /**
     * @param option                  可选的高级参数(代理、身份验证、连接和TLS)
     * @param bindCallingAet          当为true时，它将设置侦听器DICOM节点的AET。只有匹配称为AETitle的请求将被接受。
     *                                如果为假，所有被调用的AETs将被接受
     * @param sopClassesTCS  获取包含传输功能(sopclass、role、transferSyntaxes)的文件的URL
     * @param acceptedCallingAETitles 可接受的呼叫aetitle的列表。空将接受所有aetitle
     */
    public Args(Option option,
                boolean bindCallingAet,
                URL sopClassesTCS,
                String... acceptedCallingAETitles) {
        this.bindCallingAet = bindCallingAet;
        this.sopClassesTCS = sopClassesTCS;
        this.acceptedCallingAETitles = null == acceptedCallingAETitles ? new String[0] : acceptedCallingAETitles;
        if (null == option && null != this.option) {
            this.option.setMaxOpsInvoked(15);
            this.option.setMaxOpsPerformed(15);
        }
    }

    public void configureConnect(AAssociateRQ aAssociateRQ, Connection remote, Node calledNode) {
        aAssociateRQ.setCalledAET(calledNode.getAet());
        if (identityRQ != null) {
            aAssociateRQ.setIdentityRQ(identityRQ);
        }
        remote.setHostname(calledNode.getHostname());
        remote.setPort(calledNode.getPort());
    }

    /**
     * 使用callingNode绑定连接
     *
     * @param connection  连接信息
     * @param callingNode 节点信息
     */
    public void configureBind(Connection connection, Node callingNode) {
        if (callingNode.getHostname() != null) {
            connection.setHostname(callingNode.getHostname());
        }
        if (callingNode.getPort() != null) {
            connection.setPort(callingNode.getPort());
        }
    }

    public void configureBind(AAssociateRQ aAssociateRQ,
                              Connection remote,
                              Node calledNode) {
        aAssociateRQ.setCalledAET(calledNode.getAet());
        if (null != identityRQ) {
            aAssociateRQ.setIdentityRQ(identityRQ);
        }
        if (null != identityAC) {
            aAssociateRQ.setIdentityAC(identityAC);
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
    public void configureBind(ApplicationEntity applicationEntity, Connection connection, Node callingNode) {
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
        if (option != null) {
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
        if (option != null) {
            if (option.getCipherSuites() != null) {
                conn.setTlsCipherSuites(option.getCipherSuites().toArray(new String[0]));
            }
            if (option.getTlsProtocols() != null) {
                conn.setTlsProtocols(option.getTlsProtocols().toArray(new String[0]));
            }
            conn.setTlsNeedClientAuth(option.isTlsNeedClientAuth());

            Device device = conn.getDevice();
            try {
                device.setKeyManager(TrustAnyTrustManager.createKeyManager(option.getKeystoreType(), option.getKeystoreURL(), option.getKeystorePass(), option.getKeyPass()));
                device.setTrustManager(TrustAnyTrustManager.createTrustManager(option.getTruststoreType(), option.getTruststoreURL(), option.getTruststorePass()));
                if (remote != null) {
                    remote.setTlsProtocols(conn.getTlsProtocols());
                    remote.setTlsCipherSuites(conn.getTlsCipherSuites());
                }
            } catch (GeneralSecurityException e) {
                throw new IOException(e);
            }
        }
    }

    public boolean hasEditors() {
        return editors != null && !editors.isEmpty();
    }

}
