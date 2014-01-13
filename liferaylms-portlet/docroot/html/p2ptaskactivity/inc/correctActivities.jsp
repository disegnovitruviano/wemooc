<%@page import="com.liferay.portal.kernel.servlet.BrowserSnifferUtil"%>
<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="com.liferay.portal.kernel.util.Constants"%>
<%@page import="java.util.Collection"%>
<%@page import="com.liferay.lms.service.impl.LearningActivityLocalServiceImpl"%>
<%@page import="com.liferay.lms.model.Module"%>
<%@page import="com.liferay.lms.service.ModuleLocalServiceUtil"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.liferay.lms.model.LearningActivityTry"%>
<%@page import="com.liferay.lms.model.LearningActivityResult"%>
<%@page import="com.liferay.lms.service.LearningActivityResultLocalServiceUtil"%>
<%@page import="com.liferay.lms.model.P2pActivityCorrections"%>
<%@page import="com.liferay.lms.model.P2pActivity"%>
<%@page import="com.liferay.lms.service.P2pActivityLocalServiceUtil"%>
<%@page import="com.liferay.lms.service.P2pActivityCorrectionsLocalServiceUtil"%>
<%@page import="com.liferay.lms.model.LearningActivity"%>
<%@page import="com.liferay.lms.service.LearningActivityLocalServiceUtil"%>
<%@page import="com.liferay.portlet.documentlibrary.model.DLFileEntry"%>
<%@page import="com.liferay.portlet.documentlibrary.service.DLFileEntryLocalServiceUtil"%>
<%@page import="com.liferay.lms.service.LearningActivityTryLocalServiceUtil"%>
<%@page import="com.liferay.lms.portlet.p2p.P2PActivityPortlet"%>

<%@include file="/init.jsp" %>

<liferay-ui:error key="p2ptask-no-empty-answer" message="p2ptask-no-empty-answer" />
<liferay-ui:error key="error-p2ptask-correction" message="error-p2ptask-correction" />
<liferay-ui:error key="p2ptaskactivity-error-file-type" message="p2ptaskactivity.error.file.type" />

<%
String textoCorrecion = LanguageUtil.get(pageContext,"p2ptask-text-corrections");
String correctionsCompleted = ParamUtil.getString(request, "correctionsCompleted", "false");
String correctionsSaved = ParamUtil.getString(request, "correctionSaved", "false");


long actId=ParamUtil.getLong(request,"actId",0);
long latId=ParamUtil.getLong(request,"latId",0);
long resultuser=ParamUtil.getLong(request,"resultuser",0);


LearningActivity activity = LearningActivityLocalServiceUtil.getLearningActivity(actId);
String numCorrecciones = "3";
if(!activity.getExtracontent().equals("")){
	//numCorrecciones=activity.getExtracontent();
	numCorrecciones = LearningActivityLocalServiceUtil.getExtraContentValue(actId,"validaciones");
}
	
boolean anonimous = false;
String anonimousString = LearningActivityLocalServiceUtil.getExtraContentValue(actId,"anonimous");

if(anonimousString.equals("true")){
	anonimous = true;
}


boolean result = false;
String resultString = LearningActivityLocalServiceUtil.getExtraContentValue(actId,"result");

if(resultString.equals("true")){
	result = true;
}
%>

<script type="text/javascript">
	var noFile ='<liferay-ui:message key="p2ptaskactivity.inc.nofileselected" />';
	
	Liferay.provide(
	        window,
	        '<portlet:namespace />checkDataformC',
	        function (thisForm) {
				var A = AUI();
				
				var selector = 'form[name="'+thisForm+'"]';
				var descriptionVal = A.one(selector).one('textarea[name="<portlet:namespace />description"]').val();
				if (descriptionVal == "" || descriptionVal == "<%= StringEscapeUtils.unescapeHtml(textoCorrecion) %>") {
					alert('<liferay-ui:message key="p2ptask-no-empty-answer" />');
				}
				else {
					<portlet:namespace />openPopUp(thisForm);
				}
	        },
	        ['node']
	);
	
	Liferay.provide(
	        window,
	        '<portlet:namespace />clearText',
	        function (id) {
				var A = AUI();
				
				var idtext = "#"+id;
				
				var textReplace = "<%= StringEscapeUtils.unescapeHtml(textoCorrecion)  %>";
				if (A.one(idtext).val() == textReplace) {
					A.one(idtext).val("");
				}
	        },
	        ['node']
	);
	
	Liferay.provide(
	        window,
	        '<portlet:namespace />openPopUp',
	        function (formName) {
				var A = AUI();
				
				var selector = 'form[name="'+formName+'"]';
				var fileName = A.one(selector).one('input[name="<portlet:namespace />fileName"]').val();
				var textDesc = A.one(selector).one('textarea[name="<portlet:namespace />description"]').val();
				var textResult = ''; 
					
				if(	A.one('select[name="<portlet:namespace />resultuser"]') != null){
					textResult = A.one('select[name="<portlet:namespace />resultuser"]').val();
				}

				if (fileName != "") {
					var pos = fileName.lastIndexOf("\\");
					if (pos > 0) {
						fileName = fileName.substring(pos + 1);
					}
				} else {
					fileName = noFile;
				}
				
				//Start opening popUp
				if(A.one('#<portlet:namespace />p2pconfrmCorrec')) {
					window.<portlet:namespace />p2pconfrmCorrecTitle = A.one('#<portlet:namespace />p2pconfrmCorrec h1').html();
					window.<portlet:namespace />p2pconfrmCorrecBody = A.one('#<portlet:namespace />p2pconfrmCorrec').html();
					A.one('#<portlet:namespace />p2pconfrmCorrec').remove();
				}
				
				window.<portlet:namespace />p2pconfrmCorrec = new A.Dialog({
					id:'<portlet:namespace />showp2pconfrmCorrec',
					title: window.<portlet:namespace />p2pconfrmCorrecTitle,
		            bodyContent: window.<portlet:namespace />p2pconfrmCorrecBody,
		            centered: true,
		            modal: true,
		            width: "auto",
		            height: "auto"
		        }).render();
		        
				A.one("#contentFileCorrec").html(fileName);
				A.one("#contentDescriptionCorrec").html(textDesc);
				if(textResult != ''){
					A.one("#contentResult").html(textResult);
				}
				A.one("#submitCorrec").on('click', function(){<portlet:namespace />commitForm(formName);});
				
				window.<portlet:namespace />p2pconfrmCorrec.show();
	        },
	        ['node','aui-dialog']
	);
	
	Liferay.provide(
	        window,
	        '<portlet:namespace />commitForm',
	        function (formName) {
	        	var A = AUI();
				var selector = 'form[name="'+formName+'"]';
				A.one("#submitCorrec").attr("disabled", "disabled");
				A.one(selector).submit();
	        },
	        ['node']
	);
	
	Liferay.provide(
	        window,
	        '<portlet:namespace />closeModal',
	        function () {
	        	var A = AUI();
	        	A.DialogManager.closeByChild('#<portlet:namespace />showp2pSaved');
				window.setTimeout(function() {<portlet:namespace />openCompleted();}, 300);
	        },
	        ['aui-dialog']
	);
	
	Liferay.provide(
	        window,
	        '<portlet:namespace />openSaved',
	        function () {
	        	var A = AUI();
	        	
				if(A.one('#<portlet:namespace />p2pSaved')){
					window.<portlet:namespace />p2pSavedTitle = A.one('#<portlet:namespace />p2pSaved h1').html();
					window.<portlet:namespace />p2pSavedBody = A.one('#<portlet:namespace />p2pSaved').html();
					A.one('#<portlet:namespace />p2pSaved').remove();
				}
				
				window.<portlet:namespace />p2pSaved = new A.Dialog({
					id:'<portlet:namespace />showp2pSaved',
		            title: window.<portlet:namespace />p2pSavedTitle,
		            bodyContent: window.<portlet:namespace />p2pSavedBody,
		            centered: true,
		            modal: true,
		            width: "auto",
		            height: "auto"
		        }).render().show();
	        },
	        ['aui-dialog']
	);
	
	Liferay.provide(
	        window,
	        '<portlet:namespace />openCompleted',
	        function () {
	        	var A = AUI();
	        	
	        	if(A.one('#<portlet:namespace />p2pCorrectionCompleted')){
					window.<portlet:namespace />p2pCorrectionCompletedTitle = A.one('#<portlet:namespace />p2pCorrectionCompleted h1').html();
					window.<portlet:namespace />p2pCorrectionCompletedBody = A.one('#<portlet:namespace />p2pCorrectionCompleted').html();
					A.one('#<portlet:namespace />p2pCorrectionCompleted').remove();
				}
				
				window.<portlet:namespace />p2pCorrectionCompleted = new A.Dialog({
					id:'<portlet:namespace />showp2pCorrectionCompleted',
		            title: window.<portlet:namespace />p2pCorrectionCompletedTitle,
		            bodyContent: window.<portlet:namespace />p2pCorrectionCompletedBody,
		            centered: true,
		            modal: true,
		            width: "auto",
		            height: "auto"
		        }).render().show();				
	        },
	        ['aui-dialog']
	);
	
	Liferay.provide(
	        window,
	        '<portlet:namespace />closeConfirmationCorrection',
	        function() {
				var A = AUI();
				
				A.DialogManager.closeByChild('#<portlet:namespace />showp2pconfrmCorrec');
	        },
	        ['aui-dialog']
	    );
	
	Liferay.provide(
	        window,
	        '<portlet:namespace />closeFormP2pSaved',
	        function() {
				var A = AUI();
				A.DialogManager.closeByChild('#<portlet:namespace />showp2pSaved');
	        },
	        ['aui-dialog']
	    );
	
	Liferay.provide(
	        window,
	        '<portlet:namespace />closeFormsP2pCorrectionCompleted',
	        function() {
				var A = AUI();
				A.DialogManager.closeByChild('#<portlet:namespace />showp2pCorrectionCompleted');
	        },
	        ['aui-dialog']
	    );
	
</script>

<!-- Start PopUp confirmation -->

<div id="<portlet:namespace />p2pconfrmCorrec" style="display:none">
	<h1><liferay-ui:message key="p2ptask-uploadcorrect-confirmation" /></h1>
	<div class="desc color_tercero"><liferay-ui:message key="p2ptask-uploadcorrect-description" /></div>
	<br />
	<div class="contDesc description">
		<p><span class="label"><liferay-ui:message key="p2ptask-description-task" />: </span><span id="contentDescriptionCorrec"></span></p>
		<p><span class="label"><liferay-ui:message key="p2ptask-file-name" />: </span> <span id="contentFileCorrec"></span></p>
		<c:if test="<%=result %>">
			<p><span class="label"><liferay-ui:message key="p2ptask-file-result" />: </span> <span id="contentResult"></span></p>
		</c:if>
		<p class="message"><liferay-ui:message key="p2ptask-uploadcorrect-task-message" /></p>
	</div>
	<div class="buttons">
		<input type="button" class="button simplemodal-close" onclick="<portlet:namespace />closeConfirmationCorrection()" value="<liferay-ui:message key="cancel" />" />
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		<input type="button" class="button" id="submitCorrec" value="<liferay-ui:message key="p2ptask-correction" />" />
	</div>
</div>
<!-- End PopUp confrimation -->


<div id="<portlet:namespace />p2pCorrectionCompleted" style="display:none">
	<h1><liferay-ui:message key="p2ptaskactivity.inc.p2pcorrectioncompleted.title" /></h1>
	<div class="desc color_tercero"><liferay-ui:message key="p2ptaskactivity.inc.p2pcorrectioncompleted.subtitle" /></div>
	<br />
	<div class="contDesc bg-icon-check">
		<p><liferay-ui:message key="p2ptaskactivity.inc.p2pcorrectioncompleted.message" /></p>
	</div>
	<div class="buttons">
		<input type="button" class="button simplemodal-close" id="buttonClose" value="<liferay-ui:message key="close" />" onclick="<portlet:namespace />closeFormsP2pCorrectionCompleted()" />
	</div>
</div>

<div id="<portlet:namespace />p2pSaved" style="display:none">
	<h1><liferay-ui:message key="p2ptaskactivity.inc.p2puploadedcompleted.title" /></h1>
	<div class="desc color_tercero"><liferay-ui:message key="p2ptaskactivity.inc.p2puploadedcompleted.subtitle" /></div>
	<br />
	<div class="contDesc bg-icon-ok">
		<p><liferay-ui:message key="p2ptaskactivity.inc.p2puploadedcompleted.message" /></p>
	</div>
	<div class="buttons">
	<%if(correctionsCompleted.equals("true")){ %>
		<input type="button" class="button " id="<portlet:namespace />completed" value="<liferay-ui:message key="close" />" onclick=" <portlet:namespace />closeModal();" />
	<%}else{ %>
		<input type="button" class="button simplemodal-close" id="buttonClose" value="<liferay-ui:message key="close" />" onclick="<portlet:namespace />closeFormP2pSaved()" />
	<%} %>
	</div>
</div>


<%

if(latId==0){
	if(LearningActivityTryLocalServiceUtil.getLearningActivityTryByActUserCount(actId, themeDisplay.getUserId())>0){
		List<LearningActivityTry> latList = LearningActivityTryLocalServiceUtil.
				getLearningActivityTryByActUser(actId, themeDisplay.getUserId());
		if(!latList.isEmpty())
		{
			for(LearningActivityTry lat :latList){
				latId = lat.getLatId();
			}
		}
	}
}


int numCorrecl = Integer.parseInt(numCorrecciones);

long userId = themeDisplay.getUserId();
int cont = 0;
List<Long> listIdActivY = new ArrayList<Long>(); 

boolean allCorrected = true;

//Obtenemos todas las correcciones que tiene asignado el usuario.
//List<P2pActivityCorrections> p2pActList = P2pActivityCorrectionsLocalServiceUtil.findByActIdIdAndUserId(actId, userId);
List<P2pActivityCorrections> p2pActList = P2pActivityCorrectionsLocalServiceUtil.findByActIdAndUserIdOrderById(actId, userId);

int contaValidations = 0;

LearningActivityTry larEntry=LearningActivityTryLocalServiceUtil.getLearningActivityTry(latId);

SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
dateFormat.setTimeZone(timeZone);

List<P2pActivityCorrections> listDone = P2pActivityCorrectionsLocalServiceUtil.getCorrectionsDoneByUserInP2PActivity(actId, userId);

String []arg = {String.valueOf(numCorrecl-listDone.size()) , String.valueOf(numCorrecl)};

%>
	<div class="numbervalidations color_tercero font_13">
		<p>
			<c:if test="<%=numCorrecl-listDone.size() > 0 %>">
				<liferay-ui:message key="p2ptaskactivity.inc.numbervalidations" arguments="<%=arg %>" /> 
			</c:if>
			<c:if test="<%=numCorrecl-listDone.size() == 0 %>">
				<liferay-ui:message key="p2ptaskactivity.inc.validationscompleted" /> 
			</c:if>
		</p>
	</div>

<%
if(!p2pActList.isEmpty()){
	
	for (P2pActivityCorrections myP2PActiCor : p2pActList){
		
		if(contaValidations >= numCorrecl){
			break;
		}
		
		P2pActivity myP2PActivity = P2pActivityLocalServiceUtil.getP2pActivity(myP2PActiCor.getP2pActivityId());
		
		//Si no estamos en el usuario actual.
		if(myP2PActivity.getUserId()!=userId){
			
			//Si la fecha de correcci�n es null, es que no se ha corregido la tarea.
			if(myP2PActiCor.getDate() == null)
			{
				User propietary = UserLocalServiceUtil.getUser(myP2PActivity.getUserId());
				
				DLFileEntry dlfile = null;
				String urlFile = "";
				int size = 0;
				int sizeKb = 0;
				String title = "";
				String descriptionFile = "";
				
				if(myP2PActivity.getFileEntryId() != 0)
				{
				
					try{
						dlfile = DLFileEntryLocalServiceUtil.getDLFileEntry(myP2PActivity.getFileEntryId());
						urlFile = themeDisplay.getPortalURL()+"/documents/"+dlfile.getGroupId()+"/"+dlfile.getUuid();
						size = Integer.parseInt(String.valueOf(dlfile.getSize()));
						sizeKb = size/1024; //Lo paso a Kilobytes
						title = dlfile.getTitle();
						descriptionFile = dlfile.getDescription();
						
					}catch(Exception e){
						e.printStackTrace();
					}
					
				}
				
				if(descriptionFile.equals("") && myP2PActivity.getDescription()!=null && !myP2PActivity.getDescription().equals("")){
					descriptionFile = myP2PActivity.getDescription();
				}
				
				cont++;

				allCorrected = false;
				%>
				<div class="option-more">
					
					<span class="label-col"><liferay-ui:message key="p2ptask-exercise" /> 
						<c:if test="<%=!anonimous %>">
							<span class="name">
								<liferay-ui:message key="of" /> 
								<%=propietary.getFullName() %>
							</span>
						</c:if>
						<c:if test="<%=anonimous %>">
						<span class="number">
							<liferay-ui:message key="number" /> 
							<%=cont%>
						</span>
						</c:if>
					</span>
					<div class="collapsable">
						<form enctype="multipart/form-data" method="post" action="<portlet:actionURL name="saveCorrection"></portlet:actionURL>" name="<portlet:namespace />f1_<%=cont%>" id="<portlet:namespace />f1_<%=cont%>">
							<input type="hidden" name="actId" value="<%=actId%>"  />
							<input type="hidden" name="latId" value="<%=latId%>"  />
							<input type="hidden" name="p2pActivityCorrectionId" value="0"  />
							<input type="hidden" name="p2pActivityId" value="<%=myP2PActivity.getP2pActivityId()%>"  />
							<input type="hidden" name="userId" value="<%=userId%>"  />
							
							<div class="description">
								<%=HtmlUtil.escape(descriptionFile).replaceAll("(\r\n|\n)", "<br />") %>
							</div>
	
							<c:if test="<%=myP2PActivity.getFileEntryId() != 0 %>">
								<div class="doc_descarga">
									<span><%=title%>&nbsp;(<%= sizeKb%> Kb)&nbsp;</span>
									<a href="<%=urlFile%>" class="verMas" target="_blank"><liferay-ui:message key="p2ptask-donwload" /></a>
								</div>
							</c:if>
							
							<div class="container-textarea">
								<textarea rows="6" cols="90" name="<portlet:namespace />description" onfocus="javascript:<portlet:namespace />clearText('<portlet:namespace />description_<%=cont%>')" id="<portlet:namespace />description_<%=cont%>"><%=textoCorrecion %></textarea>
							</div>
							<liferay-ui:error key="p2ptaskactivity-error-file-size" message="p2ptaskactivity.error.file.size" />
							<div class="container-file">
								<aui:input inlineLabel="left" inlineField="true"
										  	name="fileName" id="fileName" type="file" value="" />
							</div>
							<c:if test="<%=result %>">
								<div class="container-result color_tercero font_14">
									<liferay-ui:message key="p2ptask.correction.selected.result" />
									<aui:select name="resultuser"  id="resultuser" label="">
								    	<%for(int i=100; i>=0; i=i-10){%>
								        	<option value="<%=i%>"><%=i %></option>
								        <%}%>
								    </aui:select>
								</div>
							</c:if>
							<div>
								<input type="button" class="button floatr" value="<liferay-ui:message key="p2ptask-correction" />" onclick="<portlet:namespace />checkDataformC('<portlet:namespace />f1_<%=cont%>')" />
							</div>
						</form>
					</div>
				</div>
			<%
			} 
			//Si ya ha corregido la tarea, solo debemos mostrar lo que se ha introducido.
			else
			{
				listIdActivY.add(myP2PActivity.getPrimaryKey());
				User propietary = UserLocalServiceUtil.getUser(myP2PActivity.getUserId());
				
				cont++;
				
				String description = myP2PActiCor.getDescription();
				String textButton = "p2ptask-correction";
	
				DLFileEntry dlfile = null;
				String urlFile = "";
				int size = 0;
				int sizeKb = 0;
				String title = "";
				String descriptionFile = "";
				
				if(myP2PActivity.getFileEntryId() != 0)
				{
				
					try{
						dlfile = DLFileEntryLocalServiceUtil.getDLFileEntry(myP2PActivity.getFileEntryId());
						urlFile = themeDisplay.getPortalURL()+"/documents/"+dlfile.getGroupId()+"/"+dlfile.getUuid(); 
						size = Integer.parseInt(String.valueOf(dlfile.getSize()));
						sizeKb = size/1024; //Lo paso a Kilobytes
						title = dlfile.getTitle();
					}catch(Exception e){
						e.printStackTrace();
					}
					
					if(myP2PActivity.getDescription() != null){
						descriptionFile = myP2PActivity.getDescription();
					}
				}
	
				%>
	
				<div class="option-more">
					<span class="label-col">
						<liferay-ui:message key="p2ptask-exercise" />
						<c:if test="<%=!anonimous %>">
							<span class="name">
								<liferay-ui:message key="of" /> 
								<%=propietary.getFullName() %>
							</span>
						</c:if>
						<span class="number">
							<liferay-ui:message key="number" /> 
							<%=cont%>
						</span>
					</span>
					<div class="collapsable">

						<div class="description">
							<%=HtmlUtil.escape(descriptionFile).replaceAll("(\r\n|\n)", "<br />") %>
						</div>

						<c:if test="<%=myP2PActivity.getFileEntryId() != 0 %>">
							<div class="doc_descarga">
								<span><%=title%>&nbsp;(<%= sizeKb%> Kb)&nbsp;</span>
								<a href="<%=urlFile%>" class="verMas" target="_blank"><liferay-ui:message key="p2ptask-donwload" /></a>
							</div>
						</c:if>
						<div class="degradade">
							<div class="subtitle"><liferay-ui:message key="p2ptask-your-valoration" /> :</div>
							<div class="container-textarea">
								<textarea rows="6" cols="80" name="<portlet:namespace />description" readonly="readonly"><%=description %></textarea>
							</div>
							<%

							if(myP2PActiCor.getFileEntryId()!=0){
								
								DLFileEntry dlfileCor = null;
								try{
									dlfileCor = DLFileEntryLocalServiceUtil.getDLFileEntry(myP2PActiCor.getFileEntryId());
								}catch(Exception e){
									//e.printStackTrace();
								}
								
								if(dlfileCor != null){
									String urlFileCor = themeDisplay.getPortalURL()+"/documents/"+dlfileCor.getGroupId()+"/"+dlfileCor.getUuid();
									size = Integer.parseInt(String.valueOf(dlfileCor.getSize()));
									sizeKb = size/1024; //Lo paso a Kilobytes
								%>
								<div class="doc_descarga">
									<span><%=dlfileCor.getTitle()%>&nbsp;(<%= sizeKb%> Kb)&nbsp;</span>
									<a href="<%=urlFileCor%>" class="verMas" target="_blank"><liferay-ui:message key="p2ptask-donwload" /></a>
								</div>
							<%	}
							}%>
						</div>
					<c:if test="<%=result %>">
							<div class="color_tercero font_13"><liferay-ui:message key="p2ptask.correction.activity" />: <%=myP2PActiCor.getResult() %></div>
							</c:if>
					</div>
				</div>
				<%
			}
		}
		//Mostrar s�lo el numero de correcciones que debe corregir.
		contaValidations++;
	}
}



if(p2pActList.isEmpty()){
	Group grupo=themeDisplay.getScopeGroup();
	long retoplid=themeDisplay.getPlid();
	for(Layout theLayout:LayoutLocalServiceUtil.getLayouts(grupo.getGroupId(),false))
	{

		if(theLayout.getFriendlyURL().equals("/reto"))
		{
			retoplid=theLayout.getPlid();
		}
	}
	LearningActivity  act = LearningActivityLocalServiceUtil.getLearningActivity(actId);
	Module myModule = ModuleLocalServiceUtil.getModule(act.getModuleId());
	
	%>
	<liferay-portlet:renderURL plid="<%=retoplid %>" portletName="lmsactivitieslist_WAR_liferaylmsportlet" var="gotoModuleURL">
	<liferay-portlet:param name="moduleId" value="<%=Long.toString(myModule.getModuleId()) %>"></liferay-portlet:param>
	</liferay-portlet:renderURL>
	
	<div class="no-p2pActivites-uploaded"><liferay-ui:message key="no-p2pActivites-uploaded" /></div>
	<%
}%>

<%
if(correctionsSaved.equals("true")){
	correctionsSaved="false";
	request.setAttribute("correctionSaved", correctionsSaved);
	%><script><portlet:namespace />openSaved();</script><%
}

%>