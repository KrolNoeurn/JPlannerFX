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
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/*************************************************************************************************/
/******************* Abstract JavaFX control to allow user to pick from list *********************/
/*************************************************************************************************/

public abstract class AbstractCombo extends StackPane
{
  private TextField          m_displayText     = new TextField();
  private Canvas             m_button          = new Canvas();
  private DropDown           m_dropdown;
  private int                m_selectedIndex   = -1;

  private static final Color BUTTON_BACKGROUND = Color.rgb( 225, 225, 225 ); // light gray
  private static final Color BUTTON_ARROW      = Color.BLACK;

  /**************************************** constructor ******************************************/
  public AbstractCombo()
  {
    // construct combo box
    super();
    m_displayText.setEditable( false );
    getChildren().addAll( m_displayText, m_button );
    StackPane.setAlignment( m_button, Pos.CENTER_RIGHT );

    // when combo box changes size re-draw button
    heightProperty().addListener( ( property, oldHeight, newHeight ) -> drawButton() );
    widthProperty().addListener( ( property, oldWidth, newWidth ) -> drawButton() );

    // react to key presses and mouse clicks
    m_displayText.setOnKeyPressed( event -> keyPressed( event ) );
    m_displayText.setOnKeyTyped( event -> keyTyped( event ) );
    m_displayText.setOnMousePressed( event -> mousePressed( event ) );
    m_button.setOnMousePressed( event -> mousePressed( event ) );
  }

  /**************************************** getItemCount *****************************************/
  abstract public int getItemCount();

  /******************************************* getItem *******************************************/
  abstract public String getItem( int num );

  /************************************** setSelectedIndex ***************************************/
  public void setSelectedIndex( int index )
  {
    // set selected index and update displayed text to match
    m_selectedIndex = index;
    m_displayText.setText( getItem( index ) );
    if ( m_dropdown != null )
      m_dropdown.redrawCanvas();
  }

  /************************************** getSelectedIndex ***************************************/
  public int getSelectedIndex()
  {
    // return currently selected index
    return m_selectedIndex;
  }

  /******************************************* setText *******************************************/
  public void setText( String text )
  {
    // set displayed text and set selected index to found match
    m_displayText.setText( text );

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

  /******************************************* getText *******************************************/
  public String getText()
  {
    // return displayed text
    return m_displayText.getText();
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
    Platform.runLater( () -> m_displayText.requestFocus() );

    // open drop-down list
    m_dropdown = new DropDown( this );
    m_dropdown.setOnHiding( hideEvent -> m_dropdown = null );
  }

  /****************************************** drawButton *****************************************/
  private void drawButton()
  {
    // determine size and draw button
    GraphicsContext gc = m_button.getGraphicsContext2D();
    double h = getHeight() - 4;
    double w = getWidth() / 2;
    if ( w > h )
      w = h;
    m_button.setHeight( h );
    m_button.setWidth( w );

    // fill background
    gc.clearRect( 0.0, 0.0, w, h );
    gc.setFill( BUTTON_BACKGROUND );
    w -= 2;
    gc.fillRect( 0.0, 0.0, w, h );

    // draw down arrow
    gc.setStroke( BUTTON_ARROW );
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
