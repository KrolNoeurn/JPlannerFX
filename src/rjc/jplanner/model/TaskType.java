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

/*************************************************************************************************/
/***************************** Task type regarding starting & length *****************************/
/*************************************************************************************************/

public class TaskType
{
  private static final int   TYPES_COUNT  = 5;                     // number of different task types

  public static final String ASAP_FDUR    = "ASAP - duration";     // early as possible - fixed duration
  public static final String ASAP_FWORK   = "ASAP - work";         // early as possible - fixed work
  public static final String SON_FDUR     = "Start on - duration"; // start on - fixed duration
  public static final String SON_FWORK    = "Start on - work";     // start on - fixed work
  public static final String FIXED_PERIOD = "Fixed period";        // fixed period

  private String             m_type;

  /***************************************** constructor *****************************************/
  public TaskType( String str )
  {
    // create task type, don't assume string-pointer is correct even if string is valid
    if ( str.equals( ASAP_FDUR ) )
      m_type = ASAP_FDUR;

    else if ( str.equals( ASAP_FWORK ) )
      m_type = ASAP_FWORK;

    else if ( str.equals( SON_FDUR ) )
      m_type = SON_FDUR;

    else if ( str.equals( SON_FWORK ) )
      m_type = SON_FWORK;

    else if ( str.equals( FIXED_PERIOD ) )
      m_type = FIXED_PERIOD;

    else
      throw new IllegalArgumentException( "str=" + str );
  }

  /***************************************** constructor *****************************************/
  public TaskType( int num )
  {
    // create task type based on number
    m_type = TaskType.toString( num );
  }

  /****************************************** toString *******************************************/
  @Override
  public String toString()
  {
    // returns string representation
    return m_type;
  }

  /****************************************** toString *******************************************/
  public static String toString( int num )
  {
    // return task type string for n'th task type
    switch ( num )
    {
      case 0:
        return ASAP_FDUR;
      case 1:
        return ASAP_FWORK;
      case 2:
        return SON_FDUR;
      case 3:
        return SON_FWORK;
      case 4:
        return FIXED_PERIOD;
      default:
        throw new IllegalArgumentException( "num=" + num );
    }
  }

  /******************************************** count *********************************************/
  public static int count()
  {
    // return number of valid task types (used for drop-down lists etc)
    return TYPES_COUNT;
  }

  /************************************** isSectionEditable **************************************/
  public boolean isSectionEditable( int section )
  {
    // returns true if section is editable based on task type
    if ( section == Task.SECTION_DURATION )
      if ( m_type == ASAP_FWORK || m_type == SON_FWORK || m_type == FIXED_PERIOD )
        return false;

    if ( section == Task.SECTION_START )
      if ( m_type == ASAP_FDUR || m_type == ASAP_FWORK )
        return false;

    if ( section == Task.SECTION_END )
      if ( m_type == ASAP_FDUR || m_type == ASAP_FWORK || m_type == SON_FDUR || m_type == SON_FWORK )
        return false;

    if ( section == Task.SECTION_WORK )
      if ( m_type == ASAP_FDUR || m_type == SON_FDUR || m_type == FIXED_PERIOD )
        return false;

    return true;
  }

  /****************************************** hashCode *******************************************/
  @Override
  public int hashCode()
  {
    // return hash code which is hash code of type string
    return m_type.hashCode();
  }

  /******************************************* equals ********************************************/
  @Override
  public boolean equals( Object other )
  {
    // return true if this task-type and other task-type are same
    if ( other instanceof TaskType )
      return m_type == ( (TaskType) other ).m_type;
    else
      return false;
  }

}
