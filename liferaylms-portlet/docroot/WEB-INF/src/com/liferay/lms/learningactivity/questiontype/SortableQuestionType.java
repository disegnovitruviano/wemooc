package com.liferay.lms.learningactivity.questiontype;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.portlet.ActionRequest;

import com.liferay.lms.model.TestAnswer;
import com.liferay.lms.model.TestQuestion;
import com.liferay.lms.service.LearningActivityLocalServiceUtil;
import com.liferay.lms.service.TestAnswerLocalServiceUtil;
import com.liferay.lms.service.TestQuestionLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.portal.theme.ThemeDisplay;

public class SortableQuestionType extends BaseQuestionType {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String inputType = "textarea";

	public String getInputType() {
		return inputType;
	}

	public void setInputType(String inputType) {
		this.inputType = inputType;
	}
		
	public long getTypeId(){
		return 5;
	}
	
	public String getName() {
		return "sortable";
	}

	public String getTitle(Locale locale) {
		return LanguageUtil.get(locale, "sortable.title");
	}

	public String getDescription(Locale locale) {
		return LanguageUtil.get(locale, "sortable.description");
	}
	
	public String getAnswerEditingAdvise(Locale locale) {
		return LanguageUtil.get(locale, "sortable.advise");
	}
	
	public String getURLEdit(){
		return "/html/execactivity/test/admin/editAnswerSortable.jsp";
	}
	
	public String getURLNew(){
		return "/html/execactivity/test/admin/popups/sortable.jsp";
	}
	
	public boolean correct(ActionRequest actionRequest, long questionId){
		List<TestAnswer> testAnswers = new ArrayList<TestAnswer>();
		try {
			testAnswers.addAll(TestAnswerLocalServiceUtil.getTestAnswersByQuestionId(questionId));
		} catch (SystemException e) {
			e.printStackTrace();
		}

		List<Long> answersId = new ArrayList<Long>();
		long[] answers = ParamUtil.getLongValues(actionRequest, "question_"+questionId+"_ans");
		for(long id:answers){
			answersId.add(id);
		}

		if(!isCorrect(answersId, testAnswers)) return false;
		return true;
	}
	
	protected boolean isCorrect(List<Long> answersId, List<TestAnswer> testAnswers){
		for(int i=0;i<testAnswers.size();i++) 
			if(answersId.get(i) == -1 || answersId.get(i) != testAnswers.get(i).getAnswerId())	return false;
		return true;
	}
	
	public String getHtmlView(long questionId, ThemeDisplay themeDisplay, Document document){
		return getHtml(document,questionId,false,themeDisplay);
	}
	
	public Element getResults(ActionRequest actionRequest, long questionId){
		List<TestAnswer> testAnswers = new ArrayList<TestAnswer>();
		try {
			testAnswers.addAll(TestAnswerLocalServiceUtil.getTestAnswersByQuestionId(questionId));
		} catch (SystemException e) {
			e.printStackTrace();
		}

		List<Long> answersId = new ArrayList<Long>();
		long[] answers = ParamUtil.getLongValues(actionRequest, "question_"+questionId+"_ans");
		for(long id:answers){
			answersId.add(id);
		}

		Element questionXML=SAXReaderUtil.createElement("question");
		questionXML.addAttribute("id", Long.toString(questionId));
		
		long currentQuestionId = ParamUtil.getLong(actionRequest, "currentQuestionId");
		if (currentQuestionId == questionId) {
			questionXML.addAttribute("current", "true");
		}

		for(long answer:answersId){
			Element answerXML=SAXReaderUtil.createElement("answer");
			answerXML.addAttribute("id", Long.toString(answer));
			questionXML.add(answerXML);
		}
		return questionXML;
	}
	
	private String getHtml(Document document, long questionId, boolean feedback, ThemeDisplay themeDisplay){
		String html = "", showCorrectAnswer="false";
		String namespace = themeDisplay != null ? themeDisplay.getPortletDisplay().getNamespace() : "";
		try {
			TestQuestion question = TestQuestionLocalServiceUtil.fetchTestQuestion(questionId);
			List<TestAnswer> answersSelected=getAnswerSelected(document, questionId);
			List<TestAnswer> tA= TestAnswerLocalServiceUtil.getTestAnswersByQuestionId(question.getQuestionId());
			List<Long>answersSelectedIds = new ArrayList<Long>();
			for(TestAnswer ans:answersSelected)
				answersSelectedIds.add(ans.getAnswerId());
			
			ArrayList<TestAnswer> testAnswers = new ArrayList<TestAnswer>();
			testAnswers.addAll(tA);
			Collections.shuffle(testAnswers);

			String correctionClass = "";
			if(feedback){
				showCorrectAnswer = LearningActivityLocalServiceUtil.getExtraContentValue(question.getActId(), "showCorrectAnswer");
				if(isCorrect(answersSelectedIds, tA)) correctionClass = " correct";
				else correctionClass = " incorrect";
			}

			html += "<div id=\"id"+questionId+"\" class=\"question sortable" + correctionClass + " questiontype_" + getName() + " question_" + getName() + " questiontype_" + getTypeId() + "\">"+
						"<input type=\"hidden\" name=\""+themeDisplay.getPortletDisplay().getNamespace()+"question\" value=\"" + question.getQuestionId() + "\"/>"+
						"<div class=\"questiontext\">" + question.getText() + "</div>" +
						"<div class=\"content_answer\">"+
							"<ul class=\"sortable\" id=\"question_"+question.getQuestionId() + "\" >";
	
								List<TestAnswer> answers = testAnswers;
								if(answersSelected != null && answersSelected.size()>0) answers = answersSelected;
								for(int i=0;i<answers.size();i++){
									html += "<li class=\"ui-sortable-default\" id=\""+answers.get(i).getAnswerId()+"\">"+
												"<input type=\"hidden\" name=\""+namespace+"question_" + question.getQuestionId()+"_ans\"  value=\""+answers.get(i).getAnswerId()+"\"/>"+
												"<div class=\"answer ui-corner-all\">"+ answers.get(i).getAnswer() + "</div>" +
											"</li> ";
									if("true".equals(showCorrectAnswer)) {
										html += "<div class=\" font_14 color_cuarto negrita\">" + tA.get(i).getAnswer() + "</div>";
									}
								}
			
				html += 	"</ul>"+
						"</div>";
			html += "</div>";

		} catch (SystemException e) {
			e.printStackTrace();
		}


		return html;
	}
	
	public String getHtmlFeedback(Document document,long questionId, ThemeDisplay themeDisplay){

		return getHtml(document, questionId, true, themeDisplay);
	}
	
	protected List<TestAnswer> getAnswerSelected(Document document,long questionId){
		List<TestAnswer> answerSelected = new ArrayList<TestAnswer>();
		if (document != null) {
			Iterator<Element> nodeItr = document.getRootElement().elementIterator();
			while(nodeItr.hasNext()) {
				Element element = nodeItr.next();
		         if("question".equals(element.getName()) && questionId == Long.valueOf(element.attributeValue("id"))){
		        	 Iterator<Element> elementItr = element.elementIterator();
		        	 while(elementItr.hasNext()) {
		        		 Element elementElement = elementItr.next();
		        		 if("answer".equals(elementElement.getName())) {
		        			 try {
								answerSelected.add(TestAnswerLocalServiceUtil.getTestAnswer(Long.valueOf(elementElement.attributeValue("id"))));
							} catch (NumberFormatException e) {
								e.printStackTrace();
							} catch (PortalException e) {
								e.printStackTrace();
							} catch (SystemException e) {
								e.printStackTrace();
							}
		        		 }
		        	 }
		         }
		    }
		}
		return answerSelected;
	}
	
	public int getMaxAnswers(){
		return 1000;
	}
	public int getDefaultAnswersNo(){
		return GetterUtil.getInteger(PropsUtil.get("lms.defaultAnswersNo.sortable"), 2);
	}
	
}
