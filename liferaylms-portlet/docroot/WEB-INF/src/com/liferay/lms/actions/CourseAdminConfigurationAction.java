package com.liferay.lms.actions;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.liferay.portal.kernel.portlet.ConfigurationAction;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portlet.PortletPreferencesFactoryUtil;

public class CourseAdminConfigurationAction implements ConfigurationAction {
	public static final String JSP = "/html/courseadmin/config/edit.jsp";

	public String render(PortletConfig config, RenderRequest renderRequest, RenderResponse renderResponse) throws Exception 
	{
		return JSP; 
	}
	
	public void processAction( 
			PortletConfig portletConfig, ActionRequest actionRequest, 
			ActionResponse actionResponse) 
		throws Exception { 
		
		if (!Constants.UPDATE.equals(actionRequest.getParameter(Constants.CMD))) {
			return;
		} 
		
		PortletPreferences portletPreferences =
		PortletPreferencesFactoryUtil.getPortletSetup( 
				actionRequest, ParamUtil.getString(actionRequest, "portletResource")); 
		
		portletPreferences.setValue("showInscriptionDate",Boolean.toString(ParamUtil.getBoolean(actionRequest, "inscriptionDate",true)));
		portletPreferences.setValue("categories",Boolean.toString(ParamUtil.getBoolean(actionRequest, "categories",true)));
		portletPreferences.setValue("showcatalog",Boolean.toString(ParamUtil.getBoolean(actionRequest, "showcatalog",true)));
		portletPreferences.setValue("courseTemplates",	StringUtil.merge(actionRequest.getParameterMap().get( "courseTemplates")));
		
		portletPreferences.setValue("showClose",	Boolean.toString(ParamUtil.getBoolean(actionRequest, "showClose",	true)));
		portletPreferences.setValue("showDelete",	Boolean.toString(ParamUtil.getBoolean(actionRequest, "showDelete",	true)));
		portletPreferences.setValue("showMembers",	Boolean.toString(ParamUtil.getBoolean(actionRequest, "showMembers",	true)));
		portletPreferences.setValue("showExport",	Boolean.toString(ParamUtil.getBoolean(actionRequest, "showExport",	true)));
		portletPreferences.setValue("showImport",	Boolean.toString(ParamUtil.getBoolean(actionRequest, "showImport",	true)));
		portletPreferences.setValue("showClone",	Boolean.toString(ParamUtil.getBoolean(actionRequest, "showClone",	true)));
		portletPreferences.setValue("showGo",		Boolean.toString(ParamUtil.getBoolean(actionRequest, "showGo",		true)));
		portletPreferences.setValue("showPermission",Boolean.toString(ParamUtil.getBoolean(actionRequest, "showPermission",	true)));
		portletPreferences.setValue("showRegistrationType", Boolean.toString(ParamUtil.getBoolean(actionRequest, "showRegistrationType",	true)));
		portletPreferences.setValue("showMaxUsers", Boolean.toString(ParamUtil.getBoolean(actionRequest, "showMaxUsers",	true)));
		
		portletPreferences.setValue("showSearchTags",Boolean.toString(ParamUtil.getBoolean(actionRequest, "showSearchTags",	false)));
		portletPreferences.setValue("showWelcomeMsg",Boolean.toString(ParamUtil.getBoolean(actionRequest, "showWelcomeMsg",	true)));
		
		portletPreferences.store();
		SessionMessages.add( 
				actionRequest, portletConfig.getPortletName() + ".doConfigure"); 

		
	} 
}
