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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import rjc.jplanner.JPlanner;
import rjc.jplanner.XmlLabels;

/*************************************************************************************************/
/************************************* Single plan resource **************************************/
/*************************************************************************************************/

public class Resource
{
  private String          m_initials;           // must be unique across all resources in model
  private String          m_name;               // free text
  private String          m_org;                // free text
  private String          m_group;              // free text
  private String          m_role;               // free text
  private String          m_alias;              // free text
  private Date            m_start;              // date availability starts inclusive
  private Date            m_end;                // date availability end inclusive
  private double          m_availability;       // number available
  private double          m_cost;               // cost TODO
  private Calendar        m_calendar;           // calendar for resource
  private String          m_comment;            // free text

  public static final int SECTION_INITIALS = 0;
  public static final int SECTION_NAME     = 1;
  public static final int SECTION_ORG      = 2;
  public static final int SECTION_GROUP    = 3;
  public static final int SECTION_ROLE     = 4;
  public static final int SECTION_ALIAS    = 5;
  public static final int SECTION_START    = 6;
  public static final int SECTION_END      = 7;
  public static final int SECTION_AVAIL    = 8;
  public static final int SECTION_COST     = 9;
  public static final int SECTION_CALENDAR = 10;
  public static final int SECTION_COMMENT  = 11;
  public static final int SECTION_MAX      = 11;

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
        case XmlLabels.XML_ID:
          break;
        case XmlLabels.XML_INITIALS:
          m_initials = xsr.getAttributeValue( i );
          break;
        case XmlLabels.XML_NAME:
          m_name = xsr.getAttributeValue( i );
          break;
        case XmlLabels.XML_ORG:
          m_org = xsr.getAttributeValue( i );
          break;
        case XmlLabels.XML_GROUP:
          m_group = xsr.getAttributeValue( i );
          break;
        case XmlLabels.XML_ROLE:
          m_role = xsr.getAttributeValue( i );
          break;
        case XmlLabels.XML_ALIAS:
          m_alias = xsr.getAttributeValue( i );
          break;
        case XmlLabels.XML_START:
          m_start = Date.fromString( xsr.getAttributeValue( i ) );
          break;
        case XmlLabels.XML_END:
          m_end = Date.fromString( xsr.getAttributeValue( i ) );
          break;
        case XmlLabels.XML_AVAIL:
          m_availability = Double.parseDouble( xsr.getAttributeValue( i ) );
          break;
        case XmlLabels.XML_COST:
          m_cost = Double.parseDouble( xsr.getAttributeValue( i ) );
          break;
        case XmlLabels.XML_CALENDAR:
          m_calendar = JPlanner.plan.getCalendar( Integer.parseInt( xsr.getAttributeValue( i ) ) );
          break;
        case XmlLabels.XML_COMMENT:
          m_comment = xsr.getAttributeValue( i );
          break;
        default:
          JPlanner.trace( "Unhandled attribute '" + xsr.getAttributeLocalName( i ) + "'" );
          break;
      }
  }

  /**************************************** toStringShort ****************************************/
  public String toStringShort()
  {
    // short string summary
    return "Resource@" + Integer.toHexString( hashCode() ) + "[" + m_initials + "]";
  }

  /****************************************** toString *******************************************/
  @Override
  public String toString()
  {
    // string summary
    return "Resource@" + Integer.toHexString( hashCode() ) + "[" + m_initials + ", " + m_name + ", " + m_org + ", "
        + m_group + ", " + m_role + ", " + m_alias + ", " + m_start + ", " + m_end + ", " + m_availability + "]";
  }

  /****************************************** getValue *******************************************/
  public Object getValue( int section )
  {
    // return display string for given section
    if ( section == SECTION_INITIALS )
      return m_initials;

    // if resource is null return blank for all other sections
    if ( isNull() )
      return null;

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
      return m_start;

    if ( section == SECTION_END )
      return m_end;

    if ( section == SECTION_AVAIL )
      return m_availability;

    if ( section == SECTION_COST )
      return m_cost;

    if ( section == SECTION_CALENDAR )
      return m_calendar;

    if ( section == SECTION_COMMENT )
      return m_comment;

    throw new IllegalArgumentException( "Section=" + section );
  }

  /****************************************** setValue ******************************************/
  public void setValue( int section, Object newValue )
  {
    // update resource with new value
    if ( section == SECTION_INITIALS )
    {
      if ( isNull() )
        m_calendar = JPlanner.plan.getDefaultCalendar();

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

    else if ( section == SECTION_CALENDAR )
      m_calendar = (Calendar) newValue;

    else if ( section == SECTION_START )
      m_start = (Date) newValue;

    else if ( section == SECTION_END )
      m_end = (Date) newValue;

    else if ( section == SECTION_AVAIL )
      m_availability = (double) newValue;

    // TODO !!!!!!!!!!!!!!!!!!!!!!!!!! m_cost

    else
      throw new IllegalArgumentException( "Section=" + section );
  }

  /****************************************** isNull *********************************************/
  public boolean isNull()
  {
    // resource is considered null if id not set
    return ( m_initials == null );
  }

  /*************************************** getSectionName ****************************************/
  public static String getSectionName( int num )
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
    xsw.writeEmptyElement( XmlLabels.XML_RESOURCE );
    xsw.writeAttribute( XmlLabels.XML_ID, Integer.toString( this.getIndex() ) );

    if ( !isNull() )
    {
      xsw.writeAttribute( XmlLabels.XML_INITIALS, m_initials );
      if ( m_name != null )
        xsw.writeAttribute( XmlLabels.XML_NAME, m_name );
      if ( m_org != null )
        xsw.writeAttribute( XmlLabels.XML_ORG, m_org );
      if ( m_group != null )
        xsw.writeAttribute( XmlLabels.XML_GROUP, m_group );
      if ( m_role != null )
        xsw.writeAttribute( XmlLabels.XML_ROLE, m_role );
      if ( m_alias != null )
        xsw.writeAttribute( XmlLabels.XML_ALIAS, m_alias );
      if ( m_start != null )
        xsw.writeAttribute( XmlLabels.XML_START, m_start.toString() );
      if ( m_end != null )
        xsw.writeAttribute( XmlLabels.XML_END, m_end.toString() );
      xsw.writeAttribute( XmlLabels.XML_AVAIL, Double.toString( m_availability ) );
      xsw.writeAttribute( XmlLabels.XML_COST, Double.toString( m_cost ) );
      xsw.writeAttribute( XmlLabels.XML_CALENDAR, Integer.toString( m_calendar.getIndex() ) );
      if ( m_comment != null )
        xsw.writeAttribute( XmlLabels.XML_COMMENT, m_comment );
    }
  }

  /****************************************** getIndex *******************************************/
  public int getIndex()
  {
    return JPlanner.plan.getIndex( this );
  }

  /****************************************** getStart *******************************************/
  public DateTime getStart()
  {
    // return date-time when this resource starts being available
    if ( m_start == null )
      return DateTime.MIN_VALUE;
    return new DateTime( m_start, Time.MIN_VALUE );
  }

  /******************************************* getEnd ********************************************/
  public DateTime getEnd()
  {
    // return date-time when this resource stops being available
    if ( m_end == null )
      return DateTime.MAX_VALUE;
    return new DateTime( m_end, Time.MAX_VALUE );
  }

  /**************************************** getAvailable *****************************************/
  public double getAvailable()
  {
    // return the number of these resource's available
    return m_availability;
  }

  /**************************************** getAvailable *****************************************/
  public double getAvailable( Date date )
  {
    // return the number of these resource's available at specified date-time
    int dateEpoch = date.getEpochday();
    if ( dateEpoch < m_start.getEpochday() || dateEpoch > m_end.getEpochday() )
      return 0.0;

    return m_availability;
  }

  /**************************************** getAvailable *****************************************/
  public double getAvailable( DateTime dt )
  {
    // return the number of these resource's available at specified date-time
    return getAvailable( dt.getDate() );
  }

  /***************************************** getInitials *****************************************/
  public String getInitials()
  {
    // return the resource's initials
    return m_initials;
  }

  /******************************************* hasTag ********************************************/
  public boolean hasTag( String tag )
  {
    // return true is tag matches a resource field
    if ( tag.equals( m_initials ) || tag.equals( m_name ) || tag.equals( m_org ) || tag.equals( m_group )
        || tag.equals( m_role ) || tag.equals( m_alias ) )
      return true;

    return false;
  }

  /***************************************** getTagCount *****************************************/
  public int getTagCount( String tag )
  {
    // return count of number of times specified tag is used
    int count = 0;
    if ( tag.equals( m_initials ) )
      count++;
    if ( tag.equals( m_name ) )
      count++;
    if ( tag.equals( m_org ) )
      count++;
    if ( tag.equals( m_group ) )
      count++;
    if ( tag.equals( m_role ) )
      count++;
    if ( tag.equals( m_alias ) )
      count++;

    return count;
  }

}