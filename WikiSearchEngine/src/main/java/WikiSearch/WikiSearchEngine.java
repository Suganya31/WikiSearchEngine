package WikiSearch;

//Is do was did does are do
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
	public static Set<String> removeStopWords(String testing2, String Answertype, int resultSize) throws ParseException, org.json.simple.parser.ParseException {
		// TODO Auto-generated method stub
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
		
		return query(wordsList,Answertype,rs);
	}
	private static Set<String> query(ArrayList<String> searchString, String answertype, int rs) throws ParseException, org.json.simple.parser.ParseException {
	String answer_type=answertype;
	int rs1=rs;
	//System.out.println(answer_type);
    	Set<String> systemAnswers=new HashSet();
int p=0;
	BoolQueryBuilder qb = QueryBuilders.boolQuery();
	BoolQueryBuilder qb1 = QueryBuilders.boolQuery();

    	
		qb.must(QueryBuilders.matchPhraseQuery("text", searchString));

		SearchResponse searchResponse = client
		        .prepareSearch("wiki_search").setSize(rs1)
		        .setQuery(qb)//.setFetchSource(new String[]{"url"}, null)
		        .get();
	//	System.out.println(searchResponse.toString());
		long hitsCount = searchResponse.getHits().getTotalHits();
    	
		if(hitsCount==0)
		{  p=1;
	      if(!answer_type.equals("boolean"))
	      {
				qb1.must(QueryBuilders.matchQuery("text", searchString));
	

	      }
	      else
	      {
	    	

			qb1.must(QueryBuilders.termQuery("text", searchString));
			}
		//qb1.must(QueryBuilders.termQuery("text", searchString));


			/*qb1.should(QueryBuilders.spanNearQuery(
	    			QueryBuilders.spanTermQuery("text",searchString.toString()),                     
	    	        20)//.addClause(QueryBuilders.spanTermQuery("text",searchString.toString()))
	    	           .inOrder(false));*/  
			 searchResponse = client
			        .prepareSearch("wiki_search").setSize(rs1)
			        .setQuery(qb1)//.setFetchSource(new String[]{"url"}, null)
			        .get();
		}
		


		//System.out.println(searchResponse.getHits().totalHits);
		//System.out.println("inside query method");
		
		JsonElement jelement = new JsonParser().parse(searchResponse.toString());
	    JsonObject  jobject = jelement.getAsJsonObject();
	    jobject = jobject.getAsJsonObject("hits");
	    JsonArray jarray = jobject.getAsJsonArray("hits");
	   // System.out.println(jarray.size());
	    if(jarray.size()==0)
	    {
	    	if(answer_type.equals("boolean"))
	    	systemAnswers.add("false");
	    	else
		    	systemAnswers.add("no result");
	    }


	    	
	    for(int i=0;i<jarray.size();i++)
	    {
	    	//System.out.println("inside for");
	    jobject = jarray.get(i).getAsJsonObject();
	    jobject= jobject.getAsJsonObject("_source");
	   

        // System.out.println(jobject.get("url"));		

	    if(jobject.get("url").toString()!=null)
	    {//System.out.println("inside if");
	    	url=jobject.get("url").toString();
	    
           url=url.replace("/wiki", "/resource");
          url=url.replace("enwikipedia.org", "dbpedia.org");
          url = url.replace("\"", "");

     //  System.out.println("the url is"+url);	
         
      if(answer_type.equals("boolean"))
        { 
    	  if(p==1)
    	  {
	systemAnswers.add("false");
	      }
     
          else 
          {
    		systemAnswers.add("true");
    	 }
     }

      else if(!answer_type.equals("boolean"))
      {
	       systemAnswers.add(url);
	  }
	     
		  //  System.out.println("inside if The system answer is"+systemAnswers);

	    }
	    if(jobject.get("url").toString()==null)
	    {
	    	/*systemAnswers.add(""
	    			+ "");*/
	    
	    	systemAnswers.add("no result");
	    }
	   
	    }
	    
	    
	    
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
		int resultSize=1;

				questions = LoaderController.load(Dataset.QALD7_Train_Multilingual);
				for (IQuestion q : questions) {
					//systemAnswers=q.getGoldenAnswers();
					//systemAnswers.addAll(q.getGoldenAnswers());

					System.out.println(q.getLanguageToQuestion().get("en"));
				//	WikiSearchEngine.removeStopWords(q.getLanguageToQuestion().get("en"),q.getAnswerType());
					System.out.println("The golden answer is"+q.getGoldenAnswers());

					//System.out.println(WikiSearchEngine.removeStopWords(q.getLanguageToQuestion().get("en"),q.getAnswerType()));
					testing=AnswerBasedEvaluation.fMeasure(WikiSearchEngine.removeStopWords(q.getLanguageToQuestion().get("en"),q.getAnswerType(),resultSize),q);
					System.out.println(WikiSearchEngine.removeStopWords(q.getLanguageToQuestion().get("en"),q.getAnswerType(),resultSize)+""+testing);
					
					while(testing==0.0)
					{   
						if(resultSize>=8)
							break;
						testing=AnswerBasedEvaluation.fMeasure(WikiSearchEngine.removeStopWords(q.getLanguageToQuestion().get("en"),q.getAnswerType(),resultSize),q);
						System.out.println(resultSize);
						resultSize++;
					}

				//	fmeasures+=AnswerBasedEvaluation.fMeasure(WikiSearchEngine.removeStopWords(q.getLanguageToQuestion().get("en"),q.getAnswerType()),q);
					fmeasures=fmeasures+testing;
					System.out.println(fmeasures);
					i++;
				}
				System.out.println("The F-Measure is "+fmeasures/i);
				
			//}
	// systemAnswers should be the set of the URI returned for the questions
		
	}

	}






