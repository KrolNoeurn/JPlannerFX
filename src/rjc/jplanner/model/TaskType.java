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

/*************************************************************************************************/
/***************************** Task type regarding starting & length *****************************/
/*************************************************************************************************/

public class TaskType
{
  public static final String ASAP_FDUR    = "ASAP - duration";    // Early as possible - fixed duration
  public static final String ASAP_FWORK   = "ASAP - work";        // Early as possible - fixed work
  public static final String SON_FDUR     = "Start on - duration"; // Start on - fixed duration
  public static final String SON_FWORK    = "Start on - work";    // Start on - fixed work
  public static final String FIXED_PERIOD = "Fixed period";       // Fixed period

  private String             m_type;

  /***************************************** constructor *****************************************/
  public TaskType( String str )
  {
    // create type type, don't assume string-pointer is correct even if string is valid 
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
      throw new IllegalArgumentException( str );
  }

  /****************************************** toString *******************************************/
  @Override
  public String toString()
  {
    // returns string representation
    return m_type;
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
}
