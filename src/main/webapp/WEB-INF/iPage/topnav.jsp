<li><a href="${ctx}/Widget/Kettle/Repos/List" class="active"><spring:message code="org.ukettle.Menu.Kettle" /></a></li>
<li><a href="${ctx}/Widget/Quartz/List"><spring:message code="org.ukettle.Menu.Quartz" /></a></li>
<li class="logout"><a href="${ctx}/logout"><spring:message code="org.ukettle.Menu.Logout" /></a></a></li>
<li class="logout"><a href="${ctx}/System/User/List"><spring:message code="org.ukettle.Menu.User" /></a></li>
<li class="info"><spring:message code="org.ukettle.Shiro.Welcome" /><i class="icon-user" ><span class="email"><shiro:user><shiro:principal property="email" /></shiro:user></span></i></li>