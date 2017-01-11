<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@ include file="../../Comm/tags.jsp"%>
<!DOCTYPE html>
<html>
<head>
<title><spring:message code="org.ukettle.Title" /></title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
<%@ include file="../../Comm/resource.jsp"%>
<script src="${ctx}/Html/js/ui.quartz.js" type="text/javascript"></script>
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
    		  <li><a href="${ctx}/Widget/Quartz/Insert" class="active"><spring:message code="org.ukettle.Menu.Quartz.Insert" /></a></li>
    		  <li><a href="${ctx}/Widget/Quartz/List"><spring:message code="org.ukettle.Menu.Quartz.List" /></a></li>
		   </ul>
         </div>    
         <div id="main">
         	<h3 style="float:left"><spring:message code="org.ukettle.Quartz.List" /></h3>
	        <div style="float:right; margin:4px 0 0 0">
	         	<form action="${ctx}/Widget/Quartz/List" method="post">
	         	<spring:message code="org.ukettle.Quartz.List.s.Group" /> <select id="group" name="group">
						<option value=""><spring:message code="org.ukettle.Quartz.List.s.Group.Choose" /></option>
						<c:forEach var="g" items="${List}">
							<option id="${g.id}" value="${g.name}" <c:if test="${g.name==group}">selected</c:if>>${g.name}</option>
						</c:forEach>
					  </select>
		  		 <spring:message code="org.ukettle.Quartz.List.s.Title" /> <input type="text" class="text-long" id="title" name="title" value="${title}"/>
		 		 <input type="submit" id="iSearch" value="<spring:message code="org.ukettle.Quartz.List.s.Search" />" />
			  </form>
		 	</div>
		 <table>
		    <tr style="text-align:left;">
				<td><spring:message code="org.ukettle.Quartz.List.Title" /></td>
				<td><spring:message code="org.ukettle.Quartz.List.Group" /></td>
				<td><spring:message code="org.ukettle.Quartz.List.Type" /></td>
				<td><spring:message code="org.ukettle.Quartz.List.Parameters" /></td>
				<td><spring:message code="org.ukettle.Quartz.List.status" /></td>
				<td><spring:message code="org.ukettle.Quartz.List.PreviousTime" /></td>
				<td><spring:message code="org.ukettle.Quartz.List.NextTime" /></td>
				<td><spring:message code="org.ukettle.Action" /></td>
			</tr><% int i = 0; %>
			<c:forEach var="jobInfoEntity" items="${Entity.jobList}" varStatus="status">
				<c:set var="jobDetail" value="${jobInfoEntity.get('jobDetail')}" />
				<c:set var="triggers" value="${jobInfoEntity.get('triggers')}" />
				<c:set var="params" value="${jobInfoEntity.get('params')}" />
				<c:set var="type" value="${jobInfoEntity.get('type')}" />
			<tr <%if(i%2==0){%>class="odd"<%}%>>
				<td title="${jobDetail.name}">
				<c:choose>
				  <c:when test="${fn:length(jobDetail.name) > 50}">
				    <c:out value="${fn:substring(jobDetail.name, 0, 50)}..." />
				  </c:when>
			      <c:otherwise>
			        <c:out value="${jobDetail.name}" />
			      </c:otherwise>
				</c:choose>
				</td>
				<td title="${jobDetail.group}">
				<c:choose>
				  <c:when test="${fn:length(jobDetail.group) > 5}">
				    <c:out value="${fn:substring(jobDetail.group, 0, 5)}..." />
				  </c:when>
			      <c:otherwise>
			        <c:out value="${jobDetail.group}" />
			      </c:otherwise>
				</c:choose>
				</td>
				<td>${type}</td>
				<td title='${params}'>
				<c:choose>
				  <c:when test="${fn:length(params) > 8}">
				    <c:out value="${fn:substring(params, 0, 8)}..." />
				  </c:when>
			      <c:otherwise>
			        <c:out value="${params}" />
			      </c:otherwise>
				</c:choose>
				</td>
				<td>
				<c:forEach var="trigger" items="${triggers}">
				<c:set var="status"  value="${Entity.scheduler.getTriggerState(trigger.key)}" />
					<i <c:choose><c:when test="${status.name() == 'ERROR'}">style="color:#FF0000;font-family:Arial,Helvetica,sans-serif;font-size:10px;"</c:when><c:when test="${status.name() =='PAUSED'}">style="color:#FACC2E;font-family:Arial,Helvetica,sans-serif;font-size:10px;"</c:when><c:otherwise>style="color:#00A600;font-family:Arial,Helvetica,sans-serif;font-size:10px;"</c:otherwise></c:choose>>
						<c:out value="${status.name()}" />
					</i>
				</c:forEach>
				</td>
				<td>
				<c:forEach var="trigger" items="${triggers}">
					<fmt:formatDate value="${trigger.getPreviousFireTime()}" type="date" pattern="HH:mm:ss" />
				</c:forEach>
				</td>
				<td>
				<c:forEach var="trigger" items="${triggers}">
					<fmt:formatDate value="${trigger.getNextFireTime()}" type="date" pattern="HH:mm:ss" />
				</c:forEach>
				</td>
				<td class="action">
				    <a href="${ctx}/Widget/Quartz/Execute?id=${jobDetail.key.toString()}" title="<spring:message code="org.ukettle.Action.Execute" />"><i class="icon-expand"></i></a>
				    <a href="${ctx}/Widget/Quartz/Pause?id=${jobDetail.key.toString()}" title="<spring:message code="org.ukettle.Action.Pause" />"><i class="icon-pause"></i></a>
				    <a href="${ctx}/Widget/Quartz/Resume?id=${jobDetail.key.toString()}" title="<spring:message code="org.ukettle.Action.Resume" />"><i class="icon-undo"></i></a>
				    <a href="${ctx}/Widget/Quartz/View?id=${jobDetail.key.toString()}" title="<spring:message code="org.ukettle.Action.View" />"><i class="icon-eye-open"></i></a>
					<a href="${ctx}/Widget/Quartz/Update?id=${jobDetail.key.toString()}" title="<spring:message code="org.ukettle.Action.Update" />"><i class="icon-pencil"></i></a>
					<a href="${ctx}/Widget/Quartz/Delete?id=${jobDetail.key.toString()}" title="<spring:message code="org.ukettle.Action.Delete" />"><i class="icon-remove"></i></a>
				</td>
			</tr><% i++; %>
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