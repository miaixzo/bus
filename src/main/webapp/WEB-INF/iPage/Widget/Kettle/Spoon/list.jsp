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
         <h3><spring:message code="org.ukettle.Kettle.Running.Info" /></h3>
		 <table>
		    <tr style="text-align:left;">
				<td><spring:message code="org.ukettle.Kettle.Running.Name" /></td>
				<td><spring:message code="org.ukettle.Kettle.Running.Type" /></td>
				<td><spring:message code="org.ukettle.Kettle.Running.Status" /></td>
				<td><spring:message code="org.ukettle.Kettle.Running.Repository" /></td>
				<td><spring:message code="org.ukettle.Kettle.Running.Dir" /></td>
				<td><spring:message code="org.ukettle.Kettle.Running.Parameters" /></td>
				<td><spring:message code="org.ukettle.Action" /></td>
			</tr>
		    <% int cnt = 0; %>
			<c:forEach var="i" items="${List}">
			<tr <%if(cnt%2==0){%>class="odd"<%}%> <c:choose><c:when test="${i.status =='MATCH'}">style="color:#FF9B05;"</c:when></c:choose>>
				<td title="${i.method}">
				<c:choose>
				  <c:when test="${fn:length(i.method) > 50}">
				    <c:out value="${fn:substring(i.method, 0, 50)}..." />
				  </c:when>
			      <c:otherwise>
			        <c:out value="${i.method}" />
			      </c:otherwise>
				</c:choose>
				</td>
				<td>${i.type}</td>
				<td>${i.status}</td>
				<td>${i.repo}</td>
				<td title='${i.dir}'>
				<c:choose>
				  <c:when test="${fn:length(i.dir) > 18}">
				    <c:out value="${fn:substring(i.dir, 0, 18)}..." />
				  </c:when>
			      <c:otherwise>
			        <c:out value="${i.dir}" />
			      </c:otherwise>
				</c:choose>
				</td>
				<td title='${i.params}'>
				<c:choose>
				  <c:when test="${fn:length(i.params) > 25}">
				    <c:out value="${fn:substring(i.params, 0, 25)}..." />
				  </c:when>
			      <c:otherwise>
			        <c:out value="${i.params}" />
			      </c:otherwise>
				</c:choose>
				</td>
				<td class="action">
				    <a href="${ctx}/Widget/Kettle/Spoon/View?id=${i.id}" title="<spring:message code="org.ukettle.Action.View" />"><i class=" icon-eye-open"></i></a>
				    <a href="${ctx}/Widget/Kettle/Spoon/Update?id=${i.id}" title="<spring:message code="org.ukettle.Action.Update" />"><i class="icon-pencil"></i></a><c:choose><c:when test="${i.status =='MATCH'}">
				    <a href="javascript:;" title="<spring:message code="org.ukettle.Action.Delete" />"><i class="icon-remove" style="color:#eee"></i></a></c:when><c:otherwise>
					<a href="${ctx}/Widget/Kettle/Spoon/Delete?id=${i.id}" title="<spring:message code="org.ukettle.Action.Delete" />"><i class="icon-remove"></i></a></c:otherwise>
				</c:choose>
				</td>
			</tr>
			<% cnt++; %>
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