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

import java.util.HashMap;

import javafx.geometry.Orientation;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

/*************************************************************************************************/
/***************************** Displays data in gui scrollable table *****************************/
/*************************************************************************************************/

public class Table extends GridPane
{
  public static final Color         COLOR_GRID            = Color.SILVER;
  public static final Color         COLOR_DISABLED_CELL   = Color.rgb( 227, 227, 227 );     // medium grey
  public static final Color         COLOR_NORMAL_CELL     = Color.WHITE;
  public static final Color         COLOR_NORMAL_HEADER   = Color.rgb( 240, 240, 240 );     // light grey
  public static final Color         COLOR_NORMAL_TEXT     = Color.BLACK;
  public static final Color         COLOR_SELECTED_CELL   = Color.rgb( 51, 153, 255 );      // light blue;
  public static final Color         COLOR_SELECTED_HEADER = Color.rgb( 192, 192, 192 );     // medium dark grey
  public static final Color         COLOR_SELECTED_TEXT   = Color.WHITE;

  private ITableDataSource          m_data;                                                 // data source for the table

  private int                       m_defaultRowHeight    = 20;
  private int                       m_defaultColumnWidth  = 100;
  private int                       m_minimumRowHeight    = 15;
  private int                       m_minimumColumnWidth  = 40;
  private int                       m_hHeaderHeight       = 20;
  private int                       m_vHeaderWidth        = 30;

  private HeaderCorner              m_headerCorner;
  private Header                    m_vHeader;
  private Header                    m_hHeader;
  private Body                      m_body;
  private TableScrollBar            m_vScrollBar;
  private TableScrollBar            m_hScrollBar;

  // all columns have default widths, and rows default heights, except those in these maps, -ve means hidden
  private HashMap<Integer, Integer> m_columnWidths        = new HashMap<Integer, Integer>();
  private HashMap<Integer, Integer> m_rowHeights          = new HashMap<Integer, Integer>();

  /**************************************** constructor ******************************************/
  public Table( ITableDataSource data )
  {
    // setup grid pane
    super();

    // initial private variables and table components
    m_data = data;

    m_headerCorner = new HeaderCorner( this );
    m_hHeader = new Header( this, Orientation.HORIZONTAL );
    m_vHeader = new Header( this, Orientation.VERTICAL );
    m_body = new Body( this );

    m_vScrollBar = new TableScrollBar( this, Orientation.VERTICAL );
    m_hScrollBar = new TableScrollBar( this, Orientation.HORIZONTAL );
    m_vScrollBar.setOther( m_hScrollBar );
    m_hScrollBar.setOther( m_vScrollBar );

    // place table components onto grid
    add( m_headerCorner, 0, 0 );
    add( m_hHeader, 1, 0 );
    add( m_vHeader, 0, 1 );
    add( m_body, 1, 1 );
    add( m_vScrollBar, 2, 0, 1, 2 );
    add( m_hScrollBar, 0, 2, 2, 1 );

    // table body should grow to fill all available space
    setHgrow( m_body, Priority.ALWAYS );
    setVgrow( m_body, Priority.ALWAYS );

    // set listeners for scroll bar visibility (can only be setup after both scroll bars created)
    m_hHeader.setScrollBarListeners();
    m_vHeader.setScrollBarListeners();
    m_body.setScrollBarListeners();
  }

  /*************************************** getDataSource *****************************************/
  public ITableDataSource getDataSource()
  {
    return m_data;
  }

  /************************************* getVerticalHeader ***************************************/
  public Header getVerticalHeader()
  {
    return m_vHeader;
  }

  /************************************ getHorizontalHeader **************************************/
  public Header getHorizontalHeader()
  {
    return m_hHeader;
  }

  /****************************************** getBody ********************************************/
  public Body getBody()
  {
    return m_body;
  }

  /************************************ getVerticalScrollBar *************************************/
  public TableScrollBar getVerticalScrollBar()
  {
    return m_vScrollBar;
  }

  /*********************************** getHorizontalScrollBar ************************************/
  public TableScrollBar getHorizontalScrollBar()
  {
    return m_hScrollBar;
  }

  /************************************** getColumnExactAtX **************************************/
  public int getColumnExactAtX( double x )
  {
    // return column index at specified x-coordinate, or -1 if before, MAX_INT if after
    if ( x < 0.0 )
      return -1;

    int last = m_data.getColumnCount() - 1;
    for ( int column = 0; column <= last; column++ )
    {
      x -= getColumnWidth( column );
      if ( x <= 0.0 )
        return column;
    }

    return Integer.MAX_VALUE;
  }

  /***************************************** getColumnAtX ****************************************/
  public int getColumnAtX( double x )
  {
    // return column index at specified x-coordinate, or nearest
    int last = m_data.getColumnCount() - 1;
    for ( int column = 0; column <= last; column++ )
    {
      x -= getColumnWidth( column );
      if ( x <= 0.0 )
        return column;
    }

    return last;
  }

  /*************************************** getColumnStartX ***************************************/
  public int getColumnStartX( int column )
  {
    // return start-x of specified column
    if ( column > m_data.getColumnCount() )
      column = m_data.getColumnCount();

    int startX = 0;
    for ( int c = 0; c < column; c++ )
      startX += getColumnWidth( c );

    return startX;
  }

  /*************************************** getColumnWidth ****************************************/
  public int getColumnWidth( int column )
  {
    // return width of column
    int width = m_defaultColumnWidth;

    if ( m_columnWidths.containsKey( column ) )
    {
      width = m_columnWidths.get( column );
      if ( width < 0 )
        return 0; // -ve means column hidden, so return zero
    }

    return width;
  }

  /*************************************** getRowExactAtY ****************************************/
  public int getRowExactAtY( double y )
  {
    // return row index at specified x-coordinate, or -1 if before, MAX_INT if after
    if ( y < 0.0 )
      return -1;

    int last = m_data.getRowCount() - 1;
    for ( int row = 0; row <= last; row++ )
    {
      y -= getRowHeight( row );
      if ( y <= 0.0 )
        return row;
    }

    return Integer.MAX_VALUE;
  }

  /****************************************** getRowAtY ******************************************/
  public int getRowAtY( double y )
  {
    // return row index at specified x-coordinate, or nearest
    int last = m_data.getRowCount() - 1;
    for ( int row = 0; row <= last; row++ )
    {
      y -= getRowHeight( row );
      if ( y <= 0.0 )
        return row;
    }

    return last;
  }

  /**************************************** getRowStartY *****************************************/
  public int getRowStartY( int row )
  {
    // return start-y of specified row
    if ( row > m_data.getRowCount() )
      row = m_data.getRowCount();

    int startY = 0;
    for ( int r = 0; r < row; r++ )
      startY += getRowHeight( r );

    return startY;
  }

  /**************************************** getRowHeight *****************************************/
  public int getRowHeight( int row )
  {
    // return height of row
    int height = m_defaultRowHeight;

    if ( m_rowHeights.containsKey( row ) )
    {
      height = m_rowHeights.get( row );
      if ( height < 0 )
        return 0; // -ve means row hidden, so return zero
    }

    return height;
  }

  /************************************ setDefaultColumnWidth ************************************/
  public void setDefaultColumnWidth( int width )
  {
    m_defaultColumnWidth = width;
  }

  /************************************* setDefaultRowHeight *************************************/
  public void setDefaultRowHeight( int height )
  {
    m_defaultRowHeight = height;
  }

  /*********************************** getVerticalHeaderWidth ************************************/
  public int getVerticalHeaderWidth()
  {
    return m_vHeaderWidth;
  }

  /*********************************** setVerticalHeaderWidth ************************************/
  public void setVerticalHeaderWidth( int width )
  {
    m_vHeaderWidth = width;
    m_headerCorner.setWidth( width );
    m_headerCorner.redraw();
  }

  /********************************** getHorizontalHeaderHeight **********************************/
  public int getHorizontalHeaderHeight()
  {
    return m_hHeaderHeight;
  }

  /********************************** setHorizontalHeaderHeight **********************************/
  public void setHorizontalHeaderHeight( int height )
  {
    m_hHeaderHeight = height;
    m_headerCorner.setHeight( height );
    m_headerCorner.redraw();
  }

  /*************************************** setColumnWidth ****************************************/
  public void setColumnWidth( int column, int newWidth )
  {
    // width should not be below minimum
    if ( newWidth < m_minimumColumnWidth )
      newWidth = m_minimumColumnWidth;

    // record width so overrides default
    int oldWidth = getColumnWidth( column );
    m_columnWidths.put( column, newWidth );

    // if new width different to old width, update horizontal header and body cells
    if ( newWidth != oldWidth )
    {
      m_hHeader.updateColumnWidth( column, oldWidth, newWidth );
      m_body.updateColumnWidth( column, oldWidth, newWidth );
      m_hScrollBar.checkSettings();
    }
  }

  /**************************************** setRowHeight *****************************************/
  public void setRowHeight( int row, int newHeight )
  {
    // height should not be below minimum
    if ( newHeight < m_minimumRowHeight )
      newHeight = m_minimumRowHeight;

    // record height so overrides default
    int oldHeight = getRowHeight( row );
    m_rowHeights.put( row, newHeight );

    // if new height different to old height, update vertical header and body cells
    if ( newHeight != oldHeight )
    {
      m_vHeader.updateRowHeight( row, oldHeight, newHeight );
      m_body.updateRowHeight( row, oldHeight, newHeight );
      m_vScrollBar.checkSettings();
    }
  }

}
