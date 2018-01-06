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

package rjc.jplanner.gui.plan;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import rjc.jplanner.JPlanner;
import rjc.jplanner.command.CommandPlanSetNotes;

/*************************************************************************************************/
/**************************** Widget display & editing of plan notes *****************************/
/*************************************************************************************************/

public class PlanNotes extends GridPane
{
  private TextArea m_notes = new TextArea();

  /**************************************** constructor ******************************************/
  public PlanNotes()
  {
    // setup notes panel
    setVgap( 5.0 );
    setPadding( new Insets( 5.0 ) );
    setMinWidth( 0.0 );

    // notes label
    add( new Label( "Notes" ), 0, 0 );

    // notes area
    m_notes.setWrapText( true );
    add( m_notes, 0, 1 );

    // notes area should grow to fill all available space
    setHgrow( m_notes, Priority.ALWAYS );
    setVgrow( m_notes, Priority.ALWAYS );
  }

  /****************************************** getText ********************************************/
  public String getText()
  {
    return m_notes.getText();
  }

  /****************************************** setText ********************************************/
  public void setText( String txt )
  {
    m_notes.setText( txt );
  }

  /*************************************** updateFromPlan ****************************************/
  public void updateFromPlan()
  {
    // update the gui notes with text from plan
    m_notes.setText( JPlanner.plan.getNotes() );
  }

  /************************************** updatePlanNotes ****************************************/
  public void updatePlan()
  {
    // if notes not changed, return doing nothing, otherwise update via undo-stack command
    if ( JPlanner.plan.getNotes().equals( m_notes.getText() ) )
      return;

    JPlanner.plan.getUndostack().push( new CommandPlanSetNotes( m_notes.getText() ) );
  }

}
