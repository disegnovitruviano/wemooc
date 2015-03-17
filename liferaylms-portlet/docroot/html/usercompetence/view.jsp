<%@page import="com.liferay.lms.model.Competence"%>

<%@ include file="/init.jsp" %>

<jsp:useBean id="competences" type="java.util.List" scope="request" />
<jsp:useBean id="totale" class="java.lang.String" scope="request" />
<jsp:useBean id="delta" class="java.lang.String" scope="request" />

<% 
//QQQ esto hay que revisarlo, esta raro.
	PortletURL viewURL = renderResponse.createRenderURL(); 
	viewURL.setParameter("delta", delta);
	if(competences==null|| competences.size()==0)
		{
			renderRequest.setAttribute(WebKeys.PORTLET_CONFIGURATOR_VISIBILITY, Boolean.FALSE);
		}
	else
	{
%>
<div id="user_competences">
	<liferay-ui:search-container curParam="act" emptyResultsMessage="there-are-no-competences" delta="10" deltaConfigurable="true" iteratorURL="<%=viewURL%>"  >
		<liferay-ui:search-container-results>
		<%
			pageContext.setAttribute("results", competences);
			try{
				pageContext.setAttribute("total", Integer.valueOf(totale));
			}catch(NumberFormatException nfe){
				pageContext.setAttribute("total", competences.size());
			}
		%>
		</liferay-ui:search-container-results>
		<liferay-ui:search-container-row className="com.liferay.lms.views.CompetenceView" keyProperty="competenceId" modelVar="cc">
			<liferay-ui:search-container-column-text name="competence.label" >
			
			    <%if(cc.getGenerateCertificate())
			    	{
			    	%>
			    	<portlet:resourceURL var="resourceURL" >
						<portlet:param name="competenceId" value="<%=String.valueOf(cc.getCompetenceId())%>" />
					</portlet:resourceURL>
				<a target="_blank" href="<%=resourceURL %>" ><%=cc.getTitle(themeDisplay.getLocale()) %></a>
				<%
				}
			    else
			    {
			    	%>
			    	<%=cc.getTitle(themeDisplay.getLocale())%>
			    	<%
			    }
				%>
			</liferay-ui:search-container-column-text>
			<liferay-ui:search-container-column-text name="date" >
				<%=cc.getFormatDate(cc.getDate(),themeDisplay.getLocale(), themeDisplay.getTimeZone()) %>
			</liferay-ui:search-container-column-text>
		</liferay-ui:search-container-row>
		<liferay-ui:search-iterator />
	</liferay-ui:search-container>
</div>
<%
}
%>