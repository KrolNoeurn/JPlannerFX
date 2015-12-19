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

import java.time.LocalDate;

/*************************************************************************************************/
/************************************ Date (with no timezone) ************************************/
/*************************************************************************************************/

public class Date
{
  private int m_epochday; // simple count of days where day 0 is 01-Jan-1970

  // min int=-2^31 gives minimum date of approx 5,800,000 BC
  // max int=2^31-1 gives maximum date of approx 5,800,000 AD

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

  /**************************************** fromLocalDate ****************************************/
  public static Date fromLocalDate( LocalDate localDate )
  {
    // return a new Date from LocalDate
    return new Date( (int) localDate.toEpochDay() );
  }

  /********************************************* now *********************************************/
  public static Date fromNow()
  {
    // return a new Date from current system clock
    return new Date( (int) LocalDate.now().toEpochDay() );
  }

  /***************************************** fromString ******************************************/
  public static Date fromString( String str )
  {
    // if string of type YYYY-MM-DD or YYYY/MM/DD
    if ( str.matches( "\\d\\d\\d\\d-\\d\\d-\\d\\d" ) || str.matches( "\\d\\d\\d\\d/\\d\\d/\\d\\d" ) )
    {
      int year = Integer.parseInt( str.substring( 0, 4 ) );
      int mon = Integer.parseInt( str.substring( 5, 7 ) );
      int day = Integer.parseInt( str.substring( 8, 10 ) );
      return new Date( year, mon, day );
    }

    throw new IllegalArgumentException( "String=" + str );
  }

  /******************************************* addDays *******************************************/
  public Date addDays( int days )
  {
    return new Date( m_epochday + days );
  }

  /********************************************* day *********************************************/
  public int getDay()
  {
    LocalDate ld = LocalDate.ofEpochDay( m_epochday );
    return ld.getDayOfMonth();
  }

  /****************************************** epochday *******************************************/
  public int getEpochday()
  {
    // return int count of days from day 0 is 01-Jan-1970
    return m_epochday;
  }

  /******************************************** month ********************************************/
  public int getMonth()
  {
    // return month of year as number 1 to 12
    LocalDate ld = LocalDate.ofEpochDay( m_epochday );
    return ld.getMonthValue();
  }

  /******************************************** year *********************************************/
  public int getYear()
  {
    LocalDate ld = LocalDate.ofEpochDay( m_epochday );
    return ld.getYear();
  }

  /****************************************** toString *******************************************/
  @Override
  public String toString()
  {
    // convert to string in "YYYY-MM-DD" format
    LocalDate ld = LocalDate.ofEpochDay( m_epochday );
    return ld.toString();
  }
}