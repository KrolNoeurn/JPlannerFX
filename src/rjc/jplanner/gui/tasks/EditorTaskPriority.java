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

import rjc.jplanner.gui.SpinEditor;
import rjc.jplanner.gui.table.AbstractCellEditor;

/*************************************************************************************************/
/****************************** Table cell editor for task priority ******************************/
/*************************************************************************************************/

public class EditorTaskPriority extends AbstractCellEditor
{
  SpinEditor m_spin; // spin editor

  /**************************************** constructor ******************************************/
  public EditorTaskPriority( int columnIndex, int row )
  {
    // default spin editor is fine
    super( columnIndex, row );
    m_spin = new SpinEditor();
    setControl( m_spin );
  }

  /******************************************* getValue ******************************************/
  @Override
  public Object getValue()
  {
    // return priority value as integer
    return m_spin.getInteger();
  }

  /******************************************* setValue ******************************************/
  @Override
  public void setValue( Object value )
  {
    // set value depending on type
    if ( value instanceof Integer )
      m_spin.setInteger( (int) value );
    else
      m_spin.setValue( (String) value );
  }

  /***************************************** isValueValid ****************************************/
  @Override
  public boolean isValueValid( Object value )
  {
    // value is valid if null or converts to an integer
    if ( value == null )
      return true;

    try
    {
      Integer.parseInt( (String) value );
      return true;
    }
    catch ( Exception exception )
    {
      return false; // not valid integer
    }
  }
}
