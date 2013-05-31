package com.liferay.lms.learningactivity;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.portlet.PortletResponse;

import com.liferay.lms.model.LearningActivity;
import com.liferay.lms.model.LearningActivityClp;
import com.liferay.lms.service.ClpSerializer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.portlet.PortletClassLoaderUtil;
import com.liferay.portal.kernel.upload.UploadRequest;
import com.liferay.portal.kernel.util.ClassLoaderProxy;
import com.liferay.portal.kernel.util.MethodHandler;
import com.liferay.portal.kernel.util.MethodKey;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.kernel.xml.DocumentException;
import com.liferay.portlet.asset.model.AssetRenderer;

public class LearningActivityTypeClp implements LearningActivityType {

	private ClassLoaderProxy clp;
	
	public LearningActivityTypeClp(ClassLoaderProxy clp) {
		this.clp = clp;
	}
		
	public java.lang.Object invokeMethod(java.lang.String name,
			java.lang.String[] parameterTypes, java.lang.Object[] arguments)
			throws java.lang.Throwable {
		throw new UnsupportedOperationException();
	}
		
	public long getDefaultScore() {
		Object returnObj = null;

		try {
			returnObj = clp.invoke("getDefaultScore", new Object[] {});
		}
		catch (Throwable t) {
			t = ClpSerializer.translateThrowable(t);

			if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			}
			else {
				throw new RuntimeException(t.getClass().getName() +
					" is not a valid exception");
			}
		}

		return ((Long)returnObj).longValue();
	}
	
	public long getDefaultTries() {
		Object returnObj = null;

		try {
			returnObj = clp.invoke("getDefaultTries", new Object[] {});
		}
		catch (Throwable t) {
			t = ClpSerializer.translateThrowable(t);

			if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			}
			else {
				throw new RuntimeException(t.getClass().getName() +
					" is not a valid exception");
			}
		}

		return ((Long)returnObj).longValue();
	}
	
	public long getTypeId() {
		Object returnObj = null;

		try {
			returnObj = clp.invoke("getTypeId",	new Object[] {});
		}
		catch (Throwable t) {
			t = ClpSerializer.translateThrowable(t);

			if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			}
			else {
				throw new RuntimeException(t.getClass().getName() +
					" is not a valid exception");
			}
		}

		return ((Long)returnObj).longValue();
	}
	
	public String getDefaultFeedbackCorrect() {
		Object returnObj = null;

		try {
			returnObj = clp.invoke("getDefaultFeedbackCorrect", new Object[] {});
		}
		catch (Throwable t) {
			t = ClpSerializer.translateThrowable(t);

			if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			}
			else {
				throw new RuntimeException(t.getClass().getName() +
					" is not a valid exception");
			}
		}

		return ((String)returnObj);
	}
	
	public String getDefaultFeedbackNoCorrect() {
		Object returnObj = null;

		try {
			returnObj = clp.invoke("getDefaultFeedbackNoCorrect", new Object[] {});
		}
		catch (Throwable t) {
			t = ClpSerializer.translateThrowable(t);

			if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			}
			else {
				throw new RuntimeException(t.getClass().getName() +
					" is not a valid exception");
			}
		}

		return ((String)returnObj);
	}
	
	public boolean isScoreConfigurable() {
		Object returnObj = null;

		try {
			returnObj = clp.invoke("isScoreConfigurable", new Object[] {});
		}
		catch (Throwable t) {
			t = ClpSerializer.translateThrowable(t);

			if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			}
			else {
				throw new RuntimeException(t.getClass().getName() +
					" is not a valid exception");
			}
		}

		return ((Boolean)returnObj).booleanValue();
	}
	
	public boolean isTriesConfigurable() {
		Object returnObj = null;

		try {
			returnObj = clp.invoke("isTriesConfigurable", new Object[] {});
		}
		catch (Throwable t) {
			t = ClpSerializer.translateThrowable(t);

			if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			}
			else {
				throw new RuntimeException(t.getClass().getName() +
					" is not a valid exception");
			}
		}

		return ((Boolean)returnObj).booleanValue();
	}
	
	public boolean isFeedbackCorrectConfigurable() {
		Object returnObj = null;

		try {
			returnObj = clp.invoke("isFeedbackCorrectConfigurable", new Object[] {});
		}
		catch (Throwable t) {
			t = ClpSerializer.translateThrowable(t);

			if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			}
			else {
				throw new RuntimeException(t.getClass().getName() +
					" is not a valid exception");
			}
		}

		return ((Boolean)returnObj).booleanValue();
	}
	
	public boolean isFeedbackNoCorrectConfigurable() {
		Object returnObj = null;

		try {
			returnObj = clp.invoke("isFeedbackNoCorrectConfigurable", new Object[] {});
		}
		catch (Throwable t) {
			t = ClpSerializer.translateThrowable(t);

			if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			}
			else {
				throw new RuntimeException(t.getClass().getName() +
					" is not a valid exception");
			}
		}

		return ((Boolean)returnObj).booleanValue();
	}
	
	public String getName() {
		Object returnObj = null;

		try {
			returnObj = clp.invoke("getName", new Object[] {});
		}
		catch (Throwable t) {
			t = ClpSerializer.translateThrowable(t);

			if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			}
			else {
				throw new RuntimeException(t.getClass().getName() +
					" is not a valid exception");
			}
		}

		return ((String)returnObj);
	}
	
	private Object translateLearningActivity(LearningActivity larn) {
		Object larnObj = null;
		try {
			larnObj = Class.forName(LearningActivityClp.class.getName(), true, clp.getClassLoader()).newInstance();
			
			ClassLoaderProxy clp2 = new ClassLoaderProxy(larnObj, LearningActivityClp.class.getName(), clp.getClassLoader());
			clp2.invoke("setModelAttributes", new Object[] {larn.getModelAttributes()});
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (Throwable t) {
			t = ClpSerializer.translateThrowable(t);

			if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			}
			else {
				throw new RuntimeException(t.getClass().getName() +
					" is not a valid exception");
			}
		}
		return larnObj;
	}
	
	public AssetRenderer getAssetRenderer(LearningActivity larn) {
		Object returnObj = null;

		try {
			returnObj = clp.invoke("getAssetRenderer", new Object[] {translateLearningActivity(larn)});
		}
		catch (Throwable t) {
			t = ClpSerializer.translateThrowable(t);

			if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			}
			else {
				throw new RuntimeException(t.getClass().getName() +
					" is not a valid exception");
			}
		}

		return (AssetRenderer)returnObj;
	}
	
	public String getUrlIcon() {
		Object returnObj = null;

		try {
			returnObj = clp.invoke("getUrlIcon", new Object[] {});
		}
		catch (Throwable t) {
			t = ClpSerializer.translateThrowable(t);

			if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			}
			else {
				throw new RuntimeException(t.getClass().getName() +
					" is not a valid exception");
			}
		}

		return ((String)returnObj);
	}
	
	public String getDescription() {
		Object returnObj = null;

		try {
			returnObj = clp.invoke("getDescription", new Object[] {});
		}
		catch (Throwable t) {
			t = ClpSerializer.translateThrowable(t);

			if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			}
			else {
				throw new RuntimeException(t.getClass().getName() +
					" is not a valid exception");
			}
		}

		return ((String)returnObj);
	}
	
	public boolean gradebook() {
		Object returnObj = null;

		try {
			returnObj = clp.invoke("gradebook", new Object[] {});
		}
		catch (Throwable t) {
			t = ClpSerializer.translateThrowable(t);

			if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			}
			else {
				throw new RuntimeException(t.getClass().getName() +
					" is not a valid exception");
			}
		}

		return ((Boolean)returnObj).booleanValue();
	}
	
	public boolean hasEditDetails() {
		Object returnObj = null;

		try {
			returnObj = clp.invoke("hasEditDetails", new Object[] {});
		}
		catch (Throwable t) {
			t = ClpSerializer.translateThrowable(t);

			if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			}
			else {
				throw new RuntimeException(t.getClass().getName() +
					" is not a valid exception");
			}
		}

		return ((Boolean)returnObj).booleanValue();
	}
	
	public String getExpecificContentPage() {
		Object returnObj = null;

		try {
			returnObj = clp.invoke("getExpecificContentPage", new Object[] {});
		}
		catch (Throwable t) {
			t = ClpSerializer.translateThrowable(t);

			if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			}
			else {
				throw new RuntimeException(t.getClass().getName() +
					" is not a valid exception");
			}
		}

		return ((String)returnObj);
	}
	
	public void setExtraContent(UploadRequest uploadRequest, 
			PortletResponse portletResponse,LearningActivity learningActivity) 
			throws PortalException,SystemException,DocumentException,IOException {
		try {
			ClassLoader classLoader = clp.getClassLoader();
			Class learningActivityTypeClass = Class.forName(LearningActivityType.class.getName(),true, classLoader);
			Class uploadRequestActivityClass = Class.forName(UploadRequest.class.getName());
			Class portletResponseClass = Class.forName(PortletResponse.class.getName());
			Class learningActivityClass = Class.forName(LearningActivity.class.getName(),true, classLoader);
			
			Method setExtraContentMethod = learningActivityTypeClass.getMethod("setExtraContent", uploadRequestActivityClass,portletResponseClass, learningActivityClass);    
			clp.invoke(new MethodHandler(setExtraContentMethod, uploadRequest, portletResponse, translateLearningActivity(learningActivity)));
		}
		catch (Throwable t) {
			t = ClpSerializer.translateThrowable(t);
			
			if (t instanceof com.liferay.portal.kernel.exception.PortalException) {
				throw (com.liferay.portal.kernel.exception.PortalException)t;
			}
			
			if (t instanceof IOException) {
				throw (IOException)t;
			}
			
			if (t instanceof DocumentException) {
				throw (DocumentException)t;
			}
			
			if (t instanceof com.liferay.portal.kernel.exception.SystemException) {
				throw (com.liferay.portal.kernel.exception.SystemException)t;
			}

			if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			}
			else {
				throw new RuntimeException(t.getClass().getName() +
					" is not a valid exception");
			}
		}

	}
	
	public boolean especificValidations(UploadRequest uploadRequest,PortletResponse portletResponse) {
		Object returnObj = null;

		try {
			ClassLoader classLoader = clp.getClassLoader();
			
			Class learningActivityTypeClass = Class.forName(LearningActivityType.class.getName(),true, classLoader);
			Class uploadRequestActivityClass = Class.forName(UploadRequest.class.getName());
			Class portletResponseClass = Class.forName(PortletResponse.class.getName());
		    
			Method especificValidationsMethod = learningActivityTypeClass.getMethod("especificValidations", uploadRequestActivityClass,portletResponseClass);    
		    
			returnObj = clp.invoke(new MethodHandler(especificValidationsMethod, uploadRequest, portletResponse));
		}
		catch (Throwable t) {
			t = ClpSerializer.translateThrowable(t);

			if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			}
			else {
				t.printStackTrace();
				throw new RuntimeException(t.getClass().getName() +
					" is not a valid exception");
			}
		}
		return ((Boolean)returnObj).booleanValue();
	}
	
	public void afterInsertOrUpdate(UploadRequest uploadRequest,PortletResponse portletResponse,LearningActivity learningActivity) 
			throws PortalException,SystemException {
		
		try {
			ClassLoader classLoader = clp.getClassLoader();
			Class learningActivityTypeClass = Class.forName(LearningActivityType.class.getName(),true, classLoader);
			Class uploadRequestActivityClass = Class.forName(UploadRequest.class.getName());
			Class portletResponseClass = Class.forName(PortletResponse.class.getName());
			Class learningActivityClass = Class.forName(LearningActivity.class.getName(),true, classLoader);
			
			Method afterInsertOrUpdateMethod = learningActivityTypeClass.getMethod("afterInsertOrUpdate", uploadRequestActivityClass,portletResponseClass, learningActivityClass);    
			clp.invoke(new MethodHandler(afterInsertOrUpdateMethod, uploadRequest, portletResponse, translateLearningActivity(learningActivity)));
		}
		catch (Throwable t) {
			t = ClpSerializer.translateThrowable(t);
			
			if (t instanceof com.liferay.portal.kernel.exception.PortalException) {
				throw (com.liferay.portal.kernel.exception.PortalException)t;
			}
						
			if (t instanceof com.liferay.portal.kernel.exception.SystemException) {
				throw (com.liferay.portal.kernel.exception.SystemException)t;
			}

			if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			}
			else {
				throw new RuntimeException(t.getClass().getName() +
					" is not a valid exception");
			}
		}
	}
	
	@Override
	public String getMesageEditDetails() {
		Object returnObj = null;

		try {
			returnObj = clp.invoke("getMesageEditDetails", new Object[] {});
		}
		catch (Throwable t) {
			t = ClpSerializer.translateThrowable(t);

			if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			}
			else {
				throw new RuntimeException(t.getClass().getName() +
					" is not a valid exception");
			}
		}

		return ((String)returnObj);
	}
	
	public String getPortletId() {
		Object returnObj = null;

		try {
			returnObj = clp.invoke("getPortletId", new Object[] {});
		}
		catch (Throwable t) {
			t = ClpSerializer.translateThrowable(t);

			if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			}
			else {
				throw new RuntimeException(t.getClass().getName() +
					" is not a valid exception");
			}
		}

		return ((String)returnObj);
	}

}
