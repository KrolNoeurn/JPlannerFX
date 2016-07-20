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

package rjc.jplanner.gui.table;

import static java.lang.Math.min;

import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;

/*************************************************************************************************/
/*********************** JavaFX table display with canvas and scroll bars ************************/
/*************************************************************************************************/

public class TableDisplay extends TableParent
{
  protected ScrollBar m_vScrollBar;       // vertical scroll bar
  protected ScrollBar m_hScrollBar;       // horizontal scroll bar
  private TableCanvas m_canvas;           // table canvas
  private Table       m_table;            // table definition

  private static int  SCROLLBAR_SIZE = 18;

  /************************************** initialiseDisplay **************************************/
  public void initialiseDisplay( Table table )
  {
    // construct table display with canvas and scroll bars
    m_table = table;
    m_canvas = new TableCanvas( m_table );

    // vertical scroll bar
    m_vScrollBar = new ScrollBar();
    m_vScrollBar.setOrientation( Orientation.VERTICAL );
    m_vScrollBar.setMinWidth( SCROLLBAR_SIZE );

    // horizontal scroll bar
    m_hScrollBar = new ScrollBar();
    m_hScrollBar.setMinHeight( SCROLLBAR_SIZE );

    // add canvas and scroll bars as parent displayed children
    add( m_canvas );
    add( m_vScrollBar );
    add( m_hScrollBar );

    // listen to scroll bar values for table scrolling
    m_vScrollBar.valueProperty().addListener( ( observable, oldValue, newValue ) -> redraw() );
    m_hScrollBar.valueProperty().addListener( ( observable, oldValue, newValue ) -> redraw() );

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
    setCanvasScrollBars();
  }

  /************************************ setCanvasScrollBars **************************************/
  public void setCanvasScrollBars()
  {
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
    m_canvas.redrawAll();
  }

  /*********************************** animationScrollVertical ***********************************/
  public void animateToVOffset( int endValue )
  {
    // create scroll vertical animation
    animate( m_vScrollBar.valueProperty(), endValue, 100 );
  }

  /********************************** animationScrollHorizontal **********************************/
  public void animateToHOffset( int endValue )
  {
    // create scroll horizontal animation
    animate( m_hScrollBar.valueProperty(), endValue, 100 );
  }

  /************************************** animateScrollToUp **************************************/
  public void animateScrollToUp()
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

  /************************************ animateScrollToRight *************************************/
  public void animateScrollToRight()
  {
    // create scroll right animation
    int value = getHOffset();
    int max = (int) m_hScrollBar.getMax();
    animate( m_hScrollBar.valueProperty(), max, 5 * ( max - value ) );
  }

  /************************************* animateScrollToLeft *************************************/
  public void animateScrollToLeft()
  {
    // create scroll left animation
    int value = getHOffset();
    animate( m_hScrollBar.valueProperty(), 0, 5 * value );
  }

}
