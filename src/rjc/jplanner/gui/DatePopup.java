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
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.stage.Popup;
import rjc.jplanner.model.Date;

/*************************************************************************************************/
/********************* Pop-up window to display date-time selection widgets **********************/
/*************************************************************************************************/

class DatePopup extends Popup
{
  private DateEditor          m_parent;
  private DateSelector        m_dateSelector;

  private static final double SHADOW_RADIUS = 4.0;

  /**************************************** constructor ******************************************/
  public DatePopup( DateEditor parent )
  {
    // create pop-up window to display date-time selection widgets
    m_parent = parent;
    setAutoHide( true );
    setConsumeAutoHidingEvents( false );

    // set popup contents to a date selector
    m_dateSelector = new DateSelector();
    getContent().add( m_dateSelector );

    // add shadow to popup
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

    // keep parent editor and this selector synchronised
    m_parent.textProperty().addListener( ( observable, oldText, newText ) -> setDate( m_parent.getDate() ) );
  }

  /***************************************** setDate *****************************************/
  private void setDate( Date date )
  {
    // set selector to specified date
    m_dateSelector.getDateProperty().set( date );
  }

  /******************************************* getDate *******************************************/
  private Date getDate()
  {
    // return date shown by selector
    return m_dateSelector.getDateProperty().get();
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
      Point2D point = m_parent.localToScreen( 0.0, m_parent.getHeight() );
      show( m_parent, point.getX() - SHADOW_RADIUS + 1.0, point.getY() - SHADOW_RADIUS + 1.0 );
      m_dateSelector.arrangeSelector();
    }
  }

}
