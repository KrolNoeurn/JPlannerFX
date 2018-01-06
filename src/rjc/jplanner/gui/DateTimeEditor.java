/**************************************************************************
 *  Copyright (C) 2018 by Richard Crook                                   *
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

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import rjc.jplanner.JPlanner;
import rjc.jplanner.model.DateTime;

/*************************************************************************************************/
/******************************* Generic date-time editor control ********************************/
/*************************************************************************************************/

public class DateTimeEditor extends XTextField
{
  private SimpleLongProperty m_milliseconds; // editor date-time milliseconds (or most recent valid)

  /**************************************** constructor ******************************************/
  public DateTimeEditor()
  {
    // construct editor
    m_milliseconds = new SimpleLongProperty();
    setButtonType( ButtonType.DOWN );
    new DateTimePopup( this );

    // react to editor text changes
    textProperty().addListener( ( property, oldText, newText ) ->
    {
      // if text cannot be parsed set editor into error state
      DateTime dt = DateTime.parse( newText );
      if ( dt == null )
        JPlanner.setError( this, "Date-time not in recognised format" );
      else
      {
        // only set milliseconds if TextField is editable as only then is user typed
        if ( isEditable() )
        {
          // set twice to ensure listeners are triggered even if no ms change
          m_milliseconds.set( DateTime.MIN_VALUE.getMilliseconds() );
          m_milliseconds.set( dt.getMilliseconds() );
        }
      }
    } );

    // react to milliseconds changes
    m_milliseconds.addListener(
        ( property, oldNumber, newNumber ) -> JPlanner.setNoError( this, "Date-time: " + getDateTime().toFormat() ) );

    // modify date if up or down arrows pressed
    addEventFilter( KeyEvent.KEY_PRESSED, event ->
    {
      if ( event.getCode() == KeyCode.UP )
      {
        event.consume();
        if ( !event.isShiftDown() && !event.isControlDown() )
          setDateTime( getDateTime().plusDays( 1 ) );
        if ( event.isShiftDown() && !event.isControlDown() )
          setDateTime( getDateTime().plusMonths( 1 ) );
        if ( !event.isShiftDown() && event.isControlDown() )
          setDateTime( getDateTime().plusYears( 1 ) );
      }

      if ( event.getCode() == KeyCode.DOWN )
      {
        event.consume();
        if ( !event.isShiftDown() && !event.isControlDown() )
          setDateTime( getDateTime().plusDays( -1 ) );
        if ( event.isShiftDown() && !event.isControlDown() )
          setDateTime( getDateTime().plusMonths( -1 ) );
        if ( !event.isShiftDown() && event.isControlDown() )
          setDateTime( getDateTime().plusYears( -1 ) );
      }

      if ( event.getCode() == KeyCode.PAGE_UP )
      {
        event.consume();
        if ( !event.isShiftDown() && !event.isControlDown() )
          setDateTime( getDateTime().plusMilliseconds( 3600000L ) );
        if ( event.isShiftDown() && !event.isControlDown() )
          setDateTime( getDateTime().plusMilliseconds( 60000L ) );
        if ( !event.isShiftDown() && event.isControlDown() )
          setDateTime( getDateTime().plusMilliseconds( 1000L ) );
      }

      if ( event.getCode() == KeyCode.PAGE_DOWN )
      {
        event.consume();
        if ( !event.isShiftDown() && !event.isControlDown() )
          setDateTime( getDateTime().plusMilliseconds( -3600000L ) );
        if ( event.isShiftDown() && !event.isControlDown() )
          setDateTime( getDateTime().plusMilliseconds( -60000L ) );
        if ( !event.isShiftDown() && event.isControlDown() )
          setDateTime( getDateTime().plusMilliseconds( -1000L ) );
      }

    } );
  }

  /***************************************** addListener *****************************************/
  public void addListener( ChangeListener<? super Number> listener )
  {
    // add listener to milliseconds property
    m_milliseconds.addListener( listener );
  }

  /**************************************** getDateTime ******************************************/
  public DateTime getDateTime()
  {
    // return editor date-time
    return new DateTime( m_milliseconds.get() );
  }

  /**************************************** setDateTime ******************************************/
  public void setDateTime( DateTime dt )
  {
    // set editor to specified date-time, this will trigger text listener if change
    setText( dt.toFormat() );
    positionCaret( getText().length() );

    // set milliseconds property after setting text, so listeners fired after
    m_milliseconds.set( dt.getMilliseconds() );
  }

  /**************************************** scrollEvent ******************************************/
  public void scrollEvent( ScrollEvent event )
  {
    // increment or decrement date depending on mouse wheel scroll event
    if ( event.getDeltaY() > 0 )
      setDateTime( getDateTime().plusDays( 1 ) );
    else
      setDateTime( getDateTime().plusDays( -1 ) );
  }

}
