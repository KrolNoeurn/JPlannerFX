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
import javafx.scene.input.MouseEvent;

/*************************************************************************************************/
/************************** Mouse drag detected handler for table bodies *************************/
/*************************************************************************************************/

public class BodyDragDetected implements EventHandler<MouseEvent>
{
  private Body m_body;

  /**************************************** constructor ******************************************/
  public BodyDragDetected( Body body )
  {
    // initialise private variables
    m_body = body;
  }

  /******************************************* handle ********************************************/
  @Override
  public void handle( MouseEvent event )
  {
    // handle user clicking on a body cell, if Alt down do nothing
    if ( event.isAltDown() )
      return;

    // if no focus cell, do nothing
    if ( m_body.getFocusColumn() < 0 || m_body.getFocusRow() < 0 )
      return;

    Table table = m_body.m_table;
    int column = table.getColumnAtX( event.getX() );
    int row = table.getRowAtY( event.getY() );

    // if control & shift not down, remove all previous selections
    if ( !event.isControlDown() && !event.isShiftDown() )
      m_body.removeAllSelections();

    // ensure column <= column2, swap if necessary
    int column2 = m_body.getFocusColumn();
    if ( column2 < column )
    {
      int temp = column;
      column = column2;
      column2 = temp;
    }

    // ensure row <= row2, swap if necessary
    int row2 = m_body.getFocusRow();
    if ( row2 < row )
    {
      int temp = row;
      row = row2;
      row2 = temp;
    }

    // select area
    for ( int c = column; c <= column2; c++ )
      for ( int r = row; r <= row2; r++ )
      {
        Cell cell = m_body.getCell( c, r );
        if ( cell != null )
          cell.setSelected( true );
      }

    // ensure headers selected sections are consistent with table body
    table.getVerticalHeader().setSelected();
    table.getHorizontalHeader().setSelected();
  }

}
