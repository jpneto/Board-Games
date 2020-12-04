package toolbox;

import java.awt.event.*;
import java.applet.Applet;
import java.awt.*;

/**
 * <p>Title: GAMeAPPLET Class</p>
 *
 * <p>Description: This class provides all the necessary human interface
 * to play a specific abstract board game. It is an abstract class, since
 * it requires a game object - thru createGame(), and a player object -
 * thru createPlayer(). A working applet must implement these two methods!</p>
 *
 * <p>Copyright: Copyright Joao Pedro Neto (c) 2003</p>
 * <p>Company: WAG website</p>
 * @author Joao Pedro Neto
 * @version 1.0
 */

public abstract class GameApplet extends Applet
                              implements MouseListener,
                                         KeyListener,
                                         FocusListener,
                                         ActionListener {
  Game    game, gameCopy;
  Player  player;
  String  move, lastMove,
          status="",
          version="v.1.10";
  int     cpuPlayer, humanPlayer;
  boolean makingMove   = false,
          wait4newGame = false;

  /////////////////////////////////////////////////////////////////
  ////////////////// ABSTRACT METHODS /////////////////////////////
  /////////////////////////////////////////////////////////////////

  abstract public Game   createGame();
  abstract public Player createPlayer(int cpuTurn);

  /////////////////////////////////////////////////////////////////
  // graphical attributes

  TextField comments;
  Color     background = new Color(213, 189, 131); // The board background
  Color     foreground = new Color(  0,   0,   0); // The board lines
  Graphics  bufferGraphics; // to write on, instead at the standard screen graphics
  Image     offscreen,      // for double buffering
            popup,          // the popup icon image
            back;           // the background image
  Image[]   piecesImg;      // the array of pieces images (loaded from files)

  int    appletWidth  = 300,  // default values, can be changed in the HTML
         appletHeight = 200;
  String backgPath    = "images/back-board.jpg";

  /////////////////////////////////////////////////////////////////
  // popup menu objects

  public PopupMenu menu;
  public MenuItem  items[];
         Menu      subMenu;
  public MenuItem  Entries[];

  String mainPopupLabels[]  = { "Pass","Resume","New Game","Switch","Hint" };
  String boardSubLabels[]   = { "Undo", "Redo", "Start", "End", "Copy", "Paste", "Swap" };
  String plySubLabels[]     = { "1", "2", "3*", "4" };

  /////////////////////////////////////////////////////////////////

  public void init() {

    // loading HTML parameters
    if (getParameter("WIDTH")!=null)
      appletWidth  = Integer.parseInt(getParameter("WIDTH"));

    if (getParameter("HEIGHT")!=null)
      appletHeight = Integer.parseInt(getParameter("HEIGHT"));

    if (getParameter("BACKG")!=null)
      backgPath = "images/" + getParameter("BACKG");

    // creating the comments textfield
    comments   = new TextField(appletWidth/12);  //note: add(comments);

    // loading icons images
    back   = getImage(getDocumentBase(),backgPath);
    popup  = getImage(getDocumentBase(),"images/iconPopup.gif");

    // loading pieces images
    piecesImg = new Image [4];

    if (getParameter("PIECES")!=null && getParameter("PIECES").equals("kings")) {
      // games of kings and soldiers
      piecesImg[0] = getImage(getDocumentBase(),"images/blacksoldier.gif");
      piecesImg[1] = getImage(getDocumentBase(),"images/redsoldier.gif");
      piecesImg[2] = getImage(getDocumentBase(),"images/blackking.gif");
      piecesImg[3] = getImage(getDocumentBase(),"images/redking.gif");
    } else {
      // games of soldiers
      piecesImg[0] = getImage(getDocumentBase(),"images/blackstone.gif");
      piecesImg[1] = getImage(getDocumentBase(),"images/whitestone.gif");
      piecesImg[2] = getImage(getDocumentBase(),"images/redstone.gif");
      piecesImg[3] = getImage(getDocumentBase(),"images/greenstone.gif");
    }

    if (getParameter("CPU")!=null && getParameter("CPU").equals("1st")) {
      cpuPlayer   = Game.FIRST;
      humanPlayer = Game.SECOND;
    } else {
      cpuPlayer   = Game.SECOND;
      humanPlayer = Game.FIRST;
    }

    /////////////////////////////////////////////////////////////////
    // creating the board object for this match
    game = createGame();

    /////////////////////////////////////////////////////////////////
    // creating a computer player
    player = createPlayer(cpuPlayer);

    /////////////////////////////////////////////////////////////////
    // popup menu construction
    //
    // shortcut eg   Entries[0].setShortcut(new MenuShortcut(65));
    // disable eg    Entries[5].setEnabled(false);

    menu    = new PopupMenu( "Popup Menu" );
    Entries = new MenuItem[16];
    int pp  = 0;

    Entries[pp] = new MenuItem (mainPopupLabels[0]);    menu.add(Entries[pp]);
    Entries[pp].setEnabled(game.canPass());
    Entries[pp++].addActionListener( this );
    Entries[pp] = new MenuItem (mainPopupLabels[1]);    menu.add(Entries[pp]);
    Entries[pp++].addActionListener( this );
    Entries[pp] = new MenuItem (mainPopupLabels[2]) ;   menu.add(Entries[pp]);
    Entries[pp++].addActionListener( this );
    Entries[pp] = new MenuItem (mainPopupLabels[3]) ;   menu.add(Entries[pp]);
    Entries[pp++].addActionListener( this );
    Entries[pp] = new MenuItem (mainPopupLabels[4]) ;   menu.add(Entries[pp]);
    Entries[pp++].addActionListener( this );

    // Entering Board SubMenu

    subMenu = new Menu ("Board");
    Entries[pp] = new MenuItem(boardSubLabels[0]);    subMenu.add(Entries[pp]);
    Entries[pp++].addActionListener( this );
    Entries[pp] = new MenuItem(boardSubLabels[1]);    subMenu.add(Entries[pp]);
    Entries[pp++].addActionListener( this );
    Entries[pp] = new MenuItem(boardSubLabels[2]);    subMenu.add(Entries[pp]);
    Entries[pp++].addActionListener( this );
    Entries[pp] = new MenuItem(boardSubLabels[3]);    subMenu.add(Entries[pp]);
    Entries[pp++].addActionListener( this );
    Entries[pp] = new MenuItem(boardSubLabels[4]);    subMenu.add(Entries[pp]);
    Entries[pp++].addActionListener( this );
    Entries[pp] = new MenuItem(boardSubLabels[5]);    subMenu.add(Entries[pp]);
    Entries[pp++].addActionListener( this );
    Entries[pp] = new MenuItem(boardSubLabels[6]);    subMenu.add(Entries[pp]);
    Entries[pp++].addActionListener( this );
    menu.add(subMenu);

    // Entering Ply SubMenu
    // These should be the last entries - check actionPerformed() below!

    subMenu = new Menu ("Ply");
    Entries[pp] = new MenuItem(plySubLabels[0]);    subMenu.add(Entries[pp]);
    Entries[pp++].addActionListener( this );
    Entries[pp] = new MenuItem(plySubLabels[1]);    subMenu.add(Entries[pp]);
    Entries[pp++].addActionListener( this );
    Entries[pp] = new MenuItem(plySubLabels[2]);    subMenu.add(Entries[pp]);
    Entries[pp++].addActionListener( this );
    Entries[pp] = new MenuItem(plySubLabels[3]);    subMenu.add(Entries[pp]);
    Entries[pp++].addActionListener( this );
    menu.add(subMenu);

    // add main popup menu
    add( menu );

    //////////////////////////////////////////////////

    addMouseListener(this);
    addFocusListener(this);
    addKeyListener(this);
    requestFocus();
  }

  //////////////////////////////////////////////////////////////////////

  public void paint(Graphics g) {

   if (offscreen == null) {

     // Programming note: Only when you go to first use the image do you
     // create it.  This will likely be in the Components paint() method.
     // Is this anywhere printed in an obvious place? No...

     // Create an offscreen image to draw on
     // Make it the size of the applet, this is just perfect
     // A larger size could slow it down unnecessary.
     offscreen = createImage(appletWidth,appletHeight);

     // by doing this everything that is drawn by bufferGraphics
     // will be written on the offscreen image.
     bufferGraphics = offscreen.getGraphics();
   }

   // Wipe off everything that has been drawn before
   // Otherwise previous drawings would also be displayed.
   bufferGraphics.setColor(background);
   bufferGraphics.fillRect(0,0,appletWidth,appletHeight);
   bufferGraphics.setColor(foreground);

   // place the background (if any...)
   if (back != null)
     for(int i=0;i<appletWidth;i+=back.getWidth(this))
       for(int j=0;j<appletHeight;j+=back.getHeight(this))
         bufferGraphics.drawImage(back, i, j,
                                  back.getWidth(this), back.getHeight(this),
                                  null, this);
   // place icons
   if (popup != null)
     bufferGraphics.drawImage(popup, 0, appletHeight-20, 20, 20, null, this);

   // print version
   bufferGraphics.drawString(version,appletWidth-6*version.length(),appletHeight-4);

   // print status or comments
   if (status!=null && status!="")
     bufferGraphics.drawString(status,22,appletHeight-4);
   else
     bufferGraphics.drawString(game.getActualComment(),22,appletHeight-4);

   // print board
   game.show(bufferGraphics);

   // Draw the offscreen image to the screen like a normal image.
   // Since offscreen is the screen width, we start at 0,0.
   g.drawImage(offscreen,0,0,this);

  } //paint()

  //////////////////////////////////////////////////////////////////////
  // Programming note: Always required for good double-buffering. This will
  // cause the applet not to first wipe off previous drawings but to
  // immediately repaint. The wiping off also causes flickering.
  // Update is called automatically when repaint() is called.

  public void update(Graphics g) {
    paint(g);
  }

  //////////////////////////////////////////////////////////////////////
  //////////////////////// LISTENERS ///////////////////////////////////
  //////////////////////////////////////////////////////////////////////

  /////////////////// MOUSE LISTENER ///////////////////////////////////

  public void mouseClicked(MouseEvent event) {}
  public void mouseExited(MouseEvent event) {}
  public void mouseEntered(MouseEvent event) {}
  public void mouseReleased(MouseEvent event) {}
  public void mousePressed(MouseEvent event) {

    requestFocus();

    // First check if there was a right click...
    if (event.getModifiers() == event.BUTTON3_MASK) {
      menu.show( this, event.getX(), event.getY() );   // ... then show popup menu
      return;
    }

    // Check if some icon was pressed...
    if ( event.getX()<16 && event.getY() > appletHeight-16 ) {  // popup icon selected
      menu.show( this, event.getX(), event.getY() );   // ... then show popup menu
      return;
    }

    // Get the board clicked cell
    String actualMove = game.getBoard().getCell(event.getX(),event.getY());
    String thisPlayer = game.getActualPlayer();

    // Check if it really was inside the board...
    if (!game.getBoard().isCellValid(actualMove))
      return;                             // if not, do nothing and return

    // Check if the game ended and no 'undo' or 'new game' order was issued
    if (wait4newGame)
      return;                             // if not, do nothing and return

    // Check if this is the human turn
    if (game.getActualPlayerId() != humanPlayer) {
      status = "CPU's turn, please resume";
      repaint();
      return;
    }

    // Clean Status
    status = "";

    // Check if a double click happened (which finishes a move)
    if (event.getClickCount() == 2) {
      makingMove = false;      // any eventual multimove is finished
      move = actualMove;
      game.executeMove(actualMove,comments.getText());
      comments.setText("");
      if (game.endOfGame()) {
        game.executeMove(game.setdown());
        wait4newGame = true;
      }
      repaint();

      // if the next player is the computer, get its move
      if (game.getActualPlayerId() == cpuPlayer) {
        game.executeMove(player.getMove(game));
        if (game.endOfGame()) {
          game.executeMove(game.setdown());
          wait4newGame = true;
        }
        repaint();
      }
      return;
    }

    // ok, we had a left-click
    if (!makingMove) {
      makingMove = true;
      lastMove = move = actualMove;
      game.getBoard().paintCell(this.getGraphics(),actualMove,Color.red);
    }
    else if (event.isShiftDown()) {     // this is a partial move

      if (!lastMove.equals(actualMove))
        move += "-" + actualMove;
      game.getBoard().paintCell(this.getGraphics(),actualMove,Color.blue);
      lastMove = actualMove;

    } else {  // multimove finishing

      makingMove = false;
      if (!lastMove.equals(actualMove))
        move += "-" + actualMove;
      game.executeMove(move,comments.getText());
      if (game.endOfGame()) {
        game.executeMove(game.setdown());
        wait4newGame = true;
      }
      repaint();

      // if the next player is the computer, get its move
      if (game.getActualPlayerId() == cpuPlayer) {
        game.executeMove(player.getMove(game));
        if (game.endOfGame()) {
          game.executeMove(game.setdown());
          wait4newGame = true;
        }
        repaint();
      }

    }

  } // mousePressed()

  ///////////////////// KEY LISTENER ///////////////////////////////////

  public void keyReleased ( KeyEvent event ) { }
  public void keyTyped ( KeyEvent event ) {

    if (!event.isControlDown())   // only CTRL keys are expected here
      return;

    switch (event.getKeyChar()) {
       case '\u0010':             // CTRL+p  PASS
         if (game.canPass()) {
           status = game.getActualPlayer() + " passed";
           game.executeMove(Game.PASS,comments.getText());
           repaint();
         }
         break;
       case '\u0003':             // CTRL+c  COPY
         gameCopy = (Game)game.clone();
         break;
       case '\u0016':             // CTRL+v  PASTE
         game = (Game)gameCopy.clone();
         repaint();
         break;
       case '\u0018':             // CTRL+x  SWAP
         if (gameCopy==null)
           return;
         Game temp = (Game)game.clone();
         game      = gameCopy;
         gameCopy  = temp;
         repaint();
         break;
       case '\u0019':             // CTRL+y  REDO
         if (event.isShiftDown()) {
           game.redoAll();
           repaint();
         }
         else {
           game.redo();
           repaint();
         }
         break;
       case '\u001A':             // CTRL+z  UNDO
         if (event.isShiftDown()) {
           game.undoAll();
           repaint();
         }
         else {
           game.undo();
           repaint();
         }
         break;
       case '\u0008':             // CTRL+h  HINT
         if (wait4newGame)
           return;

         if (player.getTurn()==Game.SECOND)
           player.setTurn(Game.FIRST);
         else
           player.setTurn(Game.SECOND);

         game.executeMove(player.getMove(game));
         if (game.endOfGame()) {
           game.executeMove(game.setdown());
           wait4newGame = true;
         }

         if (player.getTurn()==Game.SECOND)  // reset to initial value
           player.setTurn(Game.FIRST);
         else
           player.setTurn(Game.SECOND);
         repaint();
         break;
     } // switch
     wait4newGame = game.endOfGame();
   }

   public void keyPressed ( KeyEvent event ) {

     switch (event.getKeyCode()) {
       case 155: // insert
         comments.requestFocus();
         break;
       case 35:  // end
         game.redoAll();
         repaint();
         break;
       case 36:  // home
         game.undoAll();
         repaint();
         break;
       case 37:  // left arrow
         game.undo();
         repaint();
         break;
       case 38:  // up arrow
         game.redo();
         repaint();
         break;
       case 39:  // right arrow
         game.redo();
         repaint();
         break;
       case 40:  // down arrow
         game.undo();
         repaint();
         break;
     } // switch
     wait4newGame = game.endOfGame();
   }

  //////////////////// ACTION LISTENER /////////////////////////////////

  // mainly executes popup operations
  //eg to change labels: ((MenuItem)evt.getSource()).setLabel("whatever");
  public void actionPerformed( ActionEvent evt ){

   String label = ((MenuItem)evt.getSource()).getLabel();

   if( label == mainPopupLabels[0] ) {       // PASS (if possible)
     if (game.canPass()) {
       status = game.getActualPlayer() + " passed";
       game.executeMove(Game.PASS,comments.getText());
       repaint();
     }
   }
   else if( label == mainPopupLabels[1] ) {  // RESUME, ie, make a cpu move
     status = "";
     while (game.getActualPlayerId() == cpuPlayer) {
       game.executeMove(player.getMove(game));
       if (game.endOfGame()) {
         game.executeMove(game.setdown());
         wait4newGame = true;
       }
       repaint();
     }
   }
   else if( label == mainPopupLabels[2] ) {  // NEW GAME
     comments.setText("");
     status = "New game";
     game.clear();
     repaint();
   }
   else if( label == mainPopupLabels[3] ) {  // SWITCH SIDES
     if (humanPlayer == Game.FIRST) {
       cpuPlayer   = Game.FIRST;
       humanPlayer = Game.SECOND;
     } else {
       cpuPlayer   = Game.SECOND;
       humanPlayer = Game.FIRST;
     }
     player.setTurn(cpuPlayer);
   }
   else if ( label == mainPopupLabels[4] ) {  // HINT
     if (wait4newGame)
       return;

     if (player.getTurn()==Game.SECOND)
       player.setTurn(Game.FIRST);
     else
       player.setTurn(Game.SECOND);

     game.executeMove(player.getMove(game));
     if (game.endOfGame()) {
       game.executeMove(game.setdown());
       wait4newGame = true;
     }

     if (player.getTurn()==Game.SECOND)  // reset to initial value
       player.setTurn(Game.FIRST);
     else
       player.setTurn(Game.SECOND);

     repaint();
   }
   else if ( label == boardSubLabels[0] ) {  // UNDO
     game.undo();
     repaint();
   }
   else if ( label == boardSubLabels[1] ) {  // REDO
     game.redo();
     repaint();
   }
   else if ( label == boardSubLabels[2] ) {  // UNDO ALL
     game.undoAll();
     repaint();
   }
   else if ( label == boardSubLabels[3] ) {  // REDO ALL
     game.redoAll();
     repaint();
   }
   else if ( label == boardSubLabels[4] ) {  // COPY
     gameCopy = (Game)game.clone();
   }
   else if ( label == boardSubLabels[5] ) {  // PASTE
     game = (Game)gameCopy.clone();
     repaint();
   }
   else if ( label == boardSubLabels[6] ) {  // SWAP
     if (gameCopy==null)
       return;
     Game temp = (Game)game.clone();
     game = gameCopy;
     gameCopy = temp;
     repaint();
   }
   else if ( label == plySubLabels[0] ) {  // PLY 1
     player.setDepth(1);
     Entries[Entries.length-4].setLabel("1*");
     Entries[Entries.length-3].setLabel("2");
     Entries[Entries.length-2].setLabel("3");
     Entries[Entries.length-1].setLabel("4");
   }
   else if ( label == plySubLabels[1] ) {  // PLY 2
     player.setDepth(3);
     Entries[Entries.length-4].setLabel("1");
     Entries[Entries.length-3].setLabel("2*");
     Entries[Entries.length-2].setLabel("3");
     Entries[Entries.length-1].setLabel("4");
   }
   else if ( label == plySubLabels[2] ) {  // PLY 3
     player.setDepth(5);
     Entries[Entries.length-4].setLabel("1");
     Entries[Entries.length-3].setLabel("2");
     Entries[Entries.length-2].setLabel("3*");
     Entries[Entries.length-1].setLabel("4");
   }
   else if ( label == plySubLabels[3] ) {  // PLY 4
     player.setDepth(7);
     Entries[Entries.length-4].setLabel("1");
     Entries[Entries.length-3].setLabel("2");
     Entries[Entries.length-2].setLabel("3");
     Entries[Entries.length-1].setLabel("4*");
   }
   // insert other options...
   wait4newGame = game.endOfGame();
  }

  //////////////////// FOCUS LISTENER /////////////////////////////////

  public void focusGained(FocusEvent evt) {}
  public void focusLost(FocusEvent evt) {}

}  // end Class