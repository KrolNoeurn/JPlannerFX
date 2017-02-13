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

package rjc.jplanner.gui.plan;

import javafx.geometry.Bounds;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import rjc.jplanner.JPlanner;

/*************************************************************************************************/
/***************** Pane to display number of tasks/resources/calendars/day-types *****************/
/*************************************************************************************************/

public class NumberOf extends Pane
{
  private Canvas m_canvas = new Canvas(); // canvas to display group box and contents

  private double m_width;                 // width in pixels of string "Number of"
  private double m_height;                // height in pixels of each row displayed
  private double m_inset;                 // inset in pixels of grouping border from top edge
  private double m_numbers;               // indent in pixels of displayed numbers from right edge

  /**************************************** constructor ******************************************/
  public NumberOf()
  {
    // create pane to display number of tasks/resources/calendars/day-types
    Bounds bounds = new Text( "Number of" ).getLayoutBounds();
    m_width = Math.ceil( bounds.getWidth() );
    m_height = Math.ceil( bounds.getHeight() * 1.2 );
    m_inset = Math.floor( bounds.getHeight() / 2 ) + 0.5;
    m_numbers = new Text( "Resources  " ).getLayoutBounds().getWidth() + m_inset;

    // set minimum height of pane based on group label plus four displayed rows
    setMinHeight( m_height * 5.2 );

    // every time pane changes size redraw the canvas
    heightProperty().addListener( ( observable, oldValue, newValue ) -> redraw() );
    widthProperty().addListener( ( observable, oldValue, newValue ) -> redraw() );

    // add canvas to pane children so it is displayed
    getChildren().add( m_canvas );
  }

  /**************************************** redraw ******************************************/
  public void redraw()
  {
    // make canvas same size as pane and redraw
    GraphicsContext gc = m_canvas.getGraphicsContext2D();
    double w = getWidth();
    double h = getHeight();
    m_canvas.setHeight( h );
    m_canvas.setWidth( w );
    gc.clearRect( 0.0, 0.0, w, h );

    // draw group box rectangle
    gc.setStroke( Color.LIGHTGRAY );
    gc.strokeRect( 0.5, m_inset, w - 1.0, h - m_inset - 0.5 );

    // draw group box label
    gc.clearRect( m_inset * 0.9, 0.0, m_width + m_inset * 0.4, m_height );
    gc.setFill( Color.BLACK );
    gc.setTextBaseline( VPos.CENTER );
    gc.fillText( "Number of", m_inset, m_inset );

    // draw labels and number of
    double y = m_inset + m_height;
    gc.fillText( "Tasks", m_inset, y );
    gc.fillText( ": " + JPlanner.plan.getTasksNotNullCount(), m_numbers, y );
    y += m_height;
    gc.fillText( "Recources", m_inset, y );
    gc.fillText( ": " + JPlanner.plan.getResourcesNotNullCount(), m_numbers, y );
    y += m_height;
    gc.fillText( "Calendars", m_inset, y );
    gc.fillText( ": " + JPlanner.plan.getCalendarsCount(), m_numbers, y );
    y += m_height;
    gc.fillText( "Days", m_inset, y );
    gc.fillText( ": " + JPlanner.plan.getDaysCount(), m_numbers, y );
  }

}
