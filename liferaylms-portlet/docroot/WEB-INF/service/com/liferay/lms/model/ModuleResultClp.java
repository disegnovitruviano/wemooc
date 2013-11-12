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

import com.liferay.lms.service.ModuleResultLocalServiceUtil;

import com.liferay.portal.kernel.bean.AutoEscapeBeanHandler;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.model.BaseModel;
import com.liferay.portal.model.impl.BaseModelImpl;
import com.liferay.portal.util.PortalUtil;

import java.io.Serializable;

import java.lang.reflect.Proxy;

import java.util.HashMap;
import java.util.Map;

/**
 * @author TLS
 */
public class ModuleResultClp extends BaseModelImpl<ModuleResult>
	implements ModuleResult {
	public ModuleResultClp() {
	}

	public Class<?> getModelClass() {
		return ModuleResult.class;
	}

	public String getModelClassName() {
		return ModuleResult.class.getName();
	}

	public long getPrimaryKey() {
		return _mrId;
	}

	public void setPrimaryKey(long primaryKey) {
		setMrId(primaryKey);
	}

	public Serializable getPrimaryKeyObj() {
		return new Long(_mrId);
	}

	public void setPrimaryKeyObj(Serializable primaryKeyObj) {
		setPrimaryKey(((Long)primaryKeyObj).longValue());
	}

	@Override
	public Map<String, Object> getModelAttributes() {
		Map<String, Object> attributes = new HashMap<String, Object>();

		attributes.put("moduleId", getModuleId());
		attributes.put("result", getResult());
		attributes.put("comments", getComments());
		attributes.put("userId", getUserId());
		attributes.put("passed", getPassed());
		attributes.put("mrId", getMrId());

		return attributes;
	}

	@Override
	public void setModelAttributes(Map<String, Object> attributes) {
		Long moduleId = (Long)attributes.get("moduleId");

		if (moduleId != null) {
			setModuleId(moduleId);
		}

		Long result = (Long)attributes.get("result");

		if (result != null) {
			setResult(result);
		}

		String comments = (String)attributes.get("comments");

		if (comments != null) {
			setComments(comments);
		}

		Long userId = (Long)attributes.get("userId");

		if (userId != null) {
			setUserId(userId);
		}

		Boolean passed = (Boolean)attributes.get("passed");

		if (passed != null) {
			setPassed(passed);
		}

		Long mrId = (Long)attributes.get("mrId");

		if (mrId != null) {
			setMrId(mrId);
		}
	}

	public long getModuleId() {
		return _moduleId;
	}

	public void setModuleId(long moduleId) {
		_moduleId = moduleId;
	}

	public long getResult() {
		return _result;
	}

	public void setResult(long result) {
		_result = result;
	}

	public String getComments() {
		return _comments;
	}

	public void setComments(String comments) {
		_comments = comments;
	}

	public long getUserId() {
		return _userId;
	}

	public void setUserId(long userId) {
		_userId = userId;
	}

	public String getUserUuid() throws SystemException {
		return PortalUtil.getUserValue(getUserId(), "uuid", _userUuid);
	}

	public void setUserUuid(String userUuid) {
		_userUuid = userUuid;
	}

	public boolean getPassed() {
		return _passed;
	}

	public boolean isPassed() {
		return _passed;
	}

	public void setPassed(boolean passed) {
		_passed = passed;
	}

	public long getMrId() {
		return _mrId;
	}

	public void setMrId(long mrId) {
		_mrId = mrId;
	}

	public BaseModel<?> getModuleResultRemoteModel() {
		return _moduleResultRemoteModel;
	}

	public void setModuleResultRemoteModel(BaseModel<?> moduleResultRemoteModel) {
		_moduleResultRemoteModel = moduleResultRemoteModel;
	}

	public void persist() throws SystemException {
		if (this.isNew()) {
			ModuleResultLocalServiceUtil.addModuleResult(this);
		}
		else {
			ModuleResultLocalServiceUtil.updateModuleResult(this);
		}
	}

	@Override
	public ModuleResult toEscapedModel() {
		return (ModuleResult)Proxy.newProxyInstance(ModuleResult.class.getClassLoader(),
			new Class[] { ModuleResult.class }, new AutoEscapeBeanHandler(this));
	}

	@Override
	public Object clone() {
		ModuleResultClp clone = new ModuleResultClp();

		clone.setModuleId(getModuleId());
		clone.setResult(getResult());
		clone.setComments(getComments());
		clone.setUserId(getUserId());
		clone.setPassed(getPassed());
		clone.setMrId(getMrId());

		return clone;
	}

	public int compareTo(ModuleResult moduleResult) {
		int value = 0;

		if (getMrId() < moduleResult.getMrId()) {
			value = -1;
		}
		else if (getMrId() > moduleResult.getMrId()) {
			value = 1;
		}
		else {
			value = 0;
		}

		if (value != 0) {
			return value;
		}

		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		ModuleResultClp moduleResult = null;

		try {
			moduleResult = (ModuleResultClp)obj;
		}
		catch (ClassCastException cce) {
			return false;
		}

		long primaryKey = moduleResult.getPrimaryKey();

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
		StringBundler sb = new StringBundler(13);

		sb.append("{moduleId=");
		sb.append(getModuleId());
		sb.append(", result=");
		sb.append(getResult());
		sb.append(", comments=");
		sb.append(getComments());
		sb.append(", userId=");
		sb.append(getUserId());
		sb.append(", passed=");
		sb.append(getPassed());
		sb.append(", mrId=");
		sb.append(getMrId());
		sb.append("}");

		return sb.toString();
	}

	public String toXmlString() {
		StringBundler sb = new StringBundler(22);

		sb.append("<model><model-name>");
		sb.append("com.liferay.lms.model.ModuleResult");
		sb.append("</model-name>");

		sb.append(
			"<column><column-name>moduleId</column-name><column-value><![CDATA[");
		sb.append(getModuleId());
		sb.append("]]></column-value></column>");
		sb.append(
			"<column><column-name>result</column-name><column-value><![CDATA[");
		sb.append(getResult());
		sb.append("]]></column-value></column>");
		sb.append(
			"<column><column-name>comments</column-name><column-value><![CDATA[");
		sb.append(getComments());
		sb.append("]]></column-value></column>");
		sb.append(
			"<column><column-name>userId</column-name><column-value><![CDATA[");
		sb.append(getUserId());
		sb.append("]]></column-value></column>");
		sb.append(
			"<column><column-name>passed</column-name><column-value><![CDATA[");
		sb.append(getPassed());
		sb.append("]]></column-value></column>");
		sb.append(
			"<column><column-name>mrId</column-name><column-value><![CDATA[");
		sb.append(getMrId());
		sb.append("]]></column-value></column>");

		sb.append("</model>");

		return sb.toString();
	}

	private long _moduleId;
	private long _result;
	private String _comments;
	private long _userId;
	private String _userUuid;
	private boolean _passed;
	private long _mrId;
	private BaseModel<?> _moduleResultRemoteModel;
}