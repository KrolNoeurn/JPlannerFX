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
import javafx.scene.input.MouseEvent;
import rjc.jplanner.JPlanner;
import rjc.jplanner.gui.table.Header.State;

/*************************************************************************************************/
/************************* Mouse drag detected handler for table headers *************************/
/*************************************************************************************************/

public class HeaderDragDetected implements EventHandler<MouseEvent>
{
  private Header m_header;
  private int    m_oldSize;

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
    // determine if re-order or re-size started
    if ( m_header.state == State.NORMAL && m_header.getCursor() == Cursor.DEFAULT )
    {
      // start reorder
      m_header.state = State.REORDER;
      JPlanner.trace( "REORDER starting on section=" + m_header.section );

    }
    else if ( m_header.state == State.NORMAL )
    {
      // resize
      m_header.state = State.RESIZE;

      // ensure correct section is to be resized
      if ( m_header.section == Integer.MAX_VALUE )
      {
        m_header.section = m_header.getTable().getDataSource().getRowCount() - 1;
        if ( m_header.getOrientation() == Orientation.HORIZONTAL )
          m_header.section = m_header.getTable().getDataSource().getColumnCount() - 1;
      }
      else
      {
        if ( m_header.section != 0 && m_header.pos - m_header.sectionStart < HeaderMouseMoved.PROXIMITY )
          m_header.section--;
      }

      m_oldSize = 100;

      JPlanner.trace( "RESIZE starting on section=" + m_header.section );
    }

    // action re-size
    if ( m_header.state == State.RESIZE )
    {
      if ( m_header.getOrientation() == Orientation.HORIZONTAL )
      {
        int width = (int) ( m_oldSize - m_header.pos + event.getX() );
        m_header.getTable().setColumnWidth( m_header.section, width );
      }
      else
      {
        int height = (int) ( m_oldSize - m_header.pos + event.getY() );
        m_header.getTable().setRowHeight( m_header.section, height );
      }
    }

    // action re-order
    if ( m_header.state == State.REORDER )
    {
      // TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }
  }

}
