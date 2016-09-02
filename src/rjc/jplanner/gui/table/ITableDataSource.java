/**************************************************************************
 *  Copyright (C) 2016 by Richard Crook                                   *
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
import javafx.scene.text.Font;
import rjc.jplanner.gui.table.Table.Alignment;

/*************************************************************************************************/
/******************************** Interface for table data source ********************************/
/*************************************************************************************************/

public interface ITableDataSource
{
  // return number of columns to be displayed in table
  int getColumnCount();

  // return number of rows to be displayed in table
  int getRowCount();

  // return column title for specified column index
  String getColumnTitle( int columnIndex );

  // return row title for specified row index
  String getRowTitle( int rowIndex );

  // return editor to use for specified cell index
  AbstractCellEditor getEditor( int columnIndex, int rowIndex );

  // return cell value for specified cell index
  Object getValue( int columnIndex, int rowIndex );

  // set cell value for specified cell index
  void setValue( int columnIndex, int rowIndex, Object newValue );

  // return cell contents alignment for specified cell index
  Alignment getCellAlignment( int columnIndex, int rowIndex );

  // return cell background colour for specified cell index
  Paint getCellBackground( int columnIndex, int rowIndex );

  // return cell display text for specified cell index
  String getCellText( int columnIndex, int rowIndex );

  // return cell display font for specified cell index
  Font getCellFont( int columnIndex, int rowIndex );
}
