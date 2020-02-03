package in.ecgcltd.erp.dms.dao;

import java.io.FileInputStream;
import java.io.IOException;

public interface DMSDao {

	public String getFileInstance(String mappingId, String path, boolean isLatest) throws IOException;
	
	public void uploadFile(String mappingId, FileInputStream source, String mimeType, String fileExtension, String uploadedBy, String moduleName, String fileName);
	
	public void getFileAllVersions(String mappingId, String path) throws IOException;
}
