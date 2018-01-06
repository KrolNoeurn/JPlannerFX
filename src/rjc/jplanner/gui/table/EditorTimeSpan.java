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

package rjc.jplanner.gui.table;

import rjc.jplanner.gui.SpinEditor;
import rjc.jplanner.model.TimeSpan;

/*************************************************************************************************/
/***************************** Table cell editor for TimeSpan fields *****************************/
/*************************************************************************************************/

public class EditorTimeSpan extends AbstractCellEditor
{
  SpinEditor m_spin; // spin editor

  /**************************************** constructor ******************************************/
  public EditorTimeSpan( int columnIndex, int row )
  {
    // start with default spin editor
    super( columnIndex, row );
    m_spin = new SpinEditor();
    setControl( m_spin );
  }

  /******************************************* getValue ******************************************/
  @Override
  public Object getValue()
  {
    // return value as a TimeSpan
    return new TimeSpan( m_spin.getDouble(), getUnits() );
  }

  /******************************************* setValue ******************************************/
  @Override
  public void setValue( Object value )
  {
    // if value of type TimeSpan
    if ( value instanceof TimeSpan )
    {
      setTimeSpan( (TimeSpan) value );
      return;
    }

    // if value of type String
    if ( value instanceof String )
    {
      String str = ( (String) value ).replaceAll( "\\s+", "" );

      // if string is just decimal point then get time-span from data source and overwrite number
      if ( str.equals( "." ) )
      {
        setTimeSpan( (TimeSpan) getDataSourceValue() );
        m_spin.setValue( str );
        return;
      }

      // if string valid number then add units from data source
      try
      {
        Double.parseDouble( str );
        char units = ( (TimeSpan) getDataSourceValue() ).getUnits();
        setTimeSpan( new TimeSpan( str + units ) );
      }
      catch ( Exception exception )
      {
        // if value just units then add number from data source
        char unit = str.charAt( 0 );
        if ( TimeSpan.isValidUnits( unit ) )
        {
          double num = ( (TimeSpan) getDataSourceValue() ).getNumber();
          setTimeSpan( new TimeSpan( num, unit ) );
        }
        else
          // else use full time-span from data source
          setTimeSpan( (TimeSpan) getDataSourceValue() );
      }

      return;
    }

    // unhandled value type 
    throw new IllegalArgumentException( "Invalid value type " + value.getClass() );
  }

  /********************************************* open ********************************************/
  @Override
  public void open( Table table, Object value )
  {
    // open cell editor
    super.open( table, value );

    // add listener to react to user typing new TimeSpan units
    m_spin.setOnKeyTyped( event ->
    {
      char unit = event.getCharacter().charAt( 0 );
      if ( TimeSpan.isValidUnits( unit ) )
        setTimeSpan( new TimeSpan( m_spin.getDouble(), unit ) );
    } );
  }

  /******************************************* getUnits ******************************************/
  private char getUnits()
  {
    // return units which is last character of suffix (= last character of displayed text)
    String text = m_spin.getSuffix();
    return text.charAt( text.length() - 1 );
  }

  /***************************************** setTimeSpan *****************************************/
  private void setTimeSpan( TimeSpan ts )
  {
    // number of seconds must be integer, otherwise non-integer allowed
    if ( ts.getUnits() == TimeSpan.UNIT_SECONDS )
      m_spin.setRange( 0, 9999, 0 );
    else
      m_spin.setRange( 0.0, 9999.99, 2 );

    m_spin.setPrefixSuffix( null, " " + ts.getUnits() );
    m_spin.setDouble( ts.getNumber() );
  }

}
