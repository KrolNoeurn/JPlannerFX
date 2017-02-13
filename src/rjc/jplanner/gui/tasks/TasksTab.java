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

package rjc.jplanner.gui.tasks;

import javafx.scene.control.Tab;
import rjc.jplanner.gui.XSplitPane;
import rjc.jplanner.gui.gantt.Gantt;
import rjc.jplanner.gui.table.Table;

/*************************************************************************************************/
/******************** Tab showing table of the plan tasks alongside the gantt ********************/
/*************************************************************************************************/

public class TasksTab extends Tab
{
  private Table      m_table;
  private Gantt      m_gantt;
  private XSplitPane m_split;

  /**************************************** constructor ******************************************/
  public TasksTab( String text )
  {
    // construct tab
    super( text );
    setClosable( false );

    // showing table of the plan tasks
    m_table = new Table( "Tasks", new TasksData() );

    // alongside the gantt
    m_gantt = new Gantt();
    m_split = new XSplitPane( m_table, m_gantt );
    m_gantt.setSplitPane( m_split );

    // only have tab contents set if tab selected
    selectedProperty().addListener( ( observable, oldValue, newValue ) ->
    {
      if ( newValue )
        setContent( m_split );
      else
        setContent( null );
    } );
  }

  /****************************************** getTable *******************************************/
  public Table getTable()
  {
    return m_table;
  }

  /****************************************** getGantt *******************************************/
  public Gantt getGantt()
  {
    return m_gantt;
  }

  /************************************** getSplitPosition ***************************************/
  public int getSplitPosition()
  {
    return m_split.preferredLeftNodeWidth;
  }

  /************************************** setSplitPosition ***************************************/
  public void setSplitPosition( int pos )
  {
    m_split.preferredLeftNodeWidth = pos;
  }

}
