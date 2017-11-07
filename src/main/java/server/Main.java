package server;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
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
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;


public class Main {
	
	public static final String indexPath = "index";
	
    public static void main(String[] args) throws Exception {
    	
        // 0. Specify the analyzer for tokenizing text.
        //    The same analyzer should be used for indexing and searching
        StandardAnalyzer analyzer = new StandardAnalyzer();

		File indexDir = new File(indexPath);
		File docs = new File("Data");
		
		Directory directory = FSDirectory.open(Paths.get(indexPath));
		IndexWriterConfig conf = new IndexWriterConfig(analyzer);
		IndexWriter writer = new IndexWriter(directory, conf);
		writer.deleteAll();
        
		for (File file : docs.listFiles()) {
			Metadata metadata = new Metadata();
			ContentHandler handler = new BodyContentHandler();
			ParseContext context = new ParseContext();
			Parser parser = new AutoDetectParser();
		
			ParseContext pcontext = new ParseContext();
			FileInputStream inputstream = new FileInputStream(file);
		      //Html parser 
		    HtmlParser htmlparser = new HtmlParser();
		    htmlparser.parse(inputstream, handler, metadata,pcontext);
		    System.out.println("Contents of the document:" + handler.toString());
		    System.out.println("Metadata of the document:");
		    String[] metadataNames = metadata.names();
		      
		    for(String name : metadataNames) {
		         System.out.println(name + ":   " + metadata.get(name));  
		    }
		      
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
			String fileName = file.getName();		
			
			Document doc = new Document();
			doc.add(new TextField("file", fileName, Field.Store.YES));
			
			
			/*
			for (String key : metadata.names()) {
				String name = key.toLowerCase();
				String value = metadata.get(key);
				System.out.println("Name " + name + "Value " + value);
				
			}*/
			
		}
		
		writer.commit();
		writer.deleteUnusedFiles();
		
		System.out.println(writer.maxDoc() + " documents written");
		
		
		/*
        // 0. Specify the analyzer for tokenizing text.
        //    The same analyzer should be used for indexing and searching
        StandardAnalyzer analyzer = new StandardAnalyzer();

        // 1. create the index
        Directory index = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter w = new IndexWriter(index, config);
        addDoc(w, "Lucene in Action", "193398817");
        addDoc(w, "Lucene for Dummies", "55320055Z");
        addDoc(w, "Managing Gigabytes", "55063554A");
        addDoc(w, "The Art of Computer Science", "9900333X");
        w.close();

        // 2. query
        String querystr = args.length > 0 ? args[0] : "lucene";

        // the "title" arg specifies the default field to use
        // when no field is explicitly specified in the query.
        Query q = new QueryParser("title", analyzer).parse(querystr);

        // 3. search
        int hitsPerPage = 10;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(q, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;

        // 4. display results
        System.out.println("Found " + hits.length + " hits.");
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println((i + 1) + ". " + d.get("isbn") + "\t" + d.get("title"));
        }

        // reader can only be closed when there
        // is no need to access the documents any more.
        reader.close();
        */
    }



}
