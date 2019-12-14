package ru.examples.guiExample.ReportExample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

public class FormProgressBarEngine implements MouseListener {

    static final Logger LOG = LogManager.getLogger();

    private FormProgressBar formProgressBar;

    FormProgressBarEngine(FormProgressBar formProgressBar) {
        this.formProgressBar = formProgressBar;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
/*
        LOG.info("\n{}\n{}\n",
                e.getComponent(),
                formProgressBar.getJLabels(0),
                formProgressBar.getJLabels(1));
*/

        if (e.getComponent().equals(formProgressBar.getJLabels(2))) {
            openFile("sample_excel.xlsx");
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }


    public void openFile(String fileSting) {
        File file = new File(fileSting);
        String cmd = String.format("cmd.exe /C start %s", file.getAbsolutePath());
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            LOG.error(e);
        }
    }

}

