package com.liferay.lms;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.util.bridges.mvc.MVCPortlet;

/**
 * Portlet implementation class about
 */
public class About extends MVCPortlet {
	private static final Log log = LogFactoryUtil.getLog(About.class);
	private static final String WEBAPPS = "webapps";
	private static final String PORTLET = "portlet";
	private static final String HOOK = "hook";
	private static final String WEBINF = "WEB-INF";
	private static final String CLASSES = "classes";
	private static final String SERVICE = "service.properties";
	private static final String LIFERAY = "liferay-plugin-package.properties";
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd - MM - yyyy");

	private String viewJSP; 
	
	public void init() throws PortletException {
		viewJSP = getInitParameter("view-template");
	}
	

	public void doView(RenderRequest renderRequest,RenderResponse renderResponse) throws IOException, PortletException {
		StringBuffer server = new StringBuffer(PropsUtil.get("catalina.home"));
		
		List<String> names = new ArrayList<String>();
		HashMap<String, HashMap<String, String>> properties = new HashMap<String, HashMap<String,String>>();
		HashMap<String, HashMap<String, String>> liferay = new HashMap<String, HashMap<String,String>>();
		
		if(!StringPool.BLANK.equals(server.toString())){
			server.append(File.separator);
			server.append(WEBAPPS);
			
			File webapps = new File(server.toString());
			
			File[] apps = webapps.listFiles();
			
			if(apps!=null){
				for(File app : apps){
					if(app.isDirectory()){
						String[] parts = app.getName().split(StringPool.DASH);
						if(parts.length>1&&(HOOK.equals(parts[1])||PORTLET.equals(parts[1]))){
							if(log.isDebugEnabled())log.debug(app.getCanonicalPath());
							StringBuffer appBase = new StringBuffer(server.toString());

							appBase.append(File.separator);
							appBase.append(app.getName());
							appBase.append(File.separator);
							appBase.append(WEBINF);
							appBase.append(File.separator);

							StringBuffer appProperties = new StringBuffer(appBase.toString());
							appProperties.append(LIFERAY);
							
							StringBuffer appService = new StringBuffer(appBase.toString());
							appService.append(CLASSES);
							appService.append(File.separator);
							appService.append(SERVICE);
							
							File service = new File(appService.toString());

							if(log.isDebugEnabled())log.debug(service.getCanonicalPath());
							
							names.add(app.getName());
							
							if(service.exists()&&service.isFile()){
								Properties prop = new Properties();
								prop.load(new FileInputStream(appService.toString()));
								
								HashMap<String,String> props = new HashMap<String, String>();

								long buildNumber = 0;
								
								try{
									buildNumber = Long.valueOf(prop.getProperty("build.date",StringPool.BLANK));
								}catch(Exception e){
									if(log.isDebugEnabled())e.printStackTrace();
								}
								
								String date = sdf.format(new Date(buildNumber));
								
								props.put("build.date", date);
								props.put("build.number", prop.getProperty("build.number",StringPool.BLANK));
								props.put("build.auto.upgrade", prop.getProperty("build.auto.upgrade",StringPool.BLANK));
								
								properties.put(app.getName(), props);
								
								if(log.isDebugEnabled()){
									log.debug(date);
									log.debug(prop.getProperty("build.number",StringPool.BLANK));
									log.debug(prop.getProperty("build.auto.upgrade",StringPool.BLANK));
								}
							}
							

							File fileProperties = new File(appProperties.toString());

							if(fileProperties.exists()&&fileProperties.isFile()){
								Properties prop = new Properties();
								prop.load(new FileInputStream(appProperties.toString()));

								HashMap<String,String> props = new HashMap<String, String>();

								props.put("module-incremental-version", prop.getProperty("module-incremental-version",StringPool.BLANK));
								props.put("liferay-versions", prop.getProperty("liferay-versions",StringPool.BLANK));
								
								liferay.put(app.getName(), props);
							}
						}
					}
				}
			}
		}	
		renderRequest.setAttribute("names", names);
		renderRequest.setAttribute("properties", properties);
		renderRequest.setAttribute("liferay", liferay);
		
		include(viewJSP, renderRequest, renderResponse);	
	}

	protected void include(String path, RenderRequest renderRequest,
			RenderResponse renderResponse) throws IOException, PortletException {

		PortletRequestDispatcher portletRequestDispatcher = getPortletContext()
				.getRequestDispatcher(path);

		if (portletRequestDispatcher == null) {
		} else {
			portletRequestDispatcher.include(renderRequest, renderResponse);
		}
	}
}
