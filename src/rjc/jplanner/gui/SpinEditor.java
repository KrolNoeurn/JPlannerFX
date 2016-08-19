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

package rjc.jplanner.gui;

import java.text.DecimalFormat;
import java.util.regex.Pattern;

import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

/*************************************************************************************************/
/***************************** Generic spin editor for number fields *****************************/
/*************************************************************************************************/

public class SpinEditor extends XTextField
{
  private Canvas           m_buttons         = new Canvas();            // buttons part of editor

  private double           m_min;                                       // minimum number allowed
  private double           m_max;                                       // maximum number allowed
  private int              m_dp;                                        // number of digits after decimal point

  private double           m_page;
  private double           m_step;
  private String           m_prefix;
  private String           m_suffix;

  private static final int BUTTONS_WIDTH_MAX = 16;
  private static final int BUTTONS_PADDING   = 2;
  private DecimalFormat    m_numberFormat    = new DecimalFormat( "0" );

  /**************************************** constructor ******************************************/
  public SpinEditor()
  {
    // set default spin editor characteristics
    setRange( 0.0, 999.0, 0 );
    setStepPage( 1.0, 10.0 );

    // when editor changes size re-draw buttons
    heightProperty().addListener( ( property, oldHeight, newHeight ) -> drawButtons() );
    widthProperty().addListener( ( property, oldWidth, newWidth ) -> drawButtons() );

    // react to key presses and button mouse clicks
    setOnKeyPressed( event -> keyPressed( event ) );
    m_buttons.setOnMousePressed( event -> buttonPressed( event ) );

    // add listener to ensure error status is correct
    textProperty().addListener( ( observable, oldText, newText ) ->
    {
      double num = getDouble();
      MainWindow.setError( num < m_min || num > m_max || getText().length() < 1, this );

      // remove any excess zero at start of number
      String str = getTextCore();
      if ( str.length() > 1 && str.charAt( 0 ) == '0' && str.charAt( 1 ) != '.' )
        setText( newText.substring( 1 ) );
      if ( str.length() > 2 && str.charAt( 0 ) == '-' && str.charAt( 1 ) == '0' && str.charAt( 2 ) != '.' )
        setText( "-" + newText.substring( 2 ) );
    } );
  }

  /***************************************** getTextCore *****************************************/
  public String getTextCore()
  {
    // return editor text less prefix + suffix
    String text = getText();
    if ( m_prefix != null )
      text = text.substring( m_prefix.length() );
    if ( m_suffix != null )
      text = text.substring( 0, text.length() - m_suffix.length() );

    return text;
  }

  /***************************************** setTextCore *****************************************/
  public void setTextCore( String text )
  {
    // set editor text adding prefix and suffix
    if ( m_prefix != null && !text.startsWith( m_prefix ) )
      text = m_prefix + text;
    if ( m_suffix != null && !text.endsWith( m_suffix ) )
      text = text + m_suffix;

    // set caret position before suffix 
    int caretPos = text.length();
    if ( m_suffix != null )
      caretPos -= m_suffix.length();

    setTextCaret( text, caretPos );
  }

  /****************************************** setDouble ******************************************/
  public void setDouble( double value )
  {
    // set editor text (adding prefix and suffix)
    setTextCore( m_numberFormat.format( value ) );
  }

  /****************************************** getDouble ******************************************/
  public double getDouble()
  {
    // return editor text (less prefix + suffix) converted to double number
    try
    {
      return Double.parseDouble( getTextCore() );
    }
    catch ( Exception exception )
    {
      return 0.0;
    }
  }

  /***************************************** setInteger ******************************************/
  public void setInteger( int value )
  {
    // set editor text (adding prefix and suffix)
    setTextCore( String.valueOf( value ) );
  }

  /***************************************** getInteger ******************************************/
  public int getInteger()
  {
    // return editor text (less prefix + suffix) converted to integer number
    try
    {
      return Integer.parseInt( getTextCore() );
    }
    catch ( Exception exception )
    {
      return 0;
    }
  }

  /******************************************* setRange ******************************************/
  public void setRange( double min, double max, int dp )
  {
    // check inputs
    if ( min > max )
      throw new IllegalArgumentException( "Min greater than max! " + min + " " + max );
    if ( dp < 0 || dp > 8 )
      throw new IllegalArgumentException( "Digits after deciminal place out of 0-8 range! " + dp );

    // set range and number of digits after decimal point
    m_min = min;
    m_max = max;
    m_dp = dp;

    // sets the max number of digits after decimal point when displayed as text
    m_numberFormat.setMaximumFractionDigits( dp );
    setAllowed();
  }

  /***************************************** setStepPage *****************************************/
  public void setStepPage( double step, double page )
  {
    // set step and page increment/decrement sizes
    m_step = step;
    m_page = page;
  }

  /*************************************** setPrefixSuffix ***************************************/
  public void setPrefixSuffix( String prefix, String suffix )
  {
    // set prefix and suffix
    m_prefix = prefix;
    m_suffix = suffix;
    setAllowed();
  }

  /***************************************** setAllowed ******************************************/
  private void setAllowed()
  {
    // determine regular expression defining text allowed to be entered
    StringBuilder allow = new StringBuilder( 32 );

    if ( m_prefix != null )
      allow.append( Pattern.quote( m_prefix ) );

    if ( m_min < 0.0 )
      allow.append( "-?" );
    allow.append( "\\d*" );
    if ( m_dp > 0 )
      allow.append( "\\.?\\d{0," + m_dp + "}" );

    if ( m_suffix != null )
      allow.append( Pattern.quote( m_suffix ) );

    setAllowed( allow.toString() );
  }

  /***************************************** getButtons ******************************************/
  public Canvas getButtons()
  {
    // return buttons
    return m_buttons;
  }

  /***************************************** drawButtons *****************************************/
  private void drawButtons()
  {
    // determine size and draw button
    GraphicsContext gc = m_buttons.getGraphicsContext2D();
    double h = getHeight() - 2 * BUTTONS_PADDING;
    double w = getWidth() / 2;
    if ( w > BUTTONS_WIDTH_MAX )
      w = BUTTONS_WIDTH_MAX;
    m_buttons.setHeight( h );
    m_buttons.setWidth( w );

    // set editor insets and buttons position
    setPadding( new Insets( 0, w + PADDING, 0, PADDING ) );
    m_buttons.setLayoutX( getLayoutX() + getWidth() - w - BUTTONS_PADDING );
    m_buttons.setLayoutY( getLayoutY() + BUTTONS_PADDING );

    // fill background
    gc.setFill( MainWindow.BUTTON_BACKGROUND );
    gc.fillRect( 0.0, 0.0, w, h );

    // draw arrows
    gc.setStroke( MainWindow.BUTTON_ARROW );
    int x1 = (int) ( w * 0.2 + 0.5 );
    int y1 = (int) ( h * 0.1 + 0.6 );
    int y2 = (int) ( h * 0.5 - y1 );
    for ( int y = y1; y <= y2; y++ )
    {
      double x = x1 + ( w * 0.5 - x1 ) / ( y2 - y1 ) * ( y2 - y );
      gc.strokeLine( x, y + .5, w - x, y + .5 );
      gc.strokeLine( x, h - ( y + .5 ), w - x, h - ( y + .5 ) );
    }
  }

  /**************************************** mousePressed ****************************************/
  private void buttonPressed( MouseEvent event )
  {
    // if user clicked top half of buttons, step up, else step down
    if ( event.getY() < m_buttons.getHeight() / 2 )
      changeNumber( m_step );
    else
      changeNumber( -m_step );
  }

  /**************************************** changeNumber *****************************************/
  private void changeNumber( double delta )
  {
    // modify number, ensuring it is between min and max
    double num = getDouble() + delta;
    if ( num < m_min )
      num = m_min;
    if ( num > m_max )
      num = m_max;
    setDouble( num );
  }

  /***************************************** keyPressed ******************************************/
  private void keyPressed( KeyEvent event )
  {
    // action key press to change value up or down
    switch ( event.getCode() )
    {
      case DOWN:
        changeNumber( -m_step );
        break;
      case PAGE_DOWN:
        changeNumber( -m_page );
        break;
      case UP:
        changeNumber( m_step );
        break;
      case PAGE_UP:
        changeNumber( m_page );
        break;
      case HOME:
        setDouble( m_min );
        break;
      case END:
        setDouble( m_max );
        break;
      default:
        break;
    }
  }

  /***************************************** scrollEvent *****************************************/
  public void scrollEvent( ScrollEvent event )
  {
    // increment or decrement value depending on mouse wheel scroll event
    if ( event.getDeltaY() > 0 )
      changeNumber( m_step );
    else
      changeNumber( -m_step );
  }

}
