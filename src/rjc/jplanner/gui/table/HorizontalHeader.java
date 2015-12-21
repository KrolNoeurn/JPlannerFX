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
/************************* Horizontal header that shows column titles ****************************/
/*************************************************************************************************/

public class HorizontalHeader extends Pane
{
  private Table m_table;

  /**************************************** constructor ******************************************/
  public HorizontalHeader( Table table )
  {
    // construct default table horizontal header for column titles
    super();
    m_table = table;

    // listener for header size changes
    widthProperty().addListener( new ChangeListener<Number>()
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
    // determine which columns are visible
    int startColumn = m_table.getColumnAtX( 0.0 );
    int endColumn = m_table.getColumnAtX( getWidth() );

    // if width wider than table, limit to last column
    int last = m_table.getDataSource().getColumnCount() - 1;
    if ( endColumn > last )
      endColumn = last;

    // clear any old header cells and re-generate new ones (TODO something more efficient)
    getChildren().clear();
    int x = m_table.getColumnStartX( startColumn );
    int height = (int) m_table.getCornerHeader().getHeight();

    for ( int column = startColumn; column <= endColumn; column++ )
    {
      int width = m_table.getColumnWidth( column );
      String txt = m_table.getDataSource().getColumnTitle( column );
      HeaderCell hc = new HeaderCell( txt, x, 0, width, height );

      getChildren().add( hc );
      x += width;
    }
  }

}
