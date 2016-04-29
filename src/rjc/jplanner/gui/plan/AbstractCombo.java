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

package rjc.jplanner.gui.plan;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import rjc.jplanner.JPlanner;

/*************************************************************************************************/
/******************* Abstract JavaFX control to allow user to pick from list *********************/
/*************************************************************************************************/

public abstract class AbstractCombo extends StackPane
{
  private TextField           m_display = new TextField();
  private Canvas              m_button  = new Canvas();
  protected ArrayList<String> m_items   = new ArrayList<String>();
  protected int               m_index   = -1;

  private static Color        LIGHTGRAY = Color.rgb( 225, 225, 225 );

  /**************************************** constructor ******************************************/
  public AbstractCombo()
  {
    // construct combo box
    super();
    m_display.setEditable( false );
    getChildren().addAll( m_display, m_button );
    StackPane.setAlignment( m_button, Pos.CENTER_RIGHT );

    // when display changes size re-draw button
    m_display.heightProperty().addListener( ( observable, oldValue, newValue ) -> drawButton() );
    m_display.widthProperty().addListener( ( observable, oldValue, newValue ) -> drawButton() );

    // when display gains focus refresh the items list
    m_display.focusedProperty().addListener( ( observable, oldValue, newValue ) -> focusChanged( newValue ) );

    // when .............
    m_display.setOnKeyPressed( event -> keyPressed( event ) );
    m_display.setOnKeyTyped( event -> keyTyped( event ) );
    m_display.setOnMousePressed( event -> mousePressed( event ) );
    m_button.setOnMousePressed( event -> mousePressed( event ) );
  }

  /***************************************** refreshList *****************************************/
  abstract void refreshList();

  /**************************************** focusChanged *****************************************/
  private void focusChanged( Boolean focus )
  {
    // if display has gained focus refresh items list
    if ( focus )
    {
      refreshList();
      m_index = m_items.indexOf( m_display.getText() );
    }
  }

  /****************************************** keyTyped *******************************************/
  private void keyTyped( KeyEvent event )
  {
    // find next item that starts with typed key case insensitive
    String key = event.getCharacter().substring( 0, 1 ).toLowerCase();
    int size = m_items.size();
    for ( int i = 1; i < size; i++ )
    {
      String item = m_items.get( ( m_index + i ) % size ).substring( 0, 1 ).toLowerCase();
      if ( key.equals( item ) )
      {
        m_index = ( m_index + i ) % size;
        m_display.setText( m_items.get( m_index ) );
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
        m_index = ( m_index + 1 ) % m_items.size();
        m_display.setText( m_items.get( m_index ) );
        break;
      case UP:
      case LEFT:
      case PAGE_UP:
        m_index = ( m_index - 1 + m_items.size() ) % m_items.size();
        m_display.setText( m_items.get( m_index ) );
        break;
      case HOME:
        m_index = 0;
        m_display.setText( m_items.get( m_index ) );
        break;
      case END:
        m_index = m_items.size() - 1;
        m_display.setText( m_items.get( m_index ) );
        break;
      default:
        break;
    }
  }

  /**************************************** mousePressed ****************************************/
  private void mousePressed( MouseEvent event )
  {
    // TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    JPlanner.trace( "Mouse pressed" );

    Platform.runLater( new Runnable()
    {
      @Override
      public void run()
      {
        m_display.requestFocus();
      }
    } );

    Dropdown dd = new Dropdown( this );
  }

  /****************************************** drawButton *****************************************/
  private void drawButton()
  {
    // size and draw button
    GraphicsContext gc = m_button.getGraphicsContext2D();
    double h = m_display.getHeight() - 4;
    double w = m_display.getWidth() / 2;
    if ( w > h )
      w = h;
    m_button.setHeight( h );
    m_button.setWidth( w );

    // fill background
    gc.clearRect( 0.0, 0.0, w, h );
    gc.setFill( LIGHTGRAY );
    w -= 2;
    gc.fillRect( 0.0, 0.0, w, h );

    // draw down arrow
    gc.setStroke( Color.BLACK );
    int x1 = (int) ( w * 0.3 + 0.5 );
    int y1 = (int) ( h * 0.3 + 0.5 );
    int y2 = (int) ( h - y1 );
    for ( int y = y1; y <= y2; y++ )
    {
      double x = x1 + ( w * 0.5 - x1 ) / ( y2 - y1 ) * ( y - y1 );
      gc.strokeLine( x, y + .5, w - x, y + .5 );
    }
  }

  /******************************************* setText *******************************************/
  public void setText( String text )
  {
    // set displayed text and update index to match
    m_display.setText( text );
    m_index = m_items.indexOf( text );
  }

  /******************************************* getText *******************************************/
  public String getText()
  {
    // get displayed index
    return m_display.getText();
  }

  /****************************************** setIndex *******************************************/
  public void setIndex( int index )
  {
    // set index and update displayed text to match
    m_display.setText( m_items.get( index ) );
    m_index = index;
  }

  /****************************************** getIndex *******************************************/
  public int getIndex()
  {
    // get index
    return m_index;
  }

}
