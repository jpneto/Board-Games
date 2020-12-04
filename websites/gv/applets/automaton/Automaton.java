/* Automaton.java */

/* Automaton Wars in Java
 * (K) All Rites Reversed -- Copy What You Like
 *
 * Authors:
 *      Peter Hartley   <peter@ant.co.uk>
 *
 * History:
 *      22/06/97 pdh Started
 *      23/06/97 pdh Add buttons, opponent (thick)
 *      24/06/97 pdh Add notSoThick opponent, improve graphics
 *      06/09/97 pdh Add replaying of games, hall-of-fame stuff
 *		14/09/97 pdh Fix hall-of-fame stuff for Java 1.1 (grrr)
 *
 * See also:
 *      http://www.ant.co.uk/~peter/java/   Peter's Java playground
 *
 */

import java.applet.*;
import java.awt.*;
import java.util.*;
import java.net.*;
import java.io.*;



        /*================*
         *   The applet   *
         *================*/


public class Automaton extends Applet
{
    // GUI components
    AutomatonBoard thePanel;
    Button butNewGame;
    Choice opponent;
    Choice firstPlayer;

    Button butNext;
    Scrollbar sbReplay;
    TextField tfReplayPos;

    TextField tfSubmitName;
    TextField tfSubmitEmail;
    TextField tfSubmitMoves;
    Button butSubmit;

	boolean senddetails;

	static final String GAME = "game";
	static final String SEND = "send";

    int XSIZE, YSIZE;

    String boardSquares, replay;

    public void init()
    {
        super.init();

		senddetails = false;

        String value = getParameter( "xsize" );

        try
		{
            XSIZE = Integer.parseInt( value );
		}
        catch ( NumberFormatException e )
		{
            XSIZE = 6;
		}

        if ( XSIZE < 4 || XSIZE > 10 )
            XSIZE = 6;

        value = getParameter( "ysize" );

        try
		{
            YSIZE = Integer.parseInt( value );
		}
        catch ( NumberFormatException e )
		{
            YSIZE = 6;
		}

        if ( YSIZE < 4 || YSIZE > 10 )
            YSIZE = 6;

        boardSquares = getParameter( "board" );

        if ( boardSquares != null )
        {
            // Allow |...| for space suppression (bloody internet bloody explorer)
            if ( boardSquares.length() == XSIZE*YSIZE+2 )
                boardSquares = boardSquares.substring( 1, XSIZE*YSIZE+1 );

            if ( boardSquares.length() != XSIZE*YSIZE )
                boardSquares = null;
        }

        replay = getParameter( "replay" );
        //replay = "rA9F4SCSGACAZBCSBY4MHGBSIMASTAMAHAHFIEHFHEBDBKTI6A6DYU4E4W4XV";

        this.setLayout( new CardLayout() );

        Panel p1 = new Panel();     // with the board in
        Panel p2 = new Panel();     // congratulations

        // Exciting gridbag layout stuff

        p1.setLayout( new GridBagLayout() );

        thePanel = new AutomatonBoard( this, XSIZE, YSIZE, boardSquares, replay );

        gridBagHelper( p1, thePanel, 0, 0, 3, 1, GridBagConstraints.NONE, 0 );

        if ( replay == null )
        {
            butNewGame = new Button("New game");

            opponent = new Choice();
            opponent.addItem( "2 Player" );
            opponent.addItem( "v Computer (thick)" );
            opponent.addItem( "v Computer (not so thick)" );

            firstPlayer = new Choice();
            firstPlayer.addItem( "Blue goes first" );
            firstPlayer.addItem( "Red goes first" );

            gridBagHelper( p1, opponent,    0, 1, 1, 1, GridBagConstraints.NONE,       2 );
            gridBagHelper( p1, butNewGame,  1, 1, 1, 1, GridBagConstraints.HORIZONTAL, 2 );
            gridBagHelper( p1, firstPlayer, 2, 1, 1, 1, GridBagConstraints.NONE,       2 );

            p2.setLayout( new GridBagLayout() );

            gridBagHelper( p2, new Label("You've won: congratulations!"), 0, 0, 2, 1, GridBagConstraints.HORIZONTAL, 0 );
            gridBagHelper( p2, new Label("Now enter the Hall of Fame..."), 0, 1, 2, 1, GridBagConstraints.HORIZONTAL, 0 );

            tfSubmitName = new TextField("");
            gridBagHelper( p2, new Label("Name:"), 0, 2, 1, 1, GridBagConstraints.NONE, 2 );
            gridBagHelper( p2, tfSubmitName,       1, 2, 1, 1, GridBagConstraints.HORIZONTAL, 0 );

            tfSubmitEmail = new TextField("");
            gridBagHelper( p2, new Label("Email:"), 0, 3, 1, 1, GridBagConstraints.NONE, 2 );
            gridBagHelper( p2, tfSubmitEmail,       1, 3, 1, 1, GridBagConstraints.HORIZONTAL, 0 );
            gridBagHelper( p2, new Label("(email address will not appear on any Web page)"), 1, 4, 1, 1, GridBagConstraints.HORIZONTAL, 0 );

            tfSubmitMoves = new TextField("<moves>");
            tfSubmitMoves.setEditable( false );
            gridBagHelper( p2, tfSubmitMoves,       1, 5, 1, 1, GridBagConstraints.HORIZONTAL, 2 );

            butSubmit = new Button("Submit");
            gridBagHelper( p2, butSubmit,           0, 6, 2, 1, GridBagConstraints.HORIZONTAL, 10 );
        }
        else
        {
            sbReplay = new Scrollbar( Scrollbar.HORIZONTAL, 0, 1, 0, replay.length()-2 );

            tfReplayPos = new TextField(3);
            tfReplayPos.setEditable(false);
            tfReplayPos.setText("1");

            butNext = new Button("Next");

            gridBagHelper( p1, tfReplayPos, 0, 1, 1, 1, GridBagConstraints.NONE,       2 );
            gridBagHelper( p1, sbReplay,    1, 1, 1, 1, GridBagConstraints.HORIZONTAL, 2 );
            gridBagHelper( p1, butNext,     2, 1, 1, 1, GridBagConstraints.NONE,       2 );
            gridBagHelper( p1, new Label("Replay game!"), 0, 2, 3, 1, GridBagConstraints.HORIZONTAL, 2 );
        }

        this.add( GAME, p1 );
        this.add( SEND, p2 );

        p1.setBackground( Color.white );
		
        ((CardLayout)this.getLayout()).show( this, GAME );
	}

    // O'Reilly p111
    public void gridBagHelper( Container cont, Component comp, int x, int y,
                               int w, int h, int fill, int border )
    {
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = x;
        c.gridy = y;
        c.gridwidth = w;
        c.gridheight = h;
        c.fill = fill;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(border,border,border,border);
        c.weightx = ( fill == GridBagConstraints.HORIZONTAL ) ? 1.0 : 0.0;
        c.weighty = 0.0;

        ((GridBagLayout)cont.getLayout()).setConstraints( comp, c );
        cont.add( comp );
    }

	public String getAppletInfo()
	{
		return "Automaton Wars in Java, version 1.30\n" +
		       "by Peter Hartley 1997  (K) All Rites Reversed -- Copy What You Like\n" +
               "See http://www.ant.co.uk/~peter/java/ for details";
	}

    public String[][] getParameterInfo()
    {
        String[][] answer = {
            { "xsize", "number 4..10", "width of board, default 6" },
            { "ysize", "number 4..10", "length of board, default 6" },
            { "board", "string, xsize*ysize chars long", "cells present, default all" }
        };
        return answer;
    }

    public void update( Graphics g )
    {
        paint(g);
    }

    synchronized public void winner( StringBuffer how )
    {
		senddetails = true;
        tfSubmitMoves.setText( how.toString() );
        ((CardLayout)this.getLayout()).show( this, SEND );
    }

	public void start()
	{
        MediaTracker tracker;
        int i;

        this.showStatus( "Loading images" );

        tracker = new MediaTracker(this);

        if ( thePanel.isTraditional() )
        {
            // Traditional (POVray'd) board in three pieces
            thePanel.bg = this.getImage( this.getDocumentBase(), "bg.gif" );
            thePanel.b1 = this.getImage( this.getDocumentBase(), "b1.gif" );
            thePanel.b2 = this.getImage( this.getDocumentBase(), "b2.gif" );
            tracker.addImage(thePanel.bg,0);
            tracker.addImage(thePanel.b1,0);
            tracker.addImage(thePanel.b2,0);
        }
        else
        {
            // Custom board in lots of little pieces
            thePanel.cmb = new Image[3];
            for ( i=0; i<3; i++ )
            {
                thePanel.cmb[i] = this.getImage( this.getDocumentBase(), "cmb"+i+".gif" );
                tracker.addImage( thePanel.cmb[i], 0 );
            }
        }

        thePanel.pieces = new Image[thePanel.NPIECES];

        for ( i=0; i<thePanel.NPIECES; i++ )
        {
            thePanel.pieces[i] = this.getImage( this.getDocumentBase(), "p"+(i+1)+".gif" );
            tracker.addImage( thePanel.pieces[i], 0 );
        }

        try
        {
            tracker.waitForID(0);
        }
        catch (InterruptedException e)
        {
        }

        if ( tracker.isErrorID(0) )
        {
            this.showStatus( "Error loading images" );
            return;
        }

        this.showStatus( "Images loaded OK" );

        thePanel.forceRedraw();
	}

    public boolean handleEvent( Event event )
    {
        switch ( event.id )
        {
        case event.ACTION_EVENT:
            if ( event.target == null )
            {
            }
            else if ( event.target == butNewGame )
            {
                thePanel.newGame();
                thePanel.forceRedraw();
            }
            else if ( event.target == opponent )
            {
                thePanel.setOpponent( opponent.getSelectedIndex() );
            }
            else if ( event.target == firstPlayer )
            {
                thePanel.setFirstPlayer( firstPlayer.getSelectedIndex() );
            }
            else if ( event.target == butSubmit && senddetails )
            {
                try
                {
                    URL url = new URL( "http://www.ant.co.uk/~peter/cgi-bin/autohall.py?"
                                        + "name=" + URLEncoder.encode( tfSubmitName.getText() )
                                        + "&email=" + URLEncoder.encode( tfSubmitEmail.getText() )
                                        + "&m=" + tfSubmitMoves.getText() );

					senddetails = false;
                    Object o = url.getContent();
                }
                catch (MalformedURLException e)
                {
					showStatus( "FAILED to update hall of fame (MalformedURLException)" );
                }
                catch (IOException e)
                {
					showStatus( "FAILED to update hall of fame (IOException)" );
                }
                ((CardLayout)this.getLayout()).show( this, GAME );
            }
			else if ( event.target == butNext )
			{
				int now = sbReplay.getValue();

				now++;

				if ( setReplayPos( now, false ) )
					sbReplay.setValue( now );
			}
            return true;

			/*
			int value = Integer.parseInt( tfReplayPos.getText() ) - 1;
			switch ( event.id )
			{
			case event.SCROLL_LINE_UP: value++; break;
			case event.SCROLL_LINE_DOWN: value--; break;
			case event.SCROLL_PAGE_UP: value += 10; break;
			case event.SCROLL_PAGE_DOWN: value -= 10; break;
			}
			if ( value >= 0 && value < replay.length()-1 )
			{
				sbReplay.setValue( value );
				setReplayPos( value );
			}
			return true;*/

        case event.SCROLL_ABSOLUTE:
	    case event.SCROLL_LINE_UP:
		case event.SCROLL_LINE_DOWN:
		case event.SCROLL_PAGE_UP:
		case event.SCROLL_PAGE_DOWN:
            setReplayPos( ((Integer)event.arg).intValue(), true );
            return true;
        }
        return false;
    }

    public boolean setReplayPos( int pos, boolean fast )
    {
        if ( replay != null && pos >= 0 && pos < replay.length()-1 )
        {
            tfReplayPos.setText( "" + (pos+1) );
            thePanel.setReplayPos( pos, fast );
			return true;
        }
		return false;
    }
}
