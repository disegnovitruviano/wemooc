<%@page import="com.liferay.portal.kernel.util.ArrayUtil"%>
<%@page import="com.liferay.portal.kernel.util.FileUtil"%>
<%@page import="com.liferay.portal.kernel.util.PropsUtil"%>
<%@page import="java.io.File"%>
<%@page import="java.util.Locale"%>
<%@page import="com.liferay.lms.model.Course"%>
<%@page import="com.liferay.lms.service.CourseLocalServiceUtil"%>
<%@ page import="com.liferay.portal.LARFileException" %>
<%@ page import="com.liferay.portal.LARTypeException" %>
<%@ page import="com.liferay.portal.LayoutImportException" %>

<%@ page import="com.liferay.portal.kernel.lar.PortletDataException" %>
<%@ page import="com.liferay.portal.kernel.lar.PortletDataHandler" %>
<%@ page import="com.liferay.portal.kernel.lar.PortletDataHandlerBoolean" %>
<%@ page import="com.liferay.portal.kernel.lar.PortletDataHandlerChoice" %>
<%@ page import="com.liferay.portal.kernel.lar.PortletDataHandlerControl" %>
<%@ page import="com.liferay.portal.kernel.lar.PortletDataHandlerKeys" %>
<%@ page import="com.liferay.portal.kernel.lar.UserIdStrategy" %>
<%@ page import="com.liferay.portal.kernel.util.Time" %>
<%@page import="com.liferay.portal.kernel.portlet.LiferayWindowState"%>

<%@ include file="/init.jsp" %>	

<%
	String groupId = request.getParameter("groupId");

	String name = groupId;
	
	Course course = CourseLocalServiceUtil.getCourseByGroupCreatedId(Long.valueOf(groupId));
	
	try{
		if(course != null){
			name = course.getTitle(themeDisplay.getLocale());
		}
	}catch(Exception e){}
%>

<liferay-portlet:renderURL var="backURL"></liferay-portlet:renderURL>
<liferay-ui:header title="<%= course != null ? course.getTitle(themeDisplay.getLocale()) : \"course\" %>" backURL="<%=backURL %>"></liferay-ui:header>

<liferay-portlet:resourceURL var="exportURL">
	<portlet:param name="action" value="exportCourse"/>
	<portlet:param name="groupId" value="<%=groupId %>"/>
</liferay-portlet:resourceURL>


<script type="text/javascript">
<!--
	 
Liferay.provide(
		window,
		'<portlet:namespace/>checkExport',
		function() {
			var A = AUI();
			// submit form and disable button
			A.one('#<portlet:namespace/>exportButton').attr('disabled', true);
			var checkExportRequest = A.io.request('<%= exportURL %>',
				{
					form: {
						id: '<portlet:namespace/>exportForm',
					},
					dataType: 'json',
					on: {
						success: function() {
							var data = this.get('responseData');
							var error = data.error;
							var status = data.status;
							var key = data.key;
							if (error) {
								// stop and show error
								
								alert(error);
							} else {
								var inputKeyVal = A.one('#<portlet:namespace/>key').val();
								if (inputKeyVal === '' || inputKeyVal === null) {
									A.one('#<portlet:namespace/>key').val(key);
								}
								if (status !== 'ready') {
									A.one('#<portlet:namespace/>loadingArea').show();
									// sleep 30 seconds and check again
									setTimeout(function () {checkExportRequest.start();}, 15000);
								} else {
									A.one('#<portlet:namespace/>loadingArea').hide();
									Liferay.Portlet.refresh('#p_p_id<%=renderResponse.getNamespace()%>');
								}
							}
							
						}
					}
				}		
			);
			checkExportRequest.start();
		},
		['node', 'aui-io-request']
		);
//-->
</script>
	
<aui:form name="exportForm" action="<%=exportURL%>" method="post" >

	<liferay-ui:error key="courseadmin.delete.exported.confirmation.error" message="courseadmin.delete.exported.confirmation.error"></liferay-ui:error>
	<liferay-ui:success key="courseadmin.delete.exported.confirmation.success" message="courseadmin.delete.exported.confirmation.success"></liferay-ui:success>
	<liferay-ui:success key="courseadmin.export.confirmation.success" message="courseadmin.export.confirmation.success"></liferay-ui:success>
	
	<aui:input label="export-the-selected-data-to-the-given-lar-file-name" name="exportFileName" size="50" value='<%= HtmlUtil.escape(StringUtil.replace( StringUtil.replace(name, " ", "_"),"/",""   )) + "-" + Time.getShortTimestamp() + ".lar" %>' />
	
	<aui:input type="hidden" name="key" value=""/>
	
	<div class="options" style="display:none;">
		
		<liferay-ui:message key="what-would-you-like-to-export" />
	
		<%
		String rootPortletId = themeDisplay.getPortletDisplay().getRootPortletId();
		String taglibOnChange = renderResponse.getNamespace() + "toggleChildren(this, '" + renderResponse.getNamespace() + PortletDataHandlerKeys.PORTLET_DATA + StringPool.UNDERLINE + rootPortletId + "Controls');";
		%>

		<aui:input label="data"			name="<%= PortletDataHandlerKeys.PORTLET_DATA + StringPool.UNDERLINE + rootPortletId %>" type="checkbox" value="<%= true %>" onchange="<%= taglibOnChange %>" />
		<aui:input label="categories" 	name="<%= PortletDataHandlerKeys.CATEGORIES %>" 				type="checkbox" value="<%= false %>" />
		<aui:input label="permissions" 	name="<%= PortletDataHandlerKeys.PERMISSIONS %>" 				type="checkbox" value="<%= false %>" />
		<aui:input label="setup" 		name="<%= PortletDataHandlerKeys.PORTLET_SETUP %>" 				type="checkbox" value="<%= false %>" />
		<aui:input label="preferences" 	name="<%= PortletDataHandlerKeys.PORTLET_USER_PREFERENCES %>" 	type="checkbox" value="<%= false %>" />
		<aui:input label="permissions" 	name="<%= PortletDataHandlerKeys.USER_PERMISSIONS %>" 			type="checkbox" value="<%= false %>" />
				
	</div>
		
	<aui:button-row>
		<aui:button id='<%= renderResponse.getNamespace() + "exportButton" %>' value="export" onClick='<%= renderResponse.getNamespace()+"checkExport()" %>'/>
	</aui:button-row>
	
</aui:form>
<div class="aui-helper-hidden" id="<portlet:namespace/>loadingArea"><img alt="loading" src='<%= themeDisplay.getPathThemeImages() + "/application/loading_indicator.gif" %>' /></div>
<%
// Get exported lars
String[] lars = _getExportedLars(themeDisplay.getCompanyId(), GetterUtil.getLong(groupId));

for (String lar : lars) { %>
<div class="lar">
	<span>
		<liferay-portlet:actionURL var="deleteExportedURL" name="deleteExportedCourse">
			<portlet:param name="groupId" value="<%=groupId %>"/>
			<portlet:param name="fileName" value="<%= lar %>"/>
			<portlet:param name="redirect" value="<%= currentURL %>"/>
		</liferay-portlet:actionURL>
		<liferay-ui:icon-delete url="<%= deleteExportedURL %>" />
	</span>
	<span>
		<a href="/liferaylms-portlet/exports/courses/<%= themeDisplay.getCompanyId() %>/<%= groupId %>/<%= lar %>"><%= lar %></a>
	</span>
</div>
<%
}
%>

<%!
public static String[] _getExportedLars(long companyId, long groupId) {
	File directory = new File(PropsUtil.get("liferay.home")+"/data/lms_exports/courses/"+companyId+"/"+groupId);
	if (directory != null && directory.isDirectory()) {
		String[] listFiles = FileUtil.listFiles(directory);
		ArrayUtil.reverse(listFiles);
		return listFiles;
	}
	
	return new String[0];
}
%>