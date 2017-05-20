/**************************************************************************
 *  Copyright (C) 2017 by Richard Crook                                   *
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import rjc.jplanner.JPlanner;
import rjc.jplanner.XmlLabels;

/*************************************************************************************************/
/**************** Display gui scrollable table with horizontal & vertical header *****************/
/*************************************************************************************************/

public class Table extends TableDisplay
{
  private AbstractDataSource    m_data;                                             // data source for the table

  private int                   m_defaultRowHeight;
  private int                   m_defaultColumnWidth;
  private int                   m_minimumRowHeight;
  private int                   m_minimumColumnWidth;
  private int                   m_hHeaderHeight;
  private int                   m_vHeaderWidth;

  private int                   m_bodyWidth;                                        // body cells total width (excludes header)
  private int                   m_bodyHeight;                                       // body cells total height (excludes header)

  // all columns have default widths, and rows default heights, except those in these maps, -ve means hidden
  private Map<Integer, Integer> m_columnWidths    = new HashMap<Integer, Integer>();
  private Map<Integer, Integer> m_rowHeights      = new HashMap<Integer, Integer>();
  private ArrayList<Integer>    m_rowYStarts      = new ArrayList<Integer>();

  // set of collapsed rows (only used for collapsed tasks)
  private Set<Integer>          m_rowCollapsed    = new HashSet<Integer>();

  // array with mapping from position to index
  private ArrayList<Integer>    m_columnIndexes   = new ArrayList<Integer>();

  // set of body cells that are currently selected, where Integer = columnPos * SELECT_HASH + row
  private static final int      SELECT_HASH       = 9999;
  private Set<Integer>          m_selectedCells   = new HashSet<Integer>();
  private Set<Integer>          m_selectedRows    = new HashSet<Integer>();
  private Set<Integer>          m_selectedColumns = new HashSet<Integer>();

  public static enum Alignment// alignment of text to be drawn in cell
  {
    LEFT, MIDDLE, RIGHT
  }

  public String name; // table name helpful when debugging

  /**************************************** constructor ******************************************/
  public Table( String name, AbstractDataSource data )
  {
    // prepare table
    m_data = data;
    this.name = name;
    assemble( this );
    reset();
  }

  /******************************************** reset ********************************************/
  public void reset()
  {
    // set all table parameters to default
    m_defaultRowHeight = 20;
    m_defaultColumnWidth = 100;
    m_minimumRowHeight = 17;
    m_minimumColumnWidth = 40;
    m_hHeaderHeight = 20;
    m_vHeaderWidth = 30;

    m_columnWidths.clear();
    m_rowHeights.clear();
    m_rowCollapsed.clear();
    clearAllSelection();

    // prepare row-y-start array with row 0 starting at y=0
    m_rowYStarts.clear();
    m_rowYStarts.ensureCapacity( m_data.getRowCount() );
    m_rowYStarts.add( 0 );

    // initialise column position to index mapping
    m_columnIndexes.clear();
    int count = m_data.getColumnCount();
    for ( int column = 0; column < count; column++ )
      m_columnIndexes.add( column );

    // calculate body width & height
    calculateBodyWidth();
    calculateBodyHeight();

    // adopt table data source default table modifications
    m_data.defaultTableModifications( this );

    // set scroll bar
    resizeCanvasScrollBars();
    m_vScrollBar.setValue( 0.0 );
    m_hScrollBar.setValue( 0.0 );
  }

  /****************************************** relayout *******************************************/
  public void relayout()
  {
    // ensure array with mapping column position to index is correct size
    int count = m_data.getColumnCount();
    while ( m_columnIndexes.size() < count )
      m_columnIndexes.add( m_columnIndexes.size() );
    while ( m_columnIndexes.size() > count )
      m_columnIndexes.remove( Integer.valueOf( m_columnIndexes.size() - 1 ) );

    // re-calculate table size canvas for example after change in number of columns or rows
    calculateBodyHeight();
    calculateBodyWidth();
    resizeCanvasScrollBars();
  }

  /************************************ calculateBodyHeight **************************************/
  private void calculateBodyHeight()
  {
    // calculate height of table body cell rows
    int exceptionsCount = 0;
    int rowCount = m_data.getRowCount();
    m_bodyHeight = 0;
    for ( int row : m_rowHeights.keySet() )
    {
      if ( row < rowCount )
      {
        exceptionsCount++;
        int height = m_rowHeights.get( row );
        if ( height > 0 )
          m_bodyHeight += height;
      }
      else
        m_rowHeights.remove( row );
    }

    m_bodyHeight += ( rowCount - exceptionsCount ) * m_defaultRowHeight;
  }

  /************************************ calculateBodyWidth ***************************************/
  private void calculateBodyWidth()
  {
    // calculate width of table body cell columns
    int exceptionsCount = 0;
    int columnCount = m_data.getColumnCount();
    m_bodyWidth = 0;
    for ( int row : m_columnWidths.keySet() )
    {
      if ( row < columnCount )
      {
        exceptionsCount++;
        int width = m_columnWidths.get( row );
        if ( width > 0 )
          m_bodyWidth += width;
      }
      else
        m_columnWidths.remove( row );
    }

    m_bodyWidth += ( columnCount - exceptionsCount ) * m_defaultColumnWidth;
  }

  /****************************************** getData ********************************************/
  public AbstractDataSource getData()
  {
    // return data source for this table
    return m_data;
  }

  /**************************************** getTableWidth ****************************************/
  public int getTableWidth()
  {
    // return table width (might be smaller or larger than display node)
    return m_bodyWidth + m_vHeaderWidth;
  }

  /*************************************** getTableHeight ****************************************/
  public int getTableHeight()
  {
    // return table height (might be smaller or larger than display node)
    return m_bodyHeight + m_hHeaderHeight;
  }

  /**************************************** getBodyHeight ****************************************/
  public int getBodyHeight()
  {
    // return table body height (i.e. sum of height of cells without header)
    return m_bodyHeight;
  }

  /**************************************** getBodyWidth *****************************************/
  public int getBodyWidth()
  {
    // return table body height (i.e. sum of width of cells without header)
    return m_bodyWidth;
  }

  /********************************** getColumnPositionExactAtX **********************************/
  public int getColumnPositionExactAtX( int x )
  {
    // return column position at specified x-coordinate, or -1 if before, MAX_INT if after
    x += getHOffset() - m_vHeaderWidth;
    if ( x < 0 )
      return -1;

    int last = m_data.getColumnCount() - 1;
    for ( int columnPos = 0; columnPos <= last; columnPos++ )
    {
      int width = m_columnWidths.getOrDefault( m_columnIndexes.get( columnPos ), m_defaultColumnWidth );
      if ( width <= 0 )
        continue;
      x -= width;
      if ( x <= 0 )
        return columnPos;
    }

    return Integer.MAX_VALUE;
  }

  /************************************* getColumnPositionAtX ************************************/
  public int getColumnPositionAtX( int x )
  {
    // return column position at specified x-coordinate, or nearest
    x += getHOffset() - m_vHeaderWidth;
    int last = m_data.getColumnCount() - 1;
    for ( int columnPos = 0; columnPos <= last; columnPos++ )
    {
      int width = m_columnWidths.getOrDefault( m_columnIndexes.get( columnPos ), m_defaultColumnWidth );
      if ( width <= 0 )
        continue;
      x -= width;
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

    int startX = m_vHeaderWidth - getHOffset();
    for ( int column = 0; column < columnPos; column++ )
      startX += getWidthByColumnPosition( column );

    return startX;
  }

  /********************************** getWidthByColumnPosition ***********************************/
  public int getWidthByColumnPosition( int columnPos )
  {
    // return width from column position - bit slower
    if ( columnPos < 0 || columnPos >= m_data.getColumnCount() )
      return Integer.MAX_VALUE;

    int width = m_columnWidths.getOrDefault( m_columnIndexes.get( columnPos ), m_defaultColumnWidth );
    if ( width < 0 )
      return 0; // -ve means column hidden, so return zero

    return width;
  }

  /************************************ getWidthByColumnIndex ************************************/
  public int getWidthByColumnIndex( int columnIndex )
  {
    // return width from column index - bit faster
    if ( columnIndex < 0 || columnIndex >= m_data.getColumnCount() )
      return Integer.MAX_VALUE;

    int width = m_columnWidths.getOrDefault( columnIndex, m_defaultColumnWidth );
    if ( width < 0 )
      return 0; // -ve means column hidden, so return zero

    return width;
  }

  /*************************************** getRowExactAtY ****************************************/
  public int getRowExactAtY( int y )
  {
    // return row position at specified y-coordinate, or -1 if before, MAX_INT if after
    y += getVOffset() - m_hHeaderHeight;
    if ( y < 0 )
      return -1;

    int last = m_data.getRowCount() - 1;
    for ( int row = 0; row <= last; row++ )
    {
      int height = m_rowHeights.getOrDefault( row, m_defaultRowHeight );
      if ( height <= 0 )
        continue;
      y -= height;
      if ( y <= 0 )
        return row;
    }

    return Integer.MAX_VALUE;
  }

  /***************************************** getRowAtY *******************************************/
  public int getRowAtY( int y )
  {
    // return row position at specified y-coordinate, or nearest
    y += getVOffset() - m_hHeaderHeight;
    int last = m_data.getRowCount() - 1;
    for ( int row = 0; row <= last; row++ )
    {
      int height = m_rowHeights.getOrDefault( row, m_defaultRowHeight );
      if ( height <= 0 )
        continue;
      y -= height;
      if ( y <= 0 )
        return row;
    }

    return last;
  }

  /*************************************** getYStartByRow ****************************************/
  public int getYStartByRow( int row )
  {
    // if first row (or negative row number) return top of table body
    int startY = m_hHeaderHeight - getVOffset();
    if ( row <= 0 )
      return startY;

    // calculate any missing row-y-start array entries
    for ( int r = m_rowYStarts.size() - 1; r < row; r++ )
      m_rowYStarts.add( m_rowYStarts.get( r ) + getRowHeight( r ) );

    // return start-y of specified row position
    return startY + m_rowYStarts.get( row );
  }

  /**************************************** getRowHeight *****************************************/
  public int getRowHeight( int row )
  {
    // return height from row position
    if ( row < 0 || row >= m_data.getRowCount() )
      return Integer.MAX_VALUE;

    int height = m_rowHeights.getOrDefault( row, m_defaultRowHeight );
    if ( height < 0 )
      return 0; // -ve means row hidden, so return zero

    return height;
  }

  /************************************ setDefaultColumnWidth ************************************/
  public void setDefaultColumnWidth( int width )
  {
    m_defaultColumnWidth = width;
    calculateBodyWidth();
  }

  /************************************* setDefaultRowHeight *************************************/
  public void setDefaultRowHeight( int height )
  {
    m_defaultRowHeight = height;
    truncateRowYStarts( 0 );
    calculateBodyHeight();
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
    m_bodyWidth = m_bodyWidth - oldWidth + newWidth;
    m_columnWidths.put( columnIndex, newWidth );
  }

  /*************************************** setHeightByRow ****************************************/
  public void setHeightByRow( int row, int newHeight )
  {
    // height should not be below minimum
    if ( newHeight < m_minimumRowHeight )
      newHeight = m_minimumRowHeight;

    // if height is not changed, return doing nothing
    int oldHeight = getRowHeight( row );
    if ( newHeight == oldHeight )
      return;

    // record new height
    m_bodyHeight = m_bodyHeight - oldHeight + newHeight;
    if ( newHeight == m_defaultRowHeight )
      m_rowHeights.remove( row );
    else
      m_rowHeights.put( row, newHeight );
    truncateRowYStarts( row );
  }

  /************************************* truncateRowYStarts **************************************/
  private void truncateRowYStarts( int row )
  {
    // truncate m_rowYStarts cache to specified row
    if ( ++row > m_rowYStarts.size() )
      return;

    m_rowYStarts.subList( row, m_rowYStarts.size() ).clear();
  }

  /********************************** getColumnIndexByPosition ***********************************/
  public int getColumnIndexByPosition( int columnPos )
  {
    // return column index from position (faster)
    return m_columnIndexes.get( columnPos );
  }

  /********************************** getColumnPositionByIndex ***********************************/
  public int getColumnPositionByIndex( int columnIndex )
  {
    // return column position from index (slower)
    return m_columnIndexes.indexOf( columnIndex );
  }

  /****************************************** scrollTo *******************************************/
  public void scrollTo( int columnPos, int row )
  {
    // determine if any horizontal scrolling needed
    int leftEdge = getXStartByColumnPosition( columnPos ) - m_vHeaderWidth;
    if ( leftEdge < 0 )
      animateToHOffset( getHOffset() + leftEdge );
    else
    {
      int rightEdge = leftEdge + getWidthByColumnPosition( columnPos ) + m_vHeaderWidth;
      if ( rightEdge > getCanvasWidth() )
        animateToHOffset( getHOffset() + rightEdge - getCanvasWidth() );
    }

    // determine if any vertical scrolling needed, finishing any horizontal scrolling first
    int topEdge = getYStartByRow( row ) - m_hHeaderHeight;
    if ( topEdge < 0 )
    {
      finishAnimation();
      animateToVOffset( getVOffset() + topEdge );
    }
    else
    {
      int bottomEdge = topEdge + getRowHeight( row ) + m_hHeaderHeight;
      if ( bottomEdge > getCanvasHeight() )
      {
        finishAnimation();
        animateToVOffset( getVOffset() + bottomEdge - getCanvasHeight() );
      }
    }
  }

  /******************************************* hideRow *******************************************/
  public void hideRow( int row )
  {
    // if already hidden do nothing
    int oldHeight = m_rowHeights.getOrDefault( row, m_defaultRowHeight );
    if ( oldHeight < 0 )
      return;

    m_rowHeights.put( row, -oldHeight );
    truncateRowYStarts( row );
    m_bodyHeight = m_bodyHeight - oldHeight;
  }

  /******************************************* showRow *******************************************/
  public void showRow( int row )
  {
    // if already shown do nothing
    int oldHeight = m_rowHeights.getOrDefault( row, m_defaultRowHeight );
    if ( oldHeight > 0 )
      return;

    m_rowHeights.put( row, -oldHeight );
    truncateRowYStarts( row );
    m_bodyHeight = m_bodyHeight - oldHeight;
  }

  /***************************************** moveColumn ******************************************/
  public void moveColumn( int oldPos, int newPos )
  {
    // move column index from old position to new position
    int index = m_columnIndexes.remove( oldPos );
    m_columnIndexes.add( newPos, index );
  }

  /******************************************* moveRow *******************************************/
  public void moveRow( int oldPos, int newPos )
  {
    // move row from old position to new position

    // TODO
  }

  /************************************** clearAllSelection **************************************/
  public void clearAllSelection()
  {
    // clear selection from all cells
    m_selectedCells.clear();
    m_selectedRows.clear();
    m_selectedColumns.clear();
  }

  /***************************************** isSelected ******************************************/
  public boolean isSelected( int columnPos, int row )
  {
    // return true if specified row is selected
    if ( m_selectedRows.contains( row ) )
      return true;

    // return true if specified column-position is selected
    if ( m_selectedColumns.contains( columnPos ) )
      return true;

    // return true if specified body cell is selected
    return m_selectedCells.contains( columnPos * SELECT_HASH + row );
  }

  /************************************ doesRowHaveSelection *************************************/
  public boolean doesRowHaveSelection( int row )
  {
    // return true if any column selected
    if ( !m_selectedColumns.isEmpty() )
      return true;

    // return true if specified row is selected
    if ( m_selectedRows.contains( row ) )
      return true;

    // return true if any selected body cells on specified row
    for ( int hash : m_selectedCells )
      if ( hash % SELECT_HASH == row )
        return true;

    return false;
  }

  /**************************************** isRowSelected ****************************************/
  public boolean isRowSelected( int row )
  {
    // return true if specified row is selected
    if ( m_selectedRows.contains( row ) )
      return true;

    // return true if all visible columns selected
    int count = getData().getColumnCount();
    boolean all = true;
    for ( int columnPos = 0; columnPos < count; columnPos++ )
      if ( m_columnWidths.getOrDefault( m_columnIndexes.get( columnPos ), m_defaultColumnWidth ) > 0
          && !m_selectedColumns.contains( columnPos ) )
      {
        all = false;
        break;
      }
    if ( all )
      return true;

    // return true if every visible body cell in row is selected
    for ( int columnPos = 0; columnPos < count; columnPos++ )
      if ( m_columnWidths.getOrDefault( m_columnIndexes.get( columnPos ), m_defaultColumnWidth ) > 0
          && !isSelected( columnPos, row ) )
        return false;

    return true;
  }

  /*********************************** doesColumnHaveSelection ***********************************/
  public boolean doesColumnHaveSelection( int columnPos )
  {
    // return true if any row selected
    if ( !m_selectedRows.isEmpty() )
      return true;

    // return true if specified column-position is selected
    if ( m_selectedColumns.contains( columnPos ) )
      return true;

    // return true if any selected body cells on specified column
    for ( int hash : m_selectedCells )
      if ( hash / SELECT_HASH == columnPos )
        return true;

    return false;
  }

  /************************************** isColumnSelected ***************************************/
  public boolean isColumnSelected( int columnPos )
  {
    // return true if specified column-position is selected
    if ( m_selectedColumns.contains( columnPos ) )
      return true;

    // return true if all visible rows selected
    int count = getData().getRowCount();
    boolean all = true;
    for ( int row = 0; row < count; row++ )
      if ( m_rowHeights.getOrDefault( row, m_defaultRowHeight ) > 0 && !m_selectedRows.contains( row ) )
      {
        all = false;
        break;
      }
    if ( all )
      return true;

    // return true if every visible body cell in column is selected
    for ( int row = 0; row < count; row++ )
      if ( m_rowHeights.getOrDefault( row, m_defaultRowHeight ) > 0 && !isSelected( columnPos, row ) )
        return false;

    return true;
  }

  /******************************************* select *******************************************/
  public void select( int columnPos, int row, boolean selected )
  {
    // set whether specified body cell is selected
    if ( selected )
      m_selectedCells.add( columnPos * SELECT_HASH + row );
    else
      m_selectedCells.remove( columnPos * SELECT_HASH + row );
  }

  /***************************************** selectRows ******************************************/
  public void selectRows( int row1, int row2, boolean selected )
  {
    // ensure row positions are within bounds
    row1 = clamp( row1, 0, m_data.getRowCount() - 1 );
    row2 = clamp( row2, 0, m_data.getRowCount() - 1 );

    // determine min & max positions
    int r1 = Math.min( row1, row2 );
    int r2 = Math.max( row1, row2 );

    for ( int row = r1; row <= r2; row++ )
    {
      // set whether specified table row is selected
      if ( selected )
        m_selectedRows.add( row );
      else
        m_selectedRows.remove( row );
    }
  }

  /**************************************** selectColumns ****************************************/
  public void selectColumns( int columnPos1, int columnPos2, boolean selected )
  {
    // ensure column and row positions are within bounds
    columnPos1 = clamp( columnPos1, 0, m_data.getColumnCount() - 1 );
    columnPos2 = clamp( columnPos2, 0, m_data.getColumnCount() - 1 );

    // determine min & max positions
    int c1 = Math.min( columnPos1, columnPos2 );
    int c2 = Math.max( columnPos1, columnPos2 );

    // set whether specified table region is selected
    for ( int column = c1; column <= c2; column++ )
    {
      // set whether specified table column is selected
      if ( selected )
        m_selectedColumns.add( column );
      else
        m_selectedColumns.remove( column );
    }
  }

  /******************************************* select *******************************************/
  public void select( int columnPos1, int row1, int columnPos2, int row2, boolean selected )
  {
    // ensure column and row positions are within bounds
    columnPos1 = clamp( columnPos1, 0, m_data.getColumnCount() - 1 );
    columnPos2 = clamp( columnPos2, 0, m_data.getColumnCount() - 1 );
    row1 = clamp( row1, 0, m_data.getRowCount() - 1 );
    row2 = clamp( row2, 0, m_data.getRowCount() - 1 );

    // determine min & max positions
    int c1 = Math.min( columnPos1, columnPos2 );
    int c2 = Math.max( columnPos1, columnPos2 );
    int r1 = Math.min( row1, row2 );
    int r2 = Math.max( row1, row2 );

    // set whether specified table region is selected
    for ( int column = c1; column <= c2; column++ )
      for ( int row = r1; row <= r2; row++ )
      {
        if ( selected )
          m_selectedCells.add( column * SELECT_HASH + row );
        else
          m_selectedCells.remove( column * SELECT_HASH + row );
      }
  }

  public static int clamp( int val, int min, int max )
  {
    return Math.max( min, Math.min( max, val ) );
  }

  /*************************************** getSelectedRows ***************************************/
  public Set<Integer> getSelectedRows()
  {
    // return set of selected row indexes
    Set<Integer> rows = new HashSet<Integer>();

    for ( int hash : m_selectedCells )
      rows.add( hash % SELECT_HASH );

    return rows;
  }

  /****************************************** writeXML *******************************************/
  public void writeXML( XMLStreamWriter xsw ) throws XMLStreamException
  {
    // write editor cell
    xsw.writeAttribute( XmlLabels.XML_COLUMN, Integer.toString( getCanvas().getEditCellColumnPosition() ) );
    xsw.writeAttribute( XmlLabels.XML_ROW, Integer.toString( getCanvas().getEditCellRow() ) );

    // write column widths (all)
    xsw.writeStartElement( XmlLabels.XML_COLUMNS );
    xsw.writeAttribute( XmlLabels.XML_WIDTH, Integer.toString( m_defaultColumnWidth ) );
    xsw.writeAttribute( XmlLabels.XML_SCROLL, Integer.toString( (int) m_hScrollBar.getValue() ) );
    int count = m_data.getColumnCount();
    for ( int columnIndex = 0; columnIndex < count; columnIndex++ )
    {
      xsw.writeEmptyElement( XmlLabels.XML_COLUMN );
      xsw.writeAttribute( XmlLabels.XML_ID, Integer.toString( columnIndex ) );

      int width = m_columnWidths.getOrDefault( columnIndex, m_defaultColumnWidth );
      if ( width != m_defaultColumnWidth )
        xsw.writeAttribute( XmlLabels.XML_WIDTH, Integer.toString( width ) );

      int columnPos = getColumnPositionByIndex( columnIndex );
      xsw.writeAttribute( XmlLabels.XML_POSITION, Integer.toString( columnPos ) );

      if ( isColumnSelected( columnPos ) )
        xsw.writeAttribute( XmlLabels.XML_SELECTED, Boolean.toString( true ) );
    }
    xsw.writeEndElement(); // XML_COLUMNS

    // write row heights (only those with non-default-height or collapsed)
    xsw.writeStartElement( XmlLabels.XML_ROWS );
    xsw.writeAttribute( XmlLabels.XML_HEIGHT, Integer.toString( m_defaultRowHeight ) );
    xsw.writeAttribute( XmlLabels.XML_SCROLL, Integer.toString( (int) m_vScrollBar.getValue() ) );
    count = m_data.getRowCount();
    for ( int row = 0; row < count; row++ )
    {
      int height = m_rowHeights.getOrDefault( row, m_defaultRowHeight );
      boolean collapsed = m_rowCollapsed.contains( row );

      if ( height != m_defaultRowHeight || collapsed || isRowSelected( row ) )
      {
        xsw.writeEmptyElement( XmlLabels.XML_ROW );
        xsw.writeAttribute( XmlLabels.XML_ID, Integer.toString( row ) );

        if ( height != m_defaultRowHeight )
          xsw.writeAttribute( XmlLabels.XML_HEIGHT, Integer.toString( m_rowHeights.get( row ) ) );

        if ( collapsed )
          xsw.writeAttribute( XmlLabels.XML_COLLAPSED, "true" );

        if ( isRowSelected( row ) )
          xsw.writeAttribute( XmlLabels.XML_SELECTED, Boolean.toString( true ) );
      }
    }
    xsw.writeEndElement(); // XML_ROWS

    // write any selected cells
    for ( int hash : m_selectedCells )
    {
      xsw.writeEmptyElement( XmlLabels.XML_SELECTED );
      xsw.writeAttribute( XmlLabels.XML_COLUMN, Integer.toString( hash / SELECT_HASH ) );
      xsw.writeAttribute( XmlLabels.XML_ROW, Integer.toString( hash % SELECT_HASH ) );
    }

  }

  /************************************** loadXML ***************************************/
  public void loadXML( XMLStreamReader xsr ) throws XMLStreamException
  {
    // determine element end to exit on
    String element = xsr.getLocalName();

    // read any attributes
    for ( int i = 0; i < xsr.getAttributeCount(); i++ )
      switch ( xsr.getAttributeLocalName( i ) )
      {
        case XmlLabels.XML_COLUMN:
          getCanvas().setEditCellColumnPosition( Integer.parseInt( xsr.getAttributeValue( i ) ) );
          break;
        case XmlLabels.XML_ROW:
          getCanvas().setEditCellRow( Integer.parseInt( xsr.getAttributeValue( i ) ) );
          break;
        default:
          JPlanner.trace( "Unhandled attribute '" + xsr.getAttributeLocalName( i ) + "'" );
          break;
      }

    // read table XML elements
    while ( xsr.hasNext() )
    {
      xsr.next();

      // if reached end of tab data, break
      if ( xsr.isEndElement() && xsr.getLocalName().equals( element ) )
        break;

      if ( xsr.isStartElement() )
        switch ( xsr.getLocalName() )
        {
          case XmlLabels.XML_COLUMNS:
            loadColumns( xsr );
            break;
          case XmlLabels.XML_ROWS:
            loadRows( xsr );
            break;
          case XmlLabels.XML_SELECTED:
            loadSelected( xsr );
            break;
          default:
            JPlanner.trace( "Unhandled start element '" + xsr.getLocalName() + "'" );
            break;
        }
    }
  }

  /***************************************** loadColumns *****************************************/
  public void loadColumns( XMLStreamReader xsr ) throws XMLStreamException
  {
    // read XML columns attributes
    for ( int i = 0; i < xsr.getAttributeCount(); i++ )
      switch ( xsr.getAttributeLocalName( i ) )
      {
        case XmlLabels.XML_WIDTH:
          setDefaultColumnWidth( Integer.parseInt( xsr.getAttributeValue( i ) ) );
          break;
        case XmlLabels.XML_SCROLL:
          m_hScrollBar.setValue( Integer.parseInt( xsr.getAttributeValue( i ) ) );
          break;
        default:
          JPlanner.trace( "Unhandled attribute '" + xsr.getAttributeLocalName( i ) + "'" );
          break;
      }

    // read XML individual columns
    ArrayList<Integer> selectedIndex = new ArrayList<Integer>();
    boolean selected;
    while ( xsr.hasNext() )
    {
      xsr.next();

      // if reached end of columns data, exit loop
      if ( xsr.isEndElement() && xsr.getLocalName().equals( XmlLabels.XML_COLUMNS ) )
      {
        // select columns from indexes to positions
        selectedIndex.forEach(
            index -> selectColumns( getColumnPositionByIndex( index ), getColumnPositionByIndex( index ), true ) );
        break;
      }

      if ( xsr.isStartElement() )
        switch ( xsr.getLocalName() )
        {
          case XmlLabels.XML_COLUMN:

            // get attributes from column element to set column width
            int id = -1;
            for ( int i = 0; i < xsr.getAttributeCount(); i++ )
              switch ( xsr.getAttributeLocalName( i ) )
              {
                case XmlLabels.XML_ID:
                  id = Integer.parseInt( xsr.getAttributeValue( i ) );
                  break;
                case XmlLabels.XML_WIDTH:
                  m_columnWidths.put( id, Integer.parseInt( xsr.getAttributeValue( i ) ) );
                  break;
                case XmlLabels.XML_POSITION:
                  m_columnIndexes.set( Integer.parseInt( xsr.getAttributeValue( i ) ), id );
                  break;
                case XmlLabels.XML_SELECTED:
                  selected = Boolean.parseBoolean( xsr.getAttributeValue( i ) );
                  if ( selected )
                    selectedIndex.add( id );
                  break;
                default:
                  JPlanner.trace( "Unhandled attribute '" + xsr.getAttributeLocalName( i ) + "'" );
                  break;
              }
            break;

          default:
            JPlanner.trace( "Unhandled start element '" + xsr.getLocalName() + "'" );
            break;
        }
    }

    // refresh calculated body width
    calculateBodyWidth();
  }

  /****************************************** loadRows *******************************************/
  public void loadRows( XMLStreamReader xsr ) throws XMLStreamException
  {
    // read XML rows attributes
    for ( int i = 0; i < xsr.getAttributeCount(); i++ )
      switch ( xsr.getAttributeLocalName( i ) )
      {
        case XmlLabels.XML_HEIGHT:
          setDefaultRowHeight( Integer.parseInt( xsr.getAttributeValue( i ) ) );
          break;
        case XmlLabels.XML_SCROLL:
          m_vScrollBar.setValue( Integer.parseInt( xsr.getAttributeValue( i ) ) );
          break;
        default:
          JPlanner.trace( "Unhandled attribute '" + xsr.getAttributeLocalName( i ) + "'" );
          break;
      }

    // read XML individual rows
    while ( xsr.hasNext() )
    {
      xsr.next();

      // if reached end of rows data, exit loop
      if ( xsr.isEndElement() && xsr.getLocalName().equals( XmlLabels.XML_ROWS ) )
        break;

      if ( xsr.isStartElement() )
        switch ( xsr.getLocalName() )
        {
          case XmlLabels.XML_ROW:

            // get attributes from row element to set row height and hidden
            int id = -1;
            for ( int i = 0; i < xsr.getAttributeCount(); i++ )
              switch ( xsr.getAttributeLocalName( i ) )
              {
                case XmlLabels.XML_ID:
                  id = Integer.parseInt( xsr.getAttributeValue( i ) );
                  break;
                case XmlLabels.XML_HEIGHT:
                  m_rowHeights.put( id, Integer.parseInt( xsr.getAttributeValue( i ) ) );
                  break;
                case XmlLabels.XML_COLLAPSED:
                  if ( Boolean.parseBoolean( xsr.getAttributeValue( i ) ) )
                    m_rowCollapsed.add( id );
                  break;
                case XmlLabels.XML_SELECTED:
                  selectRows( id, id, Boolean.parseBoolean( xsr.getAttributeValue( i ) ) );
                  break;
                default:
                  JPlanner.trace( "Unhandled attribute '" + xsr.getAttributeLocalName( i ) + "'" );
                  break;
              }
            break;

          default:
            JPlanner.trace( "Unhandled start element '" + xsr.getLocalName() + "'" );
            break;
        }
    }

    // refresh calculated body height
    truncateRowYStarts( 0 );
    calculateBodyHeight();
  }

  /**************************************** loadSelected *****************************************/
  public void loadSelected( XMLStreamReader xsr ) throws XMLStreamException
  {
    // get attributes from position for selected cells
    int column = -1;
    int row = -1;
    for ( int i = 0; i < xsr.getAttributeCount(); i++ )
      switch ( xsr.getAttributeLocalName( i ) )
      {
        case XmlLabels.XML_COLUMN:
          column = Integer.parseInt( xsr.getAttributeValue( i ) );
          break;
        case XmlLabels.XML_ROW:
          row = Integer.parseInt( xsr.getAttributeValue( i ) );
          break;
        default:
          JPlanner.trace( "Unhandled attribute '" + xsr.getAttributeLocalName( i ) + "'" );
          break;
      }

    if ( column >= 0 && row >= 0 )
      select( column, row, true );
  }

  /*************************************** isRowCollapsed ****************************************/
  public boolean isRowCollapsed( int row )
  {
    // return true if the summary on specified row is collapsed
    return m_rowCollapsed.contains( row );
  }

  /************************************** collapseSummary ****************************************/
  public void collapseSummary( int summaryRow )
  {
    // check row is plan summary task
    if ( !m_data.isCellSummary( 0, summaryRow ) )
      throw new IllegalArgumentException( "Task " + summaryRow + " is not a summary!" );

    // hide all summary sub-tasks
    int endRow = m_data.getSummaryEndRow( 0, summaryRow );
    for ( int row = summaryRow + 1; row <= endRow; row++ )
      hideRow( row );

    // mark summary as collapsed
    m_rowCollapsed.add( summaryRow );
  }

  /**************************************** expandSummary ****************************************/
  public void expandSummary( int summaryRow )
  {
    // check row is plan summary task
    if ( !m_data.isCellSummary( 0, summaryRow ) )
      throw new IllegalArgumentException( "Task " + summaryRow + " is not a summary!" );

    // show all summary sub-tasks
    int endRow = m_data.getSummaryEndRow( 0, summaryRow );
    for ( int row = summaryRow + 1; row <= endRow; row++ )
    {
      showRow( row );

      // except those of collapsed sub-summaries, skip over these
      if ( isRowCollapsed( row ) )
        row = m_data.getSummaryEndRow( 0, row );
    }

    // remove summary as collapsed
    m_rowCollapsed.remove( summaryRow );
  }

  /**************************************** getCollapsed *****************************************/
  public Set<Integer> getCollapsed()
  {
    // return collapsed summary rows
    return m_rowCollapsed;
  }

  /**************************************** setCollapsed *****************************************/
  public void setCollapsed( Set<Integer> rows )
  {
    // show all hidden rows (except row 0)
    m_rowHeights.forEach( ( row, height ) ->
    {
      if ( row > 0 && height < 0 )
      {
        m_rowHeights.put( row, -height );
        m_bodyHeight = m_bodyHeight - height;
      }
    } );

    // collapse all valid summaries
    rows.forEach( row ->
    {
      if ( m_data.isCellSummary( 0, row ) )
        collapseSummary( row );
    } );

    truncateRowYStarts( 0 );
    resizeCanvasScrollBars();
  }

  /******************************** getVisibleColumnPositionRight ********************************/
  public int getVisibleColumnPositionRight( int columnPos )
  {
    // return visible column-position to right, or if none, this one
    int right = columnPos + 1;
    while ( getWidthByColumnPosition( right ) <= 0 )
      right++;

    if ( right < m_data.getColumnCount() )
      return right;

    return columnPos;
  }

  /******************************** getVisibleColumnPositionLeft *********************************/
  public int getVisibleColumnPositionLeft( int columnPos )
  {
    // return visible column-position to left, or if none, this one
    int left = columnPos - 1;
    while ( getWidthByColumnPosition( left ) <= 0 )
      left--;

    if ( left >= 0 )
      return left;

    return columnPos;
  }

  /************************************* getVisibleRowAbove **************************************/
  public int getVisibleRowAbove( int row )
  {
    // return visible row above, or if none, this one
    int above = row - 1;
    while ( getRowHeight( above ) <= 0 )
      above--;

    if ( above >= 0 )
      return above;

    return row;
  }

  /************************************* getVisibleRowBelow **************************************/
  public int getVisibleRowBelow( int row )
  {
    // return visible row below, or if none, this one
    int below = row + 1;
    while ( getRowHeight( below ) <= 0 )
      below++;

    if ( below < m_data.getRowCount() )
      return below;

    return row;
  }

}
