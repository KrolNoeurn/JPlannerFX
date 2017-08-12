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

import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import rjc.jplanner.JPlanner;
import rjc.jplanner.model.Date;
import rjc.jplanner.model.DateTime;

/*************************************************************************************************/
/************************ Pop-up window to display date selection widgets ************************/
/*************************************************************************************************/

class DatePopup extends Popup
{
  private DateEditor          m_parent;
  private DateSelector        m_dateSelector;

  private HBox                m_buttons;          // for today + start + end buttons
  private Button              m_today;            // set date to today
  private Button              m_start;            // set date to plan default start
  private Button              m_end;              // set date to plan actual end

  private static final double SHADOW_RADIUS = 4.0;

  /**************************************** constructor ******************************************/
  public DatePopup( DateEditor parent )
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

    // ensure parent editor is editable when selector is hidden
    setOnHidden( event -> m_parent.setEditable( true ) );

    // keep parent editor and selector synchronised
    m_dateSelector.getEpochDaySpinEditor().textProperty().addListener(
        ( observable, oldT, newT ) -> m_parent.setText( getDate().toString( JPlanner.plan.getDateFormat() ) ) );
  }

  /*************************************** constructPopup ****************************************/
  private void constructPopup()
  {
    // create buttons
    m_today = new Button( "Today" );
    m_start = new Button( "Start" );
    m_end = new Button( "End" );
    m_buttons = new HBox();
    m_buttons.setSpacing( DateSelector.PADDING );
    m_buttons.getChildren().addAll( m_today, m_start, m_end );

    // button actions
    m_today.setOnAction( event -> setDate( Date.now() ) );
    m_start.setOnAction( event -> setDate( JPlanner.plan.getDefaultStart().getDate() ) );
    m_end.setOnAction( event ->
    {
      DateTime end = JPlanner.plan.getLatestTaskEnd();
      if ( end != null )
        setDate( end.getDate() );
    } );

    // set pop-up contents to a date selector with buttons below
    m_dateSelector = new DateSelector( m_buttons );
    getContent().addAll( m_dateSelector );

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

  /******************************************* setDate *******************************************/
  private void setDate( Date date )
  {
    // set selector to specified date
    if ( date != null )
      m_dateSelector.getEpochDaySpinEditor().setInteger( date.getEpochday() );
  }

  /******************************************* getDate *******************************************/
  private Date getDate()
  {
    // return date shown by selector
    return new Date( m_dateSelector.getEpochDaySpinEditor().getInteger() );
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
      setDate( m_parent.getDate() );
      Point2D point = m_parent.localToScreen( 0.0, m_parent.getHeight() );
      show( m_parent, point.getX() - SHADOW_RADIUS + 1.0, point.getY() - SHADOW_RADIUS + 1.0 );
      m_dateSelector.arrange();
      m_today.setPrefWidth(
          m_dateSelector.getPrefWidth() - m_start.getWidth() - m_end.getWidth() - 4 * DateSelector.PADDING );
    }
  }

}
