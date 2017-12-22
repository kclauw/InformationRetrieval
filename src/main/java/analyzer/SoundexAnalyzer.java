package analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.phonetic.PhoneticFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.commons.codec.Encoder;
import org.apache.commons.codec.language.Soundex;

import token.PermutermFilter;

import org.apache.lucene.analysis.standard.StandardFilter;


public class SoundexAnalyzer extends Analyzer {
	

	private Encoder encoder  = new Soundex();
	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new StandardTokenizer();
        TokenStream stream = new StandardFilter(tokenizer);
        stream = new PermutermFilter(stream);
        stream =  new PhoneticFilter(stream,encoder, true);
        stream = new LowerCaseFilter(stream);
        return new TokenStreamComponents(tokenizer, stream);
	}
	

}
