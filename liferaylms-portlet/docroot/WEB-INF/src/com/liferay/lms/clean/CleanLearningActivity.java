package com.liferay.lms.clean;

import com.liferay.lms.model.LearningActivity;
import com.liferay.lms.model.LearningActivityResult;
import com.liferay.lms.model.LearningActivityTry;
import com.liferay.lms.model.ModuleResult;
import com.liferay.lms.service.LearningActivityLocalServiceUtil;
import com.liferay.lms.service.LearningActivityResultLocalServiceUtil;
import com.liferay.lms.service.ModuleResultLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

public class CleanLearningActivity {
	Log log = LogFactoryUtil.getLog(CleanLearningActivity.class);
	

	public void createInstance(long companyId,long groupId,long userId, long moduleId, long actId){
		
	}

	public void endInstance(){
		
	}
	
	public boolean processTry(LearningActivityTry lat){
	
		//Audit
	
		/*LearningActivityTryDeleted latd = new LearningActivityTryDeletedImpl();
		
		latd.setActId(lat.getActId());
		latd.setActManAuditId(actManAudit.getActManAuditId());
		latd.setEndDate(lat.getEndDate());
		latd.setLatId(lat.getLatId());
		latd.setUserId(lat.getUserId());
		latd.setStartDate(lat.getStartDate());
		latd.setResult(lat.getResult());
		latd.setTryData(lat.getTryData());
		latd.setTryResultData(lat.getTryResultData());
		latd.setComments(lat.getComments());
		
		try {
			latd = LearningActivityTryDeletedLocalServiceUtil.addLearningActivityTryDeleted(latd);
			LearningActivityTryLocalServiceUtil.deleteLearningActivityTry(lat);
			actManAudit.setNumber(actManAudit.getNumber()+1);
			actManAudit = ActManAuditLocalServiceUtil.updateActManAudit(actManAudit);
			
		} catch (SystemException e) {
			if(log.isInfoEnabled())log.info(e.getMessage());
			if(log.isDebugEnabled())e.printStackTrace();
		}*/
		
		LearningActivityResult res= null;
		try {
			res = LearningActivityResultLocalServiceUtil.getByActIdAndUserId(lat.getActId(), lat.getUserId());
		} catch (SystemException e) {
			if(log.isInfoEnabled())log.info(e.getMessage());
			if(log.isDebugEnabled())e.printStackTrace();
		}
		

		LearningActivity larn = null;
		try {
			larn = LearningActivityLocalServiceUtil.getLearningActivity(lat.getActId());
		} catch (PortalException e) {
			if(log.isInfoEnabled())log.info(e.getMessage());
			if(log.isDebugEnabled())e.printStackTrace();
		} catch (SystemException e) {
			if(log.isInfoEnabled())log.info(e.getMessage());
			if(log.isDebugEnabled())e.printStackTrace();
		}
		
		if(res!=null){
			res.setResult(0);
			res.setPassed(false);
			res.setEndDate(null);
			try {
				LearningActivityResultLocalServiceUtil.updateLearningActivityResult(res);
			} catch (SystemException e) {
				if(log.isInfoEnabled())log.info(e.getMessage());
				if(log.isDebugEnabled())e.printStackTrace();
			}
		}
		
		if(larn!=null&&larn.getWeightinmodule()>0){
			ModuleResult mr = null;
			try {
				mr = ModuleResultLocalServiceUtil.getByModuleAndUser(larn.getModuleId(), lat.getUserId());
			} catch (SystemException e) {
				if(log.isInfoEnabled())log.info(e.getMessage());
				if(log.isDebugEnabled())e.printStackTrace();
			}
			if(mr!=null){
				mr.setPassed(false);
				try {
					ModuleResultLocalServiceUtil.updateModuleResult(mr);
				} catch (SystemException e) {
					if(log.isInfoEnabled())log.info(e.getMessage());
					if(log.isDebugEnabled())e.printStackTrace();
				}
			}
		}
		
		return true;
	}

}
