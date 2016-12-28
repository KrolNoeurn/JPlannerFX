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

package rjc.jplanner.command;

import java.util.HashSet;
import java.util.Set;

import rjc.jplanner.JPlanner;
import rjc.jplanner.model.Task;
import rjc.jplanner.model.Tasks.PredecessorsList;

/*************************************************************************************************/
/******************************* UndoCommand for outdenting tasks ********************************/
/*************************************************************************************************/

public class CommandTaskOutdent implements IUndoCommand
{
  private Set<Integer>     m_rows;                    // rows to be outdented
  private PredecessorsList m_predecessors;            // predecessors before cleaning
  private int              m_min = Integer.MAX_VALUE; // min row being indented
  private int              m_max = Integer.MIN_VALUE; // max row being indented

  /**************************************** constructor ******************************************/
  public CommandTaskOutdent( Set<Integer> rows )
  {
    // add summary task's subtasks, assume tasks that can't be outdented already removed
    Set<Integer> subtaskRows = new HashSet<Integer>();
    for ( int row : rows )
    {
      if ( row < m_min )
        m_min = row;

      int summaryEnd = JPlanner.plan.task( row ).summaryEnd();
      if ( summaryEnd > 0 && summaryEnd > m_max )
        m_max = summaryEnd;

      if ( summaryEnd > row )
        for ( int i = row + 1; i <= summaryEnd; i++ )
          if ( !JPlanner.plan.task( i ).isNull() )
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
    // action command
    for ( int row : m_rows )
    {
      Task task = JPlanner.plan.task( row );
      task.setIndent( task.indent() - 1 );
    }
    JPlanner.plan.tasks.updateSummaryMarkers();
    m_predecessors = JPlanner.plan.tasks.cleanPredecessors();
    JPlanner.gui.message( m_predecessors.toString( "Cleaned" ) );
  }

  /******************************************* undo **********************************************/
  @Override
  public void undo()
  {
    // revert command
    for ( int row : m_rows )
    {
      Task task = JPlanner.plan.task( row );
      task.setIndent( task.indent() + 1 );
    }
    JPlanner.plan.tasks.updateSummaryMarkers();
    JPlanner.plan.tasks.restorePredecessors( m_predecessors );
    JPlanner.gui.message( m_predecessors.toString( "Restored" ) );
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
      return "Outdented task " + m_min;
    else
      return "Outdented tasks " + m_min + " to " + m_max;
  }

}