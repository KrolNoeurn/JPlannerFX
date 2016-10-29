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

import javafx.scene.input.KeyEvent;
import rjc.jplanner.JPlanner;
import rjc.jplanner.gui.AbstractComboEditor;
import rjc.jplanner.gui.table.AbstractCellEditor;
import rjc.jplanner.model.Day;

/*************************************************************************************************/
/**************** Table cell editor for selecting a day-type from drop-down list *****************/
/*************************************************************************************************/

public class EditorSelectDay extends AbstractCellEditor
{
  public class DayCombo extends AbstractComboEditor
  {

    /**************************************** getItemCount *****************************************/
    @Override
    public int getItemCount()
    {
      // return number of day-types
      return JPlanner.plan.daysCount();
    }

    /******************************************* getItem *******************************************/
    @Override
    public String getItem( int num )
    {
      // return day-type name
      return JPlanner.plan.day( num ).name();
    }

    /****************************************** setString ******************************************/
    public void setString( String str )
    {
      // set editor to data source value, then react to string as if typed
      Day day = (Day) getDataSourceValue();
      setSelectedIndex( JPlanner.plan.index( day ) );
      keyTyped( new KeyEvent( KeyEvent.KEY_TYPED, str, str, null, false, false, false, false ) );
    }

  }

  DayCombo m_combo; // combo editor

  /**************************************** constructor ******************************************/
  public EditorSelectDay( int columnIndex, int rowIndex )
  {
    // create day-type editor
    super( columnIndex, rowIndex );
    m_combo = new DayCombo();
    setControl( m_combo );
  }

  /******************************************* getValue ******************************************/
  @Override
  public Object getValue()
  {
    // return selected plan day-type
    return JPlanner.plan.day( m_combo.getSelectedIndex() );
  }

  /******************************************* setValue ******************************************/
  @Override
  public void setValue( Object value )
  {
    // set editor display to value if valid day-type, otherwise day-type from data source
    if ( value instanceof Day )
      m_combo.setSelectedIndex( JPlanner.plan.index( (Day) value ) );
    else if ( value instanceof String )
      m_combo.setString( (String) value );
    else
      throw new IllegalArgumentException( value.getClass() + " '" + value + "'" );
  }

}
