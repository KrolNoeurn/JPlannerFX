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

import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.ContextMenuEvent;
import rjc.jplanner.JPlanner;
import rjc.jplanner.gui.table.AbstractCellEditor;
import rjc.jplanner.model.DateTime;

/*************************************************************************************************/
/********************************* Context menu for Gantt plots **********************************/
/*************************************************************************************************/

class GanttPlotMenu extends ContextMenu
{
  private static GanttPlotMenu m_menu;
  private static int           m_x;

  /**************************************** constructor ******************************************/
  private GanttPlotMenu()
  {
    // create the gantt context menu

    // zoom in
    MenuItem zoomIn = new MenuItem( "Zoom in" );
    zoomIn.setOnAction( event -> zoomIn( event ) );

    // zoom out
    MenuItem zoomOut = new MenuItem( "Zoom out" );
    zoomOut.setOnAction( event -> zoomOut( event ) );

    // zoom fit
    MenuItem zoomFit = new MenuItem( "Zoom fit" );
    zoomFit.setOnAction( event -> zoomFit() );

    // stretch tasks
    MenuItem stretch = new MenuItem( "Stretch tasks" );
    stretch.setOnAction( event -> JPlanner.trace( "NOT IMPLEMENTED" ) );

    // shade non-working tasks
    MenuItem nonWorking = new MenuItem( "Non-working days" );
    nonWorking.setOnAction( event -> JPlanner.trace( "NOT IMPLEMENTED" ) );

    // current date mark
    MenuItem currentData = new MenuItem( "Current date" );
    currentData.setOnAction( event -> JPlanner.trace( "NOT IMPLEMENTED" ) );

    getItems().addAll( zoomIn, zoomOut, zoomFit, new SeparatorMenuItem(), stretch, nonWorking, currentData );
  }

  /******************************************** open *********************************************/
  public static void open( GanttPlot gantt, ContextMenuEvent event )
  {
    // show the gantt plot context menu
    if ( m_menu == null )
      m_menu = new GanttPlotMenu();

    AbstractCellEditor.endEditing();
    m_x = (int) event.getX();
    m_menu.show( gantt, event.getScreenX(), event.getScreenY() );
  }

  /******************************************* zoomFit *******************************************/
  private void zoomFit()
  {
    // determine plan start
    DateTime start = JPlanner.plan.getEarliestTaskStart();
    if ( start == null )
      start = JPlanner.plan.getStart();

    // if possible buffer of 1% of width plus 10 pixels given at each end of gantt
    Gantt gantt = ( (GanttPlot) this.getOwnerNode() ).getGantt();
    double width = gantt.getWidth();
    double buffer = width / 100.0 + 10.0;
    if ( width < 4.0 * buffer )
      buffer = width / 4.0;
    JPlanner.trace( "GANTT WIDTH = " + width, buffer );

    // determine plan end and suitable milliseconds-per-pixel
    DateTime end = JPlanner.plan.getLatestTaskEnd();
    long mspp = 3600 * 3000;
    if ( end == null || end == start )
      end = start;
    else
    {
      long ms = end.getMilliseconds() - start.getMilliseconds();
      mspp = (long) ( ms / ( width - buffer - buffer ) );
    }

    // update gantt and redraw
    gantt.setMsPP( mspp );
    gantt.setStart( start.plusMilliseconds( (long) ( -10 * mspp ) ) );
    gantt.setEnd( end.plusMilliseconds( (long) ( buffer * mspp ) ) );
    gantt.redraw();
  }

  /******************************************* zoomIn ********************************************/
  private void zoomIn( ActionEvent event )
  {
    JPlanner.trace( "NOT IMPLEMENTED", event );

    Gantt gantt = ( (GanttPlot) this.getOwnerNode() ).getGantt();
    DateTime dt = gantt.datetime( m_x );
    gantt.setMsPP( ( gantt.getMsPP() * 2L ) / 3L );
    gantt.centre( dt );
    //gantt.redraw();

    // TODO zoom in centred around mouse x
  }

  /******************************************* zoomOut ********************************************/
  private void zoomOut( ActionEvent event )
  {
    JPlanner.trace( "NOT IMPLEMENTED", event );
  }

}
