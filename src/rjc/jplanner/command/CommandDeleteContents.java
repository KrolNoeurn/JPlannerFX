/**************************************************************************
 *  Copyright (C) 2017 by Richard Crook                                   *
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
import java.util.Set;

import rjc.jplanner.JPlanner;
import rjc.jplanner.gui.table.AbstractDataSource;
import rjc.jplanner.gui.table.TableSelection;

/*************************************************************************************************/
/************************* UndoCommand for deleting table cell contents **************************/
/*************************************************************************************************/

public class CommandDeleteContents implements IUndoCommand
{
  private ArrayList<IUndoCommand> m_commands; // merged commands

  /**************************************** constructor ******************************************/
  public CommandDeleteContents( AbstractDataSource data, Set<Integer> cells )
  {
    // create list of merged commands that set value to null to delete contents
    m_commands = new ArrayList<IUndoCommand>();

    JPlanner.plan.getUndostack().startMerge( this );
    for ( int hash : cells )
    {
      int row = hash % TableSelection.SELECT_HASH;
      int columnIndex = hash / TableSelection.SELECT_HASH;
      data.setValue( columnIndex, row, null );
    }
    JPlanner.plan.getUndostack().endMerge();
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
    // command description
    return "Delete " + m_commands.size();
  }

  /******************************************** merge ********************************************/
  @Override
  public void merge( IUndoCommand command )
  {
    // add command to list
    m_commands.add( command );
  }

}
