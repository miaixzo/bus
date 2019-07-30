package org.aoju.bus.base.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Transient;

/**
 * <p>
 * 授权公用类
 * </p>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Data
public class OAuth2 extends Entity {

    private static final long serialVersionUID = -611369123580520190L;

    @Transient
    @ApiModelProperty("当前用户ID")
    protected String x_user_id;

    @Transient
    @ApiModelProperty("当前用户名称")
    protected String x_user_name;

    @Transient
    @ApiModelProperty("当前用户工号")
    protected String x_user_code;

    @Transient
    @ApiModelProperty("当前用户角色ID")
    private String x_role_id;

    @Transient
    @ApiModelProperty("当前用户职称ID")
    private String x_duty_id;

    @Transient
    @ApiModelProperty("当前用户组织ID")
    private String x_org_id;

    @Transient
    @ApiModelProperty("可选参数信息")
    private String x_extract;

}
