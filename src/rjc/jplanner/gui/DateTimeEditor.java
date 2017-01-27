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

import rjc.jplanner.JPlanner;
import rjc.jplanner.model.DateTime;

/*************************************************************************************************/
/*********************************** Generic date-time editor ************************************/
/*************************************************************************************************/

public class DateTimeEditor extends XTextField
{
  private DateTime         m_datetime;
  private DateTimeSelector m_selector;
  private String           m_validText;

  /**************************************** constructor ******************************************/
  public DateTimeEditor()
  {
    // construct editor
    super();
    setButtonType( ButtonType.DOWN );
    m_selector = new DateTimeSelector( this );

    // react to changes to editor text
    textProperty().addListener( ( property, oldText, newText ) ->
    {
      // only check if gui and plan created
      if ( JPlanner.gui == null || JPlanner.plan == null )
        return;

      // check editor text can be parsed successfully to determine new date-time
      m_datetime = DateTime.parse( newText, JPlanner.plan.getDateTimeFormat() );
      if ( m_datetime == null )
        JPlanner.gui.setError( this, "Format not '" + JPlanner.plan.getDateTimeFormat() + "'" );
      else
        JPlanner.gui.setError( this, null );

      JPlanner.trace( oldText, newText, DateTime.parse( newText, JPlanner.plan.getDateTimeFormat() ) );
    } );

    // react to changes in editor focus state
    focusedProperty().addListener( ( property, oldFocus, newFocus ) ->
    {
      // if editor loses focus and is in error, return text to a valid value
      if ( newFocus == false && MainWindow.isError( this ) )
        setText( m_validText );
    } );

  }

  /**************************************** getDateTime ******************************************/
  public DateTime getDateTime()
  {
    // return editor current date-time (null if invalid)
    return m_datetime;
  }

  /**************************************** setDateTime ******************************************/
  public void setDateTime( DateTime dt )
  {
    // set editor current date-time
    m_validText = dt.toString( JPlanner.plan.getDateTimeFormat() );
    setText( m_validText );
  }

}
