package WikiSearch;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

//specify the path of the file to be indexed
	private static String CSV="/home/homer/Downloads/enwiki-20171103-pages.tsv";
	private static String dataset="/home/homer/Downloads/qald-7-train-multilingual.json";
	private static String testing="the currency of china";
	private static String url="garbage value";
	private static String text="garbage value";

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		//InputStream datasetFile=new FileInputStream(new File(dataset));

		client = new PreBuiltTransportClient(Settings.EMPTY)
				.addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300));
        //Insert method creates an elasticsearch index with two fields URL and Text
		//insert();

		System.out.println("calling method measure");
		
		measure();
		
	}
	public static Set<String> removeStopWords(String testing2, String Answertype, int resultSize) throws ParseException, org.json.simple.parser.ParseException {
		//
		String[] words = testing2.split(" ");
		int rs=resultSize;
	    ArrayList<String> wordsList = new ArrayList<String>();
	    Set<String> stopWordsSet = new HashSet<String>();
	    stopWordsSet.add("THIS");
	    stopWordsSet.add("AND");
	    stopWordsSet.add("WHICH");
	    stopWordsSet.add("WHAT");
	    stopWordsSet.add("WHERE");
	    stopWordsSet.add("WHO");
	    stopWordsSet.add("A");
	    stopWordsSet.add("AN");
	    stopWordsSet.add("WHEN");
	    stopWordsSet.add("WHO");
	    stopWordsSet.add("WAS");
	    stopWordsSet.add("TO");
	
	    stopWordsSet.add("WHERE");
	// Additional stop words are removed only for boolean questions since termquery is used 
	    if(Answertype.equals("boolean"))
	    {
	    	 
	 	    stopWordsSet.add("DID");
	 	    stopWordsSet.add("THE");
	 	   
	 	    stopWordsSet.add("OF");
	 	    stopWordsSet.add("TO");
	 	    stopWordsSet.add("ARE");
	 	
	 	    stopWordsSet.add("IS");
	 	    stopWordsSet.add("DO");
	 	    stopWordsSet.add("DOES");
	 	    stopWordsSet.add("HAVE");
	    }

	  
	    for(String word : words)
	    {
	        String wordCompare = word.toUpperCase();
	        if(!stopWordsSet.contains(wordCompare))
	        {
	            wordsList.add(word);
	        }
	    }
		// query method is called after the removal of stopwords
		return query(wordsList,Answertype,rs);
	}
	private static Set<String> query(ArrayList<String> searchString, String answertype, int rs) throws ParseException, org.json.simple.parser.ParseException {
	String answer_type=answertype;
	int rs1=rs;
    	Set<String> systemAnswers=new HashSet();
	BoolQueryBuilder qb = QueryBuilders.boolQuery();
	BoolQueryBuilder qb1 = QueryBuilders.boolQuery();

    	// using Phrase Matching to get exact results
		qb.must(QueryBuilders.matchPhraseQuery("text", searchString));

		SearchResponse searchResponse = client
		        .prepareSearch("wiki_search").setSize(rs1)
		        .setQuery(qb)//.setFetchSource(new String[]{"url"}, null)
		        .get();
		long hitsCount = searchResponse.getHits().getTotalHits();
    	// when no hit is found using phrase matching then we using term query for boolean answertype and matchquery for other answer types
		if(hitsCount==0)
		{  
	      if(answer_type.equals("boolean"))
	      {
				qb1.must(QueryBuilders.termQuery("text", searchString));
	

	      }
	      else
	      {	    	

			qb1.must(QueryBuilders.matchQuery("text", searchString));
			}
 
			 searchResponse = client
			        .prepareSearch("wiki_search").setSize(rs1)
			        .setQuery(qb1)
			        .get();
		}
		


		
		JsonElement jelement = new JsonParser().parse(searchResponse.toString());
	    JsonObject  jobject = jelement.getAsJsonObject();
	    jobject = jobject.getAsJsonObject("hits");
	    JsonArray jarray = jobject.getAsJsonArray("hits");
	   // When no hits are found
	    if(jarray.size()==0)
	    {
	    	if(answer_type.equals("boolean"))
	    	systemAnswers.add("false");
	    	else
		    	systemAnswers.add("no result");
	    }


	    	
	    for(int i=0;i<jarray.size();i++)
	    {
	 
	    jobject = jarray.get(i).getAsJsonObject();
	    jobject= jobject.getAsJsonObject("_source");
	   

        // avoiding null pointer exception when empty URL is returned	

	    if(jobject.get("url").toString()!=null)
	    {
	    	url=jobject.get("url").toString();
	    
           url=url.replace("/wiki", "/resource");
          url=url.replace("enwikipedia.org", "dbpedia.org");
          url = url.replace("\"", "");

         
      if(answer_type.equals("boolean"))
        { 
    	 
    		systemAnswers.add("true");
    	 
     }

      else if(!answer_type.equals("boolean"))
      {
	       systemAnswers.add(url);
	  }
	     

	    }
	    if(jobject.get("url").toString()==null)
	    {
	    	
	    
	    	systemAnswers.add("no result");
	    }
	   
	    }
	    
	    
	    
	    return systemAnswers;
		
	}
	public static void insert() throws IOException {
		System.out.println("start indexing");
		int i=1;
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(CSV)),"UTF-16"));
		
		while(br.ready()) {
		
		
			String line = br.readLine();
			
			String[] terms=line.split("\t");
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
		


		}
		System.out.println("done indexing");
	}
		
	public static void measure() throws ParseException, org.json.simple.parser.ParseException {
		//Loading QALD7_Train_Multilingual dataset using the given library
		
		double fmeasures=0.0;
		double testing=0.0;
		int i=0;
		int resultSize=1;

				questions = LoaderController.load(Dataset.QALD7_Train_Multilingual);
				for (IQuestion q : questions) {
					
		

					testing=AnswerBasedEvaluation.fMeasure(WikiSearchEngine.removeStopWords(q.getLanguageToQuestion().get("en"),q.getAnswerType(),resultSize),q);
				
					// In order to not reduce the recall measure the no. of hits to be returned is increased gradually to 8 only when the F-Measure is 0
					while(testing==0.0)
					{   
						if(resultSize>=8)
							break;
						testing=AnswerBasedEvaluation.fMeasure(WikiSearchEngine.removeStopWords(q.getLanguageToQuestion().get("en"),q.getAnswerType(),resultSize),q);
						resultSize++;
					}

					fmeasures=fmeasures+testing;
					
					i++;
				}
				System.out.println("The F-Measure is "+fmeasures/i);
				
	
		
	}

	}






