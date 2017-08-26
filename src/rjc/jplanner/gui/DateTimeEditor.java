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

import javafx.scene.input.ScrollEvent;
import rjc.jplanner.JPlanner;
import rjc.jplanner.model.DateTime;

/*************************************************************************************************/
/******************************* Generic date-time editor control ********************************/
/*************************************************************************************************/

public class DateTimeEditor extends XTextField
{
  private DateTime m_datetime; // editor date-time (or most recent valid)

  /**************************************** constructor ******************************************/
  public DateTimeEditor()
  {
    // construct editor
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
        m_datetime = dt;
        JPlanner.setError( this, null );
        if ( JPlanner.gui != null )
          JPlanner.gui.message( "Date-time: " + dt.toFormat() );
      }
    } );
  }

  /**************************************** getDateTime ******************************************/
  public DateTime getDateTime()
  {
    // return editor date-time
    return m_datetime;
  }

  /**************************************** setDateTime ******************************************/
  public void setDateTime( DateTime dt )
  {
    // set editor to specified date-time
    m_datetime = dt;
    setText( dt.toFormat() );
    positionCaret( getText().length() );
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
