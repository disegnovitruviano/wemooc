<%@page import="com.liferay.portlet.asset.model.AssetRendererFactory"%>
<%@page import="com.liferay.portlet.asset.model.AssetRenderer"%>
<%@page import="com.liferay.portlet.asset.AssetRendererFactoryRegistryUtil"%>
<%@page import="com.liferay.portal.kernel.exception.PortalException"%>
<%@page import="com.liferay.portlet.asset.service.AssetEntryLocalServiceUtil"%>
<%@page import="com.liferay.portlet.asset.model.AssetEntry"%>
<%@page import="com.liferay.lms.service.LearningActivityLocalServiceUtil"%>
<%@page import="com.liferay.lms.model.LearningActivity"%>
<%@page import="com.liferay.portal.kernel.portlet.LiferayWindowState"%>
<%@ include file="/init.jsp" %>

<liferay-portlet:renderURL var="selectResource" windowState="<%=LiferayWindowState.POP_UP.toString() %>">
	<liferay-portlet:param name="jspPage" value="/html/scormactivity/admin/searchresource.jsp"/>
</liferay-portlet:renderURL>

<script type="text/javascript">
<!--

function <portlet:namespace />search() {
	AUI().use('node',function(A) {
		var backbutton = A.one('#<portlet:namespace/>backButton').one('span').clone();
		backbutton.setAttribute('id','<portlet:namespace/>backbutton');
		A.one('.portlet-body').prepend(backbutton);
		var iframe = A.Node.create('<iframe id="<portlet:namespace/>finder" src="javascript:false;" onload="<portlet:namespace />load(this)" frameBorder="0" scrolling="no" width="'+A.getBody().width()+'" height="0"></iframe>');
		A.one('.portlet-body').append(iframe);
		A.all('.acticons').each(function(icon){ icon.hide(); });
		A.one('#<portlet:namespace/>fm').hide();
		iframe.setAttribute('src','<%=selectResource %>');
	});
}
	
function <portlet:namespace />load(source) {

	AUI().use('node','querystring-parse',function(A) {
		
		var params=A.QueryString.parse(source.contentWindow.location.search.replace('?',''));
	
		if((params['<portlet:namespace />jspPage']=='/html/scormactivity/admin/result.jsp')&&
           (A.Lang.isNumber(params['<portlet:namespace />assertId']))&&
           (A.Lang.isString(params['<portlet:namespace />assertTitle']))) {
			A.one('#<portlet:namespace/>backbutton').remove();
			A.one('#<portlet:namespace/>finder').remove();
			A.all('.acticons').each(function(icon){ icon.show(); });
			A.one('#<portlet:namespace/>fm').show();
			A.one('#<portlet:namespace/>assetEntryId').set('value',params['<portlet:namespace />assertId']);		
			A.one('#<portlet:namespace/>assetEntryName').set('value',params['<portlet:namespace />assertTitle']);	
		}
		else {
		    if (source.Document && source.Document.body.scrollHeight) 
		        source.height = source.contentWindow.document.body.scrollHeight;
		    else if (source.contentDocument && source.contentDocument.body.scrollHeight) 
		        source.height = source.contentDocument.body.scrollHeight + 35;
		    else (source.contentDocument && source.contentDocument.body.offsetHeight) 
		        source.height = source.contentDocument.body.offsetHeight + 35;
		}
	
	});
}

function <portlet:namespace />back() {
	AUI().use('node',function(A) {
		A.one('#<portlet:namespace/>backbutton').remove();
		A.one('#<portlet:namespace/>finder').remove();
		A.all('.acticons').each(function(icon){ icon.show(); });
		A.one('#<portlet:namespace/>fm').show();

	});
}

//-->
</script>

<%
long assetId=0;
Boolean openWindow = true;
String assetTitle=StringPool.BLANK;

if(request.getAttribute("activity")!=null) {	
	LearningActivity learningActivity=(LearningActivity)request.getAttribute("activity");
	if ((learningActivity.getExtracontent()!=null)&&(learningActivity.getExtracontent().trim().length()!=0)) {

		try{
			AssetEntry entry=AssetEntryLocalServiceUtil.getEntry(
				GetterUtil.getLong(LearningActivityLocalServiceUtil.getExtraContentValue(learningActivity.getActId(),"assetEntry")));
			assetId=entry.getEntryId();
			
			openWindow = GetterUtil.getBoolean(LearningActivityLocalServiceUtil.getExtraContentValue(learningActivity.getActId(), "openWindow"));
			
			AssetRendererFactory assetRendererFactory=AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(entry.getClassName());
			AssetRenderer assetRenderer= AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(entry.getClassName()).getAssetRenderer(entry.getClassPK());			
			assetTitle=assetRenderer.getTitle(themeDisplay.getLocale());	
		}
		catch(PortalException e)
		{
		}
	}	
}

%>

<aui:input type="hidden" name="assetEntryId" ignoreRequestValue="true" value="<%=Long.toString(assetId) %>"/>
<aui:field-wrapper name="scormactivity.edit.asserts" cssClass="search-button-container">
	<aui:input type="text" name="assetEntryName" ignoreRequestValue="true" value="<%=assetTitle %>" label="" inlineField="true" disabled="true"  size="50"/>
	<button type="button" id="<portlet:namespace/>searchEntry" onclick="<portlet:namespace/>search();" >
	    <span class="aui-buttonitem-icon aui-icon aui-icon-search"></span>
	    <span class="aui-buttonitem-label"><%= LanguageUtil.get(pageContext, "search") %></span>
	</button>
</aui:field-wrapper>
<aui:field-wrapper name="scormactivity.edit.openwindow">
	<aui:input type="checkbox" name="openWindow" value="<%= String.valueOf(openWindow) %>" />
</aui:field-wrapper>
	
<div id="<portlet:namespace/>backButton" style="display:none;">
	<liferay-ui:icon image="back" message="back" url="<%=\"javascript:\"+renderResponse.getNamespace()+\"back();\" %>" label="true"  />
</div>

