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

import java.text.DateFormatSymbols;
import java.time.Month;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import rjc.jplanner.JPlanner;

/*************************************************************************************************/
/**************************** Spin editor for selecting month-of-year ****************************/
/*************************************************************************************************/

public class MonthSpinEditor extends XTextField
{
  private ReadOnlyIntegerWrapper m_month; // read only wrapper for editor month number
  private SpinEditor             m_year;  // spin editor showing year

  /**************************************** constructor ******************************************/
  public MonthSpinEditor()
  {
    // set default spin editor characteristics
    m_month = new ReadOnlyIntegerWrapper();
    setButtonType( ButtonType.UP_DOWN );
    setEditable( false );

    // react to key presses and button mouse clicks
    setOnKeyPressed( event -> keyPressed( event ) );
    getButton().setOnMousePressed( event -> buttonPressed( event ) );

    // use TextFormatter to correctly position caret
    setTextFormatter( new TextFormatter<>( change ->
    {
      JPlanner.trace( change );
      return change;
    } ) );

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
    setMonth( month.getValue() );
  }

  /****************************************** setMonth *******************************************/
  private void setMonth( int month )
  {
    // set month from number ISO-8601 value 1 (January) to 12 (December)
    m_month.set( month );
    setText( DateFormatSymbols.getInstance().getMonths()[month - 1] );
  }

  /****************************************** getMonth *******************************************/
  public Month getMonth()
  {
    // return month
    return Month.of( m_month.get() );
  }

  /*************************************** getMonthNumber ****************************************/
  public int getMonthNumber()
  {
    // return month number ISO-8601 value 1 (January) to 12 (December)
    return m_month.get();
  }

  /************************************** getMonthProperty ***************************************/
  public ReadOnlyIntegerProperty getMonthProperty()
  {
    // return read-only property for editor month number
    return m_month.getReadOnlyProperty();
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
      setMonth( num - 12 * yearDelta );
      if ( m_year != null )
        m_year.setInteger( m_year.getInteger() + yearDelta );
    }

    if ( num < 1 )
    {
      int yearDelta = ( num - 12 ) / 12;
      setMonth( num - 12 * yearDelta );
      if ( m_year != null )
        m_year.setInteger( m_year.getInteger() + yearDelta );
    }

    if ( num >= 1 && num <= 12 )
      setMonth( num );
  }

  /**************************************** buttonPressed ****************************************/
  private void buttonPressed( MouseEvent event )
  {
    // if user clicked top half of buttons, step up, else step down
    if ( event.getY() < getButton().getHeight() / 2 )
      change( 1 );
    else
      change( -1 );

    event.consume();
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
