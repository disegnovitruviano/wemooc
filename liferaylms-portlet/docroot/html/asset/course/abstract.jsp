<%@page import="com.liferay.lms.model.Course"%>
<%@page import="java.util.List"%>
<%@page import="com.liferay.portal.kernel.util.HtmlUtil"%>
<%@page import="com.liferay.portal.kernel.repository.model.FileEntry"%>
<%@page import="com.liferay.portlet.documentlibrary.service.DLAppLocalServiceUtil"%>
<%@page import="com.liferay.portlet.documentlibrary.util.DLUtil"%>
<%@page import="com.liferay.portlet.expando.model.ExpandoTableConstants"%>
<%@page import="com.liferay.portlet.asset.service.AssetEntryLocalServiceUtil"%>
<%@page import="com.liferay.portlet.asset.model.AssetEntry"%>
<%@page import="com.liferay.portlet.asset.model.AssetRenderer"%>
<%@page import="com.liferay.portlet.asset.model.AssetRendererFactory"%>
<%@page import="com.liferay.portlet.asset.AssetRendererFactoryRegistryUtil"%>
<%@page import="com.liferay.portal.service.GroupLocalServiceUtil"%>
<%@ include file="/init.jsp"%>
<%
String abstractLength = renderRequest.getPreferences().getValue("abstractLength", "200");

Course course=(Course)request.getAttribute("course");
Group generatedGroup=GroupLocalServiceUtil.getGroup(course.getGroupCreatedId());
AssetRendererFactory assetRendererFactory = AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(Course.class.getName());
AssetRenderer assetRenderer = assetRendererFactory.getAssetRenderer(course.getCourseId());
AssetEntry assetEntry=AssetEntryLocalServiceUtil.fetchEntry(Course.class.getName(), course.getCourseId());
%>
<div class="asset-resource-image">
<%
if (Validator.isNotNull(course.getIcon())) 
{
	long logoId = course.getIcon();
	FileEntry fileEntry = DLAppLocalServiceUtil.getFileEntry(logoId);
	%>
	<img class="courselogo" src="<%= DLUtil.getPreviewURL(fileEntry, fileEntry.getFileVersion(), themeDisplay, StringPool.BLANK) %>">
	
	<%
}
else
{
	if(generatedGroup.getPublicLayoutSet().getLogo())
	{
	long logoId = generatedGroup.getPublicLayoutSet().getLogoId();
	%>
	<img class="courselogo" src="/image/layout_set_logo?img_id=<%=logoId%>">
	
	<%
	}
	else
	{
		%>
		<liferay-ui:icon
							image='<%= "../file_system/large/course" %>'
							label="<%= false %>"
							message=""	
						/>
		<%
	}
}
%>
	</div>
<%
String summary = assetEntry.getSummary(themeDisplay.getLocale());

summary = StringUtil.shorten(summary, Integer.valueOf(abstractLength), "...");

%>
<p><%= summary %></p>