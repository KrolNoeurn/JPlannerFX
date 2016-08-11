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

package rjc.jplanner.gui.tasks;

import rjc.jplanner.gui.table.EditorSpin;

/*************************************************************************************************/
/****************************** Table cell editor for task priority ******************************/
/*************************************************************************************************/

public class EditorTaskPriority extends EditorSpin
{

  /**************************************** constructor ******************************************/
  public EditorTaskPriority( int columnIndex, int rowIndex )
  {
    // default spin editor is fine
    super( columnIndex, rowIndex );
  }

  /******************************************* getValue ******************************************/
  @Override
  public Object getValue()
  {
    // return priority value as integer
    return getInteger();
  }

  /******************************************* setValue ******************************************/
  @Override
  public void setValue( Object value )
  {
    // set value depending on type
    if ( value instanceof Integer )
      setInteger( (int) value );
    else
      super.setValue( value );
  }

}
