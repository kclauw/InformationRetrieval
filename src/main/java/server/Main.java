package server;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import config.Config;
import index.Index;
import token.PermutermFilter;
import query.SearchQuery;
import ranking.Ranking;
import ranking.RankingCosine;
import ranking.RankingEuclideanDistance;


import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.search.ScoreDoc;

/***
 * 
 * 
 *
 */
public class Main {
	
	
	
	
   

    public static void main(String[] args) throws Exception {
    	

    	//Create the index
    	InformationRetrieval app =  new InformationRetrieval(Config.DATA_DIR, Config.INDEX_DIR);
    	

		
    	
        String query = null;

        //  prompt the user to enter their name

       System.out.print("Enter your query: ");
        //  open up standard input, and buffer it
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        query = bufferedReader.readLine();
        System.out.println("Retrieving documents containing : " + query.toLowerCase());
        
        
        
        ScoreDoc[] results = app.searchIndexQuery(query.toLowerCase(),10);
     
        
        //ScoreDoc[] hits = app.searchIndexQuery("(parameter + estimation) ^ (for ^ parameter)",10);
       
        
       // ScoreDoc[] hits = app.searchIndexQuery(query.toLowerCase(),100);
        
        
    
        app.printResults(results);
        
       // vsmPrecisionRecall.calculate(vsmResults, 10);
        
        

        

        
        RankingEuclideanDistance euclidean = new RankingEuclideanDistance(results,app);
        RankingCosine cosine = new RankingCosine(results,app);
        int n = results.length / 4;
        if(results.length > 4 ) {
        	System.out.print("Rank the " + n + " documents according to euclidean distance");
            euclidean.initializeHeap();
            euclidean.printBestDocuments(n);
           
            System.out.println("\n");
         
            System.out.print("Rank the " + n + " documents according to cosine distance");
        
            cosine.initializeHeap();
            cosine.printBestDocuments(n);
        	
        }

         
        
        
    }
        
      
   
     

    
    	
 


   



}
