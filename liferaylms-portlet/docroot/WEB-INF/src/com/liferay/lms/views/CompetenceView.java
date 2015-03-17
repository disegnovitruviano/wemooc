package com.liferay.lms.views;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.liferay.lms.model.Competence;
import com.liferay.lms.model.UserCompetence;
import com.liferay.portal.kernel.util.FastDateFormatFactoryUtil;

public class CompetenceView {
	private Competence competence = null;
	private UserCompetence userCompetence = null;
	
	
	public CompetenceView(Competence competence, UserCompetence userCompetence) {
		super();
		this.competence = competence;
		this.userCompetence = userCompetence;
	}
	
	public String getTitle(Locale locale) {
		return competence.getTitle(locale);
	}
    public boolean getGenerateCertificate()
    {
    	return competence.getGenerateCertificate();
    }
	public String getFormatDate(Date date, Locale locale, TimeZone timeZone) {
		System.out.println(locale);
		Format dateFormatDate = FastDateFormatFactoryUtil.getDate(locale, timeZone);
		
		
		if(userCompetence.getCompDate()==null){
			return "";
		}else{
			return dateFormatDate.format(date);
		}		
	}
	
	
	public Date getDate()
	{
		return userCompetence.getCompDate();
	}
	public Long getCompetenceId(){
		return competence.getCompetenceId();
	}
}
