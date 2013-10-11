/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
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

package com.liferay.lms.model;

import com.liferay.lms.service.LmsPrefsLocalServiceUtil;

import com.liferay.portal.kernel.bean.AutoEscapeBeanHandler;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.model.BaseModel;
import com.liferay.portal.model.impl.BaseModelImpl;

import java.io.Serializable;

import java.lang.reflect.Proxy;

import java.util.HashMap;
import java.util.Map;

/**
 * @author TLS
 */
public class LmsPrefsClp extends BaseModelImpl<LmsPrefs> implements LmsPrefs {
	public LmsPrefsClp() {
	}

	public Class<?> getModelClass() {
		return LmsPrefs.class;
	}

	public String getModelClassName() {
		return LmsPrefs.class.getName();
	}

	public long getPrimaryKey() {
		return _companyId;
	}

	public void setPrimaryKey(long primaryKey) {
		setCompanyId(primaryKey);
	}

	public Serializable getPrimaryKeyObj() {
		return new Long(_companyId);
	}

	public void setPrimaryKeyObj(Serializable primaryKeyObj) {
		setPrimaryKey(((Long)primaryKeyObj).longValue());
	}

	@Override
	public Map<String, Object> getModelAttributes() {
		Map<String, Object> attributes = new HashMap<String, Object>();

		attributes.put("companyId", getCompanyId());
		attributes.put("teacherRole", getTeacherRole());
		attributes.put("editorRole", getEditorRole());
		attributes.put("lmsTemplates", getLmsTemplates());

		return attributes;
	}

	@Override
	public void setModelAttributes(Map<String, Object> attributes) {
		Long companyId = (Long)attributes.get("companyId");

		if (companyId != null) {
			setCompanyId(companyId);
		}

		Long teacherRole = (Long)attributes.get("teacherRole");

		if (teacherRole != null) {
			setTeacherRole(teacherRole);
		}

		Long editorRole = (Long)attributes.get("editorRole");

		if (editorRole != null) {
			setEditorRole(editorRole);
		}

		String lmsTemplates = (String)attributes.get("lmsTemplates");

		if (lmsTemplates != null) {
			setLmsTemplates(lmsTemplates);
		}
	}

	public long getCompanyId() {
		return _companyId;
	}

	public void setCompanyId(long companyId) {
		_companyId = companyId;
	}

	public long getTeacherRole() {
		return _teacherRole;
	}

	public void setTeacherRole(long teacherRole) {
		_teacherRole = teacherRole;
	}

	public long getEditorRole() {
		return _editorRole;
	}

	public void setEditorRole(long editorRole) {
		_editorRole = editorRole;
	}

	public String getLmsTemplates() {
		return _lmsTemplates;
	}

	public void setLmsTemplates(String lmsTemplates) {
		_lmsTemplates = lmsTemplates;
	}

	public BaseModel<?> getLmsPrefsRemoteModel() {
		return _lmsPrefsRemoteModel;
	}

	public void setLmsPrefsRemoteModel(BaseModel<?> lmsPrefsRemoteModel) {
		_lmsPrefsRemoteModel = lmsPrefsRemoteModel;
	}

	public void persist() throws SystemException {
		if (this.isNew()) {
			LmsPrefsLocalServiceUtil.addLmsPrefs(this);
		}
		else {
			LmsPrefsLocalServiceUtil.updateLmsPrefs(this);
		}
	}

	@Override
	public LmsPrefs toEscapedModel() {
		return (LmsPrefs)Proxy.newProxyInstance(LmsPrefs.class.getClassLoader(),
			new Class[] { LmsPrefs.class }, new AutoEscapeBeanHandler(this));
	}

	@Override
	public Object clone() {
		LmsPrefsClp clone = new LmsPrefsClp();

		clone.setCompanyId(getCompanyId());
		clone.setTeacherRole(getTeacherRole());
		clone.setEditorRole(getEditorRole());
		clone.setLmsTemplates(getLmsTemplates());

		return clone;
	}

	public int compareTo(LmsPrefs lmsPrefs) {
		long primaryKey = lmsPrefs.getPrimaryKey();

		if (getPrimaryKey() < primaryKey) {
			return -1;
		}
		else if (getPrimaryKey() > primaryKey) {
			return 1;
		}
		else {
			return 0;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		LmsPrefsClp lmsPrefs = null;

		try {
			lmsPrefs = (LmsPrefsClp)obj;
		}
		catch (ClassCastException cce) {
			return false;
		}

		long primaryKey = lmsPrefs.getPrimaryKey();

		if (getPrimaryKey() == primaryKey) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return (int)getPrimaryKey();
	}

	@Override
	public String toString() {
		StringBundler sb = new StringBundler(9);

		sb.append("{companyId=");
		sb.append(getCompanyId());
		sb.append(", teacherRole=");
		sb.append(getTeacherRole());
		sb.append(", editorRole=");
		sb.append(getEditorRole());
		sb.append(", lmsTemplates=");
		sb.append(getLmsTemplates());
		sb.append("}");

		return sb.toString();
	}

	public String toXmlString() {
		StringBundler sb = new StringBundler(16);

		sb.append("<model><model-name>");
		sb.append("com.liferay.lms.model.LmsPrefs");
		sb.append("</model-name>");

		sb.append(
			"<column><column-name>companyId</column-name><column-value><![CDATA[");
		sb.append(getCompanyId());
		sb.append("]]></column-value></column>");
		sb.append(
			"<column><column-name>teacherRole</column-name><column-value><![CDATA[");
		sb.append(getTeacherRole());
		sb.append("]]></column-value></column>");
		sb.append(
			"<column><column-name>editorRole</column-name><column-value><![CDATA[");
		sb.append(getEditorRole());
		sb.append("]]></column-value></column>");
		sb.append(
			"<column><column-name>lmsTemplates</column-name><column-value><![CDATA[");
		sb.append(getLmsTemplates());
		sb.append("]]></column-value></column>");

		sb.append("</model>");

		return sb.toString();
	}

	private long _companyId;
	private long _teacherRole;
	private long _editorRole;
	private String _lmsTemplates;
	private BaseModel<?> _lmsPrefsRemoteModel;
}