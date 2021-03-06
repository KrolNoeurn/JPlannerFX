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

import java.util.ArrayList;

import rjc.jplanner.JPlanner;
import rjc.jplanner.model.Calendar;
import rjc.jplanner.model.Day;

/*************************************************************************************************/
/************************ UndoCommand for updating calendar cycle length *************************/
/*************************************************************************************************/

public class CommandCalendarSetCycleLength implements IUndoCommand
{
  private Calendar       m_calendar;   // calendar being updated
  private ArrayList<Day> m_newNormals; // new list of normal-cycle-days after command
  private ArrayList<Day> m_oldNormals; // old list of normal-cycle-days before command

  /**************************************** constructor ******************************************/
  public CommandCalendarSetCycleLength( Calendar cal, int newLength, int oldLength )
  {
    // initialise private variables
    m_calendar = cal;
    m_oldNormals = new ArrayList<Day>( cal.getNormals() );
    m_newNormals = new ArrayList<Day>( cal.getNormals() );

    if ( newLength > oldLength )
    {
      // need to add new normal-cycle-days
      Day day = JPlanner.plan.getDay( 0 );
      for ( int count = oldLength; count < newLength; count++ )
        m_newNormals.add( day );
    }
    else
    {
      // need to reduce number of normal-cycle-days
      for ( int count = oldLength - 1; count >= newLength; count-- )
        m_newNormals.remove( count );
    }
  }

  /******************************************* redo **********************************************/
  @Override
  public void redo()
  {
    // action command
    m_calendar.setNormals( m_newNormals );
  }

  /******************************************* undo **********************************************/
  @Override
  public void undo()
  {
    // revert command
    m_calendar.setNormals( m_oldNormals );
  }

  /****************************************** update *********************************************/
  @Override
  public int update()
  {
    // reset calendar tables because number of row probably changed, and trigger re-schedule
    return RESET_CALENDARS | RESCHEDULE;
  }

  /******************************************* text **********************************************/
  @Override
  public String text()
  {
    // text description of command
    return "Calendar " + ( m_calendar.getIndex() + 1 ) + " Cycle = " + m_newNormals.size();
  }

}
