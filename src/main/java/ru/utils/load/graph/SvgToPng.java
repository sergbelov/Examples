package ru.utils.load.graph;

//import org.apache.batik.transcoder.TranscoderException;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
//import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.Paths;

public class SvgToPng {
    private static final Logger LOG = LoggerFactory.getLogger(SvgToPng.class);

    public boolean convert(String svgFile) {
        return convert(svgFile,
                svgFile.replace(".SVG", ".png")
                        .replace(".svg", ".png"));
    }

    public boolean convert(String svgFile, String pngFile) {
        LOG.info("Конвертация {} в {}", svgFile, pngFile);
        String svgUrl = null;
        try {
            svgUrl = Paths.get(svgFile).toUri().toURL().toString();
        } catch (MalformedURLException e) {
            LOG.error("Ошибка при чтении файла {}\n", svgFile, e);
            return false;
        }

        TranscoderInput svgImage = new TranscoderInput(svgUrl);

        try (OutputStream outputStream = new FileOutputStream(pngFile)) {
            TranscoderOutput transcoderOutput = new TranscoderOutput(outputStream);
            PNGTranscoder pngTranscoder = new PNGTranscoder();
            pngTranscoder.transcode(svgImage, transcoderOutput);
            outputStream.flush();
        } catch (Exception e) {
            LOG.error("Ошибка при конвертации файла {} в {}\n", svgFile, pngFile, e);
            return false;
        }

        return true;
    }
}
