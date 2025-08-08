package com.charlie;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ai.djl.Application;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.nlp.DefaultVocabulary;
import ai.djl.modality.nlp.Vocabulary;
import ai.djl.modality.nlp.bert.BertFullTokenizer;
import ai.djl.modality.nlp.bert.BertToken;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Batchifier;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import ai.djl.translate.TranslatorFactory;

@RestController
@RequestMapping(path = "engine")
public class BertQuestionAnswering {

	@GetMapping(path = "right")
	public String right() throws IOException, ModelException, TranslateException {
		Translator<Map<String, String>, String> translator = new QaTranslator();

		Criteria<Map<String,String>, String> criteria = Criteria.builder()
			    .setTypes((Class) Map.class, String.class)
			    .optEngine("PyTorch")
			    .optModelPath(Paths.get("C:\\Users\\Dell\\eclipse-workspace\\bert\\BertQuestionsAnswers\\src\\main\\python\\traced.pt")) // <-- the .pt file itself
			    .optTranslator(new QaTranslator())
			    .build();
		
		try (ZooModel<Map<String, String>, String> model = ModelZoo.loadModel(criteria);
				Predictor<Map<String, String>, String> predictor = model.newPredictor()) {

			Map<String, String> input = new HashMap<>();
			input.put("question", "What color is the fox?");
			input.put("context", "The quick brown fox jumps over the lazy dog.");

			return "Answer: " + predictor.predict(input);
		}
	}

	static class QaTranslator implements Translator<Map<String, String>, String> {
		private static final int MAX_LEN = 384; // safe default
		private Vocabulary vocab;
		private BertFullTokenizer tokenizer;
		private List<String> lastWpTokens = null; // for decoding

		@Override
		public void prepare(TranslatorContext ctx) throws Exception {
			URL vocabPath = ctx.getModel().getArtifact("vocab.txt");
			vocab = DefaultVocabulary.builder()
					.addFromTextFile(vocabPath)
					.optUnknownToken("[UNK]")
					.build();
			tokenizer = new BertFullTokenizer(vocab, true); // lowercased (uncased)
		}

		static final int SEQ_LEN = 384;

		private long[] padOrTrunc(List<Long> ids) {
			long[] out = new long[SEQ_LEN];
			int n = Math.min(ids.size(), SEQ_LEN);
			for (int i = 0; i < n; i++) out[i] = ids.get(i);
			// remaining zeros are pad
			return out;
		}

		@Override
		public NDList processInput(TranslatorContext ctx, Map<String, String> input) {
			// Tokenize
			BertToken tok = tokenizer.encode(input.get("question"), input.get("context"));
			lastWpTokens = tok.getTokens(); // keep for decoding later

			// Convert tokens -> ids using the vocab
			long[] idsRaw = lastWpTokens.stream()
					.mapToLong(t -> vocab.getIndex(t))   // map each token to its vocab index
					.toArray();

			// Pad/truncate to the fixed length your traced model expects
			long[] ids = new long[SEQ_LEN];
			long[] mask = new long[SEQ_LEN];
			int n = Math.min(idsRaw.length, SEQ_LEN);
			System.arraycopy(idsRaw, 0, ids, 0, n);
			Arrays.fill(mask, 0, n, 1L);                 // 1 for real tokens, 0 for padding

			NDManager nd = ctx.getNDManager();
			NDArray idsNd  = nd.create(ids).reshape(1, SEQ_LEN);
			NDArray maskNd = nd.create(mask).reshape(1, SEQ_LEN);

			return new NDList(idsNd, maskNd);        }

		@Override
		public String processOutput(TranslatorContext ctx, NDList out) {
			// out[0] = start_logits [1, L], out[1] = end_logits [1, L]
			NDArray startLogits = out.get(0);
			NDArray endLogits   = out.get(1);

			int start = (int) startLogits.argMax(1).toLongArray()[0];
			int end   = (int) endLogits.argMax(1).toLongArray()[0];
			if (end < start) { int t = start; start = end; end = t; }

			if (lastWpTokens == null || lastWpTokens.isEmpty()) return "";

			// Clamp to valid range
			start = Math.max(0, Math.min(start, lastWpTokens.size() - 1));
			end   = Math.max(0, Math.min(end,   lastWpTokens.size() - 1));

			// Extract tokens and clean subword markers
			String span = String.join(" ", lastWpTokens.subList(start, end + 1));
			return span.replace(" ##", "");
		}

		@Override
		public Batchifier getBatchifier() {
		    return null;  // <-- was STACK
		}
	}
}
