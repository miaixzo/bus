/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.image;

import lombok.Data;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.image.galaxy.data.Attributes;
import org.aoju.bus.image.metric.Progress;

import java.util.ArrayList;
import java.util.List;

/**
 * 相关处理状态
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Data
public class Status {

    public static final int Success = 0x0000;

    public static final int Pending = 0xFF00;
    public static final int PendingWarning = 0xFF01;

    public static final int Cancel = 0xFE00;

    /**
     * 失败:无此类属性(105H): 无法识别指定属性的标签
     * 在N-SET-RSP，N-CREATE-RSP中使用 可能包含:属性标识符列表(0000,1005)
     */
    public static final int NoSuchAttribute = 0x0105;

    /**
     * 失败:属性值无效(106H): 指定的属性值超出范围或不合适
     * 在N-SET-RSP，N-CREATE-RSP中使用
     * 可能包含:修改列表/属性列表(无标签)
     */
    public static final int InvalidAttributeValue = 0x0106;

    /**
     * 警告:属性列表错误(107H): 由于无法识别指定的属性
     * 因此未读取/修改/创建一个或多个属性值
     * 在N-GET-RSP，N-SET-RSP，N-CREATE-RSP 中使用可能包含:
     * 受影响的SOP类UID(0000,0002)
     * 受影响的SOP实例UID(0000,1000)
     * 属性标识符列表(0000,1005)
     */
    public static final int AttributeListError = 0x0107;

    /**
     * 失败:处理失败(110H): 在处理操作时遇到一般失败
     * 用于N-EVENT-REPORT-RSP，N-GET-RSP，N-SET-RSP，N-ACTION-RSP，N-CREATE-RSP，N-DELETE-RSP
     * 可能包含:
     * 受影响的SOP类UID(0000,0002)
     * 错误注释(0000,0902)
     * 错误ID(0000,0903)
     * 受影响的SOP实例UID(0000,1000)
     */
    public static final int ProcessingFailure = 0x0110;

    /**
     * 失败:重复的SOP实例(111H):
     * 由调用DIMSE-service-user提供的新的托管SOP实例值已经为指定SOP类的托管SOP实例注册
     * 在N-CREATE-RSP中使用可能包含:
     * 受影响的SOP实例UID(0000,1000)
     */
    public static final int DuplicateSOPinstance = 0x0111;

    /**
     * 失败:没有这样的SOP实例(112H):无法识别SOP实例
     * 用于N-EVENT-REPORT-RSP，N-SET-RSP，N-ACTION-RSP，N-DELETE-RSP
     * 可能包含:受影响的SOP实例UID(0000,1000)
     */
    public static final int NoSuchObjectInstance = 0x0112;

    /**
     * 失败:没有这样的事件类型(113H):指定的事件类型无法识别
     * 用于N-EVENT-REPORT-RSP
     * 可能包含:
     * 受影响的SOP类UID(0000,0002)
     * 事件类型ID (0000,1002)
     */
    public static final int NoSuchEventType = 0x0113;

    /**
     * 失败:没有这样的参数(114H): 指定的事件/动作信息
     * 没有得到认可/支持
     * 用于n事件-报告- rsp, n行动- rsp
     * 可能包含:
     * 受影响的SOP类UID (0000,0002)
     * 事件类型ID (0000,1002)
     * 动作类型ID (0000,1008)
     */
    public static final int NoSuchArgument = 0x0114;

    /**
     * 失败:无效参数值(115H): 事件/动作信息
     * 指定的值超出范围或不合适
     * 用于n事件-报告- rsp, n行动- rsp
     * 可能包含:
     * 受影响的SOP类UID (0000,0002)
     * 受影响的SOP实例UID (0000,1000)
     * 事件类型ID (0000,1002)
     * 事件信息(无标签)
     * 动作类型ID (0000,1008)
     * 动作信息(无标签)
     */
    public static final int InvalidArgumentValue = 0x0115;

    /**
     * 警告:属性值超出范围(116H):属性值
     * 指定超出范围或不合适
     * 用于N-SET-RSP, N-CREATE-RSP
     * 可能包含:
     * 修改列表/属性列表
     */
    public static final int AttributeValueOutOfRange = 0x0116;

    /**
     * 失败:无效的SOP实例(117H): 指定的SOP实例UID暗示违反了UID构造规则
     * 用于N-EVENT-REPORT-RSP, N-GET-RSP, N-SET-RSP, N-ACTION-RSP，
     * N-CREATE-RSP N-DELETE-RSP
     * 可能包含:受影响的SOP实例UID (0000,1000)
     */
    public static final int InvalidObjectInstance = 0x0117;

    /**
     * 失败:没有SOP类(118H): SOP类不被认可
     * 用于N-EVENT-REPORT-RSP, N-GET-RSP, N-SET-RSP, N-ACTION-RSP，
     * N-CREATE-RSP N-DELETE-RSP
     * 可能包含:
     * 受影响的SOP类UID (0000,0002)
     */
    public static final int NoSuchSOPclass = 0x0118;

    /**
     * 失败:类实例冲突(119H):指定的SOP实例为不是指定SOP类的成员
     * 用于N-EVENT-REPORT-RSP, N-GET-RSP, N-SET-RSP, N-ACTION-RSP，
     * N-DELETE-RSP
     * 可能包含:
     * 受影响的SOP类UID (0000,0002)
     * 受影响的SOP实例UID (0000,1000)
     */
    public static final int ClassInstanceConflict = 0x0119;

    /**
     * 失败:缺少属性(120H):没有提供必需的属性
     * 用于N-CREATE-RSP
     * 可能包含:
     * 修改列表/属性列表(无标签)
     */
    public static final int MissingAttribute = 0x0120;

    /**
     * 失败:缺少属性值(121H):没有提供所需的属性值，默认值不可用
     * 用于N-SET-RSP, N-CREATE-RSP
     * 可能包含:
     * 属性标识符列表(0000,1005)
     */
    public static final int MissingAttributeValue = 0x0121;

    /**
     * 拒绝:不支持SOP类(112H)
     * 用于C-STORE-RSP, C-FIND-RSP, C-GET-RSP, C-MOVE-RSP
     * 可能包含:
     * 受影响的SOP类UID (0000,0002)
     */
    public static final int SOPclassNotSupported = 0x0122;

    /**
     * 失败:没有这样的动作类型(123H):不支持指定的动作类型
     * 用于N-ACTION-RSP
     * 可能包含:
     * 受影响的SOP类UID (0000,0002)
     * 动作类型ID (0000,1008)
     */
    public static final int NoSuchActionType = 0x0123;

    /**
     * 拒绝:未授权(124H): dimse -service用户未被授权调用操作
     * 用于C-STORE-RSP, C-FIND-RSP, C-GET-RSP, C-MOVE-RSP, N-GET-RSP，
     * N-SET-RSP, N-ACTION-RSP, N-CREATE-RSP， -DELETE-RSP
     * 可能包含:
     * 错误注释(0000,0902)
     */
    public static final int NotAuthorized = 0x0124;

    /**
     * 失败:重复调用(210H):消息ID (0000,0110)指定的是分配给另一个通知或操作
     * 用于C-STORE-RSP, C-FIND-RSP, C-GET-RSP, C-MOVE-RSP, c - echoc - rsp，
     * N-EVENT-REPORT-RSP, N-GET-RSP, N-SET-RSP, N-ACTION-RSP, N-CREATE-RSP，
     * N-DELETE-RSP
     */
    public static final int DuplicateInvocation = 0x0210;

    /**
     * 故障:无法识别的操作(211H):该操作不是dimse -service用户之间约定的操作之一
     * 用于C-STORE-RSP, C-FIND-RSP, C-GET-RSP, C-MOVE-RSP,
     * c - echoe - rsp, N-EVENT-REPORT-RSP， -GET-RSP, N-SET-RSP,
     * N-ACTION-RSP, N-CREATE-RSP, N-DELETE-RSP
     */
    public static final int UnrecognizedOperation = 0x0211;

    /**
     * 失败:参数输入错误(212H):所提供的参数之一未被同意用于dimse -service用户之间的关联
     * 用于N-EVENT-REPORT-RSP, N-GET-RSP, N-SET-RSP,
     * N-ACTION-RSP, N-CREATE-RSP, N-DELETE-RSP
     */
    public static final int MistypedArgument = 0x0212;

    /**
     * 失败:资源限制(213H):由于资源限制，没有执行操作
     */
    public static final int ResourceLimitation = 0x0213;

    public static final int OutOfResources = 0xA700;
    public static final int UnableToCalculateNumberOfMatches = 0xA701;
    public static final int UnableToPerformSubOperations = 0xA702;
    public static final int MoveDestinationUnknown = 0xA801;
    public static final int IdentifierDoesNotMatchSOPClass = 0xA900;
    public static final int DataSetDoesNotMatchSOPClassError = 0xA900;

    public static final int OneOrMoreFailures = 0xB000;
    public static final int CoercionOfDataElements = 0xB000;
    public static final int ElementsDiscarded = 0xB006;
    public static final int DataSetDoesNotMatchSOPClassWarning = 0xB007;

    public static final int UnableToProcess = 0xC000;
    public static final int CannotUnderstand = 0xC000;

    private final Progress progress;
    private final List<Attributes> list;
    private volatile int status;
    private String message;

    public Status() {
        this(Pending, null, null);
    }

    public Status(Progress progress) {
        this(Pending, null, progress);
    }

    public Status(int status, String message, Progress progress) {
        this.status = status;
        this.message = message;
        this.progress = progress;
        this.list = new ArrayList<>();
    }

    public static Status build(Status dcmState, String timeMessage, Exception e) {
        Status state = dcmState;
        if (null == state) {
            state = new Status(Status.UnableToProcess, null, null);
        }

        Progress p = state.getProgress();
        int s = state.getStatus();

        StringBuilder msg = new StringBuilder();

        boolean hasFailed = false;
        if (null != p) {
            int failed = p.getNumberOfFailedSuboperations();
            int warning = p.getNumberOfWarningSuboperations();
            int remaining = p.getNumberOfRemainingSuboperations();
            if (failed > 0) {
                hasFailed = true;
                msg.append(String.format("%d/%d operations has failed.", failed,
                        failed + p.getNumberOfCompletedSuboperations()));
            } else if (remaining > 0) {
                msg.append(String.format("%d operations remains. ", remaining));
            } else if (warning > 0) {
                msg.append(String.format("%d operations has a warning status. ", warning));
            }
        }
        if (null != e) {
            hasFailed = true;
            if (msg.length() > 0) {
                msg.append(Symbol.SPACE);
            }
            msg.append(e.getLocalizedMessage());
        }

        if (null != p && null != p.getAttributes()) {
            String error = p.getErrorComment();
            if (StringKit.hasText(error)) {
                hasFailed = true;
                if (msg.length() > 0) {
                    msg.append(Symbol.LF);
                }
                msg.append("DICOM error : ");
                msg.append(error);
            }

            if (!Status.isPending(s) && s != -1 && s != Status.Success && s != Status.Cancel) {
                if (msg.length() > 0) {
                    msg.append(Symbol.LF);
                }
                msg.append("DICOM status : ");
                msg.append(s);
            }
        }

        if (!hasFailed) {
            if (null != timeMessage) {
                msg.append(timeMessage);
            }
        } else {
            if (Status.isPending(s) || s == -1) {
                state.setStatus(Status.UnableToProcess);
            }
        }
        state.setMessage(msg.toString());
        return state;
    }

    public static boolean isPending(int status) {
        return (status & Pending) == Pending;
    }

    public int getStatus() {
        if (null != progress && null != progress.getAttributes()) {
            return progress.getStatus();
        }
        return status;
    }

    public void setList(Attributes attributes) {
        if (null != attributes) {
            this.list.add(attributes);
        }
    }

}
