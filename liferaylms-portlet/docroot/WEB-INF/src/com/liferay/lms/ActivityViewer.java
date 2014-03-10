package com.liferay.lms;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.portlet.MimeResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ValidatorException;
import javax.portlet.filter.RenderResponseWrapper;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.liferay.lms.learningactivity.LearningActivityType;
import com.liferay.lms.learningactivity.LearningActivityTypeRegistry;
import com.liferay.lms.model.LearningActivity;
import com.liferay.lms.service.LearningActivityLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.io.WriterOutputStream;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.LayoutConstants;
import com.liferay.portal.model.Portlet;
import com.liferay.portal.model.PortletConstants;
import com.liferay.portal.model.PortletWrapper;
import com.liferay.portal.model.PublicRenderParameter;
import com.liferay.portal.model.ResourceConstants;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.service.LayoutLocalServiceUtil;
import com.liferay.portal.service.PortletLocalServiceUtil;
import com.liferay.portal.service.ResourceActionLocalServiceUtil;
import com.liferay.portal.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.service.ResourcePermissionServiceUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.permission.PortletPermissionUtil;
import com.liferay.portal.theme.PortletDisplay;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.PortletConfigFactoryUtil;
import com.liferay.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portlet.PortletQName;
import com.liferay.portlet.PortletQNameUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;

import static com.liferay.lms.asset.LearningActivityBaseAssetRenderer.ACTION_VIEW;

/**
 * Portlet implementation class ActivityViewer
 */
public class ActivityViewer extends MVCPortlet 
{

	private static Set<String> reservedAttrs = new HashSet<String>();
	private volatile Constructor<?> createComponentContext;
	private volatile Method getContext;
	private volatile Method setContext;
	private volatile Method getPublicParameters;

	static {
		reservedAttrs.add(WebKeys.PAGE_TOP);
		reservedAttrs.add(WebKeys.AUI_SCRIPT_DATA);
	}
	
	
	@Override
	public void init(PortletConfig config) throws PortletException {
		super.init(config);
		try {
 			Class<?> publicRenderParametersPoolClass = PortalClassLoaderUtil.getClassLoader().loadClass("com.liferay.portlet.PublicRenderParametersPool");
 			getPublicParameters = publicRenderParametersPoolClass.getMethod("get", HttpServletRequest.class, Long.TYPE);
			
 			Class<?> componentContextClass = PortalClassLoaderUtil.getClassLoader().loadClass("org.apache.struts.tiles.ComponentContext");
			createComponentContext = componentContextClass.getConstructor(Map.class);
			getContext = componentContextClass.getMethod("getContext", ServletRequest.class);
			setContext = componentContextClass.getMethod("setContext",componentContextClass,ServletRequest.class);
		} catch (Throwable e) {
			throw new PortletException(e);
		}
	}
	
	@SuppressWarnings({"unchecked"})
	private final Map<String, String[]> getPublicParameters(HttpServletRequest request, long plid) throws SystemException{
		try {
			return (Map<String, String[]>) getPublicParameters.invoke(null, request, plid);
		} catch (Throwable e) {
			throw new SystemException(e);
		} 
	}
	
	private final  Object createComponentContext(Map<String,String> attributes) throws SystemException{
		try {
			return createComponentContext.newInstance(attributes);
		} catch (Throwable e) {
			throw new SystemException(e);
		} 
	}

	private final  Object getContext(ServletRequest servletRequest) throws SystemException{
		try {
			return getContext.invoke(null,servletRequest);
		} catch (Throwable e) {
			throw new SystemException(e);
		} 
	}

	private final void setContext(Object componetContext,ServletRequest servletRequest) throws SystemException{
		try {
			setContext.invoke(null,componetContext,servletRequest);
		} catch (Throwable e) {
			throw new SystemException(e);
		} 
	}

	@Override
	public void render(RenderRequest renderRequest, RenderResponse renderResponse)
			throws PortletException, IOException {
		ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
		boolean isWidget = themeDisplay.isWidget();
		long actId=GetterUtil.DEFAULT_LONG;		
		if((!isWidget)&&
				(ParamUtil.getBoolean(renderRequest, "actionEditingDetails", false))){
			actId=ParamUtil.getLong(renderRequest, "resId", 0);
			renderResponse.setProperty("clear-request-parameters",Boolean.TRUE.toString());
		}
		else{
			actId=ParamUtil.getLong(renderRequest, "actId");
		}
		
		if(Validator.isNull(actId)) {
			renderRequest.setAttribute(WebKeys.PORTLET_CONFIGURATOR_VISIBILITY, Boolean.FALSE);
		}
		else {
			try {
				LearningActivity learningActivity = LearningActivityLocalServiceUtil.getLearningActivity(actId);
	
				if(Validator.isNull(learningActivity)) {
					renderRequest.setAttribute(WebKeys.PORTLET_CONFIGURATOR_VISIBILITY, Boolean.FALSE);
				}
				else {
					LearningActivityType learningActivityType=new LearningActivityTypeRegistry().getLearningActivityType(learningActivity.getTypeId());
					
					if((Validator.isNull(learningActivityType))||
					   ((!isWidget)&&
					    (themeDisplay.getLayoutTypePortlet().getPortletIds().contains(learningActivityType.getPortletId())))) {
						renderRequest.setAttribute(WebKeys.PORTLET_CONFIGURATOR_VISIBILITY, Boolean.FALSE);
					}
					else {
						Portlet portlet = PortletLocalServiceUtil.getPortletById(themeDisplay.getCompanyId(), learningActivityType.getPortletId());
						HttpServletRequest renderHttpServletRequest = PortalUtil.getHttpServletRequest(renderRequest);
						PortletPreferencesFactoryUtil.getLayoutPortletSetup(themeDisplay.getLayout(), portlet.getPortletId());
						
						if(isWidget) {
							Map<String, String[]> publicParameters = getPublicParameters(renderHttpServletRequest, themeDisplay.getPlid());
							for(PublicRenderParameter publicRenderParameter:portlet.getPublicRenderParameters()) {
								String[] parameterValues = renderRequest.getParameterValues(publicRenderParameter.getIdentifier());
								if(Validator.isNotNull(parameterValues)) {
									String publicRenderParameterName = PortletQNameUtil.getPublicRenderParameterName(publicRenderParameter.getQName());
									String[] currentValues = publicParameters.get(publicRenderParameterName);
									if(Validator.isNotNull(currentValues)){
										parameterValues = ArrayUtil.append(parameterValues, currentValues);
									}
									publicParameters.put(publicRenderParameterName, parameterValues);
								}
							}
							renderResponse.setProperty("clear-request-parameters",StringPool.TRUE);

							if(ParamUtil.getBoolean(renderRequest, "scriptMobile",true)) {
								RenderResponseWrapper renderResponseWrapper = new RenderResponseWrapper(renderResponse) {
									private final StringWriter stringWriter = new StringWriter();
								
									@Override
									public PrintWriter getWriter() throws IOException {
										return new PrintWriter(stringWriter);
									}
									
									@Override
									public OutputStream getPortletOutputStream()
										throws IOException {
										return new WriterOutputStream(stringWriter);
									}
									
									@Override
									public String toString() {
										return stringWriter.toString();
									}
							
								};
								include("/html/activityViewer/scriptMobile.jsp", renderRequest, renderResponseWrapper);

								StringBundler pageTopStringBundler = (StringBundler)renderRequest.getAttribute(WebKeys.PAGE_TOP);
	
								if (pageTopStringBundler == null) {
									pageTopStringBundler = new StringBundler();
									renderRequest.setAttribute(WebKeys.PAGE_TOP, pageTopStringBundler);
								}
								
								
								pageTopStringBundler.append(renderResponseWrapper.toString());
							}
						}							

						String activityContent = renderPortlet(renderRequest, renderResponse, 
								themeDisplay, themeDisplay.getScopeGroupId(), portlet, isWidget, true);
						renderResponse.setContentType(ContentTypes.TEXT_HTML_UTF8);
						renderResponse.getWriter().print(activityContent);
						
						String resourcePrimKey = PortletPermissionUtil.getPrimaryKey(
								themeDisplay.getPlid(), learningActivityType.getPortletId());
						String portletName = learningActivityType.getPortletId();

						int warSeparatorIndex = portletName.indexOf(PortletConstants.WAR_SEPARATOR);
						if (warSeparatorIndex != -1) {
							portletName = portletName.substring(0, warSeparatorIndex);
						}

						if ((ResourcePermissionLocalServiceUtil.getResourcePermissionsCount(
								themeDisplay.getCompanyId(), portletName,
								ResourceConstants.SCOPE_INDIVIDUAL, resourcePrimKey) == 0)&&
							(ResourceActionLocalServiceUtil.fetchResourceAction(portletName, ACTION_VIEW)!=null)) {
				        	Role siteMember = RoleLocalServiceUtil.getRole(themeDisplay.getCompanyId(),RoleConstants.SITE_MEMBER);
			        		ResourcePermissionServiceUtil.setIndividualResourcePermissions(themeDisplay.getScopeGroupId(), themeDisplay.getCompanyId(), 
			        				portletName, resourcePrimKey, siteMember.getRoleId(), new String[]{ACTION_VIEW});
						}
					}
				}
			}
			catch(Throwable throwable) {
				renderRequest.setAttribute(WebKeys.PORTLET_CONFIGURATOR_VISIBILITY, Boolean.FALSE);
			}
		}
		
		//super.render(renderRequest, renderResponse);
	}

    /**
     * Renders the given portlet as a runtime portlet and returns the portlet's HTML.
     * Based on http://www.devatwork.nl/2011/07/liferay-embedding-portlets-in-your-portlet/
     * @throws PortalException 
     */
    @SuppressWarnings("unchecked")
	public String renderPortlet(final RenderRequest request, final RenderResponse response,final ThemeDisplay themeDisplay,
			final long scopeGroup, final Portlet portlet,final boolean copyNonNamespaceParameters,final boolean copyPublicParameters) 
			throws SystemException, IOException, ServletException, ValidatorException, PortalException {
        // Get servlet request / response
    	HttpServletRequest renderServletRequest = PortalUtil.getHttpServletRequest(request);
    	HttpSession renderServletSession = renderServletRequest.getSession();
        HttpServletRequest servletRequest = PortalUtil.getOriginalServletRequest(renderServletRequest);
    	HttpSession servletSession = servletRequest.getSession();
        HttpServletResponse servletResponse = PortalUtil.getHttpServletResponse(response);
    	

        
        PortletDisplay portletDisplay = themeDisplay.getPortletDisplay();
        PortletDisplay portletDisplayClone = new PortletDisplay();
        portletDisplay.copyTo(portletDisplayClone);
        final Map<String, Object> requestAttributeBackup = new HashMap<String, Object>();
        for (final String key : Collections.list((Enumeration<String>) servletRequest.getAttributeNames())) {
            requestAttributeBackup.put(key, servletRequest.getAttribute(key));
        }
        
        final Map<String, Object> sessionAttributeBackup = new HashMap<String, Object>();
        for (final String key : Collections.list((Enumeration<String>) servletSession.getAttributeNames())) {
			sessionAttributeBackup.put(key, servletSession.getAttribute(key));
        }

        for (final String key : Collections.list((Enumeration<String>) renderServletSession.getAttributeNames())) {
        	if(Validator.isNull(servletSession.getAttribute(key))) {
        		servletSession.setAttribute(key, renderServletSession.getAttribute(key));
        	}
        }
        
        // Render the portlet as a runtime portlet
        String result=null;
        long currentScopeGroup = themeDisplay.getScopeGroupId();
        String currentOuterPortlet = (String) servletRequest.getAttribute("OUTER_PORTLET_ID");
        Layout currentLayout = (Layout)servletRequest.getAttribute(WebKeys.LAYOUT);
        try {
			ServletContext servletContext = (ServletContext)servletRequest.getAttribute(WebKeys.CTX);
        	servletRequest.setAttribute(WebKeys.RENDER_PORTLET_RESOURCE, Boolean.TRUE);
        	long defaultGroupPlid = LayoutLocalServiceUtil.getDefaultPlid(scopeGroup);
        	if(defaultGroupPlid!=LayoutConstants.DEFAULT_PLID) {
        		servletRequest.setAttribute(WebKeys.LAYOUT, LayoutLocalServiceUtil.getLayout(defaultGroupPlid));
        	}

        	servletRequest.setAttribute("OUTER_PORTLET_ID",PortalUtil.getPortletId(request));

        	StringBundler queryStringStringBundler = new StringBundler();

        	if(copyNonNamespaceParameters) {
        		String portletNamespace = PortalUtil.getPortletNamespace(portlet.getPortletId());
        		Map<String, String[]> parameters = servletRequest.getParameterMap();
	        	for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
	        		if((!entry.getKey().startsWith(PortletQName.PUBLIC_RENDER_PARAMETER_NAMESPACE))&&
	        		   (!entry.getKey().startsWith(portletNamespace))&&
	        		   (!PortalUtil.isReservedParameter(entry.getKey()))&&
	        		   (!request.getPublicParameterMap().containsKey(entry.getKey()))) {
						for(String value:entry.getValue()) {
							if(queryStringStringBundler.index()!=0) {
								queryStringStringBundler.append(StringPool.AMPERSAND);
							}
							queryStringStringBundler.append(entry.getKey());
			        		queryStringStringBundler.append(StringPool.EQUAL);
			        		queryStringStringBundler.append(value);
						}
	        		}
				}
        	}

        	if(copyPublicParameters) {
	        	for (Entry<String, String[]> entry : request.getPublicParameterMap().entrySet()) {
	        		String[] values = entry.getValue();
					for(int itrValues=values.length-1;itrValues>=0;itrValues--) {
						if(queryStringStringBundler.index()!=0) {
							queryStringStringBundler.append(StringPool.AMPERSAND);
						}
						queryStringStringBundler.append(entry.getKey());
		        		queryStringStringBundler.append(StringPool.EQUAL);
		        		queryStringStringBundler.append(values[itrValues]);
					}
				}
        	}

            String renderedPortlet = PortalUtil.renderPortlet(servletContext, servletRequest, servletResponse, 
            		new PortletWrapper(portlet){
						private static final long serialVersionUID = 229422682924083706L;
		
						@Override
		        		public boolean isUseDefaultTemplate() {
		        			return false;
		        		}
					}, queryStringStringBundler.toString(), false);

            for (final String key : Collections.list((Enumeration<String>) servletSession.getAttributeNames())) {
				if(Validator.isNull(renderServletRequest.getAttribute(key))) {
					renderServletRequest.setAttribute(key, servletSession.getAttribute(key));
				}
            }

			List<String> markupHeaders = (List<String>)servletRequest.getAttribute(MimeResponse.MARKUP_HEAD_ELEMENT);
			if((Validator.isNotNull(markupHeaders))&&(!markupHeaders.isEmpty())) {
				StringBundler pageTopStringBundler = (StringBundler)request.getAttribute(WebKeys.PAGE_TOP);

				if (pageTopStringBundler == null) {
					pageTopStringBundler = new StringBundler();
					request.setAttribute(WebKeys.PAGE_TOP, pageTopStringBundler);
				}
				
				for(String markupHeader:markupHeaders) {
					pageTopStringBundler.append(markupHeader);
				}
			}

            if(portlet.isUseDefaultTemplate()) {
				String  portletHeader = StringPool.BLANK, 
						portletBody = renderedPortlet,
						portletQueue = StringPool.BLANK;

				int portletBodyBegin = renderedPortlet.indexOf(PORTLET_BODY);
				if(portletBodyBegin>0) {
					int portletBodyEnd = renderedPortlet.lastIndexOf(DIV_END, renderedPortlet.lastIndexOf(DIV_END)-1);
					portletBodyBegin+=PORTLET_BODY.length();
					portletHeader = renderedPortlet.substring(0, portletBodyBegin);
					portletBody = renderedPortlet.substring(portletBodyBegin, portletBodyEnd);
					portletQueue = renderedPortlet.substring(portletBodyEnd);
				}

				if(Validator.isNull(getContext(servletRequest))) {
					Map<String,String> attributes = new HashMap<String,String>();
					attributes.put("portlet_content", themeDisplay.getTilesContent());
					setContext(createComponentContext(attributes), servletRequest);
				}

				servletRequest.setAttribute(PortletRequest.LIFECYCLE_PHASE,PortletRequest.RENDER_PHASE);
				servletRequest.setAttribute(WebKeys.RENDER_PORTLET, portlet);
				servletRequest.setAttribute(JavaConstants.JAVAX_PORTLET_REQUEST, request);
				servletRequest.setAttribute(JavaConstants.JAVAX_PORTLET_RESPONSE, response);
				servletRequest.setAttribute(JavaConstants.JAVAX_PORTLET_CONFIG, PortletConfigFactoryUtil.create(portlet, servletContext));
				servletRequest.setAttribute("PORTLET_CONTENT", portletBody);
				result = portletHeader+
						 PortalUtil.renderPage(servletContext, servletRequest, servletResponse, "/html/common/themes/portlet.jsp",false)+
						 portletQueue;
            }
            else {
            	result = renderedPortlet;
            }

			Set<String> runtimePortletIds = (Set<String>)request.getAttribute(
					WebKeys.RUNTIME_PORTLET_IDS);

			if (runtimePortletIds == null) {
				runtimePortletIds = new HashSet<String>();
			}

			runtimePortletIds.add(portlet.getPortletId());

			request.setAttribute(WebKeys.RUNTIME_PORTLET_IDS, runtimePortletIds);
        }finally {
            // Restore the state
        	Set<Entry<String,Object>> sessionAttributesSet = sessionAttributeBackup.entrySet();
        	for (Entry<String, Object> entry : sessionAttributesSet) {
				if(Validator.isNull(servletRequest.getAttribute(entry.getKey()))) {
					servletSession.setAttribute(entry.getKey(), entry.getValue());
				}
			}
        	        	
            for (final String key : Collections.list((Enumeration<String>) servletSession.getAttributeNames())) {
            	if(!sessionAttributeBackup.containsKey(key)) {
            		servletSession.removeAttribute(key);
            	}
            }
  
        	themeDisplay.setScopeGroupId(currentScopeGroup);
            servletRequest.setAttribute(WebKeys.LAYOUT, currentLayout);
            servletRequest.setAttribute("OUTER_PORTLET_ID",currentOuterPortlet);
            portletDisplay.copyFrom(portletDisplayClone);
            portletDisplayClone.recycle();
            for (final String key : Collections.list((Enumeration<String>) servletRequest.getAttributeNames())) {
                if ((!requestAttributeBackup.containsKey(key))&&(!reservedAttrs.contains(key))) {
                    servletRequest.removeAttribute(key);
                }
            }
            for (final Map.Entry<String, Object> entry : requestAttributeBackup.entrySet()) {
                servletRequest.setAttribute(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }
    
    private static final String PORTLET_BODY = "<div class=\"portlet-body\">";
    private static final String DIV_END = "</div>";
}
