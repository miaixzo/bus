package org.aoju.bus.swagger;

import io.swagger.models.parameters.Parameter;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2MapperImpl;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 重写 将Document转换成Swagger 类, 根据order进行排序
 *
 * @author Kimi Liu
 * @version 3.2.8
 * @since JDK 1.8
 */
@Primary
@Component("ServiceModelToSwagger2Mapper")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ModelToSwaggerMapper extends ServiceModelToSwagger2MapperImpl {

    @Override
    protected List<Parameter> parameterListToParameterList(List<springfox.documentation.service.Parameter> list) {
        //list需要根据order|postion排序
        list = list.stream().sorted((p1, p2) -> Integer.compare(p1.getOrder(), p2.getOrder())).collect(Collectors.toList());
        return super.parameterListToParameterList(list);
    }
}