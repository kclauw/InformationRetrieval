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
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;

public class SoundexAnalyzer extends Analyzer {
	


	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new LetterTokenizer();

    
        
       
       
        TokenStream stream = new DoubleMetaphoneFilter(tokenizer, 6, false);
        stream = new LowerCaseFilter(tokenizer);
        
        //   stream = new CJKWidthFilter(stream);  //Note this WidthFilter!  I believe this does the char width transform you are looking for.
     //   stream = new PorterStemFilter(stream); //Nothing stopping you using a second stemmer, really.
        return new TokenStreamComponents(tokenizer, stream);
	}
	

}
