
# HY463 - Information Retrieval Project

This repository contains the source code and necessary files for the **HY463 - Information Retrieval** course project. The project involves implementing an **inverted file structure** using the method of **partial indexing** for handling large data collections, similar to the sample collection provided in `/Resources/MiniCollection`. The created inverted file (IF) is then utilized for performing searches on the dataset. Additionally, the project includes evaluation mechanisms to assess the performance of the system and analyze the search results.

## Project Structure

```
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
                                           |_________ IRQualityEvaluator.java
                                           |_________ measures.java
                                           |_________ QueryEditor.java
                                           |_________ QuerySenderGUI.java
                             |_________ Search
                                           |_________ Search.java
                             |_________ Stemming
                                           |_________ Stemming.java
```

### Key Directories and Files

- **libs/**: Contains essential libraries like `BioReader.jar` and `Stemmer.jar` required for reading biological data and stemming, respectively.
- **resources/**: Contains collections and important files for indexing and searching, such as the `MiniCollection`, stopword lists, and the inverted file components (`VocabularyFile.txt`, `PostingFile.txt`, etc.).
- **src/main/java/**: The main source folder with the Java code divided into packages for indexing, searching, stemming, and evaluation.
- **results.txt, results_sorted.txt**: Store search results and their sorted versions.
- **eval_results.txt**: Contains the evaluation of the search engine's performance using precision and recall metrics.

## How to Run the Project

### Option 1: Run via Command Line

1. Open the terminal and navigate to the project directory.
2. Use the following command to execute the project using the `java` command:

   ```bash
   C:\Users\< YOUR_USERNAME >\.jdks\openjdk-22\bin\java.exe -jar project_463.jar
   ```

   Make sure to replace `< YOUR_USERNAME >` with your actual username.

### Option 2: Run via IDE

1. Open the project in your preferred Java IDE (such as IntelliJ IDEA or Eclipse).
2. Inside the IDE, run the following classes:

   - **`QuerySenderGUI.main`**: Provides a GUI for sending search queries.
   - **`IRQualityEvaluator.main`**: Evaluates the quality of the search engine's results based on predefined metrics.

### Notes
- The project requires Java 8+ for successful execution.
- Ensure the project structure remains intact, including all resources and libraries.

## Project Description

The project centers around building an inverted file (IF) structure to efficiently index and search large document collections. This is achieved using **partial indexing**, which handles large datasets by breaking the indexing process into manageable segments. The project workflow includes:

1. **Indexing**: The `pindexing.java` class is responsible for creating the inverted file structure, consisting of:
   - **Vocabulary File**: A list of terms extracted from the document collection.
   - **Posting File**: Stores the document IDs and positions for each term.
2. **Search**: The `Search.java` class uses the inverted file to search for documents that match a given query.
3. **Stemming**: The `Stemming.java` class ensures that different forms of the same word are treated as equivalent by reducing words to their root forms.
4. **Stopword Removal**: Common words like "the" or "and" are removed from the indexing and search processes to enhance efficiency.
5. **Evaluation**: The `IRQualityEvaluator.java` class evaluates the search engine's results using metrics such as precision and recall, based on relevance judgments in `qrels.txt`.

### MiniCollection

The project uses a **MiniCollection** as a sample dataset for indexing and search. This collection simulates the process of handling larger document collections through partial indexing.

## Evaluation

The system includes tools for evaluating search results against a set of relevance judgments. After a set of queries is executed, results are generated in `results.txt` and can be compared to the ground truth in `qrels.txt`.

### Key Files for Evaluation:
- **eval_results.txt**: Stores the evaluation output after running the IR system.
- **qrels.txt**: Contains relevance judgments for each query.
- **topics.xml**: A set of predefined queries used for testing and evaluation.

## Requirements

- **Java Development Kit (JDK) 8 or higher**.
- **BioReader.jar** and **Stemmer.jar** must be included in the classpath for successful execution.
- Ensure that the provided stopword lists (`stopwordsEn.txt`, `stopwordsGr.txt`) are correctly configured.

## License

This project is developed for the **HY463 - Information Retrieval** course as part of an academic project. It is intended for educational purposes only.


