package citruscups.com.sitelinkmobile.helper;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import citruscups.com.sitelinkmobile.dataStructures.DataSet;
import citruscups.com.sitelinkmobile.dataStructures.DataTable;

/**
 * Created by Jakkyl on 8/8/2014.
 */
public class ResponseParserHandler extends DefaultHandler
{
    private DataSet dataSet = new DataSet();
    private Map<String, Object> _currentRow;

    //As we read any XML element we will push that in this stack
    private ArrayList _elementList = new ArrayList<String>();
    //As we complete one user block in XML, we will push the User instance in userList

    public void startDocument() throws SAXException
    {
        //System.out.println("start of the document   : ");
    }

    public void endDocument() throws SAXException
    {
        //System.out.println("end of the document document     : ");
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        //Push it in element stack
        this._elementList.add(qName);
        if (attributes != null && attributes.getLength() == 1)
        {
            Log.i("Attr:", attributes.getValue(0));
        }
        //If this is start of 'user' element then prepare a new User instance and push it in object stack
        if (parentElement().equals("NewDataSet"))
        {
            DataTable table = null;
            if (dataSet.getTables().size() > 0)
            {
                table = dataSet.getTableByName(qName);
            }

            if (table == null)
            {
                table = new DataTable();
                table.setName(qName);
                dataSet.addTable(table);
            }
            else
            {
                table.setClosed(false);
            }

            _currentRow = new HashMap<String, Object>();
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        //User instance has been constructed so pop it from object stack and push in userList
        if (parentElement().equals("NewDataSet"))
        {
            DataTable table = dataSet.getTables().get(dataSet.getTables().size() - 1);
            table.addRow(_currentRow);
            table.setClosed(true);
            _currentRow = null;
        }

        //Remove last added  element
        this._elementList.remove(_elementList.size() - 1);
    }

    /**
     * This will be called everytime parser encounter a value node
     */
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        String value = new String(ch, start, length).trim();

        if (value.length() == 0)
        {
            return; // ignore white space
        }

        if (dataSet.getTables().size() > 0)
        {
            DataTable table = dataSet.getTables().get(dataSet.getTables().size() - 1);
            if (!table.getClosed())
            {
                if (_currentRow == null)
                    _currentRow = new HashMap<String, Object>();

                _currentRow.put(currentElement(), value);
            }
        }
    }

    /**
     * Utility method for getting the current element in processing
     */
    private String currentElement()
    {
        return this._elementList.get(_elementList.size() - 1).toString();
    }

    private String parentElement()
    {
        return _elementList.size() > 1 ? _elementList.get(_elementList.size() - 2).toString() : "";
    }

    //Accessor for userList object
    public DataSet getDataSet()
    {
        return dataSet;
    }
}
