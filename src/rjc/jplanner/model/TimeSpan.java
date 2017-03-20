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

package rjc.jplanner.model;

import java.text.DecimalFormat;

/*************************************************************************************************/
/********************************** Quantity of time with units **********************************/
/*************************************************************************************************/

public class TimeSpan
{
  private double             m_num;
  private char               m_units;

  public static final char   UNIT_SECONDS = 'S';
  public static final char   UNIT_MINUTES = 'M';
  public static final char   UNIT_HOURS   = 'H';
  public static final char   UNIT_DAYS    = 'd';
  public static final char   UNIT_WEEKS   = 'w';
  public static final char   UNIT_MONTHS  = 'm';
  public static final char   UNIT_YEARS   = 'y';
  public static final char   UNIT_DEFAULT = UNIT_DAYS;
  public static final String VALID_UNITS  = "SMHdwmy";

  // private inner class defining time-span number formatting
  private static final class TimeSpanFormat extends DecimalFormat
  {
    private static final long serialVersionUID = 1L;

    public TimeSpanFormat()
    {
      super();
      applyPattern( "0" );
      setMaximumFractionDigits( 2 );
    }
  }

  private static final TimeSpanFormat FORMAT = new TimeSpanFormat();
  private static final char           POINT  = FORMAT.getDecimalFormatSymbols().getDecimalSeparator();

  /**************************************** constructor ******************************************/
  public TimeSpan()
  {
    // construct default time-span
    m_num = 0.0;
    m_units = UNIT_DEFAULT;
  }

  /**************************************** constructor ******************************************/
  public TimeSpan( String str )
  {
    // construct time-span from string, start with default
    this();

    // if string is null, don't do anything more
    if ( str == null )
      return;

    // if string with white-spaces removed is zero length, don't do anything more
    str = str.replaceAll( "\\s+", "" );
    if ( str.length() == 0 )
      return;

    // if last char is not a number digit, check if it is a valid units 
    char lastchr = str.charAt( str.length() - 1 );
    if ( !isNumberOrPoint( lastchr ) )
    {
      if ( isValidUnits( lastchr ) )
      {
        m_units = lastchr;
        str = str.substring( 0, str.length() - 1 );
      }
      else
        throw new IllegalArgumentException( "Invalid units '" + str + "'" );
    }

    // check remaining string to be simple number
    for ( int c = 0; c < str.length(); c++ )
      if ( "0123456789.+-".indexOf( str.charAt( c ) ) < 0 )
        throw new IllegalArgumentException( "Invalid number '" + str + "'" );

    // set number rounding to 2 decimal places
    m_num = Math.rint( Double.parseDouble( str ) * 100.0 ) / 100.0;
  }

  /**************************************** constructor ******************************************/
  public TimeSpan( double num, char units )
  {
    // construct time-span from parameters, rounding number based on units
    if ( !isValidUnits( units ) )
      throw new IllegalArgumentException( "Invalid units '" + units + "'" );

    m_units = units;
    if ( units == UNIT_SECONDS )
      m_num = Math.rint( num );
    else
      m_num = Math.rint( num * 100.0 ) / 100.0;
  }

  /****************************************** toString *******************************************/
  @Override
  public String toString()
  {
    // return time-span as string
    return FORMAT.format( m_num ) + " " + m_units;
  }

  /****************************************** getUnits *******************************************/
  public char getUnits()
  {
    // return time-span units
    return m_units;
  }

  /****************************************** getNumber ******************************************/
  public double getNumber()
  {
    // return time-span number
    return m_num;
  }

  /******************************************** minus ********************************************/
  public TimeSpan minus()
  {
    // return a new time-span with minus number
    return new TimeSpan( -m_num, m_units );
  }

  /**************************************** isValidUnits *****************************************/
  public static boolean isValidUnits( char unit )
  {
    // return true if specified char is a valid time-span unit
    return VALID_UNITS.indexOf( unit ) >= 0;
  }

  /*************************************** isNumberOrPoint ***************************************/
  public static boolean isNumberOrPoint( char num )
  {
    // return true if specified char is a numeric digit or decimal point
    return ( num >= '0' && num <= '9' ) || num == POINT;
  }

  /******************************************* equals ********************************************/
  @Override
  public boolean equals( Object other )
  {
    // return true if this time-span and other time-span are same
    if ( other instanceof TimeSpan )
    {
      TimeSpan ts = (TimeSpan) other;
      return m_units == ts.m_units && Math.abs( m_num - ts.m_num ) < 0.01;
    }

    return false;
  }

}
