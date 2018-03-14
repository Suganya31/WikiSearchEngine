package WikiSearch;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
/*import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;*/
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.ParseException;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
/*import org.json.JSONString;
import org.json.simple.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
*/
import org.json.simple.parser.JSONParser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;




public class WikiSearchEngine {
	static TransportClient client;
	static IndexResponse response;
//	static String url= new String("garbage url");
//	static String text=new String("garbage text");
	private static String CSV="/home/homer/Downloads/enwiki-20171103-pages.tsv";
	private static String dataset="/home/homer/Downloads/qald-7-train-multilingual.json";
	private static String testing="the currency of china";
	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		InputStream datasetFile=new FileInputStream(new File(dataset));

		client = new PreBuiltTransportClient(Settings.EMPTY)
				.addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300));

		//insert();
		ArrayList<String> SearchString=removeStopWords(testing);
		//System.out.println(SearchString);
		query(SearchString);
		
	}
	private static ArrayList<String> removeStopWords(String testing2) {
		// TODO Auto-generated method stub
		String[] words = testing2.split(" ");
	    ArrayList<String> wordsList = new ArrayList<String>();
	    Set<String> stopWordsSet = new HashSet<String>();
	   // stopWordsSet.add("OF");
	    stopWordsSet.add("THIS");
	    stopWordsSet.add("AND");
	    stopWordsSet.add("which");
	    stopWordsSet.add("WHAT");
	    stopWordsSet.add("DID");
	    stopWordsSet.add("THE");
	    stopWordsSet.add("A");



	    for(String word : words)
	    {
	        String wordCompare = word.toUpperCase();
	        if(!stopWordsSet.contains(wordCompare))
	        {
	            wordsList.add(word);
	        }
	    }
		return wordsList;
	}
	private static void query(ArrayList<String> searchString) throws ParseException, org.json.simple.parser.ParseException {
		// TODO Auto-generated method stub
		/*SearchResponse response = client.prepareSearch("wiki_search")
		        .setQuery(QueryBuilders.matchQuery("text", searchString)).get();   // Query
		        
		//System.out.println(response.toString());
		System.out.println(response.getHits().totalHits);*/
        int n=searchString.size();
       // System.out.println(i);
		BoolQueryBuilder qb = QueryBuilders.boolQuery();

        for(int i=0;i<n;i++)
        {
        	if(i==n-1)
 		   qb.must(QueryBuilders.matchQuery("text", searchString.get(i)));
        else
        {
    		qb.must(QueryBuilders.matchQuery("text", searchString.get(i)+" "+searchString.get(i+1)));}

        	

        }


		SearchResponse searchResponse = client
		        .prepareSearch("wiki_search").setSize(1)
		        .setQuery(qb).setFetchSource(new String[]{"url"}, null)
		        .get();
	//	System.out.println(searchResponse.toString());
	

		//System.out.println(searchResponse.getHits().totalHits);
		
		
		JsonElement jelement = new JsonParser().parse(searchResponse.toString());
	    JsonObject  jobject = jelement.getAsJsonObject();
	    jobject = jobject.getAsJsonObject("hits");
	    JsonArray jarray = jobject.getAsJsonArray("hits");
	    jobject = jarray.get(0).getAsJsonObject();
	    jobject= jobject.getAsJsonObject("_source");

         System.out.println(jobject.get("url"));		
		
	}
	/*public static void insert() throws IOException {
		System.out.println("start indexing");
		int i=1;
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(CSV)),"UTF-16"));
		
		while(br.ready()) {
		
		
			String line = br.readLine();
			
			String[] terms=line.split("\t");
		//	System.out.println(terms[0]);
			if(terms.length>1)
			{
				url=line.split("\t")[0];
				text=line.split("\t")[1];
			}

			response = client.prepareIndex("wiki_search", "wiki", Integer.toString(i))
					.setSource(jsonBuilder()
							.startObject()
							.field("url", url)
							.field("text", text)
							.endObject()
							)
					.get();
			i++;
			System.out.println(response.getResult().toString());
			System.out.println(url);
//			System.out.println(text);


		}*/
		//System.out.println("done indexing");
		


	}






