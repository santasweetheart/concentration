import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//represents the game
class Concentration extends World {
  int width = 1080;
  int height = 720;
  ArrayList<Card> deck;
  int score; //current score of the game 
  int time; // time elapsed
  int steps; //number of moves left in the game 

  Concentration() {
    this.deck = makeDeck();
    this.shuffleDeck(); //Shuffles the deck at the beginning of the game
    this.score = 26;
    this.time = 0;
    this.steps =  135; 

  }

  //This is to help make testing easier so the deck is not shuffled
  Concentration(int time) {
    this.deck = makeDeck();
    this.score = 26;
    this.time = 0;
    this.steps =  90; 
  }

  // draws scene for game
  public WorldScene makeScene() { 
    WorldImage backgroundImage = new RectangleImage(this.width, this.height, "solid", Color.gray);
    WorldImage score = new TextImage("Score: " + String.valueOf(this.score), 25, Color.white);
    WorldImage steps = new TextImage("Steps: " + String.valueOf(this.steps), 25, Color.red);
    WorldImage time = new TextImage(timeinMins(), 25, Color.WHITE);

    WorldScene backgroundScene = new WorldScene(this.width, this.height);
    backgroundScene.placeImageXY(backgroundImage, 0, 0);
    backgroundScene.placeImageXY(completeGrid(decideNumOfRows()), decideSpaceOfShortestRow(), 
        23 * decideNumOfRows());
    backgroundScene.placeImageXY(score, 70 ,230);
    backgroundScene.placeImageXY(time, 370 ,230);
    backgroundScene.placeImageXY(steps, 220, 230);
    return backgroundScene;
  }

  //ensures that the rows are placed in the current position on the screen
  int decideSpaceOfShortestRow() {
    if (this.deck.size() >= 13) {
      return 228;
    } else {
      return this.deck.size() / 2 * 35;
    }
  }

  //Returns the number of rows needed for the drawing method 
  //ensures the grid is aligned with the top of the screen 
  int decideNumOfRows() {
    int numOfRows = Math.floorDiv(this.deck.size(), 13) + 1;
    int remainder = this.deck.size() % 13; 

    if (remainder == 0) {
      numOfRows -= 1;
    } 
    return numOfRows;
  }

  //Counts the number of seconds that has passed 
  public void onTick() {
    time = time + 1; 
  }
  

  //Returns the time in minutes and Seconds 
  String timeinMins() {
    int minutes = Math.floorDiv(time, 60);
    int seconds = time % 60;
    if (seconds < 10) {
      return "Time:  " + String.valueOf(minutes) + " : 0" +  String.valueOf(seconds);
    }
    return "Time:  " + String.valueOf(minutes) + " : " +  String.valueOf(seconds);
  }

  // make a deck of cards
  ArrayList<Card> makeDeck() {
    ArrayList<Integer> vals = new ArrayList<Integer>(
        Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13));
    ArrayList<String> suits = new ArrayList<String>(Arrays.asList("♥", "♣", "♠ ", "♦"));
    ArrayList<Card> cards = new ArrayList<Card>();
    for (int i = 0; i < vals.size(); i = i + 1) {
      for (int j = 0; j < suits.size(); j = j + 1) {
        cards.add(new Card(vals.get(i), suits.get(j)));
      }
    }
    return cards;
  }

  // Draws rows on top of each other
  WorldImage completeGrid(int numRows) { 
    WorldImage grid;

    if (this.deck.size() >= 13) {
      grid = drawRow(0, 12);
    } else {
      grid = drawRow(0, this.deck.size() - 1);
    }

    for (int row = 1; row < numRows; row = row + 1) {
      int index = row * 13;
      int remaining = this.deck.size() - index;

      //Continuously aligns rows to the left 
      if (remaining >= 13) {
        grid = new AboveAlignImage(AlignModeX.LEFT,grid, drawRow(row, 12));
      } else if (remaining > 0) {
        grid = new AboveAlignImage(AlignModeX.LEFT,grid, drawRow(row, remaining - 1));
      }
    }
    return grid;
  }

  // Draws a row of cards
  WorldImage drawRow(int index, int numCol) {
    WorldImage grid = new TextImage("", Color.red);
    for (int col = 0; col <= numCol; col = col + 1) {
      grid = new BesideImage(grid, this.deck.get(col + index * 13).drawCard());
    }
    return grid;
  }


  //Effect: shuffles the cards in the deck
  void shuffleDeck() {
    for (int card = 0; card < 52; card += 1) {
      Random rand = new Random();
      int otherIndex = rand.nextInt(52);

      new ArrayUtils().swap(this.deck, card, otherIndex);
    }

  }

  //Effect: add onClick in part II
  public void onMouseClicked(Posn pos) {
    int row = Math.floorDiv(pos.y,45);
    int col = Math.floorDiv(pos.x, 35);
    int cardNum = (row * 13) + col;

    if (cardNum < this.deck.size()) {
      int sizeOfFlippedCards = this.flippedCards().size();
      this.steps -= 1; //clicked on a card so number of steps decreases
      if (sizeOfFlippedCards == 2) {
        if (checkMatch()) {
          this.deck.remove(this.flippedCards().get(0));
          this.deck.remove(this.flippedCards().get(0));
          this.score -= 1;  //matched cards = score increase
       
          flipAllCards();

        } else {

          flipAllCards();
        }

      } else {
        this.deck.get(cardNum).flipCard();
      }
    
    } 
  }
  

  //Effect: flips all the cards in the deck so they're facing the back
  void flipAllCards() {
    for (int index = 0; index < this.deck.size(); index += 1) {  //Make Helper Method
      this.deck.get(index).flipped = false;
    }
  }

  //Ends the game is the player has a score of 0 or when the player runs out of steps
  public WorldEnd worldEnds() {
    TextImage message = new TextImage("You Win!", 50, Color.black);
    WorldScene end = makeScene();
    end.placeImageXY(message, 228, 23 * 4);

    TextImage lose = new TextImage("You Lose! :(", 50, Color.red);
    WorldScene loser = makeScene();

    loser.placeImageXY(lose, 228, 23 * 4);
    if (this.score == 0) {
      return new WorldEnd(true, end);
    } else if (this.steps == 0) {
      return new WorldEnd(true, loser);
    } else {
      return new WorldEnd(false, this.makeScene());
    }
  }



  //Is a card in the deck flipped (facing the front)?
  ArrayList<Card> flippedCards() {
    ArrayList<Card> flippedCard = new ArrayList<Card>();
    for (int index = 0; index < this.deck.size(); index += 1) {
      if (this.deck.get(index).flipped) {
        flippedCard.add(this.deck.get(index));
      }
    }
    return flippedCard;
  }

  //Do the two flipped cards have the same rank and color?
  boolean checkMatch() {
    Card index1 = this.flippedCards().get(0);
    Card index2 = this.flippedCards().get(1);
    Color index1Color = index1.findColor();
    Color index2Color = index2.findColor();

    return index1.rank == index2.rank 
        && index1Color == index2Color; 
  }

  //EFFECT: resets the game 
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      Concentration reset = new Concentration();
      this.deck = reset.deck;
      this.score = reset.score;
      this.time = reset.time;
      this.steps = reset.steps;
    } 
  }

}

//represents utility methods for array lists
class ArrayUtils {

  //EFFECT: Exchanges the values at the given two indices in the given array
  <T> void swap(ArrayList<T> arr, int index1, int index2) {
    T oldValueAtIndex1 = arr.get(index1);
    T oldValueAtIndex2 = arr.get(index2);

    arr.set(index2, oldValueAtIndex1);
    arr.set(index1, oldValueAtIndex2);
  }
} 


//represents a playing card
class Card {
  int rank;
  String suit;
  boolean flipped;
  WorldImage back;

  // constructor
  Card(int rank, String suit) {
    this.rank = rank;
    this.suit = suit;
    this.flipped = false;
    this.back = new OverlayImage(new RectangleImage(30, 40, "solid", Color.black),
        new RectangleImage(35, 45, "solid", Color.gray));

  }

  // updates the state of the card
  void flipCard() {
    if (this.flipped) {
      this.flipped = false;
    }
    else {
      this.flipped = true;
    }
  }

  // finds the color of the card based on its suit
  public Color findColor() {
    if (this.suit.equals("♥") || this.suit.equals("♦")) {
      return Color.red;
    }
    else {
      return Color.black;
    }
  }

  // draws the card in its current state
  public WorldImage drawCard() {
    if (this.flipped) {
      return this.makeCardFront();
    }
    else {
      return this.back;
    }
  }

  // constructs an image of the front of the card
  public WorldImage makeCardFront() {
    return new OverlayImage(
        new OverlayImage(
            (new AboveImage(new TextImage(Integer.toString(this.rank), 10, this.findColor()),
                new TextImage(this.suit, 20, this.findColor()))),
            new RectangleImage(30, 40, "solid", Color.white)),
        new RectangleImage(35, 45, "solid", Color.gray));
  }
}

class ExamplesConcentration {
  int WIDTH = 460;
  int HEIGHT = 250;

  Card oneClub;
  Card twoHeart;
  Card threeDiamond;
  Card fourSpade;
  Concentration test;
  Concentration c1;
  ArrayList<Card> array;

  void InitialCond() {
    oneClub = new Card(1, "♣");
    twoHeart = new Card(2, "♥");
    threeDiamond = new Card(3, "♦");
    fourSpade = new Card(4, "♠");
    test = new Concentration(0); //Not shuffled
    array = new ArrayList<Card>();
    c1 = new Concentration(0);
  }

  void testflipCard(Tester t) {

    InitialCond();
    t.checkExpect(oneClub.flipped, false);
    oneClub.flipCard();
    t.checkExpect(oneClub.flipped, true);
  }

  void testfindColor(Tester t) {
    InitialCond();
    t.checkExpect(twoHeart.findColor(), Color.red);
    t.checkExpect(threeDiamond.findColor(), Color.red);
    t.checkExpect(fourSpade.findColor(), Color.black);
    t.checkExpect(oneClub.findColor(), Color.black);
  }

  void testDrawCard(Tester t) {
    InitialCond();
    t.checkExpect(twoHeart.drawCard(),
        new OverlayImage(new RectangleImage(30, 40, "solid", Color.black),
            new RectangleImage(35, 45, "solid", Color.gray)));

    twoHeart.flipCard();

    t.checkExpect(twoHeart.drawCard(),
        new OverlayImage(
            new OverlayImage(
                (new AboveImage(new TextImage(Integer.toString(2), 10, Color.red),
                    new TextImage("♥", 20, Color.red))),
                new RectangleImage(30, 40, "solid", Color.white)),
            new RectangleImage(35, 45, "solid", Color.gray)));
  }

  void testMakeCardFront(Tester t) {
    InitialCond();

    twoHeart.flipCard();

    t.checkExpect(twoHeart.drawCard(),
        new OverlayImage(
            new OverlayImage(
                (new AboveImage(new TextImage(Integer.toString(2), 10, Color.red),
                    new TextImage("♥", 20, Color.red))),
                new RectangleImage(30, 40, "solid", Color.white)),
            new RectangleImage(35, 45, "solid", Color.gray)));

    fourSpade.flipCard();

    t.checkExpect(fourSpade.drawCard(),
        new OverlayImage(
            new OverlayImage(
                (new AboveImage(new TextImage(Integer.toString(4), 10, Color.black),
                    new TextImage("♠", 20, Color.black))),
                new RectangleImage(30, 40, "solid", Color.white)),
            new RectangleImage(35, 45, "solid", Color.gray)));
  }

  void testDrawRow(Tester t) {
    InitialCond();
    t.checkExpect(test.drawRow(0, 3),
        new BesideImage(
            new BesideImage(new BesideImage(
                new BesideImage(new TextImage("", Color.red), test.deck.get(0).drawCard()),
                test.deck.get(1).drawCard()), test.deck.get(2).drawCard()),
            test.deck.get(3).drawCard()));

    t.checkExpect(test.drawRow(0, 2),
        new BesideImage(new BesideImage(
            new BesideImage(new TextImage("", Color.red), test.deck.get(0).drawCard()),
            test.deck.get(1).drawCard()), test.deck.get(2).drawCard()));

  }

  void testCompleteGrid(Tester t) {
    InitialCond();
    t.checkExpect(test.completeGrid(1), test.drawRow(0, 12));

    t.checkExpect(test.completeGrid(4),
        new AboveAlignImage(AlignModeX.LEFT, new AboveAlignImage(AlignModeX.LEFT, 
            new AboveAlignImage(AlignModeX.LEFT, test.drawRow(0, 12), test.drawRow(1, 12)),
            test.drawRow(2, 12)), test.drawRow(3, 12)));
  }

  void testMakeScene(Tester t) { 
    InitialCond();
    int width = 1080;
    int height = 720;

    WorldImage score = new TextImage("Score: " + String.valueOf(test.score), 25, Color.white);
    WorldImage steps = new TextImage("Steps: " + String.valueOf(test.steps), 25, Color.red);
    WorldImage time = new TextImage(test.timeinMins(), 25, Color.WHITE);
    WorldImage backgroundImage = new RectangleImage(width, height, "solid", Color.gray);
    WorldScene backgroundScene = new WorldScene(width, height);

    backgroundScene.placeImageXY(backgroundImage, 0, 0);
    backgroundScene.placeImageXY(test.completeGrid(test.decideNumOfRows()), 
        test.decideSpaceOfShortestRow(), 23 * test.decideNumOfRows());
    backgroundScene.placeImageXY(score, 70 ,230);
    backgroundScene.placeImageXY(time, 370 ,230);
    backgroundScene.placeImageXY(steps, 220, 230);

    t.checkExpect(test.makeScene(), backgroundScene);

    test.deck.get(0).flipCard();
    WorldScene backgroundS = new WorldScene(width, height);
    backgroundS.placeImageXY(backgroundImage, 0, 0);
    backgroundS.placeImageXY(test.completeGrid(test.decideNumOfRows()), 
        test.decideSpaceOfShortestRow(),  23 * test.decideNumOfRows());
    backgroundS.placeImageXY(score, 70 ,230);
    backgroundS.placeImageXY(time, 370 ,230);
    backgroundS.placeImageXY(steps, 220, 230);

    t.checkExpect(test.makeScene(), backgroundS);
  }

  void testCheckMatch(Tester t) { //If deck is not shuffled 
    InitialCond(); 
    test.deck.get(0).flipCard();
    test.deck.get(1).flipCard();
    t.checkExpect(test.checkMatch(), false);

    InitialCond();
    test.deck.get(1).flipCard();
    test.deck.get(2).flipCard();
    t.checkExpect(test.checkMatch(), true);
  }

  //tests decideSpaceOfShortestRow
  void testDecideSpaceOfShortestRow(Tester t) { //If deck is not shuffled 

    InitialCond(); 
    t.checkExpect(test.decideSpaceOfShortestRow(), 228);

    for (int i = 46; i > 0; i -= 1) {
      test.deck.remove(i);
    }
    t.checkExpect(test.decideSpaceOfShortestRow(), 3 * 35);
  }


  // tests decideNumOfRows
  void testDecideNumOfRows(Tester t) { //If deck is not shuffled 
    InitialCond(); 
    t.checkExpect(test.decideNumOfRows(), 4);
    for (int i = 0; i < 26; i += 1) {
      test.deck.remove(i);
    }

    t.checkExpect(test.decideNumOfRows(), 2);

    InitialCond();
    for (int i = 45; i > 0; i -= 1) {
      test.deck.remove(i);
    }

    t.checkExpect(test.decideNumOfRows(), 1);
  }

  //tests onMouseCLicked
  void testOnMouseCLicked(Tester t) { //If deck is not shuffled 
    InitialCond(); 
    Concentration c1 = new Concentration(0);
    c1.deck.get(0).flipCard();
    test.onMouseClicked(new Posn(0, 0));
    t.checkExpect(test.deck, c1.deck);
    test.onMouseClicked(new Posn(10000, 10000));
    t.checkExpect(test.deck, c1.deck);
  }

  //tests flipAllCards
  void testflipAllCards(Tester t) { //If deck is not shuffled 
    InitialCond(); 
    Concentration c1 = new Concentration(0);
    c1.deck.get(0).flipCard();
    c1.flipAllCards();
    boolean init = true;
    for (int i = 0; i <= 52; i += 1) {
      init = init && !(c1.deck.get(0).flipped);
    }
    t.checkExpect(init, true);

    for (int i = 0; i <= 51; i += 1) {
      test.deck.get(i).flipCard();
    }
    test.flipAllCards();
    boolean init2 = true;
    for (int i = 0; i <= 52; i += 1) {
      init2 = init2 && !(test.deck.get(0).flipped);
    }
    t.checkExpect(init2, true);
  }

  //tests flippedCards
  void testflippedCards(Tester t) { //If deck is not shuffled 
    InitialCond(); 
    Concentration c1 = new Concentration(0);
    c1.flipAllCards();
    t.checkExpect(test.flippedCards(), new ArrayList<Card>());
    t.checkExpect(c1.flippedCards(), new ArrayList<Card>());
    test.deck.get(1).flipCard();
    test.deck.get(2).flipCard();
    ArrayList<Card> ac = new ArrayList<Card>();
    ac.add(test.deck.get(1));
    ac.add(test.deck.get(2));
    t.checkExpect(test.flippedCards(), ac);
  }

  //tests onKeyEvent
  void testOnKeyEvent(Tester t) { //If deck is not shuffled 
    InitialCond(); 
    c1.score = 3;
    c1.steps = 83;
    c1.time = 106;
    c1.onKeyEvent("r");
    t.checkExpect(c1.deck.size(), test.deck.size());
    t.checkExpect(c1.steps, 135);
    t.checkExpect(c1.score, 26);
    t.checkExpect(c1.time, 0);

    InitialCond(); 
    c1.deck.remove(0);
    c1.onKeyEvent("r");
    t.checkExpect(c1.deck.size(), 52);
  }

  void testOnTick(Tester t) {
    InitialCond();
    test.onTick();
    t.checkExpect(test.time, 1);

    InitialCond();
    test.time = 20;
    test.onTick();
    t.checkExpect(test.time, 21);
  }

  void testTimeInMin(Tester t) {
    InitialCond();
    t.checkExpect(test.timeinMins(), "Time:  0 : 00");
    test.time = 9;
    t.checkExpect(test.timeinMins(), "Time:  0 : 09");
    test.time = 125;
    t.checkExpect(test.timeinMins(), "Time:  2 : 05");
  }

  void testMakeDeck(Tester t) {
    InitialCond();
    t.checkExpect(test.deck.size(), 52);
    //Not really sure how to test this w/o writing out 52 cards 
  }

  void testWorldEnds(Tester t) {
    InitialCond();
    t.checkExpect(test.worldEnds(), new WorldEnd(false, test.makeScene()));

    test.score = 0;
    test.steps = 20; 
    WorldScene win = test.makeScene();
    win.placeImageXY(new TextImage("You Win!", 50, Color.black), 228, 23 * 4);
    t.checkExpect(test.worldEnds(), new WorldEnd(true, win));

    test.score = 2;
    test.steps = 0;
    WorldScene lost = test.makeScene();
    lost.placeImageXY(new TextImage("You Lose! :(", 50, Color.red), 228, 23 * 4);
    t.checkExpect(test.worldEnds(), new WorldEnd(true, lost)); 
  }


  void testSwap(Tester t) {
    InitialCond();
    array.add(oneClub);
    array.add(twoHeart);

    new ArrayUtils().swap(array, 0, 1);
    t.checkExpect(array.get(0), twoHeart);
    t.checkExpect(array.get(1), oneClub);

    InitialCond();
    ArrayList<Card> arr = new ArrayList<Card>();
    arr.add(oneClub);
    arr.add(twoHeart);
    arr.add(threeDiamond);
    arr.add(fourSpade);

    array.add(oneClub);
    array.add(twoHeart);
    array.add(threeDiamond);
    array.add(fourSpade);

    new ArrayUtils().swap(arr, 0, 3);
    new ArrayUtils().swap(arr, 1, 2);
    new ArrayUtils().swap(arr, 0, 3);
    new ArrayUtils().swap(arr, 1, 2);
    t.checkExpect(arr, array);
  }

  void testshuffleDeck(Tester t) {
    Concentration c2 = new Concentration();
    InitialCond();
    t.checkExpect(c1.deck, test.deck);
    c1.shuffleDeck();
    t.checkExpect(c1.deck != c2.deck, true);
  }

  //Uses the second constructor, for testing purposes only
  /*void testConcentrationUnShuffled(Tester t) {
    Concentration game = new Concentration(0);
    game.bigBang(WIDTH, HEIGHT, 1.0);
  }*/ 

  //Run this one to play the game in all its glory
  void testPlayGame(Tester t) {
    Concentration game = new Concentration();
    game.bigBang(WIDTH, HEIGHT, 1.0);
  }

}