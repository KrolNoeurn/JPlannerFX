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
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import rjc.jplanner.JPlanner;

/*************************************************************************************************/
/**************************** Window for plan undo-stack command list ****************************/
/*************************************************************************************************/

public class UndoStackWindow extends Stage
{
  private Canvas             m_canvas            = new Canvas();
  private ScrollBar          m_scrollbar         = new ScrollBar();
  private int                m_rowDescent;
  private int                m_rowHeight;

  private static final Color COLOR_NORMAL_CELL   = Color.WHITE;
  private static final Color COLOR_NORMAL_TEXT   = Color.BLACK;
  private static final Color COLOR_SELECTED_CELL = Color.rgb( 51, 153, 255 ); // light blue;
  private static final Color COLOR_SELECTED_TEXT = Color.WHITE;

  private static int         SCROLLBAR_SIZE      = 18;

  /**************************************** constructor ******************************************/
  public UndoStackWindow()
  {
    // create undo-stack window
    super();
    setTitle( "Undostack" );
    setWidth( 250.0 );
    setHeight( 200.0 );

    // setup scroll bar
    m_scrollbar.setOrientation( Orientation.VERTICAL );
    m_scrollbar.setMinWidth( SCROLLBAR_SIZE );

    // set grid layout    
    GridPane grid = new GridPane();
    Scene scene = new Scene( grid );
    setScene( scene );
    grid.add( m_canvas, 0, 0 );
    grid.add( m_scrollbar, 1, 0 );

    // determine typical text bounds and step
    Bounds bounds = ( new Text( "Qwerty" ) ).getLayoutBounds();
    m_rowHeight = (int) Math.ceil( bounds.getHeight() );
    m_rowDescent = (int) Math.floor( -bounds.getMinY() );

    // update scroll bar and canvas on scene size change
    scene.heightProperty().addListener( ( observable, oldValue, newValue ) -> updateScrollBarAndCanvas() );
    scene.widthProperty().addListener( ( observable, oldValue, newValue ) -> updateScrollBarAndCanvas() );
    m_canvas.heightProperty().addListener( ( observable, oldValue, newValue ) -> redrawCanvas() );
    m_canvas.widthProperty().addListener( ( observable, oldValue, newValue ) -> redrawCanvas() );
    m_scrollbar.valueProperty().addListener( ( observable, oldValue, newValue ) -> redrawCanvas() );

    // update selected index on mouse button press and drag
    m_canvas.setOnMousePressed( event -> select( (int) event.getY() ) );
    m_canvas.setOnMouseDragged( event -> select( (int) event.getY() ) );

    // let canvas has focus and react to key presses to navigate undo stack 
    m_canvas.setFocusTraversable( true );
    m_canvas.setOnKeyPressed( event ->
    {
      switch ( event.getCode() )
      {
        case HOME:
          setIndex( 0 );
          makeCurrentIndexVisible();
          break;
        case END:
          setIndex( size() );
          makeCurrentIndexVisible();
          break;
        case PAGE_UP:
          setIndex( index() - (int) ( m_canvas.getHeight() / m_rowHeight ) );
          makeCurrentIndexVisible();
          break;
        case PAGE_DOWN:
          setIndex( index() + (int) ( m_canvas.getHeight() / m_rowHeight ) );
          makeCurrentIndexVisible();
          break;
        case UP:
        case KP_UP:
          setIndex( index() - 1 );
          makeCurrentIndexVisible();
          break;
        case DOWN:
        case KP_DOWN:
          setIndex( index() + 1 );
          makeCurrentIndexVisible();
          break;

        default:
          break;
      }
    } );

    // make current index visible
    makeCurrentIndexVisible();
  }

  /******************************************** size *********************************************/
  private int size()
  {
    // get undo stack size
    return JPlanner.plan.undostack().size();
  }

  /******************************************** index ********************************************/
  private int index()
  {
    // get undo stack current index
    return JPlanner.plan.undostack().index();
  }

  /****************************************** setIndex *******************************************/
  private void setIndex( int index )
  {
    // set undo stack current index
    JPlanner.plan.undostack().setIndex( index );
  }

  /**************************************** getIndexAtY ******************************************/
  private int getIndexAtY( int y )
  {
    // get index at y-coordinate on canvas
    int row = (int) ( ( y + m_scrollbar.getValue() ) / m_rowHeight ) - 1;
    if ( row >= size() )
      return size() - 1;

    return row;
  }

  /************************************** getYStartByIndex ***************************************/
  private int getYStartByIndex( int index )
  {
    // get start y-coordinate for index row on canvas
    return ( index + 1 ) * m_rowHeight - (int) m_scrollbar.getValue();
  }

  /************************************** makeIndexVisible ***************************************/
  private void makeCurrentIndexVisible()
  {
    // scroll canvas to make current index visible
    makeIndexVisible( index() );
  }

  /************************************** makeIndexVisible ***************************************/
  private void makeIndexVisible( int index )
  {
    // make specified index visible
    int y = getYStartByIndex( index - 1 );

    if ( y < 0 )
      m_scrollbar.setValue( m_scrollbar.getValue() + y );
    else if ( y > m_canvas.getHeight() - m_rowHeight )
      m_scrollbar.setValue( m_scrollbar.getValue() + y - m_canvas.getHeight() + m_rowHeight );

    // make sure canvas is redrawn
    redrawCanvas();
  }

  /********************************* updateScrollBarsAndCanvas ***********************************/
  private void updateScrollBarAndCanvas()
  {
    // set scroll bar to correct visibility
    double fullHeight = m_rowHeight * ( size() + 1 );
    boolean need = getScene().getHeight() < fullHeight;
    if ( need != m_scrollbar.isVisible() )
    {
      m_scrollbar.setVisible( need );
      return;
    }

    // set scroll bars correct thumb size and position
    if ( m_scrollbar.isVisible() )
    {
      double max = fullHeight - getScene().getHeight();
      m_scrollbar.setMax( max );
      m_scrollbar.setVisibleAmount( max * getScene().getHeight() / fullHeight );
      if ( m_scrollbar.getValue() > max )
        m_scrollbar.setValue( max );
    }
    else
      m_scrollbar.setValue( 0.0 );

    // set canvas to correct size to not overlap scroll bars
    m_canvas.setHeight( getScene().getHeight() );

    int width = (int) getScene().getWidth();
    if ( m_scrollbar.isVisible() )
      width -= SCROLLBAR_SIZE;
    if ( width != (int) m_canvas.getWidth() )
      m_canvas.setWidth( width );
  }

  /******************************************* select ********************************************/
  private void select( int y )
  {
    // select undo stack index at y-coordinate and update canvas 
    int index = getIndexAtY( y ) + 1;
    if ( index != index() )
    {
      setIndex( index );
      redrawCanvas();
    }
  }

  /**************************************** redrawCanvas *****************************************/
  private void redrawCanvas()
  {
    // redraw undo-stack contents onto canvas 
    GraphicsContext gc = m_canvas.getGraphicsContext2D();
    gc.setFontSmoothingType( FontSmoothingType.LCD );

    // fill background
    gc.setFill( COLOR_NORMAL_CELL );
    gc.fillRect( 0.0, 0.0, getWidth(), getHeight() );

    // determine undo-stack visible range
    int min = getIndexAtY( 0 );
    int max = getIndexAtY( (int) m_canvas.getHeight() - 1 );

    // draw undo-stack text
    String text;
    for ( int item = min; item <= max; item++ )
    {
      if ( item < 0 )
        text = "<empty>";
      else
        text = JPlanner.plan.undostack().text( item );

      // colour current index item differently
      int y = getYStartByIndex( item );
      if ( item == index() - 1 )
      {
        gc.setFill( COLOR_SELECTED_CELL );
        gc.fillRect( 0.0, y, getWidth(), m_rowHeight );
        gc.setFill( COLOR_SELECTED_TEXT );
      }
      else
        gc.setFill( COLOR_NORMAL_TEXT );

      gc.fillText( text, 3.0, y + m_rowDescent );
    }

  }

}
