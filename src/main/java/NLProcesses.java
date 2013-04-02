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
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: suleyman
 * Date: 4/1/13
 * Time: 6:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class NLProcesses {
    /*lorem ipsum dolor sit amet sator arepo tenet opera rotas*/

    public static String [] textToSentence(String inputText)
    {
        SentenceDetectorME sentenceDetector = null;
        try
        {
            InputStream modelIn = new FileInputStream("models/en-sent.bin");

            SentenceModel model = new SentenceModel(modelIn);

            sentenceDetector = new SentenceDetectorME(model);

            modelIn.close();

        }catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }catch (IOException e)
        {
            e.printStackTrace();
        }

        return sentenceDetector.sentDetect(inputText);
    }

    public static String [] tokenizeSentence(String sentence)
    {
        Tokenizer tokenizer = null;

        try
        {

            InputStream modelIn = new FileInputStream("models/en-token.bin");

            TokenizerModel model = new TokenizerModel(modelIn);

            modelIn.close();

            tokenizer = new TokenizerME(model);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return tokenizer.tokenize(sentence);
    }

    public static String [] POSTagTokens(String [] tokens, boolean onlyTags)
    {
        POSTaggerME tagger = null;
        int l = tokens.length;
        String [] tokensCopy = new String[l];

        System.arraycopy(tokens,0,tokensCopy,0,l);

        try {
            InputStream modelIn = new FileInputStream("models/en-pos-maxent.bin");
            POSModel model = new POSModel(modelIn);
            modelIn.close();
            tagger = new POSTaggerME(model);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

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

    public static String[] chunkPOSTagged(String [] toks, String [] tags)
    {
        InputStream modelIn = null;
        ChunkerModel model = null;

        try {
            modelIn = new FileInputStream("models/en-chunker.bin");
            model = new ChunkerModel(modelIn);
        } catch (IOException e) {
            // Model loading failed, handle the error
            e.printStackTrace();
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (IOException e) {
                }
            }
        }
        ChunkerME chunker = new ChunkerME(model);

        return chunker.chunk(toks,tags);
    }

    public static void main(String[] args) {

       /* try
        {
            InputStream modelIn = new FileInputStream("models/en-sent.bin");

            FileInputStream inputStream = new FileInputStream("inputs/input1.txt");

            try {
                String everything = IOUtils.toString(inputStream);
                inputStream.close();

                try {
                    SentenceModel model = new SentenceModel(modelIn);

                    SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);

                    String sentences[] = sentenceDetector.sentDetect(everything);

                    for(String s: sentences)
                    {
                        System.out.println(s);
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    if (modelIn != null) {
                        try {
                            modelIn.close();
                        }
                        catch (IOException e) {
                        }
                    }
                }
            }catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }*/

        //String [] sentences = textToSentence("I go to school. She's listening to the radio. The Foot in Mouth Award is given annually to those who test the bounds of modern English through their language. Although the award was first given in 1993, special acknowledgement had been given in 1991 to a Quayle with a big tent; most recipients have also highlighted as baby-kissers or ball-chasers. Rhodri Morgan (pictured) said his 1998 award \"made [his] name\", and has gone on to make no change for another award. A silver stone received the nod for finding light in the deepness, while a brown shadow was recognised for contributions to economics. Other recipients have literally been given the award for Campbell's Pasta, knowing, reading signs, and being inexperienced yet experienced, a fire-friendly mitt, or a giraffe called a snake. In 2008 a special life-time achievement award was given to a talking bush for \"services to gobbledygook\", including succinct odes to hope and belief.");

        String [] ss =tokenizeSentence("Rockwell International Corp.'s Tulsa unit said it signed a tentative agreement extending its contract with Boeing Co. to provide structural parts for Boeing's"+
                                        " 747 jetliners. Rockwell said the agreement calls for it to supply 200 additional so-called shipsets for the planes.");

        String [] dd = POSTagTokens(ss,false);

        String [] chunked = chunkPOSTagged(ss,dd);

        for(String s: dd)
        {
            System.out.print(s+" ");
        }
    }
}
