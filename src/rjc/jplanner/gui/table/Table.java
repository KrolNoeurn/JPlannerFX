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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import rjc.jplanner.JPlanner;

/*************************************************************************************************/
/***************************** Displays data in gui scrollable table *****************************/
/*************************************************************************************************/

public class Table extends GridPane
{
  public static final int           DEFAULT_ROW_HEIGHT               = 20;
  public static final int           DEFAULT_COLUMN_WIDTH             = 50;
  public static final int           DEFAULT_HORIZONTAL_HEADER_HEIGHT = DEFAULT_ROW_HEIGHT;
  public static final int           DEFAULT_VERTICAL_HEADER_WIDTH    = DEFAULT_COLUMN_WIDTH;

  public static final Color         COLOR_GRID                       = Color.SILVER;
  public static final Color         COLOR_HEADER_FILL                = Color.rgb( 240, 240, 240 );

  private ITableDataSource          m_data;
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

    m_cells.widthProperty().addListener( new ChangeListener<Number>()
    {
      @Override
      public void changed( ObservableValue<? extends Number> observable, Number oldValue, Number newValue )
      {
        // TODO Auto-generated method stub
        JPlanner.trace( "old=" + oldValue + "   new=" + newValue );
      }
    } );

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
