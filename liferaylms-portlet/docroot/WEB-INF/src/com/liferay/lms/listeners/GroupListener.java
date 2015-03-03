package com.liferay.lms.listeners;

import javax.mail.internet.InternetAddress;

import com.liferay.lms.auditing.AuditConstants;
import com.liferay.lms.auditing.AuditingLogFactory;
import com.liferay.lms.model.Course;
import com.liferay.lms.service.CourseLocalServiceUtil;
import com.liferay.mail.service.MailServiceUtil;
import com.liferay.portal.ModelListenerException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.mail.MailMessage;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.BaseModelListener;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.PrincipalThreadLocal;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;

public class GroupListener extends BaseModelListener<Group> {
	Log log = LogFactoryUtil.getLog(GroupListener.class);

	@Override
	public void onAfterAddAssociation(Object classPK,
			String associationClassName, Object associationClassPK)
			throws ModelListenerException {
		if(log.isDebugEnabled())log.debug("onAfterAddAssociation");
		long groupId = GetterUtil.getLong(classPK);
		long userId = GetterUtil.getLong(associationClassPK);
		try {
			Course course = CourseLocalServiceUtil.fetchByGroupCreatedId(groupId);
			if((User.class.getName().equals(associationClassName))&&
				(Validator.isNotNull(course))){
				AuditingLogFactory.audit(course.getCompanyId(), course.getGroupCreatedId(), Course.class.getName(), 
						course.getCourseId(), userId, PrincipalThreadLocal.getUserId(), AuditConstants.REGISTER, null);
			}

			if(course.isWelcome()&&course.getWelcomeMsg()!=null&&!StringPool.BLANK.equals(course.getWelcomeMsg())){
				User user = null;
				Company company = null;
				try {
					user = UserLocalServiceUtil.getUser(userId);
					company = CompanyLocalServiceUtil.getCompany(course.getCompanyId());
				} catch (PortalException e) {
				}
				
				if(user!=null&&company!=null){

			    	String fromName = PrefsPropsUtil.getString(course.getCompanyId(),
							PropsKeys.ADMIN_EMAIL_FROM_NAME);
					String fromAddress = PrefsPropsUtil.getString(course.getCompanyId(),
							PropsKeys.ADMIN_EMAIL_FROM_ADDRESS);
			    	String emailTo = user.getEmailAddress();
			    	String nameTo = user.getFullName();

					try{
						InternetAddress to = new InternetAddress(emailTo, nameTo);
						InternetAddress from = new InternetAddress(fromAddress, fromName);
						
				    	String url = PortalUtil.getPortalURL(company.getVirtualHostname(), 80, false);
				    	String urlcourse = url+"/web"+course.getFriendlyURL();
				    	String subject = LanguageUtil.format(user.getLocale(),"welcome-subject", new String[]{course.getTitle(user.getLocale())});
				    	String body = StringUtil.replace(
			    			course.getWelcomeMsg(),
			    			new String[] {"[$FROM_ADDRESS$]", "[$FROM_NAME$]", "[$PAGE_URL$]","[$PORTAL_URL$]","[$TO_ADDRESS$]","[$TO_NAME$]"},
			    			new String[] {fromAddress, fromName, urlcourse, url, user.getEmailAddress(), user.getFullName()});
			    	
						if(log.isDebugEnabled()){
							log.debug(from);
							log.debug(to);
							log.debug(subject);
							log.debug(body);
						}
						MailMessage mailm = new MailMessage(from, to, subject, body, true);
						MailServiceUtil.sendEmail(mailm);
					}
					catch(Exception ex)
					{
						if(log.isDebugEnabled())ex.printStackTrace();
					}		
				}
			}
		} catch (SystemException e) {
			throw new ModelListenerException(e);
		}
	}
	
	
	@Override
	public void onAfterRemoveAssociation(Object classPK,
			String associationClassName, Object associationClassPK)
			throws ModelListenerException {
		if(log.isDebugEnabled())log.debug("onAfterRemoveAssociation");
		long groupId = GetterUtil.getLong(classPK);
		long userId = GetterUtil.getLong(associationClassPK);
		try {
			Course course = CourseLocalServiceUtil.fetchByGroupCreatedId(groupId);
			if((User.class.getName().equals(associationClassName))&&
				(Validator.isNotNull(course))){
				AuditingLogFactory.audit(course.getCompanyId(), course.getGroupCreatedId(), Course.class.getName(), 
						course.getCourseId(), userId, PrincipalThreadLocal.getUserId(), AuditConstants.UNREGISTER, null);
			}
		} catch (SystemException e) {
			throw new ModelListenerException(e);
		}
	}
}
