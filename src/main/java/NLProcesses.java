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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

//BUNU KULLANMIYORUZ
/**
 * Created with IntelliJ IDEA.
 * User: suleyman
 * Date: 4/1/13
 * Time: 6:44 PM
 * BUG: PROBLEM WITH QUOTATION MARKS!!!
 */
public class NLProcesses {

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

    public static String [] chunkPOSTagged(String [] toks, String [] tags)
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

    public static Span[] chunkAsSpanPOSTagged(String [] toks, String [] tags)
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

        return chunker.chunkAsSpans(toks, tags);
    }

    public static Span [] spansWithNounPhrases(String[] tokens, String[] tags, Span[] chunks)
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

    public static Span [] spansWithNounPhrases2(String[] tokens, String[] tags, Span[] chunks)
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

    public static boolean testNoun (String tag)
    {
        if(tag.contentEquals("NNP"))
            return false;
        else if(tag.contentEquals("POS"))
            return false;
        else if(tag.contentEquals("DT"))
            return false;
        else if(tag.contains("PRP"))
            return false;
        else return true;
    }

    public static String [] getNounPhrases(String[] sentence, Span[] namePhrasesSpan)
    {
        ArrayList<String> nounPhrases = new ArrayList<String>();

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



    public static void main(String[] args) {

        String [] sentences;
        ArrayList<String> nounPhrasesList = new ArrayList<String>();
        String [] nounPhrasesArray;

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

        //sentence =tokenizeSentence("Rockwell International Corp.'s Tulsa unit said it signed a tentative agreement extending its contract with Boeing Co. to provide structural parts for Boeing's"+
        //        " 747 jetliners. Rockwell said the agreement calls for it to supply 200 additional so-called shipsets for the planes.");

        //sentences = textToSentence("Rhodri Morgan (pictured) said his 1998 award \"made [his] name\", and has gone on to make no change for another award. In 2008 a special life-time achievement award was given to a talking bush for \"services to gobbledygook\", including succinct odes to hope and belief.");

        sentences = textToSentence("Three points. During this entire Big East season, Louisville guard Tim Henderson scored three points.\n" +
                "\n" +
                "Well, Henderson doubled his conference season output at the Final Four, and now Louisville is going to the title game.\n" +
                "\n" +
                "Louisville beat Wichita State 72-68 in the first national semifinal; the Cardinals face Michigan, who beat Syracuse 61-56 in the nightcap, for the national championship on Monday night. After Louisville lost reserve guard Kevin Ware to his now infamous leg fracture during last Sunday’s regional final against Duke, Louisville coach Rick Pitino mentioned that Henderson, a walk-on, would have to play some minutes, to give star guards Russ Smith and Peyton Siva a rest. Like most walk-ons, Henderson had spent most games barely getting off the bench. For the year, he played 88 minutes, and scored 16 points. And now Pitino would need him for the Final Four, in front of 75,000 fans at the Georgia Dome, and millions more on national television? Good luck with that one.\n" +
                "\n" +
                "(MORE: After Horrific Injury, What Now For Louisville’s Kevin Ware?)\n" +
                "\n" +
                "But Henderson, a Louisville native, more than delivered. He may have saved the season. With Louisville down 47-35 to Wichita State with almost 13 minutes left, Henderson hit three-pointers on back-to-back possessions to cut the lead in half, and completely change the feel of the game. Before Henderson’s big shots, the Shockers had total control. Afterwards, Louisville was energized, and brought a little buzz back to the building. “I felt it,” says Henderson. “I felt like they got back on their heels a little bit.”\n" +
                "\n" +
                "Now, a Louisville charge was all but assured.  “I think that was the big turning point right there,” says Louisville forward Wayne Blackshear. “After that, we started to get our press on.” Indeed, Louisville forced a few turnovers, and sixth man Luke Hancock got hot down the stretch. “Tim has to guard Russ every day in practice,” says Hancock. “A lot of times it’s not pretty. Russ kind of has his way with things. If you guard a guy like that every day, you’re going to get better, be a great defender. Once he hit those shots, I knew, ‘this was it.’ We were going to make our run now, or it wasn’t going to happen.” Hancock scored 20 points on 6 for 9 shooting, including 3 for 5 from three-point range; Smith had 21 points.\n" +
                "\n" +
                "(MORE: At Final Four, It’s Mike Rice On The Mind)\n" +
                "\n" +
                "But none were as important as Henderson’s quick half-dozen. On both shots, a teammate — first Hancock, then Smith — drove to the basket and kicked the ball out to a wide-open Henderson. “We watched it, they really want to keep you out of the paint,” says Henderson of Wichita State’s schemes. “They want you to beat them from the outside. We saw that in scouting, and Luke and Russ made the right plays.”\n" +
                "\n" +
                "Henderson says the first shot didn’t feel pure. “I thought it might have been short,” he says. “Once I saw it go in, it gave me the confidence. The second one felt perfect right away.” He erupted after making the second shot. “I just didn’t know whether to yell or play defense,” Henderson says. “I finally figured I’d yell then try to find my man.”\n" +
                "\n" +
                "After the game, Henderson’s teammates said his clutch shooting didn’t surprise them. Pitino called their bluff. “They’re being very kind,” Pitino says. “I was shocked. Not shocked that he made ‘em, just that he had the gumption to take them, then take it again. That’s pretty darn big on this stage. That shows incredible fortitude for a young man that hasn’t played any minutes.”\n" +
                "\n" +
                "Henderson, a Louisville kid who grew up loving Cardinals basketball, had a poster of Darrell Griffith — a.k.a. Dr. Dunkenstein — in his bedroom (Griffith led Louisville to the 1980 championship). While in high school, Henderson wrote about four letters to Pitino before the coach ever even acknowledged him. Henderson offered “to do whatever it takes” for a spot on the team. Henderson started going to pickup games at the Louisville gym to get noticed; Pitino liked his moxie. Henderson’s options were to walk on a Louisville, or accept a scholarship offer from Indiana University-Southeast, an NAIA school.  “I wanted to go to Louisville really bad,” Henderson says.\n" +
                "\n" +
                "Henderson will never buy a drink in his hometown again. “No comment,” he says, laughing. Though Henderson has dreamed of wearing Louisville red, walk-on life is bittersweet. “There were times when it was just unbelievable hard to keep on going,” Henderson says. “Going from high school and playing all the time to going to college, it’s just frustrating. It wears on you. You’ve just got to stick it out. There’s no point in being miserable.”\n" +
                "\n" +
                "In his Louisville basketball bio, Henderson says Leonardo DiCaprio would play him in a movie. Henderson says he doesn’t know where that comment came from. The school’s sports information department “just hands these forms out – I probably just saw him in a movie.”\n" +
                "\n" +
                "After tonight, however, that Tim Henderson flick seems like a good call. “Yeah,” Henderson says. “Could be.” Especially if there’s a sequel Monday night.");


        for(String sentence: sentences)
        {
            nounPhrasesList.addAll(Arrays.asList(anaMetodIsimlendirilecek(tokenizeSentence(sentence))));
        }

        nounPhrasesArray = new String[nounPhrasesList.size()];
        nounPhrasesList.toArray(nounPhrasesArray);

        pr(nounPhrasesArray);

        /*String [] tagged = POSTagTokens(sentence,true);

        String [] tagged2 = POSTagTokens(sentence,false);

        String [] chunked = chunkPOSTagged(sentence,tagged);

        Span [] chunked_as_span = chunkAsSpanPOSTagged(sentence,tagged);

        Span [] spansOfNametags = spansWithNounPhrases(sentence, tagged, chunked_as_span);

        Span [] spansOfNametags2 = spansWithNounPhrases2(sentence, tagged, chunked_as_span);

        String [] nounPhrases = getNounPhrases(sentence, spansOfNametags);

        String [] nounPhrases2 = getNounPhrases(sentence, spansOfNametags2);

        System.out.println("--------------------------------------------------------------------------------");
        pr(sentence);
        pr(tagged);
        pr(tagged2);
        pr(chunked);
        pr(chunked_as_span);
        pr(spansOfNametags);
        pr(nounPhrases);
        pr(nounPhrases2);*/

        //String [] sentences = textToSentence("I go to school. She's listening to the radio. The Foot in Mouth Award is given annually to those who test the bounds of modern English through their language. Although the award was first given in 1993, special acknowledgement had been given in 1991 to a Quayle with a big tent; most recipients have also highlighted as baby-kissers or ball-chasers. Rhodri Morgan (pictured) said his 1998 award \"made [his] name\", and has gone on to make no change for another award. A silver stone received the nod for finding light in the deepness, while a brown shadow was recognised for contributions to economics. Other recipients have literally been given the award for Campbell's Pasta, knowing, reading signs, and being inexperienced yet experienced, a fire-friendly mitt, or a giraffe called a snake. In 2008 a special life-time achievement award was given to a talking bush for \"services to gobbledygook\", including succinct odes to hope and belief.");

    }

    public static String [] anaMetodIsimlendirilecek(String[] sentence)
    {
        String [] tagged = POSTagTokens(sentence,true);

        /**/String [] tagged2 = POSTagTokens(sentence,false);

        String [] chunked = chunkPOSTagged(sentence,tagged);

        Span [] chunked_as_span = chunkAsSpanPOSTagged(sentence,tagged);

        //Span [] spansOfNametags = spansWithNounPhrases(sentence, tagged, chunked_as_span);

        Span [] spansOfNametags2 = spansWithNounPhrases2(sentence, tagged, chunked_as_span);

        //String [] nounPhrases = getNounPhrases(sentence, spansOfNametags);

        String [] nounPhrases2 = getNounPhrases(sentence, spansOfNametags2);
        /*pr(sentence);
        pr(tagged);
        pr(chunked);
        pr(chunked_as_span);
        pr(tagged2);*/
        return nounPhrases2;
    }

    public static void pr(Object [] o)
    {
        System.out.println(Arrays.toString(o));
    }
}
