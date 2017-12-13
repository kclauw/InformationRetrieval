package query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.AutomatonQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.automaton.Automata;
import org.apache.lucene.util.automaton.Automaton;
import org.apache.lucene.util.automaton.Operations;

public class PermutermQuery extends WildcardQuery {

	public PermutermQuery(Term term) {
		super(term);
		// TODO Auto-generated constructor stub
	}





	
	
	}