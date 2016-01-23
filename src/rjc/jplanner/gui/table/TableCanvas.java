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
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import rjc.jplanner.JPlanner;
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

  public static String      ELLIPSIS              = "...";                      // ellipsis to show text has been truncated
  public static int         CELL_PADDING          = 4;                          // cell padding for text left & right edges

  public static final Color COLOR_GRID            = Color.SILVER;
  public static final Color COLOR_DISABLED_CELL   = Color.rgb( 227, 227, 227 ); // medium grey
  public static final Color COLOR_NORMAL_CELL     = Color.WHITE;
  public static final Color COLOR_NORMAL_HEADER   = Color.rgb( 240, 240, 240 ); // light grey
  public static final Color COLOR_NORMAL_TEXT     = Color.BLACK;
  public static final Color COLOR_SELECTED_CELL   = Color.rgb( 51, 153, 255 );  // light blue;
  public static final Color COLOR_SELECTED_HEADER = Color.rgb( 192, 192, 192 ); // medium dark grey
  public static final Color COLOR_SELECTED_TEXT   = Color.WHITE;

  private Table             m_table;

  /***************************************** constructor *****************************************/
  public TableCanvas( Table table )
  {
    // setup table canvas
    super();
    m_table = table;

    // when size changes draw new bits
    widthProperty().addListener( ( observable, oldW, newW ) -> drawWidthChange( (double) oldW, (double) newW ) );
    heightProperty().addListener( ( observable, oldH, newH ) -> drawHeightChange( (double) oldH, (double) newH ) );

    // when mouse moves
    setOnMouseMoved( event -> mouseMoved( event ) );
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

  /*************************************** drawWidthChange ***************************************/
  private void drawWidthChange( double oldW, double newW )
  {
    JPlanner.trace( "WIDTH " + m_table.name + " " + oldW + "->" + newW );

    // draw only if increase in width
    if ( newW <= oldW )
      return;

    // draw only if not beyond edge of table
    if ( oldW > m_table.getBodyWidth() + m_table.getVerticalHeaderWidth() )
      return;

    // check if any columns need to be drawn 
    if ( newW > m_table.getVerticalHeaderWidth() )
    {
      int column1 = m_table.getColumnPositionAtX( (int) oldW );
      int column2 = m_table.getColumnPositionAtX( (int) newW );
      JPlanner.trace( "Columns " + column1 + " to " + column2 );

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

  /************************************** drawHeightChange ***************************************/
  private void drawHeightChange( double oldH, double newH )
  {
    JPlanner.trace( "HEIGHT " + m_table.name + " " + oldH + "->" + newH );

    // draw only if increase in height
    if ( newH <= oldH )
      return;

    // draw only if not below edge of table
    if ( oldH > m_table.getBodyHeight() + m_table.getHorizontalHeaderHeight() )
      return;

    // check if any rows need to be drawn 
    if ( newH > m_table.getHorizontalHeaderHeight() )
    {
      int row1 = m_table.getRowPositionAtY( (int) oldH );
      int row2 = m_table.getRowPositionAtY( (int) newH );
      JPlanner.trace( "Rows " + row1 + " to " + row2 );

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
      drawCell( x, y, w, h, columnPos, rowPos );
      y += h;
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
      drawCell( x, y, w, h, columnPos, rowPos );
      x += w;
    }
  }

  /****************************************** drawCell *******************************************/
  private void drawCell( int x, int y, int w, int h, int columnPos, int rowPos )
  {
    JPlanner.trace( "Cell " + columnPos + "," + rowPos + " @ " + x + "," + y + " size " + w + "," + h );

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
    JPlanner.trace( "Row header " + rowPos );

    // draw row header
    GraphicsContext gc = getGraphicsContext2D();
    int y = m_table.getYStartByRowPosition( rowPos );
    double w = m_table.getVerticalHeaderWidth();
    double h = m_table.getHeightByRowPosition( rowPos );

    // fill
    gc.setFill( TableCanvas.COLOR_NORMAL_HEADER );
    gc.fillRect( 0, y, w - 1.0, h - 1.0 );

    // grid
    gc.setStroke( TableCanvas.COLOR_GRID );
    gc.strokeLine( w - 0.5, y + 0.5, w - 0.5, y + h - 0.5 );
    gc.strokeLine( 0.5, y + h - 0.5, w - 0.5, y + h - 0.5 );

    // label
    int rowIndex = m_table.getRowIndexByPosition( rowPos );
    String text = m_table.getDataSource().getRowTitle( rowIndex );
    ArrayList<TextLine> lines = getTextLines( text, Alignment.MIDDLE, w, h );
    boolean m_selected = false;
    if ( m_selected )
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
    double w = m_table.getWidthByColumnPosition( columnPos );
    double h = m_table.getHorizontalHeaderHeight();

    // fill
    gc.setFill( TableCanvas.COLOR_NORMAL_HEADER );
    gc.fillRect( x, 0, w - 1.0, h - 1.0 );

    // grid
    gc.setStroke( TableCanvas.COLOR_GRID );
    gc.strokeLine( x + w - 0.5, 0.5, x + w - 0.5, h - 0.5 );
    gc.strokeLine( x + 0.5, h - 0.5, x + w - 0.5, h - 0.5 );

    // label
    int columnIndex = m_table.getColumnIndexByPosition( columnPos );
    String text = m_table.getDataSource().getColumnTitle( columnIndex );
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

  /****************************************** mouseMoved *****************************************/
  private void mouseMoved( MouseEvent event )
  {
    int x = (int) event.getX();
    int y = (int) event.getY();
    //JPlanner.trace( "Mouse " + x + "," + y );

    // TODO Auto-generated method stub

  }

}
