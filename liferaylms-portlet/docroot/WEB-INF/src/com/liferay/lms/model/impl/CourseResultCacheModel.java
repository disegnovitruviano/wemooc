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

package com.liferay.lms.model.impl;

import com.liferay.lms.model.CourseResult;

import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.CacheModel;

import java.io.Serializable;

import java.util.Date;

/**
 * The cache model class for representing CourseResult in entity cache.
 *
 * @author TLS
 * @see CourseResult
 * @generated
 */
public class CourseResultCacheModel implements CacheModel<CourseResult>,
	Serializable {
	@Override
	public String toString() {
		StringBundler sb = new StringBundler(15);

		sb.append("{crId=");
		sb.append(crId);
		sb.append(", courseId=");
		sb.append(courseId);
		sb.append(", result=");
		sb.append(result);
		sb.append(", comments=");
		sb.append(comments);
		sb.append(", userId=");
		sb.append(userId);
		sb.append(", passed=");
		sb.append(passed);
		sb.append(", passedDate=");
		sb.append(passedDate);
		sb.append("}");

		return sb.toString();
	}

	public CourseResult toEntityModel() {
		CourseResultImpl courseResultImpl = new CourseResultImpl();

		courseResultImpl.setCrId(crId);
		courseResultImpl.setCourseId(courseId);
		courseResultImpl.setResult(result);

		if (comments == null) {
			courseResultImpl.setComments(StringPool.BLANK);
		}
		else {
			courseResultImpl.setComments(comments);
		}

		courseResultImpl.setUserId(userId);
		courseResultImpl.setPassed(passed);

		if (passedDate == Long.MIN_VALUE) {
			courseResultImpl.setPassedDate(null);
		}
		else {
			courseResultImpl.setPassedDate(new Date(passedDate));
		}

		courseResultImpl.resetOriginalValues();

		return courseResultImpl;
	}

	public long crId;
	public long courseId;
	public long result;
	public String comments;
	public long userId;
	public boolean passed;
	public long passedDate;
}