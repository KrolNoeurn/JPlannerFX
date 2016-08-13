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

package rjc.jplanner.gui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

/*************************************************************************************************/
/*********************************** Enhanced JavaFX TextField ***********************************/
/*************************************************************************************************/

public class XTextField extends TextField
{
  private String          m_allowed;   // regular expression defining text allowed to be entered
  private String          m_valid;     // regular expression defining text not error
  private double          m_minWidth;  // minimum width for editor
  private double          m_maxWidth;  // maximum width for editor

  public static final int PADDING = 4; // padding for text left & right edges

  /**************************************** constructor ******************************************/
  public XTextField()
  {
    // create enhanced text field control
    super();
    setPadding( new Insets( 0, PADDING, 0, PADDING ) );

    // add listener to check new values
    textProperty().addListener( ( observable, oldText, newText ) ->
    {
      // ensure text is always allowed
      if ( m_allowed != null && !newText.matches( m_allowed ) )
        setText( oldText );

      // ensure error status is correct
      if ( m_valid != null )
        MainWindow.setError( !getText().matches( m_valid ), this );

      // increase width if needed to show whole text
      Text text = new Text( getText() );
      double width = text.getLayoutBounds().getWidth() + getPadding().getLeft() + getPadding().getRight() + PADDING;
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

  /***************************************** setTextCaret ****************************************/
  public void setTextCaret( String text, int caretPos )
  {
    // set editor text value (cannot override final TextField setText method)
    setText( text );

    // place editor caret (in future so not overtaken other caret moving activities)
    Platform.runLater( () -> selectRange( caretPos, caretPos ) );
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

  /****************************************** setWidths ******************************************/
  public void setWidths( double min, double max )
  {
    // set editor minimum and maximum width
    m_minWidth = min;
    m_maxWidth = max;
  }

}
