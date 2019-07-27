package org.aoju.bus.base.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Id;
import java.io.Serializable;


/**
 * <p>
 * Entity 实体
 * </p>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Data
public abstract class Entity implements Serializable {

    private static final long serialVersionUID = -621369123580520198L;

    @Id
    @ApiModelProperty("主键")
    protected String id;

}
