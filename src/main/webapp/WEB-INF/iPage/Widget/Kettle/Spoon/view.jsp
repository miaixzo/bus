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
         <form id="formId" action="${ctx}/Widget/Quartz/Insert" method="get">
         <input type="hidden" id="pid" name="id" value="${Entity.id}"/>
         <input type="hidden" id="dir" name="dir" value="${Entity.dir}"/>
         <input type="hidden" id="async" name="async" value="${Entity.async}"/>
         <input type="hidden" id="type" name="type" value="${Entity.type}"/>
         <h3><spring:message code="org.ukettle.Kettle.JobTrans.Info" /></h3>
     	 <fieldset>
     	 <div class="left">
         	<p><label><spring:message code="org.ukettle.Kettle.JobTrans.Repository" /></label>
       		  <select id="repository" name="repo" disabled>
					<option value="${Entity.rid}">${Entity.repo}</option>
			  </select>
         	</p>
             <p><label><spring:message code="org.ukettle.Kettle.JobTrans.Remark" /></label>
             	<textarea readOnly rows="1" cols="1" name="remark">${Entity.remark}</textarea>
             </p>
             <div id="params">
         	 <p><label><spring:message code="org.ukettle.Kettle.JobTrans.Parameters" /></label>
         	  <input type="text" class="text-small" id="argKey" readOnly/>
         	  <input type="text" class="text-long" id="argVal" name="method" readOnly/>
         	 </p>
         	 </div>
	             <input type="reset" onClick="window.history.go(-1);" value="<spring:message code="org.ukettle.Kettle.Running.Back" />" />
	             <input type="button" id="iSyanc" value="<spring:message code="org.ukettle.Kettle.Running.Sync" />" />
             </div>
             <div class="right"><label><b><spring:message code="org.ukettle.Kettle.JobTrans.Tips" /></b></label>
             	<spring:message code="org.ukettle.Kettle.JobTrans.Tips.Info" />
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
<script type="text/javascript">
$(function() {
	var json = eval('${Entity.params}');
    for (var i in json){
       for(var j in json[i]){
           if ($(j)){
               var key = $(j).selector;
               var value = json[i][j];
               if(key=="method"){
            	   $("#argKey").val(key);
            	   $("#argVal").val(value);
               }else if(key != "id" 
        		   && key !="dir" 
        		   && key !="type" 
        		   && key !="repo" 
        		   && key !="async" 
        		   && key !="remark"){
            	   kettle.view(key,value);
        	   }
           }
       }
    } 
});
</script>
</body>
</html>