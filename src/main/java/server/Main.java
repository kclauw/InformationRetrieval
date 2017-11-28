package server;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import config.Config;
import index.Index;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;


public class Main {
	

	public static void searchIndexQuery(String query,Index indexFile) throws CorruptIndexException, IOException {
		
		
		BooleanQuery bq = createBooleanQuery(indexFile,query);
		
		 
        // 3. search
        int hitsPerPage = 10;
        IndexReader reader = DirectoryReader.open(indexFile.directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(bq, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;

        // 4. display results
        
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println((i + 1) + ". " + d.get("file"));
        }

        // reader can only be closed when there
        // is no need to access the documents any more.
        reader.close();

	}
	
	
	//Retrieve the terms between bracekts
	void splitBrackets(String query,List<String> elements) {
		 Matcher m = Pattern.compile("\\((.*?)\\)").matcher(query);

		 while(m.find()) {
			 //Filter the '(' symbols
			 String word = m.group(1);
			 if(word.charAt(0) == '(') {
				 word = word.substring(1);
			 }	 		 
			 elements.add(word);
		 }
	}
	
  public static List<String> tokenizeString(Analyzer analyzer, String string) {
	    List<String> result = new ArrayList<String>();
	    try {
	      TokenStream stream  = analyzer.tokenStream(null, new StringReader(string));
	      stream.reset();
	      while (stream.incrementToken()) {
	        result.add(stream.getAttribute(CharTermAttribute.class).toString());
	      }
	    } catch (IOException e) {
	      // not thrown b/c we're using a string reader...
	      throw new RuntimeException(e);
	    }
	    return result;
	  }
	  
  
  static BooleanQuery createTermQuery(List<String> termElements,List<String> operatorElements) {
	  	BooleanQuery.Builder query = new BooleanQuery.Builder();
	  	int it = 0;

	  
  		for(String token :termElements) {
  		  	String currentOperator = operatorElements.get(it);
			char currentChar = token.charAt(0);
			System.out.println(token + " " + currentOperator);
        	if(currentChar == '!'){
        		//Remove the ! symbol
        		token = token.substring(1);
        		TermQuery t = new TermQuery(new Term("text", token));
        		query.add(new BooleanClause(t,BooleanClause.Occur.MUST_NOT));
        	}else if(currentChar == '+'){
    	      	query.add(new BooleanClause(new TermQuery(new Term("text", token)),BooleanClause.Occur.SHOULD));
        	}else {
        		query.add(new BooleanClause(new TermQuery(new Term("text", token)),BooleanClause.Occur.MUST));
        	}
        	it++;
		 }
  		System.out.println("\n");
  		 return query.build();	
  	}

	
	static BooleanQuery createBooleanQuery(Index indexFile,String query) {
		
		 
		 //Extract the elements between brackets
		
		
		 String[] terms = query.split(" ");
		 
		 List<String> termElements = new ArrayList<String>();
		 List<String> operatorElements = new ArrayList<String>();
		 
		 //Filter the ( ) tags
		 
		 
		 // (A ^ B ^ C) ^ (A) 
		 
		 //Filter terms and boolean expressions
		 for(String token : terms) {
			char currentChar = token.charAt(0);
			 
			switch(currentChar) {
			case '*' :
					operatorElements.add(token);
					break;
			case '+' :
					operatorElements.add(token);
					break;
			case '!' :
					termElements.add(token);
					break;
			default:termElements.add(token); 
					break;
						
			}
		 }
	
		 //To match the size of the terms -> Indicates the end of the query
		 operatorElements.add("");
		 System.out.println(termElements);
		 BooleanQuery b = createTermQuery(termElements,operatorElements);

	
		 /*
		 int i = 0;
		 String current = operatorElements.get(0);
		 String previous = operatorElements.get(0);
		 
		 BooleanQuery orQuery = new BooleanQuery.Builder().build();
		 BooleanQuery tempQuery = new BooleanQuery.Builder().build();
		 System.out.println(query);
		 for(String token :termElements) {
			 
			 String operator = operatorElements.get(i);
			 System.out.println(token + " " + operator);
	      	 BooleanClause b = new BooleanClause(new TermQuery(new Term("text", "token")),
                        BooleanClause.Occur.MUST);
	      	 
			 //Add boolean
			 if(operator == "*") {
				 tempQuery.clauses().add(b);
			 }
			 
			 //Add boolean
			 if(operator == "+") {
				 tempQuery.clauses().add(b);
			 }
			 
			 //End reached
			 if(operator == " ") {
				 tempQuery.clauses().add(b);
			 }
			 
			 //Add boolean
			 if(operator != previous) {
				 BooleanClause c = new BooleanClause(tempQuery, BooleanClause.Occur.SHOULD);
				 BooleanClause d = new BooleanClause(tempQuery, BooleanClause.Occur.SHOULD);
				 
				
			 }
			 
			 previous = operator;
			 
			 i++;
	
			 
		 }*/
	


		 /*
		 
		 for(String token : terms) {
			char currentChar = token.charAt(0);
			System.out.println(token);
        	if(currentChar == '!'){
        		//Remove the ! symbol
        		token = token.substring(1);
        	 	BooleanClause b = new BooleanClause(new TermQuery(new Term("text", "token")),
                        BooleanClause.Occur.MUST);
        	 	termsAnd.add(b);
        	}
		 }*/
		      

        return b;
		}
	
	

    public static void main(String[] args) throws Exception {
    	
    	//System.out.println("Enter query :");
		//String querystr = System.console().readLine();
		
    	//Create the index
    	Index indexFile = new Index(Config.DATA_DIR,Config.INDEX_DIR);
    	
  
    
        //searchIndexQuery("Perlis,",indexFile);
        searchIndexQuery("samelson",indexFile);
       
        
        
        
        
       
     


        
    }



}
