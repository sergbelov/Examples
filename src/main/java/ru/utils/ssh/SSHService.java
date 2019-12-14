package ru.utils.ssh;

import com.jcraft.jsch.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SSHService {

    private static final Logger LOG = LogManager.getLogger();
    private DateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final int CONNECTION_TIMEOUT = 10000;
    private final int BUFFER_SIZE = 1024;
    private Session session;

    public boolean connect(
        String host,
        int port,
        String userName,
        String password) {

        this.session = getSession(
                host,
                port,
                userName,
                password);

        return this.session != null;
    }

    public void disconnect(){
        this.session.disconnect();
        this.session = null;
    }

    public Session getSession(
            String host,
            int port,
            String userName,
            String password) {

        LOG.debug("##### SSH connection: {} {} {}", host, port, userName);
        JSch jsch = new JSch();
        Session session = null;
        try {
            session = jsch.getSession(userName, host, port);
/*
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
*/
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(password);
            session.connect(CONNECTION_TIMEOUT);
        } catch (JSchException e) {
            LOG.error(e);
        }
        LOG.debug("SSH connected: {} {} {}", host, port, userName);
        return session;
    }

    public ChannelSftp getChannelSftp() {
        return getChannelSftp(this.session);
    }

    public ChannelSftp getChannelSftp(Session session) {
        ChannelSftp channel = null;
        try {
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
        } catch (JSchException e) {
            LOG.error(e);
        }
        return channel;
    }

    public Channel getChannel(String commands) {
        return getChannel(this.session, commands);
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
            LOG.error(e);
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
            sleep(1000);
        }
        return result.toString();
    }

    private void sleep(int sleepTimeInMilliseconds) {
        try {
            Thread.sleep(sleepTimeInMilliseconds);
        } catch (Exception e) {
        }
    }

    public String getDateModifyFile(String file) {
        return getDateModifyFile(this.session, file);
    }

    public String getDateModifyFile(Session session, String file) {
        String r = null;
        List<String> fileStat = new ArrayList<>(getDataList(session, "stat " + file));
        LOG.trace("Файл инфо: {}\n{}", file, fileStat);

        for (int i = 0; i < fileStat.size(); i++) {
            if (fileStat.get(i).startsWith("Modify: ")) {
                r = fileStat.get(i).substring(8, 18);
                break;
            }
        }
        return r;
    }

    public boolean getBetweenDateTimeFile(
            String file,
            long start,
            long stop) {

        return getBetweenDateTimeFile(
                this.session,
                file,
                start,
                stop);
    }

    public boolean getBetweenDateTimeFile(
            Session session,
            String file,
            long start,
            long stop) {

        long access = 0L, modify = 0L;
        List<String> fileStat = new ArrayList<>(getDataList(session, "stat " + file));
        LOG.trace("Файл инфо: {}\n{}", file, fileStat);

        Pattern pattern = Pattern.compile("(Access: |Modify: )([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2})");
        Matcher matcher;

        for (int i = 0; i < fileStat.size(); i++) {
            matcher = pattern.matcher(fileStat.get(i));
            if (matcher.find() && matcher.group(2) != null){
                if (fileStat.get(i).startsWith("Modify: ")) {
                    try {
                        modify = sdf2.parse(fileStat.get(i).substring(8, 27)).getTime();
                    } catch (ParseException e) {
                        LOG.warn("Ошибка при получении даты {} {}", fileStat.get(i), e);
                    }
                } else {
                    try {
                        access = sdf2.parse(fileStat.get(i).substring(8, 27)).getTime();
                    } catch (ParseException e) {
                        LOG.warn("Ошибка при получении даты {} {}", fileStat.get(i), e);
                    }
                }
            }
        }

//        return access < stop && modify > start;
        return modify > start;
    }

    public List<String> getDataList(String command) {
        return getDataList(this.session, command);
    }

    public List<String> getDataList(Session session, String command) {

// ls /opt/logs/my-module_2019-10-30*.log /opt/logs/my-module_2019-10-30*.zip /opt/logs/my-module.log -rt --full-time
// find /opt/logs/ -name 'my-module*' -mtime 0

        List<String> dataList = new ArrayList<String>();
        try {
            Channel channel = getChannel(session, command);
            InputStream in = channel.getInputStream();
            channel.connect();
            String dataFromChannel = getDataFromChannel(channel, in);
            dataList.addAll(Arrays.asList(dataFromChannel.split("\n")));
            channel.disconnect();
        } catch (Exception e) {
            LOG.error(e);
        }
        for (int i = 0; i < dataList.size(); i++){
            LOG.trace("{}", dataList.get(i));
        }
        return dataList;
    }

    public void readFile(String fileName) {
        readFile(this.session, fileName);
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

        readFile(session, fileName);
        session.disconnect();
    }

    public void readFile(
            Session session,
            String fileName) {

        if (session != null) {
            ChannelSftp channel = getChannelSftp(session);
            if (channel != null) {
                try (
                        InputStream out = channel.get(fileName);
                        BufferedReader br = new BufferedReader(new InputStreamReader(out))
                ) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        LOG.debug(line);
                    }
                    br.close();
                    out.close();
                } catch (IOException e) {
                    LOG.error(e);
                } catch (SftpException e) {
                    LOG.error(e);
                }

                channel.disconnect();
            }
        }
    }

    public void copyFileFromSFTP(
            String sourceFileName,
            String targetPathName,
            boolean onlyNew) {

        copyFileFromSFTP(
                this.session,
                sourceFileName,
                targetPathName,
                onlyNew);
    }

    public void copyFileFromSFTP(
            Session session,
            String sourceFileName,
            String targetPathName,
            boolean onlyNew) {

        String fileName = sourceFileName.substring(sourceFileName.lastIndexOf("/") + 1);
        File targetFile = new File(targetPathName, fileName);
        if (!targetFile.exists() || !onlyNew) {
            LOG.info("копирование {} -> {}", sourceFileName, targetPathName);

            File targetPath = new File(targetPathName);
            if (!targetPath.exists()) {
                targetPath.mkdirs();
            }

            ChannelSftp channelSftp = getChannelSftp(session);
            try {
                channelSftp.get(sourceFileName, targetPath.toString());
            } catch (SftpException e) {
                LOG.error(e);
            }
            channelSftp.disconnect();
        }
    }

}
