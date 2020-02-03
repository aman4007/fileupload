package in.ecgcltd.erp.dms.util;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class MongoClientLoader {
	
	private MongoClient mongoClient;
	private static MongoClientLoader loader;
	private static Object object = new Object();

	private MongoClientLoader() {
		
		
		MongoCredential credential = MongoCredential.createCredential("avinash", "video", "avinash".toCharArray());
		MongoClientOptions.Builder options = new MongoClientOptions.Builder();
		mongoClient = new MongoClient(new ServerAddress("localhost:27017"), credential, options.build());
	}

	public static MongoClientLoader getInstance() {

		synchronized (object) {
			if (loader == null) {
				loader = new MongoClientLoader();
			}
		}
		return loader;
	}

	public MongoClient getMongoClient() {
		return mongoClient;
	}

}
