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
import javafx.scene.control.Spinner;

/*************************************************************************************************/
/***************************** Table cell editor for TimeSpan fields *****************************/
/*************************************************************************************************/

// NOTES
// http://stackoverflow.com/questions/32613619/how-to-make-a-timespinner-in-javafx

public class EditorTimeSpan extends CellEditor
{

  /**************************************** constructor ******************************************/
  public EditorTimeSpan( int columnIndex, int rowIndex )
  {
    // create time-span table cell editor
    super( columnIndex, rowIndex );

    Spinner<Integer> spin = new Spinner<>( 0, 100, 10 );
    spin.setPadding( new Insets( 0, 0, 0, 0 ) );
    setEditor( spin );
  }

  /******************************************* getText *******************************************/
  @Override
  public String getText()
  {
    // TODO Auto-generated method stub

    return ( (Spinner<Integer>) getfocusControl() ).getValue().toString();
  }

  /******************************************* setValue ******************************************/
  @Override
  public void setValue( Object value )
  {
    // TODO Auto-generated method stub

    Spinner<Integer> editor = (Spinner<Integer>) getfocusControl();
    int integer = 10;
    //editor.getValueFactory().setValue( integer );
  }

}
