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
    getIcons().add( MainWindow.JPLANNER_ICON );

    // setup scroll bar
    m_scrollbar.setOrientation( Orientation.VERTICAL );
    m_scrollbar.setMinWidth( SCROLLBAR_SIZE );
    m_scrollbar.setVisible( false );

    // set grid layout    
    GridPane grid = new GridPane();
    Scene scene = new Scene( grid );
    setScene( scene );
    grid.addRow( 0, m_canvas, m_scrollbar );

    // determine typical text bounds and step
    Bounds bounds = ( new Text( "Qwerty" ) ).getLayoutBounds();
    m_rowHeight = (int) Math.ceil( bounds.getHeight() );
    m_rowDescent = (int) Math.floor( -bounds.getMinY() );

    // update scroll bar and canvas on scene size change
    scene.heightProperty().addListener( ( observable, oldValue, newValue ) -> updateScrollBarAndCanvas() );
    scene.widthProperty().addListener( ( observable, oldValue, newValue ) -> updateScrollBarAndCanvas() );

    // update canvas on scroll bar movement
    m_scrollbar.valueProperty().addListener( ( observable, oldValue, newValue ) -> redrawCanvas() );

    // update selected index on mouse button press and drag
    m_canvas.setOnMousePressed( event -> setIndex( getIndexAtY( event.getY() ) + 1 ) );
    m_canvas.setOnMouseDragged( event -> setIndex( getIndexAtY( event.getY() ) + 1 ) );

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
  private int getIndexAtY( double y )
  {
    // get index at y-coordinate on canvas
    int row = (int) ( ( y + m_scrollbar.getValue() ) / m_rowHeight ) - 1;
    if ( row >= size() )
      return size() - 1;
    if ( row < -1 )
      return -1;

    return row;
  }

  /************************************** getYStartByIndex ***************************************/
  private int getYStartByIndex( int index )
  {
    // get start y-coordinate for index row on canvas
    return ( index + 1 ) * m_rowHeight - (int) m_scrollbar.getValue();
  }

  /************************************** makeIndexVisible ***************************************/
  public void makeCurrentIndexVisible()
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
  }

  /********************************* updateScrollBarsAndCanvas ***********************************/
  public void updateScrollBarAndCanvas()
  {
    // if window not showing, return immediately
    if ( !isShowing() )
      return;

    // set scroll bar to correct visibility
    double fullHeight = m_rowHeight * ( size() + 1 );
    boolean need = getScene().getHeight() < fullHeight;
    m_scrollbar.setVisible( need );

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
    m_canvas.setWidth( width );

    // make sure canvas is redrawn
    redrawCanvas();
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
    int min = getIndexAtY( 0.0 );
    int max = getIndexAtY( m_canvas.getHeight() - 1.0 );

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
