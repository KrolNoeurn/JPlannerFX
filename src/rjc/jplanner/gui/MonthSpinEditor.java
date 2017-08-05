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

package rjc.jplanner.gui;

import java.text.DateFormatSymbols;
import java.time.Month;

import javafx.application.Platform;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

/*************************************************************************************************/
/**************************** Spin editor for selecting month-of-year ****************************/
/*************************************************************************************************/

class MonthSpinEditor extends XTextField
{
  private Month      m_month; // month being displayed
  private SpinEditor m_year;  // spin editor showing year

  /**************************************** constructor ******************************************/
  public MonthSpinEditor()
  {
    // set default spin editor characteristics
    setButtonType( ButtonType.UP_DOWN );
    setEditable( false );
    setMonth( Month.JANUARY );

    // react to key presses and button mouse clicks
    setOnKeyPressed( event -> keyPressed( event ) );
    setOnKeyTyped( event -> keyTyped( event ) );
    getButton().setOnMousePressed( event -> buttonPressed( event ) );

    // prevent text being selected when editor gets focus
    focusedProperty().addListener( ( property, oldFocus, newFocus ) -> Platform.runLater( () -> deselect() ) );
  }

  /************************************** setYearSpinEditor **************************************/
  public void setYearSpinEditor( SpinEditor editor )
  {
    // set spin editor for year which this editor will adjust when rolls over year
    m_year = editor;
  }

  /****************************************** setMonth *******************************************/
  public void setMonth( Month month )
  {
    // set month
    m_month = month;
    setText( DateFormatSymbols.getInstance().getMonths()[month.getValue() - 1] );
  }

  /****************************************** setMonth *******************************************/
  public void setMonth( int month )
  {
    // set month from number ISO-8601 value 1 (January) to 12 (December)
    setMonth( Month.of( month ) );
  }

  /****************************************** getMonth *******************************************/
  public Month getMonth()
  {
    // return month
    return m_month;
  }

  /*************************************** getMonthNumber ****************************************/
  public int getMonthNumber()
  {
    // return month number ISO-8601 value 1 (January) to 12 (December)
    return m_month.getValue();
  }

  /******************************************* change ********************************************/
  public void change( int delta )
  {
    // move forward specified number of month, incrementing year spin editor if rolls over year
    if ( delta == 0 )
      return;

    int num = getMonthNumber() + delta;
    if ( num > 12 )
    {
      int yearDelta = ( num - 1 ) / 12;
      if ( m_year != null )
        m_year.setInteger( m_year.getInteger() + yearDelta );
      setMonth( num - 12 * yearDelta );
    }

    if ( num < 1 )
    {
      int yearDelta = ( num - 12 ) / 12;
      if ( m_year != null )
        m_year.setInteger( m_year.getInteger() + yearDelta );
      setMonth( num - 12 * yearDelta );
    }

    if ( num >= 1 && num <= 12 )
      setMonth( num );
  }

  /**************************************** buttonPressed ****************************************/
  private void buttonPressed( MouseEvent event )
  {
    // if user clicked top half of buttons, step up, else step down
    event.consume();
    requestFocus();
    if ( event.getY() < getButton().getHeight() / 2 )
      change( 1 );
    else
      change( -1 );
  }

  /****************************************** keyTyped *******************************************/
  private void keyTyped( KeyEvent event )
  {
    // action key typed to change month
    char letter = event.getCharacter().charAt( 0 );
    int month = m_month.getValue();
    for ( int loop = 1; loop <= 12; loop++ )
    {
      month = ( month == 12 ) ? 1 : month + 1;
      char monthChar = DateFormatSymbols.getInstance().getMonths()[month - 1].charAt( 0 );

      // if typed letter matches first character of month name, change to that month
      if ( Character.toLowerCase( letter ) == Character.toLowerCase( monthChar ) )
      {
        setMonth( month );
        return;
      }
    }

  }

  /***************************************** keyPressed ******************************************/
  private void keyPressed( KeyEvent event )
  {
    // action key press to change value up or down
    switch ( event.getCode() )
    {
      case DOWN:
        change( -1 );
        event.consume();
        break;
      case PAGE_DOWN:
        change( -3 );
        event.consume();
        break;
      case UP:
        change( 1 );
        event.consume();
        break;
      case PAGE_UP:
        change( 3 );
        event.consume();
        break;
      case HOME:
        setMonth( Month.JANUARY );
        event.consume();
        break;
      case END:
        setMonth( Month.DECEMBER );
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
      change( 1 );
    else
      change( -1 );
  }
}
