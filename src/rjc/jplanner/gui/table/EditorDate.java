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

package rjc.jplanner.gui.table;

import javafx.beans.value.ChangeListener;
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

  /************************************* addEpochdayListener *************************************/
  public void addEpochdayListener( ChangeListener<? super Number> listener )
  {
    // add listener to epoch-day integer property
    m_editor.addListener( listener );
  }

  /******************************************* getValue ******************************************/
  @Override
  public Object getValue()
  {
    // return value date
    return m_editor.getDate();
  }

  /******************************************* setValue ******************************************/
  @Override
  public void setValue( Object value )
  {
    // set value depending on type
    if ( value == null )
      m_editor.setDate( Date.now() );
    else if ( value instanceof Date )
      m_editor.setDate( (Date) value );
    else if ( value instanceof String )
    {
      // seed editor with suitable date before setting with input string
      setValue( (Date) getDataSourceValue() );
      m_editor.setCaretPos( ( (String) value ).length() );
      m_editor.setText( (String) value );
    }
    else
      throw new IllegalArgumentException( "Don't know how to handle " + value.getClass() + " " + value );
  }

}
