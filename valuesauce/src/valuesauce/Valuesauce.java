/*
* Revices tunnels from sleetlocust agents. Agreagates SNMP get requests for SNMP targets, 
* determining the minimum interval, makes the GET requests itself and cache the result.
* Subsequent GET resquests from sleetlocust agents are served the most recent cahed result
* for that specific target.
*/
package valuesauce;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import solidtea.Solidtea;
import solidtea.DatabaseEngine;
import solidtea.objects.DBConnection;

import java.util.logging.Logger;

/**
 *
 * @author snr
 */
public class Valuesauce implements Solidtea { 
    private final String _CLASS;
    private Properties _psProps, _sysProps;
    private DatabaseEngine _dEngine;
    
    DBConnection dbcon;
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Valuesauce vs = new Valuesauce("settings.properties", "v6macassoc/preparedstatements.properties");
    }
    
    public Valuesauce(String propsStr, String psRBStr) {
        this._CLASS = this.getClass().getName();
        
        Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));
        
        try {
            _sysProps = this.loadPropsFromFile(settingsTxt, true);
            _psProps =  this.loadPropsFromFile(psTxt, false);
            
            //assignSystemVariables();
            createDBConnection(this.getSysProperty("sql_server_ip_addr"), this.getSysProperty("sql_server_username"), this.getSysProperty("sql_server_password"));
            
            _dEngine = new DatabaseEngine(this, dbcon);
            
            this.runAsDaemon(30);
            
        } catch (IOException ioe) { System.out.println(_CLASS+"/"+ioe); }
        
    }
    
    private void runAsDaemon(int interval) {
        while(true) {
            _dEngine.execute();
            try {
                Thread.sleep(interval);
            } catch(InterruptedException ie) {
                System.out.println(_CLASS+"/runAsDaemon - exception");
                ie.printStackTrace();
            }
        }
    }
    
    public void shutdownThreads() {
        System.out.println(_CLASS+"/shutdownThreads - starting");
        _dEngine.shutdown();
    }
    
    private void createDBConnection(String ip, String u, String p) {
        dbcon = new DBConnection(ip, u, p, _psProps);
    }
    
    
    private Properties loadPropsFromFile(String p1, boolean external) {
        System.out.println(_CLASS+"/loadPropsFromFile - attempting to load "+p1);
        Properties tmp_prop = new java.util.Properties();
        InputStream in = null;

        try {
            if(external) 
                in = new FileInputStream(p1);
            else
                in = this.getClass().getClassLoader().getResourceAsStream(p1);
                
            if (in == null) {
                System.out.println(_CLASS+"/loadPropsFromFile - "+p1+ " not found!!!");
                tmp_prop = null;
            } else {
                tmp_prop.load(in);
            }
        } catch(IOException ioe) { System.out.println(_CLASS+"/loadPropsFromFile - "+ioe); }

        return tmp_prop;
    }

    public String getSysProperty(String arg) throws IOException {
        System.out.println(_CLASS+"/getSysProperty - "+arg);
        String s;
        if(_sysProps==null) {
            throw new IOException(_CLASS+"/getSysProperty - Props file not loaded!");
        } else {
            s = _sysProps.getProperty(arg);
            if(s==null)
                throw new IOException(_CLASS+"/getSysProperty - Null value. Does field exist??");
            
            System.out.println(_CLASS+"/getSysProperty - value is "+s);
            return s;
        }
    }
    
    public Object saveSysProperty(String key, String value) { return _sysProps.setProperty(key, value); }

    @Override
    public void log(Level level, String sourceClass, String sourceMethod, String message) {
        myLogger.logp(level, sourceClass, sourceMethod, message);
    }

    @Override
    public void log(Level level, String sourceClass, String sourceMethod, Exception e) {
        this.exceptionEncountered(level, sourceClass, sourceMethod, e);
    }
    
    /**
     * Lets handle some exceptions in imaginative and useful ways!
     * 
     * @param level
     * @param sourceClass
     * @param sourceMethod
     * @param e
     */
    public void exceptionEncountered(Level level, String sourceClass, String sourceMethod, Exception e) {
        if(e instanceof java.sql.SQLException) {
             java.sql.SQLException sqle = (java.sql.SQLException)e;
             String message = "SQLException: " + sqle.getMessage() +" /n"+"SQLState: " + sqle.getSQLState()+" /n"+"VendorError: " + sqle.getErrorCode();
             
             myLogger.logp(level, sourceClass, sourceMethod, message);
        } else if(e instanceof java.io.IOException) {
            java.io.IOException ioe = (java.io.IOException)e;
            myLogger.logp(level, sourceClass, sourceMethod, ioe.getMessage());
        } else {
            myLogger.logp(level, sourceClass, sourceMethod, e.toString());
        }
    }
    
}
