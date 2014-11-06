package com.liferay.lms.learningactivity;

import java.io.IOException;

import javax.portlet.PortletResponse;

import com.liferay.lms.asset.TaskOnlineAssetRenderer;
import com.liferay.lms.model.LearningActivity;
import com.liferay.lms.model.LearningActivityTry;
import com.liferay.lms.service.ClpSerializer;
import com.liferay.lms.service.LearningActivityLocalServiceUtil;
import com.liferay.lms.service.LearningActivityTryLocalServiceUtil;
import com.liferay.portal.kernel.dao.orm.Criterion;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.upload.UploadRequest;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.DocumentException;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.portal.model.PortletConstants;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.asset.model.AssetRenderer;

public class TaskOnlineLearningActivityType extends BaseLearningActivityType {

	public static String PORTLET_ID = 
			PortalUtil.getJsSafePortletId(
					"onlinetaskactivity" + PortletConstants.WAR_SEPARATOR + ClpSerializer.getServletContextName());
	
	@Override
	public AssetRenderer getAssetRenderer(LearningActivity larn) throws SystemException, PortalException {
		return new TaskOnlineAssetRenderer(larn,this);
	}

	@Override
	public String getName() {	
		return "learningactivity.online";
	}
	
	@Override
	public boolean isScoreConfigurable() {
		return true;
	}

	@Override
	public long getTypeId() {
		return 6;
	}
	@Override
	public long getDefaultTries() {
		// TODO Auto-generated method stub
		return 1;
	}	
	@Override
	public String getExpecificContentPage() {
		return "/html/onlinetaskactivity/admin/edit.jsp";
	}
	
	@Override
	public boolean hasEditDetails() {
		return false;
	}
	
	@Override
	public void setExtraContent(UploadRequest uploadRequest,
			PortletResponse portletResponse, LearningActivity learningActivity)
			throws NumberFormatException, Exception {
		
		ThemeDisplay themeDisplay = (ThemeDisplay) uploadRequest.getAttribute(WebKeys.THEME_DISPLAY);
		
			String fichero = ParamUtil.getString(uploadRequest, "fichero", StringPool.FALSE);
			String textoenr = ParamUtil.getString(uploadRequest, "textoenr", StringPool.FALSE);
			String team = ParamUtil.getString(uploadRequest, "team","0");
			long teamId = 0;
			if(!team.equalsIgnoreCase("0")){
				teamId = Long.parseLong(team);
			}
			
			Document document = null;
			Element rootElement = null;
			if((learningActivity.getExtracontent()==null)||(learningActivity.getExtracontent().trim().length()==0)){
				document = SAXReaderUtil.createDocument();
				rootElement = document.addElement("online");
			}
			else
			{
				document=SAXReaderUtil.read(learningActivity.getExtracontent());
				rootElement =document.getRootElement();
			}
			
			Element ficheroXML=rootElement.element("fichero");
			if(ficheroXML!=null)
			{
				ficheroXML.detach();
				rootElement.remove(ficheroXML);
			}
			ficheroXML=SAXReaderUtil.createElement("fichero");
			ficheroXML.addText(fichero);
			rootElement.add(ficheroXML);
			
			Element textoenrXML=rootElement.element("textoenr");
			if(textoenrXML!=null)
			{
				textoenrXML.detach();
				rootElement.remove(textoenrXML);
			}
			textoenrXML=SAXReaderUtil.createElement("textoenr");
			textoenrXML.addText(textoenr);
			rootElement.add(textoenrXML);
			
			Element teamElement=rootElement.element("team");
			if(teamElement!=null)
			{
				teamElement.detach();
				rootElement.remove(teamElement);
			}
			if(teamId!=0){
				teamElement = SAXReaderUtil.createElement("team");
				teamElement.setText(Long.toString(teamId));
				rootElement.add(teamElement);
			}
			learningActivity.setExtracontent(document.formattedString());
		
	}
	
	@Override
	public String getDescription() {
		return "learningactivity.online.helpmessage";
	}
	
	@Override
	public String getPortletId() {
		return PORTLET_ID;
	}
	
	@Override
	public boolean hasDeleteTries() {
		return true;
	}
}
