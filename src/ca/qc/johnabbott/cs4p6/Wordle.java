/*
 * Copyright (c) 2022 Ian Clement. All rights reserved.
 */

package ca.qc.johnabbott.cs4p6;

import java.util.*;

/**
 * Word guessing puzzle.
 *
 * https://www.nytimes.com/games/wordle/index.html
 */
public class Wordle {

    /**
     * Represent the wordle guess colors and their emojis
     */
    public enum Color {
        UNKNOWN("?"),
        GREY("⬛"),
        YELLOW("\uD83D\uDFE8"),
        GREEN("\uD83D\uDFE9");

        private final String emoji;

        Color(String emoji) {
            this.emoji = emoji;
        }

        @Override
        public String toString() {
            return emoji;
        }
    }

    /**
     * Stores the resulting information of a single guess, including structures for solution searching.
     */
    public static class Result {

        private int guessNumber;
        private boolean correct;
        private String message;

        private Set<Character> greys;
        private Map<Integer, Character> greens;
        private List<Character> yellows;

        // create a result
        private Result(int guessNumber, boolean correct, String message, Set<Character> greys, Map<Integer, Character> greens, List<Character> yellows) {
            this.guessNumber = guessNumber;
            this.correct = correct;
            this.message = message;
            this.greys = greys;
            this.greens = greens;
            this.yellows = yellows;
        }

        private Result(int guessNumber, boolean correct, String message) {
            this.guessNumber = guessNumber;
            this.correct = correct;
            this.message = message;
            this.greys = new HashSet<>();
            this.greens = new HashMap<>();
            this.yellows = new ArrayList<>();
        }

        public int getGuessNumber() {
            return guessNumber;
        }

        public boolean isCorrect() {
            return correct;
        }

        public String getMessage() {
            return message;
        }

        public List<Character> getYellows() {
            return yellows;
        }

        public Map<Integer, Character> getGreens() {
            return greens;
        }

        public Set<Character> getGreys() {
            return greys;
        }
    }

    private int currentGuess;
    private final String wordStr;
    private final char[] word;

    private Lexicon lexicon;

    private Set<Character> greys;
    private Map<Integer, Character> greens;
    private List<Character> yellows;

    /**
     * Construct a wordle puzzle with a set word.
     * @param wordStr
     */
    public Wordle(String wordStr) {
        this.wordStr = wordStr;
        this.word = wordStr.toCharArray();
        this.greys = new HashSet<>();
        this.greens = new HashMap<>();
        this.yellows = new ArrayList<>();
    }

    /**
     * Set the lexicon.
     * @param lexicon
     */
    public void setLexicon(Lexicon lexicon) {
        this.lexicon = lexicon;
    }

    /**
     * Get current guess.
     * @return
     */
    public int getCurrentGuess() {
        return currentGuess;
    }

    /**
     * Determine if the game is done.
     * @return
     */
    public boolean isDone() {
        return currentGuess > 6;
    }

    public Result guess(String guessStr) {
        if (guessStr.length() != wordStr.length())
            return new Result(currentGuess, false, "Guess isn't long enough.");
        else if (currentGuess == 6)
            return new Result(currentGuess, false, "Out of guesses.");
        else if (!lexicon.contains(guessStr))
            return new Result(currentGuess, false, "Guess isn't a dictionary word.");

        // count this guess
        this.currentGuess++;

        char[] guess = guessStr.toCharArray();

        // start with all colors unknown
        Color[] colors = new Color[word.length];
        Arrays.fill(colors, Color.UNKNOWN);

        // determine if the guess it correct
        // set green and grey colors
        boolean correct = true;
        for (int i = 0; i < word.length; i++) {
            if (word[i] == guess[i])
                colors[i] = Color.GREEN;
            else if(!find(word, guess[i])) {
                colors[i] = Color.GREY;
                // add the character to the set of greys now since we will
                //switch some yellows to grey below
                greys.add(guess[i]);
                correct = false;
            }
            else  // incorrect character place
                correct = false;
        }

        // count all non-green character occurrences to figure out how many yellows we need.
        Map<Character, Integer> nonGreenCounts = new HashMap<>();
        for(int i=0; i< word.length; i++) {
            int tmp = 0;
            switch (colors[i]) {
                case YELLOW:
                    throw new IllegalStateException();
                case UNKNOWN:
                case GREY:
                    tmp = 1;
                    break;
                case GREEN:
            }
            if(nonGreenCounts.containsKey(word[i]))
                tmp += nonGreenCounts.get(word[i]);
            nonGreenCounts.put(word[i], tmp);
        }

        // set the yellow colors and remaining greys. Some characters are grey not yellow because
        // of how many letters are needed in the puzzle.
        for (int i = 0; i < word.length; i++) {
            if(colors[i] == Color.UNKNOWN) {
                if(nonGreenCounts.containsKey(guess[i])) {
                    int tmp = nonGreenCounts.get(guess[i]);

                    // if we still have possible placements for these characters, they are yellow
                    if(tmp > 0) {
                        colors[i] = Color.YELLOW;
                        nonGreenCounts.put(guess[i], tmp - 1);  // one less character to consider
                    }
                    else
                        colors[i] = Color.GREY;  // no more correct positions to fill so it's green.
                }
                else
                    colors[i] = Color.GREY;  // not sure if this can happen, but being cautious.
            }
        }

        // update yellow list and green map
        yellows.clear(); // simplification: yellows are reset each guess...
        for (int i=0; i<colors.length; i++) {
            switch (colors[i]) {
                case YELLOW:
                    yellows.add(guess[i]);
                    break;
                case GREEN:
                    greens.put(i, guess[i]);
                    break;
                // there should be no more unknowns at this point
                case UNKNOWN:
                    throw new IllegalStateException();
            }
        }

        // create a pattern for the user to see their guess
        StringBuilder pattern = new StringBuilder();
        for (Color color : colors) pattern.append(color);

        // return the result
        return new Result(currentGuess, correct, pattern.toString(), Set.copyOf(greys), Map.copyOf(greens), List.copyOf(yellows));
    }

    // simple linear find
    private  boolean find(char[] arr, char value) {
        for (char c : arr)
            if (c == value)
                return true;
        return false;
    }
}