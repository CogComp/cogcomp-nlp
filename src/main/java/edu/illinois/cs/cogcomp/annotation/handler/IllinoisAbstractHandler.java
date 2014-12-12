package edu.illinois.cs.cogcomp.annotation.handler;

import org.apache.thrift.TException;

/**
 * Abstract class for the handlers
 */
public class IllinoisAbstractHandler {

    public String sub_class_name;
    public String sub_class_version;
    public String sub_class_identifier;
    
    /**
     * CRITICAL: identifier and version will be concatenated (separated by "-") to 
     *   create the resource name used to check whether the component version
     *   has changed. These values MUST be consistent with the name assigned
     *   by the tool to the record view identifier. 
     */
    
    public IllinoisAbstractHandler(String name, String version, String identifier){
        sub_class_name = name;
        setVersion( version );
	setName( identifier);
	setIdentifier( identifier );
    }


    public IllinoisAbstractHandler(String name )
    {
        sub_class_name = name;
        sub_class_version = null;
        sub_class_identifier = name;
    }
    
    
    public void setVersion( String version ) 
    {
	sub_class_version = version;
        return;
    }

    public void setName( String name ) 
    {
	sub_class_name = name;
        return;
    }

    public void setIdentifier( String identifier )
    {
	sub_class_identifier = identifier;
	return;
    }

    public boolean ping() throws TException {
        return true;
    }

    public String getName() throws TException {
        return sub_class_name;
    }

    public String getVersion() throws TException {
        return sub_class_version;
    }

    public String getSourceIdentifier() throws TException {
        return sub_class_identifier + "-" + getVersion();
    }
}
