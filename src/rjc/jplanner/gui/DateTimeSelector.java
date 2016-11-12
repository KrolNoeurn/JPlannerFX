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

import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import rjc.jplanner.JPlanner;

/*************************************************************************************************/
/********************* Pop-up window to display date-time selection widgets **********************/
/*************************************************************************************************/

public class DateTimeSelector extends Popup
{
  private Pane                m_pane     = new Pane();           // contents of pop-up
  private HBox                m_date     = new HBox();           // for entering month + year
  private HBox                m_time     = new HBox();           // for entering hours + mins + secs + millisecs
  private Canvas              m_calendar = new Canvas();         // for picking date
  private HBox                m_buttons  = new HBox();           // for today + now + start + end buttons

  private static final double PADDING    = 3.0;
  private static final Insets INSETS     = new Insets( PADDING );

  /**************************************** constructor ******************************************/
  public DateTimeSelector( DateTimeEditor parent )
  {
    // create pop-up window to display date-time selection widgets
    super();
    setAutoHide( true );
    setConsumeAutoHidingEvents( false );

    // determine pop-up position
    Point2D point = parent.localToScreen( 0.0, parent.getHeight() );
    setX( point.getX() );
    setY( point.getY() );

    // create pop-up contents and show
    createContents();
    getContent().add( m_pane );
    show( parent.getScene().getWindow() );
    positionContents();
  }

  /*************************************** createContents ****************************************/
  private void createContents()
  {
    // define background and border for the selector
    Background BACKGROUND = new Background( new BackgroundFill( Colors.GENERAL_BACKGROUND, null, null ) );
    Border BORDER = new Border( new BorderStroke( Colors.FOCUSED_BLUE, BorderStrokeStyle.SOLID, null, null ) );

    // create spin editors for entering time hh:mm:ss:mmm
    m_time.setPadding( INSETS );
    m_time.setSpacing( PADDING );
    SpinEditor hours = createSpinEditor( 0, 24, 50 );
    SpinEditor mins = createSpinEditor( 0, 60, 50 );
    SpinEditor secs = createSpinEditor( 0, 60, 50 );
    SpinEditor millisecs = createSpinEditor( 0, 999, 70 );
    m_time.getChildren().addAll( hours, mins, secs, millisecs );

    // create spin editors for entering date month-year
    Button monthPrevious = new Button( "<" );
    Button month = new Button( "November" );
    Button monthNext = new Button( ">" );
    Button yearPrevious = new Button( "<" );
    Button year = new Button( "2016" );
    Button yearNext = new Button( ">" );
    m_date.getChildren().addAll( monthPrevious, month, monthNext, yearPrevious, year, yearNext );

    // create calendar canvas
    m_calendar.setHeight( 100.0 );
    JPlanner.trace( m_calendar.getWidth(), m_calendar.getHeight() );

    m_pane.getChildren().addAll( m_date, m_calendar, m_time, m_buttons );
    m_pane.setBackground( BACKGROUND );
    m_pane.setBorder( BORDER );
  }

  /************************************* createSpinEditor ****************************************/
  private SpinEditor createSpinEditor( int min, int max, int width )
  {
    // create a spin-editor
    SpinEditor spin = new SpinEditor();
    spin.setRange( min, max, 0 );
    spin.setPrefWidth( width );
    spin.setMinHeight( 17.0 );
    spin.setMaxHeight( 17.0 );
    return spin;
  }

  /************************************** positionContents ***************************************/
  private void positionContents()
  {
    // position date
    double x = ( m_time.getWidth() - m_date.getWidth() ) / 2.0;
    double y = PADDING;
    m_date.relocate( x, y );

    // position calendar below date
    y += m_date.getHeight() + PADDING;
    m_calendar.setWidth( m_time.getWidth() - PADDING - PADDING );
    m_calendar.relocate( PADDING, y );

    GraphicsContext gc = m_calendar.getGraphicsContext2D();
    gc.setFill( Color.YELLOW );
    gc.fillRect( 0.0, 0.0, m_calendar.getWidth(), m_calendar.getHeight() - PADDING );

    // position time
    JPlanner.trace( m_calendar.getWidth(), m_calendar.getHeight() );
    y += m_calendar.getHeight() + PADDING;
    m_time.relocate( 0.0, y );

    m_pane.autosize();
  }

}
