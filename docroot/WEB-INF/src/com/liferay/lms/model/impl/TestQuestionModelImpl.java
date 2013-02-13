/**
 * Copyright (c)2013 Telefonica Learning Services. All rights reserved.
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

package com.liferay.lms.model.impl;

import com.liferay.lms.model.TestQuestion;
import com.liferay.lms.model.TestQuestionModel;
import com.liferay.lms.model.TestQuestionSoap;

import com.liferay.portal.kernel.bean.AutoEscapeBeanHandler;
import com.liferay.portal.kernel.json.JSON;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.CacheModel;
import com.liferay.portal.model.impl.BaseModelImpl;
import com.liferay.portal.service.ServiceContext;

import com.liferay.portlet.expando.model.ExpandoBridge;
import com.liferay.portlet.expando.util.ExpandoBridgeFactoryUtil;

import java.io.Serializable;

import java.sql.Types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The base model implementation for the TestQuestion service. Represents a row in the &quot;Lms_TestQuestion&quot; database table, with each column mapped to a property of this class.
 *
 * <p>
 * This implementation and its corresponding interface {@link com.liferay.lms.model.TestQuestionModel} exist only as a container for the default property accessors generated by ServiceBuilder. Helper methods and all application logic should be put in {@link TestQuestionImpl}.
 * </p>
 *
 * @author TLS
 * @see TestQuestionImpl
 * @see com.liferay.lms.model.TestQuestion
 * @see com.liferay.lms.model.TestQuestionModel
 * @generated
 */
@JSON(strict = true)
public class TestQuestionModelImpl extends BaseModelImpl<TestQuestion>
	implements TestQuestionModel {
	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this class directly. All methods that expect a test question model instance should use the {@link com.liferay.lms.model.TestQuestion} interface instead.
	 */
	public static final String TABLE_NAME = "Lms_TestQuestion";
	public static final Object[][] TABLE_COLUMNS = {
			{ "uuid_", Types.VARCHAR },
			{ "questionId", Types.BIGINT },
			{ "actId", Types.BIGINT },
			{ "text_", Types.VARCHAR },
			{ "questionType", Types.BIGINT }
		};
	public static final String TABLE_SQL_CREATE = "create table Lms_TestQuestion (uuid_ VARCHAR(75) null,questionId LONG not null primary key,actId LONG,text_ VARCHAR(75) null,questionType LONG)";
	public static final String TABLE_SQL_DROP = "drop table Lms_TestQuestion";
	public static final String ORDER_BY_JPQL = " ORDER BY testQuestion.questionId ASC";
	public static final String ORDER_BY_SQL = " ORDER BY Lms_TestQuestion.questionId ASC";
	public static final String DATA_SOURCE = "liferayDataSource";
	public static final String SESSION_FACTORY = "liferaySessionFactory";
	public static final String TX_MANAGER = "liferayTransactionManager";
	public static final boolean ENTITY_CACHE_ENABLED = GetterUtil.getBoolean(com.liferay.util.service.ServiceProps.get(
				"value.object.entity.cache.enabled.com.liferay.lms.model.TestQuestion"),
			true);
	public static final boolean FINDER_CACHE_ENABLED = GetterUtil.getBoolean(com.liferay.util.service.ServiceProps.get(
				"value.object.finder.cache.enabled.com.liferay.lms.model.TestQuestion"),
			true);
	public static final boolean COLUMN_BITMASK_ENABLED = GetterUtil.getBoolean(com.liferay.util.service.ServiceProps.get(
				"value.object.column.bitmask.enabled.com.liferay.lms.model.TestQuestion"),
			true);
	public static long ACTID_COLUMN_BITMASK = 1L;
	public static long UUID_COLUMN_BITMASK = 2L;

	/**
	 * Converts the soap model instance into a normal model instance.
	 *
	 * @param soapModel the soap model instance to convert
	 * @return the normal model instance
	 */
	public static TestQuestion toModel(TestQuestionSoap soapModel) {
		if (soapModel == null) {
			return null;
		}

		TestQuestion model = new TestQuestionImpl();

		model.setUuid(soapModel.getUuid());
		model.setQuestionId(soapModel.getQuestionId());
		model.setActId(soapModel.getActId());
		model.setText(soapModel.getText());
		model.setQuestionType(soapModel.getQuestionType());

		return model;
	}

	/**
	 * Converts the soap model instances into normal model instances.
	 *
	 * @param soapModels the soap model instances to convert
	 * @return the normal model instances
	 */
	public static List<TestQuestion> toModels(TestQuestionSoap[] soapModels) {
		if (soapModels == null) {
			return null;
		}

		List<TestQuestion> models = new ArrayList<TestQuestion>(soapModels.length);

		for (TestQuestionSoap soapModel : soapModels) {
			models.add(toModel(soapModel));
		}

		return models;
	}

	public static final long LOCK_EXPIRATION_TIME = GetterUtil.getLong(com.liferay.util.service.ServiceProps.get(
				"lock.expiration.time.com.liferay.lms.model.TestQuestion"));

	public TestQuestionModelImpl() {
	}

	public long getPrimaryKey() {
		return _questionId;
	}

	public void setPrimaryKey(long primaryKey) {
		setQuestionId(primaryKey);
	}

	public Serializable getPrimaryKeyObj() {
		return new Long(_questionId);
	}

	public void setPrimaryKeyObj(Serializable primaryKeyObj) {
		setPrimaryKey(((Long)primaryKeyObj).longValue());
	}

	public Class<?> getModelClass() {
		return TestQuestion.class;
	}

	public String getModelClassName() {
		return TestQuestion.class.getName();
	}

	@Override
	public Map<String, Object> getModelAttributes() {
		Map<String, Object> attributes = new HashMap<String, Object>();

		attributes.put("uuid", getUuid());
		attributes.put("questionId", getQuestionId());
		attributes.put("actId", getActId());
		attributes.put("text", getText());
		attributes.put("questionType", getQuestionType());

		return attributes;
	}

	@Override
	public void setModelAttributes(Map<String, Object> attributes) {
		String uuid = (String)attributes.get("uuid");

		if (uuid != null) {
			setUuid(uuid);
		}

		Long questionId = (Long)attributes.get("questionId");

		if (questionId != null) {
			setQuestionId(questionId);
		}

		Long actId = (Long)attributes.get("actId");

		if (actId != null) {
			setActId(actId);
		}

		String text = (String)attributes.get("text");

		if (text != null) {
			setText(text);
		}

		Long questionType = (Long)attributes.get("questionType");

		if (questionType != null) {
			setQuestionType(questionType);
		}
	}

	@JSON
	public String getUuid() {
		if (_uuid == null) {
			return StringPool.BLANK;
		}
		else {
			return _uuid;
		}
	}

	public void setUuid(String uuid) {
		if (_originalUuid == null) {
			_originalUuid = _uuid;
		}

		_uuid = uuid;
	}

	public String getOriginalUuid() {
		return GetterUtil.getString(_originalUuid);
	}

	@JSON
	public long getQuestionId() {
		return _questionId;
	}

	public void setQuestionId(long questionId) {
		_columnBitmask = -1L;

		_questionId = questionId;
	}

	@JSON
	public long getActId() {
		return _actId;
	}

	public void setActId(long actId) {
		_columnBitmask |= ACTID_COLUMN_BITMASK;

		if (!_setOriginalActId) {
			_setOriginalActId = true;

			_originalActId = _actId;
		}

		_actId = actId;
	}

	public long getOriginalActId() {
		return _originalActId;
	}

	@JSON
	public String getText() {
		if (_text == null) {
			return StringPool.BLANK;
		}
		else {
			return _text;
		}
	}

	public void setText(String text) {
		_text = text;
	}

	@JSON
	public long getQuestionType() {
		return _questionType;
	}

	public void setQuestionType(long questionType) {
		_questionType = questionType;
	}

	public long getColumnBitmask() {
		return _columnBitmask;
	}

	@Override
	public ExpandoBridge getExpandoBridge() {
		return ExpandoBridgeFactoryUtil.getExpandoBridge(0,
			TestQuestion.class.getName(), getPrimaryKey());
	}

	@Override
	public void setExpandoBridgeAttributes(ServiceContext serviceContext) {
		ExpandoBridge expandoBridge = getExpandoBridge();

		expandoBridge.setAttributes(serviceContext);
	}

	@Override
	public TestQuestion toEscapedModel() {
		if (_escapedModelProxy == null) {
			_escapedModelProxy = (TestQuestion)ProxyUtil.newProxyInstance(_classLoader,
					_escapedModelProxyInterfaces,
					new AutoEscapeBeanHandler(this));
		}

		return _escapedModelProxy;
	}

	@Override
	public Object clone() {
		TestQuestionImpl testQuestionImpl = new TestQuestionImpl();

		testQuestionImpl.setUuid(getUuid());
		testQuestionImpl.setQuestionId(getQuestionId());
		testQuestionImpl.setActId(getActId());
		testQuestionImpl.setText(getText());
		testQuestionImpl.setQuestionType(getQuestionType());

		testQuestionImpl.resetOriginalValues();

		return testQuestionImpl;
	}

	public int compareTo(TestQuestion testQuestion) {
		int value = 0;

		if (getQuestionId() < testQuestion.getQuestionId()) {
			value = -1;
		}
		else if (getQuestionId() > testQuestion.getQuestionId()) {
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

		TestQuestion testQuestion = null;

		try {
			testQuestion = (TestQuestion)obj;
		}
		catch (ClassCastException cce) {
			return false;
		}

		long primaryKey = testQuestion.getPrimaryKey();

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
	public void resetOriginalValues() {
		TestQuestionModelImpl testQuestionModelImpl = this;

		testQuestionModelImpl._originalUuid = testQuestionModelImpl._uuid;

		testQuestionModelImpl._originalActId = testQuestionModelImpl._actId;

		testQuestionModelImpl._setOriginalActId = false;

		testQuestionModelImpl._columnBitmask = 0;
	}

	@Override
	public CacheModel<TestQuestion> toCacheModel() {
		TestQuestionCacheModel testQuestionCacheModel = new TestQuestionCacheModel();

		testQuestionCacheModel.uuid = getUuid();

		String uuid = testQuestionCacheModel.uuid;

		if ((uuid != null) && (uuid.length() == 0)) {
			testQuestionCacheModel.uuid = null;
		}

		testQuestionCacheModel.questionId = getQuestionId();

		testQuestionCacheModel.actId = getActId();

		testQuestionCacheModel.text = getText();

		String text = testQuestionCacheModel.text;

		if ((text != null) && (text.length() == 0)) {
			testQuestionCacheModel.text = null;
		}

		testQuestionCacheModel.questionType = getQuestionType();

		return testQuestionCacheModel;
	}

	@Override
	public String toString() {
		StringBundler sb = new StringBundler(11);

		sb.append("{uuid=");
		sb.append(getUuid());
		sb.append(", questionId=");
		sb.append(getQuestionId());
		sb.append(", actId=");
		sb.append(getActId());
		sb.append(", text=");
		sb.append(getText());
		sb.append(", questionType=");
		sb.append(getQuestionType());
		sb.append("}");

		return sb.toString();
	}

	public String toXmlString() {
		StringBundler sb = new StringBundler(19);

		sb.append("<model><model-name>");
		sb.append("com.liferay.lms.model.TestQuestion");
		sb.append("</model-name>");

		sb.append(
			"<column><column-name>uuid</column-name><column-value><![CDATA[");
		sb.append(getUuid());
		sb.append("]]></column-value></column>");
		sb.append(
			"<column><column-name>questionId</column-name><column-value><![CDATA[");
		sb.append(getQuestionId());
		sb.append("]]></column-value></column>");
		sb.append(
			"<column><column-name>actId</column-name><column-value><![CDATA[");
		sb.append(getActId());
		sb.append("]]></column-value></column>");
		sb.append(
			"<column><column-name>text</column-name><column-value><![CDATA[");
		sb.append(getText());
		sb.append("]]></column-value></column>");
		sb.append(
			"<column><column-name>questionType</column-name><column-value><![CDATA[");
		sb.append(getQuestionType());
		sb.append("]]></column-value></column>");

		sb.append("</model>");

		return sb.toString();
	}

	private static ClassLoader _classLoader = TestQuestion.class.getClassLoader();
	private static Class<?>[] _escapedModelProxyInterfaces = new Class[] {
			TestQuestion.class
		};
	private String _uuid;
	private String _originalUuid;
	private long _questionId;
	private long _actId;
	private long _originalActId;
	private boolean _setOriginalActId;
	private String _text;
	private long _questionType;
	private long _columnBitmask;
	private TestQuestion _escapedModelProxy;
}