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

import javafx.scene.Group;

/*************************************************************************************************/
/***************************** Displays data in gui scrollable table *****************************/
/*************************************************************************************************/

public class Table extends Group
{
  public static final int           DEFAULT_HORIZONTAL_HEADER_HEIGHT = 30;
  public static final int           DEFAULT_VERTICAL_HEADER_WIDTH    = 40;
  public static final int           DEFAULT_ROW_HEIGHT               = 20;
  public static final int           DEFAULT_COLUMN_WIDTH             = 50;

  private ITableDataSource          m_data;
  private HeaderCorner              m_cHeader;
  private VerticalHeader            m_vHeader;
  private HorizontalHeader          m_hHeader;
  private TableCells                m_cells;
  private GridLines                 m_gridLines;

  // all columns have default widths, and rows default heights, except those in these maps, -ve means hidden
  private HashMap<Integer, Integer> m_columnWidths                   = new HashMap<Integer, Integer>();
  private HashMap<Integer, Integer> m_rowHeights                     = new HashMap<Integer, Integer>();

  /**************************************** constructor ******************************************/
  public Table( ITableDataSource data )
  {
    super();

    // initial private variables
    m_data = data;
    m_cHeader = new HeaderCorner( this );
    m_vHeader = new VerticalHeader( this );
    m_hHeader = new HorizontalHeader( this );
    m_cells = new TableCells( this );
    m_gridLines = new GridLines( this );

    // add table elements to JavaFX group
    getChildren().add( m_cHeader );
    getChildren().add( m_vHeader );
    getChildren().add( m_hHeader );
    getChildren().add( m_cells );
    getChildren().add( m_gridLines );
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

  /*************************************** getColumnStartX ***************************************/
  public int getColumnStartX( int column )
  {
    // return start-x of specified column, ignoring any headers (i.e. starting at zero)
    if ( column < 0 )
      throw new IllegalArgumentException( "column=" + column );
    if ( column >= m_data.getColumnCount() )
      return -1;

    int start = 0;
    for ( int c = 0; c < column; c++ )
      start += getColumnWidth( c );

    return start;
  }

  /*************************************** getColumnWidth ****************************************/
  public int getColumnWidth( int column )
  {
    // return width of column
    int width = DEFAULT_COLUMN_WIDTH;

    if ( m_columnWidths.containsKey( column ) )
    {
      width = m_columnWidths.get( column );
      if ( width < 0.0 )
        return 0; // -ve means column hidden, so return zero
    }

    return width;
  }

  /**************************************** getRowStartY *****************************************/
  public int getRowStartY( int row )
  {
    // return start-y of specified row, ignoring any headers (i.e. starting at zero)
    if ( row < 0 )
      throw new IllegalArgumentException( "row=" + row );
    if ( row >= m_data.getRowCount() )
      return -1;

    int start = 0;
    for ( int r = 0; r < row; r++ )
      start += getRowHeight( r );

    return start;
  }

  /**************************************** getRowHeight *****************************************/
  public int getRowHeight( int row )
  {
    // return height of row
    int height = DEFAULT_ROW_HEIGHT;

    if ( m_rowHeights.containsKey( row ) )
    {
      height = m_rowHeights.get( row );
      if ( height < 0.0 )
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
    int max = m_data.getRowCount() - 1;
    return getRowStartY( max ) + getRowHeight( max );
  }

}
