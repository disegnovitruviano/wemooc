<%@page import="javax.portlet.PortletPreferences"%>
<%@page import="com.liferay.portlet.PortletPreferencesFactoryUtil"%>
<%@page import="com.liferay.lms.service.CompetenceServiceUtil"%>
<%@page import="java.util.Enumeration"%>
<%@page import="com.liferay.portal.model.RoleConstants"%>
<%@page import="com.liferay.lms.service.LmsPrefsLocalServiceUtil"%>
<%@page import="com.liferay.lms.model.LmsPrefs"%>
<%@page import="com.liferay.portal.kernel.util.ListUtil"%>
<%@page import="com.liferay.lms.service.CourseLocalServiceUtil"%>
<%@page import="com.liferay.lms.service.CourseServiceUtil"%>
<%@page import="com.liferay.lms.model.Course"%>
<%@page import="com.liferay.portal.model.Role"%>
<%@page import="com.liferay.portal.service.RoleLocalServiceUtil"%>
<%@ include file="/init.jsp" %>
<%
ResultRow row = (ResultRow)request.getAttribute(WebKeys.SEARCH_CONTAINER_RESULT_ROW);


	 
Course myCourse = (Course)row.getObject();
LmsPrefs prefs=LmsPrefsLocalServiceUtil.getLmsPrefs(themeDisplay.getCompanyId());
String name = Course.class.getName();
String primKey = String.valueOf(myCourse.getCourseId());

long count = 0;
long countGroup = CompetenceServiceUtil.getCountCompetencesOfGroup(myCourse.getGroupCreatedId());
long countParentGroup = CompetenceServiceUtil.getCountCompetencesOfGroup(myCourse.getGroupId());
count = countGroup + countParentGroup;

PortletPreferences preferences = null;
String portletResource = ParamUtil.getString(request, "portletResource");

if (Validator.isNotNull(portletResource)) {
	preferences = PortletPreferencesFactoryUtil.getPortletSetup(request, portletResource);
}else{
	preferences = renderRequest.getPreferences();
}

boolean showClose 	= preferences.getValue("showClose",  "true").equals("true");
boolean showDelete 	= preferences.getValue("showDelete", "true").equals("true");
boolean showMembers = preferences.getValue("showMembers","true").equals("true");
boolean showExport 	= preferences.getValue("showExport", "true").equals("true");
boolean showImport	= preferences.getValue("showImport", "true").equals("true");
boolean showClone 	= preferences.getValue("showClone",  "true").equals("true");
boolean showGo 		= preferences.getValue("showGo", 	 "true").equals("true");
boolean showPermission = preferences.getValue("showPermission", "true").equals("true");

%>
<liferay-ui:icon-menu>
<portlet:renderURL var="editURL">
	<portlet:param name="courseId" value="<%=primKey %>" />
	<portlet:param name="jspPage" value="/html/courseadmin/editcourse.jsp" />
	<portlet:param name="redirect" value='<%= ParamUtil.getString(request, "redirect", currentURL) %>'/>
</portlet:renderURL>
<%
if( permissionChecker.hasPermission(themeDisplay.getScopeGroupId(),  Course.class.getName(),primKey,ActionKeys.UPDATE)&& ! myCourse.isClosed())
{
%>
<liferay-ui:icon image="edit" message="edit" url="<%=editURL.toString() %>" />

<%
}
%>
<portlet:actionURL name="closeCourse" var="closeURL">
<portlet:param name="courseId" value="<%= primKey %>" />
<portlet:param name="redirect" value='<%= ParamUtil.getString(request, "redirect", currentURL) %>'/>
</portlet:actionURL>
<%
if( permissionChecker.hasPermission(themeDisplay.getScopeGroupId(),  Course.class.getName(),primKey,ActionKeys.UPDATE)&& ! myCourse.isClosed() && showClose)
{
%>
	<liferay-ui:icon image="close" message="close" url="<%=closeURL.toString() %>" />
<%
}else if(permissionChecker.hasPermission(themeDisplay.getScopeGroupId(),  Course.class.getName(),primKey,ActionKeys.UPDATE)&& myCourse.isClosed()){
%>
	<portlet:actionURL name="openCourse" var="openURL">
		<portlet:param name="courseId" value="<%= primKey %>" />
		<portlet:param name="redirect" value='<%= ParamUtil.getString(request, "redirect", currentURL) %>'/>
	</portlet:actionURL>
	<liferay-ui:icon src="<%= themeDisplay.getPathThemeImages() + \"/dock/my_places_private.png\" %>" message="open-course" url="<%=openURL.toString() %>" />
<%} %>
<portlet:actionURL name="deleteCourse" var="deleteURL">
<portlet:param name="courseId" value="<%= primKey %>" />
</portlet:actionURL>
<%
if( permissionChecker.hasPermission(themeDisplay.getScopeGroupId(),  Course.class.getName(),primKey,ActionKeys.DELETE)&& ! myCourse.isClosed() && showDelete)
{
%>
<liferay-ui:icon-delete url="<%=deleteURL.toString() %>" />
<%
}
%>
<portlet:renderURL var="memebersURL">
	<portlet:param name="courseId" value="<%=primKey %>" />
	<portlet:param name="backToEdit" value="<%=StringPool.FALSE %>" />
	<portlet:param name="jspPage" value="/html/courseadmin/rolememberstab.jsp" />
</portlet:renderURL>
<%
if(permissionChecker.hasPermission(themeDisplay.getScopeGroupId(),  Course.class.getName(),primKey,ActionKeys.ASSIGN_MEMBERS)&& ! myCourse.isClosed() && showMembers)
{
%>

<liferay-ui:icon image="group" message="assign-member" url="<%=memebersURL.toString() %>" />

<%
}
%>

<c:if test="<%= permissionChecker.hasPermission(myCourse.getGroupId(), Course.class.getName(), myCourse.getCourseId(), ActionKeys.PERMISSIONS)&& ! myCourse.isClosed() %>">
	<%if(showPermission){%>
	<liferay-security:permissionsURL
		modelResource="<%=Course.class.getName() %>"
		modelResourceDescription="<%= myCourse.getTitle(themeDisplay.getLocale()) %>"
		resourcePrimKey="<%= String.valueOf(myCourse.getCourseId()) %>"
		var="permissionsURL"
	/>
	<liferay-ui:icon image="permissions" message="courseadmin.adminactions.permissions" url="<%=permissionsURL %>" />
	<%}%>
	</c:if>
	<%if(showExport){%>	
	<portlet:renderURL var="exportURL">
		<portlet:param name="groupId" value="<%=String.valueOf(myCourse.getGroupCreatedId()) %>" />
		<portlet:param name="jspPage" value="/html/courseadmin/export.jsp" />
	</portlet:renderURL>
	<liferay-ui:icon image="download" message="courseadmin.adminactions.export" url="<%=exportURL %>" />			
	<%}%>
	<%if(showImport && permissionChecker.hasPermission(themeDisplay.getScopeGroupId(),  Course.class.getName(),primKey,ActionKeys.UPDATE)){%>	
	<portlet:renderURL var="importURL">
		<portlet:param name="groupId" value="<%=String.valueOf(myCourse.getGroupCreatedId()) %>" />
		<portlet:param name="jspPage" value="/html/courseadmin/import.jsp" />
	</portlet:renderURL>
	<liferay-ui:icon image="post" message="courseadmin.adminactions.import" url="<%=importURL %>" />			
	<%}%>
	<%if(showClone && permissionChecker.hasPermission(themeDisplay.getScopeGroupId(),  Course.class.getName(),primKey,ActionKeys.UPDATE)){%>	
	<portlet:renderURL var="cloneURL">
		<portlet:param name="groupId" value="<%=String.valueOf(myCourse.getGroupCreatedId()) %>" />
		<portlet:param name="jspPage" value="/html/courseadmin/clone.jsp" />
	</portlet:renderURL>
	<liferay-ui:icon image="copy" message="courseadmin.adminactions.clone" url="<%=cloneURL%>" />	
	<%}%>			


<c:if test="<%=count>0 && permissionChecker.hasPermission(themeDisplay.getScopeGroupId(),  Course.class.getName(),primKey,ActionKeys.UPDATE) && ! myCourse.isClosed()%>">
	<portlet:renderURL var="competenceURL">
		<portlet:param name="groupId" value="<%=String.valueOf(myCourse.getGroupCreatedId()) %>" />
		<portlet:param name="courseId" value="<%=String.valueOf(myCourse.getCourseId()) %>" />
		<portlet:param name="jspPage" value="/html/courseadmin/competencetab.jsp" />
	</portlet:renderURL>
	<liferay-ui:icon image="tag" message="competence.label" url="<%=competenceURL %>" />
</c:if>


</liferay-ui:icon-menu>