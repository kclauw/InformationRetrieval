package analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.cjk.CJKWidthFilter;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.phonetic.DoubleMetaphoneFilter;
import org.apache.lucene.analysis.phonetic.PhoneticFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.commons.codec.Encoder;
import org.apache.commons.codec.language.RefinedSoundex;
import org.apache.commons.codec.language.Soundex;

import token.PermutermFilter;

import org.apache.lucene.analysis.standard.StandardFilter;


public class SoundexAnalyzer extends Analyzer {
	

	private Encoder encoder = new RefinedSoundex();
	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new StandardTokenizer();
       
        TokenStream stream = new DoubleMetaphoneFilter(tokenizer, 6, false);
     
        //stream = new PermutermFilter(tokenizer);
        stream = new PermutermFilter(tokenizer);
        stream = new PhoneticFilter(tokenizer, encoder, true);
        stream = new LowerCaseFilter(tokenizer);
       
        
        //Custom PermutermFilter
        
        
    
  
        return new TokenStreamComponents(tokenizer, stream);
	}
	

}
