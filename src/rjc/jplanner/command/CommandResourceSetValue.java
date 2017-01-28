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

import rjc.jplanner.model.Calendar;
import rjc.jplanner.model.Resource;

/*************************************************************************************************/
/****************************** UndoCommand for updating resources *******************************/
/*************************************************************************************************/

public class CommandResourceSetValue implements IUndoCommand
{
  private Resource m_res;      // resource in plan
  private int      m_section;  // section number
  private Object   m_newValue; // new value after command
  private Object   m_oldValue; // old value before command

  /**************************************** constructor ******************************************/
  public CommandResourceSetValue( Resource res, int section, Object newValue, Object oldValue )
  {
    // initialise private variables
    m_res = res;
    m_section = section;
    m_newValue = newValue;
    m_oldValue = oldValue;
  }

  /******************************************* redo **********************************************/
  @Override
  public void redo()
  {
    // action command
    m_res.setValue( m_section, m_newValue );
  }

  /******************************************* undo **********************************************/
  @Override
  public void undo()
  {
    // revert command
    m_res.setValue( m_section, m_oldValue );
  }

  /****************************************** update *********************************************/
  @Override
  public int update()
  {
    // update resources tables
    int updates = UPDATE_RESOURCES;

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
    String newValue = m_newValue.toString();
    if ( m_newValue instanceof Calendar )
      newValue = ( (Calendar) m_newValue ).getName();

    return "Resource " + m_res.getIndex() + " " + Resource.getSectionName( m_section ) + " = " + newValue;
  }

}
