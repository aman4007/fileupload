package in.ecgcltd.erp.dms.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.util.*;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.MongoClientException;

import in.ecgcltd.erp.dms.exception.DMSException;
import in.ecgcltd.erp.dms.service.DMSService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/dmsrestfileupload")
@Api(value = "DMS")
public class DMSRESTController {

	
	@Autowired
	private DMSService service;

	/*@GetMapping("/showfileupload")
	public String showFileUpload(HttpServletRequest request){
		//request.getSession().setAttribute("filename", 1234);
		return "showfileupload";
	}
	
	@GetMapping("/showfiledownload")
	public String showFileDownload(){		
		return "downloadfile";
	}*/
	
	/*@RequestMapping(value = "/helloworld", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String helloWorld() {		
		return "Hello world";
	}*/
	
	//@PostMapping("/uploadfile")
	
	@RequestMapping(value = "/uploadfile", method = RequestMethod.POST)
	@ApiOperation(value = "To upload a document on DMS", httpMethod = "POST", consumes = MediaType.MULTIPART_FORM_DATA_VALUE )
	@ApiResponses(
			{
				@ApiResponse(code = 201, message = "Document uploaded"),
				@ApiResponse(code = 500, message = "Internal Server error")
			}
			)
	public ResponseEntity uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request, 
			@RequestParam("mappingId") String mappingId, @RequestParam("moduleId") String moduleId, @RequestParam("uploadedBy") String uploadedBy){
		
		
		if(!file.isEmpty()){
			String absPath = request.getServletContext().getRealPath("/");
			System.out.println(absPath);
			try {
				String fileName = file.getOriginalFilename();				
				byte [] bytes = file.getBytes();
				System.out.println(bytes.length);
				String path = absPath+File.separator+fileName;
				System.out.println("upload path: "+path);
				File serverFile = new File(path);
		
				//String mimeType = URLConnection.guessContentTypeFromName(serverFile.getName());
				String mimeType = file.getContentType();
				int extIndex = path.lastIndexOf(".");
				System.out.println("extIndex: "+extIndex);
				String fileExt = "";
				if(extIndex > 0) {
					fileExt = path.substring(extIndex+1);
					int len = fileExt != null ? fileExt.length() : 0; 
					if(len <= 0 || len > 4) {
						fileExt = "";
					}
				}		
				System.out.println(fileExt);
				BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
				stream.write(bytes);
				stream.close();
				FileInputStream fis = new FileInputStream(serverFile);
				//service.uploadFile(String.valueOf(System.currentTimeMillis()), fis);
				//service.uploadFile(mappingId, fis, mimeType, fileExt, "-1", "PEB",fileName);
				service.uploadFile(mappingId, fis, mimeType, fileExt, uploadedBy, moduleId,fileName);
				fis.close();
				Files.delete(Paths.get(path));

			} catch (IOException | MongoClientException e) {
				System.out.println("Exception occurred");
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			}
			
		}
		else{
			//model.addAttribute("errorMsg", "You failed to upload because the file was empty.");
			throw new DMSException("File empty");
		}
		//return "downloadfile";
		return ResponseEntity.created(null).build();
		//return true;
	}
	
	@GetMapping("/downloadlatest")	
	public void downloadLatest(@RequestParam ("mappingId")String mappingId, HttpServletRequest request, HttpServletResponse response){
		System.out.println("downloadlatest: "+mappingId);
		String dirName = mappingId + String.valueOf(System.currentTimeMillis());
		String absPath = request.getServletContext().getRealPath("/");
		File dir = new File(absPath+File.separator+dirName);
		System.out.println(absPath+File.separator+dirName);
		dir.mkdir();
		try {
			String mimeType = service.getFileInstance(mappingId, absPath+File.separator+dirName, true);
			File []fileArr = dir.listFiles();
			if(fileArr != null && fileArr.length > 0){
				System.out.println("true");
				//System.out.println((int)fileArr[0].length()+" "+fileArr[0].getName());
				// downloading logic comes here
				response.setContentLength((int)fileArr[0].length());
				//String mimeType = URLConnection.guessContentTypeFromName(fileArr[0].getName());
				response.setContentType(mimeType);
				 
		        InputStream inputStream = new BufferedInputStream(new FileInputStream(fileArr[0]));
		        System.out.println("deleting");
		        //dir.delete();
		        //Copy bytes from source to destination(outputstream in this example), closes both streams.
		        FileCopyUtils.copy(inputStream, response.getOutputStream());
		        try{
		        	//boolean isDeleted = dir.delete();
		        	System.out.println("deleting : "+absPath+File.separator+dirName+File.separator+fileArr[0].getName());
		        	Files.delete(Paths.get(absPath+File.separator+dirName+File.separator+fileArr[0].getName()));
		        	Files.delete(Paths.get(absPath+File.separator+dirName));
		        	//System.out.println("is deleted: "+isDeleted);
		        }catch(Exception e){
		        	e.printStackTrace();
		        }
		        
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}						
	}

	@GetMapping("/downloadoldest")
	public void downloadOldest(@RequestParam ("mappingId") String mappingId, HttpServletRequest request, HttpServletResponse response){
		System.out.println("downloadoldest: "+mappingId);
		String dirName = mappingId + String.valueOf(System.currentTimeMillis());
		String absPath = request.getServletContext().getRealPath("/");
		File dir = new File(absPath+File.separator+dirName);
		System.out.println(absPath+File.separator+dirName);
		dir.mkdir();
		try {
			String mimeType = service.getFileInstance(mappingId, absPath+File.separator+dirName, false);
			File []fileArr = dir.listFiles();
			if(fileArr != null && fileArr.length > 0){
				System.out.println("true");
				// downloading logic comes here
				response.setContentLength((int)fileArr[0].length());
				//String mimeType = URLConnection.guessContentTypeFromName(fileArr[0].getName());
				System.out.println("download all MIME :"+mimeType);
				response.setContentType(mimeType);
		        InputStream inputStream = new BufferedInputStream(new FileInputStream(fileArr[0]));
		        System.out.println("deleting");
		        //dir.delete();

		        //Copy bytes from source to destination(outputstream in this example), closes both streams.
		        FileCopyUtils.copy(inputStream, response.getOutputStream());
		        try{
		        	//boolean isDeleted = dir.delete();
		        	System.out.println("deleting : "+absPath+File.separator+dirName+File.separator+fileArr[0].getName());
		        	Files.delete(Paths.get(absPath+File.separator+dirName+File.separator+fileArr[0].getName()));
		        	Files.delete(Paths.get(absPath+File.separator+dirName));
		        }catch(Exception e){
		        	e.printStackTrace();
		        }
		        
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dir.delete();
	}
	
	@GetMapping("/downloadall")
	public void downloadAllVersions(@RequestParam ("mappingId") String mappingId, HttpServletRequest request, HttpServletResponse response){
		System.out.println("downloadall: "+mappingId);
		String dirName = mappingId + String.valueOf(System.currentTimeMillis());
		String absPath = request.getServletContext().getRealPath("/");
		File dir = new File(absPath+File.separator+dirName);
		System.out.println(absPath+File.separator+dirName);
		dir.mkdir();
		try {
			//service.getFileInstance(mappingId, absPath+File.separator+dirName, true);
			service.getFileAllVersions(mappingId, absPath+File.separator+dirName);
			File []fileArr = dir.listFiles();
			if(fileArr != null && fileArr.length > 0){
				System.out.println("true");
				// downloading logic comes here
				
				response.setContentLength((int)fileArr[0].length());
				String mimeType= URLConnection.guessContentTypeFromName(fileArr[0].getName());
				response.setContentType(mimeType);
		        InputStream inputStream = new BufferedInputStream(new FileInputStream(fileArr[0]));
		        System.out.println("deleting");
		        //Copy bytes from source to destination(outputstream in this example), closes both streams.
		        FileCopyUtils.copy(inputStream, response.getOutputStream());
		        try{
		        	//boolean isDeleted = dir.delete();
		        	System.out.println("deleting : "+absPath+File.separator+dirName+File.separator+fileArr[0].getName());
		        	Files.delete(Paths.get(absPath+File.separator+dirName+File.separator+fileArr[0].getName()));
		        	Files.delete(Paths.get(absPath+File.separator+dirName));
		        }catch(Exception e){
		        	e.printStackTrace();
		        }
		        
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		dir.delete();
	}

}
