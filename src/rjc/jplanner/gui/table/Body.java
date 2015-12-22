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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import rjc.jplanner.gui.table.Cell.Alignment;

/*************************************************************************************************/
/************************** Display area that shows table cell contents **************************/
/*************************************************************************************************/

public class Body extends Pane
{
  private Table m_table;

  /**************************************** constructor ******************************************/
  public Body( Table table )
  {
    // construct default table cells display
    super();
    m_table = table;

    // listener for visible area size changes
    ChangeListener<Number> listener = new ChangeListener<Number>()
    {
      @Override
      public void changed( ObservableValue<? extends Number> observable, Number oldValue, Number newValue )
      {
        updateCells();
      }
    };

    widthProperty().addListener( listener );
    heightProperty().addListener( listener );
  }

  /**************************************** updateCells ******************************************/
  private void updateCells()
  {
    // determine which columns & rows are visible
    int startColumn = m_table.getColumnAtX( 0.0 );
    int endColumn = m_table.getColumnAtX( getWidth() );
    int startRow = m_table.getRowAtY( 0.0 );
    int endRow = m_table.getRowAtY( getHeight() );

    // if width wider than table, limit to last column
    int last = m_table.getDataSource().getColumnCount() - 1;
    if ( endColumn > last )
      endColumn = last;

    // if height higher than table, limit to last row
    last = m_table.getDataSource().getRowCount() - 1;
    if ( endRow > last )
      endRow = last;

    // clear any old cells and re-generate new ones (TODO something more efficient)
    getChildren().clear();
    int y = m_table.getRowStartY( startRow );

    for ( int row = startRow; row <= endRow; row++ )
    {
      int height = m_table.getRowHeight( row );
      int x = m_table.getColumnStartX( startColumn );

      for ( int column = startColumn; column <= endColumn; column++ )
      {
        int width = m_table.getColumnWidth( column );
        String txt = m_table.getDataSource().getCellText( column, row );
        Alignment align = m_table.getDataSource().getCellAlignment( column, row );
        Paint color = m_table.getDataSource().getCellBackground( column, row );

        BodyCell hc = new BodyCell( txt, align, x, y, width, height, color );

        getChildren().add( hc );
        x += width;
      }

      y += height;
    }
  }

}
