<%@page import="com.liferay.portal.service.TeamLocalServiceUtil"%>
<%@page import="com.liferay.portal.model.Team"%>
<%@page import="com.liferay.portal.kernel.util.ListUtil"%>
<%@page import="com.liferay.portal.kernel.dao.orm.QueryUtil"%>
<%@page import="com.liferay.portal.service.ResourceBlockLocalServiceUtil"%>
<%@page import="com.liferay.portal.kernel.util.PropsKeys"%>
<%@page import="com.liferay.portal.kernel.util.PropsUtil"%>
<%@page import="com.liferay.portal.security.permission.PermissionCheckerFactoryUtil"%>
<%@page import="com.liferay.lms.service.LearningActivityTryLocalServiceUtil"%>
<%@page import="com.liferay.lms.model.LearningActivityTry"%>
<%@page import="com.liferay.lms.service.LearningActivityResultLocalServiceUtil"%>
<%@page import="com.liferay.lms.service.LearningActivityLocalServiceUtil"%>
<%@page import="com.liferay.lms.model.LearningActivity"%>
<%@page import="com.liferay.portal.service.ServiceContextFactory"%>
<%@page import="com.liferay.portal.service.ServiceContext"%>
<%@page import="javax.portlet.RenderResponse"%>
<%@page import="com.liferay.portal.model.Role"%>
<%@page import="com.liferay.portal.model.RoleConstants"%>
<%@page import="com.liferay.portal.kernel.portlet.LiferayWindowState"%>
<%@page import="com.liferay.portal.kernel.util.OrderByComparator"%>
<%@page import="com.liferay.portal.kernel.dao.orm.CustomSQLParam"%>
<%@page import="com.liferay.lms.model.LearningActivityResult"%>
<%@page import="com.liferay.lms.model.Course"%>
<%@page import="com.liferay.lms.OfflineActivity"%>
<%@page import="com.liferay.lms.service.CourseLocalServiceUtil"%>
<%@page import="com.liferay.portal.util.comparator.UserFirstNameComparator"%>
<%@page import="com.liferay.portal.kernel.workflow.WorkflowConstants"%>
<%@page import="com.liferay.portal.service.RoleLocalServiceUtil"%>
<%@ include file="/init.jsp" %>
<div class="container-activity">
<%
	long actId = ParamUtil.getLong(request,"actId",0);
	
	if(actId==0)
	{
		renderRequest.setAttribute(WebKeys.PORTLET_CONFIGURATOR_VISIBILITY, Boolean.FALSE);
	}
	else
	{
		Course course=CourseLocalServiceUtil.fetchByGroupCreatedId(themeDisplay.getScopeGroupId());
		LearningActivity activity = LearningActivityLocalServiceUtil.getLearningActivity(actId);
		long typeId=activity.getTypeId();
		
		boolean isOffline = activity.getTypeId() == 5;
		
		LearningActivityResult result = LearningActivityResultLocalServiceUtil.getByActIdAndUserId(actId, themeDisplay.getUserId());
		Object  [] arguments=null;
		
		if(result!=null){	
			arguments =  new Object[]{result.getResult()};
		}
		
		boolean isTeacher=permissionChecker.hasPermission(themeDisplay.getScopeGroupId(), "com.liferay.lms.model",themeDisplay.getScopeGroupId(), "VIEW_RESULTS");	
		
		if(typeId==5&&(!LearningActivityLocalServiceUtil.islocked(actId,themeDisplay.getUserId())||
				permissionChecker.hasPermission(activity.getGroupId(), LearningActivity.class.getName(), actId, ActionKeys.UPDATE)||
				permissionChecker.hasPermission(themeDisplay.getScopeGroupId(), "com.liferay.lms.model",themeDisplay.getScopeGroupId(),"ACCESSLOCK")))
		{
		%>

			<div class="offlinetaskactivity view">

				<h2 class="description-title"><%=activity.getTitle(themeDisplay.getLocale()) %></h2>
										
				<% if(isTeacher){ %>
									
				<portlet:renderURL var="viewUrlPopImportGrades" windowState="<%= LiferayWindowState.POP_UP.toString() %>">   
					<portlet:param name="actId" value="<%=String.valueOf(activity.getActId()) %>" />      
		            <portlet:param name="jspPage" value="/html/offlinetaskactivity/popups/importGrades.jsp" />           
		        </portlet:renderURL>
		        
				<portlet:renderURL var="viewUrlPopGrades" windowState="<%= LiferayWindowState.POP_UP.toString() %>">   
					<portlet:param name="actId" value="<%=String.valueOf(activity.getActId()) %>" />      
		            <portlet:param name="jspPage" value="/html/offlinetaskactivity/popups/grades.jsp" />           
		        </portlet:renderURL>
		        
		        <portlet:renderURL var="setGradesURL" windowState="<%= LiferayWindowState.EXCLUSIVE.toString() %>">   
					<portlet:param name="actId" value="<%=String.valueOf(activity.getActId()) %>" /> 
					<portlet:param name="ajaxAction" value="setGrades" />      
		            <portlet:param name="jspPage" value="/html/offlinetaskactivity/popups/grades.jsp" />           
		        </portlet:renderURL>

				<script type="text/javascript">
			    <!--


				    function <portlet:namespace />showPopupImportGrades()
				    {
						AUI().use('aui-dialog','liferay-portlet-url','event', function(A){
							window.<portlet:namespace />popupImportGrades = new A.Dialog({
								id:'<portlet:namespace />showPopupImportGrades',
					            title: '<liferay-message key="offlinetaskactivity.import.grades" />',
						    	centered: true,
					            modal: true,
					            width: 550,
					            height: 320,
					            after: {   
						          	close: function(event){ 
						          		document.getElementById('<portlet:namespace />studentsearch').submit();
					            	}
					            }
					        }).plug(A.Plugin.IO, {
					            uri: '<%= viewUrlPopImportGrades %>'
					        }).render();
							window.<portlet:namespace />popupImportGrades.show();   
						});
				    }

				    function <portlet:namespace />doClosePopupImportGrades()
				    {
				        AUI().use('aui-dialog', function(A) {
				        	window.<portlet:namespace />popupImportGrades.close();
				        });
				    }


				    function <portlet:namespace />doImportGrades()
				    {
						var importGradesDIV=document.getElementById('<portlet:namespace />import_frame').
											contentDocument.getElementById('<portlet:namespace />importErrors');
						if(importGradesDIV){
							document.getElementById('<portlet:namespace />importErrors').innerHTML=importGradesDIV.innerHTML;
						}
						else {
							document.getElementById('<portlet:namespace />importErrors').innerHTML='';
						}
				    }


				    function <portlet:namespace />showPopupGrades(studentId)
				    {

						AUI().use('aui-dialog','liferay-portlet-url', function(A){
							var renderUrl = Liferay.PortletURL.createRenderURL();							
							renderUrl.setWindowState('<%= LiferayWindowState.POP_UP.toString() %>');
							renderUrl.setPortletId('<%=portletDisplay.getId()%>');
							renderUrl.setParameter('actId', '<%=String.valueOf(activity.getActId()) %>');
							renderUrl.setParameter('studentId', studentId);
							renderUrl.setParameter('jspPage', '/html/offlinetaskactivity/popups/grades.jsp');

							window.<portlet:namespace />popupGrades = new A.Dialog({
								id:'<portlet:namespace />showPopupGrades',
					            title: '<%=LanguageUtil.format(pageContext, "offlinetaskactivity.set.grades", new Object[]{""})%>',
					            centered: true,
					            modal: true,
					            width: 600,
					            height: 350,
					            after: {   
						          	close: function(event){ 
						          		document.getElementById('<portlet:namespace />studentsearch').submit();
					            	}
					            }
					        }).plug(A.Plugin.IO, {
					            uri: renderUrl.toString(),
					            parseContent: true
					        }).render();
							window.<portlet:namespace />popupGrades.show();   
						});
				    }

				    function <portlet:namespace />doClosePopupGrades()
				    {
				        AUI().use('aui-dialog', function(A) {
				        	window.<portlet:namespace />popupGrades.close();
				        });
				    }

				    function <portlet:namespace />doSaveGrades()
				    {
				        AUI().use('aui-io-request','io-form', function(A) {
				            A.io.request('<%= setGradesURL %>', { 
				                method : 'POST', 
				                form: {
				                    id: '<portlet:namespace />fn_grades'
				                },
				                dataType : 'html', 
				                on : { 
				                    success : function() { 
				                    	A.one('.aui-dialog-bd form').set('innerHTML',A.Node.create('<div>'+this.get('responseData')+'</div>').one('form').get('innerHTML'));	
				                    	createValidator();			                    	
				                    } 
				                } 
				            });
				        });
				    }
			
				    //-->
				</script>

				<div class="container-toolbar" >
					
					<liferay-ui:icon-menu cssClass='bt_importexport' direction="down" extended="<%= false %>" message="export-import" showWhenSingleIcon="<%= true %>">
					
						<div>
							<liferay-portlet:resourceURL var="exportURL" >
								<portlet:param name="action" value="export"/>
								<portlet:param name="resId" value="<%=String.valueOf(activity.getActId()) %>"/>
							</liferay-portlet:resourceURL>
							<liferay-ui:icon image="export" label="<%= true %>" message="offlinetaskactivity.csv.export" method="get" url="<%=exportURL%>" />
						</div>
						<div>
							<liferay-ui:icon image="add" label="<%= true %>" message="offlinetaskactivity.import.grades" url='<%="javascript:"+renderResponse.getNamespace() + "showPopupImportGrades();" %>'/>
						</div>
					</liferay-ui:icon-menu>

				</div>
				
				<% } %>
				<h3><liferay-ui:message key="offlinetaskactivity.description" /> </h3>
				<p><%=activity.getDescription(themeDisplay.getLocale()) %></p>
				
				
				<% if((PermissionCheckerFactoryUtil.create(themeDisplay.getUser())).hasPermission(themeDisplay.getScopeGroupId(), "com.liferay.lms.model", themeDisplay.getScopeGroupId(), "VIEW_RESULTS")){ 
					String criteria = request.getParameter("criteria");
					String gradeFilter = request.getParameter("gradeFilter");

					if (criteria == null) criteria = "";	
					if (gradeFilter == null) gradeFilter = "";	
					
					PortletURL portletURL = renderResponse.createRenderURL();
					portletURL.setParameter("jspPage","/html/offlinetaskactivity/view.jsp");
					portletURL.setParameter("criteria", criteria); 
					portletURL.setParameter("gradeFilter", gradeFilter);
				
				%>
				
				<liferay-portlet:renderURL var="returnurl" />
				
				<h5><liferay-ui:message key="studentsearch"/></h5>
				<aui:form name="studentsearch" action="<%=returnurl %>" method="post">
					<aui:fieldset>
						<aui:column>
							<aui:input label="studentsearch.text.criteria" name="criteria" size="25" value="<%=criteria %>" />	
						</aui:column>	
						<aui:column>
							<aui:select label="offlinetaskactivity.status" name="gradeFilter" onchange='<%="document.getElementById(\'" + renderResponse.getNamespace() + "studentsearch\').submit();" %>'>
								<aui:option selected='<%= gradeFilter.equals("") %>' value=""><liferay-ui:message key="offlinetaskactivity.all" /></aui:option>
								<aui:option selected='<%= gradeFilter.equals("nocalification") %>' value="nocalification"><liferay-ui:message key="offlinetaskactivity.status.passed" /></aui:option>
								<aui:option selected='<%= gradeFilter.equals("passed") %>' value="passed"><liferay-ui:message key="offlinetaskactivity.passed" /></aui:option>
								<aui:option selected='<%= gradeFilter.equals("failed") %>' value="failed"><liferay-ui:message key="offlinetaskactivity.failed" /></aui:option>
							</aui:select>
						</aui:column>	
						<aui:column>
							<aui:button cssClass="inline-button" name="searchUsers" value="search" type="submit" />
						</aui:column>
					</aui:fieldset>
				</aui:form>
				
					
					<liferay-ui:search-container iteratorURL="<%=portletURL%>" emptyResultsMessage="there-are-no-results" delta="10" deltaConfigurable="true">

				   	<liferay-ui:search-container-results>
						<%
							String middleName = null;
					
							LinkedHashMap<String,Object> params = new LinkedHashMap<String,Object>();
							
							params.put("usersGroups", new Long(themeDisplay.getScopeGroupId()));
							if(gradeFilter.equals("passed")) {
								params.put("passed",new CustomSQLParam(OfflineActivity.ACTIVITY_RESULT_PASSED_SQL,actId));
							}
							else {
								if(gradeFilter.equals("failed")) {
									params.put("failed",new CustomSQLParam(OfflineActivity.ACTIVITY_RESULT_FAIL_SQL,actId));
								} else {
									if (gradeFilter.equals("nocalification")) {
										params.put("nocalification",new CustomSQLParam(OfflineActivity.ACTIVITY_RESULT_NO_CALIFICATION_SQL,actId));
									}
								}
							}
																				
							OrderByComparator obc = new UserFirstNameComparator(true);
							
							if(!StringPool.BLANK.equals(LearningActivityLocalServiceUtil.getExtraContentValue(actId,"team"))){
								String teamId = LearningActivityLocalServiceUtil.getExtraContentValue(actId,"team");
								Team team = TeamLocalServiceUtil.getTeam(Long.parseLong(teamId));
								params.put("usersTeams", team.getTeamId());
							}
							if ((GetterUtil.getInteger(PropsUtil.get(PropsKeys.PERMISSIONS_USER_CHECK_ALGORITHM))==6)&&(!ResourceBlockLocalServiceUtil.isSupported("com.liferay.lms.model"))){		
								
								params.put("notTeacher",new CustomSQLParam(OfflineActivity.NOT_TEACHER_SQL,themeDisplay.getScopeGroupId()));
								List<User> userListPage = UserLocalServiceUtil.search(themeDisplay.getCompanyId(), criteria, WorkflowConstants.STATUS_ANY, params, searchContainer.getStart(), searchContainer.getEnd(), obc);
								int userCount = UserLocalServiceUtil.searchCount(themeDisplay.getCompanyId(), criteria,  WorkflowConstants.STATUS_ANY, params);
								pageContext.setAttribute("results", userListPage);
							    pageContext.setAttribute("total", userCount);
							    
							}
							else{
						
								List<User> userListsOfCourse = UserLocalServiceUtil.search(themeDisplay.getCompanyId(), criteria, WorkflowConstants.STATUS_ANY, params, QueryUtil.ALL_POS, QueryUtil.ALL_POS, obc);
								List<User> userLists =  new ArrayList<User>(userListsOfCourse.size());
								
								for(User userOfCourse:userListsOfCourse){							
									if(!PermissionCheckerFactoryUtil.create(userOfCourse).hasPermission(themeDisplay.getScopeGroupId(), "com.liferay.lms.model",themeDisplay.getScopeGroupId(), "VIEW_RESULTS")){
										userLists.add(userOfCourse);
									}
								}	
								
								pageContext.setAttribute("results", ListUtil.subList(userLists, searchContainer.getStart(), searchContainer.getEnd()));
							    pageContext.setAttribute("total", userLists.size());	
							}
						%>
					</liferay-ui:search-container-results>
					
					<liferay-ui:search-container-row className="com.liferay.portal.model.User" keyProperty="userId" modelVar="user">
					<liferay-ui:search-container-column-text name="name">
						<liferay-ui:user-display userId="<%=user.getUserId() %>"></liferay-ui:user-display>
					</liferay-ui:search-container-column-text>
					<liferay-ui:search-container-column-text name="calification">
						<% LearningActivityResult learningActivityResult = LearningActivityResultLocalServiceUtil.getByActIdAndUserId(actId, user.getUserId()); 
						   if((learningActivityResult!=null)&&(learningActivityResult.getEndDate()!= null)) {	   
								   Object  [] arg =  new Object[]{learningActivityResult.getResult(),activity.getPasspuntuation()};
								   if(learningActivityResult.getPassed()){
									   %><liferay-ui:message key="offlinetaskactivity.student.passed"  arguments="<%=arg %>" /><%
								   }else {
									   %><liferay-ui:message key="offlinetaskactivity.student.failed"  arguments="<%=arg %>" /><%
								   }
							  
			               	}else{
								   %><liferay-ui:message key="offlinetaskactivity.student.without.qualification" /><% 
							}%>
			            <p class="see-more">
							<a href="javascript:<portlet:namespace />showPopupGrades(<%=Long.toString(user.getUserId()) %>);">
								<liferay-ui:message key="offlinetaskactivity.set.grades"/>
							</a>
						</p>
					</liferay-ui:search-container-column-text>
					</liferay-ui:search-container-row>
					
				 	<liferay-ui:search-iterator />
				 	
				</liferay-ui:search-container>
				
				
				<% } %>	
				
				<div class="nota"> 

<%if(!isTeacher) {%>
	<h3><liferay-ui:message key="offlinetaskactivity.your-calification" /> </h3>
	<%if ((result!=null)&&(result.getEndDate()!=null)){ %>
		<p><liferay-ui:message key="offlinetaskactivity.your-result" arguments="<%=new Object[]{(arguments.length>0) ? arguments[0]:\"\"} %>" /></p>
		<p><liferay-ui:message key="offlinetaskactivity.needed-to-pass" arguments="<%=new Object[]{activity.getPasspuntuation()} %>" /></p>
	<%}else {%>
		<div class="nota_nocorregida"><liferay-ui:message key="offlinetaskactivity.not.qualificated.activity" /></div>
	<%}%>
	
	<h3><liferay-ui:message key="offlinetaskactivity.result.teachercoment" /> </h3>
	<%if ((result!=null)&&!"".equals(result.getComments().trim())){ %>
		<p><span class="destacado"><%=result.getComments() %></span></p>
	<% } else if(result==null){%>
		<p><liferay-ui:message key="offlinetaskactivity.no-teacher-comments-yet" /></p>
	<%}else {%>
		<p><liferay-ui:message key="offlinetaskactivity.no-teacher-comments" /></p>
	<%} %>
</div>
			</div>
			<%
		}
	}
	}
%>
</div>