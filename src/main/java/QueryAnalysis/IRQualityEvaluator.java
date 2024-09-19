package QueryAnalysis;

import gr.uoc.csd.hy463.Topic;

import java.io.*;
import java.util.*;

import static gr.uoc.csd.hy463.TopicsReader.readTopics;

/**
 * Class that will Automate the Calculations of the EVALUATION metrics.
 *
 * readTopicsXML() - Function that will read the topics file (nxml) of the evaluation collection
 * -----------------------------------
 * The program should be able to read the * topics * file of the evaluation collection,         | ✔️ |
 *
 * send queries to the system you built in Phase A,                                             | ❌️ |
 * store the top results (as returned by your system) in a file named "results.txt",
 * then read the file with the relevance results from the evaluation collection (qrels.txt)
 * and by comparing it with the results of your system (in results.txt), calculate the values of the evaluation
 * metrics for each topic, and store them in a TSV (tab-separated values) file named "eval_results.txt".
 * Using the metrics, you will evaluate the overall effectiveness of your system.
 * By observing the results of the evaluation of effectiveness, you can make any variation you think in your system
 * (e.g. in the weighting of the index, in the query processor, in the relevance score calculation function, etc.)
 */
public class IRQualityEvaluator {

    static int number2_scores = 0;
    static int number1_scores = 0;
    static int number0_scores = 0;

    /**
     * Function that will read the topics file (nxml) of the evaluation collection
     * Returns a Hash Map of Strings with the Small texts - Summary or Description of the Topic
     * @param summaryOrDescription - 0 for summary, 1 for description
     * @throws Exception
     * @return HashMap<String,String> - Key: Topic Num - Type, Value: Summary or Description
     *
     */
    public HashMap<String,String> readTopicsXML(int summaryOrDescription) throws Exception {
        String filepath = "resources/topics.xml";
        ArrayList<Topic> topics = readTopics(filepath); // tzitzikas code
        Iterator i$ = topics.iterator();

        HashMap<String,String> topicMap = new HashMap<>();
        String key = "";

        while(i$.hasNext()) {
            Topic topic = (Topic) i$.next();
//            System.out.println("number:\t\t\t"+ topic.getNumber());
//            System.out.println("type:\t\t\t"+topic.getType());
//            System.out.println("summary:\t\t"+topic.getSummary());
//            System.out.println("description:\t"+topic.getDescription());
//            System.out.println("---------");
            key = topic.getNumber()+"-"+ topic.getType();
            if(summaryOrDescription == 0){
                topicMap.put(key,topic.getSummary());           // key: Topic Num - Type, Value: Summary
            }else {
                topicMap.put(key, topic.getDescription());      // key: Topic Num - Type, Value: Description
            }

        }
        return topicMap;
    }

    public void calculate_metrics() throws IOException {
        BufferedReader qrels_file = new BufferedReader(new FileReader("resources/qrels.txt"));
        BufferedReader results_file = new BufferedReader(new FileReader("resources/results_sorted.txt"));


        for(int i=1; i<=30; i++){
            number1_scores = 0;
            number2_scores = 0;
            Map<String,Integer> qrels =  loadQrels(qrels_file, i);

            double relevant_retrieved = 0;
            double irrelevant_retrieved = 0;

            double bpref = 0;
            double precision_i = 0;
            double aveP = 0;
            double dcg = 0;
            int retrieved = 1;          // just retrieved docs
            String line;

            // ################################################################################################
            // #  bpref   =   1/R    *    Σ_ r  (  ( 1  -  |  n ranked higher than r |  )/  min (R,N)    )
            // #
            // #       R        - Relevant Retrieved
            // #       N        - Non-Relevant Retrieved
            // #       r        - a Relevant retrieved doc
            // #       n        - a Non-Relevant retrieved doc
            // #       Σ_r      - happens r times ( r - number of relevant documents )
            // #       min(R,N) - finds which Set is smaller R or N
            // ################################################################################################

            while((line = results_file.readLine()) != null && Integer.parseInt(line.split("\t")[0]) == i){
                String[] parts = line.split("\t");

                if(qrels.containsKey(parts[2])) {       // the file exists

                    retrieved ++;                       // moved pos - read a doc

                    if (qrels.get(parts[2]) != 0) {     // RELEVANT ! --- RELEVANCE SCORE 1 , 2
                        relevant_retrieved++;
                        dcg = dcg + qrels.get(parts[2])/ (Math.log(retrieved + 1)/Math.log(2)); // DCG

                        bpref += irrelevant_retrieved;  // | n ranked higher than r | in this position plus all the previous
                        precision_i = precision_i + (relevant_retrieved / retrieved ) ; // summing all the precision_i
                    } else {
                        irrelevant_retrieved++;
                    }
                }
            }
            double idcg = 0;
            for(int j = 1; j<=relevant_retrieved; j++){ // ideal dcg, cutted based on the number of relevant docs we found between (results and qrels) we don't care about not judged
                if(j <= number2_scores){
                    idcg = idcg + 2/ (Math.log(j + 1)/Math.log(2)); // DCG
                }else {
                    idcg = idcg + 1/ (Math.log(j + 1)/Math.log(2)); // DCG
                }
            }
            if(relevant_retrieved>0){

//                System.out.println(i+" relevant: "+relevant_retrieved + " irrelevant: "+irrelevant_retrieved);
                if(relevant_retrieved>irrelevant_retrieved){
                    bpref = (relevant_retrieved-(bpref/irrelevant_retrieved))/relevant_retrieved; // 1/R * Σ_ r ( 1 - | n ranked higher than r | ) / min(R,N)
                }else{
                    bpref = (relevant_retrieved-(bpref/relevant_retrieved))/relevant_retrieved;
                }
                // 1/R * Σ_ r ( 1 - | n ranked higher than r | ) / min(R,N)
                aveP = precision_i / relevant_retrieved; // aveP is the sum calculated of all the precision_i
                idcg = dcg / idcg;

            }else{
                bpref = 0;
                aveP = 0;
                idcg = 0;
            }
            System.out.println("Topic: "+i+" bpref: "+bpref+" aveP: "+aveP+" idcg: "+idcg);
            writeEvalResultsFile(i,  bpref, aveP, idcg, "1");
        }

        qrels_file.close();
        results_file.close();
    }

    private static Map<String,Integer> loadQrels(BufferedReader filename, int topicNo) throws IOException {
        Map<String, Integer> qrels = new HashMap<>();

            String line;

            while ((line = filename.readLine()) != null && Integer.valueOf(line.split("\t")[0]) == topicNo){
                String[] parts = line.split("\t");
                String topic = parts[0];
                String docId = parts[2];
                int score = Integer.parseInt(parts[3]);

                if(score == 2) {
                    number2_scores++;
                }
                else if(score == 1) {
                    number1_scores++;
                }
                else if(score == 0) {
                    number0_scores++;
                }
                qrels.put(docId, score);
            }
        return qrels;
    }

    /**
     * Function that will write the results of the search in a file named "results.txt"
     * @param topicNo - the number of the topic
     * @param q0  - standard
     * @param pmcid - the name of the file
     * @param rank - the rank of the document
     * @param score - the score of the document
     * @param run_name - the name of the run
     * @throws IOException - if the file is not found
     */
    public void writeResultsFile(int topicNo, int q0, String pmcid, int rank, String score, String run_name) throws IOException {
        // Buffered Reader "results.txt" - open file with w+
        FileWriter fileWriter = new FileWriter("resources/results.txt", true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        //pre-process output
        score = score.replace("Score: ", "");
        // replace the last 5 chars of pmcid with ""
        pmcid  = pmcid.substring(0, pmcid.length() - 5);

        bufferedWriter.write(topicNo + "\t" + q0 + "\t" + pmcid + "\t" + rank + "\t" + score + "\t" + run_name + "\n");
        bufferedWriter.close();
    }

    /**
     * Function that will write the evaluation results in a file named "eval_results.txt"
     * @param topicNo  - the number of the topic
     * @param bpref - the bpref value
     * @param avep - the avep value
     * @param ndcg - the ndcg value
     * @param run_name - the name of the run
     * @throws IOException - if the file is not found
     */
    public void writeEvalResultsFile(int topicNo , double bpref, double avep, double ndcg, String run_name) throws IOException {
        // Buffered Reader "eval_results.txt" - open file with w+
        FileWriter fileWriter = new FileWriter("resources/eval_results.txt", true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        // TOPIC_NO BPREF_VALUE AVEP_VALUE NDCG_VALUE

        bufferedWriter.write(topicNo + "\t" + bpref + "\t" + avep + "\t" + ndcg + "\t" + run_name + "\n");
        bufferedWriter.close();
    }

    /**
     * Function that sorts the results of the run so that we have data sorted
     * in ascending order given the topicsNo
     * @throws IOException throws exc
     */
    public static void sortResultsFile() throws IOException {
        // read results from file (1st column)
        BufferedReader results_file = new BufferedReader(new FileReader("resources/results.txt"));
        List<String[]> lines = new ArrayList<>();

        String line;
        while ((line = results_file.readLine()) != null) {
            String[] parts = line.split("\t");
            lines.add(parts);
        }
        results_file.close();

        // Sort lines based on the topic_no
        lines.sort(Comparator.comparingInt(a -> Integer.parseInt(a[0])));

        // write the sorted lines back to a new file
        BufferedWriter writer = new BufferedWriter(new FileWriter("resources/results_sorted.txt"));
        for (String[] parts : lines) {
            writer.write(String.join("\t", parts));
            writer.newLine();
        }
        writer.close();
    }


    // testing the class and its functions

    public static void main(String[] args) {
//        try {
//            sortResultsFile();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        IRQualityEvaluator irQualityEvaluator = new IRQualityEvaluator();
        try {
            File file = new File("resources/eval_results.txt"); // replace with your file path

            if (file.exists()) {
                boolean result = file.delete();

                if(result) {
                    System.out.println("File deleted successfully");
                } else {
                    System.out.println("Failed to delete the file");
                }
            } else {
                System.out.println("File does not exist");
            }
//            irQualityEvaluator.readTopicsXML(0);
            irQualityEvaluator.calculate_metrics();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
