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

import java.time.DayOfWeek;
import java.time.LocalDate;
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

/*************************************************************************************************/
/********************* Pop-up window to display date-time selection widgets **********************/
/*************************************************************************************************/

public class DateTimeSelector extends Popup
{
  private Pane                m_pane;        // contains the pop-up widgets
  private HBox                m_date;        // for entering month + year
  private HBox                m_time;        // for entering hours + mins + secs + millisecs
  private Canvas              m_calendar;    // for picking date
  private HBox                m_buttons;     // for today + start + end buttons

  private SpinEditor          m_month;
  private SpinEditor          m_year;

  private SpinEditor          m_hours;
  private SpinEditor          m_mins;
  private SpinEditor          m_secs;
  private SpinEditor          m_millisecs;

  private Button              m_today;
  private Button              m_start;
  private Button              m_end;

  private GraphicsContext     m_gc;
  private double              m_columnWidth;
  private double              m_rowHeight;
  private double              m_x;
  private double              m_y;

  private static final double PADDING = 3.0;
  private static final double HEIGHT  = 19.0;

  /**************************************** constructor ******************************************/
  public DateTimeSelector( DateTimeEditor parent )
  {
    // create pop-up window to display date-time selection widgets
    super();
    setAutoHide( true );
    setConsumeAutoHidingEvents( false );
    createContents();
    setValues();
    getContent().add( m_pane );

    // add shadow
    DropShadow shadow = new DropShadow();
    shadow.setColor( Colors.FOCUSED_BLUE );
    shadow.setRadius( 4.0 );
    getScene().getRoot().setEffect( shadow );

    // toggle pop-up when parent button is pressed
    parent.getButton().setOnMousePressed( event ->
    {
      if ( isShowing() )
        hide();
      else
      {
        // set pop-up position and show 
        Point2D point = parent.localToScreen( 0.0, parent.getHeight() );
        show( parent, point.getX() - shadow.getRadius() + 1.0, point.getY() - shadow.getRadius() + 1.0 );
        positionContents();
      }
    } );

    m_calendar.setOnMouseClicked( event -> calendarMouseClicked( event ) );
  }

  private void calendarMouseClicked( MouseEvent event )
  {
    // TODO Auto-generated method stub

  }

  /*************************************** createContents ****************************************/
  private void createContents()
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
    m_month = createSpinEditor( 1, 12, 121 );
    m_year = createSpinEditor( -999999, 999999, 80 );
    m_date.getChildren().addAll( m_month, m_year );

    // create spin editors for entering time hh:mm:ss:mmm
    m_time = new HBox();
    m_time.setPadding( INSETS );
    m_time.setSpacing( PADDING );
    m_hours = createSpinEditor( 0, 23, 47 );
    m_mins = createSpinEditor( 0, 59, 47 );
    m_secs = createSpinEditor( 0, 59, 47 );
    m_millisecs = createSpinEditor( 0, 999, 53 );
    m_time.getChildren().addAll( m_hours, m_mins, m_secs, m_millisecs );

    // create calendar canvas
    m_calendar = new Canvas();
    m_calendar.setHeight( 132.0 );

    // create buttons
    m_buttons = new HBox();
    m_buttons.setPadding( INSETS );
    m_buttons.setSpacing( PADDING );
    m_today = new Button( "Today" );
    m_start = new Button( "Day Start" );
    m_end = new Button( "Day End" );
    m_buttons.getChildren().addAll( m_today, m_start, m_end );

    // populate pane
    m_pane = new Pane();
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
    spin.setMinHeight( HEIGHT );
    spin.setMaxHeight( HEIGHT );
    return spin;
  }

  /************************************** positionContents ***************************************/
  private void positionContents()
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
    double x = ( m_time.getWidth() - m_buttons.getWidth() ) / 2.0;

    y += m_time.getHeight();
    m_buttons.relocate( 0.0, y );

    m_pane.autosize();
  }

  /****************************************** setValues ******************************************/
  private void setValues()
  {
    // TODO
    m_month.setInteger( 11 );
    m_year.setInteger( 2016 );

    m_hours.setInteger( 23 );
    m_mins.setInteger( 59 );
    m_secs.setInteger( 59 );
    m_millisecs.setInteger( 999 );
  }

  /**************************************** drawCalendar *****************************************/
  private void drawCalendar()
  {
    // draw calendar for month-year specified in the spin editors
    double w = m_calendar.getWidth();
    double h = m_calendar.getHeight();
    int month = m_month.getInteger();
    int year = m_year.getInteger();

    LocalDate ld = LocalDate.of( year, month, 1 );
    int dow = ld.getDayOfWeek().getValue();
    ld = ld.minusDays( dow - 1 );

    // calculate calendar cell width & height
    m_columnWidth = Math.floor( w / 7.0 );
    m_rowHeight = Math.floor( h / 6.0 );

    // clear the calendar
    m_gc = m_calendar.getGraphicsContext2D();
    m_gc.clearRect( 0.0, 0.0, m_calendar.getWidth(), m_calendar.getHeight() );
    m_gc.setFontSmoothingType( FontSmoothingType.LCD );

    // draw the calendar day labels and day-of-month numbers
    for ( int row = 0; row < 6; row++ )
    {
      m_y = row * m_rowHeight;
      for ( int column = 0; column < 7; column++ )
      {
        m_x = column * m_columnWidth;

        if ( row == 0 )
        {
          // in first row put day of week
          DayOfWeek day = DayOfWeek.of( column + 1 );
          String label = day.getDisplayName( TextStyle.SHORT, Locale.getDefault() ).substring( 0, 2 );
          drawText( label, Color.BLACK, Color.BEIGE );
        }
        else
        {
          // in other rows put day-of-month numbers
          Color textColor = Color.BLACK;
          if ( ld.getMonthValue() != month )
            textColor = Color.GRAY;
          if ( column > 4 )
            textColor = Color.RED;

          Color backColor = Color.WHITE;
          if ( ld.isEqual( LocalDate.now() ) )
            backColor = Color.CHARTREUSE;

          drawText( "" + ld.getDayOfMonth(), textColor, backColor );
          ld = ld.plusDays( 1 );
        }
      }
    }

  }

  /****************************************** drawText *******************************************/
  private void drawText( String text, Color textColor, Color backgroundColor )
  {
    // draw text centred in box defined by m_x, m_y, m_columnWidth, m_rowHeight
    if ( backgroundColor != null )
    {
      m_gc.setFill( backgroundColor );
      m_gc.fillRect( m_x, m_y, m_columnWidth, m_rowHeight );
    }

    Bounds bounds = new Text( text ).getLayoutBounds();
    double x = m_x + ( m_columnWidth - bounds.getWidth() ) / 2.0;
    double y = m_y + ( m_rowHeight - bounds.getHeight() ) / 2.0 - bounds.getMinY();
    m_gc.setFill( textColor );
    m_gc.fillText( text, x, y );
  }

}
