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
	}

	/**
	 * This method calculates the Euclidean distance between two files
	 *
	 * @param file1
	 *        file2  
	 * @return - double: the distance
	 * @throws ParseException
	 * @throws IOException
	 */

	private static double GetEuclideanDistance(List<Double> file1, List<Double> file2) {
		double diff_square_sum = 0.0;

		for (int i = 0; i < file1.size(); i++) {

			double x = file1.get(i).doubleValue();
			double y = file2.get(i).doubleValue();
			diff_square_sum = diff_square_sum + (x - y) * (x - y);
		}

		return Math.sqrt((double) diff_square_sum);
	}

	public void initializeHeap() throws ParseException, IOException {

		tdfIdfScores = getTFIdfScores();
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

			rankingPriorityQueue.add(new DocumentDistancePair(min_distance, docX));

		}
	}

}
