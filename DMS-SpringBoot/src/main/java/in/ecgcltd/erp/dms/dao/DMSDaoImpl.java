package in.ecgcltd.erp.dms.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.Document;
import org.springframework.stereotype.Repository;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;

import in.ecgcltd.erp.dms.util.MongoClientLoader;



@Repository
public class DMSDaoImpl implements DMSDao {

	// upload file
	public void uploadFile(String mappingId, FileInputStream source, String mimeType, String fileExtension, String uploadedBy, String moduleName, String fileName){
		System.out.println("mappingId "+mappingId);
		
		MongoClient client = MongoClientLoader.getInstance().getMongoClient();
		MongoDatabase database = client.getDatabase("video");
		GridFSBucket bucket = GridFSBuckets.create(database, "ecgc");
		int versionId = 0;
		GridFSFindIterable iterator =  bucket.find(
				new Document("metadata.mappingId",mappingId)).
				sort(new Document("metadata.uploadDate", -1)).limit(1);
		for(GridFSFile fs : iterator){
			int vId = fs.getMetadata().getInteger("version");
			versionId = vId;
		}

		GridFSUploadOptions gridFSUploadOptions = new GridFSUploadOptions().metadata(new Document("mappingId", mappingId).
				append("module_name", moduleName).append("uploadDate", new Date()).append("uploadedBy", uploadedBy).append("MIMEType", mimeType)
				.append("fileext", fileExtension).append("version", (versionId+1)));
		
		bucket.uploadFromStream(fileName, source, gridFSUploadOptions);
		
	}
	
	
	// list all versions
	public void getFileAllVersions(String mappingId, String path) throws IOException{
		MongoClient client = MongoClientLoader.getInstance().getMongoClient();
		MongoDatabase database = client.getDatabase("video");
		GridFSBucket bucket = GridFSBuckets.create(database, "ecgc");
		int sortDirection = 1;
		GridFSFindIterable iterator =  bucket.find(new Document("metadata.mappingId",mappingId)).sort(new Document("metadata.uploadDate", sortDirection));
		String zipFileName = path+System.getProperty("file.separator")+"xyz.zip";
		FileOutputStream zippedFileOutputStream = new FileOutputStream(zipFileName);
		ZipOutputStream zos = new ZipOutputStream(zippedFileOutputStream);
		for(GridFSFile fs : iterator){
			 
			//BsonString id = (BsonString)fs.getId();
			BsonValue id = fs.getId();
			String fileFormat = fs.getMetadata().getString("fileext");
			//String fileName = fs.getFilename()+"_"+id.getValue();
			String fileName = fs.getMetadata().getInteger("version")+"_"+fs.getFilename();
			zos.putNextEntry(new ZipEntry(fileName));
			String filePath = path+System.getProperty("file.separator")+fileName+"."+fileFormat;
			File file = new File(filePath);	
			FileOutputStream stream = new FileOutputStream(file);
			//bucket.downloadToStream(id, new FileOutputStream(new File(path+"\\"+fileName+".pdf")));
			bucket.downloadToStream(id, stream);
			// file size cannot exceed 2GB for the following method to work.
			byte [] bytes = Files.readAllBytes(Paths.get(filePath));
			zos.write(bytes, 0, bytes.length);
			zos.closeEntry();			
			stream.close();
			file.delete();
		}
		//System.out.println(System.getProperty("user.dir"));
		zos.close();
		zippedFileOutputStream.close();
		
	}

	
	// retrieve latest/oldest version
	public String getFileInstance(String mappingId, String path, boolean isLatest) throws IOException{
		MongoClient client = MongoClientLoader.getInstance().getMongoClient();
		MongoDatabase database = client.getDatabase("video");
		GridFSBucket bucket = GridFSBuckets.create(database, "ecgc");
		int sortDirection = 1;
		if(isLatest){
			sortDirection = -1;
		}
		String MIMEType = "";
		GridFSFindIterable iterator =  bucket.find(new Document("metadata.mappingId",mappingId)).sort(new Document("metadata.uploadDate", sortDirection)).limit(1);
		for(GridFSFile fs : iterator){
			//BsonString id = (BsonString)fs.getId();
			BsonValue id = fs.getId();
			System.out.println(id);
			String fileFormat = fs.getMetadata().getString("fileext");
			String fileName = fs.getFilename();			
			MIMEType = fs.getMetadata().getString("MIMEType");
			String filePath = path+System.getProperty("file.separator")+fileName+"."+fileFormat;
			File file = new File(filePath);	
			FileOutputStream stream = new FileOutputStream(file);						
			bucket.downloadToStream(id, stream);
			stream.close();
		}
		return MIMEType;
	}
	//
	
	public static void main(String []args) throws IOException{
		DMSDaoImpl impl = new DMSDaoImpl();
		String path = System.getProperty("user.home")+System.getProperty("file.separator")+"testdms";
		//impl.getFileAllVersions("1", path);
		impl.getFileInstance("1", path, false);
		//System.out.println(System.getProperty("user.home")+System.getProperty("file.separator")+"testdms");
	}

}
