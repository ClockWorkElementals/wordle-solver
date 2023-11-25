package ca.qc.johnabbott.cs4p6;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Trie implements Lexicon {

    private Node<Character> root;
    private Node<Character> projection;

    public Trie() {
        root = new Node<Character>(' ');
        projection = new Node<Character>(' ');
    }

    @Override
    public void add(String word) {

        Node<Character> current = root;
        add(current, word, 0);

        echoToProjection(word);
    }

    // Recursive helper method for add to fulfill assignment requirements.
    private void add(Node<Character> current, String word, int place) {
        // Base case: word is empty or at the end of the word.
        if (word.length() == 0 || word.length() - 1 == place) {
            current.setWord(true);
            return;
        }

        // Recursive case: word is not empty and not at the end of the word.
        // If the node already has a child with the current letter, then
        // recursively call add on that child. Otherwise, add a new child
        // with the current letter and recursively call add on that child.
        if (current.getChildren().containsKey(word.charAt(place)))
            add(current.getChildren().get(word.charAt(place)), word, place + 1);
        else {
            current.addChild(word.charAt(place));
            add(current.getChildren().get(word.charAt(place)), word, place + 1);
        }

    }

    // Helper method to echo the word to the projection trie, used to avoid
    // mutating the original trie. Also, used to showcase my original appraoch
    // to add before I realized recursion was required.
    private void echoToProjection(String word) {
        Node<Character> current = projection;
        if (word.length() == 0) {
            projection = current;
            return;
        }

        for (int i = 0; i < word.length(); i++) {
            if (current.getChildren().containsKey(word.charAt(i)))
                current = current.getChildren().get(word.charAt(i));
            else {
                current.addChild(word.charAt(i));
                current = current.getChildren().get(word.charAt(i));
            }

            if (i == word.length() - 1) {
                current.setWord(true);
            }
        }
    }

    @Override
    public boolean contains(String word) {
        Node<Character> current = root;
        return contains(current, word, 0);
    }

    // Recursive helper method for contains to fulfill assignment requirements.
    private boolean contains(Node<Character> current, String word, int place) {
        // Base case: word is empty or at the end of the word.
        if (word.length() == 0 || word.length() - 1 == place)
            return current.isWord();

        // Recursive case: word is not empty and not at the end of the word.
        // Check if the node has a child with the current letter. If it does,
        // then recursively call contains on that child. Otherwise, return false.
        if (current.getChildren().containsKey(word.charAt(place)))
            return contains(current.getChildren().get(word.charAt(place)), word, place + 1);
        else
            return false;
    }

    @Override
    public List<String> wordle(int length, Set<Character> greys, Map<Integer, Character> greens,
            List<Character> yellows) {

        // Cull branches that don't lead to a word of the correct length.
        branchToCount(0, length, projection);
        branchToWord(0, length, projection);

        // Cull branches that contain a grey letter.
        branchWithNoGreys(0, length, projection, greys);

        // Cull branches that don't have a green letter in the correct position.
        greensInPlace(-1, length, projection, greens);

        // Return all words that are left.
        List<String> words = getWords(projection, "", new ArrayList<String>(), length, yellows);
        return words;
    }

    // #region Branch To Count
    // Comb through the trie and remove any branches that don't lead to the passed
    // word length at the beginning of a wordle game.
    private boolean branchToCount(int start, int length, Node<Character> node) {
        boolean flag = false;
        List<Character> removedChildren = new ArrayList<Character>();

        // Base case: branch reaches correct length.
        if (start == length)
            return true;

        // Base case: branch is too short.
        if (node.getChildren().size() == 0) {
            node.setCanLeadToWord(false);
            return false;
        }

        // Recursive case: branch is long enough, but not yet at the correct length.
        // If any of the children branches can reach the correct length, then this
        // branch can reach the correct length, but any branch that doesn't reach the
        // correct length is still removed.
        for (Node<Character> child : node.getChildren().values()) {
            if (branchToCount(start + 1, length, child))
                flag = true;
            else {
                child.setCanLeadToWord(false);
                removedChildren.add(child.getElement());
            }
        }

        // Remove all children that have a gray.
        // * Was required to use a list and remove them after the loop. Otherwise, the
        // * loop would throw a concurrent modification exception.
        for (Character child : removedChildren)
            node.getChildren().remove(child);

        // If no children branches can reach the correct length, then this branch
        // cannot reach the correct length.
        if (!flag)
            node.setCanLeadToWord(false);

        return flag;
    }
    // #endregion

    // #region Branch To Word
    // Comb through the trie and remove any branches that don't lead to a word at
    // the beginning of a wordle game.
    private boolean branchToWord(int start, int length, Node<Character> node) {
        boolean flag = false;
        List<Character> removedChildren = new ArrayList<Character>();

        // Base case: branch reaches correct length and is a word.
        if (start == length && node.isWord())
            return true;

        // Base case: Branch length is correct, but is not a word.
        if (start == length && !node.isWord()) {
            node.setCanLeadToWord(false);
            return false;
        }

        // Recursive case: branch is long enough, but not yet at the correct length to
        // be a word. If any of the children branches can reach the correct length and
        // are words, then this branch remains. If any of the children branches do not
        // end up being words, then they are removed.
        for (Node<Character> child : node.getChildren().values()) {
            if (branchToWord(start + 1, length, child))
                flag = true;
            else {
                child.setCanLeadToWord(false);
                removedChildren.add(child.getElement());
            }
        }

        // Remove all children that have a gray.
        // * Was required to use a list and remove them after the loop. Otherwise, the
        // * loop would throw a concurrent modification exception.
        for (Character child : removedChildren)
            node.getChildren().remove(child);

        // If no children are words, then this branch cannot lead to a word.
        if (!flag)
            node.setCanLeadToWord(false);

        return flag;
    }
    // #endregion

    // #region Branch With No Greys
    // Comb through the trie and remove any branches that contain a grey letter at
    // any point in the branch.
    private boolean branchWithNoGreys(int start, int length, Node<Character> node, Set<Character> greys) {
        boolean flag = false;
        List<Character> removedChildren = new ArrayList<Character>();

        // Base case: branch reaches correct length, and no greys are found.
        if (start == length && !greys.contains(node.getElement()))
            return true;

        // Base case: Node's element is a grey.
        if (greys.contains(node.getElement()))
            return false;

        // Recursive case: A gray has not been found yet, and the branch is has not
        // reached the correct length. If any of the children branches have a gray,
        // it is removed. If all branches have a gray, then this branch is removed.
        for (Node<Character> child : node.getChildren().values()) {
            if (branchWithNoGreys(start + 1, length, child, greys))
                flag = true;
            else {
                child.setCanLeadToWord(false);
                removedChildren.add(child.getElement());
            }
        }

        // Remove all children that have a gray.
        // * Was required to use a list and remove them after the loop. Otherwise, the
        // * loop would throw a concurrent modification exception.
        for (Character child : removedChildren)
            node.getChildren().remove(child);

        // If all children have a gray, then this branch cannot lead to a word.
        if (!flag)
            node.setCanLeadToWord(false);

        return flag;
    }
    // #endregion

    // #region Greens In Place
    // Comb through the trie and remove any branches that don't have green letters
    // in
    // the correct place.
    private boolean greensInPlace(int start, int length, Node<Character> node, Map<Integer, Character> greens) {
        boolean flag = false;
        List<Character> removedChildren = new ArrayList<Character>();

        // Base case: There are no greens at this point in the branch.
        if (!greens.containsKey(start))
            flag = true;

        // Check if start is one of the keys. If it is, check if the element is the same
        // as the value.
        if (greens.containsKey(start) && greens.get(start) == node.getElement())
            flag = true;

        // If start is in the map, but the element is not the same, then this no
        // branches from this node can lead to a word.
        if (greens.containsKey(start) && greens.get(start) != node.getElement()) {
            node.setCanLeadToWord(false);
            return false;
        }

        // Loop through children and check if any of them have greens in place.
        for (Node<Character> child : node.getChildren().values()) {
            if (greensInPlace(start + 1, length, child, greens))
                flag = true;
            else {
                child.setCanLeadToWord(false);
                removedChildren.add(child.getElement());
            }
        }

        // Remove all children that do not have greens in their proper place.
        // * Was required to use a list and remove them after the loop. Otherwise, the
        // * loop would throw a concurrent modification exception.
        for (Character child : removedChildren)
            node.getChildren().remove(child);

        // If no children have greens in place, then this branch cannot lead to a word.
        if (!flag)
            node.setCanLeadToWord(false);

        return flag;
    }
    // #endregion

    // #region Get Words
    // Comb through the trie and return all words that are the correct length and
    // contain all yellow letters.
    private List<String> getWords(Node<Character> node, String word, List<String> words, int length,
            List<Character> yellows) {

        boolean flag = true;

        // Check if the word is missing any yellows.
        for (Character yellow : yellows) {
            if (!word.contains(yellow.toString()))
                flag = false;
        }

        // If the word is the correct length and contains all yellows, add it to the
        // list.
        if (node.isWord() && word.length() == length && flag) {
            words.add(word);
        }

        // Recursive case: The word is not the correct length yet. Continue down the
        // branch.
        for (Node<Character> child : node.getChildren().values()) {
            if (child.canLeadToWord())
                getWords(child, word + child.getElement(), words, length, yellows);
        }

        return words;
    }
    // #endregion
}

class Node<T> {
    private T element;
    private Map<T, Node<T>> children;
    private boolean isWord = false;
    private boolean canLeadToWord = true;

    public Node(T element) {
        this.element = element;
        this.children = new HashMap<T, Node<T>>();
    }

    public T getElement() {
        return element;
    }

    public void setElement(T element) {
        this.element = element;
    }

    public Map<T, Node<T>> getChildren() {
        return children;
    }

    public void addChild(T element) {
        if (!children.containsKey(element))
            children.put(element, new Node<T>(element));
    }

    public boolean isWord() {
        return isWord;
    }

    public void setWord(boolean isWord) {
        this.isWord = isWord;
    }

    public boolean canLeadToWord() {
        return canLeadToWord;
    }

    public void setCanLeadToWord(boolean flag) {
        this.canLeadToWord = flag;
    }
}