<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@ include file="Comm/tags.jsp"%>
<!DOCTYPE html>
<html>
<head>
<title><spring:message code="org.ukettle.Title" /></title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
<link type="image/x-icon" href="<%=request.getContextPath()%>/Html/img/favicon.ico" rel="shortcut icon"/>
<link type="text/css" href="<%=request.getContextPath()%>/Html/css/ui.login.css" rel="stylesheet">
<script type="text/javascript" src="<%=request.getContextPath()%>/Html/js/libs/ui.jquery.js"></script>
</head>
<body>
<div class="message">${message}</div>
<div class="form">
<form action="<%=request.getContextPath()%>/Index" method="post">
    <p class="field">
        <label for="email"></label><input type="email" name="username" value="${username}" required placeholder="<spring:message code="org.ukettle.Shiro.Email" />">
        <i class="icon-user icon-large"></i>
    </p>
    <p class="field">
        <input type="password" name="password" required placeholder="<spring:message code="org.ukettle.Shiro.Pass" />">
        <i class="icon-lock icon-large"></i>
    </p>
    <p class="submit">
        <button type="submit" name="submit" id="login"><i class="icon-arrow-right icon-large"></i></button>
    </p>
</form>
</div>
</body>
</html>