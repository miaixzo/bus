package org.ukettle.system.mapper;

import org.springframework.stereotype.Repository;
import org.ukettle.basics.base.mapper.BaseMapper;


@Repository
public interface RestMapper<T extends Object> extends BaseMapper<T> {

}
