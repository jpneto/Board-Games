<?xml version="1.0"?>

<xsl:stylesheet 
     version="1.0" 
     xmlns:xsl = "http://www.w3.org/1999/XSL/Transform">


  <!-- ****** define global vars for game ****** -->

  <!-- section markup ='soldier' if Game of Soldiers, 
                      ='king'    if Game of King and Soldiers
                      ='stack'   if Game of Towers -->

  <!-- board markup   ='sq' if square, 
                      ='hex' if hex, 
                      ='sqstack' if square stacking -->

 <xsl:variable name="backgrd">
   <xsl:choose>
    <xsl:when test="/game/components/section='soldier'">icons/back-board.jpg</xsl:when>
    <xsl:when test="/game/components/section='king'">icons/back-kings.jpg</xsl:when>
    <xsl:when test="/game/components/section='stack'">tower2diag/back-board.jpg</xsl:when>
    <xsl:otherwise></xsl:otherwise>
   </xsl:choose>
 </xsl:variable>


<xsl:template match="/">
  <xsl:apply-templates />
</xsl:template>


<xsl:template match="game">
<html>
 <head>
  <title>World of Abstract Games - <xsl:value-of select="title" /></title>
  <link rel="stylesheet" type="text/css" href="wagstyle.css"/>

  <!-- Importing graphical diagram tools --> 

  <script LANGUAGE="JavaScript" src="board2diag/board2diag.js"></script>
  <script LANGUAGE="JavaScript" src="hex2diag/hex2diag.js"></script>
  <script LANGUAGE="JavaScript" src="tower2diag/tower2diag.js"></script>

 </head>

 <body>

   <!-- ****** present initial game information ****** -->
   <p id="title"><xsl:value-of select="title" /></p>
   <p id="subtitle"><xsl:value-of select="subtitle" /></p>
   <p id="author">
     <xsl:apply-templates select="meta/author" />
     <xsl:apply-templates select="meta/publisher" />
     &#160;&#169;<xsl:value-of select="meta/year" />
   </p>

   <!-- ****** present history comments ****** -->
   <xsl:apply-templates select="notes/history" />

   <!-- ****** present initial sentence ****** -->
   <xsl:if test="count(components/diagram) = 1">
     <xsl:choose>

      <xsl:when test="components/board='sq'">
        <p>
          <xsl:value-of select="title" /> is played on the following 
          <xsl:value-of select="components/board/@row" />x<xsl:value-of select="components/board/@col"/> square board:
        </p>
      </xsl:when>

      <xsl:when test="components/board='hex'">
        <p>
          <xsl:value-of select="title" /> is played on the following 
          <xsl:value-of select="components/board/@top" />x<xsl:value-of select="components/board/@middle" />x<xsl:value-of select="components/board/@bottom" /> hexagonal board:
        </p>
      </xsl:when>

      <xsl:when test="components/board='sqstack'">
        <p>
          <xsl:value-of select="title" /> is played on the following 
          <xsl:value-of select="components/board/@row" />x<xsl:value-of select="components/board/@col"/> square board:
        </p>
      </xsl:when>

     </xsl:choose>
   </xsl:if>

   <xsl:if test="count(components/diagram) > 1">
    <p><xsl:value-of select="title" /> is played on one of the following boards:</p>
   </xsl:if>

   <!-- ****** present setup diagrams or image ****** -->
   <xsl:apply-templates select="components/diagram" />
   <xsl:apply-templates select="components/image" />

   <!-- ****** present game components ****** -->
   <p id="components">
     <u>Piece requirements</u>:<xsl:apply-templates select="components/pieces" />.
   </p>

   <!-- ****** present game rules ****** -->
   <table class="rules"> 
     <tr><td class="rules" width="100%">
       <xsl:apply-templates select="components/features" />
       <xsl:apply-templates select="components/zillions" />
       <xsl:apply-templates select="description" /> 
     </td></tr> 
   </table>
   <br/>

   <!-- ****** present rule notes ****** -->
   <xsl:if test="count(notes/rulenote) = 0">
     <br/>
   </xsl:if>

   <xsl:if test="count(notes/rulenote) > 0">
     <xsl:apply-templates select="notes/rulenote" />
   </xsl:if>
   
   <!-- ****** present examples ****** -->
   <xsl:apply-templates select="description/example" />  

   <!-- ****** present remaining notes ****** -->
   <xsl:apply-templates select="notes/authornote" />
   <xsl:apply-templates select="notes/note" />

   <!-- ****** separator ****** -->
   <div class="hr"><hr /></div>

   <!-- ****** present variants ****** -->
   <xsl:apply-templates select="variant" />

   <!-- ****** present related games ****** -->
   
   <xsl:if test="count(related) > 0">
    <p>Also check: <xsl:apply-templates select="related" />.</p>
   </xsl:if>

   <!-- ****** final stuff ****** -->
   <p id="back2WAG">     
      <xsl:apply-templates select="meta/writer" /><br/>
      return to <a href="index.htm">WAG</a>
   </p>

 </body>
</html>

</xsl:template>




<!-- ************************************** -->
<!-- ****** include game description ****** -->

<xsl:template match="description">
   <xsl:apply-templates select="setup" />
   <xsl:apply-templates select="rules" />
   <xsl:apply-templates select="goal"  />
</xsl:template>



<!-- ****** include game components ****** -->

<xsl:template match="components/pieces">
  <xsl:text> </xsl:text>
  <xsl:value-of select="@number" />     <xsl:text> </xsl:text>
  <xsl:value-of select="@color" />   <xsl:text> </xsl:text>
  <xsl:value-of select="@id" />
  <xsl:if test="position() != last()">,</xsl:if>
</xsl:template>



<!-- ****** include game notes ****** -->

<xsl:template match="rulenote">
  <p class="rulenote">Rule note: <xsl:apply-templates /></p>
</xsl:template>

<xsl:template match="authornote">
  <p class="authornote">Author note: <i><xsl:apply-templates /></i></p>
</xsl:template>

<xsl:template match="note">
   <p class="note"><xsl:apply-templates /></p>
</xsl:template>

<xsl:template match="history">
   <p id="history"><xsl:apply-templates /></p>
</xsl:template>



<!-- ****** include zillions reference ****** -->

<xsl:template match="zillions">
  <xsl:text> </xsl:text>
  <a>
    <xsl:attribute name="href"> 
       <xsl:value-of select="." />
    </xsl:attribute>
    <img border="0" src="icons/has_zrf.gif" alt="Check ZRF" width="16" height="16"/>
  </a>
</xsl:template>



<!-- ****** include game features ****** -->

<xsl:template match="features">

   <xsl:if test="@grid = 'sq'">
    <img border="0" src="icons/sqGrid.gif" alt="Square Grid" width="16" height="16"/>   
   </xsl:if>    

   <xsl:if test="@grid = 'hex'">
    <img border="0" src="icons/hexGrid.gif" alt="Hexagonal Grid" width="16" height="16"/>     
   </xsl:if> 

   <xsl:if test="@grid = 'oned'">
    <img border="0" src="icons/oneGrid.gif" alt="1-D Grid" width="16" height="16"/>     
   </xsl:if> 
   
   <xsl:if test="@candrop = 'true'">
     <img border="0" src="icons/drop.gif" alt="Pieces may be dropped" width="16" height="16"/>
   </xsl:if>
   <xsl:if test="@canmove = 'true'">
       <img border="0" src="icons/move.gif" alt="Pieces may move" width="16" height="16"/>
   </xsl:if>   
   <xsl:if test="@canjump = 'true'">
     <img border="0" src="icons/jump.gif" alt="Pieces may jump" width="16" height="16"/>  
   </xsl:if> 
   <xsl:if test="@cancapture = 'true'">
     <img border="0" src="icons/capture.gif" alt="Pieces may capture" width="16" height="16"/>
   </xsl:if> 
   <xsl:if test="@canremove = 'true'">
     <img border="0" src="icons/cellDestr.gif" alt="Cells may be removed" width="16" height="16"/>   
   </xsl:if> 
   <xsl:if test="@muststalemate = 'true'">
     <img border="0" src="icons/stalemate.gif" alt="Stalemate oriented" width="16" height="16"/>  
   </xsl:if> 
   <xsl:if test="@mustpattern = 'true'">
    <img border="0" src="icons/pattern.gif" alt="Pattern oriented" width="16" height="16"/>   
   </xsl:if> 
   <xsl:if test="@mustcell = 'true'">
      <img border="0" src="icons/cellGoal.gif" alt="Goal oriented" width="16" height="16"/>  
   </xsl:if> 
   <xsl:if test="@mustcapture = 'true'">
     <img border="0" src="icons/captGoal.gif" alt="Capture oriented" width="16" height="16"/>  
   </xsl:if> 
   <xsl:if test="@mustconnect = 'true'">
     <img border="0" src="icons/connect.gif" alt="Connection oriented" width="16" height="16"/>  
   </xsl:if> 
   <xsl:if test="@mustterritory = 'true'">
     <img border="0" src="icons/territory.gif" alt="Area oriented" width="16" height="16"/>  
   </xsl:if>                            
   
</xsl:template>



<!-- ****** author and publisher info ****** -->

<xsl:template match="author">
   <xsl:value-of select="name" />
   <xsl:apply-templates select="mail"  />
   <xsl:apply-templates select="url"  />,  
</xsl:template>

<xsl:template match="publisher">
   Publisher: <xsl:value-of select="name" />
   <xsl:apply-templates select="mail"  />
   <xsl:apply-templates select="url"  />, 
</xsl:template>

<xsl:template match="writer">
   Written by <xsl:value-of select="name" />
   <xsl:apply-templates select="mail"  />
   <xsl:apply-templates select="url"  />, <xsl:apply-templates select="year"/> 
</xsl:template>

<xsl:template match="mail">
  <a>
    <xsl:attribute name="href"> 
       <xsl:value-of select="concat('mailto:',.)" />
    </xsl:attribute>
    <xsl:text> </xsl:text>
    <img border="0" src="icons/email.gif" alt="Send email" width="14" height="11"/>
  </a>
</xsl:template>

<xsl:template match="url">
  <xsl:text> </xsl:text>
  <a target="_blank">
    <xsl:attribute name="href"> 
       <xsl:value-of select="." />
    </xsl:attribute>
    <img border="0" src="icons/net.gif" alt="Check URL" width="15" height="15"/>
  </a>
</xsl:template>



<!-- ****** game rules ****** -->

<xsl:template match="rulesection">
  <xsl:variable name="ruleClass">
    <xsl:choose>
      <xsl:when test="count(ancestor::rulesection)=0">rule</xsl:when>
      <xsl:when test="count(ancestor::rulesection)=1">srule</xsl:when>
      <xsl:when test="count(ancestor::rulesection)=2">ssrule</xsl:when>
      <xsl:otherwise></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
   <ul>
      <xsl:attribute name="class">   
       <xsl:value-of select="$ruleClass" />
     </xsl:attribute>
     <xsl:apply-templates />
   </ul>
</xsl:template>

<xsl:template match="rule">
   <li>     
     <xsl:apply-templates />
   </li>
</xsl:template>

<xsl:template match="rtitle">
   <b><xsl:value-of select="." />: </b>
</xsl:template>



<!-- ****** special rule sections ****** -->

<xsl:template match="goal">
  <p><b>GOAL: </b><xsl:apply-templates /></p>
</xsl:template>

<xsl:template match="setup">
  <p><b>SETUP: </b><xsl:apply-templates /></p>
</xsl:template>



<!-- ****** present a board diagram ****** -->

<xsl:template match="diagram">

  <xsl:variable name="width">
    <xsl:choose>
      <xsl:when test="@type='sq'"><xsl:value-of select="@col*22+16" /></xsl:when>
      <xsl:when test="@type='hex'"><xsl:value-of select="@middle*26+20" /></xsl:when>
      <xsl:when test="@type='sqstack'"><xsl:value-of select="@col*44+10" /></xsl:when>
    </xsl:choose>
  </xsl:variable>
  
  <center>
    <table border="1">
      <xsl:attribute name="width">
        <xsl:value-of select="$width" />
      </xsl:attribute>

      <tr><td width="100%">
 
       <xsl:if test="@type != 'sqstack'">
         <xsl:attribute name="class">
           <xsl:value-of select="'wide'" />
         </xsl:attribute>

         <xsl:attribute name="background">
           <xsl:value-of select="$backgrd" />
         </xsl:attribute>
       </xsl:if>

       <script LANGUAGE="JavaScript">      <!-- ****** insert the JavaScript setup board ****** -->
        <xsl:choose>
         <xsl:when test="@type='sq'">
           boardgv2diag(<xsl:value-of select="." />);
         </xsl:when>
         <xsl:when test="@type='hex'">
           hexgv2diag(<xsl:value-of select="." />);
         </xsl:when>
         <xsl:when test="@type='sqstack'">
           towergv2diag("tower2diag/back-board.jpg",<xsl:value-of select="." />);
         </xsl:when>
        </xsl:choose>
       </script>

      </td></tr>

    </table>
  </center>

</xsl:template>



<!-- ****** show examples ****** -->

<xsl:template match="example">
  <table class="example" width="600"> 
    <tr><td>
      <xsl:apply-templates select="diagram" />   
    </td> 
    <td class="rules" valign="top">
      <b><xsl:value-of select="title" /></b><br/>
      <xsl:apply-templates select="note" />   
    </td> 
  </tr> 
  </table> 
  <br/>
</xsl:template>



<!-- ****** show variants ****** -->

<xsl:template match="variant">

   <br/>
   <p id="subtitle">Variant: <xsl:value-of select="title" /></p>
   <p id="author">
     <xsl:apply-templates select="meta/author" />
     <xsl:apply-templates select="meta/publisher" />
     &#160;&#169;<xsl:value-of select="meta/year" />
   </p>

   <xsl:apply-templates select="components/diagram" />   
   <br/>

   <!-- ****** present game rules ****** -->
   <table class="rules">
     <tr><td class="rules" width="100%">
      <xsl:apply-templates select="description" />
     </td> </tr> 
   </table> 

   <!-- ****** present rule notes ****** -->
   <xsl:if test="count(notes/rulenote) = 0">
     <br/>
   </xsl:if>

   <xsl:if test="count(notes/rulenote) > 0">
     <xsl:apply-templates select="notes/rulenote" />
   </xsl:if>
   
   <!-- ****** present examples ****** -->
   <xsl:apply-templates select="description/example" />  

   <!-- ****** present remaining notes ****** -->
   <xsl:apply-templates select="notes/authornote" />
   <xsl:apply-templates select="notes/note" />

   <!-- ****** separator ****** -->
   <div class="hr"><hr /></div>

</xsl:template>



<!-- ****** related games ****** -->

<xsl:template match="related">
   <xsl:value-of select="name" />
   <xsl:apply-templates select="url"  />
   <xsl:if test="position() != last()">, </xsl:if>
</xsl:template>



<!-- ****** format stuff ****** -->

<xsl:template match="b">
   <b><xsl:apply-templates /></b>
</xsl:template>

<xsl:template match="u">
   <u><xsl:apply-templates /></u>
</xsl:template>

<xsl:template match="i">
   <i><xsl:apply-templates /></i>
</xsl:template>

<xsl:template match="a">
   <a target="_blank">
    <xsl:attribute name="href"> 
       <xsl:value-of select="@href" />
    </xsl:attribute>
    <xsl:value-of select="." />
   </a>
</xsl:template>

<xsl:template match="br">
   <br/>
</xsl:template>




<!-- ****** show image (and optional caption) ****** -->

<xsl:template match="image">
   <center>
     <img border="1">
       <xsl:attribute name="width"> 
          <xsl:value-of select="@width" />
       </xsl:attribute>

       <xsl:attribute name="height"> 
          <xsl:value-of select="@height" />
       </xsl:attribute>

       <xsl:attribute name="src"> 
          <xsl:value-of select="@src" />
       </xsl:attribute>
     </img><br/><p id="caption"><xsl:value-of select="@text" /></p>

   </center>
</xsl:template>


</xsl:stylesheet>
