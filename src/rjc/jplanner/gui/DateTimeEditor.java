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
  private DateTime m_datetime;

  /**************************************** constructor ******************************************/
  public DateTimeEditor()
  {
    // construct editor
    super();
    setButtonType( ButtonType.DOWN );

    // when button pressed open date-time selector
    getButton().setOnMousePressed( event -> new DateTimeSelector( this ) );

    // react to changes to editor text
    textProperty().addListener( ( property, oldText, newText ) ->
    {
      JPlanner.trace( "'" + oldText + "' -> '" + newText + "'" );
      // TODO
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
    m_datetime = dt;
    setText( dt.toString( JPlanner.plan.datetimeFormat() ) );
  }

}
