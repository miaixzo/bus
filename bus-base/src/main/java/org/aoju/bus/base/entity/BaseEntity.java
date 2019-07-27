package org.aoju.bus.base.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * <p>
 * Entity 基本信息.
 * </p>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@MappedSuperclass
@Data
public class BaseEntity extends OAuth2 {

    private static final long serialVersionUID = -601369123580520198L;

    @ApiModelProperty(value = "状态", notes = "-1删除,0无效,1正常")
    protected String status;

    @ApiModelProperty("创建人")
    protected String creator;

    @ApiModelProperty("创建时间")
    protected String created;

    @ApiModelProperty("修改人")
    protected String modifier;

    @ApiModelProperty("修改时间")
    protected String modified;

    @Transient
    @ApiModelProperty(value = "页码", notes = "默认值:1")
    protected Integer pageNo = 1;

    @Transient
    @ApiModelProperty(value = "分页大小", notes = "默认值:20")
    protected Integer pageSize = 20;

}