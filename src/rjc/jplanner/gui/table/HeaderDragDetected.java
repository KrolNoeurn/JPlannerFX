/**************************************************************************
 *  Copyright (C) 2015 by Richard Crook                                   *
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

package rjc.jplanner.gui.table;

import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import rjc.jplanner.JPlanner;
import rjc.jplanner.gui.table.Cell.Alignment;
import rjc.jplanner.gui.table.Header.State;

/*************************************************************************************************/
/************************* Mouse drag detected handler for table headers *************************/
/*************************************************************************************************/

public class HeaderDragDetected implements EventHandler<MouseEvent>
{
  private Header m_header;
  private int    m_offset;

  /**************************************** constructor ******************************************/
  public HeaderDragDetected( Header header )
  {
    // initialise private variables
    m_header = header;
  }

  /******************************************* handle ********************************************/
  @Override
  public void handle( MouseEvent event )
  {
    // determine if starting resize or reorder
    if ( m_header.state == State.NORMAL )
      if ( m_header.getCursor() == Cursor.DEFAULT )
      {
        // start reorder
        m_header.state = State.REORDER;
        JPlanner.trace( "REORDER starting on section=" + m_header.section );

        createReorderSlider();
        createReorderPointer();
        if ( m_header.getOrientation() == Orientation.HORIZONTAL )
          m_offset = (int) ( m_header.getTable().getVerticalHeaderWidth() - m_header.pointer.getWidth() / 2 );
        else
          m_offset = (int) ( m_header.getTable().getHorizontalHeaderHeight() - m_header.pointer.getHeight() / 2 );
      }
      else
      {
        // start resize
        m_header.state = State.RESIZE;
        m_offset = m_header.pos - m_header.getSectionSize( m_header.section );
      }

    // action resize
    if ( m_header.state == State.RESIZE )
      if ( m_header.getOrientation() == Orientation.HORIZONTAL )
        m_header.getTable().setColumnWidth( m_header.section, (int) event.getX() - m_offset );
      else
        m_header.getTable().setRowHeight( m_header.section, (int) event.getY() - m_offset );

    // action reorder
    if ( m_header.state == State.REORDER )
    {
      // get mouse position related data
      int pos = m_header.getPos( event );
      int section = m_header.getSection( pos );
      int start = m_header.getSectionStart( section );
      int size = m_header.getSectionSize( section );

      // would this result in a move
      boolean noMove = section == m_header.section;
      if ( section == m_header.section + 1 && pos < start + size / 2 )
        noMove = true;
      if ( section == m_header.section - 1 && pos > start + size / 2 )
        noMove = true;

      // determine position of pointer
      if ( noMove )
        start = m_header.getSectionStart( m_header.section ) + m_header.getSectionSize( m_header.section ) / 2;
      else
      {
        if ( pos >= start + size / 2 )
          section++;
        start = m_header.getSectionStart( section );
      }

      // update position of sliding cell and pointer
      if ( m_header.getOrientation() == Orientation.HORIZONTAL )
      {
        m_header.slider.setLayoutX( pos );
        m_header.pointer.setTranslateX( start + m_offset );
      }
      else
      {
        m_header.slider.setLayoutY( pos );
        m_header.pointer.setTranslateY( start + m_offset );
      }
    }

  }

  /************************************* createReorderSlider *************************************/
  private void createReorderPointer()
  {
    // create reorder pointer
    Canvas pointer = new Canvas( 20, 20 );
    GraphicsContext gc = pointer.getGraphicsContext2D();
    gc.setLineWidth( 3.0 );
    gc.setStroke( Color.RED );
    gc.setLineCap( StrokeLineCap.ROUND );

    if ( m_header.getOrientation() == Orientation.HORIZONTAL )
    {
      gc.strokeLine( 9.5, 17.0, 9.5, 3.0 );
      gc.strokeLine( 4.5, 10.0, 9.5, 3.0 );
      gc.strokeLine( 14.5, 10.0, 9.5, 3.0 );
      pointer.setTranslateY( m_header.getTable().getHorizontalHeaderHeight() );
    }
    else
    {
      gc.strokeLine( 3.0, 9.5, 17.0, 9.5 );
      gc.strokeLine( 3.0, 9.5, 10.0, 4.5 );
      gc.strokeLine( 3.0, 9.5, 10.0, 14.5 );
      pointer.setTranslateX( m_header.getTable().getVerticalHeaderWidth() );
    }

    m_header.m_table.getChildren().add( pointer );
    m_header.pointer = pointer;
  }

  /************************************* createReorderSlider *************************************/
  private void createReorderSlider()
  {
    // create reorder slider
    Table table = m_header.getTable();
    int section = m_header.section;
    String txt;
    int h = table.getHorizontalHeaderHeight();
    int w = table.getVerticalHeaderWidth();

    // get label and correct size
    if ( m_header.getOrientation() == Orientation.HORIZONTAL )
    {
      txt = table.getDataSource().getColumnTitle( section );
      w = table.getColumnWidth( section );
    }
    else
    {
      txt = table.getDataSource().getRowTitle( section );
      h = table.getRowHeight( section );
    }

    // create slider and add to header
    m_header.slider = new BodyCell( txt, Alignment.MIDDLE, 0, 0, w, h, Color.rgb( 100, 100, 100, 0.5 ) );
    m_header.getChildren().add( m_header.slider );

    // adjust slider position based on where user clicked
    if ( m_header.getOrientation() == Orientation.HORIZONTAL )
    {
      int start = table.getColumnStartX( section );
      m_header.slider.setTranslateX( start - m_header.pos );
    }
    else
    {
      int start = table.getRowStartY( section );
      m_header.slider.setTranslateY( start - m_header.pos );
    }

  }

}
