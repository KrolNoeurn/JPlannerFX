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

package rjc.jplanner.gui.calendars;

import javafx.scene.input.KeyEvent;
import rjc.jplanner.gui.table.AbstractCellEditor;
import rjc.jplanner.model.Calendar;

/*************************************************************************************************/
/**************** Table cell editor for selecting a calendar from drop-down list *****************/
/*************************************************************************************************/

public class EditorSelectCalendar extends AbstractCellEditor
{
  CalendarCombo m_combo; // combo editor

  /**************************************** constructor ******************************************/
  public EditorSelectCalendar( int columnIndex, int rowIndex )
  {
    // create calendar editor
    super( columnIndex, rowIndex );
    m_combo = new CalendarCombo();
    setControl( m_combo );
  }

  /******************************************* getValue ******************************************/
  @Override
  public Object getValue()
  {
    // return selected plan calendar
    return m_combo.getCalendar();
  }

  /******************************************* setValue ******************************************/
  @Override
  public void setValue( Object value )
  {
    // set editor value if valid calendar, otherwise calendar from data source
    if ( value instanceof Calendar )
      m_combo.setCalendar( (Calendar) value );
    else if ( value instanceof String )
    {
      // set editor to data source value, then react to string as if typed
      String str = (String) value;
      m_combo.setCalendar( (Calendar) getDataSourceValue() );
      m_combo.keyTyped( new KeyEvent( KeyEvent.KEY_TYPED, str, str, null, false, false, false, false ) );
    }
    else
      throw new IllegalArgumentException( value.getClass() + " '" + value + "'" );
  }

}
