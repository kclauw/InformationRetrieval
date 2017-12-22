package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import config.Config;
import ranking.RankingCosine;
import ranking.RankingEuclideanDistance;

import org.apache.commons.codec.Encoder;
import org.apache.commons.codec.language.Soundex;
import org.apache.lucene.search.ScoreDoc;

import clustering.Kmeans;

/***
 * 
 * 
 *
 */
public class Main {

	public static void main(String[] args) throws Exception {

		// Create the index
		InformationRetrieval app = new InformationRetrieval(Config.DATA_DIR, Config.INDEX_DIR);

		String query = null;

		// prompt the user to enter their name

		System.out.print("Enter your query: ");
		// open up standard input, and buffer it
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

		query = bufferedReader.readLine();
		System.out.println("Retrieving documents containing : " + query.toLowerCase());
	
		//System.out.println("Encoder : " + encoder.encode("parameter"));

	  	Encoder encoder = new Soundex();
	  	String encoded_query = encoder.encode(query.toLowerCase()).toString().toLowerCase();
	  	String normal_query = query.toLowerCase();
	  	
	  	
	  	
	  	
		ScoreDoc[] results = app.searchIndexQuery(normal_query, 100);

		app.printResults(results);

		RankingEuclideanDistance euclidean = new RankingEuclideanDistance(results, app);
		RankingCosine cosine = new RankingCosine(results, app);

		int n = results.length - 1;
		
		System.out.println("Rank the documents according to euclidean distance");
		euclidean.initializeHeap();
		euclidean.printBestDocuments(n);
		
		System.out.println("\n");
		System.out.println("Rank the documents according to cosine distance");
		cosine.initializeHeap();
		cosine.printBestDocuments(n);
		
		System.out.println("\n");
		System.out.println("Clustering the documents using K-Means");
		Kmeans k = new Kmeans(app);
		k.cluster(4, 5, 100);
		for (ScoreDoc c : results) {
			System.out.print("Cluster for document ");
     		System.out.print(c.doc);
			System.out.print(": ");
			System.out.println(k.getCluster(c.doc));
		}
		app.reader.close();
		
	}

}
