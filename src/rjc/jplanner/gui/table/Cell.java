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
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;

/*************************************************************************************************/
/****************** Abstract gui node for displaying table body or header cell *******************/
/*************************************************************************************************/

public abstract class Cell extends Canvas
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

  private static String ELLIPSIS     = "..."; // ellipsis to show text has been truncated
  private static int    CELL_PADDING = 4;     // cell padding for text left & right edges

  public static enum Alignment// alignment of text to be drawn in cell
  {
    LEFT, MIDDLE, RIGHT
  }

  /**************************************** constructor ******************************************/
  public Cell( int w, int h, Paint fill )
  {
    // construct cell drawing area
    super( w, h );

    // fill cell background with specified paint
    GraphicsContext gc = getGraphicsContext2D();
    gc.setFill( fill );
    gc.fillRect( 0, 0, getWidth() - 1.0, getHeight() - 1.0 );
  }

  /***************************************** drawGrid ********************************************/
  public void drawGrid()
  {
    // draw grid lines
    GraphicsContext gc = getGraphicsContext2D();
    gc.setStroke( Table.COLOR_GRID );
    gc.strokeLine( getWidth() - 0.5, 0, getWidth() - 0.5, getHeight() );
    gc.strokeLine( 0, getHeight() - 0.5, getWidth(), getHeight() - 0.5 );
  }

  /***************************************** drawText ********************************************/
  public void drawText( String text, Alignment alignment )
  {
    // break text down into individual lines
    ArrayList<TextLine> lines = getTextLines( text, alignment );

    // draw individual lines
    GraphicsContext gc = getGraphicsContext2D();
    gc.setFontSmoothingType( FontSmoothingType.LCD );
    gc.setFill( Color.BLACK );
    for ( TextLine line : lines )
      gc.fillText( line.txt, line.x, line.y );
  }

  /**************************************** getTextLines *****************************************/
  private ArrayList<TextLine> getTextLines( String text, Alignment alignment )
  {
    // initialise variables
    ArrayList<TextLine> lines = new ArrayList<TextLine>();
    double width = getWidth() - 2 * CELL_PADDING;
    double height = getHeight() - CELL_PADDING;
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
}
