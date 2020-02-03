package in.ecgcltd.erp.dms.service;

import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import in.ecgcltd.erp.dms.dao.DMSDao;



@Service
public class DMSServiceImpl implements DMSService{

	@Autowired
	private DMSDao dao;
	
	public String getFileInstance(String mappingId, String path, boolean isLatest) throws IOException {
		// TODO Auto-generated method stub
		return dao.getFileInstance(mappingId, path, isLatest);
	}

	public void uploadFile(String mappingId, FileInputStream source, String mimeType, String fileExtension, String uploadedBy, String moduleName, String fileName) {
		// TODO Auto-generated method stub
		dao.uploadFile(mappingId, source, mimeType, fileExtension, uploadedBy, moduleName, fileName);
	}

	public void getFileAllVersions(String mappingId, String path) throws IOException {
		// TODO Auto-generated method stub
		dao.getFileAllVersions(mappingId, path);
	}

}
