package server;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import analyzer.SoundexAnalyzer;
import config.Config;
import index.Index;
import query.SearchQuery;

public class InformationRetrieval {
	
	
	private static Directory directory;
	private static Analyzer analyzer;
	private Index index;
	
	private static ScoreDoc[] hits;
	private static BooleanQuery bq;
	
	
	
	static Index indexFile;
	
	public InformationRetrieval(String dataDirectory,String indexDirectory) throws IOException, SAXException, TikaException {
		this.directory = FSDirectory.open(Paths.get(indexDirectory));
    	//this.analyzer = new SimpleAnalyzer();
    	this.analyzer = new SoundexAnalyzer();
    	this.index = new Index(directory,analyzer);
    	index.createIndex(dataDirectory,indexDirectory);

	}
	
	
	private static final Pattern TAG_REGEX = Pattern.compile("<term>(.+?)</term>");

	private static List<String> getTagValues(final String str) {
	    final List<String> tagValues = new ArrayList<String>();
	    final Matcher matcher = TAG_REGEX.matcher(str);
	    while (matcher.find()) {
	        tagValues.add(matcher.group(1));
	    }
	    return tagValues;
	}

	
	public static void printResults(String query) throws IOException, ParseException, InvalidTokenOffsetsException {
		
		

		
		ScoreDoc[] hits = searchIndexQuery(query);
		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher searcher = new IndexSearcher(reader);
		
		SimpleHTMLFormatter simpleHTMLFormatter = new
				SimpleHTMLFormatter("<term>", "</term>");
				SimpleHTMLEncoder simpleHTMLEncoder = new SimpleHTMLEncoder();
				Highlighter highlighter = new Highlighter(simpleHTMLFormatter,
				simpleHTMLEncoder, new QueryScorer(bq));
				
	
				

	
		
	
			
	
		
	
        System.out.println("-----------------");
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
   
        
   
            System.out.println((i + 1) + ". " + d.get("file"));
            String text = d.get("content");
            

			TokenStream tokenStream =
			TokenSources.getAnyTokenStream(reader, hits[i].doc,
			"content", analyzer);
					
		
			
            String[] frags = highlighter.getBestFragments(tokenStream, text, 10);
            for (String frag : frags)
            {

                System.out.println(Arrays.toString(getTagValues(frag).toArray()));
                
            }
            
           
        
            		
            text = text.replaceAll("\\d","");
            text = text.replaceAll(" ","");
    
            //System.out.println(text);
            System.out.println("\n");
            
            Terms termVector = reader.getTermVector(docId,"contents");
            TermsEnum itr = termVector.iterator();
            
            BytesRef term = null;
            while((term = itr.next()) != null){
                try{
                    String termText = term.utf8ToString();
                    Term termInstance = new Term("contents",term);
                    long termFreq = reader.totalTermFreq(termInstance);
                    long docCount = reader.docFreq(termInstance);

                    System.out.println("term: "+termText+", termFreq = "+termFreq+", docCount = "+docCount);
                }catch(Exception e){
                    System.out.println(e);
                }
            }  
            
            
    		
        }
        

        System.out.println("-----------------");
        reader.close();
	}
	
	public static ScoreDoc[] searchIndexQuery(String query) throws CorruptIndexException, IOException {
		bq = SearchQuery.createBooleanQuery(indexFile,query);
        int hitsPerPage = 10;
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(bq, hitsPerPage);
        
        ScoreDoc[] hits = docs.scoreDocs;
        return hits;
	}
	

}
