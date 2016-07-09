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

import javafx.geometry.Insets;
import javafx.scene.control.TextField;

/*************************************************************************************************/
/******************************* Table cell editor for simple text *******************************/
/*************************************************************************************************/

public class EditorText extends CellEditor
{

  /**************************************** constructor ******************************************/
  public EditorText( int columnIndex, int rowIndex )
  {
    // create text table cell editor
    super( columnIndex, rowIndex );
    TextField textfield = new TextField();
    textfield.setPadding( new Insets( 0, TableCanvas.CELL_PADDING, 0, TableCanvas.CELL_PADDING ) );
    setEditor( textfield );
  }

  /******************************************* getText *******************************************/
  @Override
  String getText()
  {
    // get editor text
    return ( (TextField) getfocusControl() ).getText();
  }

  /******************************************* setValue ******************************************/
  @Override
  void setValue( Object value )
  {
    // set text editor value
    TextField editor = (TextField) getfocusControl();
    editor.setText( (String) value );
    editor.selectRange( editor.getLength(), editor.getLength() );
  }

}
