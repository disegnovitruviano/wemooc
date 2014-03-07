package com.liferay.lms;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.jsoup.Jsoup;

import au.com.bytecode.opencsv.CSVWriter;

import com.liferay.lms.asset.LearningActivityAssetRendererFactory;
import com.liferay.lms.auditing.AuditConstants;
import com.liferay.lms.auditing.AuditingLogFactory;
import com.liferay.lms.learningactivity.questiontype.QuestionType;
import com.liferay.lms.learningactivity.questiontype.QuestionTypeRegistry;
import com.liferay.lms.model.LearningActivity;
import com.liferay.lms.model.LearningActivityResult;
import com.liferay.lms.model.LearningActivityTry;
import com.liferay.lms.model.TestAnswer;
import com.liferay.lms.model.TestQuestion;
import com.liferay.lms.service.LearningActivityLocalServiceUtil;
import com.liferay.lms.service.LearningActivityResultLocalServiceUtil;
import com.liferay.lms.service.LearningActivityTryLocalServiceUtil;
import com.liferay.lms.service.TestAnswerLocalServiceUtil;
import com.liferay.lms.service.TestQuestionLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.DocumentException;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.portal.model.User;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.asset.model.AssetRenderer;
import com.liferay.util.bridges.mvc.MVCPortlet;

/**
 * Portlet implementation class ExecActivity
 */
public class ExecActivity extends MVCPortlet 
{
	static final Pattern DOCUMENT_EXCEPTION_MATCHER = Pattern.compile("Error on line (\\d+) of document ([^ ]+) : (.*)");

	HashMap<Long, TestAnswer> answersMap = new HashMap<Long, TestAnswer>(); 

	public void correct	(ActionRequest actionRequest,ActionResponse actionResponse)	throws Exception {

		long actId=ParamUtil.getLong(actionRequest, "actId");
		long latId=ParamUtil.getLong(actionRequest,"latId" );
		String navigate = ParamUtil.getString(actionRequest, "navigate");
		boolean isPartial = false;
		if (Validator.isNotNull(navigate)) {
			if (Validator.equals(navigate, "backward") || Validator.equals(navigate, "forward")) {
				isPartial = true;
			}
		}

		LearningActivityTry larntry=LearningActivityTryLocalServiceUtil.getLearningActivityTry(latId);

		//Comprobar que el usuario tenga intentos posibles.
		if (larntry.getEndDate() == null){

			long correctanswers=0;
			Element resultadosXML=SAXReaderUtil.createElement("results");
			Document resultadosXMLDoc=SAXReaderUtil.createDocument(resultadosXML);

			long[] questionIds = ParamUtil.getLongValues(actionRequest, "question");


			for (long questionId : questionIds) {
				TestQuestion question = TestQuestionLocalServiceUtil.fetchTestQuestion(questionId);
				QuestionType qt = new QuestionTypeRegistry().getQuestionType(question.getQuestionType());
				if(!isPartial && qt.correct(actionRequest, questionId)) {
					correctanswers++;
				}
				resultadosXML.add(qt.getResults(actionRequest, questionId));								
			}

			long random = GetterUtil.getLong(LearningActivityLocalServiceUtil.getExtraContentValue(actId,"random"));
			List<TestQuestion> questions=TestQuestionLocalServiceUtil.getQuestions(actId);
			long score=isPartial ? 0 : correctanswers*100/((random!=0 && random<questions.size())?random:questions.size());

			LearningActivityResult learningActivityResult = LearningActivityResultLocalServiceUtil.getByActIdAndUserId(actId, PortalUtil.getUserId(actionRequest));
			long oldResult=-1;
			if(learningActivityResult!=null) oldResult=learningActivityResult.getResult();

			larntry.setTryResultData(resultadosXMLDoc.formattedString());
			if (!isPartial) {
				larntry.setResult(score);
				larntry.setEndDate(new java.util.Date(System.currentTimeMillis()));
			}

			LearningActivityTryLocalServiceUtil.updateLearningActivityTry(larntry);

			actionResponse.setRenderParameters(actionRequest.getParameterMap());

			if (isPartial) {
				actionResponse.setRenderParameter("improve", ParamUtil.getString(actionRequest, "improve", Boolean.FALSE.toString()));
				actionResponse.setRenderParameter("jspPage", "/html/execactivity/test/view.jsp");
			} else {
				actionResponse.setRenderParameter("oldResult", Long.toString(oldResult));
				actionResponse.setRenderParameter("correction", Boolean.toString(true));
				actionResponse.setRenderParameter("jspPage", "/html/execactivity/test/results.jsp");
			}
		}else{
			actionResponse.setRenderParameters(actionRequest.getParameterMap());
			actionRequest.setAttribute("actId", actId);
			actionResponse.setRenderParameter("jspPage", "/html/execactivity/test/preview.jsp");
		}						

	}

	public void camposExtra(ActionRequest actionRequest, ActionResponse actionResponse)
			throws Exception {

		long actId = ParamUtil.getLong(actionRequest, "actId", 0);
		long randomString=ParamUtil.getLong(actionRequest, "randomString",0);
		String passwordString=ParamUtil.getString(actionRequest, "passwordString",StringPool.BLANK);
		long hourDurationString=ParamUtil.getLong(actionRequest, "hourDurationString",0);
		long minuteDurationString=ParamUtil.getLong(actionRequest, "minuteDurationString",0);
		long secondDurationString=ParamUtil.getLong(actionRequest, "secondDurationString",0);
		long timeStamp = hourDurationString * 3600 + minuteDurationString * 60 + secondDurationString;

		String showCorrectAnswer=ParamUtil.getString(actionRequest, "showCorrectAnswer", "false");
		String improve=ParamUtil.getString(actionRequest, "improve", "false");

		long questionsPerPage = ParamUtil.getInteger(actionRequest, "questionsPerPage", 1);

		if(randomString==0) {
			LearningActivityLocalServiceUtil.setExtraContentValue(actId, "random", StringPool.BLANK);
		}
		else {
			LearningActivityLocalServiceUtil.setExtraContentValue(actId, "random", Long.toString(randomString));
		}

		LearningActivityLocalServiceUtil.setExtraContentValue(actId, "password", HtmlUtil.escape(passwordString.trim()));

		if(timeStamp==0) {
			LearningActivityLocalServiceUtil.setExtraContentValue(actId, "timeStamp", StringPool.BLANK);
		}
		else {
			LearningActivityLocalServiceUtil.setExtraContentValue(actId, "timeStamp", Long.toString(timeStamp));
		}

		if(showCorrectAnswer.equals("true")) {
			LearningActivityLocalServiceUtil.setExtraContentValue(actId, "showCorrectAnswer", "true");
		}else if(showCorrectAnswer.equals("false")){
			LearningActivityLocalServiceUtil.setExtraContentValue(actId, "showCorrectAnswer", "false");
		}

		if(improve.equals("true")) {
			LearningActivityLocalServiceUtil.setExtraContentValue(actId, "improve", "true");
		}else if(improve.equals("false")) {
			LearningActivityLocalServiceUtil.setExtraContentValue(actId, "improve", "false");
		}

		if(questionsPerPage == 0) {
			LearningActivityLocalServiceUtil.setExtraContentValue(actId, "questionsPerPage", StringPool.BLANK);
		}
		else {
			LearningActivityLocalServiceUtil.setExtraContentValue(actId, "questionsPerPage", Long.toString(questionsPerPage));
		}

		SessionMessages.add(actionRequest, "activity-saved-successfully");
		actionResponse.setRenderParameter("jspPage", "/html/execactivity/test/admin/edit.jsp");

	}

	public void importQuestions(ActionRequest actionRequest, ActionResponse actionResponse)
			throws Exception {

		UploadPortletRequest request = PortalUtil.getUploadPortletRequest(actionRequest);

		long actId = ParamUtil.getLong(actionRequest, "resId");
		String fileName = request.getFileName("fileName");
		if(fileName==null || StringPool.BLANK.equals(fileName)){
			SessionErrors.add(actionRequest, "execativity.editquestions.importquestions.xml.fileRequired");
			actionResponse.setRenderParameter("jspPage", "/html/execactivity/test/admin/importquestions.jsp");
		}
		else{ 
			String contentType = request.getContentType("fileName");	
			if (!ContentTypes.TEXT_XML.equals(contentType) && !ContentTypes.TEXT_XML_UTF8.equals(contentType) ) {
				SessionErrors.add(actionRequest, "execativity.editquestions.importquestions.xml.badFormat");	
				actionResponse.setRenderParameter("jspPage", "/html/execactivity/test/admin/importquestions.jsp");
			}
			else {
				try {
					Document document = SAXReaderUtil.read(request.getFile("fileName"));
					TestQuestionLocalServiceUtil.importXML(actId, document);
					SessionMessages.add(actionRequest, "questions-added-successfully");
					actionResponse.setRenderParameter("jspPage", "/html/execactivity/test/admin/editquestions.jsp");
				} catch (DocumentException e) {
					Matcher matcher = DOCUMENT_EXCEPTION_MATCHER.matcher(e.getMessage());

					if(matcher.matches()) {
						SessionErrors.add(actionRequest, "execativity.editquestions.importquestions.xml.parseXMLLine", matcher.group(1));
					}
					else{
						SessionErrors.add(actionRequest, "execativity.editquestions.importquestions.xml.parseXML");					
					}
					actionResponse.setRenderParameter("jspPage", "/html/execactivity/test/admin/importquestions.jsp");
				} catch (Exception e) {
					SessionErrors.add(actionRequest, "execativity.editquestions.importquestions.xml.generic");
					actionResponse.setRenderParameter("jspPage", "/html/execactivity/test/admin/importquestions.jsp");
				}

			}

		}

		actionResponse.setRenderParameter("actionEditingDetails", StringPool.TRUE);
		actionResponse.setRenderParameter("resId", Long.toString(actId));	
	}

	public void editQuestion(ActionRequest actionRequest, ActionResponse actionResponse)
			throws Exception {

		long questionId = ParamUtil.getLong(actionRequest, "questionId", 0);
		long actid = ParamUtil.getLong(actionRequest, "resId");
		long questionType = ParamUtil.getLong(actionRequest, "typeId", -1);
		String questionText = ParamUtil.get(actionRequest, "text", "");
		ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);
		if(Validator.isNotNull(questionText)){//porque no se permite vacio ya que eliminar pregunta va por otro lado
			TestQuestion question = null;
			if(questionId == 0){//Nueva pregunta
				question = TestQuestionLocalServiceUtil.addQuestion(actid, questionText, questionType);
			}else{//Pregunta existente
				question = TestQuestionLocalServiceUtil.getTestQuestion(questionId);
				if(!questionText.equals(question.getText())){//Edicion de pregunta
					question.setText(questionText);
					TestQuestionLocalServiceUtil.updateTestQuestion(question);
				}
			}
			if(question!=null){
				questionId = question.getQuestionId();
				//Obtengo un array con los ids de las respuestas que ya conten�a la pregunta
				List<TestAnswer> existingAnswers = TestAnswerLocalServiceUtil.getTestAnswersByQuestionId(questionId);
				List<Long> existingAnswersIds = new ArrayList<Long>();
				for(TestAnswer answer:existingAnswers){
					existingAnswersIds.add(answer.getAnswerId());
				}
				//Recorro todas las respuestas y las actualizo o las creo en funcion de si son nuevas o modificaciones y si son modificaciones guardo sus ids en un array para despu�s borrar las que no existan.
				String[] newAnswersIds = ParamUtil.getParameterValues(actionRequest, "answerId", null);
				List<Long> editingAnswersIds = new ArrayList<Long>();
				if(newAnswersIds != null){
					for(String newAnswerId:newAnswersIds){
						String answer = ParamUtil.get(actionRequest, "answer_"+newAnswerId, "");
						if(Validator.isNotNull(answer)){
							boolean correct = ParamUtil.getBoolean(actionRequest, "correct_"+newAnswerId);
							String feedbackCorrect = ParamUtil.getString(actionRequest, "feedbackCorrect_"+newAnswerId, "");
							if(feedbackCorrect.length()>300) feedbackCorrect = feedbackCorrect.substring(0, 300);
							String feedbackNoCorrect = ParamUtil.getString(actionRequest, "feedbackNoCorrect_"+newAnswerId, "");
							if(feedbackNoCorrect.length()>300) feedbackNoCorrect = feedbackNoCorrect.substring(0, 300);
							if("".equals(feedbackNoCorrect)) feedbackNoCorrect = feedbackCorrect;
							if(newAnswerId.startsWith("new")){
								//creo respuesta
								TestAnswerLocalServiceUtil.addTestAnswer(questionId, answer, feedbackCorrect, feedbackNoCorrect, correct);
							}else {
								editingAnswersIds.add(Long.parseLong(newAnswerId));//almaceno en array para posterior borrado de las que no est�n
								//actualizo respuesta
								TestAnswer testanswer = TestAnswerLocalServiceUtil.getTestAnswer(Long.parseLong(newAnswerId));
								testanswer.setAnswer(answer);
								testanswer.setIsCorrect(correct);
								testanswer.setFeedbackCorrect(feedbackCorrect);
								testanswer.setFeedbacknocorrect(feedbackNoCorrect);
								TestAnswerLocalServiceUtil.updateTestAnswer(testanswer);
							}
						}else if(Validator.isNotNull(ParamUtil.getString(actionRequest, "feedbackCorrect_"+newAnswerId, "")) ||
								Validator.isNotNull(ParamUtil.getString(actionRequest, "feedbackNoCorrect_"+newAnswerId, "")) ||
								ParamUtil.getBoolean(actionRequest, "correct_"+newAnswerId)==true)
							SessionErrors.add(actionRequest, "answer-test-required");
					}
				}

				//Recorro los ids de respuestas que ya contenia y compruebo si siguen estando, si no, elimino dichas respuestas.
				for(Long existingAnswerId:existingAnswersIds){
					if(editingAnswersIds != null && editingAnswersIds.size()>0){
						if(!editingAnswersIds.contains(existingAnswerId)){
							TestAnswerLocalServiceUtil.deleteTestAnswer(existingAnswerId);
						}
					}else TestAnswerLocalServiceUtil.deleteTestAnswer(existingAnswerId);
				}
				
				actionResponse.setRenderParameter("message", LanguageUtil.get(themeDisplay.getLocale(), "execativity.editquestions.editquestion"));
			}else {
				SessionErrors.add(actionRequest, "execativity.test.error");
				actionResponse.setRenderParameter("message", LanguageUtil.get(themeDisplay.getLocale(), "execactivity.editquestions.newquestion"));
			}
		}else {
			SessionErrors.add(actionRequest, "execactivity.editquestions.newquestion.error.text.required");
			actionResponse.setRenderParameter("message", LanguageUtil.get(themeDisplay.getLocale(), "execactivity.editquestions.newquestion"));
		}

		if(SessionErrors.size(actionRequest)==0) SessionMessages.add(actionRequest, "question-modified-successfully");
		actionResponse.getRenderParameterMap().putAll(actionRequest.getParameterMap());
		actionResponse.setRenderParameter("questionId", Long.toString(questionId));
		actionResponse.setRenderParameter("actionEditingDetails", StringPool.TRUE);
		actionResponse.setRenderParameter("resId", Long.toString(actid));
		actionResponse.setRenderParameter("typeId", Long.toString(questionType));
		actionResponse.setRenderParameter("jspPage", "/html/execactivity/test/admin/editQuestion.jsp");
	}


	public void edit(ActionRequest actionRequest, ActionResponse actionResponse)
			throws PortalException, SystemException, Exception {

		actionResponse.setRenderParameters(actionRequest.getParameterMap());
		if(ParamUtil.getLong(actionRequest, "actId", 0)==0)// TODO Auto-generated method stub
		{
			actionResponse.setRenderParameter("jspPage", "/html/lmsactivitieslist/view.jsp");
		}
	}
	public void editactivity(ActionRequest actionRequest, ActionResponse actionResponse)
			throws PortalException, SystemException, Exception {
		long actId = ParamUtil.getInteger(actionRequest, "actId");
		// LearningActivity learnact =
		// com.liferay.lms.service.LearningActivityServiceUtil.getLearningActivity(actId);
		LearningActivityAssetRendererFactory laf = new LearningActivityAssetRendererFactory();
		if (laf != null) {
			AssetRenderer assetRenderer = laf.getAssetRenderer(actId, 0);

			String urlEdit = assetRenderer.getURLEdit((LiferayPortletRequest) actionRequest, (LiferayPortletResponse) actionResponse).toString();
			actionResponse.sendRedirect(urlEdit);
		}
		SessionMessages.add(actionRequest, "asset-renderer-not-defined");
	}

	@Override
	public void render(RenderRequest renderRequest, RenderResponse renderResponse)
			throws PortletException, IOException {
		long actId=0;

		if(ParamUtil.getBoolean(renderRequest, "actionEditingDetails", false)){

			actId=ParamUtil.getLong(renderRequest, "resId", 0);
			renderResponse.setProperty("clear-request-parameters",Boolean.TRUE.toString());
		}
		else{
			actId=ParamUtil.getLong(renderRequest, "actId", 0);

		}
		renderResponse.setProperty("clear-request-parameters",Boolean.TRUE.toString());

		if(actId==0)// TODO Auto-generated method stub
		{
			renderRequest.setAttribute(WebKeys.PORTLET_CONFIGURATOR_VISIBILITY, Boolean.FALSE);
		}
		else
		{
			LearningActivity activity;
			try {

				//auditing
				ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);
				AuditingLogFactory.audit(themeDisplay.getCompanyId(), themeDisplay.getScopeGroupId(), LearningActivity.class.getName(), 
						actId, themeDisplay.getUserId(), AuditConstants.VIEW, null);

				activity = LearningActivityLocalServiceUtil.getLearningActivity(actId);
				long typeId = activity.getTypeId();

				if(typeId==0)
				{
					super.render(renderRequest, renderResponse);
				}
				else
				{
					renderRequest.setAttribute(WebKeys.PORTLET_CONFIGURATOR_VISIBILITY, Boolean.FALSE);
				}
			} catch (PortalException e) {
			} catch (SystemException e) {
			}			
		}
	}


	public void  serveResource(ResourceRequest request, ResourceResponse response)throws PortletException, IOException {

		String action = ParamUtil.getString(request, "action");
		long actId = ParamUtil.getLong(request, "resId",0);
		
		response.setCharacterEncoding("ISO-8859-1");
		try {
			if(action.equals("exportResultsCsv")){
				response.addProperty(HttpHeaders.CONTENT_DISPOSITION,"attachment; fileName=data.csv");
				response.setContentType("text/csv;charset=ISO-8859-1");
				byte b[] = {(byte)0xEF, (byte)0xBB, (byte)0xBF};
				response.getPortletOutputStream().write(b);
				CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.getPortletOutputStream(),"ISO-8859-1"),';');

				//Crear la cabecera con las preguntas.
				List<TestQuestion> questions = TestQuestionLocalServiceUtil.getQuestions(actId);
				List<TestQuestion> questionsTitle = new ArrayList<TestQuestion>();
				for(TestQuestion question:questions){
					if(question.getQuestionType() == 0) questionsTitle.add(question);
				}
				//A�adimos x columnas para mostrar otros datos que no sean las preguntas como nombre de usuario, fecha, etc.
				int numExtraCols = 3;
				String[] cabeceras = new String[questionsTitle.size()+numExtraCols];

				//Guardamos el orden en que obtenemos las preguntas de la base de datos para poner las preguntas en el mismo orden.
				Long []questionOrder = new Long[questionsTitle.size()];

				//En las columnas extra ponemos la cabecera
				cabeceras[0]="User";
				cabeceras[1]="UserId";
				cabeceras[2]="Date";

				for(int i=numExtraCols;i<questionsTitle.size()+numExtraCols;i++){
					cabeceras[i]=formatString(questionsTitle.get(i-numExtraCols).getText())+" ("+questionsTitle.get(i-numExtraCols).getQuestionId()+")";
					questionOrder[i-numExtraCols]=questionsTitle.get(i-numExtraCols).getQuestionId();
				}
				writer.writeNext(cabeceras);

				//Partiremos del usuario para crear el csv para que sea m�s facil ver los intentos.
				List<User> users = LearningActivityTryLocalServiceUtil.getUsersByLearningActivity(actId);

				for(User user:users){

					//Para cada usuario obtenemos los intentos para la learning activity.
					List<LearningActivityTry> activities = LearningActivityTryLocalServiceUtil.getLearningActivityTryByActUser(actId, user.getUserId());
					List<Long> answersIds = new ArrayList<Long>();

					for(LearningActivityTry activity:activities){

						String xml = activity.getTryResultData();

						//Leemos el xml que contiene lo que ha respondido el estudiante.
						if(!xml.equals("")){

							Document document = SAXReaderUtil.read(xml);
							Element rootElement = document.getRootElement();

							//Obtenemos las respuestas que hay introducido.
							for(Element question:rootElement.elements("question")){

								TestQuestion q = TestQuestionLocalServiceUtil.getTestQuestion(Long.valueOf(question.attributeValue("id")));	        		

								if(q.getQuestionType() == 0){

									for(Element answerElement:question.elements("answer")){
										//Guardamos el id de la respuesta para posteriormente obtener su texto.
										if(Validator.isNumber(answerElement.attributeValue("id"))){
											answersIds.add(Long.valueOf(answerElement.attributeValue("id")));
										}
									}
								}

							}

							//Array con los resultados de los intentos.
							String[] resultados = new String[questionOrder.length+numExtraCols];

							//Introducimos los datos de las columnas extra
							resultados[0]=user.getFullName();
							resultados[1] = String.valueOf(user.getUserId());
							resultados[2] = String.valueOf(activity.getEndDate());

							for(int i=numExtraCols;i <questionOrder.length+numExtraCols ; i++){
								//Si no tenemos respuesta para la pregunta, guardamos ""
								resultados[i] = "-";

								for(int j=0;j <answersIds.size() ; j++){
									//Cuando la respuesta se corresponda con la pregunta que corresponde.
									if(Long.valueOf(getQuestionIdByAnswerId(answersIds.get(j))).compareTo(Long.valueOf(questionOrder[i-numExtraCols])) == 0){
										//Guardamos la respuesta en el array de resultados
										resultados[i]=getAnswerTextByAnswerId(answersIds.get(j));
									}
								}

							}
							//Escribimos las respuestas obtenidas para el intento en el csv.
							writer.writeNext(resultados);
						}
					}
				}

				writer.flush();
				writer.close();
					
			}else if(action.equals("exportXml")){
				response.addProperty(HttpHeaders.CONTENT_DISPOSITION,"attachment; fileName=data.xml");
				response.setContentType("text/xml; charset=UTF-8");
				PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(response.getPortletOutputStream(),"ISO-8859-1"));
				Element quizXML=SAXReaderUtil.createElement("quiz");
				Document quizXMLDoc=SAXReaderUtil.createDocument(quizXML);
				
				List<TestQuestion> questions = TestQuestionLocalServiceUtil.getQuestions(actId);
				if(questions!=null &&questions.size()>0){
					for(TestQuestion question:questions){
						QuestionType qt =new QuestionTypeRegistry().getQuestionType(question.getQuestionType());
						quizXML.add(qt.exportXML(question.getQuestionId()));
					}
				}
				
				printWriter.write(quizXMLDoc.formattedString());
				printWriter.flush();
				printWriter.close();
			}
		
			response.getPortletOutputStream().flush();
			response.getPortletOutputStream().close();

		} catch (PortalException e) {
			e.printStackTrace();
		} catch (SystemException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}finally{
			response.getPortletOutputStream().flush();
			response.getPortletOutputStream().close();
		}
	}

	private String formatString(String str) {

		String res = "";

		//Jsoup elimina todas la etiquetas html del string que se le pasa, devolviendo �nicamente el texto plano.
		res = Jsoup.parse(str).text();

		//Si el texto es muy largo, lo recortamos para que sea m�s legible.
		if(res.length() > 50){
			res = res.substring(0, 50);
		}

		return res;
	}

	private String getAnswerTextByAnswerId(Long answerId) throws PortalException, SystemException{
		//Buscamos la respuesta en el hashmap, si no lo tenemos, lo obtenemos de la bd y lo guardamos.
		if(!answersMap.containsKey(answerId))
		{
			TestAnswer answer = TestAnswerLocalServiceUtil.getTestAnswer(Long.valueOf(answerId));
			answersMap.put(answerId, answer);
		}

		return formatString(answersMap.get(answerId).getAnswer())+" ("+answersMap.get(answerId).getAnswerId()+")";
	}

	private Long getQuestionIdByAnswerId(Long answerId) throws PortalException, SystemException{
		//Buscamos la respuesta en el hashmap, si no lo tenemos, lo obtenemos y lo guardamos.
		if(!answersMap.containsKey(answerId))
		{
			TestAnswer answer = TestAnswerLocalServiceUtil.getTestAnswer(Long.valueOf(answerId));
			answersMap.put(answerId, answer);
		}

		return answersMap.get(answerId).getQuestionId();
	}

	public void deletequestion(ActionRequest actionRequest, ActionResponse actionResponse)
			throws Exception {

		TestQuestion question = TestQuestionLocalServiceUtil.getTestQuestion(ParamUtil.getLong(actionRequest, "questionId"));
		LearningActivity learnact = LearningActivityLocalServiceUtil.getLearningActivity(question.getActId());
		TestQuestionLocalServiceUtil.deleteTestQuestion(question.getQuestionId());
		SessionMessages.add(actionRequest, "question-deleted-successfully");

		if (learnact.getTypeId() == 0) {
			QuestionType qt =new QuestionTypeRegistry().getQuestionType(question.getQuestionType());
			actionResponse.setRenderParameter("actionEditingDetails", StringPool.TRUE);
			actionResponse.setRenderParameter("resId", Long.toString(question.getActId()));
			actionResponse.setRenderParameter("jspPage", qt.getURLBack());
		}
	}


}
