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
import javafx.scene.paint.Color;

/*************************************************************************************************/
/************************* Horizontal header that shows column titles ****************************/
/*************************************************************************************************/

public class VerticalHeader extends Canvas
{

  /**************************************** constructor ******************************************/
  public VerticalHeader()
  {
    // construct default table header-corner
    super();

    setWidth( Table.DEFAULT_VERTICAL_HEADER_WIDTH );
    setHeight( 200.0 );

    setLayoutX( 0.0 );
    setLayoutY( Table.DEFAULT_HORIZONTAL_HEADER_HEIGHT );

    GraphicsContext gc = getGraphicsContext2D();
    gc.setFill( Color.LIGHTGRAY );
    gc.fillRect( 0.0, 0.0, getWidth(), getHeight() );
  }

}
