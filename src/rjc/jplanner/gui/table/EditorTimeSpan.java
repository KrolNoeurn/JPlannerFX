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

package rjc.jplanner.gui.table;

import javafx.event.EventHandler;
import javafx.scene.input.ScrollEvent;
import rjc.jplanner.JPlanner;
import rjc.jplanner.gui.SpinEditor;
import rjc.jplanner.model.TimeSpan;

/*************************************************************************************************/
/***************************** Table cell editor for TimeSpan fields *****************************/
/*************************************************************************************************/

public class EditorTimeSpan extends AbstractCellEditor
{
  SpinEditor m_spin; // spin editor

  /**************************************** constructor ******************************************/
  public EditorTimeSpan( int columnIndex, int rowIndex )
  {
    // start with default spin editor
    super( columnIndex, rowIndex );
    m_spin = new SpinEditor();
    setControl( m_spin );
  }

  /******************************************* getValue ******************************************/
  @Override
  public Object getValue()
  {
    // return value as a TimeSpan
    return new TimeSpan( m_spin.getDouble(), getUnits() );
  }

  /******************************************* setValue ******************************************/
  @Override
  public void setValue( Object value )
  {
    JPlanner.trace( value.getClass(), value );

    // set value depending on type
    if ( value instanceof TimeSpan )
      setTimeSpan( (TimeSpan) value );
    else
      setTimeSpan( new TimeSpan( (String) value ) );
  }

  /******************************************** open *********************************************/
  @Override
  public void open( Table table, Object value, MoveDirection move )
  {
    // determine editor maximum width
    int columnPos = table.getColumnPositionByIndex( getColumnIndex() );
    double max = table.getWidth() - table.getXStartByColumnPosition( columnPos ) + 1;

    // determine editor minimum width
    double min = table.getWidthByColumnIndex( getColumnIndex() ) + 1;
    if ( min > max )
      min = max;

    // open editor
    m_spin.setWidths( min, max );
    super.open( table, value, move );

    // add buttons and include table scroll events 
    table.add( m_spin.getButtons() );
    EventHandler<? super ScrollEvent> previousScrollHander = table.getOnScroll();
    table.setOnScroll( event -> m_spin.scrollEvent( event ) );

    // when focus lost, remove buttons and reset table scroll handler
    m_spin.focusedProperty().addListener( ( observable, oldFocus, newFocus ) ->
    {
      if ( !newFocus )
      {
        table.remove( m_spin.getButtons() );
        table.setOnScroll( previousScrollHander );
      }
    } );
  }

  /******************************************* getUnits ******************************************/
  private char getUnits()
  {
    // ???????????????????
    return TimeSpan.UNIT_HOURS;
  }

  /***************************************** setTimeSpan *****************************************/
  private void setTimeSpan( TimeSpan ts )
  {
    // number of seconds must be integer, otherwise non-integer allowed
    if ( ts.units() == TimeSpan.UNIT_SECONDS )
      m_spin.setRange( 0, 9999, 0 );
    else
      m_spin.setRange( 0.0, 9999.99, 2 );

    m_spin.setPrefixSuffix( null, " " + ts.units() );
    m_spin.setDouble( ts.number() );
  }

}
