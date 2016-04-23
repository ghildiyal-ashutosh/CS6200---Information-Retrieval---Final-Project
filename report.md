# CS6200 Information Retrieval Final Project

* Course Name: Information Retrieval
* Semester: Spring 2015
* Team Members: Wei Xu, Yu Xie, Yue Liu
* Intructor: Prof. Naji

## Introduction
This project uses a combination of Java and Python. All retrieval models themselves are written in Java, and everything else including query expansion, evaluation, and other text processing is written in Python.

## Literature and Resources
###  Retrieval Models
We used 3 retrieval models:

1. BM-25
2. Tf-Idf
3. Lucene (default setting)

BM 25 is the one selected to apply advanced searching options, including stopping, stemming, and query expansion.

### Stopping
Stopping is a technique that exclude words that are too common to identify targeted documents when evaluating scores of documents for given queries. The provided [Common Words List](common_words) is used. When applying stopping, these words are both excluded from documents and queries.

### Query Expansion
[Words API](https://www.wordsapi.com/) is used to retrieve information about synonyms and derivants of English words. It has 2500 free hits each day, which is enough for our project.

Even for search engines that do not apply stopping, we elimitate common words when expanding queries. These words would remain in queries, but we do not expand them. For example, it doesn't make any sense to expand query "what is cacm" into something like "what which whats is are am be cacm".

We use the same term weigts for original query terms and expanded terms, and two expansion techniques are used as follows: 

#### 1. Synonym
Expand queries with words that have similar meanings. For example. the word "code" is expanded as 6 other words: "encipher", "cypher", "encrypt" "codification", "inscribe", and "cipher".

#### 2. Derivants
Expand queries with derivants of the original term. For example, the word "code" is expanded as 3 other words: "codify", "coding", and "coder".

### External Libraries Required
1. [unirest](http://unirest.io/): The HTTP request library;
2. [Natural Language Toolkit](http://www.nltk.org/): The NLP library;

## Implementation and Discussion
### Query Expansion
First, we tokenize the original 64 queries using [parseQueries.py](/python/parseQueries.py).

Then we use [Words API](https://www.wordsapi.com/) to retreive ([getWordInfo.py](/python/getWordInfo.py)) synonyms and derivants of each term that apper in tokenized queries. After that, we got two files [derivative](/python/derivative.txt) and [similar](/python/similar.txt). The first word of each row of these files comes from original queries, and remaining ones are expanded query terms. When expanding, we exclude common words ([CommonWords.py](/python/CommonWords.py)).

Finally, use [getQueries.py](/python/getQueries.py) to appy the query expansion. At the same time, we also generate queries with stop words excluded. A combination of stopping and expansion (using derivants) is also applied. Together we got five files in total:

1. [Original Queries](/queries/originalQueriesTokens.txt)
2. [Expanded Queries Using Derivants](/queries/expandedQueriesTokensUsingDerivantsOriginalIncluded.txt)
3. [Expanded Queries Using Synonyms](/queries/expandedQueriesTokensUsingSynonymOriginalIncluded.txt)
4. [Queries without Common Words (Stopping)](/queries/stoppedQueriesTokens.txt)
5. [Expanded Queries Using Derivants without Common Words (Stopping)](/queries/stoppedExpandedQueriesTokensUsingDerivantsOriginalIncluded.txt)

We use the five queries files above as following: In task 1, we use 1; In task 2, we combine 1 with 2 and 3, respectively; In task 3A, we use 4; In Phase 2 (7th run), we use the combination of 4 and 5.

### Retrieval Models
TODO

### Query-By-Query Analysis for Stemming

### Evaluation
All evaluation values are obtained through [evaluation.py](/python/evaluation.py).

## Results

## Conclusions and Outlook

## Bibliography
1. Manning, Christopher D., Prabhakar Raghavan, and Hinrich Schütze. "Introduction to information retrieval/Christopher D." (2008).
2. Croft, W. Bruce, Donald Metzler, and Trevor Strohman. Search engines: Information retrieval in practice. Vol. 283. Reading: Addison-Wesley, 2010.
3. Course Notes and Slides of CS 6200 Spring 2016, Northeastern University.



