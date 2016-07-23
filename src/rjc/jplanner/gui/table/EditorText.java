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

package rjc.jplanner.gui.table;

/*************************************************************************************************/
/******************************* Table cell editor for simple text *******************************/
/*************************************************************************************************/

public class EditorText extends CellEditor
{
  CellTextField m_editor; // text editor

  /**************************************** constructor ******************************************/
  public EditorText( int columnIndex, int rowIndex )
  {
    // create text table cell editor
    super( columnIndex, rowIndex );
    m_editor = new CellTextField();
    setEditor( m_editor );
  }

  /******************************************* getText *******************************************/
  @Override
  public String getText()
  {
    // get editor text
    return m_editor.getText();
  }

  /******************************************* setValue ******************************************/
  @Override
  public void setValue( Object value )
  {
    // set editor text
    m_editor.setValue( (String) value );
  }

  /******************************************** open *********************************************/
  @Override
  public void open( Table table, Object value, MoveDirection move )
  {
    // open editor
    m_editor.calculateWidth( table, getColumnIndex() );
    super.open( table, value, move );
  }

}
