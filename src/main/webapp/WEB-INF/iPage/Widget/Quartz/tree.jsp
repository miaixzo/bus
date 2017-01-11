<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@ include file="../../Comm/tags.jsp"%>
<!DOCTYPE html>
<html>
<head>
<title><spring:message code="org.ukettle.Title" /></title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
<%@ include file="../../Comm/resource.jsp"%>
<script src="${ctx}/Html/js/ui.quartz.js" type="text/javascript"></script>
<style type="text/css">
table td{
	border: 1px solid #999;
	color: #000;
	text-align: right;
	background: #f6f6f6;
	padding: 3px;
}
table th {
	background: #f0f7fa;
	text-align: left;
	padding: 4px;
}
</style>
</head>
<body style="overflow-x: hidden; overflow-y: auto;margin:3px;background:#fff;">
<table style="width: 100%;border: 1px solid #999; font-size:12px; color: #000;">
<c:forEach var="map" items="${Entity}">
	<tr>
		<th>Directory：<c:out value="${map.key}" /></th>
		<th></th>
	</tr>
	<c:forEach var="jobClassName" items="${map.value}">
		<tr>
			<td><font color="red">Class：<c:out value="${jobClassName}" /></font></td>
			<td style="text-align: center;"><input type="radio" name="jobClass" value="${jobClassName}" onclick="exec('${map.key}','${jobClassName}')" /></td>
		</tr>
	</c:forEach>
</c:forEach>
</table>
<script type="text/javascript">
function exec(path,clazz){
    parent.quartz.exec(path,clazz);
}
</script>
</body>
</html>