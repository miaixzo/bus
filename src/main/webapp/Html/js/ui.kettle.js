var kettle = {};
kettle.dialog = null;

/**重新加载指定资源库*/
kettle.loading = function(name){
	$.post(ctx+"/Widget/Kettle/Repos/Loading?name="+name, function(data) {
	}).success(function(data) { 
		alert($.i18n.get('org.ukettle.iQuartz.Verify.Success'));
	}).error(function(data) {
		alert($.i18n.get('org.ukettle.iQuartz.Verify.Error'));
	});
};
/**断开指定资源库*/
kettle.discard = function (name){
	$.post(ctx+"/Widget/Kettle/Repos/Discard?name="+name, function(data) {
	}).success(function(data) { 
		alert($.i18n.get('org.ukettle.iQuartz.Verify.Success'));
	}).error(function(data) {
		alert($.i18n.get('org.ukettle.iQuartz.Verify.Error'));
	});
};
/**启用、禁用指定资源库*/
kettle.status = function(id,status){
	$.post(ctx+"/Widget/Kettle/Repos/Update?id="+id+"&status="+status, function(data) {
	}).success(function(data) { 
		alert($.i18n.get('org.ukettle.iQuartz.Verify.Success'));
		location.reload();
	}).error(function(data) {
		alert($.i18n.get('org.ukettle.iQuartz.Verify.Error'));
	});
};
/**
 * 资源库树形结构
 */
kettle.choose = function(){
	var title = $.trim($("#repository").val());
	if (!title) {
		alert($.i18n.get('org.ukettle.iQuartz.Verify.Kettle.Repository.Choose'));
		return false;
	}
	var dlg = new Dialog({
	    type:'iframe',
	    value:ctx +'/Widget/Kettle/Spoon/Tree'
	  },{
		title:$.i18n.get('org.ukettle.iQuartz.Verify.Kettle.Repository'),
		closeText : '<i class="icon-remove"></i>',
	    width:'460px',
	    height:'390px'
	  });
	kettle.dialog = dlg;
	dlg.show();
};
/**关闭弹出窗口*/
kettle.close = function() {
	kettle.dialog.close();
};
/**获取仓库名称*/
kettle.repo = function(){
	return $.trim($("#repository").val());
};
/**
 * 设置所加在JOB、Trasn相关参数
 */
kettle.get = function(repo,dir,method,type){
	$("#dir").val(dir);
    $("#argKey").val("method");
    $("#argVal").val(method);
    $("#type").val(type);
    $.post(ctx+"/Widget/Kettle/Spoon/get?repo="+repo+"&dir="+dir+"&method="+method+"&type="+type,function(json){
    	$('#params').find("p[id^=id_]").remove();
		var json = $.parseJSON(json);
	    for (var i in json){
	       for(var j in json[i]){
	           if ($(j)){
	               var id = $(j).selector;
	               var val = json[i][j];
	               kettle.insert(id,val);
	           }
	       }
		}
		$("#iKettle").attr("disabled", false);
	});
    kettle.close();
};

/**新增-添加参数信息*/
kettle.insert = function(id,val) {
	$('#params').append('<p id="id_'+id+'"><input type="text" class="text-small" readOnly id="argKey" value="'+id+'" /><input type="text" class="text-long" id="argVal" name="'+id+'" value="'+val+'" /><a href="javascript:void(0);" onclick=kettle.remove("id_'+id+'")><i class="icon-remove-sign"></i></a></p>');
};
/**更新-添加参数信息*/
kettle.update = function(id,val) {
	$('#params').append('<p id="id_'+id+'"><input type="text" class="text-small" readOnly id="argKey" value="'+id+'" /><input type="text" class="text-long" id="argVal" name="'+id+'" value="'+val+'"/><a href="javascript:void(0);" onclick=kettle.remove("id_'+id+'")><i class="icon-remove-sign"></i></a></p>');
};
/**查看-添加参数信息*/
kettle.view  = function(id,val) {
	$('#params').append('<p><input type="text" class="text-small" readOnly id="argKey" value="'+id+'" /><input type="text" class="text-long" id="argVal" readOnly name="'+id+'" value="'+val+'" /></p>');
};
/**删除参数信息*/
kettle.remove = function (id) {
	$("#" + id).remove();
};
/**加载相关输出信息*/
kettle.outs = function(id){
    var dlg = new Dialog({
	    type : 'url',
	    value : ctx+'/Widget/Kettle/Result/View?id='+id
	  },{
		title : $.i18n.get('org.ukettle.iQuartz.Verify.Kettle.Monitor.Out'),
		closeText : '<i class="icon-remove"></i>',
	    width : '600px',
	    height : '200px'
	 });
	dlg.show();
};
/**加载log内容*/
kettle.logs = function(id){
    var dlg = new Dialog({
	    type : 'url',
	    value : ctx+'/Widget/Kettle/Result/Text?mid='+id
	  },{
		title : $.i18n.get('org.ukettle.iQuartz.Verify.Kettle.Monitor.Log'),
		closeText : '<i class="icon-remove"></i>',
	    width : '600px',
	    height : '390px',
	    timeOut : 500
	 });
	dlg.show();
};
$(function(){
	$("#iKettle").click(function() {
		var repo = $.trim($("#repository").val());
		$("#rid").val($("#repository option:selected").attr("id"));
		if (!repo) {
			alert($.i18n.get('org.ukettle.iQuartz.Verify.Kettle.Repository.Choose'));
			return false;
		}
    	var argK = $("input[id='argKey']"), argV = $("input[id='argVal']");
		for (i = 0; i < argK.length; i++) {
			var argKey = $.trim(argK[i].value);
			var argVal = $.trim(argV[i].value);
			if (!argVal) {
				alert($.i18n.get('org.ukettle.iQuartz.Verify.Kettle.Repository.Parameters.A')+" ‘" + argKey +"’ " + $.i18n.get('org.ukettle.iQuartz.Verify.Kettle.Repository.Parameters.B'));
				return false;
			}
		}
		$('#formId').submit();
	});
	
	$("#iSyanc").click(function() {
		var repo = $.trim($("#repository").val());
		if (!repo) {
			alert($.i18n.get('org.ukettle.iQuartz.Verify.Kettle.Repository.Choose'));
			return false;
		}
		$('#formId').submit();
	});
});