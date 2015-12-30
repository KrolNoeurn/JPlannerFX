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

/*************************************************************************************************/
/************ Header corner that sits in corner between vertical & horizontal headers ************/
/*************************************************************************************************/

public class HeaderCorner extends Cell
{

  /**************************************** constructor ******************************************/
  public HeaderCorner( Table table )
  {
    // construct default table header corner
    super( table.getVerticalHeaderWidth(), table.getHorizontalHeaderHeight(), Table.COLOR_HEADER_FILL );
  }

  /****************************************** redraw *********************************************/
  public void redraw()
  {
    // redraw corner
    fill( Table.COLOR_HEADER_FILL );
    drawGrid();
  }

}
