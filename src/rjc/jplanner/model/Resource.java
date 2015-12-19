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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import rjc.jplanner.JPlanner;

/*************************************************************************************************/
/************************************* Single plan resource **************************************/
/*************************************************************************************************/

public class Resource
{
  private String              m_initials;                       // must be unique across all resources in model
  private String              m_name;                           // free text
  private String              m_org;                            // free text
  private String              m_group;                          // free text
  private String              m_role;                           // free text
  private String              m_alias;                          // free text
  private Date                m_start;                          // date availability starts inclusive
  private Date                m_end;                            // date availability end inclusive
  private double              m_availability;                   // number available
  private double              m_cost;                           // cost TODO
  private Calendar            m_calendar;                       // calendar for resource
  private String              m_comment;                        // free text

  public static final int     SECTION_INITIALS = 0;
  public static final int     SECTION_NAME     = 1;
  public static final int     SECTION_ORG      = 2;
  public static final int     SECTION_GROUP    = 3;
  public static final int     SECTION_ROLE     = 4;
  public static final int     SECTION_ALIAS    = 5;
  public static final int     SECTION_START    = 6;
  public static final int     SECTION_END      = 7;
  public static final int     SECTION_AVAIL    = 8;
  public static final int     SECTION_COST     = 9;
  public static final int     SECTION_CALENDAR = 10;
  public static final int     SECTION_COMMENT  = 11;
  public static final int     SECTION_MAX      = 11;

  public static final String  XML_RESOURCE     = "resource";
  private static final String XML_ID           = "id";
  private static final String XML_INITIALS     = "initials";
  private static final String XML_NAME         = "name";
  private static final String XML_ORG          = "org";
  private static final String XML_GROUP        = "group";
  private static final String XML_ROLE         = "role";
  private static final String XML_ALIAS        = "alias";
  private static final String XML_START        = "start";
  private static final String XML_END          = "end";
  private static final String XML_AVAIL        = "availability";
  private static final String XML_COST         = "cost";
  private static final String XML_CALENDAR     = "calendar";
  private static final String XML_COMMENT      = "comment";

  /**************************************** constructor ******************************************/
  public Resource()
  {
    // initialise private variables
    m_availability = 1.0;
  }

  /**************************************** constructor ******************************************/
  public Resource( XMLStreamReader xsr ) throws XMLStreamException
  {
    this();
    // read XML resource attributes
    for ( int i = 0; i < xsr.getAttributeCount(); i++ )
      switch ( xsr.getAttributeLocalName( i ) )
      {
        case XML_INITIALS:
          m_initials = xsr.getAttributeValue( i );
          break;
        case XML_NAME:
          m_name = xsr.getAttributeValue( i );
          break;
        case XML_ORG:
          m_org = xsr.getAttributeValue( i );
          break;
        case XML_GROUP:
          m_group = xsr.getAttributeValue( i );
          break;
        case XML_ROLE:
          m_role = xsr.getAttributeValue( i );
          break;
        case XML_ALIAS:
          m_alias = xsr.getAttributeValue( i );
          break;
        case XML_START:
          m_start = Date.fromString( xsr.getAttributeValue( i ) );
          break;
        case XML_END:
          m_end = Date.fromString( xsr.getAttributeValue( i ) );
          break;
        case XML_AVAIL:
          m_availability = Double.parseDouble( xsr.getAttributeValue( i ) );
          break;
        case XML_COST:
          m_cost = Double.parseDouble( xsr.getAttributeValue( i ) );
          break;
        case XML_CALENDAR:
          m_calendar = JPlanner.plan.calendar( Integer.parseInt( xsr.getAttributeValue( i ) ) );
          break;
        case XML_COMMENT:
          m_comment = xsr.getAttributeValue( i );
          break;
      }
  }

  /****************************************** toString *******************************************/
  public String toString( int section )
  {
    // return display string for given section
    if ( section == SECTION_INITIALS )
      return m_initials;

    // if resource is null return blank for all other sections
    if ( isNull() )
      return "";

    if ( section == SECTION_NAME )
      return m_name;

    if ( section == SECTION_ORG )
      return m_org;

    if ( section == SECTION_GROUP )
      return m_group;

    if ( section == SECTION_ROLE )
      return m_role;

    if ( section == SECTION_ALIAS )
      return m_alias;

    if ( section == SECTION_START )
    {
      if ( m_start == null )
        return "NA";
      else
        return m_start.toString();
    }

    if ( section == SECTION_END )
    {
      if ( m_end == null )
        return "NA";
      else
        return m_end.toString();
    }

    if ( section == SECTION_AVAIL )
      return String.format( "%.2f", m_availability );

    if ( section == SECTION_COST )
      return String.format( "%.2f", m_cost );

    if ( section == SECTION_CALENDAR )
      return m_calendar.name();

    if ( section == SECTION_COMMENT )
      return m_comment;

    throw new IllegalArgumentException( "Section=" + section );
  }

  /****************************************** setData ********************************************/
  public void setData( int section, Object newValue )
  {
    // set resource data for given section 
    if ( section == SECTION_INITIALS )
    {
      if ( isNull() )
        m_calendar = JPlanner.plan.calendar();

      m_initials = (String) newValue;
    }

    else if ( section == SECTION_NAME )
      m_name = (String) newValue;

    else if ( section == SECTION_ORG )
      m_org = (String) newValue;

    else if ( section == SECTION_GROUP )
      m_group = (String) newValue;

    else if ( section == SECTION_ROLE )
      m_role = (String) newValue;

    else if ( section == SECTION_ALIAS )
      m_alias = (String) newValue;

    else if ( section == SECTION_COMMENT )
      m_comment = (String) newValue;

    // TODO !!!!!!!!!!!!!!!!!!!!!!!!!!

    else
      throw new IllegalArgumentException( "Section=" + section );
  }

  /****************************************** isNull *********************************************/
  public boolean isNull()
  {
    // resource is considered null if id not set
    return ( m_initials == null );
  }

  /**************************************** sectionName ******************************************/
  public static String sectionName( int num )
  {
    // return section title
    if ( num == SECTION_INITIALS )
      return "Initials";

    if ( num == SECTION_NAME )
      return "Name";

    if ( num == SECTION_ORG )
      return "Organisation";

    if ( num == SECTION_GROUP )
      return "Group";

    if ( num == SECTION_ROLE )
      return "Role";

    if ( num == SECTION_ALIAS )
      return "Alias";

    if ( num == SECTION_START )
      return "Start";

    if ( num == SECTION_END )
      return "End";

    if ( num == SECTION_AVAIL )
      return "Available";

    if ( num == SECTION_COST )
      return "Cost";

    if ( num == SECTION_CALENDAR )
      return "Calendar";

    if ( num == SECTION_COMMENT )
      return "Comment";

    throw new IllegalArgumentException( "Section=" + num );
  }

  /****************************************** saveToXML ******************************************/
  public void saveToXML( XMLStreamWriter xsw ) throws XMLStreamException
  {
    // write resource data to xml stream
    xsw.writeStartElement( XML_RESOURCE );
    xsw.writeAttribute( XML_ID, Integer.toString( JPlanner.plan.index( this ) ) );

    if ( !isNull() )
    {
      xsw.writeAttribute( XML_INITIALS, m_initials );
      if ( m_name != null )
        xsw.writeAttribute( XML_NAME, m_name );
      if ( m_org != null )
        xsw.writeAttribute( XML_ORG, m_org );
      if ( m_group != null )
        xsw.writeAttribute( XML_GROUP, m_group );
      if ( m_role != null )
        xsw.writeAttribute( XML_ROLE, m_role );
      if ( m_alias != null )
        xsw.writeAttribute( XML_ALIAS, m_alias );
      if ( m_start != null )
        xsw.writeAttribute( XML_START, m_start.toString() );
      if ( m_end != null )
        xsw.writeAttribute( XML_END, m_end.toString() );
      xsw.writeAttribute( XML_AVAIL, Double.toString( m_availability ) );
      xsw.writeAttribute( XML_COST, Double.toString( m_cost ) );
      xsw.writeAttribute( XML_CALENDAR, Integer.toString( JPlanner.plan.index( m_calendar ) ) );
      if ( m_comment != null )
        xsw.writeAttribute( XML_COMMENT, m_comment );
    }

    xsw.writeEndElement(); // XML_RESOURCE
  }

}
