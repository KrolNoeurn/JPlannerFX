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
import rjc.jplanner.gui.table.Table.Alignment;

/*************************************************************************************************/
/******************************** Interface for table data source ********************************/
/*************************************************************************************************/

public interface ITableDataSource
{
  // return number of columns to be displayed
  int getColumnCount();

  // return number of rows to be displayed
  int getRowCount();

  // return column title to be displayed
  String getColumnTitle( int columnIndex );

  // return row title to be displayed
  String getRowTitle( int rowIndex );

  // return editor
  CellEditor getEditor( int columnIndex, int rowIndex );

  // return cell text to be displayed
  String getCellText( int columnIndex, int rowIndex );

  // return cell value
  Object getValue( int columnIndex, int rowIndex );

  // set cell value
  void setValue( int columnIndex, int rowIndex, Object newValue );

  // return cell text alignment
  Alignment getCellAlignment( int columnIndex, int rowIndex );

  // return cell background colour
  Paint getCellBackground( int columnIndex, int rowIndex );
}
