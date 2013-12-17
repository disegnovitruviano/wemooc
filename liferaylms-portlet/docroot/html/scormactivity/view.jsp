<%@page import="com.liferay.portlet.asset.model.AssetRenderer"%>
<%@page import="com.liferay.portlet.asset.AssetRendererFactoryRegistryUtil"%>
<%@page import="com.liferay.portlet.asset.model.AssetRendererFactory"%>
<%@page import="com.liferay.portlet.asset.service.AssetEntryLocalServiceUtil"%>
<%@page import="com.liferay.portlet.asset.model.AssetEntry"%>
<%@page import="com.liferay.portal.kernel.portlet.LiferayWindowState"%>
<%@page import="com.liferay.portal.kernel.portlet.LiferayPortletURL"%>
<%@page import="com.liferay.taglib.portlet.RenderURLTag"%>
<%@page import="com.liferay.lms.model.LearningActivityResult"%>
<%@page import="com.liferay.lms.service.impl.LearningActivityResultLocalServiceImpl"%>
<%@page import="java.util.HashMap"%>
<%@page import="com.liferay.portal.kernel.xml.Element"%>
<%@page import="com.liferay.portal.kernel.xml.SAXReaderUtil"%>
<%@page import="com.liferay.portal.kernel.xml.Document"%>
<%@page import="java.util.Hashtable"%>
<%@page import="com.liferay.lms.service.LearningActivityResultLocalServiceUtil"%>
<%@page import="com.liferay.lms.model.TestAnswer"%>
<%@page import="com.liferay.lms.service.TestAnswerLocalServiceUtil"%>
<%@page import="com.liferay.lms.model.TestQuestion"%>
<%@page import="com.liferay.lms.service.TestQuestionLocalServiceUtil"%>
<%@page import="com.liferay.portal.service.ServiceContextFactory"%>
<%@page import="com.liferay.portal.service.ServiceContext"%>
<%@page import="com.liferay.lms.service.LearningActivityTryLocalServiceUtil"%>
<%@page import="com.liferay.lms.model.LearningActivityTry"%>
<%@page import="com.liferay.lms.model.LearningActivity"%>
<%@page import="com.liferay.lms.service.LearningActivityLocalServiceUtil"%>
<%@page import="javax.portlet.RenderResponse"%>
<%@page import="com.liferay.lms.model.Course"%>
<%@page import="com.liferay.lms.service.CourseLocalServiceUtil"%>
<%@page import="com.liferay.portal.kernel.util.GetterUtil"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.liferay.portal.kernel.xml.Element"%>
<%@page import="com.liferay.portal.kernel.xml.SAXReaderUtil"%>
<%@page import="com.liferay.util.JavaScriptUtil"%>

<%@ include file="/init.jsp"%>
<div class="container-activity">
<%
	long actId=ParamUtil.getLong(request,"actId",0);
	boolean improve =ParamUtil.getBoolean(request, "improve", true);
	long userId = themeDisplay.getUserId();
	Course course=CourseLocalServiceUtil.fetchByGroupCreatedId(themeDisplay.getScopeGroupId());

	String openWindow = GetterUtil.getString(LearningActivityLocalServiceUtil.getExtraContentValue(actId, "openWindow"), "true");

	//Obtener si puede hacer un intento de mejorar el resultado.
	boolean improving = false;
	LearningActivityResult result = LearningActivityResultLocalServiceUtil.getByActIdAndUserId(actId, userId);
	if (result != null) {
		if (!LearningActivityLocalServiceUtil.islocked(actId, userId)) {
			improving = true;
		}
	}


	if (actId==0) {
		renderRequest.setAttribute(WebKeys.PORTLET_CONFIGURATOR_VISIBILITY, Boolean.FALSE);
	} else {
		LearningActivity activity=LearningActivityLocalServiceUtil.getLearningActivity(actId);
		long typeId=activity.getTypeId();
		
		if( typeId==9
		&& (!LearningActivityLocalServiceUtil.islocked(actId,userId)
		|| permissionChecker.hasPermission( activity.getGroupId(),LearningActivity.class.getName(), actId, ActionKeys.UPDATE)
		|| permissionChecker.hasPermission(themeDisplay.getScopeGroupId(), "com.liferay.lms.model",themeDisplay.getScopeGroupId(),"ACCESSLOCK")))
		{
%>

<h2 class="description-title"><%=activity.getTitle(themeDisplay.getLocale())%></h2>
<h3><liferay-ui:message key="description" /></h3>
<div class="description"><%=activity.getDescription(themeDisplay.getLocale())%></div>

<%
	if((!LearningActivityLocalServiceUtil.islocked(actId,userId)
	|| permissionChecker.hasPermission( activity.getGroupId(), LearningActivity.class.getName(), actId, ActionKeys.UPDATE)
	|| permissionChecker.hasPermission(themeDisplay.getScopeGroupId(), "com.liferay.lms.model",themeDisplay.getScopeGroupId(),"ACCESSLOCK")))
		{		
		if(LearningActivityResultLocalServiceUtil.userPassed(actId,themeDisplay.getUserId()))
		{
			request.setAttribute("learningActivity",activity);
			request.setAttribute("larntry",LearningActivityTryLocalServiceUtil.getLastLearningActivityTryByActivityAndUser(actId, userId));
			Object[] arguments = new Object[] { result.getResult() };
			Object[] arg = new Object[] { activity.getPasspuntuation() };
			%>
			<p>
				<liferay-ui:message key="activity-done" />
			</p>
			<p>
				<liferay-ui:message key="activity.your-result" arguments="<%=arguments%>" />
			</p>
			<p class="color_tercero negrita">
				<liferay-ui:message key="activity.your-result-pass" arguments="<%=arg%>" />
			</p>
			
			<% 
		} 
		if (permissionChecker.hasPermission(activity.getGroupId(), LearningActivity.class.getName(),actId, ActionKeys.UPDATE)
		|| permissionChecker.hasPermission(themeDisplay.getScopeGroupId(), "com.liferay.lms.model",themeDisplay.getScopeGroupId(),"ACCESSLOCK")
	    || improving )
		{

	ServiceContext serviceContext = ServiceContextFactory.getInstance(LearningActivityTry.class.getName(), renderRequest);
	long activityTimestamp=0;
	long timestamp=0;
	
	LearningActivityTry learningTry = LearningActivityTryLocalServiceUtil.getLearningActivityTryNotFinishedByActUser(actId,userId);
	
	//Comprobar si tenemos un try sin fecha de fin, para continuar en ese try.
	if(learningTry == null)
	{
		learningTry =LearningActivityTryLocalServiceUtil.createLearningActivityTry(actId,serviceContext);
	}
	else
	{
		activityTimestamp = GetterUtil.getLong(LearningActivityLocalServiceUtil.getExtraContentValue(activity.getActId(),"timeStamp"));
		timestamp=activityTimestamp*1000 - (new Date().getTime() - learningTry.getStartDate().getTime());
	}
	
	if((activityTimestamp!=0)&&(timestamp<0)){
		request.setAttribute("learningActivity",activity);
		request.setAttribute("larntry",learningTry);
%>
<liferay-util:include page="/html/execactivity/test/expired.jsp"
	servletContext="<%=this.getServletContext()%>">
	<liferay-util:param value="<%=Long.toString(activity.getActId())%>"
		name="actId" />
</liferay-util:include>
<%
	}
	else {
	Object [] arg =  new Object [] { activity.getPasspuntuation() };
%>

<% if ("true".equals(openWindow)) { %>
				<liferay-portlet:renderURL var="scormwindow" windowState="<%= LiferayWindowState.POP_UP.toString() %>">
					<liferay-portlet:param name="jspPage" value="/html/scormactivity/window.jsp"/>
					<liferay-portlet:param name="latId" value="<%= String.valueOf(learningTry.getLatId()) %>"/>
				</liferay-portlet:renderURL>
				
			<script type="text/javascript">
			function _checkPopupBlocker(poppedWindow) {
				setTimeout(function() {
					if (_doCheckPopupBlocker(poppedWindow)) {
						var A = AUI();
						A.one('p.activity-message').setStyle('display', 'block');
						A.one('span.newitem2').setStyle('display', 'block');
					}
				}, 2000);
			}
			function _doCheckPopupBlocker(poppedWindow) {
			    var result = false;
			    try {
			        if (typeof poppedWindow == 'undefined') {
			            // Safari with popup blocker... leaves the popup window handle undefined
			            result = true;
			        }
			        else if (poppedWindow && poppedWindow.closed) {
			            // This happens if the user opens and closes the client window...
			            // Confusing because the handle is still available, but it's in a "closed" state.
			            // We're not saying that the window is not being blocked, we're just saying
			            // that the window has been closed before the test could be run.
			            result = false;
			        }
			        else if (poppedWindow && (poppedWindow.outerHeight > 0 || typeof poppedWindow.outerHeight == 'undefined')) {
			            // This is the actual test. The client window should be fine.
			            result = false;
			        }
			        else {
			            // Else we'll assume the window is not OK
			            result = true;
			        }

			    } catch (err) {
			    	
			    }
			    return result;
			}
			
			Liferay.provide(
					window,
					'<portlet:namespace />abrirActividad',
					function(e) {
						var A = AUI();
						if (e != null && window.<portlet:namespace />ventana != null && !window.<portlet:namespace />ventana.closed) {
							window.<portlet:namespace />ventana.close();
						}
						window.<portlet:namespace />ventana = window.open('','scormactivity','height=768,width=1024,scrollbars=0');
						if (window.<portlet:namespace />ventana != null) {
							window.<portlet:namespace />ventana.location = '<%= scormwindow %>';
							_checkPopupBlocker(window.<portlet:namespace />ventana);
						} else {
							A.one('p.activity-message').setStyle('display', 'block');
							A.one('span.newitem2').setStyle('display', 'block');
						}
					},
					['node']
			);
			
			</script>
			<% if(!LearningActivityResultLocalServiceUtil.userPassed(actId,themeDisplay.getUserId())) { %>
			
			<script type="text/javascript">
				AUI().ready(function() {					
					<portlet:namespace />abrirActividad(null);					
				});
			</script>
			
				<p style="display:none" class="activity-message"><liferay-ui:message key="activity.openwindow"></liferay-ui:message></p>
				
				<span style="display:none" class="newitem2"><a class="newitem2" onclick="<portlet:namespace />abrirActividad(event)" target="scormactivity" href="javascript:void(0)"><liferay-ui:message key="activity.go"></liferay-ui:message></a></span>
			
			<% } else { %>
				
				<p class="activity-message"><liferay-ui:message key="activity.openwindow.passed"></liferay-ui:message></p>
				
				<span class="newitem2"><a class="newitem2" onclick="<portlet:namespace />abrirActividad(event)" target="scormactivity" href="javascript:void(0)"><liferay-ui:message key="activity.go"></liferay-ui:message></a></span>
				<% }
			} else {
				request.setAttribute("learningTry", learningTry);
				%>
			<liferay-util:include page="/html/scormactivity/window.jsp" servletContext="<%=this.getServletContext() %>"></liferay-util:include>
			<%
		}
	}
}
//Si no ha pasado el test, ni tiene mas intentos.
else {
	//LearningActivityResult result = LearningActivityResultLocalServiceUtil.getByActIdAndUserId(actId, userId);
	Object[] arguments = new Object[] { result.getResult() };
	Object[] arg = new Object[] { activity.getPasspuntuation() };
%>
<p>
	<liferay-ui:message key="activity-done" />
</p>
<p>
	<liferay-ui:message key="activity.your-result" arguments="<%=arguments%>" />
</p>
<p class="color_tercero negrita">
	<liferay-ui:message key="activity.your-result-dont-pass" arguments="<%=arg%>" />
</p>
<p>
	<liferay-ui:message key="activity.your-result-no-more-tries" />
</p>
<%
	}

			}
		}
	}
%>
</div>