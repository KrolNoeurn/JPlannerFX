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

import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import rjc.jplanner.JPlanner;

/*************************************************************************************************/
/**************************** Window for plan undo-stack command list ****************************/
/*************************************************************************************************/

public class UndoStackWindow extends Stage
{
  private Canvas    m_canvas    = new Canvas();
  private ScrollBar m_scrollbar = new ScrollBar();

  /**************************************** constructor ******************************************/
  public UndoStackWindow()
  {
    // create undo-stack window
    super();
    setTitle( "Undostack" );
    setWidth( 250.0 );
    setHeight( 200.0 );

    // set layout    
    BorderPane root = new BorderPane();
    Scene scene = new Scene( root );
    setScene( scene );
    root.setCenter( m_canvas );

    // bind canvas size to scene size
    m_canvas.heightProperty().bind( scene.heightProperty() );
    m_canvas.widthProperty().bind( scene.widthProperty() );
    m_canvas.heightProperty().addListener( ( observable, oldValue, newValue ) -> redrawCanvas() );
    m_canvas.widthProperty().addListener( ( observable, oldValue, newValue ) -> redrawCanvas() );
  }

  /**************************************** redrawCanvas *****************************************/
  private void redrawCanvas()
  {
    // redraw undo-stack contents onto canvas 
    GraphicsContext gc = m_canvas.getGraphicsContext2D();
    double w = m_canvas.getWidth();
    double h = m_canvas.getHeight();
    Bounds bounds = ( new Text( "Qwerty" ) ).getLayoutBounds();
    double step = Math.ceil( bounds.getHeight() );

    JPlanner.trace( "BOUNDS " + bounds );

    for ( int i = 0; i < 10; i++ )
    {
      gc.setFill( Color.rgb( ( i * 17 ) % 256, ( i * 57 ) % 256, ( i * 97 ) % 256 ) );
      gc.fillRect( 0, i * bounds.getHeight(), w, bounds.getHeight() );
      gc.setFill( Color.WHITE );
      gc.fillText( "Label" + i, -bounds.getMinX(), i * bounds.getHeight() - bounds.getMinY() );
    }

  }

}
