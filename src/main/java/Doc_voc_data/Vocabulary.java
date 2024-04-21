package Doc_voc_data;


import java.util.HashMap;
import java.util.List;

public class Vocabulary {
   HashMap<String, List<String>> vocabulary; // key: term, value: list of documents
   HashMap<String, document> DocList; // key: document, value: document object

    public void setVocabulary(HashMap<String, List<String>> vocabulary) {
        this.vocabulary = vocabulary;
    }
    public HashMap<String, List<String>> getVocabulary() {
        return vocabulary;
    }

    public void setDocList(HashMap<String, document> docList) {
        DocList = docList;
    }
    public HashMap<String, document> getDocList() {
        return DocList;
    }

    public Vocabulary () {
        this.vocabulary = new HashMap<>();
        this.DocList = new HashMap<>();
    }
}
