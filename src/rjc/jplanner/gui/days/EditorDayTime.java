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

package rjc.jplanner.gui.days;

import rjc.jplanner.JPlanner;
import rjc.jplanner.gui.SpinEditor;
import rjc.jplanner.gui.table.AbstractCellEditor;
import rjc.jplanner.model.Day;
import rjc.jplanner.model.Time;

/*************************************************************************************************/
/******************* Table cell editor for day-type period start-or-end time *********************/
/*************************************************************************************************/

class EditorDayTime extends AbstractCellEditor
{
  SpinEditor m_spin; // spin editor
  int        m_min;  // minimum valid time in milliseconds
  int        m_max;  // maximum valid time in milliseconds

  /**************************************** constructor ******************************************/
  public EditorDayTime( int columnIndex, int row )
  {
    // use spin editor
    super( columnIndex, row );
    m_spin = new SpinEditor();
    m_spin.setFormat( "00" );
    setControl( m_spin );

    // determine min & max valid times
    m_min = 0;
    m_max = Time.MILLISECONDS_IN_DAY;
    Day day = JPlanner.plan.daytypes.get( row );
    if ( columnIndex > Day.SECTION_START1 )
    {
      m_min = ( (Time) day.getValue( columnIndex - 1 ) ).getDayMilliseconds();
      m_min += 60000;
    }
    int num = day.getNumberOfPeriods();
    if ( columnIndex < Day.SECTION_START1 + 2 * num - 1 )
    {
      m_max = ( (Time) day.getValue( columnIndex + 1 ) ).getDayMilliseconds();
      m_max -= 60000;
    }

    // add listener text changes to wrap hours and minutes, and set error status
    addListener( ( observable, oldText, newText ) ->
    {
      // as prefix hold hours, if not null means minutes are being edited
      if ( m_spin.getPrefix() != null )
      {
        // if minutes greater than 59, increase hours
        int minutes = m_spin.getInteger();
        if ( minutes > 59 )
        {
          String prefix = m_spin.getPrefix();
          int hours = minutes / 60 + Integer.parseInt( prefix.substring( 0, prefix.length() - 1 ) );
          m_spin.setPrefixSuffix( hours + ":", null );
          m_spin.setInteger( minutes % 60 );
          return;
        }

        // if minutes less than 0, decrease hours
        if ( minutes < 0 )
        {
          String prefix = m_spin.getPrefix();
          int hours = ( minutes - 60 ) / 60 + Integer.parseInt( prefix.substring( 0, prefix.length() - 1 ) );
          m_spin.setPrefixSuffix( hours + ":", null );
          m_spin.setInteger( ( minutes + 60 ) % 60 );
          return;
        }
      }

      // check editor value and if error
      String error = check( newText );
      if ( error == null )
        JPlanner.setNoError( getControl(), "" );
      else
        JPlanner.setError( getControl(), error );
    } );

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
    if ( time.getDayMilliseconds() < m_min || time.getDayMilliseconds() > m_max )
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
      // default to editing minutes, with hours in prefix
      Time time = (Time) value;
      String prefix = time.getHours() + ":";
      m_spin.setPrefixSuffix( prefix, null );
      m_spin.setInteger( time.getMinutes() );
      m_spin.setRange( -99, 99, 0 );
      m_spin.setStepPage( 1, 10 );
    }
    else
      m_spin.setValue( (String) value );
  }

  /***************************************** isValueValid ****************************************/
  @Override
  public boolean isValueValid( Object value )
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
