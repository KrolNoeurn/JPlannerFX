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

import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import rjc.jplanner.JPlanner;
import rjc.jplanner.XmlLabels;

/*************************************************************************************************/
/**************************** Handles table cell/row/column selection ****************************/
/*************************************************************************************************/

public class TableSelection
{
  private Table           m_table;             // associated table

  // set of body cells that are currently selected, where Integer = columnPos * SELECT_HASH + row
  public static final int SELECT_HASH = 9999;
  private Set<Integer>    m_selectedCells;
  private Set<Integer>    m_selectedRows;
  private Set<Integer>    m_selectedColumnsPos;

  /**************************************** constructor ******************************************/
  public TableSelection( Table table )
  {
    // prepare table selection
    m_table = table;
    m_selectedCells = new HashSet<Integer>();
    m_selectedRows = new HashSet<Integer>();
    m_selectedColumnsPos = new HashSet<Integer>();
  }

  private static int clamp( int val, int min, int max )
  {
    return Math.max( min, Math.min( max, val ) );
  }

  /******************************************** clear ********************************************/
  public void clear()
  {
    // clear selection from all cells
    m_selectedCells.clear();
    m_selectedRows.clear();
    m_selectedColumnsPos.clear();
  }

  /***************************************** isSelected ******************************************/
  public boolean isSelected( int columnPos, int row )
  {
    // return true if specified row is selected
    if ( m_selectedRows.contains( row ) )
      return true;

    // return true if specified column-position is selected
    if ( m_selectedColumnsPos.contains( columnPos ) )
      return true;

    // return true if specified body cell is selected
    return m_selectedCells.contains( columnPos * SELECT_HASH + row );
  }

  /******************************************* select *******************************************/
  public void select( int columnPos, int row, boolean selected )
  {
    // set whether specified body cell is selected
    if ( selected )
    {
      // only need to record if not already selected via row/column selection
      if ( !m_selectedColumnsPos.contains( columnPos ) && !m_selectedRows.contains( row ) )
        m_selectedCells.add( columnPos * SELECT_HASH + row );
    }
    else
      m_selectedCells.remove( columnPos * SELECT_HASH + row );
  }

  /******************************************* select *******************************************/
  public void select( int columnPos1, int row1, int columnPos2, int row2, boolean selected )
  {
    // ensure column and row positions are within bounds
    columnPos1 = clamp( columnPos1, 0, m_table.getData().getColumnCount() - 1 );
    columnPos2 = clamp( columnPos2, 0, m_table.getData().getColumnCount() - 1 );
    row1 = clamp( row1, 0, m_table.getData().getRowCount() - 1 );
    row2 = clamp( row2, 0, m_table.getData().getRowCount() - 1 );

    // determine min & max positions
    int c1 = Math.min( columnPos1, columnPos2 );
    int c2 = Math.max( columnPos1, columnPos2 );
    int r1 = Math.min( row1, row2 );
    int r2 = Math.max( row1, row2 );

    // set whether specified table region is selected
    for ( int column = c1; column <= c2; column++ )
      for ( int row = r1; row <= r2; row++ )
        select( column, row, selected );
  }

  /***************************************** selectRows ******************************************/
  public void selectRows( int row1, int row2, boolean selected )
  {
    // ensure row positions are within bounds
    row1 = clamp( row1, 0, m_table.getData().getRowCount() - 1 );
    row2 = clamp( row2, 0, m_table.getData().getRowCount() - 1 );

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
    columnPos1 = clamp( columnPos1, 0, m_table.getData().getColumnCount() - 1 );
    columnPos2 = clamp( columnPos2, 0, m_table.getData().getColumnCount() - 1 );

    // determine min & max positions
    int c1 = Math.min( columnPos1, columnPos2 );
    int c2 = Math.max( columnPos1, columnPos2 );

    // set whether specified table region is selected
    for ( int column = c1; column <= c2; column++ )
    {
      // set whether specified table column is selected
      if ( selected )
        m_selectedColumnsPos.add( column );
      else
        m_selectedColumnsPos.remove( column );
    }
  }

  /************************************ doesRowHaveSelection *************************************/
  public boolean doesRowHaveSelection( int row )
  {
    // return true if any column selected
    if ( !m_selectedColumnsPos.isEmpty() )
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

    // return false if any visible column cell in row is not selected
    int columns = m_table.getData().getColumnCount();
    for ( int columnPos = 0; columnPos < columns; columnPos++ )
      if ( m_table.getWidthByColumnPosition( columnPos ) > 0 )
        if ( !isSelected( columnPos, columnPos ) )
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
    if ( m_selectedColumnsPos.contains( columnPos ) )
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
    // return true if specified column is selected
    if ( m_selectedColumnsPos.contains( columnPos ) )
      return true;

    // return false if any visible row cell in column is not selected
    int rows = m_table.getData().getRowCount();
    for ( int row = 0; row < rows; row++ )
      if ( m_table.getRowHeight( row ) > 0 )
        if ( !isSelected( columnPos, row ) )
          return false;

    return true;
  }

  /************************************ getRowsWithSelection *************************************/
  public Set<Integer> getRowsWithSelection()
  {
    // return set of row indexes with at least one table cell selected
    Set<Integer> rows = new HashSet<Integer>();

    if ( m_selectedColumnsPos.isEmpty() )
    {
      for ( int hash : m_selectedCells )
        rows.add( hash % SELECT_HASH );

      for ( int row : m_selectedRows )
        rows.add( row );
    }
    else
      for ( int index = 0; index < m_table.getData().getRowCount(); index++ )
        rows.add( index );

    return rows;
  }

  /**************************************** setValuesNull ****************************************/
  public void setValuesNull()
  {
    // attempt to set all editable selected table cell values to null by undo-command
    Set<Integer> cells = new HashSet<Integer>();
    AbstractDataSource data = m_table.getData();

    // add visible cells from selected rows
    for ( int row : m_selectedRows )
      if ( m_table.getRowHeight( row ) > 0 )
      {
        int columns = data.getColumnCount();
        for ( int columnIndex = 0; columnIndex < columns; columnIndex++ )
          if ( m_table.getWidthByColumnIndex( columnIndex ) > 0 && data.getValue( columnIndex, row ) != null )
            cells.add( columnIndex * SELECT_HASH + row );
      }

    // add visible cells from selected columns
    for ( int columnPos : m_selectedColumnsPos )
      if ( m_table.getWidthByColumnPosition( columnPos ) > 0 )
      {
        int columnIndex = m_table.getColumnIndexByPosition( columnPos );
        int rows = data.getRowCount();
        for ( int row = 0; row < rows; row++ )
          if ( m_table.getRowHeight( row ) > 0 && data.getValue( columnIndex, row ) != null )
            cells.add( columnIndex * SELECT_HASH + row );
      }

    // add visible other selected cells
    for ( int hash : m_selectedCells )
    {
      int row = hash % SELECT_HASH;
      int columnIndex = m_table.getColumnIndexByPosition( hash / SELECT_HASH );
      if ( m_table.getWidthByColumnIndex( columnIndex ) > 0 && m_table.getRowHeight( row ) > 0
          && data.getValue( columnIndex, row ) != null )
        cells.add( columnIndex * SELECT_HASH + row );
    }

    // use data source to set these cells to null
    if ( !cells.isEmpty() )
      data.setNull( cells );
  }

  /****************************************** writeXML *******************************************/
  public void writeXML( XMLStreamWriter xsw ) throws XMLStreamException
  {
    // write selected cells to XML stream (not including rows & columns)
    for ( int hash : m_selectedCells )
    {
      xsw.writeEmptyElement( XmlLabels.XML_SELECTED );
      xsw.writeAttribute( XmlLabels.XML_COLUMN, Integer.toString( hash / SELECT_HASH ) );
      xsw.writeAttribute( XmlLabels.XML_ROW, Integer.toString( hash % SELECT_HASH ) );
    }
  }

  /******************************************* loadXML *******************************************/
  public void loadXML( XMLStreamReader xsr ) throws XMLStreamException
  {
    // get attributes for position of selected cell
    int columnPos = -1;
    int row = -1;
    for ( int i = 0; i < xsr.getAttributeCount(); i++ )
      switch ( xsr.getAttributeLocalName( i ) )
      {
        case XmlLabels.XML_COLUMN:
          columnPos = Integer.parseInt( xsr.getAttributeValue( i ) );
          break;
        case XmlLabels.XML_ROW:
          row = Integer.parseInt( xsr.getAttributeValue( i ) );
          break;
        default:
          JPlanner.trace( "Unhandled attribute '" + xsr.getAttributeLocalName( i ) + "'" );
          break;
      }

    if ( columnPos >= 0 && row >= 0 )
      select( columnPos, row, true );
  }

  /****************************************** fillDown *******************************************/
  public void fillDown()
  {
    // can only fill-down table cell contents if simple rectangular selection 

    // check for just columns
    if ( m_selectedColumnsPos.size() > 0 )
      if ( m_selectedRows.isEmpty() && m_selectedCells.isEmpty() )
      {
        // TODO
        JPlanner.trace( "NOT YET IMPLEMENTED - Fill down columns", this );
      }
      else
        // cannot cope with other selection, so do nothing
        return;

    // check for just rows
    if ( m_selectedRows.size() > 1 )
      if ( m_selectedColumnsPos.isEmpty() && m_selectedCells.isEmpty() )
      {
        // TODO
        JPlanner.trace( "NOT YET IMPLEMENTED - Fill down rows", this );
      }
      else
        // cannot cope with other selection, so do nothing
        return;

    // check for just cells
    if ( m_selectedCells.size() > 1 )
      if ( m_selectedRows.isEmpty() && m_selectedColumnsPos.isEmpty() )
      {
        // TODO
        JPlanner.trace( "NOT YET IMPLEMENTED - Fill down cells", this );
      }
      else
        // cannot cope with other selection, so do nothing
        return;
  }

  /****************************************** toString *******************************************/
  @Override
  public String toString()
  {
    // convert to string
    return getClass().getSimpleName() + "@" + Integer.toHexString( hashCode() ) + "[cols=" + m_selectedColumnsPos.size()
        + " rows=" + m_selectedRows.size() + " cells=" + m_selectedCells.size() + "]";
  }

}
