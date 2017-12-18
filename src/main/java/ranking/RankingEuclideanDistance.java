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
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.util.BytesRef;

import server.InformationRetrieval;

public class RankingEuclideanDistance {

	
	private static HashMap<Integer,HashMap<String, Double>> tdfIdfScores;
	private static PriorityQueue<DocumentDistancePair> rankingPriorityQueue;
	private static InformationRetrieval app;
	private static ScoreDoc[] hits;
	
	
	

    private static double GetEuclideanDistance(List<Double> collection, List<Double> collection2) {
        double diff_square_sum = 0.0;
    
        for (int i = 0; i < collection.size(); i++) {
        	
        	double x = collection.get(i).doubleValue();
        	double y = collection2.get(i).doubleValue();
        	diff_square_sum = diff_square_sum + (x - y) * (x - y);
        }
    
        //diff_square_sum = (collection - collection2) * (collection - collection2);
        return Math.sqrt((double)diff_square_sum);
    }
	
    public static class DocumentDistancePair {
        private double key;
        private Integer value;
        
        public DocumentDistancePair(double min_distance, Integer value) {
            this.key = min_distance;
            this.value = value;
        }

        // Constructors, getters etc.
    }
    
	public static Comparator<DocumentDistancePair> DocumentComparator = new Comparator<DocumentDistancePair>(){
		


		public int compare(DocumentDistancePair d1, DocumentDistancePair d2) {
			// TODO Auto-generated method stub
			 return (int) (d2.key - d1.key);
		}
	};
	
	
	public RankingEuclideanDistance(ScoreDoc[] hits,InformationRetrieval app) {
		this.app = app;
		this.hits = hits;
		
	}
	
	
	
	public static void initializeHeap() throws ParseException, IOException {
	   tdfIdfScores = getTFIdfScores(hits);
	   rankingPriorityQueue = new PriorityQueue<DocumentDistancePair>(DocumentComparator);
	    

	    for(Integer docX: tdfIdfScores.keySet()) {
	    
	    	double min_distance = 500000.0;
	 		for(Integer docY:  tdfIdfScores.keySet()) {
				
	 			HashMap<String, Double> termMapX = tdfIdfScores.get(docX);
				HashMap<String, Double> termMapY = tdfIdfScores.get(docY);

		//		System.out.println(termMapY.values());
				
				ArrayList<Double> yVector = new ArrayList<Double>(termMapX.values());
				ArrayList<Double> xVector = new ArrayList<Double>(termMapY.values());
				
				
				if(docX != docY) {
	    	     	double distance = GetEuclideanDistance(xVector,yVector);
	    	     //	System.out.println(distance);
	    	     	if(distance < min_distance) {
	    	     		min_distance = distance;
	    	     	}
	    			
	    			
	    			
					
				}
				
			

			}
	 		
	 		System.out.println("Document " + docX + " "  + min_distance);
			rankingPriorityQueue.add(new DocumentDistancePair(min_distance, docX));
		       
	 		
	    

	    }
	}
	
	
	
	/**
	 * This method calculates the TF-IDF score for each terms in the indexed
	 * documents
	 *
	 * @param total retrieved documents
	 * @return - Hashmap of TF-IDF score per each term in document 
	 * @throws ParseException
	 * @throws IOException 
	 */
	public static HashMap<Integer, HashMap<String, Double>> getTFIdfScores(ScoreDoc[] hits) throws ParseException, IOException {
		
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
                    Term termInstance = new Term("contents",term);
                    termsInCollection.add(term.utf8ToString());
                    // TF-IDF calculations
                    long tf = reader.totalTermFreq(termInstance);
                    long docCount = reader.docFreq(termInstance);
	                double idf = simi.idf(docCount, totalDocuments);
	                
	               
	                termMap.put(term.utf8ToString().toString(), (tf * idf));
	  
                }catch(Exception e){
                    System.out.println(e);
                }
            }  	
            
        
            documentMap.put(docId, termMap);
        }
        
        
        
        //Add 
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
    	   System.out.println("Selected documents");
    	    for(int i = 0; i <= rankN;i++) {
    	    	DocumentDistancePair entry = rankingPriorityQueue.poll();
    	    	System.out.println("Document : "+entry.key+ " " +entry.value );
    	    	
    	    }	
    }
 
}
