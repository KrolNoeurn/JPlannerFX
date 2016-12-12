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

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import rjc.jplanner.JPlanner;
import rjc.jplanner.gui.Colors;
import rjc.jplanner.gui.table.AbstractCellEditor.MoveDirection;
import rjc.jplanner.gui.table.Table.Alignment;

/*************************************************************************************************/
/***************************** Displays data in gui scrollable table *****************************/
/*************************************************************************************************/

public class TableCanvas extends Canvas
{
  // structure that contains one line of text to be drawn in cell
  public class TextLine
  {
    public String  txt;
    public double  x;
    public double  y;
    public boolean ellipsis = false;

    @Override
    public String toString()
    {
      return "TextLine " + txt + " " + x + " " + y + " " + ellipsis;
    }
  }

  public static final String  ELLIPSIS          = "...";          // ellipsis to show text has been truncated
  public static final int     CELL_PADDING      = 4;              // cell padding for text left & right edges
  private static final int    PROXIMITY         = 4;              // used to distinguish resize from reorder
  private static final int    INDENT            = 14;             // cell left padding per indent level 

  private static final Cursor CURSOR_H_RESIZE   = Cursor.H_RESIZE;
  private static final Cursor CURSOR_V_RESIZE   = Cursor.V_RESIZE;
  private static final Cursor CURSOR_DEFAULT    = Cursor.DEFAULT;
  private static Cursor       CURSOR_DOWNARROW;
  private static Cursor       CURSOR_RIGHTARROW;
  private static Cursor       CURSOR_CROSS;

  private Table               m_table;                            // table displaying this table canvas
  private int                 m_x;                                // last mouse move/drag x
  private int                 m_y;                                // last mouse move/drag y
  private int                 m_column;                           // last mouse move or reorder column position
  private int                 m_row;                              // last mouse move or reorder row position
  private int                 m_offset;                           // x or y resize/reorder offset
  private int                 m_selectedColumn  = -1;             // column of last single cell selected
  private int                 m_selectedRow     = -1;             // row of last single cell selected
  private int                 m_index           = -1;             // column or row index for resize or reorder
  private Canvas              m_reorderSlider;                    // visual slider for when reordering
  private Canvas              m_reorderMarker;                    // visual marker for new position when reordering

  private boolean             m_recentlyRedrawn = false;          // set true if has table be totally redrawn recently

  /***************************************** constructor *****************************************/
  public TableCanvas( Table table )
  {
    // setup table canvas
    super();
    m_table = table;

    // ensure cursors are setup
    if ( CURSOR_DOWNARROW == null )
    {
      CURSOR_DOWNARROW = new ImageCursor( new Image( getClass().getResourceAsStream( "arrowdown.png" ) ), 7, 16 );
      CURSOR_RIGHTARROW = new ImageCursor( new Image( getClass().getResourceAsStream( "arrowright.png" ) ), 16, 24 );
      CURSOR_CROSS = new ImageCursor( new Image( getClass().getResourceAsStream( "cross.png" ) ), 16, 20 );
    }

    // when size changes draw new bits
    widthProperty().addListener( ( observable, oldW, newW ) -> drawWidth( oldW.intValue(), newW.intValue() ) );
    heightProperty().addListener( ( observable, oldH, newH ) -> drawHeight( oldH.intValue(), newH.intValue() ) );

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

  /****************************************** redrawAll ******************************************/
  public void redrawAll()
  {
    // redraw whole canvas
    drawWidth( 0, (int) getWidth() );
    m_recentlyRedrawn = true;
  }

  /****************************************** drawWidth ******************************************/
  public void drawWidth( int oldW, int newW )
  {
    // canvas update means something important such as scrolling or resize, therefore end editing
    AbstractCellEditor.endEditing();

    // draw only if increase in width
    if ( getHeight() <= 0.0 || newW <= oldW )
      return;

    // draw only if not beyond edge of table
    int headerWidth = m_table.getVerticalHeaderWidth();
    if ( oldW > m_table.getBodyWidth() + headerWidth )
      return;

    // clear background
    GraphicsContext gc = getGraphicsContext2D();
    gc.clearRect( oldW, 0.0, newW, getHeight() );

    // check if any columns need to be drawn 
    if ( newW > headerWidth )
    {
      int column1 = m_table.getColumnPositionAtX( oldW < headerWidth ? headerWidth : oldW );
      int column2 = m_table.getColumnPositionAtX( newW );
      oldW = m_table.getXStartByColumnPosition( column1 );

      // draw column cells
      if ( getHeight() > m_table.getHorizontalHeaderHeight() )
        for ( int columnPos = column1; columnPos <= column2; columnPos++ )
          drawColumnCells( columnPos );

      // draw horizontal header
      for ( int columnPos = column1; columnPos <= column2; columnPos++ )
        drawColumnHeader( columnPos );
    }

    // check if any vertical header needs to be drawn
    if ( oldW <= headerWidth )
    {
      int row1 = m_table.getRowAtY( m_table.getHorizontalHeaderHeight() );
      int row2 = m_table.getRowAtY( (int) getHeight() );

      for ( int row = row1; row <= row2; row++ )
        drawRowHeader( row );

      drawHeaderCorner();
    }
  }

  /***************************************** drawHeight ******************************************/
  public void drawHeight( int oldH, int newH )
  {
    // canvas update means something important such as scrolling or resize, therefore end editing
    AbstractCellEditor.endEditing();

    // draw only if increase in height
    if ( getWidth() <= 0.0 || newH <= oldH )
      return;

    // draw only if not below edge of table
    int headerHeight = m_table.getHorizontalHeaderHeight();
    if ( oldH > m_table.getBodyHeight() + headerHeight )
      return;

    // clear background
    GraphicsContext gc = getGraphicsContext2D();
    gc.clearRect( 0.0, oldH, getWidth(), newH );

    // check if any rows need to be drawn 
    if ( newH > headerHeight )
    {
      int row1 = m_table.getRowAtY( oldH < headerHeight ? headerHeight : oldH );
      int row2 = m_table.getRowAtY( newH );
      oldH = m_table.getYStartByRow( row1 );

      // draw column cells
      if ( getWidth() > m_table.getVerticalHeaderWidth() )
        for ( int row = row1; row <= row2; row++ )
          drawRowCells( row );

      // draw vertical header
      for ( int row = row1; row <= row2; row++ )
        drawRowHeader( row );
    }

    // check if any horizontal header needs to be drawn
    if ( oldH <= headerHeight )
    {
      int column1 = m_table.getColumnPositionAtX( m_table.getVerticalHeaderWidth() );
      int column2 = m_table.getColumnPositionAtX( (int) getWidth() );

      for ( int columnPos = column1; columnPos <= column2; columnPos++ )
        drawColumnHeader( columnPos );

      drawHeaderCorner();
    }
  }

  /*************************************** drawColumnCells ***************************************/
  private void drawColumnCells( int columnPos )
  {
    // do not draw hidden columns
    int w = m_table.getWidthByColumnPosition( columnPos );
    if ( w <= 0 )
      return;

    // draw column cells on canvas
    int row1 = m_table.getRowAtY( m_table.getHorizontalHeaderHeight() );
    int row2 = m_table.getRowAtY( (int) getHeight() );

    int x = m_table.getXStartByColumnPosition( columnPos );
    int y = m_table.getYStartByRow( row1 );

    for ( int row = row1; row <= row2; row++ )
    {
      int h = m_table.getHeightByRow( row );
      if ( h > 0 )
      {
        drawCell( x, y, w, h, columnPos, row );
        y += h;
      }
    }
  }

  /**************************************** drawRowCells *****************************************/
  private void drawRowCells( int row )
  {
    // do not draw hidden rows
    int h = m_table.getHeightByRow( row );
    if ( h <= 0 )
      return;

    // draw row cell on canvas
    int column1 = m_table.getColumnPositionAtX( m_table.getVerticalHeaderWidth() );
    int column2 = m_table.getColumnPositionAtX( (int) getWidth() );

    int x = m_table.getXStartByColumnPosition( column1 );
    int y = m_table.getYStartByRow( row );

    for ( int columnPos = column1; columnPos <= column2; columnPos++ )
    {
      int w = m_table.getWidthByColumnPosition( columnPos );
      if ( w > 0 )
      {
        drawCell( x, y, w, h, columnPos, row );
        x += w;
      }
    }
  }

  /****************************************** drawCell *******************************************/
  private void drawCell( int x, int y, int w, int h, int columnPos, int row )
  {
    // draw body cell
    GraphicsContext gc = getGraphicsContext2D();
    int columnIndex = m_table.getColumnIndexByPosition( columnPos );

    // fill
    boolean m_selected = m_table.isSelected( columnPos, row );
    Paint color = m_table.getDataSource().getCellBackground( columnIndex, row );
    if ( m_selected && color == Colors.NORMAL_HEADER )
      gc.setFill( Colors.SELECTED_HEADER );
    else if ( m_selected )
      gc.setFill( Colors.SELECTED_CELL );
    else
      gc.setFill( color );
    gc.fillRect( x, y, w - 1, h - 1 );

    // grid
    gc.setStroke( Colors.TABLE_GRID );
    gc.strokeLine( x + w - 0.5, y + 0.5, x + w - 0.5, y + h - 0.5 );
    gc.strokeLine( x + 0.5, y + h - 0.5, x + w - 0.5, y + h - 0.5 );

    // text
    String text = m_table.getDataSource().getCellText( columnIndex, row );
    if ( text != null )
    {
      Font font = m_table.getDataSource().getCellFont( columnIndex, row );
      gc.setFont( font );

      // text alignment and colour
      int indent = m_table.getDataSource().getCellIndent( columnIndex, row );
      Alignment alignment = m_table.getDataSource().getCellAlignment( columnIndex, row );
      ArrayList<TextLine> lines = getTextLines( text, font, alignment, w - indent * INDENT, h );
      if ( m_selected )
        gc.setFill( Colors.SELECTED_TEXT );
      else
        gc.setFill( Colors.NORMAL_TEXT );

      // draw text
      gc.setFontSmoothingType( FontSmoothingType.LCD );
      x += indent * INDENT;
      for ( TextLine line : lines )
        gc.fillText( line.txt, x + line.x, y + line.y );

      // draw expand-hide marker
      Rectangle2D rect = getExpandHideMarkerRectangle( columnIndex, row, indent, x, y, h );
      if ( rect != null )
      {
        // draw outer box
        gc.setStroke( gc.getFill() );
        gc.strokeRect( rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight() );

        // determine box centre
        double cx = rect.getMinX() + rect.getWidth() / 2.0;
        double cy = rect.getMinY() + rect.getHeight() / 2.0;

        // draw horizontal line (minus)
        gc.strokeRect( rect.getMinX() + 2.0, cy - 0.5, rect.getWidth() - 4.0, 1.0 );

        // draw vertical line (to make plus) only if row is marked as collapsed        
        if ( m_table.isRowCollapsed( row ) )
          gc.strokeRect( cx - 0.5, rect.getMinY() + 2.0, 1.0, rect.getHeight() - 4.0 );
      }
    }
  }

  /**************************************** drawRowHeader ****************************************/
  private void drawRowHeader( int rowIndex )
  {
    // do not draw hidden rows
    int h = m_table.getHeightByRow( rowIndex );
    if ( h <= 0 )
      return;

    // draw row header
    GraphicsContext gc = getGraphicsContext2D();
    int y = m_table.getYStartByRow( rowIndex );
    int w = m_table.getVerticalHeaderWidth();
    String text = m_table.getDataSource().getRowTitle( rowIndex );
    boolean selected = m_table.doesRowHaveSelection( rowIndex );

    drawRowHeaderCell( gc, y, w, h, text, selected );
  }

  /************************************** drawRowHeaderCell **************************************/
  private void drawRowHeaderCell( GraphicsContext gc, int y, double w, double h, String text, boolean selected )
  {
    // draw row header - fill
    if ( selected )
      gc.setFill( Colors.SELECTED_HEADER );
    else
      gc.setFill( Colors.NORMAL_HEADER );
    gc.fillRect( 0, y, w - 1.0, h - 1.0 );

    // grid
    gc.setStroke( Colors.TABLE_GRID );
    gc.strokeLine( w - 0.5, y + 0.5, w - 0.5, y + h - 0.5 );
    gc.strokeLine( 0.5, y + h - 0.5, w - 0.5, y + h - 0.5 );

    // label
    ArrayList<TextLine> lines = getTextLines( text, null, Alignment.MIDDLE, w, h );
    if ( selected )
      gc.setFill( Colors.SELECTED_TEXT );
    else
      gc.setFill( Colors.NORMAL_TEXT );

    gc.setFontSmoothingType( FontSmoothingType.LCD );
    gc.setFont( Font.getDefault() );
    for ( TextLine line : lines )
      gc.fillText( line.txt, line.x, y + line.y );
  }

  /*************************************** drawColumnHeader **************************************/
  private void drawColumnHeader( int columnPos )
  {
    // do not draw hidden columns
    int w = m_table.getWidthByColumnPosition( columnPos );
    if ( w <= 0 )
      return;

    // draw column header
    GraphicsContext gc = getGraphicsContext2D();
    int x = m_table.getXStartByColumnPosition( columnPos );
    int h = m_table.getHorizontalHeaderHeight();
    int columnIndex = m_table.getColumnIndexByPosition( columnPos );
    String text = m_table.getDataSource().getColumnTitle( columnIndex );
    boolean selected = m_table.doesColumnHaveSelection( columnPos );

    drawColumnHeaderCell( gc, x, w, h, text, selected );
  }

  /************************************ drawColumnHeaderCell *************************************/
  private void drawColumnHeaderCell( GraphicsContext gc, int x, int w, int h, String text, boolean selected )
  {
    // draw column header - fill
    if ( selected )
      gc.setFill( Colors.SELECTED_HEADER );
    else
      gc.setFill( Colors.NORMAL_HEADER );
    gc.fillRect( x, 0, w - 1.0, h - 1.0 );

    // grid
    gc.setStroke( Colors.TABLE_GRID );
    gc.strokeLine( x + w - 0.5, 0.5, x + w - 0.5, h - 0.5 );
    gc.strokeLine( x + 0.5, h - 0.5, x + w - 0.5, h - 0.5 );

    // label
    ArrayList<TextLine> lines = getTextLines( text, null, Alignment.MIDDLE, w, h );
    if ( selected )
      gc.setFill( Colors.SELECTED_TEXT );
    else
      gc.setFill( Colors.NORMAL_TEXT );

    gc.setFontSmoothingType( FontSmoothingType.LCD );
    gc.setFont( Font.getDefault() );
    for ( TextLine line : lines )
      gc.fillText( line.txt, x + line.x, line.y );
  }

  /*************************************** drawHeaderCorner **************************************/
  private void drawHeaderCorner()
  {
    // draw header corner on canvas
    GraphicsContext gc = getGraphicsContext2D();
    double w = m_table.getVerticalHeaderWidth();
    double h = m_table.getHorizontalHeaderHeight();

    // fill
    gc.setFill( Colors.NORMAL_HEADER );
    gc.fillRect( 0, 0, w - 1.0, h - 1.0 );

    // grid
    gc.setStroke( Colors.TABLE_GRID );
    gc.strokeLine( w - 0.5, 0.5, w - 0.5, h - 0.5 );
    gc.strokeLine( 0.5, h - 0.5, w - 0.5, h - 0.5 );
  }

  /**************************************** getTextLines *****************************************/
  private ArrayList<TextLine> getTextLines( String string, Font font, Alignment alignment, double w, double h )
  {
    // initialise variables
    ArrayList<TextLine> lines = new ArrayList<TextLine>();
    final double width = w - 2 * CELL_PADDING;
    final double height = h - 0.5 * CELL_PADDING;
    double x = CELL_PADDING;
    double y = 0.0;
    Bounds bounds;
    double alignX = 0.0;
    double alignY = 0.0;
    Text text = new Text();
    text.setFont( font );

    // determine how text needs to be split into lines
    while ( string != null )
    {
      TextLine line = new TextLine();
      text.setText( string );
      bounds = text.getLayoutBounds();

      if ( bounds.getWidth() <= width )
      {
        // text width fits in cell
        if ( alignment == Alignment.RIGHT )
          alignX = width - bounds.getWidth();
        if ( alignment == Alignment.MIDDLE )
          alignX = ( width - bounds.getWidth() ) / 2.0;

        line.txt = string;
        line.x = x + alignX;
        line.y = y;
        string = null;
      }
      else
      {
        // text width exceeds cell width
        boolean isLastLine = y + bounds.getHeight() + bounds.getHeight() > height;
        if ( isLastLine )
        {
          // last line so fit as much as possible with ellipsis on end
          int cut = string.length();
          try
          {
            do
            {
              cut--;
              text.setText( string.substring( 0, cut ) + ELLIPSIS );
              bounds = text.getLayoutBounds();
            }
            while ( bounds.getWidth() > width );
          }
          catch ( StringIndexOutOfBoundsException tooLittleSpace )
          {
            cut = 0;
          }

          if ( alignment == Alignment.RIGHT )
            alignX = width - bounds.getWidth();
          if ( alignment == Alignment.MIDDLE )
            alignX = ( width - bounds.getWidth() ) / 2.0;

          line.txt = string.substring( 0, cut ) + ELLIPSIS;
          line.x = x + alignX;
          line.y = y;
          line.ellipsis = true;
          string = null;
        }
        else
        {
          // not last line so break at word end
          int cut = string.length();
          try
          {
            do
            {
              cut--;
              text.setText( string.substring( 0, cut ) );
              bounds = text.getLayoutBounds();
            }
            while ( bounds.getWidth() > width || !Character.isWhitespace( string.charAt( cut ) ) );

            if ( alignment == Alignment.RIGHT )
              alignX = width - bounds.getWidth();
            if ( alignment == Alignment.MIDDLE )
              alignX = ( width - bounds.getWidth() ) / 2.0;

            line.txt = string.substring( 0, cut );
            line.x = x + alignX;
            line.y = y;

            while ( Character.isWhitespace( string.charAt( cut ) ) )
              cut++;
            string = string.substring( cut );
          }
          catch ( StringIndexOutOfBoundsException wordTooLong )
          {
            // even one word is too long so don't bother looking for word end
            cut = string.length();
            do
            {
              cut--;
              text.setText( string.substring( 0, cut ) + ELLIPSIS );
              bounds = text.getLayoutBounds();
            }
            while ( bounds.getWidth() > width && cut > 0 );

            if ( alignment == Alignment.RIGHT )
              alignX = width - bounds.getWidth();
            if ( alignment == Alignment.MIDDLE )
              alignX = ( width - bounds.getWidth() ) / 2.0;

            line.txt = string.substring( 0, cut ) + ELLIPSIS;
            line.x = x + alignX;
            line.y = y;
            line.ellipsis = true;

            try
            {
              while ( !Character.isWhitespace( string.charAt( cut ) ) )
                cut++;
              while ( Character.isWhitespace( string.charAt( cut ) ) )
                cut++;
              string = string.substring( cut );
            }
            catch ( StringIndexOutOfBoundsException noWordsRemain )
            {
              string = null;
            }
          }

        }
      }

      alignY = ( h - y + 1 ) / 2.0 + bounds.getMinY() + bounds.getHeight();
      if ( alignY < bounds.getHeight() / 1.8 )
        alignY = bounds.getHeight() / 1.8;

      lines.add( line );
      y += Math.rint( bounds.getHeight() );
    }

    // move all lines down to vertically centre within cell
    for ( TextLine line : lines )
      line.y += alignY;

    return lines;
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
      int h = m_table.getHeightByRowIndex( m_index );
      int w = m_table.getVerticalHeaderWidth();
      String text = m_table.getDataSource().getRowTitle( m_index );
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
      String text = m_table.getDataSource().getColumnTitle( m_index );
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
        int rightmost = m_table.getDataSource().getColumnCount() - 1;
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
        int rows = m_table.getDataSource().getRowCount();
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
      case KP_RIGHT:
        // find visible cell to right
        int columns = m_table.getDataSource().getColumnCount();
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
          if ( m_column >= m_table.getDataSource().getColumnCount() )
            m_column = m_table.getDataSource().getColumnCount() - 1;
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
          if ( m_row >= m_table.getDataSource().getRowCount() )
            m_row = m_table.getDataSource().getRowCount() - 1;
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
    int indent = m_table.getDataSource().getCellIndent( columnIndex, row );
    if ( indent <= 0 )
      return null;

    int rowBelow = m_table.getNonNullRowBelow( row );
    if ( rowBelow <= 0 )
      return null;

    if ( m_table.getDataSource().getCellIndent( columnIndex, rowBelow ) > indent )
    {
      double x = m_table.getXStartByColumnPosition( columnPos ) + indent * INDENT;
      double y = m_table.getYStartByRow( row ) + m_table.getHeightByRow( row ) / 2.0;
      return new Rectangle2D( x - 8.5, y - 4.5, 9.0, 9.0 );
    }

    return null;
  }

  /******************************** getExpandHideMarkerRectangle ********************************/
  private Rectangle2D getExpandHideMarkerRectangle( int columnIndex, int row, int indent, int x, int y, int h )
  {
    // return expand-hide marker rectangle, or null if none
    if ( indent <= 0 )
      return null;

    int rowBelow = m_table.getNonNullRowBelow( row );
    if ( rowBelow <= 0 )
      return null;

    if ( m_table.getDataSource().getCellIndent( columnIndex, rowBelow ) > indent )
      return new Rectangle2D( x - 8.5, y + h / 2 - 4.5, 9.0, 9.0 );

    return null;
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
      m_table.setCanvasScrollBars();
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

      m_table.setHeightByRowIndex( m_index, m_y - m_offset );
      m_recentlyRedrawn = false;
      m_table.setCanvasScrollBars();
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
        m_table.expandRow( m_row );
      else
        m_table.collapsedRow( m_row );

      // redraw table from mouse y downwards 
      drawHeight( m_y, (int) getHeight() );
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
    AbstractCellEditor editor = m_table.getDataSource().getEditor( columnIndex, rowIndex );

    // open editor if one available
    if ( editor != null && editor.validValue( value ) )
      editor.open( m_table, value, MoveDirection.DOWN );
  }

}
