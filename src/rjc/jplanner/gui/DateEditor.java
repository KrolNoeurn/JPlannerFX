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
import rjc.jplanner.model.Date;

/*************************************************************************************************/
/********************************* Generic date editor control ***********************************/
/*************************************************************************************************/

public class DateEditor extends XTextField
{
  private Date m_date; // editor date (or most recent valid)

  /**************************************** constructor ******************************************/
  public DateEditor()
  {
    // construct editor
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
        m_date = date;
        JPlanner.setError( this, null );
        if ( JPlanner.gui != null )
          JPlanner.gui.message( "Date: " + date.toFormat() );
      }
    } );
  }

  /****************************************** getDate ********************************************/
  public Date getDate()
  {
    // return editor date (null if invalid)
    return m_date;
  }

  /****************************************** setDate ********************************************/
  public void setDate( Date date )
  {
    // set editor to specified date
    m_date = date;
    setText( date.toFormat() );
    positionCaret( getText().length() );
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
