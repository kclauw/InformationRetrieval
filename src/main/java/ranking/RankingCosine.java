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

public class RankingCosine  extends Ranking{

	
	public RankingCosine(ScoreDoc[] hits, InformationRetrieval app) {
		super(hits, app);
		// TODO Auto-generated constructor stub
	}





	private static HashMap<Integer,HashMap<String, Double>> tdfIdfScores;

	private static InformationRetrieval app;
	private static ScoreDoc[] hits;
	
	
	
	
	

    private static double GetCosineSimilarity(List<Double> collection, List<Double> collection2) {
        double dot_product_total=0;
        double length_product = collection.size()*collection2.size();
    
        for (int i = 0; i < collection.size(); i++) {
        	
        	double x = collection.get(i).doubleValue();
        	double y = collection2.get(i).doubleValue();
        	dot_product_total = dot_product_total + x*y;
        	
        }
    
        double cosineSimilarity =  dot_product_total/length_product;  
               
        return Math.sqrt((double)cosineSimilarity);
        
    }
	
   
	

	
	/**
	 * This method initializes the heap
	 * documents
	 *
	 * 
	 * @return - Void
	 * @throws ParseException
	 * @throws IOException 
	 */
	
	public void initializeHeap() throws ParseException, IOException {
		   tdfIdfScores = getTFIdfScores();
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
		    	     	double distance = GetCosineSimilarity(xVector,yVector);
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
	
	
	
 
}
