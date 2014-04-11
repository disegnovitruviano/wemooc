package com.liferay.lms.messagelistener;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.liferay.lms.ModuleUpdateResult;
import com.liferay.lms.model.Course;
import com.liferay.lms.model.CourseResult;
import com.liferay.lms.model.LearningActivity;
import com.liferay.lms.model.LearningActivityResult;
import com.liferay.lms.model.Module;
import com.liferay.lms.model.ModuleResult;
import com.liferay.lms.service.ClpSerializer;
import com.liferay.lms.service.CourseLocalServiceUtil;
import com.liferay.lms.service.CourseResultLocalServiceUtil;
import com.liferay.lms.service.LearningActivityLocalServiceUtil;
import com.liferay.lms.service.LearningActivityResultLocalServiceUtil;
import com.liferay.lms.service.ModuleLocalServiceUtil;
import com.liferay.lms.service.ModuleResultLocalServiceUtil;
import com.liferay.portal.kernel.bean.PortletBeanLocatorUtil;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageListener;
import com.liferay.portal.kernel.messaging.MessageListenerException;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;

public class PortalAdminMessageListener implements MessageListener {

	@Override
	public void receive(Message message) throws MessageListenerException {
		// TODO Auto-generated method stub
		try {
			
			if("updateModulePassedDate".equals(message.getString("action"))){
				updateModulePassedDate(message);
			}
			
			
		} catch (Exception e) {
			_log.error("Unable to process message " + message, e);
		}
	}

	@SuppressWarnings("unchecked")
	protected void updateModulePassedDate(Message message) throws Exception {
		
		boolean updateDB = message.getBoolean("updateDB");
		
		String trace = "updateModulePassedDate ";
		int conta = 0;
		Calendar start = Calendar.getInstance();

		ClassLoader classLoader = (ClassLoader) PortletBeanLocatorUtil.locate(ClpSerializer.getServletContextName(),"portletClassLoader");
		
		//Obtenemos todos los moduleResult que no tengan la fecha en que se aprobaron y est�n aprobados.
		DynamicQuery dq = DynamicQueryFactoryUtil.forClass(ModuleResult.class,classLoader)
				.add(PropertyFactoryUtil.forName("passed").eq(true))
				.add(PropertyFactoryUtil.forName("passedDate").isNull())
				.addOrder(PropertyFactoryUtil.forName("userId").asc());
		
		List<ModuleResult> modules = LearningActivityLocalServiceUtil.dynamicQuery(dq);
		
		System.out.println("\n\n ## START ## "+start.getTime()+"\nModules result passed without passedDate : " + modules.size() +", Update DB: "+ updateDB );
		trace += start.getTime()+", Update DB: "+ updateDB+"\n";
		
		for(ModuleResult moduleResult:modules){
			
			User user;
			String userName="";
			try {
				user = UserLocalServiceUtil.getUserById(moduleResult.getUserId());
				userName = user.getFullName();
			} catch (Exception e1) {/*e1.printStackTrace();*/}
			System.out.println("\n ModuleResult: moduleId: " + moduleResult.getModuleId() + ", passedDate: " + moduleResult.getPassedDate() + ", " + userName +" (" + moduleResult.getUserId()+")");
		
			//Obtenemos las actividades que tiene el m�dulo
			DynamicQuery dqa = DynamicQueryFactoryUtil.forClass(LearningActivity.class,classLoader)
					.add(PropertyFactoryUtil.forName("moduleId").eq(moduleResult.getModuleId()))
					.add(PropertyFactoryUtil.forName("weightinmodule").eq((long)1))
					.addOrder(PropertyFactoryUtil.forName("priority").desc());
			
			List<LearningActivity> activities = LearningActivityLocalServiceUtil.dynamicQuery(dqa);
			
			for(LearningActivity activity:activities){
				
				LearningActivityResult newestActivityResult = LearningActivityResultLocalServiceUtil.getByActIdAndUserId(activity.getActId(), moduleResult.getUserId());
				
				if(newestActivityResult != null && newestActivityResult.getPassed()){
					
					conta++;
					
					System.out.println("   activity passed: " + activity.getTitle(Locale.getDefault()) + " " + activity.getPriority() );
					
					System.out.println("     passedDate : " + newestActivityResult.getEndDate());
					
					if(updateDB){
						moduleResult.setPassedDate(newestActivityResult.getEndDate());
						ModuleResultLocalServiceUtil.updateModuleResult(moduleResult);
					}
						
					
					Course course = null;
					try {
						course = CourseLocalServiceUtil.getCourseByGroupCreatedId(ModuleLocalServiceUtil.getModule(moduleResult.getModuleId()).getGroupId());
						
						if(course != null){

							CourseResult courseResult = CourseResultLocalServiceUtil.getByUserAndCourse(course.getCourseId(), moduleResult.getUserId());
							
							if(courseResult != null && courseResult.getPassedDate() == null && courseResult.getPassed()){
								System.out.println("       course : " + course.getTitle(Locale.getDefault()));
								System.out.println("       * courseResult : " + courseResult);
								
								Module nextModule = ModuleLocalServiceUtil.getNextModule(moduleResult.getModuleId());
																
								//Si no tiene modulo siguiente, es que es el ultimo.
								if(nextModule == null){
									
									System.out.println("         + courseResult passedDate : " + newestActivityResult.getEndDate());
									
									if(updateDB){
										courseResult.setPassedDate(newestActivityResult.getEndDate());
										CourseResultLocalServiceUtil.update(courseResult);
									}
								}
							}
						}else{
							System.out.println("       No course result");
						}
												
					} catch (Exception e) { e.printStackTrace();}

					//Traza para el fichero
					
					trace += "User: "+userName +" (userId:" + moduleResult.getUserId()+")"+(course!=null?", Course: "+course.getTitle(Locale.getDefault())+" (courseId:" + course.getCourseId()+")":"")+", ModuleId:" + moduleResult.getModuleId() + ", PassedDate : " + newestActivityResult.getEndDate() +"(actId:"+ newestActivityResult.getActId() +")\n";
					
					break;
				}
				
			}

		}
		
		Calendar end = Calendar.getInstance();
		System.out.println("------------------------------------------------");
		System.out.println(" ## START ## "+start.getTime());
		System.out.println(" ##  END  ## "+end.getTime());
		System.out.println(" ##  UPDATED  ## "+conta);
		System.out.println("------------------------------------------------");
		
		
		try {
			if(updateDB){
				ModuleUpdateResult.saveStringToFile("updateModulePassedDate.txt", trace+"\nUPDATED: "+conta+"\n");
			}
		} catch (Exception e) {
			System.out.println("");
			//e.printStackTrace();
		}
		
	}
		
	private static Log _log = LogFactoryUtil.getLog(PortalAdminMessageListener.class);
}
