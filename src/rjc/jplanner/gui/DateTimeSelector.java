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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import rjc.jplanner.JPlanner;
import rjc.jplanner.model.Calendar;
import rjc.jplanner.model.Date;
import rjc.jplanner.model.DateTime;
import rjc.jplanner.model.Day;
import rjc.jplanner.model.Time;

/*************************************************************************************************/
/********************* Pop-up window to display date-time selection widgets **********************/
/*************************************************************************************************/

public class DateTimeSelector extends Popup
{
  private DateTimeEditor      m_parent;
  private boolean             m_ignoreUpdates;

  private Pane                m_pane;              // contains the pop-up widgets
  private HBox                m_date;              // for entering month + year
  private HBox                m_time;              // for entering hours + mins + secs + millisecs
  private Canvas              m_calendar;          // for picking date
  private HBox                m_buttons;           // for today + start + end buttons

  private SpinEditor          m_year;
  private MonthSpinEditor     m_month;
  private SpinEditor          m_epochDay;

  private SpinEditor          m_hours;
  private SpinEditor          m_mins;
  private SpinEditor          m_secs;
  private SpinEditor          m_millisecs;

  private Button              m_today;
  private Button              m_start;
  private Button              m_end;
  private Button              m_forward;
  private Button              m_back;

  private GraphicsContext     m_gc;
  private double              m_columnWidth;
  private double              m_rowHeight;
  private double              m_x;
  private double              m_y;

  private static final double PADDING       = 3.0;
  private static final double HEIGHT        = 23.0;
  private static final double SHADOW_RADIUS = 4.0;

  /**************************************** constructor ******************************************/
  public DateTimeSelector( DateTimeEditor parent )
  {
    // create pop-up window to display date-time selection widgets
    super();
    m_parent = parent;
    setAutoHide( true );
    setConsumeAutoHidingEvents( false );
    constructSelector();

    // add shadow
    DropShadow shadow = new DropShadow();
    shadow.setColor( Colors.FOCUSED_BLUE );
    shadow.setRadius( SHADOW_RADIUS );
    getScene().getRoot().setEffect( shadow );

    // toggle pop-up when parent button is pressed
    m_parent.getButton().setOnMousePressed( event -> toggleSelector( event ) );

    // ensure parent editor is editable when selector is hidden
    setOnHidden( event -> m_parent.setEditable( true ) );

    // keep parent editor and this selector synchronised
    m_parent.textProperty().addListener( ( observable, oldText, newText ) -> setDateTime( m_parent.getDateTime() ) );
    m_calendar.setOnMouseClicked( event -> calendarMouseClicked( event ) );
    m_year.textProperty().addListener( ( observable, oldText, newText ) -> updateParent( m_year ) );
    m_month.textProperty().addListener( ( observable, oldText, newText ) -> updateParent( m_month ) );
    m_hours.textProperty().addListener( ( observable, oldText, newText ) -> updateParent( m_hours ) );
    m_mins.textProperty().addListener( ( observable, oldText, newText ) -> updateParent( m_mins ) );
    m_secs.textProperty().addListener( ( observable, oldText, newText ) -> updateParent( m_secs ) );
    m_millisecs.textProperty().addListener( ( observable, oldText, newText ) -> updateParent( m_millisecs ) );
  }

  /*************************************** updateParent ******************************************/
  private void updateParent( XTextField trigger )
  {
    // if editor not showing, return immediately not doing anything
    if ( !isShowing() || m_ignoreUpdates )
      return;

    // if triggering editor was month or year, update never visible epoch-day editor
    if ( trigger == m_month || trigger == m_year )
    {
      int year = m_year.getInteger();
      int month = m_month.getMonthNumber();
      int day = LocalDate.ofEpochDay( m_epochDay.getInteger() ).getDayOfMonth();

      YearMonth ym = YearMonth.of( year, month );
      int max = ym.lengthOfMonth();
      day = Math.min( day, max );
      if ( day > max )
        day = max;

      m_epochDay.setDouble( LocalDate.of( year, month, day ).toEpochDay() );
    }

    // update parent editor to reflect date-time shown in selector
    m_parent.setDateTime( getDateTime() );
  }

  /***************************************** setDateTime *****************************************/
  private void setDateTime( DateTime dt )
  {
    // set selector to specified date-time
    if ( dt == null )
      return;

    m_ignoreUpdates = true;
    m_epochDay.setInteger( dt.getDate().getEpochday() );
    m_month.setMonth( Month.of( dt.getDate().getMonth() ) );
    m_year.setInteger( dt.getDate().getYear() );
    m_hours.setInteger( dt.getTime().getHours() );
    m_mins.setInteger( dt.getTime().getMinutes() );
    m_secs.setInteger( dt.getTime().getSeconds() );
    m_millisecs.setInteger( dt.getTime().getMs() );
    m_ignoreUpdates = false;
    drawCalendar();
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
    return new Date( m_epochDay.getInteger() );
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
  private void toggleSelector( MouseEvent event )
  {
    // if selector open, hide, if hidden, open
    if ( isShowing() )
      hide();
    else
    {
      m_parent.setEditable( false );
      Point2D point = m_parent.localToScreen( 0.0, m_parent.getHeight() );
      show( m_parent, point.getX() - SHADOW_RADIUS + 1.0, point.getY() - SHADOW_RADIUS + 1.0 );
      arrangeSelector();
    }
  }

  /************************************** constructSelector **************************************/
  private void constructSelector()
  {
    // define background and border for the selector
    Background BACKGROUND = new Background( new BackgroundFill( Colors.GENERAL_BACKGROUND, null, null ) );
    Border BORDER = new Border( new BorderStroke( Colors.FOCUSED_BLUE, BorderStrokeStyle.SOLID, null, null ) );

    // padding around the widgets
    Insets INSETS = new Insets( PADDING - 2.0, PADDING - 1.0, PADDING, PADDING );

    // create spin editors for entering date month-year
    m_date = new HBox();
    m_date.setPadding( INSETS );
    m_date.setSpacing( PADDING );
    m_year = createSpinEditor( -999999, 999999, 80, "0000", null );
    m_month = new MonthSpinEditor();
    m_month.setPrefWidth( 121 );
    m_month.setMinHeight( HEIGHT );
    m_month.setMaxHeight( HEIGHT );
    m_month.setYearSpinEditor( m_year );
    m_date.getChildren().addAll( m_month, m_year );

    // this spin editor is never visible, just used to support wrapping
    m_epochDay = createSpinEditor( Integer.MIN_VALUE, Integer.MAX_VALUE, 0, "0", null );

    // create spin editors for entering time hh:mm:ss:mmm
    m_time = new HBox();
    m_time.setPadding( INSETS );
    m_time.setSpacing( PADDING );
    m_hours = createSpinEditor( 0, 23, 47, "00", m_epochDay );
    m_hours.setStepPage( 1, 6 );
    m_mins = createSpinEditor( 0, 59, 47, "00", m_hours );
    m_secs = createSpinEditor( 0, 59, 47, "00", m_mins );
    m_millisecs = createSpinEditor( 0, 999, 53, "000", m_secs );
    m_millisecs.setStepPage( 1, 100 );
    m_time.getChildren().addAll( m_hours, m_mins, m_secs, m_millisecs );

    // create calendar canvas
    m_calendar = new Canvas();
    m_calendar.setHeight( 17.0 * 7.0 );

    // create buttons
    m_buttons = new HBox();
    m_buttons.setPadding( INSETS );
    m_buttons.setSpacing( PADDING );
    m_today = new Button( "Today" );
    m_back = new Button( "<" );
    m_start = new Button( "Start" );
    m_end = new Button( "End" );
    m_forward = new Button( ">" );
    m_buttons.getChildren().addAll( m_today, m_back, m_start, m_end, m_forward );

    // add button actions
    m_today.setOnAction( event -> m_parent.setDateTime( new DateTime( Date.now(), getTime() ) ) );

    m_back.setOnAction( event -> JPlanner.trace( "BACK" ) );

    m_start.setOnAction( event ->
    {
      Day day = JPlanner.gui.getPropertiesPane().getCalendar().getDay( getDate() );
      if ( day.getNumberOfPeriods() > 0 )
        m_parent.setDateTime( new DateTime( getDate(), day.getStart() ) );
      else
        m_parent.setDateTime( new DateTime( getDate(), Time.MIN_VALUE ) );
    } );

    m_end.setOnAction( event ->
    {
      Day day = JPlanner.gui.getPropertiesPane().getCalendar().getDay( getDate() );
      if ( day.getNumberOfPeriods() > 0 )
        m_parent.setDateTime( new DateTime( getDate(), day.getEnd() ) );
      else
        m_parent.setDateTime( new DateTime( getDate(), Time.MAX_VALUE ) );
    } );

    m_forward.setOnAction( event -> JPlanner.trace( "FORWARD" ) );

    // populate pane
    m_pane = new Pane();
    m_pane.getChildren().addAll( m_date, m_calendar, m_time, m_buttons );
    m_pane.setBackground( BACKGROUND );
    m_pane.setBorder( BORDER );
    getContent().add( m_pane );
  }

  /************************************* createSpinEditor ****************************************/
  private SpinEditor createSpinEditor( int minValue, int maxValue, int width, String format, SpinEditor wrap )
  {
    // create a spin-editor
    SpinEditor spin = new SpinEditor();
    spin.setRange( minValue, maxValue, 0 );
    spin.setPrefWidth( width );
    spin.setMinHeight( HEIGHT );
    spin.setMaxHeight( HEIGHT );
    spin.setFormat( format );
    spin.setWrapSpinEditor( wrap );
    return spin;
  }

  /*************************************** arrangeSelector ***************************************/
  private void arrangeSelector()
  {
    // position date
    double y = PADDING;
    m_date.relocate( 0.0, y );

    // position calendar below date
    y += m_date.getHeight();
    m_calendar.setWidth( m_time.getWidth() - 2 * PADDING + 1.0 );
    m_calendar.relocate( PADDING + 1.0, y );
    drawCalendar();

    // position time
    y += m_calendar.getHeight() + PADDING;
    m_time.relocate( 0.0, y );

    // position buttons
    y += m_time.getHeight();
    m_buttons.relocate( 0.0, y );

    // resize 'today' button
    m_today.setPrefWidth( m_time.getWidth() - m_back.getWidth() - m_start.getWidth() - m_end.getWidth()
        - m_forward.getWidth() - 6 * PADDING + 1.0 );

    m_pane.autosize();
  }

  /**************************************** drawCalendar *****************************************/
  private void drawCalendar()
  {
    // only draw if selector is showing
    if ( !isShowing() )
      return;

    // draw calendar for month-year specified in the spin editors
    int month = m_month.getMonthNumber();
    int year = m_year.getInteger();
    LocalDate localdate = LocalDate.of( year, month, 1 );
    localdate = localdate.minusDays( localdate.getDayOfWeek().getValue() - 1 );
    LocalDate selecteddate = m_parent.getDateTime().getDate().localDate();

    // calculate calendar cell width & height
    double w = m_calendar.getWidth();
    double h = m_calendar.getHeight();
    m_columnWidth = Math.floor( w / 7.0 );
    m_rowHeight = Math.floor( h / 7.0 );

    // clear the calendar
    m_gc = m_calendar.getGraphicsContext2D();
    m_gc.clearRect( 0.0, 0.0, w, h );
    m_gc.setFontSmoothingType( FontSmoothingType.LCD );
    Calendar calendar = JPlanner.gui.getPropertiesPane().getCalendar();

    // draw the calendar day labels and day-of-month numbers
    for ( int row = 0; row < 7; row++ )
    {
      m_y = row * m_rowHeight;
      for ( int column = 0; column < 7; column++ )
      {
        m_x = column * m_columnWidth;

        if ( row == 0 )
        // in first row put day of week
        {
          DayOfWeek day = DayOfWeek.of( column + 1 );
          String label = day.getDisplayName( TextStyle.SHORT, Locale.getDefault() ).substring( 0, 2 );
          drawText( label, Color.BLACK, Color.BEIGE );
        }
        else
        // in other rows put day-of-month numbers
        {
          // numbers are black except gray for other months and red for today
          Color textColor = Color.BLACK;
          if ( localdate.getMonthValue() != month )
            textColor = Color.GRAY;
          if ( localdate.isEqual( selecteddate ) )
            textColor = Color.WHITE;
          if ( localdate.isEqual( LocalDate.now() ) )
            textColor = Color.RED;

          // number background colour is shade of gray to white depending of day work
          double work = calendar.getDay( new Date( localdate ) ).getWork();
          if ( work > 1.0 )
            work = 1.0;
          Color backColor = Color.gray( work / 10.0 + 0.9 );

          // select day is blue
          if ( localdate.isEqual( selecteddate ) )
            backColor = Colors.FOCUSED_BLUE;

          // draw number and move to next day
          drawText( localdate.getDayOfMonth(), textColor, backColor );
          localdate = localdate.plusDays( 1 );
        }
      }
    }

  }

  /****************************************** drawText *******************************************/
  private void drawText( Object text, Color textColor, Color backgroundColor )
  {
    // draw text centred in box defined by m_x, m_y, m_columnWidth, m_rowHeight
    m_gc.setFill( backgroundColor );
    m_gc.fillRect( m_x, m_y, m_columnWidth, m_rowHeight );

    Bounds bounds = new Text( text.toString() ).getLayoutBounds();
    double x = m_x + ( m_columnWidth - bounds.getWidth() ) / 2.0;
    double y = m_y + ( m_rowHeight - bounds.getHeight() ) / 2.0 - bounds.getMinY();

    m_gc.setFill( textColor );
    m_gc.fillText( text.toString(), x, y );
  }

  /************************************** calendarMouseClicked **************************************/
  private void calendarMouseClicked( MouseEvent event )
  {
    // update editor date with calendar date clicked
    int column = (int) ( event.getX() / m_columnWidth );
    int row = (int) ( event.getY() / m_rowHeight );
    if ( row < 1 )
      return;

    LocalDate ld = LocalDate.of( m_year.getInteger(), m_month.getMonthNumber(), 1 );
    ld = ld.minusDays( ld.getDayOfWeek().getValue() - 1 );
    ld = ld.plusDays( column + 7 * ( --row ) );

    DateTime dt = new DateTime( new Date( ld ), getTime() );
    m_parent.setDateTime( dt );
  }

}
