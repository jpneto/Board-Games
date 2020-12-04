package toolbox;

public class TacticNeural extends Tactic {

  double[][] layer1;
  double[]   layer2, piecesVal;

  /*
   *  This neural net has as many inputs has cell boards.
   *  It starts with cells a1,b1,c1... then a2,b2,c2,... and so on...
   *  Each input value depends of what piece is on that cell.
   */
  public double eval(Game g) {

    double result = 0.0;
    double[] hiddenLayer = new double[ layer1[0].length ];
    Board b = g.getBoard();
    int   m = 0;

    // First Layer
    if (b.isHex()) {

      for (int k=0;k<layer1[0].length;k++)  // for each middle layer result
        for (int i=0;i<b.getMiddle();i++)
          for (int j=0;j<b.getMiddle();j++)
            if (b.isCellValid(b.point2coord(i,j)))
              hiddenLayer[k] += piecesVal[b.getPositionCell(i,j)] *layer1[k][m++];

    } else {

      for (int k=0;k<layer1[0].length;k++)  // for each middle layer result
        for (int i=0;i<b.getRows();i++)
          for (int j=0;j<b.getCols();j++)
            hiddenLayer[k] += piecesVal[b.getPositionCell(i,j)] *layer1[k][m++];

    }

    // Second Layer
    for (int i=0;i<hiddenLayer.length;i++)
      result += hiddenLayer[i] * layer2[i];

    return result;
  }

  public TacticNeural(double[][] l1, double[] l2, double[] pv) {
    description = "Neural Tactic";
    layer1      = l1;
    layer2      = l2;
    piecesVal   = pv;
  }
}