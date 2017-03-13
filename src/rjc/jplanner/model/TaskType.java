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
  private static final int     TYPES_COUNT  = 5;                 // number of different task types

  public static final TaskType ASAP_FDUR    = new TaskType( 0 ); // early as possible - fixed duration
  public static final TaskType ASAP_FWORK   = new TaskType( 1 ); // early as possible - fixed work
  public static final TaskType SON_FDUR     = new TaskType( 2 ); // start on - fixed duration
  public static final TaskType SON_FWORK    = new TaskType( 3 ); // start on - fixed work
  public static final TaskType FIXED_PERIOD = new TaskType( 4 ); // fixed period

  private short                m_type;

  /***************************************** constructor *****************************************/
  private TaskType( int num )
  {
    // create task type based on number
    m_type = (short) num;
  }

  /****************************************** toString *******************************************/
  @Override
  public String toString()
  {
    // returns string representation
    return toString( m_type );
  }

  /****************************************** toString *******************************************/
  public static String toString( int num )
  {
    // return task type string for n'th task type
    switch ( num )
    {
      case 0:
        return "ASAP - duration";
      case 1:
        return "ASAP - work";
      case 2:
        return "Start on - duration";
      case 3:
        return "Start on - work";
      case 4:
        return "Fixed period";
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
      if ( this == ASAP_FWORK || this == SON_FWORK || this == FIXED_PERIOD )
        return false;

    if ( section == Task.SECTION_START )
      if ( this == ASAP_FDUR || this == ASAP_FWORK )
        return false;

    if ( section == Task.SECTION_END )
      if ( this == ASAP_FDUR || this == ASAP_FWORK || this == SON_FDUR || this == SON_FWORK )
        return false;

    if ( section == Task.SECTION_WORK )
      if ( this == ASAP_FDUR || this == SON_FDUR || this == FIXED_PERIOD )
        return false;

    return true;
  }

  /******************************************** from *********************************************/
  public static TaskType from( String text )
  {
    // return task-type from string
    if ( text.equals( TaskType.ASAP_FDUR.toString() ) )
      return TaskType.ASAP_FDUR;

    if ( text.equals( TaskType.ASAP_FWORK.toString() ) )
      return TaskType.ASAP_FWORK;

    if ( text.equals( TaskType.FIXED_PERIOD.toString() ) )
      return TaskType.FIXED_PERIOD;

    if ( text.equals( TaskType.SON_FDUR.toString() ) )
      return TaskType.SON_FDUR;

    if ( text.equals( TaskType.SON_FWORK.toString() ) )
      return TaskType.SON_FWORK;

    throw new IllegalArgumentException( "Invalid TaskType '" + text + "'" );
  }

}
