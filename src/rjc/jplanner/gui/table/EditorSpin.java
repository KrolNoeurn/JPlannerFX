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

import javafx.scene.layout.StackPane;

/*************************************************************************************************/
/****************************** Table cell editor for number fields ******************************/
/*************************************************************************************************/

public class EditorSpin extends CellEditor
{
  private StackPane     m_stack;  // overall editor that includes buttons and text field
  private CellTextField m_editor; // text field part of editor

  private double        m_min;    // minimum number allowed
  private double        m_max;    // maximum number allowed
  private int           m_dp;     // number of digits after decimal point

  private String        m_prefix;
  private String        m_suffix;

  /**************************************** constructor ******************************************/
  public EditorSpin( int columnIndex, int rowIndex )
  {
    // create spin editor
    super( columnIndex, rowIndex );

    // stack of text editor and buttons
    m_stack = new StackPane();
    m_editor = new CellTextField();
    m_stack.getChildren().add( m_editor );
    setRange( 0.0, 999.0, 0 );

    // add listener to ensure error status is correct
    m_editor.textProperty().addListener( ( observable, oldText, newText ) ->
    {
      double num = getNumber();
      boolean error = num < m_min || num > m_max || getText().length() < 1;
      CellEditor.setError( error, m_editor );
    } );

    // lock x-layout to 0 to prevent any shudder or incorrect placing
    m_editor.layoutXProperty().addListener( ( observable, oldValue, newValue ) -> m_editor.setLayoutX( 0.0 ) );

    setEditor( m_stack, m_editor );
  }

  /******************************************* getText *******************************************/
  @Override
  public String getText()
  {
    // get editor text

    return m_editor.getText();
  }

  /******************************************* setValue ******************************************/
  @Override
  public void setValue( Object value )
  {
    // set editor text
    m_editor.setValue( (String) value );
  }

  /******************************************** open *********************************************/
  @Override
  public void open( Table table, Object value, MoveDirection move )
  {
    // open editor
    m_editor.calculateWidth( table, getColumnIndex() );
    super.open( table, value, move );
  }

  /****************************************** getNumber ******************************************/
  public double getNumber()
  {
    // get editor text converted to number
    try
    {
      return Double.parseDouble( getText() );
    }
    catch ( Exception exception )
    {
      return 0.0;
    }

  }

  /******************************************* setRange ******************************************/
  public void setRange( double min, double max, int dp )
  {
    // check inputs
    if ( min > max )
      throw new IllegalArgumentException( "Min greater than max! " + min + " " + max );
    if ( dp < 0 || dp > 8 )
      throw new IllegalArgumentException( "Digits after deciminal place out of 0-8 range! " + dp );

    // set range and number of digits after decimal point
    m_min = min;
    m_max = max;
    m_dp = dp;

    // determine regular expression defining text allowed to be entered
    StringBuilder allow = new StringBuilder( 32 );
    if ( min < 0.0 )
      allow.append( "-?" );
    allow.append( "\\d*" );
    if ( dp > 0 )
      allow.append( "\\.?\\d{0," + dp + "}" );
    m_editor.setAllowed( allow.toString() );
  }

}
