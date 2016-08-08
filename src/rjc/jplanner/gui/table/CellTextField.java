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

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

/*************************************************************************************************/
/*********************** JavaFX TextField enhanced for table cell editing ************************/
/*************************************************************************************************/

public class CellTextField extends TextField
{
  private String m_allowed;  // regular expression defining text allowed to be entered
  private String m_valid;    // regular expression defining text not error
  private double m_minWidth; // minimum width for editor
  private double m_maxWidth; // maximum width for editor

  /**************************************** constructor ******************************************/
  public CellTextField()
  {
    // create text field enhanced for table cell editing
    super();
    setPadding( new Insets( 0, TableCanvas.CELL_PADDING, 0, TableCanvas.CELL_PADDING ) );

    // add listener to check new values
    textProperty().addListener( ( observable, oldText, newText ) ->
    {
      // ensure text is always allowed
      if ( m_allowed != null && !newText.matches( m_allowed ) )
        setText( oldText );

      // ensure error status is correct
      if ( m_valid != null )
        AbstractCellEditor.setError( !getText().matches( m_valid ), this );

      // increase width if needed to show whole text
      Text text = new Text( getText() );
      double width = text.getLayoutBounds().getWidth() + TableCanvas.CELL_PADDING * 3;
      if ( width < m_minWidth )
        width = m_minWidth;
      if ( width > m_maxWidth )
        width = m_maxWidth;
      if ( getWidth() != width )
      {
        setMinWidth( width );
        setMaxWidth( width );
      }
    } );

  }

  /****************************************** setValue *****************************************/
  public void setValue( String text )
  {
    // set text editor value
    setText( text );

    // place editor caret at end (in future so not overtaken other caret moving activities)
    Platform.runLater( () -> selectRange( text.length(), text.length() ) );
  }

  /****************************************** setAllowed *****************************************/
  public void setAllowed( String allowed )
  {
    // regular expression that limits what can be entered into editor
    m_allowed = allowed;
  }

  /****************************************** setValid *******************************************/
  public void setValid( String valid )
  {
    // if text match this regular expression, then not in error
    m_valid = valid;
  }

  /*************************************** calculateWidth ****************************************/
  public void calculateWidth( Table table, int columnIndex )
  {
    // determine editor maximum width
    int columnPos = table.getColumnPositionByIndex( columnIndex );
    m_maxWidth = table.getWidth() - table.getXStartByColumnPosition( columnPos ) + 1;

    // determine editor minimum width
    m_minWidth = table.getWidthByColumnIndex( columnIndex ) + 1;
    if ( m_minWidth > m_maxWidth )
      m_minWidth = m_maxWidth;
  }

}
