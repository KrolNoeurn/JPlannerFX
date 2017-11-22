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

import rjc.jplanner.gui.SpinEditor;
import rjc.jplanner.gui.table.AbstractCellEditor;

/*************************************************************************************************/
/************************** Table cell editor for resource availability **************************/
/*************************************************************************************************/

class EditorResourceAvailable extends AbstractCellEditor
{
  SpinEditor m_spin; // spin editor

  /**************************************** constructor ******************************************/
  public EditorResourceAvailable( int columnIndex, int row )
  {
    // use spin editor
    super( columnIndex, row );
    m_spin = new SpinEditor();
    m_spin.setFormat( "0" );
    m_spin.setRange( 0.0, 99999.99, 2 );
    m_spin.setStepPage( 1.0, 10.0 );
    setControl( m_spin );
  }

  /******************************************* getValue ******************************************/
  @Override
  public Object getValue()
  {
    // return work value as double
    return m_spin.getDouble();
  }

  /******************************************* setValue ******************************************/
  @Override
  public void setValue( Object value )
  {
    // set value depending on type
    if ( value instanceof Double )
      m_spin.setDouble( (double) value );
    else
      m_spin.setValue( (String) value );
  }

  /***************************************** isValueValid ****************************************/
  @Override
  public boolean isValueValid( Object value )
  {
    // value is valid if null or converts to a double or is decimal point
    if ( value == null )
      return true;

    try
    {
      String str = (String) value;

      if ( str.equals( "." ) )
        return true; // is decimal point

      Double.parseDouble( str );
      return true; // converts to a double
    }
    catch ( Exception exception )
    {
      return false; // not valid double
    }
  }
}
