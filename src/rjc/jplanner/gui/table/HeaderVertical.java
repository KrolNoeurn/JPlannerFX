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

/*************************************************************************************************/
/*************************** Vertical header that shows row titles *******************************/
/*************************************************************************************************/

public class HeaderVertical extends Pane
{
  private Table m_table;

  /**************************************** constructor ******************************************/
  public HeaderVertical( Table table )
  {
    // construct default table header-corner
    super();
    m_table = table;

    // listener for header size changes
    heightProperty().addListener( new ChangeListener<Number>()
    {
      @Override
      public void changed( ObservableValue<? extends Number> observable, Number oldValue, Number newValue )
      {
        updateHeader();
      }
    } );
  }

  /*************************************** updateHeader ******************************************/
  private void updateHeader()
  {
    // determine which rows are visible
    int startRow = m_table.getRowAtY( 0.0 );
    int endRow = m_table.getRowAtY( getHeight() );

    // if height higher than table, limit to last row
    int last = m_table.getDataSource().getRowCount() - 1;
    if ( endRow > last )
      endRow = last;

    // clear any old header cells and re-generate new ones (TODO something more efficient)
    getChildren().clear();
    int y = m_table.getRowStartY( startRow );
    int width = (int) m_table.getCornerHeader().getWidth();

    for ( int row = startRow; row <= endRow; row++ )
    {
      int height = m_table.getRowHeight( row );
      String txt = m_table.getDataSource().getRowTitle( row );
      HeaderCell hc = new HeaderCell( txt, 0, y, width, height );

      getChildren().add( hc );
      y += height;
    }
  }

}
