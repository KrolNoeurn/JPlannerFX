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

package rjc.jplanner.gui;

import javafx.scene.input.KeyEvent;

/*************************************************************************************************/
/******************* Abstract JavaFX control to allow user to pick from list *********************/
/*************************************************************************************************/

public abstract class AbstractComboEditor extends XTextField
{
  private ComboDropDown m_dropdown;
  private int           m_selectedIndex = -1;

  abstract public int getItemCount(); // return number of items user can choose from

  abstract public String getItem( int num ); // return n'th item

  /**************************************** constructor ******************************************/
  public AbstractComboEditor()
  {
    // construct combo box
    super();
    setEditable( false );
    setButtonType( ButtonType.DOWN );
    m_dropdown = new ComboDropDown( this );

    // react to key presses
    setOnKeyPressed( event -> keyPressed( event ) );
    setOnKeyTyped( event -> keyTyped( event ) );

    // react to text changes to set selected index
    textProperty().addListener( ( property, oldValue, newValue ) ->
    {
      // if index not correct, search for new index
      int size = getItemCount();
      if ( m_selectedIndex < 0 || m_selectedIndex >= size || !newValue.equals( getItem( m_selectedIndex ) ) )
      {
        m_selectedIndex = -1;
        for ( int i = 0; i < size; i++ )
          if ( newValue.equals( getItem( i ) ) )
          {
            m_selectedIndex = i;
            m_dropdown.redrawCanvas();
            break;
          }
      }
    } );
  }

  /************************************** setSelectedIndex ***************************************/
  public void setSelectedIndex( int index )
  {
    // set selected index and update displayed text to match
    m_selectedIndex = index;
    setText( getItem( index ) );
    m_dropdown.redrawCanvas();
  }

  /************************************** getSelectedIndex ***************************************/
  public int getSelectedIndex()
  {
    // return currently selected index
    return m_selectedIndex;
  }

  /****************************************** keyTyped *******************************************/
  public void keyTyped( KeyEvent event )
  {
    // find next item that starts with typed key (case-insensitive)
    String key = event.getCharacter().substring( 0, 1 ).toLowerCase();
    int size = getItemCount();
    for ( int i = 1; i < size; i++ )
    {
      String itemFirstChar = getItem( ( m_selectedIndex + i ) % size ).substring( 0, 1 ).toLowerCase();
      if ( key.equals( itemFirstChar ) )
      {
        setSelectedIndex( ( m_selectedIndex + i ) % size );
        break;
      }
    }
  }

  /***************************************** keyPressed ******************************************/
  protected void keyPressed( KeyEvent event )
  {
    // action key press to change current selected item
    switch ( event.getCode() )
    {
      case DOWN:
      case RIGHT:
      case PAGE_DOWN:
        setSelectedIndex( ( m_selectedIndex + 1 ) % getItemCount() );
        break;
      case UP:
      case LEFT:
      case PAGE_UP:
        setSelectedIndex( ( m_selectedIndex - 1 + getItemCount() ) % getItemCount() );
        break;
      case HOME:
        setSelectedIndex( 0 );
        break;
      case END:
        setSelectedIndex( getItemCount() - 1 );
        break;
      default:
        break;
    }
  }

}
