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

package rjc.jplanner.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/*************************************************************************************************/
/************************************ Date (with no timezone) ************************************/
/*************************************************************************************************/

public class Date
{
  private int                 m_epochday;                               // simple count of days where day 0 is 01-Jan-1970

  // min int=-2^31 gives minimum date of approx 5,800,000 BC
  public static final Date    MIN_VALUE = new Date( Integer.MIN_VALUE );

  // max int=2^31-1 gives maximum date of approx 5,800,000 AD
  public static final Date    MAX_VALUE = new Date( Integer.MAX_VALUE );

  private static final char   QUOTE     = '\'';
  private static final char   CHARB     = 'B';
  private static final String CODE      = "#@B!";

  /**************************************** constructor ******************************************/
  public Date( int epochday )
  {
    // constructor from epoch-day
    m_epochday = epochday;
  }

  /**************************************** constructor ******************************************/
  public Date( int year, int month, int day )
  {
    // constructor from specified year, month, day
    m_epochday = (int) LocalDate.of( year, month, day ).toEpochDay();
  }

  /**************************************** constructor ******************************************/
  public Date( LocalDate localDate )
  {
    // return a new Date from LocalDate
    m_epochday = (int) localDate.toEpochDay();
  }

  /**************************************** getEpochday ******************************************/
  public int getEpochday()
  {
    // return int count of days from day 0 is 01-Jan-1970
    return m_epochday;
  }

  /****************************************** toString *******************************************/
  @Override
  public String toString()
  {
    // convert to string in ISO-8601 format "uuuu-MM-dd"
    LocalDate ld = LocalDate.ofEpochDay( m_epochday );
    return ld.toString();
  }

  /****************************************** toString *******************************************/
  public String toString( String format )
  {
    // convert to string in specified format
    LocalDate ld = LocalDate.ofEpochDay( m_epochday );

    // to support half-of-year using Bs, quote any unquoted Bs in format
    StringBuilder newFormat = new StringBuilder();
    boolean inQuote = false;
    boolean inB = false;
    char here;
    for ( int i = 0; i < format.length(); i++ )
    {
      here = format.charAt( i );

      // are we in quoted text?
      if ( here == QUOTE )
        inQuote = !inQuote;

      // replace unquoted Bs with special code
      if ( inB && here == CHARB )
      {
        newFormat.append( CODE );
        continue;
      }

      // come to end of unquoted Bs
      if ( inB && here != CHARB )
      {
        newFormat.append( QUOTE );
        inB = false;
        inQuote = false;
      }

      // start of unquoted Bs, start quote with special code
      if ( !inQuote && here == CHARB )
      {
        // avoid creating double quotes
        if ( newFormat.length() > 0 && newFormat.charAt( newFormat.length() - 1 ) == QUOTE )
        {
          newFormat.deleteCharAt( newFormat.length() - 1 );
          newFormat.append( CODE );
        }
        else
          newFormat.append( "'" + CODE );
        inQuote = true;
        inB = true;
      }
      else
      {
        newFormat.append( here );
      }
    }

    // close quote if quote still open
    if ( inQuote )
      newFormat.append( QUOTE );

    String str = ld.format( DateTimeFormatter.ofPattern( newFormat.toString() ) );

    // no special code so can return string immediately
    if ( !str.contains( CODE ) )
      return str;

    // determine half-of-year
    String yearHalf;
    if ( getMonth() < 7 )
      yearHalf = "1";
    else
      yearHalf = "2";

    // four or more Bs is not allowed
    String Bs = CODE + CODE + CODE + CODE;
    if ( str.contains( Bs ) )
      throw new IllegalArgumentException( "Too many pattern letters: B" );

    // replace three Bs
    Bs = CODE + CODE + CODE;
    if ( yearHalf.equals( "1" ) )
      str = str.replace( Bs, yearHalf + "st half" );
    else
      str = str.replace( Bs, yearHalf + "nd half" );

    // replace two Bs
    Bs = CODE + CODE;
    str = str.replace( Bs, "H" + yearHalf );

    // replace one Bs
    Bs = CODE;
    str = str.replace( Bs, yearHalf );

    return str;
  }

  /********************************************* now *********************************************/
  public static Date now()
  {
    // return a new Date from current system clock
    return new Date( (int) LocalDate.now().toEpochDay() );
  }

  /***************************************** fromString ******************************************/
  public static Date fromString( String str )
  {
    // if string of type YYYY-MM-DD or YYYY/MM/DD
    try
    {
      String[] parts = str.split( "/" );
      if ( parts.length != 3 )
        parts = str.split( "-" );

      int year = Integer.parseInt( parts[0] );
      int mon = Integer.parseInt( parts[1] );
      int day = Integer.parseInt( parts[2] );
      return new Date( year, mon, day );
    }
    catch ( Exception exception )
    {
      // some sort of exception thrown
      exception.printStackTrace();
      throw new IllegalArgumentException( "String=" + str );
    }
  }

  /****************************************** getYear ********************************************/
  public int getYear()
  {
    LocalDate ld = LocalDate.ofEpochDay( m_epochday );
    return ld.getYear();
  }

  /****************************************** getMonth *******************************************/
  public int getMonth()
  {
    // return month of year as number 1 to 12
    LocalDate ld = LocalDate.ofEpochDay( m_epochday );
    return ld.getMonthValue();
  }

  /*************************************** getDayOfMonth *****************************************/
  public int getDayOfMonth()
  {
    LocalDate ld = LocalDate.ofEpochDay( m_epochday );
    return ld.getDayOfMonth();
  }

  /***************************************** increment *******************************************/
  public void increment()
  {
    m_epochday++;
  }

  /***************************************** decrement *******************************************/
  public void decrement()
  {
    m_epochday--;
  }

  /****************************************** plusDays *******************************************/
  public Date plusDays( int days )
  {
    return new Date( m_epochday + days );
  }

  /***************************************** plusWeeks *******************************************/
  public Date plusWeeks( int weeks )
  {
    return new Date( m_epochday + 7 * weeks );
  }

  /***************************************** plusMonths ******************************************/
  public Date plusMonths( int months )
  {
    return new Date( LocalDate.ofEpochDay( m_epochday ).plusMonths( months ) );
  }

  /***************************************** plusYears *******************************************/
  public Date plusYears( int years )
  {
    return new Date( LocalDate.ofEpochDay( m_epochday ).plusYears( years ) );
  }

  /******************************************* equals ********************************************/
  public boolean equals( Date other )
  {
    return m_epochday == other.m_epochday;
  }

}