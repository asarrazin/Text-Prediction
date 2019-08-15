import java.util.Random;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File; 
import java.util.Scanner; 
import java.util.Map;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.*; 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * SequenceTable
 *  Reads a text file and stores string sequences and FrequencyMap objects within the 
 *  SequenceTable objects. Used to return lists of word suggestions based off of a particular word
 *  Reads an input text files into the table and creates FrequencyMaps, storing the FrequencyMaps 
 *  in the Map instance variable. Also stores sets for topicVerbs, topicNouns, topicAdjectives which 
 *  represent the words that if encountered will indicate that this table's topic is the topic of the
 *  sentence. 
 *  
 * @author Anaïs Sarrazin & Jack O'Connor
 */
public class SequenceTable
{
    private Map<String, FrequencyMap> seqTable;
    private static int kLevel;      
    private final static int resultTextLength = 500; // number of words generated in result string
    private final Random generator = new Random();
    private String firstWord = "";
    private String tableName = ""; //represents the name of the topic that this table represents
    private Set<String> topicNouns = new HashSet<String>();
    private Set<String> topicVerbs = new HashSet<String>();
    private Set<String> topicAdj = new HashSet<String>();
    private Set<String> genericWords = new HashSet<String>();
    
    private HashMap<String, String> dictionary;  // hashmap storing word & part of speech
     
    
    /**
     * Constructor that maps string sequences to FrequencyMaps.
     */
    public SequenceTable(String name)
    {
        this.seqTable = new HashMap<String, FrequencyMap>();
        this.tableName = name;
       
    }

    
    public String getTableName() {
        return this.tableName;
    }
    
    
    public Set<String> getTopicNouns() {
        return this.topicNouns;
    }
    
    
    public Set<String> getTopicVerbs() {
        return this.topicVerbs;
    }
    
    
    public Set<String> getTopicAdj() {
        return this.topicAdj;
    }
    
    
    public void setDictionary(HashMap<String, String> dict) {
        this.dictionary = dict;
    }
    
    
    /**
     * setGenericSet
     * adds the generic words to the GenericSet for the table
     * 
     * @param words the String representing the generic words to be put into the set
     */
    public void setGenericSet(String words) {
        String[] theLines = words.split("\\s+"); // Puts every word into its own spot in array
        ArrayList<String> newLines = new ArrayList<String>(Arrays.asList(theLines));
        for(int i = 0; i < newLines.size(); i++ ) {
            this.genericWords.add(newLines.get(i));
        }
    }
    
    
    /**
     * scanText
     * Prompts the user for filename and desired klevel of analysis. 
     * Then it reads in the text of the file. 
     * @return a string representation of the text in the file. 
     */
    public static String scanText() {
        Scanner scan = new Scanner(System.in); //creates a new scanner object
        System.out.println("Enter your filename: "); //prompts the user for a filename
        String fileName = scan.nextLine();
        String resultText = readFileAsString(fileName); //stores the text in a string
        return resultText;
    }
       
        
    /**
     * readFileAsString
     * Reads the file and returns a string representation of it.
     * @ param filename the name of the file input by the user. 
     * @ returns a string if the file is found in the computer's directory or null if the file
     *   was not recognized.
     *   
     *  @author Sean Barker 
     */
    private static String readFileAsString(String filename) {
        try {//if the file is found
            // return a string of the text within the file
            return new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) {//if the string is not found
            System.out.println("File name not found."); // print an error messagge
            return null;
        }
    }
    
    
    /**
     * addSequence
     * Maps the words to FrequencyMaps storing word frequencies.
     * 
     * @param lines the string of the text from the file that was read
     */
    public void addSequence(String lines) {
        
        String[] theLines = lines.split("\\s+"); // Puts every word into its own spot in array
        
        ArrayList<String> newLines = new ArrayList<String>(Arrays.asList(theLines));

        firstWord = newLines.get(0).replace(",","").replace(".", "").toLowerCase();
        
        for (int index = 0; index < newLines.size() - 2; index++){ 
            String mapName = newLines.get(index); // assigns word to name of map
            mapName = mapName.replace(",","").toLowerCase();
            
            String wordPOS = this.dictionary.get(mapName);
            
            if (wordPOS != null) { //adds words that are nouns, verbs, adj to the topic indicative sets
                if(wordPOS.equals("n") && !this.genericWords.contains(mapName)) {
                    this.topicNouns.add(mapName);
                } else if (wordPOS.equals("adj") && !this.genericWords.contains(mapName)) {
                    this.topicAdj.add(mapName);
                } else if (wordPOS.equals("v") && !this.genericWords.contains(mapName)) {
                    this.topicVerbs.add(mapName);
                }
            }
            
            
            if(mapName.replace("."," ").equals(mapName) || mapName.replace(",", " ").equals(mapName)){
                String inputWord = newLines.get(index + 1);//word to be key in FrequencyMap
                inputWord = inputWord.replace(",","").replace(".", "").toLowerCase(); //take out commas and periods
                if (this.seqTable.containsKey(mapName)) {     //if the sequence is already a key
                    //creates new FrequencyMap for the sequence
                    FrequencyMap freqMap = this.seqTable.get(mapName);
                    freqMap.addWord(inputWord); // calls the addWord method from FrequencyMap
                } else{    //if sequence is not a key
                    FrequencyMap probMap = new FrequencyMap(); 
                    //assigns a FrequencyMap value to a sequence key
                    this.seqTable.put(mapName, probMap); 
                    probMap.addWord(inputWord); //calls the addWord method from FrequencyMap
               
                }
            }
            }
        }
        
        
        /**
         * Generates a list of possible next words based off the previous words.
         *   
         * @param word the current word being used to determine next words 
         * @return nextCharacter a character randomly generated using the pickChar in FrequencyMap
         */
    public List<String> nextWord(String word) {
    
        word = word.toLowerCase().trim();
        List<String> nextWords = new ArrayList<String>();
        if (this.seqTable.containsKey(word) == true) {
            FrequencyMap freqMap = this.seqTable.get(word);
            nextWords = freqMap.pickNewWords(word, dictionary); // at this point, you should pick word/sequence with highest seq
        } else { // use the part of speech of the last word and use grammar rules
            nextWords = pickGeneric(word, dictionary);
        }
        return nextWords;
    }
    
    
    /**
     * pickGeneric
     * picks a list of generic words to return based on the part of speach of the given word
     * 
     * @param lastWord String representing the word in question
     * @param dictionary Map representing the part of speech information
     */
    public static List<String> pickGeneric(String lastWord, Map<String, String> dictionary) {
        List<String> result = new ArrayList<String>();
        String posLastWord = dictionary.get(lastWord);
        
        if(posLastWord.equals("n")) { //noun
            result.add("and");
            result.add("is");
            result.add("was");
        } else if(posLastWord.equals("v")) { //verb
            result.add("the");
            result.add("to");
            result.add("an");
            result.add("a");
            result.add("and");
        } else if(posLastWord.equals("pron")) { //pronoun
            result.add("the");
            result.add("to");
            result.add("an");
            result.add("a");
            
        } else if(posLastWord.equals("prep")) { //preposition
            result.add("the");
            result.add("that");
            result.add("these");
            
        } else if(posLastWord.equals("adj") ){ //adj
            result.add("and");
            
            
        } else if(posLastWord.equals("conj")) { //conjunction
            
            
        } else {
            result.add("a");
            result.add("the");
            result.add("and");
        }
        return result;
    }
  
}
