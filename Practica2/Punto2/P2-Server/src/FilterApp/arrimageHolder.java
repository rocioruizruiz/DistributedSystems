package FilterApp;


/**
* FilterApp/arrimageHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from src/Filter.idl
* Wednesday, April 21, 2021 7:51:46 PM CEST
*/

public final class arrimageHolder implements org.omg.CORBA.portable.Streamable
{
  public int value[][] = null;

  public arrimageHolder ()
  {
  }

  public arrimageHolder (int[][] initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = FilterApp.arrimageHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    FilterApp.arrimageHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return FilterApp.arrimageHelper.type ();
  }

}
