<%@page import="com.liferay.lms.model.CourseResult"%>
<%@page import="com.liferay.lms.service.CourseResultLocalServiceUtil"%>
<%@page import="com.liferay.lms.model.Competence"%>
<%@page import="com.liferay.lms.service.CompetenceLocalServiceUtil"%>
<%@page import="com.liferay.lms.model.UserCompetence"%>
<%@page import="com.liferay.lms.model.CourseCompetence"%>
<%@page import="com.liferay.lms.service.CourseCompetenceLocalServiceUtil"%>
<%@page import="com.liferay.lms.service.UserCompetenceLocalServiceUtil"%>
<%@page import="com.liferay.portal.model.MembershipRequest"%>
<%@page import="com.liferay.portal.model.MembershipRequestConstants"%>
<%@page import="com.liferay.portal.service.MembershipRequestLocalServiceUtil"%>
<%@page import="com.liferay.lms.model.Course"%>
<%@page import="com.liferay.lms.service.CourseLocalServiceUtil"%>
<%@include file="/init.jsp" %>
<%@page import="java.net.URLEncoder"%>
<%@page import="javax.portlet.PortletPreferences"%>
<%@page import="com.liferay.util.LiferayViewUtil"%>
<%@page import="com.liferay.util.JavaScriptUtil"%>

<div id="caja_inscripcion">
	
<%
Course course=CourseLocalServiceUtil.fetchByGroupCreatedId(themeDisplay.getScopeGroupId());

if(course!=null && permissionChecker.hasPermission(course.getGroupId(),  Course.class.getName(),course.getCourseId(),ActionKeys.VIEW)){
	int numberUsers = UserLocalServiceUtil.getGroupUsersCount(course.getGroupCreatedId());
	
	if(themeDisplay.isSignedIn())
	{	
		if(GroupLocalServiceUtil.hasUserGroup(themeDisplay.getUserId(),themeDisplay.getScopeGroupId()))
		{
		%>
			<portlet:actionURL name="desinscribir"  var="desinscribirURL" windowState="NORMAL"/>
			<script type="text/javascript">
				function <portlet:namespace />enviar() {
					if(confirm('<%=JavaScriptUtil.markupToStringLiteral(LanguageUtil.get(pageContext, "inscripcion.desinscribete.seguro")) %>')) {
						window.location.href = "<%=desinscribirURL %>";
					}
				}
			</script>
			<div class="mensaje_marcado"><liferay-ui:message key="inscripcion.inscrito" /></div>	
			<div class="boton_inscibirse ">
				<a href="#" onclick="javascript:<portlet:namespace />enviar();"><liferay-ui:message key="inscripcion.desinscribete" /></a>
			</div>			
		<%
		
		} else 	{
			Group groupC = GroupLocalServiceUtil.getGroup(course.getGroupCreatedId());
			
			List<CourseCompetence> courseCompetences = CourseCompetenceLocalServiceUtil.findBycourseId(course.getCourseId(), true);
			
			boolean pass=true;
			if(courseCompetences!=null&&courseCompetences.size()>0){
				%><div><liferay-ui:message key="competences.necessary" />:</div><ul><%
				for(CourseCompetence courseCompetence : courseCompetences){
					UserCompetence uc = UserCompetenceLocalServiceUtil.findByUserIdCompetenceId(themeDisplay.getUserId(), courseCompetence.getCompetenceId());
					if(uc!=null)
					{
						Competence compet =CompetenceLocalServiceUtil.getCompetence(uc.getCompetenceId());
						if(c!=null)
						{
							%><li><liferay-ui:icon image="checked"/><%=compet.getTitle(themeDisplay.getLocale())%></li><%
						}
					}else{
						pass=false;
						Competence compet =CompetenceLocalServiceUtil.getCompetence(courseCompetence.getCompetenceId());
						if(c!=null){
							%><li><liferay-ui:icon image="unchecked"/><%=compet.getTitle(themeDisplay.getLocale())%></li><%
						}
					}
				}
				%></ul><%
			}
			Date now=new Date(System.currentTimeMillis());
			if((course.getStartDate().before(now)&&course.getEndDate().after(now))&&permissionChecker.hasPermission(course.getGroupId(),  Course.class.getName(),course.getCourseId(),"REGISTER")){
				if((course.getMaxusers()<=0||numberUsers<course.getMaxusers())&&groupC.getType()!=GroupConstants.TYPE_SITE_PRIVATE){
					if(groupC.getType()==GroupConstants.TYPE_SITE_OPEN){
					%>
						<portlet:actionURL name="inscribir"  var="inscribirURL" windowState="NORMAL"/>
						<div class="boton_inscibirse ">
							<%if(pass){ %>
								<div class="mensaje_marcado"><liferay-ui:message key="inscripcion.noinscrito" /></div>
								<a href="<%=inscribirURL %>"><liferay-ui:message key="inscripcion.inscribete" /></a>
							<%}else{ %>
								<liferay-ui:message key="competence.block" />
							<%} %>
						</div>			
					<%
					}else if(groupC.getType()==GroupConstants.TYPE_SITE_RESTRICTED){
						List<MembershipRequest> pending = MembershipRequestLocalServiceUtil.getMembershipRequests(themeDisplay.getUserId(), themeDisplay.getScopeGroupId(), MembershipRequestConstants.STATUS_PENDING);
						
						if(pending.size()>0){
							%><div class="mensaje_marcado"><liferay-ui:message key="course.pending" /></div><%
						}else{
							List<MembershipRequest> denied = MembershipRequestLocalServiceUtil.getMembershipRequests(themeDisplay.getUserId(), themeDisplay.getScopeGroupId(), MembershipRequestConstants.STATUS_DENIED);
							
							if(denied.size()>0){
								%><div class="mensaje_marcado"><liferay-ui:message key="course.denied" /></div><%
							}else{
								%>
								<portlet:actionURL name="member"  var="memberURL" windowState="NORMAL"/>
								<div class="boton_inscibirse ">
									<%if(pass){ %>
										<div class="mensaje_marcado"><liferay-ui:message key="inscripcion.surveillance" /></div>
										<a href="<%=memberURL %>"><liferay-ui:message key="inscripcion.request" /></a>
									<%}else{ %>
										<liferay-ui:message key="competence.block" />
									<%} %>
								</div>	
								<%	
							}
						}
					}
				}else{
					if(groupC.getType()==GroupConstants.TYPE_SITE_PRIVATE){
						renderRequest.setAttribute(WebKeys.PORTLET_CONFIGURATOR_VISIBILITY, Boolean.FALSE);
					}
					%>
					<div class="mensaje_marcado"><liferay-ui:message key="course.full" /></div>
					<%
				}
			}else{
				if(!permissionChecker.hasPermission(course.getGroupId(),  Course.class.getName(),course.getCourseId(),"REGISTER")){
					%><div class="mensaje_marcado"><liferay-ui:message key="inscripcion.permission" /></div><%
				}else if(course.getStartDate().after(now)){
					%><div class="mensaje_marcado"><liferay-ui:message key="inscripcion.date" /></div><%
				}else{
					%><div class="mensaje_marcado"><liferay-ui:message key="inscripcion.date.pass" /></div><%
				}
				
			}
		}
	} else {
		String urlRedirect= themeDisplay.getURLCurrent();	
		%>
		<div class="mensaje_marcado"><liferay-ui:message key="inscripcion.nologado" /></div>
		<div class="boton_inscibirse ">
			<a href="/c/portal/login?redirect=<%=urlRedirect%>"><liferay-ui:message key="inscripcion.registrate" /></a>
		</div>
		<%	
	}
}else{
	renderRequest.setAttribute(WebKeys.PORTLET_CONFIGURATOR_VISIBILITY, Boolean.FALSE);
}
%>
</div>