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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import rjc.jplanner.JPlanner;
import rjc.jplanner.XmlLabels;
import rjc.jplanner.gui.table.CellEditor.MoveDirection;

/*************************************************************************************************/
/**************** Display gui scrollable table with horizontal & vertical header *****************/
/*************************************************************************************************/

public class Table extends TableDisplay
{
  private ITableDataSource          m_data;                                                // data source for the table

  private int                       m_defaultRowHeight   = 20;
  private int                       m_defaultColumnWidth = 100;
  private int                       m_minimumRowHeight   = 17;
  private int                       m_minimumColumnWidth = 40;
  private int                       m_hHeaderHeight      = 20;
  private int                       m_vHeaderWidth       = 30;

  private int                       m_bodyWidth          = 0;                              // body cells total width (excludes header)
  private int                       m_bodyHeight         = 0;                              // body cells total height (excludes header)

  // all columns have default widths, and rows default heights, except those in these maps, -ve means hidden
  private HashMap<Integer, Integer> m_columnWidths       = new HashMap<Integer, Integer>();
  private HashMap<Integer, Integer> m_rowHeights         = new HashMap<Integer, Integer>();

  // set of collapsed rows (only used for collapsed tasks)
  private HashSet<Integer>          m_rowCollapsed       = new HashSet<Integer>();

  // array with mapping from position to index
  private ArrayList<Integer>        m_columnIndexes      = new ArrayList<Integer>();
  private ArrayList<Integer>        m_rowIndexes         = new ArrayList<Integer>();

  // set of body cells that are currently selected, where Integer = columnPos * SELECT_HASH + rowPos
  private static final int          SELECT_HASH          = 9999;
  private HashSet<Integer>          m_selected           = new HashSet<Integer>();

  public static enum Alignment// alignment of text to be drawn in cell
  {
    LEFT, MIDDLE, RIGHT
  }

  public String name; // table name helpful when debugging

  /**************************************** constructor ******************************************/
  public Table( String name, ITableDataSource data )
  {
    // prepare table
    m_data = data;
    this.name = name;
    initialiseDisplay( this );

    // initialise column & row position to index mapping
    int count = m_data.getColumnCount();
    for ( int column = 0; column < count; column++ )
      m_columnIndexes.add( column );
    count = m_data.getRowCount();
    for ( int row = 0; row < count; row++ )
      m_rowIndexes.add( row );

    // calculate body width & height
    calculateBodyWidth();
    calculateBodyHeight();
  }

  /*************************************** getDataSource *****************************************/
  public ITableDataSource getDataSource()
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
    x += getHOffset() - m_vHeaderWidth;
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

  /*********************************** getRowPositionExactAtY ************************************/
  public int getRowPositionExactAtY( int y )
  {
    // return row position at specified y-coordinate, or -1 if before, MAX_INT if after
    y += getVOffset() - m_hHeaderHeight;
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
    y += getVOffset() - m_hHeaderHeight;
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

    int startY = m_hHeaderHeight - getVOffset();
    for ( int row = 0; row < rowPos; row++ )
      startY += getHeightByRowPosition( row );

    return startY;
  }

  /*********************************** getHeightByRowPosition ************************************/
  public int getHeightByRowPosition( int rowPos )
  {
    // return height from row position - bit slower
    if ( rowPos < 0 || rowPos >= m_data.getRowCount() )
      return Integer.MAX_VALUE;

    int height = m_rowHeights.getOrDefault( m_rowIndexes.get( rowPos ), m_defaultRowHeight );
    if ( height < 0 )
      return 0; // -ve means row hidden, so return zero

    return height;
  }

  /************************************* getHeightByRowIndex *************************************/
  public int getHeightByRowIndex( int rowIndex )
  {
    // return height from row index - bit faster
    if ( rowIndex < 0 || rowIndex >= m_data.getRowCount() )
      return Integer.MAX_VALUE;

    int height = m_rowHeights.getOrDefault( rowIndex, m_defaultRowHeight );
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

  /************************************ setHeightByRowIndex **************************************/
  public void setHeightByRowIndex( int rowIndex, int newHeight )
  {
    // height should not be below minimum
    if ( newHeight < m_minimumRowHeight )
      newHeight = m_minimumRowHeight;

    // record height so overrides default
    int oldHeight = getHeightByRowIndex( rowIndex );
    m_bodyHeight = m_bodyHeight - oldHeight + newHeight;
    m_rowHeights.put( rowIndex, newHeight );
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

  /****************************************** scrollTo *******************************************/
  public void scrollTo( int columnPos, int rowPos )
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
    int topEdge = getYStartByRowPosition( rowPos ) - m_hHeaderHeight;
    if ( topEdge < 0 )
    {
      finishAnimation();
      animateToVOffset( getVOffset() + topEdge );
    }
    else
    {
      int bottomEdge = topEdge + getHeightByRowPosition( rowPos ) + m_hHeaderHeight;
      if ( bottomEdge > getCanvasHeight() )
      {
        finishAnimation();
        animateToVOffset( getVOffset() + bottomEdge - getCanvasHeight() );
      }
    }
  }

  /******************************************** reset ********************************************/
  public void reset()
  {
    // reset table canvas for example after change in number of columns or rows
    calculateBodyHeight();
    calculateBodyWidth();
    setCanvasScrollBars();
    redraw();
  }

  /******************************************* hideRow *******************************************/
  public void hideRow( int rowIndex )
  {
    // get old height
    int oldHeight = m_defaultRowHeight;
    if ( m_rowHeights.containsKey( rowIndex ) )
      oldHeight = m_rowHeights.get( rowIndex );

    // if already hidden do nothing
    if ( oldHeight < 0 )
      return;

    m_rowHeights.put( rowIndex, -oldHeight );
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
    // move row index from old position to new position
    int index = m_rowIndexes.remove( oldPos );
    m_rowIndexes.add( newPos, index );
  }

  /************************************** clearAllSelection **************************************/
  public void clearAllSelection()
  {
    // clear selection from all cells
    m_selected.clear();
  }

  /**************************************** selectionCount ***************************************/
  public int selectionCount()
  {
    // return number of selected cells
    return m_selected.size();
  }

  /***************************************** isSelected ******************************************/
  public boolean isSelected( int columnPos, int rowPos )
  {
    // return true if specified body cell is selected
    return m_selected.contains( columnPos * SELECT_HASH + rowPos );
  }

  /************************************ doesRowHaveSelection *************************************/
  public boolean doesRowHaveSelection( int rowPos )
  {
    // return true if any selected body cells on specified row
    for ( int hash : m_selected )
      if ( hash % SELECT_HASH == rowPos )
        return true;

    return false;
  }

  /************************************** isRowAllSelected ***************************************/
  public boolean isRowAllSelected( int rowPos )
  {
    // return true if every body cell in row is selected
    int num = getDataSource().getColumnCount();
    for ( int columnPos = 0; columnPos < num; columnPos++ )
      if ( !isSelected( columnPos, rowPos ) )
        return false;

    return true;
  }

  /*********************************** doesColumnHaveSelection ***********************************/
  public boolean doesColumnHaveSelection( int columnPos )
  {
    // return true if any selected body cells on specified column
    for ( int hash : m_selected )
      if ( hash / SELECT_HASH == columnPos )
        return true;

    return false;
  }

  /************************************* isColumnAllSelected *************************************/
  public boolean isColumnAllSelected( int columnPos )
  {
    // return true if every body cell in column is selected
    int num = getDataSource().getRowCount();
    for ( int rowPos = 0; rowPos < num; rowPos++ )
      if ( !isSelected( columnPos, rowPos ) )
        return false;

    return true;
  }

  /**************************************** setSelection *****************************************/
  public void setSelection( int columnPos, int rowPos, boolean selected )
  {
    // set whether specified body cell is selected
    if ( selected )
      m_selected.add( columnPos * SELECT_HASH + rowPos );
    else
      m_selected.remove( columnPos * SELECT_HASH + rowPos );
  }

  /*************************************** setRowSelection ***************************************/
  public void setRowSelection( int rowPos, boolean selected )
  {
    // set whether specified table row is selected
    int num = getDataSource().getColumnCount();
    if ( selected )
      for ( int columnPos = 0; columnPos < num; columnPos++ )
        m_selected.add( columnPos * SELECT_HASH + rowPos );
    else
      for ( int columnPos = 0; columnPos < num; columnPos++ )
        m_selected.remove( columnPos * SELECT_HASH + rowPos );
  }

  /************************************* setColumnSelection **************************************/
  public void setColumnSelection( int columnPos, boolean selected )
  {
    // set whether specified table column is selected
    int num = getDataSource().getRowCount();
    if ( selected )
      for ( int rowPos = 0; rowPos < num; rowPos++ )
        m_selected.add( columnPos * SELECT_HASH + rowPos );
    else
      for ( int rowPos = 0; rowPos < num; rowPos++ )
        m_selected.remove( columnPos * SELECT_HASH + rowPos );
  }

  /****************************************** writeXML *******************************************/
  public void writeXML( XMLStreamWriter xsw ) throws XMLStreamException
  {
    // write column widths
    xsw.writeStartElement( XmlLabels.XML_COLUMNS );
    xsw.writeAttribute( XmlLabels.XML_WIDTH, Integer.toString( m_defaultColumnWidth ) );
    xsw.writeAttribute( XmlLabels.XML_SCROLL, Integer.toString( (int) m_hScrollBar.getValue() ) );
    int count = m_data.getColumnCount();
    for ( int columnIndex = 0; columnIndex < count; columnIndex++ )
    {
      xsw.writeStartElement( XmlLabels.XML_COLUMN );
      xsw.writeAttribute( XmlLabels.XML_ID, Integer.toString( columnIndex ) );

      if ( m_columnWidths.containsKey( columnIndex ) )
        xsw.writeAttribute( XmlLabels.XML_WIDTH, Integer.toString( m_columnWidths.get( columnIndex ) ) );

      xsw.writeAttribute( XmlLabels.XML_POSITION, Integer.toString( getColumnPositionByIndex( columnIndex ) ) );
      xsw.writeEndElement(); // XML_COLUMN
    }
    xsw.writeEndElement(); // XML_COLUMNS

    // write row heights
    xsw.writeStartElement( XmlLabels.XML_ROWS );
    xsw.writeAttribute( XmlLabels.XML_HEIGHT, Integer.toString( m_defaultRowHeight ) );
    xsw.writeAttribute( XmlLabels.XML_SCROLL, Integer.toString( (int) m_vScrollBar.getValue() ) );
    count = m_data.getRowCount();
    for ( int rowIndex = 0; rowIndex < count; rowIndex++ )
    {
      xsw.writeStartElement( XmlLabels.XML_ROW );
      xsw.writeAttribute( XmlLabels.XML_ID, Integer.toString( rowIndex ) );

      if ( m_rowHeights.containsKey( rowIndex ) )
        xsw.writeAttribute( XmlLabels.XML_HEIGHT, Integer.toString( m_rowHeights.get( rowIndex ) ) );

      if ( m_rowCollapsed.contains( rowIndex ) )
        xsw.writeAttribute( XmlLabels.XML_COLLAPSED, "true" );

      xsw.writeAttribute( XmlLabels.XML_POSITION, Integer.toString( getRowPositionByIndex( rowIndex ) ) );
      xsw.writeEndElement(); // XML_ROW
    }
    xsw.writeEndElement(); // XML_ROWS
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
    while ( xsr.hasNext() )
    {
      xsr.next();

      // if reached end of columns data, exit loop
      if ( xsr.isEndElement() && xsr.getLocalName().equals( XmlLabels.XML_COLUMNS ) )
        break;

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
                case XmlLabels.XML_POSITION:
                  m_rowIndexes.set( Integer.parseInt( xsr.getAttributeValue( i ) ), id );
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
    calculateBodyHeight();
  }

  /****************************************** moveFocus ******************************************/
  public void moveFocus( MoveDirection direction )
  {
    // TODO Auto-generated method stub

  }

}
