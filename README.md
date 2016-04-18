# CS6200---Information-Retrieval---Final-Project

The python code and files should go to /python

The Java code and files should go to /java


## Queries
This part of the project is implemented using Python 2.7.

### External Libraries Required
1. [unirest](http://unirest.io/): The HTTP request library;
2. [Natural Language Toolkit](http://www.nltk.org/): The NLP library;

### Query Expansion Techniques
[Words API](https://www.wordsapi.com/) is used to retrieve information about synonyms and derivants of English words. It has 2500 free hits each day, which is enough for our project.

We use two query expansion techniques: 

#### Synonym
Expand queries with words that have similar meanings. For example. the word "code" is expanded as 6 other words: "encipher", "cypher", "encrypt" "codification", "inscribe", and "cipher".

#### Derivants
Expand queries with derivants of the original term. For example, the word "code" is expanded as 3 other words: "codify", "coding", and "coder".

### Stopping
Stopping is a technique that exclude words that are too common to identify targeted documents. The provided [Common Words List](common_words) is used.

### Implementations
First, we tokenize the original 64 queries using [parseQueries.py](/python/parseQueries.py).

Then we use [Words API](https://www.wordsapi.com/) to retreive ([getWordInfo.py](/python/getWordInfo.py)) synonyms and derivants of each term that apper in tokenized queries. After that, we got two files [derivative](/python/derivative.txt) and [similar](/python/similar.txt). The first word of each row of these files comes from original queries, and remaining ones are expanded query terms. When expanding, we exclude common words ([CommonWords.py](/python/CommonWords.py)). Although not using stopping at this part, we still exclude common words when applying the queries expansion. 

Finally, use [getQueries.py](/python/getQueries.py) to appy the query expansion. At the same time, we also generate queries with stop words excluded. A combination of stopping and expansion (using derivants) is also applied. Together we got five files in total:

1. [Original Queries](/queries/originalQuriesTokens.txt)
2. [Expanded Queries Using Derivants](/queries/expandedQueriesTokensUsingDerivants.txt)
3. [Expanded Queries Using Synonyms](/queries/expandedQueriesTokensUsingDerivants.txt)
4. [Queries without Common Words (Stopping)](/queries/stoppedQueriesTokens.txt)
5. [Expanded Queries Using Derivants without Common Words](/queries/stoppedQueriesTokens.txt)

We use the five queries files above as following: In task 1, we use 1; In task 2, we combine 1 with 2 and 3, respectively; In task 3A, we use 4; In Phase 2 (7th run), we use the combination of 4 and 5.


