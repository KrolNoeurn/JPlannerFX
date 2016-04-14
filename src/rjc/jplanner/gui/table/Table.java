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

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Duration;
import rjc.jplanner.JPlanner;
import rjc.jplanner.XmlLabels;

/*************************************************************************************************/
/**************** Display gui scrollable table with horizontal & vertical header *****************/
/*************************************************************************************************/

public class Table extends GridPane
{
  private ITableDataSource          m_data;                                                // data source for the table
  private TableCanvas               m_canvas;                                              // canvas where table is drawn
  private ScrollBar                 m_vScrollBar;                                          // vertical scroll bar
  private ScrollBar                 m_hScrollBar;                                          // horizontal scroll bar
  private Timeline                  m_animation;                                           // used for table scrolling

  private int                       m_defaultRowHeight   = 20;
  private int                       m_defaultColumnWidth = 100;
  private int                       m_minimumRowHeight   = 15;
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

  private static int                SCROLLBAR_SIZE       = 18;

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

    // initialise column & row position to index mapping
    int count = m_data.getColumnCount();
    for ( int column = 0; column < count; column++ )
      m_columnIndexes.add( column );
    count = m_data.getRowCount();
    for ( int row = 0; row < count; row++ )
      m_rowIndexes.add( row );

    // calculate body width & height
    setMinWidth( 0.0 );
    setMinHeight( 0.0 );
    calculateBodyWidth();
    calculateBodyHeight();

    // setup canvas and scroll bars
    m_canvas = new TableCanvas( this );
    m_vScrollBar = new ScrollBar();
    m_vScrollBar.setOrientation( Orientation.VERTICAL );
    m_vScrollBar.setMinWidth( SCROLLBAR_SIZE );
    m_hScrollBar = new ScrollBar();
    m_hScrollBar.setMinHeight( SCROLLBAR_SIZE );
    add( m_canvas, 0, 0 );
    add( m_vScrollBar, 1, 0, 1, 1 );
    add( m_hScrollBar, 0, 1, 1, 1 );

    // table body to grow to fill all available space
    setValignment( m_canvas, VPos.TOP );
    setHgrow( m_canvas, Priority.ALWAYS );
    setVgrow( m_canvas, Priority.ALWAYS );
    heightProperty().addListener( ( observable, oldValue, newValue ) -> setCanvasScrollBars() );
    widthProperty().addListener( ( observable, oldValue, newValue ) -> setCanvasScrollBars() );
    m_vScrollBar.visibleProperty().addListener( ( observable, oldValue, newValue ) -> setCanvasScrollBars() );
    m_hScrollBar.visibleProperty().addListener( ( observable, oldValue, newValue ) -> setCanvasScrollBars() );
    m_vScrollBar.valueProperty().addListener( ( observable, oldValue, newValue ) -> redraw() );
    m_hScrollBar.valueProperty().addListener( ( observable, oldValue, newValue ) -> redraw() );
  }

  /************************************ setCanvasScrollBars **************************************/
  public void setCanvasScrollBars()
  {
    // set canvas to correct size to not overlap scroll bars
    int height = (int) getHeight();
    if ( m_hScrollBar.isVisible() )
      height -= SCROLLBAR_SIZE;
    if ( height != (int) m_canvas.getHeight() )
      m_canvas.setHeight( height );

    int width = (int) getWidth();
    if ( m_vScrollBar.isVisible() )
      width -= SCROLLBAR_SIZE;
    if ( width != (int) m_canvas.getWidth() )
      m_canvas.setWidth( width );

    // set scroll bars to correct visibility
    boolean hNeed = width < m_bodyWidth + m_vHeaderWidth;
    if ( hNeed != m_hScrollBar.isVisible() )
    {
      m_hScrollBar.setVisible( hNeed );
      return;
    }

    boolean vNeed = height < m_bodyHeight + m_hHeaderHeight;
    if ( vNeed != m_vScrollBar.isVisible() )
    {
      m_vScrollBar.setVisible( vNeed );
      return;
    }

    // set scroll bars to correct span
    if ( m_vScrollBar.isVisible() && m_hScrollBar.isVisible() )
    {
      // both visible so both should have span of 1
      if ( getRowSpan( m_vScrollBar ) != 1 )
        setRowSpan( m_vScrollBar, 1 );

      if ( getColumnSpan( m_hScrollBar ) != 1 )
        setColumnSpan( m_hScrollBar, 1 );
    }
    else
    {
      // if either visible should have span of 2
      if ( m_vScrollBar.isVisible() && getRowSpan( m_vScrollBar ) != 2 )
        setRowSpan( m_vScrollBar, 2 );

      if ( m_hScrollBar.isVisible() && getColumnSpan( m_hScrollBar ) != 2 )
        setColumnSpan( m_hScrollBar, 2 );
    }

    // set scroll bars correct thumb size and position
    if ( m_vScrollBar.isVisible() )
    {
      double max = m_bodyHeight + m_hHeaderHeight - height;
      m_vScrollBar.setMax( max );
      m_vScrollBar.setVisibleAmount( max * height / ( m_bodyHeight + m_hHeaderHeight ) );
      if ( m_vScrollBar.getValue() > max )
        m_vScrollBar.setValue( max );
    }
    else
      m_vScrollBar.setValue( 0.0 );

    if ( m_hScrollBar.isVisible() )
    {
      double max = m_bodyWidth + m_vHeaderWidth - width;
      m_hScrollBar.setMax( max );
      m_hScrollBar.setVisibleAmount( max * width / ( m_bodyWidth + m_vHeaderWidth ) );
      if ( m_hScrollBar.getValue() > max )
        m_hScrollBar.setValue( max );
    }
    else
      m_hScrollBar.setValue( 0.0 );
  }

  /************************************ calculateBodyHeight **************************************/
  private void calculateBodyHeight()
  {
    // calculate height of table body rows
    m_bodyHeight = 0;
    int count = m_data.getRowCount();
    for ( int index = 0; index < count; index++ )
    {
      int height = m_rowHeights.getOrDefault( index, m_defaultRowHeight );
      if ( height > 0 )
        m_bodyHeight += height;
    }
  }

  /************************************ calculateBodyWidth ***************************************/
  private void calculateBodyWidth()
  {
    // calculate width of table body columns
    m_bodyWidth = 0;
    int count = m_data.getColumnCount();
    for ( int index = 0; index < count; index++ )
    {
      int width = m_columnWidths.getOrDefault( index, m_defaultColumnWidth );
      if ( width > 0 )
        m_bodyWidth += width;
    }
  }

  /************************************** getBodyHeight ******************************************/
  public int getBodyHeight()
  {
    return m_bodyHeight;
  }

  /*************************************** getBodyWidth ******************************************/
  public int getBodyWidth()
  {
    return m_bodyWidth;
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
    x += (int) m_hScrollBar.getValue() - m_vHeaderWidth;
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
    x += (int) m_hScrollBar.getValue() - m_vHeaderWidth;
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

    int startX = m_vHeaderWidth - (int) m_hScrollBar.getValue();
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
    y += (int) m_vScrollBar.getValue() - m_hHeaderHeight;
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
    y += (int) m_vScrollBar.getValue() - m_hHeaderHeight;
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

    int startY = m_hHeaderHeight - (int) m_vScrollBar.getValue();
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

  /******************************************* redraw ********************************************/
  public void redraw()
  {
    // trigger simple complete redraw of table
    m_canvas.redrawAll();
  }

  /******************************************** reset ********************************************/
  public void reset()
  {
    // reset table canvas for example after change in number of columns or rows
    calculateBodyHeight();
    calculateBodyWidth();
    setCanvasScrollBars();
    m_canvas.redrawAll();
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

  /************************************** animationScrollUp **************************************/
  public void animationScrollUp()
  {
    // create scroll up animation, unless animation already in place
    if ( m_animation != null )
      return;

    double value = m_vScrollBar.getValue();
    KeyValue kv = new KeyValue( m_vScrollBar.valueProperty(), 0 );
    KeyFrame kf = new KeyFrame( Duration.millis( value * 5 ), kv );
    m_animation = new Timeline( kf );
    m_animation.play();
  }

  /************************************* animationScrollDown *************************************/
  public void animationScrollDown()
  {
    // create scroll down animation, unless animation already in place
    if ( m_animation != null )
      return;

    double value = m_vScrollBar.getValue();
    double max = m_vScrollBar.getMax();
    KeyValue kv = new KeyValue( m_vScrollBar.valueProperty(), max );
    KeyFrame kf = new KeyFrame( Duration.millis( ( max - value ) * 5 ), kv );
    m_animation = new Timeline( kf );
    m_animation.play();
  }

  /************************************ animationScrollRight *************************************/
  public void animationScrollRight()
  {
    // create scroll right animation, unless animation already in place
    if ( m_animation != null )
      return;

    double value = m_hScrollBar.getValue();
    double max = m_hScrollBar.getMax();
    KeyValue kv = new KeyValue( m_hScrollBar.valueProperty(), max );
    KeyFrame kf = new KeyFrame( Duration.millis( ( max - value ) * 5 ), kv );
    m_animation = new Timeline( kf );
    m_animation.play();
  }

  /************************************* animationScrollLeft *************************************/
  public void animationScrollLeft()
  {
    // create scroll left animation, unless animation already in place
    if ( m_animation != null )
      return;

    double value = m_hScrollBar.getValue();
    KeyValue kv = new KeyValue( m_hScrollBar.valueProperty(), 0 );
    KeyFrame kf = new KeyFrame( Duration.millis( value * 5 ), kv );
    m_animation = new Timeline( kf );
    m_animation.play();
  }

  /**************************************** animationStop ****************************************/
  public void animationStop()
  {
    // stop animation if one exists
    if ( m_animation == null )
      return;

    m_animation.stop();
    m_animation = null;
  }

  /****************************************** writeXML *******************************************/
  public void writeXML( XMLStreamWriter xsw ) throws XMLStreamException
  {
    // write column widths
    xsw.writeStartElement( XmlLabels.XML_COLUMNS );
    xsw.writeAttribute( XmlLabels.XML_WIDTH, Integer.toString( m_defaultColumnWidth ) );
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
    // read XML columns data
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
    // read XML rows data
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

}
