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

import rjc.jplanner.JPlanner;
import rjc.jplanner.model.Calendar;

/*************************************************************************************************/
/************* UndoCommand for updating calendars (except cycle-length & exceptions) *************/
/*************************************************************************************************/

public class CommandCalendarSetValue implements IUndoCommand
{
  private int    m_calID;    // calendar number in plan
  private int    m_section;  // section number
  private Object m_newValue; // new value after command
  private Object m_oldValue; // old value before command

  /**************************************** constructor ******************************************/
  public CommandCalendarSetValue( int calendarID, int section, Object newValue, Object oldValue )
  {
    // initialise private variables
    m_calID = calendarID;
    m_section = section;
    m_newValue = newValue;
    m_oldValue = oldValue;
  }

  /******************************************* redo **********************************************/
  @Override
  public void redo()
  {
    // action command
    JPlanner.plan.calendar( m_calID ).setData( m_section, m_newValue );
  }

  /******************************************* undo **********************************************/
  @Override
  public void undo()
  {
    // revert command
    JPlanner.plan.calendar( m_calID ).setData( m_section, m_oldValue );
  }

  /****************************************** update *********************************************/
  @Override
  public void update()
  {
    // update calendars tables
    //JPlanner.gui.redrawCalendarTables();

    // if name changed, update resources tables and properties, otherwise re-schedule
    if ( m_section == Calendar.SECTION_NAME )
    {
      //JPlanner.gui.redrawResourceTables();
      //JPlanner.gui.properties().updateFromPlan();
    }
    //else
    //JPlanner.gui.schedule();
  }

  /******************************************* text **********************************************/
  @Override
  public String text()
  {
    // command description
    return "Day " + ( m_calID + 1 ) + " " + Calendar.sectionName( m_section ) + " = " + m_newValue;
  }

}
