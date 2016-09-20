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

import java.util.ArrayList;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import javafx.scene.control.ScrollBar;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import rjc.jplanner.JPlanner;
import rjc.jplanner.XmlLabels;
import rjc.jplanner.model.DateTime;
import rjc.jplanner.model.DateTime.Interval;

/*************************************************************************************************/
/********************** Gantt shows tasks in a gantt plot with gantt scales **********************/
/*************************************************************************************************/

public class Gantt extends Region
{
  private ArrayList<GanttScale> m_scales;              // list of gantt-scales
  private GanttPlot             m_plot;                // gantt plot
  private ScrollBar             m_scrollBar;           // horizontal scroll bar

  private DateTime              m_start;               // gantt start date-time
  private DateTime              m_end;                 // gantt end date-time for determining scroll bar
  private long                  m_millisecondsPP;      // milliseconds per pixel

  final public static int       SCROLLBAR_SIZE    = 18;
  final public static int       GANTTSCALE_HEIGHT = 15;

  /**************************************** constructor ******************************************/
  public Gantt()
  {
    // create default gantt 
    m_plot = new GanttPlot();
    m_scales = new ArrayList<GanttScale>();
    m_scrollBar = new ScrollBar();
    setDefault();

    // add nodes to region
    for ( GanttScale scale : m_scales )
      getChildren().add( scale );
    getChildren().add( m_plot );
    getChildren().add( m_scrollBar );

    // add listeners to react to size changes
    widthProperty().addListener( ( observable, oldW, newW ) -> widthChange( oldW.intValue(), newW.intValue() ) );
    heightProperty().addListener( ( observable, oldH, newH ) -> heightChange( oldH.intValue(), newH.intValue() ) );

    m_scrollBar.visibleProperty().addListener( ( observable, oldV, newV ) -> heightChange( 0, (int) getHeight() ) );
  }

  /***************************************** setDefault ******************************************/
  public void setDefault()
  {
    // default gantt has two scales
    m_scales.add( new GanttScale( Interval.MONTH, "MMM-YYYY" ) );
    m_scales.add( new GanttScale( Interval.WEEK, "dd" ) );

    // set sensible start, mspp and end
    setStart( new DateTime( JPlanner.plan.start().milliseconds() - 300000000L ) );
    setMsPP( 3600 * 6000 );
    setEnd( new DateTime( m_start.milliseconds() + m_millisecondsPP * (long) getWidth() ) );

    // set scroll-bar height
    m_scrollBar.setMinHeight( SCROLLBAR_SIZE );
  }

  /******************************************* loadXML *******************************************/
  public void loadXML( XMLStreamReader xsr ) throws XMLStreamException
  {
    // start with default gantt and no scales
    setDefault();
    for ( GanttScale scale : m_scales )
      getChildren().remove( scale );
    m_scales.clear();

    // adopt gantt display data from XML stream, starting with the attributes
    for ( int i = 0; i < xsr.getAttributeCount(); i++ )
      switch ( xsr.getAttributeLocalName( i ) )
      {
        case XmlLabels.XML_START:
          m_start = new DateTime( xsr.getAttributeValue( i ) );
          break;
        case XmlLabels.XML_END:
          m_end = new DateTime( xsr.getAttributeValue( i ) );
          break;
        case XmlLabels.XML_MSPP:
          m_millisecondsPP = Long.parseLong( xsr.getAttributeValue( i ) );
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

    // read XML gantt data elements (for example gantt-scales)
    while ( xsr.hasNext() )
    {
      xsr.next();

      // if reached end of gantt data, exit
      if ( xsr.isEndElement() && xsr.getLocalName().equals( XmlLabels.XML_GANTT ) )
        break;

      if ( xsr.isStartElement() )
        switch ( xsr.getLocalName() )
        {
          case XmlLabels.XML_SCALE:
            m_scales.add( new GanttScale( xsr ) );
            break;
          default:
            JPlanner.trace( "Unhandled start element '" + xsr.getLocalName() + "'" );
            break;
        }
    }

    // ensure all plot and scales have same start & mspp set correctly, and are children of gantt region
    setStart( m_start );
    setMsPP( m_millisecondsPP );
    for ( GanttScale scale : m_scales )
      getChildren().add( scale );
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

    // write gantt-scale display data to XML stream
    int id = 0;
    for ( GanttScale scale : m_scales )
    {
      xsw.writeStartElement( XmlLabels.XML_SCALE );
      xsw.writeAttribute( XmlLabels.XML_ID, Integer.toString( ++id ) );
      scale.writeXML( xsw );
      xsw.writeEndElement(); // XML_SCALE
    }

    // close gantt element
    xsw.writeEndElement(); // XML_GANTT
  }

  /****************************************** setStart *******************************************/
  public void setStart( DateTime start )
  {
    // set start for gantt and scale/plot components
    m_start = start;
    m_plot.setStart( start );
    for ( GanttScale scale : m_scales )
      scale.setStart( start );
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
    m_plot.setMsPP( mspp );
    for ( GanttScale scale : m_scales )
      scale.setMsPP( mspp );
  }

  /**************************************** heightChange *****************************************/
  private void heightChange( int oldHeight, int newHeight )
  {
    JPlanner.trace( "HEIGHT", this, oldHeight, newHeight );

    // if new height is over large, do nothing
    if ( newHeight > Screen.getPrimary().getVisualBounds().getHeight() * 10 )
      return;

    // set gantt-scales vertical positions
    double y = 0.0;
    for ( GanttScale scale : m_scales )
    {
      scale.setLayoutY( y );
      y += scale.getHeight();
    }

    // set gantt-plot vertical position & height
    m_plot.setLayoutY( y );
    double height = newHeight - y;
    if ( m_scrollBar.isVisible() )
      height -= m_scrollBar.getHeight();
    m_plot.setHeight( height );

    // set scroll-bar vertical position
    m_scrollBar.setLayoutY( newHeight - m_scrollBar.getHeight() );
  }

  /***************************************** widthChange *****************************************/
  private void widthChange( int oldWidth, int newWidth )
  {
    // if new width is over large, do nothing
    if ( newWidth > Screen.getPrimary().getVisualBounds().getWidth() * 10 )
      return;

    // set gantt-scales width
    for ( GanttScale scale : m_scales )
      scale.setWidth( newWidth );

    // set gantt-plot width
    m_plot.setWidth( newWidth );

    // set scroll-bar width
    int ganttWidth = (int) ( ( m_end.milliseconds() - m_start.milliseconds() ) / m_millisecondsPP );
    if ( ganttWidth > newWidth )
    {
      m_scrollBar.setVisible( true );
      m_scrollBar.setMinWidth( newWidth );

      double max = ganttWidth - newWidth;
      m_scrollBar.setMax( max );
      m_scrollBar.setVisibleAmount( max * newWidth / ganttWidth );

      if ( m_scrollBar.getValue() > max )
        m_scrollBar.setValue( max );
      if ( m_scrollBar.getValue() < 0.0 )
        m_scrollBar.setValue( 0.0 );
    }
    else
    {
      m_scrollBar.setVisible( false );
      m_scrollBar.setValue( 0.0 );
    }
  }

}
