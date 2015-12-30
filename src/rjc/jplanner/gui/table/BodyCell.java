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

import javafx.scene.paint.Paint;

/*************************************************************************************************/
/************************* Table cell that displays data source contents *************************/
/*************************************************************************************************/

public class BodyCell extends Cell
{
  private String    m_str;
  private Paint     m_color;
  private Alignment m_alignment;

  /**************************************** constructor ******************************************/
  public BodyCell( String str, Alignment alignment, int x, int y, int w, int h, Paint color )
  {
    // construct table display cell
    super( w, h, color );
    setLayoutX( x );
    setLayoutY( y );
    drawText( str, alignment );

    m_str = str;
    m_color = color;
    m_alignment = alignment;
  }

  /****************************************** redraw *********************************************/
  @Override
  void redraw()
  {
    // redraw body cell
    fill( m_color );
    drawText( m_str, m_alignment );
    drawGrid();
  }

}
