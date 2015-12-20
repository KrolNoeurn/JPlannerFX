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
/***************************** Draws the table cell grid lines *****************************/
/*************************************************************************************************/

public class GridLines extends Canvas
{

  /**************************************** constructor ******************************************/
  public GridLines( Table table )
  {
    // construct default grid lines
    super();

    ITableDataSource data = table.getDataSource();
    double h = table.getCornerHeader().getHeight() + table.getCellsHeight();
    double w = table.getCornerHeader().getWidth() + table.getCellsWidth();
    setWidth( w );
    setHeight( h );

    GraphicsContext gc = getGraphicsContext2D();
    gc.setStroke( Color.SILVER );

    double x = table.getCornerHeader().getWidth() - 0.5;
    for ( int col = 0; col <= data.getColumnCount(); col++ )
    {
      gc.strokeLine( x, 0.0, x, h );
      x += table.getColumnWidth( col );
    }

    double y = table.getCornerHeader().getHeight() - 0.5;
    for ( int row = 0; row <= data.getRowCount(); row++ )
    {
      gc.strokeLine( 0.0, y, w, y );
      y += table.getRowHeight( row );
    }

  }

}
