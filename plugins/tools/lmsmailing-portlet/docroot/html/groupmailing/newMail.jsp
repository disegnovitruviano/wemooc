<%@page import="com.tls.liferaylms.util.JavaScriptUtil"%>
<%@page import="com.liferay.portal.kernel.util.HttpUtil"%>
<%@page import="com.liferay.portal.kernel.portlet.LiferayWindowState"%>
<%@page import="com.liferay.portal.kernel.workflow.WorkflowConstants"%>
<%@page import="com.liferay.portal.util.comparator.UserFirstNameComparator"%>
<%@page import="com.liferay.portal.kernel.util.OrderByComparator"%>
<%@ include file="/init.jsp" %>

<%
	String body=""; 
	String extractCodeFromEditor = renderResponse.getNamespace() + "extractCodeFromEditor()";
	String criteria = request.getParameter("criteria");

	if (criteria == null) criteria = "";	
	
	PortletURL portletURL = renderResponse.createRenderURL();
	portletURL.setParameter("jspPage","/html/groupmailing/newMail.jsp");
	portletURL.setParameter("criteria", criteria); 
%>

<script type="text/javascript">

	YUI.add('<portlet:namespace />user-model', function(A) {
	    A.<portlet:namespace />UserModel = A.Base.create('<portlet:namespace />UserModel', A.Model, [], {
	    }, {
	        ATTRS: {
	            name: {
	                value: ''
	            }
	        }
	    });

	    A.<portlet:namespace />UserModelList = A.Base.create('<portlet:namespace />UserModelList', A.ModelList, [], {
	        comparator: function (model) {
	            return model.get('name');
	        },
	        model: A.<portlet:namespace />UserModel
	    });
	
	}, '' ,{requires:['model-list']});

	function <portlet:namespace />addUser(userId, userName){
		AUI().use('node-base','<portlet:namespace />user-model', function(A) {
			var existingUser=window.<portlet:namespace />selectedUsers.getById(userId);
			if(existingUser!=null){
				window.<portlet:namespace />selectedUsers.remove(existingUser);
			}	
			window.<portlet:namespace />selectedUsers.add(
					new A.<portlet:namespace />UserModel({id:userId,name:userName}));
			
			A.one('#<portlet:namespace />selected_users').setContent(window.<portlet:namespace />selectedUsers.get('name').toString());	
			A.one('#<portlet:namespace />to').val(window.<portlet:namespace />selectedUsers.get('id').toString());
			A.one('#<portlet:namespace />addUser_'+userId).hide();
			A.one('#<portlet:namespace />deleteUser_'+userId).show();
		});			
	}
	
	function <portlet:namespace />deleteUser(userId){
		AUI().use('node-base','<portlet:namespace />user-model', function(A) {
			var existingUser=window.<portlet:namespace />selectedUsers.getById(userId);
			if(existingUser!=null){
				window.<portlet:namespace />selectedUsers.remove(existingUser);
			}	
			A.one('#<portlet:namespace />selected_users').setContent(window.<portlet:namespace />selectedUsers.get('name').toString());	
			A.one('#<portlet:namespace />to').val(window.<portlet:namespace />selectedUsers.get('id').toString());
			A.one('#<portlet:namespace />addUser_'+userId).show();
			A.one('#<portlet:namespace />deleteUser_'+userId).hide();	
		});		
	}
	
	function <portlet:namespace />changeSelection(){
		AUI().use('node-base','<portlet:namespace />user-model', function(A) {
			if (A.one('input:radio[name=<portlet:namespace />radio_to]:checked').get('value')=='all') {
				window.<portlet:namespace />selectedUsers.each(
					function(userModel){
						A.all('#<portlet:namespace />addUser_'+userModel.get('id')).each(function(addUserDiv){ addUserDiv.show(); });  
						A.all('#<portlet:namespace />deleteUser_'+userModel.get('id')).each(function(deleteUserDiv){ deleteUserDiv.hide(); });  
					}
				);
				window.<portlet:namespace />selectedUsers.reset();
				A.one('#<portlet:namespace />to').val ('');
				A.one('#<portlet:namespace />selected_users').setContent('<liferay-ui:message key="all"/>');
				A.one('#<portlet:namespace />student_search').hide();
			}
			else if (A.one('input:radio[name=<portlet:namespace />radio_to]:checked').get('value')=='student') {
				A.one('#<portlet:namespace />selected_users').setContent('');
				A.one('#<portlet:namespace />student_search').show();
			}
			
		});
		
	}

	AUI().ready('node-base','<portlet:namespace />user-model', function(A) {
		window.<portlet:namespace />selectedUsers = new A.<portlet:namespace />UserModelList();
		
		var searchContainer = A.one('#<%=renderResponse.getNamespace() %>usersSearchContainerSearchContainer').ancestor('.lfr-search-container');
		searchContainer.on('ajaxLoaded',function(){
			window.<portlet:namespace />selectedUsers.each(
				function(userModel){
					A.all('#<portlet:namespace />addUser_'+userModel.get('id')).each(function(addUserDiv){ addUserDiv.hide(); });  
					A.all('#<portlet:namespace />deleteUser_'+userModel.get('id')).each(function(deleteUserDiv){ deleteUserDiv.show(); });  
				}
			);
		});

		A.one('#<%=renderResponse.getNamespace() %>form_mail').on('submit', function(evt) {			
	         if((A.one('input:radio[name=<portlet:namespace />radio_to]:checked').get('value')=='student')&& 
	    	    (window.<portlet:namespace />selectedUsers.isEmpty ())) {
	             evt.preventDefault();
	             evt.halt();             
	         }
	    });
		
	});

</script>

<aui:form name="form_to" >
	<aui:field-wrapper name="mailto">
		<aui:input checked="<%= true %>" inlineLabel="all" name="radio_to" type="radio" value="all" label="all"  onClick="<%=renderResponse.getNamespace()+\"changeSelection()\" %>" />
		<aui:input  inlineLabel="student" name="radio_to" type="radio" value="student" label="student" onClick="<%=renderResponse.getNamespace()+\"changeSelection()\" %>"  />
	</aui:field-wrapper>
</aui:form>
	
	
<div id="<portlet:namespace />student_search" class="aui-helper-hidden" > 
	
	<liferay-ui:search-container iteratorURL="<%=portletURL%>" emptyResultsMessage="there-are-no-results" delta="5" deltaConfigurable="true" >

	   	<liferay-ui:search-container-results>
			<%
			
				String middleName = null;
		
				LinkedHashMap<String,Object> params=null;			
				
				OrderByComparator obc = new UserFirstNameComparator(true);
			
				List<User> userListPage = UserLocalServiceUtil.search(themeDisplay.getCompanyId(), criteria, WorkflowConstants.STATUS_ANY, params, searchContainer.getStart(), searchContainer.getEnd(), obc);
				int userCount = UserLocalServiceUtil.searchCount(themeDisplay.getCompanyId(), criteria,  WorkflowConstants.STATUS_ANY, params);
						
				pageContext.setAttribute("results", userListPage);
			    pageContext.setAttribute("total", userCount);
			
			%>
		</liferay-ui:search-container-results>
		
		<liferay-ui:search-container-row className="com.liferay.portal.model.User" keyProperty="userId" modelVar="user">
			<liferay-ui:search-container-column-text name="studentsearch.user.firstName" title="studentsearch.user.firstName"><%=user.getFullName() %></liferay-ui:search-container-column-text>
			<liferay-ui:search-container-column-text>
				<a id="<portlet:namespace />addUser_<%=user.getUserId() %>" onClick="<portlet:namespace />addUser(<%=user.getUserId() %>, '<%=user.getFullName() %>')" style="Cursor:pointer;" >
				<liferay-ui:message key="select" /></a>
				<a id="<portlet:namespace />deleteUser_<%=user.getUserId() %>" class="aui-helper-hidden" onClick="<portlet:namespace />deleteUser(<%=user.getUserId() %>)" style="Cursor:pointer;" >
				<liferay-ui:message key="groupmailing.deselect" /></a>			
			</liferay-ui:search-container-column-text>
		</liferay-ui:search-container-row>

	 	<liferay-ui:search-iterator />
		
		<script type="text/javascript">
		<!--
			function <portlet:namespace />ajaxMode<%= searchContainer.getId(request, renderResponse.getNamespace()) %>SearchContainer(A) {

				var searchContainer = A.one('#<%=renderResponse.getNamespace() %><%= searchContainer.getId(request, renderResponse.getNamespace()) %>SearchContainer').ancestor('.lfr-search-container');
				
				function <portlet:namespace />reload<%= searchContainer.getId(request, renderResponse.getNamespace()) %>SearchContainer(url){

					var params = {};
					var urlPieces = url.split('?');
					if (urlPieces.length > 1) {
						params = A.QueryString.parse(urlPieces[1]);
						params.p_p_state='<%=LiferayWindowState.EXCLUSIVE.toString() %>';
						url = urlPieces[0];
					}
	
					A.io.request(
						url,
						{
							data: params,
							dataType: 'html',
							on: {
								failure: function(event, id, obj) {
									var portlet = A.one('#p_p_id<portlet:namespace />');
									portlet.hide();
									portlet.placeAfter('<div class="portlet-msg-error"><liferay-ui:message key="there-was-an-unexpected-error.-please-refresh-the-current-page"/></div>');
								},
								success: function(event, id, obj) {
									searchContainer.setContent(A.Node.create(this.get('responseData')).one('#<%=renderResponse.getNamespace() %><%= searchContainer.getId(request, renderResponse.getNamespace()) %>SearchContainer').ancestor('.lfr-search-container').getContent ());
									<portlet:namespace />ajaxMode<%= searchContainer.getId(request, renderResponse.getNamespace()) %>SearchContainer(A);
									searchContainer.fire('ajaxLoaded');
								}
							}
						}
					);
				}

				
				<portlet:namespace /><%= searchContainer.getCurParam() %>updateCur = function(box){
					<portlet:namespace />reload<%= searchContainer.getId(request, renderResponse.getNamespace()) 
					%>SearchContainer('<%=HttpUtil.removeParameter(searchContainer.getIteratorURL().toString(), renderResponse.getNamespace() + searchContainer.getCurParam()) 
						%>&<%= renderResponse.getNamespace() + searchContainer.getCurParam() %>=' + A.one(box).val());
				};

				<portlet:namespace /><%= searchContainer.getDeltaParam() %>updateDelta = function(box){
					<portlet:namespace />reload<%= searchContainer.getId(request, renderResponse.getNamespace()) 
						%>SearchContainer('<%=HttpUtil.removeParameter(searchContainer.getIteratorURL().toString(), renderResponse.getNamespace() + searchContainer.getDeltaParam()) 
							%>&<%= renderResponse.getNamespace() + searchContainer.getDeltaParam() %>=' + A.one(box).val());
				};

				searchContainer.all('.taglib-page-iterator').each(
					function(pageIterator){
						pageIterator.all('a').each(
							function(anchor){
								var url=anchor.get('href');
								anchor.set('href','#');
							    anchor.on('click',
									function(){
							    		<portlet:namespace />reload<%= searchContainer.getId(request, renderResponse.getNamespace()) %>SearchContainer(url);
								    }
							    );
							}
						);
					}
				);

			};

			AUI().ready('aui-io-request','querystring-parse','aui-parse-content',<portlet:namespace />ajaxMode<%= searchContainer.getId(request, renderResponse.getNamespace()) %>SearchContainer);
		//-->
		</script>

	</liferay-ui:search-container>
			
	
	<div class="to">
		<liferay-ui:message key="groupmailing.messages.to" />
		<div id="<portlet:namespace />selected_users" ><liferay-ui:message key="all"/></div>
	</div>
	
</div>



<div class="newmail">

	<liferay-portlet:actionURL name="sendNewMail" var="sendNewMailURL" >
	</liferay-portlet:actionURL>
	
	<liferay-portlet:renderURL var="returnURL">
		<liferay-portlet:param name="jspPage" value="/html/groupmailing/view.jsp"></liferay-portlet:param>
	</liferay-portlet:renderURL>
		
	<aui:form action="<%=sendNewMailURL %>" method="POST" name="form_mail">
	
		<aui:input type="hidden" name="to" id="to" value="" />
	
		<div class="mail_subject" >
			<aui:input name="subject" title="groupmailing.messages.subject"></aui:input>
		</div>
		
		<div class="mail_content" >
			<aui:field-wrapper label="body">
				<liferay-ui:input-editor name="body" />
				<script type="text/javascript">
			    <!--				
				    function <portlet:namespace />initEditor()
				    {
				    	return "<%=JavaScriptUtil.markupToStringLiteral(body)%>";
				    }
			        //-->
			    </script>
		    </aui:field-wrapper>
		</div>
				
		<div class="check_testing" >
			<aui:input name="testing" label="send-test" type="checkbox"></aui:input>
		</div>

    	<p><%=LanguageUtil.get(pageContext,"groupmailing.messages.explain")%></p>
    	<ul>
    		<li>[@portal] - <%=LanguageUtil.get(pageContext,"groupmailing.messages.portal")%></li>
    		<li>[@course] - <%=LanguageUtil.get(pageContext,"groupmailing.messages.course")%></li>
    		<li>[@student] - <%=LanguageUtil.get(pageContext,"groupmailing.messages.student")%></li>
    		<li>[@teacher] - <%=LanguageUtil.get(pageContext,"groupmailing.messages.teacher")%></li>
    		<li>[@urlcourse] - <%=LanguageUtil.get(pageContext,"groupmailing.messages.urlcourse")%></li>
    		<li>[@url] - <%=LanguageUtil.get(pageContext,"groupmailing.messages.url")%></li>
    	</ul>

		<aui:button-row>
			<aui:button type="submit" value="send" label="send" class="submit"></aui:button>
			<aui:button onClick="<%=returnURL.toString() %>" type="cancel" ></aui:button>
		</aui:button-row>
	</aui:form>

</div>