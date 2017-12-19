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
	
	
	 public static BooleanQuery add_term(String term) {
		BooleanQuery.Builder query = new BooleanQuery.Builder();
    	if (term.charAt(0) == '!'){
   		query.add(new BooleanClause(new WildcardQuery(new Term("text", term.substring(1))),BooleanClause.Occur.MUST_NOT));	
   	}else{
   		query.add(new BooleanClause(new WildcardQuery(new Term("text", term)),BooleanClause.Occur.MUST));
   	};
   	return query.build();
	 }
	 

	 static BooleanQuery proccessTermsBetweenBracket(String term){

		 
		  	BooleanQuery.Builder query = new BooleanQuery.Builder();
		  	
  		 	List<String> termElements = new ArrayList<String>();
  		 	List<Character> operatorElements = new ArrayList<Character>();
  		    create_term_operator(term,termElements,operatorElements);


		  	int it = 0;
			String term1;	
			String term2;
		  	for(char currentOperator :operatorElements) {
        	term1 = termElements.get(it);
        	term2 = termElements.get(it+1);

				 switch(currentOperator){
		            case '+':
		            	query.add(new BooleanClause(new WildcardQuery(new Term("text", term1)),BooleanClause.Occur.SHOULD));
		            	query.add(new BooleanClause(new WildcardQuery(new Term("text", term2)),BooleanClause.Occur.SHOULD));
		           	 	break;
		            case '^': 
		            	query.add(new BooleanClause(new WildcardQuery(new Term("text", term1)),BooleanClause.Occur.MUST));
		            	query.add(new BooleanClause(new WildcardQuery(new Term("text", term2)),BooleanClause.Occur.MUST));
		            	break;
		            default :
		            	//add_term(query,term1);
		            	break;
				 } 
	        	it++;
			 }
	  		 return query.build();	
	  	}
	 
	 
	 
		public static void create_term_operator(String query,List<String> termElements,List<Character> operatorElements) {
			 String[] terms = query.split(" ");
			 int i = 0;
			 while(i < terms.length) {
				String token = terms[i];
				char currentChar = token.charAt(0);
				char lastChar = token.charAt(token.length()-1);
				if(currentChar == '^' || currentChar == '+') {
					operatorElements.add(currentChar);
				}else if(token.charAt(0) == '(') {
					//Add the elements between brackets
					//operatorElements.add(currentChar);
					String bracket = "";
					
					while(lastChar != ')' && i < terms.length) {
						token = terms[i];
						lastChar = token.charAt(token.length()-1);
						bracket += token + " ";				
						i++;
					}
					i--;
					
					termElements.add(bracket.replaceAll("[)]", ""));
				}else {
					termElements.add(token);
				}	
				i++;
			 }
		}
		
		

	
	 static BooleanQuery createTermQuery(List<String> termElements,List<Character> operatorElements,String q) {

	
		  	BooleanQuery.Builder main_query = new BooleanQuery.Builder();
		  	BooleanQuery.Builder query = new BooleanQuery.Builder();
	
		  	

		  	
		  	List<BooleanQuery> processedTermElements = new ArrayList();
		  	
		  	//Loop over terms and transform elements to query
		  	for(String term :termElements) {
		  		
		  		if(term.charAt(0) == '(') {
		  			term = term.substring(1);
	       		    BooleanQuery bracketQuery = proccessTermsBetweenBracket(term);
	       		    processedTermElements.add(bracketQuery);
		  		}else {
		  			BooleanQuery singleQuery = add_term(term);
		  			processedTermElements.add(singleQuery);
		  		}
		  		
		  	}
		  	
		  	
		  	//In the case of a single term
		  	if(processedTermElements.size() == 1) {
		  		
		  		main_query.add(processedTermElements.get(0),BooleanClause.Occur.MUST);
		  		return main_query.build();	
		  	}
		  	
		  	
		  	//In the case of multiple terms
		  	int it = 0;
		  	for(char currentOperator :operatorElements) {
		  		switch(currentOperator){
	            case '+':
	            	main_query.add(processedTermElements.get(it),BooleanClause.Occur.SHOULD);
	            	main_query.add(processedTermElements.get(it+1),BooleanClause.Occur.SHOULD);
	           	 	break;
		  			  		
		  		case '^':
	            	main_query.add(processedTermElements.get(it),BooleanClause.Occur.MUST);
	            	main_query.add(processedTermElements.get(it+1),BooleanClause.Occur.MUST);
          	 		break;
	
		  		}
		  		it++;
		  	}
		  
	
	  		 return main_query.build();	
	  	}


	
	

		

	
	public static BooleanQuery createBooleanQuery(String query) {
		 List<String> termElements = new ArrayList<String>();
		 List<Character> operatorElements = new ArrayList<Character>();
		 create_term_operator(query,termElements,operatorElements);
		 BooleanQuery b = createTermQuery(termElements,operatorElements,query);
		 return b;
		}
	

	
	

}
