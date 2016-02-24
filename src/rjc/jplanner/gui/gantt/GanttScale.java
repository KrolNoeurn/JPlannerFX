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

package rjc.jplanner.gui.gantt;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import rjc.jplanner.JPlanner;
import rjc.jplanner.XmlLabels;
import rjc.jplanner.model.DateTime;
import rjc.jplanner.model.DateTime.Interval;

/*************************************************************************************************/
/************************* GanttScale provides a scale for the gantt plot ************************/
/*************************************************************************************************/

public class GanttScale
{
  private DateTime        m_start;
  private long            m_millisecondsPP;
  private Interval        m_interval;
  private String          m_format;

  final public static int GANTTSCALE_HEIGHT = 15;

  /****************************************** writeXML *******************************************/
  public void writeXML( XMLStreamWriter xsw ) throws XMLStreamException
  {
    // write gantt-scale display data to XML stream
    xsw.writeAttribute( XmlLabels.XML_INTERVAL, m_interval.toString() );
    xsw.writeAttribute( XmlLabels.XML_FORMAT, m_format );
  }

  /****************************************** setConfig ******************************************/
  public void setInterval( Interval interval, String format )
  {
    // set gantt-scale configuration
    m_interval = interval;
    m_format = format;
  }

  /****************************************** setStart *******************************************/
  public void setStart( DateTime start )
  {
    m_start = start;
  }

  /****************************************** setMsPP ********************************************/
  public void setMsPP( long mspp )
  {
    m_millisecondsPP = mspp;
  }

  /******************************************* loadXML *******************************************/
  public void loadXML( XMLStreamReader xsr )
  {
    // adopt gantt-scale display data from XML stream
    for ( int i = 0; i < xsr.getAttributeCount(); i++ )
      switch ( xsr.getAttributeLocalName( i ) )
      {
        case XmlLabels.XML_INTERVAL:
          m_interval = Interval.valueOf( xsr.getAttributeValue( i ) );
          break;
        case XmlLabels.XML_FORMAT:
          m_format = xsr.getAttributeValue( i );
          break;
        default:
          JPlanner.trace( "Unhandled attribute '" + xsr.getAttributeLocalName( i ) + "'" );
          break;
      }
  }
}
