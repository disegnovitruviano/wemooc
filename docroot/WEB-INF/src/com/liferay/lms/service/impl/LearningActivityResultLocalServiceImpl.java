/**
 * 2012 TELEFONICA LEARNING SERVICES. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.lms.service.impl;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.liferay.lms.learningactivity.calificationtype.CalificationType;
import com.liferay.lms.learningactivity.calificationtype.CalificationTypeRegistry;
import com.liferay.lms.learningactivity.questiontype.QuestionType;
import com.liferay.lms.learningactivity.questiontype.QuestionTypeRegistry;
import com.liferay.lms.model.Course;
import com.liferay.lms.model.LearningActivity;
import com.liferay.lms.model.LearningActivityResult;
import com.liferay.lms.model.LearningActivityTry;
import com.liferay.lms.service.ClpSerializer;
import com.liferay.lms.service.LearningActivityLocalServiceUtil;
import com.liferay.lms.service.LearningActivityTryLocalServiceUtil;
import com.liferay.lms.service.ModuleResultLocalServiceUtil;
import com.liferay.lms.service.base.LearningActivityResultLocalServiceBaseImpl;
import com.liferay.portal.kernel.bean.PortletBeanLocatorUtil;
import com.liferay.portal.kernel.dao.orm.Criterion;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.ProjectionFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.DocumentException;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.portlet.asset.model.AssetEntry;
import com.liferay.portlet.asset.service.AssetEntryLocalServiceUtil;


/**
 * The implementation of the learning activity result local service.
 *
 * <p>
 * All custom service methods should be put in this class. Whenever methods are added, rerun ServiceBuilder to copy their definitions into the {@link com.liferay.lms.service.LearningActivityResultLocalService} interface.
 * </p>
 *
 * <p>
 * Never reference this interface directly. Always use {@link com.liferay.lms.service.LearningActivityResultLocalServiceUtil} to access the learning activity result local service.
 * </p>
 *
 * <p>
 * This is a local service. Methods of this service will not have security checks based on the propagated JAAS credentials because this service can only be accessed from within the same VM.
 * </p>
 *
 * @author cvicente
 * @see com.liferay.lms.service.base.LearningActivityResultLocalServiceBaseImpl
 * @see com.liferay.lms.service.LearningActivityResultLocalServiceUtil
 */
public class LearningActivityResultLocalServiceImpl
	extends LearningActivityResultLocalServiceBaseImpl {
	public LearningActivityResult update(LearningActivityTry learningActivityTry) throws SystemException, PortalException
	{
		LearningActivityResult learningActivityResult=null;
		long actId=learningActivityTry.getActId();
		long userId=learningActivityTry.getUserId();
		LearningActivity learningActivity=LearningActivityLocalServiceUtil.getLearningActivity(actId);
		if(!existsLearningActivityResult(actId, userId))
		{	
			learningActivityResult=
				learningActivityResultPersistence.create(counterLocalService.increment(
						LearningActivityResult.class.getName()));
			learningActivityResult.setStartDate(learningActivityTry.getStartDate());
			learningActivityResult.setActId(actId);
			learningActivityResult.setUserId(userId);
			learningActivityResult.setPassed(false);
		}
		else
		{
			learningActivityResult=learningActivityResultPersistence.fetchByact_user(actId, userId);
		}
		if(learningActivityTry.getEndDate()!=null)
		{
			learningActivityResult.setEndDate(learningActivityTry.getEndDate());
			if(learningActivityTry.getResult()>learningActivityResult.getResult())
			{
				
			
				learningActivityResult.setResult(learningActivityTry.getResult());
			}
			if(!learningActivityResult.getPassed())
			{
				if(learningActivityTry.getResult()>=learningActivity.getPasspuntuation())
				{
					learningActivityResult.setPassed(true);
				  
				}
			}
			
			learningActivityResult.setComments(learningActivityTry.getComments());
		}
		learningActivityResultPersistence.update(learningActivityResult, true);
		if(learningActivityResult.getPassed()==true)
		{
			ModuleResultLocalServiceUtil.update(learningActivityResult);
		}
		return learningActivityResult;
		
	}
	public LearningActivityResult update(long latId, long result, String tryResultData, long userId) throws SystemException, PortalException {
		LearningActivityTry learningActivityTry = LearningActivityTryLocalServiceUtil.getLearningActivityTry(latId);
		if (userId != learningActivityTry.getUserId()) {
			throw new PortalException();
		}
		if (result >= 0L) {
			learningActivityTry.setResult(result);
			
			Date endDate = new Date(System.currentTimeMillis());
			learningActivityTry.setEndDate(endDate);
		}
		learningActivityTry.setTryResultData(tryResultData);
		LearningActivityTryLocalServiceUtil.updateLearningActivityTry(learningActivityTry);
		
		return update(learningActivityTry);
	}
	public LearningActivityResult update(long latId, String tryResultData, long userId) throws SystemException, PortalException {
		LearningActivityTry learningActivityTry = LearningActivityTryLocalServiceUtil.getLearningActivityTry(latId);
		if (userId != learningActivityTry.getUserId()) {
			throw new PortalException();
		}
		
		LearningActivity learningActivity = LearningActivityLocalServiceUtil.getLearningActivity(learningActivityTry.getActId());
		String assetEntryId = LearningActivityLocalServiceUtil.getExtraContentValue(learningActivityTry.getActId(), "assetEntry");
		AssetEntry assetEntry = AssetEntryLocalServiceUtil.getAssetEntry(Long.valueOf(assetEntryId));		
		
		List<String> manifestItems = new ArrayList<String>();
		Map<String, String> recursos = new HashMap<String, String>();
		
		Map<String, String> manifestResources = new HashMap<String, String>();
		
		try {
			String urlString = assetEntry.getUrl();
			if (Validator.isNotNull(urlString)) {
				Document imsdocument = null;
				URL url = new URL(urlString);
				if (urlString.startsWith("http://") || urlString.startsWith("https://")) {
					imsdocument = SAXReaderUtil.read(new URL(urlString).openStream());
				}
				if (urlString.startsWith("file://")) {
					imsdocument = SAXReaderUtil.read(new File( URLDecoder.decode( url.getFile(), "UTF-8" ) ));
				}
				List<Element> resources = new ArrayList<Element>();
				resources = imsdocument.getRootElement().element("resources").elements("resource");
				for(Element resource : resources) {
					String identifier = resource.attributeValue("identifier");
					String type = resource.attributeValue("scormType");
					String type2 = resource.attributeValue("scormtype");
					manifestResources.put(identifier, type != null ? type : type2);
				}
				
				List<Element> items = new ArrayList<Element>();
				items.addAll(imsdocument.getRootElement().element("organizations").elements("organization").get(0).elements("item"));
				for (int i = 0; i < items.size(); i++) {
					Element item = items.get(i);
					String identifier = item.attributeValue("identifier");
					String identifierref = item.attributeValue("identifierref");
					manifestItems.add(identifier);
					recursos.put(identifier, identifierref);
					items.addAll(item.elements("item"));
				}
			}
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Long master_score = new Integer(learningActivity.getPasspuntuation()).longValue();
		
		JSONObject scorm = JSONFactoryUtil.createJSONObject();
		scorm = JSONFactoryUtil.createJSONObject(tryResultData);
		
		JSONObject organizations = scorm.getJSONObject("organizations");
		JSONArray organizationNames = organizations.names();
		JSONObject organization = organizations.getJSONObject(organizationNames.getString(0));
		
		JSONObject cmis = organization.getJSONObject("cmi");
		JSONArray cmiNames = cmis.names();
		
		List<String> completion_statuses = new ArrayList<String>();
		List<Long> scores = new ArrayList<Long>();
		
		String total_completion_status = "not attempted";
		Double total_score = 0.0;
		
		for (int i = 0; i < cmiNames.length(); i++) {
			JSONObject cmi = cmis.getJSONObject(cmiNames.getString(0));
			String typeCmi = manifestResources.get(recursos.get(cmiNames.getString(i)));
			
			String completion_status = null;
			String success_status = null;
			Long max_score = null;
			Long min_score = null;
			Long raw_score = null;
			Long scaled_score = null;
			
			if (cmi.getJSONObject("cmi.core.lesson_status") != null) { // 1.2
				String lesson_status = cmi.getJSONObject("cmi.core.lesson_status").getString("value");
				//"passed", "completed", "failed", "incomplete", "browsed", "not attempted"
				if ("passed".equals(lesson_status)) {
					success_status = "passed";
					completion_status = "completed";
				} else if ("failed".equals(lesson_status)) {
					success_status = "failed";
					completion_status = "completed";
				} else if ("completed".equals(lesson_status)) { 
					success_status = "unknown"; // or passed
					completion_status = "completed";
				} else if ("browsed".equals(lesson_status)) {
					success_status = "passed";
					completion_status = "completed";
				} else if ("incomplete".equals(lesson_status)) {
					success_status = "unknown";
					completion_status = "incomplete";
				} else if ("not attempted".equals(lesson_status)) {
					success_status = "unknown";
					completion_status = "not attempted";
				}
				max_score = cmi.getJSONObject("cmi.core.score.max").getLong("value", 100L);
				min_score = cmi.getJSONObject("cmi.core.score.min").getLong("value", 0L);
				raw_score = cmi.getJSONObject("cmi.core.score.raw").getLong("value", "completed".equals(completion_status) || "asset".equals(typeCmi) ? 100L : 0L);
				scaled_score = new Long(Math.round((raw_score * 100L) / (max_score - min_score)));
			} else { // 1.3
				//"completed", "incomplete", "not attempted", "unknown"
				completion_status = cmi.getJSONObject("cmi.completion_status").getString("value");
				//"passed", "failed", "unknown"
				success_status = cmi.getJSONObject("cmi.success_status").getString("value");
				max_score = cmi.getJSONObject("cmi.score.max").getLong("value", 100L);
				min_score = cmi.getJSONObject("cmi.score.min").getLong("value", 0L);
				raw_score = cmi.getJSONObject("cmi.score.raw").getLong("value", "completed".equals(completion_status) || "asset".equals(typeCmi) ? 100L : 0L);
				scaled_score = cmi.getJSONObject("cmi.score.scaled").getLong("value", Math.round((raw_score * 100L) / (max_score - min_score)));
			}
			completion_statuses.add(completion_status);
			scores.add(scaled_score);
		}
		
		if (manifestItems.size() <= 1) {
			if (completion_statuses.size() == 1) {
				total_completion_status = completion_statuses.get(0);
			}
		} else {
			if (completion_statuses.size() < manifestItems.size()) {
				if (completion_statuses.size() <= 1) {
					total_completion_status = completion_statuses.get(0).equals("completed") ? "incomplete" : completion_statuses.get(0);
				} else {
					total_completion_status = "incomplete";
				}
			} else if (completion_statuses.size() == manifestItems.size()) {
				for (int i = 0; i < completion_statuses.size(); i++) {
					total_score += scores.get(i);
					if ("incomplete".equals(completion_statuses.get(i))) {
						total_completion_status = "incomplete";
						break;
					}
					if ("completed".equals(completion_statuses.get(i))) {
						if ("not attempted".equals(total_completion_status)) {
							total_completion_status = "completed";
						}
						if ("unknown".equals(total_completion_status)) {
							total_completion_status = "incomplete";
							break;
						}
					}
					if ("not attempted".equals(completion_statuses.get(i))) {
						if ("completed".equals(total_completion_status)) {
							total_completion_status = "incomplete";
							break;
						}
						if ("unknown".equals(total_completion_status)) {
							total_completion_status = "unknown";
						}
					}
					if ("unknown".equals(completion_statuses.get(i))) {
						if ("completed".equals(total_completion_status)) {
							total_completion_status = "incomplete";
							break;
						}
						if ("unknown".equals(total_completion_status) || "not attempted".equals(total_completion_status)) {
							total_completion_status = "unknown";
						}
					}
				}
			}
		}
		
		for (int i = 0; i < scores.size(); i++) {
			total_score += scores.get(i);
		}
		total_score = total_score / (manifestItems.size() > 0 ? manifestItems.size() : 1);
		
		if ("incomplete".equals(total_completion_status) || "completed".equals(total_completion_status)) {
			learningActivityTry.setTryResultData(tryResultData);
			learningActivityTry.setResult(Math.round(total_score));
			
			if (Math.round(total_score) >= master_score) {
				Date endDate = new Date(System.currentTimeMillis());
				learningActivityTry.setEndDate(endDate);
			}
			
			LearningActivityTryLocalServiceUtil.updateLearningActivityTry(learningActivityTry);
			
		}
		
		return this.getByActIdAndUserId(learningActivityTry.getActId(), userId);
	}
	public boolean existsLearningActivityResult(long actId,long userId) throws SystemException
	{
		if(learningActivityResultPersistence.countByact_user(actId, userId)>0)
		{
			return true;
		}
		else
		{
		 
			return false;
		}
	}
	public boolean userPassed(long actId,long userId) throws SystemException
	{
		if(!existsLearningActivityResult(actId, userId))
		{
			return false;
		}
		else
		{
			return getByActIdAndUserId(actId, userId).isPassed();
		}
	}
	public long countPassed(long actId) throws SystemException
	{
		return learningActivityResultPersistence.countByap(actId, true);
	}
	public long countNotPassed(long actId) throws SystemException
	{
		ClassLoader classLoader = (ClassLoader) PortletBeanLocatorUtil.locate(ClpSerializer.getServletContextName(), "portletClassLoader"); 
		DynamicQuery dq=DynamicQueryFactoryUtil.forClass(LearningActivityResult.class, classLoader);
		Criterion criterion=PropertyFactoryUtil.forName("passed").eq(false);
		dq.add(criterion);
		criterion=PropertyFactoryUtil.forName("actId").eq(actId);
		dq.add(criterion);
		criterion=PropertyFactoryUtil.forName("endDate").isNotNull();
		dq.add(criterion);
		return learningActivityResultPersistence.countWithDynamicQuery(dq);
	}
	public Double avgResult(long actId) throws SystemException
	{
		ClassLoader classLoader = (ClassLoader) PortletBeanLocatorUtil.locate(ClpSerializer.getServletContextName(), "portletClassLoader"); 
		DynamicQuery dq=DynamicQueryFactoryUtil.forClass(LearningActivityResult.class, classLoader);
		Criterion criterion=PropertyFactoryUtil.forName("actId").eq(actId);
		dq.add(criterion);
		criterion=PropertyFactoryUtil.forName("endDate").isNotNull();
		dq.add(criterion);
		dq.setProjection(ProjectionFactoryUtil.avg("result"));
		return (Double)(learningActivityResultPersistence.findWithDynamicQuery(dq).get(0));
	}
	public long countStarted(long actId) throws SystemException
	{
		return learningActivityResultPersistence.countByac(actId);
	}
	public double triesPerUser(long actId) throws SystemException
	{
		long tries=learningActivityTryPersistence.countByact(actId);
		long started=countStarted(actId);
		if(started==0)
		{
			return 0;
		}
		return ((double) tries)/((double) started);
	}
	public LearningActivityResult getByActIdAndUserId(long actId,long userId) throws SystemException
	{
		return learningActivityResultPersistence.fetchByact_user(actId, userId);
	}
	
	public List<LearningActivityResult> getByActId(long actId) throws SystemException
	{
		List<LearningActivityResult> results;
		
		ClassLoader classLoader = (ClassLoader) PortletBeanLocatorUtil.locate(ClpSerializer.getServletContextName(), "portletClassLoader"); 
		DynamicQuery consulta = DynamicQueryFactoryUtil.forClass(LearningActivityResult.class, classLoader)
					.add(PropertyFactoryUtil.forName("actId").eq(new Long(actId)));
					
		results = (List<LearningActivityResult>)learningActivityResultPersistence.findWithDynamicQuery(consulta);

		return results;	
		
	}
	
	public String translateResult(double result, long groupId){
		String translatedResult = "";
		try {
			Course curso = courseLocalService.getCourseByGroupCreatedId(groupId);
			if(curso != null){
				CalificationType ct = new CalificationTypeRegistry().getCalificationType(curso.getCalificationType());
				translatedResult = ct.translate(result);
			}
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return translatedResult;
	}
}