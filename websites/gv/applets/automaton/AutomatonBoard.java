/* AutomatonBoard.class */

/* Main guts of Automaton Wars in Java
 * (K) All Rites Reversed -- Copy What You Like
 *
 * Authors:
 *      Peter Hartley   <peter@ant.co.uk>
 *
 * History:
 *      22/06/97 pdh Started
 *      23/06/97 pdh Add buttons, opponent (thick)
 *      24/06/97 pdh Add notSoThick opponent, improve graphics
 *      06/09/97 pdh Add replaying of games
 *      06/09/97 pdh Separate from AutoWars.java
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



/* Automaton Wars in Java consists of two parts: the board itself, which is
 * implemented as a custom component, and the applet, which contains a board
 * plus all the controls.
 */

public class AutomatonBoard extends Canvas
{
    // Board shape

    int XSIZE = 6, YSIZE = 6;

    int[] heights;

    int panelx, panely;
    boolean traditional = true;

    // Drawing the pieces

    static final int NPIECES = 6;
    Image[] pieces;
    Image bg,b1,b2;
    Image buffer;
    Image[] cmb;

    boolean showGhost;
    int ghostptr = -1;

    // Game state

    int[] state;
    int player;
    boolean won;
    int opponent = 0;   /* 0=>person 1=>thick 2=>notsothick */
    int moves;
    int firstPlayer = 0;

    int opponentNextGame = 0;

    StringBuffer movesRecord;

    // Replaying games
    String replay;
    int replaypos;

    Automaton parent;


        /*=================*
         *   Constructor   *
         *=================*/


    public AutomatonBoard( Automaton aw, int x, int y, String board, String rp )
    {
        super();
        parent = aw;

        XSIZE = x;
        YSIZE = y;

        panelx = (XSIZE+YSIZE)*28 + 2;
        panely = (XSIZE+YSIZE)*14 + 220-168;

        traditional = ( x==6 && y==6 && board==null );

        replay = rp;

        heights = new int[XSIZE*YSIZE];

        int i;

        if ( board != null )
        {
            for ( i=0; i<XSIZE*YSIZE; i++ )
                heights[i] = (board.charAt(i) == ' ') ? -1 : 1;
        }
        else
        {
            for ( i=0; i<XSIZE*YSIZE; i++ )
                heights[i] = 1;
        }

        sortOutHeights();
        
        state = new int[XSIZE*YSIZE];
        newGame();
        if ( replay != null )
            setReplayPos(0,false);
    }

    protected final void sortOutHeights()
    {
        int ptr = 0;
        for ( int x=0; x<XSIZE; x++ )
        {
            for ( int y=0; y<YSIZE; y++ )
            {
                if ( heights[ptr] >= 0 )
                {
                    heights[ptr] = (isThere(x-1,y) ? 0 : 1 )
                                 + (isThere(x+1,y) ? 0 : 1 )
                                 + (isThere(x,y-1) ? 0 : 1 )
                                 + (isThere(x,y+1) ? 0 : 1 );
                }
                ptr++;
            }
        }
    }

    protected final boolean isThere( int x, int y )
    {
        if ( x<0 || x>=XSIZE || y<0 || y >= YSIZE )
            return false;
        return heights[x*YSIZE+y] >= 0;
    }

    public boolean isTraditional()
    {
        return traditional;
    }


        /*====================================*
         *   Overrides and message handlers   *
         *====================================*/


    public void addNotify()
    {
        super.addNotify();
        if ( buffer == null )
            buffer = this.createImage(panelx, panely);
    }

    public Dimension preferredSize()
    {
        return new Dimension(panelx, panely);
    }
    
    public Dimension minimumSize()
    {
        return new Dimension(panelx, panely);
    }

    public void paint(Graphics g)
    {
        g.drawImage(buffer,0,0,this);
    }

    public void update( Graphics g )
    {
        paint(g);
    }

	public boolean mouseDown(Event evt, int x, int y)
	{
        if ( won || replay != null )
            return true;

        int ptr = find( x, y );

        if ( ptr != -1 && ptr == ghostptr )
        {
            if ( state[ptr] == 0
                 || ( state[ptr] & 1 ) == player )
            {
                ghostptr = -1;
                makeMove( ptr );
                player = player ^ 1;

                shouldComputerMove();
            }
        }

		return true;
	}

    public boolean mouseMove( Event evt, int x, int y )
    {
        if ( won || (replay != null) )
            return true;

        int ptr = find( x, y );

        if ( ptr != -1 
             && state[ptr] > 0
             && (state[ptr] & 1) != player )
            ptr = -1;

        if ( ptr != ghostptr )
        {
            ghostptr = ptr;
            forceRedraw();
        }
        return true;
    }


        /*==================*
         *   Board redraw   *
         *==================*/


    private final void drawCell( Graphics g, int x, int y )
    {
        int off = x*YSIZE+y;
        int xpos = YSIZE*28 + 149 - 168 + x*28 - y*28;
        int ypos = 31 + x*14 + y*14 - 8*heights[off];
        int n = state[off];
        int which = n & 1;
        n = n >> 1;

        if ( heights[off] < 0 )
            return;

        if ( !traditional )
            g.drawImage( cmb[heights[off]], xpos-8, ypos+3, this );

        if ( n + heights[off] >= 3 )
            which += 2;

        for ( int p=0; p<n; p++ )
        {
            g.drawImage( pieces[which], xpos, ypos, this );
            ypos -= 8;
        }

        if ( ghostptr == off )
        {
            g.drawImage( pieces[4+player], xpos, ypos, this );
        }
    }

    public void drawBoard( Graphics g )
	{
        int x,y;

        g.setPaintMode();

        if ( traditional )
            g.drawImage(bg,0,0,this);
        else
        {
            g.setColor( Color.white );
            g.fillRect( 0,0, panelx, panely );
        }

        for ( x=0; x<XSIZE-1; x++ )
            for ( y=0; y<YSIZE-1; y++ )
                drawCell( g, x, y );

        if ( traditional )
            g.drawImage(b1,0,69+20,this);

        for ( x=0; x<XSIZE-1; x++ )
        {
            drawCell( g, x, YSIZE-1 );
        }

        for ( y=0; y<YSIZE-1; y++ )
        {
            drawCell( g, XSIZE-1, y );
        }

        if ( traditional )
            g.drawImage(b2,139,139+20,this);

        drawCell( g, XSIZE-1, YSIZE-1 );
	}

    public final void forceRedraw()
    {
        drawBoard( buffer.getGraphics() );
        buffer.flush();
        paint( this.getGraphics() );
    }


    // Figuring out which board position the user's clicked on

    final int find( int x, int y )
    {
        int bx,by,ptr;

        x = ( x - YSIZE*28 ) / 2;

        y -= 35;

        for ( bx=XSIZE-1; bx>=0; bx-- )
        {
            for ( by=YSIZE-1; by>=0; by-- )
            {
                int sx = (bx-by)*14;
                int sy = (bx+by)*14;
                int ext;
                int res;

                ptr = bx*YSIZE + by;

                if ( heights[ptr] >= 0 )
                {

                    if ( state[ptr] > 0 )
                    {
                        sy = sy + 3 - 7*( state[ptr] >> 1 );
                        ext = 20;
                    }
                    else
                    {
                        ext = 28;
                    }

                    sy -= 7 * heights[ptr];

                    sx = x - sx;
                    sy = y - sy;

                    res = sx + sy;
                    if ( res >= 0 && res < ext )
                    {
                        res = sy - sx;
                        if ( res >= 0 && res < ext )
                        {
                            return ptr;
                        }
                    }
                }
                ptr++;
            }
        }
        return -1;
    }


        /*==========================*
         *   Board state machines   *
         *==========================*/


    /* State machine for the "real" board */

    /* Make a move on the real board, including all exploding and screen updating
     */
    final void makeMove( int ptr )
    {
        state[ptr] = ( state[ptr] + 2 ) | player;

        if ( traditional && replay == null )
        {
            int code;

            if ( ptr < 26 )
                code = 'A' + ptr;
            else
                code = '0' + (ptr-26);

            movesRecord.append( (char)code );
            parent.showStatus( movesRecord.toString() );
        }

        if ( buffer != null)
            forceRedraw();

        while ( explode() )
        {
            if ( buffer != null )
            {
                try
				{
                    Thread.sleep(250);
				}
                catch (InterruptedException e)
				{
				}
                forceRedraw();
            }
        }

        moves++;
    }

    /* One "round" of explosions. Returns true if any explosions occurred,
     * in which case it should be called again.
     */
    final boolean explode()
    {
        int[] newboard = new int[XSIZE*YSIZE];
        int x,y,ptr;
        boolean any = false;
        boolean[] contend = { false, false };

        if ( moves > 1 )
        {
            for ( ptr=0; ptr<XSIZE*YSIZE; ptr++ )
            {
                x = state[ptr];
                if ( x > 0 )
                    contend[ x & 1 ] = true;
                newboard[ptr] = x;
            }

            if ( !contend[0] || !contend[1] )
            {
                if ( replay == null && opponent == 2 && !contend[1] )
                    parent.winner( movesRecord );
                won = true;
                return false;
            }
        }

        ptr=0;
        for ( x=0; x<XSIZE; x++ )
        {
            for ( y=0; y<YSIZE; y++ )
            {
                if ( ((newboard[ptr]>>1) + heights[ptr]) >= 4 )
                {
                    state[ptr] -= 2*( 4-heights[ptr] );
                    if ( state[ptr] == 1 )
                        state[ptr] = 0;
                    if ( isThere(x,y-1) )
                        state[ptr-1] = capture( state[ptr-1] );
                    if ( isThere(x,y+1) )
                        state[ptr+1] = capture( state[ptr+1] );
                    if ( isThere(x-1,y) )
                        state[ptr-YSIZE] = capture( state[ptr-YSIZE] );
                    if ( isThere(x+1,y) )
                        state[ptr+YSIZE] = capture( state[ptr+YSIZE] );
                    any = true;
                }
                ptr++;
            }
        }

        return any;
    }

    /* Capturing a square
     */
    final int capture( int state )
    {
        return ( ( state + 2 ) & 0xFE ) | player;
    }


    /* A second state machine for internal use by computer players */

    /* All routines resemble the "real" ones above, but can be applied by
     * either player to any board.
     */
    protected final int[] quickMove( int[] stateNow, int[] stateAfter,
                                     int[] stateTemp,
                                     int ptr, int player )
    {
        boolean more = true;
        for ( int i=0; i<XSIZE*YSIZE; i++ )
            stateAfter[i] = stateNow[i];

        stateAfter[ptr] = (stateAfter[ptr] + 2) | player;

        while ( more )
            more = quickExplode( stateAfter, stateTemp, player );
        
        return stateAfter;
    }

    protected final boolean quickExplode( int[] stateFrom, int[] stateTemp, int player )
    {
        int x,y,ptr;
        boolean any = false;
        boolean[] contend = { false, false };

        if ( moves > 1 )
        {
            for ( ptr=0; ptr<XSIZE*YSIZE; ptr++ )
            {
                x = stateFrom[ptr];
                if ( x > 0 )
                    contend[ x & 1 ] = true;
                stateTemp[ptr] = x;
            }

            if ( !contend[0] || !contend[1] )
                return false;
        }

        ptr=0;
        for ( x=0; x<XSIZE; x++ )
        {
            for ( y=0; y<YSIZE; y++ )
            {
                if ( ((stateTemp[ptr]>>1) + heights[ptr]) >= 4 )
                {
                    stateFrom[ptr] -= 2*( 4-heights[ptr] );
                    if ( stateFrom[ptr] == 1 )
                        stateFrom[ptr] = 0;
                    if ( isThere(x,y-1) )
                        stateFrom[ptr-1] = quickCapture( stateFrom[ptr-1], player );
                    if ( isThere(x,y+1) )
                        stateFrom[ptr+1] = quickCapture( stateFrom[ptr+1], player );
                    if ( isThere(x-1,y) )
                        stateFrom[ptr-YSIZE] = quickCapture( stateFrom[ptr-YSIZE], player );
                    if ( isThere(x+1,y) )
                        stateFrom[ptr+YSIZE] = quickCapture( stateFrom[ptr+YSIZE], player );
                    any = true;
                }
                ptr++;
            }
        }

        return any;
    }

    protected final int quickCapture( int state, int player )
    {
        return ( ( state + 2 ) & 0xFE ) | player;
    }


        /*===================================*
         *   Routines used by applet class   *
         *===================================*/


    synchronized public void setReplayPos( int pos, boolean fast )
    {
        int ptr = replayMove( pos );

        if ( pos == moves-1 )
            return;
        
        firstPlayer = (replay.charAt(0) == 'b') ? 0 : 1;

        if ( pos == 0 || pos < moves )
        {
            for ( int i=0; i<XSIZE*YSIZE; i++ )
                state[i] = 0;
            moves = 0;
        }

        player = (moves & 1 ) ^ firstPlayer;

        if ( pos == moves && !fast )
            opponentMove( ptr );
        else
        {
            for ( ; moves<=pos; moves++ )
            {
                ptr = replayMove( moves );

                player = (moves & 1 ) ^ firstPlayer;

                quickMove( state, state, new int[XSIZE*YSIZE], ptr, player );
            }
            forceRedraw();
        }

        moves = pos+1;
    }

    private final int replayMove( int which )
    {
        int ptr = replay.charAt( which+1 );
        if ( ptr < 'A' )
            ptr = (ptr-'0')+26;
        else
            ptr -= 'A';
        return ptr;
    }

    public void newGame()
    {
        won = false;
        for ( int i=0; i<XSIZE*YSIZE; i++ )
            state[i] = 0;
        moves = 0;
        movesRecord = new StringBuffer(36*4);
        movesRecord.append( firstPlayer == 0 ? 'b' : 'r' );
        opponent = opponentNextGame;
        player = firstPlayer;
        shouldComputerMove();
    }

    public void setOpponent( int op )
    {
        opponentNextGame = op;
        if ( moves == 0 && replay == null )
        {
            opponent = op;
            shouldComputerMove();
        }
    }

    public void setFirstPlayer( int pl )
    {
        firstPlayer = pl;

        if ( moves == 0 )
        {
            player = pl;
            movesRecord.setLength(0);
            movesRecord.append( pl == 0 ? 'b' : 'r' );
            shouldComputerMove();
        }
    }


        /*===============================*
         *   Computer player (generic)   *
         *===============================*/


    public void shouldComputerMove()
    {
        if ( player == 1 && !won && replay == null )
        {
            if ( opponent == 1 )
                opponentMove( thick(state,1,false) );
            else if ( opponent == 2 )
                opponentMove( notSoThick(state,1) );
        }
    }

    /* Perform the opponent's move, including exciting flashing square
     */
    public void opponentMove( int ptr )
    {
        if ( buffer != null )
        {
            for ( int i=0; i<4; i++ )
            {
                ghostptr = ((i&1) == 0 ) ? ptr : -1;
                forceRedraw();
                try
				{
                    Thread.sleep(150);
				}
                catch ( InterruptedException e )
                {
				}
            }
        }
        makeMove( ptr );
        player = player ^ 1;
    }


        /*=============================*
         *   Computer player (thick)   *
         *=============================*/


    /* Calculate the best move, given the initial state. Returns either the
     * move itself, or the score that would result, according to the giveScore
     * parameter.
     */
    protected final int thick( int[] state, int player, boolean giveScore )
    {
        int[] stateAfter = new int[XSIZE*YSIZE];
        int[] stateTemp = new int[XSIZE*YSIZE];
        int x,y,ptr;
        int bestptr=0, bestscore = -1001;

        ptr = 0;
        for ( x=0; x<XSIZE; x++ )
        {
            for ( y=0; y<YSIZE; y++ )
            {
                if ( heights[ptr] >= 0
                     && ( state[ptr] == 0
                          || ( state[ptr] & 1 ) == player ) )
                {
                    int thisscore = score( quickMove( state, stateAfter,
                                                      stateTemp, ptr,
                                                      player ),
                                           player );
                
                    if ( thisscore > bestscore )
                    {
                        bestscore = thisscore;
                        bestptr = ptr;
                    }
                }
                ptr++;
            }
        }

        return giveScore ? bestscore : bestptr;
    }

    /* Evaluate this board position for this player
     */
    protected final int score( int state[], int player )
    {
        int x,y,ptr;
        int result = 0;

        ptr = 0;
        for ( x=0; x<XSIZE; x++ )
        {
            for ( y=0; y<YSIZE; y++ )
            {
                if ( ( state[ptr] & 1 ) == player )
                {
                    result += cellScore( state, player, x, y, ptr );
                }
                ptr++;
            }
        }

        if ( result == 0 )
            result = -1000; /* No pieces on the board! Particularly bad! */

        return result;
    }

    /* Evaluate how useful this cell is to us... this is the principal "brain"
     * of the thick player.
     */
    protected final int cellScore( int state[], int player, int x, int y, int ptr )
    {
        int me = ( state[ptr] >> 1 );
        int result = 0;

        // 100 points for each piece on the board
        result += me * 100;

        me += heights[ptr];

        // +100 points for a flasher
        if ( me == 3 )
            result += 100;

        // +20 points if not overlooked
        if (    !taller( state, player, x-1, y, me )
             && !taller( state, player, x+1, y, me )
             && !taller( state, player, x, y-1, me )
             && !taller( state, player, x, y+1, me ) )
            result += 20;

        // 5 points for edge, 10 for corner
        if ( x==0 || x==XSIZE-1 )
            result += 5;
        if ( y==0 || y==YSIZE-1 )
            result += 5;

        // 1 point for diagonally opposite an enemy flasher
        if ( taller( state, player, x-1, y-1, 3 ) )
            result++;
        if ( taller( state, player, x+1, y-1, 3 ) )
            result++;
        if ( taller( state, player, x-1, y+1, 3 ) )
            result++;
        if ( taller( state, player, x+1, y+1, 3 ) )
            result++;

        // 1 point for next but one to an enemy flasher
        if ( taller( state, player, x-2, y, 3 ) )
            result++;
        if ( taller( state, player, x+2, y, 3 ) )
            result++;
        if ( taller( state, player, x, y-2, 3 ) )
            result++;
        if ( taller( state, player, x, y+2, 3 ) )
            result++;

        return result;
    }

    /* Is the named square (a) owned by the enemy and (b) taller than 'me'?
     */
    protected final boolean taller( int state[], int player, int x, int y, int me )
    {
        int ptr;

        if ( x<0 || y<0 || x>=XSIZE || y>=YSIZE )
            return false;
        ptr = x*YSIZE + y;
        if ( state[ptr] > 0
             && ( state[ptr] & 1 ) == player )
            return false;
        return ( ( state[ptr] >> 1 ) + heights[ptr] ) >= me;
    }


        /*====================================*
         *   Computer player (not so thick)   *
         *====================================*/


	protected final boolean anyPieces( int[] state, int player )
	{
		for ( int i=0; i < XSIZE*YSIZE; i++ )
			if ( state[i] > 0 && ( (state[i] & 1) == player) )
				return true;
		return false;
	}

    protected final int notSoThick( int[] state, int player )
    {
        int[] stateAfter = new int[XSIZE*YSIZE];
        int[] stateTemp = new int[XSIZE*YSIZE];
        int x,y,ptr;
        int bestptr=0, bestscore=-1001;

		if ( moves < 2 )
			return thick( state, player, false );

        ptr = 0;
        for ( x=0; x<XSIZE; x++ )
        {
            for ( y=0; y<YSIZE; y++ )
            {
                if ( heights[ptr] >= 0
                     && ( state[ptr] == 0
                          || ( state[ptr] & 1 ) == player ) )
                {
					int thisscore;
                    quickMove( state, stateAfter, stateTemp, ptr, player );
					if ( anyPieces( stateAfter, player^1 ) )
					{
						int otherptr = thick( stateAfter, player^1, false );
						quickMove( stateAfter, stateAfter, stateTemp, otherptr, player^1 );
						thisscore = score( stateAfter, player );
					}
					else
					{
						// We win! prune tree immediately
						return ptr;
					}

                    if ( thisscore > bestscore )
                    {
                        bestscore = thisscore;
                        bestptr = ptr;
                    }
                }
                ptr++;
            }
        }
        return bestptr;
    }
}
