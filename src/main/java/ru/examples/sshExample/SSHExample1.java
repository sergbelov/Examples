package ru.examples.sshExample;

import com.jcraft.jsch.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SSHExample1 {
    private static final int SSH_PORT = 22;
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int BUFFER_SIZE = 1024;

    private static final String HOSTNAME = "grid520";
    private static final String USERNAME = "user";
    private static final String PASSWORD = "pass";

    public static void main(String[] args) {
        SshManager manager = new SshManager();
        List<String> lines = manager.connectAndExecuteListCommand(HOSTNAME, USERNAME, PASSWORD);
        System.out.println(lines);
    }

    static class SshManager {
        public List<String> connectAndExecuteListCommand (String host, String username, String password){
        List<String> lines = new ArrayList<String>();
        try {
//            String command = "ls -1\n";
//            String command = "ls /opt/pprb/logs/\n";
            String command = "find /opt/pprb/logs/ -iname 'cm-module*'\n";
//            String command = "less /opt/pprb/logs/cm-module.log\n";
            Session session = initSession(host, username, password);
            Channel channel = initChannel(command, session);
            InputStream in = channel.getInputStream();
            channel.connect();
            String dataFromChannel = getDataFromChannel(channel, in);
            lines.addAll(Arrays.asList(dataFromChannel.split("\n")));
            channel.disconnect();
            session.disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
        return lines;
    }

    private Session initSession(String host, String username, String password) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, SSH_PORT);
        session.setPassword(password);
        UserInfo userInfo = new MyUserInfo();
        session.setUserInfo(userInfo);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(CONNECTION_TIMEOUT);
        return session;
    }

    public class MyUserInfo implements UserInfo {

        private String password;

        public void showMessage(String message) {
            System.out.println(message);
        }

        public boolean promptYesNo(String message) {
            System.out.println(message);
            return true;
        }

        @Override
        public String getPassphrase() {
            return null;
        }

        @Override
        public String getPassword() {
            return this.password;
        }

        @Override
        public boolean promptPassphrase(String arg0) {
            System.out.println(arg0);
            return true;
        }

        @Override
        public boolean promptPassword(String arg0) {
            System.out.println(arg0);
            this.password = arg0;
            return true;
        }
    }

    private Channel initChannel(String commands, Session session) throws JSchException {
        Channel channel = session.openChannel("exec");
        ChannelExec channelExec = (ChannelExec) channel;
        channelExec.setCommand(commands);
        channelExec.setInputStream(null);
        channelExec.setErrStream(System.err);
        return channel;
    }

    private String getDataFromChannel(Channel channel, InputStream in)
            throws IOException {
        StringBuilder result = new StringBuilder();
        byte[] tmp = new byte[BUFFER_SIZE];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, BUFFER_SIZE);
                if (i < 0) {
                    break;
                }
                result.append(new String(tmp, 0, i));
            }
            if (channel.isClosed()) {
                int exitStatus = channel.getExitStatus();
                System.out.println("exit-status: " + exitStatus);
                break;
            }
            trySleep(1000);
        }
        return result.toString();
    }

    private void trySleep(int sleepTimeInMilliseconds) {
        try {
            Thread.sleep(sleepTimeInMilliseconds);
        } catch (Exception e) {
        }
    }
}

}
