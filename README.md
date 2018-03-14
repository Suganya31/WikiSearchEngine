# WikiSearchEngine

The main class is the WikiSearchEngine and F-Measure is calculated when this class is executed. 
Insert method creates an elasticsearch index with two fields URL and Text. Please uncomment the method call(insert()) when you want to create the index
StopWords are removed using removeStopWords() method 
Query method uses boolQuery() ,matchPhraseQuery(), matchQuery() and termQuery()
Boolean Answer type questions are answered using termQuery
Used (​ https://github.com/dice-group/NLIWOD/tree/master/qa.commons​ ) to load the QALD dataset and to calculate F-Measure
The F-Measure was calculated to be 0.10182388670760759
