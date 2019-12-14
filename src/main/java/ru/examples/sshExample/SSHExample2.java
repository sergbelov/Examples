package ru.examples.sshExample;

import com.jcraft.jsch.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SSHExample2 {

    public static void main(String[] args) {

        int port = 22;
        String host = "grid520";
        String user = "user";
        String password = "pass";

        String remoteFile = "/opt/pprb/logs/cm-module.log";

        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
//            System.out.println("Establishing Connection...");
            session.connect();
//            System.out.println("Connection established.");
//            System.out.println("Creating SFTP Channel.");
            ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
//            System.out.println("SFTP Channel created.");
            InputStream out = null;
            out = sftpChannel.get(remoteFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(out));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            br.close();
            sftpChannel.disconnect();
            session.disconnect();
        } catch (JSchException | SftpException | IOException e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }

}
