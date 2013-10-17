package com.liferay.lms.learningactivity.calificationtype;

import java.util.Locale;

public interface CalificationType 
{
	public long getTypeId();
	public String getName();
	public String getTitle(Locale locale);
	public String getDescription(Locale locale);
	public String translate(double result);
}
