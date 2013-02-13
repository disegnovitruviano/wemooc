package com.liferay.lms;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.portlet.ActionRequest;
import org.apache.commons.fileupload.FileItem;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceContextFactory;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.documentlibrary.service.DLAppLocalServiceUtil;


import com.liferay.lms.model.Module;

/**
 * Upload implementation class module
 */
public class moduleUpload {

	public static String HIDDEN = "HIDDEN";
	public static String SEPARATOR = "_";

	public static String IMAGEGALLERY_REQUESTFOLDER = HIDDEN+SEPARATOR+"folderIGId";
	public static String DOCUMENTLIBRARY_REQUESTFOLDER = HIDDEN+SEPARATOR+"folderDLId";

	public static String IMAGEFILE = "IMAGEFILE";
	public static String IMAGEGALLERY_MAINFOLDER = "PortletUploads";
	public static String IMAGEGALLERY_PORTLETFOLDER = "module";
	public static String IMAGEGALLERY_MAINFOLDER_DESCRIPTION = "Portlet Image Uploads";
	public static String IMAGEGALLERY_PORTLETFOLDER_DESCRIPTION = "";
	public static String IMAGE_DELETE = "DELETEIMAGE";

	public static String DOCUMENTFILE = "DOCUMENTFILE";
	public static String DOCUMENTLIBRARY_MAINFOLDER = "PortletUploads";
	public static String DOCUMENTLIBRARY_PORTLETFOLDER = "module";
	public static String DOCUMENTLIBRARY_MAINFOLDER_DESCRIPTION = "Portlet Document Uploads";
	public static String DOCUMENTLIBRARY_PORTLETFOLDER_DESCRIPTION = "";
	public static String DOCUMENT_DELETE = "DELETEDOCUMENT";

	private Long igFolderId = 0L;
	private Long dlFolderId = 0L;

	private List<FileItem> files = null;
	private HashMap hiddens = null;
	private HashMap deleteds = null;

	public moduleUpload() {
		init();
	}

	private void init(){
		files = new ArrayList<FileItem>();
		hiddens = new HashMap();
		deleteds = new HashMap();
		igFolderId = 0L;
		dlFolderId = 0L;
	}

	public void add(FileItem item) {
		if(files==null) files = new ArrayList<FileItem>();
		files.add(item);
	}

	public void addHidden(String formField, Long value){
		if(hiddens==null) hiddens = new HashMap();
		//Check if Hidden folders
		if (formField.equalsIgnoreCase(IMAGEGALLERY_REQUESTFOLDER)){
			if((value!=null)&& (value!=0L)) igFolderId = value;
		} else if (formField.equalsIgnoreCase(DOCUMENTLIBRARY_REQUESTFOLDER)){
			if((value!=null)&& (value!=0L)) dlFolderId = value;
		} else {
			hiddens.put(formField, value);
		}
	}

	public void addDeleted(String formField) {
		if(deleteds==null) deleteds = new HashMap();
		deleteds.put(formField,new Boolean("true"));
	}

	public Module uploadFiles(ActionRequest request,Module module) throws PortalException, SystemException, IOException, SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException{

		ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
		User defaultuser = themeDisplay.getCompany().getDefaultUser();
		
        Long userId = Long.parseLong(request.getRemoteUser());
    	User user = UserLocalServiceUtil.getUserById(userId);
    	Long groupId = UserLocalServiceUtil.getUser(userId).getGroup().getGroupId();

		ServiceContext igServiceContext = null;
		ServiceContext dlServiceContext = null;

		for(FileItem item : files) {
			String formField = item.getFieldName();
			String strType = formField.substring(formField.lastIndexOf(SEPARATOR)+1);
			if(strType.equalsIgnoreCase(IMAGEFILE)){
				formField = getFieldFromAttribute(extractSufix(IMAGEFILE,formField));
				if(deleteds.get(formField)!=null) {
					if(hiddens!=null) {
						Long prevImage = (Long)hiddens.get(HIDDEN+SEPARATOR+formField);
						if((prevImage!=null)&&(prevImage!=0L)) {
							DLAppLocalServiceUtil.deleteFileEntry(prevImage);
						}
					}
				} else if(!item.getName().equals("")){
					if(igServiceContext == null){
						igServiceContext = createServiceContext(request,FileEntry.class.getName(),userId,groupId);
					}
					if(igFolderId==0L){
						createIGFolders(request,userId,groupId,igServiceContext);
					}
					
					String  contentType= MimeTypesUtil.getContentType(item.getName());
					FileEntry igImage = DLAppLocalServiceUtil.addFileEntry(userId, groupId, igFolderId, item.getName(), contentType, item.getName(), item.getName(), "", item.getInputStream(),item.getSize(), igServiceContext);
					callSetMethod(formField,module,igImage.getFileEntryId());
					//Check possible previous values
					if(hiddens!=null){
						Long prevImage = (Long)hiddens.get(HIDDEN+SEPARATOR+formField);
						if((prevImage!=null) && (prevImage!=0L)){
							//Delete previous image
							DLAppLocalServiceUtil.deleteFileEntry(prevImage);
						}
					}
				} else {
					//See hidden value, possible edit
					if(hiddens!=null){
						Long prevImage = (Long)hiddens.get(HIDDEN+SEPARATOR+formField);
						if((prevImage!=null)&&(prevImage!=0L)){
							callSetMethod(formField,module,(Long)hiddens.get(HIDDEN+SEPARATOR+formField));
						}
					}

				}
			}else if(strType.equalsIgnoreCase(DOCUMENTFILE)) {
				formField = getFieldFromAttribute(extractSufix(DOCUMENTFILE,formField));
				if(deleteds.get(formField)!=null){
					Long prevDocument = (Long)hiddens.get(HIDDEN+SEPARATOR+formField);
					if((prevDocument!=null)&&(prevDocument!=0L)) {
						DLAppLocalServiceUtil.deleteFileEntry(prevDocument);
					}
				} else if(!item.getName().equals("")){
					if(dlServiceContext == null){
						dlServiceContext = createServiceContext(request,FileEntry.class.getName(),userId,groupId);
					}
					if(dlFolderId==0L) {
						
						createDLFolders(request,userId,groupId,dlServiceContext);
					}
					FileEntry dlDocument = DLAppLocalServiceUtil.addFileEntry(userId, groupId, dlFolderId, item.getName(), item.getName(), item.getName(), "", "", item.getInputStream(),item.getSize(),dlServiceContext);
					callSetMethod(formField,module,dlDocument.getFileEntryId());
					//Check possible previous values
					if(hiddens!=null){
						Long prevDocument = (Long)hiddens.get(HIDDEN+SEPARATOR+formField);
						if((prevDocument!=null)&&(prevDocument!=0L)){
							//Delete previous document
							DLAppLocalServiceUtil.deleteFileEntry(prevDocument);
						}
					}
				} else {
					//See hidden value, possible edit
					if(hiddens!=null){
						Long prevDocument = (Long)hiddens.get(HIDDEN+SEPARATOR+formField);
						if((prevDocument!=null)&&(prevDocument!=0L)) {
							callSetMethod(formField,module,(Long)hiddens.get(HIDDEN+SEPARATOR+formField));
						}
					}
				}
			}
		}
		return module;
	}

	public void deleteFiles() throws PortalException, SystemException{
		if(igFolderId!=0L) {
			DLAppLocalServiceUtil.deleteFolder(igFolderId);
		}
		if(dlFolderId!=0L) {
			DLAppLocalServiceUtil.deleteFolder(dlFolderId);
		}
	}

	private void callSetMethod(String formField, Module module, Long value) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		String strMethod = "set"+StringUtil.upperCaseFirstLetter(formField);
		Method methodSet = module.getClass().getMethod(strMethod,long.class);
		methodSet.invoke(module, value);
	}

	public Long getIgFolderId() {
		return igFolderId;
	}

	public void setIgFolderId(Long igFolderId) {
		this.igFolderId = igFolderId;
	}

	public Long getDlFolderId() {
		return dlFolderId;
	}

	public void setDlFolderId(Long dlFolderId) {
		this.dlFolderId = dlFolderId;
	}

	/**
	 * Create a serviceContext with given arguments
	 * @param request
	 * @param className
	 * @param userId
	 * @param groupId
	 * @return
	 * @throws PortalException
	 * @throws SystemException
	 */
	private ServiceContext createServiceContext(ActionRequest request, String className, Long userId, Long groupId) throws PortalException, SystemException{
		ServiceContext serviceContext = ServiceContextFactory.getInstance(className, request);
        serviceContext.setAddCommunityPermissions(true);
        serviceContext.setAddGuestPermissions(true);
        serviceContext.setUserId(userId);
        serviceContext.setScopeGroupId(groupId);
        return serviceContext;
	}

	/**
	 * Create folders for upload images from our portlet to ImageGallery portlet
	 * @param request
	 * @param userId
	 * @param groupId
	 * @param serviceContext
	 * @return
	 * @throws PortalException
	 * @throws SystemException
	 */
	private void createIGFolders(ActionRequest request,Long userId,Long groupId, ServiceContext serviceContext) throws PortalException, SystemException{
		//Variables for folder ids
		Long igMainFolderId = 0L;
		Long igPortletFolderId = 0L;
		Long igRecordFolderId = 0L;
        //Search for folders
        boolean igMainFolderFound = false;
        boolean igPortletFolderFound = false;
        try {
        	//Get the main folder
        	Folder igMainFolder = DLAppLocalServiceUtil.getFolder(groupId,0,IMAGEGALLERY_MAINFOLDER);
        	igMainFolderId = igMainFolder.getFolderId();
        	igMainFolderFound = true;
        	//Get the portlet folder
        	Folder igPortletFolder = DLAppLocalServiceUtil.getFolder(groupId,igMainFolderId,IMAGEGALLERY_PORTLETFOLDER);
        	igPortletFolderId = igPortletFolder.getFolderId();
        	igPortletFolderFound = true;
        } catch (Exception ex) {
        	ex.printStackTrace(); //Not found main folder
        }
        //Create main folder if not exist
        if(!igMainFolderFound) {
        	Folder newImageMainFolder = DLAppLocalServiceUtil.addFolder(userId, 0, IMAGEGALLERY_MAINFOLDER, IMAGEGALLERY_MAINFOLDER_DESCRIPTION, serviceContext);
        	igMainFolderId = newImageMainFolder.getFolderId();
        	igMainFolderFound = true;
        }
        //Create portlet folder if not exist
        if(igMainFolderFound && !igPortletFolderFound){
        	Folder newImagePortletFolder = DLAppLocalServiceUtil.addFolder(userId, igMainFolderId, IMAGEGALLERY_PORTLETFOLDER, IMAGEGALLERY_PORTLETFOLDER_DESCRIPTION, serviceContext);
        	igPortletFolderFound = true;
        	igPortletFolderId = newImagePortletFolder.getFolderId();
        }
        //Create this record folder
        if(igPortletFolderFound){
        	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        	Date date = new Date();
        	String igRecordFolderName=dateFormat.format(date)+SEPARATOR+userId;
        	Folder newImageRecordFolder = DLAppLocalServiceUtil.addFolder(userId, igPortletFolderId,igRecordFolderName, "", serviceContext);
        	igRecordFolderId = newImageRecordFolder.getFolderId();
        }
        igFolderId = igRecordFolderId;
      }

	/**
	 * Create folders for upload documents from our portlet to DocumentLibrary portlet
	 * @param request
	 * @param userId
	 * @param groupId
	 * @param serviceContext
	 * @return
	 * @throws PortalException
	 * @throws SystemException
	 */
	private void createDLFolders(ActionRequest request,Long userId,Long groupId,ServiceContext serviceContext) throws PortalException, SystemException{
		//Variables for folder ids
		Long dlMainFolderId = 0L;
		Long dlPortletFolderId = 0L;
		Long dlRecordFolderId = 0L;
		//Search for folder in Document Library
        boolean dlMainFolderFound = false;
        boolean dlPortletFolderFound = false;
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
		User defaultuser = themeDisplay.getCompany().getDefaultUser();
        //Get main folder
        try {
        	//Get main folder
        	Folder dlFolderMain = DLAppLocalServiceUtil.getFolder(groupId,0,DOCUMENTLIBRARY_MAINFOLDER);
        	dlMainFolderId = dlFolderMain.getFolderId();
        	dlMainFolderFound = true;
        	//Get portlet folder
        	Folder dlFolderPortlet = DLAppLocalServiceUtil.getFolder(groupId,dlMainFolderId,DOCUMENTLIBRARY_PORTLETFOLDER);
        	dlPortletFolderId = dlFolderPortlet.getFolderId();
        	dlPortletFolderFound = true;
        } catch (Exception ex){
        	ex.printStackTrace();//Not found Main Folder
        }
        //Create main folder if not exist
        if(!dlMainFolderFound){
        	Folder newDocumentMainFolder = DLAppLocalServiceUtil.addFolder(defaultuser.getUserId(), groupId, 0, DOCUMENTLIBRARY_MAINFOLDER, DOCUMENTLIBRARY_MAINFOLDER_DESCRIPTION, serviceContext);
        	String[] communityPermissions = new String[]{ActionKeys.VIEW,ActionKeys.ADD_FOLDER,ActionKeys.ADD_DOCUMENT};
        	String[] guestPermissions = new String[]{};
        	DLAppLocalServiceUtil.addFolderResources(newDocumentMainFolder, communityPermissions, guestPermissions);
        	dlMainFolderId = newDocumentMainFolder.getFolderId();
        	dlMainFolderFound = true;
        }
        //Create portlet folder if not exist
        if(dlMainFolderFound && !dlPortletFolderFound){
        	
    		
        	Folder newDocumentPortletFolder = DLAppLocalServiceUtil.addFolder(defaultuser.getUserId(), groupId, dlMainFolderId , DOCUMENTLIBRARY_PORTLETFOLDER, DOCUMENTLIBRARY_PORTLETFOLDER_DESCRIPTION, serviceContext);
        	String[] communityPermissions = new String[]{ActionKeys.VIEW,ActionKeys.ADD_FOLDER,ActionKeys.ADD_DOCUMENT};
        	String[] guestPermissions = new String[]{};
        	DLAppLocalServiceUtil.addFolderResources(newDocumentPortletFolder, communityPermissions, guestPermissions);
        	dlPortletFolderFound = true;
            dlPortletFolderId = newDocumentPortletFolder.getFolderId();
        }

        //Create this record folder
        if(dlPortletFolderFound){
        	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        	Date date = new Date();
        	String dlRecordFolderName = dateFormat.format(date)+SEPARATOR+userId;
        	Folder newDocumentRecordFolder = DLAppLocalServiceUtil.addFolder(userId, groupId, dlPortletFolderId, dlRecordFolderName, dlRecordFolderName, serviceContext);
        	String[] communityPermissions = new String[]{ActionKeys.VIEW};
        	String[] guestPermissions = new String[]{};
        	DLAppLocalServiceUtil.addFolderResources(newDocumentRecordFolder, communityPermissions, guestPermissions);
        	dlRecordFolderId = newDocumentRecordFolder.getFolderId();
        }
        dlFolderId = dlRecordFolderId;
	}

	/**
	  * Extract a given sufix from a String
	  * This method loof for sufix, and then, substring the rest to the left.
	  * Posible last char = "_" deleted
	  * @param sufix
	  * @param itemName
	  * @return
	  */
	private String extractSufix (String sufix, String itemName){
		String result = itemName;
			if(itemName!=null && sufix!=null){
				int lastPos  = itemName.lastIndexOf(sufix);
				result = itemName.substring(0,lastPos);
				//Delete posible "_" char
				if(result.substring(result.length()-1,result.length()).equals("_")) {
					result = result.substring(0,result.length()-1);
				}
			}
		return result;
	}

	/**
	 * Get the field string from the attribute string
	 * @param attribute
	 * @return
	 */
	private String getFieldFromAttribute(String attribute){
		return attribute.substring(attribute.lastIndexOf(SEPARATOR)+1);
	}
}
