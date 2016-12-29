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

import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import rjc.jplanner.gui.Colors;
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

  public static final String ELLIPSIS          = "..."; // ellipsis to show text has been truncated
  public static final int    CELL_PADDING      = 4;     // cell padding for text left & right edges
  protected static final int INDENT            = 14;    // cell left padding per indent level 

  protected Table            m_table;                   // table displaying this table canvas
  protected boolean          m_recentlyRedrawn = false; // set true if has table be totally redrawn recently

  /***************************************** constructor *****************************************/
  public TableCanvas( Table table )
  {
    // setup table canvas
    super();
    m_table = table;

    // when size changes draw new bits
    widthProperty().addListener( ( observable, oldW, newW ) -> drawWidth( oldW.intValue(), newW.intValue() ) );
    heightProperty().addListener( ( observable, oldH, newH ) -> drawHeight( oldH.intValue(), newH.intValue() ) );
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
    Paint color = m_table.getData().getCellBackground( columnIndex, row );
    if ( m_selected )
      gc.setFill( Colors.SELECTED_CELL );
    else
      gc.setFill( color );
    gc.fillRect( x, y, w - 1, h - 1 );

    // grid
    gc.setStroke( Colors.TABLE_GRID );
    gc.strokeLine( x + w - 0.5, y + 0.5, x + w - 0.5, y + h - 0.5 );
    gc.strokeLine( x + 0.5, y + h - 0.5, x + w - 0.5, y + h - 0.5 );

    // text
    String text = m_table.getData().getCellText( columnIndex, row );
    if ( text != null )
    {
      Font font = m_table.getData().getCellFont( columnIndex, row );
      gc.setFont( font );

      // text alignment and colour
      int indent = m_table.getData().getCellIndent( columnIndex, row );
      Alignment alignment = m_table.getData().getCellAlignment( columnIndex, row );
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
    String text = m_table.getData().getRowTitle( rowIndex );
    boolean selected = m_table.doesRowHaveSelection( rowIndex );

    drawRowHeaderCell( gc, y, w, h, text, selected );
  }

  /************************************** drawRowHeaderCell **************************************/
  protected void drawRowHeaderCell( GraphicsContext gc, int y, double w, double h, String text, boolean selected )
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
    String text = m_table.getData().getColumnTitle( columnIndex );
    boolean selected = m_table.doesColumnHaveSelection( columnPos );

    drawColumnHeaderCell( gc, x, w, h, text, selected );
  }

  /************************************ drawColumnHeaderCell *************************************/
  protected void drawColumnHeaderCell( GraphicsContext gc, int x, int w, int h, String text, boolean selected )
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

  /******************************** getExpandHideMarkerRectangle ********************************/
  private Rectangle2D getExpandHideMarkerRectangle( int columnIndex, int row, int indent, int x, int y, int h )
  {
    // return expand-hide marker rectangle, or null if none
    if ( indent > 0 && m_table.getData().isCellSummary( columnIndex, row ) )
      return new Rectangle2D( x - 8.5, y + h / 2 - 4.5, 9.0, 9.0 );

    return null;
  }

}
