package QueryAnalysis;

import gr.uoc.csd.hy463.Topic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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
        ArrayList<Topic> topics = readTopics(filepath);
        Iterator i$ = topics.iterator();

        HashMap<String,String> topicMap = new HashMap<>();
        String key = "";

        while(i$.hasNext()) {
            Topic topic = (Topic) i$.next();
            System.out.println("number:\t\t\t"+ topic.getNumber());
            System.out.println("type:\t\t\t"+topic.getType());
            System.out.println("summary:\t\t"+topic.getSummary());
            System.out.println("description:\t"+topic.getDescription());
            System.out.println("---------");
            key = topic.getNumber()+"-"+ topic.getType();
            if(summaryOrDescription == 0){
                topicMap.put(key,topic.getSummary());           // key: Topic Num - Type, Value: Summary
            }else {
                topicMap.put(key, topic.getDescription());      // key: Topic Num - Type, Value: Description
            }

        }
        return topicMap;
    }



    // testing the class and its functions

    public static void main(String[] args) {
        IRQualityEvaluator irQualityEvaluator = new IRQualityEvaluator();
        try {
            irQualityEvaluator.readTopicsXML(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
