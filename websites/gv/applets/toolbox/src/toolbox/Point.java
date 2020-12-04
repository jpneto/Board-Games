package toolbox;

public class Point {

  protected int x, y;

  public int getX() { return x; }
  public int getY() { return y; }

  public String toString() {
    return "["+x+","+y+"]";
  }

  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }
}
