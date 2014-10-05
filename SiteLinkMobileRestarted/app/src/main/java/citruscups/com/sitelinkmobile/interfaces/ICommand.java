package citruscups.com.sitelinkmobile.interfaces;

/**
 * Created by Jakkyl on 8/18/2014.
 * Purpose:
 */
public interface ICommand
{
    public int executeColor(String text);

    public String executeText(String text, Object data);
}
