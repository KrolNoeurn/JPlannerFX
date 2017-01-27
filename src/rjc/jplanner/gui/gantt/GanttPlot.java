/**************************************************************************
 *  Copyright (C) 2016 by Richard Crook                                   *
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
import javafx.scene.paint.Color;
import rjc.jplanner.JPlanner;
import rjc.jplanner.gui.Colors;
import rjc.jplanner.model.Calendar;
import rjc.jplanner.model.Date;
import rjc.jplanner.model.DateTime;

/*************************************************************************************************/
/***************** GanttPlot provides a view of the plan tasks and dependencies ******************/
/*************************************************************************************************/

public class GanttPlot extends Canvas
{
  private Gantt         m_gantt;

  private static int    m_taskHeight = 6;
  private static int    m_arrowSize  = 4;

  public static boolean ganttStretch;

  /**************************************** constructor ******************************************/
  public GanttPlot( Gantt gantt )
  {
    // construct gantt-plot
    m_gantt = gantt;

    widthProperty().addListener( ( observable, oldW, newW ) -> widthChange( oldW.intValue(), newW.intValue() ) );
    heightProperty().addListener( ( observable, oldH, newH ) -> heightChange( oldH.intValue(), newH.intValue() ) );
  }

  /******************************************* redraw ********************************************/
  public void redraw()
  {
    widthChange( 0, (int) getWidth() );
  }

  /***************************************** widthChange *****************************************/
  private void widthChange( int oldW, int newW )
  {
    // draw only if increase in width
    if ( getHeight() <= 0.0 || newW <= oldW )
      return;

    // TODO ...............
    GraphicsContext gc = getGraphicsContext2D();
    gc.setFill( Color.WHITE );
    gc.fillRect( oldW, 0.0, newW - oldW, getHeight() );
    shadeNonWorking( oldW, 0, newW - oldW, (int) getHeight() );
  }

  /***************************************** heightChange ****************************************/
  private void heightChange( int oldH, int newH )
  {
    JPlanner.trace( "PLOT", this, oldH, newH, getHeight() );

    // draw only if increase in height
    if ( getWidth() <= 0.0 || newH <= oldH )
      return;

    // TODO ...............
    GraphicsContext gc = getGraphicsContext2D();
    gc.setFill( Color.WHITE );
    gc.fillRect( 0.0, oldH, getWidth(), newH - oldH );
    shadeNonWorking( 0, oldH, (int) getWidth(), newH - oldH );
  }

  /*************************************** shadeNonWorking ****************************************/
  private void shadeNonWorking( int x, int y, int w, int h )
  {
    // shade non-working time on gantt-plot
    Calendar calendar = JPlanner.plan.getDefaultcalendar();
    Date date = m_gantt.datetime( x - 1 ).date();
    int endEpoch = m_gantt.datetime( x + w ).date().epochday();
    int startShadeEpoch;

    GraphicsContext gc = getGraphicsContext2D();
    gc.setFill( Colors.DISABLED_CELL );
    do
    {
      // find start of non-working period
      if ( !calendar.isWorking( date ) )
      {
        startShadeEpoch = date.epochday();

        // find end of non-working period
        do
          date.increment();
        while ( date.epochday() <= endEpoch && !calendar.isWorking( date ) );

        // if width at least 1 pixel shade non-working period
        long width = ( date.epochday() - startShadeEpoch ) * DateTime.MILLISECONDS_IN_DAY / m_gantt.getMsPP();
        if ( width > 0L )
        {
          long xe = m_gantt.x( new DateTime( date.epochday() * DateTime.MILLISECONDS_IN_DAY ) );
          gc.fillRect( xe - width, y, width, h );
        }
      }

      date.increment();
    }
    while ( date.epochday() <= endEpoch );
  }

}
