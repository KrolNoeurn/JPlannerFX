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

import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import rjc.jplanner.JPlanner;

/*************************************************************************************************/
/***************************** Generic spin editor for number fields *****************************/
/*************************************************************************************************/

public class SpinEditor extends XTextField
{
  private double        m_minValue;          // minimum number allowed
  private double        m_maxValue;          // maximum number allowed
  private int           m_maxFractionDigits; // number of digits after decimal point
  private int           m_caretPos = -1;     // set to >= 0 to position caret *before* setting text

  private double        m_page;              // value increment or decrement on page-up or page-down
  private double        m_step;              // value increment or decrement on arrow-up or arrow-down
  private String        m_prefix;            // prefix shown before value
  private String        m_suffix;            // suffix shown after value

  private DecimalFormat m_numberFormat;      // number decimal format
  private String        m_rangeError;        // error message when value out of range
  private SpinEditor    m_wrapSpinEditor;    // spin editor to provide wrap support

  /**************************************** constructor ******************************************/
  public SpinEditor()
  {
    // set default spin editor characteristics
    setPrefixSuffix( null, null );
    setFormat( "0" );
    setRange( 0.0, 999.0, 0 );
    setStepPage( 1.0, 10.0 );
    setButtonType( ButtonType.UP_DOWN );

    // react to key presses and button mouse clicks
    setOnKeyPressed( event -> keyPressed( event ) );
    getButton().setOnMousePressed( event -> buttonPressed( event ) );

    // use TextFormatter to correctly position caret
    setTextFormatter( new TextFormatter<>( change ->
    {
      // position caret
      if ( m_caretPos >= 0 )
      {
        change.selectRange( m_caretPos, m_caretPos );
        m_caretPos = -1;
      }
      return change;
    } ) );

    // add listener to set control error state and remove any excess leading zeros
    textProperty().addListener( ( observable, oldText, newText ) ->
    {
      // if spinner value not in range, set control into error state
      double num = getDouble();
      if ( JPlanner.gui != null )
        JPlanner.gui.setError( this,
            num < m_minValue || num > m_maxValue || getText().length() < 1 ? m_rangeError : null );
    } );

  }

  /***************************************** replaceText *****************************************/
  @Override
  public void replaceText( int start, int end, String text )
  {
    // determine text before and after replace
    String oldText = getText();
    String newText = oldText.substring( 0, start ) + text + oldText.substring( end );

    // determine start of integer part of number
    int intStart = m_prefix.length();
    if ( newText.length() > intStart && newText.charAt( intStart ) == '-' )
      intStart++;

    // determine end of integer part of number
    int intEnd = newText.length() - m_suffix.length();
    int pointPos = newText.indexOf( '.', intStart );
    if ( pointPos >= 0 && pointPos < intEnd )
      intEnd = pointPos;

    // check if integer part longer than minimum and starts with zero
    if ( intEnd - intStart > m_numberFormat.getMinimumIntegerDigits() && newText.charAt( intStart ) == '0' )
    {
      // determine new start of integer part skipping excess zeros
      int newStart = intStart;
      while ( intEnd - newStart > m_numberFormat.getMinimumIntegerDigits() && newText.charAt( newStart ) == '0' )
        newStart++;

      // replace old text with new text with excess zeros removed
      newText = newText.substring( 0, intStart ) + newText.substring( newStart, newText.length() );
      m_caretPos = intStart;
      super.replaceText( 0, oldText.length(), newText );
      return;
    }

    // proceed with normal replace (no excess zeros)
    super.replaceText( start, end, text );
  }

  /****************************************** setValue *******************************************/
  public void setValue( String text )
  {
    // set editor text adding prefix and suffix
    m_caretPos = m_prefix.length() + text.length();
    setText( m_prefix + text + m_suffix );
  }

  /****************************************** getValue *******************************************/
  public String getValue()
  {
    // return editor text without prefix + suffix
    return getText().substring( m_prefix.length(), getText().length() - m_suffix.length() );
  }

  /****************************************** setDouble ******************************************/
  public void setDouble( double value )
  {
    // set editor text (adding prefix and suffix)
    setValue( m_numberFormat.format( value ) );
  }

  /****************************************** getDouble ******************************************/
  public double getDouble()
  {
    // return editor text (less prefix + suffix) converted to double number
    try
    {
      return Double.parseDouble( getValue() );
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
    setValue( m_numberFormat.format( value ) );
  }

  /***************************************** getInteger ******************************************/
  public int getInteger()
  {
    // return editor text (less prefix + suffix) converted to integer number
    try
    {
      return Integer.parseInt( getValue() );
    }
    catch ( Exception exception )
    {
      return 0;
    }
  }

  /****************************************** setFormat ******************************************/
  public void setFormat( String format )
  {
    // set number format
    m_numberFormat = new DecimalFormat( format );
  }

  /******************************************* setRange ******************************************/
  public void setRange( double minValue, double maxValue, int maxFractionDigits )
  {
    // check inputs
    if ( minValue > maxValue )
      throw new IllegalArgumentException( "Min greater than max! " + minValue + " " + maxValue );
    if ( maxFractionDigits < 0 || maxFractionDigits > 8 )
      throw new IllegalArgumentException( "Digits after deciminal place out of 0-8 range! " + maxFractionDigits );

    // set range and number of digits after decimal point
    m_minValue = minValue;
    m_maxValue = maxValue;
    m_maxFractionDigits = maxFractionDigits;

    // sets the max number of digits after decimal point when displayed as text
    m_numberFormat.setMaximumFractionDigits( maxFractionDigits );
    determineAllowed();
    m_rangeError = "Value not between " + m_numberFormat.format( m_minValue ) + " and "
        + m_numberFormat.format( m_maxValue );
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
    // set prefix and suffix, translating null to ""
    m_prefix = ( prefix == null ? "" : prefix );
    m_suffix = ( suffix == null ? "" : suffix );
    determineAllowed();
  }

  /****************************************** getPrefix ******************************************/
  public String getPrefix()
  {
    // return prefix
    return m_prefix;
  }

  /****************************************** getSuffix ******************************************/
  public String getSuffix()
  {
    // return suffix
    return m_suffix;
  }

  /************************************** determineAllowed ***************************************/
  private void determineAllowed()
  {
    // determine regular expression defining text allowed to be entered
    StringBuilder allow = new StringBuilder( 32 );
    allow.append( Pattern.quote( m_prefix ) );

    if ( m_minValue < 0.0 )
      allow.append( "-?" );
    allow.append( "\\d*" );
    if ( m_maxFractionDigits > 0 )
      allow.append( "\\.?\\d{0," + m_maxFractionDigits + "}" );

    allow.append( Pattern.quote( m_suffix ) );
    super.setAllowed( allow.toString() );
  }

  /**************************************** buttonPressed ****************************************/
  private void buttonPressed( MouseEvent event )
  {
    // if user clicked top half of buttons, step up, else step down
    if ( event.getY() < getButton().getHeight() / 2 )
      changeNumber( m_step );
    else
      changeNumber( -m_step );

    event.consume();
    m_caretPos = getText().length() - m_suffix.length();
    requestFocus();
  }

  /**************************************** changeNumber *****************************************/
  private void changeNumber( double delta )
  {
    // modify number, ensuring it is between min and max
    double num = getDouble() + delta;

    if ( m_wrapSpinEditor == null )
    {
      // no wrap editor so simply limit to min & max values
      if ( num < m_minValue )
        num = m_minValue;
      if ( num > m_maxValue )
        num = m_maxValue;
      setDouble( num );
    }
    else
    {
      // wrap editor exists
      if ( num < m_minValue )
      {
        m_wrapSpinEditor.changeNumber( -1 );
        num = m_maxValue - ( m_minValue - num - 1 );
      }
      if ( num > m_maxValue )
      {
        m_wrapSpinEditor.changeNumber( 1 );
        num = m_minValue + ( num - m_maxValue - 1 );
      }
      setDouble( num );
    }
  }

  /***************************************** keyPressed ******************************************/
  private void keyPressed( KeyEvent event )
  {
    // action key press to change value up or down
    switch ( event.getCode() )
    {
      case DOWN:
        changeNumber( -m_step );
        event.consume();
        break;
      case PAGE_DOWN:
        changeNumber( -m_page );
        event.consume();
        break;
      case UP:
        changeNumber( m_step );
        event.consume();
        break;
      case PAGE_UP:
        changeNumber( m_page );
        event.consume();
        break;
      case HOME:
        setDouble( m_minValue );
        event.consume();
        break;
      case END:
        setDouble( m_maxValue );
        event.consume();
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

  /************************************** setWrapSpinEditor **************************************/
  public void setWrapSpinEditor( SpinEditor wrap )
  {
    // set spin editor to increment or decrement on this spin trying to go beyond min or max
    m_wrapSpinEditor = wrap;
  }

}
