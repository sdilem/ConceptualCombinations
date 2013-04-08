import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: suleyman
 * Date: 4/7/13
 * Time: 8:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class NLPTool {
    SentenceModel sentenceModel;
    TokenizerModel tokenizerModel;
    POSModel posModel;
    ChunkerModel chunkerModel;


    /**
     * Default constructor initializes models from model files.
     */
    public NLPTool()
    {
        try
        {
            InputStream modelIn = new FileInputStream("models/en-sent.bin");

            sentenceModel = new SentenceModel(modelIn);

            modelIn.close();

            modelIn = new FileInputStream("models/en-token.bin");

            tokenizerModel = new TokenizerModel(modelIn);

            modelIn.close();

            modelIn = new FileInputStream("models/en-pos-maxent.bin");

            posModel = new POSModel(modelIn);

            modelIn.close();

            modelIn = new FileInputStream("models/en-chunker.bin");

            chunkerModel = new ChunkerModel(modelIn);

            modelIn.close();

        } catch (IOException e) {
            // Model loading failed, handle the error
            e.printStackTrace();
        }
    }

    /**
     * Divides text into sentences.
     * @param inputText text to be divided into sentences
     * @return a string array of sentences
     */
    public String [] textToSentence(String inputText)
    {
        SentenceDetectorME sentenceDetector = new SentenceDetectorME(sentenceModel);

        return sentenceDetector.sentDetect(inputText);
    }

    /**
     * Tokinizes given sentence.
     * WARNING! this function does not work properly because of some lacks in opennlp models.
     * It does not tokenize some special characters such as []"() properly.
     * @param sentence sentence to be tokenized
     * @return array of tokens
     */
    public String [] tokenizeSentence(String sentence)
    {
        Tokenizer tokenizer = new TokenizerME(tokenizerModel);

        return tokenizer.tokenize(sentence);
    }

    /**
     * Marks tokens corresponding word types.
     * @param tokens tokens to be tagged
     * @param onlyTags true returns only POS tags, false returns words and tags in 'word_TAG' format.
     * @return array of Part Of Speech tags.
     */
    public String [] POSTagTokens(String [] tokens, boolean onlyTags)
    {
        int l = tokens.length;
        String [] tokensCopy = new String[l];

        System.arraycopy(tokens,0,tokensCopy,0,l);


        POSTaggerME tagger = new POSTaggerME(posModel);
        tokens = tagger.tag(tokens);

        if(onlyTags)
            return tokens;
        else
        {
            for (int i = 0 ; i < l ; i++)
            {
                tokens[i] = tokensCopy[i]+"_"+tokens[i];
            }

            return tokens;
        }
    }

    /**
     * Divides a sentence in syntactically correlated parts of words, like noun groups, verb groups etc.
     * <b>Precondition:</b> Indices of tokens and their tags must be the same.
     * @param toks tokenized sentence as a token array
     * @param tags array of tags after POS tagging
     * @return array of chunk tags for each token in the input array
     */
    public String [] chunkPOSTagged(String [] toks, String [] tags)
    {
        ChunkerME chunker = new ChunkerME(chunkerModel);

        return chunker.chunk(toks,tags);
    }

    /**
     * Divides a sentence in syntactically correlated parts of words, like noun groups, verb groups etc.
     * Returns as Span.
     * <b>Precondition:</b> Indices of tokens and their tags must be the same.
     * @param toks tokenized sentence as a token array
     * @param tags array of tags after POS tagging
     * @return array of tags for each word group as a Span
     * @see opennlp.tools.util.Span
     */
    public Span[] chunkAsSpanPOSTagged(String [] toks, String [] tags)
    {
        ChunkerME chunker = new ChunkerME(chunkerModel);

        return chunker.chunkAsSpans(toks, tags);
    }

    /**
     * <b>UNCOMPLETED!!</b>
     * Currently returns noun phrases from a given chunk array
     * @param tokens not used currently
     * @param tags array of POS tags
     * @param chunks array of chunks
     * @return indices of noun phrases (+2)
     */
    public Span [] spansWithNounPhrases(String[] tokens, String[] tags, Span[] chunks)
    {
        ArrayList<Span> nounPhrases = new ArrayList<>();
        for( Span s : chunks)
        {
            if( s.length()>= 2 && s.getType().contentEquals("NP"))
                nounPhrases.add(s);
        }
        Span [] s = new Span[nounPhrases.size()];
        return nounPhrases.toArray(s);
    }

    /**
     * Finds bigram noun phrases in a chunk array after testing.
     * @param tags POS tags of words
     * @param chunks Spans of each chunk
     * @return Spans of bigram noun phrases
     */
    public Span [] spansWithNounPhrases2(String[] tags, Span[] chunks)
    {
        ArrayList<Span> nounPhrases = new ArrayList<>();
        for( Span s : chunks)
        {
            if( s.length()== 2 && s.getType().contentEquals("NP"))
            {
                int i = s.getStart();
                if(testNoun(tags[i]))
                    if(testNoun(tags[i+1]))
                        nounPhrases.add(s);
            }
        }
        Span [] s = new Span[nounPhrases.size()];
        return nounPhrases.toArray(s);
    }

    /**
     * Tests whether given noun is not a proper noun, determiner or proposition.
     * <b>Should be improved</b>
     * @param tag noun tag
     * @return true if given tag is appropriate false if it's a determiner etc.
     */
    public static boolean testNoun (String tag)
    {
        return !tag.contentEquals("NNP")&& !tag.contentEquals("POS")&& !tag.contentEquals("DT")&& !tag.contains("PRP");
    }

    /**
     * Returns actual words from a given chunk Span.
     * <b>Precondition:</b> namePhrasesSpan array must be the one generated from sentence array
     * @param sentence array of the whole tokens
     * @param namePhrasesSpan Span of name phrases
     * @return noun phrases from sentence array
     */
    public String [] getNounPhrases(String[] sentence, Span[] namePhrasesSpan)
    {
        ArrayList<String> nounPhrases = new ArrayList<>();

        for( Span s: namePhrasesSpan)
        {
            String nounPhrase = "";

            for( int i = s.getStart(); i < s.getEnd(); i++)
            {
                nounPhrase += (sentence[i]+" ");
            }

            nounPhrases.add(nounPhrase);
        }

        String [] nounPhrasesFinal = new String [nounPhrases.size()];

        return nounPhrases.toArray(nounPhrasesFinal);
    }

    /**
     * This method takes a tokenized sentence and return noun phrases in that sentence by using NLP tools.
     * @param sentence tokenized sentence
     * @return noun phrases
     */
    public String [] getNounPhrases(String[] sentence)
    {
        //call POS tagger
        String [] tagged = POSTagTokens(sentence,true);

        //call chunker
        Span [] chunked_as_span = chunkAsSpanPOSTagged(sentence,tagged);

        //call bigram NP spans
        Span [] spansOfNametags2 = spansWithNounPhrases2(tagged, chunked_as_span);

        //call bigram noun phrases
        return getNounPhrases(sentence, spansOfNametags2);
    }

    /**
     * this little method prints an array to string. written as a code simplifier.
     * used frequently during development phase.
     * @param o array to be printed
     */
    public static void pr(Object [] o)
    {
        System.out.println(Arrays.toString(o));
    }
}
