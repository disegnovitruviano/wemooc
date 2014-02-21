package com.liferay.lms.learningactivity.questiontype;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import javax.portlet.ActionRequest;

import org.apache.commons.io.IOUtils;

import com.liferay.lms.lar.ModuleDataHandlerImpl;
import com.liferay.lms.model.LearningActivity;
import com.liferay.lms.model.TestAnswer;
import com.liferay.lms.service.TestAnswerLocalService;
import com.liferay.lms.service.TestAnswerLocalServiceUtil;
import com.liferay.lms.service.TestQuestionLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.lar.PortletDataContext;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.documentlibrary.DuplicateFileException;
import com.liferay.portlet.documentlibrary.model.DLFolderConstants;
import com.liferay.portlet.documentlibrary.service.DLAppLocalServiceUtil;


public abstract class BaseQuestionType implements QuestionType, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static Locale locale=null;

	@Override
	public void setLocale(Locale locale){
		this.locale=locale;
	}
	
	@Override
	public long getTypeId(){
		return -1;
	}
	
	@Override
	public String getName(){
		return "";
	}
	
	@Override
	public String getTitle(Locale locale){
		return "";
	}
	
	@Override
	public String getDescription(Locale locale){
		return "";
	}
	
	@Override
	public String getAnswerEditingAdvise(Locale locale){
		return "";
	}
	
	@Override
	public String getURLEdit(){
		return "";
	}
	
	@Override
	public String getURLNew(){
		return "";
	}
	
	@Override
	public String getURLBack(){
		return "/html/execactivity/test/admin/editquestions.jsp";
	}
	
	@Override
	public void delete(long questionId) throws PortalException, SystemException{
		List<TestAnswer> answers = TestAnswerLocalServiceUtil.getTestAnswersByQuestionId(questionId);
		for(TestAnswer answer:answers )
			TestAnswerLocalServiceUtil.deleteTestAnswer(answer.getAnswerId());
		TestQuestionLocalServiceUtil.deleteTestQuestion(questionId);
	}
	
	@Override
	public boolean correct(ActionRequest actionRequest, long questionId){
		return false;
	}
	
	@Override
	public String getHtmlView(long questionId, ThemeDisplay themeDisplay, Document document){
		return "";
	}
	
	@Override
	public Element getResults(ActionRequest actionRequest, long questionId){
		return null;
	}
	
	@Override
	public String getHtmlFeedback(Document document,long questionId, ThemeDisplay themeDisplay){
		return "";
	}
	
	@Override
	public void exportQuestionAnswers(PortletDataContext context, Element root, long questionId, LearningActivity activity) throws PortalException, SystemException{
		List<TestAnswer> answers=TestAnswerLocalServiceUtil.getTestAnswersByQuestionId(questionId);
		for(TestAnswer answer:answers){
			String patha = getEntryPath(context, answer);
			Element entryElementa= root.addElement("questionanswer");
			entryElementa.addAttribute("path", patha);
			context.addZipEntry(patha, answer);
			
			//Exportar los ficheros que tiene la descripcion de la respuesta
			ModuleDataHandlerImpl m = new ModuleDataHandlerImpl();
			m.descriptionFileParserDescriptionToLar("<root><Description>"+answer.getAnswer()+"</Description></root>", activity.getGroupId(), activity.getModuleId(), context, entryElementa);	
		}
	}
	
	@Override
	public void importQuestionAnswers(PortletDataContext context, Element entryElement, long questionId, long userId, ServiceContext serviceContext) throws SystemException, PortalException{
		for(Element aElement:entryElement.elements("questionanswer")){
			String patha = aElement.attributeValue("path");
			TestAnswer oldanswer=(TestAnswer)context.getZipEntryAsObject(patha);
			TestAnswer answer = TestAnswerLocalServiceUtil.addTestAnswer(questionId, oldanswer.getAnswer(), oldanswer.getFeedbackCorrect(), oldanswer.getFeedbacknocorrect(), oldanswer.isIsCorrect());
			
			//Si tenemos ficheros en las descripciones de las respuestas.
			for (Element actElementFile : aElement.elements("descriptionfile")) {
				FileEntry oldFile = (FileEntry)context.getZipEntryAsObject(actElementFile.attributeValue("path"));
				ModuleDataHandlerImpl m = new ModuleDataHandlerImpl();
				FileEntry newFile;
				long folderId=0;
				String description = "";
				
				try {
					InputStream input = context.getZipEntryAsInputStream(actElementFile.attributeValue("file"));
					long repositoryId = DLFolderConstants.getDataRepositoryId(context.getScopeGroupId(), DLFolderConstants.DEFAULT_PARENT_FOLDER_ID);
					folderId = m.createDLFolders(userId,repositoryId,serviceContext);
					newFile = DLAppLocalServiceUtil.addFileEntry(userId, repositoryId , folderId , oldFile.getTitle(), "contentType", oldFile.getTitle(), StringPool.BLANK, StringPool.BLANK, IOUtils.toByteArray(input), serviceContext );
					description = m.descriptionFileParserLarToDescription(answer.getAnswer(), oldFile, newFile);
				} catch(DuplicateFileException dfl){
					FileEntry existingFile = DLAppLocalServiceUtil.getFileEntry(context.getScopeGroupId(), folderId, oldFile.getTitle());
					description = m.descriptionFileParserLarToDescription(answer.getAnswer(), oldFile, existingFile);
				} catch (Exception e) {
					System.out.println("* ERROR! Question descriptionfile: " + e.getMessage());
				}
				answer.setAnswer(description);
				TestAnswerLocalServiceUtil.updateTestAnswer(answer);
			}
		}
	}
	
	protected String getEntryPath(PortletDataContext context, TestAnswer answer) {
		
		StringBundler sb = new StringBundler(4);
		sb.append(context.getPortletPath("moduleportlet_WAR_liferaylmsportlet"));
		sb.append("/moduleentries/activities/questions/answers");
		sb.append(answer.getAnswerId());
		sb.append(".xml");
		return sb.toString();
	}
	
	public void importMoodle(long actId, Element question, TestAnswerLocalService testAnswerLocalService)throws SystemException, PortalException {}

	public int getMaxAnswers(){
		return -1;
	}
	public int getDefaultAnswersNo(){
		return -1;
	}
	
}
