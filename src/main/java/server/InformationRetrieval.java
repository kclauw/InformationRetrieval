package server;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.EncoderException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import analyzer.SoundexAnalyzer;
import index.Index;
import query.SearchQuery;

/**
 * this is the main class of out project. All the pre-indexing is done here
 *
 */
public class InformationRetrieval {

	public static Directory directory;
	private static Analyzer analyzer;
	private Index index;

	private static BooleanQuery bq;

	public static IndexReader reader;
	public static IndexSearcher searcher;

	// Ranking statistics
	private static Set<String> termsInCollection = new TreeSet<String>();

	static Index indexFile;

	public InformationRetrieval(String dataDirectory, String indexDirectory)
			throws IOException, SAXException, TikaException {
		this.directory = FSDirectory.open(Paths.get(indexDirectory));
		// this.analyzer = new SimpleAnalyzer();
		this.analyzer = new SoundexAnalyzer();
		this.index = new Index(directory, analyzer);
		index.createIndex(dataDirectory, indexDirectory);
		this.reader = DirectoryReader.open(directory);
		this.searcher = new IndexSearcher(reader);

	}

	public Directory getDirectory() {
		return directory;
	}

	public Analyzer getAnalyzer() {
		return analyzer;
	}

	public Set<String> getTermsInCollection() {
		return termsInCollection;
	}

	private static final Pattern TAG_REGEX = Pattern.compile("<term>(.+?)</term>");

	private static List<String> getTagValues(final String str) {
		final List<String> tagValues = new ArrayList<String>();
		final Matcher matcher = TAG_REGEX.matcher(str);
		while (matcher.find()) {
			tagValues.add(matcher.group(1));
		}
		return tagValues;
	}

	/**
	 * pretty print for the results
	 * @param hits
	 * @throws IOException
	 * @throws ParseException
	 * @throws InvalidTokenOffsetsException
	 */
	public static void printResults(ScoreDoc[] hits) throws IOException, ParseException, InvalidTokenOffsetsException {

		SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<term>", "</term>");
		SimpleHTMLEncoder simpleHTMLEncoder = new SimpleHTMLEncoder();
		Highlighter highlighter = new Highlighter(simpleHTMLFormatter, simpleHTMLEncoder, new QueryScorer(bq));

		System.out.println("-----------------");
		for (int i = 0; i < hits.length; ++i) {
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);
			String text = d.get("content");

			System.out.println((i + 1) + ". " + d.get("file"));

			TokenStream tokenStream = TokenSources.getAnyTokenStream(reader, hits[i].doc, "content", analyzer);

			String[] frags = highlighter.getBestFragments(tokenStream, text, 10);
			for (String frag : frags) {

				System.out.println(Arrays.toString(getTagValues(frag).toArray()));
			}

			System.out.println("\n");

		}
		System.out.println("-----------------");

	}

	public static ScoreDoc[] searchIndexQuery(String query, int hitsPerPage) throws CorruptIndexException, IOException, EncoderException {
		bq = SearchQuery.createBooleanQuery(query);

		TopDocs docs = searcher.search(bq, hitsPerPage);

		ScoreDoc[] hits = docs.scoreDocs;
		return hits;
	}

}
