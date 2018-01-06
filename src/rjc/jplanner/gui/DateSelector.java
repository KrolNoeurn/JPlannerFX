/**************************************************************************
 *  Copyright (C) 2018 by Richard Crook                                   *
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
import java.time.Year;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import rjc.jplanner.JPlanner;
import rjc.jplanner.model.Calendar;
import rjc.jplanner.model.Date;

/*************************************************************************************************/
/************************* Pane allowing user to select month/year/date **************************/
/*************************************************************************************************/

public class DateSelector extends Pane
{
  private MonthSpinEditor m_month;       // for picking year
  private SpinEditor      m_year;        // for picking month
  private Canvas          m_calendar;    // for picking date
  private SpinEditor      m_epochDay;    // hidden internal for wrapping
  private Pane            m_extra;       // extra node to be shown on selector

  private GraphicsContext m_gc;
  private double          m_columnWidth;
  private double          m_rowHeight;
  private double          m_x;
  private double          m_y;

  static final double     PADDING = 3.0;
  static final double     HEIGHT  = 23.0;

  /**************************************** constructor ******************************************/
  public DateSelector( Pane extra )
  {
    // create pane allowing user to select month/year/date
    super();
    m_extra = extra;

    // create year spin editor
    m_year = new SpinEditor()
    {
      @Override
      protected void keyPressed( KeyEvent event )
      {
        // action key press to change value up or down
        switch ( event.getCode() )
        {
          case HOME:
            LocalDate ld = LocalDate.of( m_year.getInteger(), 1, 1 );
            m_epochDay.setInteger( (int) ld.toEpochDay() );
            event.consume();
            break;
          case END:
            ld = LocalDate.of( m_year.getInteger(), 12, 31 );
            m_epochDay.setInteger( (int) ld.toEpochDay() );
            event.consume();
            break;
          default:
            super.keyPressed( event );
            break;
        }
      }
    };
    m_year.setRange( -999999, 999999, 0 );
    m_year.setPrefWidth( 80 );
    m_year.setMinHeight( HEIGHT );
    m_year.setMaxHeight( HEIGHT );
    m_year.setFormat( "0000" );

    // create month spin editor
    m_month = new MonthSpinEditor();
    m_month.setPrefWidth( 115 );
    m_month.setMinHeight( HEIGHT );
    m_month.setMaxHeight( HEIGHT );
    m_month.setYearSpinEditor( m_year );

    // this spin editor is never visible, just used to support wrapping
    m_epochDay = new SpinEditor();
    m_epochDay.setRange( Integer.MIN_VALUE, Integer.MAX_VALUE, 0 );

    // when spin editors change ensure date property is kept up to date
    m_year.textProperty().addListener( ( observable, oldT, newT ) -> updateEpochDay() );
    m_month.textProperty().addListener( ( observable, oldT, newT ) -> updateEpochDay() );
    m_epochDay.textProperty().addListener( ( observable, oldT, newT ) -> update() );

    // create calendar canvas
    m_calendar = new Canvas();
    m_calendar.setHeight( 17.0 * 7.0 );
    m_calendar.setFocusTraversable( true );
    m_calendar.setOnKeyPressed( event -> calendarKeyPressed( event ) );
    m_calendar.setOnKeyTyped( event -> calendarKeyTyped( event ) );
    m_calendar.setOnMouseClicked( event -> calendarMouseClicked( event ) );
    m_calendar.focusedProperty().addListener( ( observable, oldFocus, newFocus ) ->
    {
      if ( newFocus.booleanValue() )
        m_calendar.setStyle( "-fx-effect: dropshadow(gaussian, #039ed3, 4, 0.75, 0, 0);" );
      else
        m_calendar.setStyle( "" );
    } );

    // populate pane
    getChildren().addAll( m_calendar, m_extra, m_month, m_year );
    Background BACKGROUND = new Background( new BackgroundFill( Colors.GENERAL_BACKGROUND, null, null ) );
    Border BORDER = new Border( new BorderStroke( Colors.FOCUSBLUE, BorderStrokeStyle.SOLID, null, null ) );
    setBackground( BACKGROUND );
    setBorder( BORDER );

    // when scene is set add focus owner listener
    sceneProperty().addListener( ( observable, oldScene, newScene ) ->
    {
      if ( newScene != null )
        newScene.focusOwnerProperty().addListener( ( property, oldNode, newNode ) ->
        {
          setOnScroll( null );
          if ( newNode instanceof SpinEditor )
            setOnScroll( event -> ( (SpinEditor) newNode ).scrollEvent( event ) );
          if ( newNode instanceof MonthSpinEditor )
            setOnScroll( event -> ( (MonthSpinEditor) newNode ).scrollEvent( event ) );
          if ( newNode == m_calendar )
            setOnScroll( event -> calendarScrollEvent( event ) );
        } );
    } );
  }

  /*************************************** updateEpochDay ****************************************/
  private void updateEpochDay()
  {
    // use run-later to ensure all spin-editors wrapping has completed first
    Platform.runLater( () ->
    {
      // update epoch day spin editor from month & year spin editors
      int month = m_month.getMonthNumber();
      int year = m_year.getInteger();
      int day = getLocalDate().getDayOfMonth();

      // ensure day is valid for month-year
      if ( day > YearMonth.of( year, month ).lengthOfMonth() )
        day = YearMonth.of( year, month ).lengthOfMonth();

      LocalDate ld = LocalDate.of( year, month, day );
      m_epochDay.setInteger( (int) ld.toEpochDay() );
    } );
  }

  /************************************ getEpochDaySpinEditor ************************************/
  public SpinEditor getEpochDaySpinEditor()
  {
    // return epoch day spin editor 
    return m_epochDay;
  }

  /**************************************** getLocalDate *****************************************/
  public LocalDate getLocalDate()
  {
    // return local-date for date represented in epoch day spin editor 
    return LocalDate.ofEpochDay( m_epochDay.getInteger() );
  }

  /****************************************** moveDate *******************************************/
  public void moveDate( int num )
  {
    // change date represented in epoch day spin editor and update display
    m_epochDay.setInteger( m_epochDay.getInteger() + num );
  }

  /******************************************* arrange *******************************************/
  public void arrange()
  {
    // position month & year spin editors
    double y = PADDING;
    double x = PADDING;
    m_month.relocate( x, y );
    m_year.relocate( x + m_month.getWidth() + PADDING, y );

    // position calendar below month & year spin editors
    y += m_month.getHeight() + PADDING + 1.0;
    m_calendar.setWidth( m_month.getWidth() + m_year.getWidth() + 1.0 );
    m_calendar.relocate( x + 1.0, y );
    update();

    // position extra node below calendar
    y += m_calendar.getHeight() + PADDING + 1.0;
    m_extra.relocate( x, y );

    // size pane to contents
    setPrefWidth( m_calendar.getWidth() + 2 * PADDING + 2.0 );
    setPrefHeight( y + m_extra.getHeight() + PADDING + 1.0 );
  }

  /***************************************** update ******************************************/
  private void update()
  {
    // display the date from epoch-day-spin-editor on month, year & calendar
    LocalDate selectedDate = getLocalDate();
    m_month.setMonth( selectedDate.getMonthValue() );
    m_year.setInteger( selectedDate.getYear() );

    // draw calendar for month-year
    Calendar calendar = JPlanner.plan.getDefaultCalendar();
    LocalDate localdate = selectedDate.minusDays( selectedDate.getDayOfMonth() );
    localdate = localdate.minusDays( localdate.getDayOfWeek().getValue() - 1 );
    double w = m_calendar.getWidth();
    double h = m_calendar.getHeight();
    m_columnWidth = Math.floor( w / 7.0 );
    m_rowHeight = Math.floor( h / 7.0 );

    // clear the calendar canvas
    m_gc = m_calendar.getGraphicsContext2D();
    m_gc.clearRect( 0.0, 0.0, w, h );
    m_gc.setFontSmoothingType( FontSmoothingType.LCD );

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
          if ( localdate.getMonthValue() != selectedDate.getMonthValue() )
            textColor = Color.GRAY;
          if ( localdate.isEqual( selectedDate ) )
            textColor = Color.WHITE;
          if ( localdate.isEqual( LocalDate.now() ) )
            textColor = Color.RED;

          // number background colour is shade of gray to white depending of day work
          double work = calendar.getDay( new Date( localdate ) ).getWork();
          if ( work > 1.0 )
            work = 1.0;
          Color backColor = Color.gray( work / 10.0 + 0.9 );

          // select day is blue
          if ( localdate.isEqual( selectedDate ) )
            backColor = Colors.FOCUSBLUE;

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

  /************************************ calendarMouseClicked *************************************/
  private void calendarMouseClicked( MouseEvent event )
  {
    // update editor date with calendar date clicked
    m_calendar.requestFocus();
    int column = (int) ( event.getX() / m_columnWidth );
    int row = (int) ( event.getY() / m_rowHeight );

    if ( row > 0 )
    {
      LocalDate ld = LocalDate.of( m_year.getInteger(), m_month.getMonthNumber(), 1 );
      if ( ld.getDayOfWeek() == DayOfWeek.MONDAY )
        ld = ld.minusDays( 7 );
      ld = ld.minusDays( ld.getDayOfWeek().getValue() - 1 );
      ld = ld.plusDays( column + 7 * ( --row ) );

      m_epochDay.setInteger( (int) ld.toEpochDay() );
    }
  }

  /************************************ calendarKeyPressed ***************************************/
  private void calendarKeyPressed( KeyEvent event )
  {
    // react to key presses
    boolean handled = true;
    switch ( event.getCode() )
    {
      case HOME:
        int day = getLocalDate().getDayOfMonth();
        moveDate( 1 - day );
        break;

      case END:
        boolean leap = Year.isLeap( m_year.getInteger() );
        int len = m_month.getMonth().length( leap );
        day = getLocalDate().getDayOfMonth();
        moveDate( len - day );
        break;

      case PAGE_UP:
        LocalDate ld = getLocalDate().plusMonths( -1 );
        m_epochDay.setInteger( (int) ld.toEpochDay() );
        break;

      case PAGE_DOWN:
        ld = getLocalDate().plusMonths( 1 );
        m_epochDay.setInteger( (int) ld.toEpochDay() );
        break;

      case UP:
      case KP_UP:
        moveDate( -7 );
        break;

      case DOWN:
      case KP_DOWN:
        moveDate( 7 );
        break;

      case RIGHT:
      case KP_RIGHT:
        moveDate( 1 );
        break;

      case LEFT:
      case KP_LEFT:
        moveDate( -1 );
        break;

      default:
        handled = false;
        break;
    }

    // if handled then consume
    if ( handled )
      event.consume();
  }

  /************************************ calendarKeyPressed ***************************************/
  private void calendarKeyTyped( KeyEvent event )
  {
    // reach to key typed
    char key = event.getCharacter().charAt( 0 );

    // if digit typed, move date forward until day-of-month contains typed digit
    if ( Character.isDigit( key ) )
    {
      LocalDate ld = getLocalDate();
      do
        ld = ld.plusDays( 1 );
      while ( Integer.toString( ld.getDayOfMonth() ).indexOf( key ) < 0 );
      m_epochDay.setInteger( (int) ld.toEpochDay() );
    }

  }

  /************************************* calendarScrollEvent *************************************/
  private void calendarScrollEvent( ScrollEvent event )
  {
    // increment or decrement value depending on mouse wheel scroll event
    if ( event.getDeltaY() > 0 )
      moveDate( 1 );
    else
      moveDate( -1 );
  }

}
