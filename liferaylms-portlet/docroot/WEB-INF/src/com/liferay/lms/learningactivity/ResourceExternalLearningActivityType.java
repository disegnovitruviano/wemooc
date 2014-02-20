package com.liferay.lms.learningactivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import com.liferay.lms.asset.ResourceExternalAssetRenderer;
import com.liferay.lms.model.LearningActivity;
import com.liferay.lms.service.ClpSerializer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.upload.UploadRequest;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.DocumentException;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.portal.model.PortletConstants;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceContextFactory;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.asset.model.AssetEntry;
import com.liferay.portlet.asset.model.AssetRenderer;
import com.liferay.portlet.asset.service.AssetEntryLocalServiceUtil;
import com.liferay.portlet.documentlibrary.model.DLFileEntry;
import com.liferay.portlet.documentlibrary.model.DLFolder;
import com.liferay.portlet.documentlibrary.model.DLFolderConstants;
import com.liferay.portlet.documentlibrary.service.DLAppLocalServiceUtil;

public class ResourceExternalLearningActivityType extends BaseLearningActivityType 
{
	private static final long serialVersionUID = 346346367722124L;

	private static Log log = LogFactoryUtil.getLog(ResourceExternalLearningActivityType.class);
	
	public final static int DEFAULT_FILENUMBER = 5;
	
	public static String DOCUMENTLIBRARY_MAINFOLDER = "ResourceUploads";
	public static String PORTLET_ID = 
			PortalUtil.getJsSafePortletId(
					"resourceExternalActivity" + PortletConstants.WAR_SEPARATOR + ClpSerializer.getServletContextName());
	
	@Override
	public boolean gradebook() {
		return false;
	}


	@Override
	public long getDefaultScore() {
		return 0;
	}


	@Override
	public String getName() {
		
		return "learningactivity.external";
	}


	@Override
	public AssetRenderer getAssetRenderer(LearningActivity learningactivity) {
		
		return new ResourceExternalAssetRenderer(learningactivity);
	}


	@Override
	public long getTypeId() {
		return 2;
	}
	
	@Override
	public String getExpecificContentPage() {
		return "/html/resourceExternalActivity/admin/edit.jsp";
	}
	
	@Override
	public boolean hasEditDetails() {
		return false;
	}

	
	@Override
	public void setExtraContent(UploadRequest uploadRequest, PortletResponse portletResponse, LearningActivity learningActivity) {
		/**
		 * 	Todo esto te viene a continuación te puede resultar un poco confuso, pero el desarrollo siguiente debe ser compatible 
		 *  con otras configuraciones de esta actividad.
		 */
		ThemeDisplay themeDisplay = (ThemeDisplay) uploadRequest.getAttribute(WebKeys.THEME_DISPLAY);
			PortletRequest portletRequest = (PortletRequest)uploadRequest.getAttribute(
					JavaConstants.JAVAX_PORTLET_REQUEST);
			String youtubecode=ParamUtil.getString(uploadRequest,"youtubecode");
			//String additionalFile = uploadRequest.getFileName("additionalFile");
			//boolean deleteVideo=ParamUtil.getBoolean(uploadRequest, "deleteAdditionalFile",false);
			String team = ParamUtil.getString(uploadRequest, "team","0");
			long teamId = 0;
			if(!team.equalsIgnoreCase("0")&&!team.isEmpty()){
				teamId = Long.parseLong(team);
			}
			
			Integer maxfile = DEFAULT_FILENUMBER;
			try{
				maxfile = Integer.valueOf(PropsUtil.get("lms.learningactivity.maxfile"));
			}catch(NumberFormatException nfe){
			}
			
			List<Integer> files = new ArrayList<Integer>();
			for(int i=0;i<=maxfile;i++){
				String param = "additionalFile";
				if(i > 0){
					param = param + (i-1);
				}
				
				String fileName = uploadRequest.getFileName(param);
				if(fileName!=null&&!"".equals(fileName)){
					files.add(i);
				}else{
					fileName = ParamUtil.getString(uploadRequest, param, null);
					if(fileName!=null&&!"".equals(fileName)){
						files.add(i);
					}
				}
			}
			
			if((!StringPool.BLANK.equals(youtubecode.trim())) || files.size()>0 || (!StringPool.BLANK.equals(team)) ){
				
				Document document = null;
				Element rootElement = null;
				if((learningActivity.getExtracontent()==null)||(learningActivity.getExtracontent().trim().length()==0)){
					document = SAXReaderUtil.createDocument();
					rootElement = document.addElement("multimediaentry");
				}else{
					try {
						document=SAXReaderUtil.read(learningActivity.getExtracontent());
					} catch (DocumentException e) {
						if(log.isDebugEnabled())e.printStackTrace();
						if(log.isErrorEnabled())log.error(e.getMessage());
					}
					rootElement =document.getRootElement();
				}
				
				Element video=rootElement.element("video");
				if(video!=null)
				{
					video.detach();
					rootElement.remove(video);
				}
				
				if(!StringPool.BLANK.equals(youtubecode.trim())){
					video = SAXReaderUtil.createElement("video");
					video.setText(youtubecode);		
					rootElement.add(video);
				}
				
				if(files.size()>0){
					List<Element> elements = new ArrayList<Element>(); 
					List<Element> createelements = new ArrayList<Element>(); 
					
					long repositoryId = DLFolderConstants.getDataRepositoryId(themeDisplay.getScopeGroupId(), DLFolderConstants.DEFAULT_PARENT_FOLDER_ID);
					long folderId = 0;
					
					try {
						folderId = createDLFolders(themeDisplay.getUserId(),repositoryId, portletRequest,learningActivity.getActId());
					} catch (PortalException e) {
						if(log.isDebugEnabled())e.printStackTrace();
						if(log.isErrorEnabled())log.error(e.getMessage());
						return;
					} catch (SystemException e) {
						if(log.isDebugEnabled())e.printStackTrace();
						if(log.isErrorEnabled())log.error(e.getMessage());
						return;
					}
					
					
					ServiceContext serviceContext = null;
					try {
						serviceContext = ServiceContextFactory.getInstance( DLFileEntry.class.getName(), portletRequest);
					} catch (PortalException e) {
						if(log.isDebugEnabled())e.printStackTrace();
						if(log.isErrorEnabled())log.error(e.getMessage());
					} catch (SystemException e) {
						if(log.isDebugEnabled())e.printStackTrace();
						if(log.isErrorEnabled())log.error(e.getMessage());
					}
					
					Element additionalDocumentElement = null;
					int j = 0;
					do{
						String documentt = "document";
						if(j>0){
							documentt = documentt+(j-1);
						}
						additionalDocumentElement=rootElement.element(documentt);
						if(additionalDocumentElement!=null){
							elements.add(additionalDocumentElement);
						}
						j++;
					}while(additionalDocumentElement!=null);
										
					j = 0;
					for(Integer i:files){
						String param = "additionalFile";
						String documentt = "document";
						if(i > 0){
							param = param + (i-1);
						}
						if(j > 0){
							documentt = documentt + (j-1);
						}
						/*if(i > 0){
							param = param + (i-1);
							int diff = 0;
							if(i!=j&&i>j){
								diff=i-j;
							}
							documentt = documentt + (i-(1+diff));
							if(log.isDebugEnabled())log.debug(" --"+diff+"-->"+i+"j"+j);
						}*/
						if(log.isDebugEnabled())log.debug("AddElement:"+documentt);
						
						String fileName = uploadRequest.getFileName(param);
						if(fileName!=null&&!"".equals(fileName)){
							FileEntry dlDocument = null;
							try {
								dlDocument = DLAppLocalServiceUtil.addFileEntry(
								          themeDisplay.getUserId(), repositoryId , folderId , uploadRequest.getFileName(param), uploadRequest.getContentType(param), 
								          uploadRequest.getFileName(param), StringPool.BLANK, StringPool.BLANK, uploadRequest.getFile(param) , serviceContext );
							} catch (PortalException e) {
								if(log.isDebugEnabled())e.printStackTrace();
								if(log.isErrorEnabled())log.error(e.getMessage());
								continue;
							} catch (SystemException e) {
								if(log.isDebugEnabled())e.printStackTrace();
								if(log.isErrorEnabled())log.error(e.getMessage());
								continue;
							}
							Element element=SAXReaderUtil.createElement(documentt);
							try {
								element.addAttribute("id",String.valueOf(AssetEntryLocalServiceUtil.getEntry(DLFileEntry.class.getName(), dlDocument.getPrimaryKey()).getEntryId()));
							} catch (PortalException e) {
								if(log.isDebugEnabled())e.printStackTrace();
								if(log.isErrorEnabled())log.error(e.getMessage());
								continue;
							} catch (SystemException e) {
								if(log.isDebugEnabled())e.printStackTrace();
								if(log.isErrorEnabled())log.error(e.getMessage());
								continue;
							}
							createelements.add(element);
						}else{
							fileName = ParamUtil.getString(uploadRequest, param, null);
							if(fileName!=null&&!"".equals(fileName)){
								Element element=SAXReaderUtil.createElement(documentt);
								element.addAttribute("id",fileName);
								createelements.add(element);
							}
						}
						
						j++;
					}

					for(Element element : elements){
						boolean find = false;
						for(Element celement : createelements){
							if(element.attribute("id").getStringValue().equals(celement.attribute("id").getStringValue())){
								find = true;
							}
						}
						if(!find){
							AssetEntry videoAsset;
							try {
								videoAsset = AssetEntryLocalServiceUtil.getAssetEntry(Long.parseLong(element.attributeValue("id")));
								FileEntry videofile=DLAppLocalServiceUtil.getFileEntry(videoAsset.getClassPK());
								DLAppLocalServiceUtil.deleteFileEntry(videofile.getFileEntryId());
							} catch (NumberFormatException e) {
								if(log.isDebugEnabled())e.printStackTrace();
								if(log.isErrorEnabled())log.error(e.getMessage());
							} catch (PortalException e) {
								if(log.isDebugEnabled())e.printStackTrace();
								if(log.isErrorEnabled())log.error(e.getMessage());
							} catch (SystemException e) {
								if(log.isDebugEnabled())e.printStackTrace();
								if(log.isErrorEnabled())log.error(e.getMessage());
							}
						}
						element.detach();
						rootElement.remove(element);
					}

					for(Element element : createelements){
						if(log.isDebugEnabled())log.debug("AddElement:"+element.getName());
						rootElement.add(element);
					}
				}else{
					//Delete all
					for(int i=0;i<maxfile;i++){
						String documentt = "document";
						if(i > 0){
							documentt = documentt + (i-1);
						}
						Element element = rootElement.element(documentt);
						if(element!=null){
							try {
								AssetEntry videoAsset = AssetEntryLocalServiceUtil.getAssetEntry(Long.parseLong(element.attributeValue("id")));
								FileEntry videofile=DLAppLocalServiceUtil.getFileEntry(videoAsset.getClassPK());
								DLAppLocalServiceUtil.deleteFileEntry(videofile.getFileEntryId());
							} catch (NumberFormatException e) {
								if(log.isDebugEnabled())e.printStackTrace();
								if(log.isErrorEnabled())log.error(e.getMessage());
							} catch (PortalException e) {
								if(log.isDebugEnabled())e.printStackTrace();
								if(log.isErrorEnabled())log.error(e.getMessage());
							} catch (SystemException e) {
								if(log.isDebugEnabled())e.printStackTrace();
								if(log.isErrorEnabled())log.error(e.getMessage());
							}
							
							element.detach();
							rootElement.remove(element);
						}
					}
				}
				
				if(!StringPool.BLANK.equals(team)){
					Element teamElement=rootElement.element("team");
					if(teamElement!=null){
						teamElement.detach();
						rootElement.remove(teamElement);
					}
					
					if(teamId!=0){
						teamElement = SAXReaderUtil.createElement("team");
						teamElement.setText(Long.toString(teamId));
						rootElement.add(teamElement);
					}
				}
				try {
					learningActivity.setExtracontent(document.formattedString());
				} catch (IOException e) {
					if(log.isDebugEnabled())e.printStackTrace();
					if(log.isErrorEnabled())log.error(e.getMessage());
				}	
			}
	}
	
	private long createDLFolders(Long userId,Long repositoryId,PortletRequest portletRequest,long actId) throws PortalException, SystemException{
		//Variables for folder ids
		Long dlMainFolderId = 0L;
		//Search for folder in Document Library
        boolean dlMainFolderFound = false;
        //Get main folder
        try {
        	//Get main folder
        	Folder dlFolderMain = DLAppLocalServiceUtil.getFolder(repositoryId,DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,DOCUMENTLIBRARY_MAINFOLDER+actId);
        	dlMainFolderId = dlFolderMain.getFolderId();
        	dlMainFolderFound = true;
        	//Get portlet folder
        } catch (Exception ex){
        }
        
		ServiceContext serviceContext= ServiceContextFactory.getInstance( DLFolder.class.getName(), portletRequest);
		//Damos permisos al archivo para usuarios de comunidad.
		serviceContext.setAddGroupPermissions(true);
        
        //Create main folder if not exist
        if(!dlMainFolderFound){
        	Folder newDocumentMainFolder = DLAppLocalServiceUtil.addFolder(userId, repositoryId,DLFolderConstants.DEFAULT_PARENT_FOLDER_ID, DOCUMENTLIBRARY_MAINFOLDER+actId, DOCUMENTLIBRARY_MAINFOLDER+actId, serviceContext);
        	dlMainFolderFound = true;
        	dlMainFolderId = newDocumentMainFolder.getFolderId();
        }
        //Create portlet folder if not exist
        return dlMainFolderId;
	}
	
	@Override
	public String getDescription() {
		return "learningactivity.external.helpmessage";
	}
	
	@Override
	public String getPortletId() {
		return PORTLET_ID;
	}

	@Override
	public boolean hasDeleteTries() {
		return true;
	}
}
