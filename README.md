[See project report here](/report.md)

* All queries are stored in "quries" file.
    - File names that ends with "OriginalIncluded" include both original terms and expanded terms. These versions are used in our project.
    - All queries can be re-generated by running "getQueries.py" in "python" directory.
    - Note that when running "getQueries.py", we need two text files "similar.txt" and "derivative.txt" (in "python" directory). These files store all expansion terms in the 64 queries. To re-generate these files, we need to run "getWordInfo.py", which access the WORDS API (https://www.wordsapi.com/), and a free account only have 2500 hits per day (by one hit we get information about one word). It should be enough for two or three runs.

* For evaluation, simply run the file "evaluation.py" in "python" directory.
    - Note that when running evaluation, we need to comment line 46 and 47 of file "evaluation.py". However when running BM25, we need to uncomment these two lines (this is the only python file called by the java project).
