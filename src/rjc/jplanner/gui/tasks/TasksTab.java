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

package rjc.jplanner.gui.tasks;

import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import rjc.jplanner.gui.gantt.Gantt;
import rjc.jplanner.gui.table.Table;
import rjc.jplanner.model.Task;

/*************************************************************************************************/
/******************** Tab showing table of the plan tasks alongside the gantt ********************/
/*************************************************************************************************/

public class TasksTab extends Tab
{

  /**************************************** constructor ******************************************/
  public TasksTab( String text )
  {
    // construct tab
    super( text );
    setClosable( false );

    // showing table of the plan tasks
    Table table = new Table( "Tasks", new TasksData() );
    table.setDefaultColumnWidth( 110 );
    table.setWidthByColumnIndex( Task.SECTION_TITLE, 200 );
    table.setWidthByColumnIndex( Task.SECTION_DURATION, 60 );
    table.setWidthByColumnIndex( Task.SECTION_START, 140 );
    table.setWidthByColumnIndex( Task.SECTION_END, 140 );

    // alongside the gantt
    Gantt gantt = new Gantt();
    SplitPane split = new SplitPane( table, gantt );

    // only have tab contents set if tab selected
    selectedProperty().addListener( ( observable, oldValue, newValue ) ->
    {
      if ( newValue )
        setContent( split );
      else
        setContent( null );
    } );
  }

}
