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
import rjc.jplanner.gui.XSplitPane;
import rjc.jplanner.gui.table.Table;
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
  private XSplitPane            m_split;               // split-pane displaying this gantt

  private DateTime              m_start;               // gantt start date-time
  private DateTime              m_end;                 // gantt end date-time for determining scroll bar
  private long                  m_millisecondsPP;      // milliseconds per pixel

  final public static int       SCROLLBAR_SIZE    = 18;
  final public static int       GANTTSCALE_HEIGHT = 15;

  public static boolean         ganttStretch;

  /**************************************** constructor ******************************************/
  public Gantt( Table table )
  {
    // create default gantt 
    m_plot = new GanttPlot( this, table );
    m_scales = new ArrayList<GanttScale>();
    m_scrollBar = new ScrollBar();
    setDefault();

    // add nodes to region
    getChildren().add( m_plot );
    getChildren().add( m_scrollBar );

    // add listeners to react to size changes
    widthProperty().addListener( ( observable, oldW, newW ) -> widthChange( oldW.intValue(), newW.intValue() ) );
    heightProperty().addListener( ( observable, oldH, newH ) -> heightChange( oldH.intValue(), newH.intValue() ) );

    m_scrollBar.visibleProperty().addListener( ( observable, oldV, newV ) -> heightChange( 0, (int) getHeight() ) );
    m_scrollBar.valueProperty().addListener( ( observable, oldV, newV ) -> redraw() );
  }

  /***************************************** setDefault ******************************************/
  public void setDefault()
  {
    // default gantt has two scales
    for ( GanttScale scale : m_scales )
      getChildren().remove( scale );
    m_scales.clear();
    m_scales.add( new GanttScale( this, Interval.MONTH, "MMM-YYYY" ) );
    m_scales.add( new GanttScale( this, Interval.WEEK, "dd" ) );
    for ( GanttScale scale : m_scales )
      getChildren().add( scale );

    // set sensible start, mspp and end
    setStart( new DateTime( JPlanner.plan.getStart().getMilliseconds() - 300000000L ) );
    setEnd( new DateTime( m_start.getMilliseconds() + m_millisecondsPP * (long) getWidth() ) );
    setMsPP( 3600 * 6000 );

    // set scroll-bar height & value
    m_scrollBar.setMinHeight( SCROLLBAR_SIZE );
    m_scrollBar.setValue( 0.0 );

    // trigger correct positioning and sizing of gantt children
    heightChange( (int) getHeight(), (int) getHeight() );
    widthChange( (int) getWidth(), (int) getWidth() );
  }

  /**************************************** setSplitPane *****************************************/
  public void setSplitPane( XSplitPane splitter )
  {
    // record the split-pane due to display this gantt
    m_split = splitter;
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
        case XmlLabels.XML_SPLITTER:
          m_split.preferredLeftNodeWidth = Integer.parseInt( xsr.getAttributeValue( i ) );
          break;
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
          ganttStretch = Boolean.parseBoolean( xsr.getAttributeValue( i ) );
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
            GanttScale scale = new GanttScale( this, xsr );
            m_scales.add( scale );
            getChildren().add( scale );
            break;
          default:
            JPlanner.trace( "Unhandled start element '" + xsr.getLocalName() + "'" );
            break;
        }
    }
  }

  /****************************************** writeXML *******************************************/
  public void writeXML( XMLStreamWriter xsw ) throws XMLStreamException
  {
    // write gantt display data to XML stream
    xsw.writeAttribute( XmlLabels.XML_START, m_start.toString() );
    xsw.writeAttribute( XmlLabels.XML_END, m_end.toString() );
    xsw.writeAttribute( XmlLabels.XML_MSPP, Long.toString( m_millisecondsPP ) );
    xsw.writeAttribute( XmlLabels.XML_NONWORKING, "???" );
    xsw.writeAttribute( XmlLabels.XML_CURRENT, "???" );
    xsw.writeAttribute( XmlLabels.XML_STRETCH, Boolean.toString( ganttStretch ) );

    // write gantt-scale display data to XML stream
    int id = 0;
    for ( GanttScale scale : m_scales )
    {
      xsw.writeEmptyElement( XmlLabels.XML_SCALE );
      xsw.writeAttribute( XmlLabels.XML_ID, Integer.toString( id++ ) );
      scale.writeXML( xsw );
    }
  }

  /****************************************** setStart *******************************************/
  public void setStart( DateTime start )
  {
    // set start for gantt
    m_start = start;
  }

  /******************************************* setEnd ********************************************/
  public void setEnd( DateTime end )
  {
    // set end for gantt
    m_end = end;
  }

  /******************************************* setMsPP *******************************************/
  public void setMsPP( long mspp )
  {
    // set milliseconds-per-pixel for gantt
    m_millisecondsPP = mspp;
  }

  /******************************************* getMsPP *******************************************/
  public long getMsPP()
  {
    // return milliseconds-per-pixel for gantt
    return m_millisecondsPP;
  }

  /******************************************* redraw ********************************************/
  public void redraw()
  {
    // redraw whole gantt
    checkScrollbar();
    for ( GanttScale scale : m_scales )
      scale.redraw();
    m_plot.redraw();
  }

  /**************************************** heightChange *****************************************/
  private void heightChange( int oldHeight, int newHeight )
  {
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
    checkScrollbar();
  }

  /***************************************** checkScrollbar *****************************************/
  private void checkScrollbar()
  {
    // check scroll-bar visibility, size and settings
    int ganttWidth = (int) ( ( m_end.getMilliseconds() - m_start.getMilliseconds() ) / m_millisecondsPP );
    int visibleWidth = (int) getWidth();
    if ( ganttWidth > visibleWidth )
    {
      m_scrollBar.setVisible( true );
      m_scrollBar.setMinWidth( visibleWidth );
      m_scrollBar.setMaxWidth( visibleWidth );

      double max = ganttWidth - visibleWidth;
      m_scrollBar.setMax( max );
      m_scrollBar.setVisibleAmount( max * visibleWidth / ganttWidth );

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

  /********************************************** x **********************************************/
  public int x( DateTime dt )
  {
    // return x-coordinate for stretched if needed date-time
    dt = JPlanner.plan.stretch( dt, ganttStretch );
    long dtMilliseconds = dt.getMilliseconds();
    long startMilliseconds = m_start.getMilliseconds();

    if ( dtMilliseconds > startMilliseconds )
      return (int) ( ( dtMilliseconds - startMilliseconds ) / m_millisecondsPP - m_scrollBar.getValue() );
    return (int) ( ( startMilliseconds - dtMilliseconds ) / -m_millisecondsPP - m_scrollBar.getValue() );
  }

  /****************************************** datetime *******************************************/
  public DateTime datetime( int x )
  {
    // return date-time for specified x-coordinate
    return m_start.plusMilliseconds( (long) ( x + m_scrollBar.getValue() ) * m_millisecondsPP );
  }

  /******************************************* centre ********************************************/
  public void centre( DateTime dt )
  {
    // scroll if able so date-time is at centre of gantt
    if ( m_scrollBar.isVisible() )
    {
      int delta = x( dt ) - ( (int) getWidth() ) / 2;
      m_scrollBar.setValue( delta - m_scrollBar.getValue() );
      //int newValue = (int) m_scrollBar.getValue() - delta;
      //JPlanner.trace( delta );

      // TODO middle of doing this .....................
    }
  }

}
