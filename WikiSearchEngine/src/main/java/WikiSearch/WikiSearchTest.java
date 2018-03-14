package WikiSearch;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

public class WikiSearchTest {

	static TransportClient client;
	private static String CSV="/home/homer/Downloads/sample.csv";
	public static void main(String[] args) throws Exception {
		client = new PreBuiltTransportClient(Settings.EMPTY)
		        .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300));
		insert();
	}
	 public static void insert() throws IOException {
     BufferedReader br = new BufferedReader(new FileReader(CSV));
      while(br.ready()) {
      String line = br.readLine();


IndexResponse response = client.prepareIndex("wiki_test", "wiki1", "1")
	                .setSource(jsonBuilder()
	                        .startObject()
	                        .field("url", line.split(",")[0])
	                        .field("text", line.split(",")[1])
	                        .endObject()
	                )
	                .get();
/*SearchResponse response1 = client.prepareSearch("index1")

.get();
System.out.println(response1.toString());*/

}
	       
System.out.println("done inserting");

	    }
	
	 


}
