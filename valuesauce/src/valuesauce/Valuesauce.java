/*
* Revices tunnels from sleetlocust agents. Agreagates SNMP get requests for SNMP targets, 
* determining the minimum interval, makes the GET requests itself and cache the result.
* Subsequent GET resquests from sleetlocust agents are served the most recent cahed result
* for that specific target.
*/
package valuesauce;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import solidtea.Solidtea;

import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author snr
 */
public class Valuesauce implements Solidtea { 
    private final String _class;
    private Logger myLogger;
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
    
    public Valuesauce() {
        this._class = this.getClass().getName();
    }
    
    private void initComponents() {
        System.out.println(_class+"/initComponents - entered");
        try {
            sysProps = this.loadPropsFromFile(propsStr, true);
            psProps = this.loadPropsFromFile(psRBStr, false); 

            if(sysProps != null) {
                myLogger.addHandler(new FileHandler("%t/"+myLogger.getName()));
                dbCon2 = new dbConnection2(this, sysProps);
                
                this.setSize(Integer.parseInt(getSysProperty("sizeX")), Integer.parseInt(getSysProperty("sizeY")));
                this.createdbConnection(getSysProperty("jdbc.server"), getSysProperty("jdbc.username"), getSysProperty("jdbc.password"));
                if(getSysProperty("jdbcApp.displayExceptions").equals("true") )
                    this.displayExceptions = true;
                
                //myLogger.addHandler(new FileHandler("%t/"+myLogger.getName()));
                
                //dbCon2 = new dbConnection2(this, sysProps);
            } else {
                log(Level.INFO, _class, "initComponents", "system props not loaded, so using defaults.");
            }
            //assignSystemVariables();
        } catch (IOException ioe) { System.out.println(ioe); }


        content = this.getContentPane();
        content.setLayout(new BorderLayout(2, 2));

        sBar = new statusBar(this);
        sBar.setSize(0,10);

        content.add(sBar, BorderLayout.SOUTH);

        WindowListener l = new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                closeApp();
            }
        };
        this.addWindowListener(l);
    }
    
    

    @Override
    public Properties loadPropsFromFile(String p1, boolean external) {
        log(Level.INFO, _class, "loadPropsFromFile", "Attempting to load "+p1);
        Properties tmp_prop = new java.util.Properties();
        InputStream in = null;

        try {
            if(external) 
                in = new FileInputStream(p1);
            else
                in = this.getClass().getClassLoader().getResourceAsStream(p1);
                
            if (in == null) {
                log(Level.INFO, _class, "loadPropsFromFile", p1+" not found!!!");
                tmp_prop = null;
            } else {
                tmp_prop.load(in);
            }
        } catch(IOException ioe) { log(Level.SEVERE, _class, "loadPropsFromFile", ioe); }

        return tmp_prop;
    }

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
        
        if(displayExceptions)
            JOptionPane.showMessageDialog(null, sourceClass+"/"+sourceMethod+"\n"+e.toString(), "Exception", JOptionPane.ERROR_MESSAGE);
    }
    
}
