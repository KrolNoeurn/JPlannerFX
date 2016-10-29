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

package rjc.jplanner.gui.days;

import rjc.jplanner.JPlanner;
import rjc.jplanner.gui.SpinEditor;
import rjc.jplanner.gui.XTextField;
import rjc.jplanner.gui.table.AbstractCellEditor;
import rjc.jplanner.model.Day;
import rjc.jplanner.model.Time;

/*************************************************************************************************/
/******************* Table cell editor for day-type period start-or-end time *********************/
/*************************************************************************************************/

public class EditorDayTime extends AbstractCellEditor
{
  SpinEditor m_spin; // spin editor
  int        m_min;  // minimum valid time in milliseconds
  int        m_max;  // maximum valid time in milliseconds

  /**************************************** constructor ******************************************/
  public EditorDayTime( int columnIndex, int rowIndex )
  {
    // use spin editor
    super( columnIndex, rowIndex );
    m_spin = new SpinEditor();
    m_spin.setFormat( "00" );
    setControl( m_spin );

    // determine min & max valid times
    m_min = 0;
    m_max = Time.MILLISECONDS_IN_DAY;
    Day day = JPlanner.plan.daytypes.get( rowIndex );
    if ( columnIndex > Day.SECTION_START1 )
    {
      m_min = ( (Time) day.getValue( columnIndex - 1 ) ).milliseconds();
      m_min += 60000;
    }
    int num = day.numPeriods();
    if ( columnIndex < Day.SECTION_START1 + 2 * num - 1 )
    {
      m_max = ( (Time) day.getValue( columnIndex + 1 ) ).milliseconds();
      m_max -= 60000;
    }

    // add listener to set error status
    ( (XTextField) getControl() ).textProperty()
        .addListener( ( observable, oldText, newText ) -> JPlanner.gui.setError( getControl(), check( newText ) ) );

  }

  /******************************************* check ******************************************/
  private String check( String text )
  {
    // check text is a valid time
    Time time;
    try
    {
      time = Time.fromString( text );
    }
    catch ( Exception exception )
    {
      return "Time is not valid";
    }

    // check time is between min & max
    if ( time.milliseconds() < m_min || time.milliseconds() > m_max )
      return "Time not between " + Time.fromMilliseconds( m_min ).toStringShort() + " and "
          + Time.fromMilliseconds( m_max ).toStringShort();

    return null;
  }

  /******************************************* getValue ******************************************/
  @Override
  public Object getValue()
  {
    // return priority value as Time
    return Time.fromString( m_spin.getText() );
  }

  /******************************************* setValue ******************************************/
  @Override
  public void setValue( Object value )
  {
    // set value depending on type
    if ( value instanceof Time )
    {
      Time time = (Time) value;
      String prefix = time.hours() + ":";
      m_spin.setPrefixSuffix( prefix, null );
      m_spin.setInteger( time.minutes() );
    }
    else
      m_spin.setTextCore( (String) value );
  }

  /****************************************** validValue *****************************************/
  @Override
  public boolean validValue( Object value )
  {
    // value is valid if null or converts to a integer or is colon punctuation
    if ( value == null )
      return true;

    try
    {
      String str = (String) value;

      if ( str.equals( ":" ) )
        return true; // is colon punctuation

      Integer.parseInt( (String) value );
      return true; // converts to a integer
    }
    catch ( Exception exception )
    {
      return false; // not valid integer
    }
  }
}
