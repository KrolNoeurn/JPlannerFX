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

package rjc.jplanner.gui.table;

import javafx.event.ActionEvent;
import javafx.scene.control.Control;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import rjc.jplanner.JPlanner;

/*************************************************************************************************/
/********************** Abstract gui control for editing a table cell value **********************/
/*************************************************************************************************/

public abstract class CellEditor
{
  public static CellEditor cellEditorInProgress;

  private boolean          m_error;
  private Control          m_prime;             // prime control that has focus
  private Region           m_editor;            // overall editor can be different to prime control that takes focus

  public static enum MoveDirection// selection movement after committing an edit
  {
    LEFT, RIGHT, UP, DOWN, NONE
  }

  /***************************************** constructor *****************************************/
  public CellEditor()
  {
    // initialise private variables
    cellEditorInProgress = this;
  }

  /***************************************** endEditing ******************************************/
  public void endEditing()
  {
    // close or commit editor depending if in error state
    if ( m_error )
      close();
    else
      commit( MoveDirection.NONE );
  }

  /****************************************** setEditor ******************************************/
  public void setEditor( Control prime )
  {
    // simple case where overall editor and prime control are the same
    setEditor( prime, prime );
  }

  /****************************************** setEditor ******************************************/
  public void setEditor( Region editor, Control prime )
  {
    // set overall editor and prime control
    m_editor = editor;
    m_prime = prime;

    // set location
  }

  /****************************************** getPrime *******************************************/
  public Control getPrime()
  {
    return m_prime;
  }

  /****************************************** getColumn ******************************************/
  public int getColumn()
  {
    return 1;
  }

  /******************************************* getRow ********************************************/
  public int getRow()
  {
    return 1;
  }

  /******************************************* getText *******************************************/
  abstract String getText();

  /******************************************* commit ********************************************/
  public void commit( MoveDirection move )
  {
    // update date source with new value
    //getData().setValue( getColumn(), getRow(), getText() );
    close();
  }

  /******************************************** close ********************************************/
  public void close()
  {
    // close editor by removing from table
    //m_body.getChildren().remove( m_editor );
    cellEditorInProgress = null;
  }

  /***************************************** keyPressed ******************************************/
  private void keyPressed( KeyEvent event )
  {
    // TODO Auto-generated method stub
    JPlanner.trace( "KEYEVENT " + event );

    if ( event.getCode() == KeyCode.TAB )
      event.consume();
  }

  /******************************************* action ********************************************/
  private void action( ActionEvent event )
  {
    // TODO Auto-generated method stub
    JPlanner.trace( "ACTIONEVENT " + event );
  }

  /**************************************** focusChange ******************************************/
  private void focusChange()
  {
    // handle loss of focus
    if ( getPrime().focusedProperty().get() == false )
    {
      close();
      JPlanner.trace( "CLOSING EDITOR " + getText() );
    }
  }

}
