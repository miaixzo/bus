/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.metric.manual;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.crypto.Builder;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.metric.ApiContext;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 负责校验,校验工作都在这里
 *
 * @author Kimi Liu
 * @version 6.0.5
 * @since JDK 1.8++
 */
public class ApiValidator implements Validator {

    private static final int MILLISECOND_OF_ONE_SECOND = 1000;

    private static final String LEFT_TOKEN = "{";
    private static final String RIGHT_TOKEN = "}";
    private static final List<String> SYSTEM_PACKAGE_LIST = Arrays.asList("java.lang", "java.math", "java.util", "sun.util");
    private static List<String> FORMAT_LIST = Arrays.asList("json", "xml");
    private static Object[] EMPTY_OBJ_ARRAY = {};
    private static ValidatorFactory factory;
    private static javax.validation.Validator validator;

    static {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    public static javax.validation.Validator getValidator() {
        return validator;
    }

    public static void setValidator(javax.validation.Validator validator) {
        ApiValidator.validator = validator;
    }

    @Override
    public void validate(ApiParam param) {
        if (ApiContext.getConfig().isIgnoreValidate() || param.fatchIgnoreValidate()) {
            Logger.debug("忽略所有验证(ignoreValidate=true), name:{}, version:{}", param.fatchName(), param.fatchVersion());
            return;
        }
        Assert.notNull(ApiContext.getConfig().getAppSecretManager(), "appSecretManager未初始化");

        if (param.fatchIgnoreSign() || ApiContext.isEncryptMode()) {
            Logger.debug("忽略签名验证, name:{}, version:{}", param.fatchName(), param.fatchVersion());
        } else {
            // 需要验证签名
            checkAppKey(param);
            checkSign(param);
        }
        checkUploadFile(param);
        checkTimeout(param);
        checkFormat(param);
    }

    /**
     * 校验上传文件内容
     *
     * @param param 参数
     */
    protected void checkUploadFile(ApiParam param) {
        Upload upload = ApiContext.getUploadContext();
        if (upload != null) {
            try {
                List<MultipartFile> files = upload.getAllFile();
                for (MultipartFile file : files) {
                    // 客户端传来的文件md5
                    String clientMd5 = param.getString(file.getName());
                    if (clientMd5 != null) {
                        String fileMd5 = Builder.md5Hex(file.getBytes());
                        if (!clientMd5.equals(fileMd5)) {
                            throw Errors.ERROR_UPLOAD_FILE.getException();
                        }
                    }
                }
            } catch (IOException e) {
                Logger.error("验证上传文件MD5错误", e);
                throw Errors.ERROR_UPLOAD_FILE.getException();
            }
        }

    }

    protected void checkTimeout(ApiParam param) {
        int timeoutSeconds = ApiContext.getConfig().getTimeoutSeconds();
        // 如果设置为0，表示不校验
        if (timeoutSeconds == 0) {
            return;
        }
        if (timeoutSeconds < 0) {
            throw new IllegalArgumentException("服务端timeoutSeconds设置错误");
        }
        String requestTime = param.fatchTimestamp();
        try {
            Date requestDate = new SimpleDateFormat(ParamNames.TIMESTAMP_PATTERN).parse(requestTime);
            long requestMilliseconds = requestDate.getTime();
            if (System.currentTimeMillis() - requestMilliseconds > timeoutSeconds * MILLISECOND_OF_ONE_SECOND) {
                throw Errors.TIMEOUT.getException(param.fatchNameVersion(), timeoutSeconds);
            }
        } catch (ParseException e) {
            throw Errors.TIME_INVALID.getException(param.fatchNameVersion());
        }
    }

    protected void checkAppKey(ApiParam param) {
        Assert.notNull(ApiContext.getConfig().getAppSecretManager(), "appSecretManager未初始化");
        if (StringUtils.isEmpty(param.fatchAppKey())) {
            throw Errors.NO_APP_ID.getException(param.fatchNameVersion(), ParamNames.APP_KEY_NAME);
        }
        boolean isTrueAppKey = ApiContext.getConfig().getAppSecretManager().isValidAppKey(param.fatchAppKey());
        if (!isTrueAppKey) {
            throw Errors.ERROR_APP_ID.getException(param.fatchNameVersion(), ParamNames.APP_KEY_NAME);
        }
    }

    protected void checkSign(ApiParam param) {
        if (StringUtils.isEmpty(param.fatchSign())) {
            throw Errors.NO_SIGN_PARAM.getException(param.fatchNameVersion(), ParamNames.SIGN_NAME);
        }
        String secret = ApiContext.getConfig().getAppSecretManager().getSecret(param.fatchAppKey());

        Signer signer = ApiContext.getConfig().getSigner();
        boolean isRightSign = signer.isRightSign(param, secret, param.fatchSignMethod());
        // 错误的sign
        if (!isRightSign) {
            throw Errors.ERROR_SIGN.getException(param.fatchNameVersion());
        }
    }

    protected void checkFormat(ApiParam param) {
        String format = param.fatchFormat();
        boolean contains = FORMAT_LIST.contains(format.toLowerCase());

        if (!contains) {
            throw Errors.NO_FORMATTER.getException(param.fatchNameVersion(), format);
        }
    }

    @Override
    public void validateBusiParam(Object obj) {
        if (obj == null) {
            return;
        }
        // 先校验属性对象
        List<Object> fields = listObjectField(obj);
        if (!fields.isEmpty()) {
            fields.forEach(this::validateBusiParam);
        }
        Set<ConstraintViolation<Object>> set = validator.validate(obj);
        if (CollKit.isNotEmpty(set)) {
            ConstraintViolation<Object> oneError = set.iterator().next();
            String errorMsg = oneError.getMessage();
            throw this.getValidateBusiParamException(errorMsg);
        }
    }

    private List<Object> listObjectField(Object object) {
        List<Object> ret = new ArrayList<>();
        ReflectionUtils.doWithFields(object.getClass(), field -> {
            ReflectionUtils.makeAccessible(field);
            ret.add(field.get(object));
        }, this::isMatchField);
        return ret;
    }

    /**
     * 匹配校验字段。
     * <p>
     * 1. 不为基本类型；
     * 2. 不为java自带的类型；
     * 3. 不为枚举
     * 4. 不为Map
     *
     * @param field field
     * @return true，是自定义的
     */
    private boolean isMatchField(Field field) {
        Class<?> fieldType = field.getType();
        if (fieldType.isPrimitive()) {
            return false;
        }
        if (Map.class.isAssignableFrom(fieldType)) {
            return false;
        }
        Class<?> declaringClass = field.getDeclaringClass();
        boolean isSame = declaringClass == fieldType;
        boolean isAssignableFrom = declaringClass.isAssignableFrom(fieldType)
                || fieldType.isAssignableFrom(declaringClass);
        // 如果是相同类，或者有继承关系不校验。
        if (isSame || isAssignableFrom) {
            return false;
        }
        Package aPackage = fieldType.getPackage();
        if (aPackage == null) {
            return false;
        }
        String packageName = aPackage.getName();
        for (String prefix : SYSTEM_PACKAGE_LIST) {
            if (packageName.startsWith(prefix)) {
                return false;
            }
        }
        return true;
    }

    private RuntimeException getValidateBusiParamException(String errorMsg) {
        String code = Errors.BUSI_PARAM_ERROR.getCode();
        String[] msgToken = errorMsg.split("=");
        String msg = msgToken[0];
        if (msg.startsWith(LEFT_TOKEN) && msg.endsWith(RIGHT_TOKEN)) {
            String module = msg.substring(1, msg.length() - 1);
            Object[] params = this.buildParams(msgToken);
            String error = ErrorFactory.getErrorMessage(module, ApiContext.getLocal(), params);
            return new InstrumentException(code, error);
        } else {
            return new InstrumentException(code, errorMsg);
        }
    }

    private Object[] buildParams(String[] msgToken) {
        if (msgToken.length == 2) {
            return msgToken[1].split(Symbol.COMMA);
        } else {
            return EMPTY_OBJ_ARRAY;
        }
    }

}
