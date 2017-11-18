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
        
     //   System.out.println("Found " + hits.length + " hits.");
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println((i + 1) + ". " + d.get("file") + " " +  d.get("text"));
        }

        // reader can only be closed when there
        // is no need to access the documents any more.
        reader.close();

	}
	
	
	static BooleanQuery createBooleanQuery(Index indexFile,String query) {
		
		 List<String> elements = new ArrayList<String>();
		 //Extract the elements between brackets
		 Matcher m = Pattern.compile("\\((.*?)\\)").matcher(query);

		 while(m.find()) {
			 
			 //Filter the '(' symbols
			 String word = m.group(1);
			 if(word.charAt(0) == '(') {
				 word = word.substring(1);
			 }
			 
			 
			 elements.add(word);
		 }
		 
		 System.out.println(elements);
		 
		
		 
		 
		//Split the elements into tokens
	
		 
		String[] splitQuery = elements.get(0).split("\\^");
		for (String token : splitQuery) {
				
			    // Test NOT condition of token
		        if (!"".equals(token)) {
		        	if(token.charAt(0) == '!'){
		        		//Remove the ! symbol
		        		token = token.substring(1);
		        		System.out.println("NOT");
		        		System.out.print(token);
		        	}else {
		        		System.out.print(token);
		        	}
		        	System.out.println("\n");
		        }
		 }
		
	
	 	BooleanClause b = new BooleanClause(
                new TermQuery(new Term("text", "abcd")),
                BooleanClause.Occur.MUST);
        /*
        BooleanClause b2 = new BooleanClause(
                new TermQuery(new Term("file", "CACM-0002.html")),
                BooleanClause.Occur.SHOULD);
         */
   
        BooleanQuery bq = new BooleanQuery.Builder().add(b).build();
        
        return bq;
		}
	
	

    public static void main(String[] args) throws Exception {
    	
    	//System.out.println("Enter query :");
		//String querystr = System.console().readLine();
		
    	//Create the index
    	Index indexFile = new Index(Config.DATA_DIR,Config.INDEX_DIR);
    	
  
    	//Retrieve a query from the user
		Term t = new Term("file", "java");
		Query query = new TermQuery(t);
		
		//Query based on file
        Query q = new QueryParser("file", indexFile.analyzer).parse("CACM-0001.html");
        //Query bassed on context of text
        Query q2 = new QueryParser("text", indexFile.analyzer).parse("CACM-0001.html");
        
        //`A Term represents a word from text. This is the unit of search. It is composed of two elements, 
        //the text of the word, as a string, and the name of the field that the text occurred in.
        Query q3 = new TermQuery(new Term("file", "CACM-0001.html"));
        
        
        searchIndexQuery("((!CACM-0001.html^CACM-0002.html)^(!CACM-0001.html^CACM-0002.html))",indexFile);
        
       
        
        
        
        
       
     


        
    }



}
