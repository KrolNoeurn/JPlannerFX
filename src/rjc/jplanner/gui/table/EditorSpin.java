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

import javafx.scene.control.Spinner;
import javafx.scene.input.ScrollEvent;

/*************************************************************************************************/
/****************************** Table cell editor for number fields ******************************/
/*************************************************************************************************/

public class EditorSpin extends CellEditor
{

  /**************************************** constructor ******************************************/
  public EditorSpin( int columnIndex, int rowIndex )
  {
    // create number spin table cell editor
    super( columnIndex, rowIndex );

    Spinner<Integer> spin = new Spinner<Integer>( 0, 999, 200 );

    spin.setOnScroll( event -> onScroll( event ) );
    setEditor( spin );
  }

  private void onScroll( ScrollEvent event )
  {
    // TODO Auto-generated method stub
    if ( event.getDeltaY() > 0 )
    {
      getSpinner().decrement();
    }
    else if ( event.getDeltaY() < 0 )
    {
      getSpinner().increment();
    }
  }

  /******************************************* getText *******************************************/
  @Override
  public String getText()
  {
    // TODO Auto-generated method stub
    //return ( (Spinner<Integer>) getfocusControl() ).getValue().toString();
    return getSpinner().getValue().toString();
  }

  @SuppressWarnings( "unchecked" )
  private Spinner<Integer> getSpinner()
  {
    // TODO Auto-generated method stub
    return (Spinner<Integer>) getfocusControl();
  }

  /******************************************* setValue ******************************************/
  @Override
  public void setValue( Object value )
  {
    // TODO Auto-generated method stub

  }

}
