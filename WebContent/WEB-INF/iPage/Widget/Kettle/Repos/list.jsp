<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@ include file="../../../Comm/tags.jsp"%>
<!DOCTYPE html>
<html>
<head>
<title><spring:message code="com.ukettle.Title" /></title>
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
         <h3><spring:message code="com.ukettle.Kettle.Repository.info" /></h3>
		 <table>
		    <tr style="text-align:left;">
				<td><spring:message code="com.ukettle.Kettle.Repository.Name" /></td>
				<td><spring:message code="com.ukettle.Kettle.Repository.User" /></td>
				<td><spring:message code="com.ukettle.Kettle.Repository.Pass" /></td>
				<td><spring:message code="com.ukettle.Kettle.Repository.DB" /></td>
				<td><spring:message code="com.ukettle.Kettle.Repository.dbUser" /></td>
				<td><spring:message code="com.ukettle.Kettle.Repository.dbPass" /></td>
				<td><spring:message code="com.ukettle.Kettle.Repository.Host" /></td>
				<td><spring:message code="com.ukettle.Kettle.Repository.Port" /></td>
				<td><spring:message code="com.ukettle.Status" /></td>
				<td><spring:message code="com.ukettle.Action" /></td>
			</tr><% int cnt = 0; %>
			<c:forEach var="i" items="${List}">
			<tr <%if(cnt%2==0){%>class="odd"<%}%>>
				<td title="${i.name}">${fn:substring(i.name, 0, 4)}...</td>
				<td>${fn:substring(i.user, 0, 4)}...</td>
				<td>*****</td>
				<td>${i.db}</td>
				<td>${fn:substring(i.username, 0, 4)}...</td>
				<td>*****</td>
				<td title="${i.server}">${fn:substring(i.server, 0, 6)}...</td>
				<td>${i.port}</td>
				<td <c:choose><c:when test="${i.status =='ENABLED'}">style="color:#00A600;"</c:when><c:otherwise>style="color:#FF0000;"</c:otherwise></c:choose>>
					<c:choose><c:when test="${i.status =='ENABLED'}"><spring:message code="com.ukettle.Status.Enabled" /></c:when><c:otherwise><spring:message code="com.ukettle.Status.Disabled" /></c:otherwise></c:choose>
				</td>
				<td class="action">
				<c:choose>
				  <c:when test="${i.status =='ENABLED'}">
				    <a href="javascript:;" onclick="kettle.loading('${i.name}');" title="<spring:message code="com.ukettle.Action.Loading" />"><i class="icon-signin"></i></a>
				    <a href="javascript:;" onclick="kettle.discard('${i.name}');" title="<spring:message code="com.ukettle.Action.Discard" />"><i class="icon-signout"></i></a>
				    <a href="javascript:;" onclick="kettle.status('${i.id}','DISABLED');" title="<spring:message code="com.ukettle.Status.Disabled" />"><i class="icon-check"></i></a>
				  </c:when>
			      <c:otherwise>
				    <a href="javascript:;" style="color:#eee" title="loading"><i class="icon-signin"></i></a>
				    <a href="javascript:;" style="color:#eee" title="discard"><i class="icon-signout"></i></a>
				    <a href="javascript:;" onclick="kettle.status('${i.id}','ENABLED');" title="<spring:message code="com.ukettle.Status.Enabled" />"><i class="icon-check-empty"></i></a>
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