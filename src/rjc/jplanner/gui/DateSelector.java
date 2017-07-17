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
import java.time.Year;
import java.time.format.TextStyle;
import java.util.Locale;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
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
  private SimpleObjectProperty<Date> m_dateProperty;

  private MonthSpinEditor            m_month;       // for picking year
  private SpinEditor                 m_year;        // for picking month
  private Canvas                     m_calendar;    // for picking date
  private SpinEditor                 m_epochDay;    // hidden internal for wrapping

  private GraphicsContext            m_gc;
  private double                     m_columnWidth;
  private double                     m_rowHeight;
  private double                     m_x;
  private double                     m_y;

  private static final double        PADDING = 3.0;
  private static final double        HEIGHT  = 23.0;

  /**************************************** constructor ******************************************/
  public DateSelector()
  {
    // create pane allowing user to select month/year/date
    super();
    m_dateProperty = new SimpleObjectProperty<Date>();

    // create year spin editor
    m_year = new SpinEditor();
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
    m_dateProperty.addListener( ( observable, oldDate, newDate ) -> updatePane() );

    // populate pane
    getChildren().addAll( m_month, m_year, m_calendar );
    Background BACKGROUND = new Background( new BackgroundFill( Colors.GENERAL_BACKGROUND, null, null ) );
    Border BORDER = new Border( new BorderStroke( Colors.FOCUSBLUE, BorderStrokeStyle.SOLID, null, null ) );
    setBackground( BACKGROUND );
    setBorder( BORDER );
  }

  /************************************* getDateProperty *****************************************/
  public SimpleObjectProperty<Date> getDateProperty()
  {
    // return date property containing currently selected date 
    return m_dateProperty;
  }

  /************************************ getEpochDaySpinEditor ************************************/
  public SpinEditor getEpochDaySpinEditor()
  {
    // return hidden spin editor to be incremented/decremented when hours spin editor wraps
    return m_epochDay;
  }

  /*************************************** arrangeSelector ***************************************/
  public void arrangeSelector()
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
    Platform.runLater( () -> m_calendar.requestFocus() );
    updatePane();

    // size pane to contents
    setMinWidth( m_calendar.getWidth() + 2 * PADDING + 2.0 );
    setMaxWidth( getMinWidth() );
    setMinHeight( y + m_calendar.getHeight() + PADDING + 1.0 );
    setMaxHeight( getMinHeight() );
  }

  /***************************************** updatePane ******************************************/
  private void updatePane()
  {
    // update month & year spin editors
    Date date = m_dateProperty.get();
    m_month.setMonth( date.getMonth() );
    m_year.setInteger( date.getYear() );

    // draw calendar for month-year specified in the spin editors
    int month = m_month.getMonthNumber();
    int year = m_year.getInteger();
    LocalDate localdate = LocalDate.of( year, month, 1 );
    localdate = localdate.minusDays( localdate.getDayOfWeek().getValue() - 1 );
    LocalDate selecteddate = m_dateProperty.get().localDate();

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

  /************************************** calendarMouseClicked **************************************/
  private void calendarMouseClicked( MouseEvent event )
  {
    // update editor date with calendar date clicked
    m_calendar.requestFocus();

    int column = (int) ( event.getX() / m_columnWidth );
    int row = (int) ( event.getY() / m_rowHeight );

    if ( row > 0 )
    {
      LocalDate ld = LocalDate.of( m_year.getInteger(), m_month.getMonthNumber(), 1 );
      ld = ld.minusDays( ld.getDayOfWeek().getValue() - 1 );
      ld = ld.plusDays( column + 7 * ( --row ) );

      m_dateProperty.set( new Date( ld ) );
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
        int day = m_dateProperty.get().getDayOfMonth();
        m_dateProperty.set( m_dateProperty.get().plusDays( 1 - day ) );
        break;

      case END:
        boolean leap = Year.isLeap( m_year.getInteger() );
        int len = m_month.getMonth().length( leap );
        day = m_dateProperty.get().getDayOfMonth();
        m_dateProperty.set( m_dateProperty.get().plusDays( len - day ) );
        break;

      case PAGE_UP:
        m_dateProperty.set( m_dateProperty.get().plusDays( -28 ) );
        break;

      case PAGE_DOWN:
        m_dateProperty.set( m_dateProperty.get().plusDays( 28 ) );
        break;

      case UP:
      case KP_UP:
        m_dateProperty.set( m_dateProperty.get().plusDays( -7 ) );
        break;

      case DOWN:
      case KP_DOWN:
        m_dateProperty.set( m_dateProperty.get().plusDays( 7 ) );
        break;

      case RIGHT:
      case KP_RIGHT:
        m_dateProperty.set( m_dateProperty.get().plusDays( 1 ) );
        break;

      case LEFT:
      case KP_LEFT:
        m_dateProperty.set( m_dateProperty.get().plusDays( -1 ) );
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
      Date date = m_dateProperty.get();
      do
        date.increment();
      while ( Integer.toString( date.getDayOfMonth() ).indexOf( key ) < 0 );
      m_dateProperty.set( date );
    }

  }

}
