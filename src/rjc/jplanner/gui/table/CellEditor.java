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

import javafx.scene.control.Control;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import rjc.jplanner.JPlanner;

/*************************************************************************************************/
/********************** Abstract gui control for editing a table cell value **********************/
/*************************************************************************************************/

public abstract class CellEditor
{
  public static CellEditor cellEditorInProgress;

  private Table            m_table;
  private int              m_columnPos;
  private int              m_rowPos;
  private Control          m_focusControl;      // prime control that has focus
  private Region           m_overallEditor;     // overall editor can be different to control that takes focus

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
    JPlanner.trace( "ENDING CELL EDITING ", m_focusControl.getId() );

    if ( m_overallEditor.getId() == JPlanner.ERROR )
      close();
    else
      commit( MoveDirection.NONE );
  }

  /****************************************** setEditor ******************************************/
  public void setEditor( Control focusControl )
  {
    // simple case where overall editor and prime control are one and the same
    setEditor( focusControl, focusControl );
  }

  /****************************************** setEditor ******************************************/
  public void setEditor( Region overall, Control focusControl )
  {
    // set overall editor and focus control
    m_overallEditor = overall;
    m_focusControl = focusControl;
  }

  /*************************************** getfocusControl ***************************************/
  public Control getfocusControl()
  {
    return m_focusControl;
  }

  /******************************************* getText *******************************************/
  abstract String getText();

  /******************************************* setValue ******************************************/
  abstract void setValue( Object value );

  /******************************************* commit ********************************************/
  public void commit( MoveDirection move )
  {
    // update date source with new value
    int columnIndex = m_table.getColumnIndexByPosition( m_columnPos );
    int rowIndex = m_table.getRowIndexByPosition( m_rowPos );
    m_table.getDataSource().setValue( columnIndex, rowIndex, getText() );
    close();

    // TODO Move !!!!!!!!!!!!!
  }

  /******************************************** close ********************************************/
  public void close()
  {
    // close editor and set to error state so losing focus does not commit
    m_overallEditor.setId( JPlanner.ERROR );
    m_table.remove( m_overallEditor );
    cellEditorInProgress = null;
  }

  /********************************************* open ********************************************/
  public void open( Table table, int columnPos, int rowPos, Object value )
  {
    // set cell editor position & size 
    m_table = table;
    m_columnPos = columnPos;
    m_rowPos = rowPos;
    int columnIndex = m_table.getColumnIndexByPosition( columnPos );
    int rowIndex = m_table.getRowIndexByPosition( rowPos );
    int w = m_table.getWidthByColumnIndex( columnIndex ) + 1;
    int h = m_table.getHeightByRowIndex( rowIndex ) + 1;
    m_overallEditor.setLayoutX( m_table.getXStartByColumnPosition( m_columnPos ) - 1 );
    m_overallEditor.setLayoutY( m_table.getYStartByRowPosition( m_rowPos ) - 1 );
    m_overallEditor.setMaxSize( w, h );
    m_overallEditor.setMinSize( w, h );

    // set value
    if ( value != null )
      setValue( value );
    else
      setValue( m_table.getDataSource().getValue( columnIndex, rowIndex ) );

    // display and request focus
    table.add( m_overallEditor );
    m_focusControl.requestFocus();

    // add listeners for behaviour
    m_focusControl.focusedProperty().addListener( ( observable, oldF, newF ) -> endEditing() );
    m_focusControl.setOnKeyPressed( event ->
    {
      if ( event.getCode() == KeyCode.ESCAPE )
        close();
      if ( event.getCode() == KeyCode.ENTER && m_focusControl.getId() != JPlanner.ERROR )
        commit( MoveDirection.NONE );
    } );
  }

}
