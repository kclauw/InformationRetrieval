package ranking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;

import server.InformationRetrieval;


public class RankingCosine extends Ranking {

	public RankingCosine(ScoreDoc[] hits, InformationRetrieval app) {
		super(hits, app);
	}

	private static HashMap<Integer, HashMap<String, Double>> tdfIdfScores;

	/** Calculate the cosine similarity between two files
	 * @param file1
	 * @param file2
	 * @return
	 */
	private static double GetCosineSimilarity(List<Double> file1, List<Double> file2) {
		double dot_product_total = 0;
		
		double sum_x = 0;
		double sum_y = 0;
		
		for (int i = 0; i < file1.size(); i++) {

			double x = file1.get(i).doubleValue();
			double y = file2.get(i).doubleValue();
			dot_product_total += x * y;
			sum_x += (double)Math.pow(x,2);
			sum_y += (double)Math.pow(y,2);
		}
		sum_x = Math.sqrt(sum_x);
		sum_y = Math.sqrt(sum_y);
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
