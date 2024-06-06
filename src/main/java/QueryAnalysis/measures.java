package QueryAnalysis;
import java.util.List;

public class measures {
    public static double precision(int relevant_retrieved, int retrieved) { //relevant_retrieved = total_relevant ∧ retrieved
        if (retrieved == 0) {
            return 0.0;
        }
        return (double) relevant_retrieved / retrieved;
    }

    public static double recall(int relevant_retrieved, int total_relevant) { //relevant_retrieved = total_relevant ∧ retrieved
        if (total_relevant == 0) {
            return 0.0;
        }
        return (double) relevant_retrieved / total_relevant;
    }

    public static double calculate_nDCG(List<Integer> retrieved,List<Integer> ideal, int size){
        double dcg = calculate_dcg(retrieved, size);
        double idcg = calculate_idcg(ideal, size);
        return  dcg / idcg;
    }

    public static double calculate_dcg(List<Integer> relevants, int size){
        double dcg = 0;
        for(int i = 1; i<=size; i++){
            dcg = dcg + (relevants.get(i-1)/ (Math.log(i + 1)/Math.log(2)));
        }
        return dcg;
    }

    public static double calculate_idcg(List<Integer> relevanceScores, int size) {
        relevanceScores.sort((a, b) -> Integer.compare(b, a)); // Sort in descending order

        return calculate_dcg(relevanceScores, size);
    }


    /**
     * Function that will calculate the Average Precision value
     * @param qrelsComponents - the components of the qrels file
     * @param resultsComponents - the components of the results file
     * @return
     */
    public double calculateAVEP(String[] qrelsComponents, String[] resultsComponents) {

        //  mean of the precisions for each postition
        // -----------------------------------------------
        // aveP = 1/R * Σ isrel(r)P(r)
        //  where,
        //      isrel (r) --> 0 /1
        // -----------------------------------------------

        double sumOfPrecisions = 0.0;
        int numberOfRelevantDocuments = 0;
        int totalNumberOfDocuments = resultsComponents.length;

        // Assuming that the relevance score is in the fourth position of the qrelsComponents array
        int relevanceScore = Integer.parseInt(qrelsComponents[3]);

        // If the document is relevant
        if (relevanceScore > 0) {
            numberOfRelevantDocuments++;
            sumOfPrecisions += (double) numberOfRelevantDocuments / totalNumberOfDocuments;
        }
        // Calculate AVEP
        double avepValue = sumOfPrecisions / numberOfRelevantDocuments;
        return avepValue;
    }

}
