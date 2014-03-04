<%@page import="com.liferay.portal.kernel.util.PropsUtil"%>
<%@page import="java.io.IOException"%>
<%@page import="java.net.URL"%>
<%@page import="java.io.File"%>
<%@page import="com.liferay.lms.service.SCORMContentLocalServiceUtil"%>
<%@page import="com.liferay.lms.model.SCORMContent"%>

<%@ include file="/init.jsp" %>

<% SCORMContent scorm=(SCORMContent)request.getAttribute("scorm");

if (Validator.isNotNull(scorm.getDescription()) && request.getAttribute("learningTry") == null) { %>

<script type="text/javascript">
	localStorage.removeItem('scormpool');
</script>

<div class="asset-description"><%=scorm.getDescription() %></div>

<% } 
String urlIndex=themeDisplay.getPortalURL()+this.getServletContext().getContextPath()+"/scorm/"+Long.toString(scorm.getCompanyId())+"/"+Long.toString(scorm.getGroupId())+"/"+scorm.getUuid()+"/imsmanifest.xml";

String iconsDir = "/liferaylms-portlet";
String conTema = PropsUtil.get("scorm.icons.theme");
try {
	if (conTema != null && Boolean.valueOf(conTema)) {
		iconsDir = themeDisplay.getPathThemeImages()+"/custom";
	}
} catch(Exception e) {
	
}
%>

   <script type="text/javascript">
     function InitPlayer() {
       PlayerConfiguration.Debug = false;
       PlayerConfiguration.StorageSupport = true;

       PlayerConfiguration.TreeMinusIcon = "<%= iconsDir %>/icons/scorm/minus.gif";
       PlayerConfiguration.TreePlusIcon = "<%= iconsDir %>/icons/scorm/plus.gif";
       PlayerConfiguration.TreeLeafIcon = "<%= iconsDir %>/icons/scorm/leaf.gif";
       PlayerConfiguration.TreeActiveIcon = "<%= iconsDir %>/icons/scorm/select.gif";

       PlayerConfiguration.BtnPreviousLabel = "<liferay-ui:message key="scorm.previous" />";
       PlayerConfiguration.BtnContinueLabel = "<liferay-ui:message key="scorm.next" />";
       PlayerConfiguration.BtnExitLabel = "<liferay-ui:message key="scorm.exit" />";
       PlayerConfiguration.BtnExitAllLabel = "<liferay-ui:message key="activity.try.exit" />";
       PlayerConfiguration.BtnAbandonLabel = "<liferay-ui:message key="scorm.abandon" />";
       PlayerConfiguration.BtnAbandonAllLabel = "<liferay-ui:message key="scorm.abandonall" />";
       PlayerConfiguration.BtnSuspendAllLabel = "<liferay-ui:message key="scorm.suspendall" />";

       //manifest by URL   
       Run.ManifestByURL("<%=urlIndex%>", false);
     }
     
     AUI().ready(function() {
    	 InitPlayer();
   	 });
     
  </script>

  <div id="placeholder_clicker" style="display: none"></div>
  <div style="width: 99%;">
 	<div id="placeholder_navigationContainer"></div>
 	<div id="placeholder_treeContainer"></div>
  </div>
<div id="placeholder_treecontentContainer">
	
	<div id="placeholder_contentIFrame" style="height: 670px; width: 99%;">
          <iframe id="contentIFrame" style="height:100%; width:100%"></iframe>
    </div>
</div>
<div id="placeholder_navigationContainer2"></div>

