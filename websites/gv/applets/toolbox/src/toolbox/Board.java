package toolbox;

import java.awt.*;
import java.awt.Color;
import java.net.*;

/**
 * <p>Title: BOARD Class</p>
 *
 * <p>Description: The board class has the following goals: (i) keep
 * the needed board information of an actual game match, (ii) provide
 * graphicalmethods for drawing and finding the right cells given mouse
 * clicks, (iii) perform set of Actions into the position, and (iv) offer
 * some miscellaneous methods to help finding endOfGames over
 * families of games (see checkNrow() for an example). <br> This is the
 * largest and more complex toolbox class.</p>
 *
 * <p>Copyright: Copyright Joao Pedro Neto (c) 2003</p>
 * <p>Company: WAG website</p>
 * @author Joao Pedro Neto
 * @version 1.0
 */

public class Board implements Cloneable {

  public static final char W_SOLDIER = 'o',
                           B_SOLDIER = 'x', // Black is ALWAYS the 1st player!!!
                           W_KING    = 'w',
                           B_KING    = 'b',
                           EMPTY     = '.',
                           NON_CELL  = '#',
                           STONE     = '~'; // a stack is made of stones

  private int[] registers;
  private String comment;

  {
    registers = new int[16];
    for(int i=0;i<16;i++)
      registers[i] = 0;
  }

  private char[][] position;   // the actual board configuration

  ////////////////////////////////////////////////////////////////////////

  /**
   * This method execute the actions described by move[]
   * @param move The array of actions of a given move
   */
  public void doMove(Action[] move) {

    comment = "";
    for(int i=0;i<move.length;i++)
      switch (move[i].getActionId()) {

        case Action.INS:
          position[move[i].getX()][move[i].getY()] = move[i].getPiece();
          break;

        case Action.REM:
          position[move[i].getX()][move[i].getY()] = EMPTY;
          break;

        case Action.ADD:
          registers[move[i].getRegister()] += move[i].getValue();
          break;

        case Action.PASS:  // do nothing
          break;
        case Action.COMM:
          comment = move[i].getComment();
          break;
      } // switch
  }

  /**
   * This method undo the actions described by move[]
   * @param move The array of actions of a given move
   */
  public void undoMove(Action[] move) {

    comment = "";
    for(int i=move.length-1;i>=0;i--)
      switch (move[i].getActionId()) {

        case Action.INS:
          position[move[i].getX()][move[i].getY()] = EMPTY;
          break;

        case Action.REM:
          position[move[i].getX()][move[i].getY()] = move[i].getPiece();
          break;

        case Action.ADD:
          registers[move[i].getRegister()] -= move[i].getValue();
          break;

        case Action.PASS:  // do nothing
          break;
        case Action.COMM:
          comment = move[i].getComment();
          break;
      } // switch
  }

  ////////////////////////////////////////////////////////////////////////

  public String point2coord(int r, int c) {  // eg, (1,4) -> "e2"
    String s;

    if (c<26)
      s = "" + Character.forDigit(c+10,36);
    else
      s = ""+Character.forDigit( 9+Math.round(c)/26,36) +
             Character.forDigit(10+Math.round(c)%26,36);

    return s + (r+1);
  }

  public Point coord2point(String id) {  // eg, "e2" -> (1,4)
    int letter=0;
    int digit=0;

    if (id.length()==2) {  // eg, "f5"
      letter = (int)id.charAt(0) - 96;
      digit  = Integer.parseInt(id.substring(1,2));
    }

    if (id.length()==3) {  // eg, "f15" or "aa1"
      if (id.charAt(1)>'0' && id.charAt(1)<'9') {  // eg, "f15"
        letter = (int)id.charAt(0) - 96;
        digit  = Integer.parseInt(id.substring(1,2))*10 +
                 Integer.parseInt(id.substring(2,3));
      } else {  // eg, "aa1"
        letter = ((int)id.charAt(0)-96)*26 + (int)id.charAt(1) - 96;
        digit  = Integer.parseInt(id.substring(2,3));
      }
    }

    if (id.length()==4) {  // eg, "ab13"
      letter = ((int)id.charAt(0)-96)*26 + (int)id.charAt(1) - 96;
      digit  = Integer.parseInt(id.substring(2,3))*10 +
               Integer.parseInt(id.substring(3,4));
    }

    return new Point(digit-1,letter-1);
  }

  ////////////////////////////////////////////////////////////////////////

  public void clearPosition() {
    if (isHex) {

     /*                        2 3 4
                              1 . . .                      3 # # . .
                               . . . .                     2 # . . .
        eg, board 3,4,2 is    a . . .    which becomes     1 . . . .
                               b . .                       0 . . . #
                                c d                          0 1 2 3
                                                            [a b c d]
     */

      for (int i=0;i<hexMiddle;i++)
        for (int j=0;j<hexMiddle;j++)
          if (isHexCellValid(point2coord(i,j)))
            position[i][j] = EMPTY;
          else
            position[i][j] = NON_CELL;
    } else {
        for (int i=0;i<rows;i++)
          for (int j=0;j<cols;j++)
            position[i][j] = EMPTY;   // eg, 'a4' is at position[3][0]
    }
  }

  ////////////////////////////////////////////////////////////////////////

  public boolean isHex() {
    return isHex;
  }

  public char getPositionCell(int x, int y) {
    return position[x][y];
  }

  public int getPositionIndex(int x, int y) {
    switch(position[x][y]) {
      case EMPTY:
      case NON_CELL:
        return 0;
      case B_SOLDIER:
        return 1;
      case W_SOLDIER:
        return 2;
      case B_KING:
        return 3;
      case W_KING:
        return 4;
    }
    return 0;  // should never be used...
  }

  public int getRows() {
    return isHex ? 0 : rows;
  }

  public int getCols() {
    return isHex ? 0 : cols;
  }

  public int getUpper() {
    return isHex ? hexUpper : 0;
  }

  public int getMiddle() {
    return isHex ? hexMiddle : 0;
  }

  public int getLower() {
    return isHex ? hexLower : 0;
  }

  ////////////////////////////////////////////////////////////////////////

  public String toString() {
    return isHex ? fromHex() : fromSq();
  }

  private String fromSq() {
    String s = "";

    for (int i=rows-1;i>=0;i--) {
      if (i<9)
        s += " ";
      s += (i+1) + " ";
      for (int j=0;j<cols;j++)
        s += position[i][j] + " ";
      s += "\n";
    }

    s += "   ";   // printing the final column letters
    for (int j=0;j<cols;j++) {
      if (j<26)
        s += Character.forDigit(j+10,36);
      else
        s += "" + Character.forDigit( 9+Math.round(j)/26,36) +
                  Character.forDigit(10+Math.round(j)%26,36);

      s += j<26?" ":"";
    }

    return s;
  }

  private String fromHex() {
    String s = "";

    for(int i=0;i<hexMiddle-hexUpper+1;i++)
      s += " ";
    for (int i=(hexMiddle-hexUpper+1);i<=hexMiddle;i++)  // print upper numbers
      s += i + (i<10?" ":"");
    s += "\n";

    for(int i=hexUpper;i<=hexMiddle;i++) {    // print upper board
      for(int j=0;j<hexMiddle-i;j++)
        s += " ";
      s += (((hexMiddle-i)==0)?" ":""+(hexMiddle-i)) + " ";
      for (int j=0;j<i;j++)
         s += position[hexMiddle-i+j][j] + " ";
      s += "\n";
    }

    for(int i=hexMiddle-1;i>=hexLower;i--) {  // print lower board
      for(int j=0;j<hexMiddle-i;j++)
        s += " ";

       s += ((hexMiddle-i)<=26? ""+Character.forDigit(9+(hexMiddle-i),36):
                            "a"+Character.forDigit((hexMiddle-i)-17,36)) + " ";

       for (int j=0;j<i;j++)
         s += position[j][hexMiddle-i+j] + " ";
      s += "\n";
    }

    for(int i=0;i<hexMiddle-hexLower+1;i++)
      s += " ";

    for (int i=0;i<hexLower;i++)  // print lower letters
      s += ((hexMiddle-hexLower+i+1)<=26? ""+Character.forDigit(9+(hexMiddle-hexLower+i+1),36):
                  "a"+Character.forDigit((hexMiddle-hexLower+i+1)-17,36)) + " ";

    return s;
  }

  ////////////////////////////////////////////////////////////////////////

  public String getComment() {
    return comment;
  }

  ////////////////////////////////////////////////////////////////////////

  public Object clone() {
    try {
      Board copy = (Board)super.clone();

      copy.registers  = (int[])registers.clone();
      copy.comment    = comment+"";
      copy.position   = (char[][])position.clone();
      for(int i=0;i<position.length;i++)
        copy.position[i] = (char[])position[i].clone();
      copy.background = background;
      copy.foreground = foreground;
      copy.myPieces   = (Image[])myPieces.clone();

      return copy;
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }

  ////////////////////////////////////////////////////////////////////////
  //////////////////////// GRAPHICAL  METHODS ////////////////////////////
  ////////////////////////////////////////////////////////////////////////

  private int     xBegin, yBegin;  // the top-left board point

  private boolean isHex;           // is this an Hex board?
  private int     rows, cols;      // square board dimension
  private int     hexUpper,
                  hexMiddle,
                  hexLower;        // hex board dimension

  private Color background, foreground;

  private Image[] myPieces;

  static final int CELL_SIZE = 20;  // for square boards

  /*            /\
   *           /  \    _
   *          |    |  |  HEX_EDGE
   *          |    |  |_
   *           \  /
   *            \/
   *          |____| HEX_WIDTH
   */

  static final int HEX_EDGE  = 14;
  static final int HEX_WIDTH = 24; // approx: sqrt[HEX_EDGE^2-(HEX_EDGE/2)^2]

  //////////////////////////////////////////////////////////////////////////
  //////////////////////// draw the cells //////////////////////////////////
  //////////////////////////////////////////////////////////////////////////

  private void drawSqCell(Graphics g, int x, int y) {
    g.drawRect(x,y,CELL_SIZE,CELL_SIZE);
  }

  private void drawHexCell(Graphics g, int x, int y) {
    int[] xx = {x+HEX_WIDTH/2,x+HEX_WIDTH,x+HEX_WIDTH,x+HEX_WIDTH/2,x,x};
    int[] yy = {y,y+HEX_EDGE/2,y+3*HEX_EDGE/2,y+2*HEX_EDGE,y+3*HEX_EDGE/2,y+HEX_EDGE/2};

    g.drawPolygon(xx,yy,6);
  }

  //////////////////////////////////////////////////////////////////////////
  //////////////////////// mark the cells //////////////////////////////////
  //////////////////////////////////////////////////////////////////////////

  public void paintCell(Graphics g, String id, Color cl) {
    if (!isCellValid(id))
      return;

    if (isHex)
      paintHexCell(g,id,cl);
    else
      paintSqCell(g,id,cl);
  }

  private void paintSqCell(Graphics g, String id, Color cl) {

    int xTopLeft = xBegin;
    int yTopLeft = yBegin;

    if (id.length()==2) {  // eg, "f5"
      xTopLeft += ((int)id.charAt(0) - 97) * CELL_SIZE  - CELL_SIZE/2;
      yTopLeft += (rows - Integer.parseInt(id.substring(1,2))) * CELL_SIZE + CELL_SIZE/2;
    }

    if (id.length()==3) {  // eg, "f15" or "aa1"
      if (id.charAt(1)>'0' && id.charAt(1)<'9') {  // eg, "f15"
        xTopLeft += ((int)id.charAt(0) - 97) * CELL_SIZE  - CELL_SIZE/2;
        yTopLeft += (rows - (Integer.parseInt(id.substring(1,2))*10 +
                             Integer.parseInt(id.substring(2,3)))) * CELL_SIZE + CELL_SIZE/2;
      } else {  // eg, "aa1"
        xTopLeft += (((int)id.charAt(0)-96)*26 + (int)id.charAt(1) - 97) * CELL_SIZE  - CELL_SIZE/2;
        yTopLeft += (rows - Integer.parseInt(id.substring(2,3))) * CELL_SIZE + CELL_SIZE/2;
      }
    }

    if (id.length()==4) {  // eg, "ab13"
      xTopLeft += (((int)id.charAt(0)-96)*26 + (int)id.charAt(1) - 97) * CELL_SIZE  - CELL_SIZE/2;
      yTopLeft += (rows - (Integer.parseInt(id.substring(2,3))*10 +
                          Integer.parseInt(id.substring(3,4)))) * CELL_SIZE + CELL_SIZE/2;
    }

    g.setColor(cl);
    g.drawRect(xTopLeft+5,yTopLeft-CELL_SIZE+5,CELL_SIZE-10,CELL_SIZE-10);
    g.setColor(foreground);
  }

  private void paintHexCell(Graphics g, String id, Color cl) {

    Point p = coord2point(id);

    int ii = p.getX();
    int jj = p.getY();

    g.setColor(cl);
    if (jj>ii) { // lower half

      int i = hexMiddle-(jj-ii);
      int j = ii;
      int x = xBegin + (hexUpper-i)*HEX_WIDTH/2+j*HEX_WIDTH + HEX_WIDTH/4;
      int y = yBegin + (2*hexMiddle-hexUpper-i)*3*HEX_EDGE/2 + HEX_EDGE/2;
      int[] xx = {x+HEX_WIDTH/4,x+HEX_WIDTH/2,x+HEX_WIDTH/2,x+HEX_WIDTH/4,x,x};
      int[] yy = {y,y+HEX_EDGE/4,y+3*HEX_EDGE/4,y+2*HEX_EDGE/2,y+3*HEX_EDGE/4,y+HEX_EDGE/4};

      g.drawPolygon(xx,yy,6);

    } else {    // upper half (including the main line)

      int i = hexMiddle-(ii-jj);
      int j = jj;
      int x = xBegin - (i-hexUpper)*HEX_WIDTH/2+j*HEX_WIDTH + HEX_WIDTH/4;
      int y = yBegin + (i-hexUpper)*3*HEX_EDGE/2 + HEX_EDGE/2;
      int[] xx = {x+HEX_WIDTH/4,x+HEX_WIDTH/2,x+HEX_WIDTH/2,x+HEX_WIDTH/4,x,x};
      int[] yy = {y,y+HEX_EDGE/4,y+3*HEX_EDGE/4,y+2*HEX_EDGE/2,y+3*HEX_EDGE/4,y+HEX_EDGE/4};

      g.drawPolygon(xx,yy,6);
    }
    g.setColor(foreground);
  }

  //////////////////////////////////////////////////////////////////////////
  //////////////////////// mark the cells //////////////////////////////////
  //////////////////////////////////////////////////////////////////////////

  public void paintStone(Graphics g, String id, Image im) {
    if (!isCellValid(id))
      return;

    if (isHex)
      paintHexStone(g,id,im);
    else
      paintSqStone(g,id,im);
  }

  private void paintSqStone(Graphics g, String id, Image im) {

    int xTopLeft = xBegin;
    int yTopLeft = yBegin;

    if (id.length()==2) {  // eg, "f5"
      xTopLeft += ((int)id.charAt(0) - 97) * CELL_SIZE  - CELL_SIZE/2;
      yTopLeft += (rows - Integer.parseInt(id.substring(1,2))) * CELL_SIZE + CELL_SIZE/2;
    }

    if (id.length()==3) {  // eg, "f15" or "aa1"
      if (id.charAt(1)>'0' && id.charAt(1)<'9') {  // eg, "f15"
        xTopLeft += ((int)id.charAt(0) - 97) * CELL_SIZE  - CELL_SIZE/2;
        yTopLeft += (rows - (Integer.parseInt(id.substring(1,2))*10 +
                             Integer.parseInt(id.substring(2,3)))) * CELL_SIZE + CELL_SIZE/2;
      } else {  // eg, "aa1"
        xTopLeft += (((int)id.charAt(0)-96)*26 + (int)id.charAt(1) - 97) * CELL_SIZE  - CELL_SIZE/2;
        yTopLeft += (rows - Integer.parseInt(id.substring(2,3))) * CELL_SIZE + CELL_SIZE/2;
      }
    }

    if (id.length()==4) {  // eg, "ab13"
      xTopLeft += (((int)id.charAt(0)-96)*26 + (int)id.charAt(1) - 97) * CELL_SIZE  - CELL_SIZE/2;
      yTopLeft += (rows - (Integer.parseInt(id.substring(2,3))*10 +
                          Integer.parseInt(id.substring(3,4)))) * CELL_SIZE + CELL_SIZE/2;
    }

    g.drawImage(im, xTopLeft, yTopLeft-CELL_SIZE, CELL_SIZE, CELL_SIZE, null, null);

  }

  private void paintHexStone(Graphics g, String id, Image im) {

    Point p = coord2point(id);

    int ii = p.getX();
    int jj = p.getY();

    if (jj>ii) { // lower half

      int i = hexMiddle-(jj-ii);
      int j = ii;
      int x = xBegin + (hexUpper-i)*HEX_WIDTH/2+j*HEX_WIDTH + HEX_WIDTH/4;
      int y = yBegin + (2*hexMiddle-hexUpper-i)*3*HEX_EDGE/2 + HEX_EDGE/2;

      g.drawImage(im, x-2, y-1, HEX_EDGE+2, HEX_EDGE+2, null, null);

    } else {    // upper half (including the main line)

      int i = hexMiddle-(ii-jj);
      int j = jj;
      int x = xBegin - (i-hexUpper)*HEX_WIDTH/2+j*HEX_WIDTH + HEX_WIDTH/4;
      int y = yBegin + (i-hexUpper)*3*HEX_EDGE/2 + HEX_EDGE/2;

      g.drawImage(im, x-2, y-1, HEX_EDGE+2, HEX_EDGE+2, null, null);
    }
  }

  //////////////////////////////////////////////////////////////////////////
  //////////////////////// draw the boards /////////////////////////////////
  //////////////////////////////////////////////////////////////////////////

  /**
   * Draw the board
   *
   * @param g The Graphical Object where the board is drawn
   * @param l Print labels? If true, the top-left is a bit higher than (x,y)
   */
  public void drawBoard(Graphics g, boolean l) {
    g.setColor(foreground);
    if (isHex)
      drawHexBoard(g,l);
    else
      drawSqBoard(g,l);
  }

  private void drawSqBoard(Graphics g, boolean l) {

    for(int i=0;i<rows-1;i++)
      for(int j=0;j<cols-1;j++) {
        drawSqCell(g,xBegin+j*CELL_SIZE,yBegin+i*CELL_SIZE);
      }

    if (l)    // print cols coordinates
      for(int i=0;i<cols;i++)
        if (i<=25)
          g.drawString(""+Character.forDigit(10+i,36),xBegin+i*CELL_SIZE-2,yBegin+(rows)*CELL_SIZE);
        else {
          String letter = ""+Character.forDigit( 9+Math.round(i)/26,36) +
                             Character.forDigit(10+Math.round(i)%26,36);
          g.drawString(letter,xBegin+i*CELL_SIZE-2-6,yBegin+(rows)*CELL_SIZE+2);
        }

    if (l)    // print rows coordinates
      for(int i=0;i<rows;i++)
        if (rows-i<=9)
          g.drawString(""+(rows-i),xBegin-CELL_SIZE/2-8,yBegin+i*CELL_SIZE+4);
        else
          g.drawString(""+(rows-i),xBegin-CELL_SIZE/2-15,yBegin+i*CELL_SIZE+4);
  }

  private void drawHexBoard(Graphics g, boolean l) {

    if (l)    // print upper digits
      for (int i=(hexMiddle-hexUpper+2);i<=hexMiddle;i++)
        g.drawString(""+i,xBegin+(i-hexMiddle+hexUpper-1)*HEX_WIDTH-2,yBegin);

    for(int i=hexUpper;i<=hexMiddle;i++) {    // print upper board
      if (l)
        g.drawString(""+(hexMiddle-i+1),xBegin-(i-hexUpper)*HEX_WIDTH/2-2,yBegin+(i-hexUpper)*3*HEX_EDGE/2);
      for (int j=0;j<i;j++)
         drawHexCell(g,xBegin-(i-hexUpper)*HEX_WIDTH/2+j*HEX_WIDTH,yBegin+(i-hexUpper)*3*HEX_EDGE/2);
    }

    for(int i=hexMiddle-1;i>=hexLower;i--) {  // print lower board
      if (l)
        g.drawString(((hexMiddle-i)<=26? ""+Character.forDigit(9+(hexMiddle-i),36):
                                 "a"+Character.forDigit((hexMiddle-i)-17,36)),
                     xBegin+(hexUpper-i-1)*HEX_WIDTH/2,
                     yBegin+(2*hexMiddle-hexUpper-i+1)*3*HEX_EDGE/2);
      for (int j=0;j<i;j++)
        drawHexCell(g,xBegin+(hexUpper-i)*HEX_WIDTH/2+j*HEX_WIDTH,yBegin+(2*hexMiddle-hexUpper-i)*3*HEX_EDGE/2);
    }

    if (l)   // print lower letters
      for (int i=0;i<hexLower;i++)
        g.drawString(((hexMiddle-hexLower+i+1)<=26? ""+Character.forDigit(9+(hexMiddle-hexLower+i+1),36):
                                     "a"+Character.forDigit((hexMiddle-hexLower+i+1)-17,36)),
                     xBegin+(hexUpper-hexLower)*HEX_WIDTH/2+i*HEX_WIDTH,
                     yBegin+(2*hexMiddle-hexUpper-hexLower+2)*3*HEX_EDGE/2);
  }

  //////////////////////////////////////////////////////////////////////////
  //////////////////////// draw the board position /////////////////////////
  //////////////////////////////////////////////////////////////////////////

  /**
   * Draw the board position
   *
   * @param g The Graphical Object where the board is drawn
   * @param l Print labels? If true, the top-left is a bit higher than (x,y)
   */
  public void drawPosition(Graphics g, boolean l) {
    g.setColor(foreground);
    if (isHex)
      drawHexPosition(g,l);
    else
      drawSqPosition(g,l);
  }

  private void drawSqPosition(Graphics g, boolean l) {

    drawSqBoard(g,l);

    for (int i=0;i<rows;i++)
      for (int j=0;j<cols;j++)
        if (position[i][j] != EMPTY)
          paintStone(g,point2coord(i,j),getImage(position[i][j]));
  }

  private void drawHexPosition(Graphics g, boolean l) {

    drawHexBoard(g,l);

    for (int i=0;i<hexMiddle;i++)
      for (int j=0;j<hexMiddle;j++)
        if ((position[i][j] != EMPTY) && (position[i][j] != NON_CELL))
          paintStone(g,point2coord(i,j),getImage(position[i][j]));
  }

  //////////////////////////////////////////////////////////////////////////
  //////////////////////// get cells methods ///////////////////////////////
  //////////////////////////////////////////////////////////////////////////

  /**
   * Returns the description of the cell at mouse position (x,y)
   *
   * @param x      The input x
   * @param y      The input y
   * @return The cell description (like "a2" or "g8"), otherwise ""
   */
  public String getCell(int x, int y) {
    return (isHex)? getHexCell(x,y) : getSqCell(x,y);
  }

  private double dist(int x1, int y1, int x2, int y2) {
    return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
  }

  private String getSqCell(int x, int y) {

    float i = (float)(x-xBegin) / CELL_SIZE;
    float j = (float)(y-yBegin) / CELL_SIZE;
    String letter;

    if (!(i>=-0.5 && j>=-0.5 && i<=cols+0.5 && j<=rows+0.5))
      return "";

    if (i<=25.5)  // eg, "d"
      letter = ""+Character.forDigit(10+Math.round(i),36);
    else          // eg, "ab"
      letter = ""+Character.forDigit( 9+Math.round(i)/26,36) +
                  Character.forDigit(10+Math.round(i)%26,36);

    return letter + (rows-(int)Math.round(j));
  }

  private String getHexCell(int x, int y) {

    double min = HEX_EDGE, actual;
    int    digit=0;
    String letter =" ";

    for(int i=hexUpper;i<=hexMiddle;i++)    // check upper board
      for (int j=0;j<i;j++) {
        actual = dist ( xBegin-(i-hexUpper)*HEX_WIDTH/2+j*HEX_WIDTH+HEX_WIDTH/2,
                        yBegin+(i-hexUpper)*3*HEX_EDGE/2+HEX_EDGE,
                        x, y );
        if (actual<min) {
          min   = actual;
          if (j<26)
            letter = ""+Character.forDigit(10+j,36);
          else
            letter = ""+Character.forDigit( 9+j/26,36) +
                        Character.forDigit(10+j%26,36);
          digit  = j+(hexMiddle-i)+1;
        }
      }

    for(int i=hexMiddle-1;i>=hexLower;i--)   // check lower board
      for (int j=0;j<i;j++) {
        actual = dist ( xBegin+(hexUpper-i)*HEX_WIDTH/2+j*HEX_WIDTH+HEX_WIDTH/2,
                        yBegin+(2*hexMiddle-hexUpper-i)*3*HEX_EDGE/2+HEX_EDGE,
                        x, y );
        if (actual<min) {
          min   = actual;
          if (j+(hexMiddle-i)<26)
            letter = ""+Character.forDigit(10+j+(hexMiddle-i),36);
          else
            letter = ""+Character.forDigit( 9+(j+(hexMiddle-i))/26,36) +
                        Character.forDigit(10+(j+(hexMiddle-i))%26,36);
          digit  = i+j-(i-1);
        }
      }

    if (letter.equals(" "))  // not inside the board
      return "";

    return ""+letter+digit;
  }

  //////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////

  /**
   * Checks if the cell description is valid within this board
   *
   * @param id The cell description (like "ab2" or "g8")
   * @return true is the descriptions exists in the board
   */
  public boolean isCellValid(String id) {
    if (id.equals("") || id.length()>4)
      return false;

    return (isHex)? isHexCellValid(id) : isSqCellValid(id);
  }

  private boolean isSqCellValid(String id) {

    Point p = coord2point(id);

    return p.getX() >= 0 && p.getY() >= 0 && p.getY()<cols && p.getX()<rows;
  }

  private boolean isHexCellValid(String id) {

    int ii = 0;
    int jj = 0;

    if (id.length()==2) {  // eg, "f5"
      ii = Character.digit(id.charAt(1),10);
      jj = Character.digit(id.charAt(0),36)-10;
     }

    if (id.length()==3) {  // eg, "f15" or "aa1"
      if (id.charAt(1)>'0' && id.charAt(1)<'9') {  // eg, "f15"
        ii = Character.digit(id.charAt(1),10)*10 + Character.digit(id.charAt(2),10);
        jj = Character.digit(id.charAt(0),36)-10;
      } else {  // eg, "aa1"
        ii = Character.digit(id.charAt(2),10);
        jj = (Character.digit(id.charAt(0),36)-9)*26 + Character.digit(id.charAt(1),36)-10;
      }
    }

    if (id.length()==4) {  // eg, "ab13"
      ii = Character.digit(id.charAt(2),10)*10 + Character.digit(id.charAt(3),10);
      jj = (Character.digit(id.charAt(0),36)-9)*26 + Character.digit(id.charAt(1),36)-10;
    }

    if (ii<=0)
      return false;

    if (jj>ii) // lower half
      return jj-ii+1 <= hexMiddle - hexLower;
    else       // upper half
      return ii-jj-1 <= hexMiddle - hexUpper;
  }

  //////////////////////////////////////////////////////////////////////////

  public Image getImage(char piece) {
    switch (piece) {
      case B_SOLDIER:
        return myPieces[0];
      case W_SOLDIER:
        return myPieces[1];
      case B_KING:
        return myPieces[2];
      case W_KING:
        return myPieces[3];
    } //switch
    return null;
  }

  //////////////////////////////////////////////////////////////////////////
  ///////////////////////  CONSTRUCTORS  ///////////////////////////////////
  //////////////////////////////////////////////////////////////////////////

  public Board(int x, int y, int r, int c, Color bc, Color fc, Image[] pieces) {  // create a square board

    xBegin = x;
    yBegin = y;
    rows   = r;
    cols   = c;
    isHex = false;
    background = bc;
    foreground = fc;
    myPieces = pieces;
    comment = "";
    position = new char[rows][cols];
    clearPosition();
  }

  public Board(int x, int y, int a, int b, int c, Color bc, Color fc, Image[] pieces) {  // create an hex board

    xBegin = x;
    yBegin = y;
    hexUpper  = a;
    hexMiddle = b;
    hexLower  = c;
    isHex = true;
    background = bc;
    foreground = fc;
    myPieces = pieces;
    comment = "";
    position = new char[b][b];
    clearPosition();
  }

  ////////////////// AUXILIARY METHODS ////////////////////////

  // checks if there's a N in-a-row of 'piece' on the board
  public boolean checkNrow(int N, char piece) {
    boolean fail;

    // check horizontals
    for(int i=0;i<position.length;i++)
      for(int j=0;j<=position[0].length-N;j++) {
        fail = false;
        for(int k=0;k<N && !fail;k++)
          if (position[i][j+k] != piece)
            fail = true;
        if (!fail)
          return true;
      }

    // check verticals
    for(int j=0;j<position[0].length;j++)
      for(int i=0;i<=position.length-N;i++) {
        fail = false;
        for(int k=0;k<N && !fail;k++)
          if (position[i+k][j] != piece)
            fail = true;
        if (!fail)
          return true;
      }

    // check 45º diagonals
    for(int i=0;i<position.length-N;i++)
      for(int j=0;j<=position[0].length-N;j++) {
        fail = false;
        for(int k=0;k<N && !fail;k++)
          if (position[i+k][j+k] != piece)
            fail = true;
        if (!fail)
          return true;
      }

    // check 135º diagonals - just for square boards!
    if (!isHex)
      for(int i=position.length-1;i>N-2;i--)
        for(int j=0;j<=position[0].length-N;j++) {
          fail = false;
          for(int k=0;k<N && !fail;k++)
            if (position[i-k][j+k] != piece)
              fail = true;
          if (!fail)
            return true;
        }

    return false;
  } // checkNrow()

}