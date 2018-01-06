/**************************************************************************
 *  Copyright (C) 2018 by Richard Crook                                   *
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

import static java.lang.Math.min;

import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.ScrollEvent;

/*************************************************************************************************/
/*********************** JavaFX table display with canvas and scroll bars ************************/
/*************************************************************************************************/

class TableDisplay extends TableParent
{
  protected ScrollBar m_vScrollBar;       // vertical scroll bar
  protected ScrollBar m_hScrollBar;       // horizontal scroll bar
  private TableEvents m_canvas;           // table canvas
  private Table       m_table;            // table definition

  private static int  SCROLLBAR_SIZE = 18;

  /****************************************** assemble *******************************************/
  public void assemble( Table table )
  {
    // construct table display with canvas and scroll bars
    m_table = table;
    m_canvas = new TableEvents( table );

    // vertical scroll bar
    m_vScrollBar = new TableScrollBar( table );
    m_vScrollBar.setOrientation( Orientation.VERTICAL );
    m_vScrollBar.setMinWidth( SCROLLBAR_SIZE );

    // horizontal scroll bar
    m_hScrollBar = new TableScrollBar( table );
    m_hScrollBar.setMinHeight( SCROLLBAR_SIZE );

    // add canvas and scroll bars as parent displayed children
    add( m_canvas );
    add( m_vScrollBar );
    add( m_hScrollBar );
    setOnScroll( event -> scrollEvent( event ) );

    // listen to scroll bar values for table scrolling
    m_vScrollBar.valueProperty().addListener( ( observable, oldValue, newValue ) ->
    {
      if ( oldValue.intValue() != newValue.intValue() )
        redraw();
    } );
    m_hScrollBar.valueProperty().addListener( ( observable, oldValue, newValue ) ->
    {
      if ( oldValue.intValue() != newValue.intValue() )
        redraw();
    } );

    // lock layout to 0,0 to prevent any shudder or incorrect placing
    layoutXProperty().addListener( ( observable, oldValue, newValue ) -> setLayoutX( 0.0 ) );
    layoutYProperty().addListener( ( observable, oldValue, newValue ) -> setLayoutY( 0.0 ) );
  }

  /******************************************* resize ********************************************/
  @Override
  public void resize( double width, double height )
  {
    // when parent resized ensure canvas and scroll bars adjust accordingly
    super.resize( width, height );
    resizeCanvasScrollBars();
  }

  /**************************************** requestFocus *****************************************/
  @Override
  public void requestFocus()
  {
    // setting focus on table should set focus on canvas
    m_canvas.requestFocus();
  }

  /*********************************** resizeCanvasScrollBars ************************************/
  public void resizeCanvasScrollBars()
  {
    // check height & width are real before proceeding
    if ( getHeight() == Integer.MAX_VALUE || getWidth() == Integer.MAX_VALUE )
      return;

    // determine which scroll-bars should be visible
    boolean isVSBvisible = getHeight() < m_table.getTableHeight();
    int visibleWidth = isVSBvisible ? getWidth() - SCROLLBAR_SIZE : getWidth();
    boolean isHSBvisible = visibleWidth < m_table.getTableWidth();
    int visibleHeight = isHSBvisible ? getHeight() - SCROLLBAR_SIZE : getHeight();
    isVSBvisible = visibleHeight < m_table.getTableHeight();
    visibleWidth = isVSBvisible ? getWidth() - SCROLLBAR_SIZE : getWidth();

    // set scroll-bars visibility and size
    setVScrollBar( isVSBvisible, visibleHeight );
    setHScrollBar( isHSBvisible, visibleWidth );

    // set canvas size
    m_canvas.setWidth( min( visibleWidth, m_table.getTableWidth() ) );
    m_canvas.setHeight( min( visibleHeight, m_table.getTableHeight() ) );
  }

  /*************************************** setHScrollBar *****************************************/
  private void setHScrollBar( boolean visible, int width )
  {
    // set horizontal scroll-bar visibility, size, location and thumb
    m_hScrollBar.setVisible( visible );

    if ( visible )
    {
      m_hScrollBar.setMinWidth( width );
      m_hScrollBar.setLayoutY( getHeight() - SCROLLBAR_SIZE );

      double max = m_table.getTableWidth() - width;
      m_hScrollBar.setMax( max );
      m_hScrollBar.setVisibleAmount( max * width / m_table.getTableWidth() );
      m_hScrollBar.setBlockIncrement( width - m_table.getVerticalHeaderWidth() );

      if ( m_hScrollBar.getValue() > max )
        m_hScrollBar.setValue( max );
      if ( m_hScrollBar.getValue() < 0.0 )
        m_hScrollBar.setValue( 0.0 );
    }
    else
      m_hScrollBar.setValue( 0.0 );
  }

  /*************************************** setVScrollBar *****************************************/
  private void setVScrollBar( boolean visible, int height )
  {
    // set vertical scroll-bar visibility, size, location and thumb
    m_vScrollBar.setVisible( visible );

    if ( visible )
    {
      m_vScrollBar.setMinHeight( height );
      m_vScrollBar.setLayoutX( getWidth() - SCROLLBAR_SIZE );

      double max = m_table.getTableHeight() - height;
      m_vScrollBar.setMax( max );
      m_vScrollBar.setVisibleAmount( max * height / m_table.getTableHeight() );
      m_vScrollBar.setBlockIncrement( height - m_table.getHorizontalHeaderHeight() );

      if ( m_vScrollBar.getValue() > max )
        m_vScrollBar.setValue( max );
      if ( m_vScrollBar.getValue() < 0.0 )
        m_vScrollBar.setValue( 0.0 );
    }
    else
      m_vScrollBar.setValue( 0.0 );
  }

  /***************************************** getVOffset ******************************************/
  public int getVOffset()
  {
    // return table vertical offset due to scroll bar
    return (int) m_vScrollBar.getValue();
  }

  /***************************************** getHOffset ******************************************/
  public int getHOffset()
  {
    // return table horizontal offset due to scroll bar
    return (int) m_hScrollBar.getValue();
  }

  /****************************************** getCanvas ******************************************/
  public TableEvents getCanvas()
  {
    // return table canvas
    return m_canvas;
  }

  /*************************************** getCanvasWidth ****************************************/
  public int getCanvasWidth()
  {
    // return width of canvas
    return (int) m_canvas.getWidth();
  }

  /*************************************** getCanvasHeight ***************************************/
  public int getCanvasHeight()
  {
    // return height of canvas
    return (int) m_canvas.getHeight();
  }

  /******************************************* redraw ********************************************/
  public void redraw()
  {
    // trigger simple complete redraw of table canvas
    AbstractCellEditor.endEditing();
    if ( getHeight() > 0 && getWidth() > 0 )
      m_canvas.redrawTableCanvas();

    // ensure reorder marker if exists is correctly positioned due to scrolling
    m_canvas.setMarkerPosition();
  }

  /************************************** animateToVOffset ***************************************/
  public void animateToVOffset( int endValue )
  {
    // create scroll vertical animation
    if ( endValue < m_vScrollBar.getMin() )
      endValue = (int) m_vScrollBar.getMin();
    if ( endValue > m_vScrollBar.getMax() )
      endValue = (int) m_vScrollBar.getMax();
    animate( m_vScrollBar.valueProperty(), endValue, 200 );
  }

  /************************************** animateToHOffset ***************************************/
  public void animateToHOffset( int endValue )
  {
    // create scroll horizontal animation
    if ( endValue < m_hScrollBar.getMin() )
      endValue = (int) m_vScrollBar.getMin();
    if ( endValue > m_hScrollBar.getMax() )
      endValue = (int) m_vScrollBar.getMax();
    animate( m_hScrollBar.valueProperty(), endValue, 200 );
  }

  /************************************* animateScrollToTop **************************************/
  public void animateScrollToTop()
  {
    // create scroll up animation
    int value = getVOffset();
    animate( m_vScrollBar.valueProperty(), 0, 5 * value );
  }

  /************************************ animateScrollToBottom ************************************/
  public void animateScrollToBottom()
  {
    // create scroll down animation
    int value = getVOffset();
    int max = (int) m_vScrollBar.getMax();
    animate( m_vScrollBar.valueProperty(), max, 5 * ( max - value ) );
  }

  /********************************** animateScrollToRightEdge ***********************************/
  public void animateScrollToRightEdge()
  {
    // create scroll right animation
    int value = getHOffset();
    int max = (int) m_hScrollBar.getMax();
    animate( m_hScrollBar.valueProperty(), max, 5 * ( max - value ) );
  }

  /*********************************** animateScrollToLeftEdge ***********************************/
  public void animateScrollToLeftEdge()
  {
    // create scroll left animation
    int value = getHOffset();
    animate( m_hScrollBar.valueProperty(), 0, 5 * value );
  }

  /**************************************** scrollEvent ******************************************/
  public void scrollEvent( ScrollEvent event )
  {
    // scroll up or down depending on mouse wheel scroll event
    if ( event.getDeltaY() > 0 )
      m_vScrollBar.decrement();
    else
      m_vScrollBar.increment();
    finishAnimation();
  }

}
