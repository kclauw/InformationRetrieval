package clustering;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.util.BytesRef;

import clustering.Tuple;
import server.InformationRetrieval;

public class Kmeans {

	public static InformationRetrieval app;

	public Kmeans(InformationRetrieval app) {
		Kmeans.app = app;
	}
	
	public static HashMap <Integer, ArrayList<Integer>> clusters = null;

	public void cluster(int k, double thresh, int maxiter) throws IOException{
		HashMap<Integer, HashMap<String, Double>> tfIdfs = getTFIdfScores();
		boolean go = true;
		int iter=0;
		HashMap<Integer, HashMap<String, Double>> centroids = randomAssign(tfIdfs, k);
		

		while (go) {
			clusters = new HashMap <Integer, ArrayList<Integer>>();
			for (int centroid: centroids.keySet()) {
				clusters.put(centroid, new ArrayList<Integer>());
			}

			//cluster assignment step
			for (Integer doc : tfIdfs.keySet()) {
				int cent = 0;
				double dist = Double.POSITIVE_INFINITY;
				for (int centroid: centroids.keySet()) {
					double cdist = euclidean(centroids.get(centroid), tfIdfs.get(doc));
					if (cdist < dist) {
						dist = cdist;
						cent = centroid;
					}
				}
				clusters.get(cent).add(doc);
			}
			//System.out.println(clusters.keySet());
			//System.out.println(clusters);
			//System.out.println(tfIdfs);

			//centroid update step
			HashMap<String, Tuple<Double, Integer>> newCollection; //to get the sum of term and the term count
			HashMap<Integer, HashMap<String, Double>> newCentroids = new HashMap<Integer, HashMap<String, Double>>();
			for (int centId: clusters.keySet()) {
				newCollection = new HashMap<String, Tuple<Double, Integer>>();
				for(int docId : clusters.get(centId)) {
					HashMap<String, Double> doc = tfIdfs.get(docId);
					for (String term : doc.keySet()) {
						Tuple<Double, Integer> tup = newCollection.get(term);
						if(tup == null) {
							tup = new Tuple<Double, Integer>(doc.get(term), 1); 
						} else {
							tup.setX(tup.x + doc.get(term));
							tup.setY(tup.y + 1);
						}
						newCollection.put(term, tup);
					}
				}
				HashMap<String, Double> newDoc = new HashMap<String, Double>();
				for (String term : newCollection.keySet()) {
					double avg = newCollection.get(term).x / newCollection.get(term).y;
					newDoc.put(term, avg);
				}
				newCentroids.put(centId, newDoc);
			}

			//check break conditions
			double diff;
			go = false;
			int i =0;
			for (int cent: newCentroids.keySet()) {
				diff = euclidean(centroids.get(cent), newCentroids.get(cent));
				i++;
				//System.out.print("Iter in cent diff: ");
				//System.out.println(i);
				if (!go && diff > thresh) {
					//System.out.print("cent diff: ");
					//System.out.println(diff);
					go = true;
				}
			}
			if (++iter >= maxiter) go = false;
			centroids = newCentroids;
			//System.out.println(iter);
		}
	}


	private static HashMap<Integer,HashMap<String, Double>> randomAssign(HashMap<Integer, HashMap<String, Double>> tfIdfs, Integer k2) {
		Random randomGen = new Random();
		HashMap<Integer,HashMap<String, Double>> randomCentroids = new HashMap<Integer, HashMap<String, Double>>();
		int index;
		for(int i = k2; i > 0; i--){
			index = randomGen.nextInt(tfIdfs.size());
			HashMap<String, Double> item = tfIdfs.get(index);
			randomCentroids.put(index,item);
		}
		return randomCentroids;
	}

	static double euclidean(HashMap<String, Double> hashMap, HashMap<String, Double> hashMap2) {
		double dist = 0;
		HashMap<String, Double> vect = (hashMap.keySet().size() >= hashMap2.keySet().size()? hashMap:hashMap2);
		for (String i: vect.keySet()) {
			dist += Math.pow((hashMap.get(i)!=null ? hashMap.get(i):0) - (hashMap2.get(i)!=null ? hashMap2.get(i):0), 2) ;
		}
		return Math.sqrt(dist);
	}


	public HashMap<Integer, HashMap<String, Double>> getTFIdfScores() throws IOException {

		Set<String> termsInCollection = new TreeSet<String>();

		int totalDocuments = app.reader.numDocs();

		HashMap<Integer, HashMap<String, Double>> documentMap = new HashMap<Integer, HashMap<String, Double>>();

		for (int i = 0; i < totalDocuments; ++i) {
			int docId = i;
			Document d = app.searcher.doc(docId);
			HashMap<String, Double> termMap = new HashMap<String, Double>();

			TokenStream tokenStream = TokenSources.getAnyTokenStream(app.reader, docId, "content", app.getAnalyzer());
			Terms termVector = app.reader.getTermVector(docId, "contents");
			TermsEnum itr = termVector.iterator();
			BytesRef term = null;
			while ((term = itr.next()) != null) {

				try {
					ClassicSimilarity simi = new ClassicSimilarity();
					BM25Similarity simi2 = new BM25Similarity();
					Term termInstance = new Term("contents", term);
					termsInCollection.add(term.utf8ToString());
					// TF-IDF calculations
					long tf = app.reader.totalTermFreq(termInstance);
					long docCount = app.reader.docFreq(termInstance);
					//	System.out.println(term.utf8ToString() + " " + tf);
					double idf_value = idf(docCount, totalDocuments);
					// System.out.println("IIIDF" + simi.idf(docCount+1, totalDocuments));

					termMap.put(term.utf8ToString().toString(), (tf * idf_value));

				} catch (Exception e) {
					System.out.println(e);
				}
			}

			documentMap.put(docId, termMap);
		}
		// Add the empty terms
		for (Integer documentId : documentMap.keySet()) {

			for (String term : termsInCollection) {
				if (documentMap.get(documentId).get(term) == null) {
					documentMap.get(documentId).put(term, 0.0);
				}
			}
		}

		System.out.println("-----------------");
		// System.out.println(termsInCollection.size());

		return documentMap;
	}

	public static float idf(long docFreq, long docCount) {

		// return (float) Math.log(1 + (docCount - docFreq)/(docFreq));
		return (float) Math.log(1 + (docCount - docFreq + 0.5D) / (docFreq + 0.5D));
	}
	
	public static ArrayList<Integer> getCluster(int docId){
		ArrayList<Integer> result = null;
		for (int c : clusters.keySet()) {
			if (clusters.get(c).contains(docId))
				result = clusters.get(c);
		}
		return result;
	}


}