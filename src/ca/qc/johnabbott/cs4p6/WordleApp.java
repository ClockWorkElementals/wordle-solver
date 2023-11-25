package ca.qc.johnabbott.cs4p6;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

/**
 * Console Wordle
 */
public class WordleApp {

    public static final boolean CHEAT_MODE = true;
    public static final int LENGTH = 5;

    public static void main(String[] args) throws FileNotFoundException {

        Lexicon lexicon = new Trie();
        List<String> words = new ArrayList<>();

        // load words into trie
        Scanner scanner = new Scanner(new FileReader(Alphabets.LEXICON_FULL));
        while (scanner.hasNext()) {
            String word = scanner.next();
            lexicon.add(word);
            if (word.length() == LENGTH)
                words.add(word);
        }
        scanner.close();

        // chose a random word
        Random random = new Random();
        String chosen = words.get(random.nextInt(words.size()));

        if (CHEAT_MODE)
            System.out.println("Word is: " + chosen);

        // create puzzle and set lexicon for word validation
        Wordle wordle = new Wordle(chosen);
        wordle.setLexicon(lexicon);

        Scanner stdin = new Scanner(System.in);

        while (!wordle.isDone()) {

            // get use guess
            System.out.print("> ");
            String word = stdin.next();

            Wordle.Result result = wordle.guess(word);
            System.out.println(result.getMessage());

            if (CHEAT_MODE) {
                System.out.println(result.getGreys());
                System.out.println(result.getGreens());
                System.out.println(result.getYellows());
                List<String> solutions = lexicon.wordle(LENGTH, result.getGreys(), result.getGreens(),
                        result.getYellows());
                System.out.println("Possible solutions: " + flatten(solutions));
            }

            if (result.isCorrect()) {
                System.out.println("Solved in " + wordle.getCurrentGuess() + " guesses.");
                break;
            }
        }
    }

    private static String flatten(List<String> list) {
        StringJoiner joiner;
        joiner = new StringJoiner(", ");
        for (String s : list)
            joiner.add(s);
        return joiner.toString();
    }
}
