package com.liferay.lms.lar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.portlet.PortletPreferences;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.liferay.lms.NoSuchSCORMContentException;
import com.liferay.lms.model.SCORMContent;
import com.liferay.lms.service.SCORMContentLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.lar.BasePortletDataHandler;
import com.liferay.portal.kernel.lar.PortletDataContext;
import com.liferay.portal.kernel.lar.PortletDataHandlerBoolean;
import com.liferay.portal.kernel.lar.PortletDataHandlerControl;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.portal.model.Group;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portlet.asset.model.AssetEntry;
import com.liferay.portlet.asset.service.AssetEntryLocalServiceUtil;

public class ScormDataHandlerImpl extends BasePortletDataHandler {

	private static final String _NAMESPACE = "scorm"; // mejorable??
	
	private static final boolean _ALWAYS_EXPORTABLE = true;

	private static final boolean _PUBLISH_TO_LIVE_BY_DEFAULT = true;
	
	private static PortletDataHandlerBoolean _foldersAndDocuments =
			new PortletDataHandlerBoolean(
				_NAMESPACE, "folders-and-documents", true, true);

	private static PortletDataHandlerBoolean _categories = new PortletDataHandlerBoolean(
			_NAMESPACE, "categories", true, true);

	private static PortletDataHandlerBoolean _entries = new PortletDataHandlerBoolean(
			_NAMESPACE, "entries", true, true);

	private static PortletDataHandlerBoolean _tags = new PortletDataHandlerBoolean(
			_NAMESPACE, "tags", true, true);
	
	private static PortletDataHandlerControl[] _metadataControls =
		new PortletDataHandlerControl[] {
			_categories,
			_tags
		};

	@Override
	public boolean isAlwaysExportable() {
		return _ALWAYS_EXPORTABLE;
	}
	
	@Override
	public boolean isPublishToLiveByDefault() {
		return _PUBLISH_TO_LIVE_BY_DEFAULT;
	}
	
	@Override
	public PortletDataHandlerControl[] getExportControls() {
		return new PortletDataHandlerControl[] { _entries, _foldersAndDocuments};
	}

	@Override
	public PortletDataHandlerControl[] getImportControls() {
		return new PortletDataHandlerControl[] { _entries, _foldersAndDocuments};
	}
	
	@Override
	public PortletDataHandlerControl[] getExportMetadataControls() {
		return new PortletDataHandlerControl[] {
			new PortletDataHandlerBoolean(
				_NAMESPACE, "entries", true, _metadataControls)
		};
	}

	@Override
	public PortletDataHandlerControl[] getImportMetadataControls() {
		return new PortletDataHandlerControl[] {
			new PortletDataHandlerBoolean(
				_NAMESPACE, "entries", true, _metadataControls)
		};
	}

	@Override
	protected PortletPreferences doDeleteData(PortletDataContext context,
			String portletId, PortletPreferences preferences) throws Exception {

		System.out.println("  ::: ScormDataHandlerImpl.doDeleteData ::: "
				+ portletId + " " + context.getGroupId() + " "
				+ context.getScopeGroupId());

		try {
			String groupIdStr = String.valueOf(context.getScopeGroupId());

			Group group = GroupLocalServiceUtil.getGroup(context
					.getScopeGroupId());

			long groupId = 0;

			if (Validator.isNumber(groupIdStr)) {
				groupId = Long.parseLong(groupIdStr);
			}

			System.out.println("   groupId : " + groupId + ", name: "
					+ group.getName());

			List<SCORMContent> scorms = SCORMContentLocalServiceUtil
					.getSCORMContentOfGroup(groupId);

			for (SCORMContent scorm : scorms) {

				System.out.println("    scorm : " + scorm.getScormId());

				SCORMContentLocalServiceUtil.delete(scorm.getScormId());

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("  ::: ScormDataHandlerImpl.doDeleteData ::: ends ");

		return super.doDeleteData(context, portletId, preferences);
	}

	@Override
	protected String doExportData(PortletDataContext context, String portletId,
			PortletPreferences preferences) throws Exception {

		System.out.println(" doExportData portletId : " + portletId);

		context.addPermissions("com.liferay.lms.model.SCORMContent",
				context.getScopeGroupId());

		Document document = SAXReaderUtil.createDocument();

		Element rootElement = document.addElement("scormdata");

		rootElement.addAttribute("group-id",
				String.valueOf(context.getScopeGroupId()));

		List<SCORMContent> entries = SCORMContentLocalServiceUtil
				.getSCORMContentOfGroup(context.getScopeGroupId());

		System.out.println(" entries : " + entries.size());

		for (SCORMContent entry : entries) {
			exportEntry(context, rootElement, entry);
		}

		return document.formattedString();
	}

	private void exportEntry(PortletDataContext context, Element root,
			SCORMContent entry) throws PortalException, SystemException {

		AssetEntry assetEntry = AssetEntryLocalServiceUtil.getEntry(SCORMContent.class.getName(), entry.getScormId());
		if (!context.isWithinDateRange(assetEntry.getModifiedDate())) {
			return;
		}

		if (entry.getStatus() != WorkflowConstants.STATUS_APPROVED) {
			return;
		}

		String path = getEntryPath(context, entry);

		System.out.println(" path : " + path);

		if (!context.isPathNotProcessed(path)) {
			return;
		}

		Element entryElement = root.addElement("scormentry");

		entryElement.addAttribute("path", path);

		context.addPermissions(SCORMContent.class, entry.getScormId());

		entry.setUserUuid(entry.getUserUuid());
		if (context.getBooleanParameter(_NAMESPACE, "categories")) {
			context.addAssetCategories(SCORMContent.class, entry.getScormId());
		}

		if (context.getBooleanParameter(_NAMESPACE, "comments")) {
			context.addComments(SCORMContent.class, entry.getScormId());
		}

		if (context.getBooleanParameter(_NAMESPACE, "ratings")) {
			context.addRatingsEntries(SCORMContent.class, entry.getScormId());
		}

		if (context.getBooleanParameter(_NAMESPACE, "tags")) {
			context.addAssetTags(SCORMContent.class, entry.getScormId());
		}

		// Exportamos zip
		String scormPath = SCORMContentLocalServiceUtil
				.getDirScormzipPath(entry) + "/" + entry.getUuid() + ".zip";
		String pathFile = getFilePath(context, entry.getScormId());

		entryElement.addAttribute("file", pathFile + entry.getUuid() + ".zip");

		// Guardar el fichero en el zip.
		File fileScorm = new File(scormPath);
		try {
			InputStream input = new FileInputStream(fileScorm);
			context.addZipEntry(getFilePath(context, entry.getScormId())
					+ entry.getUuid() + ".zip", input);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new SystemException(e);
		}
		context.addZipEntry(path, entry);

	}

	private String getEntryPath(PortletDataContext context,
			SCORMContent scormContent) {

		StringBundler sb = new StringBundler(4);
		sb.append(context.getPortletPath("scormadmin_WAR_liferaylmsportlet"));
		sb.append("/scormentries/");
		sb.append(scormContent.getScormId());
		sb.append(".xml");
		return sb.toString();
	}

	private String getFilePath(PortletDataContext context, long scormId) {

		StringBundler sb = new StringBundler(4);
		sb.append(context.getPortletPath("scormadmin_WAR_liferaylmsportlet"));
		sb.append("/scormentries/" + String.valueOf(scormId) + "/");
		return sb.toString();
	}

	@Override
	protected PortletPreferences doImportData(PortletDataContext context,
			String portletId, PortletPreferences preferences, String data)
			throws Exception {

		context.importPermissions("com.liferay.lms.model.SCORMContent",
				context.getSourceGroupId(), context.getScopeGroupId());

		Document document = SAXReaderUtil.read(data);

		System.out.println("import xml : \n" + data);

		Element rootElement = document.getRootElement();

		for (Element entryElement : rootElement.elements("scormentry")) {
			String path = entryElement.attributeValue("path");

			System.out.println(" entry : " + path);

			if (!context.isPathNotProcessed(path)) {
				continue;
			}
			SCORMContent entry = (SCORMContent) context
					.getZipEntryAsObject(path);

			System.out.println(" Scorm : " + entry.getScormId());

			importEntry(context, entryElement, entry);
		}

		return null;
	}

	private void importEntry(PortletDataContext context, Element entryElement,
			SCORMContent entry) throws SystemException, PortalException {
		
		long userId = context.getUserId(entry.getUserUuid());
		entry.setGroupId(context.getScopeGroupId());
		entry.setUserId(userId);

		// Long defaultCompanyId = PortalUtil.getDefaultCompanyId();
		// Long defaultGroupId =
		// GroupLocalServiceUtil.getGroup(defaultCompanyId,
		// "Guest").getGroupId();
		SCORMContent scocontent = (SCORMContent) context
				.getZipEntryAsObject(entryElement.attributeValue("path"));

		SCORMContent oldScormContent = null;
		try {
			oldScormContent = SCORMContentLocalServiceUtil
					.getSCORMContentByUuidAndGroupId(scocontent.getUuid(),
							context.getScopeGroupId());
		} catch (NoSuchSCORMContentException e) {

		}
		if (Validator.isNull(oldScormContent)) {
			ServiceContext serviceContext2 = new ServiceContext();
			serviceContext2.setAssetCategoryIds(context.getAssetCategoryIds(
					SCORMContent.class, scocontent.getScormId()));
			serviceContext2.setAssetTagNames(context.getAssetTagNames(
					SCORMContent.class, scocontent.getScormId()));
			serviceContext2.setUserId(userId);
			serviceContext2.setCompanyId(context.getCompanyId());
			serviceContext2.setScopeGroupId(context.getScopeGroupId());
			serviceContext2.setUuid(scocontent.getUuid());

			scocontent.setGroupId(context.getScopeGroupId());

			InputStream is = context.getZipEntryAsInputStream(entryElement
					.attributeValue("file"));
			try {
				byte[] dataFileScorm = IOUtils.toByteArray(is);
				File scormfile = new File(System.getProperty("java.io.tmpdir")
						+ "/scorms/" + scocontent.getUuid() + ".zip");
				FileUtils.writeByteArrayToFile(scormfile, dataFileScorm);

				// SCORMContent newScormContent =
				SCORMContentLocalServiceUtil.addSCORMContent(
						scocontent.getTitle(), scocontent.getDescription(),
						scormfile, serviceContext2);

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (is != null) {
						is.close();
					}
				} catch (IOException e1) {

				}
			}
		}
	}

}
