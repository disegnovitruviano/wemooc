package com.liferay.lms.learningactivity;

import java.io.IOException;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import com.liferay.lms.asset.SCORMAssetRenderer;
import com.liferay.lms.model.LearningActivity;
import com.liferay.lms.model.SCORMContent;
import com.liferay.lms.service.ClpSerializer;
import com.liferay.lms.service.SCORMContentLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.upload.UploadRequest;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.DocumentException;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.portal.model.PortletConstants;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.asset.AssetRendererFactoryRegistryUtil;
import com.liferay.portlet.asset.model.AssetEntry;
import com.liferay.portlet.asset.model.AssetRenderer;
import com.liferay.portlet.asset.model.AssetRendererFactory;
import com.liferay.portlet.asset.service.AssetEntryLocalServiceUtil;

public class SCORMLearningActivityType extends BaseLearningActivityType {

	public static String PORTLET_ID = PortalUtil
			.getJsSafePortletId("scormactivity"
					+ PortletConstants.WAR_SEPARATOR
					+ ClpSerializer.getServletContextName());

	@Override
	public boolean gradebook() {
		return false;
	}

	@Override
	public long getDefaultScore() {
		return 100;
	}

	@Override
	public boolean isScoreConfigurable() {
		return true;
	}

	@Override
	public String getName() {

		return "learningactivity.scorm";
	}

	@Override
	public AssetRenderer getAssetRenderer(LearningActivity learningactivity) {

		return new SCORMAssetRenderer(learningactivity);
	}

	@Override
	public long getTypeId() {
		return 9;
	}

	@Override
	public String getExpecificContentPage() {
		return "/html/scormactivity/admin/edit.jsp";
	}

	@Override
	public boolean hasEditDetails() {
		return false;
	}

	@Override
	public void setExtraContent(UploadRequest uploadRequest,
			PortletResponse portletResponse, LearningActivity learningActivity)
			throws PortalException, SystemException, DocumentException,
			IOException {
		
		Document document = null;
		Element rootElement = null;
		if ((learningActivity.getExtracontent() == null)
				|| (learningActivity.getExtracontent().trim().length() == 0)) {
			document = SAXReaderUtil.createDocument();
			rootElement = document.addElement("scorm");
		} else {
			document = SAXReaderUtil.read(learningActivity.getExtracontent());
			rootElement = document.getRootElement();
		}

		Element assetEntry = rootElement.element("assetEntry");
		if (assetEntry != null) {
			assetEntry.detach();
			rootElement.remove(assetEntry);
		}
		assetEntry = SAXReaderUtil.createElement("assetEntry");
		assetEntry.setText(ParamUtil.getString(uploadRequest, "assetEntryId",
				"0"));
		rootElement.add(assetEntry);

		Element openWindow = rootElement.element("openWindow");
		if (openWindow != null) {
			openWindow.detach();
			rootElement.remove(openWindow);
		}
		openWindow = SAXReaderUtil.createElement("openWindow");
		openWindow.setText(ParamUtil
				.getString(uploadRequest, "openWindow", "0"));
		rootElement.add(openWindow);
		
		Element improve=rootElement.element("improve");
		if(improve!=null)
		{
			improve.detach();
			rootElement.remove(improve);
		}
		improve = SAXReaderUtil.createElement("improve");
		improve.setText(Boolean.toString(ParamUtil.get(uploadRequest,"improve",true)));
		rootElement.add(improve);
		
		Element uuid = rootElement.element("uuid");
		if (uuid != null) {
			uuid.detach();
			rootElement.remove(uuid);
		}
		AssetRenderer scorm = null;
		if (Validator.isNotNull(assetEntry.getText())) {
			AssetEntry entry = AssetEntryLocalServiceUtil.getEntry(Long.valueOf(assetEntry.getText()));		
			scorm = AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(entry.getClassName()).getAssetRenderer(entry.getClassPK());
		}
		uuid = SAXReaderUtil.createElement("uuid");
		uuid.setText(scorm != null ? scorm.getUuid() : "");
		
		rootElement.add(uuid);
		
		String team = ParamUtil.getString(uploadRequest, "team","0");
		long teamId = 0;
		if(!team.equalsIgnoreCase("0")){
			teamId = Long.parseLong(team);
		}
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
	public boolean especificValidations(UploadRequest uploadRequest,
			PortletResponse portletResponse) {
		PortletRequest actionRequest = (PortletRequest)uploadRequest.getAttribute(
				JavaConstants.JAVAX_PORTLET_REQUEST);
		boolean validate=true;
		
		if((Validator.isNotNull(uploadRequest.getParameter("assetEntryId")))&&
		   ((!Validator.isNumber(uploadRequest.getParameter("assetEntryId")))||
		    (Long.parseLong(uploadRequest.getParameter("assetEntryId")) <= 0))) {
			SessionErrors.add(actionRequest, "scormactivity.error.invalid-asset");
			validate = false;
		}
		return validate;
	}

	@Override
	public String getDescription() {
		return "learningactivity.scorm.helpmessage";
	}
	
	@Override
	public String getPortletId() {
		return PORTLET_ID;
	}

}
