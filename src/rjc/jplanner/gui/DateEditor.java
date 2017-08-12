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
  private Date   m_date;      // editor date, or null if editor date invalid
  private String m_validText; // last valid editor text used if focus lost when in error

  /**************************************** constructor ******************************************/
  public DateEditor()
  {
    // construct editor
    setButtonType( ButtonType.DOWN );

    // react to editor text changes
    textProperty().addListener( ( property, oldText, newText ) ->
    {
      // only check if gui and plan created
      if ( JPlanner.gui == null || JPlanner.plan == null )
        return;

      // if text cannot be parsed set editor into error state
      m_date = Date.parse( newText );
      if ( m_date == null )
        JPlanner.gui.setError( this, "Date not in recognised format" );
      else
      {
        JPlanner.gui.setError( this, null );
        JPlanner.gui.message( "Date: " + m_date.toString( JPlanner.plan.getDateFormat() ) );
        m_validText = newText;
      }
    } );

    // react to changes in editor focus state
    focusedProperty().addListener( ( property, oldFocus, newFocus ) ->
    {
      // if editor loses focus and is in error, return text to a valid value
      if ( newFocus == false && MainWindow.isError( this ) )
        setText( m_validText );
    } );

    new DatePopup( this );
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
    setText( date.toString( JPlanner.plan.getDateFormat() ) );
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
