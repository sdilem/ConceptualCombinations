import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/*
* This program finds bi-gram noun phrases from a given text file and writes them into an output file.
* TODO: Tokenizer doesn't work correctly. Can't tokenize ()[]" properly. Tokenize model should be trained well.
* TODO: More filters should be added to NLPTool.testNoun()
* */
public class Main {
    public static void main(String[] args) {

        NLPTool nlpTool = new NLPTool();

        ArrayList<String> nounPhrasesList = new ArrayList<>();
        String [] nounPhrasesArray;

        String input = fileToString("inputs/input1.txt");
        String [] sentences = nlpTool.textToSentence(input);

        for(String sentence: sentences)
        {
            nounPhrasesList.addAll(Arrays.asList(nlpTool.getNounPhrases(nlpTool.tokenizeSentence(sentence))));
        }

        nounPhrasesArray = new String[nounPhrasesList.size()];
        nounPhrasesList.toArray(nounPhrasesArray);

        //NLPTool.pr(nounPhrasesArray);
        stringToFile(nounPhrasesArray, "output.txt");
        System.out.println("Check ~projectFolder/outputs/output.txt!");
    }

    //reads from a text file. returns string.
    public static String fileToString(String path)
    {
        String input = "";
        try
        {
            InputStream inputStream = new FileInputStream(path);

            try {
                input = IOUtils.toString(inputStream);
                inputStream.close();

            }catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return input;
    }

    //writes given string array to file as element per line.
    public static void stringToFile(String [] strings, String fileName)
    {
        try
        {
            OutputStream outputStream = new FileOutputStream("outputs/"+fileName);

            try {
                for(String s: strings)
                {
                    IOUtils.write(s+"\n",outputStream);
                }
                outputStream.close();

            }catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}
