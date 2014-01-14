
<%@page import="com.liferay.lms.model.LearningActivityTry"%>
<%@page import="com.liferay.lms.service.LearningActivityTryLocalServiceUtil"%>
<%@page import="com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil"%>
<%@page import="com.liferay.lms.model.P2pActivity"%>
<%@page import="com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil"%>
<%@page import="com.liferay.lms.service.P2pActivityLocalServiceUtil"%>
<%@page import="com.liferay.portal.kernel.servlet.SessionErrors"%>
<%@page import="com.liferay.portal.kernel.portlet.LiferayWindowState"%>
<%@page import="com.liferay.lms.model.Course"%>
<%@page import="com.liferay.lms.service.CourseLocalServiceUtil"%>
<%@page import="com.liferay.lms.service.ModuleLocalServiceUtil"%>
<%@page import="com.liferay.lms.service.LearningActivityLocalServiceUtil"%>
<%@page import="com.liferay.lms.model.LearningActivity"%>
<%@ include file="/init.jsp" %>
<%
	long moduleId=ParamUtil.getLong(renderRequest,"resModuleId",0);
	long random = 0;
	long questionsPerPage = 0;
	String password = StringPool.BLANK;
	long hourDuration=0,minuteDuration=0,secondDuration=0;
	boolean showCorrectAnswer= false;
	boolean improve=false;
	boolean newOrCourseEditor=true;
	boolean disabled = false;
	
	//Permiso de editar los campos del extra content.
	boolean edit = true;
	
	if(request.getAttribute("activity")!=null) {
		LearningActivity learningActivity=(LearningActivity)request.getAttribute("activity");	
		
		if(!LearningActivityLocalServiceUtil.getExtraContentValue(learningActivity.getActId(),"random").equals("")){
			random = Long.parseLong(LearningActivityLocalServiceUtil.getExtraContentValue(learningActivity.getActId(),"random"));		
		}
		
		password = HtmlUtil.unescape(LearningActivityLocalServiceUtil.getExtraContentValue(learningActivity.getActId(),"password")).trim();
		questionsPerPage = Long.parseLong(LearningActivityLocalServiceUtil.getExtraContentValue(learningActivity.getActId(),"questionsPerPage"));
		
		long timestamp = GetterUtil.getLong(LearningActivityLocalServiceUtil.getExtraContentValue(learningActivity.getActId(),"timeStamp"));		
		hourDuration = timestamp / 3600;
		minuteDuration = (timestamp % 3600) / 60;
		secondDuration = timestamp % 60;
		
		showCorrectAnswer = StringPool.TRUE.equals(LearningActivityLocalServiceUtil.getExtraContentValue(learningActivity.getActId(),"showCorrectAnswer"));
		improve = StringPool.TRUE.equals(LearningActivityLocalServiceUtil.getExtraContentValue(learningActivity.getActId(),"improve"));	
		
		moduleId=learningActivity.getModuleId();
		Course course=CourseLocalServiceUtil.fetchByGroupCreatedId(themeDisplay.getScopeGroupId());
		newOrCourseEditor=permissionChecker.hasPermission(course.getGroupId(), Course.class.getName(),course.getCourseId(),"COURSEEDITOR");
		
		//NUEVOS PERMISOS
		//Permisos por el rol del usuario
		boolean isUserAdmin = themeDisplay.getPermissionChecker().isOmniadmin();
		boolean canUpdate = permissionChecker.hasPermission(learningActivity.getGroupId(), LearningActivity.class.getName(),learningActivity.getActId(),"UPDATE_ACTIVE");
		
		boolean userPermission = isUserAdmin || canUpdate;
		
		//Permisos por el estado de la actividad
		boolean courseStarted = ModuleLocalServiceUtil.getModule(moduleId).getStartDate().before(new Date());
		boolean activityDone = false;
		if(learningActivity != null){
			activityDone = LearningActivityTryLocalServiceUtil.dynamicQueryCount(DynamicQueryFactoryUtil.forClass(LearningActivityTry.class).add(PropertyFactoryUtil.forName("actId").eq(learningActivity.getActId()))) != 0;
		}
		
		boolean actStarted = courseStarted || activityDone;
		
		//Permiso para editar los campos de la actividad
		edit = userPermission || !actStarted;
	}

%>

<script type="text/javascript">
<!--

AUI().use('aui-form-validator', function(A) {
	A.mix(
			YUI.AUI.defaults.FormValidator.REGEX,
			{
				positiveLong: /^[0-9]*$/			
			},
			true
		);
	
	var oldRequired=YUI.AUI.defaults.FormValidator.RULES.required;
	
	A.mix(
		YUI.AUI.defaults.FormValidator.RULES,
		{
			required: function(val, fieldNode, ruleValue) {
				switch (fieldNode.get('name')) {
			    case "<portlet:namespace />random":
					return true;		        
			    default:
			    	return oldRequired(val, fieldNode, ruleValue);
				}				
			},
			randomRule: function(val, fieldNode, ruleValue) {
				if(!A.Node.getDOMNode(fieldNode).form.<portlet:namespace />random.disabled){
					return YUI.AUI.defaults.FormValidator.REGEX.positiveLong.test(val);
				}
				return true;
			},
			qppRule: function(val, fieldNode, ruleValue) {
				if(!A.Node.getDOMNode(fieldNode).form.<portlet:namespace />questionsPerPage.disabled){
					return YUI.AUI.defaults.FormValidator.REGEX.positiveLong.test(val);
				}
				return true;
			}
		},
		true
	);
	
});

window.<portlet:namespace />validate_execactivity={
		rules:
		{
			<portlet:namespace />random: {
				required: true,
				randomRule: true
			},
			<portlet:namespace />questionsPerPage: {
				qppRule: true
			}
		},
		fieldStrings:
		{
			<portlet:namespace />random: {
				randomRule: '<liferay-ui:message key="execActivity.options.error.random" />'
            },
            <portlet:namespace />questionsPerPage: {
            	qppRule: '<liferay-ui:message key="execActivity.options.error.questionsPerPage" />'
			}
		}	
};

<% if(!newOrCourseEditor){ %>
	AUI().ready('node','event','aui-io-request','aui-parse-content','liferay-portlet-url', function(A) {

		A.one('#<portlet:namespace />resModuleId').on('change', function (e) {
			var renderUrl = Liferay.PortletURL.createRenderURL();							
				renderUrl.setWindowState('<%=LiferayWindowState.EXCLUSIVE.toString()%>');
				renderUrl.setPortletId('<%=portletDisplay.getId()%>');
				renderUrl.setParameter('jspPage','/html/execactivity/test/admin/editModule.jsp');
				renderUrl.setParameter('resModuleId',e.target.getDOM().options[e.target.getDOM().selectedIndex].value);
			A.io.request(renderUrl.toString(), {
			   dataType: 'json',
		       on: {  
		      		success: function() {  
		      			var notEditable = !!this.get('responseData')['notEditable'];
		      			A.one('#<portlet:namespace />random').set('disabled',notEditable);		
		      			A.one('#<portlet:namespace />password').set('disabled',notEditable);
						A.one('#<portlet:namespace />fm * select[name=<portlet:namespace />hourDuration]').set('disabled',notEditable);
						A.one('#<portlet:namespace />fm * select[name=<portlet:namespace />minuteDuration]').set('disabled',notEditable);
						A.one('#<portlet:namespace />fm * select[name=<portlet:namespace />secondDuration]').set('disabled',notEditable);
						A.one('#<portlet:namespace />showCorrectAnswerCheckbox').set('disabled',notEditable);
						A.one('#<portlet:namespace />improveCheckbox').set('disabled',notEditable);
	
		      			if(notEditable) {
			      			A.one('#<portlet:namespace />random').set('value','<%=(random>0)?Long.toString(random):StringPool.BLANK%>');		
			      			A.one('#<portlet:namespace />password').set('value','<%=password%>');
							A.one('#<portlet:namespace />fm * select[name=<portlet:namespace />hourDuration]').set('value',<%=Long.toString(hourDuration)%>);
							A.one('#<portlet:namespace />fm * select[name=<portlet:namespace />minuteDuration]').set('value',<%=Long.toString(minuteDuration)%>);
							A.one('#<portlet:namespace />fm * select[name=<portlet:namespace />secondDuration]').set('value',<%=Long.toString(secondDuration)%>);
							A.one('#<portlet:namespace />showCorrectAnswer').set('value','<%=Boolean.toString(showCorrectAnswer)%>');
							A.one('#<portlet:namespace />showCorrectAnswerCheckbox').set('checked',<%=Boolean.toString(showCorrectAnswer)%>);
							A.one('#<portlet:namespace />improve').set('value','<%=Boolean.toString(improve)%>');
							A.one('#<portlet:namespace />improveCheckbox').set('checked',<%=Boolean.toString(improve)%>);
			      		}
			        } 
			   } 
			}); 
		});
	
	});
<% } %>

//-->
</script>


	<aui:input type="text" size="3" name="random" label="execActivity.options.random" value="<%=(random>0)?Long.toString(random):StringPool.BLANK %>" disabled="<%=!edit %>" 
		ignoreRequestValue="true" helpMessage="execActivity.options.random.helpMessage"></aui:input>
	<div id="<portlet:namespace />randomError" class="<%=(SessionErrors.contains(renderRequest, "execActivity.options.error.random"))?"portlet-msg-error":StringPool.BLANK %>">
	 	<%=(SessionErrors.contains(renderRequest, "execActivity.options.error.random"))?
				LanguageUtil.get(pageContext,"execActivity.options.error.random"):StringPool.BLANK %>
	</div>
	
	<aui:input type="text" size="3" name="questionsPerPage" label="execActivity.options.questionsPerPage" value="<%=(questionsPerPage>0)?Long.toString(questionsPerPage):StringPool.BLANK %>" disabled="<%=!edit %>" ignoreRequestValue="true"
	helpMessage="execActivity.options.questionsPerPage.helpMessage"></aui:input>
	<div id="<portlet:namespace />questionsPerPageError" class="<%=(SessionErrors.contains(renderRequest, "execActivity.options.error.questionsPerPage"))?"portlet-msg-error":StringPool.BLANK %>">
	 	<%=(SessionErrors.contains(renderRequest, "execActivity.options.error.questionsPerPage"))?
				LanguageUtil.get(pageContext,"execActivity.options.error.questionsPerPage"):StringPool.BLANK %>
	</div>
	
	<aui:input type="text" name="password" label="execActivity.options.password" value='<%=password %>' ignoreRequestValue="true" 
	helpMessage="<%=LanguageUtil.get(pageContext,\"execActivity.options.password.help\")%>" disabled="<%=!edit %>"></aui:input>
 
 	<aui:field-wrapper label="execActivity.options.timestamp" helpMessage="execActivity.options.timestamp.helpMessage">
	<%
	NumberFormat timeNumberFormat = NumberFormat.getInstance(locale);
	timeNumberFormat.setMinimumIntegerDigits(2);
	%>
		<select name="<portlet:namespace />hourDuration" <% if(!edit){ %> disabled="disabled" <% } %> >
			<%
			for (int i = 0; i < 24; i++) {
			%>
					<option <%= (hourDuration == i) ? "selected" : StringPool.BLANK %> value="<%= i %>"><%= timeNumberFormat.format(i) %></option>
			<%
			}
			%>
		</select>
		<select name="<portlet:namespace />minuteDuration" <% if(!edit){ %> disabled="disabled" <% } %> >
			<%
			for (int i = 0; i < 60; i++) {
			%>
					<option <%= (minuteDuration == i) ? "selected" : StringPool.BLANK %> value="<%= i %>">:<%= timeNumberFormat.format(i) %></option>
			<%
			}
			%>
		</select>	
		<select name="<portlet:namespace />secondDuration" <% if(!edit){ %> disabled="disabled" <% } %> > 
			<%
			for (int i = 0; i < 60; i++) {
			%>
					<option <%= (secondDuration == i) ? "selected" : StringPool.BLANK %> value="<%= i %>">:<%= timeNumberFormat.format(i) %></option>
			<%
			}
			%>
		</select>			
	</aui:field-wrapper>
	
	<aui:input type="checkbox" name="showCorrectAnswer" label="exectactivity.edit.showcorrect" checked="<%=showCorrectAnswer %>" disabled="<%=!edit %>" 
		ignoreRequestValue="true" helpMessage="exectactivity.edit.showcorrect.helpMessage"></aui:input>
		
	<aui:input type="checkbox" name="improve" label="exectactivity.edit.improve" checked="<%=improve %>" disabled="<%=!edit %>" 
		ignoreRequestValue="true" helpMessage="exectactivity.edit.improve.helpMessage"></aui:input>

