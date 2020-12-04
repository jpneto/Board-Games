package toolbox;

/**
 * <p>Title: ACTION Class</p>
 *
 * <p>Description: An action is an atomic modification over a board.
 * This means that a game move is possibly made of several actions.
 * For eg, the checker move a3-b4 would be translated into two actions:
 * (i) remove white stone from a3, and (ii) insert white stone into b4<br>
 * This means that every possible game move can be described into set
 * of atomic actions. Besides the INS and REM actions, it's possible
 * to pass, to keep comments and to modify integer registers that are
 * useful in some game (like keeping the number of stones offboard, or
 * counting the actual number of captures for each side)</p>
 *
 * <p>Copyright: Copyright Joao Pedro Neto (c) 2003</p>
 * <p>Company: WAG website</p>
 * @author Joao Pedro Neto
 * @version 1.0
 */

public class Action {

  private char   id;     // the action identifier
  private Object args;   // the action arguments

  public static final char ADD = 'a',    // add a value to a register
                           INS = 'i',    // insert a piece at a cell
                           REM = 'r',    // remove a piece from a cell
                           PASS = 'p',   // do nothing
                           COMM = 'c';   // a comment

  ///////////////////////////////////////////////////////////////////////

  private class BoardCmd {        // includes INS and REM actions
    private char piece;
    private byte xx, yy;

    public BoardCmd(char p, byte x, byte y) {
      piece = p; xx = x; yy = y;
    }
  }

  private class RegisterCmd {     // includes ADD actions
    private byte register;
    private int  value;

    public RegisterCmd(byte r, int v) {
      register = r; value = v;
    }
  }

  ///////////////////////////////////////////////////////////////////////

  public char getActionId() {
    return id;
  }

  public char getPiece() {
    if (args instanceof BoardCmd) {
      return ((BoardCmd)args).piece;
    }
    return ' ';
  }

  public byte getX() {
    if (args instanceof BoardCmd) {
      return ((BoardCmd)args).xx;
    }
    return -1;
  }

  public byte getY() {
    if (args instanceof BoardCmd) {
      return ((BoardCmd)args).yy;
    }
    return -1;
  }

  public byte getRegister() {
    if (args instanceof RegisterCmd) {
      return ((RegisterCmd)args).register;
    }
    return -1;
  }

  public int getValue() {
    if (args instanceof RegisterCmd) {
      return ((RegisterCmd)args).value;
    }
    return -1;
  }

  public String getComment() {
    if (args instanceof String) {
      return (String)args;
    }
    return null;
  }

  public String toString() {
    switch (id) {
      case INS:
        return "Insert " + ((BoardCmd)args).piece +
               " at row " +  ((BoardCmd)args).xx +
               " and column " + +  ((BoardCmd)args).yy;
      case REM:
        return "Remove " + ((BoardCmd)args).piece +
               " from row " +  ((BoardCmd)args).xx +
               " and column " + +  ((BoardCmd)args).yy;
      case ADD:
        return "Add " + ((RegisterCmd)args).value +
               " to register " +  ((RegisterCmd)args).register;
      case PASS:
        return "A pass";
      case COMM:
        return "Comment: \"" + (String)args + "\"";
    }
    return "Unknown Action";
  }

  ///////////////////////////////////////////////////////////////////////

  public Action(char command, char piece, int xx, int yy) {
    id   = command;
    args = new BoardCmd(piece,(byte)xx,(byte)yy);
  }

  public Action(int register, int value) {
    id   = ADD;
    args = new RegisterCmd((byte)register,value);
  }

  public Action() {
    id   = PASS;
    args = null;
  }

  public Action(String comment) {
    id   = COMM;
    args = comment;
  }
}