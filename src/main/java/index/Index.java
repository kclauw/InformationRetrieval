package index;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
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

public class Index {
	
	private static Directory directory;
	private static Analyzer analyzer;


	
	public Index(Directory directory,Analyzer analyzer) throws IOException, SAXException, TikaException {
	
    	this.directory = directory;
    	this.analyzer = analyzer;
	}
	
	
	
	/**
	 * Indexes all files in a directory
	 * @param dataDirectory
	 * @param indexDirectory
	 * @throws IOException
	 * @throws SAXException
	 * @throws TikaException
	 */
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
		writer.close();
		//writer.deleteUnusedFiles();
		//System.out.println(writer.maxDoc() + " documents written");
	}
	


}
