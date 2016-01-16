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

import javafx.scene.paint.Paint;
import rjc.jplanner.gui.table.Cell.Alignment;

/*************************************************************************************************/
/*********************** Display area that shows table body cell contents ************************/
/*************************************************************************************************/

public class Body extends CellGrid
{
  private int m_focusColumn = -1;
  private int m_focusRow    = -1;

  /**************************************** constructor ******************************************/
  public Body( Table table )
  {
    // construct default table cells display
    super( table );

    // add listeners to support cell selecting
    setOnMousePressed( new BodyMousePressed( this ) );
    setOnMouseDragged( new BodyDragDetected( this ) );
    setOnMouseClicked( new BodyMouseClicked( this ) );
  }

  /***************************************** createCell ******************************************/
  @Override
  Cell createCell( int column, int row, int x, int y, int w, int h )
  {
    // create body cell 
    String txt = getData().getCellText( column, row );
    Alignment align = getData().getCellAlignment( column, row );
    Paint color = getData().getCellBackground( column, row );

    return new BodyCell( txt, align, x, y, w, h, color );
  }

  /****************************************** setFocus *******************************************/
  public void setFocus( int column, int row )
  {
    m_focusColumn = column;
    m_focusRow = row;
  }

  /*************************************** getFocusColumn ****************************************/
  public int getFocusColumn()
  {
    return m_focusColumn;
  }

  /**************************************** getFocusRow ******************************************/
  public int getFocusRow()
  {
    return m_focusRow;
  }

}
