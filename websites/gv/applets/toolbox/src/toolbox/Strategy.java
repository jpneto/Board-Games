package toolbox;

/**
 * <p>Title: STRATEGY Class</p>
 * <p>Description: A strategy is a weightened set of different tactics</p>
 * <p>Copyright: Copyright Joao Pedro Neto (c) 2003</p>
 * <p>Company: WAG website</p>
 * @author Joao Pedro Neto
 * @version 1.0
 */

public class Strategy {

  Tactic[] tactics;
  int[]    ponderation; // the relevance of each tactic

  public double valueOf(Game g) {
    double result=0.0;

    for(int i=0;i<tactics.length;i++)
      result += ponderation[i] * tactics[i].eval(g);

    return result;
  }

  //////////////////////// CONSTRUCTORS /////////////////////////

  public Strategy(Tactic t) {
    ponderation = new int[] { 1 };;
    tactics     = new Tactic[] { t };
  }

  public Strategy(int[] p, Tactic[] t) {
    ponderation = p;
    tactics     = t;
  }

}