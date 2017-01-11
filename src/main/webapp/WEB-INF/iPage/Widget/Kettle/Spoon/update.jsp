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
         <form id="formId" action="${ctx}/Widget/Kettle/Spoon/Update" method="POST">
         <input type="hidden" id="pid" name="id" value="${Entity.id}"/>
         <input type="hidden" id="dir" name="dir" value="${Entity.dir}"/>
         <input type="hidden" id="rid" name="rid" value="${Entity.rid}"/>
          <input type="hidden" name="async" value="${Entity.async}"/>
         <input type="hidden" id="type" name="type" value="${Entity.type}"/>
         <h3><spring:message code="org.ukettle.Kettle.JobTrans.Info" /></h3>
     	 <fieldset>
     	 <div class="left">
         	<p><label><spring:message code="org.ukettle.Kettle.JobTrans.Repository" /></label>
       		  <select id="repository" name="repo">
				<option style="display: none" value=""><spring:message code="org.ukettle.Kettle.JobTrans.Repository.Choose" /></option>
				<c:forEach var="r" items="${List}">
					<option id="${r.id}" value="${r.name}" <c:if test="${r.name == Entity.repo}">selected</c:if>>${r.name}</option>
				</c:forEach>
			  </select>
         	</p>
             <p><label><spring:message code="org.ukettle.Kettle.JobTrans.Remark" /></label>
             	<textarea rows="1" cols="1" name="remark">${Entity.remark}</textarea>
             </p>
             <div id="params">
         	 <p><label><spring:message code="org.ukettle.Kettle.JobTrans.Parameters" /></label>
         	  <input type="text" class="text-small" id="argKey" readOnly/>
         	  <input type="text" class="text-long" id="argVal" name="method" readOnly/>
         	 </p>
         	 </div>
         	 <input type="reset" onClick="window.history.go(-1);" value="<spring:message code="org.ukettle.Kettle.Running.Back" />" />
             <input type="button" id="iKettle" value="<spring:message code="org.ukettle.Kettle.JobTrans.submit" />" />
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
               var id = $(j).selector;
               var value = json[i][j];
               if(id=="method"){
            	   $("#argKey").val(id);
            	   $("#argVal").val(value);
               }else if(id != "id" 
            		   && id !="dir" 
            		   && id !="type" 
            		   && id !="repo" 
            		   && id !="async" 
            		   && id !="remark"){
            	   kettle.update(id,value);
        	   }
           }
       }
    } 
});
</script>
</body>
</html>