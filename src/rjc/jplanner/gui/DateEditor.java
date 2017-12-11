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

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import rjc.jplanner.JPlanner;
import rjc.jplanner.model.Date;

/*************************************************************************************************/
/********************************* Generic date editor control ***********************************/
/*************************************************************************************************/

public class DateEditor extends XTextField
{
  private SimpleIntegerProperty m_epochday; // editor date epoch-day (or most recent valid)

  /**************************************** constructor ******************************************/
  public DateEditor()
  {
    // construct editor
    m_epochday = new SimpleIntegerProperty();
    setButtonType( ButtonType.DOWN );
    new DatePopup( this );

    // react to editor text changes
    textProperty().addListener( ( property, oldText, newText ) ->
    {
      // if text cannot be parsed set editor into error state
      Date date = Date.parse( newText );
      if ( date == null )
        JPlanner.setError( this, "Date not in recognised format" );
      else
      {
        // only set epoch-day if TextField is editable as only then is user typed
        if ( isEditable() )
        {
          // set twice to ensure listeners are triggered even if no epoch-day change
          m_epochday.set( Date.MIN_VALUE.getEpochday() );
          m_epochday.set( date.getEpochday() );
        }
      }
    } );

    // react to epoch-day changes
    m_epochday.addListener(
        ( property, oldNumber, newNumber ) -> JPlanner.setNoError( this, "Date: " + getDate().toFormat() ) );

    // modify date if up or down arrows pressed
    addEventFilter( KeyEvent.KEY_PRESSED, event ->
    {
      if ( event.getCode() == KeyCode.UP )
      {
        event.consume();
        if ( !event.isShiftDown() && !event.isControlDown() )
          setDate( getDate().plusDays( 1 ) );
        if ( event.isShiftDown() && !event.isControlDown() )
          setDate( getDate().plusMonths( 1 ) );
        if ( !event.isShiftDown() && event.isControlDown() )
          setDate( getDate().plusYears( 1 ) );
      }

      if ( event.getCode() == KeyCode.DOWN )
      {
        event.consume();
        if ( !event.isShiftDown() && !event.isControlDown() )
          setDate( getDate().plusDays( -1 ) );
        if ( event.isShiftDown() && !event.isControlDown() )
          setDate( getDate().plusMonths( -1 ) );
        if ( !event.isShiftDown() && event.isControlDown() )
          setDate( getDate().plusYears( -1 ) );
      }
    } );
  }

  /***************************************** addListener *****************************************/
  public void addListener( ChangeListener<? super Number> listener )
  {
    // add listener to epoch-day property
    m_epochday.addListener( listener );
  }

  /****************************************** getDate ********************************************/
  public Date getDate()
  {
    // return editor date (null if invalid)
    return new Date( m_epochday.get() );
  }

  /****************************************** setDate ********************************************/
  public void setDate( Date date )
  {
    // set editor to specified date, this will trigger text listener if change
    setText( date.toFormat() );
    positionCaret( getText().length() );

    // set epoch-day property after setting text, so listeners fired after
    m_epochday.set( date.getEpochday() );
  }

  /**************************************** scrollEvent ******************************************/
  public void scrollEvent( ScrollEvent event )
  {
    // increment or decrement date depending on mouse wheel scroll event
    if ( event.getDeltaY() > 0 )
      setDate( getDate().plusDays( 1 ) );
    else
      setDate( getDate().plusDays( -1 ) );
  }

}
