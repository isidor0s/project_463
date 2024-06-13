#######################################################

STRUCTURE of the project should remain as is
#######################################################

    -- libs
    |_________ BioReader.jar
    |_________ Stemmer.jar
    -- out
    -- artifacts
    -- resources
    |_________ CollectionIndex
    |_________ if
                |_________ PostingFile.txt
                |_________ VocabularyFile.txt
                |_________ temp.txt
                |_________ DocumentsFile.txt
    |_________ MiniCollection
    |_________ Stemming
    |_________ Stopwords
                |_________ stopwordsEn.txt
                |_________ stopwordsGr.txt
    - eval_results.txt
    - qrels.txt
    - results.txt
    - results_sorted.txt
    - topics.xml
    -- src
    |_________ main
                |_________ java
                            |_________ Doc_voc_data
                                          |_________ document.java
                                          |_________ term_data.java
                                          |_________ utilities.java
                                          |_________ Vocabulary.java
                            |_________ GUI
                                          |_________ DirectorySelector.java
                                          |_________ QueryGUI.java
                            |_________ pIndexing
                                          |_________ pindexing.java
                            |_________ QueryAnalysis
                                          |_________ IRQualityEvaluator.java- measures.java
                                          |_________ QueryEditor.java
                                          |_________  QuerySenderGUI.java
                            |_________ Search
                                          |_________ Search.java
                            |_________ Stemmming
                                          |_________ Stemming.java

################################################################