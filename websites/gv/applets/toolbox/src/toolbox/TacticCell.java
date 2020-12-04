package toolbox;

public class TacticCell extends Tactic {

  double[][] weights;
  double[]   piecesVal;

  public double eval(Game g) {

    double result = 0.0;
    Board b = g.getBoard();

    if (g.getActualPlayerId() == g.SECOND)  // switch values
      for(int i=0;i<piecesVal.length;i++)
        piecesVal[i] *= -1;

    if (b.isHex()) {
      for (int i=0;i<b.getMiddle();i++)
        for (int j=0;j<b.getMiddle();j++)
          result += piecesVal[b.getPositionIndex(i,j)] * weights[i][j];
    } else {
      for (int i=0;i<b.getRows();i++)
        for (int j=0;j<b.getCols();j++)
          result += piecesVal[b.getPositionIndex(i,j)] * weights[i][j];
    }

    if (g.getActualPlayerId() == g.SECOND)  // reset values
      for(int i=0;i<piecesVal.length;i++)
        piecesVal[i] *= -1;

    return result;
  }

  public TacticCell(double[][] w, double[] pv) {
    description = "Tactic Cell";
    weights     = w;
    piecesVal   = pv;
  }

}