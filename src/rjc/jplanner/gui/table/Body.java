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
  /**************************************** constructor ******************************************/
  public Body( Table table )
  {
    // construct default table cells display
    super( table );
  }

  /***************************************** createCell ******************************************/
  @Override
  Cell createCell( int column, int row, int x, int y, int w, int h )
  {
    // create body cell 
    String txt = m_table.getDataSource().getCellText( column, row );
    Alignment align = m_table.getDataSource().getCellAlignment( column, row );
    Paint color = m_table.getDataSource().getCellBackground( column, row );

    return new BodyCell( txt, align, x, y, w, h, color );
  }

}
