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

package rjc.jplanner.gui.resources;

import rjc.jplanner.JPlanner;
import rjc.jplanner.gui.XTextField;
import rjc.jplanner.gui.table.EditorText;

/*************************************************************************************************/
/**************************** Table cell editor for resource initials ****************************/
/*************************************************************************************************/

class EditorResourceInitials extends EditorText
{

  /**************************************** constructor ******************************************/
  public EditorResourceInitials( int columnIndex, int row )
  {
    // create editor
    super( columnIndex, row );

    // allow any non-whitespace characters except square brackets and comma
    setAllowed( "^[^\\s\\[\\],]*$" );

    // add listener to set error status
    ( (XTextField) getControl() ).textProperty().addListener( ( observable, oldText, newText ) ->
    {
      // length must be between 1 and 20 characters long
      String error = null;
      int len = newText.length();
      if ( len < 1 || len > 20 )
        error = "Initials length not between 1 and 20 characters";

      // initials should be unique
      if ( !JPlanner.plan.resources.isInitialsUnique( newText, row ) )
        error = "Initials not unique";

      // display error message and set editor error status
      JPlanner.gui.setError( getControl(), error );
    } );

    // set initial value to ensure text property listener is triggered is start value is zero length 
    this.setValue( "?" );
  }

}
