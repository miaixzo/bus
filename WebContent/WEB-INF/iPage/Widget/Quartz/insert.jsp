<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@ include file="../../Comm/tags.jsp"%>
<!DOCTYPE html>
<html>
<head>
<title><spring:message code="com.ukettle.Title" /></title>
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
    		  <li><a href="${ctx}/Widget/Quartz/Insert" class="active"><spring:message code="com.ukettle.Menu.Quartz.Insert" /></a></li>
    		  <li><a href="${ctx}/Widget/Quartz/List"><spring:message code="com.ukettle.Menu.Quartz.List" /></a></li>
		   </ul>
         </div>  
         <div id="main">
         <input type="hidden" id="jsons" name="jsons" value='${Entity.params}'/>
         <form id="formId" action="${ctx}/Widget/Quartz/Insert" method="post">
         <input type="hidden" id="dir" name="dir" value="${Entity.dir}"/>
         <input type="hidden" name="isAsync" value="true"/>
         <input type="hidden" id="type" name="type" value="${Entity.type}"/>
         <input type="hidden" id="repo" name="repo" value="${Entity.repo}"/> 
         <input type="hidden" id="logging" name="logging" value="${Entity.logs}"/> 
         <h3><spring:message code="com.ukettle.Quartz.Info" /></h3>
     	 <fieldset>
     	 <div class="left">
         	<p><label><spring:message code="com.ukettle.Quartz.Group" /></label>
       		  <select id="group" name="group">
				<option style="display: none" value=""><spring:message code="com.ukettle.Quartz.Group.Choose" /></option>
				<c:forEach var="g" items="${List}">
					<option value="${g.name}">${g.name}</option>
				</c:forEach>
			  </select>
         	 </p>
			 <p><label><spring:message code="com.ukettle.Quartz.Title" /></label>
			   <input type="text" class="text-long" id="title" name="title" />
			 </p>
			 <c:choose>
               <c:when test="${empty Entity.repo}">
			  	   <p><label><spring:message code="com.ukettle.Quartz.Executor" /></label><input type="text" class="text-long" id="executor" name="executor" readOnly/>
			       <a href="javascript:void(0);" onclick="quartz.choose();"><i class="icon-zoom-in"></i></a>
			       </p>
			   </c:when>
			   <c:otherwise>  
			      <input type="hidden" id="executor" name="executor" value="com.ukettle.quartz.timer.kettle.KettleExecutor" readOnly/> 
			   </c:otherwise>
			 </c:choose>
             <div id="params">
         	 <p><label><spring:message code="com.ukettle.Quartz.Parameters" /></label>
         	 <c:choose>
               <c:when test="${empty Entity.repo}">
               	   <input type="text" class="text-small" id="argKey" /><input type="text" id="argVal" class="text-long" id="argVal" name="method" />
               	   <a href="javascript:;" onclick="quartz.add();"><i class="icon-plus-sign"></i></a>
			   </c:when>
			   <c:otherwise>  
			      <input type="text" class="text-small" id="argKey" readOnly/><input type="text" class="text-long" id="argVal" name="method" readOnly />
			   </c:otherwise>
			 </c:choose>
         	 </p>
         	 </div>
         	 <p><label><spring:message code="com.ukettle.Quartz.Trigger" /></label>
				<input type="radio" id="triggerType" name="triggerType" value="simple" checked onclick="quartz.trigger();" /><spring:message code="com.ukettle.Quartz.Trigger.Simple" />&nbsp;&nbsp;
				<input type="radio"  id="triggerType" name="triggerType" value="cron" onclick="quartz.trigger();" /><spring:message code="com.ukettle.Quartz.Trigger.Cron" />
			 </p>
			 <div id="simpleTrigger">
         	     <p><label class="inline"><spring:message code="com.ukettle.Quartz.Trigger.Frequency" /></label><input type="number" required max="60" data-error-type="inline" style="padding-left: 66px;" id="frequency" name="frequency" class="text-long" value="5"/> <spring:message code="com.ukettle.Quartz.Trigger.Tips.Minute" /></p>
	         	 <p><label class="inline"><spring:message code="com.ukettle.Quartz.Trigger.Quantity" /></label><input type="number" required max="60" data-error-type="inline" style="padding-left: 66px;" id="quantity" name="quantity" class="text-long" value="-1" /> <spring:message code="com.ukettle.Quartz.Trigger.Tips.Continuous" /></p>
         	 </div>
			 <div id="cronTrigger" style="display: none;">
	         	 <p><label class="inline"><spring:message code="com.ukettle.Quartz.Trigger.Second" /></label><input type="text" data-error-type="inline" style="padding-left: 66px;" id="second" name="second" value="0" class="text-long"/></p>
	         	 <p><label class="inline"><spring:message code="com.ukettle.Quartz.Trigger.Minute" /></label><input type="text" data-error-type="inline" style="padding-left: 66px;" id="minute" name="minute" value="*" class="text-long"/></p>
	         	 <p><label class="inline"><spring:message code="com.ukettle.Quartz.Trigger.Hour" /></label><input type="text" data-error-type="inline" style="padding-left: 66px;" id="hour" name="hour" value="*" class="text-long"/></p>
	         	 <p><label class="inline"><spring:message code="com.ukettle.Quartz.Trigger.Day" /></label><input type="text" data-error-type="inline" style="padding-left: 66px;" id="day" name="day" value="*" class="text-long"/></p>
	         	 <p><label class="inline"><spring:message code="com.ukettle.Quartz.Trigger.Month" /></label><input type="text" data-error-type="inline" style="padding-left: 66px;" id="month" name="month" value="*" class="text-long"/></p>
	         	 <p><label class="inline"><spring:message code="com.ukettle.Quartz.Trigger.Week" /></label><input type="text" data-error-type="inline" style="padding-left: 66px;" id="week" name="week" value="?" class="text-long"/></p>
         	 </div>
         	 <p><label><spring:message code="com.ukettle.Quartz.Description" /></label>
         	 	<textarea rows="1" cols="1" name="description">${Entity.remark}</textarea>
         	 </p>
             <input type="button" id="iQuartz" value="<spring:message code="com.ukettle.User.Submit" />" />
             </div>
             <div class="right"><label><b><spring:message code="com.ukettle.Quartz.Tips" /></b></label>
                <table style="width:100%">
				    <tr style="text-align:left;">
						<td><spring:message code="com.ukettle.Quartz.Tips.Name" /></td>
						<td><spring:message code="com.ukettle.Quartz.Tips.Value" /></td>
						<td><spring:message code="com.ukettle.Quartz.Tips.Symbol" /></td>
					</tr>
					<tr class="odd" >
						<td><spring:message code="com.ukettle.Quartz.Tips.Quantity" /></td>
						<td>0-N</td>
						<td></td>
					</tr>
					<tr>
						<td><spring:message code="com.ukettle.Quartz.Tips.Frequency" /></td>
						<td>0-N</td>
						<td>-1</td>
					</tr>
					<tr class="odd" >
						<td><spring:message code="com.ukettle.Quartz.Tips.Second" /></td>
						<td>0-59</td>
						<td>, - * /</td>
					</tr>
					<tr>
						<td><spring:message code="com.ukettle.Quartz.Tips.Minute" /></td>
						<td>0-59</td>
						<td>, - * /</td>
					</tr>
					<tr class="odd" >
						<td><spring:message code="com.ukettle.Quartz.Tips.Hour" /></td>
						<td>0-23</td>
						<td>, - * /</td>
					</tr>
					<tr>
						<td><spring:message code="com.ukettle.Quartz.Tips.Day" /></td>
						<td>0-31</td>
						<td>, - * ? / L W C</td>
					</tr>
					<tr class="odd" >
						<td><spring:message code="com.ukettle.Quartz.Tips.Month" /></td>
						<td>1-12 or JAN-DEC</td>
						<td>, - * /</td>
					</tr>
					<tr>
						<td><spring:message code="com.ukettle.Quartz.Tips.Week" /></td>
						<td>1-7 or SUN-SAT</td>
						<td>, - * ? / L C #</td>
					</tr>
				 </table>
             </div>
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