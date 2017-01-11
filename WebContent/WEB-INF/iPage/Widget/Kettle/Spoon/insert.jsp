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
         <form id="formId" action="${ctx}/Widget/Kettle/Spoon/Insert" method="post">
         <input type="hidden" id="dir" name="dir"/>
         <input type="hidden" name="async" value="false"/>
         <input type="hidden" id="type" name="type" />
         <input type="hidden" id="rid" name="rid" />
         <h3><spring:message code="com.ukettle.Kettle.JobTrans.Info" /></h3>
     	 <fieldset>
     	 <div class="left">
         	<p><label><spring:message code="com.ukettle.Kettle.JobTrans.Repository" /></label>
       		  <select id="repository" name="repo">
				<option style="display: none" value=""><spring:message code="com.ukettle.Kettle.JobTrans.Repository.Choose" /></option>
				<c:forEach var="r" items="${List}">
				<option id="${r.id}" value="${r.name}">${r.name}</option>
				</c:forEach>
			  </select>
         	</p>
             <p><label><spring:message code="com.ukettle.Kettle.JobTrans.Remark" /></label><textarea rows="1" cols="1" name="remark"></textarea></p>
             <div id="params">
         	 <p><label><spring:message code="com.ukettle.Kettle.JobTrans.Parameters" /></label>
         	  <input type="text" class="text-small" id="argKey" readOnly/>
         	  <input type="text" class="text-long" id="argVal" name="method" readOnly/>
         	  <a href="javascript:;" onclick="kettle.choose();"><i class="icon-zoom-in"></i></a>
         	 </p>
         	 </div>
             <input type="button" disabled id="iKettle" value="<spring:message code="com.ukettle.Kettle.JobTrans.submit" />" />
             </div>
             <div class="right"><label><b><spring:message code="com.ukettle.Kettle.JobTrans.Tips" /></b></label>
             	<spring:message code="com.ukettle.Kettle.JobTrans.Tips.Info" />
             </div>
         </fieldset>
         </form>
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