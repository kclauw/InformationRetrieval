package server;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
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
import org.apache.lucene.search.BooleanClause;
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
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
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
	
	private static IndexReader reader;
	private static IndexSearcher searcher;
	
	
	
	static Index indexFile;
	
	public InformationRetrieval(String dataDirectory,String indexDirectory) throws IOException, SAXException, TikaException {
		this.directory = FSDirectory.open(Paths.get(indexDirectory));
    	//this.analyzer = new SimpleAnalyzer();
    	this.analyzer = new SoundexAnalyzer();
    	this.index = new Index(directory,analyzer);
    	index.createIndex(dataDirectory,indexDirectory);
		this.reader = DirectoryReader.open(directory);
		this.searcher = new IndexSearcher(reader);

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
	
	/**
	 * This method calculates the TF-IDF score for each terms in the indexed
	 * documents
	 *
	 * @param total retrieved documents
	 * @return - Hashmap of TF-IDF score per each term in document 
	 * @throws ParseException
	 * @throws IOException 
	 */
	public static HashMap<Integer, HashMap> tfIdfScore(ScoreDoc[] hits) throws ParseException, IOException {
		
		
		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher searcher = new IndexSearcher(reader);
		
		
		int totalDocuments = hits.length;
		
		HashMap<Integer, HashMap> documentMap = new HashMap<Integer, HashMap>();
	
        for(int i=0;i<totalDocuments;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            HashMap<String, Float> termMap = new HashMap<String, Float>();
       
			TokenStream tokenStream =
			TokenSources.getAnyTokenStream(reader, hits[i].doc,
			"content", analyzer);
            Terms termVector = reader.getTermVector(docId,"contents");
            TermsEnum itr = termVector.iterator();
            
            System.out.println("DOCUMENT" + docId);
            BytesRef term = null;
            while((term = itr.next()) != null){
            	
                try{
                	ClassicSimilarity simi = new ClassicSimilarity();
	         

                    Term termInstance = new Term("contents",term);
                  //  System.out.println(term.utf8ToString());
                    // TF-IDF calculations
                    long tf = reader.totalTermFreq(termInstance);
                    long docCount = reader.docFreq(termInstance);
	                float idf = simi.idf(docCount, totalDocuments);
	                termMap.put(term.utf8ToString(), (tf * idf));
                    
                  //  System.out.println("term: "+term.utf8ToString()+", termFreq = "+tf+", docCount = "+docCount + " total document " + totalDocuments + "TF * IDF " + (tf * idf));
                
                }catch(Exception e){
                    System.out.println(e);
                }
            }  	
            
           // System.out.println(termMap);
            
            documentMap.put(docId, termMap);
        }
        
        System.out.println("-----------------");
        reader.close();
     
	    return documentMap;
	}


	
	public static void printResults(ScoreDoc[] hits) throws IOException, ParseException, InvalidTokenOffsetsException {
		
	
		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher searcher = new IndexSearcher(reader);
		
		//Part of highlighter
		SimpleHTMLFormatter simpleHTMLFormatter = new
				SimpleHTMLFormatter("<term>", "</term>");
				SimpleHTMLEncoder simpleHTMLEncoder = new SimpleHTMLEncoder();
				Highlighter highlighter = new Highlighter(simpleHTMLFormatter,
				simpleHTMLEncoder, new QueryScorer(bq));
			
        System.out.println("-----------------");
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            String text = d.get("content");
   
            System.out.println((i + 1) + ". " + d.get("file"));
           
			TokenStream tokenStream =
			TokenSources.getAnyTokenStream(reader, hits[i].doc,
			"content", analyzer);
					
            String[] frags = highlighter.getBestFragments(tokenStream, text, 10);
            for (String frag : frags)
            {

                System.out.println(Arrays.toString(getTagValues(frag).toArray()));
            }
          
            System.out.println("\n");
    
        }
        

        
        System.out.println("-----------------");
        reader.close();
	}
	
	public static ScoreDoc[] searchIndexQuery(String query,int hitsPerPage) throws CorruptIndexException, IOException {
		bq = SearchQuery.createBooleanQuery(indexFile,query);

        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
  
        TopDocs docs = searcher.search(bq, hitsPerPage);
        
        ScoreDoc[] hits = docs.scoreDocs;
        return hits;
	}
	

}
