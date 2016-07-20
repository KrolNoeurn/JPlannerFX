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
  private static CellEditor m_cellEditorInProgress;

  private Table             m_table;
  private int               m_columnIndex;
  private int               m_rowIndex;
  private MoveDirection     m_moveDirection;

  private Control           m_focusControl;        // prime control that has focus
  private Region            m_overallEditor;       // overall editor can be different to control that takes focus

  public static enum MoveDirection// selection movement after committing an edit
  {
    LEFT, RIGHT, UP, DOWN, NONE
  }

  /***************************************** constructor *****************************************/
  public CellEditor( int columnIndex, int rowIndex )
  {
    // initialise private variables
    m_columnIndex = columnIndex;
    m_rowIndex = rowIndex;
  }

  /******************************************* getText *******************************************/
  abstract public String getText();

  /******************************************* setValue ******************************************/
  abstract public void setValue( Object value );

  /***************************************** endEditing ******************************************/
  public static void endEditing()
  {
    // if there is a currently active editor, close it
    if ( m_cellEditorInProgress != null )
      m_cellEditorInProgress.close( !m_cellEditorInProgress.isError() );
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

    // add listener to end editing if focus lost
    m_focusControl.focusedProperty().addListener( ( observable, oldF, newF ) ->
    {
      if ( !newF )
        endEditing();
    } );

    // add listener to close if escape or enter pressed
    m_focusControl.setOnKeyPressed( event ->
    {
      if ( event.getCode() == KeyCode.ESCAPE )
        close( false );
      if ( event.getCode() == KeyCode.ENTER && m_focusControl.getId() != JPlanner.ERROR )
        close( !isError() );
    } );
  }

  /*************************************** getfocusControl ***************************************/
  public Control getfocusControl()
  {
    // return focus control
    return m_focusControl;
  }

  /******************************************* isError *******************************************/
  public Boolean isError()
  {
    // return if editor in error state
    return m_overallEditor == null || m_overallEditor.getId() == JPlanner.ERROR;
  }

  /****************************************** setError *******************************************/
  public void setError( boolean error )
  {
    // set if editor is error state
    if ( error )
      m_overallEditor.setId( JPlanner.ERROR );
    else
      m_overallEditor.setId( null );
  }

  /******************************************** close ********************************************/
  public void close( boolean commit )
  {
    JPlanner.trace( "Closing editor", commit, m_overallEditor );

    // if requested commit new value to table data source & move focus
    if ( commit )
    {
      m_table.getDataSource().setValue( m_columnIndex, m_rowIndex, getText() );
      //m_table.moveFocus( m_moveDirection );
    }

    // remove editor from table
    m_table.remove( m_overallEditor );
    m_overallEditor = null;
    m_cellEditorInProgress = null;
  }

  /********************************************* open ********************************************/
  public void open( Table table, int columnPos, int rowPos, Object value, MoveDirection move )
  {
    // check editor set
    if ( m_overallEditor == null )
      throw new IllegalStateException( "Cell editor not set " + table + " " + columnPos + " " + rowPos );

    // set editor position & size
    m_table = table;
    m_moveDirection = move;

    int w = m_table.getWidthByColumnIndex( m_columnIndex ) + 1;
    int h = m_table.getHeightByRowIndex( m_rowIndex ) + 1;
    m_overallEditor.setLayoutX( m_table.getXStartByColumnPosition( columnPos ) - 1 );
    m_overallEditor.setLayoutY( m_table.getYStartByRowPosition( rowPos ) - 1 );
    m_overallEditor.setMaxSize( w, h );
    m_overallEditor.setMinSize( w, h );

    // set editor value
    if ( value != null )
      setValue( value );
    else
      setValue( m_table.getDataSource().getValue( m_columnIndex, m_rowIndex ) );

    // display editor and give focus
    m_cellEditorInProgress = this;
    m_table.add( m_overallEditor );
    m_focusControl.requestFocus();
  }

}
