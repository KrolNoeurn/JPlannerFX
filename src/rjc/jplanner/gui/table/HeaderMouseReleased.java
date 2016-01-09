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

import java.util.HashSet;

import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import rjc.jplanner.gui.table.Header.State;

/*************************************************************************************************/
/************************** Mouse released handler for table headers *****************************/
/*************************************************************************************************/

public class HeaderMouseReleased implements EventHandler<MouseEvent>
{
  private Header m_header;

  /**************************************** constructor ******************************************/
  public HeaderMouseReleased( Header header )
  {
    // initialise private variables
    m_header = header;
  }

  /******************************************* handle ********************************************/
  @Override
  public void handle( MouseEvent event )
  {
    // any resize or reorder now finished, so set header state back to normal
    m_header.state = State.NORMAL;
    if ( m_header.slider != null )
    {
      // end reorder
      m_header.getChildren().remove( m_header.slider );
      m_header.m_table.getChildren().remove( m_header.pointer );
      return;
    }

    // check if user selecting whole column/row
    if ( m_header.getCursor() == Cursor.DEFAULT )
    {
      Body body = m_header.m_table.getBody();
      if ( !event.isControlDown() )
        body.removeAllSelections();

      // set of selected columns/rows
      HashSet<Integer> sections = new HashSet<Integer>();
      sections.add( m_header.section );

      int focusSection = body.getFocusColumn();
      if ( m_header.getOrientation() == Orientation.VERTICAL )
        focusSection = body.getFocusRow();

      // if shift, then multiple columns/rows
      if ( event.isShiftDown() && focusSection >= 0 )
      {
        int section1 = m_header.section;
        int section2 = focusSection;
        if ( section2 < section1 )
        {
          int temp = section1;
          section1 = section2;
          section2 = temp;
        }

        for ( int section = section1; section <= section2; section++ )
          sections.add( section );
      }

      if ( m_header.getOrientation() == Orientation.HORIZONTAL )
      {
        body.setSelectedColumns( sections, true );
        body.setFocus( m_header.section, -1 );
      }
      else
      {
        body.setSelectedRows( sections, true );
        body.setFocus( -1, m_header.section );
      }
    }

    // ensure headers selected sections are consistent with table body
    m_header.m_table.getVerticalHeader().setSelected();
    m_header.m_table.getHorizontalHeader().setSelected();
  }

}
