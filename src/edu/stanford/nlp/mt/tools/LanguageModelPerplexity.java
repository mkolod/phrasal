package edu.stanford.nlp.mt.tools;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import edu.stanford.nlp.mt.lm.LanguageModel;
import edu.stanford.nlp.mt.lm.LanguageModelFactory;
import edu.stanford.nlp.mt.util.IOTools;
import edu.stanford.nlp.mt.util.IString;
import edu.stanford.nlp.mt.util.IStrings;
import edu.stanford.nlp.mt.util.Sequence;
import edu.stanford.nlp.mt.util.Sequences;

/**
 * Evaluate the perplexity of an input file under a language model.
 * 
 * @author danielcer
 * @author Spence Green
 *
 */
public final class LanguageModelPerplexity {
  
  private LanguageModelPerplexity() {}
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      System.err
          .printf("Usage: java %s type:path [input_file] < input_file%n", LanguageModelPerplexity.class.getName());
      System.exit(-1);
    }

    String model = args[0];
    System.out.printf("Loading lm: %s...%n", model);
    LanguageModel<IString> lm = LanguageModelFactory.load(model);

    LineNumberReader reader = (args.length == 1) ? 
        new LineNumberReader(new InputStreamReader(System.in)) :
          IOTools.getReaderFromFile(args[1]);
    
        
    int wordCount = 0;
    double logSum = 0.0;
    final long startTimeMillis = System.nanoTime();
    for (String sent; (sent = reader.readLine()) != null;) {
      Sequence<IString> seq = IStrings.tokenize(sent);
      Sequence<IString> paddedSequence = Sequences.wrapStartEnd(seq, lm.getStartToken(),
          lm.getEndToken());
      final double score = lm.score(paddedSequence, 1, null).getScore();
      wordCount += paddedSequence.size() - 1;
      assert score != 0.0;
      assert ! Double.isNaN(score);
      assert ! Double.isInfinite(score);
      
      logSum += score;
      
      System.out.println("Sentence: " + sent);
      System.out.printf("Sequence score: %f score_log10: %f%n", score, score / Math.log(10.0));
    }
    reader.close();
    
    
    System.out.printf("Word count: %d%n", wordCount);
    System.out.printf("Log sum score: %e%n", logSum);
    System.out.printf("Log10 sum score: %e%n", logSum / Math.log(10.0));
    System.out.printf("Log2 sum score: %e%n", logSum / Math.log(2.0));
    System.out.printf("Log2 Perplexity: %e%n", Math.pow(2.0, -logSum / Math.log(2.0) / wordCount));
    System.out.printf("Log10 Perplexity: %e%n", Math.pow(10.0, -logSum / Math.log(10.0) / wordCount));
        
    double elapsed = (System.nanoTime() - startTimeMillis) / 1e9;
    System.err.printf("Elapsed time: %.3fs%n", elapsed);
  }
}
