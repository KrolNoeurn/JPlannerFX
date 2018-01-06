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

/*************************************************************************************************/
/**************************** Interface for all commands on UndoStack ****************************/
/*************************************************************************************************/

public interface IUndoCommand
{
  public static final int RESCHEDULE        = 1 << 0;  // re-schedule the plan (which will update tasks/gantt/plan)
  public static final int UPDATE_TASKS      = 1 << 1;  // update tasks tables
  public static final int UPDATE_RESOURCES  = 1 << 2;  // update resources tables
  public static final int UPDATE_CALENDARS  = 1 << 3;  // update calendars tables
  public static final int UPDATE_DAYS       = 1 << 4;  // update day-types tables
  public static final int RESET_TASKS       = 1 << 5;  // reset tasks tables
  public static final int RESET_RESOURCES   = 1 << 6;  // reset resources tables
  public static final int RESET_CALENDARS   = 1 << 7;  // reset calendars tables
  public static final int RESET_DAYS        = 1 << 8;  // reset day-types tables
  public static final int UPDATE_PROPERTIES = 1 << 9;  // update plan properties
  public static final int UPDATE_NOTES      = 1 << 10; // update plan notes

  // applies the command
  public void redo();

  // reverts the command
  public void undo();

  // updates needs after command, bitwise-or of above static constants
  public int update();

  // short text string describing what this command, e.g. "insert text"
  public String text();

  // merge commands together if needed
  default public void merge( IUndoCommand command )
  {
  };
}
