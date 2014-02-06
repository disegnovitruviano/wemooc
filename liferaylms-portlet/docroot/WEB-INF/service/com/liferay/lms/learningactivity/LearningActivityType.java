package com.liferay.lms.learningactivity;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletResponse;

import com.liferay.lms.model.LearningActivity;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.upload.UploadRequest;
import com.liferay.portal.kernel.xml.DocumentException;
import com.liferay.portal.service.InvokableService;
import com.liferay.portlet.asset.model.AssetRenderer;


public interface LearningActivityType extends InvokableService
{
	public long getDefaultScore();
	public long getDefaultTries();
	public long getTypeId();
	public String getDefaultFeedbackCorrect();
	public String getDefaultFeedbackNoCorrect();
	public boolean isScoreConfigurable();
	public boolean isTriesConfigurable();
	public boolean isFeedbackCorrectConfigurable();
	public boolean isFeedbackNoCorrectConfigurable();
	public String getName();
	public AssetRenderer getAssetRenderer(LearningActivity larn);
	public String getUrlIcon();
	public String getDescription();
	public boolean gradebook();
	String getMesageEditDetails();
	public boolean hasEditDetails();
	public boolean hasDeleteTries();
	
	public String getExpecificContentPage();
	public void setExtraContent(UploadRequest uploadRequest,PortletResponse portletResponse,LearningActivity learningActivity) throws PortalException,SystemException,DocumentException,IOException, NumberFormatException, Exception;
	public boolean especificValidations(UploadRequest uploadRequest,PortletResponse portletResponse);
	public void afterInsertOrUpdate(UploadRequest uploadRequest,PortletResponse portletResponse,LearningActivity learningActivity) throws PortalException,SystemException;
	public String getPortletId();
	public void deleteResources(ActionRequest actionRequest,ActionResponse actionResponse,LearningActivity larn) throws PortalException,SystemException,DocumentException,IOException;
}
