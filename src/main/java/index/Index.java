package index;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
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
import org.apache.lucene.document.Field;

import analyzer.SoundexAnalyzer;
import config.Config;
import query.SearchQuery;

public class Index {
	
	private static Directory directory;
	private static Analyzer analyzer;


	
	public Index(Directory directory,Analyzer analyzer) throws IOException, SAXException, TikaException {
	
    	this.directory = directory;
    	this.analyzer = analyzer;
	}
	
	
	
	public static void createIndex(String dataDirectory,String indexDirectory) throws IOException, SAXException, TikaException {
    
		File documents = new File(dataDirectory);
		IndexWriterConfig conf = new IndexWriterConfig(analyzer);
		IndexWriter writer = new IndexWriter(directory, conf);
		writer.deleteAll();
        
		
		//Read all files in directory
		for (File file : documents.listFiles()) {
			Metadata metadata = new Metadata();
			ContentHandler handler = new BodyContentHandler();
			ParseContext context = new ParseContext();
			Parser parser = new AutoDetectParser();
		
			ParseContext pcontext = new ParseContext();
			FileInputStream inputstream = new FileInputStream(file);
			
		
			
			//Html parser 
		    HtmlParser htmlparser = new HtmlParser();
		    htmlparser.parse(inputstream, handler, metadata,pcontext);

			try {
				parser.parse(inputstream, handler, metadata, context);
			}
			catch (TikaException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
			finally {
				inputstream.close();
			}
			String text = handler.toString();
			
			text = text.replaceAll("\\d","");
			String fileName = file.getName();
			//System.out.println("Indexing :  " + fileName);
			
			
			Document doc = new Document();
			doc.add(new TextField("file", fileName, Store.YES));
			doc.add(new StoredField("content", text));
			
			FieldType myFieldType = new FieldType(TextField.TYPE_STORED);
			myFieldType.setStoreTermVectors(true);

			doc.add(new Field("contents", text, myFieldType));
		    
	
			doc.add(new TextField("text", text, Store.YES));
			writer.addDocument(doc);
		}
		
		
		writer.commit();
		//writer.deleteUnusedFiles();
		System.out.println(writer.maxDoc() + " documents written");
	}
	


}
