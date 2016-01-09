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
    // as mouse moves along header ensure correct cursor is displayed
    int pos = m_header.getPos( event );
    int section = m_header.getSectionExact( pos );
    int start = m_header.getSectionStart( section );
    int size = m_header.getSectionSize( section );

    if ( section == Integer.MAX_VALUE )
      section = m_header.getSectionCount();

    // determine if to display normal cursor or resize cursor
    m_header.setCursor( Cursor.DEFAULT );
    if ( ( pos - start < PROXIMITY && section != 0 ) || start + size - pos < PROXIMITY )
      if ( m_header.getOrientation() == Orientation.HORIZONTAL )
        m_header.setCursor( Cursor.H_RESIZE );
      else
        m_header.setCursor( Cursor.V_RESIZE );

    // if cursor at beginning of section, then resize previous section
    if ( pos - start < PROXIMITY && section != 0 )
      section--;

    // update header tracking
    m_header.pos = pos;
    m_header.section = section;
  }

}
