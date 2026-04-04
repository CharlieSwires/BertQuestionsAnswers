package com.charlie;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ai.djl.Device;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.nlp.DefaultVocabulary;
import ai.djl.modality.nlp.Vocabulary;
import ai.djl.modality.nlp.bert.BertFullTokenizer;
import ai.djl.modality.nlp.bert.BertToken;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.Batchifier;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

@RestController
@RequestMapping(path = "BERT")
public class BertQuestionAnswering {

    @GetMapping(path = "Question")
    public String getright() {
        return "Hello";
    }

    @PostMapping(path = "Question", consumes = "application/json")
    public String right(@RequestBody QuestionRequest request)
            throws IOException, ModelException, TranslateException {

        @SuppressWarnings("unchecked")
        Criteria<Map<String, String>, String> criteria = Criteria.builder()
                .setTypes((Class) Map.class, String.class)
                .optEngine("PyTorch")
                .optDevice(Device.cpu())
                .optModelPath(Paths.get(
                        "C:\\Users\\charl\\eclipse-workspace\\BertQuestionsAnswers\\src\\main\\python\\distilbert_qa_traced.pt"))
                .optTranslator(new QaTranslator())
                .build();

        try (ZooModel<Map<String, String>, String> model = ModelZoo.loadModel(criteria);
             Predictor<Map<String, String>, String> predictor = model.newPredictor()) {

            Map<String, String> input = new HashMap<>();
            input.put("question", request.getQuestion());
            input.put("context", request.getContext());

            return input.get("context")
                    + "\n"
                    + input.get("question")
                    + "\n"
                    + "Answer: "
                    + predictor.predict(input);
        }
    }

    static class QaTranslator implements Translator<Map<String, String>, String> {
        private static final int MAX_LEN = 384;
        private Vocabulary vocab;
        private BertFullTokenizer tokenizer;
        private List<String> lastWpTokens = null;

        @Override
        public void prepare(TranslatorContext ctx) throws Exception {
            URL vocabPath = ctx.getModel().getArtifact("vocab.txt");
            vocab = DefaultVocabulary.builder()
                    .addFromTextFile(vocabPath)
                    .optUnknownToken("[UNK]")
                    .build();
            tokenizer = new BertFullTokenizer(vocab, true);
        }

        static final int SEQ_LEN = 384;

        @Override
        public NDList processInput(TranslatorContext ctx, Map<String, String> input) {
            BertToken tok = tokenizer.encode(input.get("question"), input.get("context"));
            lastWpTokens = tok.getTokens();

            long[] idsRaw = lastWpTokens.stream()
                    .mapToLong(t -> vocab.getIndex(t))
                    .toArray();

            long[] ids = new long[SEQ_LEN];
            long[] mask = new long[SEQ_LEN];
            int n = Math.min(idsRaw.length, SEQ_LEN);
            System.arraycopy(idsRaw, 0, ids, 0, n);
            Arrays.fill(mask, 0, n, 1L);

            NDManager nd = ctx.getNDManager();
            NDArray idsNd = nd.create(ids).reshape(1, SEQ_LEN);
            NDArray maskNd = nd.create(mask).reshape(1, SEQ_LEN);

            return new NDList(idsNd, maskNd);
        }

        @Override
        public String processOutput(TranslatorContext ctx, NDList out) {
            NDArray startLogits = out.get(0);
            NDArray endLogits = out.get(1);

            int start = (int) startLogits.argMax(1).toLongArray()[0];
            int end = (int) endLogits.argMax(1).toLongArray()[0];
            if (end < start) {
                int t = start;
                start = end;
                end = t;
            }

            if (lastWpTokens == null || lastWpTokens.isEmpty()) {
                return "";
            }

            start = Math.max(0, Math.min(start, lastWpTokens.size() - 1));
            end = Math.max(0, Math.min(end, lastWpTokens.size() - 1));

            String span = String.join(" ", lastWpTokens.subList(start, end + 1));
            return span.replace(" ##", "");
        }

        @Override
        public Batchifier getBatchifier() {
            return null;
        }
    }
}