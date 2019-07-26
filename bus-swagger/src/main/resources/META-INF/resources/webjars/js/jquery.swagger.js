$.views.settings.allowCode(true);
$.views.converters("getResponseModelName", function (val) {
    return getResponseModelName(val);
});

Date.prototype.Format = function (fmt) {
    var o = {
        "M+": this.getMonth() + 1, //月份
        "d+": this.getDate(), //日
        "h+": this.getHours(), //小时
        "m+": this.getMinutes(), //分
        "s+": this.getSeconds(), //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S": this.getMilliseconds() //毫秒
    };
    if (/(y+)/.test(fmt)) fmt =
        fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt))
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ?
                (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
};

var tempBody = $.templates('#temp_body');
var tempBodyRefModel = $.templates('#temp_body_ref_model');
var tempBodyType = $.templates('#temp_body_type');

function getUrl(type) {
    var url = location.protocol + "//" + location.host;
    if (url.charAt(0) == "#") {
        url = url.substr(1);
    }
    if (type == 'api') {
        url = $("#iptApiUrl").val() || location.hash;

        url = url + "/v2/api-docs";
        if (location.search && location.search.indexOf("_ijt=") != -1) {
            url = "swagger.json";
        }
    }
    return url;
}

/**
 * 渲染ref类型参数
 * @param domId 需要添加的domId
 * @param jsonData
 * @param modelName
 */
function renderRefModel(domId, jsonData, modelName) {
    if (modelName) {
        var model = jsonData.definitions[modelName];
        model.name = modelName;
        model.domId = domId;
        //修改有嵌套对象的type
        $.each(model.properties, function (i, v) {
            if (v.items) {
                $.each(v.items, function (j, item) {
                    var typeModel = item.startsWith("#") ? getRefName(item) : item;
                    model.properties[i].type = "Array[" + typeModel + "]";
                });
            }

            //自定义对象类型（非Array）
            if (!v.type) {
                model.properties[i].type = getRefName(v["$ref"]);
            }
        });
        //如果该对象没有被渲染到页面，则渲染
        if ($("#ref-" + domId + "-" + modelName).length == 0) {
            $("#" + domId).append(tempBodyRefModel.render(model));
        }

        //递归渲染多层对象嵌套
        $.each(model.properties, function (i, v) {
            //Array
            if (v.items) {
                $.each(v.items, function (j, item) {

                    if (item.startsWith("#")) {
                        renderRefModel(domId, jsonData, getRefName(item));
                    }
                });
            }

            //单个对象引用
            if (v.hasOwnProperty("$ref")) {
                renderRefModel(domId, jsonData, getRefName(v["$ref"]));
            }

        });
    }
}

//获得模型名字
function getRefName(val) {
    if (!val) {
        return null;
    }
    return val.substring(val.lastIndexOf("/") + 1, val.length);
}

/**
 * 请求类型
 */
function changeParameterType(el) {
    var operationId = $(el).attr("operationId");
    var type = $(el).attr("type");
    $("#content_type_" + operationId).val(type);
    $(el).addClass("layui-btn-normal").removeClass("layui-btn-primary");
    if ("form" == type) {
        $("#text_tp_" + operationId).hide();
        $("#table_tp_" + operationId).show();
        $("#pt_json_" + operationId).addClass("layui-btn-primary").removeClass("layui-btn-normal");
    } else if ("json" == type) {
        $("#text_tp_" + operationId).show();
        $("#table_tp_" + operationId).hide();
        $("#pt_form_" + operationId).addClass("layui-btn-primary").removeClass("layui-btn-normal");
    }
}

/**
 * 请求日志
 * @param message message
 */
function write(message) {
    $('#response_get').append("<p>[" + new Date().Format('yyyy-MM-dd hh:mm:ss.S') + "] " + message + "</p>");
}

/**
 * 请求转发
 * @param url 地址
 * @param id id
 * @param header header
 * @param data data
 */
function route(url, id, header, data) {
    var type = $("[m_operationId='" + id + "']").attr("method");
    $("#response_get").html("");
    write('Send ' + type + ' request');
    //是否有formData类型数据
    var hasForm = $("[p_operationId='" + id + "'][in='formData']").length >= 1;
    //是否有body类型数据
    var hasBody = $("[p_operationId='" + id + "'][in='body']").length >= 1;
    var contentType = $("#consumes_" + id).text();
    var options = {withQuotes: true};
    //requestBody 请求
    if (hasForm) {
        data = new FormData($("#form_" + id)[0]);
    } else if (hasBody) {
        data = $("[p_operationId='" + id + "'][in='body']")[0].val();
    }
    //querystring ,将参数加在url后面
    url = appendParameterToUrl(url, data);
    write('Parameters' + JSON.stringify(data));
    //发送请求
    $.ajax({
        type: type,
        url: url,
        headers: header,
        data: type == 'get' ? '' : data,
        dataType: 'json',
        cache: hasForm ? false : true,
        processData: hasForm ? false : true,
        contentType: hasForm ? false : contentType,
        success: function (result) {
            write(type.toUpperCase() + ' ' + url);
            write('Result ：');
            $("#json-response").jsonViewer(result, options);

        },
        error: function (e) {
            $("#json-response").html("");
            layer.msg("" + JSON.stringify(e), {icon: 5});
        }
    });

}

/**
 * 给url拼装参数
 * @param url
 * @param parameter
 */
function appendParameterToUrl(url, parameter) {
    if ($.isEmptyObject(parameter)) {
        return url;
    }
    $.each(parameter, function (k, v) {
        if (url.indexOf("?") == -1) {
            url += "?";
        }
        url += k;
        url += "=";
        url += v;
        url += "&";
    });
    return url.substring(0, url.length - 1);
}

/**
 *
 * @param id operationId
 */
function fetching(operationId) {
    var path = getUrl('path') + $("[m_operationId='" + operationId + "']").attr("path");
    //path 参数
    $("[p_operationId='" + operationId + "'][in='path']").each(function (index, domEle) {
        var k = $(domEle).attr("name");
        var v = $(domEle).val();
        if (v) {
            path = path.replace("{" + k + "}", v);
        }
    });

    //header参数
    var headerJson = {};
    $("[p_operationId='" + operationId + "'][in='header']").each(function (index, domEle) {
        var k = $(domEle).attr("name");
        var v = $(domEle).val();
        if (v) {
            headerJson[k] = v;
        }
    });

    //请求方式
    var parameterType = $("#content_type_" + operationId).val();

    //query 参数
    var parameterJson = {};
    if ("form" == parameterType) {
        var isValid = false, valid = '';
        $("[p_operationId='" + operationId + "'][in='query']").each(function (index, domEle) {
            var r = $(domEle).attr("required");
            var k = $(domEle).attr("name");
            var v = $(domEle).val();
            if (!v && r == 'required') {
                isValid = true;
                valid = k;
                return false;
            } else if (v) {
                parameterJson[k] = v;
            }
        });
        if (isValid) {
            layer.msg("required field : " + valid, {icon: 5});
            return false;
        }
    } else if ("json" == parameterType) {
        var str = $("#text_tp_" + operationId).val();
        try {
            parameterJson = JSON.parse(str);
        } catch (error) {
            layer.msg("" + error, {icon: 5});
            return false;
        }
    }
    //发送请求
    route(path, operationId, headerJson, parameterJson);
}

$(function () {
    $.ajax({
        url: getUrl('api'),
        dataType: "json",
        type: "get",
        async: false,
        success: function (data) {
            //layui init
            layui.use(['layer', 'jquery', 'form', 'element'], function () {
                var $ = layui.jquery,
                    layer = layui.layer,
                    form = layui.form,
                    element = layui.element;
            });
            var jsonData = eval(data);

            $("#title").html(jsonData.info.title);
            $("body").html($("#template").render(jsonData));

            $("[name='a_path']").click(function () {
                var path = $(this).attr("path");
                var method = $(this).attr("method");
                var operationId = $(this).attr("operationId");
                $.each(jsonData.paths[path], function (i, item) {
                    if (item.operationId == operationId) {
                        item.path = path, item.method = method;
                        $("#path-body").html(tempBody.render(item));
                        //如果没有返回值，直接跳过
                        if (!item.responses["200"].hasOwnProperty("schema")) {
                            return true;
                        }
                        //基本类型
                        if (item.responses["200"]["schema"].hasOwnProperty("type")) {
                            var model = {"type": item.responses["200"]["schema"]["type"]};
                            $("#path-body-response-model").append(tempBodyType.render(model));
                            return true;
                        }

                        //引用类型
                        var modelName = getRefName(item.responses["200"]["schema"]["$ref"]);
                        if (item.parameters) {
                            $.each(item.parameters, function (i, ref) {
                                if (ref["schema"]) {
                                    var parameterModelName = getRefName(ref["schema"]["$ref"]);
                                    renderRefModel("path-body-request-model", jsonData, parameterModelName);
                                }
                            });
                        }
                        renderRefModel("path-body-response-model", jsonData, modelName);
                    }
                });
                layui.element.init();
                layui.form.on('select(api-quick)', function (data) {
                    data = data.value.split("::");
                    var $target = $(".layui-nav a[path='" + data[1] + "'][method='" + data[0] + "']");
                    if (!$target.parents('.layui-nav-item').hasClass('layui-nav-itemed')) {
                        $('.layui-nav-item').removeClass('layui-nav-itemed');
                        $target.parents('.layui-nav-item').addClass('layui-nav-itemed');
                    }
                    $target.click();
                });
                layui.form.render("select");
            });
        },
        error: function () {
            layer.msg('Failed to load, please confirm that the API document address is correct', {icon: 5});
        }
    });

});