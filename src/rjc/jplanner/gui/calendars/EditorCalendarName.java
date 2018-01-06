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

package rjc.jplanner.gui.calendars;

import rjc.jplanner.JPlanner;
import rjc.jplanner.gui.table.EditorText;

/*************************************************************************************************/
/****************************** Table cell editor for calendar name ******************************/
/*************************************************************************************************/

class EditorCalendarName extends EditorText
{

  /**************************************** constructor ******************************************/
  public EditorCalendarName( int columnIndex, int row )
  {
    // create editor
    super( columnIndex, row );

    // add listener to set error status
    addListener( ( observable, oldText, newText ) ->
    {
      // length must be between 1 and 40 characters long
      String error = null;
      String tidy = JPlanner.clean( newText ).trim();
      int len = tidy.length();
      if ( len < 1 || len > 40 )
        error = "Name length not between 1 and 40 characters";

      // name should be unique
      if ( JPlanner.plan.calendars.isDuplicateName( tidy, columnIndex ) )
        error = "Name not unique";

      // display error message and set editor error status
      if ( error == null )
        JPlanner.setNoError( getControl(), "" );
      else
        JPlanner.setError( getControl(), error );
    } );

  }

  /******************************************* getValue ******************************************/
  @Override
  public Object getValue()
  {
    // return editor text cleaned and trimmed
    return JPlanner.clean( (String) super.getValue() ).trim();
  }

}
