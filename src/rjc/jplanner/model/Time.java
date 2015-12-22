/**************************************************************************
 *  Copyright (C) 2015 by Richard Crook                                   *
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

package rjc.jplanner.model;

import java.time.LocalTime;

/*************************************************************************************************/
/******************** Time of day from 00:00:00.000 to 24:00:00.000 inclusive ********************/
/*************************************************************************************************/

public class Time
{
  private int              m_milliseconds;                                                    // milliseconds from 00:00:00.000 start of day

  public static final int  MILLISECONDS_IN_DAY = 24 * 3600 * 1000;                            // milliseconds in day
  public static final Time MIN_VALUE           = Time.fromMilliseconds( 0 );
  public static final Time MAX_VALUE           = Time.fromMilliseconds( MILLISECONDS_IN_DAY );

  // anything between Zero and MAX inclusive is valid, anything else invalid

  /* ======================================= constructor ======================================= */
  private Time( int milliseconds )
  {
    // constructor (from pre-validated milliseconds) hence *PRIVATE*
    m_milliseconds = milliseconds;
  }

  /**************************************** constructor ******************************************/
  public Time( LocalTime localTime )
  {
    // return a new Time from a java.time.LocalTime
    m_milliseconds = (int) ( localTime.toNanoOfDay() / 1_000_000L );
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

  /**************************************** milliseconds *****************************************/
  public int milliseconds()
  {
    // return int number of milliseconds from start of day
    return m_milliseconds;
  }

  /***************************************** fromString ******************************************/
  public static Time fromString( String str )
  {
    // split the time hours:mins:secs by colon separator
    String[] parts = str.split( ":" );
    if ( parts.length < 2 )
      throw new IllegalArgumentException( "str=" + str );

    // hours & minutes parts must be integers
    int hours = Integer.parseInt( parts[0] );
    int mins = Integer.parseInt( parts[1] );
    if ( parts.length == 2 )
      return new Time( hours, mins, 0, 0 );

    // split seconds into integer and milliseconds sections
    String[] seconds = parts[2].split( "\\." );
    int secs = Integer.parseInt( seconds[0] );
    if ( seconds.length == 1 )
      return new Time( hours, mins, secs, 0 );

    // ensure we look at first three digits only for milliseconds
    String milli = ( seconds[1] + "00" ).substring( 0, 3 );
    int ms = Integer.parseInt( milli );
    return new Time( hours, mins, secs, ms );
  }

  /****************************************** fromHours ******************************************/
  public static Time fromHours( double hours )
  {
    // return a Time from double hours
    if ( hours < 0.0 || hours > 24.0 )
      throw new IllegalArgumentException( "hours=" + hours );

    return new Time( (int) Math.round( hours * 3600_000.0 ) );
  }

  /************************************** fromMilliseconds ***************************************/
  public static Time fromMilliseconds( int milliseconds )
  {
    // return a Time from int milliseconds
    if ( milliseconds < 0 || milliseconds > MILLISECONDS_IN_DAY )
      throw new IllegalArgumentException( "milliseconds=" + milliseconds );

    return new Time( milliseconds );
  }

  /****************************************** toString *******************************************/
  @Override
  public String toString()
  {
    // convert to string to "hh:mm:ss.mmm" format
    StringBuilder buf = new StringBuilder( 16 );
    int hour = hours();
    int minute = minutes();
    int second = seconds();
    int milli = m_milliseconds % 1000;

    buf.append( hour < 10 ? "0" : "" ).append( hour );
    buf.append( minute < 10 ? ":0" : ":" ).append( minute );
    buf.append( second < 10 ? ":0" : ":" ).append( second );
    buf.append( milli < 10 ? ".0" : "." ).append( milli < 100 ? "0" : "" ).append( milli );

    return buf.toString();
  }

  /****************************************** toString *******************************************/
  public String toStringShort()
  {
    // convert to string to "hh:mm" format
    StringBuilder buf = new StringBuilder( 6 );
    int hour = hours();
    int minute = minutes();

    buf.append( hour < 10 ? "0" : "" ).append( hour );
    buf.append( minute < 10 ? ":0" : ":" ).append( minute );

    return buf.toString();
  }

  /********************************************* now *********************************************/
  public static Time now()
  {
    // return a new Time from current system clock
    return new Time( (int) ( LocalTime.now().toNanoOfDay() / 1_000_000L ) );
  }

  /******************************************** hours ********************************************/
  public int hours()
  {
    return m_milliseconds / 3600_000;
  }

  /******************************************* minutes *******************************************/
  public int minutes()
  {
    return m_milliseconds / 60_000 % 60;
  }

  /******************************************* seconds *******************************************/
  public int seconds()
  {
    return m_milliseconds / 1000 % 60;
  }

  /******************************************* equals ********************************************/
  public boolean equals( Time other )
  {
    return m_milliseconds == other.m_milliseconds;
  }

  /*************************************** addMilliseconds ***************************************/
  public Time addMilliseconds( int ms )
  {
    m_milliseconds += ms;
    return this;
  }

}