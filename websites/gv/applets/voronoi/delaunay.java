/*-------------------------------------------------------------------
   Copyright (c) Ohio State University. All Rights Reserved.

   Zhiyuan Zhao    < zhao.29.osu.edu>
   Alan Saalfeld   < saalfeld.1@osu.edu>

   Department of Geodetic Science and Surveying
   Ohio State University, Columbus, OH 43210, U.S.A.

   06/19/1996  first version of Java
   10/09/1996  second version,  add Voronoi diagram
   10/17/1996  second version,  add node deletion
   11/10/1996  third version, change data structures

   Permission to use, copy, modify, and distribute this program for
   non-commercial purpose without fee is granted as long as this
   copyright note is included and an email is send to the authors
-------------------------------------------------------------------*/
import java.awt.*;
import java.applet.*;
import java.lang.*;
import java.util.*;

public class delaunay extends Applet
{
  DrawPad dp;
  Button bt_cl;
  Checkbox cb_pt,cb_tri,cb_vor,cb_cc;

  public void init()
    {
      setLayout(new BorderLayout());
      Panel p = new Panel();
      add("North", p);
      p.add(bt_cl=new Button("Reset"));
      p.add(cb_pt=new Checkbox("Show Nodes"));
      p.add(cb_tri=new Checkbox("Triangulations"));
      p.add(cb_cc=new Checkbox("Circumcircles"));
      p.add(cb_vor=new Checkbox("Voronoi Digram"));
      add("Center",dp= new DrawPad());
      cb_pt.setState(true);
      cb_tri.setState(false);
      cb_vor.setState(true);
      cb_cc.setState(false);
    }
  public boolean action(Event e, Object o)
    {
      if(e.target==bt_cl) { dp.Clear(); return true; }
      if(e.target==cb_pt) { dp.showpt=!dp.showpt; dp.repaint(); return true; }
      if(e.target==cb_tri) { dp.showtri=!dp.showtri; dp.repaint(); return true; }
      if(e.target==cb_vor) { dp.showvor=!dp.showvor; dp.repaint(); return true; }
      if(e.target==cb_cc) { dp.showcir=!dp.showcir; dp.repaint(); return true; }
      return false;
    }
}

class DrawPad extends Panel
{
    DelaunayT dt;
    boolean showpt=true,showtri=false, showcir=false,showvor=true;

    public DrawPad()
      {
        Color color =  new Color(213, 189, 131);
        dt=new DelaunayT(200);
        setBackground(color);
      }
    public boolean mouseDown(Event e, int x, int y)
      {
        if(e.metaDown()) dt.Delete(x,y);
        else dt.Insert(x,y);
        repaint();
        return true;
      }
    public void paint(Graphics g)
      {
        if(showcir==true) dt.DrawCircles(g,Color.white);
        if(showtri==true) dt.DrawTriangles(g,Color.green);
        if(showvor==true) dt.DrawVoronoiDigram(g,Color.black);
        if(showpt==true)  dt.DrawPoints(g,Color.black);
      }
    void Clear() { dt.Clear(); repaint(); }
}

class Node extends Object
{
     int   x,y;       // coordinate X,Y
     Edge anEdge;     // an edge which start from this node
     char  type;      // 1: inner nodes, 2: on convex hull, 3: ...

     public Node(int x, int y) {this.x=x; this.y=y;anEdge=null;}
     public Edge GetEdge() { return anEdge;}
     public double Distance(double px, double py)
       { double dx=px-x;
         double dy=py-y;
         return (Math.sqrt(dx*dx+dy*dy));
       }
}

class Edge extends Object
{
     Node         p1,p2;         // start and end point of the edge
     Edge         invE=null;     // inverse edge (p2->p1)
     Edge         nextE=null;    // next edge in the triangle in countclickwise
     Edge         nextH=null;    // convex hull link
     Triangle     inT=null;      // triangle containing this edge
     double a,b,c;               // line equation parameters. aX+bY+c=0

    public Edge(Node p1, Node p2) { Update(p1,p2); }
    public void Update(Node p1, Node p2) { this.p1=p1; this.p2=p2; Setabc(); AsIndex(); }
    Node P1() { return p1;}
    Node P2() { return p2;}
    Edge InvE() { return invE;}
    Edge NextE() { return nextE;}
    Edge NextH() { return nextH;}
    Triangle Tri() { return inT;}
    void setNextE(Edge e) {nextE=e;}
    void setNextH(Edge e) {nextH=e;}
    void setTri(Triangle t) {inT=t;}
    void setInvE(Edge e) {invE=e;}
    Edge MakeSymm() { Edge e=new Edge(p2,p1); LinkSymm(e); return e; }
    void LinkSymm(Edge e) { this.invE=e; if(e!=null) e.invE=this; }
    public int onSide(Node nd)
       { double s=a*nd.x+b*nd.y+c;
         if(s>0.0) return 1;
         if(s<0.0) return -1;
         return 0;
       }
    void Setabc()   // set parameters of a,b,c
       { a=p2.y-p1.y; b=p1.x-p2.x; c=p2.x*p1.y-p1.x*p2.y; }
    void AsIndex()  { p1.anEdge=this;}
    Edge MostLeft()
       { Edge ee,e=this;
         while((ee=e.NextE().NextE().InvE())!=null && ee!=this) e=ee;
         return e.NextE().NextE();
       }
    Edge MostRight()
       { Edge ee,e=this;
         while(e.InvE()!=null && (ee=e.InvE().NextE())!=this) e=ee;
         return e;
       }
    public void Draw(Graphics g) { g.drawLine(p1.x,p1.y,p2.x,p2.y); }
}

class Triangle extends Object
{
     Edge    anEdge;      // an edge of this triangle
     double  c_cx;        // center of circle: X
     double  c_cy;        // center of circle: Y
     double  c_r;         // radius of circle

     public  Triangle(Edge e1, Edge e2, Edge e3) {Update(e1,e2,e3);}
     public  Triangle(Vector edges, Edge e1, Edge e2, Edge e3)
       {
         Update(e1,e2,e3);
         edges.addElement(e1);
         edges.addElement(e2);
         edges.addElement(e3);
       }
     public void Update(Edge e1, Edge e2, Edge e3)
       {
         anEdge=e1;
         e1.setNextE(e2);
         e2.setNextE(e3);
         e3.setNextE(e1);
         e1.setTri(this);
         e2.setTri(this);
         e3.setTri(this);
         FindCircle();
       }
     public Edge GetEdge() { return anEdge;}
     boolean InCircle(Node nd) { return nd.Distance(c_cx,c_cy)<c_r; }
     void RemoveEdges(Vector edges)
       {
         edges.removeElement(anEdge);
         edges.removeElement(anEdge.NextE());
         edges.removeElement(anEdge.NextE().NextE());
       }
     void FindCircle()
       {
         double x1=(double) anEdge.P1().x;
         double y1=(double) anEdge.P1().y;
         double x2=(double) anEdge.P2().x;
         double y2=(double) anEdge.P2().y;
         double x3=(double) anEdge.NextE().P2().x;
         double y3=(double) anEdge.NextE().P2().y;
         double a=(y2-y3)*(x2-x1)-(y2-y1)*(x2-x3);
         double a1=(x1+x2)*(x2-x1)+(y2-y1)*(y1+y2);
         double a2=(x2+x3)*(x2-x3)+(y2-y3)*(y2+y3);
         c_cx=(a1*(y2-y3)-a2*(y2-y1))/a/2;
         c_cy=(a2*(x2-x1)-a1*(x2-x3))/a/2;
         c_r=anEdge.P1().Distance(c_cx,c_cy);
       }
     public void DrawCircles(Graphics g)
       {
         int x0,y0,x,y;
 		 x0 = (int) (c_cx-c_r);
		 y0 = (int) (c_cy-c_r);
		 x =  (int) (2.0*c_r);
		 y =  (int) (2.0*c_r);
		 g.drawOval((int)(c_cx-c_r), (int)(c_cy-c_r), (int)(2.0*c_r), (int)(2.0*c_r));
       }
}

class DelaunayT
{
   Vector nodes;        // nodes set
   Vector edges;        // edges set
   Vector tris;         // triangles set
   Edge   hullStart;    // entring edge of convex hull
   Edge   actE;

   public DelaunayT(int size)
     {
       tris=new Vector(size);
       nodes=new Vector(3*size);
       edges=new Vector(3*size);
     }
   public void Clear()
     {
       nodes.removeAllElements();
       edges.removeAllElements();
       tris.removeAllElements();
     }
   public void Insert(int px, int py)
      {
        int eid;
        Node nd=new Node(px,py);
        nodes.addElement(nd);
        if(nodes.size()<3) return;
        if(nodes.size()==3)    // create the first triangle
          {
            Node p1=(Node)nodes.elementAt(0);
            Node p2=(Node)nodes.elementAt(1);
            Node p3=(Node)nodes.elementAt(2);
            Edge e1=new Edge(p1,p2);
            if(e1.onSide(p3)==0) { nodes.removeElement(nd); return; }
            if(e1.onSide(p3)==-1)  // right side
              {
                p1=(Node)nodes.elementAt(1);
                p2=(Node)nodes.elementAt(0);
                e1.Update(p1,p2);
              }
            Edge e2=new Edge(p2,p3);
            Edge e3=new Edge(p3,p1);
            e1.setNextH(e2);
            e2.setNextH(e3);
            e3.setNextH(e1);
            hullStart=e1;
            tris.addElement(new Triangle(edges,e1,e2,e3));
            return;
          }
        actE=(Edge)edges.elementAt(0);
        if(actE.onSide(nd)==-1)
          { if(actE.InvE()==null) eid=-1;
            else eid=SearchEdge(actE.InvE(),nd);
          }
        else eid=SearchEdge(actE,nd);
        if(eid==0) { nodes.removeElement(nd); return; }
        if(eid>0) ExpandTri(actE,nd,eid);   // nd is inside or on a triangle
        else ExpandHull(nd);                // nd is outside convex hull
      }
    public void Delete(int px, int py)
      {
        if(nodes.size()<=3) return;   // not allow deletion for only 1 triangle
        Node nd=Nearest((double)px,(double)py);
        if(nd==null) return;          // not found
        nodes.removeElement(nd);
        Edge e,ee,start;
        start=e=nd.GetEdge().MostRight();
        int nodetype=0;
        int idegree=-1;
        Edge index[]=new Edge[100];
        while(nodetype==0)
         {
           edges.removeElement(ee=e.NextE());
           index[++idegree]=ee;
           ee.AsIndex();
           tris.removeElement(e.Tri());   // delete triangles involved
           edges.removeElement(e);
           edges.removeElement(ee.NextE());
           e=ee.NextE().InvE();            // next left edge
           if(e==null) nodetype=2;         // nd on convex hull
           if(e==start) nodetype=1;        // inner node
         }
        // generate new triangles and add to triangulation
        int cur_i=0,cur_n=0;
        int last_n=idegree;
        Edge e1=null,e2=null,e3;
        while(last_n>=1)
          {
             e1=index[cur_i];
             e2=index[cur_i+1];
             if(last_n==2 && nodetype==1)
               {
                 tris.addElement(new Triangle(edges,e1,e2,index[2]));
                 SwapTest(e1);
                 SwapTest(e2);
                 SwapTest(index[2]);
                 break;
               }
             if(last_n==1 && nodetype==1)
               {
                 index[0].InvE().LinkSymm(index[1].InvE());
                 index[0].InvE().AsIndex();
                 index[1].InvE().AsIndex();
                 SwapTest(index[0].InvE());
                 break;
               }
             if(e1.onSide(e2.P2())==1)  // left side
               {
                 e3=new Edge(e2.P2(),e1.P1());
                 cur_i+=2;
                 index[cur_n++]=e3.MakeSymm();
                 tris.addElement(new Triangle(edges,e1,e2,e3));
                 SwapTest(e1);
                 SwapTest(e2);
               }
             else index[cur_n++]=index[cur_i++];
             if(cur_i==last_n) index[cur_n++]=index[cur_i++];
             if(cur_i==last_n+1)
               {
                 if(last_n==cur_n-1) break;
                 last_n=cur_n-1;
                 cur_i=cur_n=0;
               }
          }
        if(nodetype==2)   // reconstruct the convex hull
         {
           index[last_n].InvE().MostLeft().setNextH(hullStart=index[last_n].InvE());
           for(int i=last_n;i>0;i--)
             { index[i].InvE().setNextH(index[i-1].InvE());
               index[i].InvE().setInvE(null);
             }
           index[0].InvE().setNextH(start.NextH());
           index[0].InvE().setInvE(null);
         }
     }
   void ExpandTri(Edge e, Node nd, int type)
     {
       Edge e1=e;
       Edge e2=e1.NextE();
       Edge e3=e2.NextE();
       Node p1=e1.P1();
       Node p2=e2.P1();
       Node p3=e3.P1();
       if(type==2)    // nd is inside of the triangle
         {
          Edge e10=new Edge(p1,nd);
          Edge e20=new Edge(p2,nd);
          Edge e30=new Edge(p3,nd);
          e.Tri().RemoveEdges(edges);
          tris.removeElement(e.Tri());     // remove old triangle
          tris.addElement(new Triangle(edges,e1,e20,e10.MakeSymm()));
          tris.addElement(new Triangle(edges,e2,e30,e20.MakeSymm()));
          tris.addElement(new Triangle(edges,e3,e10,e30.MakeSymm()));
          SwapTest(e1);   // swap test for the three new triangles
          SwapTest(e2);
          SwapTest(e3);
         }
       else           // nd is on the edge e
         {
          Edge e4=e1.InvE();
          if(e4==null || e4.Tri()==null)           // one triangle involved
            {
              Edge e30=new Edge(p3,nd);
              Edge e02=new Edge(nd,p2);
              Edge e10=new Edge(p1,nd);
              Edge e03=e30.MakeSymm();
              e10.AsIndex();
              e1.MostLeft().setNextH(e10);
              e10.setNextH(e02);
              e02.setNextH(e1.NextH());
              hullStart=e02;
              tris.removeElement(e1.Tri());                   // remove oldtriangle               // add two new triangles
              edges.removeElement(e1);
              edges.addElement(e10);
              edges.addElement(e02);
              edges.addElement(e30);
              edges.addElement(e03);
              tris.addElement(new Triangle(e2,e30,e02));
              tris.addElement(new Triangle(e3,e10,e03));
              SwapTest(e2);   // swap test for the two new triangles
              SwapTest(e3);
              SwapTest(e30);
            }
          else         // two triangle involved
            {
              Edge e5=e4.NextE();
              Edge e6=e5.NextE();
              Node p4=e6.P1();
              Edge e10=new Edge(p1,nd);
              Edge e20=new Edge(p2,nd);
              Edge e30=new Edge(p3,nd);
              Edge e40=new Edge(p4,nd);
              tris.removeElement(e.Tri());                   // remove oldtriangle
              e.Tri().RemoveEdges(edges);
              tris.removeElement(e4.Tri());               // remove old triangle
              e4.Tri().RemoveEdges(edges);
              e5.AsIndex();   // because e, e4 removed, reset edge index of node p1 and p2
              e2.AsIndex();
              tris.addElement(new Triangle(edges,e2,e30,e20.MakeSymm()));
              tris.addElement(new Triangle(edges,e3,e10,e30.MakeSymm()));
              tris.addElement(new Triangle(edges,e5,e40,e10.MakeSymm()));
              tris.addElement(new Triangle(edges,e6,e20,e40.MakeSymm()));
              SwapTest(e2);   // swap test for the three new triangles
              SwapTest(e3);
              SwapTest(e5);
              SwapTest(e6);
              SwapTest(e10);
              SwapTest(e20);
              SwapTest(e30);
              SwapTest(e40);
            }
         }
     }
   void ExpandHull(Node nd)
     {
        Edge e1,e2,e3=null,enext;
        Edge e=hullStart;
        Edge comedge=null,lastbe=null;
        while(true)
          {
            enext=e.NextH();
            if(e.onSide(nd)==-1)   // right side
              {
                if(lastbe!=null)
                  {
                     e1=e.MakeSymm();
                     e2=new Edge(e.P1(),nd);
                     e3=new Edge(nd,e.P2());
                     if(comedge==null)
                       {
                         hullStart=lastbe;
                         lastbe.setNextH(e2);
                         lastbe=e2;
                       }
                     else comedge.LinkSymm(e2);
                     comedge=e3;
                     tris.addElement(new Triangle(edges,e1,e2,e3));
                     SwapTest(e);
                  }
              }
            else
              {
                if(comedge!=null) break;
                lastbe=e;
              }
            e=enext;
          }

        lastbe.setNextH(e3);
        e3.setNextH(e);
     }
   int SearchEdge(Edge e, Node nd)
     {
      int f2,f3;
      Edge e0=null;
      if((f2=e.NextE().onSide(nd))==-1)
        { if(e.NextE().InvE()!=null) return SearchEdge(e.NextE().InvE(),nd);
          else { actE=e; return -1;}
        }
      if(f2==0) e0=e.NextE();
      Edge ee=e.NextE();
      if((f3=ee.NextE().onSide(nd))==-1)
        { if(ee.NextE().InvE()!=null) return SearchEdge(ee.NextE().InvE(),nd);
          else { actE=ee.NextE(); return -1;}
        }
      if(f3==0) e0=ee.NextE();
      if(e.onSide(nd)==0) e0=e;
      if(e0!=null)
        {
          actE=e0;
          if(e0.NextE().onSide(nd)==0) {actE=e0.NextE(); return 0;}
          if(e0.NextE().NextE().onSide(nd)==0) return 0;
          return 1;
        }
      actE=ee;
      return 2;
     }
   void SwapTest(Edge e11)
     {
       Edge e21=e11.InvE();
       if(e21==null || e21.Tri()==null) return;
       Edge e12=e11.NextE();
       Edge e13=e12.NextE();
       Edge e22=e21.NextE();
       Edge e23=e22.NextE();
       if(e11.Tri().InCircle(e22.P2()) || e21.Tri().InCircle(e12.P2()))
         {
           e11.Update(e22.P2(),e12.P2());
           e21.Update(e12.P2(),e22.P2());
           e11.LinkSymm(e21);
           e13.Tri().Update(e13,e22,e11);
           e23.Tri().Update(e23,e12,e21);
           e12.AsIndex();
           e22.AsIndex();
           SwapTest(e12);
           SwapTest(e22);
           SwapTest(e13);
           SwapTest(e23);
         }
     }
   Node Nearest(double x, double y)
     {
       // locate a node nearest to (px,py)
       double dismin=0.0,s;
       Node nd=null;
       for(int i=0;i<nodes.size();i++)
         {
           s=((Node)nodes.elementAt(i)).Distance(x,y);
           if(s<dismin||nd==null) { dismin=s;nd=(Node)nodes.elementAt(i);}
         }
       return nd;
     }
   public void DrawPoints(Graphics g, Color color)
     {
       g.setColor(color);
       for(int i=0;i<nodes.size();i++)
         g.drawRect(((Node)nodes.elementAt(i)).x-1,((Node)nodes.elementAt(i)).y-1,2,2);
     }
   public void DrawTriangles(Graphics g, Color color)
     {
       g.setColor(color);
       if(nodes.size()==1)
         g.drawRect(((Node)nodes.elementAt(0)).x,((Node)nodes.elementAt(0)).y,1,1);
       if(nodes.size()==2)
         g.drawLine(((Node)nodes.elementAt(0)).x,((Node)nodes.elementAt(0)).y,((Node)nodes.elementAt(1)).x,((Node)nodes.elementAt(1)).y);
       for (int i=0; i<edges.size(); i++) ((Edge)edges.elementAt(i)).Draw(g);
       for (int i=0; i<tris.size(); i++)
         { Triangle t=(Triangle)tris.elementAt(i);
           t.anEdge.Draw(g);
           t.anEdge.NextE().Draw(g);
           t.anEdge.NextE().NextE().Draw(g);
         }
     }
   public void DrawCircles(Graphics g, Color color)
     {
       g.setColor(color);
       for(int i=0;i<tris.size();i++)  ((Triangle)tris.elementAt(i)).DrawCircles(g);
     }
   public void DrawVoronoiDigram(Graphics g, Color color)
     {
        g.setColor(color);
        double tcx,tcy;
        for (int i=0; i<edges.size(); i++)
          {
            Edge e = (Edge)edges.elementAt(i);
            Edge ee=e.InvE();
            if(ee==null || ee.Tri()==null)
              {
                tcx=e.Tri().c_cx-e.P2().y+e.P1().y;
                tcy=e.Tri().c_cy-e.P1().x+e.P2().x;
              }
           else
             {
               tcx=ee.Tri().c_cx;
               tcy=ee.Tri().c_cy;
             }
           g.drawLine((int)e.Tri().c_cx,(int)e.Tri().c_cy,(int)tcx,(int)tcy);
         }
     }
}
// ------- end of the file ------------------------------------------

