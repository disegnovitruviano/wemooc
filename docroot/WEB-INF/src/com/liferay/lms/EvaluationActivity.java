package com.liferay.lms;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.liferay.lms.learningactivity.TaskEvaluationLearningActivityType;
import com.liferay.lms.model.LearningActivity;
import com.liferay.lms.model.LearningActivityResult;
import com.liferay.lms.model.LearningActivityTry;
import com.liferay.lms.service.LearningActivityLocalServiceUtil;
import com.liferay.lms.service.LearningActivityResultLocalServiceUtil;
import com.liferay.lms.service.LearningActivityTryLocalServiceUtil;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.ProjectionFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBusUtil;
import com.liferay.portal.kernel.messaging.MessageListener;
import com.liferay.portal.kernel.messaging.MessageListenerException;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.DateFormatFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.DocumentException;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.portal.model.ResourceConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.util.bridges.mvc.MVCPortlet;


/**
 * Portlet implementation class EvaluationActivity
 */
public class EvaluationActivity extends MVCPortlet implements MessageListener{
	
	private static DateFormat _dateFormat = DateFormatFactoryUtil.getSimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:sszzz",Locale.US);
	
	public static final String NOT_TEACHER_SQL = "WHERE User_.userId NOT IN "+
			 "( SELECT Usergrouprole.userId "+
			 "    FROM Usergrouprole "+ 
			 "   INNER JOIN Resourcepermission ON Usergrouprole.roleId = Resourcepermission.roleId "+
			 "   INNER JOIN Resourceaction ON Resourcepermission.name = Resourceaction.name "+
			 "	   					      AND (BITAND(CAST_LONG(ResourcePermission.actionIds), CAST_LONG(ResourceAction.bitwiseValue)) != 0)"+
			 "   WHERE Resourcepermission.scope="+ResourceConstants.SCOPE_GROUP_TEMPLATE+
			 "     AND Resourceaction.actionId = 'VIEW_RESULTS' "+
			 "     AND Resourceaction.name='com.liferay.lms.model' "+
			 "     AND Usergrouprole.groupid=? ) ";

	public static final String COURSE_RESULT_PASSED_SQL = "WHERE (EXISTS (SELECT 1 FROM lms_learningactivityresult " +
			"WHERE User_.userId = lms_courseresult.userId " +
			" AND lms_courseresult.passed > 0 AND lms_courseresult.courseId = ? ))"; 

	public static final String COURSE_RESULT_FAIL_SQL = "WHERE (EXISTS (SELECT 1 FROM lms_learningactivityresult " +
			"WHERE User_.userId = lms_courseresult.userId " +
			" AND lms_courseresult.passed = 0 AND lms_courseresult.courseId = ? ))"; 

	public static final String COURSE_RESULT_NO_CALIFICATION_SQL = "WHERE (NOT EXISTS (SELECT 1 FROM lms_learningactivityresult " +
			"WHERE User_.userId = lms_courseresult.userId AND lms_courseresult.courseId = ? ))"; 
	
	private static Log _log = LogFactoryUtil.getLog(EvaluationActivity.class);
	
	@Override
	@SuppressWarnings("unchecked")
	public void receive(Message message) throws MessageListenerException {
		long actId = message.getLong("actId");
		
		if(actId!=0){
			try {
				evaluate(actId);
			} catch (Exception e) {
				_log.error("Error during evaluation: "+actId, e);
			}
		}
		else{
			// Scheduler trigger this execution. We must evaluate all activities.
			
			try {
				
				for (LearningActivity learningActivity : (List<LearningActivity>)LearningActivityLocalServiceUtil.dynamicQuery(
						DynamicQueryFactoryUtil.forClass(LearningActivity.class).
						add(PropertyFactoryUtil.forName("typeId").eq((int)new TaskEvaluationLearningActivityType().getTypeId())))) {
					try {
						evaluate(learningActivity.getActId());
					} catch (Exception e) {
						_log.error("Error during evaluation: "+actId, e);
					}					
				}
			} catch (SystemException e) {
				_log.error("Error during evaluation job ");
			}

		}
		
	}	

	private double calculateMean(double[] values, double[] weights) {
		int i;
		double sumWeight=0;
		for (i = 0; i < weights.length; i++) {
			sumWeight+=weights[i];
		}
		
		double mean=0;
		for (i = 0; i < values.length; i++) {
			mean+=weights[i]*values[i];
		}
		mean/=sumWeight;
		
		//Correction factor
		double correction=0;
		for (i = 0; i < values.length; i++) {
			correction += weights[i] * (values[i] - mean);
		}
		
		return mean + (correction/sumWeight);
	}
	
	private void updateLearningActivityTryAndResult(
			LearningActivityTry learningActivityTry) throws PortalException,
			SystemException {
		LearningActivityTryLocalServiceUtil.updateLearningActivityTry(learningActivityTry);
		
		LearningActivityResult learningActivityResult = LearningActivityResultLocalServiceUtil.getByActIdAndUserId(learningActivityTry.getActId(), learningActivityTry.getUserId());
		if(learningActivityResult.getResult() != learningActivityTry.getResult()) {
			LearningActivity learningActivity = LearningActivityLocalServiceUtil.getLearningActivity(learningActivityTry.getActId());
			learningActivityResult.setResult(learningActivityTry.getResult());
			learningActivityResult.setPassed(learningActivityTry.getResult()>=learningActivity.getPasspuntuation());
			learningActivityResult.setComments(learningActivityTry.getComments());
			LearningActivityResultLocalServiceUtil.updateLearningActivityResult(learningActivityResult);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Map<Long, Long> getLearningActivities(
			LearningActivity learningActivity) throws DocumentException, SystemException {
		
		Map<Long,Long> activities= new HashMap<Long,Long>();
		if((learningActivity.getExtracontent()!=null)&&(learningActivity.getExtracontent().length()!=0)) {
			Element activitiesElement = SAXReaderUtil.read(learningActivity.getExtracontent()).getRootElement().element("activities");
			
			if(activitiesElement!=null){
				Iterator<Element> activitiesElementItr = activitiesElement.elementIterator();
				while(activitiesElementItr.hasNext()) {
					Element activity =activitiesElementItr.next();
					if(("activity".equals(activity.getName()))&&(activity.attribute("id")!=null)&&(activity.attribute("id").getValue().length()!=0)){
						try{
							activities.put(Long.valueOf(activity.attribute("id").getValue()),Long.valueOf(activity.getText()));
						}
						catch(NumberFormatException e){}
					}
				}				
			}
	
			List<Long> actIdsInDatabase = 
					LearningActivityLocalServiceUtil.dynamicQuery(
					DynamicQueryFactoryUtil.forClass(LearningActivity.class)
					.add(PropertyFactoryUtil.forName("typeId").ne(8))
					.add(PropertyFactoryUtil.forName("actId").in((Collection<Object>)(Collection<?>)activities.keySet()))
					.setProjection(ProjectionFactoryUtil.property("actId")));
			
			Iterator<Map.Entry<Long,Long>> activitiesIterator = activities.entrySet().iterator();
			while (activitiesIterator.hasNext()) {
				if(!actIdsInDatabase.contains(activitiesIterator.next().getKey())){
					activitiesIterator.remove();
			    }
			}
		}
		
		
		return activities;
	}
		
	private void evaluate(long actId)
			throws Exception {
		
		LearningActivity learningActivity = LearningActivityLocalServiceUtil.getLearningActivity(actId);

		try{

			Map<Long, Long> activities = getLearningActivities(learningActivity);
		
			if(activities.size()!=0){
				for(User user:UserLocalServiceUtil.getGroupUsers(learningActivity.getGroupId())) {
					if(!PermissionCheckerFactoryUtil.create(user).hasPermission(learningActivity.getGroupId(), "com.liferay.lms.model",learningActivity.getGroupId(), "VIEW_RESULTS")){
						evaluateUser(actId, user.getUserId(), activities);	
					}
				}
			}

		}catch(DocumentException e){}	
	}

	private void evaluateUser(long actId, long userId,Map<Long, Long> activities) throws SystemException {
		{
			double[] values = new double[activities.size()];
			double[] weights = new double[activities.size()];
			
			int i=0;
			for(Map.Entry<Long, Long> evalAct:activities.entrySet()){
				LearningActivityResult learningActivityResult = LearningActivityResultLocalServiceUtil.getByActIdAndUserId(evalAct.getKey(), userId);
				if(learningActivityResult==null){
					values[i]=0;
				}
				else{
					values[i]=learningActivityResult.getResult();
				}
				weights[i]=evalAct.getValue();		
				i++;
			}

			try {
				LearningActivityTry  learningActivityTry =  LearningActivityTryLocalServiceUtil.getLastLearningActivityTryByActivityAndUser(actId, userId);
				if(learningActivityTry==null){
					ServiceContext serviceContext = new ServiceContext();
					serviceContext.setUserId(userId);
					learningActivityTry =  LearningActivityTryLocalServiceUtil.createLearningActivityTry(actId,serviceContext);
				}
				learningActivityTry.setEndDate(new Date());
				learningActivityTry.setResult((long)calculateMean(values, weights));
				learningActivityTry.setComments(StringPool.BLANK);
				updateLearningActivityTryAndResult(learningActivityTry);
				
			} catch (NestableException e) {
				_log.error("Error updating evaluation: "+actId+" result of user: "+userId, e);
			}						
		
		}
	}
	
    
    @SuppressWarnings({ "unchecked" })
	public void saveEvalModel(ActionRequest actionRequest,ActionResponse actionResponse){

    	ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);
    	try {
        	LearningActivity learningActivity = LearningActivityLocalServiceUtil.getLearningActivity(ParamUtil.getLong(actionRequest, "actId"));
        	if(learningActivity==null){
        		actionResponse.setRenderParameter("responseCode",StringPool.ASCII_TABLE[48]); //0    		
        		actionResponse.setRenderParameter("message",new String[]{LanguageUtil.get(getPortletConfig(), themeDisplay.getLocale(), "evaluationtaskactivity.error.noLearningActivityFound")}); 
        		return;
        	}
        	
    		JSONObject jsonObjectModel=null;
			try {
				jsonObjectModel = JSONFactoryUtil.createJSONObject(actionRequest.getParameter("model"));
			} catch (JSONException e) {
        		actionResponse.setRenderParameter("responseCode",StringPool.ASCII_TABLE[48]); //0    		
        		actionResponse.setRenderParameter("message",new String[]{LanguageUtil.get(getPortletConfig(), themeDisplay.getLocale(), "evaluationtaskactivity.error.courseModel")});  
        		return;
			}
			
    		if(!jsonObjectModel.has("activities")){
        		actionResponse.setRenderParameter("responseCode",StringPool.ASCII_TABLE[48]); //0    		
        		actionResponse.setRenderParameter("message",new String[]{LanguageUtil.get(getPortletConfig(), themeDisplay.getLocale(), "evaluationtaskactivity.error.noActivities")}); 
        		return;
    		}
			
			JSONArray activities = jsonObjectModel.getJSONArray("activities");

			List<String> errors = new ArrayList<String>();
			Map<Long, Long> activitiesMap = new HashMap<Long, Long>();
			
			for (int i = 0; i < activities.length(); i++) {
				JSONObject activity = activities.getJSONObject(i);
				String id = activity.getString("id");
				String weight = activity.getString("weight");
				if(!Validator.isNumber(id)) {
					errors.add(LanguageUtil.format(getPortletConfig(), themeDisplay.getLocale(), "evaluationtaskactivity.error.idNumber",new Object[]{id},false));
				}
				
				if(!Validator.isNumber(weight)) {
					errors.add(LanguageUtil.format(getPortletConfig(), themeDisplay.getLocale(), "evaluationtaskactivity.error.weightNumber",new Object[]{weight},false));
				}
				
				Long idLong = GetterUtil.getLong(id);
				
				if(activitiesMap.containsKey(idLong)){
					errors.add(LanguageUtil.format(getPortletConfig(), themeDisplay.getLocale(), "evaluationtaskactivity.error.idNumberRepeated",new Object[]{weight},false));
				}
				else{
					activitiesMap.put(idLong, GetterUtil.getLong(weight));
				}
			}
			
			if(!errors.isEmpty()){
	    		actionResponse.setRenderParameter("responseCode",StringPool.ASCII_TABLE[48]); //0    
	    		return;
			}
			
			List<Long> actIdsInDatabase = 
					LearningActivityLocalServiceUtil.dynamicQuery(
					DynamicQueryFactoryUtil.forClass(LearningActivity.class)
					.add(PropertyFactoryUtil.forName("typeId").ne(8))
					.add(PropertyFactoryUtil.forName("actId").in((Collection<Object>)(Collection<?>)activitiesMap.keySet()))
					.setProjection(ProjectionFactoryUtil.property("actId")));
				
			Iterator<Map.Entry<Long,Long>> evaluationMapIterator = activitiesMap.entrySet().iterator();
			while (evaluationMapIterator.hasNext()) {
				Map.Entry<Long,Long> evaluationEntry = evaluationMapIterator.next();
				if(!actIdsInDatabase.contains(evaluationEntry.getKey())){
					errors.add(LanguageUtil.format(getPortletConfig(), themeDisplay.getLocale(), "evaluationtaskactivity.error.idNumberNotInDatabase",new Object[]{evaluationEntry.getKey()},false));
					evaluationMapIterator.remove();
			    }
			}
			
			if(activitiesMap.isEmpty()){
        		actionResponse.setRenderParameter("responseCode",StringPool.ASCII_TABLE[48]); //0    		
        		actionResponse.setRenderParameter("message",new String[]{LanguageUtil.get(getPortletConfig(), themeDisplay.getLocale(), "evaluationtaskactivity.error.noEvaluations")}); 
        		return;				
			}
					
            if(errors.isEmpty()) {
        		Document document = null;
        		Element rootElement = null;
        		Date firedDate = null;
        		Date publishDate = null;
            	
        		if((learningActivity.getExtracontent()!=null)&&(learningActivity.getExtracontent().trim().length()!=0)){
        			try {
        				document=SAXReaderUtil.read(learningActivity.getExtracontent());
        				rootElement =document.getRootElement();
        				Element firedDateElement = rootElement.element("firedDate");
        				if(firedDateElement!=null){
        					firedDate =(Date)_dateFormat.parseObject(firedDateElement.getTextTrim());
        				}
        				
        				Element publishdDateElement = rootElement.element("publishDate");
        				if(publishdDateElement!=null){
        					publishDate =(Date)_dateFormat.parseObject(publishdDateElement.getTextTrim());
        				}
        			} catch (Throwable e) {
        			}	
        		}
        		
        		document = SAXReaderUtil.createDocument();
        		rootElement = document.addElement("evaluation");
        		
        		if(firedDate!=null){
        			rootElement.addElement("firedDate").setText(_dateFormat.format(firedDate));
        		}
        		
        		if(publishDate!=null){
        			rootElement.addElement("publishDate").setText(_dateFormat.format(publishDate));
        		}
        		
        		
    			Element activitiesElement = rootElement.addElement("activities");
    			
    			for (Map.Entry<Long,Long> activity : activitiesMap.entrySet()) {
					Element activityElement = activitiesElement.addElement("activity");
					activityElement.addAttribute("id", Long.toString(activity.getKey()));
					activityElement.setText(Long.toString(activity.getValue()));
				}
    			
    			learningActivity.setExtracontent(document.formattedString());
				LearningActivityLocalServiceUtil.updateLearningActivity(learningActivity);
	    		actionResponse.setRenderParameter("responseCode",StringPool.ASCII_TABLE[49]); //1 
	    		actionResponse.setRenderParameter("message",new String[]{LanguageUtil.get(getPortletConfig(), themeDisplay.getLocale(), "evaluationtaskactivity.updating")});  
            }
            else{
	    		actionResponse.setRenderParameter("responseCode",StringPool.ASCII_TABLE[48]); //0   
	    		actionResponse.setRenderParameter("message", errors.toArray(new String[errors.size()]));
            }
    		
    	} catch (Exception e) {	
    		actionResponse.setRenderParameter("responseCode",StringPool.ASCII_TABLE[48]); //0    		
    		actionResponse.setRenderParameter("message",new String[]{LanguageUtil.get(getPortletConfig(), themeDisplay.getLocale(), "evaluationtaskactivity.error.systemError")});  
    	} finally{
	    	actionResponse.setRenderParameter("jspPage","/html/evaluationtaskactivity/popups/activitiesResult.jsp");   	
    	}
    }
    
	public void update(ActionRequest actionRequest,ActionResponse actionResponse) throws Exception{
		
		LearningActivity learningActivity = LearningActivityLocalServiceUtil.getLearningActivity(ParamUtil.getLong(actionRequest, "actId"));
		
		Document document = SAXReaderUtil.read(learningActivity.getExtracontent());
		Element rootElement = document.getRootElement();
		
		Element firedDateElement = rootElement.element("firedDate");
		//if(firedDateElement==null){
			rootElement.addElement("firedDate").setText(_dateFormat.format(new Date()));
			learningActivity.setExtracontent(document.formattedString());
			LearningActivityLocalServiceUtil.updateLearningActivity(learningActivity);
			
			Message message = new Message();
			message.put("actId", learningActivity.getActId());
			MessageBusUtil.sendMessage("liferay/lms/evaluationActivity", message);
		//}
		
		PortletURL viewPortletURL = ((LiferayPortletResponse)actionResponse).createRenderURL();
		viewPortletURL.setParameter("jspPage","/html/evaluationtaskactivity/view.jsp");   	
    	actionResponse.sendRedirect(viewPortletURL.toString());
	}
	
	public void publish(ActionRequest actionRequest,ActionResponse actionResponse) throws Exception{
		
		LearningActivity learningActivity = LearningActivityLocalServiceUtil.getLearningActivity(ParamUtil.getLong(actionRequest, "actId"));
		
		Document document = SAXReaderUtil.read(learningActivity.getExtracontent());
		Element rootElement = document.getRootElement();
		
		Element publishDate = rootElement.element("publishDate");
		if(publishDate==null){
			rootElement.addElement("publishDate").setText(_dateFormat.format(new Date()));
		}
		learningActivity.setExtracontent(document.formattedString());
		LearningActivityLocalServiceUtil.updateLearningActivity(learningActivity);

		
		PortletURL viewPortletURL = ((LiferayPortletResponse)actionResponse).createRenderURL();
		viewPortletURL.setParameter("jspPage","/html/evaluationtaskactivity/view.jsp");   	
    	actionResponse.sendRedirect(viewPortletURL.toString());
	}
	
	public void reCalculate(ActionRequest actionRequest,ActionResponse actionResponse) throws Exception{

		long userId = ParamUtil.getLong(actionRequest, "userId");
		
		if(userId==0){
			SessionErrors.add(actionRequest, "evaluationtaskactivity.reCalculate.userId");			
		}
		else{
			LearningActivity learningActivity = LearningActivityLocalServiceUtil.getLearningActivity(ParamUtil.getLong(actionRequest, "actId"));
			evaluateUser(learningActivity.getActId(), userId, getLearningActivities(learningActivity));		
			SessionMessages.add(actionRequest, "evaluationtaskactivity.reCalculate.ok");
		}

		PortletURL viewPortletURL = ((LiferayPortletResponse)actionResponse).createRenderURL();
		viewPortletURL.setParameter("jspPage","/html/evaluationtaskactivity/view.jsp");   	
    	actionResponse.sendRedirect(viewPortletURL.toString());
	}
	
	public void setGrade(ActionRequest actionRequest,ActionResponse actionResponse) throws Exception{
		
		ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);
    	try {
    		List<String> errors = new ArrayList<String>();
    		
    		long actId = ParamUtil.getLong(actionRequest,"actId"); 
        	if(actId==0){        		
        		errors.add(LanguageUtil.get(getPortletConfig(), themeDisplay.getLocale(), "evaluationAvg.error.noActIdParam"));
        	}
        	
        	long userId = ParamUtil.getLong(actionRequest, "userId");
        	if(userId==0){        		
        		errors.add(LanguageUtil.get(getPortletConfig(), themeDisplay.getLocale(), "evaluationAvg.error.noUserIdParam"));
        	}
        	
    		long result = ParamUtil.getLong(actionRequest, "result");
        	if((result<0)||(result>100)){
        		errors.add(LanguageUtil.get(getPortletConfig(), themeDisplay.getLocale(), "evaluationAvg.error.resultNumberRange"));
        	}
        	
    		String comments = ParamUtil.getString(actionRequest, "comments");
        	
        	if(errors.size()!=0){
        		actionResponse.setRenderParameter("responseCode",StringPool.ASCII_TABLE[48]); //0    		
        		actionResponse.setRenderParameter("message",errors.toArray(new String[errors.size()]));  
        		return;
        	}
        	
        	
			LearningActivityTry  learningActivityTry =  LearningActivityTryLocalServiceUtil.getLastLearningActivityTryByActivityAndUser(actId, userId);
        	if(learningActivityTry==null){
        		actionResponse.setRenderParameter("responseCode",StringPool.ASCII_TABLE[48]); //0    		
        		actionResponse.setRenderParameter("message",new String[]{LanguageUtil.get(getPortletConfig(), themeDisplay.getLocale(), "evaluationAvg.error.noLearningActivityTry")});  
        		return;
        	}

			learningActivityTry.setEndDate(new Date());
			learningActivityTry.setResult(result);
			learningActivityTry.setComments(comments);
			updateLearningActivityTryAndResult(learningActivityTry);
    		actionResponse.setRenderParameter("responseCode",StringPool.ASCII_TABLE[49]); //1    		
    		actionResponse.setRenderParameter("message",new String[]{LanguageUtil.get(getPortletConfig(), themeDisplay.getLocale(), "evaluationAvg.grade.updating")});  
    		
    	} catch (Exception e) {	
    		actionResponse.setRenderParameter("responseCode",StringPool.ASCII_TABLE[48]); //0    		
    		actionResponse.setRenderParameter("message",new String[]{LanguageUtil.get(getPortletConfig(), themeDisplay.getLocale(), "evaluationAvg.error.systemError")});  
    	} finally{
	    	actionResponse.setRenderParameter("jspPage","/html/evaluationtaskactivity/popups/activitiesResult.jsp");   	
	    	actionResponse.setRenderParameter(WebKeys.PORTLET_CONFIGURATOR_VISIBILITY,StringPool.TRUE);
    	}

	}
	
	@Override
	public void render(RenderRequest renderRequest, RenderResponse renderResponse)
			throws PortletException, IOException {
		long actId=0;
		
		if(ParamUtil.getBoolean(renderRequest, "actionEditingDetails", false)){
			
			actId=ParamUtil.getLong(renderRequest, "resId", 0);
			renderResponse.setProperty("clear-request-parameters",Boolean.TRUE.toString());
		}
		else{
			actId=ParamUtil.getLong(renderRequest, "actId", 0);
		}
					
		if(actId==0)// TODO Auto-generated method stub
		{
			renderRequest.setAttribute(WebKeys.PORTLET_CONFIGURATOR_VISIBILITY, Boolean.FALSE);
		}
		else
		{
				LearningActivity activity;
				try {
					activity = LearningActivityLocalServiceUtil.getLearningActivity(actId);
					long typeId=activity.getTypeId();
					
					if(typeId==8)
					{
						super.render(renderRequest, renderResponse);
					}
					else
					{
						renderRequest.setAttribute(WebKeys.PORTLET_CONFIGURATOR_VISIBILITY, Boolean.FALSE);
					}
				} catch (PortalException e) {
				} catch (SystemException e) {
				}			
		}
	}

}
