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
  private static final char  UNIT_DEFAULT = UNIT_DAYS;

  public static final String NUMPOINT     = "01234567890.";
  public static final String UNITS        = "SMHdwmy";

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
    // construct time-span from string
    this();

    // is string is of zero length, don't do anything more
    if ( str.length() == 0 )
      return;

    // remove any spaces and determine last character
    str = str.replaceAll( "\\s+", "" );
    char lastchr = str.charAt( str.length() - 1 );

    // if last char is not a number digit, check if it is a valid units 
    if ( NUMPOINT.indexOf( lastchr ) < 0 )
    {
      if ( UNITS.indexOf( lastchr ) >= 0 ) // check if valid units
      {
        m_units = lastchr;
        str = str.substring( 0, str.length() - 1 );
      }
      else
      {
        throw new IllegalArgumentException( "Invalid units '" + str + "'" );
      }
    }

    m_num = Double.parseDouble( str );
  }

  /**************************************** constructor ******************************************/
  public TimeSpan( double num, char units )
  {
    // construct time-span from parameters, rounding number based on units
    m_units = units;
    if ( units == UNIT_SECONDS )
      m_num = Math.rint( num );
    else
      m_num = Math.rint( num * 100.0 ) / 100.0;
  }

  /***************************************** toString ********************************************/
  @Override
  public String toString()
  {
    DecimalFormat df = new DecimalFormat( "0" );
    df.setMaximumFractionDigits( 3 );
    return df.format( m_num ) + " " + m_units;
  }

  /******************************************** units ********************************************/
  public char units()
  {
    return m_units;
  }

  /******************************************* number ********************************************/
  public double number()
  {
    return m_num;
  }

  /******************************************** minus ********************************************/
  public TimeSpan minus()
  {
    return new TimeSpan( -m_num, m_units );
  }

  /******************************************* equals ********************************************/
  @Override
  public boolean equals( Object other )
  {
    // return true if this time-span and other time-span are same
    if ( other != null && other instanceof TimeSpan )
    {
      TimeSpan ts = (TimeSpan) other;
      return m_units == ts.m_units && Math.abs( m_num - ts.m_num ) < 0.01;
    }
    else
      return false;
  }

}
