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
         <form id="formId" action="${ctx}/System/User/Update" method="post">
         <input type="hidden" id="id" name="id" value="${Entity.id}"/>
         <h3><spring:message code="org.ukettle.User.Info" /></h3>
     	 <fieldset>
     	 <div class="left">
     	     <p><label><spring:message code="org.ukettle.User.Name" /></label>
         	  <input type="text" class="text-long" id="name" name="name" value="${Entity.name}"/>
         	 </p>
         	 <p><label><spring:message code="org.ukettle.User.Email" /></label>
         	  <input type="text" class="text-long" id="email" name="email" value="${Entity.email}" readOnly />
         	 </p>
         	 <p><label><spring:message code="org.ukettle.User.Mobile" /></label>
         	  <input type="text" class="text-long" id="mobile" name="mobile" value="${Entity.mobile}"/>
         	 </p>
         	 <p><label><spring:message code="org.ukettle.User.Password" /></label>
         	  <input type="text" class="text-long" id="password1"/>
         	 </p>
         	 <p><label><spring:message code="org.ukettle.User.Confirm" /></label>
         	  <input type="text" class="text-long" id="password" name="password"/>
         	 </p>
         	 <p><label><spring:message code="org.ukettle.User.Type" /></label>
         	  <select id="type" name="type">
				  <option value="Kettle" <c:if test="${Entity.type=='Kettle'}">selected</c:if>><spring:message code="org.ukettle.Type.Kettle" /></option>
				  <option value="Quartz" <c:if test="${Entity.type=='Quartz'}">selected</c:if>><spring:message code="org.ukettle.Type.Quartz" /></option>
			  </select>
         	 </p>
         	 <p><label><spring:message code="org.ukettle.User.Status" /></label>
         	  <select id="status" name="status">
				  <option value="ENABLED" <c:if test="${Entity.status=='ENABLED'}">selected</c:if>><spring:message code="org.ukettle.Status.Enabled" /></option>
				  <option value="DISABLED" <c:if test="${Entity.status=='DISABLED'}">selected</c:if>><spring:message code="org.ukettle.Status.Disabled" /></option>
			  </select>
         	 </p>
             <p><label><spring:message code="org.ukettle.User.Remark" /></label>
             	<textarea rows="1" cols="1" name="remark">${Entity.remark}</textarea>
             </p>
             <div id="params">
         	 </div>
             <input type="button" id="iUser" value="<spring:message code="org.ukettle.User.Submit" />" />
             </div>
             <div class="right"><label><b><spring:message code="org.ukettle.User.Tips" /></b></label><spring:message code="org.ukettle.User.Tips.Info" /></div>
         </fieldset>
         </form>
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