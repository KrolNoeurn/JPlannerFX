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
/*********************** Mouse button pressed event handler for table body ***********************/
/*************************************************************************************************/

public class BodyMousePressed implements EventHandler<MouseEvent>
{
  private Body m_body;

  /**************************************** constructor ******************************************/
  public BodyMousePressed( Body body )
  {
    // initialise private variables
    m_body = body;
  }

  /******************************************* handle ********************************************/
  @Override
  public void handle( MouseEvent event )
  {
    // handle user clicking on a body cell, do nothing if Alt down
    if ( event.isAltDown() )
      return;

    Table table = m_body.getTable();
    int column = table.getColumnExactAtX( event.getX() );
    if ( column < 0 || column == Integer.MAX_VALUE )
      return;

    int row = table.getRowExactAtY( event.getY() );
    if ( row < 0 || row == Integer.MAX_VALUE )
      return;

    // if control & shift not down, remove all previous selections
    if ( !event.isControlDown() && !event.isShiftDown() )
      m_body.removeAllSelections();

    // if shift down, select area, else select just individual cell
    if ( event.isShiftDown() && m_body.getFocusColumn() >= 0 )
    {
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
          m_body.getCell( c, r ).setSelected( true );
    }
    else
    {
      // toggle cell selection
      Cell cell = m_body.getCell( column, row );
      cell.setSelected( !cell.isSelected() );

      if ( cell.isSelected() )
        m_body.setFocus( column, row );
    }

    // ensure headers selected sections are consistent with table body
    table.getVerticalHeader().setSelected();
    table.getHorizontalHeader().setSelected();
  }

}
