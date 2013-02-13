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

package com.liferay.lms.model;

import com.liferay.portal.model.BaseModel;
import com.liferay.portal.model.CacheModel;
import com.liferay.portal.service.ServiceContext;

import com.liferay.portlet.expando.model.ExpandoBridge;

import java.io.Serializable;

import java.util.Date;

/**
 * The base model interface for the CheckP2pMailing service. Represents a row in the &quot;Lms_CheckP2pMailing&quot; database table, with each column mapped to a property of this class.
 *
 * <p>
 * This interface and its corresponding implementation {@link com.liferay.lms.model.impl.CheckP2pMailingModelImpl} exist only as a container for the default property accessors generated by ServiceBuilder. Helper methods and all application logic should be put in {@link com.liferay.lms.model.impl.CheckP2pMailingImpl}.
 * </p>
 *
 * @author TLS
 * @see CheckP2pMailing
 * @see com.liferay.lms.model.impl.CheckP2pMailingImpl
 * @see com.liferay.lms.model.impl.CheckP2pMailingModelImpl
 * @generated
 */
public interface CheckP2pMailingModel extends BaseModel<CheckP2pMailing> {
	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this interface directly. All methods that expect a check p2p mailing model instance should use the {@link CheckP2pMailing} interface instead.
	 */

	/**
	 * Returns the primary key of this check p2p mailing.
	 *
	 * @return the primary key of this check p2p mailing
	 */
	public long getPrimaryKey();

	/**
	 * Sets the primary key of this check p2p mailing.
	 *
	 * @param primaryKey the primary key of this check p2p mailing
	 */
	public void setPrimaryKey(long primaryKey);

	/**
	 * Returns the check p2p ID of this check p2p mailing.
	 *
	 * @return the check p2p ID of this check p2p mailing
	 */
	public long getCheckP2pId();

	/**
	 * Sets the check p2p ID of this check p2p mailing.
	 *
	 * @param checkP2pId the check p2p ID of this check p2p mailing
	 */
	public void setCheckP2pId(long checkP2pId);

	/**
	 * Returns the act ID of this check p2p mailing.
	 *
	 * @return the act ID of this check p2p mailing
	 */
	public long getActId();

	/**
	 * Sets the act ID of this check p2p mailing.
	 *
	 * @param actId the act ID of this check p2p mailing
	 */
	public void setActId(long actId);

	/**
	 * Returns the date of this check p2p mailing.
	 *
	 * @return the date of this check p2p mailing
	 */
	public Date getDate();

	/**
	 * Sets the date of this check p2p mailing.
	 *
	 * @param date the date of this check p2p mailing
	 */
	public void setDate(Date date);

	public boolean isNew();

	public void setNew(boolean n);

	public boolean isCachedModel();

	public void setCachedModel(boolean cachedModel);

	public boolean isEscapedModel();

	public Serializable getPrimaryKeyObj();

	public void setPrimaryKeyObj(Serializable primaryKeyObj);

	public ExpandoBridge getExpandoBridge();

	public void setExpandoBridgeAttributes(ServiceContext serviceContext);

	public Object clone();

	public int compareTo(CheckP2pMailing checkP2pMailing);

	public int hashCode();

	public CacheModel<CheckP2pMailing> toCacheModel();

	public CheckP2pMailing toEscapedModel();

	public String toString();

	public String toXmlString();
}