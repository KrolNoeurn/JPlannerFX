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
import rjc.jplanner.model.Day;

/*************************************************************************************************/
/****************** UndoCommand for updating day-types (except num of periods) *******************/
/*************************************************************************************************/

public class CommandSetDayValue implements IUndoCommand
{
  private int    m_dayID;   // day number in plan
  private int    m_section; // section number
  private Object m_newValue; // new value after command
  private Object m_oldValue; // old value before command

  /**************************************** constructor ******************************************/
  public CommandSetDayValue( int dayID, int section, Object newValue, Object oldValue )
  {
    // check not being used for updating number of work periods
    if ( section == Day.SECTION_PERIODS )
      throw new UnsupportedOperationException( "Number of work-periods" );

    // initialise private variables
    m_dayID = dayID;
    m_section = section;
    m_newValue = newValue;
    m_oldValue = oldValue;
  }

  /******************************************* redo **********************************************/
  @Override
  public void redo()
  {
    // action command
    JPlanner.plan.day( m_dayID ).setData( m_section, m_newValue );

    // update day-types table, and if name section also update calendars table
    // --JPlanner.gui.dayTables().refresh();
    // --if ( m_section == Day.SECTION_NAME )
    // --JPlanner.gui.calendarTables().refresh();
  }

  /******************************************* undo **********************************************/
  @Override
  public void undo()
  {
    // revert command
    JPlanner.plan.day( m_dayID ).setData( m_section, m_oldValue );

    // update day-types table, and if name section also update calendars table
    // --JPlanner.gui.dayTables().refresh();
    // --if ( m_section == Day.SECTION_NAME )
    // --JPlanner.gui.calendarTables().refresh();
  }

  /******************************************* text **********************************************/
  @Override
  public String text()
  {
    // command description
    return "Day " + ( m_dayID + 1 ) + " " + Day.sectionName( m_section ) + " = " + m_newValue;
  }

}
