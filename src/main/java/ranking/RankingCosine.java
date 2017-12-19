package ranking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.util.BytesRef;

import server.InformationRetrieval;

public class RankingCosine extends Ranking {

	public RankingCosine(ScoreDoc[] hits, InformationRetrieval app) {
		super(hits, app);
		// TODO Auto-generated constructor stub
	}

	private static HashMap<Integer, HashMap<String, Double>> tdfIdfScores;

	private static InformationRetrieval app;
	private static ScoreDoc[] hits;

	private static double GetCosineSimilarity(List<Double> collection, List<Double> collection2) {
		double dot_product_total = 0;
		double length_product = collection.size() * collection2.size();
		
		double sum_x = 0;
		double sum_y = 0;
		
		for (int i = 0; i < collection.size(); i++) {

			double x = collection.get(i).doubleValue();
			double y = collection2.get(i).doubleValue();
			dot_product_total += x * y;
			sum_x += Math.sqrt((double)Math.pow(x,2));
			sum_y += Math.sqrt((double)Math.pow(y,2));
			//System.out.println(sum_x + " " + sum_y);
		}
		
		double cosineSimilarity = dot_product_total / (double)(sum_x * sum_y);
		return (double) cosineSimilarity;

	}

	/**
	 * This method initializes the heap documents
	 *
	 * 
	 * @return - Void
	 * @throws ParseException
	 * @throws IOException
	 */

	public void initializeHeap() throws ParseException, IOException {
		tdfIdfScores = getTFIdfScores();
		rankingPriorityQueue = new PriorityQueue<DocumentDistancePair>(DocumentComparatorMax);

		for (Integer docX : tdfIdfScores.keySet()) {

			double max_distance = 0;
			for (Integer docY : tdfIdfScores.keySet()) {

				HashMap<String, Double> termMapX = tdfIdfScores.get(docX);
				HashMap<String, Double> termMapY = tdfIdfScores.get(docY);

				ArrayList<Double> yVector = new ArrayList<Double>(termMapX.values());
				ArrayList<Double> xVector = new ArrayList<Double>(termMapY.values());

				if (docX != docY) {
					double distance = GetCosineSimilarity(xVector, yVector);
					if (distance > max_distance) {
						max_distance = distance;
					}

				}

			}

			rankingPriorityQueue.add(new DocumentDistancePair(max_distance, docX));

		}
	}

}
