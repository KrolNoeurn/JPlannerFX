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

import java.util.Set;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import rjc.jplanner.JPlanner;
import rjc.jplanner.command.CommandTaskIndent;
import rjc.jplanner.command.CommandTaskOutdent;
import rjc.jplanner.gui.table.AbstractCellEditor.MoveDirection;

/*************************************************************************************************/
/************************* Handle TableCanvas mouse and keyboard events **************************/
/*************************************************************************************************/

public class TableEvents extends TableCanvas
{
  private static final int    PROXIMITY        = 4;              // used to distinguish resize from reorder

  private static final Cursor CURSOR_H_RESIZE  = Cursor.H_RESIZE;
  private static final Cursor CURSOR_V_RESIZE  = Cursor.V_RESIZE;
  private static final Cursor CURSOR_DEFAULT   = Cursor.DEFAULT;
  private static Cursor       CURSOR_DOWNARROW;
  private static Cursor       CURSOR_RIGHTARROW;
  private static Cursor       CURSOR_CROSS;

  private int                 m_x;                               // last mouse move/drag x
  private int                 m_y;                               // last mouse move/drag y
  private int                 m_column;                          // last mouse move or reorder column position
  private int                 m_row;                             // last mouse move or reorder row position
  private int                 m_offset;                          // x or y resize/reorder offset
  private int                 m_selectedColumn = -1;             // column of last single cell selected
  private int                 m_selectedRow    = -1;             // row of last single cell selected
  private int                 m_index          = -1;             // column or row index for resize or reorder
  private Canvas              m_reorderSlider;                   // visual slider for when reordering
  private Canvas              m_reorderMarker;                   // visual marker for new position when reordering

  /***************************************** constructor *****************************************/
  public TableEvents( Table table )
  {
    // setup table canvas
    super( table );

    // ensure cursors are setup
    if ( CURSOR_DOWNARROW == null )
    {
      CURSOR_DOWNARROW = new ImageCursor( new Image( getClass().getResourceAsStream( "arrowdown.png" ) ), 7, 16 );
      CURSOR_RIGHTARROW = new ImageCursor( new Image( getClass().getResourceAsStream( "arrowright.png" ) ), 16, 24 );
      CURSOR_CROSS = new ImageCursor( new Image( getClass().getResourceAsStream( "cross.png" ) ), 16, 20 );
    }

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

  /****************************************** mouseMoved *****************************************/
  private void mouseMoved( MouseEvent event )
  {
    // update cursor for potential column/row resizing or re-ordering
    m_x = (int) event.getX();
    m_y = (int) event.getY();
    m_column = m_table.getColumnPositionExactAtX( m_x );
    m_row = m_table.getRowExactAtY( m_y );

    setCursor( CURSOR_CROSS );
    m_index = -1;

    // check for column resize or reorder
    if ( m_y < m_table.getHorizontalHeaderHeight() )
    {
      if ( m_x > m_table.getVerticalHeaderWidth() + PROXIMITY )
      {
        int start = m_table.getXStartByColumnPosition( m_column );

        if ( m_x - start < PROXIMITY )
        {
          setCursor( CURSOR_H_RESIZE );
          m_column--;
          if ( m_column >= m_table.getData().getColumnCount() )
            m_column = m_table.getData().getColumnCount() - 1;
          return;
        }

        int width = m_table.getWidthByColumnPosition( m_column );
        if ( start + width - m_x < PROXIMITY )
        {
          setCursor( CURSOR_H_RESIZE );
          return;
        }
      }

      setCursor( CURSOR_DOWNARROW );
    }

    // check for row resize or reorder
    if ( m_x < m_table.getVerticalHeaderWidth() )
    {
      if ( m_y > m_table.getHorizontalHeaderHeight() + PROXIMITY )
      {
        int start = m_table.getYStartByRow( m_row );

        if ( m_y - start < PROXIMITY )
        {
          setCursor( CURSOR_V_RESIZE );
          m_row--;
          if ( m_row >= m_table.getData().getRowCount() )
            m_row = m_table.getData().getRowCount() - 1;
          return;
        }

        int height = m_table.getHeightByRow( m_row );
        if ( start + height - m_y < PROXIMITY )
        {
          setCursor( CURSOR_V_RESIZE );
          return;
        }
      }

      setCursor( CURSOR_RIGHTARROW );
    }

    // check for table headers corner
    if ( m_x < m_table.getVerticalHeaderWidth() && m_y < m_table.getHorizontalHeaderHeight() )
      setCursor( CURSOR_DEFAULT );

    // check for beyond table cells
    if ( m_x >= m_table.getVerticalHeaderWidth() + m_table.getBodyWidth()
        || m_y >= m_table.getHorizontalHeaderHeight() + m_table.getBodyHeight() )
      setCursor( CURSOR_DEFAULT );

    // check for expand/hide markers
    if ( isExpandHideMarker() )
      setCursor( CURSOR_DEFAULT );
  }

  /***************************************** mouseDragged ****************************************/
  private void mouseDragged( MouseEvent event )
  {
    // handle resizing, reordering and selecting
    m_x = (int) event.getX();
    m_y = (int) event.getY();

    // handle column resize
    if ( getCursor() == CURSOR_H_RESIZE )
    {
      if ( m_index < 0 )
      {
        m_index = m_table.getColumnIndexByPosition( m_column );
        m_offset = m_x - m_table.getWidthByColumnPosition( m_column );
      }

      m_table.setWidthByColumnIndex( m_index, m_x - m_offset );
      m_recentlyRedrawn = false;
      m_table.resizeCanvasScrollBars();
      if ( !m_recentlyRedrawn )
        drawWidth( m_table.getXStartByColumnPosition( m_column ), (int) getWidth() );
      return;
    }

    // handle row resize
    if ( getCursor() == CURSOR_V_RESIZE )
    {
      if ( m_index < 0 )
      {
        m_index = m_row;
        m_offset = m_y - m_table.getHeightByRow( m_row );
      }

      m_table.setHeightByRow( m_index, m_y - m_offset );
      m_recentlyRedrawn = false;
      m_table.resizeCanvasScrollBars();
      if ( !m_recentlyRedrawn )
        drawHeight( m_table.getYStartByRow( m_row ), (int) getHeight() );
      return;
    }

    // handle column reorder
    if ( getCursor() == CURSOR_DOWNARROW )
      columnReorderDragged();

    // handle row reorder
    if ( getCursor() == CURSOR_RIGHTARROW )
      rowReorderDragged();

    // handle cell selecting
    if ( getCursor() == CURSOR_CROSS && event.isPrimaryButtonDown() && !event.isAltDown() )
    {
      int columnPos = m_table.getColumnPositionAtX( m_x );
      int rowPos = m_table.getRowAtY( m_y );
      m_table.scrollTo( columnPos, rowPos );

      m_table.clearAllSelection();
      int column1 = Math.min( columnPos, m_selectedColumn );
      int column2 = Math.max( columnPos, m_selectedColumn );
      int row1 = Math.min( rowPos, m_selectedRow );
      int row2 = Math.max( rowPos, m_selectedRow );
      for ( int column = column1; column <= column2; column++ )
        for ( int row = row1; row <= row2; row++ )
          m_table.setSelection( column, row, true );
      redrawAll();

      return;
    }

  }

  /**************************************** mouseReleased ****************************************/
  private void mouseReleased( MouseEvent event )
  {
    // handle down arrow
    if ( getCursor() == CURSOR_DOWNARROW )
    {
      if ( m_reorderMarker != null )
      {
        // handle column reorder completion
        m_table.stopAnimation();
        m_table.remove( m_reorderSlider );
        m_table.remove( m_reorderMarker );
        m_reorderSlider = null;
        m_reorderMarker = null;

        int oldPos = m_table.getColumnPositionByIndex( m_index );
        if ( m_column < oldPos )
          m_table.moveColumn( oldPos, m_column );
        if ( m_column > oldPos + 1 )
          m_table.moveColumn( oldPos, m_column - 1 );
        redrawAll();
      }
      else
      {
        // handle column select
        if ( event.isAltDown() )
          return;

        int column1 = m_column;
        int column2 = m_column;
        boolean select = true;

        if ( !event.isControlDown() && !event.isShiftDown() )
          m_table.clearAllSelection();

        if ( event.isShiftDown() && m_selectedColumn >= 0 )
        {
          column1 = Math.min( m_column, m_selectedColumn );
          column2 = Math.max( m_column, m_selectedColumn );
        }

        if ( event.isControlDown() && !event.isShiftDown() )
          select = !m_table.isColumnAllSelected( m_column );

        m_selectedColumn = m_column;
        m_selectedRow = 0;
        for ( int column = column1; column <= column2; column++ )
          m_table.setColumnSelection( column, select );
        redrawAll();
      }
    }

    // handle right arrow
    if ( getCursor() == CURSOR_RIGHTARROW )
    {
      if ( m_reorderMarker != null )
      {
        // handle row reorder completion
        m_table.stopAnimation();
        m_table.remove( m_reorderSlider );
        m_table.remove( m_reorderMarker );
        m_reorderSlider = null;
        m_reorderMarker = null;

        int oldPos = m_index;
        if ( m_row < oldPos )
          m_table.moveRow( oldPos, m_row );
        if ( m_row > oldPos + 1 )
          m_table.moveRow( oldPos, m_row - 1 );
        redrawAll();
      }
      else
      {
        // handle row select
        if ( event.isAltDown() )
          return;

        int row1 = m_row;
        int row2 = m_row;
        boolean select = true;

        if ( !event.isControlDown() && !event.isShiftDown() )
          m_table.clearAllSelection();

        if ( event.isShiftDown() && m_selectedRow >= 0 )
        {
          row1 = Math.min( m_row, m_selectedRow );
          row2 = Math.max( m_row, m_selectedRow );
        }

        if ( event.isControlDown() && !event.isShiftDown() )
          select = !m_table.isRowAllSelected( m_row );

        m_selectedColumn = 0;
        m_selectedRow = m_row;
        for ( int row = row1; row <= row2; row++ )
          m_table.setRowSelection( row, select );
        redrawAll();
      }
    }

    // call mouse moved to ensure cursor is correct etc 
    mouseMoved( event );
  }

  /**************************************** mousePressed *****************************************/
  private void mousePressed( MouseEvent event )
  {
    // request focus for the table (and so close any active cell editors)
    Platform.runLater( () -> requestFocus() );

    // handle cross cursor
    if ( getCursor() == CURSOR_CROSS && event.isPrimaryButtonDown() && !event.isAltDown() )
    {
      // scroll to cell
      m_table.scrollTo( m_column, m_row );

      // no shift + no control = select single cell
      if ( !event.isControlDown() && !event.isShiftDown() )
      {
        m_selectedColumn = m_column;
        m_selectedRow = m_row;
        m_table.clearAllSelection();
        m_table.setSelection( m_column, m_row, true );
        redrawAll();
        return;
      }

      // control + no shift = toggle single cell
      if ( event.isControlDown() && !event.isShiftDown() )
      {
        boolean wasSelected = m_table.isSelected( m_column, m_row );
        if ( !wasSelected )
        {
          m_selectedColumn = m_column;
          m_selectedRow = m_row;
        }
        else
        {
          m_selectedColumn = -1;
          m_selectedRow = -1;
        }
        m_table.setSelection( m_column, m_row, !wasSelected );
        redrawAll();
        return;
      }

      // shift = select rectangular area between previous selected cell and this 
      if ( event.isShiftDown() )
      {
        if ( m_selectedColumn < 0 || m_selectedRow < 0 )
        {
          m_selectedColumn = m_column;
          m_selectedRow = m_row;
        }

        m_table.clearAllSelection();
        int column1 = Math.min( m_column, m_selectedColumn );
        int column2 = Math.max( m_column, m_selectedColumn );
        int row1 = Math.min( m_row, m_selectedRow );
        int row2 = Math.max( m_row, m_selectedRow );
        for ( int column = column1; column <= column2; column++ )
          for ( int row = row1; row <= row2; row++ )
            m_table.setSelection( column, row, true );
        redrawAll();
        return;
      }
    }

  }

  /**************************************** mouseClicked *****************************************/
  private void mouseClicked( MouseEvent event )
  {
    // was mouse double clicked?
    boolean doubleClicked = event.getClickCount() == 2;

    // open cell editor if cross cursor and double click
    if ( getCursor() == CURSOR_CROSS && doubleClicked )
      openCellEditor( null );

    // auto-resize column if horizontal resize cursor and double click
    if ( getCursor() == CURSOR_H_RESIZE && doubleClicked )
      JPlanner.trace( "TODO - Implement auto-resize column" );

    // auto-resize row if vertical resize cursor and double click
    if ( getCursor() == CURSOR_V_RESIZE && doubleClicked )
      JPlanner.trace( "TODO - Implement auto-resize row" );

    // expand or collapse rows if expand-hide marker clicked
    if ( getCursor() == CURSOR_DEFAULT && isExpandHideMarker() )
    {
      if ( m_table.isRowCollapsed( m_row ) )
        m_table.expandSummary( m_row );
      else
        m_table.collapseSummary( m_row );

      // redraw table from mouse y downwards
      m_table.resizeCanvasScrollBars();
      drawHeight( m_y, (int) getHeight() );
    }
  }

  /******************************************* keyTyped ******************************************/
  private void keyTyped( KeyEvent event )
  {
    // open cell editor if key typed is suitable
    char key = event.getCharacter().charAt( 0 );
    if ( key >= ' ' )
      openCellEditor( event.getCharacter() );
  }

  /****************************************** keyPressed *****************************************/
  private void keyPressed( KeyEvent event )
  {
    // react to cursor moving keyboard events 
    boolean redraw = false;

    switch ( event.getCode() )
    {
      case HOME:
        // find left-most visible column
        int leftmost = 0;
        while ( m_table.getWidthByColumnPosition( leftmost ) <= 0 )
          leftmost++;

        if ( m_selectedColumn != leftmost )
        {
          m_selectedColumn = leftmost;
          m_table.scrollTo( m_selectedColumn, m_selectedRow );
          redraw = true;
        }
        if ( m_table.selectionCount() > 1 || redraw )
        {
          m_table.clearAllSelection();
          m_table.setSelection( m_selectedColumn, m_selectedRow, true );
          redraw = true;
        }
        if ( redraw )
          redrawAll();

        break;

      case END:
        // find right-most visible column
        int rightmost = m_table.getData().getColumnCount() - 1;
        while ( m_table.getWidthByColumnPosition( rightmost ) <= 0 )
          rightmost--;

        if ( m_selectedColumn != rightmost )
        {
          m_selectedColumn = rightmost;
          m_table.scrollTo( m_selectedColumn, m_selectedRow );
          redraw = true;
        }
        if ( m_table.selectionCount() > 1 || redraw )
        {
          m_table.clearAllSelection();
          m_table.setSelection( m_selectedColumn, m_selectedRow, true );
          redraw = true;
        }
        if ( redraw )
          redrawAll();

        break;

      case PAGE_UP:
        JPlanner.trace( "TODO - handle PAGE_UP key press ..." );
        break;
      case PAGE_DOWN:
        JPlanner.trace( "TODO - handle PAGE_DOWN key press ..." );
        break;

      case UP:
      case KP_UP:
        // find visible cell above
        int above = m_selectedRow - 1;
        while ( m_table.getHeightByRow( above ) <= 0 && above >= 0 )
          above--;

        // ensure only cell visible above is selected
        if ( above >= 0 )
          m_selectedRow = above;
        m_table.clearAllSelection();
        m_table.setSelection( m_selectedColumn, m_selectedRow, true );
        m_table.scrollTo( m_selectedColumn, m_selectedRow );
        redrawAll();
        break;

      case DOWN:
      case KP_DOWN:
        // find visible cell above
        int rows = m_table.getData().getRowCount();
        int below = m_selectedRow + 1;
        while ( m_table.getHeightByRow( below ) <= 0 && below < rows )
          below++;

        // ensure only cell visible below is selected
        if ( below < rows )
          m_selectedRow = below;
        m_table.clearAllSelection();
        m_table.setSelection( m_selectedColumn, m_selectedRow, true );
        m_table.scrollTo( m_selectedColumn, m_selectedRow );
        redrawAll();
        break;

      case RIGHT:
        if ( event.isAltDown() )
        {
          Set<Integer> indent = JPlanner.plan.tasks.canIndent( m_table.getSelectedRows() );
          if ( !indent.isEmpty() )
            JPlanner.plan.undostack().push( new CommandTaskIndent( indent ) );
          break;
        }
      case KP_RIGHT:
        // find visible cell to right
        int columns = m_table.getData().getColumnCount();
        int right = m_selectedColumn + 1;
        while ( m_table.getWidthByColumnPosition( right ) <= 0 && right < columns )
          right++;

        if ( right < columns && m_selectedColumn != right )
        {
          m_selectedColumn = right;
          m_table.scrollTo( m_selectedColumn, m_selectedRow );
          redraw = true;
        }
        if ( m_table.selectionCount() > 1 || redraw )
        {
          m_table.clearAllSelection();
          m_table.setSelection( m_selectedColumn, m_selectedRow, true );
          redraw = true;
        }
        if ( redraw )
          redrawAll();

        break;

      case LEFT:
        if ( event.isAltDown() )
        {
          Set<Integer> outdent = JPlanner.plan.tasks.canOutdent( m_table.getSelectedRows() );
          if ( !outdent.isEmpty() )
            JPlanner.plan.undostack().push( new CommandTaskOutdent( outdent ) );
          break;
        }
      case KP_LEFT:
        // find visible cell to left
        int left = m_selectedColumn - 1;
        while ( m_table.getWidthByColumnPosition( left ) <= 0 && left >= 0 )
          left--;

        if ( left >= 0 && m_selectedColumn != left )
        {
          m_selectedColumn = left;
          m_table.scrollTo( m_selectedColumn, m_selectedRow );
          redraw = true;
        }
        if ( m_table.selectionCount() > 1 || redraw )
        {
          m_table.clearAllSelection();
          m_table.setSelection( m_selectedColumn, m_selectedRow, true );
          redraw = true;
        }
        if ( redraw )
          redrawAll();

        break;

      case F2:
        // open cell editor with current cell contents
        openCellEditor( null );
        break;

      default:
        break;
    }
  }

  /*************************************** openCellEditor ****************************************/
  private void openCellEditor( Object value )
  {
    // scroll to cell
    m_table.scrollTo( m_selectedColumn, m_selectedRow );
    m_table.finishAnimation();

    // open cell editor for currently selected table cell
    int columnIndex = m_table.getColumnIndexByPosition( m_selectedColumn );
    int rowIndex = m_selectedRow;
    AbstractCellEditor editor = m_table.getData().getEditor( columnIndex, rowIndex );

    // open editor if one available
    if ( editor != null && editor.isValueValid( value ) )
      editor.open( m_table, value, MoveDirection.DOWN );
  }

  /***************************************** scrollTable *****************************************/
  private void scrollTable()
  {
    // determine whether table needs to be scrolled to make reorder marker visible
    if ( getCursor() == CURSOR_DOWNARROW )
    {
      // vertical marker
      if ( m_x < m_table.getVerticalHeaderWidth() )
      {
        m_table.animateScrollToLeft();
        return;
      }

      if ( m_x > getWidth() )
      {
        m_table.animateScrollToRight();
        return;
      }
    }
    else
    {
      // horizontal marker
      if ( m_y < m_table.getHorizontalHeaderHeight() )
      {
        m_table.animateScrollToUp();
        return;
      }

      if ( m_y > getHeight() )
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
    Rectangle2D rect = getExpandHideMarkerRectangle( m_column, m_row );
    if ( rect != null )
      return rect.contains( m_x, m_y );

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
      double y = m_table.getYStartByRow( row ) + m_table.getHeightByRow( row ) / 2.0;
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

    if ( getCursor() == CURSOR_DOWNARROW )
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
      if ( getCursor() == CURSOR_DOWNARROW )
      {
        // vertical reorder marker
        m_column = m_table.getColumnPositionAtX( m_x );
        int x = m_table.getXStartByColumnPosition( m_column );
        int w = m_table.getWidthByColumnPosition( m_column );
        if ( m_x > x + w / 2 )
        {
          m_column++;
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
        m_row = m_table.getRowAtY( m_y );
        int y = m_table.getYStartByRow( m_row );
        int h = m_table.getHeightByRow( m_row );
        if ( m_y > y + h / 2 )
        {
          m_row++;
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
    if ( m_index < 0 )
    {
      // start reorder
      m_index = m_row;
      m_offset = m_y - m_table.getYStartByRow( m_row );
      m_table.clearAllSelection();
      redrawAll();

      // create reorder slider (translucent cell header)
      int h = m_table.getHeightByRow( m_index );
      int w = m_table.getVerticalHeaderWidth();
      String text = m_table.getData().getRowTitle( m_index );
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
    m_reorderSlider.setTranslateY( m_y - m_offset );

    // set marker position
    setMarkerPosition();

    // scroll table if needed to make marker visible
    scrollTable();
  }

  /************************************ columnReorderDragged *************************************/
  private void columnReorderDragged()
  {
    // is a reorder already in progress
    if ( m_index < 0 )
    {
      // start reorder
      m_index = m_table.getColumnIndexByPosition( m_column );
      m_offset = m_x - m_table.getXStartByColumnPosition( m_column );
      m_table.clearAllSelection();
      redrawAll();

      // create reorder slider (translucent cell header)
      int w = m_table.getWidthByColumnIndex( m_index );
      int h = m_table.getHorizontalHeaderHeight();
      String text = m_table.getData().getColumnTitle( m_index );
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
    m_reorderSlider.setTranslateX( m_x - m_offset );

    // set marker position
    setMarkerPosition();

    // scroll table if needed to make marker visible
    scrollTable();
  }

}
