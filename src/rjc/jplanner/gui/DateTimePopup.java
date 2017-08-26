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

import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import rjc.jplanner.JPlanner;
import rjc.jplanner.model.Calendar;
import rjc.jplanner.model.Date;
import rjc.jplanner.model.DateTime;
import rjc.jplanner.model.Day;
import rjc.jplanner.model.Time;

/*************************************************************************************************/
/************************ Pop-up window to display date selection widgets ************************/
/*************************************************************************************************/

class DateTimePopup extends Popup
{
  private DateTimeEditor      m_parent;
  private DateSelector        m_dateSelector;

  private Pane                m_pane;             // contains the time and buttons
  private HBox                m_time;             // for entering hours + mins + secs + millisecs
  private HBox                m_buttons;          // for today + start + end buttons

  private SpinEditor          m_hours;
  private SpinEditor          m_mins;
  private SpinEditor          m_secs;
  private SpinEditor          m_millisecs;

  private Button              m_today;            // set date to today, without changing time
  private Button              m_start;            // set time to working start of day, without changing date
  private Button              m_end;              // set time to working end of day, without changing date
  private Button              m_forward;          // move date-time forward to next working period boundary
  private Button              m_back;             // move date-time back to next working period boundary

  private static final double SHADOW_RADIUS = 4.0;

  /**************************************** constructor ******************************************/
  public DateTimePopup( DateTimeEditor parent )
  {
    // create pop-up window to display date-time selection widgets
    m_parent = parent;
    setAutoHide( true );
    setConsumeAutoHidingEvents( false );
    constructPopup();

    // add shadow to pop-up
    DropShadow shadow = new DropShadow();
    shadow.setColor( Colors.FOCUSBLUE );
    shadow.setRadius( SHADOW_RADIUS );
    getScene().getRoot().setEffect( shadow );

    // toggle pop-up when parent button is pressed or F2 key is pressed
    m_parent.getButton().setOnMousePressed( event -> toggleSelector() );
    m_parent.setOnKeyPressed( event ->
    {
      if ( event.getCode() == KeyCode.F2 )
        toggleSelector();
    } );

    // ensure parent editor is editable when pop-up is hidden
    setOnHidden( event -> m_parent.setEditable( true ) );

    // keep parent editor and pop-up synchronised
    m_dateSelector.getEpochDaySpinEditor().textProperty().addListener( ( observable, oldT, newT ) -> updateParent() );
    m_hours.textProperty().addListener( ( observable, oldT, newT ) -> updateParent() );
    m_mins.textProperty().addListener( ( observable, oldT, newT ) -> updateParent() );
    m_secs.textProperty().addListener( ( observable, oldT, newT ) -> updateParent() );
    m_millisecs.textProperty().addListener( ( observable, oldT, newT ) -> updateParent() );
  }

  /**************************************** updateParent *****************************************/
  private void updateParent()
  {
    // update parent to reflect pop-up date-time
    m_parent.setDateTime( getDateTime() );
  }

  /*************************************** constructPopup ****************************************/
  private void constructPopup()
  {
    // padding around the widgets
    Insets INSETS = new Insets( DateSelector.PADDING, 0, 0, 0 );

    // create spin editors for entering time hh:mm:ss:mmm
    m_time = new HBox();
    m_time.setSpacing( DateSelector.PADDING );
    m_hours = createSpinEditor( 0, 23, 45, "00", null );
    m_hours.setStepPage( 1, 6 );
    m_mins = createSpinEditor( 0, 59, 45, "00", m_hours );
    m_secs = createSpinEditor( 0, 59, 45, "00", m_mins );
    m_millisecs = createSpinEditor( 0, 999, 54, "000", m_secs );
    m_millisecs.setStepPage( 1, 100 );
    m_time.getChildren().addAll( m_hours, m_mins, m_secs, m_millisecs );

    // create buttons
    m_buttons = new HBox();
    m_buttons.setPadding( INSETS );
    m_buttons.setSpacing( DateSelector.PADDING );
    m_today = new Button( "Today" );
    m_back = new Button( "<" );
    m_start = new Button( "Start" );
    m_end = new Button( "End" );
    m_forward = new Button( ">" );
    m_buttons.getChildren().addAll( m_today, m_back, m_start, m_end, m_forward );

    m_pane = new VBox();
    m_pane.getChildren().addAll( m_time, m_buttons );

    // add button actions
    m_today.setOnAction( event -> setDateTime( new DateTime( Date.now(), getTime() ) ) );

    m_back.setOnAction( event -> setDateTime( back() ) );

    m_start.setOnAction( event ->
    {
      Day day = JPlanner.plan.getDefaultCalendar().getDay( getDate() );
      if ( day.getNumberOfPeriods() > 0 )
        setDateTime( new DateTime( getDate(), day.getStart() ) );
      else
        setDateTime( new DateTime( getDate(), Time.MIN_VALUE ) );
    } );

    m_end.setOnAction( event ->
    {
      Day day = JPlanner.plan.getDefaultCalendar().getDay( getDate() );
      if ( day.getNumberOfPeriods() > 0 )
        setDateTime( new DateTime( getDate(), day.getEnd() ) );
      else
        setDateTime( new DateTime( getDate(), Time.MAX_VALUE ) );
    } );

    m_forward.setOnAction( event -> setDateTime( forward() ) );

    // set pop-up contents to a date selector with time & buttons pane below
    m_dateSelector = new DateSelector( m_pane );
    getContent().addAll( m_dateSelector );
    m_hours.setWrapSpinEditor( m_dateSelector.getEpochDaySpinEditor() );

    // intercept ESCAPE and ENTER events and pass to parent
    addEventFilter( KeyEvent.KEY_PRESSED, event ->
    {
      if ( event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.ENTER )
      {
        hide();
        m_parent.fireEvent( event.copyFor( m_parent, m_parent ) );
        event.consume();
      }
    } );
  }

  /******************************************* forward *******************************************/
  private DateTime forward()
  {
    // return date-time forward to next working period boundary
    Calendar cal = JPlanner.plan.getDefaultCalendar();
    Date date = getDate();
    Day day = cal.getDay( date );
    int ms = getTime().getDayMilliseconds();
    int num = day.getNumberOfPeriods();

    // loop around any working periods in day
    for ( int period = 0; period < num; period++ )
    {
      if ( ms < day.getStart( period ).getDayMilliseconds() )
        return new DateTime( date, day.getStart( period ) );
      if ( ms < day.getEnd( period ).getDayMilliseconds() )
        return new DateTime( date, day.getEnd( period ) );
    }

    // after final work period
    return cal.getWorkDateTimeUp( getDateTime() );
  }

  /******************************************** back *********************************************/
  private DateTime back()
  {
    // return date-time back to next working period boundary
    Calendar cal = JPlanner.plan.getDefaultCalendar();
    Date date = getDate();
    int ms = getTime().getDayMilliseconds();
    if ( ms == 0 )
    {
      date.decrement();
      ms = Time.MILLISECONDS_IN_DAY;
    }
    Day day = cal.getDay( date );
    int num = day.getNumberOfPeriods();

    // loop around any working periods in day
    for ( int period = num - 1; period >= 0; period-- )
    {
      if ( ms > day.getEnd( period ).getDayMilliseconds() )
        return new DateTime( date, day.getEnd( period ) );
      if ( ms > day.getStart( period ).getDayMilliseconds() )
        return new DateTime( date, day.getStart( period ) );
    }

    // before first work period
    return cal.getWorkDateTimeDown( getDateTime() );
  }

  /************************************* createSpinEditor ****************************************/
  private SpinEditor createSpinEditor( int minValue, int maxValue, int width, String format, SpinEditor wrap )
  {
    // create a spin-editor
    SpinEditor spin = new SpinEditor();
    spin.setRange( minValue, maxValue, 0 );
    spin.setPrefWidth( width );
    spin.setMinHeight( DateSelector.HEIGHT );
    spin.setMaxHeight( DateSelector.HEIGHT );
    spin.setFormat( format );
    spin.setWrapSpinEditor( wrap );
    return spin;
  }

  /***************************************** setDateTime *****************************************/
  private void setDateTime( DateTime dt )
  {
    // set selector to specified date-time
    if ( dt != null )
    {
      m_dateSelector.getEpochDaySpinEditor().setInteger( dt.getDate().getEpochday() );
      m_hours.setInteger( dt.getTime().getHours() );
      m_mins.setInteger( dt.getTime().getMinutes() );
      m_secs.setInteger( dt.getTime().getSeconds() );
      m_millisecs.setInteger( dt.getTime().getMilliseconds() );
    }
  }

  /***************************************** getDateTime *****************************************/
  private DateTime getDateTime()
  {
    // return date-time shown by selector
    return new DateTime( getDate(), getTime() );
  }

  /******************************************* getDate *******************************************/
  private Date getDate()
  {
    // return date shown by selector
    return new Date( m_dateSelector.getEpochDaySpinEditor().getInteger() );
  }

  /******************************************* getTime *******************************************/
  private Time getTime()
  {
    // return time shown by selector
    int hours = m_hours.getInteger();
    int mins = m_mins.getInteger();
    int secs = m_secs.getInteger();
    int ms = m_millisecs.getInteger();
    return new Time( hours, mins, secs, ms );
  }

  /*************************************** toggleSelector ****************************************/
  private void toggleSelector()
  {
    // if selector open, hide, if hidden, open
    if ( isShowing() )
      hide();
    else
    {
      m_parent.setEditable( false );
      m_parent.requestFocus();
      setDateTime( m_parent.getDateTime() );
      Point2D point = m_parent.localToScreen( 0.0, m_parent.getHeight() );
      show( m_parent, point.getX() - SHADOW_RADIUS + 1.0, point.getY() - SHADOW_RADIUS + 1.0 );
      m_dateSelector.arrange();
      m_today.setPrefWidth( m_dateSelector.getPrefWidth() - m_forward.getWidth() - m_back.getWidth()
          - m_start.getWidth() - m_end.getWidth() - 6 * DateSelector.PADDING );
    }
  }

}
