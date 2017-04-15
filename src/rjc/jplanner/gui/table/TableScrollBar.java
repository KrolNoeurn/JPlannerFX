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

package rjc.jplanner.gui.table;

import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;

/*************************************************************************************************/
/*************** Extended version of ScrollBar with special increment & decrement ****************/
/*************************************************************************************************/

class TableScrollBar extends ScrollBar
{
  private Table m_table; // table definition

  /**************************************** constructor ******************************************/
  public TableScrollBar( Table table )
  {
    // create scroll bar and record table
    m_table = table;
  }

  /****************************************** increment ******************************************/
  @Override
  public void increment()
  {
    // increase scroll bar value to next table cell boundary
    if ( getOrientation() == Orientation.HORIZONTAL )
    {
      int columnPos = m_table.getColumnPositionAtX( m_table.getVerticalHeaderWidth() );
      int newx = m_table.getXStartByColumnPosition( columnPos );
      while ( newx <= m_table.getVerticalHeaderWidth() )
        newx += m_table.getWidthByColumnPosition( columnPos++ );

      double value = getValue() + newx - m_table.getVerticalHeaderWidth();
      m_table.animate( valueProperty(), (int) ( value < getMax() ? value : getMax() ), 100 );
    }
    else
    {
      int row = m_table.getRowAtY( m_table.getHorizontalHeaderHeight() );
      int newy = m_table.getYStartByRow( row );
      while ( newy <= m_table.getHorizontalHeaderHeight() )
        newy += m_table.getRowHeight( row++ );

      double value = getValue() + newy - m_table.getHorizontalHeaderHeight();
      m_table.animate( valueProperty(), (int) ( value < getMax() ? value : getMax() ), 100 );
    }
  }

  /****************************************** decrement ******************************************/
  @Override
  public void decrement()
  {
    // decrease scroll bar value to next table cell boundary
    if ( getOrientation() == Orientation.HORIZONTAL )
    {
      int columnPos = m_table.getColumnPositionAtX( m_table.getVerticalHeaderWidth() - 1 );
      int newx = m_table.getXStartByColumnPosition( columnPos );

      double value = getValue() + newx - m_table.getVerticalHeaderWidth();
      m_table.animate( valueProperty(), (int) ( value > 0.0 ? value : 0 ), 100 );
    }
    else
    {
      int row = m_table.getRowAtY( m_table.getHorizontalHeaderHeight() - 1 );
      int newy = m_table.getYStartByRow( row );

      double value = getValue() + newy - m_table.getHorizontalHeaderHeight();
      m_table.animate( valueProperty(), (int) ( value > 0.0 ? value : 0 ), 100 );
    }
  }

  /****************************************** toString *******************************************/
  @Override
  public String toString()
  {
    return "VAL=" + getValue() + " MIN=" + getMin() + " MAX=" + getMax() + " VIS=" + getVisibleAmount() + " BLK"
        + getBlockIncrement() + " UNIT" + getUnitIncrement() + " ORIENT=" + getOrientation();
  }

}
