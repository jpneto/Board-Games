// board2diag: Written by Joao Neto based on code by Hans Bodlaender (c) 2001, 2002
// 
// Non-commercial use is permitted: `no fee - no guarantee' 
// For commercial use, ask Hans at hans@chessvariants.com and Joao at jpn@di.fc.ul.pt

function drawsquare(pict,pictpath,row,col) {

  if (pict=="z")
    outstring="<IMG SRC=\"" + pictpath+pict+".gif\" width=22 height=22>";
  else
    outstring="<IMG SRC=\"" + pictpath+pict+".gif\" title=\"cell " + String.fromCharCode(64+col).toLowerCase() + row + "\" width=22 height=22>";
  document.write(outstring);
}

function drawnewrow() {
   document.write("<BR>"); 
}

function board2diag(boardstr) {
	board2diagp(boardstr,"");
}

function boardgv2diag(boardstr) {  //jpn note: to use in the WGA website
	board2diagp(boardstr,"board2diag/");
}

function board2diagp(boardstr,gifpath) {

	// parse boardstr
    strlength = boardstr.length;
    strpos = 0;
	// first, determine which sides are existent
	// this is done by having names u (upper), l (left), r(right), or d (down)
    curchar = boardstr.charAt(strpos);

    dborder=0;  lborder=0;  rborder=0;   uborder=0;
    strpos=0;

    while (("a" <= curchar) && (curchar <= "z"))
    {
	if (curchar == "d"){dborder=1;}
	if (curchar == "u"){uborder=1;}
	if (curchar == "l"){lborder=1;}
	if (curchar == "r"){rborder=1;}
	strpos ++;
	curchar = boardstr.charAt(strpos);
    }
    borderoption = parseInt(curchar);

	// skip a possible comma
    if (curchar == ",")
    {
	strpos ++;
	curchar = boardstr.charAt(strpos);
    }

	// now, an integer follows that gives the number of rows
    nbrows =0;
    while (strpos < strlength && ("0" <= curchar) && (curchar <= "9"))
    {
         nbrows = 10*nbrows + parseInt(curchar);
	 strpos++;
	 curchar = boardstr.charAt(strpos);
    }

	// skip the comma
    if (curchar == ",")
    {
	strpos ++;
	curchar = boardstr.charAt(strpos);
    }

	// now an integer follows that gives the number of columns
    nbcolumns =0;
    while (strpos < strlength && ("0" <= curchar) && (curchar <= "9"))
    {
         nbcolumns = 10*nbcolumns + parseInt(curchar);
	 strpos++;
	 curchar = boardstr.charAt(strpos);
    }

	// skip the comma
    if (curchar == ",")
    {
	strpos ++;
	curchar = boardstr.charAt(strpos);
    }

	// and now we get the part that describes the board description, hence:
	// make diagram

    row = 1; column = 1;
    while (strpos < strlength) {

	curchar = boardstr.charAt(strpos);
      check = 0; // check tells: did we match this char?

	// options: what are we reading in the string

  	  // first case: a lower case letter from the alphabet
        if (("a" <= curchar) && (curchar <= "z") ) {
          drawsquare(curchar,gifpath,nbrows-row+1,column);
 	    column++;
            strpos++; 
 	    check = 1;
        }
	
        // second case: upper case letter from alphabet
        if (("A" <= curchar) && (curchar <= "Z") ) {
          drawsquare("k" + curchar.toLowerCase(),gifpath,nbrows-row+1,column);
	  column ++;
          strpos++; 
          check = 1;
        }
	
        // third case: digit
        if (("0" <= curchar) && (curchar <= "9")) {
           nbskips = parseInt(curchar);
           strpos++; 
           check = 1;
           curchar = boardstr.charAt(strpos);

           while (strpos < strlength && ("0" <= curchar) && (curchar <= "9")) {
              nbskips = 10*nbskips + parseInt(curchar);
	        strpos++;
	        curchar = boardstr.charAt(strpos); //
           }

           for (i=1; i <= nbskips; i++) {
	      picture = "cell";

            if ((row==1)      && (uborder==1))           picture = picture + "u";
            if ((row==nbrows) && (dborder==1))           picture = picture + "d";
            if ((column==1)   && (lborder==1))	         picture = picture + "l";
            if ((column == nbcolumns) && (rborder == 1)) picture = picture + "r";

            drawsquare(picture,gifpath,nbrows-row+1,column);
            column++;
	      if (column > nbcolumns) {
		  column = 1;
		  row ++;
		  if (row<=nbrows) drawnewrow();  // if it's the last row, don't change line
	      }
          } //for(i)
       }

       // forth case: end of row marker: '/'
       if (curchar == "/") {     // sometimes we have already skipped them
	   if (column > 1) {
              drawnewrow();
              row = row+1; 
              column = 1;
              strpos++; 
              check = 1;
	   }
       }

       // fifth case: point: empty square
       if (  (curchar == ".")||(curchar == ",")||(curchar == ";")
           ||(curchar == ":")||(curchar == "*")) {

           hasU = hasD = hasL = hasR = hasStar = false;

           switch (curchar) {
             case ".": picture = "cell";   
                       break;
             case ",": picture = "pt_";   // handicap point
                       break;
             case ";": picture = "ptg";   // with green dot
                       break;
             case ":": picture = "ptr";   // with red dot
                       break;
             case "*": picture = "star";  // with diagonal lines
                       hasStar = true;
                       break;
           }

           if ((row==1)      && (uborder==1))           hasU = true;
           if ((row==nbrows) && (dborder==1))           hasD = true;
           if ((column==1)   && (lborder==1))	        hasL = true;
           if ((column == nbcolumns) && (rborder == 1)) hasR = true;

           if (row!=1 && boardstr.charAt(strpos-nbcolumns-1) == "z")      hasU = true;
           if (row!=nbrows && boardstr.charAt(strpos+nbcolumns+1) == "z") hasD = true;
           if (column!=1 && boardstr.charAt(strpos-1) == "z")             hasL = true;
           if (column!=nbcolumns && boardstr.charAt(strpos+1) == "z")     hasR = true;

           if (hasU) picture = picture + "u";
           if (hasD) picture = picture + "d";
           if (hasL) picture = picture + "l";
           if (hasR) picture = picture + "r";

           // now, if it is a central cell, with diagonals...
           if (!hasU && !hasD && !hasL && !hasR && hasStar) {

              // and there are some non star cells...
              if (boardstr.charAt(strpos-nbcolumns-2) != "*")
                 picture = picture + '_ul';
              if (boardstr.charAt(strpos-nbcolumns  ) != "*")
                 picture = picture + '_ur';
              if (boardstr.charAt(strpos+nbcolumns  ) != "*")
                 picture = picture + '_dl';
              if (boardstr.charAt(strpos+nbcolumns+2) != "*")
                 picture = picture + '_dr';
           }

           drawsquare(picture,gifpath,nbrows-row+1,column);
	   column++;
	   check = 1;
	   strpos++;
       }

       // sixth case: specific piece games
       if (curchar == "@") {

          strpos++; 
          curchar = boardstr.charAt(strpos);

          switch (curchar) {
             
             case "c" :  // CONNECTING PIECES
               color   = boardstr.charAt(++strpos);
               curchar = boardstr.charAt(++strpos);
               number  = 8*parseInt(curchar);

               curchar = boardstr.charAt(++strpos);
               number += 4*parseInt(curchar);

               curchar = boardstr.charAt(++strpos);
               number += 2*parseInt(curchar);

               curchar = boardstr.charAt(++strpos);
               number += parseInt(curchar);

               picture = "connect_" + color + number;   
               break;
             
             case "r" :  // ROTATING PIECES
               color     = boardstr.charAt(++strpos);
               direction = boardstr.charAt(++strpos);
               picture   = "rotate_" + color + direction;   
               break;

             case "x" :  // CHESS PIECES
               color   = boardstr.charAt(++strpos);
               piece   = boardstr.charAt(++strpos);
               picture = "chess_" + color + piece;   
               break;
          }

          drawsquare(picture,gifpath,nbrows-row+1,column);
 	  column++;
          strpos++; 
 	  check = 1;
        }

	// if character hasn't been matched, we just skip it this avoids infinite loops
       if (check == 0) strpos++;
    
   }
   document.write("<br>");
}