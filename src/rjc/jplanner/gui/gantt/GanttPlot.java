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
import rjc.jplanner.model.DateTime;

/*************************************************************************************************/
/***************** GanttPlot provides a view of the plan tasks and dependencies ******************/
/*************************************************************************************************/

public class GanttPlot extends Canvas
{
  private DateTime      m_start;
  private long          m_millisecondsPP;

  private static int    m_taskHeight = 6;
  private static int    m_arrowSize  = 4;

  public static boolean ganttStretch;

  /**************************************** constructor ******************************************/
  public GanttPlot()
  {
    // construct gantt-plot
    widthProperty().addListener( ( observable, oldW, newW ) -> drawWidth( oldW.intValue(), newW.intValue() ) );
    heightProperty().addListener( ( observable, oldH, newH ) -> drawHeight( oldH.intValue(), newH.intValue() ) );
  }

  /****************************************** setStart *******************************************/
  public void setStart( DateTime start )
  {
    // set gantt-plot start date-time
    m_start = start;
  }

  /****************************************** setMsPP ********************************************/
  public void setMsPP( long mspp )
  {
    // set gantt-plot milliseconds per pixel
    m_millisecondsPP = mspp;
  }

  /****************************************** drawWidth ******************************************/
  private void drawWidth( int oldW, int newW )
  {
    // draw only if increase in width
    if ( getHeight() <= 0.0 || newW <= oldW )
      return;

    // TODO ...............
    GraphicsContext gc = getGraphicsContext2D();
    gc.setFill( Color.rgb( hashCode() % 256, hashCode() / 256 % 256, hashCode() / 65536 % 256 ) );
    gc.fillRect( oldW, 0.0, newW - oldW, getHeight() );
  }

  /****************************************** drawHeight *****************************************/
  private void drawHeight( int oldH, int newH )
  {
    JPlanner.trace( "PLOT", this, oldH, newH, getHeight() );

    // draw only if increase in height
    if ( getWidth() <= 0.0 || newH <= oldH )
      return;

    // TODO ...............
    GraphicsContext gc = getGraphicsContext2D();
    gc.setFill( Color.rgb( hashCode() % 256, hashCode() / 256 % 256, hashCode() / 65536 % 256 ) );
    gc.fillRect( 0.0, oldH, getWidth(), newH - oldH );
  }

}
