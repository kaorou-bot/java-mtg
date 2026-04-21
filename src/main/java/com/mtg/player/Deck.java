package com.mtg.player;

import com.mtg.model.Card;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private List<Card> cards;
    private final int maxDeckSize = 60;

    public Deck() {
        this.cards = new ArrayList<>();
    }

    public void addCard(Card card) {
        if (cards.size() < maxDeckSize) {
            cards.add(card);
        }
    }

    public void addCards(List<Card> cardsToAdd) {
        for (Card card : cardsToAdd) {
            addCard(card);
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card draw() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards.remove(cards.size() - 1);
    }

    public Card peek() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards.get(cards.size() - 1);
    }

    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    public void returnToDeck(Card card) {
        cards.add(0, card);
    }
}
