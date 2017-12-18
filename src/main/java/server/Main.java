package server;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.junit.Test;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import config.Config;
import index.Index;
import junit.runner.Version;
import token.PermutermFilter;
import query.SearchQuery;
import ranking.RankingEuclideanDistance;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;

/***
 * 
 * 
 *
 */
public class Main {
	
	
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

	
    public static void main(String[] args) throws Exception {
    	
    	//System.out.println("Enter query :");
		//String querystr = System.consolke().readLine();
		
    	//Create the index
    
    	InformationRetrieval app =  new InformationRetrieval(Config.DATA_DIR, Config.INDEX_DIR);

		//Index indexFile = new Index(Config.DATA_DIR,Config.INDEX_DIR);
    	
    	
    	
        String query = null;

        //  prompt the user to enter their name

        //System.out.print("Enter your query: ");
        //  open up standard input, and buffer it
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        //query = bufferedReader.readLine();
        //System.out.println("Retrieving documents containing : " + query.toLowerCase());
        
       // ScoreDoc[] hits = app.searchIndexQuery("*a",10);
        //ScoreDoc[] hits = app.searchIndexQuery("(parameter + estimation) ^ (for ^ parameter)",10);
        ScoreDoc[] hits = app.searchIndexQuery("*a ^ *b",10);
        
        app.printResults(hits);
        
        
        RankingEuclideanDistance r = new RankingEuclideanDistance(hits,app);
        
        r.initializeHeap();
        r.printBestDocuments(4);
        
        
    }
        
      
   
     

    
    	
 


   



}
