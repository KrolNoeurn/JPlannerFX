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
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Screen;

/*************************************************************************************************/
/********************* Pop-up window to display AbstractCombo drop-down list *********************/
/*************************************************************************************************/

public class ComboDropDown extends Popup
{
  private AbstractComboEditor m_parent;
  private Canvas              m_canvas;
  private int                 m_rowHeight;
  private int                 m_rowDescent;
  private int                 m_rowOffset;
  private int                 m_highlighed = -1;

  private static final int    BORDER       = 2;

  /**************************************** constructor ******************************************/
  public ComboDropDown( AbstractComboEditor parent )
  {
    // create pop-up window to display drop-down list
    super();
    setAutoHide( true );
    m_parent = parent;
    m_canvas = new Canvas();

    // determine pop-up position
    Point2D point = parent.localToScreen( 0.0, parent.getHeight() );
    setX( point.getX() );
    setY( point.getY() );

    // determine row height and row text descent
    Bounds bounds = ( new Text( "Qwerty" ) ).getLayoutBounds();
    m_rowHeight = (int) Math.ceil( bounds.getHeight() );
    m_rowDescent = (int) Math.floor( -bounds.getMinY() );

    // determine canvas size
    m_canvas.setWidth( parent.getWidth() );
    int height = count() * m_rowHeight + BORDER * 2;
    Screen screen = Screen.getScreensForRectangle( getX(), getY(), 0.0, 0.0 ).get( 0 );
    double maxY = screen.getVisualBounds().getMaxY();
    if ( getY() + height > maxY )
      height = (int) ( maxY - getY() - BORDER * 2 );
    m_canvas.setHeight( height );
    redrawCanvas();

    // react to mouse movement and mouse pressed
    m_canvas.setOnMouseMoved( event -> redrawCanvas( getIndexAtY( (int) event.getY() ) ) );
    m_canvas.setOnMousePressed( event -> setSelectedIndex( getIndexAtY( (int) event.getY() ) ) );

    // set canvas as pop-up contents and show
    getContent().add( m_canvas );
    show( parent.getScene().getWindow() );
  }

  /**************************************** redrawCanvas *****************************************/
  public void redrawCanvas()
  {
    // redraw drop-down list contents onto canvas with selected item highlighted
    redrawCanvas( getSelectedIndex() );
  }

  /**************************************** redrawCanvas *****************************************/
  public void redrawCanvas( int highlightedItem )
  {
    // if highlighted item not changed, don't redraw
    if ( m_highlighed == highlightedItem )
      return;
    m_highlighed = highlightedItem;

    // redraw drop-down list contents onto canvas with specified item highlighted
    GraphicsContext gc = m_canvas.getGraphicsContext2D();
    double w = m_canvas.getWidth();
    double h = m_canvas.getHeight();
    gc.setFontSmoothingType( FontSmoothingType.LCD );

    // fill background and draw border
    gc.setFill( Colors.NORMAL_CELL );
    gc.fillRect( 0.0, 0.0, w, h );
    gc.setStroke( Colors.SELECTED_CELL );
    gc.strokeRect( 0.5, 0.5, w - 1.0, h - 1.0 );

    // determine list visible range
    int min = getIndexAtY( BORDER );
    int max = getIndexAtY( (int) h - BORDER - 1 );

    // draw list text
    int y = getYStartByIndex( min );
    for ( int item = min; item <= max; item++ )
    {
      // colour current selected index item differently
      if ( item == highlightedItem )
      {
        gc.setFill( Colors.SELECTED_CELL );
        gc.fillRect( BORDER, y, w - BORDER * 2, m_rowHeight );
        gc.setFill( Colors.SELECTED_TEXT );
      }
      else
        gc.setFill( Colors.NORMAL_TEXT );

      gc.fillText( getItem( item ), BORDER + 3, y + m_rowDescent );
      y += m_rowHeight;
    }

  }

  /******************************************** count ********************************************/
  private int count()
  {
    return m_parent.getItemCount();
  }

  /************************************** getSelectedIndex ***************************************/
  private int getSelectedIndex()
  {
    return m_parent.getSelectedIndex();
  }

  /************************************** setSelectedIndex ***************************************/
  private void setSelectedIndex( int item )
  {
    hide();
    m_parent.setSelectedIndex( item );
  }

  /******************************************* getItem *******************************************/
  private String getItem( int index )
  {
    return m_parent.getItem( index );
  }

  /**************************************** getIndexAtY ******************************************/
  private int getIndexAtY( int y )
  {
    // get index at y-coordinate on canvas
    int index = ( y + m_rowOffset - BORDER ) / m_rowHeight;
    if ( index < 0 )
      index = 0;
    if ( index >= count() )
      index = count() - 1;
    return index;
  }

  /************************************** getYStartByIndex ***************************************/
  private int getYStartByIndex( int index )
  {
    // get start y-coordinate for index row on canvas
    return index * m_rowHeight - m_rowOffset + BORDER;
  }

}
