package ranking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.util.BytesRef;

import server.InformationRetrieval;

public abstract class Ranking {

	
	public static HashMap<Integer,HashMap<String, Double>> tdfIdfScores;
	public static PriorityQueue<DocumentDistancePair> rankingPriorityQueue;
	public static InformationRetrieval app;
	public static ScoreDoc[] hits;
	
	
	abstract void initializeHeap() throws ParseException, IOException;
	
	
	public Ranking(ScoreDoc[] hits,InformationRetrieval app) {
		this.app = app;
		this.hits = hits;
	}

    
    public static class DocumentDistancePair {
        private double key;
        private Integer value;
        
        public DocumentDistancePair(double min_distance, Integer value) {
            this.key = min_distance;
            this.value = value;
        }
    }
    
	public static Comparator<DocumentDistancePair> DocumentComparator = new Comparator<DocumentDistancePair>(){
		


		public int compare(DocumentDistancePair d1, DocumentDistancePair d2) {
			// TODO Auto-generated method stub
			 return (int) (d2.key - d1.key);
		}
	};
	

    public static float idf(long docFreq, long docCount) {
    	
        return (float) Math.log(1 + (docCount - docFreq)/(docFreq));
     }
    
    
public HashMap<Integer, HashMap<String, Double>> getTFIdfScores() throws IOException {
		
		Set<String> termsInCollection = new TreeSet<String>();
		IndexReader reader = DirectoryReader.open(app.getDirectory());
		IndexSearcher searcher = new IndexSearcher(reader);

		
		
		int totalDocuments = hits.length;
		
		HashMap<Integer, HashMap<String, Double>> documentMap = new HashMap<Integer, HashMap<String, Double>>();
	
        for(int i=0;i<totalDocuments;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            HashMap<String, Double> termMap = new HashMap<String, Double>();
       
			TokenStream tokenStream =
			TokenSources.getAnyTokenStream(reader, hits[i].doc,
			"content", app.getAnalyzer());
            Terms termVector = reader.getTermVector(docId,"contents");
            TermsEnum itr = termVector.iterator();
            BytesRef term = null;
            while((term = itr.next()) != null){
            	
                try{
                	ClassicSimilarity simi = new ClassicSimilarity();
     //           	BM25Similarity simi2 = new BM25Similarity();
                    Term termInstance = new Term("contents",term);
                    termsInCollection.add(term.utf8ToString());
                    // TF-IDF calculations
                    long tf = reader.totalTermFreq(termInstance);
                    long docCount = reader.docFreq(termInstance);

	                double idf = simi.idf(docCount, totalDocuments);

	                termMap.put(term.utf8ToString().toString(), ((tf + 1) * idf));
	  
                }catch(Exception e){
                    System.out.println(e);
                }
            }  	
            
        
            documentMap.put(docId, termMap);
        }
        
        
        
        //Add the empty terms
        for (Integer documentId : documentMap.keySet()) {
        	
		    for(String term : termsInCollection){ 
		    	if(documentMap.get(documentId).get(term) == null) {
		    		documentMap.get(documentId).put(term, 0.0);
		    	}
		    }
        }

        
        System.out.println("-----------------");
        System.out.println(termsInCollection.size());
        reader.close();
     
	    return documentMap;
	}


    
    

    
    public void printBestDocuments(int rankN) {
    	   System.out.println("Selected documents " + hits.length);
    	   
    	   
    	   
    	    for(int i = 0; i <= rankN;i++) {
    	    	
    	    	DocumentDistancePair entry = rankingPriorityQueue.poll();
    	    	
    	    	//System.out.println(hits[entry.value]);
    	    	System.out.println("Document : " + entry.value );
    	    	
    	    	
    	    }	
    }




 
}
