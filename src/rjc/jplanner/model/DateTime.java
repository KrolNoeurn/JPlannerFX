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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/*************************************************************************************************/
/********************************* Date-time (with no timezone) **********************************/
/*************************************************************************************************/

public class DateTime
{
  public enum Interval
  {
    YEAR, HALFYEAR, QUARTERYEAR, MONTH, WEEK, DAY
  }

  public static final long MILLISECONDS_IN_DAY = 24 * 3600 * 1000; // milliseconds in day

  private long             m_milliseconds;                         // milliseconds from 00:00:00.000 start of epoch-day

  /***************************************** constructor *****************************************/
  public DateTime( Date date, Time time )
  {
    // constructor
    m_milliseconds = date.getEpochday() * MILLISECONDS_IN_DAY + time.getMilliseconds();
  }

  /***************************************** constructor *****************************************/
  public DateTime( LocalDateTime dt )
  {
    // constructor
    Date date = Date.fromLocalDate( dt.toLocalDate() );
    Time time = Time.fromLocalTime( dt.toLocalTime() );
    m_milliseconds = date.getEpochday() * MILLISECONDS_IN_DAY + time.getMilliseconds();
  }

  /***************************************** constructor *****************************************/
  public DateTime( long ms )
  {
    // constructor
    m_milliseconds = ms;
  }

  /***************************************** constructor *****************************************/
  public DateTime( String str )
  {
    // constructor, date must be split from time by a space
    int split = str.indexOf( ' ' );
    Date date = Date.fromString( str.substring( 0, split ) );
    Time time = Time.fromString( str.substring( split + 1, str.length() ) );
    m_milliseconds = date.getEpochday() * MILLISECONDS_IN_DAY + time.getMilliseconds();
  }

  /********************************************* now *********************************************/
  public static DateTime fromNow()
  {
    // return a new DateTime from current system clock
    return new DateTime( LocalDateTime.now() );
  }

  /******************************************* addDays *******************************************/
  public DateTime addDays( int days )
  {
    // return new date-time specified days added or subtracted
    return new DateTime( m_milliseconds + days * MILLISECONDS_IN_DAY );
  }

  /***************************************** addInterval *****************************************/
  public DateTime addInterval( Interval interval )
  {
    // add one specified interval to date-time
    if ( interval == Interval.YEAR )
    {
      Time time = getTime();
      Date date = getDate();
      Date year = new Date( date.getYear() + 1, date.getMonth(), date.getDay() );
      return new DateTime( year, time );
    }

    if ( interval == Interval.HALFYEAR )
      return addMonths( 6 );

    if ( interval == Interval.QUARTERYEAR )
      return addMonths( 3 );

    if ( interval == Interval.MONTH )
      return addMonths( 1 );

    if ( interval == Interval.WEEK )
      return addDays( 7 );

    if ( interval == Interval.DAY )
      return addDays( 1 );

    throw new IllegalArgumentException( "interval=" + interval );
  }

  /*************************************** addMilliseconds ***************************************/
  public DateTime addMilliseconds( long ms )
  {
    return new DateTime( m_milliseconds + ms );
  }

  /****************************************** addMonths ******************************************/
  public DateTime addMonths( int months )
  {
    // return new date-time specified months added or subtracted
    LocalDateTime ldt = LocalDateTime.ofEpochSecond( m_milliseconds / 1000L, (int) ( m_milliseconds % 1000 * 1000000 ),
        ZoneOffset.UTC );
    ldt = ldt.plusMonths( months );
    return new DateTime( ldt );
  }

  /******************************************** date *********************************************/
  public Date getDate()
  {
    if ( m_milliseconds < 0 )
      return new Date( (int) ( m_milliseconds / MILLISECONDS_IN_DAY ) - 1 );

    return new Date( (int) ( m_milliseconds / MILLISECONDS_IN_DAY ) );
  }

  /********************************************* day *********************************************/
  public int getDay()
  {
    return getDate().getDay();
  }

  /******************************************** hours ********************************************/
  public int getHours()
  {
    return getTime().getHours();
  }

  /***************************************** milliseconds ****************************************/
  public long getMilliseconds()
  {
    return m_milliseconds;
  }

  /******************************************* minutes *******************************************/
  public int getMinutes()
  {
    return getTime().getMinutes();
  }

  /******************************************** month ********************************************/
  public int getMonth()
  {
    return getDate().getMonth();
  }

  /******************************************* seconds *******************************************/
  public int getSeconds()
  {
    return getTime().getSeconds();
  }

  /******************************************** time *********************************************/
  public Time getTime()
  {
    int ms = (int) ( m_milliseconds % MILLISECONDS_IN_DAY );
    if ( ms < 0 )
      ms += MILLISECONDS_IN_DAY;

    return Time.fromMilliseconds( ms );
  }

  /******************************************** year *********************************************/
  public int getYear()
  {
    return getDate().getYear();
  }

  /****************************************** toString *******************************************/
  @Override
  public String toString()
  {
    // convert to string to "YYYY-MM-DD hh:mm:ss.mmm" format
    return getDate().toString() + " " + getTime().toString();
  }

  /****************************************** toString *******************************************/
  public String toString( String format )
  {
    // convert to string in specified format
    LocalDateTime ldt = LocalDateTime.ofEpochSecond( m_milliseconds / 1000L, (int) ( m_milliseconds % 1000 * 1000000 ),
        ZoneOffset.UTC );

    return ldt.format( DateTimeFormatter.ofPattern( format ) );
  }

  /******************************************** trunc ********************************************/
  public DateTime trunc( Interval interval )
  {
    // return new date-time truncated down to specified interval
    if ( interval == Interval.YEAR )
    {
      Date date = new Date( getDate().getYear(), 1, 1 );
      return new DateTime( date.getEpochday() * MILLISECONDS_IN_DAY );
    }

    if ( interval == Interval.HALFYEAR )
    {
      Date date = getDate();
      int month = ( ( date.getMonth() - 1 ) / 6 ) * 6 + 1;

      Date hy = new Date( date.getYear(), month, 1 );
      return new DateTime( hy.getEpochday() * MILLISECONDS_IN_DAY );
    }

    if ( interval == Interval.QUARTERYEAR )
    {
      Date date = getDate();
      int month = ( ( date.getMonth() - 1 ) / 3 ) * 3 + 1;

      Date qy = new Date( date.getYear(), month, 1 );
      return new DateTime( qy.getEpochday() * MILLISECONDS_IN_DAY );
    }

    if ( interval == Interval.MONTH )
    {
      Date date = getDate();
      Date md = new Date( date.getYear(), date.getMonth(), 1 );
      return new DateTime( md.getEpochday() * MILLISECONDS_IN_DAY );
    }

    if ( interval == Interval.WEEK )
    {
      int day = (int) ( m_milliseconds / MILLISECONDS_IN_DAY );
      int dayOfWeek = ( day + 3 ) % 7;
      return new DateTime( ( day - dayOfWeek ) * MILLISECONDS_IN_DAY );
    }

    if ( interval == Interval.DAY )
    {
      long ms = ( m_milliseconds / MILLISECONDS_IN_DAY ) * MILLISECONDS_IN_DAY;
      return new DateTime( ms );
    }

    throw new IllegalArgumentException( "interval=" + interval );
  }

}