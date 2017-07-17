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

package rjc.jplanner.gui.table;

import rjc.jplanner.gui.DateEditor;
import rjc.jplanner.model.Date;

/*************************************************************************************************/
/******************************* Table cell editor for Date fields *******************************/
/*************************************************************************************************/

public class EditorDate extends AbstractCellEditor
{
  DateEditor m_editor; // date editor

  /**************************************** constructor ******************************************/
  public EditorDate( int columnIndex, int row )
  {
    // create date table cell editor
    super( columnIndex, row );
    m_editor = new DateEditor();
    setControl( m_editor );
  }

  /******************************************* getValue ******************************************/
  @Override
  public Object getValue()
  {
    // return value date-time
    return m_editor.getDate();
  }

  /******************************************* setValue ******************************************/
  @Override
  public void setValue( Object value )
  {
    // if value is null, default to today
    if ( value == null )
      value = Date.now();

    // set value depending on type
    if ( value instanceof Date )
      m_editor.setDate( (Date) value );
    else
      throw new IllegalArgumentException( "Don't know how to handle " + value.getClass() + " " + value );
  }

}
