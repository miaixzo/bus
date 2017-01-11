var quartz = {}	;
quartz.dialog = null;

/**
 * 选择Kettle资源库信息
 * 加载JOB、trans等相关内容
 * @return void
 */
quartz.choose = function(){
	var dlg = new Dialog({
	    type:'iframe',
	    value:ctx+'/Widget/Quartz/Tree'
	  },{
		title:$.i18n.get('org.ukettle.iQuartz.Verify.Quartz.Schedule.Executor'),
		closeText : '<i class="icon-remove"></i>',
	    width:'460px',
	    height:'500px'
	  });
	quartz.dialog = dlg;
	dlg.show();
};
/**
 * 选择定时任务执行者
 * @return void
 */
quartz.exec = function(path,clazz){
    $("#executor").val(path+'.'+clazz);
    quartz.close();
};
/**关闭弹出窗口*/
quartz.close =  function() {
	quartz.dialog.close();
};
/**新增-添加参数信息*/
quartz.insert = function(id,value) {
	$('#params').append('<p id="id_'+id+'"><input type="text" class="text-small" readOnly id="argKey" value="'+id+'" /><input type="text" class="text-long" id="argVal" name="'+id+'" value="'+value+'"/><a href="javascript:void(0);" onclick=quartz.remove("id_'+id+'")><i class="icon-remove-sign"></i></a></p>');
};
/**更新-添加参数信息*/
quartz.update = function(id,value) {
	$('#params').append('<p id="id_'+id+'"><input type="text" class="text-small" readOnly id="argKey" value="'+id+'" /><input type="text" class="text-long" id="argVal" name="'+id+'" value="'+value+'"/><a href="javascript:void(0);" onclick=quartz.remove("id_'+id+'")><i class="icon-remove-sign"></i></a></p>');
};
/**查看-添加参数信息*/
quartz.view = function(id,value) {
	$('#params').append('<p><input type="text" class="text-small" readOnly id="argKey" value="'+id+'" readOnly/><input type="text" class="text-long" id="argVal" name="'+id+'" value="'+value+'" readOnly/></p>');
};
/**新增-添加参数信息*/
var i = 1;
quartz.add = function() {
	$('#params').append('<p id="id_'+i+'"><input type="text" class="text-small" id="argKey" /><input type="text" class="text-long" id="argVal" /><a href="javascript:void(0);" onclick=quartz.remove("id_'+i+'")><i class="icon-remove-sign"></i></a></p>');
	i++;
};
/**删除参数信息*/
quartz.remove = function(id) {
	$("#" + id).remove();
};
/**
 * 选择定时任务类型
 * @return void
 */
quartz.trigger = function() {
	var triggerType = $("input[name='triggerType']:checked").val();
	if (triggerType == 'simple') {
		$("#simpleTrigger").show();
		$("#cronTrigger").hide();
	} else {
		$("#simpleTrigger").hide();
		$("#cronTrigger").show();
	}
};
/**检查名称*/
quartz.exists = function() {
	var group = $.trim($("#group").val());
	if (!group) {
		alert($.i18n.get('org.ukettle.iQuartz.Verify.Quartz.Schedule.Group.Choose'));
		return false;
	}
	var title = $.trim($("#title").val());
    if(!title || !plugin.isWidthChar(title)){
      alert($.i18n.get('org.ukettle.iQuartz.Verify.Quartz.Schedule.Title'));
	  return false;  
    }
    $.post(ctx+"/Widget/Quartz/exists?title="+title, function(data) {
    	if(data =='1'){
    	      alert($.i18n.get('org.ukettle.iQuartz.Verify.Quartz.Schedule.Title.Exist'));
    		  return false;  
    	}
	});
};
$(function() {
	var json = $.parseJSON($("#jsons").val());
    for (var i in json){
       for(var j in json[i]){
           if ($(j)){
               var key = $(j).selector;
               var value = json[i][j];
               if(key=="method"){
            	   $("#argKey").val(key);
            	   $("#argVal").val(value);
               }else if(key =="dir"){
            	   $("#dir").val(value);
               }else if(key =="type"){
            	   $("#type").val(value);
               }else if(key =="isAsync"){
            	   $("#isAsync").val(value);
               }else if(key =="repo"){
            	   $("#repo").val(value);
               }else if(key =="logging"){
            	   $("#logging").val(value);
               }else if(key != "id" && key !="description"){
            	   quartz.insert(key,value);
               }
           }
       }
    } 
    
	$("#iQuartz").click(function() {
		if (!$.trim($("#group").val())) {
			alert($.i18n.get('org.ukettle.iQuartz.Verify.Quartz.Schedule.Group.Choose'));
			return false;
		}
		var title = $("#title").val();
	    if(!$.trim(title) || !plugin.isWidthChar(title)){
	      alert($.i18n.get('org.ukettle.iQuartz.Verify.Quartz.Schedule.Title'));
		  return false;  
	    }
	    $.ajax({
	        url: ctx+'/Widget/Quartz/Existing?title='+title,
	        type: 'POST',
	        dataType: 'json',
	        async:true,
	        success: function(data){
	        	if(data =='1'){
	      	      alert($.i18n.get('org.ukettle.iQuartz.Verify.Quartz.Schedule.Title.Exist'));
	      		  return false;
	        	}
	        	var executor = $("#executor").val();
	    	    if(!$.trim(executor)){
	    	      alert($.i18n.get('org.ukettle.iQuartz.Verify.Quartz.Schedule.Executor.Choose'));
	    		  return false;  
	    	    }
	        	var argK = $("input[id='argKey']"), argV = $("input[id='argVal']");
    			for (i = 0; i < argK.length; i++) {
    				var argKey = $.trim(argK[i].value);
    				var argVal = $.trim(argV[i].value);
    				argV.eq(i).attr('name',argKey);
    				if (!argVal) {
    					alert($.i18n.get('org.ukettle.iQuartz.Verify.Quartz.Schedule.Parameters.A') +" ‘" + argKey +"’ " + $.i18n.get('org.ukettle.iQuartz.Verify.Quartz.Schedule.Parameters.B'));
    					return false;
    				}
    			}
	    		$('#formId').submit();
	        }
	    });
	});
	quartz.trigger();
	$("#iSearch").click(function() {
		var title = $("#title").val();
		var group = $("#group").val();
	    if(!$.trim(title) && !$.trim(group)){
	      alert($.i18n.get('org.ukettle.iQuartz.Verify.Quartz.Schedule.Search'));
		  return false;  
	    }
	});
});