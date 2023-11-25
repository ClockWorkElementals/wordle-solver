package ca.qc.johnabbott.cs4p6;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract representation of a lexicon.
 *
 * @author Ian Clement (ian.clement@johnabbott.qc.ca)
 */
public interface Lexicon {

    /**
     * Add a word to the lexicon.
     * @param word the word to add to the lexicon.
     */
    void add(String word);


    /**
     * Test if a word is in the lexicon.
     * @param word the word to check.
     * @return true if the word is in the lexicon, false otherwise.
     */
    boolean contains(String word);

    /**
     * Generate "wordle" solutions
     * @param length The word length.
     * @param greys The characters that should not appear in the word.
     * @param greens The characters correct with their position.
     * @param yellows The characters that are correct but with unknown position.
     * @return
     */
    List<String> wordle(int length, Set<Character> greys, Map<Integer, Character> greens, List<Character> yellows);

}
