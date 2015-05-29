/**************************************************************************
 *  Copyright (C) 2015 by Richard Crook                                   *
 *  https://github.com/dazzle50/JPlanner                                  *
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

public class CommandSetCalendarValue implements IUndoCommand
{
  private int    m_calID;   // calendar number in plan
  private int    m_section; // section number
  private Object m_newValue; // new value after command
  private Object m_oldValue; // old value before command

  /**************************************** constructor ******************************************/
  public CommandSetCalendarValue( int calID, int section, Object newValue, Object oldValue )
  {
    // initialise private variables
    m_calID = calID;
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

    // update calendars tables
    // --JPlanner.gui.calendarTables().refresh();

    // if name being changed, update resources table and properties in case name displayed there
    if ( m_section == Calendar.SECTION_NAME )
    {
      // --JPlanner.gui.resourceTables().refresh();
      // --JPlanner.gui.properties().updateFromPlan();
    }
  }

  /******************************************* undo **********************************************/
  @Override
  public void undo()
  {
    // revert command
    JPlanner.plan.calendar( m_calID ).setData( m_section, m_oldValue );

    // update calendars tables
    // --JPlanner.gui.calendarTables().refresh();

    // if name being changed, update resources table and properties in case name displayed there
    if ( m_section == Calendar.SECTION_NAME )
    {
      // --JPlanner.gui.resourceTables().refresh();
      // --JPlanner.gui.properties().updateFromPlan();
    }
  }

  /******************************************* text **********************************************/
  @Override
  public String text()
  {
    // command description
    return "Day " + ( m_calID + 1 ) + " " + Calendar.sectionName( m_section ) + " = " + m_newValue;
  }

}
