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

package rjc.jplanner.gui.plan;

import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import javafx.stage.Popup;
import rjc.jplanner.JPlanner;

/*************************************************************************************************/
/********************* Pop-up window to display AbstractCombo drop-down list *********************/
/*************************************************************************************************/

public class Dropdown extends Popup
{
  private Canvas     m_canvas       = new Canvas();
  private ScrollBar  m_scrollbar    = new ScrollBar();

  private static int SCROLLBAR_SIZE = 18;

  /**************************************** constructor ******************************************/
  public Dropdown( AbstractCombo parent )
  {
    // create pop-up window to display drop-down list
    super();
    JPlanner.trace( "Creating Dropdown !!!" );

    // setup scroll bar
    m_scrollbar.setOrientation( Orientation.VERTICAL );
    m_scrollbar.setMinWidth( SCROLLBAR_SIZE );

    m_canvas.setWidth( 100.0 );
    m_canvas.setHeight( 100.0 );
    redrawCanvas();

    // use grid layout to create drop-down list    
    GridPane grid = new GridPane();
    grid.addRow( 0, m_canvas, m_scrollbar );

    // set pop-up contents to be grid pane and make pop-up visible
    getContent().add( grid );
    sizeToScene();

    Bounds bounds = parent.getBoundsInLocal();
    Bounds sb = parent.localToScreen( bounds );

    setX( sb.getMinX() );
    setY( sb.getMaxY() );

    show( parent.getScene().getWindow() );
  }

  /**************************************** redrawCanvas *****************************************/
  private void redrawCanvas()
  {
    // redraw combo list contents onto canvas 
    GraphicsContext gc = m_canvas.getGraphicsContext2D();
    gc.setFontSmoothingType( FontSmoothingType.LCD );
    double w = m_canvas.getWidth();
    double h = m_canvas.getHeight();

    // fill background
    gc.setFill( Color.RED );
    gc.fillRect( 0.0, 0.0, w, h );

  }

}
