
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
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * WordGen
 * 
 * Creates SequenceTables to store input text information. Reads in text file dictionary. Starts command line loop
 * which prompts the user for input, one word at a time, and gives the user word suggestions based on what has 
 * already been typed, what the program has already encountered in text files, and the part of speech of the previous word. 
 * 
 * @author Anaïs Sarrazin & Jack O'Connor
 */
public class WordGen
{
    public static void main(String[] args) throws FileNotFoundException {
        
        HashMap<String,String> dictionary = scanDictionary();
        
        SequenceTable parkTable = new SequenceTable("park"); //table for park words
        SequenceTable oceanTable = new SequenceTable("ocean"); //table for ocean words
        SequenceTable storeTable = new SequenceTable("store"); //table for store words
        
        parkTable.setDictionary(dictionary);
        oceanTable.setDictionary(dictionary);
        storeTable.setDictionary(dictionary);
        
        //list to store the topic tables
        List<SequenceTable> tableList = new ArrayList<SequenceTable>();
        String readLines = "";
        tableList.add(parkTable);
        tableList.add(oceanTable);
        tableList.add(storeTable);
        
        //stores the file names of the text files to read into the SequenceTables
        List<String> tableFileList = new ArrayList<String>();
        tableFileList.add("park.txt");
        tableFileList.add("ocean.txt");
        tableFileList.add("store.txt");
       
        
        //loads in the generic, non-topic words into the program 
        String genericWords = "";
        try {//if the file is found
            genericWords = new String(Files.readAllBytes(Paths.get("generic.txt")));
        } catch (IOException e) {//if the string is not found
            System.out.println("File name not found."); // print an error messagge
        }
       
        
        readInTextFiles(tableFileList, tableList, genericWords); //stores generic words in SequenceTable set 
        
        
        System.out.println("**Enter words one at a time on the command line**");
        System.out.println("**The program will make word suggestions for you**");
        System.out.println("**Type a '.' (period) to end the sentence and start a new one**");
        System.out.println("**Type '***' (three stars) to terminate the program**");
        System.out.println("**Does NOT support punctuation input at this time**");
        System.out.println("**Topics currently supported: 'park', 'ocean', 'store'**");
        
        
        boolean keepGoing = true; //while loop to interact with user continues
        Scanner scan = new Scanner(System.in);
        String totalMessage = ""; //stores the current sentence 
        List<String> lastPredictions = new ArrayList<String>();
        String inputTopic = "generic"; //represents the current topic of the input  
        SequenceTable currentTable = null;
        boolean yetToSet = true; //sentence topic has not been changed from generic yet 
        List<String> suggestions = new ArrayList<String>();
        
        
        while(keepGoing) {
            System.out.println("Suggestions - " + lastPredictions.toString());
            System.out.println("(input - topic is " + inputTopic + ")->");
            String currentInput = scan.nextLine();
            
            
            while(!currentInput.equals(currentInput.replace(",","").replace(".","").replace("'","")) && currentInput.length() > 1) {
                System.out.println("Input a valid word");
                currentInput = scan.nextLine();
            }
            
            
            
            if(yetToSet && !currentInput.equals(".") && !currentInput.equals("***")){ //search for possible topic indicative input if still generic
                inputTopic = findTopic(currentInput, dictionary, tableList);
            }
                     
            
            //if non-generic topic has been identified and the topic has not been changed yet 
            if(!inputTopic.equals("generic") && yetToSet){
                for(int i = 0; i < tableList.size(); i++) {
                    if (inputTopic.equals(tableList.get(i).getTableName()) ) {
                        currentTable = tableList.get(i); //set the current table to generate relevant suggestions
                        yetToSet = false; //topic will not be changed again 
                        System.out.println("topic is " + inputTopic);
                    }
                }
            } 
            
            
            if (currentInput.equals("***") ){ //terminates the while loop
                keepGoing = false;
                break;
            } else if (currentInput.charAt(0) == '%') { //allows user to pick word in suggestions by index
                int index = Character.getNumericValue(currentInput.charAt(1));
                currentInput = lastPredictions.get(index);
            } else if (currentInput.charAt(0) == '.') { //ends the current sentence and starts a new one
                totalMessage += ".";
                System.out.println("That sentence: " + totalMessage);
                totalMessage = " ";
                lastPredictions = new ArrayList<String>();
                continue;
            }
            
            
            if (currentTable == null) { //generates generic suggestions if topic has not been indentified yet
                suggestions = genericSuggestions(currentInput, dictionary);
            } else { //generates suggestions specific to topic 
                suggestions = currentTable.nextWord(currentInput);
            }
            
            totalMessage += " ";
            totalMessage += currentInput;
            
            lastPredictions = suggestions;            
            System.out.println("Current message is: " + totalMessage + "\n");
         }
        System.out.println("SIMULATION OVER");
    }
    
    
    /**
     * readInTextFiles
     * Reads the given text files into the corresponding SequenceTables
     * 
     * @param fileNames - names of the files to be read in 
     * @param tableNames - list of Sequence tables to read the text files into 
     * @genericWords - string of generic words to be put into SequenceTable generic words set
     */
    public static void readInTextFiles(List<String> fileNames, List<SequenceTable> tableNames, String genericWords) {
        String readLines = "";
        for(int i = 0; i < tableNames.size(); i++) {
            tableNames.get(i).setGenericSet(genericWords);
            System.out.println("Added file for " + tableNames.get(i).getTableName() + " words");
                
            try {//if the file is found
                readLines = new String(Files.readAllBytes(Paths.get(fileNames.get(i))));
                System.out.println(fileNames.get(i) + " has been read.");
            } catch (IOException e) {//if the string is not found
                System.out.println("File name not found."); // print an error messagge
            }
            tableNames.get(i).addSequence(readLines); //adds text to the SequenceTable
        }
    }
    
    
    /**
     * findTopic 
     * Attempts to detect if a word is indicative of a setence topic and returns the topic if the topic is detected
     * otherwise returns "generic"
     * 
     * @param word the String of the word in question 
     * @param dictionary the Map containing the part of speech information 
     * @param tableList the list of SequenceTables
     * 
     * @return String representing the topic (park, ocean, store) or generic if not indicative of a topic 
     */
    public static String findTopic(String word, Map<String, String> dictionary, List<SequenceTable> tableList) {
        word = word.toLowerCase().replaceAll("//s","").trim();
        String wordPOS = dictionary.get(word);
        String result = "generic";
        Set<String> currentSet = new HashSet<String>();
        if(wordPOS == null) {
            return result;
        }
        
        if (wordPOS.equals("n") ) { //noun
            for(int i = 0; i < tableList.size(); i++) {
                currentSet = tableList.get(i).getTopicNouns();
                if (currentSet.contains(word)) {
                    result = tableList.get(i).getTableName();
                }
            }
        } else if(wordPOS.equals("v") ) { //verb
            for(int i = 0; i < tableList.size(); i++) {
                currentSet = tableList.get(i).getTopicVerbs();
                if (currentSet.contains(word)) {
                    result = tableList.get(i).getTableName();
                }
            }
        } else if ( wordPOS.equals("adj")) { //adjective
            for(int i = 0; i < tableList.size(); i++) {
                currentSet = tableList.get(i).getTopicAdj();
                if (currentSet.contains(word)) {
                    result = tableList.get(i).getTableName();
                }
            }
            
        } else  {
            return "generic";
        }
        return result;
    }
    
    
    /**
     * genericSuggestions
     * returns generic word selections because a topic for specific suggestions has not been selected yet 
     * 
     * @param word the String of the word in question
     * @param dictionary the Map storing the part of speech information 
     * 
     * @return List of strings with generic suggestions pertaining to the part of speech of the word
     */
    public static List<String> genericSuggestions(String word, Map<String, String> dictionary) {
        List<String> result = new ArrayList<String>();
        word = word.toLowerCase().trim();
        String posLastWord = dictionary.get(word);
        
        if(word.equals("i") ){
            result.add("am");
            result.add("have");
            result.add("want");
            return result;
        } 
        
        if(posLastWord == null) {
            result.add("and");
            result.add("the");
            return result;
        }
        
        if(word.toLowerCase().equals("the") ){
            result.add("new");
            result.add("best");
            result.add("thing");
        } else if(posLastWord.equals("n")) { //noun
            result.add("and");
            result.add("is");
            result.add("was");
        } else if(posLastWord.equals("v")) {//verb
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
            
        } else if(posLastWord.equals("adj") ){ //adjective
            result.add("and");
            result.add("but");
        } else if(posLastWord.equals("conj")) { //conjunction
            result.add("the");
            result.add("an");
            result.add("to");
            result.add("for");
            result.add("a");
            
        } 
        return result;
    }
    
    
    /**
     * scanDictionary
     * scans the given file and records the word as the key and the part of speech as the value 
     * 
     * @return HashMap representing dictionary part of speech information 
     */
    public static HashMap<String,String> scanDictionary() throws FileNotFoundException {
       
        File file = new File("dic.txt");
        HashMap<String, String> dictionary = new HashMap<String, String>();  
        Scanner sc = new Scanner(file);
        while(sc.hasNextLine()) {
            String s = sc.nextLine();
            String[] partsKey = s.split("\\s");
            String keyUpper = partsKey[0];
            String key = keyUpper.toLowerCase();
          
            if(s.contains("[")) {
                String[] partsBracket1 = s.split("\\[");    // Split by finding the 1st bracket '['
                String str1 = partsBracket1[1];             // Take everything after '['
                String[] partsBracket2 = str1.split("\\]"); // Split by finding the 2nd bracket ']'
                String str2 = partsBracket2[0];             // Take everything before ']'
                if(str2.contains(" ")) {
                    String[] partsValue = str2.split("\\s");// Split by finding the space
                    String value = partsValue[0];           // Take everything before the space & ']'
                    dictionary.put(key, value);
                } else { 
                    String value = partsBracket2[0];
                    dictionary.put(key, value);
                  
                }
            } else {
                System.out.println("nonexistent");
            }
        }
        return dictionary;
        
    }
   
}
