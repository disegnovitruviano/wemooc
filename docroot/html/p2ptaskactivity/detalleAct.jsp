<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.liferay.lms.service.P2pActivityCorrectionsLocalServiceUtil"%>
<%@page import="com.liferay.lms.model.P2pActivityCorrections"%>
<%@page import="com.liferay.portlet.documentlibrary.model.DLFileEntry"%>
<%@page import="com.liferay.portlet.documentlibrary.service.DLFileEntryLocalServiceUtil"%>
<%@page import="com.liferay.lms.service.LearningActivityLocalServiceUtil"%>
<%@page import="com.liferay.lms.service.P2pActivityLocalServiceUtil"%>
<%@page import="com.liferay.lms.service.LearningActivityTryLocalServiceUtil"%>
<%@page import="com.liferay.lms.service.LearningActivityResultLocalServiceUtil"%>
<%@page import="com.liferay.lms.model.LearningActivityResult"%>
<%@page import="com.liferay.lms.model.P2pActivity"%>
<%@page import="com.liferay.lms.model.LearningActivityTry"%>
<%@page import="com.liferay.lms.model.LearningActivity"%>

<%@ include file="/init.jsp" %>

<script type="text/javascript">
	function changeDiv(id){
		if(id=="1"){
			$("#capa1").css('display','block');
		    $("#capa2").css('display','none');
		    $("#capa3").css('display','none');
		    $("#span1").addClass('selected');
		    $("#span2").removeClass('selected');
		    $("#span3").removeClass('selected');
		}else if(id=="2"){
			$("#capa1").css('display','none');
			$("#capa2").css('display','block');
			$("#capa3").css('display','none');
			$("#span1").removeClass('selected');
		    $("#span2").addClass('selected');
		    $("#span3").removeClass('selected');
		}
		else if(id=="3"){
			$("#capa1").css('display','none');
			$("#capa2").css('display','none');
			$("#capa3").css('display','block');
			$("#span1").removeClass('selected');
		    $("#span2").removeClass('selected');
		    $("#span3").addClass('selected');
		}
	}
	
</script>

<%
long actId=ParamUtil.getLong(request,"actId",0);
long userId=ParamUtil.getLong(request,"userId",0);
String criteria = ParamUtil.getString(request,"criteria","");
String cur = ParamUtil.getString(request,"cur","");

if(actId!=0)
{
	LearningActivity activity=LearningActivityLocalServiceUtil.getLearningActivity(actId);
	long typeId=activity.getTypeId();
	long latId = ParamUtil.getLong(request,"latId",0);
	if(latId==0){
		if(LearningActivityTryLocalServiceUtil.getLearningActivityTryByActUserCount(actId, userId)>0){
			List<LearningActivityTry> latList = LearningActivityTryLocalServiceUtil.
					getLearningActivityTryByActUser(actId, userId);
			if(!latList.isEmpty())
			{
				for(LearningActivityTry lat :latList){
					latId = lat.getLatId();
				}
			}
		}
	}
	User owner = UserLocalServiceUtil.getUser(userId);
	%>
	<h2><%=activity.getTitle(themeDisplay.getLocale()) %></h2>
	<p class="sub-title"><liferay-ui:message key="p2ptask-explicacion" /></p>
	<div class="description">
		<%=activity.getDescription(themeDisplay.getLocale()) %>
	</div>
	<p class="sub-title"><liferay-ui:message key="p2ptask-done-by" /> <%=owner.getFullName()%></p>
	<%
	P2pActivity myp2pActivity = P2pActivityLocalServiceUtil.findByActIdAndUserId(actId, userId);
	
	request.setAttribute("actId", actId);
	request.setAttribute("latId", latId);
	
	LearningActivityResult learnResult = 
			LearningActivityResultLocalServiceUtil.getByActIdAndUserId(actId,userId);
	if(myp2pActivity!=null){
		String classCSS2="";
		String classCSS3="";
		String passed="";
		String javascript="";
		Long showRevision = ParamUtil.getLong(request, "showRevision",0);
		if(!learnResult.getPassed()){
			classCSS2="selected";
			if(showRevision==1)
				javascript="changeDiv(3);";
			else
				javascript="changeDiv(2);";
		}
		else{
			classCSS3="selected";
			javascript="changeDiv(3);";
			passed="done";
		}
		
	%>
	<div class="steps">
		<span id="span1" onclick="changeDiv(1)" class="clicable done"><liferay-ui:message key="p2ptask-step1-resume" />&nbsp;>&nbsp;</span>
		<span id="span2" class="<%=classCSS2 %> clicable <%=passed%>" onclick="changeDiv(2)"><liferay-ui:message key="p2ptask-step2-resume" />&nbsp;>&nbsp;</span>
		<span id="span3" class="<%=classCSS3 %> clicable" onclick="changeDiv(3)"><liferay-ui:message key="p2ptask-step3-resume" /></span>
	</div>
	<div class="preg_content" id="capa1" style="display:none">
		<%
		DLFileEntry dlfile = DLFileEntryLocalServiceUtil.getDLFileEntry(myp2pActivity.getFileEntryId());
		String urlFile = themeDisplay.getPortalURL()+"/documents/"+dlfile.getGroupId()+"/"+dlfile.getUuid();
		
		%>
		<div class="container-textarea">
			<textarea rows="6" cols="90" readonly="readonly" ><%=myp2pActivity.getDescription() %></textarea>
		</div>
		<% 
			int size = Integer.parseInt(String.valueOf(dlfile.getSize()));
			int sizeKb = size/1024; //Lo paso a Kilobytes
		%>
		<div class="doc_descarga">
			<span><%=dlfile.getTitle()%>&nbsp;(<%= sizeKb%> Kb)&nbsp;</span>
			<a href="<%=urlFile%>" class="verMas" target="_blank"><liferay-ui:message key="p2ptask-donwload" /></a>
		</div>
	</div>
	<div class="preg_content" id="capa2" style="display:none">
	<%
		List<P2pActivityCorrections> p2pActList = P2pActivityCorrectionsLocalServiceUtil.findByActIdIdAndUserId(actId, userId);
		LearningActivityTry larEntry=LearningActivityTryLocalServiceUtil.getLearningActivityTry(latId);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		int cont=0;
		if(!p2pActList.isEmpty()){
			
			for (P2pActivityCorrections myP2PActiCor : p2pActList){
				P2pActivity myP2PActivity = P2pActivityLocalServiceUtil.getP2pActivity(myP2PActiCor.getP2pActivityId());
				
				if(myP2PActivity.getUserId()!=userId){
					
					cont++;
					User propietary = UserLocalServiceUtil.getUser(myP2PActivity.getUserId());
					
					String description = myP2PActiCor.getDescription();
					String textButton = "p2ptask-correction";
					String correctionDate="";
					if(myP2PActivity.getDate()!=null){
						correctionDate = dateFormat.format(myP2PActivity.getDate());
					}
					
					dlfile = DLFileEntryLocalServiceUtil.getDLFileEntry(myP2PActivity.getFileEntryId());
					urlFile = themeDisplay.getPortalURL()+"/documents/"+dlfile.getGroupId()+"/"+dlfile.getUuid(); 
					
					%>
					<div class="option-more">
						<span class="label-col"><liferay-ui:message key="p2ptask-exercise" /> <span class="name"><liferay-ui:message key="of" /> <%=propietary.getFullName() %></span><span class="number"><liferay-ui:message key="number" /> <%=cont%></span></span>
						<div class="collapsable">
							<%
							String descriptionFile = "";
							if(myP2PActivity.getDescription()!=null){
								descriptionFile = myP2PActivity.getDescription();
							}
							%>
							<div class="description"><%=descriptionFile %></div>
							<%
							size = Integer.parseInt(String.valueOf(dlfile.getSize()));
							sizeKb = size/1024; //Lo paso a Kilobytes
							%>
							<div class="doc_descarga">
								<span><%=dlfile.getTitle()%>&nbsp;(<%= sizeKb%> Kb)&nbsp;</span>
								<a href="<%=urlFile%>" class="verMas" target="_blank"><liferay-ui:message key="p2ptask-donwload" /></a>
							</div>
							<div class="degradade">
								<div class="subtitle"><liferay-ui:message key="p2ptask-valoration" /> :</div>
								<div class="container-textarea">
									<textarea rows="6" cols="80" name="description" readonly="readonly"><%=description %></textarea>
								</div>
								<%
								if(myP2PActiCor.getFileEntryId()!=0){
									DLFileEntry dlfileCor = DLFileEntryLocalServiceUtil.getDLFileEntry(myP2PActiCor.getFileEntryId());
									String urlFileCor = themeDisplay.getPortalURL()+"/documents/"+dlfileCor.getGroupId()+"/"+dlfileCor.getUuid();
									size = Integer.parseInt(String.valueOf(dlfileCor.getSize()));
									sizeKb = size/1024; //Lo paso a Kilobytes
								%>
								<div class="doc_descarga">
									<span><%=dlfileCor.getTitle()%>&nbsp;(<%= sizeKb%> Kb)&nbsp;</span>
									<a href="<%=urlFileCor%>" class="verMas" target="_blank"><liferay-ui:message key="p2ptask-donwload" /></a>
								</div>
								<%} %>
							</div>
						</div>
					</div>
					<%
				}
			}
		}
		else{
		%>
		<div style="font-size: 14px;color: #B70050;font-weight: bold;"><liferay-ui:message key="no-p2pActivites-uploaded-resume" /></div>
		<%
		}
		%>
	</div>
	<div class="preg_content" id="capa3" style="display:none">
		<%
		List<P2pActivityCorrections> p2pActCorList = P2pActivityCorrectionsLocalServiceUtil.
				findByP2pActivityId(myp2pActivity.getP2pActivityId());

		dlfile = null;
		urlFile = "";

		dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		String correctionDate = "";
		cont=0;
		if(!p2pActCorList.isEmpty()){
			for (P2pActivityCorrections myP2PActCor : p2pActCorList){
					
				cont++;
				User propietary = UserLocalServiceUtil.getUser(myP2PActCor.getUserId());
				String correctionText = myP2PActCor.getDescription();
				if(myP2PActCor.getFileEntryId()!=0)
				{
					dlfile = DLFileEntryLocalServiceUtil.getDLFileEntry(myP2PActCor.getFileEntryId());
					urlFile = themeDisplay.getPortalURL()+"/documents/"+dlfile.getGroupId()+"/"+dlfile.getUuid();
					Date date = myP2PActCor.getDate();
					correctionDate = dateFormat.format(date);
				}
				
				%>
				<div class="option-more">
				<span class="label-col"><liferay-ui:message key="p2ptask-correction-title" /> <span class="name"><liferay-ui:message key="by" /> <%=propietary.getFullName() %></span><span class="number"><liferay-ui:message key="number" /> <%=cont%></span> <%=correctionDate %></span>
					<div class="collapsable" style="padding-left:10px">
						<div class="container-textarea">
							<textarea rows="6" cols="90" readonly="readonly" ><%=correctionText %></textarea>
						</div>
						<%
						if(dlfile!=null){
							size = Integer.parseInt(String.valueOf(dlfile.getSize()));
							sizeKb = size/1024; //Lo paso a Kilobytes
						%>
						<div class="doc_descarga">
							<span><%=dlfile.getTitle()%>&nbsp;(<%= sizeKb%> Kb)&nbsp;</span>
							<a href="<%=urlFile%>" class="verMas" target="_blank"><liferay-ui:message key="p2ptask-donwload" /></a>
						</div>
						<%
						}
						%>
					</div>
				</div>
				<%
			}
		}else{
			%>
			<div style="font-size: 14px;color: #B70050;font-weight: bold;">
				<liferay-ui:message key="no-p2pActivites-corretion-resume" />
			</div>
			<%
		}%>
	</div>
	<script type="text/javascript">
	<%=javascript%>
	</script>
	<%
	}
}
%>
<portlet:renderURL var="back">
	<portlet:param name="jspPage" value="/html/p2ptaskactivity/revisions.jsp" />
	<portlet:param name="actId" value="<%=String.valueOf(actId) %>" />
	<portlet:param name="criteria" value="<%=criteria %>" />
	<portlet:param name="cur" value="<%=cur %>" />
</portlet:renderURL>
<%
String urlback = "self.location = '"+back+"';";
%>
<aui:button cssClass="floatl" value="back" type="button" onClick="<%=urlback %>" style="margin-top:10px" />
	