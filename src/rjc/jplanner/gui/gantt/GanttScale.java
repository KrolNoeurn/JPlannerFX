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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Text;
import rjc.jplanner.JPlanner;
import rjc.jplanner.XmlLabels;
import rjc.jplanner.gui.Colors;
import rjc.jplanner.model.DateTime;
import rjc.jplanner.model.DateTime.Interval;

/*************************************************************************************************/
/************************* GanttScale provides a scale for the gantt plot ************************/
/*************************************************************************************************/

public class GanttScale extends Canvas
{
  private Gantt    m_gantt;
  private Interval m_interval;
  private String   m_format;

  /**************************************** constructor ******************************************/
  public GanttScale( Gantt gantt, Interval interval, String format )
  {
    // construct gantt-scale
    super( 0.0, Gantt.GANTTSCALE_HEIGHT );
    m_gantt = gantt;
    m_interval = interval;
    m_format = format;

    widthProperty().addListener( ( observable, oldW, newW ) -> widthChange( oldW.intValue(), newW.intValue() ) );
  }

  /**************************************** constructor ******************************************/
  public GanttScale( Gantt gantt, XMLStreamReader xsr )
  {
    // adopt gantt-scale display data from XML stream
    super( 0.0, Gantt.GANTTSCALE_HEIGHT );
    m_gantt = gantt;

    for ( int i = 0; i < xsr.getAttributeCount(); i++ )
      switch ( xsr.getAttributeLocalName( i ) )
      {
        case XmlLabels.XML_ID:
          break;
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

    widthProperty().addListener( ( observable, oldW, newW ) -> widthChange( oldW.intValue(), newW.intValue() ) );
  }

  /****************************************** writeXML *******************************************/
  public void writeXML( XMLStreamWriter xsw ) throws XMLStreamException
  {
    // write gantt-scale display data to XML stream
    xsw.writeAttribute( XmlLabels.XML_INTERVAL, m_interval.toString() );
    xsw.writeAttribute( XmlLabels.XML_FORMAT, m_format );
  }

  /****************************************** setScale *******************************************/
  public void setScale( Interval interval, String format )
  {
    // set gantt-scale configuration
    m_interval = interval;
    m_format = format;
  }

  /******************************************* redraw ********************************************/
  public void redraw()
  {
    // redraw whole gantt-scale
    widthChange( 0, (int) getWidth() );
  }

  /***************************************** widthChange *****************************************/
  private void widthChange( int oldW, int newW )
  {
    // draw only if increase in width
    if ( getHeight() <= 0.0 || newW <= oldW )
      return;

    // draw gantt scale between old-width and new-width
    GraphicsContext gc = getGraphicsContext2D();
    double y = getHeight() - 0.5;
    gc.setStroke( Colors.TABLE_GRID );
    gc.strokeLine( oldW, y, newW, y );

    // determine first interval
    DateTime start = m_gantt.datetime( oldW ).getTruncated( m_interval );
    DateTime end = start.plusInterval( m_interval );
    int xs = m_gantt.x( start );
    int xe = m_gantt.x( end );

    // draw intervals until past new-width
    do
    {
      // draw interval divider
      gc.clearRect( xs + 1.0, 0, xe - xs, getHeight() - 1.0 );
      gc.strokeLine( xe - 0.5, 0, xe - 0.5, y );

      // if enough width, add label
      double ww = xe - xs - 3.0;
      if ( ww > 1.0 )
      {
        String label = start.toString( m_format );
        Bounds bounds = new Text( label ).getLayoutBounds();
        double yy = ( getHeight() - bounds.getHeight() ) / 2.0 - bounds.getMinY() - 1.0;
        double xx = xs + 1.0;
        if ( ww > bounds.getWidth() )
          xx += ( ww - bounds.getWidth() ) / 2.0;
        gc.fillText( label, xx, yy, ww );
      }

      // move on to next interval
      start = end;
      xs = xe;
      end = end.plusInterval( m_interval );
      xe = m_gantt.x( end );
    }
    while ( xs < newW );
  }

}
