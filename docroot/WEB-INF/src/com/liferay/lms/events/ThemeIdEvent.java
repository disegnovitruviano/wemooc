package com.liferay.lms.events;

import java.io.Serializable;

public final class ThemeIdEvent implements Serializable {
	private static final long serialVersionUID = -8549599250647470213L;
	private long moduleId;
	long themeId;
	/**
	 * @return the moduleId
	 */
	public final long getModuleId() {
		return moduleId;
	}
	/**
	 * @param moduleId the moduleId to set
	 */
	public final void setModuleId(final long moduleId) {
		this.moduleId = moduleId;
	}
	/**
	 * @return the themeId
	 */
	public final long getThemeId() {
		return themeId;
	}
	/**
	 * @param themeId the themeId to set
	 */
	public final void setThemeId(final long themeId) {
		this.themeId = themeId;
	}
	
	
}
