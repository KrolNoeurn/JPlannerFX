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

import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;

/*************************************************************************************************/
/*************************** Scroll bar for tables that self-manage ******************************/
/*************************************************************************************************/

public class TableScrollBar extends ScrollBar
{

  /**************************************** constructor ******************************************/
  public TableScrollBar( Table table, Orientation orientation )
  {
    // TODO Auto-generated constructor stub
    super();

    setOrientation( orientation );
    setMinWidth( 16 );

    // by default scroll bars are hidden
    //setVisible( false );
    //setManaged( false );
  }

}
