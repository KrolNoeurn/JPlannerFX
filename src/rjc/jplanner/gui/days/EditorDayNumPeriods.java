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

package rjc.jplanner.gui.days;

import rjc.jplanner.JPlanner;
import rjc.jplanner.gui.SpinEditor;
import rjc.jplanner.gui.table.AbstractCellEditor;
import rjc.jplanner.model.Time;

/*************************************************************************************************/
/********************* Table cell editor for day-type number of work periods *********************/
/*************************************************************************************************/

public class EditorDayNumPeriods extends AbstractCellEditor
{
  SpinEditor m_spin; // spin editor

  /**************************************** constructor ******************************************/
  public EditorDayNumPeriods( int columnIndex, int rowIndex )
  {
    // use spin editor
    super( columnIndex, rowIndex );
    m_spin = new SpinEditor();

    // determine max count of number of work periods
    if ( JPlanner.plan.day( rowIndex ).numPeriods() > 0 )
    {
      Time end = JPlanner.plan.day( rowIndex ).end();
      int minutesToMidnight = ( Time.MILLISECONDS_IN_DAY - end.milliseconds() ) / 60000;
      int max = JPlanner.plan.day( rowIndex ).numPeriods() + minutesToMidnight / 2;
      m_spin.setRange( 0, max > 8 ? 8 : max, 0 );
    }
    else
      m_spin.setRange( 0, 8, 0 );

    m_spin.setStepPage( 1, 1 );
    setControl( m_spin );
  }

  /******************************************* getValue ******************************************/
  @Override
  public Object getValue()
  {
    // return number of work periods value as integer
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

  /****************************************** validValue *****************************************/
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
