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
import javafx.scene.effect.DropShadow;
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
  private int                 m_highlighed;

  private static final int    BORDER = 2;

  /**************************************** constructor ******************************************/
  public ComboDropDown( AbstractComboEditor parent )
  {
    // create pop-up window to display drop-down list
    super();
    m_parent = parent;
    m_canvas = new Canvas();
    setAutoHide( true );
    setConsumeAutoHidingEvents( false );
    getContent().add( m_canvas );

    // add shadow
    DropShadow shadow = new DropShadow();
    shadow.setColor( Colors.FOCUSED_BLUE );
    shadow.setRadius( 4.0 );
    getScene().getRoot().setEffect( shadow );

    // determine row height and row text descent
    Bounds bounds = ( new Text( "Qwerty" ) ).getLayoutBounds();
    m_rowHeight = (int) Math.ceil( bounds.getHeight() );
    m_rowDescent = (int) Math.floor( -bounds.getMinY() );

    // react to mouse movement and mouse pressed
    m_canvas.setOnMouseMoved( event -> redrawCanvas( getIndexAtY( (int) event.getY() ) ) );
    m_canvas.setOnMousePressed( event -> setSelectedIndex( getIndexAtY( (int) event.getY() ) ) );

    // toggle pop-up when parent is pressed
    parent.setOnMousePressed( event ->
    {
      if ( isShowing() )
        hide();
      else
      {
        // set pop-up position and show 
        Point2D point = parent.localToScreen( 0.0, parent.getHeight() );
        double x = point.getX() - shadow.getRadius() + 1.0;
        double y = point.getY() - shadow.getRadius() + 1.0;

        // determine canvas size
        m_canvas.setWidth( parent.getWidth() );
        int height = count() * m_rowHeight + BORDER * 2;
        Screen screen = Screen.getScreensForRectangle( x, y, 0.0, 0.0 ).get( 0 );
        double maxY = screen.getVisualBounds().getMaxY();
        if ( y + height > maxY )
          height = (int) ( maxY - y - BORDER * 2 );
        m_canvas.setHeight( height );
        redrawCanvas();
        show( parent, x, y );
      }
    } );

    // hide pop-up when parent loses focus (e.g. when TAB pressed)
    parent.focusedProperty().addListener( ( observable, oldFocus, newFocus ) -> hide() );
  }

  /**************************************** redrawCanvas *****************************************/
  public void redrawCanvas()
  {
    // redraw drop-down list contents onto canvas with selected item highlighted
    m_highlighed = -2;
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
    gc.setStroke( Colors.FOCUSED_BLUE );
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
