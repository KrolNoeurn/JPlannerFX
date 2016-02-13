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

import rjc.jplanner.JPlanner;
import rjc.jplanner.model.Calendar;
import rjc.jplanner.model.Day;

/*************************************************************************************************/
/************************ UndoCommand for updating calendar cycle length *************************/
/*************************************************************************************************/

public class CommandCalendarSetCycleLength implements IUndoCommand
{
  private int            m_calID;      // calendar number in plan
  private ArrayList<Day> m_newNormals; // new list of normal-cycle-days after command
  private ArrayList<Day> m_oldNormals; // old list of normal-cycle-days before command

  /**************************************** constructor ******************************************/
  public CommandCalendarSetCycleLength( int calID, Object newValue, Object oldValue )
  {
    // initialise private variables
    m_calID = calID;
    m_oldNormals = new ArrayList<Day>( JPlanner.plan.calendar( calID ).normals() );
    m_newNormals = new ArrayList<Day>( JPlanner.plan.calendar( calID ).normals() );

    int newNum = Integer.parseInt( (String) newValue );
    int oldNum = Integer.parseInt( (String) oldValue );

    if ( newNum > oldNum )
    {
      // need to add new normal-cycle-days
      Day day = JPlanner.plan.day( 0 );
      for ( int count = oldNum; count < newNum; count++ )
        m_newNormals.add( day );
    }
    else
    {
      // need to reduce number of normal-cycle-days
      for ( int count = oldNum - 1; count >= newNum; count-- )
        m_newNormals.remove( count );
    }
  }

  /******************************************* redo **********************************************/
  @Override
  public void redo()
  {
    // action command
    JPlanner.plan.calendar( m_calID ).setData( Calendar.SECTION_CYCLE, m_newNormals );
  }

  /******************************************* undo **********************************************/
  @Override
  public void undo()
  {
    // revert command
    JPlanner.plan.calendar( m_calID ).setData( Calendar.SECTION_CYCLE, m_oldNormals );
  }

  /****************************************** update *********************************************/
  @Override
  public void update()
  {
    // reset calendar tables because number of row might have changed
    //JPlanner.gui.resetCalendarTables();

    // re-schedule plan (which in turn will also update gui)
    //JPlanner.gui.schedule();
  }

  /******************************************* text **********************************************/
  @Override
  public String text()
  {
    // text description of command
    return "Calendar " + m_calID + " Cycle = " + m_newNormals.size();
  }

}
