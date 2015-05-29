/**************************************************************************
 *  Copyright (C) 2015 by Richard Crook                                   *
 *  https://github.com/dazzle50/JPlanner                                  *
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

package rjc.jplanner.model;

import java.time.LocalTime;

/*************************************************************************************************/
/******************** Time of day from 00:00:00.000 to 24:00:00.000 inclusive ********************/
/*************************************************************************************************/

public class Time
{
  public static final int MAX = 24 * 3600 * 1000; // milliseconds in day

  private int             m_milliseconds;        // milliseconds from 00:00:00.000 start of day

  // anything between Zero and MAX inclusive is valid, anything else invalid

  /**************************************** constructor ******************************************/
  private Time( int milliseconds )
  {
    // constructor (from pre-validated milliseconds)
    m_milliseconds = milliseconds;
  }

  /**************************************** constructor ******************************************/
  public Time( int hours, int mins, int secs, int ms )
  {
    // valid inputs
    if ( hours < 0 || hours > 24 )
      throw new IllegalArgumentException( "hours=" + hours );

    if ( mins < 0 || mins > 59 )
      throw new IllegalArgumentException( "minutes=" + mins );

    if ( secs < 0 || secs > 59 )
      throw new IllegalArgumentException( "seconds=" + secs );

    if ( ms < 0 || ms > 999 )
      throw new IllegalArgumentException( "milliseconds=" + ms );

    if ( hours == 24 && ( mins > 0 || secs > 0 || ms > 0 ) )
      throw new IllegalArgumentException( "time beyond 24H" );

    m_milliseconds = hours * 3600_000 + mins * 60_000 + secs * 1000 + ms;
  }

  /****************************************** fromHours ******************************************/
  public static Time fromHours( double hours )
  {
    // return a Time from double hours
    if ( hours < 0.0 || hours > 24.0 )
      throw new IllegalArgumentException( "hours=" + hours );

    return new Time( (int) Math.round( hours * 3600_000.0 ) );
  }

  /**************************************** fromLocalTime ****************************************/
  public static Time fromLocalTime( LocalTime localTime )
  {
    // return a new Time from a java.time.LocalTime
    return new Time( (int) ( localTime.toNanoOfDay() / 1_000_000L ) );
  }

  /************************************** fromMilliseconds ***************************************/
  public static Time fromMilliseconds( int milliseconds )
  {
    // return a Time from int milliseconds
    if ( milliseconds < 0 || milliseconds > MAX )
      throw new IllegalArgumentException( "milliseconds=" + milliseconds );

    return new Time( milliseconds );
  }

  /********************************************* now *********************************************/
  public static Time fromNow()
  {
    // return a new Time from current system clock
    return new Time( (int) ( LocalTime.now().toNanoOfDay() / 1_000_000L ) );
  }

  /***************************************** fromString ******************************************/
  public static Time fromString( String str )
  {
    // if string of type HH:MM
    if ( str.matches( "\\d\\d:\\d\\d" ) )
    {
      int hours = Integer.parseInt( str.substring( 0, 2 ) );
      int mins = Integer.parseInt( str.substring( 3, 5 ) );
      return new Time( hours, mins, 0, 0 );
    }

    // if string of type HH:MM:SS
    if ( str.matches( "\\d\\d:\\d\\d:\\d\\d" ) )
    {
      int hours = Integer.parseInt( str.substring( 0, 2 ) );
      int mins = Integer.parseInt( str.substring( 3, 5 ) );
      int secs = Integer.parseInt( str.substring( 6, 8 ) );
      return new Time( hours, mins, secs, 0 );
    }

    // if string of type HH:MM:SS.mmm
    if ( str.matches( "\\d\\d:\\d\\d:\\d\\d.\\d\\d\\d" ) )
    {
      int hours = Integer.parseInt( str.substring( 0, 2 ) );
      int mins = Integer.parseInt( str.substring( 3, 5 ) );
      int secs = Integer.parseInt( str.substring( 6, 8 ) );
      int ms = Integer.parseInt( str.substring( 9, 12 ) );
      return new Time( hours, mins, secs, ms );
    }

    throw new IllegalArgumentException( "String=" + str );
  }

  /******************************************** hours ********************************************/
  public int getHours()
  {
    return m_milliseconds / 3600_000;
  }

  /**************************************** milliseconds *****************************************/
  public int getMilliseconds()
  {
    // return int number of milliseconds from start of day
    return m_milliseconds;
  }

  /******************************************* minutes *******************************************/
  public int getMinutes()
  {
    return m_milliseconds / 60_000 % 60;
  }

  /******************************************* seconds *******************************************/
  public int getSeconds()
  {
    return m_milliseconds / 1000 % 60;
  }

  /****************************************** toString *******************************************/
  @Override
  public String toString()
  {
    // convert to string to "hh:mm:ss.mmm" format
    StringBuilder buf = new StringBuilder( 16 );
    int hour = getHours();
    int minute = getMinutes();
    int second = getSeconds();
    int milli = m_milliseconds % 1000;

    buf.append( hour < 10 ? "0" : "" ).append( hour );
    buf.append( minute < 10 ? ":0" : ":" ).append( minute );
    buf.append( second < 10 ? ":0" : ":" ).append( second );
    buf.append( milli < 10 ? ".0" : "." ).append( milli < 100 ? "0" : "" ).append( milli );

    return buf.toString();
  }
}