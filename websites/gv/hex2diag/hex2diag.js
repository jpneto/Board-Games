// hex2diag: Written by Joao Neto based on code by Hans Bodlaender (c) 2001, 2002
// 
// Non-commercial use is permitted: `no fee - no guarantee' 
// For commercial use, ask Hans at hans@chessvariants.com and Joao at jpn@di.fc.ul.pt

function drawhex(pict,pictpath,row,col) {

  if (row == 0)
    outstring="<IMG SRC=\"" + pictpath+pict+".gif\" width=26 height=24>";
  else
    outstring="<IMG SRC=\"" + pictpath+pict+".gif\" title=\"cell " + 
               String.fromCharCode(64+col).toLowerCase() + row + "\" width=26 height=24>";
  document.write(outstring);
}

function drawhalfhex(pict,pictpath) {

  outstring="<IMG SRC=\"" + pictpath+pict+".gif\" width=13 height=24>";
  document.write(outstring);
}

function drawnewrow() {
   document.write("<BR>"); 
}

function hex2diag(boardstr) {
	hex2diagp(boardstr,"");
}

function hexgv2diag(boardstr) {  //jpn note: to use in the WGA website
	hex2diagp(boardstr,"hex2diag/");
}

function hex2diagp(boardstr,gifpath) {

	// parse boardstr
    strlength = boardstr.length;
    strpos = 0;
    curchar = boardstr.charAt(strpos);

	// an integer follows that gives the initial number of coluns
    initCols =0;
    while (strpos < strlength && ("0" <= curchar) && (curchar <= "9"))
    {
       initCols = 10*initCols + parseInt(curchar);
	 strpos++;
	 curchar = boardstr.charAt(strpos);
    }

    if (curchar == ",") 	// skip the comma
	curchar = boardstr.charAt(++strpos);

	// now an integer follows that gives the number of columns in the middle of the Hex
    middleCols =0;
    while (strpos < strlength && ("0" <= curchar) && (curchar <= "9"))
    {
       middleCols = 10*middleCols + parseInt(curchar);
	 strpos++;
	 curchar = boardstr.charAt(strpos);
    }

    if (curchar == ",") 	// skip the comma
	curchar = boardstr.charAt(++strpos);

	// now an integer follows that gives the number of columns in the last row
    endCols =0;
    while (strpos < strlength && ("0" <= curchar) && (curchar <= "9"))
    {
       endCols = 10*endCols + parseInt(curchar);
	 strpos++;
	 curchar = boardstr.charAt(strpos);
    }

    if (curchar == ",")	// skip the comma
	curchar = boardstr.charAt(++strpos);

    // ***************************************************************************
    // and now we get the part that describes the board description, hence:

    nbrows     = (middleCols - initCols + 1) + (middleCols - endCols);
    nbcolumns  = initCols;
    middleRow  = middleCols - initCols + 1;
    row        = 1; 
    column     = 1;
    increasing = true;     // are the columns still increasing?

    // ** make hex top border

      //  First found out how many spaces are needed
      // this depends on how much rows are until the largest middle one
      //  If there are N rows until the middle one (including this one)
      // then there should be floor((N+1)/2) full spaces 
      // plus half one if the difference is even 

      nempties = Math.floor((middleRow-row+1)/2);

      if ((middleRow-row)%2 == 0)
        drawhalfhex("empty",gifpath);

      for (i=0;i<nempties;i++)
        drawhex("empty",gifpath,0,0);

      for (i=0;i<nbcolumns;i++)
        drawhex("hexu",gifpath,0,0);

      drawnewrow();

      if ((middleRow-row)%2 == 0)
        drawhalfhex("empty",gifpath);

      for (i=0;i<nempties-1;i++)
        drawhex("empty",gifpath,0,0);

      drawhex("hexl",gifpath,0,0);

     // ***********************

    while (strpos < strlength) {

      curchar = boardstr.charAt(strpos);
      check = 0; // check tells: did we match this char?

	// options: what are we reading in the string

  	  // first case: a lower case letter from the alphabet
        if ( (("a" <= curchar) && (curchar <= "z")) ||
             (("0" <= curchar) && (curchar <= "9")) ) {

          drawhex( curchar,gifpath, 
                   (row<=middleRow?middleRow-row:0)+column,    // give row
                   (row<=middleRow?0:row-middleRow)+column );  // give col
 	  column++;
          strpos++; 
 	  check = 1;
        }
	
        // second case: upper case letter from alphabet
        if (("A" <= curchar) && (curchar <= "Z") ) {

          drawhex( "k" + curchar.toLowerCase(), gifpath, 
                   (row<=middleRow?middleRow-row:0)+column,    // give row
                   (row<=middleRow?0:row-middleRow)+column );  // give col

          column++;
          strpos++; 
          check = 1;
        }
	
        // third case: end of row marker: '/'
       if (curchar == "/") {     // sometimes we have already skipped them

              row++; 
              column = 1;
              strpos++; 
              check = 1;

              if (increasing) {

                drawhex("hexrt",gifpath,0,0);
                nbcolumns++;
                nempties = Math.floor((middleRow-row+1)/2);
                if (((nbcolumns-1) == middleCols) && (middleCols <= endCols)) 
                  break;        // the board is a hexagonal trapezoid!
  	          drawnewrow();

                if ((middleRow-row)%2 == 0)
                  drawhalfhex("empty",gifpath);

                for (i=0;i<nempties-1;i++)
                  drawhex("empty",gifpath,0,0);

                if (nbcolumns == middleCols)
                   increasing = false; 
                else
                   drawhex("hexl",gifpath,0,0);
 
              } else {

                if (nbcolumns == middleCols)
                  drawhalfhex("hexrbhalf",gifpath); // both left/right margins should be equal
                else
                  drawhex("hexrb",gifpath,0,0);
                nbcolumns--;
                nempties = Math.floor((row-middleRow+1)/2);
  	          drawnewrow();

                if ((middleRow-row)%2 == 0)
                  drawhalfhex("empty",gifpath);

                for (i=0;i<nempties;i++)
                  drawhex("empty",gifpath,0,0);
              }
       }

       // forth case: point: empty square
       if (  (curchar == ".")||(curchar == ";")||(curchar == ":")||(curchar == ",")) {

           switch (curchar) {
             case ".": picture = "hex";  break;
             case ";": picture = "ptg";  break;   // with green dot
             case ":": picture = "ptr";  break;   // with red dot
             case ",": picture = "ptb";  break;   // with blue dot
           }

           drawhex( picture,gifpath, 
                   (row<=middleRow?middleRow-row:0)+column,    // give row
                   (row<=middleRow?0:row-middleRow)+column );  // give col
	   column++;
	   check = 1;
	   strpos++;
       }

       // fifh case: more stacks
       if (curchar == "[") {

           picture = boardstr.charAt(++strpos) + boardstr.charAt(++strpos) + boardstr.charAt(++strpos);

           strpos++ // skip the ']'

           drawhex( picture,gifpath, 
                   (row<=middleRow?middleRow-row:0)+column,    // give row
                   (row<=middleRow?0:row-middleRow)+column );  // give col
	   column++;
	   check = 1;
	   strpos++;
       }

       // sixth case: special symbols
       if (curchar == "@") {

          strpos++; 
          curchar = boardstr.charAt(strpos);

          switch (curchar) {
             
             case "a" :  // ARROWS
               picture = "a_" + boardstr.charAt(++strpos);
               break;

             case "d" :  // DICES
               picture = "dice_" + boardstr.charAt(++strpos) + boardstr.charAt(++strpos);
               break;

          }  // end switch


          drawhex( picture,gifpath, 
                   (row<=middleRow?middleRow-row:0)+column,    // give row
                   (row<=middleRow?0:row-middleRow)+column );  // give col
	  column++;
	  check = 1;
	  strpos++;
       }

       // if character hasn't been matched, we just skip it this avoids infinite loops
       if (check == 0) strpos++;
    
   }

  if (middleCols == endCols)
     drawhalfhex("hexrbhalf",gifpath); // both left/right margins should be equal
  else
     drawhex("hexrb",gifpath,0,0);

   document.write("<br><br>");
}