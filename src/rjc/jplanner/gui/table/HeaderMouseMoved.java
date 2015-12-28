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

/*************************************************************************************************/
/************************** Mouse moved event handler for table headers **************************/
/*************************************************************************************************/

public class HeaderMouseMoved implements EventHandler<MouseEvent>
{
  public static final double PROXIMITY = 4.0;

  private Header             m_header;

  /**************************************** constructor ******************************************/
  public HeaderMouseMoved( Header header )
  {
    // initialise private variables
    m_header = header;
  }

  /******************************************* handle ********************************************/
  @Override
  public void handle( MouseEvent event )
  {
    // depending on orientation interested in x or y
    Table table = m_header.getTable();
    Orientation orientation = m_header.getOrientation();
    if ( orientation == Orientation.HORIZONTAL )
      m_header.pos = event.getX();
    else
      m_header.pos = event.getY();

    // if mouse moved outside previous section, get new section details
    if ( m_header.pos < m_header.sectionStart || m_header.pos > m_header.sectionEnd )
    {
      if ( orientation == Orientation.HORIZONTAL )
      {
        m_header.section = table.getColumnExactAtX( m_header.pos );
        m_header.sectionStart = table.getColumnStartX( m_header.section );
      }
      else
      {
        m_header.section = table.getRowExactAtY( m_header.pos );
        m_header.sectionStart = table.getRowStartY( m_header.section );
      }

      if ( m_header.section == Integer.MAX_VALUE )
        m_header.sectionEnd = Integer.MAX_VALUE;
      else if ( orientation == Orientation.HORIZONTAL )
        m_header.sectionEnd = m_header.sectionStart + table.getColumnWidth( m_header.section );
      else
        m_header.sectionEnd = m_header.sectionStart + table.getRowHeight( m_header.section );
    }

    // change mouse cursor for resize if near edge of section
    if ( m_header.getCursor() == Cursor.DEFAULT )
    {
      if ( ( m_header.section != 0 && m_header.pos - m_header.sectionStart < PROXIMITY )
          || m_header.sectionEnd - m_header.pos < PROXIMITY )
        if ( orientation == Orientation.HORIZONTAL )
          m_header.setCursor( Cursor.H_RESIZE );
        else
          m_header.setCursor( Cursor.V_RESIZE );
    }
    else
    {
      if ( m_header.pos - m_header.sectionStart >= PROXIMITY && m_header.sectionEnd - m_header.pos >= PROXIMITY )
        m_header.setCursor( Cursor.DEFAULT );
    }
  }

}
