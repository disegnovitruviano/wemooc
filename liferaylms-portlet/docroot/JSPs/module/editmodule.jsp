<%@page import="com.tls.lms.util.LiferaylmsUtil"%>
<%@page import="com.liferay.portal.kernel.util.StringPool"%>
<%@page import="com.liferay.portal.kernel.servlet.SessionErrors"%>
<%@page import="com.liferay.portal.kernel.servlet.SessionMessages"%>
<%@page import="javax.portlet.WindowState"%>
<%@page import="com.liferay.portal.kernel.util.UnicodeFormatter"%>
<%@page import="com.liferay.util.JavaScriptUtil"%>
<%@ page import="com.liferay.lms.model.Module" %>
<%@ page import="com.liferay.lms.service.ModuleLocalServiceUtil"%>

<%@include file="../init.jsp" %>

<jsp:useBean id="editmoduleURL" class="java.lang.String" scope="request" />
<jsp:useBean id="module" type="com.liferay.lms.model.Module" scope="request"/>
<jsp:useBean id="title" class="java.lang.String" scope="request" />
<jsp:useBean id="description" class="java.lang.String" scope="request" />
<jsp:useBean id="icon" class="java.lang.String" scope="request" />
<jsp:useBean id="startDateDia" class="java.lang.String" scope="request" />
<jsp:useBean id="startDateMes" class="java.lang.String" scope="request" />
<jsp:useBean id="startDateAno" class="java.lang.String" scope="request" />
<jsp:useBean id="endDateDia" class="java.lang.String" scope="request" />
<jsp:useBean id="endDateMes" class="java.lang.String" scope="request" />
<jsp:useBean id="endDateAno" class="java.lang.String" scope="request" />

<portlet:defineObjects />
<script type="text/javascript">
<!--

AUI().ready('node-base' ,'aui-form-validator', 'aui-overlay-context-panel', function(A) {
	
	window.<portlet:namespace />validateActivity = new A.FormValidator({
		boundingBox: '#<portlet:namespace />addmodule',
		validateOnBlur: true,
		validateOnInput: true,
		selectText: true,
		showMessages: false,
		containerErrorClass: '',
		errorClass: '',
		rules: {			
			<portlet:namespace />title_<%=renderRequest.getLocale().toString()%>: {
				required: true
			},
        	<portlet:namespace />description: {
        		required: false
            }
		},
        fieldStrings: {			
        	<portlet:namespace />title_<%=renderRequest.getLocale().toString()%>: {
        		required: '<liferay-ui:message key="module-title-required" />'
            },
        	<portlet:namespace />description: {
        		required: '<liferay-ui:message key="module-description-required" />'
            }
		},
		
		on: {		
            errorField: function(event) {
            	var instance = this;
				var field = event.validator.field;
				var divError = A.one('#'+field.get('name')+'Error');
				if(divError) {
					divError.addClass('portlet-msg-error');
					divError.setContent(instance.getFieldErrorMessage(field,event.validator.errors[0]));
				}
            },		
            validField: function(event) {
				var divError = A.one('#'+event.validator.field.get('name')+'Error');
				if(divError) {
					divError.removeClass('portlet-msg-error');
					divError.setContent('');
				}
            }
		}
	});
});

//-->
</script>
<%
	long moduleId=0;

	if(module.getModuleId()!=0){
		moduleId=module.getModuleId();
	}

%>

<aui:model-context bean="module" model="<%= Module.class %>" />
<liferay-ui:success key="module-added-successfully" message="module-added-successfully" />
<liferay-ui:success key="module-updated-successfully" message="module-updated-successfully" />
<%if ((SessionMessages.contains(renderRequest, "module-added-successfully"))|| 
	  (SessionMessages.contains(renderRequest, "module-updated-successfully"))) { %>
<script type="text/javascript">
<!--
	AUI().ready(function(A) {
		if ((!!window.postMessage)&&(window.parent != window)) {
			if (!window.location.origin){
				window.location.origin = window.location.protocol+"//"+window.location.host;
			}
			
			if(AUI().UA.ie==0) {
				parent.postMessage({name:'reloadModule',moduleId:<%=Long.toString(moduleId)%>}, window.location.origin);
			}
			else {
				parent.postMessage(JSON.stringify({name:'reloadModule',moduleId:<%=Long.toString(moduleId)%>}), window.location.origin);
			}
		}
	});
//-->
</script>

<% } %>

<aui:form name="addmodule" action="<%=editmoduleURL %>" method="POST" enctype="multipart/form-data">
	<input type="hidden" name="resourcePrimKey" value="<%=module.getPrimaryKey() %>">

<%
	if(moduleId>0)
	{
%>	<aui:model-context bean="<%= module %>" model="<%= Module.class %>"/>
	<aui:input type="hidden" name="ordern" value="<%=Long.toString(module.getOrdern()) %>" />
<% 
	}
	else {
		%>	<aui:model-context model="<%= Module.class %>"/>
	<% 
	}
%>
	<aui:input name="title" label="title">
	</aui:input>
	<div id="<portlet:namespace />title_<%=renderRequest.getLocale().toString()%>Error" class="<%=(SessionErrors.contains(renderRequest, "module-title-required"))?
    														"portlet-msg-error":StringPool.BLANK %>">
    	<%=(SessionErrors.contains(renderRequest, "module-title-required"))?
    			LanguageUtil.get(pageContext,"module-title-required"):StringPool.BLANK %>
    </div>
    	    
    <script type="text/javascript">
	<!--
		Liferay.provide(
	        window,
	        '<portlet:namespace />onChangeDescription',
	        function(val) {
	        	var A = AUI();
				A.one('#<portlet:namespace />description').set('value',val);
				if(window.<portlet:namespace />validateActivity){
					window.<portlet:namespace />validateActivity.validateField('<portlet:namespace />description');
				}
	        },
	        ['node']
	    );
	
	    function <portlet:namespace />closeWindow(){
			if ((!!window.postMessage)&&(window.parent != window)) {
				if (!window.location.origin){
					window.location.origin = window.location.protocol+"//"+window.location.host;
				}
				
				if(AUI().UA.ie==0) {
					parent.postMessage({name:'closeModule',moduleId:<%=Long.toString(moduleId)%>}, window.location.origin);
				}
				else {
					parent.postMessage(JSON.stringify({name:'closeModule',moduleId:<%=Long.toString(moduleId)%>}), window.location.origin);
				}
			}
			else {
				window.location.href='<portlet:renderURL />';
			}
	    }
 
	//-->
	</script>
    
	<aui:field-wrapper label="description">
		<liferay-ui:input-editor name="description" width="100%" onChangeMethod="onChangeDescription" />
		<script type="text/javascript">
	        function <portlet:namespace />initEditor() 
	        { 
	            return "<%= UnicodeFormatter.toString(description) %>"; 
	        }
	    </script>
	</aui:field-wrapper>
	<div id="<portlet:namespace />descriptionError" class="<%=(SessionErrors.contains(renderRequest, "module-description-required"))?
    														"portlet-msg-error":StringPool.BLANK %>">
    	<%=(SessionErrors.contains(renderRequest, "module-description-required"))?
    			LanguageUtil.get(pageContext,"module-description-required"):StringPool.BLANK %>
    </div>
	<aui:input type="hidden" name="icon" />
	<br />
	 
	 <liferay-ui:error key="error-file-size" message="error-file-size" />
	<aui:field-wrapper label="icon">
		<aui:input inlineLabel="left" inlineField="true" name="fileName" label="" id="fileName" type="file" value="" />
	</aui:field-wrapper>	
	
	<liferay-ui:error key="module-icon-required" message="module-icon-required" />
	<liferay-ui:error key="error_number_format" message="error_number_format" />
	<br />

	<aui:field-wrapper label="start-date">
		<liferay-ui:input-date  yearRangeEnd="<%=LiferaylmsUtil.defaultEndYear %>" yearRangeStart="<%=LiferaylmsUtil.defaultStartYear %>"
		 dayParam="startDateDia" dayValue="<%= Integer.valueOf(startDateDia) %>"
		  monthParam="startDateMes" monthValue="<%= Integer.valueOf(startDateMes)-1 %>"
		   yearParam="startDateAno" yearValue="<%= Integer.valueOf(startDateAno) %>"  yearNullable="false" 
				 dayNullable="false" monthNullable="false" ></liferay-ui:input-date>
		  
	</aui:field-wrapper>
	<liferay-ui:error key="module-startDate-required" message="module-startDate-required" />
	<aui:field-wrapper label="end-date">
		<liferay-ui:input-date  yearRangeEnd="<%=LiferaylmsUtil.defaultEndYear %>" yearRangeStart="<%=LiferaylmsUtil.defaultStartYear %>" dayParam="endDateDia" dayValue="<%= Integer.valueOf(endDateDia) %>" monthParam="endDateMes" monthValue="<%= Integer.valueOf(endDateMes)-1 %>" yearParam="endDateAno" yearValue="<%= Integer.valueOf(endDateAno) %>"  yearNullable="false" 
				 dayNullable="false" monthNullable="false" ></liferay-ui:input-date>
	</aui:field-wrapper>
	<liferay-ui:error key="module-endDate-required" message="module-endDate-required" />
	<liferay-ui:error key="module-startDate-before-endDate" message="module-startDate-before-endDate" />
	<aui:select label="modulo-predecesor" name="precedence">
<%
	java.util.List<Module> modules=ModuleLocalServiceUtil.findAllInGroup(themeDisplay.getScopeGroupId());
%>
		<aui:option value="0" ><liferay-ui:message key="module-nothing" /></aui:option>
<%
	for(Module theModule2:modules)
	{
		boolean selected=false;
		if(moduleId!= theModule2.getModuleId())
		{
			if(moduleId>0&& theModule2.getModuleId()==module.getPrecedence())
			{
				selected=true;
			}
%>
		<aui:option value="<%=theModule2.getModuleId() %>" selected="<%=selected %>"><%=theModule2.getTitle(themeDisplay.getLocale()) %></aui:option>
<% 
		}
	}
%>
	</aui:select>       
	<aui:button-row>
		<aui:button type="submit"></aui:button>
		<aui:button onClick="<%=renderResponse.getNamespace() + \"closeWindow()\"%>" value="<%=LanguageUtil.get(pageContext,\"cancel\")%>" type="cancel" />
	</aui:button-row>
</aui:form>
