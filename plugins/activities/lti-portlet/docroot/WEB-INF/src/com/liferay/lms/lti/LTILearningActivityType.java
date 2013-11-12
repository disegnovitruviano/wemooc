package com.liferay.lms.lti;

import com.liferay.lms.learningactivity.BaseLearningActivityType;
import com.liferay.lms.lti.asset.LTIAssetRenderer;
import com.liferay.lms.lti.service.ClpSerializer;
import com.liferay.lms.model.LearningActivity;
import com.liferay.portal.model.PortletConstants;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.asset.model.AssetRenderer;

public class LTILearningActivityType extends BaseLearningActivityType {
	public static String PORTLET_ID = 
			PortalUtil.getJsSafePortletId(
					"lti" + PortletConstants.WAR_SEPARATOR + ClpSerializer.getServletContextName());

	@Override
	public AssetRenderer getAssetRenderer(LearningActivity larn) {
		return new LTIAssetRenderer(larn);
	}

	@Override
	public String getName() {
		return "learningactivity.lti";
	}

	@Override
	public String getDescription() {
		return "learningactivity.lti.helpmessage";
	}

	@Override
	public String getPortletId() {
		return PORTLET_ID;
	}

	@Override
	public long getTypeId() {
		return 104;
	}

	@Override
	public boolean isTriesConfigurable() {
		return true;
	}
}
