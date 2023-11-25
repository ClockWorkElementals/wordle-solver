package ca.qc.johnabbott.cs4p6;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class WordleSample {

    public static void main(String[] args) {

        Scanner lexiconFile = null;
        try {
            lexiconFile = new Scanner(new FileReader(Alphabets.LEXICON_FULL));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        Lexicon lexicon = new Trie();
        while (lexiconFile.hasNext())
            lexicon.add(lexiconFile.next());

        Map<Integer, Character> greens = new HashMap<>();
        greens.put(0, 's');
        greens.put(2, 'o');

        List<Character> yellows = new ArrayList<>();
        // yellows.add('o');
        yellows.add('n');
        // yellows.add('s');

        Set<Character> greys = new HashSet<>();
        greys.add('c');
        greys.add('a');
        greys.add('d');
        greys.add('e');
        greys.add('t');
        greys.add('b');
        greys.add('u');
        greys.add('o');

        List<String> solutions = lexicon.wordle(5, greys, greens, yellows);
        System.out.println("Possible solutions: " + flatten(solutions));

    }

    private static String flatten(List<String> list) {
        StringJoiner joiner;
        joiner = new StringJoiner(", ");
        for (String s : list)
            joiner.add(s);
        return joiner.toString();
    }
}
