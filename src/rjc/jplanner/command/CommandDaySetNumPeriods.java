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

import java.util.ArrayList;

import rjc.jplanner.model.Day;
import rjc.jplanner.model.DayWorkPeriod;
import rjc.jplanner.model.Time;

/*************************************************************************************************/
/****************** UndoCommand for updating day-types number of work periods ********************/
/*************************************************************************************************/

public class CommandDaySetNumPeriods implements IUndoCommand
{
  private Day                      m_day;        // day in plan
  private ArrayList<DayWorkPeriod> m_newPeriods; // new list of work-periods after command
  private ArrayList<DayWorkPeriod> m_oldPeriods; // old list of work-periods before command

  /**************************************** constructor ******************************************/
  public CommandDaySetNumPeriods( Day day, int newNum, int oldNum )
  {
    // initialise private variables
    m_day = day;
    m_oldPeriods = new ArrayList<DayWorkPeriod>( day.getWorkPeriods() );
    m_newPeriods = new ArrayList<DayWorkPeriod>( day.getWorkPeriods() );

    if ( newNum > oldNum )
    {
      // need to add new work-periods
      double remainingHours = 24.0;
      if ( !m_newPeriods.isEmpty() )
        remainingHours -= 24.0 * m_newPeriods.get( oldNum - 1 ).m_end.getMilliseconds() / Time.MILLISECONDS_IN_DAY;

      double increment = remainingHours / ( 1 + 2 * ( newNum - oldNum ) );
      if ( increment >= 8.0 )
        increment = 8.0;
      else if ( increment >= 4.0 )
        increment = 4.0;
      else if ( increment >= 2.0 )
        increment = 2.0;
      else if ( increment >= 1.0 )
        increment = 1.0;
      else if ( increment >= 0.5 )
        increment = 0.5;
      else if ( increment >= 10.0 / 60.0 )
        increment = 10.0 / 60.0;
      else if ( increment >= 5.0 / 60.0 )
        increment = 5.0 / 60.0;
      else
        increment = 1.0 / 60.0;

      double start = 24.0 - remainingHours + increment;

      for ( int count = oldNum; count < newNum; count++ )
      {
        m_newPeriods.add( new DayWorkPeriod( start, start + increment ) );
        start += 2 * increment;
      }
    }
    else
    {
      // need to reduce number of work-periods
      for ( int count = oldNum - 1; count >= newNum; count-- )
        m_newPeriods.remove( count );
    }
  }

  /******************************************* redo **********************************************/
  @Override
  public void redo()
  {
    // action command
    m_day.setValue( Day.SECTION_PERIODS, m_newPeriods );
  }

  /******************************************* undo **********************************************/
  @Override
  public void undo()
  {
    // revert command
    m_day.setValue( Day.SECTION_PERIODS, m_oldPeriods );
  }

  /****************************************** update *********************************************/
  @Override
  public int update()
  {
    // reset day-types tables because number of row probably changed, and trigger re-schedule
    return RESET_DAYS | RESCHEDULE;
  }

  /******************************************* text **********************************************/
  @Override
  public String text()
  {
    // text description of command
    return "Day " + ( m_day.getIndex() + 1 ) + " Periods = " + m_newPeriods.size();
  }

}
