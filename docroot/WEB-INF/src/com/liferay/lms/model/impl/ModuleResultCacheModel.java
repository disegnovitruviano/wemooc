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

import com.liferay.lms.model.ModuleResult;

import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.CacheModel;

import java.io.Serializable;

/**
 * The cache model class for representing ModuleResult in entity cache.
 *
 * @author TLS
 * @see ModuleResult
 * @generated
 */
public class ModuleResultCacheModel implements CacheModel<ModuleResult>,
	Serializable {
	@Override
	public String toString() {
		StringBundler sb = new StringBundler(13);

		sb.append("{moduleId=");
		sb.append(moduleId);
		sb.append(", result=");
		sb.append(result);
		sb.append(", comments=");
		sb.append(comments);
		sb.append(", userId=");
		sb.append(userId);
		sb.append(", passed=");
		sb.append(passed);
		sb.append(", mrId=");
		sb.append(mrId);
		sb.append("}");

		return sb.toString();
	}

	public ModuleResult toEntityModel() {
		ModuleResultImpl moduleResultImpl = new ModuleResultImpl();

		moduleResultImpl.setModuleId(moduleId);
		moduleResultImpl.setResult(result);

		if (comments == null) {
			moduleResultImpl.setComments(StringPool.BLANK);
		}
		else {
			moduleResultImpl.setComments(comments);
		}

		moduleResultImpl.setUserId(userId);
		moduleResultImpl.setPassed(passed);
		moduleResultImpl.setMrId(mrId);

		moduleResultImpl.resetOriginalValues();

		return moduleResultImpl;
	}

	public long moduleId;
	public long result;
	public String comments;
	public long userId;
	public boolean passed;
	public long mrId;
}