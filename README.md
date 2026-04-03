BERTQuestionAnswering
---------------------

<p>download https://www.openlogic.com/openjdk-downloads OpenJDK17
<p>download git https://git-scm.com/downloads</p>
<p>git clone https://github.com/CharlieSwires/BertQuestionsAnswers</p>
<p>Load python 3.12 interpreter https://www.python.org/downloads</p>
<p>cd BertQuestionsAnswers/src/main/python</p>
<p>run ./runthis.sh</p>
<p>edit test2.pt OUT_DIR = r"C:\Users\charl\eclipse-workspace\bert\BertQuestionsAnswers\src\main\python" to be the correct path</p>
<p>cd ../java/com/charlie</p>
<p>edit BertQuestionAnswering.java alter the path .optModelPath(Paths.get("C:\\Users\\charl\\eclipse-workspace\\BertQuestionsAnswers\\src\\main\\python\\distilbert_qa_traced.pt"))
)) // <-- the .pt file itself</p>
<p>cd ../../..</p>
<p>download https://maven.apache.org/download.cgi</p>
<p>mvn clean package</p>
<p>cd target</p>
<p>java -jar bert-questions-answers.jar</p>
<p>download and run https://www.postman.com/downloads set the headers as follows</p>
<p><img src="Screenshot2025-08-09044513.png" /></p>
<p>ask your question as below:</p>
<p><img src="Screenshot2025-08-09045237.png" /></p>

