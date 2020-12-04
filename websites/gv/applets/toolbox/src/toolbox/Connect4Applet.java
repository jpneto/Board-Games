package toolbox;

import java.awt.*;
import java.util.*;

/*****************************************************************
 * This is a sample. There are two classes: (i) a package class
 * defining the game features, and (ii) an extension of the GameApplet
 * class, including the required createGame() and createPlayer()
 * so that the applet may properly work.
 *****************************************************************/

class Connect4 extends Game {

  public Action[] translate(String move, String comment) {
    if (move.length()!=2)
      return null;

    Point p = actualBoard.coord2point(move);

    // The cell must be empty...
    if (actualBoard.getPositionCell(p.getX(),p.getY()) != Board.EMPTY)
      return null;

    // ... and all cells below must be occupied
    for (int i=0;i<p.getX();i++)
      if (actualBoard.getPositionCell(i,p.getY()) == Board.EMPTY)
        return null;

    // The action is a simple stone drop
    if (comment==null)
      return new Action[] {
                 new Action(Action.INS,
                            actualPlayer==FIRST ? Board.B_SOLDIER : Board.W_SOLDIER,
                            p.getX(),
                            p.getY() )
               };
    else // there is also a comment
      return new Action[] {
                 new Action(Action.INS,
                            actualPlayer==FIRST ? Board.B_SOLDIER : Board.W_SOLDIER,
                            p.getX(),
                            p.getY() ),
                 new Action(comment)
               };
  }

  ///////////////////////////////////////////////////////////////7

  public boolean endOfGame() {
    boolean end = false;

    if (actualBoard.checkNrow(4,Board.B_SOLDIER)) {
      end    = true;
      winner = FIRST;
    }
    else if (actualBoard.checkNrow(4,Board.W_SOLDIER)) {
      end    = true;
      winner = SECOND;
    }
    else {
      for(int i=0;i<actualBoard.getCols();i++)
        if (actualBoard.getPositionCell(actualBoard.getRows()-1,i) == Board.EMPTY)
          return false;
      end = true;
      winner = NOPLAYER; // a draw, the board is full
    }
    return end;
  }

  ///////////////////////////////////////////////////////////////7

  public Action[][] validMoves() {
    LinkedList l = new LinkedList();

    for(int i=0;i<actualBoard.getCols();i++)
      for(int j=0;j<actualBoard.getRows();j++)
        if (actualBoard.getPositionCell(j,i) == Board.EMPTY) {
          l.addFirst(
            new Action[] {
               new Action(Action.INS,
                          actualPlayer==FIRST ? Board.B_SOLDIER : Board.W_SOLDIER,
                          j,
                          i )
            }
          );
          break;
        }

    // Build the action array based on the linked list
    Action[][] a = new Action[l.size()][];
    for(int i=0;i<a.length;i++)
      a[i] = (Action[])l.removeFirst();
    return a;
  }

  ///////////////////////////////////////////////////////////////7

  public Connect4(int width, int height, Image[] pieces) {
    super( (width - 7*Board.CELL_SIZE)/2 + Board.CELL_SIZE/2,
           (height - 6*Board.CELL_SIZE)/2,      // this will center the board
           6,
           7,          // 6 rows and 7 columns
           pieces
         );
  }

} // endclass Connect4

////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////

public class Connect4Applet extends GameApplet {

  public Game createGame() {
    return new Connect4(appletWidth, appletHeight, piecesImg);
  }

  public Player createPlayer(int cpuTurn) {

    // this vector was made using the LUDAE project (www.di.fc.ul.pt/~jpn/ludae)
    double[][] vals = { {43,46,45,55,45,46,43}, // <- lower row, from a1 to g1
                        {45,47,46,68,46,47,45},
                        {49,57,50,65,50,57,49},
                        {52,62,71,58,71,62,52},
                        {52,68,67,56,67,68,52},
                        {48,52,51,49,51,52,48}  // <- upper row, from a6 to g6
                      };

    // From the 1st player (Black) point of view, an emtpy cell values 0,
    // a black soldier 1, and a white soldier -1
    double[] pieceVals = { 0.0, 1.0, -1.0 };

    // create the cell based tactic
    Tactic t = new TacticCell(vals, pieceVals);

    // create the player object
    return new Player(new Strategy(t), cpuTurn);
  }

}  // endclass Connect4Applet