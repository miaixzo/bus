package org.aoju.bus.swagger;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.service.AllowableListValues;
import springfox.documentation.service.AllowableValues;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.EnumTypeDeterminer;
import springfox.documentation.spi.service.ExpandedParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterExpansionContext;
import springfox.documentation.spring.web.DescriptionResolver;
import springfox.documentation.swagger.common.SwaggerPluginSupport;
import springfox.documentation.swagger.readers.parameter.Examples;
import springfox.documentation.swagger.schema.ApiModelProperties;

import java.util.Arrays;
import java.util.List;

/**
 * 修正源码query传入请求参数postion无效
 * 这里，将postion赋值给order
 *
 * @author Kimi Liu
 * @version 5.1.0
 * @since JDK 1.8+
 */
@Primary
@Component
public class SwaggerParameterBuilder implements ExpandedParameterBuilderPlugin {

    private final DescriptionResolver descriptions;
    private final EnumTypeDeterminer enumTypeDeterminer;

    @Autowired
    public SwaggerParameterBuilder(
            DescriptionResolver descriptions,
            EnumTypeDeterminer enumTypeDeterminer) {
        this.descriptions = descriptions;
        this.enumTypeDeterminer = enumTypeDeterminer;
    }

    @Override
    public void apply(ParameterExpansionContext context) {
        Optional<ApiModelProperty> apiModelPropertyOptional = context.findAnnotation(ApiModelProperty.class);
        if (apiModelPropertyOptional.isPresent()) {
            fromApiModelProperty(context, apiModelPropertyOptional.get());
        }
        Optional<ApiParam> apiParamOptional = context.findAnnotation(ApiParam.class);
        if (apiParamOptional.isPresent()) {
            fromApiParam(context, apiParamOptional.get());
        }
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return SwaggerPluginSupport.pluginDoesApply(delimiter);
    }

    private void fromApiParam(ParameterExpansionContext context, ApiParam apiParam) {
        String allowableProperty = Strings.emptyToNull(apiParam.allowableValues());
        AllowableValues allowable = allowableValues(
                Optional.fromNullable(allowableProperty),
                context.getFieldType().getErasedType());

        maybeSetParameterName(context, apiParam.name())
                .description(descriptions.resolve(apiParam.value()))
                .defaultValue(apiParam.defaultValue())
                .required(apiParam.required())
                .allowMultiple(apiParam.allowMultiple())
                .allowableValues(allowable)
                .parameterAccess(apiParam.access())
                .hidden(apiParam.hidden())
                .scalarExample(apiParam.example())
                .complexExamples(Examples.examples(apiParam.examples()))
                .order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
                .build();
    }

    private void fromApiModelProperty(ParameterExpansionContext context, ApiModelProperty apiModelProperty) {
        String allowableProperty = Strings.emptyToNull(apiModelProperty.allowableValues());
        AllowableValues allowable = allowableValues(
                Optional.fromNullable(allowableProperty),
                context.getFieldType().getErasedType());

        maybeSetParameterName(context, apiModelProperty.name())
                .description(descriptions.resolve(apiModelProperty.value()))
                .required(apiModelProperty.required())
                .allowableValues(allowable)
                .parameterAccess(apiModelProperty.access())
                .hidden(apiModelProperty.hidden())
                .scalarExample(apiModelProperty.example())
                .order(apiModelProperty.position()) //源码这里是: SWAGGER_PLUGIN_ORDER，需要修正
                .build();
    }

    private ParameterBuilder maybeSetParameterName(ParameterExpansionContext context, String parameterName) {
        if (!Strings.isNullOrEmpty(parameterName)) {
            context.getParameterBuilder().name(parameterName);
        }
        return context.getParameterBuilder();
    }

    private AllowableValues allowableValues(final Optional<String> optionalAllowable, Class<?> fieldType) {

        AllowableValues allowable = null;
        if (enumTypeDeterminer.isEnum(fieldType)) {
            allowable = new AllowableListValues(getEnumValues(fieldType), "LIST");
        } else if (optionalAllowable.isPresent()) {
            allowable = ApiModelProperties.allowableValueFromString(optionalAllowable.get());
        }
        return allowable;
    }

    private List<String> getEnumValues(final Class<?> subject) {
        return Lists.transform(Arrays.asList(subject.getEnumConstants()), new Function<Object, String>() {
            @Override
            public String apply(final Object input) {
                return input.toString();
            }
        });
    }
}
