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

package rjc.jplanner.command;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import rjc.jplanner.JPlanner;
import rjc.jplanner.gui.MainTabWidget;
import rjc.jplanner.gui.table.Table;
import rjc.jplanner.model.Task;
import rjc.jplanner.model.Tasks.PredecessorsList;

/*************************************************************************************************/
/******************************* UndoCommand for indenting tasks *********************************/
/*************************************************************************************************/

public class CommandTaskIndent implements IUndoCommand
{
  private Set<Integer>     m_rows;                    // rows to be indented
  private PredecessorsList m_predecessors;            // predecessors before cleaning
  private int              m_min = Integer.MAX_VALUE; // min row being indented
  private int              m_max = Integer.MIN_VALUE; // max row being indented

  /**************************************** constructor ******************************************/
  public CommandTaskIndent( Set<Integer> rows )
  {
    // add summary task's subtasks, assume tasks that can't be indented already removed
    Set<Integer> subtaskRows = new HashSet<Integer>();
    for ( int row : rows )
    {
      if ( row < m_min )
        m_min = row;

      int summaryEnd = JPlanner.plan.getTask( row ).getSummaryEnd();
      if ( summaryEnd > 0 && summaryEnd > m_max )
        m_max = summaryEnd;

      if ( summaryEnd > row )
        for ( int i = row + 1; i <= summaryEnd; i++ )
          if ( !JPlanner.plan.getTask( i ).isNull() )
            subtaskRows.add( i );
    }
    rows.addAll( subtaskRows );

    // initialise private variables
    m_rows = rows;
  }

  /******************************************* redo **********************************************/
  @Override
  public void redo()
  {
    // collect table collapsed summary rows
    HashMap<Table, Set<Integer>> collapsed = new HashMap<Table, Set<Integer>>();
    for ( MainTabWidget tabs : JPlanner.gui.getTabs() )
    {
      Table table = tabs.getTasksTab().getTable();
      collapsed.put( table, table.getCollapsed() );
    }

    // action command
    for ( int row : m_rows )
    {
      Task task = JPlanner.plan.getTask( row );
      task.setIndent( task.getIndent() + 1 );
    }
    JPlanner.plan.tasks.updateSummaryMarkers();
    m_predecessors = JPlanner.plan.tasks.cleanPredecessors();
    JPlanner.gui.message( m_predecessors.toString( "Cleaned" ) );

    // reinstate table collapsed summaries
    collapsed.forEach( ( table, collapse ) -> table.setCollapsed( collapse ) );
  }

  /******************************************* undo **********************************************/
  @Override
  public void undo()
  {
    // collect table collapsed summary rows
    HashMap<Table, Set<Integer>> collapsed = new HashMap<Table, Set<Integer>>();
    for ( MainTabWidget tabs : JPlanner.gui.getTabs() )
    {
      Table table = tabs.getTasksTab().getTable();
      collapsed.put( table, table.getCollapsed() );
    }

    // revert command
    for ( int row : m_rows )
    {
      Task task = JPlanner.plan.getTask( row );
      task.setIndent( task.getIndent() - 1 );
    }
    JPlanner.plan.tasks.updateSummaryMarkers();
    JPlanner.plan.tasks.restorePredecessors( m_predecessors );
    JPlanner.gui.message( m_predecessors.toString( "Restored" ) );

    // reinstate table collapsed summaries
    collapsed.forEach( ( table, collapse ) -> table.setCollapsed( collapse ) );
  }

  /****************************************** update *********************************************/
  @Override
  public int update()
  {
    // re-schedule plan (which in turn will update gui)
    return RESCHEDULE;
  }

  /******************************************* text **********************************************/
  @Override
  public String text()
  {
    // command description
    if ( m_min == m_max || m_max < 0 )
      return "Indented task " + m_min;
    else
      return "Indented tasks " + m_min + " to " + m_max;
  }

}