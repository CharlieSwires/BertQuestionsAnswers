BERTQuestionAnswering
---------------------

<p>git clone https://github.com/CharlieSwires/BertQuestionsAnswers</p>

<p>Load the <a src="https://www.python.org/downloads/">python interpreter</a></p>
<p>py -m pip install --upgrade pip</p>
<p>py -m pip install transformers</p>
<p>py -m pip install torch --index-url https://download.pytorch.org/whl/cpu</p>
<p>cd BertQuestionsAnswers/src/main/python</p>
<p>edit test2.pt OUT_DIR = r"C:\Users\Dell\eclipse-workspace\bert\BertQuestionsAnswers\src\main\python" to be the correct path</p>
<p>py test2.pt</p>
<p>cd ../java/com/charlie</p>
<p>edit BertQuestionAnswering.java alter the path 			    .optModelPath(Paths.get("C:\\Users\\Dell\\eclipse-workspace\\bert\\BertQuestionsAnswers\\src\\main\\python\\traced.pt")) // <-- the .pt file itself</p>
<p>cd ../../..</p>
<p>mvn clean package</p>
<p>cd target</p>
<p>java -jar bert-questions-answers.jar</p>
<p>download and run post-man set the headers as follows</p>
<p><image src="https://github.com/CharlieSwires/BertQuestionsAnswers/Screenshot 2025-08-09 044513.png" /></p>
<p>ask your question as below:</p>
<p><image src="https://github.com/CharlieSwires/BertQuestionsAnswers/Screenshot 2025-08-09 045237.png" /></p>

