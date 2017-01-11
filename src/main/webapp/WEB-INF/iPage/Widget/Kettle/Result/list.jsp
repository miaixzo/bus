<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@ include file="../../../Comm/tags.jsp"%>
<!DOCTYPE html>
<html>
<head>
<title><spring:message code="org.ukettle.Title" /></title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
<%@ include file="../../../Comm/resource.jsp"%>
<script src="${ctx}/Html/js/ui.kettle.js" type="text/javascript"></script>
</head>
<body>
<div id="wrapper">
<header>
<%@ include file="../../../header.jsp"%>
</header>
<ul id="mainNav">
<%@ include file="../../../topnav.jsp"%>
</ul>
<div id="holder">
    <div id="container">
         <div id="sidebar">
            <%@ include file="../../../subnav.jsp"%>
         </div>    
         <div id="main">
         <h3><c:choose>
		  <c:when test="${type =='Testing'}">
			<spring:message code="org.ukettle.Menu.Kettle.Testlog" /><spring:message code="org.ukettle.Kettle.Result.Info" />
		  </c:when>
	      <c:otherwise>
	        <spring:message code="org.ukettle.Menu.Kettle.Runlog" /><spring:message code="org.ukettle.Kettle.Result.Info" />
	      </c:otherwise>
		 </c:choose></h3>
		 <table>
		    <tr style="text-align:left;">
				<td><spring:message code="org.ukettle.Kettle.Result.Name" /></td>
				<td><spring:message code="org.ukettle.Kettle.Result.Status" /></td>
				<td><spring:message code="org.ukettle.Kettle.Result.Parameters" /></td>
				<td><spring:message code="org.ukettle.Kettle.Result.Host" /></td>
				<td><spring:message code="org.ukettle.Kettle.Result.Again" /></td>
				<td><spring:message code="org.ukettle.Kettle.Result.Times" /></td>
				<td><spring:message code="org.ukettle.Kettle.Result.StartTime" /></td>
				<td><spring:message code="org.ukettle.Kettle.Result.EndTime" /></td>
				<td><spring:message code="org.ukettle.Action" /></td>
			</tr><% int cnt = 0; %>
			<c:forEach var="i" items="${list}">
			<tr <%if(cnt%2==0){%>class="odd"<%}%> <c:choose><c:when test="${i.error > 0}">style="color:#FF0000;"</c:when></c:choose>>
				<td title="${i.name}">
				<c:choose>
				  <c:when test="${fn:length(i.name) > 30}">
				    <c:out value="${fn:substring(i.name, 0, 30)}..." />
				  </c:when>
			      <c:otherwise>
			        <c:out value="${i.name}" />
			      </c:otherwise>
				</c:choose>
				</td>
				<td>${i.status}</td>
				<td title='${i.params}'>
				<c:choose>
				  <c:when test="${fn:length(i.params) > 10}">
				    <c:out value="${fn:substring(i.params, 0, 10)}..." />
				  </c:when>
			      <c:otherwise>
			        <c:out value="${i.params}" />
			      </c:otherwise>
				</c:choose>
				</td>
				<td title='${i.host}'>
				<c:choose>
				  <c:when test="${fn:length(i.host) > 10}">
				    <c:out value="${fn:substring(i.host, 0, 10)}..." />
				  </c:when>
			      <c:otherwise>
			        <c:out value="${i.host}" />
			      </c:otherwise>
				</c:choose>
				</td>
				<td>${i.again}</td>
				<td>${i.times}</td>
				<td>
				    <fmt:parseDate var="startTime" value="${i.startTime}" type="date" pattern="yyyy-MM-dd HH:mm:ss"/>
                    <fmt:formatDate value='${startTime}' pattern='MM-dd HH:mm:ss' />
				</td>
				<td>
				    <fmt:parseDate var="endTime" value="${i.endTime}" type="date" pattern="yyyy-MM-dd HH:mm:ss"/>
                    <fmt:formatDate value='${endTime}' pattern='MM-dd HH:mm:ss' />
				</td>
				<td class="action">
				<c:choose>
				  <c:when test="${i.error > 0}">
					<a href="javascript:;" onclick="kettle.outs('${i.id}')" title="<spring:message code="org.ukettle.Action.Chart" />" style="position:relative;"><i class="icon-bar-chart"></i><span class="tips">${i.error}</span></a>
					<a href="javascript:;" onclick="kettle.logs('${i.id}')" title="<spring:message code="org.ukettle.Action.Info" />"><i class="icon-file-text-alt"></i></a>
					<a href="${ctx}/Widget/Kettle/deleteM?id=${i.id}&type=${i.type}" title="<spring:message code="org.ukettle.Action.Delete" />"><i class="icon-remove"></i></a>
				  </c:when>
			      <c:otherwise>
			        <a onclick="javascript:;" title="<spring:message code="org.ukettle.Action.Chart" />"><i class="icon-bar-chart" style="color:#eee"></i></a>
			        <a onclick="javascript:;" title="<spring:message code="org.ukettle.Action.Info" />"><i class="icon-file-text" style="color:#eee"></i></a>
					<a href="${ctx}/Widget/Kettle/deleteM?id=${i.id}&type=${i.type}" title="<spring:message code="org.ukettle.Action.Delete" />"><i class="icon-remove"></i></a>
			      </c:otherwise>
				</c:choose>
				</td>
			</tr><% cnt++; %>
			</c:forEach>
		 </table>
         </div>
     <div class="clear"></div>
     </div>
</div>	
<footer>
<%@ include file="../../../footer.jsp"%>
</footer>
</div>
</body>
</html>