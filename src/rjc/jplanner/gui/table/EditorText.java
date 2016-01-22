/**************************************************************************
 *  Copyright (C) 2015 by Richard Crook                                   *
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

import javafx.scene.control.TextField;
import javafx.scene.layout.Border;

/*************************************************************************************************/
/******************************* Table cell editor for simple text *******************************/
/*************************************************************************************************/

public class EditorText extends CellEditor
{

  /**************************************** constructor ******************************************/
  public EditorText()
  {
    // create table cell editor
    super();
    TextField textfield = new TextField();

    // set appearance
    //textfield.setPadding( new Insets( 0, Cell.CELL_PADDING - 1, 0, Cell.CELL_PADDING - 1 ) );
    textfield.setBorder( Border.EMPTY );

    // set contents
    //textfield.setText( getData().getCellText( getColumn(), getRow() ) );
    textfield.selectEnd();

    // display and focus
    setEditor( textfield );
  }

  /******************************************* getText *******************************************/
  @Override
  String getText()
  {
    return ( (TextField) getPrime() ).getText();
  }

}
