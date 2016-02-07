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

import javafx.geometry.Bounds;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
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

  public static String       ELLIPSIS              = "...";                      // ellipsis to show text has been truncated
  public static int          CELL_PADDING          = 4;                          // cell padding for text left & right edges
  private static int         PROXIMITY             = 4;                          // used to distinguish resize from reorder

  public static final Color  COLOR_GRID            = Color.SILVER;
  public static final Color  COLOR_DISABLED_CELL   = Color.rgb( 227, 227, 227 ); // medium grey
  public static final Color  COLOR_NORMAL_CELL     = Color.WHITE;
  public static final Color  COLOR_NORMAL_HEADER   = Color.rgb( 240, 240, 240 ); // light grey
  public static final Color  COLOR_NORMAL_TEXT     = Color.BLACK;
  public static final Color  COLOR_SELECTED_CELL   = Color.rgb( 51, 153, 255 );  // light blue;
  public static final Color  COLOR_SELECTED_HEADER = Color.rgb( 192, 192, 192 ); // medium dark grey
  public static final Color  COLOR_SELECTED_TEXT   = Color.WHITE;

  public static final Cursor CURSOR_H_RESIZE       = Cursor.H_RESIZE;
  public static final Cursor CURSOR_V_RESIZE       = Cursor.V_RESIZE;
  public static final Cursor CURSOR_DEFAULT        = Cursor.DEFAULT;
  public static Cursor       CURSOR_DOWNARROW;
  public static Cursor       CURSOR_RIGHTARROW;
  public static Cursor       CURSOR_CROSS;

  private Table              m_table;                                            // table defining this table canvas
  private int                m_x;                                                // last mouse move x
  private int                m_y;                                                // last mouse move y
  private int                m_columnPos;                                        // column position associated with last mouse move
  private int                m_rowPos;                                           // row position associated with last mouse move
  private int                m_index               = -1;                         // column or row index for resize or reorder
  private Canvas             m_reorderSlider;
  private Canvas             m_reorderPointer;

  public boolean             redrawn;                                            // has table be totally redrawn recently?

  /***************************************** constructor *****************************************/
  public TableCanvas( Table table )
  {
    // setup table canvas
    super();
    m_table = table;

    if ( CURSOR_DOWNARROW == null )
    {
      CURSOR_DOWNARROW = new ImageCursor( new Image( getClass().getResourceAsStream( "arrowdown.png" ) ), 7, 16 );
      CURSOR_RIGHTARROW = new ImageCursor( new Image( getClass().getResourceAsStream( "arrowright.png" ) ), 16, 24 );
      CURSOR_CROSS = new ImageCursor( new Image( getClass().getResourceAsStream( "cross.png" ) ), 16, 20 );
    }

    // when size changes draw new bits
    widthProperty().addListener( ( observable, oldW, newW ) -> drawWidth( (double) oldW, (double) newW ) );
    heightProperty().addListener( ( observable, oldH, newH ) -> drawHeight( (double) oldH, (double) newH ) );

    // when mouse moves
    setOnMouseMoved( event -> mouseMoved( event ) );
    setOnMouseDragged( event -> mouseDragged( event ) );
    setOnMouseReleased( event -> mouseReleased( event ) );
  }

  /***************************************** isResizable *****************************************/
  @Override
  public boolean isResizable()
  {
    return true;
  }

  /****************************************** prefWidth ******************************************/
  @Override
  public double prefWidth( double height )
  {
    return 0.0;
  }

  /***************************************** prefHeight ******************************************/
  @Override
  public double prefHeight( double width )
  {
    return 0.0;
  }

  /****************************************** redrawAll ******************************************/
  public void redrawAll()
  {
    // redraw whole canvas
    drawWidth( 0.0, getWidth() );
    redrawn = true;
  }

  /****************************************** drawWidth ******************************************/
  public void drawWidth( double oldW, double newW )
  {
    // draw only if increase in width
    if ( newW <= oldW )
      return;

    // draw only if not beyond edge of table
    if ( oldW > m_table.getBodyWidth() + m_table.getVerticalHeaderWidth() )
      return;

    // clear background
    GraphicsContext gc = getGraphicsContext2D();
    gc.clearRect( oldW, 0.0, newW, getHeight() );

    // check if any columns need to be drawn 
    if ( newW > m_table.getVerticalHeaderWidth() )
    {
      int column1 = m_table.getColumnPositionAtX( (int) oldW );
      int column2 = m_table.getColumnPositionAtX( (int) newW );
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
    if ( oldW <= m_table.getVerticalHeaderWidth() )
    {
      int row1 = m_table.getRowPositionAtY( m_table.getHorizontalHeaderHeight() );
      int row2 = m_table.getRowPositionAtY( (int) getHeight() );

      for ( int rowPos = row1; rowPos <= row2; rowPos++ )
        drawRowHeader( rowPos );

      drawHeaderCorner();
    }
  }

  /***************************************** drawHeight ******************************************/
  public void drawHeight( double oldH, double newH )
  {
    // draw only if increase in height
    if ( newH <= oldH )
      return;

    // draw only if not below edge of table
    if ( oldH > m_table.getBodyHeight() + m_table.getHorizontalHeaderHeight() )
      return;

    // clear background
    GraphicsContext gc = getGraphicsContext2D();
    gc.clearRect( 0.0, oldH, getWidth(), newH );

    // check if any rows need to be drawn 
    if ( newH > m_table.getHorizontalHeaderHeight() )
    {
      int row1 = m_table.getRowPositionAtY( (int) oldH );
      int row2 = m_table.getRowPositionAtY( (int) newH );
      oldH = m_table.getYStartByRowPosition( row1 );

      // draw column cells
      if ( getWidth() > m_table.getVerticalHeaderWidth() )
        for ( int rowPos = row1; rowPos <= row2; rowPos++ )
          drawRowCells( rowPos );

      // draw vertical header
      for ( int rowPos = row1; rowPos <= row2; rowPos++ )
        drawRowHeader( rowPos );
    }

    // check if any horizontal header needs to be drawn
    if ( oldH <= m_table.getHorizontalHeaderHeight() )
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
    // draw column cells on canvas
    int row1 = m_table.getRowPositionAtY( m_table.getHorizontalHeaderHeight() );
    int row2 = m_table.getRowPositionAtY( (int) getHeight() );

    int x = m_table.getXStartByColumnPosition( columnPos );
    int y = m_table.getYStartByRowPosition( row1 );
    int w = m_table.getWidthByColumnPosition( columnPos );

    for ( int rowPos = row1; rowPos <= row2; rowPos++ )
    {
      int h = m_table.getHeightByRowPosition( rowPos );
      if ( h > 0 )
      {
        drawCell( x, y, w, h, columnPos, rowPos );
        y += h;
      }
    }
  }

  /**************************************** drawRowCells *****************************************/
  private void drawRowCells( int rowPos )
  {
    // draw row cell on canvas
    int column1 = m_table.getColumnPositionAtX( m_table.getVerticalHeaderWidth() );
    int column2 = m_table.getColumnPositionAtX( (int) getWidth() );

    int x = m_table.getXStartByColumnPosition( column1 );
    int y = m_table.getYStartByRowPosition( rowPos );
    int h = m_table.getHeightByRowPosition( rowPos );

    for ( int columnPos = column1; columnPos <= column2; columnPos++ )
    {
      int w = m_table.getWidthByColumnPosition( columnPos );
      if ( w > 0 )
      {
        drawCell( x, y, w, h, columnPos, rowPos );
        x += w;
      }
    }
  }

  /****************************************** drawCell *******************************************/
  private void drawCell( int x, int y, int w, int h, int columnPos, int rowPos )
  {
    // draw body cell
    GraphicsContext gc = getGraphicsContext2D();
    int columnIndex = m_table.getColumnIndexByPosition( columnPos );
    int rowIndex = m_table.getRowIndexByPosition( rowPos );

    // fill
    boolean m_selected = false;
    Paint color = m_table.getDataSource().getCellBackground( columnIndex, rowIndex );
    if ( m_selected && color == TableCanvas.COLOR_NORMAL_HEADER )
      gc.setFill( TableCanvas.COLOR_SELECTED_HEADER );
    else if ( m_selected )
      gc.setFill( TableCanvas.COLOR_SELECTED_CELL );
    else
      gc.setFill( color );
    gc.fillRect( x, y, w - 1, h - 1 );

    // grid
    gc.setStroke( TableCanvas.COLOR_GRID );
    gc.strokeLine( x + w - 0.5, y + 0.5, x + w - 0.5, y + h - 0.5 );
    gc.strokeLine( x + 0.5, y + h - 0.5, x + w - 0.5, y + h - 0.5 );

    // text
    String text = m_table.getDataSource().getCellText( columnIndex, rowIndex );
    Alignment alignment = m_table.getDataSource().getCellAlignment( columnIndex, rowIndex );
    ArrayList<TextLine> lines = getTextLines( text, alignment, w, h );
    if ( m_selected )
      gc.setFill( TableCanvas.COLOR_SELECTED_TEXT );
    else
      gc.setFill( TableCanvas.COLOR_NORMAL_TEXT );

    gc.setFontSmoothingType( FontSmoothingType.LCD );
    for ( TextLine line : lines )
      gc.fillText( line.txt, x + line.x, y + line.y );
  }

  /**************************************** drawRowHeader ****************************************/
  private void drawRowHeader( int rowPos )
  {
    // draw row header
    GraphicsContext gc = getGraphicsContext2D();
    int y = m_table.getYStartByRowPosition( rowPos );
    int w = m_table.getVerticalHeaderWidth();
    int h = m_table.getHeightByRowPosition( rowPos );
    int rowIndex = m_table.getRowIndexByPosition( rowPos );
    String text = m_table.getDataSource().getRowTitle( rowIndex );

    drawRowHeaderCell( gc, y, w, h, text, false );
  }

  /************************************** drawRowHeaderCell **************************************/
  private void drawRowHeaderCell( GraphicsContext gc, int y, double w, double h, String text, boolean selected )
  {
    // draw row header - fill
    if ( selected )
      gc.setFill( TableCanvas.COLOR_SELECTED_HEADER );
    else
      gc.setFill( TableCanvas.COLOR_NORMAL_HEADER );
    gc.fillRect( 0, y, w - 1.0, h - 1.0 );

    // grid
    gc.setStroke( TableCanvas.COLOR_GRID );
    gc.strokeLine( w - 0.5, y + 0.5, w - 0.5, y + h - 0.5 );
    gc.strokeLine( 0.5, y + h - 0.5, w - 0.5, y + h - 0.5 );

    // label
    ArrayList<TextLine> lines = getTextLines( text, Alignment.MIDDLE, w, h );
    if ( selected )
      gc.setFill( TableCanvas.COLOR_SELECTED_TEXT );
    else
      gc.setFill( TableCanvas.COLOR_NORMAL_TEXT );

    gc.setFontSmoothingType( FontSmoothingType.LCD );
    for ( TextLine line : lines )
      gc.fillText( line.txt, line.x, y + line.y );
  }

  /*************************************** drawColumnHeader **************************************/
  private void drawColumnHeader( int columnPos )
  {
    // draw column header
    GraphicsContext gc = getGraphicsContext2D();
    int x = m_table.getXStartByColumnPosition( columnPos );
    int w = m_table.getWidthByColumnPosition( columnPos );
    int h = m_table.getHorizontalHeaderHeight();
    int columnIndex = m_table.getColumnIndexByPosition( columnPos );
    String text = m_table.getDataSource().getColumnTitle( columnIndex );

    drawColumnHeaderCell( gc, x, w, h, text, false );
  }

  /************************************ drawColumnHeaderCell *************************************/
  private void drawColumnHeaderCell( GraphicsContext gc, int x, int w, int h, String text, boolean selected )
  {
    // draw column header - fill
    if ( selected )
      gc.setFill( TableCanvas.COLOR_SELECTED_HEADER );
    else
      gc.setFill( TableCanvas.COLOR_NORMAL_HEADER );
    gc.fillRect( x, 0, w - 1.0, h - 1.0 );

    // grid
    gc.setStroke( TableCanvas.COLOR_GRID );
    gc.strokeLine( x + w - 0.5, 0.5, x + w - 0.5, h - 0.5 );
    gc.strokeLine( x + 0.5, h - 0.5, x + w - 0.5, h - 0.5 );

    // label
    ArrayList<TextLine> lines = getTextLines( text, Alignment.MIDDLE, w, h );
    boolean m_selected = false;
    if ( m_selected )
      gc.setFill( TableCanvas.COLOR_SELECTED_TEXT );
    else
      gc.setFill( TableCanvas.COLOR_NORMAL_TEXT );

    gc.setFontSmoothingType( FontSmoothingType.LCD );
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
    gc.setFill( TableCanvas.COLOR_NORMAL_HEADER );
    gc.fillRect( 0, 0, w - 1.0, h - 1.0 );

    // grid
    gc.setStroke( TableCanvas.COLOR_GRID );
    gc.strokeLine( w - 0.5, 0.5, w - 0.5, h - 0.5 );
    gc.strokeLine( 0.5, h - 0.5, w - 0.5, h - 0.5 );
  }

  /**************************************** getTextLines *****************************************/
  private ArrayList<TextLine> getTextLines( String text, Alignment alignment, double w, double h )
  {
    // initialise variables
    ArrayList<TextLine> lines = new ArrayList<TextLine>();
    double width = w - 2 * CELL_PADDING;
    double height = h - CELL_PADDING;
    double x = CELL_PADDING;
    double y = 0.0;
    Bounds bounds;
    double alignX = 0.0;
    double alignY = 0.0;

    // determine how text needs to be split into lines
    while ( text != null )
    {
      TextLine line = new TextLine();
      bounds = new Text( text ).getLayoutBounds();

      if ( bounds.getWidth() <= width )
      {
        // text width fits in cell
        if ( alignment == Alignment.RIGHT )
          alignX = width - bounds.getWidth();
        if ( alignment == Alignment.MIDDLE )
          alignX = ( width - bounds.getWidth() ) / 2.0;

        alignY = ( height - ( y + bounds.getHeight() ) ) / 2.0 + bounds.getHeight();

        line.txt = text;
        line.x = x + alignX;
        line.y = y;
        text = null;
      }
      else
      {
        // text width exceeds cell width
        boolean isLastLine = y + bounds.getHeight() + bounds.getHeight() > height;
        if ( isLastLine )
        {
          // last line so fit as much as possible with ellipsis on end
          int cut = text.length();
          try
          {
            do
            {
              cut--;
              bounds = new Text( text.substring( 0, cut ) + ELLIPSIS ).getLayoutBounds();
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

          alignY = ( height - ( y + bounds.getHeight() ) ) / 2.0 + bounds.getHeight();

          line.txt = text.substring( 0, cut ) + ELLIPSIS;
          line.x = x + alignX;
          line.y = y;
          line.ellipsis = true;
          text = null;
        }
        else
        {
          // not last line so break at word end
          int cut = text.length();
          try
          {
            do
            {
              cut--;
              bounds = new Text( text.substring( 0, cut ) ).getLayoutBounds();
            }
            while ( bounds.getWidth() > width || !Character.isWhitespace( text.charAt( cut ) ) );

            if ( alignment == Alignment.RIGHT )
              alignX = width - bounds.getWidth();
            if ( alignment == Alignment.MIDDLE )
              alignX = ( width - bounds.getWidth() ) / 2.0;

            line.txt = text.substring( 0, cut );
            line.x = x + alignX;
            line.y = y;

            while ( Character.isWhitespace( text.charAt( cut ) ) )
              cut++;
            text = text.substring( cut );
          }
          catch ( StringIndexOutOfBoundsException wordTooLong )
          {
            // even one word is too long so don't bother looking for word end
            cut = text.length();
            do
            {
              cut--;
              bounds = new Text( text.substring( 0, cut ) + ELLIPSIS ).getLayoutBounds();
            }
            while ( bounds.getWidth() > width );

            if ( alignment == Alignment.RIGHT )
              alignX = width - bounds.getWidth();
            if ( alignment == Alignment.MIDDLE )
              alignX = ( width - bounds.getWidth() ) / 2.0;

            line.txt = text.substring( 0, cut ) + ELLIPSIS;
            line.x = x + alignX;
            line.y = y;
            line.ellipsis = true;

            try
            {
              while ( !Character.isWhitespace( text.charAt( cut ) ) )
                cut++;
              while ( Character.isWhitespace( text.charAt( cut ) ) )
                cut++;
              text = text.substring( cut );
            }
            catch ( StringIndexOutOfBoundsException noWordsRemain )
            {
              alignY = ( height - ( y + bounds.getHeight() ) ) / 2.0 + bounds.getHeight();
              text = null;
            }
          }

        }
      }

      lines.add( line );
      y += bounds.getHeight();
    }

    // move all lines down to vertically centre within cell
    for ( TextLine line : lines )
      line.y += alignY - 2;

    return lines;
  }

  /************************************ createReorderPointer *************************************/
  private Canvas createReorderPointer()
  {
    // create reorder pointer
    Canvas pointer = new Canvas();
    GraphicsContext gc = pointer.getGraphicsContext2D();
    gc.setLineWidth( 3.0 );
    gc.setStroke( Color.RED );

    if ( getCursor() == CURSOR_DOWNARROW )
    {
      double h = Math.min( m_table.getBodyHeight() + m_table.getHorizontalHeaderHeight(), getHeight() );
      pointer.setWidth( 5.0 );
      pointer.setHeight( h );
      gc.strokeLine( 2.5, 0.0, 2.5, h );
    }
    else
    {
      double w = Math.min( m_table.getBodyWidth() + m_table.getVerticalHeaderWidth(), getWidth() );
      pointer.setHeight( 5.0 );
      pointer.setWidth( w );
      gc.strokeLine( 0.0, 2.5, w, 2.5 );
    }

    return pointer;
  }

  /****************************************** mouseMoved *****************************************/
  private void mouseMoved( MouseEvent event )
  {
    // update cursor for potential column/row resizing or re-ordering
    m_x = (int) event.getX();
    m_y = (int) event.getY();
    m_columnPos = m_table.getColumnPositionExactAtX( m_x );
    m_rowPos = m_table.getRowPositionExactAtY( m_y );

    setCursor( CURSOR_CROSS );
    m_index = -1;

    // check for column resize or reorder
    if ( m_y < m_table.getHorizontalHeaderHeight() )
    {
      if ( m_x > m_table.getVerticalHeaderWidth() + PROXIMITY )
      {
        int start = m_table.getXStartByColumnPosition( m_columnPos );

        if ( m_x - start < PROXIMITY )
        {
          setCursor( CURSOR_H_RESIZE );
          m_columnPos--;
          if ( m_columnPos >= m_table.getDataSource().getColumnCount() )
            m_columnPos = m_table.getDataSource().getColumnCount() - 1;
          return;
        }

        int width = m_table.getWidthByColumnPosition( m_columnPos );
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
        int start = m_table.getYStartByRowPosition( m_rowPos );

        if ( m_y - start < PROXIMITY )
        {
          setCursor( CURSOR_V_RESIZE );
          m_rowPos--;
          if ( m_rowPos >= m_table.getDataSource().getRowCount() )
            m_rowPos = m_table.getDataSource().getRowCount() - 1;
          return;
        }

        int height = m_table.getHeightByRowPosition( m_rowPos );
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
  }

  /***************************************** mouseDragged ****************************************/
  private void mouseDragged( MouseEvent event )
  {
    // handle column resize
    if ( getCursor() == CURSOR_H_RESIZE )
    {
      if ( m_index < 0 )
      {
        m_index = m_table.getColumnIndexByPosition( m_columnPos );
        m_x = m_x - m_table.getWidthByColumnPosition( m_columnPos );
      }

      m_table.setWidthByColumnIndex( m_index, ( (int) event.getX() ) - m_x );
      return;
    }

    // handle row resize
    if ( getCursor() == CURSOR_V_RESIZE )
    {
      if ( m_index < 0 )
      {
        m_index = m_table.getRowIndexByPosition( m_rowPos );
        m_y = m_y - m_table.getHeightByRowPosition( m_rowPos );
      }

      m_table.setHeightByRowIndex( m_index, ( (int) event.getY() ) - m_y );
      return;
    }

    // handle column reorder
    if ( getCursor() == CURSOR_DOWNARROW )
    {
      if ( m_index < 0 )
      {
        m_index = m_table.getColumnIndexByPosition( m_columnPos );
        m_x = m_x - m_table.getXStartByColumnPosition( m_columnPos );

        // create reorder slider
        int w = m_table.getWidthByColumnIndex( m_index );
        int h = m_table.getHorizontalHeaderHeight();
        String text = m_table.getDataSource().getColumnTitle( m_index );
        m_reorderSlider = new Canvas( w, h );
        drawColumnHeaderCell( m_reorderSlider.getGraphicsContext2D(), 0, w, h, text, true );
        m_reorderSlider.setOpacity( 0.8 );
        GridPane.setValignment( m_reorderSlider, VPos.TOP );

        // create reorder pointer
        m_reorderPointer = createReorderPointer();
        GridPane.setValignment( m_reorderPointer, VPos.TOP );

        // add slider & pointer to display
        m_table.add( m_reorderSlider, 0, 0 );
        m_table.add( m_reorderPointer, 0, 0, 1, 2 );
      }

      // set slider position
      m_reorderSlider.setTranslateX( event.getX() - m_x );

      // set reorder position
      m_columnPos = m_table.getColumnPositionAtX( (int) event.getX() );
      int x = m_table.getXStartByColumnPosition( m_columnPos );
      int w = m_table.getWidthByColumnPosition( m_columnPos );
      if ( event.getX() > x + w / 2 )
      {
        m_columnPos++;
        x += w;
      }
      m_reorderPointer.setTranslateX( x - m_reorderPointer.getWidth() / 2.0 );

      return;
    }

    // handle row reorder
    if ( getCursor() == CURSOR_RIGHTARROW )
    {
      if ( m_index < 0 )
      {
        m_index = m_table.getRowIndexByPosition( m_rowPos );
        m_y = m_y - m_table.getYStartByRowPosition( m_rowPos );

        // create reorder slider
        int h = m_table.getHeightByRowIndex( m_index );
        int w = m_table.getVerticalHeaderWidth();
        String text = m_table.getDataSource().getRowTitle( m_index );
        m_reorderSlider = new Canvas( w, h );
        drawRowHeaderCell( m_reorderSlider.getGraphicsContext2D(), 0, w, h, text, true );
        m_reorderSlider.setOpacity( 0.8 );
        GridPane.setValignment( m_reorderSlider, VPos.TOP );

        // create reorder pointer
        m_reorderPointer = createReorderPointer();
        GridPane.setValignment( m_reorderPointer, VPos.TOP );

        // add slider & pointer to display
        m_table.add( m_reorderSlider, 0, 0 );
        m_table.add( m_reorderPointer, 0, 0, 2, 1 );
      }

      // set slider position
      m_reorderSlider.setTranslateY( event.getY() - m_y );

      // set reorder position
      m_rowPos = m_table.getRowPositionAtY( (int) event.getY() );
      int y = m_table.getYStartByRowPosition( m_rowPos );
      int h = m_table.getHeightByRowPosition( m_rowPos );
      if ( event.getY() > y + h / 2 )
      {
        m_rowPos++;
        y += h;
      }
      m_reorderPointer.setTranslateY( y - m_reorderPointer.getHeight() / 2.0 );

      return;
    }
  }

  /**************************************** mouseReleased ****************************************/
  private void mouseReleased( MouseEvent event )
  {
    // handle down arrow
    if ( getCursor() == CURSOR_DOWNARROW )
    {
      if ( m_reorderPointer != null )
      {
        // handle column reorder completion
        m_table.getChildren().remove( m_reorderSlider );
        m_table.getChildren().remove( m_reorderPointer );
        m_reorderSlider = null;
        m_reorderPointer = null;

        int oldPos = m_table.getColumnPositionByIndex( m_index );
        if ( m_columnPos < oldPos )
        {
          m_table.moveColumn( oldPos, m_columnPos );
          redrawAll();
          mouseMoved( event );
          return;
        }
        if ( m_columnPos > oldPos + 1 )
        {
          m_table.moveColumn( oldPos, m_columnPos - 1 );
          redrawAll();
          mouseMoved( event );
          return;
        }
      }
      else
      {
        // handle column select

        mouseMoved( event );
        return;
      }
    }

    // handle right arrow
    if ( getCursor() == CURSOR_RIGHTARROW )
    {
      if ( m_reorderPointer != null )
      {
        // handle row reorder completion
        m_table.getChildren().remove( m_reorderSlider );
        m_table.getChildren().remove( m_reorderPointer );
        m_reorderSlider = null;
        m_reorderPointer = null;

        int oldPos = m_table.getRowPositionByIndex( m_index );
        if ( m_rowPos < oldPos )
        {
          m_table.moveRow( oldPos, m_rowPos );
          redrawAll();
          mouseMoved( event );
          return;
        }
        if ( m_rowPos > oldPos + 1 )
        {
          m_table.moveRow( oldPos, m_rowPos - 1 );
          redrawAll();
          mouseMoved( event );
          return;
        }
      }
      else
      {
        // handle row select

        mouseMoved( event );
        return;
      }
    }

    // call mouse moved to ensure cursor is correct etc 
    mouseMoved( event );
  }

}
