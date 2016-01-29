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

import java.util.ArrayList;
import java.util.HashMap;

import javafx.scene.layout.StackPane;

/*************************************************************************************************/
/**************** Display gui scrollable table with horizontal & vertical header *****************/
/*************************************************************************************************/

public class Table extends StackPane
{
  private ITableDataSource          m_data;                                                // data source for the table
  private TableCanvas               m_canvas;

  private int                       m_defaultRowHeight   = 20;
  private int                       m_defaultColumnWidth = 100;
  private int                       m_minimumRowHeight   = 15;
  private int                       m_minimumColumnWidth = 40;
  private int                       m_hHeaderHeight      = 20;
  private int                       m_vHeaderWidth       = 30;

  private int                       m_offsetX            = 0;
  private int                       m_offsetY            = 0;
  private int                       m_width              = 0;                              // body cells total width (excludes header)
  private int                       m_height             = 0;                              // body cells total height (excludes header)

  // all columns have default widths, and rows default heights, except those in these maps, -ve means hidden
  private HashMap<Integer, Integer> m_columnWidths       = new HashMap<Integer, Integer>();
  private HashMap<Integer, Integer> m_rowHeights         = new HashMap<Integer, Integer>();

  // array with mapping from position to index
  private ArrayList<Integer>        m_columnIndexes      = new ArrayList<Integer>();
  private ArrayList<Integer>        m_rowIndexes         = new ArrayList<Integer>();

  public static enum Alignment// alignment of text to be drawn in cell
  {
    LEFT, MIDDLE, RIGHT
  }

  public String name; // table name helpful when debugging

  /**************************************** constructor ******************************************/
  public Table( String name, ITableDataSource data )
  {
    // prepare table
    super();
    m_data = data;
    this.name = name;

    // setup canvas for drawing the table
    m_canvas = new TableCanvas( this );
    m_canvas.widthProperty().bind( widthProperty() );
    m_canvas.heightProperty().bind( heightProperty() );
    getChildren().add( m_canvas );

    // initialise position to index mapping
    int count = data.getColumnCount();
    for ( int column = 0; column < count; column++ )
      m_columnIndexes.add( column );
    count = data.getRowCount();
    for ( int row = 0; row < count; row++ )
      m_rowIndexes.add( row );

    // initialise width & height
    calculateWidth();
    calculateHeight();
  }

  /************************************** calculateHeight ****************************************/
  private void calculateHeight()
  {
    // calculate height of table body rows
    m_height = 0;
    int count = m_data.getRowCount();
    for ( int row = 0; row < count; row++ )
      m_height += getHeightByRowPosition( row );
  }

  /************************************** calculateWidth *****************************************/
  private void calculateWidth()
  {
    // calculate width of table body columns
    m_width = 0;
    int count = m_data.getColumnCount();
    for ( int column = 0; column < count; column++ )
      m_width += getWidthByColumnPosition( column );
  }

  /************************************** getBodyHeight ******************************************/
  public int getBodyHeight()
  {
    return m_height;
  }

  /*************************************** getBodyWidth ******************************************/
  public int getBodyWidth()
  {
    return m_width;
  }

  /*************************************** getDataSource *****************************************/
  public ITableDataSource getDataSource()
  {
    return m_data;
  }

  /********************************** getColumnPositionExactAtX **********************************/
  public int getColumnPositionExactAtX( int x )
  {
    // return column position at specified x-coordinate, or -1 if before, MAX_INT if after
    x = x - m_offsetX - m_vHeaderWidth;
    if ( x < 0 )
      return -1;

    int last = m_data.getColumnCount() - 1;
    for ( int columnPos = 0; columnPos <= last; columnPos++ )
    {
      x -= getWidthByColumnPosition( columnPos );
      if ( x <= 0 )
        return columnPos;
    }

    return Integer.MAX_VALUE;
  }

  /************************************* getColumnPositionAtX ************************************/
  public int getColumnPositionAtX( int x )
  {
    // return column position at specified x-coordinate, or nearest
    x = x - m_offsetX - m_vHeaderWidth;
    int last = m_data.getColumnCount() - 1;
    for ( int columnPos = 0; columnPos <= last; columnPos++ )
    {
      x -= getWidthByColumnPosition( columnPos );
      if ( x <= 0 )
        return columnPos;
    }

    return last;
  }

  /********************************** getXStartByColumnPosition **********************************/
  public int getXStartByColumnPosition( int columnPos )
  {
    // return start-x of specified column
    if ( columnPos > m_data.getColumnCount() )
      columnPos = m_data.getColumnCount();

    int startX = m_offsetX + m_vHeaderWidth;
    for ( int column = 0; column < columnPos; column++ )
      startX += getWidthByColumnPosition( column );

    return startX;
  }

  /********************************** getWidthByColumnPosition ***********************************/
  public int getWidthByColumnPosition( int columnPos )
  {
    // return width from column position
    if ( columnPos < 0 || columnPos >= m_data.getColumnCount() )
      return Integer.MAX_VALUE;

    int width = m_defaultColumnWidth;
    int columnIndex = m_columnIndexes.get( columnPos );
    if ( m_columnWidths.containsKey( columnIndex ) )
    {
      width = m_columnWidths.get( columnIndex );
      if ( width < 0 )
        return 0; // -ve means column hidden, so return zero
    }

    return width;
  }

  /************************************ getWidthByColumnIndex ************************************/
  public int getWidthByColumnIndex( int columnIndex )
  {
    // return width from column index
    if ( columnIndex < 0 || columnIndex >= m_data.getColumnCount() )
      return Integer.MAX_VALUE;

    int width = m_defaultColumnWidth;
    if ( m_columnWidths.containsKey( columnIndex ) )
    {
      width = m_columnWidths.get( columnIndex );
      if ( width < 0 )
        return 0; // -ve means column hidden, so return zero
    }

    return width;
  }

  /*********************************** getRowPositionExactAtY ************************************/
  public int getRowPositionExactAtY( int y )
  {
    // return row position at specified y-coordinate, or -1 if before, MAX_INT if after
    y = y - m_offsetY - m_hHeaderHeight;
    if ( y < 0 )
      return -1;

    int last = m_data.getRowCount() - 1;
    for ( int rowPos = 0; rowPos <= last; rowPos++ )
    {
      y -= getHeightByRowPosition( rowPos );
      if ( y <= 0 )
        return rowPos;
    }

    return Integer.MAX_VALUE;
  }

  /************************************* getRowPositionAtY ***************************************/
  public int getRowPositionAtY( int y )
  {
    // return row position at specified y-coordinate, or nearest
    y = y - m_offsetY - m_hHeaderHeight;
    int last = m_data.getRowCount() - 1;
    for ( int rowPos = 0; rowPos <= last; rowPos++ )
    {
      y -= getHeightByRowPosition( rowPos );
      if ( y <= 0 )
        return rowPos;
    }

    return last;
  }

  /*********************************** getYStartByRowPosition ************************************/
  public int getYStartByRowPosition( int rowPos )
  {
    // return start-y of specified row position
    if ( rowPos > m_data.getRowCount() )
      rowPos = m_data.getRowCount();

    int startY = m_offsetY + m_hHeaderHeight;
    for ( int row = 0; row < rowPos; row++ )
      startY += getHeightByRowPosition( row );

    return startY;
  }

  /*********************************** getHeightByRowPosition ************************************/
  public int getHeightByRowPosition( int rowPos )
  {
    // return height from row position
    if ( rowPos < 0 || rowPos >= m_data.getRowCount() )
      return Integer.MAX_VALUE;

    int height = m_defaultRowHeight;
    int rowIndex = m_rowIndexes.get( rowPos );
    if ( m_rowHeights.containsKey( rowIndex ) )
    {
      height = m_rowHeights.get( rowIndex );
      if ( height < 0 )
        return 0; // -ve means row hidden, so return zero
    }

    return height;
  }

  /************************************* getHeightByRowIndex *************************************/
  public int getHeightByRowIndex( int rowIndex )
  {
    // return height from row index
    if ( rowIndex < 0 || rowIndex >= m_data.getRowCount() )
      return Integer.MAX_VALUE;

    int height = m_defaultRowHeight;
    if ( m_rowHeights.containsKey( rowIndex ) )
    {
      height = m_rowHeights.get( rowIndex );
      if ( height < 0 )
        return 0; // -ve means row hidden, so return zero
    }

    return height;
  }

  /************************************ setDefaultColumnWidth ************************************/
  public void setDefaultColumnWidth( int width )
  {
    m_defaultColumnWidth = width;
    calculateWidth();
  }

  /************************************* setDefaultRowHeight *************************************/
  public void setDefaultRowHeight( int height )
  {
    m_defaultRowHeight = height;
    calculateHeight();
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
  }

  /********************************** setWidthByColumnIndex **************************************/
  public void setWidthByColumnIndex( int columnIndex, int newWidth )
  {
    // width should not be below minimum
    if ( newWidth < m_minimumColumnWidth )
      newWidth = m_minimumColumnWidth;

    // record width so overrides default
    int oldWidth = getWidthByColumnIndex( columnIndex );
    m_columnWidths.put( columnIndex, newWidth );

    // if new width different to old width, update table canvas
    if ( newWidth != oldWidth )
    {
      m_width = m_width - oldWidth + newWidth;
      int start = getXStartByColumnPosition( getColumnPositionByIndex( columnIndex ) );
      m_canvas.drawWidth( start, m_canvas.getWidth() );
    }
  }

  /************************************ setHeightByRowIndex **************************************/
  public void setHeightByRowIndex( int rowIndex, int newHeight )
  {
    // height should not be below minimum
    if ( newHeight < m_minimumRowHeight )
      newHeight = m_minimumRowHeight;

    // record height so overrides default
    int oldHeight = getHeightByRowIndex( rowIndex );
    m_rowHeights.put( rowIndex, newHeight );

    // if new height different to old height, update table canvas
    if ( newHeight != oldHeight )
    {
      m_height = m_height - oldHeight + newHeight;
      int start = getYStartByRowPosition( getRowPositionByIndex( rowIndex ) );
      m_canvas.drawHeight( start, m_canvas.getWidth() );
    }
  }

  /********************************** getColumnIndexByPosition ***********************************/
  public int getColumnIndexByPosition( int columnPos )
  {
    // return column index from position
    return m_columnIndexes.get( columnPos );
  }

  /********************************** getColumnPositionByIndex ***********************************/
  public int getColumnPositionByIndex( int columnIndex )
  {
    // return column position from index
    return m_columnIndexes.indexOf( columnIndex );
  }

  /************************************ getRowIndexByPosition ************************************/
  public int getRowIndexByPosition( int rowPos )
  {
    // return row index from position
    return m_rowIndexes.get( rowPos );
  }

  /************************************ getRowPositionByIndex ************************************/
  public int getRowPositionByIndex( int rowIndex )
  {
    // return row position from index
    return m_rowIndexes.indexOf( rowIndex );
  }

}
