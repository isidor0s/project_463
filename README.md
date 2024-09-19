HY463 - Information Retrieval Project

This repository contains the source code and necessary files for the HY463 - Information Retrieval course project.
The project involves implementetion of inverted file structure using the method of partial indexing for big Data collection similar to the /Resources/MiniCollection
and utilizing the create IF for search. It also includes evaluation of the system and the results 


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

Key Files

    libs/BioReader.jar: Contains pre-built classes used for bio-data reading (provided from course).
    libs/Stemmer.jar: Used for word stemming operations (provided from course).
    resources/CollectionIndex: Stores indexes of the document collection.
    resources/if/: Contains important indexing and vocabulary files.
    resources/Stopwords/: Stopword files for English and Greek, used to filter out common words during indexing and searching.

How to Run the Project
Option 1: Run with Command Line

    Navigate to the folder where the project is located.

    Run the following command to execute the .jar file:

    bash

    C:\Users\< YOUR_USERNAME >\.jdks\openjdk-22\bin\java.exe -jar project_463.jar

    Replace < YOUR_USERNAME > with your actual username.

Option 2: Run via IDE

    Open the project_463.zip in your preferred Java IDE (such as IntelliJ IDEA or Eclipse).

    Locate and run the following classes inside the IDE:
        QuerySenderGUI.main: GUI to send queries for testing and evaluation. There is also option to select also a folder for indexing. 
        IRQualityEvaluator.main: Evaluates the quality of the search results.

Notes

    Make sure that the project structure and resources remain intact for successful execution.
    The libraries provided in the libs/ folder are essential for the functionality of the project.

Project Description

This project is focused on Information Retrieval (IR) and aims to evaluate the performance of a search engine using custom indexing, stemming, and query processing techniques. The project includes a GUI for sending queries and receiving results, along with tools for evaluating the precision and recall of these results.
Core Components

    Document & Term Data: Doc_voc_data contains Java classes for representing documents, terms, and utilities related to vocabulary processing.
    Query GUI: The QueryGUI class in the GUI package allows for user interaction to send queries and display results.
    Indexing: The pindexing.java class in the pIndexing package handles the indexing of the document collection.
    Stemming: The Stemming.java class provides functionality to perform word stemming during the indexing and search phases.
    Search: The Search.java class is responsible for searching through the indexed documents to return relevant results based on the queries.
    Evaluation: The IRQualityEvaluator.java and measures.java provide methods for evaluating the quality of search results using standard IR metrics such as precision and recall.

Evaluation

The project provides mechanisms to evaluate the quality of search results using the provided qrels.txt and results.txt. After running a set of queries, results are evaluated, and you can see sorted results in results_sorted.txt.
Files:

    eval_results.txt: Stores the evaluation results.
    qrels.txt: Relevance judgments for evaluating search engine output.
    topics.xml: Contains a set of topics (queries) used to evaluate the system.

Requirements

    Java 8+ is required to compile and run the project.
    Libraries from the libs/ folder must be included in the project classpath.

