<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@ include file="../../../Comm/tags.jsp"%>
<!DOCTYPE html>
<html>
<head>
<title><spring:message code="com.ukettle.Title" /></title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
<%@ include file="../../../Comm/resource.jsp"%>
</head>
<body style="background:#fff;overflow-x: hidden; overflow-y: auto;border-radius: 0 0 3px 3px">
<script type="text/javascript">
$(function () {
	$.fn.zTree.init($("#ztree"), setting, zNodes);
});

var repo = parent.kettle.repo();

var setting = {
	showLine: true,   
	expandSpeed: "slow",
	data : {
		key: {
			title:"alt"
		},
		simpleData: {
			enable: true
		}
	},
	check: {
        enable: false,
        chkStyle: "radio",
        radioType: "all"
    },
	async : {
		enable : true,
		url : "${ctx}/Widget/Kettle/Spoon/Tree?repo="+ repo,
		dataType : "text",
		dataFilter : ajaxFilter,
		autoParam : [ "id","type","dir"]
	},
	callback: {
		asyncError : iError,
		onClick: iClick
	}
};

var zNodes = [{
	id : '0',
	name : repo,
	open : true,
	click : true,
	isParent : true,
	icon : '${ctx}/Html/js/libs/zTree/img/repo.png',
	type : 'dir',
	dir : '/'
}];

function ajaxFilter(treeId, parentNode, data) {
	var array = [],isParent = true;
	var icon = '${ctx}/Html/js/libs/zTree/img/folder.png';
	for ( var i = 0; i < data.length; i++) {
		if(data[i].type=="job"){
			icon = '${ctx}/Html/js/libs/zTree/img/job.png';
		}else if(data[i].type =="transformation"){
			icon = '${ctx}/Html/js/libs/zTree/img/trans.png';
		}
		array[i] = {
			pId : data[i].pId,
			id : data[i].id,
			name : data[i].name,
			isParent : data[i].isParent,
			open : data[i].open,
			icon : icon,
			click : data[i].click,
			dir : data[i].dir,
			type : data[i].type
		};
	}
	return array;
}

function iClick(event, id, json, click) {
	if("dir" == json.type){
		return false;
	}
	parent.kettle.get(repo,json.dir,json.name,json.type);
}
function iError(event, id, json, click) {
	alert('load error!');
}
</script>
<ul id="ztree" class="ztree"></ul>
</body>
</html>