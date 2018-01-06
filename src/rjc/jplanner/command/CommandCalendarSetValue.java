/**************************************************************************
 *  Copyright (C) 2018 by Richard Crook                                   *
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
import rjc.jplanner.model.Date;
import rjc.jplanner.model.Day;

/*************************************************************************************************/
/************* UndoCommand for updating calendars (except cycle-length & exceptions) *************/
/*************************************************************************************************/

public class CommandCalendarSetValue implements IUndoCommand
{
  private Calendar m_calendar; // calendar number in plan
  private int      m_section;  // section number
  private Object   m_newValue; // new value after command
  private Object   m_oldValue; // old value before command

  /**************************************** constructor ******************************************/
  public CommandCalendarSetValue( Calendar cal, int section, Object newValue, Object oldValue )
  {
    // initialise private variables
    m_calendar = cal;
    m_section = section;
    m_newValue = newValue;
    m_oldValue = oldValue;
  }

  /******************************************* redo **********************************************/
  @Override
  public void redo()
  {
    // action command
    m_calendar.setValue( m_section, m_newValue );
  }

  /******************************************* undo **********************************************/
  @Override
  public void undo()
  {
    // revert command
    m_calendar.setValue( m_section, m_oldValue );
  }

  /****************************************** update *********************************************/
  @Override
  public int update()
  {
    // if name changed, update also resources tables and properties, otherwise re-schedule
    if ( m_section == Calendar.SECTION_NAME )
      return UPDATE_CALENDARS | UPDATE_RESOURCES | UPDATE_PROPERTIES;
    else
      return UPDATE_CALENDARS | RESCHEDULE;
  }

  /******************************************* text **********************************************/
  @Override
  public String text()
  {
    // command description
    String newValue = m_newValue.toString();
    if ( m_newValue instanceof Day )
      newValue = ( (Day) m_newValue ).getName();
    if ( m_newValue instanceof Date )
      newValue = ( (Date) m_newValue ).toFormat();

    return "Calendar " + ( m_calendar.getIndex() + 1 ) + " " + Calendar.getSectionName( m_section ) + " = " + newValue;
  }

}
