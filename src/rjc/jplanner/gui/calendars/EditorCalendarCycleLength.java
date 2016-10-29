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

package rjc.jplanner.gui.calendars;

import rjc.jplanner.gui.SpinEditor;
import rjc.jplanner.gui.table.AbstractCellEditor;

/*************************************************************************************************/
/************************** Table cell editor for calendar cycle length **************************/
/*************************************************************************************************/

public class EditorCalendarCycleLength extends AbstractCellEditor
{
  SpinEditor m_spin; // spin editor

  /**************************************** constructor ******************************************/
  public EditorCalendarCycleLength( int columnIndex, int rowIndex )
  {
    // use spin editor
    super( columnIndex, rowIndex );
    m_spin = new SpinEditor();

    m_spin.setRange( 1, 99, 0 );
    m_spin.setStepPage( 1, 1 );
    setControl( m_spin );
  }

  /******************************************* getValue ******************************************/
  @Override
  public Object getValue()
  {
    // return cycle length value as integer
    return m_spin.getInteger();
  }

  /******************************************* setValue ******************************************/
  @Override
  public void setValue( Object value )
  {
    // set value depending on type
    if ( value instanceof Integer )
      m_spin.setInteger( (int) value );
    else if ( value instanceof String )
      m_spin.setInteger( Integer.parseInt( (String) value ) );
    else
      throw new IllegalArgumentException( "Unhandled " + value.getClass().getName() );
  }

  /****************************************** validValue *****************************************/
  @Override
  public boolean validValue( Object value )
  {
    // value is valid if null or converts to an integer greater than zero
    if ( value == null )
      return true;

    try
    {
      int num = Integer.parseInt( (String) value );
      if ( num > 0 )
        return true; // greater than zero
    }
    catch ( Exception exception )
    {
      return false; // not valid integer
    }

    return false; // not greater than zero
  }
}
