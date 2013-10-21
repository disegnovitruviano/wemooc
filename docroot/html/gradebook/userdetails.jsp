<%@page import="com.liferay.lms.service.ModuleResultLocalServiceUtil"%>
<%@page import="com.liferay.lms.model.ModuleResult"%>
<%@page import="com.liferay.lms.service.ModuleLocalServiceUtil"%>
<%@page import="com.liferay.lms.model.Module"%>
<%@page import="com.liferay.lms.model.LearningActivityResult"%>
<%@page import="com.liferay.lms.service.LearningActivityResultLocalServiceUtil"%>
<%@page import="com.liferay.portal.kernel.util.ListUtil"%>
<%@page import="com.liferay.lms.service.LearningActivityLocalServiceUtil"%>
<%@page import="com.liferay.lms.service.LearningActivityServiceUtil"%>
<%@page import="com.liferay.lms.model.LearningActivity"%>
<%@ include file="/init.jsp" %>

<%
	long userId=ParamUtil.getLong(request,"userId",0);
	if(userId==0)
	{
		userId=themeDisplay.getUserId();
	}

	String returnurl=ParamUtil.getString(request,"returnurl","");
	User usuario=UserLocalServiceUtil.getUser(userId);
	String title = LanguageUtil.get(pageContext,"results") +" "+ usuario.getFullName();
%>

<liferay-ui:header title="<%= title %>" backURL="<%=returnurl %>"></liferay-ui:header>
<liferay-ui:panel-container >
<%
	java.util.List<Module> modules = ModuleLocalServiceUtil.findAllInGroup(themeDisplay.getScopeGroupId());
	for(Module theModule:modules)
	{
%>
		<liferay-ui:panel id="<%=Long.toString(theModule.getModuleId()) %>" title="<%=theModule.getTitle(themeDisplay.getLocale()) %>" collapsible="true" extended="true" defaultState="collapsed">
		<liferay-ui:search-container  emptyResultsMessage="there-are-no-results" delta="50" deltaConfigurable="false">
	<liferay-ui:search-container-results>
	<% 
	List<LearningActivity> activities=LearningActivityServiceUtil.getLearningActivitiesOfModule(theModule.getModuleId());
	pageContext.setAttribute("results", activities);
    pageContext.setAttribute("total", activities.size());
	%>
	</liferay-ui:search-container-results>
	<liferay-ui:search-container-row className="com.liferay.lms.model.LearningActivity" keyProperty="actId" modelVar="learningActivity">
	<%
				String score= "-";
				String status="not-started";
				if(LearningActivityResultLocalServiceUtil.existsLearningActivityResult(learningActivity.getActId(), usuario.getUserId())){
					status="started";
					LearningActivityResult learningActivityResult=LearningActivityResultLocalServiceUtil.getByActIdAndUserId(learningActivity.getActId(), usuario.getUserId());
					score=(learningActivityResult!=null)?LearningActivityResultLocalServiceUtil.translateResult(themeDisplay, learningActivityResult.getResult(), learningActivity.getGroupId()):"";
					if(learningActivityResult.getEndDate()!=null){
							status="not-passed"	;
					}
					if(learningActivityResult.isPassed()){
						status="passed"	;
					}
				}
				%>
	<liferay-ui:search-container-column-text cssClass="number-column" name = "activity">
				<%=learningActivity.getTitle(themeDisplay.getLocale()) %>
				</liferay-ui:search-container-column-text>
				<liferay-ui:search-container-column-text cssClass="number-column" name = "result">
	
				
				<%=score %>
				</liferay-ui:search-container-column-text>
				<liferay-ui:search-container-column-text cssClass="number-column" name = "status">
	
				<%if(status.equals("passed")){%>
					<liferay-ui:icon image="checked" alt="passed"></liferay-ui:icon>
				<%}
				if(status.equals("not-passed")){%>
					<liferay-ui:icon image="close" alt="not-passed"></liferay-ui:icon>
				<%}
				if(status.equals("started")){%>
					<liferay-ui:icon image="unchecked"></liferay-ui:icon>
				<%}%>
			</liferay-ui:search-container-column-text>
</liferay-ui:search-container-row>
<liferay-ui:search-iterator></liferay-ui:search-iterator>
</liferay-ui:search-container>
	
	</liferay-ui:panel>
	<%
	}%>
</liferay-ui:panel-container>


	
