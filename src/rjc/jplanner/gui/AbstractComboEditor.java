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
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/*************************************************************************************************/
/******************* Abstract JavaFX control to allow user to pick from list *********************/
/*************************************************************************************************/

public abstract class AbstractComboEditor extends XTextField
{
  private Canvas           m_button          = new Canvas();
  private ComboDropDown    m_dropdown;
  private int              m_selectedIndex   = -1;

  private static final int BUTTONS_WIDTH_MAX = 16;
  private static final int BUTTONS_PADDING   = 2;

  abstract public int getItemCount(); // return number of items user can choose from

  abstract public String getItem( int num ); // return n'th item

  /**************************************** constructor ******************************************/
  public AbstractComboEditor()
  {
    // construct combo box
    super();
    setEditable( false );

    // when combo box changes size re-draw button
    heightProperty().addListener( ( property, oldHeight, newHeight ) -> drawButton() );
    widthProperty().addListener( ( property, oldWidth, newWidth ) -> drawButton() );

    // react to key presses and mouse clicks
    setOnKeyPressed( event -> keyPressed( event ) );
    setOnKeyTyped( event -> keyTyped( event ) );
    setOnMousePressed( event -> mousePressed( event ) );
    m_button.setOnMousePressed( event -> mousePressed( event ) );
  }

  /************************************** setSelectedIndex ***************************************/
  public void setSelectedIndex( int index )
  {
    // set selected index and update displayed text to match
    m_selectedIndex = index;
    setText( getItem( index ) );
    if ( m_dropdown != null )
      m_dropdown.redrawCanvas();
  }

  /************************************** getSelectedIndex ***************************************/
  public int getSelectedIndex()
  {
    // return currently selected index
    return m_selectedIndex;
  }

  /************************************** setSelectionText ***************************************/
  public void setSelectionText( String text )
  {
    // set displayed selection text and set selected index to found match
    setText( text );

    m_selectedIndex = -1;
    int size = getItemCount();
    for ( int i = 0; i < size; i++ )
      if ( text.equals( getItem( i ) ) )
      {
        m_selectedIndex = i;
        if ( m_dropdown != null )
          m_dropdown.redrawCanvas();
        break;
      }
  }

  /****************************************** keyTyped *******************************************/
  private void keyTyped( KeyEvent event )
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
  private void keyPressed( KeyEvent event )
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

  /**************************************** mousePressed ****************************************/
  private void mousePressed( MouseEvent event )
  {
    // request focus for display text field
    Platform.runLater( () -> requestFocus() );

    // open drop-down list
    m_dropdown = new ComboDropDown( this );
    m_dropdown.setOnHiding( hideEvent -> m_dropdown = null );
  }

  /****************************************** drawButton *****************************************/
  private void drawButton()
  {
    // determine size and draw button
    GraphicsContext gc = m_button.getGraphicsContext2D();
    double h = getHeight() - 2 * BUTTONS_PADDING;
    double w = getWidth() / 2;
    if ( w > BUTTONS_WIDTH_MAX )
      w = BUTTONS_WIDTH_MAX;
    m_button.setHeight( h );
    m_button.setWidth( w );

    // set editor insets and buttons position
    setPadding( new Insets( 0, w + PADDING, 0, PADDING ) );
    m_button.setLayoutX( getLayoutX() + getWidth() - w - BUTTONS_PADDING );
    m_button.setLayoutY( getLayoutY() + BUTTONS_PADDING );

    // fill background
    gc.setFill( MainWindow.BUTTON_BACKGROUND );
    gc.fillRect( 0.0, 0.0, w, h );

    // draw down arrow
    gc.setStroke( MainWindow.BUTTON_ARROW );
    int x1 = (int) ( w * 0.3 + 0.5 );
    int y1 = (int) ( h * 0.3 + 0.5 );
    int y2 = (int) ( h - y1 );
    for ( int y = y1; y <= y2; y++ )
    {
      double x = x1 + ( w * 0.5 - x1 ) / ( y2 - y1 ) * ( y - y1 );
      gc.strokeLine( x, y + .5, w - x, y + .5 );
    }
  }

}
