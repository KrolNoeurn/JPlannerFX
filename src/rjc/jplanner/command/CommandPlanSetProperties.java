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
import rjc.jplanner.model.DateTime;

/*************************************************************************************************/
/*************************** UndoCommand for updating plan properties ****************************/
/*************************************************************************************************/

public class CommandPlanSetProperties implements IUndoCommand
{
  private String   m_oldTitle;
  private DateTime m_oldStart;
  private Calendar m_oldCal;
  private String   m_oldDTformat;
  private String   m_oldDformat;

  private String   m_newTitle;
  private DateTime m_newStart;
  private Calendar m_newCal;
  private String   m_newDTformat;
  private String   m_newDformat;

  /**************************************** constructor ******************************************/
  public CommandPlanSetProperties( String title, DateTime start, Calendar cal, String DTformat, String Dformat )
  {
    // initialise private variables
    m_oldTitle = JPlanner.plan.title();
    m_oldStart = JPlanner.plan.start();
    m_oldCal = JPlanner.plan.calendar();
    m_oldDTformat = JPlanner.plan.datetimeFormat();
    m_oldDformat = JPlanner.plan.dateFormat();

    m_newTitle = title;
    m_newStart = start;
    m_newCal = cal;
    m_newDTformat = DTformat;
    m_newDformat = Dformat;
  }

  /******************************************* redo **********************************************/
  @Override
  public void redo()
  {
    // action command
    JPlanner.plan.setTitle( m_newTitle );
    JPlanner.plan.setStart( m_newStart );
    JPlanner.plan.setCalendar( m_newCal );
    JPlanner.plan.setDatetimeFormat( m_newDTformat );
    JPlanner.plan.setDateFormat( m_newDformat );
  }

  /******************************************* undo **********************************************/
  @Override
  public void undo()
  {
    // revert command
    JPlanner.plan.setTitle( m_oldTitle );
    JPlanner.plan.setStart( m_oldStart );
    JPlanner.plan.setCalendar( m_oldCal );
    JPlanner.plan.setDatetimeFormat( m_oldDTformat );
    JPlanner.plan.setDateFormat( m_oldDformat );
  }

  /****************************************** update *********************************************/
  @Override
  public void update()
  {
    // update plan properties & tables on gui, mostly just for possible format changes
    //JPlanner.gui.properties().updateFromPlan();
    //JPlanner.gui.redrawTaskTables();
    //JPlanner.gui.redrawResourceTables();
    //JPlanner.gui.redrawCalendarTables();

    // if start or calendar changed, re-schedule plan (which in turn will also update gui)
    //if ( !m_oldStart.equals( m_newStart ) || !m_oldCal.equals( m_newCal ) )
    //JPlanner.gui.schedule();
  }

  /******************************************* text **********************************************/
  @Override
  public String text()
  {
    // command description
    StringBuilder txt = new StringBuilder( 32 );
    txt.append( "Plan " );

    if ( !m_newTitle.equals( m_oldTitle ) )
      txt.append( "title/" );
    if ( m_newStart.milliseconds() != m_oldStart.milliseconds() )
      txt.append( "default start/" );
    if ( m_newCal != m_oldCal )
      txt.append( "default calendar/" );
    if ( !m_newDTformat.equals( m_oldDTformat ) )
      txt.append( "date-time format/" );
    if ( !m_newDformat.equals( m_oldDformat ) )
      txt.append( "date format/" );

    txt.deleteCharAt( txt.length() - 1 );
    return txt.toString();
  }

}
