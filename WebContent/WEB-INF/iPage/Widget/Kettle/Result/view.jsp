<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@ include file="../../../Comm/tags.jsp"%>
<!DOCTYPE html>
<html>
<head>
<title><spring:message code="com.ukettle.Title" /></title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
</head>
<body style="background:#fff;">
<div id="main" style="width:100%;padding:10px 0px;">
<table style="width:98%;float:right;margin-right:6px;">
	<tr style="text-align:left;">
		<td><spring:message code="com.ukettle.Kettle.Monitor.Error" /></td>
		<td><spring:message code="com.ukettle.Kettle.Monitor.Read" /></td>
		<td><spring:message code="com.ukettle.Kettle.Monitor.Written" /></td>
		<td><spring:message code="com.ukettle.Kettle.Monitor.Updated" /></td>
		<td><spring:message code="com.ukettle.Kettle.Monitor.Input" /></td>
		<td><spring:message code="com.ukettle.Kettle.Monitor.Output" /></td>
		<td><spring:message code="com.ukettle.Kettle.Monitor.Deleted" /></td>
		<td><spring:message code="com.ukettle.Kettle.Monitor.Retrieved" /></td>
		<td><spring:message code="com.ukettle.Kettle.Monitor.Rejected" /></td>
	</tr>
	<tr class="odd">
		<td>${entity.error}</td>
		<td>${entity.read}</td>
		<td>${entity.written}</td>
		<td>${entity.updated}</td>
		<td>${entity.input}</td>
		<td>${entity.output}</td>
		<td>${entity.deleted}</td>
		<td>${entity.retrieved}</td>
		<td>${entity.rejected}</td>
	</tr>
</table>
<div class="clear"></div>
<div style="width:100%;text-align:left;padding:6px;line-height: 150%"><label><b><spring:message code="com.ukettle.Kettle.Monitor.Tips" /></b></label>
	<spring:message code="com.ukettle.Kettle.Monitor.Tips.Info" />
</div>
</div>
</body>
</html>