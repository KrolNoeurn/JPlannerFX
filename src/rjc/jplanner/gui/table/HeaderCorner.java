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

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

/*************************************************************************************************/
/************ Header corner that sits in corner between vertical & horizontal headers ************/
/*************************************************************************************************/

public class HeaderCorner extends Canvas
{

  /**************************************** constructor ******************************************/
  public HeaderCorner( Table table )
  {
    // construct default table header corner
    super();

    // default size
    setHeight( Table.DEFAULT_HORIZONTAL_HEADER_HEIGHT );
    setWidth( Table.DEFAULT_VERTICAL_HEADER_WIDTH );

    // background colour
    GraphicsContext gc = getGraphicsContext2D();
    gc.setFill( Table.COLOR_HEADER_FILL );
    gc.fillRect( 0, 0, getWidth() - 1.0, getHeight() - 1.0 );

    // grid lines
    gc.setStroke( Table.COLOR_GRID );
    gc.strokeLine( getWidth() - 0.5, 0, getWidth() - 0.5, getHeight() );
    gc.strokeLine( 0, getHeight() - 0.5, getWidth(), getHeight() - 0.5 );
  }

}
