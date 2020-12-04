package toolbox;

/**
 * <p>Title: PLAYER Class</p>
 *
 * <p>Description: This class defines a computer player. It includes all
 * necessary methods to perform an alpha-beta search in a given game position<br>
 * It should receive one or two strategies to be able to perform an evaluation</p>
 *
 * <p>Copyright: Copyright Joao Pedro Neto (c) 2003</p>
 * <p>Company: WAG website</p>
 * @author Joao Pedro Neto
 * @version 1.0
 */

public class Player {

  private int  depth,  // depth of alpha-beta search
              myTurn;  // which is the cpu's turn

  {
    setDepth(5); // default value for alpha-beta search
  }

  private boolean twoStr; // if it has diff statregies for 1st and 2nd

  private Strategy   eval1,  // strategy when playing FIRST
                     eval2,  // strategy when playing SECOND
                     eval;   // the actual strategy

  private Action[][] bestPath;  // keeping the best move (used in alpha-beta)

  //////////////////////////////////////////////////////////

  public int getTurn() {
    return myTurn;
  }

  public void setTurn(int newTurn) {
    myTurn = newTurn;
  }

  public int getDepth() {
    return depth;
  }

  public void setDepth(int d) {
    depth = (d%2!=0)?d:d+1;
    bestPath = new Action[depth+1][];
  }

  //////////////////////////////////////////////////////////
  // alpha-beta - enhanced minmax searching
  //
  // game  - contains the actual position and the next player to move
  // ply   - the actual search depth (it will stop at zero
  // alpha - the lower bound of the value that a maximizing node
  //         may be assigned
  // beta  - the upper bound of the value that a minimizing node
  //         may be assigned

  public double alphaBeta(Game game, int ply, double alpha, double beta) {

    if (game.endOfGame()) {
      if (game.whoWon() == game.NOPLAYER) // a draw
        alpha = 0;
      else
        if (game.whoWon() != game.getActualPlayerId())
          alpha = Double.NEGATIVE_INFINITY;  // this player wins
        else
          alpha = Double.POSITIVE_INFINITY;  // this player loses
    }
    else if (ply <= 0) {   // end of search
      if (twoStr)          // if the player has two strategies, decide which
        eval = game.getActualPlayerId() == Game.FIRST ? eval1 : eval2;
      // find the estimated value of this position
      alpha = eval.valueOf(game);
    }
    else {

      Game next        = game.lightCopy();  // create a game copy
      Action[][] moves = next.validMoves(); // get the set of valid actions

      // make sure that always have a move
      // there is *always* at least one legal move (otherwise, the game
      // is not well defined and an NullPointerException will be raised)
      bestPath[depth-ply] = moves[0];

      for(int i=0;i<moves.length;i++) {

        // make move...
        next.doMove(moves[i]);

        // ...and recursively evaluate it (our negated beta
        // becomes the other guy's alpha, etc)
        double val = -alphaBeta(next,ply-1,-beta,-alpha);

        // is this a better move?
        if (val > alpha) {
          alpha = val;
          bestPath[depth-ply] = moves[i];
        }

        // can we deduce that there's no better move to be found?
        if (alpha >= beta) {
          alpha = val;
          bestPath[depth-ply] = moves[i];
          break;  // cut off!
        }

        // otherwise, unmake move to reuse game object 'next'
        next.undoMove(moves[i]);
      } // for(i)
    }
    return alpha;
  }  // alphaBeta()

  //////////////////////////////////////////////////////////
  //This is equal to all alfabeta players! Only changes the Strategy!

  public Action[] getMove(Game g) {  // gets the next move
    alphaBeta(g.lightCopy(),
              depth,
              Double.NEGATIVE_INFINITY,
              Double.POSITIVE_INFINITY);
    return bestPath[0];
  }

  //////////////////////// CONSTRUCTORS /////////////////////////

  public Player(Strategy s, int t) {
    myTurn   = t;
    twoStr   = false;
    eval     = s;
  }

  public Player(Strategy s1, Strategy s2, int t) {
    myTurn   = t;
    twoStr   = true;
    eval1    = s1;
    eval2    = s2;
  }

  public Player(int t) { // create only one random tactic for the default player
    myTurn   = t;
    twoStr   = false;
    eval     = new Strategy(
                     new int[]    { 1 },
                     new Tactic[] { new Tactic() }
                   );
  }

}