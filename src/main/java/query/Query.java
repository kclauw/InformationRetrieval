package query;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import index.Index;

public class Query {
	

	
	
	BooleanQuery createBooleanQuery(Index indexFile) {
		 	BooleanClause b = new BooleanClause(
	                new TermQuery(new Term("file", "CACM-0001.html")),
	                BooleanClause.Occur.SHOULD);
	        
	        BooleanClause b2 = new BooleanClause(
	                new TermQuery(new Term("file", "CACM-0002.html")),
	                BooleanClause.Occur.MUST);
	
	   
	        BooleanQuery bq = new BooleanQuery.Builder().add(b).add(b2).build();
	        return bq;
	}
	
	

}
