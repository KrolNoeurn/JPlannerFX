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

import javafx.scene.layout.Pane;
import rjc.jplanner.JPlanner;
import rjc.jplanner.XmlLabels;
import rjc.jplanner.model.DateTime;
import rjc.jplanner.model.DateTime.Interval;

/*************************************************************************************************/
/*************** Gantt shows tasks in a gantt plot with upper & lower gantt scales ***************/
/*************************************************************************************************/

public class Gantt extends Pane
{
  private DateTime   m_start;
  private DateTime   m_end;
  private long       m_millisecondsPP;

  private GanttScale m_upperScale;
  private GanttScale m_lowerScale;
  private GanttPlot  m_plot;

  /**************************************** constructor ******************************************/
  public Gantt()
  {
    super();

    m_upperScale = new GanttScale();
    m_lowerScale = new GanttScale();
    m_plot = new GanttPlot();

    // set default gantt parameters
    setDefault();
  }

  /****************************************** writeXML *******************************************/
  public void writeXML( XMLStreamWriter xsw ) throws XMLStreamException
  {
    // write gantt display data to XML stream
    xsw.writeStartElement( XmlLabels.XML_GANTT );
    xsw.writeAttribute( XmlLabels.XML_START, m_start.toString() );
    xsw.writeAttribute( XmlLabels.XML_END, m_end.toString() );
    xsw.writeAttribute( XmlLabels.XML_MSPP, Long.toString( m_millisecondsPP ) );
    xsw.writeAttribute( XmlLabels.XML_NONWORKING, "???" );
    xsw.writeAttribute( XmlLabels.XML_CURRENT, "???" );
    xsw.writeAttribute( XmlLabels.XML_STRETCH, Boolean.toString( GanttPlot.ganttStretch ) );

    // write upper-scale display data
    xsw.writeStartElement( XmlLabels.XML_UPPER_SCALE );
    m_upperScale.writeXML( xsw );
    xsw.writeEndElement(); // XML_UPPER_SCALE

    // write lower-scale display data
    xsw.writeStartElement( XmlLabels.XML_LOWER_SCALE );
    m_lowerScale.writeXML( xsw );
    xsw.writeEndElement(); // XML_LOWER_SCALE

    // close gantt element
    xsw.writeEndElement(); // XML_GANTT
  }

  /******************************************* loadXML *******************************************/
  public void loadXML( XMLStreamReader xsr ) throws XMLStreamException
  {
    // adopt gantt display data from XML stream, starting with the attributes
    setDefault();
    for ( int i = 0; i < xsr.getAttributeCount(); i++ )
      switch ( xsr.getAttributeLocalName( i ) )
      {
        case XmlLabels.XML_START:
          setStart( new DateTime( xsr.getAttributeValue( i ) ) );
          break;
        case XmlLabels.XML_END:
          setEnd( new DateTime( xsr.getAttributeValue( i ) ) );
          break;
        case XmlLabels.XML_MSPP:
          setMsPP( Long.parseLong( xsr.getAttributeValue( i ) ) );
          break;
        case XmlLabels.XML_NONWORKING:
          // TODO ..................
          break;
        case XmlLabels.XML_CURRENT:
          // TODO ..................
          break;
        case XmlLabels.XML_STRETCH:
          GanttPlot.ganttStretch = Boolean.parseBoolean( xsr.getAttributeValue( i ) );
          break;
        default:
          JPlanner.trace( "Unhandled attribute '" + xsr.getAttributeLocalName( i ) + "'" );
          break;
      }

    // read XML gantt data
    while ( xsr.hasNext() )
    {
      xsr.next();

      // if reached end of gantt data, return
      if ( xsr.isEndElement() && xsr.getLocalName().equals( XmlLabels.XML_GANTT ) )
        return;

      if ( xsr.isStartElement() )
        switch ( xsr.getLocalName() )
        {
          case XmlLabels.XML_UPPER_SCALE:
            m_upperScale.loadXML( xsr );
            break;
          case XmlLabels.XML_LOWER_SCALE:
            m_lowerScale.loadXML( xsr );
            break;
          default:
            JPlanner.trace( "Unhandled start element '" + xsr.getLocalName() + "'" );
            break;
        }
    }

  }

  /***************************************** setDefault ******************************************/
  public void setDefault()
  {
    // set gantt to default parameters and trigger redraw
    setStart( new DateTime( JPlanner.plan.start().milliseconds() - 300000000L ) );
    setMsPP( 3600 * 6000 );
    setEnd( new DateTime( m_start.milliseconds() + m_millisecondsPP * (long) getWidth() ) );
    m_upperScale.setInterval( Interval.MONTH, "MMM-YYYY" );
    m_lowerScale.setInterval( Interval.WEEK, "dd" );
  }

  /****************************************** setStart *******************************************/
  public void setStart( DateTime start )
  {
    // set start for gantt and scale/plot components
    m_start = start;
    m_upperScale.setStart( start );
    m_lowerScale.setStart( start );
    m_plot.setStart( start );
  }

  /******************************************* setEnd ********************************************/
  public void setEnd( DateTime end )
  {
    // set end for gantt, scale/plot components don't have end
    m_end = end;
  }

  /******************************************* setMsPP *******************************************/
  public void setMsPP( long mspp )
  {
    // set milliseconds-per-pixel for gantt and scale/plot components
    m_millisecondsPP = mspp;
    m_upperScale.setMsPP( mspp );
    m_lowerScale.setMsPP( mspp );
    m_plot.setMsPP( mspp );
  }

}
