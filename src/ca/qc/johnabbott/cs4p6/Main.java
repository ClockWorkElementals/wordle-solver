package ca.qc.johnabbott.cs4p6;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        String wordsFile = Alphabets.LEXICON_FULL;

        Lexicon lexicon = new Trie();

        try {
            Scanner scanner = new Scanner(new FileReader(wordsFile));
            while (scanner.hasNext())
                lexicon.add(scanner.next());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Scanner cin = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String word = cin.next();

            if (lexicon.contains(word))
                System.out.printf("%s is a lexicon word.\n", word);
            else
                System.out.printf("%s is not a lexicon word.\n", word);
        }
    }
}
