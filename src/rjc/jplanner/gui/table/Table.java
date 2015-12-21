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
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

/*************************************************************************************************/
/***************************** Displays data in gui scrollable table *****************************/
/*************************************************************************************************/

public class Table extends GridPane
{
  public static final int           DEFAULT_ROW_HEIGHT               = 20;
  public static final int           DEFAULT_COLUMN_WIDTH             = 100;
  public static final int           DEFAULT_HORIZONTAL_HEADER_HEIGHT = DEFAULT_ROW_HEIGHT;
  public static final int           DEFAULT_VERTICAL_HEADER_WIDTH    = 30;

  public static final Color         COLOR_GRID                       = Color.SILVER;
  public static final Color         COLOR_HEADER_FILL                = Color.rgb( 240, 240, 240 );

  private ITableDataSource          m_data;
  private int                       m_offsetX;
  private int                       m_offsetY;

  private ScrollBar                 m_vScrollBar;
  private ScrollBar                 m_hScrollBar;

  private HeaderCorner              m_cHeader;
  private VerticalHeader            m_vHeader;
  private HorizontalHeader          m_hHeader;
  private TableCells                m_cells;

  // all columns have default widths, and rows default heights, except those in these maps, -ve means hidden
  private HashMap<Integer, Integer> m_columnWidths                   = new HashMap<Integer, Integer>();
  private HashMap<Integer, Integer> m_rowHeights                     = new HashMap<Integer, Integer>();

  /**************************************** constructor ******************************************/
  public Table( ITableDataSource data )
  {
    // setup grid pane
    super();

    // initial private variables and table components
    m_data = data;
    m_offsetX = 0;
    m_offsetY = 0;

    m_cHeader = new HeaderCorner( this );
    m_hHeader = new HorizontalHeader( this );
    m_vHeader = new VerticalHeader( this );
    m_cells = new TableCells( this );

    m_vScrollBar = new ScrollBar();
    m_vScrollBar.setOrientation( Orientation.VERTICAL );
    m_vScrollBar.setMinWidth( 16 );

    m_hScrollBar = new ScrollBar();
    m_hScrollBar.setOrientation( Orientation.HORIZONTAL );
    m_hScrollBar.setMinHeight( 16 );

    // place table components onto grid
    add( m_cHeader, 0, 0 );
    add( m_hHeader, 1, 0 );
    add( m_vHeader, 0, 1 );
    add( m_cells, 1, 1 );
    add( m_vScrollBar, 2, 0, 1, 2 );
    add( m_hScrollBar, 0, 2, 2, 1 );

    // cells area should grow to fill all available space
    setHgrow( m_cells, Priority.ALWAYS );
    setVgrow( m_cells, Priority.ALWAYS );
  }

  /*************************************** getDataSource *****************************************/
  public ITableDataSource getDataSource()
  {
    return m_data;
  }

  /************************************** getCornerHeader ****************************************/
  public HeaderCorner getCornerHeader()
  {
    return m_cHeader;
  }

  /************************************* getVerticalHeader ***************************************/
  public VerticalHeader getVerticalHeader()
  {
    return m_vHeader;
  }

  /************************************ getHorizontalHeader **************************************/
  public HorizontalHeader getHorizontalHeader()
  {
    return m_hHeader;
  }

  /****************************************** getCells *******************************************/
  public TableCells getCells()
  {
    return m_cells;
  }

  /***************************************** getOffsetX ******************************************/
  public double getOffsetX()
  {
    return m_offsetX;
  }

  /***************************************** getOffsetY ******************************************/
  public double getOffsetY()
  {
    return m_offsetY;
  }

  /***************************************** getColumnAtX ****************************************/
  public int getColumnAtX( double xx )
  {
    // return column index at specified x-coordinate, or -1 if before, MAX if after
    int x = (int) ( xx ) + m_offsetX;
    if ( x < 0 )
      return -1;

    for ( int column = 0; column < m_data.getColumnCount(); column++ )
    {
      x -= getColumnWidth( column );
      if ( x < 0 )
        return column;
    }

    return Integer.MAX_VALUE;
  }

  /*************************************** getColumnStartX ***************************************/
  public int getColumnStartX( int column )
  {
    // return start-x of specified column
    if ( column < 0 )
      throw new IllegalArgumentException( "column=" + column );
    if ( column >= m_data.getColumnCount() )
      return -1;

    int startX = 0;
    for ( int c = 0; c < column; c++ )
      startX += getColumnWidth( c );

    return startX - m_offsetX;
  }

  /*************************************** getColumnWidth ****************************************/
  public int getColumnWidth( int column )
  {
    // return width of column
    int width = DEFAULT_COLUMN_WIDTH;

    if ( m_columnWidths.containsKey( column ) )
    {
      width = m_columnWidths.get( column );
      if ( width < 0 )
        return 0; // -ve means column hidden, so return zero
    }

    return width;
  }

  /****************************************** getRowAtY ******************************************/
  public int getRowAtY( double yy )
  {
    // return row index at specified x-coordinate, or -1 if before, MAX if after
    int y = (int) ( yy ) + m_offsetY;
    if ( y < 0 )
      return -1;

    for ( int row = 0; row < m_data.getRowCount(); row++ )
    {
      y -= getRowHeight( row );
      if ( y < 0 )
        return row;
    }

    return Integer.MAX_VALUE;
  }

  /**************************************** getRowStartY *****************************************/
  public int getRowStartY( int row )
  {
    // return start-y of specified row
    if ( row < 0 )
      throw new IllegalArgumentException( "row=" + row );
    if ( row >= m_data.getRowCount() )
      return -1;

    int startY = 0;
    for ( int r = 0; r < row; r++ )
      startY += getRowHeight( r );

    return startY - m_offsetY;
  }

  /**************************************** getRowHeight *****************************************/
  public int getRowHeight( int row )
  {
    // return height of row
    int height = DEFAULT_ROW_HEIGHT;

    if ( m_rowHeights.containsKey( row ) )
    {
      height = m_rowHeights.get( row );
      if ( height < 0 )
        return 0; // -ve means row hidden, so return zero
    }

    return height;
  }

  /**************************************** getCellsWidth ****************************************/
  public int getCellsWidth()
  {
    // return width of all the table cells
    int max = m_data.getColumnCount() - 1;
    return getColumnStartX( max ) + getColumnWidth( max );
  }

  /*************************************** getCellsHeight ****************************************/
  public int getCellsHeight()
  {
    // return height of all the table cells
    int max = m_data.getRowCount();
    return getRowStartY( max ) + getRowHeight( max );
  }

}
