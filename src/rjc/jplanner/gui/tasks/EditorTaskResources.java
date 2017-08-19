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

package rjc.jplanner.gui.tasks;

import rjc.jplanner.JPlanner;
import rjc.jplanner.gui.table.EditorText;
import rjc.jplanner.model.TaskResources;

/*************************************************************************************************/
/***************************** Table cell editor for task resources ******************************/
/*************************************************************************************************/

public class EditorTaskResources extends EditorText
{

  /**************************************** constructor ******************************************/
  public EditorTaskResources( int columnIndex, int row )
  {
    // create editor
    super( columnIndex, row );

    // add listener to set error status
    addListener( ( observable, oldText, newText ) ->
    {
      // display error message and set editor error status
      String error = TaskResources.errors( newText );
      JPlanner.gui.setError( getControl(), error );
    } );

  }

  /******************************************* getValue ******************************************/
  @Override
  public Object getValue()
  {
    // return text as a Predecessors
    return new TaskResources( (String) super.getValue() );
  }

}
