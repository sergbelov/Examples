package ru.examples.sshExample;

import com.jcraft.jsch.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SSHExample {

    public static void main(String[] args) {

        int portSSH = 22;
        String host = "grid520";
        String userName = "user";
        String password = "pass";

        String remoteFile = "/opt/pprb/logs/cm-module.log";

        List<String> arhiveList = new ArrayList<>();

        SSHService sshService = new SSHService();
        Session session = sshService.getSession(
                host,
                portSSH,
                userName,
                password);

        String targetPath = "D:/TEMP/logs/";
        if (session != null) {
            ChannelSftp channelSftp = sshService.getChannelSftp(session);
            if (channelSftp != null) {
                List<String> filesList = new ArrayList<>(sshService.getDataList(session, "find /opt/pprb/logs/ -iname 'cm-module*'"));
                System.out.println("==================");
                filesList
                        .stream()
                        .filter(x -> x.endsWith(".log")||x.endsWith(".zip"))
                        .forEach(x -> {
                            System.out.println(x);
                            if (x.endsWith(".zip")) {
                                File targetFile = new File(targetPath, x.substring(x.lastIndexOf("/")+1));
                                if (!targetFile.exists()) {
                                    System.out.println("Копируем : " + targetFile);
                                        try {
                                            channelSftp.get(x, targetPath);
                                        } catch (SftpException e) {
                                            e.printStackTrace();
                                        }
                                }
                            }
                        });

/*
                try (
                        InputStream out = channel.get(remoteFile);
                        BufferedReader br = new BufferedReader(new InputStreamReader(out))
                ) {
                    String line;
                    while ((line = br.readLine()) != null) {
//                        System.out.println(line);
                    }
                    br.close();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SftpException e) {
                    e.printStackTrace();
                }
*/

                channelSftp.disconnect();
            }
            session.disconnect();
//            arhiveList.stream().forEach(x -> sshService.extractFilesFromArhive(x, targetPath + "extract/"));
//            sshService.extractFilesFromArhive("D:/TEMP/logs/cm-module_2018-05-23_0.log.zip", targetPath + "extract/");
        }
    }

    static class SSHService {
        private final int CONNECTION_TIMEOUT = 10000;
        private final int BUFFER_SIZE = 1024;

        public Session getSession(
                String host,
                int port,
                String userName,
                String password) {

            JSch jsch = new JSch();
            Session session = null;
            try {
                session = jsch.getSession(userName, host, port);
                session.setPassword(password);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect(CONNECTION_TIMEOUT);
            } catch (JSchException e) {
                e.printStackTrace();
            }
            return session;
        }

        public ChannelSftp getChannelSftp(Session session) {
            ChannelSftp channel = null;
            try {
                channel = (ChannelSftp) session.openChannel("sftp");
                channel.connect();
            } catch (JSchException e) {
                e.printStackTrace();
            }
            return channel;
        }

        public Channel getChannel(Session session, String commands) {
            Channel channel = null;
            try {
                channel = session.openChannel("exec");
                ChannelExec channelExec = (ChannelExec) channel;
                channelExec.setCommand(commands);
                channelExec.setInputStream(null);
                channelExec.setErrStream(System.err);
            } catch (JSchException e) {
                e.printStackTrace();
            }
            return channel;
        }

        private String getDataFromChannel(Channel channel, InputStream in) throws IOException {
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
//                System.out.println("exit-status: " + exitStatus);
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

        public List<String> getDataList(Session session, String command) {
            List<String> files = new ArrayList<String>();
            try {
                Channel channel = getChannel(session, command);
                InputStream in = channel.getInputStream();
                channel.connect();
                String dataFromChannel = getDataFromChannel(channel, in);
                files.addAll(Arrays.asList(dataFromChannel.split("\n")));
                channel.disconnect();
//            session.disconnect();
            } catch (Exception e) {
                System.out.println(e);
            }
            return files;
        }

        public void readFile(
                String host,
                int port,
                String userName,
                String password,
                String fileName) {

            Session session = getSession(
                    host,
                    port,
                    userName,
                    password);

            if (session != null) {
                ChannelSftp channel = getChannelSftp(session);
                if (channel != null) {
                    try (
                            InputStream out = channel.get(fileName);
                            BufferedReader br = new BufferedReader(new InputStreamReader(out))
                    ) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            System.out.println(line);
                        }
                        br.close();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (SftpException e) {
                        e.printStackTrace();
                    }

                    channel.disconnect();
                }
                session.disconnect();
            }
        }
    }
}
