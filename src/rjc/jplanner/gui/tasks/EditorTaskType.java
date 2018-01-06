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

package rjc.jplanner.gui.tasks;

import javafx.scene.input.KeyEvent;
import rjc.jplanner.gui.AbstractComboEditor;
import rjc.jplanner.gui.table.AbstractCellEditor;
import rjc.jplanner.model.TaskType;

/*************************************************************************************************/
/******************************** Table cell editor for task type ********************************/
/*************************************************************************************************/

class EditorTaskType extends AbstractCellEditor
{
  // extended version of AbstractComboEditor with list of calendars
  private class TaskTypeCombo extends AbstractComboEditor
  {
    /**************************************** getItemCount *****************************************/
    @Override
    public int getItemCount()
    {
      // return number of task types
      return TaskType.count();
    }

    /******************************************* getItem *******************************************/
    @Override
    public String getItem( int num )
    {
      // return task type name
      return TaskType.toString( num );
    }

    /****************************************** setString ******************************************/
    public void setString( String str )
    {
      // set editor to data source value, then react to string as if typed
      setText( getDataSourceValue().toString() );
      keyTyped( new KeyEvent( KeyEvent.KEY_TYPED, str, str, null, false, false, false, false ) );
    }

  }

  private TaskTypeCombo m_combo; // combo editor

  /**************************************** constructor ******************************************/
  public EditorTaskType( int columnIndex, int row )
  {
    // create task type editor
    super( columnIndex, row );
    m_combo = new TaskTypeCombo();
    setControl( m_combo );
  }

  /******************************************* getValue ******************************************/
  @Override
  public Object getValue()
  {
    // return selected task type
    return TaskType.from( m_combo.getText() );
  }

  /******************************************* setValue ******************************************/
  @Override
  public void setValue( Object value )
  {
    // set editor display to value if valid task-type, otherwise react to string as if typed
    if ( value instanceof TaskType )
      m_combo.setText( value.toString() );
    else if ( value instanceof String )
      m_combo.setString( (String) value );
    else
      throw new IllegalArgumentException( value.getClass() + " '" + value + "'" );
  }

}
