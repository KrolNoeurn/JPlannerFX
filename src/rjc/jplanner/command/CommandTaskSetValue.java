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

import rjc.jplanner.model.Task;

/*************************************************************************************************/
/******************************** UndoCommand for updating tasks *********************************/
/*************************************************************************************************/

public class CommandTaskSetValue implements IUndoCommand
{
  private Task   m_task;     // task in plan
  private int    m_section;  // section number
  private Object m_newValue; // new value after command
  private Object m_oldValue; // old value before command

  /**************************************** constructor ******************************************/
  public CommandTaskSetValue( Task task, int section, Object newValue, Object oldValue )
  {
    // initialise private variables
    m_task = task;
    m_section = section;
    m_newValue = newValue;
    m_oldValue = oldValue;
  }

  /******************************************* redo **********************************************/
  @Override
  public void redo()
  {
    // action command
    m_task.setValue( m_section, m_newValue );
  }

  /******************************************* undo **********************************************/
  @Override
  public void undo()
  {
    // revert command
    m_task.setValue( m_section, m_oldValue );
  }

  /****************************************** update *********************************************/
  @Override
  public int update()
  {
    // update tasks tables
    int updates = UPDATE_TASKS;

    // if initials and old value was null, update properties so it shows new count of tasks
    if ( m_section == Task.SECTION_TITLE && m_oldValue == null )
      updates |= UPDATE_PROPERTIES;

    // if updating field other than title/comment/cost, trigger re-schedule
    if ( m_section != Task.SECTION_TITLE && m_section != Task.SECTION_COMMENT && m_section != Task.SECTION_COST )
      updates |= RESCHEDULE;

    return updates;
  }

  /******************************************* text **********************************************/
  @Override
  public String text()
  {
    // command description
    return "Task " + m_task.getIndex() + " " + Task.getSectionName( m_section ) + " = " + m_newValue;
  }

}
