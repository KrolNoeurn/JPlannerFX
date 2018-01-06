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

import java.util.Arrays;
import java.util.List;

import rjc.jplanner.JPlanner;

/*************************************************************************************************/
/************************** UndoCommand for simple merging of commands ***************************/
/*************************************************************************************************/

public class MergedCommand implements IUndoCommand
{
  private List<IUndoCommand> m_commands; // merged commands

  /**************************************** constructor ******************************************/
  public MergedCommand( IUndoCommand[] commands )
  {
    // create list of merged commands
    m_commands = Arrays.asList( commands );

    JPlanner.trace( m_commands );
  }

  /******************************************* redo **********************************************/
  @Override
  public void redo()
  {
    // redo the list of merged commands
    m_commands.forEach( command -> command.redo() );
  }

  /******************************************* undo **********************************************/
  @Override
  public void undo()
  {
    // undo the list of merged commands
    m_commands.forEach( command -> command.undo() );
  }

  /****************************************** update *********************************************/
  @Override
  public int update()
  {
    // updates needs after command, bitwise-or of listed commands
    int updates = 0;
    for ( IUndoCommand command : m_commands )
      updates |= command.update();
    return updates;
  }

  /******************************************* text **********************************************/
  @Override
  public String text()
  {
    // command description is from first command in list
    return m_commands.get( 0 ).text();
  }

}
