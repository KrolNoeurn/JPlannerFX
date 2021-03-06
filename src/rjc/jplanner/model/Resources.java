/**************************************************************************
 *  Copyright (C) 2018 by Richard Crook                                   *
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

import java.util.ArrayList;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import rjc.jplanner.JPlanner;
import rjc.jplanner.XmlLabels;

/*************************************************************************************************/
/************************** Holds the complete list of plan resources ****************************/
/*************************************************************************************************/

public class Resources extends ArrayList<Resource>
{
  private static final long serialVersionUID = 1L;

  /**************************************** initialise *******************************************/
  public void initialise()
  {
    // initialise list with default resources (including special resource 0)
    clear();
    for ( int count = 0; count <= 100; count++ )
      add( new Resource() );
  }

  /******************************************* loadXML *******************************************/
  public void loadXML( XMLStreamReader xsr ) throws XMLStreamException
  {
    // read any attributes
    for ( int index = 0; index < xsr.getAttributeCount(); index++ )
      switch ( xsr.getAttributeLocalName( index ) )
      {
        default:
          JPlanner.trace( "Unhandled attribute '" + xsr.getAttributeLocalName( index ) + "'" );
          break;
      }

    // read XML resource data
    while ( xsr.hasNext() )
    {
      xsr.next();

      // if reached end of resource data, return
      if ( xsr.isEndElement() && xsr.getLocalName().equals( XmlLabels.XML_RES_DATA ) )
        return;

      // if a resource element, construct a resource from it
      if ( xsr.isStartElement() )
        switch ( xsr.getLocalName() )
        {
          case XmlLabels.XML_RESOURCE:
            add( new Resource( xsr ) );
            break;
          default:
            JPlanner.trace( "Unhandled start element '" + xsr.getLocalName() + "'" );
            break;
        }

    }
  }

  /******************************************* writeXML ******************************************/
  public void writeXML( XMLStreamWriter xsw ) throws XMLStreamException
  {
    // write resources data to XML stream
    xsw.writeStartElement( XmlLabels.XML_RES_DATA );
    for ( Resource resource : this )
      resource.saveToXML( xsw );
    xsw.writeEndElement(); // XML_RES_DATA
  }

  /****************************************** isTagValid *****************************************/
  public boolean isTagValid( String tag )
  {
    // check tag is not null
    if ( tag == null )
      throw new NullPointerException( "Tag is null!" );

    // return true only if any tag is used by a resource
    for ( Resource resource : this )
      if ( resource.hasTag( tag ) )
        return true;

    return false;
  }

  /***************************************** isTagUnique *****************************************/
  public boolean isTagUnique( String tag )
  {
    // check tag is not null
    if ( tag == null )
      throw new NullPointerException( "Tag is null!" );

    // return true if tag used only once or less
    int count = 0;
    for ( Resource resource : this )
    {
      count += resource.getTagCount( tag );
      if ( count > 1 )
        return false;
    }

    return true;
  }

  /****************************************** tagCount *******************************************/
  public int tagCount( String tag )
  {
    // return count of tag defines
    int count = 0;
    for ( Resource resource : this )
      count += resource.getTagCount( tag );

    return count;
  }

  /*************************************** getResourceList ***************************************/
  public ArrayList<Resource> getResourceList( String tag )
  {
    // return list of resources that have this tag
    ArrayList<Resource> list = new ArrayList<Resource>();
    for ( Resource resource : this )
      if ( resource.hasTag( tag ) )
        list.add( resource );

    return list;
  }

  /**************************************** initialsClash ****************************************/
  public String initialsClash( String tag, int exceptIndex )
  {
    // if initials with existing field (except specified index initials) return error string
    for ( int index = 0; index < size(); index++ )
    {
      Resource resource = get( index );

      if ( index != exceptIndex && tag.equals( resource.getInitials() ) )
        return "Clash with resource " + index + " initials";

      // if exceptIndex provided means tag is initials to be check against other fields
      if ( exceptIndex >= 0 )
      {
        if ( tag.equals( resource.getValue( Resource.SECTION_NAME ) ) )
          return "Clash with resource " + index + " name";
        if ( tag.equals( resource.getValue( Resource.SECTION_ORG ) ) )
          return "Clash with resource " + index + " organisation";
        if ( tag.equals( resource.getValue( Resource.SECTION_GROUP ) ) )
          return "Clash with resource " + index + " group";
        if ( tag.equals( resource.getValue( Resource.SECTION_ROLE ) ) )
          return "Clash with resource " + index + " role";
        if ( tag.equals( resource.getValue( Resource.SECTION_ALIAS ) ) )
          return "Clash with resource " + index + " alias";
      }
    }

    return null;
  }

}
