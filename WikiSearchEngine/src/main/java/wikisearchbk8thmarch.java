package WikiSearch;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.aksw.qa.commons.measure.AnswerBasedEvaluation;
import org.apache.http.ParseException;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.JSONString;
import org.json.simple.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import org.json.simple.parser.JSONParser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.aksw.qa.commons.measure.AnswerBasedEvaluation;
import org.apache.http.ParseException;





public class WikiSearchEngine {
	static TransportClient client;
	static IndexResponse response;
	static List<IQuestion> questions;

//	static String url= new String("garbage url");
//	static String text=new String("garbage text");
	private static String CSV="/home/homer/Downloads/enwiki-20171103-pages.tsv";
	private static String dataset="/home/homer/Downloads/qald-7-train-multilingual.json";
	private static String testing="the currency of china";
	private static String url;
	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		InputStream datasetFile=new FileInputStream(new File(dataset));

		client = new PreBuiltTransportClient(Settings.EMPTY)
				.addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300));

		//insert();
	//	ArrayList<String> SearchString=removeStopWords(testing);
		//System.out.println(SearchString);
		//query(SearchString);
		System.out.println("calling method measure");
		measure();
		System.out.println("done");
		
	}
	public static Set<String> removeStopWords(String testing2) throws ParseException, org.json.simple.parser.ParseException {
		// TODO Auto-generated method stub
		String[] words = testing2.split(" ");
	    ArrayList<String> wordsList = new ArrayList<String>();
	    Set<String> stopWordsSet = new HashSet<String>();
	   // stopWordsSet.add("OF");
	    stopWordsSet.add("THIS");
	    stopWordsSet.add("AND");
	    stopWordsSet.add("WHICH");
	    stopWordsSet.add("WHAT");
	    stopWordsSet.add("WHERE");
	    stopWordsSet.add("WHO");
	    stopWordsSet.add("DID");
	    //stopWordsSet.add("THE");
	    stopWordsSet.add("A");
	    stopWordsSet.add("AN");
	    stopWordsSet.add("WHEN");
	    stopWordsSet.add("WHO");
	    stopWordsSet.add("WAS");
	   // stopWordsSet.add("OF");
	    stopWordsSet.add("TO");
	  //  stopWordsSet.add("ARE");
	    stopWordsSet.add("WHERE");
	    //stopWordsSet.add("IS");

	    for(String word : words)
	    {
	        String wordCompare = word.toUpperCase();
	        if(!stopWordsSet.contains(wordCompare))
	        {
	            wordsList.add(word);
	        }
	    }
		
		return query(wordsList);
	}
	private static Set<String> query(ArrayList<String> searchString) throws ParseException, org.json.simple.parser.ParseException {
		// TODO Auto-generated method stub
		/*SearchResponse response = client.prepareSearch("wiki_search")
		        .setQuery(QueryBuilders.matchQuery("text", searchString)).get();   // Query
		        
		//System.out.println(response.toString());
		System.out.println(response.getHits().totalHits);*/
       // int n=searchString.size();
       // System.out.println(i);
    	Set<String> systemAnswers=new HashSet();


        /*for(int i=0;i<n;i++)
        {
        	if(i==n-1)
 		   qb.must(QueryBuilders.matchQuery("text", searchString.get(i)));
        else
        {
    		qb.must(QueryBuilders.matchQuery("text", searchString.get(i)+" "+searchString.get(i+1)));}

        	 qb.must(QueryBuilders.matchQuery("text", searchString.get(i)));
         	 qb.should(QueryBuilders.matchQuery("url", searchString.get(i)));

        }
*/
		BoolQueryBuilder qb = QueryBuilders.boolQuery();
    	qb.should(QueryBuilders.matchPhraseQuery("text", searchString));
    	qb.must(QueryBuilders.matchQuery("text", searchString));

    	qb.should(QueryBuilders.spanNearQuery(
    			QueryBuilders.spanTermQuery("text",searchString.toString()),                     
    	        12)//.addClause(QueryBuilders.spanTermQuery("field","value2"))
    	           .inOrder(false));  


		SearchResponse searchResponse = client
		        .prepareSearch("wiki_search").setSize(10)
		        .setQuery(qb)//.setFetchSource(new String[]{"url"}, null)
		        .get();
	//	System.out.println(searchResponse.toString());
	

		//System.out.println(searchResponse.getHits().totalHits);
		
		
		JsonElement jelement = new JsonParser().parse(searchResponse.toString());
	    JsonObject  jobject = jelement.getAsJsonObject();
	    jobject = jobject.getAsJsonObject("hits");
	    JsonArray jarray = jobject.getAsJsonArray("hits");
	   // System.out.println(jarray.toString());
	    
	    if(jarray.size()>=1)
	    {
	    jobject = jarray.get(0).getAsJsonObject();
	    jobject= jobject.getAsJsonObject("_source");
	    }

        // System.out.println(jobject.get("url"));		

	    if(jarray.size()>=1&&jobject.get("url").toString()!=null)
	    {//System.out.println("inside if");
	    	url=jobject.get("url").toString();
	    
           url=url.replace("/wiki", "/resource");
          url=url.replace("enwikipedia.org", "dbpedia.org");
          url = url.replace("\"", "");

      //  System.out.println("the url is"+url);	
         

	       systemAnswers.add(url);
	     
		  //  System.out.println("inside if The system answer is"+systemAnswers);

	    }
	    else
	    	/*systemAnswers.add(""
	    			+ "");*/
	    	systemAnswers.add("no result");
	   // System.out.println("The system answer is"+systemAnswers);
	    return systemAnswers;
		
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
		
	public static void measure() throws ParseException, org.json.simple.parser.ParseException {
		//question ??? QALD7_Train_Multilingual
		//for (Dataset d : Dataset.values()) {
				//questions = LoaderController.load(d);
		double fmeasures=0.0;
		double testing=0.0;
		int i=0;
				questions = LoaderController.load(Dataset.QALD7_Train_Multilingual);
				for (IQuestion q : questions) {
					//systemAnswers=q.getGoldenAnswers();
					//systemAnswers.addAll(q.getGoldenAnswers());

					System.out.println(q.getLanguageToQuestion().get("en"));
					WikiSearchEngine.removeStopWords(q.getLanguageToQuestion().get("en"));
					System.out.println("The golden answer is"+q.getGoldenAnswers());

					System.out.println(WikiSearchEngine.removeStopWords(q.getLanguageToQuestion().get("en")));
					testing=AnswerBasedEvaluation.fMeasure(WikiSearchEngine.removeStopWords(q.getLanguageToQuestion().get("en")),q);
					System.out.println(WikiSearchEngine.removeStopWords(q.getLanguageToQuestion().get("en"))+""+testing);

					fmeasures+=AnswerBasedEvaluation.fMeasure(WikiSearchEngine.removeStopWords(q.getLanguageToQuestion().get("en")),q);
					System.out.println(fmeasures);
					i++;
				}
				System.out.println("The F-Measure is "+fmeasures/i);
				
			//}
	// systemAnswers should be the set of the URI returned for the questions
		
	}

	}






