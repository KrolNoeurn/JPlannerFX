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

/*************************************************************************************************/
/************************* UndoCommand for updating calendar exceptions **************************/
/*************************************************************************************************/

public class CommandCalendarSetExceptions implements IUndoCommand
{

  /**************************************** constructor ******************************************/
  public CommandCalendarSetExceptions( Calendar cal, Object newValue, Object oldValue )
  {
    // TODO Auto-generated constructor stub
  }

  /******************************************* redo **********************************************/
  @Override
  public void redo()
  {
    // TODO Auto-generated method stub

  }

  /******************************************* undo **********************************************/
  @Override
  public void undo()
  {
    // TODO Auto-generated method stub

  }

  /****************************************** update *********************************************/
  @Override
  public int update()
  {
    // update calendar tables, and trigger re-schedule
    return UPDATE_CALENDARS | RESCHEDULE;
  }

  /******************************************* text **********************************************/
  @Override
  public String text()
  {
    // TODO Auto-generated method stub
    return "Exceptions TBD";
  }

}
