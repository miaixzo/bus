<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@ include file="../../Comm/tags.jsp"%>
<!DOCTYPE html>
<html>
<head>
<title><spring:message code="org.ukettle.Title" /></title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
<%@ include file="../../Comm/resource.jsp"%>
<script src="${ctx}/Html/js/ui.user.js" type="text/javascript"></script>
</head>
<body>
<div id="wrapper">
<header>
<%@ include file="../../header.jsp"%>
</header>
<ul id="mainNav">
<%@ include file="../../topnav.jsp"%>
</ul>
<div id="holder">
    <div id="container">
         <div id="sidebar">
           <ul class="sideNav">
             <li><a href="${ctx}/System/User/Insert" class="active"><spring:message code="org.ukettle.Menu.User.Insert" /></a></li>
             <li><a href="${ctx}/System/User/List" class="odd"><spring:message code="org.ukettle.Menu.User.List" /></a></li>
		   </ul>
         </div>    
         <div id="main">
         <h3><spring:message code="org.ukettle.User.List.Info" /></h3>
		 <table>
		    <tr style="text-align:left;">
				<td><spring:message code="org.ukettle.User.List.Name" /></td>
				<td><spring:message code="org.ukettle.User.List.Type" /></td>
				<td><spring:message code="org.ukettle.User.List.Password" /></td>
				<td><spring:message code="org.ukettle.User.List.Email" /></td>
				<td><spring:message code="org.ukettle.User.List.Mobile" /></td>
				<td><spring:message code="org.ukettle.User.List.Status" /></td>
				<td><spring:message code="org.ukettle.User.List.Remark" /></td>
				<td><spring:message code="org.ukettle.Action" /></td>
			</tr><% int cnt = 0; %>
			<c:forEach var="i" items="${List}">
			<tr <%if(cnt%2==0){%>class="odd"<%}%>>
				<td>${i.name}</td>
				<td>${i.type}</td>
				<td>*****</td>
				<td>${i.email}</td>
				<td>${i.mobile}</td>
				<td <c:choose><c:when test="${i.status =='ENABLED'}">style="color:#00A600;"</c:when><c:otherwise>style="color:#FF0000;"</c:otherwise></c:choose>>
					<c:choose><c:when test="${i.status =='ENABLED'}"><spring:message code="org.ukettle.Status.Enabled" /></c:when><c:otherwise><spring:message code="org.ukettle.Status.Disabled" /></c:otherwise></c:choose>
				</td>
				<td>
				<c:choose>
				  <c:when test="${fn:length(i.remark) > 8}">
				    <c:out value="${fn:substring(i.remark, 0, 8)}..." />
				  </c:when>
			      <c:otherwise>
			        <c:out value="${i.remark}" />
			      </c:otherwise>
				</c:choose>
				</td>
				<td class="action">
				    <a href="${ctx}/System/User/Update?id=${i.id}" title="<spring:message code="org.ukettle.Action.Update" />"><i class="icon-pencil"></i></a>
				    <a href="${ctx}/System/User/Delete?id=${i.id}" title="<spring:message code="org.ukettle.Action.Delete" />"><i class="icon-remove"></i></a>
				</td>
			</tr>
			</c:forEach>
		 </table>
         </div>
     <div class="clear"></div>
     </div>
</div>	
<footer>
<%@ include file="../../footer.jsp"%>
</footer>
</div>
</body>
</html>