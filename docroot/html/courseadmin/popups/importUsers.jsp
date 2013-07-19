<%@page import="com.liferay.portal.kernel.servlet.SessionMessages"%>
<%@page import="com.liferay.portal.kernel.servlet.SessionErrors"%>
<%@page import="com.liferay.portal.kernel.portlet.LiferayWindowState"%>
<%@page import="com.liferay.portal.kernel.util.ListUtil"%>
<%@page import="java.util.List"%>
<%@page import="java.io.FileNotFoundException"%>
<%@ include file="/init.jsp" %>

<portlet:renderURL var="importUsersURL"  windowState="<%= LiferayWindowState.EXCLUSIVE.toString() %>">
	<portlet:param name="ajaxAction" value="importUserRole" /> 
	<portlet:param name="courseId" value="<%=ParamUtil.getString(renderRequest, \"courseId\") %>" /> 
	<portlet:param name="roleId" value="<%=ParamUtil.getString(renderRequest, \"roleId\") %>" /> 
	<portlet:param name="jspPage" value="/html/courseadmin/popups/importUsers.jsp" /> 
</portlet:renderURL>

<% if ((!SessionMessages.contains(renderRequest, "courseadmin.importuserrole.csv.saved"))&&(SessionErrors.isEmpty(renderRequest))) { %>

<aui:form name="fm" action="<%=importUsersURL%>"  method="post" enctype="multipart/form-data" target='<%=renderResponse.getNamespace() +"import_frame" %>' >
	<aui:fieldset>
		<aui:field-wrapper label="courseadmin.importuserrole.file" helpMessage="courseadmin.importuserrole.file.help" >
	    	<aui:input inlineLabel="left" inlineField="true" name="fileName" label="" id="fileName" type="file" value="" />
		</aui:field-wrapper>
	</aui:fieldset>
	<aui:button-row>
		<button name="Save" value="save" onclick="AUI().use(function(A) {
	    												A.one('#<portlet:namespace />fm').submit();
	    											  });" type="button">
		<liferay-ui:message key="courseadmin.importuserrole.save" />
		</button>
		<button name="Close" value="close" onclick="AUI().use('aui-dialog', function(A) {
		    												A.DialogManager.closeByChild('#<portlet:namespace />showPopupImportUsers');
		    											  });" type="button">
			<liferay-ui:message key="courseadmin.importuserrole.cancel" />
		</button>
	</aui:button-row>
</aui:form>
<% } %>
	<div id="<portlet:namespace />uploadMessages" >
		<liferay-ui:success key="courseadmin.importuserrole.csv.saved" message="courseadmin.importuserrole.csv.saved" />
		<liferay-ui:error key="courseadmin.importuserrole.csv.fileRequired" message="courseadmin.importuserrole.csv.fileRequired" />
		<liferay-ui:error key="courseadmin.importuserrole.csv.badFormat" message="courseadmin.importuserrole.csv.badFormat" />
		<% if(SessionErrors.contains(renderRequest, "courseadmin.importuserrole.csvErrors")) { %>
		<div class="portlet-msg-error">
			<% List<String> errors = (List<String>)SessionErrors.get(renderRequest, "courseadmin.importuserrole.csvErrors");
			   if(errors.size()==1) {
				  %><%=errors.get(0) %><%
			   }	
			   else {
			%>
				<ul>
				<% for(String error : errors){ %>
				 	<li><%=error %></li>
				<% } %>
				</ul>
			<% } %>
		</div>
		<% } %>
	</div>
