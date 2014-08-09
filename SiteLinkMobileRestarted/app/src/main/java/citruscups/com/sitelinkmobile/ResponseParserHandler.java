package citruscups.com.sitelinkmobile;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

/**
 * Created by Jakkyl on 8/8/2014.
 */
public class ResponseParserHandler extends DefaultHandler {
    private DataSet dataSet = new DataSet();

    //As we read any XML element we will push that in this stack
    private ArrayList _elementList = new ArrayList<String>();
    //As we complete one user block in XML, we will push the User instance in userList
    private ArrayList _objectList = new ArrayList<DataTable>();

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
        if(attributes != null && attributes.getLength() == 1)
        {
            Log.i("Attr:", attributes.getValue(0));
        }
        //If this is start of 'user' element then prepare a new User instance and push it in object stack
        if (qName.equals("Table")) {
            //New User instance
            DataTable table = new DataTable();

            //Set all required attributes in any XML element here itself
            if (attributes != null && attributes.getLength() == 1)
            {
                table.setName(attributes.getValue(0).toString());
            }
            this._objectList.add(table);
        } else if (qName.equals("NewDataSet"))
        {

        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        //Remove last added  element
        this._elementList.remove(_elementList.size() - 1);

        //User instance has been constructed so pop it from object stack and push in userList
        if ("table".equals(qName))
        {
            DataTable table = (DataTable)_objectList.remove(_objectList.size() - 1);
            dataSet.addTable(table);
        }
    }

    /**
     * This will be called everytime parser encounter a value node
     * */
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        String value = new String(ch, start, length).trim();

        if (value.length() == 0)
        {
            return; // ignore white space
        }

        //handle the value based on to which element it belongs
        if ("firstName".equals(currentElement()))
        {
            //User user = (User) this.objectStack.peek();
            //user.setFirstName(value);
        }
        else if ("lastName".equals(currentElement()))
        {
           // User user = (User) this.objectStack.peek();
            //user.setLastName(value);
        }
    }

    /**
     * Utility method for getting the current element in processing
     * */
    private String currentElement()
    {
        return this._elementList.get(_elementList.size() - 1).toString();
    }

    //Accessor for userList object
    public DataSet getDataSet()
    {
        return dataSet;
    }
}
