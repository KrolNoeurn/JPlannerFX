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

import javafx.geometry.Orientation;

/*************************************************************************************************/
/************************* Horizontal header that shows column titles ****************************/
/*************************************************************************************************/

public class Header extends CellGrid
{
  private Orientation m_orientation; // header orientation

  public static enum State
  {
    NORMAL, RESIZE, REORDER
  }

  State  state = State.NORMAL; // header state
  double pos;                  // location of mouse along header
  int    section;
  double sectionStart;
  double sectionEnd;

  /**************************************** constructor ******************************************/
  public Header( Table table, Orientation orientation )
  {
    // construct table horizontal header for column titles
    super( table );
    m_orientation = orientation;

    // add listeners to support resizing and reordering
    setOnMouseMoved( new HeaderMouseMoved( this ) );
    setOnMouseDragged( new HeaderDragDetected( this ) );
    setOnMouseReleased( new HeaderMouseReleased( this ) );
  }

  /***************************************** createCell ******************************************/
  @Override
  Cell createCell( int column, int row, int x, int y, int w, int h )
  {
    if ( m_orientation == Orientation.HORIZONTAL )
    {
      // horizontal header only has one row, so if row index not zero don't create cell
      if ( row != 0 )
        return null;

      // create horizontal header cell 
      String txt = m_table.getDataSource().getColumnTitle( column );
      h = (int) m_table.getHorizontalHeaderHeight();
      return new HeaderCell( txt, x, 0, w, h );
    }
    else
    {
      // vertical header only has one column, so if column index not zero don't create cell
      if ( column != 0 )
        return null;

      // create vertical header cell
      String txt = m_table.getDataSource().getRowTitle( row );
      w = (int) m_table.getVerticalHeaderWidth();
      return new HeaderCell( txt, 0, y, w, h );
    }

  }

  /****************************************** getTable *******************************************/
  public Table getTable()
  {
    return m_table;
  }

  /*************************************** getOrientation ****************************************/
  public Orientation getOrientation()
  {
    return m_orientation;
  }

  /***************************************** setSelected *****************************************/
  public void setSelected()
  {
    // ensure header selected sections are consistent with table body
    removeAllSelections();
    if ( m_orientation == Orientation.HORIZONTAL )
      setSelectedColumns( m_table.getBody().getSelectedColumns(), true );
    else
      setSelectedRows( m_table.getBody().getSelectedRows(), true );
  }

}
