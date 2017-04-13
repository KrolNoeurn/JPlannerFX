/**************************************************************************
 *  Copyright (C) 2017 by Richard Crook                                   *
 *  https://github.com/dazzle50/JPlannerFX                                *
 *                                                                        *
 *  This program is free software: you can redistribute it and/or modify  *
 *  it under the terms of the GNU General Public License as published by  *
 *  the Free Software Foundation, either version 3 of the License, or     *
 *  (at your option) any later version.                                   *
 *                                                                        *
 *  This program is distributed in the hope that it will be useful,       *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *  GNU General Public License for more details.                          *
 *                                                                        *
 *  You should have received a copy of the GNU General Public License     *
 *  along with this program.  If not, see http://www.gnu.org/licenses/    *
 **************************************************************************/

package rjc.jplanner.gui.gantt;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import rjc.jplanner.JPlanner;
import rjc.jplanner.gui.Colors;
import rjc.jplanner.gui.table.Table;
import rjc.jplanner.model.Calendar;
import rjc.jplanner.model.Date;
import rjc.jplanner.model.DateTime;
import rjc.jplanner.model.GanttData;
import rjc.jplanner.model.Predecessors;
import rjc.jplanner.model.Predecessors.Predecessor;
import rjc.jplanner.model.Task;

/*************************************************************************************************/
/***************** GanttPlot provides a view of the plan tasks and dependencies ******************/
/*************************************************************************************************/

public class GanttPlot extends Canvas
{
  private Gantt      m_gantt;
  private Table      m_table;

  private static int m_taskHeight = 6;
  private static int m_arrowSize  = 4;

  /**************************************** constructor ******************************************/
  public GanttPlot( Gantt gantt, Table table )
  {
    // construct gantt plot
    m_gantt = gantt;
    m_table = table;

    widthProperty().addListener( ( observable, oldW, newW ) -> widthChange( oldW.intValue(), newW.intValue() ) );
    heightProperty().addListener( ( observable, oldH, newH ) -> heightChange( oldH.intValue(), newH.intValue() ) );

    table.getCanvas().redrawBelowY.addListener( ( observable, oldY, newY ) ->
    {
      if ( newY.doubleValue() < getHeight() )
        heightChange( newY.intValue(), (int) getHeight() );
    } );

    setOnContextMenuRequested( event -> GanttPlotMenu.open( this, event ) );
  }

  /****************************************** getGantt *******************************************/
  public Gantt getGantt()
  {
    // return gantt containing with this plot
    return m_gantt;
  }

  /******************************************* redraw ********************************************/
  public void redraw()
  {
    // redraw whole gantt plot
    widthChange( 0, (int) getWidth() );
  }

  /***************************************** widthChange *****************************************/
  private void widthChange( int oldW, int newW )
  {
    // draw only if increase in width
    if ( oldW < 0 )
      oldW = 0;
    if ( getHeight() <= 0.0 || newW <= oldW )
      return;

    // fill background
    GraphicsContext gc = getGraphicsContext2D();
    gc.setFill( Colors.GANTT_BACKGROUND );
    gc.fillRect( oldW, 0.0, newW - oldW, getHeight() );

    // draw gantt plot
    shadeNonWorking( oldW, 0, newW - oldW, (int) getHeight() );
    drawTasks( 0, (int) getHeight() );
    drawDependencies();
  }

  /***************************************** heightChange ****************************************/
  private void heightChange( int oldH, int newH )
  {
    // draw only if increase in height
    if ( oldH < 0 )
      oldH = 0;
    if ( getWidth() <= 0.0 || newH <= oldH )
      return;

    // fill background
    GraphicsContext gc = getGraphicsContext2D();
    gc.setFill( Colors.GANTT_BACKGROUND );
    gc.fillRect( 0.0, oldH, getWidth(), newH - oldH );

    // draw gantt plot
    shadeNonWorking( 0, oldH, (int) getWidth(), newH - oldH );
    drawTasks( oldH, newH - oldH );
    drawDependencies();
  }

  /*************************************** shadeNonWorking ****************************************/
  private void shadeNonWorking( int x, int y, int w, int h )
  {
    // shade non-working time on gantt-plot
    Calendar calendar = JPlanner.plan.getDefaultcalendar();
    Date date = m_gantt.datetime( x - 1 ).getDate();
    int endEpoch = m_gantt.datetime( x + w ).getDate().getEpochday();
    int startShadeEpoch;

    GraphicsContext gc = getGraphicsContext2D();
    gc.setFill( Colors.GANTT_NONWORKING );
    do
    {
      // find start of non-working period
      if ( !calendar.isWorking( date ) )
      {
        startShadeEpoch = date.getEpochday();

        // find end of non-working period
        do
          date.increment();
        while ( date.getEpochday() <= endEpoch && !calendar.isWorking( date ) );

        // if width at least 1 pixel shade non-working period
        long width = ( date.getEpochday() - startShadeEpoch ) * DateTime.MILLISECONDS_IN_DAY / m_gantt.getMsPP();
        if ( width > 0L )
        {
          long xe = m_gantt.x( new DateTime( date.getEpochday() * DateTime.MILLISECONDS_IN_DAY ) );
          gc.fillRect( xe - width, y, width, h );
        }
      }

      date.increment();
    }
    while ( date.getEpochday() <= endEpoch );
  }

  /****************************************** drawTasks ******************************************/
  private void drawTasks( int y, int h )
  {
    // draw tasks on gantt
    int first = m_table.getRowAtY( y );
    int ry = m_table.getYStartByRow( first ) - m_table.getHorizontalHeaderHeight();
    int rh;
    int numTasks = JPlanner.plan.getTasksCount();
    for ( int row = first; row < numTasks; row++ )
    {
      // get row start-y and height, skip hidden rows
      rh = m_table.getRowHeight( row );
      if ( rh <= 0 )
        continue;

      // if task not null, draw task gantt-data
      GanttData data = JPlanner.plan.getTask( row ).getGanttData();
      if ( data != null )
        drawTask( ry + rh / 2, data, "TBD" );

      // if beyond area to be drawn, exit loop
      ry += rh;
      if ( ry + rh > y + h )
        break;

      // TODO also draw deadline
      /***
      if ( task->deadline() == XDateTime::NULL_DATETIME ) continue;
      int x = task->ganttData()->x( task->deadline(), m_start, m_minsPP );
      p->setPen( pen );
      p->drawLine( x, y-4, x, y+4 );
      p->drawLine( x-4, y, x, y+4 );
      p->drawLine( x+4, y, x, y+4 );
      ***/
    }

  }

  /****************************************** drawTask *******************************************/
  public void drawTask( int y, GanttData gd, String label )
  {
    // if gantt-data start not valid, don't draw anything
    if ( gd.start == null )
      return;

    // if no gantt-data value, draw milestone, otherwise summary or task bar
    if ( gd.end == null )
      drawMilestone( y, gd );
    else if ( gd.isSummary() )
      drawSummary( y, gd );
    else
      drawTaskBar( y, gd );

    // TODO draw label !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  }

  /***************************************** drawTaskBar *****************************************/
  private void drawTaskBar( int ty, GanttData gd )
  {
    // determine scale to draw offset
    double scale = 0.0;
    for ( int period = 0; period < gd.value.size(); period++ )
      if ( gd.value.get( period ) > scale )
        scale = gd.value.get( period );
    scale *= m_taskHeight;

    // set pen and fill colours
    GraphicsContext gc = getGraphicsContext2D();
    gc.setStroke( Colors.GANTT_TASK_EDGE );
    gc.setFill( Colors.GANTT_TASK_FILL );

    // calculate start position of task bar
    int tx = m_gantt.x( gd.start );

    // draw task bar shape and horizontal edges
    double xs = tx + 1.0;
    for ( int period = 0; period < gd.value.size(); period++ )
    {
      int offset = (int) ( gd.value.get( period ) * scale );
      int newX = m_gantt.x( gd.end.get( period ) );

      double xe = newX - 1.0;
      if ( xe > xs )
        if ( offset > 0 )
        {
          gc.fillRect( xs, ty - offset, xe - xs, offset + offset - 1 );
          gc.strokeLine( xs + 0.5, ty - offset - 0.5, xe - 0.5, ty - offset - 0.5 );
          gc.strokeLine( xs + 0.5, ty + offset - 0.5, xe - 0.5, ty + offset - 0.5 );
        }
        else
          gc.strokeLine( xs + 0.5, ty - 0.5, xe - 0.5, ty - 0.5 );

      xs = newX;
    }

    // draw other vertical (except last) edges
    int offset = 0;
    xs = tx + 0.5;
    for ( int period = 0; period < gd.value.size(); period++ )
    {
      int newOffset = (int) ( gd.value.get( period ) * scale );
      gc.strokeLine( xs, ty + offset + 0.5, xs, ty + newOffset - 0.5 );
      gc.strokeLine( xs, ty - offset - 0.5, xs, ty - newOffset - 0.5 );

      offset = newOffset;
      xs = m_gantt.x( gd.end.get( period ) ) - 0.5;
    }

    // draw last edge
    if ( xs > tx )
      gc.strokeLine( xs, ty + offset - 0.5, xs, ty - offset - 0.5 );
  }

  /***************************************** drawSummary *****************************************/
  private void drawSummary( int y, GanttData gd )
  {
    // draw summary
    double xs = m_gantt.x( gd.start );
    double xe = m_gantt.x( gd.end.get( 0 ) );
    if ( xe <= xs )
      xe = xs + 1.0;

    double w = m_taskHeight;
    if ( w > xe - xs )
      w = xe - xs;

    // draw main summary bar
    GraphicsContext gc = getGraphicsContext2D();
    gc.setFill( Colors.GANTT_SUMMARY );
    gc.fillRect( xs, y - m_taskHeight, xe - xs, m_taskHeight );

    // draw start and end triangles
    gc.setStroke( Colors.GANTT_SUMMARY );
    xs += 0.5;
    xe -= 0.5;
    for ( int x = 0; x < w; x++ )
    {
      gc.strokeLine( xs + x, y + 0.5, xs + x, y - 0.5 + m_taskHeight * ( w - x ) / w );
      gc.strokeLine( xe - x, y + 0.5, xe - x, y - 0.5 + m_taskHeight * ( w - x ) / w );
    }
  }

  /**************************************** drawMilestone ****************************************/
  private void drawMilestone( int y, GanttData gd )
  {
    // draw diamond shaped milestone marker
    double x = m_gantt.x( gd.start ) + 0.5;
    double size = m_taskHeight - 0.5;

    GraphicsContext gc = getGraphicsContext2D();
    gc.setStroke( Colors.GANTT_MILESTONE );
    gc.strokeLine( x, y - size, x, y + size - 1 );
    for ( int h = 1; h <= m_taskHeight - 1; h++ )
    {
      gc.strokeLine( x + h, y - size + h, x + h, y + size - h - 1 );
      gc.strokeLine( x - h, y - size + h, x - h, y + size - h - 1 );
    }
  }

  /*************************************** drawDependencies **************************************/
  private void drawDependencies()
  {
    // draw dependencies on gantt for each task
    GraphicsContext gc = getGraphicsContext2D();
    gc.setStroke( Colors.GANTT_DEPENDENCY );

    int numTasks = JPlanner.plan.getTasksCount();
    for ( int row = 0; row < numTasks; row++ )
    {
      // if hidden, skip row
      int rh = m_table.getRowHeight( row );
      if ( rh <= 0 )
        continue;

      // if no gantt-data, skip row
      Task task = JPlanner.plan.getTask( row );
      if ( task.getGanttData() == null )
        continue;

      int thisY = m_table.getYStartByRow( row ) + rh / 2 - m_table.getHorizontalHeaderHeight();

      // for each predecessor on task
      Predecessors preds = task.getPredecessors();
      for ( int p = 0; p < preds.getCount(); p++ )
      {
        // if hidden or no gantt-data, skip predecessor
        Predecessor pred = preds.get( p );
        int other = pred.task.getIndex();
        rh = m_table.getRowHeight( other );
        if ( rh <= 0 || pred.task.getGanttData() == null )
          continue;

        int otherY = m_table.getYStartByRow( other ) + rh / 2 - m_table.getHorizontalHeaderHeight();

        switch ( pred.type )
        {
          case Predecessors.TYPE_START_FINISH:
            drawDependencySF( gc, xStart( pred.task ), otherY, xEnd( task ), thisY, m_gantt.x( task.getEnd() ) );
            break;
          case Predecessors.TYPE_START_START:
            drawDependencySS( gc, xStart( pred.task ), otherY, xStart( task ), thisY );
            break;
          case Predecessors.TYPE_FINISH_FINISH:
            drawDependencyFF( gc, xEnd( pred.task ), otherY, xEnd( task ), thisY );
            break;
          case Predecessors.TYPE_FINISH_START:
            drawDependencyFS( gc, xEnd( pred.task ), otherY, xStart( task ), thisY, m_gantt.x( task.getStart() ) );
            break;
          default:
            throw new IllegalArgumentException( "Invalid predecessor type: " + pred.type );
        }
      }
    }

  }

  /******************************************* xStart ********************************************/
  private int xStart( Task task )
  {
    // return x of task start compensated if milestone
    if ( task.getGanttData().isMilestone() )
      return m_gantt.x( task.getStart() ) - m_taskHeight;
    else
      return m_gantt.x( task.getStart() );
  }

  /******************************************** xEnd *********************************************/
  private int xEnd( Task task )
  {
    // return x of task end compensated if milestone
    if ( task.getGanttData().isMilestone() )
      return m_gantt.x( task.getEnd() ) + m_taskHeight;
    else
      return m_gantt.x( task.getEnd() );
  }

  /*************************************** drawDependencyFS **************************************/
  private void drawDependencyFS( GraphicsContext gc, int x1, int y1, int x2, int y2, int x2raw )
  {
    // draw dependency from one task-finish to another task-start
    double sign = y1 > y2 ? -1.0 : 1.0;
    double xs = x1 + 0.5;
    double ys = y1 - 0.5;

    // if task-start after or equal task-finish can draw simple arrow
    if ( x2raw >= x1 )
    {
      double len = x2raw - x1 - 1.0;
      if ( len < 3.0 )
        len = 3.0;

      gc.strokeLine( xs, ys, xs + len, ys );
      len++;
      drawArrow( gc, xs + len, ys + sign, xs + len, y2 - 0.5 - sign * ( m_taskHeight + 1 ) );
      return;
    }

    // need to draw arrow double backing from later task-finish to earlier task-start
    double xe = x2 + 0.5;
    double ye = y2 - 0.5;
    gc.strokeLine( xs, ys, xs + 3, ys );
    gc.strokeLine( xs + 4, ys + sign, xs + 4, ys + sign * ( m_taskHeight + 3 ) );
    gc.strokeLine( xs + 3, ys + sign * ( m_taskHeight + 4 ), xe - 7, ys + sign * ( m_taskHeight + 4 ) );
    gc.strokeLine( xe - 8, ys + sign * ( m_taskHeight + 5 ), xe - 8, ye - sign );
    drawArrow( gc, xe - 7, ye, xe - 1, ye );
  }

  /*************************************** drawDependencySF **************************************/
  private void drawDependencySF( GraphicsContext gc, int x1, int y1, int x2, int y2, int x2raw )
  {
    // draw dependency from one task-start to another task-finish
    double sign = y1 > y2 ? -1 : 1;
    double xs = x1 - 0.5;
    double ys = y1 - 0.5;

    // if task-start after or equal task-finish can draw simple arrow
    if ( x2raw <= x1 )
    {
      double len = x2raw - x1 + 1.0;
      if ( len > -3.0 )
        len = -3.0;

      gc.strokeLine( xs, ys, xs + len, ys );
      len--;
      drawArrow( gc, xs + len, ys + sign, xs + len, y2 - 0.5 - sign * ( m_taskHeight + 1 ) );
      return;
    }

    // need to draw arrow double backing from later task-finish to earlier task-start
    double xe = x2 + 0.5;
    double ye = y2 - 0.5;
    gc.strokeLine( xs, ys, xs - 3, ys );
    gc.strokeLine( xs - 4, ys + sign, xs - 4, ys + sign * ( m_taskHeight + 3 ) );
    gc.strokeLine( xs - 3, ys + sign * ( m_taskHeight + 4 ), xe + 7, ys + sign * ( m_taskHeight + 4 ) );
    gc.strokeLine( xe + 8, ys + sign * ( m_taskHeight + 5 ), xe + 8, ye - sign );
    drawArrow( gc, xe + 7, ye, xe, ye );
  }

  /*************************************** drawDependencySS **************************************/
  private void drawDependencySS( GraphicsContext gc, int x1, int y1, int x2, int y2 )
  {
    // draw dependency from one task-start to another task-start
    double sign = y1 > y2 ? -1.0 : 1.0;
    double xs = x1 + 0.5;
    double ys = y1 - 0.5;
    double ye = y2 - 0.5;

    double x = xs - 3.0;
    if ( x > x2 - 7.5 )
      x = x2 - 7.5;

    gc.strokeLine( xs - 1, ys, x, ys );
    x--;
    gc.strokeLine( x, ys + sign, x, ye - sign );
    drawArrow( gc, x + 1, ye, x2 - 0.5, ye );
  }

  /*************************************** drawDependencyFF **************************************/
  private void drawDependencyFF( GraphicsContext gc, int x1, int y1, int x2, int y2 )
  {
    // draw dependency from one task-finish to another task-finish
    double sign = y1 > y2 ? -1.0 : 1.0;
    double xs = x1 + 0.5;
    double ys = y1 - 0.5;
    double ye = y2 - 0.5;

    double x = xs + 3.0;
    if ( x < x2 + 8.5 )
      x = x2 + 8.5;

    gc.strokeLine( xs, ys, x, ys );
    x++;
    gc.strokeLine( x, ys + sign, x, ye - sign );
    drawArrow( gc, x - 1, ye, x2 + 0.5, ye );
  }

  /****************************************** drawArrow ******************************************/
  private void drawArrow( GraphicsContext gc, double x1, double y1, double x2, double y2 )
  {
    // draw line with arrow at end
    if ( x1 == x2 )
    {
      // vertical line and arrow
      gc.strokeLine( x1, y1, x2, y2 );
      int sign = y1 > y2 ? -1 : 1;
      double y = y2 - sign * m_arrowSize;

      for ( int s = 1; s <= m_arrowSize; s++ )
      {
        y2 -= sign;
        gc.strokeLine( x2 + s, y, x2 + s, y2 );
        gc.strokeLine( x2 - s, y, x2 - s, y2 );
      }
    }
    else
    {
      // horizontal line and arrow
      gc.strokeLine( x1, y1, x2, y2 );
      int sign = x1 > x2 ? -1 : 1;
      double x = x2 - sign * m_arrowSize;

      for ( int s = 1; s <= m_arrowSize; s++ )
      {
        x2 -= sign;
        gc.strokeLine( x, y2 + s, x2, y2 + s );
        gc.strokeLine( x, y2 - s, x2, y2 - s );
      }
    }
  }

}
