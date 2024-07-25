/**
* 
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/
package ch.epfl.gsn.networking.zeromq;


public class UploadCommandData {
    private String vsname;
    private String cmd;
    private String[] paramNames;
    private String[] paramValues;
    private CommandFile[] commandFiles;

    public UploadCommandData() {
        this.vsname = "";
        this.cmd = "";
        this.paramNames = new String[0];
        this.paramValues = new String[0];
        this.commandFiles = new CommandFile[0];
    }

    public UploadCommandData(String vsname, String cmd, String[] paramNames, String[] paramValues, CommandFile[] commandFiles) {
        this.vsname = vsname;
        this.cmd = cmd;
        this.paramNames = paramNames;
        this.paramValues = paramValues;
        this.commandFiles = commandFiles;
    }

    public String toString(){
        StringBuilder s =new StringBuilder().append("UploadcommandData for "+ this.vsname+ ":" + this.cmd + "\n");
        for (int i = 0; i < paramNames.length; i++) {
            s.append("Param Name: " + paramNames[i] + ", Param Value: " + paramValues[i] +"\n");
        }
        return s.toString();
    }
    public String getVsname() {
        return vsname;
    }

    public void setVsname(String vsname) {
        this.vsname = vsname;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String[] getParamNames() {
        return paramNames;
    }

    public void setParamNames(String[] paramNames) {
        this.paramNames = paramNames;
    }

    public String[] getParamValues() {
        return paramValues;
    }

    public void setParamValues(String[] paramValues) {
        this.paramValues = paramValues;
    }

    public CommandFile[] getCommandFiles() {
        return commandFiles;
    }

    public void setCommandFiles(CommandFile[] commandFiles) {
        this.commandFiles = commandFiles;
    }
}