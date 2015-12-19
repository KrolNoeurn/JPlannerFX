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

import javafx.scene.Group;

/*************************************************************************************************/
/***************************** Displays data in gui scrollable table *****************************/
/*************************************************************************************************/

public class Table extends Group
{
  public static final double DEFAULT_HORIZONTAL_HEADER_HEIGHT = 30;
  public static final double DEFAULT_VERTICAL_HEADER_WIDTH    = 40;

  /**************************************** constructor ******************************************/
  public Table( ITableDataSource source )
  {
    // construct table elements
    super();
    getChildren().add( new HeaderCorner() );
    getChildren().add( new HorizontalHeader() );
    getChildren().add( new VerticalHeader() );
    getChildren().add( new TableCells() );
  }

}
