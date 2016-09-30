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

package rjc.jplanner.gui;

import javafx.scene.paint.Color;

/*************************************************************************************************/
/************************* Holds the JavaFX colours used in JPlanner GUI *************************/
/*************************************************************************************************/

public class Colors
{
  public static final Color LIGHTERGRAY        = Color.rgb( 225, 225, 225 );
  public static final Color BUTTON_BACKGROUND  = LIGHTERGRAY;
  public static final Color BUTTON_ARROW       = Color.BLACK;
  public static final Color GENERAL_BACKGROUND = Color.rgb( 240, 240, 240 );

  public static final Color NORMAL_CELL        = Color.WHITE;
  public static final Color SELECTED_CELL      = Color.rgb( 51, 153, 255 );  // light blue;
  public static final Color DISABLED_CELL      = Color.rgb( 227, 227, 227 ); // medium grey

  public static final Color NORMAL_TEXT        = Color.BLACK;
  public static final Color SELECTED_TEXT      = Color.WHITE;

  public static final Color TABLE_GRID         = Color.SILVER;
  public static final Color NORMAL_HEADER      = GENERAL_BACKGROUND;
  public static final Color SELECTED_HEADER    = Color.rgb( 192, 192, 192 ); // medium dark grey
}
