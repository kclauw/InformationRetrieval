package query;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;

import index.Index;

public class SearchQuery {
	


	
	 static BooleanQuery createTermQuery(List<String> termElements,List<Character> operatorElements,String q) {

		  	BooleanQuery.Builder query = new BooleanQuery.Builder();
		  	BooleanQuery.Builder query2 = new BooleanQuery.Builder();
		  	int it = 0;
		  	System.out.println(operatorElements);
		  	System.out.println(termElements);
		  	
		  	
		  	 String[] terms = q.split(" ");
		 	
			 //Filter the ( ) tags
			 
			 
			 // (A ^ B ^ C) ^ (A) 
			 
			 //Filter terms and boolean expressions
			 for(String token : terms) {
				char currentChar = token.charAt(0);
				
				switch(currentChar) {
				case '^' :
						operatorElements.add(currentChar);
						break;
				case '+' :
						operatorElements.add(currentChar);
						break;
				case '!' :
						termElements.add(token);
						break;
				default:termElements.add(token); 
						break;
							
				}
			 }
		  	
		  	
		

	  		for(char currentOperator :operatorElements) {

	  			 System.out.println("Current_operator : " + it);
	  			 String term1 = termElements.get(it);
	  			 String term2 = termElements.get(it+1);
	 
				 switch(currentOperator){
				  	
		            case '+':

		           	 	query.add(new BooleanClause(new TermQuery(new Term("text", term1)),BooleanClause.Occur.SHOULD));
		           	 	query.add(new BooleanClause(new TermQuery(new Term("text", term2)),BooleanClause.Occur.SHOULD));
		           	 	
		           	 	break;
		            case '^': 
		            	query.add(new BooleanClause(new WildcardQuery(new Term("text", term1)),BooleanClause.Occur.MUST));
		            	query.add(new BooleanClause(new WildcardQuery(new Term("text", term2)),BooleanClause.Occur.MUST));
		            	
		            	break;
				 } 
	        	if (term1.charAt(0) == '!'){
	        			query.add(new BooleanClause(new TermQuery(new Term("text", term1.substring(1))),BooleanClause.Occur.MUST_NOT));	
	        	};
			 	if (term2.charAt(0) == '!'){
	        		query.add(new BooleanClause(new TermQuery(new Term("text", term2.substring(1))),BooleanClause.Occur.MUST_NOT));	
			 	};
	        	it++;
			 }
	  		
	  		 return query.build();	
	  	}
	
	
	static //Retrieve the terms between bracekts
	 List<String> splitBrackets(String query) {
		 List<String> elements = new ArrayList<String>();
		 Matcher m = Pattern.compile("\\((.*?)\\)").matcher(query);

		 while(m.find()) {
			 //Filter the '(' symbols
			 String word = m.group(1);
			 if(word.charAt(0) == '(') {
				 word = word.substring(1);
			 }	 		 
			 elements.add(word);
		 }
		 return elements;
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
	
	
	public static BooleanQuery createBooleanQuery(Index indexFile,String query) {
		 List<String> termElements = new ArrayList<String>();
		 List<Character> operatorElements = new ArrayList<Character>();
		 
		 List<String> split  = splitBrackets(query);

		 for(String element: split) {
			 System.out.println("Ele" +element);
			 System.out.println(splitBrackets(element));
			 
			 //Create query
			 if(element.charAt(0) == '(') {
				 
			 }
			 
			 
			 //Add query
			 if(element.charAt(0) == ')') {
				 
				 
				 //End query
			 }
			 
		 }
		
		 
		
		 String[] terms = query.split(" ");
	
		 //Filter the ( ) tags
		 
		 
		 // (A ^ B ^ C) ^ (A) 
		 
		 //Filter terms and boolean expressions
		 for(String token : terms) {
			char currentChar = token.charAt(0);
			
			switch(currentChar) {
			case '^' :
					operatorElements.add(currentChar);
					break;
			case '+' :
					operatorElements.add(currentChar);
					break;
			case '!' :
					termElements.add(token);
					break;
			default:termElements.add(token); 
					break;
						
			}
		 }
	
		 //To match the size of the terms -> Indicates the end of the query

		// System.out.println(termElements);
		 BooleanQuery b = createTermQuery(termElements,operatorElements,query);
		 
	
	
       return b;
		}
	


	
	

}
