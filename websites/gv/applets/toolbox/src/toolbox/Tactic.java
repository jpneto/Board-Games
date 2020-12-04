package toolbox;

import  java.math.*;

/**
 * <p>Title: TACTIC Class</p>
 *
 * <p>A tactic is a positional evaluation technique. Each game or
 * family of games should have one or more tactics to be able to
 * perform the best move searching. This class should be extended
 * but it provides a default random evaluator that works for any
 * game (not very well, as you might imagine :^)</p>
 *
 * <p>Copyright: Copyright Joao Pedro Neto (c) 2003</p>
 * <p>Company: WAG website</p>
 * @author Joao Pedro Neto
 * @version 1.0
 */

public class Tactic {

  protected String description;

  public String toString() {
    return description;
  }

  public double eval(Game g) {  // returns a value between [-1,1]
    return (Math.random()*2)-1;
  }

  public Tactic() {
    description = "Random Tactic";
  }
}