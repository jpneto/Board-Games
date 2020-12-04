package toolbox;

import java.util.*;
import java.awt.*;

/**
 * <p>Title: GAME Class</p>
 *
 * <p>Description: The Game class keeps all the information about the
 * actual game history. It provides a set of helpful services that
 * are used in the application (like an aplet): (i) execute a move
 * given as a string (by the Human) or as a set of Actions (by the cpu),
 * (ii) has unlimited undo and redo features, (iii) can be cloned
 * or light copied (ie, without the undo/redo for fast copying), (iv)
 * get all the actual game information (eg, who's next, checks if the
 * game ended).<br>This is an <b>abstract</b> class. Every concrete game
 * must implement the following methods (check the end of file for their
 * signatures):<br><br>
 *
 *  translate()  - needed to translate the string move into a set
 *                 of actions<br>
 *  endOfGame()  - a boolean method to check the end of game <br>
 *  validMoves() - which returns a set of actions based on the
 *                 actual board position</p>
 *
 * <p>Copyright: Copyright Joao Pedro Neto (c) 2003</p>
 * <p>Company: WAG website</p>
 * @author Joao Pedro Neto
 * @version 1.0
 */

public abstract class Game implements Cloneable {

  public static final int NOPLAYER = -1; // also used to define draws
  public static final int FIRST    =  0;
  public static final int SECOND   =  1;
  public static final int THIRD    =  2;

  public static final String PASS  = "-";

  protected int   actualPlayer = FIRST,
                  winner,
                  turn = 1;

  protected boolean allowPass;

  protected Board actualBoard;

  private LinkedList undos = new LinkedList();
  private LinkedList redos = new LinkedList();

  //////////////////////////////////////////

  public void undo() {
    if (undos.isEmpty())
      return;

    Action[] a = (Action[])undos.getFirst();
    redos.addFirst(a);
    undos.removeFirst();
    undoMove(a);
  }

  public void undoAll() {
    while (!undos.isEmpty())
      undo();
  }

  public void redo() {
    if (redos.isEmpty())
      return;

    Action[] a = (Action[])redos.getFirst();
    undos.addFirst(a);
    redos.removeFirst();
    doMove(a);
  }

  public void redoAll() {
    while (!redos.isEmpty())
      redo();
  }

  public void clear() {
    undos.clear();
    redos.clear();
    actualBoard.clearPosition();
    actualPlayer = FIRST;
    turn = 1;
    winner = NOPLAYER;
  }

  public Object clone() {
    try {
      Game copy = (Game)super.clone();

      if (undos!=null)
        copy.undos = (LinkedList)undos.clone();
      if (redos!=null)
        copy.redos = (LinkedList)redos.clone();
      copy.actualBoard = (Board)actualBoard.clone();

      return copy;
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }

  public Game lightCopy() {  // a light version of game to the use by the player
    try {
      Game copy = (Game)super.clone();

      copy.actualBoard = (Board)actualBoard.clone();
      copy.undos = null;
      copy.redos = null;

      return copy;
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }

  //////////////////////////////////////////

  public void   doMove(Action[] a) {
    actualBoard.doMove(a);
    nextTurn();
  }

  public void undoMove(Action[] a) {
    actualBoard.undoMove(a);
    prevTurn();
  }


  public void executeMove(String move, String comment) {
    Action[] a = translate(move, comment);
    if (a==null) return;  // if invalid, do nothing...
    doMove(a);
    undos.addFirst(a);
    redos.clear();
  }

  public void executeMove(Action[] a) {
    if (a==null) return;  // if invalid, do nothing...
    doMove(a);
    undos.addFirst(a);
    redos.clear();
  }

  public void show(Graphics g) {
    actualBoard.drawPosition(g,true);
  }

  //////////////////////////////////////////

  public Board getBoard() {
    return actualBoard;
  }

  public String getActualComment() {
    return actualBoard.getComment();
  }

  //////////////////////////////////////////
  // These methods may be changed for some games

  public Action[] setup() {    // what to do before the game begins
    winner = NOPLAYER;
    return null;
  }

  public Action[] setdown() {  // what to do after the game ends
    String finalComments;

    if (whoWon() == Game.NOPLAYER)
      finalComments = "The Game was a draw";
    else {
      prevTurn();
      finalComments = getActualPlayer() + " won this game";
      nextTurn();
    }

    return new Action[] {
                 new Action(finalComments)
               };
  }

  public boolean canPass() {
    return allowPass;
  }

  public int whoWon() {
    return winner;
  }

  public void nextTurn() {
    actualPlayer = 1 - actualPlayer;
    turn++;
  }

  public void prevTurn() {
    actualPlayer = 1 - actualPlayer;
    turn--;
  }

  public String getActualStatus() {
    return "";
  }

  public String getActualPlayer() {
    switch (actualPlayer) {
      case FIRST:
        return "Black";
      case SECOND:
        return "White";
      case THIRD:
        return "Red";
      // add more if needed...
    }
    return "N/A";
  }

  public int getActualPlayerId() {
    return actualPlayer;
  }

  //////////////////////////////////////////
  // These methods are specific for each game

  public abstract Action[]   translate(String move, String comment);  // returns null if invalid move
  public abstract boolean    endOfGame();  // should define winner
  public abstract Action[][] validMoves(); // defines the next possible moves

  //////////////////////// CONSTRUCTORS /////////////////////////

  public Game(int x, int y, int r, int c, Image[] pieces) {
    Color     background = new Color(213, 189, 131); // The board background
    Color     foreground = new Color(  0,   0,   0); // The board lines

    actualBoard  = new Board(x,y,r,c,background,foreground,pieces);
    allowPass = translate(Game.PASS,"") != null;
  }

  public Game(int x, int y, int a, int b, int c, Image[] pieces) {
    Color     background = new Color(213, 189, 131); // The board background
    Color     foreground = new Color(  0,   0,   0); // The board lines

    actualBoard  = new Board(x,y,a,b,c,background,foreground,pieces);
    allowPass = translate(Game.PASS,"") != null;
  }

  public Game(int x, int y, int r, int c, Color bc, Color fc, Image[] pieces) {
    actualBoard  = new Board(x,y,r,c,bc,fc,pieces);
    allowPass = translate(Game.PASS,"") != null;
  }

  public Game(int x, int y, int a, int b, int c, Color bc, Color fc, Image[] pieces) {
    actualBoard  = new Board(x,y,a,b,c,bc,fc,pieces);
    allowPass = translate(Game.PASS,"") != null;
  }

}