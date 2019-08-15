import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.File; 
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.*; 
/**
 * FrequencyMap
 * 
 *  1) Makes a FrequencyMap object that is a Map of words and integers.
 *  2) Adds words to the FrequencyMap objects.
 *  3) Records the number of times a word follows a certain string sequence.
 *  4) Creates list of words to return as suggestions for a particular word based on what 
 *  has been encountered through the text file that was read in. 
 *  
 *  @author Anaïs Sarrazin & Jack O'Connor
 * 
 */
public class FrequencyMap
{
    private Map<String, Integer> probMap;   // initialise a Map of word frequencies
    private static final Random generator = new Random();  // creates a random number generator


    /**
     * Constructor method that creates new FrequencyMap objects which will map words to the frequencies 
     * in which they occur. Uses the HashMap implementation to do this.
     */
    public FrequencyMap()
    {
        this.probMap = new HashMap<String, Integer>();
    }
    
    
    /**
     * addWord
     * Adds words to FrequencyMap objects in order to update the frequencies in which the words occur 
     * after certain String sequences.
     * 
     * @param word a word representing the specific key in the FrequencyMap
     * 
     */
    public void addWord(String word) {
        if (this.probMap.containsKey(word)) { //if the word is already a key in the Map
            int frequency = this.probMap.get(word);  
            frequency++; //increases the frequency integer by one 
            this.probMap.put(word, frequency); //reassigns the value of the word in the Map
        } else { //if the word is not a key in the Map
            int frequency = 1;
            this.probMap.put(word, frequency); //assign the value of the word in the Map
        }
    }
        
    
   
   /**
    * pickNewWords
    * returns a list of word suggestions based on what words have been recorded as following 
    * the particular word that is given. 
    * 
    * @param lastWord String representing the word in question
    * @param dictionary Map representing the part of speech information
    * @return List with the word suggestions in it
    * 
    */
    public List<String> pickNewWords(String lastWord, HashMap<String, String> dictionary) {
        String lastWordLower = lastWord.toLowerCase();
        //Creates a new ArrayList to store the words that follow the string sequence
        List<String> probList = new ArrayList<String>(); 
        for (String word : this.probMap.keySet()){     //for each word key in the Map
            int num = this.probMap.get(word);      //frequency of the word
            
            for (int index = 0; index < num; index++) { 
                probList.add(word); 
                
            }
        }
        
        String posLastWord = dictionary.get(lastWordLower); // gets the value/part of speech of the word 
        if(posLastWord == null) {
            posLastWord = "n";
        }

        // list holding the words that usually follow the part of speech of the last word inputted
        List<String> newWords = new ArrayList<String>();  
        

        for(String word: this.probMap.keySet()) { 
            newWords.add(word);
            
            /*
             * this contains the "grammar rules" information that we started to impose on the word selection process
             * but decided that imposing rule based system on dynamic English language rules was inaccurate
    
            /*
            String posAssociativeWord = dictionary.get(word);// gets the part of speech of associative word
            if(posAssociativeWord == null) {
                posAssociativeWord = "n"; //if null, then probably proper noun, so make it a noun
            } else if (posAssociativeWord == "v" && word.length() > 5) {
                String ending = word.substring(word.length() - 3);
                if(ending.equals("ing")) {
                    posAssociativeWord = "n";
                }
            }
            

            if(posLastWord.equals("n") && (posAssociativeWord.equals("v") || posAssociativeWord.equals("prep"))){ 
                newWords.add(word); //verb follows noun
            } else if(posLastWord.equals("adj") && (posAssociativeWord.equals("adj") || posAssociativeWord.equals("n"))) {
                newWords.add(word); //adjective or noun follows adjective 
            } else if(posLastWord.equals("v") && (posAssociativeWord.equals("conj") || posAssociativeWord.equals("n"))) {
                newWords.add(word); //verb is followed by conjunction or pronoun 
            } else if(posLastWord.equals("pron") && (posAssociativeWord.equals("v")|| posAssociativeWord.equals("n"))) {
                newWords.add(word); //pronoun followed by verb
            } else if(posLastWord.equals("prep") && (posAssociativeWord.equals("n") || posAssociativeWord.equals("adj"))){
                newWords.add(word);
            } else if (posLastWord.equals("n") && (posAssociativeWord.equals("pron") || posAssociativeWord.equals("n") )){
                newWords.add(word);
            } else if (posLastWord.equals("v") && (posAssociativeWord.equals("prep") || posAssociativeWord.equals("adj"))){
                newWords.add(word);
            } else if (posLastWord.equals("v") && (posAssociativeWord.equals("pron"))) {
                newWords.add(word);
            } else if (posLastWord.equals("conj") && (posAssociativeWord.equals("n") || posAssociativeWord.equals("v"))) {
                newWords.add(word);
            } else if (posLastWord.equals("prep") && posAssociativeWord.equals("v") ){
                newWords.add(word);
            }else {
                //System.out.println("***" + word + "*** should not follow " + lastWordLower);
            }
            */
        }

        int bestNum = 0;
        String best = "";
        // Use the word with the highest frequency rating and satisfies the part of speech part? 
        List<String> resultList = new ArrayList<String>();
        
        
        if(newWords.size() <= 3) {
            resultList = newWords;
            return resultList;
        }
        
        int suggestions = 0;
        while(suggestions < 3 && newWords.size() > 1 ) {
            
            for(String word: newWords) {
               int num =  this.probMap.get(word);           // frequency of word 
               if(num > bestNum) {
                   bestNum = num;
                   best = word;
                }
            }
            bestNum = 0;
            resultList.add(best);
            newWords.remove(best);
            best = "";
            suggestions++;
        }
        return newWords;
    }
    
    
    /**
     * Creates string representations of the FrequencyMap objects
     * 
     * @returns a string containing word keys associated with integer frequencies
     */
    public String toString() {
        String empty = ""; 
        for (String word : this.probMap.keySet()){ //for each key in the FrequencyMap
            //adds the word and its frequency to the string
            empty += "(" + word + ":" + this.probMap.get(word) + ")"; 
        }
        return empty;
    }
}
