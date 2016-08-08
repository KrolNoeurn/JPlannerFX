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

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import rjc.jplanner.gui.table.CellTextField;

/*************************************************************************************************/
/****************************** Table cell editor for number fields ******************************/
/*************************************************************************************************/

public class SpinEditor
{
  private StackPane                      m_stack;                                     // overall editor that includes buttons and text field
  private CellTextField                  m_editor;                                    // text field part of editor
  private Canvas                         m_buttons;                                   // buttons part of editor

  private double                         m_min;                                       // minimum number allowed
  private double                         m_max;                                       // maximum number allowed
  private int                            m_dp;                                        // number of digits after decimal point

  private double                         m_page;
  private double                         m_step;
  private String                         m_prefix;
  private String                         m_suffix;

  private static final int               MAX_BUTTONS_WIDTH = 24;
  private DecimalFormat                  m_numberFormat    = new DecimalFormat( "0" );
  private EventHandler<? super KeyEvent> m_cellKeyHandler;

  /**************************************** constructor ******************************************/
  public SpinEditor( int columnIndex, int rowIndex )
  {
    // create spin editor, stack of text editor and buttons
    m_stack = new StackPane();
    m_editor = new CellTextField();
    m_buttons = new Canvas();
    m_stack.getChildren().addAll( m_editor, m_buttons );
    StackPane.setAlignment( m_buttons, Pos.CENTER_RIGHT );

    // set default spin editor characteristics
    setRange( 50.0, 999.0, 0 );
    setStepPage( 1.0, 10.0 );

    // when stack pane changes size re-draw buttons
    m_stack.heightProperty().addListener( ( property, oldHeight, newHeight ) -> drawButtons() );
    m_stack.widthProperty().addListener( ( property, oldWidth, newWidth ) -> drawButtons() );

    // react to key presses and mouse clicks & wheel scrolls
    m_cellKeyHandler = m_editor.getOnKeyPressed();
    m_editor.setOnKeyPressed( event -> keyPressed( event ) );
    m_buttons.setOnMousePressed( event -> buttonPressed( event ) );

    // lock x-layout to 0 to prevent any shudder or incorrect placing
    m_editor.layoutXProperty().addListener( ( observable, oldValue, newValue ) -> m_editor.setLayoutX( 0.0 ) );
  }

  /******************************************* getText *******************************************/
  public String getText()
  {
    // get editor text (including prefix and suffix)
    return m_editor.getText();
  }

  /******************************************* setValue ******************************************/
  public void setValue( Object value )
  {
    // ensure new value starts with prefix and ends with suffix
    String str = (String) value;
    if ( m_prefix != null && !str.startsWith( m_prefix ) )
      str = m_prefix + str;
    if ( m_suffix != null && !str.endsWith( m_suffix ) )
      str = str + m_suffix;

    // set editor text
    m_editor.setValue( str );
  }

  /******************************************* setValue ******************************************/
  public void setValue( double value )
  {
    // set editor text
    m_editor.setValue( m_prefix + m_numberFormat.format( value ) + m_suffix );
  }

  /****************************************** getNumber ******************************************/
  public double getNumber()
  {
    // get editor text (minus prefix and suffix) converted to number
    String value = getText();
    if ( m_prefix != null )
      value = value.substring( m_prefix.length() );
    if ( m_suffix != null )
      value = value.substring( 0, value.length() - m_suffix.length() );

    try
    {
      return Double.parseDouble( value );
    }
    catch ( Exception exception )
    {
      return 0.0;
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

    m_editor.setAllowed( allow.toString() );
  }

  /***************************************** drawButtons *****************************************/
  private void drawButtons()
  {
    // determine size and draw button
    GraphicsContext gc = m_buttons.getGraphicsContext2D();
    double h = m_stack.getHeight() - 4;
    double w = m_stack.getWidth() / 2;
    if ( w > MAX_BUTTONS_WIDTH )
      w = MAX_BUTTONS_WIDTH;
    m_buttons.setHeight( h );
    m_buttons.setWidth( w );

    // fill background
    gc.clearRect( 0.0, 0.0, w, h );
    gc.setFill( MainWindow.BUTTON_BACKGROUND );
    w -= 2;
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
    double num = getNumber() + delta;
    if ( num < m_min )
      num = m_min;
    if ( num > m_max )
      num = m_max;
    setValue( num );
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
        setValue( m_min );
        break;
      case END:
        setValue( m_max );
        break;
      default:
        // call CellEditor key pressed event handler (handles escape + return etc)
        m_cellKeyHandler.handle( event );
    }
  }

  /***************************************** keyPressed ******************************************/
  public void scrollEvent( ScrollEvent event )
  {
    // increment or decrement value depending on scroll event
    if ( event.getDeltaY() > 0 )
      changeNumber( m_step );
    else
      changeNumber( -m_step );
  }

}
