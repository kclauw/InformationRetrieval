package ranking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import server.InformationRetrieval;

public class RankingEuclideanDistance extends Ranking {

	public RankingEuclideanDistance(ScoreDoc[] hits, InformationRetrieval app) {
		super(hits, app);
		// TODO Auto-generated constructor stub
	}

	/**
	 * This method calculates the TF-IDF score for each terms in the indexed
	 * documents
	 *
	 * @param total
	 *            retrieved documents
	 * @return - Hashmap of TF-IDF score per each term in document
	 * @throws ParseException
	 * @throws IOException
	 */

	private static double GetEuclideanDistance(List<Double> collection, List<Double> collection2) {
		double diff_square_sum = 0.0;

		for (int i = 0; i < collection.size(); i++) {

			double x = collection.get(i).doubleValue();
			double y = collection2.get(i).doubleValue();
			diff_square_sum = diff_square_sum + (x - y) * (x - y);
		}

		// diff_square_sum = (collection - collection2) * (collection - collection2);
		return Math.sqrt((double) diff_square_sum);
	}

	public void initializeHeap() throws ParseException, IOException {

		tdfIdfScores = getTFIdfScores();
		// System.out.println(tdfIdfScores);
		rankingPriorityQueue = new PriorityQueue<DocumentDistancePair>(1,DocumentComparatorMin);

		for (Integer docX : tdfIdfScores.keySet()) {

			double min_distance = 500000.0;
			for (Integer docY : tdfIdfScores.keySet()) {

				HashMap<String, Double> termMapX = tdfIdfScores.get(docX);
				HashMap<String, Double> termMapY = tdfIdfScores.get(docY);

				ArrayList<Double> yVector = new ArrayList<Double>(termMapX.values());
				ArrayList<Double> xVector = new ArrayList<Double>(termMapY.values());

				if (docX != docY) {
					double distance = GetEuclideanDistance(xVector, yVector);
					if (distance < min_distance) {
						min_distance = distance;
					}

				}

			}

			// System.out.println("Document " + docX + " " + min_distance);
			rankingPriorityQueue.add(new DocumentDistancePair(min_distance, docX));

		}
	}

}
