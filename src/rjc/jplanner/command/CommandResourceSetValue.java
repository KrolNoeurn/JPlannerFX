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

import rjc.jplanner.JPlanner;
import rjc.jplanner.model.Calendar;
import rjc.jplanner.model.Resource;
import rjc.jplanner.model.Task;
import rjc.jplanner.model.TaskResources;

/*************************************************************************************************/
/****************************** UndoCommand for updating resources *******************************/
/*************************************************************************************************/

public class CommandResourceSetValue implements IUndoCommand
{
  private Resource                     m_res;      // resource in plan
  private int                          m_section;  // section number
  private Object                       m_oldValue; // old value before command
  private Object                       m_newValue; // new value after command
  private HashMap<Task, TaskResources> m_oldTRs;   // old task TaskResources before command
  private HashMap<Task, TaskResources> m_newTRs;   // new task TaskResources after command

  /**************************************** constructor ******************************************/
  public CommandResourceSetValue( Resource res, int section, Object newValue, Object oldValue )
  {
    // initialise private variables
    m_res = res;
    m_section = section;
    m_newValue = newValue;
    m_oldValue = oldValue;

    // if unique resource tag changed, check if TaskResources also need to change
    if ( section <= Resource.SECTION_ALIAS )
    {
      String oldTag = (String) oldValue;
      if ( oldTag != null && JPlanner.plan.resources.isTagUnique( oldTag ) )
      {
        // old tag is unique so need to check TaskResources
        m_oldTRs = JPlanner.plan.tasks.getTaskResources( oldTag );
        if ( m_oldTRs.isEmpty() )
          m_oldTRs = null;
        else
        {
          String newTag = (String) newValue;
          m_newTRs = new HashMap<Task, TaskResources>();
          m_oldTRs.forEach( ( task, tr ) -> m_newTRs.put( task, new TaskResources( tr, oldTag, newTag ) ) );
        }
      }
    }

  }

  /******************************************* redo **********************************************/
  @Override
  public void redo()
  {
    // action command
    m_res.setValue( m_section, m_newValue );

    // update if any need task TaskResources
    if ( m_newTRs != null )
      m_newTRs.forEach( ( task, tr ) -> task.setValue( Task.SECTION_RES, tr ) );
  }

  /******************************************* undo **********************************************/
  @Override
  public void undo()
  {
    // revert command
    m_res.setValue( m_section, m_oldValue );

    // restore if any need task TaskResources
    if ( m_oldTRs != null )
      m_oldTRs.forEach( ( task, tr ) -> task.setValue( Task.SECTION_RES, tr ) );
  }

  /****************************************** update *********************************************/
  @Override
  public int update()
  {
    // update resources tables
    int updates = UPDATE_RESOURCES;

    // if updating TaskResources, update tasks table
    if ( m_newTRs != null )
      updates |= UPDATE_TASKS;

    // if initials and old value was null, update properties so it shows new count of resources
    if ( m_section == Resource.SECTION_INITIALS && m_oldValue == null )
      updates |= UPDATE_PROPERTIES;

    // if updating field other than comment, trigger re-schedule
    if ( m_section != Resource.SECTION_COMMENT )
      updates |= RESCHEDULE;

    return updates;
  }

  /******************************************* text **********************************************/
  @Override
  public String text()
  {
    // command description
    String newValue = m_newValue == null ? "" : m_newValue.toString();
    if ( m_newValue instanceof Calendar )
      newValue = ( (Calendar) m_newValue ).getName();

    return "Resource " + m_res.getIndex() + " " + Resource.getSectionName( m_section ) + " = " + newValue;
  }

}
