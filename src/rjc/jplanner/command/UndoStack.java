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

/*************************************************************************************************/
/********************************** Stack of UndoCommand objects *********************************/
/*************************************************************************************************/

public class UndoStack
{
  private ArrayList<IUndoCommand> m_stack;             // stack of undo commands
  private int                     m_index;             // current command
  private int                     m_cleanIndex;        // index when declared clean
  private boolean                 m_previousCleanState;

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

  /******************************************** update *********************************************/
  public void update( int updates )
  {
    // perform requested gui updates
    if ( JPlanner.gui != null )
    {
      if ( ( updates & IUndoCommand.UPDATE_TASKS ) > 0 )
        JPlanner.gui.redrawTaskTables();
      if ( ( updates & IUndoCommand.UPDATE_RESOURCES ) > 0 )
        JPlanner.gui.redrawResourceTables();
      if ( ( updates & IUndoCommand.UPDATE_CALENDARS ) > 0 )
        JPlanner.gui.redrawCalendarTables();
      if ( ( updates & IUndoCommand.UPDATE_DAYS ) > 0 )
        JPlanner.gui.redrawDayTypeTables();

      if ( ( updates & IUndoCommand.RESET_TASKS ) > 0 )
        JPlanner.gui.resetTaskTables();
      if ( ( updates & IUndoCommand.RESET_RESOURCES ) > 0 )
        JPlanner.gui.resetResourceTables();
      if ( ( updates & IUndoCommand.RESET_CALENDARS ) > 0 )
        JPlanner.gui.resetCalendarTables();
      if ( ( updates & IUndoCommand.RESET_DAYS ) > 0 )
        JPlanner.gui.resetDayTypeTables();

      if ( ( updates & IUndoCommand.UPDATE_PROPERTIES ) > 0 )
        JPlanner.gui.properties().updateFromPlan();
      if ( ( updates & IUndoCommand.UPDATE_NOTES ) > 0 )
        JPlanner.gui.notes().updateFromPlan();
    }

    // performed re-schedule if requested
    if ( ( updates & IUndoCommand.RESCHEDULE ) > 0 )
      JPlanner.gui.schedule();
  }

  /******************************************** push *********************************************/
  public void push( IUndoCommand command )
  {
    // remove any commands from stack that haven't been actioned (i.e. above index)
    while ( m_stack.size() > m_index )
      m_stack.remove( m_stack.size() - 1 );

    // add new command to stack, do it, and increment stack index
    m_stack.add( command );
    command.redo();
    update( command.update() );
    m_index++;
    updateUndoRedoMenuItems();

    // update undo-stack window if exists
    //if ( JPlanner.gui.undoWindow != null )
    //JPlanner.gui.undoWindow.updateList( command, m_index );
  }

  /******************************************** undo *********************************************/
  public void undo()
  {
    // decrement index and revert command
    m_index--;
    m_stack.get( m_index ).undo();
    update( m_stack.get( m_index ).update() );
    updateUndoRedoMenuItems();
  }

  /******************************************** redo *********************************************/
  public void redo()
  {
    // action command and increment index
    m_stack.get( m_index ).redo();
    update( m_stack.get( m_index ).update() );
    m_index++;
    updateUndoRedoMenuItems();
  }

  /*********************************** updateUndoRedoMenuItems ***********************************/
  private void updateUndoRedoMenuItems()
  {
    // update undo menu-item
    /*
    MenuItem undo = JPlanner.gui.actionUndo;
    if ( m_index > 0 )
    {
      undo.setText( "Undo " + undoText() + "\tCtrl+Z" );
      undo.setEnabled( true );
    }
    else
    {
      undo.setText( "Undo\tCtrl+Z" );
      undo.setEnabled( false );
    }
    
    // update redo menu-item
    MenuItem redo = JPlanner.gui.actionRedo;
    if ( m_index < size() )
    {
      redo.setText( "Redo " + redoText() + "\tCtrl+Y" );
      redo.setEnabled( true );
    }
    else
    {
      redo.setText( "Redo\tCtrl+Y" );
      redo.setEnabled( false );
    }
    
    // also update undo-stack window selected item if exists
    if ( JPlanner.gui.undoWindow != null )
      JPlanner.gui.undoWindow.updateSelection();
    
    // if clean state changed, update window titles
    if ( m_previousCleanState != isClean() )
      JPlanner.gui.updateWindowTitles();
    m_previousCleanState = isClean();
    */
  }

  /****************************************** undoText *******************************************/
  public String undoText()
  {
    // return text associated with next potential undo
    return m_stack.get( m_index - 1 ).text();
  }

  /****************************************** redoText *******************************************/
  public String redoText()
  {
    // return text associated with next potential redo
    return m_stack.get( m_index ).text();
  }

  /******************************************** text *********************************************/
  public String text( int index )
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
    updateUndoRedoMenuItems();
  }

  /******************************************** size *********************************************/
  public int size()
  {
    // return stack size
    return m_stack.size();
  }

  /******************************************* index *********************************************/
  public int index()
  {
    // return command index
    return m_index;
  }

  /****************************************** setIndex *******************************************/
  public void setIndex( int index )
  {
    // execute redo's or undo's as necessary to get to target index
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
    updateUndoRedoMenuItems();
  }

}
