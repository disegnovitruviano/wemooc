<%@page import="com.sun.jmx.snmp.Timestamp"%>
<%@page import="com.liferay.lms.service.impl.LearningActivityLocalServiceImpl"%>
<%@page	import="com.liferay.portlet.asset.AssetRendererFactoryRegistryUtil"%>
<%@page import="com.liferay.portlet.asset.model.AssetRenderer"%>
<%@page import="com.liferay.portlet.asset.model.AssetRendererFactory"%>
<%@page	import="com.liferay.portlet.asset.service.AssetEntryLocalServiceUtil"%>
<%@page import="com.liferay.portlet.asset.model.AssetEntry"%>
<%@page import="com.liferay.lms.model.LearningActivity"%>
<%@page import="com.liferay.portal.kernel.util.ListUtil"%>
<%@page	import="com.liferay.lms.service.LearningActivityLocalServiceUtil"%>
<%@page	import="com.liferay.lms.service.LearningActivityTryLocalServiceUtil"%>
<%@page	import="com.liferay.portal.kernel.dao.orm.Criterion"%>
<%@page	import="com.liferay.portal.kernel.dao.orm.DynamicQuery"%>
<%@page	import="com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil"%>
<%@page	import="com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil"%>
<%@page import="com.liferay.lms.model.LearningActivityResult"%>
<%@page import="com.liferay.portal.kernel.util.HtmlUtil"%>
<%@ include file="/init.jsp" %>
<liferay-ui:header title="Opciones"></liferay-ui:header>
<%

	LearningActivity learnact=null;
	if(request.getAttribute("activity")!=null)
	{
		
	 learnact=(LearningActivity)request.getAttribute("activity");
	}
	else
	{
		learnact=LearningActivityLocalServiceUtil.getLearningActivity(ParamUtil.getLong(request,"actId"));
	}

	long random = 0;
	String password = StringPool.BLANK;
	long hourDurationString=0,minuteDurationString=0,secondDurationString=0;

	boolean textoenr = false;
	String textoenrString = StringPool.BLANK;	
	
	if (learnact.getExtracontent() != null && !StringPool.BLANK.equals(learnact.getExtracontent().trim())) {
		random = GetterUtil.getLong(LearningActivityLocalServiceUtil.getExtraContentValue(learnact.getActId(),"random"));
		password = HtmlUtil.unescape(LearningActivityLocalServiceUtil.getExtraContentValue(learnact.getActId(),"password")).trim();
		long timestamp = GetterUtil.getLong(LearningActivityLocalServiceUtil.getExtraContentValue(learnact.getActId(),"timeStamp"));		
		hourDurationString = timestamp / 3600;
		minuteDurationString = (timestamp % 3600) / 60;
		secondDurationString = timestamp % 60;
	}
	
	/*
    DynamicQuery dq=DynamicQueryFactoryUtil.forClass(LearningActivityResult.class);
  	Criterion criterion=PropertyFactoryUtil.forName("actId").eq(learnact.getActId());
	dq.add(criterion);
    boolean existTries = LearningActivityTryLocalServiceUtil.dynamicQueryCount(dq)!=0;
    */
%>
<portlet:actionURL var="camposExtraURL" name="camposExtra">
</portlet:actionURL>

<%--portlet:renderURL var="cancel">
	<portlet:param name="actId" value="0"></portlet:param>
</portlet:renderURL--%>

	<liferay-portlet:actionURL portletName="editactivity_WAR_liferaylmsportlet" name="editactivityoptions" var="cancel">
		<liferay-portlet:param name="resId" value="<%=Long.toString(learnact.getActId())%>" />
		<portlet:param name="actId" value="0" />
	</liferay-portlet:actionURL>

<aui:form action="<%=camposExtraURL%>" method="post">
	<aui:input name="actId" type="hidden" value="<%=learnact.getActId()%>"></aui:input>

	<aui:input type="text" name="randomString" label="execActivity.options.random" value='<%=(random>0)?Long.toString(random):StringPool.BLANK %>'></aui:input>
	<div id="<portlet:namespace />randomStringError"></div>

	<aui:input type="text" name="passwordString" label="execActivity.options.password" value='<%=password %>'></aui:input>
	<div id="<portlet:namespace />passwordStringError"></div>

	<aui:field-wrapper label="execActivity.options.timestamp">
	<%
	NumberFormat timeNumberFormat = NumberFormat.getInstance(locale);
	timeNumberFormat.setMinimumIntegerDigits(2);
	%>
		<select name="<portlet:namespace />hourDurationString" <%-- if(existTries){ %> disabled="disabled" <% } --%> >
			<%
			for (int i = 0; i < 60; i++) {
				String hour = timeNumberFormat.format(i);
			%>
					<option <%= (hourDurationString == i) ? "selected" : "" %> value="<%= i %>"><%= hour %></option>
			<%
			}
			%>
		</select>
		<select name="<portlet:namespace />minuteDurationString" <%-- if(existTries){ %> disabled="disabled" <% } --%> >
			<%
			for (int i = 0; i < 60; i++) {
				String minute = timeNumberFormat.format(i);
			%>
					<option <%= (minuteDurationString == i) ? "selected" : "" %> value="<%= i %>">:<%= minute %></option>
			<%
			}
			%>
		</select>	
		<select name="<portlet:namespace />secondDurationString" <%-- if(existTries){ %> disabled="disabled" <% } --%> > 
			<%
			for (int i = 0; i < 60; i++) {
				String second = timeNumberFormat.format(i);
			%>
					<option <%= (secondDurationString == i) ? "selected" : "" %> value="<%= i %>">:<%= second %></option>
			<%
			}
			%>
		</select>			
	</aui:field-wrapper>
	<div id="<portlet:namespace />timeStampStringError"></div>
		
	<aui:button-row>
		<aui:button onclick="<%=renderResponse.getNamespace()+\"submit();\"%>" type="button" value="execActivity.options.save" />
		<aui:button onclick="<%=\"window.location='\"+cancel+\"';\"%>" type="button" value="execActivity.options.cancel" />
	</aui:button-row>
</aui:form>

<script type="text/javascript">
<!--

AUI().ready('node-base' ,'aui-form-validator', 'aui-overlay-context-panel', function(A) {

	A.mix(
		YUI.AUI.defaults.FormValidator.STRINGS,
		{
			randomRule: '<liferay-ui:message key="execActivity.options.error.random" />'
		},
		true
	);

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
			    case "<portlet:namespace />randomString":
					return true;
			    case "<portlet:namespace />passwordString":
					return true;		        
			    default:
			    	return oldRequired(val, fieldNode, ruleValue);
				}				
			},
			randomRule: function(val, fieldNode, ruleValue) {
				if(!A.Node.getDOMNode(fieldNode).form.<portlet:namespace />randomString.disabled){
					return YUI.AUI.defaults.FormValidator.REGEX.positiveLong.test(val);
				}
				return true;
			}		
		},
		true
	);

	
	window.<portlet:namespace />validator = new A.FormValidator({
		boundingBox: '#<portlet:namespace />fm',
		validateOnBlur: true,
		validateOnInput: true,
		selectText: true,
		showMessages: false,
		containerErrorClass: '',
		errorClass: '',
		rules: {			
			<portlet:namespace />randomString: {
				required: true,
				randomRule: true
			},
			<portlet:namespace />passwordString: {
				required: true
			}
		},

        fieldStrings: {
        	<portlet:namespace />randomString: {
    			randomRule: '<liferay-ui:message key="execActivity.options.error.random" />',
    			passwordRule: '<liferay-ui:message key="execActivity.options.error.password" />',
    			timeStampRule: '<liferay-ui:message key="execActivity.options.error.timestamp" />'
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

function <portlet:namespace />submit(){
	if(window.<portlet:namespace />validator) {
		AUI().use('node-base' ,'aui-form-validator', 'aui-overlay-context-panel', function(A) {
			window.<portlet:namespace />validator.validate();
			if(!window.<portlet:namespace />validator.hasErrors()){
				document.getElementById('<portlet:namespace />fm').submit();
			}
		});
	}
}

//-->
</script>


