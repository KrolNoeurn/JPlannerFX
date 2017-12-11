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

import rjc.jplanner.JPlanner;

/*************************************************************************************************/
/********************************** Stack of UndoCommand objects *********************************/
/*************************************************************************************************/

public class UndoStack
{
  private ArrayList<IUndoCommand> m_stack;              // stack of undo commands
  private int                     m_index;              // current command
  private int                     m_cleanIndex;         // index when declared clean
  private boolean                 m_previousCleanState; // previous so change of clean state can be checked
  private IUndoCommand            m_parentCommand;      // set if want to merge commands

  /**************************************** constructor ******************************************/
  public UndoStack()
  {
    // initialise private variables
    m_stack = new ArrayList<IUndoCommand>();
    m_index = 0;
    m_cleanIndex = 0;
    m_previousCleanState = true;
  }

  /****************************************** setClean *******************************************/
  public void setClean()
  {
    // declare current index position as being clean
    m_cleanIndex = m_index;
    m_previousCleanState = true;
  }

  /****************************************** isClean ********************************************/
  public boolean isClean()
  {
    // stack is clean if current index equals clean index
    return m_index == m_cleanIndex;
  }

  /************************************ getPreviousCleanState ************************************/
  public boolean getPreviousCleanState()
  {
    // return previous clean state
    return m_previousCleanState;
  }

  /************************************ setPreviousCleanState ************************************/
  public void setPreviousCleanState( boolean previous )
  {
    // set previous clean state
    m_previousCleanState = previous;
  }

  /******************************************** update *********************************************/
  public void update( int updates )
  {
    // perform requested gui updates
    if ( JPlanner.gui != null )
    {
      if ( ( updates & IUndoCommand.RESCHEDULE ) > 0 )
        JPlanner.gui.schedule();

      if ( ( updates & IUndoCommand.UPDATE_TASKS ) > 0 )
        JPlanner.gui.redrawTaskTables();
      if ( ( updates & IUndoCommand.UPDATE_RESOURCES ) > 0 )
        JPlanner.gui.redrawResourceTables();
      if ( ( updates & IUndoCommand.UPDATE_CALENDARS ) > 0 )
        JPlanner.gui.redrawCalendarTables();
      if ( ( updates & IUndoCommand.UPDATE_DAYS ) > 0 )
        JPlanner.gui.redrawDayTypeTables();

      if ( ( updates & IUndoCommand.RESET_TASKS ) > 0 )
        JPlanner.gui.relayoutTaskTables();
      if ( ( updates & IUndoCommand.RESET_RESOURCES ) > 0 )
        JPlanner.gui.relayoutResourceTables();
      if ( ( updates & IUndoCommand.RESET_CALENDARS ) > 0 )
        JPlanner.gui.relayoutCalendarTables();
      if ( ( updates & IUndoCommand.RESET_DAYS ) > 0 )
        JPlanner.gui.relayoutDayTypeTables();

      if ( ( updates & IUndoCommand.UPDATE_PROPERTIES ) > 0 )
        JPlanner.gui.getPropertiesPane().updateFromPlan();
      if ( ( updates & IUndoCommand.UPDATE_NOTES ) > 0 )
        JPlanner.gui.getNotesPane().updateFromPlan();
    }
  }

  /***************************************** startMerge ******************************************/
  public void startMerge( IUndoCommand command )
  {
    // specify parent to start command merging
    m_parentCommand = command;
  }

  /****************************************** endMerge *******************************************/
  public void endMerge()
  {
    // undo parent command as will be redone when parent is added to undo stack
    m_parentCommand.undo();
    m_parentCommand = null;
  }

  /******************************************** push *********************************************/
  public void push( IUndoCommand command )
  {
    // check if merging commands
    if ( m_parentCommand == null )
    {
      // remove any commands from stack that haven't been actioned (i.e. above index)
      while ( m_stack.size() > m_index )
        m_stack.remove( m_stack.size() - 1 );

      // add new command to stack, do it, and increment stack index
      m_stack.add( command );
      command.redo();
      update( command.update() );
      m_index++;
      JPlanner.gui.updateUndoRedo();
    }
    else
    {
      // merge command and do it
      m_parentCommand.merge( command );
      command.redo();
    }
  }

  /******************************************** push *********************************************/
  public void push( IUndoCommand... commands )
  {
    // remove any commands from stack that haven't been actioned (i.e. above index)
    while ( m_stack.size() > m_index )
      m_stack.remove( m_stack.size() - 1 );

    // create merged command
    MergedCommand command = new MergedCommand( commands );

    // add new command to stack, do it, and increment stack index
    m_stack.add( command );
    command.redo();
    update( command.update() );
    m_index++;
    JPlanner.gui.updateUndoRedo();
  }

  /******************************************** undo *********************************************/
  public void undo()
  {
    // decrement index and revert command
    if ( m_index > 0 )
    {
      m_index--;
      m_stack.get( m_index ).undo();
      update( m_stack.get( m_index ).update() );
      JPlanner.gui.updateUndoRedo();
    }
  }

  /******************************************** redo *********************************************/
  public void redo()
  {
    // action command and increment index
    if ( m_index < m_stack.size() )
    {
      m_stack.get( m_index ).redo();
      update( m_stack.get( m_index ).update() );
      m_index++;
      JPlanner.gui.updateUndoRedo();
    }
  }

  /***************************************** getUndoText *****************************************/
  public String getUndoText()
  {
    // return text associated with next potential undo
    return m_stack.get( m_index - 1 ).text();
  }

  /***************************************** getRedoText *****************************************/
  public String getRedoText()
  {
    // return text associated with next potential redo
    return m_stack.get( m_index ).text();
  }

  /******************************************* getText *******************************************/
  public String getText( int index )
  {
    // return text associated with command at index
    return m_stack.get( index ).text();
  }

  /******************************************** clear ********************************************/
  public void clear()
  {
    // clean the stack
    m_stack.clear();
    m_index = 0;
    m_cleanIndex = 0;
    m_previousCleanState = true;
    JPlanner.gui.updateUndoRedo();
  }

  /******************************************** size *********************************************/
  public int size()
  {
    // return stack size
    return m_stack.size();
  }

  /****************************************** getIndex *******************************************/
  public int getIndex()
  {
    // return command index
    return m_index;
  }

  /****************************************** setIndex *******************************************/
  public void setIndex( int index )
  {
    // execute redo's or undo's as necessary to get to target index
    if ( index == m_index )
      return;

    int updates = 0;
    while ( index < m_index && m_index > 0 )
    {
      m_index--;
      m_stack.get( m_index ).undo();
      updates |= m_stack.get( m_index ).update();
    }
    while ( index > m_index && m_index < m_stack.size() )
    {
      m_stack.get( m_index ).redo();
      updates |= m_stack.get( m_index ).update();
      m_index++;
    }

    // perform updates collected from the redo's and undo's
    update( updates );
    JPlanner.gui.updateUndoRedo();
  }

}
