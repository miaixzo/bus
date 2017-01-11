package com.ukettle.system.mapper;

import org.springframework.stereotype.Repository;

import com.ukettle.basics.base.mapper.BaseMapper;


@Repository
public interface RestMapper<T extends Object> extends BaseMapper<T> {

}
