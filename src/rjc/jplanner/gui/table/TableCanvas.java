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
import rjc.jplanner.JPlanner;

/*************************************************************************************************/
/***************************** Displays data in gui scrollable table *****************************/
/*************************************************************************************************/

public class TableCanvas extends Canvas
{
  public static final Color COLOR_GRID            = Color.SILVER;
  public static final Color COLOR_DISABLED_CELL   = Color.rgb( 227, 227, 227 ); // medium grey
  public static final Color COLOR_NORMAL_CELL     = Color.WHITE;
  public static final Color COLOR_NORMAL_HEADER   = Color.rgb( 240, 240, 240 ); // light grey
  public static final Color COLOR_NORMAL_TEXT     = Color.BLACK;
  public static final Color COLOR_SELECTED_CELL   = Color.rgb( 51, 153, 255 );  // light blue;
  public static final Color COLOR_SELECTED_HEADER = Color.rgb( 192, 192, 192 ); // medium dark grey
  public static final Color COLOR_SELECTED_TEXT   = Color.WHITE;

  private Table             m_table;

  /***************************************** constructor *****************************************/
  public TableCanvas( Table table )
  {
    // setup table canvas
    super();
    m_table = table;

    // when size changes draw new bits
    widthProperty().addListener( ( observable, oldW, newW ) -> drawWidth( (double) oldW, (double) newW ) );
    heightProperty().addListener( ( observable, oldH, newH ) -> drawHeight( (double) oldH, (double) newH ) );

  }

  /****************************************** drawWidth ******************************************/
  private void drawWidth( double oldW, double newW )
  {
    JPlanner.trace( "WIDTH " + m_table.name + " " + oldW + "->" + newW );

    // draw any increase in width
    if ( oldW >= newW )
      return;

    // TODO Auto-generated method stub
    double w = getWidth();
    double h = getHeight();

    GraphicsContext gc = getGraphicsContext2D();
    gc.clearRect( 0, 0, w, h );

    gc.setStroke( Color.RED );
    gc.strokeLine( 0, 0, w, h );
    gc.strokeLine( 0, h, w, 0 );
  }

  /***************************************** drawHeight ******************************************/
  private void drawHeight( double oldH, double newH )
  {
    JPlanner.trace( "HEIGHT " + m_table.name + " " + oldH + "->" + newH );

    // draw any increase in width
    if ( oldH >= newH )
      return;

    // TODO Auto-generated method stub
    double w = getWidth();
    double h = getHeight();

    GraphicsContext gc = getGraphicsContext2D();
    gc.clearRect( 0, 0, w, h );

    gc.setStroke( Color.RED );
    gc.strokeLine( 0, 0, w, h );
    gc.strokeLine( 0, h, w, 0 );
  }

  /***************************************** isResizable *****************************************/
  @Override
  public boolean isResizable()
  {
    return true;
  }

  /****************************************** prefWidth ******************************************/
  @Override
  public double prefWidth( double height )
  {
    return 0.0;
  }

  /***************************************** prefHeight ******************************************/
  @Override
  public double prefHeight( double width )
  {
    return 0.0;
  }

}
