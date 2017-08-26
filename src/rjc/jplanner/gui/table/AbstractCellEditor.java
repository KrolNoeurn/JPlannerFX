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

package rjc.jplanner.gui.table;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Control;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import rjc.jplanner.JPlanner;
import rjc.jplanner.gui.DateEditor;
import rjc.jplanner.gui.DateTimeEditor;
import rjc.jplanner.gui.SpinEditor;
import rjc.jplanner.gui.XTextField;

/*************************************************************************************************/
/********************** Abstract gui control for editing a table cell value **********************/
/*************************************************************************************************/

abstract public class AbstractCellEditor
{
  private static AbstractCellEditor m_cellEditorInProgress;

  private Table                     m_table;
  private int                       m_columnIndex;
  private int                       m_row;

  private Control                   m_control;             // prime control that has focus

  public static enum MoveDirection// selection movement after committing an edit
  {
    LEFT, RIGHT, UP, DOWN, NONE
  }

  abstract public Object getValue(); // return cell editor value

  abstract public void setValue( Object value ); // set cell editor value

  /***************************************** constructor *****************************************/
  public AbstractCellEditor( int columnIndex, int row )
  {
    // initialise private variables
    m_columnIndex = columnIndex;
    m_row = row;
  }

  /***************************************** endEditing ******************************************/
  public static void endEditing()
  {
    // if there is a currently active editor, close it
    if ( m_cellEditorInProgress != null )
      m_cellEditorInProgress.close( !m_cellEditorInProgress.isError() );
  }

  /***************************************** setControl ******************************************/
  public void setControl( Control control )
  {
    // set focus control
    m_control = control;

    // add listener to end editing if focus lost
    m_control.focusedProperty().addListener( ( observable, oldFocus, newFocus ) ->
    {
      if ( !newFocus )
        endEditing();
    } );

    // add key press event handler to close if escape or enter pressed
    m_control.addEventHandler( KeyEvent.KEY_PRESSED, event ->
    {
      if ( event.getCode() == KeyCode.ESCAPE )
        close( false ); // abandon edit
      if ( event.getCode() == KeyCode.ENTER && !isError() )
        close( true ); // commit edit
    } );
  }

  /***************************************** getControl ******************************************/
  public Control getControl()
  {
    // return focus control
    return m_control;
  }

  /***************************************** addListener *****************************************/
  public void addListener( ChangeListener<? super java.lang.String> listener )
  {
    // add listener to text property
    ( (XTextField) m_control ).textProperty().addListener( listener );
  }

  /*************************************** getColumnIndex ****************************************/
  public int getColumnIndex()
  {
    // return column index
    return m_columnIndex;
  }

  /***************************************** getRowIndex *****************************************/
  public int getRowIndex()
  {
    // return row index
    return m_row;
  }

  /******************************************* isError *******************************************/
  public Boolean isError()
  {
    // return if editor in error state
    return JPlanner.isError( m_control );
  }

  /******************************************** close ********************************************/
  public void close( boolean commit )
  {
    // clear any error message, remove control from table, and give focus back to table
    m_cellEditorInProgress = null;
    JPlanner.setNoError( m_control, "" );
    m_table.remove( m_control );
    m_table.requestFocus();
    m_table.setOnScroll( event -> m_table.scrollEvent( event ) );

    // if commit requested, save new value to data source
    if ( commit )
      m_table.getData().setValue( m_columnIndex, m_row, getValue() );
  }

  /********************************************* open ********************************************/
  public void open( Table table, Object value )
  {
    // check editor is set
    if ( m_control == null )
      throw new IllegalStateException( "Editor control not set" );

    // set editor position & size
    m_table = table;

    int w = m_table.getWidthByColumnIndex( m_columnIndex ) + 1;
    int h = m_table.getRowHeight( m_row ) + 1;
    int columnPos = m_table.getColumnPositionByIndex( m_columnIndex );

    m_control.setLayoutX( m_table.getXStartByColumnPosition( columnPos ) - 1 );
    m_control.setLayoutY( m_table.getYStartByRow( m_row ) - 1 );
    m_control.setMaxSize( w, h );
    m_control.setMinSize( w, h );

    // display editor and give focus
    m_cellEditorInProgress = this;
    m_table.add( m_control );
    m_table.layout();
    m_control.requestFocus();

    // if control derived from XTextField then set min & max width
    if ( m_control instanceof XTextField )
    {
      double max = table.getWidth() - table.getXStartByColumnPosition( columnPos ) + 1;
      double min = table.getWidthByColumnIndex( m_columnIndex ) + 1;
      if ( min > max )
        min = max;
      ( (XTextField) m_control ).setPadding( new Insets( 0, TableCanvas.CELL_PADDING, 0, TableCanvas.CELL_PADDING ) );
      ( (XTextField) m_control ).setWidths( min, max );
    }

    // if control derived from SpinEditor add wheel scroll listener
    if ( m_control instanceof SpinEditor )
      table.setOnScroll( event -> ( (SpinEditor) m_control ).scrollEvent( event ) );
    if ( m_control instanceof DateEditor )
      table.setOnScroll( event -> ( (DateEditor) m_control ).scrollEvent( event ) );
    if ( m_control instanceof DateTimeEditor )
      table.setOnScroll( event -> ( (DateTimeEditor) m_control ).scrollEvent( event ) );

    // set editor value
    if ( value != null )
      setValue( value );
    else
      setValue( getDataSourceValue() );
  }

  /************************************* getDataSourceValue **************************************/
  public Object getDataSourceValue()
  {
    // return value for table cell from table data source
    return m_table.getData().getValue( m_columnIndex, m_row );
  }

  /***************************************** isValueValid ****************************************/
  protected boolean isValueValid( Object value )
  {
    // return true only if value valid for editor - default true
    return true;
  }

}
