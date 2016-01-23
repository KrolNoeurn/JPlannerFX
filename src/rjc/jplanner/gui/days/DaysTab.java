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

package rjc.jplanner.gui.days;

import javafx.scene.control.Tab;
import rjc.jplanner.gui.table.Table;
import rjc.jplanner.model.Day;

/*************************************************************************************************/
/************************* Tab showing table of available plan day-types *************************/
/*************************************************************************************************/

public class DaysTab extends Tab
{

  /**************************************** constructor ******************************************/
  public DaysTab( String text )
  {
    // construct tab
    super( text );
    setClosable( false );

    // showing table of available plan day-types
    Table table = new Table( text, new DaysData() );
    table.setDefaultColumnWidth( 60 );
    table.setWidthByColumnIndex( Day.SECTION_NAME, 150 );

    // only have tab contents set if tab selected
    selectedProperty().addListener( ( observable, oldValue, newValue ) ->
    {
      if ( newValue )
        setContent( table );
      else
        setContent( null );
    } );
  }

}
