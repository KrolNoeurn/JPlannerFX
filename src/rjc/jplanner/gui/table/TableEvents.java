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

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import rjc.jplanner.JPlanner;
import rjc.jplanner.gui.table.AbstractCellEditor.MoveDirection;

/*************************************************************************************************/
/************************* Handle TableCanvas mouse and keyboard events **************************/
/*************************************************************************************************/

public class TableEvents extends TableCanvas
{
  private static final int PROXIMITY    = 4;          // used to distinguish resize from reorder

  private int              m_mouseX;                  // last mouse move/drag x
  private int              m_mouseY;                  // last mouse move/drag y
  private int              m_mouseCPos;               // last mouse move or reorder column position
  private int              m_mouseRow;                // last mouse move or reorder row position
  private int              m_editCPos   = -999999999; // column position of edit cell
  private int              m_editRow    = -999999999; // row of edit cell
  private int              m_selectCPos = -999999999; // column position of last selected cell or column
  private int              m_selectRow  = -999999999; // row of last selected cell or row
  private int              m_index      = -1;         // column or row index for resize or reorder
  private int              m_offset;                  // x or y resize/reorder offset
  private Canvas           m_reorderSlider;           // visual slider for when reordering
  private Canvas           m_reorderMarker;           // visual marker for new position when reordering
  private TableSelection   m_selected;                // shortcut to table selected cells

  /***************************************** constructor *****************************************/
  public TableEvents( Table table )
  {
    // setup table canvas
    super( table );
    m_selected = table.getSelected();

    // when mouse moves
    setOnMouseMoved( event -> mouseMoved( event ) );
    setOnMouseDragged( event -> mouseDragged( event ) );
    setOnMouseReleased( event -> mouseReleased( event ) );
    setOnMousePressed( event -> mousePressed( event ) );
    setOnMouseClicked( event -> mouseClicked( event ) );

    // when key presses
    setOnKeyPressed( event -> keyPressed( event ) );
    setOnKeyTyped( event -> keyTyped( event ) );
  }

  /************************************** getSelectCellRow ***************************************/
  public int getSelectCellRow()
  {
    // return row of cell with current select
    return m_selectRow;
  }

  /********************************* getSelectCellColumnPosition *********************************/
  public int getSelectCellColumnPosition()
  {
    // return column of cell with current select
    return m_selectCPos;
  }

  /*************************************** getEditCellRow ****************************************/
  public int getEditCellRow()
  {
    // return row of cell with current editor focus
    return m_editRow;
  }

  /********************************** getEditCellColumnPosition **********************************/
  public int getEditCellColumnPosition()
  {
    // return column of cell with current editor focus
    return m_editCPos;
  }

  /*************************************** setEditCellRow ****************************************/
  public void setEditCellRow( int row )
  {
    // set row of cell with current editor focus
    m_editRow = row;
    m_selectRow = row;
  }

  /********************************** setEditCellColumnPosition **********************************/
  public void setEditCellColumnPosition( int columnPos )
  {
    // set column of cell with current editor focus
    m_editCPos = columnPos;
    m_selectCPos = columnPos;
  }

  /****************************************** mouseMoved *****************************************/
  private void mouseMoved( MouseEvent event )
  {
    // mouse has moved with no button pressed
    m_mouseX = (int) event.getX();
    m_mouseY = (int) event.getY();
    m_mouseCPos = m_table.getColumnPositionExactAtX( m_mouseX );
    m_mouseRow = m_table.getRowExactAtY( m_mouseY );

    // ######### if over table headers corner, set cursor to default
    if ( m_mouseX < m_table.getVerticalHeaderWidth() && m_mouseY < m_table.getHorizontalHeaderHeight() )
    {
      setCursor( Cursors.DEFAULT );
      return;
    }

    // ######### if beyond table cells, set cursor to default
    if ( m_mouseX >= m_table.getTableWidth() || m_mouseY >= m_table.getTableHeight() )
    {
      setCursor( Cursors.DEFAULT );
      return;
    }

    // ######### if over horizontal header, check if resize, move, or select
    if ( m_mouseY < m_table.getHorizontalHeaderHeight() )
    {
      int col = m_table.getColumnPositionAtX( m_mouseX - PROXIMITY );
      int start = m_table.getXStartByColumnPosition( col + 1 );
      m_offset = m_mouseX - m_table.getWidthByColumnPosition( col );
      m_index = m_table.getColumnIndexByPosition( col );

      // if near column edge, set cursor to resize
      if ( Math.abs( m_mouseX - start ) <= PROXIMITY )
      {
        setCursor( Cursors.H_RESIZE );
        return;
      }

      // if column is selected, set cursor to move
      if ( m_selected.isColumnSelected( m_mouseCPos ) )
      {
        setCursor( Cursors.H_MOVE );
        return;
      }

      // otherwise, set cursor to down-arrow for selecting 
      setCursor( Cursors.DOWNARROW );
      return;
    }

    // ######### if over vertical header, check if resize, move, or select
    if ( m_mouseX < m_table.getVerticalHeaderWidth() )
    {
      int row = m_table.getRowAtY( m_mouseY - PROXIMITY );
      m_offset = m_mouseY - m_table.getRowHeight( row );
      m_index = row;
      int start = m_table.getYStartByRow( row + 1 );

      // if near row edge, set cursor to resize
      if ( Math.abs( m_mouseY - start ) <= PROXIMITY )
      {
        setCursor( Cursors.V_RESIZE );
        return;
      }

      // if row is selected, set cursor to move if moving is allowed
      if ( m_selected.isRowSelected( m_mouseRow ) && m_table.getData().getMoveEndRow( m_mouseRow ) >= 0 )
      {
        setCursor( Cursors.V_MOVE );
        return;
      }

      // otherwise, set cursor to right-arrow for selecting 
      setCursor( Cursors.RIGHTARROW );
      return;
    }

    // ######### mouse over table cells, check for expand/hide markers
    if ( isExpandHideMarker() )
      setCursor( Cursors.DEFAULT );
    else
      setCursor( Cursors.CROSS );
  }

  /***************************************** mouseDragged ****************************************/
  private void mouseDragged( MouseEvent event )
  {
    // ######### mouse has moved with button pressed, handle column resize
    if ( getCursor() == Cursors.H_RESIZE )
    {
      m_table.setWidthByColumnIndex( m_index, (int) event.getX() - m_offset );
      m_recentlyRedrawn = false;
      m_table.resizeCanvasScrollBars();
      if ( !m_recentlyRedrawn )
        drawWidth( m_table.getXStartByColumnPosition( m_table.getColumnPositionByIndex( m_index ) ), (int) getWidth() );
      return;
    }

    // ######### handle row resize
    if ( getCursor() == Cursors.V_RESIZE )
    {
      m_table.setHeightByRow( m_index, (int) event.getY() - m_offset );
      m_recentlyRedrawn = false;
      m_table.resizeCanvasScrollBars();
      if ( !m_recentlyRedrawn )
        drawHeight( m_table.getYStartByRow( m_index ), (int) getHeight() );
      return;
    }

    // ######### handle column select
    if ( getCursor() == Cursors.DOWNARROW )
    {
      m_mouseX = (int) event.getX();
      if ( m_mouseX < 99 )
        JPlanner.trace();

      m_selected.selectColumns( m_editCPos, m_selectCPos, false );
      m_selectCPos = m_table.getColumnPositionAtX( m_mouseX );
      m_selected.selectColumns( m_editCPos, m_selectCPos, true );
      redrawTableCanvas();
      return;
    }

    // ######### handle row select
    if ( getCursor() == Cursors.RIGHTARROW )
    {
      m_selected.selectRows( m_editRow, m_selectRow, false );
      m_selectRow = m_table.getRowAtY( (int) event.getY() );
      m_selected.selectRows( m_editRow, m_selectRow, true );
      redrawTableCanvas();
      return;
    }

    // ######### handle selecting table cells
    if ( getCursor() == Cursors.CROSS && event.isPrimaryButtonDown() && !event.isAltDown() )
    {
      m_mouseCPos = m_table.getColumnPositionAtX( (int) event.getX() );
      m_mouseRow = m_table.getRowAtY( (int) event.getY() );
      moveSelect( m_mouseCPos, m_mouseRow, !event.isControlDown() );
      redrawTableCanvas();
      return;
    }

    // ######### handle column reorder
    if ( getCursor() == Cursors.H_MOVE )
    {
      m_mouseX = (int) event.getX();
      columnReorderDragged();
      return;
    }

    // ######### handle row reorder
    if ( getCursor() == Cursors.V_MOVE )
    {
      m_mouseY = (int) event.getY();
      rowReorderDragged();
      return;
    }
  }

  /**************************************** mouseReleased ****************************************/
  private void mouseReleased( MouseEvent event )
  {
    // mouse button release, so complete any in progress column/row reordering
    if ( m_reorderMarker == null )
    {
      // call mouseMoved to update cursor etc
      mouseMoved( event );
      return;
    }

    // ######### check if column reordering
    if ( getCursor() == Cursors.H_MOVE )
    {
      // handle column reorder completion
      m_table.stopAnimation();
      m_table.remove( m_reorderSlider );
      m_table.remove( m_reorderMarker );
      m_reorderSlider = null;
      m_reorderMarker = null;

      int oldPos = m_table.getColumnPositionByIndex( m_index );
      if ( m_mouseCPos < oldPos )
        m_table.moveColumn( oldPos, m_mouseCPos );
      if ( m_mouseCPos > oldPos + 1 )
        m_table.moveColumn( oldPos, m_mouseCPos - 1 );
      redrawTableCanvas();
      return;
    }

    // ######### handle row reorder completion
    m_table.stopAnimation();
    m_table.remove( m_reorderSlider );
    m_table.remove( m_reorderMarker );
    m_reorderSlider = null;
    m_reorderMarker = null;

    if ( m_mouseRow < m_index )
      m_table.moveRow( m_index, m_mouseRow );
    if ( m_mouseRow > m_index + 1 )
      m_table.moveRow( m_index, m_mouseRow - 1 );
    redrawTableCanvas();
  }

  /**************************************** mousePressed *****************************************/
  private void mousePressed( MouseEvent event )
  {
    // request focus and consume event so table does not loss focus to tab-pane
    requestFocus();
    event.consume();

    // ######### handle cross cursor (mouse over table cells)
    if ( getCursor() == Cursors.CROSS && event.isPrimaryButtonDown() && !event.isAltDown() )
      if ( event.isShiftDown() )
        // update selection rectangle
        moveSelect( m_mouseCPos, m_mouseRow, !event.isControlDown() );
      else
        // move editor
        moveEdit( m_mouseCPos, m_mouseRow, !event.isControlDown() );

    // ######### handle right arrow (selecting rows)
    if ( getCursor() == Cursors.RIGHTARROW && event.isPrimaryButtonDown() && !event.isAltDown() )
    {
      // if control not down, clear all previous selections
      if ( !event.isControlDown() )
        m_selected.clear();

      // if invalid or shift not down, position editor on row
      if ( m_editRow < 0 || !event.isShiftDown() )
        m_editRow = m_mouseRow;

      // position editor and select rows
      m_editCPos = m_table.getVisibleColumnPositionRight( -1 );
      m_selectRow = m_mouseRow;
      m_selectCPos = m_table.getVisibleColumnPositionLeft( m_table.getData().getColumnCount() );
      m_selected.selectRows( m_editRow, m_selectRow, true );
      redrawTableCanvas();
    }

    // ######### handle down arrow (selecting columns)
    if ( getCursor() == Cursors.DOWNARROW && event.isPrimaryButtonDown() && !event.isAltDown() )
    {
      // if control not down, clear all previous selections
      if ( !event.isControlDown() )
        m_selected.clear();

      // if invalid or shift not down, position editor on column
      if ( m_editCPos < 0 || !event.isShiftDown() )
        m_editCPos = m_mouseCPos;

      // position editor and select columns
      m_editRow = m_table.getVisibleRowBelow( -1 );
      m_selectRow = m_table.getVisibleRowAbove( m_table.getData().getRowCount() );
      m_selectCPos = m_mouseCPos;
      m_selected.selectColumns( m_editCPos, m_selectCPos, true );
      redrawTableCanvas();
    }

    // ######### handle column reorder
    if ( getCursor() == Cursors.H_MOVE )
      columnReorderDragged();

    // ######### handle row reorder
    if ( getCursor() == Cursors.V_MOVE )
      rowReorderDragged();

    // ######### select whole table if headers corner
    if ( m_mouseX < m_table.getVerticalHeaderWidth() && m_mouseY < m_table.getHorizontalHeaderHeight() )
    {
      m_selected.selectColumns( 0, Integer.MAX_VALUE, true );
      redrawTableCanvas();
    }
  }

  /**************************************** mouseClicked *****************************************/
  private void mouseClicked( MouseEvent event )
  {
    // was mouse double clicked?
    boolean doubleClicked = event.getClickCount() == 2;

    // open cell editor if cross cursor and double click
    if ( getCursor() == Cursors.CROSS && doubleClicked )
      openCellEditor( null );

    // auto-resize column if horizontal resize cursor and double click
    if ( getCursor() == Cursors.H_RESIZE && doubleClicked )
      JPlanner.trace( "TODO - Implement auto-resize column" );

    // auto-resize row if vertical resize cursor and double click
    if ( getCursor() == Cursors.V_RESIZE && doubleClicked )
      JPlanner.trace( "TODO - Implement auto-resize row" );

    // expand or collapse rows if expand-hide marker clicked
    if ( getCursor() == Cursors.DEFAULT && isExpandHideMarker() )
    {
      if ( m_table.isRowCollapsed( m_mouseRow ) )
        m_table.expandSummary( m_mouseRow );
      else
        m_table.collapseSummary( m_mouseRow );

      // redraw table from mouse y downwards
      m_table.resizeCanvasScrollBars();
      moveEdit( m_mouseCPos, m_mouseRow, true );
    }

  }

  /******************************************* keyTyped ******************************************/
  private void keyTyped( KeyEvent event )
  {
    // open cell editor if key typed is suitable
    char key = event.getCharacter().charAt( 0 );
    if ( !Character.isISOControl( key ) )
      openCellEditor( event.getCharacter() );

    // move editor up or down when carriage return typed
    if ( key == '\r' )
      if ( event.isShiftDown() )
        moveEdit( MoveDirection.UP );
      else
        moveEdit( MoveDirection.DOWN );

    // move editor right or left when tab typed
    if ( key == '\t' )
      if ( event.isShiftDown() )
        moveEdit( MoveDirection.LEFT );
      else
        moveEdit( MoveDirection.RIGHT );
  }

  /***************************************** moveSelect ******************************************/
  private void moveSelect( int columnPos, int row, boolean clearAll )
  {
    // clear selected region, move select, and redraw region 
    if ( clearAll )
      m_selected.clear();
    else
      m_selected.select( m_editCPos, m_editRow, m_selectCPos, m_selectRow, false );

    // only scroll to new select cell if old select cell was at least partially visible
    int x = m_table.getXStartByColumnPosition( m_selectCPos );
    int y = m_table.getYStartByRow( m_selectRow );
    int w = m_table.getWidthByColumnPosition( m_selectCPos );
    int h = m_table.getRowHeight( m_selectRow );
    boolean scroll = x < getWidth() && x + w > m_table.getVerticalHeaderWidth() && y < getHeight()
        && y + h > m_table.getHorizontalHeaderHeight();

    m_selectCPos = columnPos;
    m_selectRow = row;

    m_selected.select( m_editCPos, m_editRow, m_selectCPos, m_selectRow, true );
    if ( scroll )
      m_table.scrollTo( m_selectCPos, m_selectRow );
    redrawTableCanvas();
  }

  /****************************************** moveEdit *******************************************/
  private void moveEdit( int columnPos, int row, boolean clearAll )
  {
    // clear all selections, move edit and select 
    if ( clearAll )
      m_selected.clear();

    m_editCPos = columnPos;
    m_editRow = row;
    m_selectCPos = columnPos;
    m_selectRow = row;

    m_selected.select( m_editCPos, m_editRow, true );
    m_table.scrollTo( m_editCPos, m_editRow );
    redrawTableCanvas();
  }

  /****************************************** keyPressed *****************************************/
  private void keyPressed( KeyEvent event )
  {
    // react to cursor moving keyboard events 
    boolean shift = event.isShiftDown();
    boolean control = event.isControlDown();
    boolean handled = true;
    int column, row;

    switch ( event.getCode() )
    {
      case HOME:
        // left-most column
        column = m_table.getVisibleColumnPositionRight( -1 );
        row = m_table.getVisibleRowBelow( -1 );
        if ( shift )
        {
          if ( control )
            moveSelect( column, row, true );
          else
            moveSelect( column, m_selectRow, false );
        }
        else
        {
          if ( control )
            moveEdit( column, row, true );
          else
            moveEdit( column, m_editRow, true );
        }
        break;

      case END:
        // right-most column
        column = m_table.getVisibleColumnPositionLeft( m_table.getData().getColumnCount() );
        row = m_table.getVisibleRowAbove( m_table.getData().getRowCount() );
        if ( shift )
        {
          if ( control )
            moveSelect( column, row, true );
          else
            moveSelect( column, m_selectRow, false );
        }
        else
        {
          if ( control )
            moveEdit( column, row, true );
          else
            moveEdit( column, m_editRow, true );
        }
        break;

      case PAGE_UP:
        int y = m_table.getYStartByRow( m_editRow ) + m_table.getRowHeight( m_editRow ) / 2;
        double value = m_table.m_vScrollBar.getValue() - m_table.m_vScrollBar.getBlockIncrement();
        if ( value < 0.0 )
          value = 0.0;
        m_table.m_vScrollBar.setValue( value );
        row = m_table.getRowAtY( y );
        if ( shift )
          moveSelect( m_selectCPos, row, false );
        else
          moveEdit( m_editCPos, row, true );
        break;

      case PAGE_DOWN:
        y = m_table.getYStartByRow( m_editRow ) + m_table.getRowHeight( m_editRow ) / 2;
        value = m_table.m_vScrollBar.getValue() + m_table.m_vScrollBar.getBlockIncrement();
        if ( value > m_table.m_vScrollBar.getMax() )
          value = m_table.m_vScrollBar.getMax();
        m_table.m_vScrollBar.setValue( value );
        row = m_table.getRowAtY( y );
        if ( shift )
          moveSelect( m_selectCPos, row, false );
        else
          moveEdit( m_editCPos, row, true );
        break;

      case UP:
      case KP_UP:
        if ( control && shift )
          m_selectRow = m_table.getVisibleRowBelow( -1 );
        if ( control && !shift )
          m_editRow = m_table.getVisibleRowBelow( -1 );
        if ( shift )
          moveSelect( m_selectCPos, m_table.getVisibleRowAbove( m_selectRow ), control );
        else
          moveEdit( m_editCPos, m_table.getVisibleRowAbove( m_editRow ), true );
        break;

      case DOWN:
      case KP_DOWN:
        if ( control && shift )
          m_selectRow = m_table.getVisibleRowAbove( m_table.getData().getRowCount() );
        if ( control && !shift )
          m_editRow = m_table.getVisibleRowAbove( m_table.getData().getRowCount() );
        if ( shift )
          moveSelect( m_selectCPos, m_table.getVisibleRowBelow( m_selectRow ), control );
        else
          moveEdit( m_editCPos, m_table.getVisibleRowBelow( m_editRow ), true );
        break;

      case RIGHT:
      case KP_RIGHT:
        if ( control && shift )
          m_selectCPos = m_table.getVisibleColumnPositionLeft( m_table.getData().getColumnCount() );
        if ( control && !shift )
          m_editCPos = m_table.getVisibleColumnPositionLeft( m_table.getData().getColumnCount() );
        if ( shift )
          moveSelect( m_table.getVisibleColumnPositionRight( m_selectCPos ), m_selectRow, control );
        else
          moveEdit( m_table.getVisibleColumnPositionRight( m_editCPos ), m_editRow, true );
        break;

      case LEFT:
      case KP_LEFT:
        if ( control && shift )
          m_selectCPos = m_table.getVisibleColumnPositionRight( -1 );
        if ( control && !shift )
          m_editCPos = m_table.getVisibleColumnPositionRight( -1 );
        if ( shift )
          moveSelect( m_table.getVisibleColumnPositionLeft( m_selectCPos ), m_selectRow, control );
        else
          moveEdit( m_table.getVisibleColumnPositionLeft( m_editCPos ), m_editRow, true );
        break;

      case F2:
        // open cell editor with current cell contents
        openCellEditor( null );
        break;

      case DELETE:
        // attempt to delete selected cells contents
        m_selected.setValuesNull();
        break;

      case INSERT:
        // attempt to insert
        JPlanner.trace( "NOT YET IMPLEMENTED - Insert" );
        break;

      case X:
        // attempt to cut cells contents (Ctrl-X)
        if ( control )
          JPlanner.trace( "NOT YET IMPLEMENTED - Cut" );
        break;

      case C:
        // attempt to copy cells contents (Ctrl-C)
        if ( control )
          JPlanner.trace( "NOT YET IMPLEMENTED - Copy" );
        break;

      case V:
        // attempt to paste cells contents (Ctrl-V)
        if ( control )
          JPlanner.trace( "NOT YET IMPLEMENTED - Paste" );
        break;

      case D:
        // attempt to fill-down cells contents (Ctrl-D)
        if ( control )
          m_selected.fillDown();
        break;

      case PERIOD:
        // "Ctrl + >" to indent tasks 
        if ( control )
          m_table.getData().indentRows( m_selected.getRowsWithSelection() );
        break;

      case COMMA:
        // "Ctrl + <" to outdent tasks
        if ( control )
          m_table.getData().outdentRows( m_selected.getRowsWithSelection() );
        break;

      default:
        handled = false;
        break;
    }

    if ( handled )
      event.consume();
  }

  /*************************************** openCellEditor ****************************************/
  private void openCellEditor( Object value )
  {
    // only open editor if a suitable cell is selected
    if ( m_editCPos < 0 || m_editRow < 0 || m_editCPos >= m_table.getData().getColumnCount()
        || m_editRow >= m_table.getData().getRowCount() || m_table.getRowHeight( m_editRow ) < 2 )
      return;

    // scroll to cell
    m_table.scrollTo( m_editCPos, m_editRow );
    m_table.finishAnimation();

    // open cell editor for currently selected table cell
    int columnIndex = m_table.getColumnIndexByPosition( m_editCPos );
    int row = m_editRow;
    AbstractCellEditor editor = m_table.getData().getEditor( columnIndex, row );

    // open editor if one available
    if ( editor != null && editor.isValueValid( value ) )
      editor.open( m_table, value );
  }

  /****************************************** moveEdit *******************************************/
  public void moveEdit( MoveDirection direction )
  {
    // move editor in specified direction
    if ( direction == MoveDirection.DOWN )
      m_editRow = m_table.getVisibleRowBelow( m_editRow );
    if ( direction == MoveDirection.UP )
      m_editRow = m_table.getVisibleRowAbove( m_editRow );
    if ( direction == MoveDirection.LEFT )
      m_editCPos = m_table.getVisibleColumnPositionLeft( m_editCPos );
    if ( direction == MoveDirection.RIGHT )
      m_editCPos = m_table.getVisibleColumnPositionRight( m_editCPos );

    moveEdit( m_editCPos, m_editRow, true );
  }

  /***************************************** scrollTable *****************************************/
  private void scrollTable()
  {
    // determine whether table needs to be scrolled to make reorder marker visible
    if ( getCursor() == Cursors.H_MOVE )
    {
      // vertical marker
      if ( m_mouseX < m_table.getVerticalHeaderWidth() )
      {
        m_table.animateScrollToLeftEdge();
        return;
      }

      if ( m_mouseX > getWidth() )
      {
        m_table.animateScrollToRightEdge();
        return;
      }
    }
    else
    {
      // horizontal marker
      if ( m_mouseY < m_table.getHorizontalHeaderHeight() )
      {
        m_table.animateScrollToTop();
        return;
      }

      if ( m_mouseY > getHeight() )
      {
        m_table.animateScrollToBottom();
        return;
      }
    }

    // no scrolling needed so make sure any previous animation is stopped
    m_table.stopAnimation();
  }

  /************************************* isExpandHideMarker **************************************/
  private boolean isExpandHideMarker()
  {
    // return true if cursor over row expand-hide marker
    Rectangle2D rect = getExpandHideMarkerRectangle( m_mouseCPos, m_mouseRow );
    if ( rect != null )
      return rect.contains( m_mouseX, m_mouseY );

    return false;
  }

  /******************************** getExpandHideMarkerRectangle ********************************/
  private Rectangle2D getExpandHideMarkerRectangle( int columnPos, int row )
  {
    // return expand-hide marker rectangle, or null if none
    if ( columnPos < 0 || row < 0 )
      return null;

    int columnIndex = m_table.getColumnIndexByPosition( columnPos );
    if ( m_table.getData().isCellSummary( columnIndex, row ) )
    {
      int indent = m_table.getData().getCellIndent( columnIndex, row );
      double x = m_table.getXStartByColumnPosition( columnPos ) + indent * INDENT;
      double y = m_table.getYStartByRow( row ) + m_table.getRowHeight( row ) / 2.0;
      return new Rectangle2D( x - 8.5, y - 4.5, 9.0, 9.0 );
    }

    return null;
  }

  /************************************* createReorderMarker *************************************/
  private Canvas createReorderMarker()
  {
    // create reorder marker
    Canvas marker = new Canvas();
    GraphicsContext gc = marker.getGraphicsContext2D();
    gc.setLineWidth( 3.0 );
    gc.setStroke( Color.RED );

    if ( getCursor() == Cursors.H_MOVE )
    {
      double h = Math.min( m_table.getBodyHeight() + m_table.getHorizontalHeaderHeight(), getHeight() );
      marker.setWidth( 5.0 );
      marker.setHeight( h );
      gc.strokeLine( 2.5, 0.0, 2.5, h );
    }
    else
    {
      double w = Math.min( m_table.getBodyWidth() + m_table.getVerticalHeaderWidth(), getWidth() );
      marker.setHeight( 5.0 );
      marker.setWidth( w );
      gc.strokeLine( 0.0, 2.5, w, 2.5 );
    }

    return marker;
  }

  /************************************** setMarkerPosition **************************************/
  void setMarkerPosition()
  {
    // ensure reorder marker position and visibility is correct
    if ( m_reorderMarker != null )
      if ( getCursor() == Cursors.H_MOVE )
      {
        // vertical reorder marker
        m_mouseCPos = m_table.getColumnPositionAtX( m_mouseX );
        int x = m_table.getXStartByColumnPosition( m_mouseCPos );
        int w = m_table.getWidthByColumnPosition( m_mouseCPos );
        if ( m_mouseX > x + w / 2 )
        {
          m_mouseCPos++;
          x += w;
        }
        m_reorderMarker.setTranslateX( x - m_reorderMarker.getWidth() / 2.0 );

        if ( x < m_table.getVerticalHeaderWidth() || x > getWidth() )
          m_reorderMarker.setVisible( false );
        else
          m_reorderMarker.setVisible( true );
      }
      else
      {
        // horizontal reorder marker
        m_mouseRow = m_table.getRowAtY( m_mouseY );
        int y = m_table.getYStartByRow( m_mouseRow );
        int h = m_table.getRowHeight( m_mouseRow );
        if ( m_mouseY > y + h / 2 )
        {
          m_mouseRow++;
          y += h;
        }
        m_reorderMarker.setTranslateY( y - m_reorderMarker.getHeight() / 2.0 );

        if ( y < m_table.getHorizontalHeaderHeight() || y > getHeight() )
          m_reorderMarker.setVisible( false );
        else
          m_reorderMarker.setVisible( true );
      }
  }

  /************************************** rowReorderDragged **************************************/
  private void rowReorderDragged()
  {
    // is a reorder already in progress
    if ( m_reorderSlider == null )
    {
      // start reorder
      m_offset = m_mouseY - m_table.getYStartByRow( m_mouseRow );
      m_selected.clear();
      redrawTableCanvas();

      // create reorder slider (translucent cell header)
      int h = m_table.getRowHeight( m_mouseRow );
      int w = m_table.getVerticalHeaderWidth();
      String text = m_table.getData().getRowTitle( m_mouseRow );
      m_reorderSlider = new Canvas( w, h );
      drawRowHeaderCell( m_reorderSlider.getGraphicsContext2D(), 0, w, h, text, true );
      m_reorderSlider.setOpacity( 0.8 );

      // create reorder marker (red horizontal line)
      m_reorderMarker = createReorderMarker();

      // add slider & marker to display
      m_table.add( m_reorderSlider );
      m_table.add( m_reorderMarker );
    }

    // set slider position
    m_reorderSlider.setTranslateY( m_mouseY - m_offset );

    // set marker position
    setMarkerPosition();

    // scroll table if needed to make marker visible
    scrollTable();
  }

  /************************************ columnReorderDragged *************************************/
  private void columnReorderDragged()
  {
    // create reorder markers if not already created
    if ( m_reorderSlider == null )
    {
      // start reorder
      int index = m_table.getColumnIndexByPosition( m_mouseCPos );
      m_offset = m_mouseX - m_table.getXStartByColumnPosition( m_mouseCPos );
      m_selected.clear();
      redrawTableCanvas();

      // create reorder slider (translucent cell header)
      int w = m_table.getWidthByColumnIndex( index );
      int h = m_table.getHorizontalHeaderHeight();
      String text = m_table.getData().getColumnTitle( index );
      m_reorderSlider = new Canvas( w, h );
      drawColumnHeaderCell( m_reorderSlider.getGraphicsContext2D(), 0, w, h, text, true );
      m_reorderSlider.setOpacity( 0.8 );

      // create reorder marker (red vertical line)
      m_reorderMarker = createReorderMarker();

      // add slider & marker to display
      m_table.add( m_reorderSlider );
      m_table.add( m_reorderMarker );
    }

    // set slider position
    m_reorderSlider.setTranslateX( m_mouseX - m_offset );

    // set marker position
    setMarkerPosition();

    // scroll table if needed to make marker visible
    scrollTable();
  }

}
