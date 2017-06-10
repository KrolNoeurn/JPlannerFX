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
    for ( int i = 0; i < xsr.getAttributeCount(); i++ )
      switch ( xsr.getAttributeLocalName( i ) )
      {
        default:
          JPlanner.trace( "Unhandled attribute '" + xsr.getAttributeLocalName( i ) + "'" );
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
    for ( Resource res : this )
      res.saveToXML( xsw );
    xsw.writeEndElement(); // XML_RES_DATA
  }

  /****************************************** isTagValid *****************************************/
  public boolean isTagValid( String tag )
  {
    // check tag is not null
    if ( tag == null )
      throw new NullPointerException( "Tag is null!" );

    // return true only if any tag is used by a resource
    for ( Resource res : this )
      if ( res.hasTag( tag ) )
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
    for ( Resource res : this )
    {
      if ( res.isNull() )
        continue;
      count += res.getTagCount( tag );
      if ( count > 1 )
        return false;
    }

    return true;
  }

  /*************************************** getResourceList ***************************************/
  public ArrayList<Resource> getResourceList( String tag )
  {
    // return list of resources that have this tag
    ArrayList<Resource> list = new ArrayList<Resource>();
    for ( Resource res : this )
      if ( res.hasTag( tag ) )
        list.add( res );

    return list;
  }

  /************************************** isInitialsUnique ***************************************/
  public boolean isInitialsUnique( String initials, int index )
  {
    // return true if initials are unique (excluding indexed resource's initials)
    for ( int i = 0; i < size(); i++ )
    {
      Resource res = get( i );

      if ( i != index && initials.equals( res.getInitials() ) )
        return false;
      if ( initials.equals( res.getValue( Resource.SECTION_NAME ) ) )
        return false;
      if ( initials.equals( res.getValue( Resource.SECTION_ORG ) ) )
        return false;
      if ( initials.equals( res.getValue( Resource.SECTION_GROUP ) ) )
        return false;
      if ( initials.equals( res.getValue( Resource.SECTION_ROLE ) ) )
        return false;
      if ( initials.equals( res.getValue( Resource.SECTION_ALIAS ) ) )
        return false;
    }

    return true;
  }

}
