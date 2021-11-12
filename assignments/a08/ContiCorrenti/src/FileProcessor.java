package ContiCorrenti.src;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FileProcessor {
	public static void main(String[] args) {
		ExecutorService tpool = Executors.newCachedThreadPool();
		ObjectMapper mapper = new ObjectMapper();
		boolean ok = false;
		JsonFactory fact = new JsonFactory();
		try (BufferedInputStream streamIn =
				new BufferedInputStream(new FileInputStream(new File("cc.json")));) {
			JsonParser jparse = fact.createParser(streamIn);
			jparse.setCodec(mapper);
			JsonToken tok = jparse.nextToken();
			if (tok != JsonToken.START_ARRAY) {
				throw new Exception();
			}
			while ((tok = jparse.nextToken()) != JsonToken.END_ARRAY) {
				ContoCorrente cc = jparse.readValueAs(ContoCorrente.class);
				System.out.println(cc.toString());
				tpool.submit(null, null)
			}
		} catch (Exception e) {
			ok = false;
			e.printStackTrace();
		} finally {
			ok = true;
		}
	}
}
