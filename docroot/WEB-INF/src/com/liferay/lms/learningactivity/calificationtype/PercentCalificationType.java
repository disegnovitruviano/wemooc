package com.liferay.lms.learningactivity.calificationtype;


import java.util.Locale;


public class PercentCalificationType extends BaseCalificationType {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public long getTypeId() {
		return 0;
	}
	
	@Override
	public String getName() {
		return "percent_ct";
	}
	
	@Override
	public String getTitle(Locale locale) {
		return "percent_ct.title";
	}
	
	@Override
	public String getDescription(Locale locale) {
		return "percent_ct.description";
	}
	
	@Override
	public String translate(double result) {
		return Double.toString(result);
	}
	
}
